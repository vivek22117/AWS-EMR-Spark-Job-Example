package com.dd.rsvp.processor.job.service;

import com.dd.rsvp.processor.job.utility.PropertyLoader;
import net.spy.memcached.MemcachedClient;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.Serializable;
import java.net.InetSocketAddress;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

public class ElasticCacheService implements Serializable {
    private static final Logger LOGGER = Logger.getLogger(ElasticCacheService.class);

    private MemcachedClient memcachedClient;
    private static final int EXP_SECONDS_IN_30_DAYS_FROM_CURRENT_TIME = 60 * 60 * 24 * 7;
    private static final String CACHE_URL = PropertyLoader.getInstance().getPropertyValue("CACHE.URL");
    private static final Integer CACHE_PORT = Integer.valueOf(PropertyLoader.getInstance().getPropertyValue("CACHE.PORT"));

    public ElasticCacheService() {
        try {
            this.memcachedClient = new MemcachedClient(new InetSocketAddress(CACHE_URL, CACHE_PORT));
        } catch (IOException e) {
            LOGGER.error("Unable to create memcached client");
        }
    }

    public ElasticCacheService(MemcachedClient memcachedClient) {
        this.memcachedClient = memcachedClient;
    }

    public <T> Optional<T> getByKey(String key) {
        if (memcachedClient == null) {
            return Optional.empty();
        }
        try {
            return Optional.ofNullable((T) memcachedClient.get(key));
        } catch (Exception e) {
            LOGGER.error("Failed to get key " + key);
            return Optional.empty();
        }
    }

    public boolean addByKey(String key, int expiry, Object value) {
        if (memcachedClient == null) {
            return false;
        }
        try {
            return memcachedClient.add(key, expiry, value).get().booleanValue();
        } catch (InterruptedException | ExecutionException e) {
            LOGGER.error("Failed to add key " + key);
            return false;
        }
    }

    public boolean replaceKey(String key, int expiry, Object value) {
        if (memcachedClient == null) {
            return false;
        }
        try {
            return memcachedClient.replace(key, expiry, value).get();
        } catch (InterruptedException | ExecutionException e) {
            LOGGER.error("Failed to replace key " + key);
            return false;
        }
    }

    public boolean evictCacheByKey(String key) {
        if (memcachedClient == null) {
            return false;
        }
        try {
            return memcachedClient.delete(key).get();
        } catch (InterruptedException | ExecutionException e) {
            LOGGER.error("Failed to delete key " + key);
            return false;
        }
    }
}
