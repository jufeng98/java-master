package org.javamaster.mybatis.generator.utils;


import static java.lang.Character.isDigit;
import static java.lang.Character.isLetter;
import static java.lang.Character.isLetterOrDigit;
import static java.lang.Character.isLowerCase;
import static java.lang.Character.isUpperCase;
import static java.lang.Character.isWhitespace;


public class StringUtils {

    public static String wordsAndHyphenAndCamelToConstantCase(String s) {
        boolean containsLowerCase = containsLowerCase(s);

        StringBuilder buf = new StringBuilder();
        char previousChar = ' ';
        char[] chars = s.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];
            boolean isUpperCaseAndPreviousIsUpperCase = isUpperCase(previousChar) && isUpperCase(c);
            boolean isUpperCaseAndPreviousIsLowerCase = isLowerCase(previousChar) && isUpperCase(c);

            //camelCase handling - add extra _
            if (isLetter(c) && isLetter(previousChar) &&
                    (
                            isUpperCaseAndPreviousIsLowerCase
                                    || (containsLowerCase && putSeparatorBetweenUppercases() && isUpperCaseAndPreviousIsUpperCase)
                    )
            ) {
                buf.append("_");
                // extra _ after number
            } else if ((isSeparatorAfterDigit() && isDigit(previousChar) && isLetter(c))
                    || (isSeparatorBeforeDigit() && isDigit(c) && isLetter(previousChar))) {
                buf.append('_');
            }


            //replace separators by _
            if ((isSeparator(c) || isWhitespace(c)) && isLetterOrDigit(previousChar) && nextIsLetterOrDigit(s, i)) {
                buf.append('_');
            } else {
                buf.append(Character.toUpperCase(c));
            }

            previousChar = c;
        }

        return buf.toString();
    }

    private static boolean putSeparatorBetweenUppercases() {
        return true;
    }

    private static boolean nextIsLetterOrDigit(String s, int i) {
        if (i + 1 >= s.length()) {
            return false;
        } else {
            return Character.isLetterOrDigit(s.charAt(i + 1));
        }
    }


    public static boolean containsLowerCase(String s) {
        for (char c : s.toCharArray()) {
            if (isLowerCase(c)) {
                return true;
            }
        }
        return false;
    }

    protected static boolean isSeparatorBeforeDigit() {
        return true;
    }

    protected static boolean isSeparatorAfterDigit() {
        return false;
    }

    public static boolean isSeparator(char c) {
        return c == '.' || c == '-' || c == '_';
    }

}