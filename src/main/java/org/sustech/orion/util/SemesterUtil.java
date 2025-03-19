package org.sustech.orion.util;

import java.sql.Timestamp;
import java.time.LocalDate;

public class SemesterUtil {
    public static String getCurrentSemester() {
        LocalDate now = LocalDate.now();
        int year = now.getYear();
        String semester = (now.getMonthValue() >= 3 && now.getMonthValue() <= 8) ? "Spring" : "Fall";
        return year + "-" + semester;
    }
    public static String transformSemester(Timestamp timestamp) {
        LocalDate date = timestamp.toLocalDateTime().toLocalDate();
        int year = date.getYear();
        String semester = (date.getMonthValue() >= 3 && date.getMonthValue() <= 8) ? "Spring" : "Fall";
        return year + "-" + semester;
    }
}
