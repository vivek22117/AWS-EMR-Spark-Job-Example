package com.dd.rsvp.processor.job.spark;

import com.dd.rsvp.processor.job.dao.DynamoDBProcessor;
import com.dd.rsvp.processor.job.utility.AppUtil;
import com.dd.rsvp.processor.job.utility.JobStatusEnum;
import com.dd.rsvp.processor.job.utility.PropertyLoader;
import com.dd.rsvp.processor.job.utility.SharedSparkSession;
import org.apache.log4j.Logger;
import org.apache.spark.api.java.function.PairFunction;
import org.apache.spark.broadcast.Broadcast;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SQLContext;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.util.LongAccumulator;
import scala.Option;
import scala.Tuple2;
import scala.reflect.ClassTag;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.apache.spark.sql.functions.col;

public class RSVPRecordProcessor {
    private static Logger logger = Logger.getLogger(RSVPRecordProcessor.class);

    private static final String S3_KEY_PREFIX = "s3a:";
    private static final String S3_BUCKET = PropertyLoader.getInstance().getPropertyValue("S3.BUCKET.NAME");

    public static void main(String[] args) {
        SparkSession sparkSession = null;
        DynamoDBProcessor dbProcessor = new DynamoDBProcessor();

        List<String> processIdAndS3Key = new ArrayList<>();

        try{
            sparkSession = SharedSparkSession.createSession("RSVP-Record-Migrator", PropertyLoader.getSparkConfig(AppUtil.SPARK_CONF_PATH));
            sparkSession.sparkContext().setLogLevel("DEBUG");

            SQLContext sqlContext = sparkSession.sqlContext();

            processIdAndS3Key = dbProcessor.getProcessIdAndS3Key();
            logger.debug(processIdAndS3Key.toString());

            AppUtil.validateProcessList(processIdAndS3Key);

            //Broadcast list of values
            ClassTag<List> classTagList = scala.reflect.ClassTag$.MODULE$.apply(List.class);
            Broadcast<List> processIdAndS3KeyBroadcast = sparkSession.sparkContext().broadcast(processIdAndS3Key, classTagList);

            String s3DataKeys = processIdAndS3KeyBroadcast.value().get(2).toString();
            String locationForFileA = s3DataKeys.split(",")[0];
            String locationForFileB = s3DataKeys.split(",")[1];

            Broadcast<Map> broadCastForOrgIds = createBroadCastForCommonData(sparkSession, locationForFileA,
                    locationForFileB, S3_BUCKET);

            String mergedDatasetS3Key = processIdAndS3KeyBroadcast.value().get(1).toString();
            String splittedS3Key = mergedDatasetS3Key.split(":")[1];
            String concatedS3Key = S3_KEY_PREFIX.concat(splittedS3Key);

            LongAccumulator recordsCount = new LongAccumulator();
            LongAccumulator dbRecordsCount = new LongAccumulator();

            recordsCount.register(sparkSession.sparkContext(), Option.apply("recordsCount"), false);
            dbRecordsCount.register(sparkSession.sparkContext(), Option.apply("dbRecordsCount"), false);

        }catch (Exception ex){
            logger.error("RSVP processing failed! Please debug.", ex);
            updateDynamoDBStatus(processIdAndS3Key, dbProcessor, JobStatusEnum.RUNNING, null, null, null);
        } finally {
            assert sparkSession != null;
            sparkSession.close();
        }
    }

    private static Broadcast<Map> createBroadCastForCommonData(SparkSession sparkSession, String locationForFileA,
                                                               String locationForFileB, String s3Bucket) {

        Dataset<Row> rsvpCollection = sparkSession.read()
                .option("inferSchema", "true")
                .option("header", "true")
                .csv("s3a://" + s3Bucket + locationForFileA);

        Dataset<Row> rsvpRecordData = sparkSession.read()
                .option("inferSchema", "true")
                .option("header", "true")
                .csv("s3a://" + s3Bucket + locationForFileB);

        Map<Integer, String> machIdAndOrgIdMap = rsvpCollection.join(rsvpRecordData,
                rsvpCollection.col("RSVP_ID").equalTo(rsvpRecordData.col("RSVP_ID")), "full_outer")
                .select(rsvpCollection.col("EVENT_ID"), rsvpRecordData.col("EVENT_ORG_ID"))
                .filter(col("EVENT_ID").isNotNull().and(col("EVENT_ORG_ID").isNotNull()))
                .javaRDD().mapToPair(new PairFunction<Row, Integer, String>() {
                    @Override
                    public Tuple2<Integer, String> call(Row row) throws Exception {
                        return new Tuple2<>(AppUtil.getValueAsInt(row.get(0)), row.get(1).toString());
                    }
                }).collectAsMap();

        ClassTag<Map> classTagDataset = scala.reflect.ClassTag$.MODULE$.apply(Map.class);
        return sparkSession.sparkContext().broadcast(machIdAndOrgIdMap, classTagDataset);
    }

    private static void updateDynamoDBStatus(List<String> jobs, DynamoDBProcessor dynamoOperation,
                                             JobStatusEnum status, Long recordsCount, Long dbRecordsCount, Long s3KeysCount) {
        logger.debug("jobs = [" + jobs + "], dynamoOperation = ["
                + dynamoOperation + "], status = [" + status + "], recordsCount = ["
                + recordsCount + "], dbRecordsCount = [" + dbRecordsCount + "]");
        if (jobs != null && jobs.size() > 1) {
            if (status.toString().equalsIgnoreCase(JobStatusEnum.RUNNING.toString())) {
                dynamoOperation.updateDynamoDBItem(jobs.get(0), JobStatusEnum.PENDING, null, null, null);
            }
            if (status.toString().equalsIgnoreCase(JobStatusEnum.COMPLETED.toString())) {
                dynamoOperation.updateDynamoDBItem(jobs.get(0), JobStatusEnum.COMPLETED, String.valueOf(recordsCount),
                        String.valueOf(dbRecordsCount), String.valueOf(s3KeysCount));
            }
        }
    }
}
