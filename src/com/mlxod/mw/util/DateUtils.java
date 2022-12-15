/**
 * 
 */
package com.mlxod.mw.util;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import com.mlxod.mw.util.DateUtils;

public class DateUtils {

	public static void dateDifference()

	{

		// Creates two calendars instances

		Calendar cal1 = Calendar.getInstance();

		Calendar cal2 = Calendar.getInstance();

		// Set the date for both of the calendar instance

		cal1.set(2006, 12, 30);

		cal2.set(2007, 5, 3);

		// Get the represented date in milliseconds

		long milis1 = cal1.getTimeInMillis();

		long milis2 = cal2.getTimeInMillis();

		// Calculate difference in milliseconds

		long diff = milis2 - milis1;

		// Calculate difference in seconds

		long diffSeconds = diff / 1000;

		// Calculate difference in minutes

		long diffMinutes = diff / (60 * 1000);

		// Calculate difference in hours

		long diffHours = diff / (60 * 60 * 1000);

		// Calculate difference in days

		long diffDays = diff / (24 * 60 * 60 * 1000);

		System.out.println("In milliseconds: " + diff + " milliseconds.");

		System.out.println("In seconds: " + diffSeconds + " seconds.");

		System.out.println("In minutes: " + diffMinutes + " minutes.");

		System.out.println("In hours: " + diffHours + " hours.");

		System.out.println("In days: " + diffDays + " days.");

	}

	public static void main(String arg[]) {
	/*	System.out.println(DateUtils.now("dd MMMMM yyyy"));
		System.out.println(DateUtils.now("yyyyMMdd"));
		System.out.println(DateUtils.now("dd.MM.yy"));
		System.out.println(DateUtils.now("MM/dd/yy"));
		System.out.println(DateUtils.now("yyyy.MM.dd G 'at' hh:mm:ss z"));
		System.out.println(DateUtils.now("EEE, MMM d, ''yy"));
		System.out.println(DateUtils.now("h:mm a"));
		System.out.println(DateUtils.now("H:mm:ss:SSS"));
		System.out.println(DateUtils.now("K:mm a,z"));
		System.out.println(DateUtils.now("yyyy.MMMMM.dd GGG hh:mm aaa"));
		System.out.println(DateUtils.now("yy.MM.dd hh:mm"));
		dateDifference(); */
		
		String extension = "";
        File f = new File("c:/hot.txt.swp");
        String fname = f.getName();
		int i = fname.lastIndexOf('.');
		if (i > 0) {
		    extension = fname.substring(i+1);
		}
		
	}

	public static String now(String dateFormat) {
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
		return sdf.format(cal.getTime());

	}

}
