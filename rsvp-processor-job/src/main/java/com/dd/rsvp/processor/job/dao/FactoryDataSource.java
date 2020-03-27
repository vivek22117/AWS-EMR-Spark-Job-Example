package com.dd.rsvp.processor.job.dao;

import com.dd.rsvp.processor.job.utility.AWSKMSDecrypter;
import com.dd.rsvp.processor.job.utility.PropertyLoader;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;
import java.io.Serializable;

public class FactoryDataSource implements Serializable {
    private static FactoryDataSource instance;
    private final static int INITIAL_POOL_SIZE = 10;
    private final static int MAX_POOL_SIZE = 50;
    private AWSKMSDecrypter kmsDecrypter = new AWSKMSDecrypter();
    private static HikariDataSource dataSource;

    private FactoryDataSource() {
    }

    public static synchronized FactoryDataSource getInstance() {
        if (instance == null) {
            instance = new FactoryDataSource();
        }
        return instance;
    }

    public synchronized DataSource getDefaultDataSource() {
        if (dataSource == null) {
            HikariConfig config = new HikariConfig();
            config.setUsername(kmsDecrypter.decrypt(PropertyLoader.getInstance().getPropertyValue("db.username")));
            config.setPassword(kmsDecrypter.decrypt(PropertyLoader.getInstance().getPropertyValue("db.password")));
            config.setJdbcUrl(PropertyLoader.getInstance().getPropertyValue("db.url"));
            config.setMinimumIdle(INITIAL_POOL_SIZE);
            config.setMaximumPoolSize(MAX_POOL_SIZE);
            dataSource = new HikariDataSource(config);
        }
        return dataSource;
    }
}
