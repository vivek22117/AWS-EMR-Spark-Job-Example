package com.dd.rsvp.processor.job.service;

import org.apache.log4j.Logger;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.PairFunction;
import org.apache.spark.api.java.function.VoidFunction;
import org.apache.spark.broadcast.Broadcast;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.util.LongAccumulator;
import scala.Tuple2;

import java.io.Serializable;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class RSVPS3PersisterService implements Serializable {
    private static Logger logger = Logger.getLogger(RSVPS3PersisterService.class);

    private static final DateTimeFormatter SYSTEM_TIME_FORMAT = new DateTimeFormatterBuilder().appendInstant(3).toFormatter();
    private static final DateTimeFormatter TEMPORAL_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy/MM/dd/HH/mm/ss/SSS").withZone(ZoneOffset.UTC);
    private static final DateTimeFormatter UTC_ISO_WITHOUT_SPECAILCHARACTERS_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmssSSS'Z'").withZone(ZoneOffset.UTC);


    public void readMergedCSVAndPerformMigration(SparkSession session, String mergedDatasetS3Key,
                                                 Broadcast<Map> broadCastForOrgIds, String jobId,
                                                 LongAccumulator recordsCount, LongAccumulator dbRecordsCount,
                                                 LongAccumulator s3KeysCount) {
        Dataset<Row> mergedDataset = session.read()
                .option("inferSchema", "true")
                .option("header", "true")
                .csv(mergedDatasetS3Key);


        JavaRDD<Object> object1JavaRDD = mergedDataset.javaRDD().map(new Function<Row, Object>() {
            @Override
            public Object call(Row row) throws Exception {
                Object obj = new Object();
                System.out.println("Process Object");
                return obj;
            }
        });

        JavaRDD<Object> object2JavaRDD = object1JavaRDD.map(new Function<Object, Object>() {
            @Override
            public Object call(Object epoch) throws Exception {
                Object newObject = new Object();
                System.out.println("Process newObject");
                return newObject;
            }
        });

        mergedDataset.unpersist();

        JavaRDD<Object> object3JavaRdd = object2JavaRDD.map(new Function<Object, Object>() {
            @Override
            public Object call(Object modeleon) throws Exception {
                Object object3 = new Object();
                System.out.println("Process object3");
                return object3;
            }
        });

        object1JavaRDD.unpersist();

        JavaPairRDD<String, Iterable<Object>> stringIterableJavaPairRDD =
                object3JavaRdd.mapToPair(new PairFunction<Object, String, Object>() {
                    @Override
                    public Tuple2<String, Object> call(Object obj) throws Exception {
                        String key = "rsvp-" + "eventId";
                        return new Tuple2<>(key, obj);
                    }
                }).groupByKey();

        logger.debug("SPARK JOB FINISHED!");
        object2JavaRDD.unpersist();

        stringIterableJavaPairRDD.foreach(new VoidFunction<Tuple2<String, Iterable<Object>>>() {
            @Override
            public void call(Tuple2<String, Iterable<Object>> stringIterableTuple2) throws Exception {
                List<Object> rsvpRecordList = new ArrayList<Object>();
                stringIterableTuple2._2().forEach(new Consumer<Object>() {
                    @Override
                    public void accept(Object apiRecord) {
                        rsvpRecordList.add(apiRecord);
                    }
                });
                recordsCount.add(rsvpRecordList.size());
            }
        });

        logger.debug("MIGRATION JOB FINISHED!");
    }
}
