package com.dd.ria.processor.utility;

import com.dd.ria.processor.exception.ApplicationException;
import org.apache.log4j.Logger;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

import static java.nio.file.Files.lines;
import static java.util.stream.Collectors.toList;
import static org.apache.spark.sql.functions.col;

public class AppUtil implements Serializable {
    private static Logger logger = Logger.getLogger(AppUtil.class);

    public static String SPARK_CONF_PATH = "/conf/spark-config.properties";

    public static int getValueAsInt(Object input) {
        if (!isNullOrEmptyAfterTrim(input)) {
            return Integer.parseInt(input.toString());
        }
        return 0;
    }

    public static void validateProcessList(List<String> processes) {
        if (processes == null || processes.isEmpty()) {
            logger.error("No processId to process");
            throw new ApplicationException("no process Id available to process/run");
        }

        if (processes.size() < 2) {
            logger.error("Data not available to process");
            throw new ApplicationException("Data not available to process");
        }

        if (processes.size() != 3) {
            logger.error("Error from dynamoDB result");
            throw new ApplicationException("\"Error from dynamoDB result\"");
        }
    }

    private static boolean isNullOrEmptyAfterTrim(Object input) {
        return input == null || input.toString().trim().length() == 0;
    }

    private static List<String> readFile(String filePath) {

        try (Stream<String> lines = lines(Paths.get(filePath))) {
            List<String> collectedData = lines.collect(toList());
            logger.debug("Number of rows: " + collectedData.size());
            return collectedData;
        } catch (IOException e) {
            throw new RuntimeException("Unable to read and compress.." + e.getMessage());
        }
    }

    public static Dataset<Row> readData(SparkSession sparkSession, String path){
        Dataset<Row> dataset = sparkSession.read()
                .option("inferSchema", "true")
                .option("header", "true")
                .option("delimiter", "|")
                .csv(path).repartition(col("nct_id"));
        return dataset;
    }
}
