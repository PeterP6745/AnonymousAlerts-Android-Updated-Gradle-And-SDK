package com.messagelogix.anonymousalerts.utils;

public class RetryCounter {

    private int retryCount;

    public RetryCounter() {
        retryCount = 2;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public void decrementRetryCount() {
        retryCount -= 1;
    }
}
