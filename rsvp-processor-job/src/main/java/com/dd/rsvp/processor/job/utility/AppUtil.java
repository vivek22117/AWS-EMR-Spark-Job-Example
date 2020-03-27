package com.dd.rsvp.processor.job.utility;

import com.dd.rsvp.processor.job.exception.ApplicationException;
import org.apache.log4j.Logger;

import java.io.Serializable;
import java.util.List;

public class AppUtil implements Serializable {

    private static Logger logger = Logger.getLogger(AppUtil.class);

    public static String SPARK_CONF_PATH = "conf/spark-config.properties";

    public static int getValueAsInt(Object input) {
        if (!isNullOrEmptyAfterTrim(input)) {
            return Integer.valueOf(input.toString());
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
}
