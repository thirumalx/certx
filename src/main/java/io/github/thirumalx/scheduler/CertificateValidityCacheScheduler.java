package io.github.thirumalx.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import io.github.thirumalx.cache.CacheNames;

/**
 * Clears certificate validity cache every 6 hours.
 */
@Component
public class CertificateValidityCacheScheduler {

    private static final Logger logger = LoggerFactory.getLogger(CertificateValidityCacheScheduler.class);

    private final CacheManager cacheManager;

    public CertificateValidityCacheScheduler(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    @Scheduled(cron = "0 0 0,6,12,18 * * ?")
    public void clearCertificateValidityCache() {
        Cache cache = cacheManager.getCache(CacheNames.CERTIFICATE_VALIDITY);
        if (cache != null) {
            cache.clear();
            logger.debug("Cleared cache {}", CacheNames.CERTIFICATE_VALIDITY);
        } else {
            logger.warn("Cache {} not found for clearing", CacheNames.CERTIFICATE_VALIDITY);
        }
    }
}
