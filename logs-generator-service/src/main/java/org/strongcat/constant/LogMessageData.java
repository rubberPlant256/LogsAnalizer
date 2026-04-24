package org.strongcat.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Random;

@Getter
@RequiredArgsConstructor
public enum LogMessageData {
    LOGIN("User logged in"),
    DB_TIMEOUT("Database connection timeout"),
    ORDER_OK("Order processed successfully"),
    PAYMENT_FAIL("Payment failed: insufficient funds"),
    CACHE_CLR("Cache cleared"),
    MINIO_UP("File uploaded to Minio");

    private final String message;
    private static final Random RANDOM = new Random();

    public static String getRandomMessage() {
        return values()[RANDOM.nextInt(values().length)].getMessage();
    }
}