package com.messagelogix.anonymousalerts.utils;

/**
 * This is a string helper class
 */
public class Strings {

    public static boolean isNullOrEmpty(String str) {
        return str == null || str.isEmpty() || str.trim().length() == 0;
    }

}
