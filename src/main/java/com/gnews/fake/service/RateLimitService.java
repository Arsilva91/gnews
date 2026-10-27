package com.gnews.fake.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimitService {

    @Value("${gnews.rate-limit.daily-requests:100}")
    private int dailyLimit;

    // Key: ApiKey, Value: Request Count
    private final Map<String, Integer> requestCounts = new ConcurrentHashMap<>();

    public boolean allowRequest(String apiKey) {
        return requestCounts.merge(apiKey, 1, Integer::sum) <= dailyLimit;
    }

    // Reset counts every day at midnight (simulation)
    @Scheduled(cron = "0 0 0 * * *")
    public void resetCounts() {
        requestCounts.clear();
    }
}
