package com.XI.xi_oj.utils;

import java.time.Duration;

/**
 * Time helpers used by cache/rate-limit code.
 */
public final class TimeUtil {

    private TimeUtil() {
    }

    public static Duration minutes(long minutes) {
        return Duration.ofMinutes(minutes);
    }

    public static Duration seconds(long seconds) {
        return Duration.ofSeconds(seconds);
    }
}
