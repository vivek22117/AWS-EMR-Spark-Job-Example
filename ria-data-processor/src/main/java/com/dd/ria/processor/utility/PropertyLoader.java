package com.dd.ria.processor.utility;

import com.dd.ria.processor.exception.ApplicationException;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class PropertyLoader implements Serializable {

    private static Logger LOGGER = Logger.getLogger(PropertyLoader.class);
    private static PropertyLoader propertyLoader = null;

    private static final String ENVIRONMENT = "environment";
    private static final String PREFIX = "/application";
    private static final String SUFFIX = ".properties";
    private static Map<String, String> propertiesMap;

    private PropertyLoader() {
    }

    public static PropertyLoader getInstance() {
        if (propertyLoader == null) {
            synchronized (PropertyLoader.class) {
                if (propertyLoader == null) {
                    propertyLoader = new PropertyLoader();
                }
            }
        }
        return propertyLoader;
    }

    public String getPropertyValue(String propertyKey) {
        if (propertiesMap == null) {
            propertiesMap = loadAllProperties();
        }
        return propertiesMap.get(propertyKey);
    }

    private Map<String, String> loadAllProperties() {
        propertiesMap = new HashMap<>();
        String environment = System.getenv(ENVIRONMENT);
        LOGGER.debug("Environment fetched: " + environment);

        if (environment != null) {
            environment = "-" + environment;
            propertiesMap.putAll(loadProperties(PREFIX + environment + SUFFIX));
        }
        return propertiesMap;
    }

    private Map<String, String> loadProperties(String name) {
        Properties properties = new Properties();
        InputStream inputStream = PropertyLoader.class.getResourceAsStream(name);
        try {
            properties.load(inputStream);
        } catch (IOException e) {
            throw new ApplicationException("Unable to load properties.", e);
        }
        return (Map) properties;
    }

    public Map<String, String> getSparkConfig(String path) {
        Properties sparkConfig = new Properties();
        InputStream inputStream = PropertyLoader.class.getResourceAsStream(path);
        try {
            sparkConfig.load(inputStream);
        } catch (IOException e) {
            LOGGER.error("Unable to load spark configuration properties.", e);
        }
        return (Map) sparkConfig;
    }
}
