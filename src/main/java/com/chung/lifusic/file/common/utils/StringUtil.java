package com.chung.lifusic.file.common.utils;

import java.util.Date;
import java.util.Random;

public class StringUtil {
    /**
     * 랜덤 문자열 생성
     * @param targetLength 생성될 문자열의 길이
     * @return a ~ z로 구성된 랜덤 문자열 + timestamp
     */
    public static String getUniqueString(int targetLength) {
        int leftLimit = 97; // letter 'a'
        int rightLimit = 122; // letter 'z'
        Random random = new Random();

        String generatedString = random.ints(leftLimit, rightLimit + 1)
                .limit(targetLength)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
        return generatedString + new Date().getTime();
    }
}
