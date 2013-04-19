
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
	public static final class FixedlineAreaCode implements BaseColumns,
			FixedlineAreaCodeColumns {

		private FixedlineAreaCode() {
		}

		public static final String TABLE_FIXEDLINE = "fixedline";
	}

	public interface FixedlineAreaCodeColumns {
		public static final String AREA_CODE = "areacode";
		public static final String PROVINCE = "province";
		public static final String CITY = "city";
	}
}
