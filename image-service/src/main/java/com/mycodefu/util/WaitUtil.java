package com.mycodefu.util;

import java.util.function.BooleanSupplier;

public class WaitUtil {
    private static final int DEFAULT_MAX_RETRIES = 4;
    private static final int DEFAULT_SLEEP_DURATION_MS = 300;

    private WaitUtil() {
    }

    public static void waitUntil(BooleanSupplier conditionChecker, int maxRetries, int sleepDurationMs, String errorMessage) {
        boolean isConditionMet = false;
        int retryCount = 0;

        while (!isConditionMet && retryCount < maxRetries) {
            try {
                Thread.sleep(sleepDurationMs);
                if (conditionChecker.getAsBoolean()) {
                    isConditionMet = true;
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                retryCount++;
            }
        }

        if (!isConditionMet) {
            throw new RuntimeException(errorMessage);
        }
    }

    public static void waitUntil(BooleanSupplier conditionChecker) {
        waitUntil(conditionChecker, DEFAULT_MAX_RETRIES, DEFAULT_SLEEP_DURATION_MS, "Operation timed out, it is possible the change will be visible at a later stage");
    }
}
