package de.imi.mopat.helper.controller;

import java.text.SimpleDateFormat;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

@Service
public class CacheService {

    @Autowired
    CacheManager cacheManager;

    private static final Logger LOGGER = LoggerFactory.getLogger(CacheService.class);

    private String timeStamp;

    public CacheService() {
        this.setCurrentTimeAsTimestamp();
    }

    /**
     * Function to evict all caches. Can be toggled if user rights should be updated immediately
     */
    public void evictAllCaches() {
        LOGGER.info("Evicted caches: " + cacheManager.getCacheNames());
        cacheManager.getCacheNames()
            .forEach(cacheName -> Objects.requireNonNull(cacheManager.getCache(cacheName)).clear());
        this.setCurrentTimeAsTimestamp();
    }

    private void setCurrentTimeAsTimestamp() {
        this.timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new java.util.Date());
    }

    public String getTimeStamp() {
        return this.timeStamp;
    }

    /**
     * Scheduled command to evict caches after a certain amount of time (in ms)
     */

    /*
    @Scheduled(fixedRate = 1000)
    public void evictAllcachesAtIntervals() {
        evictAllCaches();
    }
     */
}
