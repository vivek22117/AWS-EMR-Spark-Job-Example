package com.dd.rsvp.processor.job.dao;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.io.Serializable;

public class DatabaseUtil implements Serializable {
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public DatabaseUtil() {
        this( new NamedParameterJdbcTemplate(FactoryDataSource.getInstance().getDefaultDataSource()));
    }

    public DatabaseUtil(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }
}
