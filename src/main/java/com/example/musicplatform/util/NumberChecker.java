package com.example.musicplatform.util;

public class NumberChecker {


        /**
         * 检查字符串是否全部由数字字符组成。
         * @param str 待检查的字符串
         * @return 如果字符串非空且每个字符都是数字，则返回true；否则返回false。
         */
        public static boolean isAllDigits(String str) {
            // 重要：先检查字符串是否为null或空
            if (str == null || str.isEmpty()) {
                // 根据业务需求，空字符串可以认为是“全数字”或“非全数字”，这里我们认为空不是
                return false;
            }

            // 遍历字符串中的每一个字符
            for (int i = 0; i < str.length(); i++) {
                char c = str.charAt(i);
                // 如果任何一个字符不是数字，则返回false
                if (!Character.isDigit(c)) {
                    return false;
                }
            }
            // 所有字符都是数字，返回true
            return true;
        }
}
