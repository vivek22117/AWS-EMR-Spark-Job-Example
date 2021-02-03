package com.dd.ria.processor;

import com.dd.ria.processor.handler.Combiner;
import com.dd.ria.processor.utility.AppUtil;
import com.dd.ria.processor.utility.PropertyLoader;
import com.dd.ria.processor.utility.SharedSparkSession;
import org.apache.log4j.Logger;
import org.apache.spark.sql.*;
import org.apache.spark.util.LongAccumulator;
import scala.Option;

import java.util.Map;

import static com.dd.ria.processor.handler.Combiner.SEPARATOR;
import static org.apache.spark.sql.functions.col;
import static org.apache.spark.sql.functions.to_date;

public class RIARecordProcessor {
    private static Logger logger = Logger.getLogger(RIARecordProcessor.class);

    private static final String S3_BUCKET = PropertyLoader.getInstance().getPropertyValue("S3.BUCKET.NAME");
    private static final String DATALAKE_S3_BUCKET = "doubledigit-datalake-qa-us-east-1";
    private static final Map<String, String> sparkConfigMap = PropertyLoader.getInstance().getSparkConfig(AppUtil.SPARK_CONF_PATH);
    private static final String S3_KEY_PREFIX = "s3a:";

    public static void main(String[] args) {
        SparkSession sparkSession = null;
        Combiner combiner = new Combiner();

        try {
            System.out.println(Runtime.getRuntime().availableProcessors());
            logger.debug("S3 Bucket Name: " + S3_BUCKET);
            sparkSession = SharedSparkSession.createSession("RIA-Record-Processor", sparkConfigMap);
            sparkSession.sparkContext().setLogLevel("ERROR");

            SQLContext sqlContext = sparkSession.sqlContext();

            createBroadCastForCommonData(sparkSession, combiner, "doubledigit-aritifactory-qa-us-east-1");

            String concatedS3Key = S3_KEY_PREFIX.concat("splittedS3Key");

            LongAccumulator recordsCount = new LongAccumulator();
            LongAccumulator dbRecordsCount = new LongAccumulator();

            recordsCount.register(sparkSession.sparkContext(), Option.apply("recordsCount"), false);
            dbRecordsCount.register(sparkSession.sparkContext(), Option.apply("dbRecordsCount"), false);

        } catch (Exception ex) {
            logger.error("RSVP processing failed! Please debug.", ex);
        } finally {
            assert sparkSession != null;
            sparkSession.close();
        }
    }

    private static void createBroadCastForCommonData(SparkSession sparkSession, Combiner combiner, String s3Bucket) {

        Dataset<Row> studiesDataset = populateStudiesData(sparkSession, s3Bucket);

        Dataset<Row> nctIdDataset = studiesDataset.select(col("nct_id"));
        combiner.populateOtherFields(sparkSession, nctIdDataset, s3Bucket);

        Dataset<Row> datasetOfOtherFields = readOtherFieldsData(sparkSession);

        Dataset<Row> meshFiledDataset = combiner.processConditionsData(sparkSession, nctIdDataset, s3Bucket);
        Dataset<Row> nameFieldDataset = combiner.processInterventionsData(sparkSession, nctIdDataset, s3Bucket);


        Dataset<Row> combinedDataset = meshFiledDataset.join(nameFieldDataset,
                meshFiledDataset.col("nct_id").equalTo(nameFieldDataset.col("nct_id")), "fullouter")
                .drop(nameFieldDataset.col("nct_id"));

        nctIdDataset.unpersist();

        Dataset<Row> finalDataset = studiesDataset.join(combinedDataset,
                studiesDataset.col("nct_id").equalTo(combinedDataset.col("nct_id")), "fullouter")
                .drop(combinedDataset.col("nct_id"));

        meshFiledDataset.unpersist();
        nameFieldDataset.unpersist();
        combinedDataset.unpersist();

        Dataset<Row> esDataset = finalDataset.join(datasetOfOtherFields,
                finalDataset.col("nct_id").equalTo(datasetOfOtherFields.col("nct_id")), "fullouter")
                .drop(datasetOfOtherFields.col("nct_id"))
                .withColumnRenamed("nct_id", "NCT Number")
                .withColumnRenamed("co_name", "Conditions")
                .withColumnRenamed("in_name", "Interventions");

//        esDataset.repartition(1).write().format("json").save("data/output/merged");

        esDataset.repartition(1)
                .write()
                .format("json")
                .save("hdfs:///temp" + SEPARATOR + "data/output/processed-data");

        esDataset.repartition(1)
                .write()
                .format("json")
                .save("s3a://" + DATALAKE_S3_BUCKET  + "/rsvp/processed-data-by-s3");
    }

    private static Dataset<Row> populateStudiesData(SparkSession sparkSession, String s3Bucket) {
//        Dataset<Row> riaStudiesCollection = AppUtil.readData(sparkSession, "ria-data-processor/src/main/resources/data/studies.txt");
        Dataset<Row> riaStudiesCollection = AppUtil.readData(sparkSession, "s3a://" + s3Bucket + "/drugs-data/studies.txt");

        Dataset<Row> tempRiaStudiesData = riaStudiesCollection.select(col("nct_id"),
                col("overall_status").alias("Status"), col("study_type").alias("Study Type"), col("phase").alias("Phase"),
                col("acronym").alias("Trial Acronym"), col("enrollment").alias("Number Enrolled"),
                to_date(col("start_date")).alias("Study Start"),
                to_date(col("study_first_posted_date")).alias("First Posted"),
                to_date(col("last_update_posted_date")).alias("Last Update Posted"),
                to_date(col("results_first_posted_date")).alias("Results First Posted"),
                to_date(col("primary_completion_date")).alias("Primary Completion"),
                to_date(col("completion_date")).alias("Study Completion"))
                .na().fill("N/A", new String[]{"Phase", "Trial Acronym", "Results First Posted"});

        return tempRiaStudiesData;
    }

    private static Dataset<Row> readOtherFieldsData(SparkSession sparkSession) {
        Dataset<Row> otherFieldDataset = sparkSession.read()
                .option("inferSchema", "true")
                .option("header", "true")
//                .json("data/output/other-fields/part-*.json")
                .json("hdfs:///temp" + SEPARATOR +"data/output/other-fields/part-*.json")
                .repartition(col("nct_id"))
                .na().fill("N/A", new String[]{"Locations", "Gender", "Other Ids",
                        "Sponsors/Collaborator", "Outcome Measures"});
        return otherFieldDataset;
    }


}
