package com.freeboard04.utils;

public class TestUtil {

    public static String getRandomString(int length) {
        String id = "";
        for (int i = 0; i < length; i++) {
            double dValue = Math.random();
            if (i % 2 == 0) {
                id += (char) ((dValue * 26) + 65);   // 대문자
                continue;
            }
            id += (char) ((dValue * 26) + 97); // 소문자
        }
        return id;
    }

}
