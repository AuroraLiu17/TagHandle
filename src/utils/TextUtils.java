package utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class TextUtils {
	private static final SimpleDateFormat OUTPUT_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
	private static final SimpleDateFormat INPUT_DATE_FORMAT = new SimpleDateFormat("yyyyMMdd");
	private static final Date DATE = new Date();
	
	public static boolean isEmpty(String text) {
		return text == null || text.length() <= 0;
	}
	
	public static int toDBDate(Calendar calendar) {
		synchronized (TextUtils.class) {
			DATE.setTime(calendar.getTimeInMillis());
			return Integer.valueOf(INPUT_DATE_FORMAT.format(DATE));
		}
	}
	
	public static String toReportDate(Calendar calendar) {
		synchronized (TextUtils.class) {
			DATE.setTime(calendar.getTimeInMillis());
			return OUTPUT_DATE_FORMAT.format(DATE);
		}
	}
}
