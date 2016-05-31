package com.orelogo.relink;

/**
 * Variables and methods used by many activities for converting time value and scales to different
 * formats.
 */
public class Convert {

    // milliseconds in an average year (assuming 365.25 days)
    static final long YEAR_MS = 31_557_600_000L;
    // milliseconds in an average month (assuming 365.25/12 days)
    static final long MONTH_MS = 2_629_800_000L;
    static final int WEEK_MS = 604_800_000; // milliseconds in a week
    static final int DAY_MS = 86_400_000;   // milliseconds in a day

    // time scale constants
    static final String DAYS_CHAR = "d";
    static final String WEEKS_CHAR = "w";
    static final String MONTHS_CHAR = "m";
    static final String YEARS_CHAR = "y";

    // time scale long form
    static final String DAYS_PLURAL = "days";
    static final String WEEKS_PLURAL = "weeks";
    static final String MONTHS_PLURAL = "months";
    static final String YEARS_PLURAL = "years";

    /**
     * Convert a time scale shorthands of d, w, m, y to the long form of days, weeks, months, years.
     *
     * @param timeScale d, w, m, or y
     * @param getPlural true if output should be plural
     * @return long form of time scale
     */
    static String getTimeScaleLong(String timeScale, boolean getPlural) {

        String timeScaleLong;

        switch (timeScale) {
            case DAYS_CHAR:
                timeScaleLong = "day";
                break;
            case WEEKS_CHAR:
                timeScaleLong = "week";
                break;
            case MONTHS_CHAR:
                timeScaleLong = "month";
                break;
            case YEARS_CHAR:
                timeScaleLong = "year";
                break;
            default:
                timeScaleLong = "error";
                break;
        }

        if (getPlural) {
            timeScaleLong += "s";
        }

        return timeScaleLong;
    }

    /**
     * Convert a time scale plural words: days, weeks, months, and years to shorthands of d, w, m,
     * and y.
     *
     * @param timeScaleLong long form of time scale
     * @return short hand form of time scale
     */
    static String getTimeScaleChar(String timeScaleLong) {

        String timeScaleChar;

        switch (timeScaleLong) {
            case DAYS_PLURAL:
                timeScaleChar = DAYS_CHAR;
                break;
            case WEEKS_PLURAL:
                timeScaleChar = WEEKS_CHAR;
                break;
            case MONTHS_PLURAL:
                timeScaleChar = MONTHS_CHAR;
                break;
            case YEARS_PLURAL:
                timeScaleChar = YEARS_CHAR;
                break;
            default:
                timeScaleChar = "x";
                break;
        }

        return timeScaleChar;
    }

    /**
     * Get milliseconds from time scale.
     *
     * @param timeScale d, w, m, or y
     * @return corresponding time in milliseconds
     */
    static long getMillisec(String timeScale) {

        long timeScaleMillisec;

        switch (timeScale) {
            case DAYS_CHAR:
                timeScaleMillisec = DAY_MS;
                break;
            case WEEKS_CHAR:
                timeScaleMillisec = WEEK_MS;
                break;
            case MONTHS_CHAR:
                timeScaleMillisec = MONTH_MS;
                break;
            case YEARS_CHAR:
                timeScaleMillisec = YEAR_MS;
                break;
            default:
                timeScaleMillisec = -1;
                break;
        }

        return timeScaleMillisec;
    }

    /**
     * Get spinner selection based on time scale.
     *
     * @param timeScale d, w, m, or y
     * @return spinner selection
     */
    static int getSpinnerSelection(String timeScale) {
        int spinnerSelection;

        switch (timeScale) {
            case (DAYS_CHAR):
                spinnerSelection = 0;
                break;
            case (WEEKS_CHAR):
                spinnerSelection = 1;
                break;
            case (MONTHS_CHAR):
                spinnerSelection = 2;
                break;
            case (YEARS_CHAR):
                spinnerSelection = 3;
                break;
            default: // error occurred
                spinnerSelection = -1;
                break;
        }
        return spinnerSelection;
    }

    /**
     * Get the next connect time, in unix time, based on the connect interval and time scale.
     *
     * @param connectInterval connect interval value
     * @param timeScale time scale of the connect interval
     * @return next connect time, in unix time
     */
    static long getNextConnect(Double connectInterval, String timeScale) {
        // time in a day, week, month, or year (in ms)
        long timeMs = Convert.getMillisec(timeScale);

        // calculate time to next connect
        long currentTime =  System.currentTimeMillis();
        long intervalTime = (long) Math.floor(connectInterval * timeMs);
        long nextConnect = currentTime + intervalTime;// unix time for next connect (in millisec)

        return nextConnect;
    }
}
