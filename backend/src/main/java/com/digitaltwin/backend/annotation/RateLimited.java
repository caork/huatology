package com.digitaltwin.backend.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimited {

    /**
     * Maximum number of requests allowed within the time window
     */
    int value() default 10;

    /**
     * Time window in seconds
     */
    int timeWindow() default 60;

    /**
     * Key strategy for rate limiting (USER, IP, GLOBAL)
     */
    KeyStrategy keyStrategy() default KeyStrategy.USER;

    public enum KeyStrategy {
        USER,    // Rate limit per authenticated user
        IP,      // Rate limit per IP address
        GLOBAL   // Global rate limit
    }
}