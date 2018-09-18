package pl.codewise.globee.core.utils;

import com.google.common.base.Splitter;

public class GlobeeStringUtils {

    public static String removeFirstAndLastCharacter(String string) {
        return string.substring(1, string.length() - 1);
    }

    public static String extractJson(String string) {
        return removeFirstAndLastCharacter(string).replace("\\", "");
    }

    public static String removeQuotationMarks(String string) {
        return string.replace("\"", "");
    }

    public static String extractExactClassName(String string) {
        return Splitter.on("$").splitToList(string).get(1);
    }
}
