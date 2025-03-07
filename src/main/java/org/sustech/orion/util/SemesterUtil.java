package org.sustech.orion.util;

import java.time.LocalDate;

public class SemesterUtil {
    public static String getCurrentSemester() {
        LocalDate now = LocalDate.now();
        int year = now.getYear();
        String semester = (now.getMonthValue() >= 3 && now.getMonthValue() <= 8) ? "Spring" : "Fall";
        return year + "-" + semester;
    }
}
