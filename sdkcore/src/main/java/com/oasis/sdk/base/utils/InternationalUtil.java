package com.oasis.sdk.base.utils;

import java.text.DateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

import com.oasis.sdk.base.entity.PhoneInfo;

import android.content.Context;

public class InternationalUtil {
	
	public static String InternationalDateFormat(Context c, Long time) {
		return DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT, new Locale(PhoneInfo.instance().locale)).format(new Date(time));

	}
	public static String dayFormat(Context c, Long time) {
		return DateFormat.getDateInstance(DateFormat.DEFAULT, new Locale(PhoneInfo.instance().locale)).format(new Date(time));

	}
	public static String timeFormat(Context c, Long time) {
		return DateFormat.getTimeInstance(DateFormat.DEFAULT, new Locale(PhoneInfo.instance().locale)).format(new Date(time));
	}
	//没有用到
	public static CharSequence formatTimeInListForOverSeaUser(final Context context, final long time, final boolean simple) {
		Locale locale=context.getResources().getConfiguration().locale;
		final long MILLSECONDS_OF_HOUR=60*60*1000;
		final long MILLSECONDS_OF_DAY=24*MILLSECONDS_OF_HOUR;
		final GregorianCalendar now = new GregorianCalendar();

		// special time
		if (time < MILLSECONDS_OF_HOUR) {
			return "";
		}
		// today
		final GregorianCalendar today = new GregorianCalendar(
		now.get(GregorianCalendar.YEAR),
		now.get(GregorianCalendar.MONTH),
		now.get(GregorianCalendar.DAY_OF_MONTH));
		final long in24h = time - today.getTimeInMillis();
		if (in24h > 0 && in24h <= MILLSECONDS_OF_DAY) {
			DateFormat df = DateFormat.getTimeInstance(
			DateFormat.SHORT, locale);
			return "" + df.format(time);
		}
//		// yesterday
//		final long in48h = time - today.getTimeInMillis() + MILLSECONDS_OF_DAY;
//		if (in48h > 0 && in48h <= MILLSECONDS_OF_DAY) {
//
//			return simple ? context.getString(R.string.fmt_pre_yesterday)
//					: context.getString(R.string.fmt_pre_yesterday)
//			+ " "
//			+ java.text.DateFormat.getTimeInstance(
//			java.text.DateFormat.SHORT, locale).format(
//			time);
//		}
		final GregorianCalendar target = new GregorianCalendar();
		target.setTimeInMillis(time);
		// same week
		if (now.get(GregorianCalendar.YEAR) == target
		.get(GregorianCalendar.YEAR)
		&& now.get(GregorianCalendar.WEEK_OF_YEAR) == target
		.get(GregorianCalendar.WEEK_OF_YEAR)) {
			java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("E", locale);
			final String dow = "" + sdf.format(time);
			return simple ? dow : dow
			+ DateFormat.getTimeInstance(
			DateFormat.SHORT, locale).format(time);
		}

		// same year
		if (now.get(GregorianCalendar.YEAR) == target
		.get(GregorianCalendar.YEAR)) {
			return simple ? DateFormat.getDateInstance(
			DateFormat.SHORT, locale).format(time)
					: DateFormat.getDateTimeInstance(
			DateFormat.SHORT,
			DateFormat.SHORT, locale).format(time);
		}
		return simple ? DateFormat.getDateInstance(
		DateFormat.SHORT, locale).format(time)
				: DateFormat.getDateTimeInstance(
		DateFormat.SHORT, DateFormat.SHORT,
		locale).format(time);
	}

}
