package com.portal.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

public class PortalTimeUtils {

	public static String dateToSQLDate( Date date, TimeZone timeZone ) {
		if( date != null ) {
			SimpleDateFormat dateFormat = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss" ); 
			dateFormat.setTimeZone( timeZone );
			return dateFormat.format( date );
		}

		return null;
	}

	public static String dateToSQLDate( Date date ) {
		if( date != null ) {
			SimpleDateFormat dateFormat = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss" ); 
			return dateFormat.format( date );
		}

		return null;
	}
	
	public static String dateToSQLDate( Date date, String pattern ) {
		if( date != null ) {
			SimpleDateFormat dateFormat = new SimpleDateFormat( pattern ); 
			return dateFormat.format( date );
		}

		return null;
	}
	
	public static String localDateToString( LocalDate date, String pattern ) {
		if( date != null ) {
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
			return date.format(formatter);
		}

		return null;
	}
	
	
	public static String dateFormat( Date date, String format ) {
		if( date != null ) {
			SimpleDateFormat dateFormat = new SimpleDateFormat( format );
			return dateFormat.format( date );
		}

		return null;
	}

	public static Calendar convertCalendar(final Calendar calendar, final TimeZone timeZone) {
		Calendar ret = new GregorianCalendar(timeZone);
		ret.setTimeInMillis(calendar.getTimeInMillis() +
				timeZone.getOffset(calendar.getTimeInMillis()) -
				TimeZone.getDefault().getOffset(calendar.getTimeInMillis()));
		ret.getTime();
		return ret;
	}

	public static Date convertStringToDate(final String data, final String pattern) {
		try {
			return new SimpleDateFormat(pattern).parse(data);
		} catch (ParseException e) {
			
		}
		return null;

	}

	public static Calendar convertStringToCalendar(final String data, final String pattern) {
		try {
			Date date = convertStringToDate(data, pattern);
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(date);
			return calendar;
		} catch (Exception e) {
			
		}
		
		return null;
	}

	public static String convertSecondsToTime( long seconds ) {
		long segundo = seconds % 60;   
		long minutos = seconds / 60;   
		long minuto	= minutos % 60;   
		long hora = minutos / 60;

		return String.format ("%02d:%02d:%02d", hora, minuto, segundo);
	}
	
	public static Calendar convertCalendarToTimezone(Calendar calendar, TimeZone timezone) {
		Calendar cal = new GregorianCalendar(timezone);
		Long timeMillis = calendar.getTimeInMillis();
		Integer offsetMillis = (timezone.getRawOffset())*-1;

		if(timezone.inDaylightTime( calendar.getTime() )) {
			offsetMillis -= timezone.getDSTSavings();
		}
		cal.setTimeZone(timezone);
		cal.setTime( new Date(timeMillis - offsetMillis) );

		return cal;
	}
	
	public static Calendar convertCalendarToUTC(final Calendar calendar, final TimeZone userTimezone) {
		Long timeMillis = calendar.getTimeInMillis();
		Integer offsetMillis = (userTimezone.getRawOffset())*-1;

		if(userTimezone.inDaylightTime( calendar.getTime() )) {
			offsetMillis -= userTimezone.getDSTSavings();
		}

		calendar.setTime( new Date(timeMillis + offsetMillis) );

		return calendar;	
	}

	public static String localDateTimeFormat( LocalDateTime ldt, String pattern ) {
		if( ldt != null ) {
			return ldt.format( DateTimeFormatter.ofPattern( pattern ) );
		}
		
		return null;
	}
	
	public static String localDateFormat( LocalDate ldt, String pattern ) {
		if( ldt != null ) {
			return ldt.format( DateTimeFormatter.ofPattern( pattern ) );
		}
		
		return null;
	}
}
