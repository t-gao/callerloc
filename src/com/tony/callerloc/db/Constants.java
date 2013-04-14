
package com.tony.callerloc.db;

import android.provider.BaseColumns;

/**
 * @author Tony Gao
 *
 */
public class Constants {

    private Constants() {
    }

    public static final class CallerLoc implements BaseColumns, CallerLocColumns {

        private CallerLoc() {
        }

        public static final String TABLE_CALLERLOC = "callerlocs";
    }

    public interface CallerLocColumns {
        public static final String PREFIX = "prefix";
        public static final String MID = "mid";
        public static final String LOC = "loc";
    }
}
