package com.dd.rsvp.processor.job.utility;

import com.dd.rsvp.processor.job.exception.ApplicationException;
import org.apache.log4j.Logger;
import org.apache.spark.sql.*;

import java.util.Map;

import static com.dd.rsvp.processor.job.utility.ApplicationEnum.SPARK_SESSION;
import static com.dd.rsvp.processor.job.utility.ApplicationEnum.SPARK_SESSION_ERROR;

public class SharedSparkSession {

    private static Logger LOGGER = Logger.getLogger(SharedSparkSession.class);

    private SharedSparkSession() {
    }

    public static SparkSession createSession(String appName, Map<String, String> configMap) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(SPARK_SESSION.getMessage());
        }
        SparkSession.Builder builder = SparkSession.builder();
        if (appName != null && !appName.isEmpty()) {
            builder.appName(appName);
        }
        if (configMap != null && !configMap.isEmpty()) {
            for (Map.Entry<String, String> entry : configMap.entrySet()) {
                builder.config(entry.getKey(), entry.getValue());
            }
        }
        return builder.getOrCreate();
    }

    public static SQLContext createSqlContext(SparkSession sparkSession) throws ApplicationException {
        if (sparkSession != null) {
            return sparkSession.sqlContext();
        }
        throw new ApplicationException(SPARK_SESSION_ERROR.getMessage());
    }

    public static String createS3FilePath(String s3Key) {
        String bucketName = PropertyLoader.getInstance().getPropertyValue("S3.BUCKET.NAME");
        StringBuilder pathBuilder = new StringBuilder();
        pathBuilder.append("s3a://");
        pathBuilder.append(bucketName);
        pathBuilder.append(s3Key);
        return pathBuilder.toString();
    }
}
