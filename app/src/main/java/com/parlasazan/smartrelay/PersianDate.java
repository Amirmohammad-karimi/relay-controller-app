package com.parlasazan.smartrelay;

/** Small dependency-free Gregorian to Solar Hijri conversion used by the controller protocol. */
public final class PersianDate {
    private static final int[] GREGORIAN_MONTH_DAYS =
            {31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};

    private PersianDate() {}

    public static int[] fromGregorian(int year, int monthOneBased, int day) {
        int gy = year - 1600;
        int gm = monthOneBased - 1;
        int gd = day - 1;

        int dayNumber = 365 * gy + (gy + 3) / 4 - (gy + 99) / 100 + (gy + 399) / 400;
        for (int i = 0; i < gm; i++) dayNumber += GREGORIAN_MONTH_DAYS[i];
        if (gm > 1 && isGregorianLeap(year)) dayNumber++;
        dayNumber += gd;

        int jalaliDayNumber = dayNumber - 79;
        int cycles = jalaliDayNumber / 12053;
        jalaliDayNumber %= 12053;
        int jy = 979 + 33 * cycles + 4 * (jalaliDayNumber / 1461);
        jalaliDayNumber %= 1461;
        if (jalaliDayNumber >= 366) {
            jy += (jalaliDayNumber - 1) / 365;
            jalaliDayNumber = (jalaliDayNumber - 1) % 365;
        }
        int jm;
        int jd;
        if (jalaliDayNumber < 186) {
            jm = 1 + jalaliDayNumber / 31;
            jd = 1 + jalaliDayNumber % 31;
        } else {
            jm = 7 + (jalaliDayNumber - 186) / 30;
            jd = 1 + (jalaliDayNumber - 186) % 30;
        }
        return new int[]{jy, jm, jd};
    }

    private static boolean isGregorianLeap(int year) {
        return year % 4 == 0 && (year % 100 != 0 || year % 400 == 0);
    }
}
