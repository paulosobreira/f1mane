package br.nnpe;


import java.sql.Date;

import java.text.SimpleDateFormat;

import java.util.Calendar;
import java.util.Locale;

public class FormatDate extends Object {
	public static String format(java.util.Date DATE, String PATTERN) {
		String date = "";
		if (DATE != null) {
			if ((PATTERN != null) && (PATTERN.length() > 0)
					&& !PATTERN.equalsIgnoreCase("null")) {
				SimpleDateFormat df = new SimpleDateFormat(PATTERN);
				date = df.format(DATE);
			} else {
				SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
				date = df.format(DATE);
			}
		}

		return date;
	}

	/**
	 * Method for formating a Timestamp in default ("dd/MM/yyyy HH:mm:ss")
	 * format.
	 */
	public static String format(java.sql.Timestamp DATE, String PATTERN) {
		String date = "";
		if (DATE != null) {
			if ((PATTERN != null) && (PATTERN.length() > 0)
					&& !PATTERN.equalsIgnoreCase("null")) {
				SimpleDateFormat df = new SimpleDateFormat(PATTERN);
				date = df.format(DATE);
			} else {
				SimpleDateFormat df = new SimpleDateFormat(
						"dd/MM/yyyy HH:mm:ss");
				date = df.format(DATE);
			}
		}

		return date;
	}

	/**
	 * Method for formating a Time in default ("dd/MM/yyyy HH:mm:ss") format.
	 */
	public static String format(java.sql.Time DATE, String PATTERN) {
		String date = "";
		if (DATE != null) {
			if ((PATTERN != null) && (PATTERN.length() > 0)
					&& !PATTERN.equalsIgnoreCase("null")) {
				SimpleDateFormat df = new SimpleDateFormat(PATTERN);
				date = df.format(DATE);
			} else {
				SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");
				date = df.format(DATE);
			}
		}

		return date;
	}

	/**
	 * Method for formating a Date in default ("dd/MM/yyyy") format.
	 */
	public static String format(java.util.Date DATE) {
		String date = format(DATE, "dd/MM/yyyy");

		return date;
	}

	/**
	 * Method for formating a Timestamp in default ("dd/MM/yyyy HH:mm:ss")
	 * format.
	 */
	public static String format(java.sql.Timestamp DATE) {
		String date = format(DATE, "dd/MM/yyyy HH:mm:ss");

		return date;
	}

	/**
	 * Method for formating a Time in default ("HH:mm:ss") format.
	 */
	public static String format(java.sql.Time DATE) {
		String date = format(DATE, "HH:mm:ss");

		return date;
	}

	/**
	 * Method for formating a data in "dd-MMM-yyyy HH:mm:ss" format.
	 * 
	 * @deprecated As of e-Gen 1.2. Use method format() instead.
	 */
	public static String formatFull(java.util.Date DATE) {
		String date = format(DATE, "dd-MMM-yyyy HH:mm:ss");

		return date;
	}

	/**
	 * Method for formating a data in "yyyy-MM-dd" format.
	 * 
	 * @deprecated As of e-Gen 1.2. Use method format() instead.
	 */
	public static String formatObject(java.util.Date DATE) {
		String date = format(DATE, "yyyy-MM-dd");

		return date;
	}

	/**
	 * Method for formating a data in Sql ("dd-MMM-yyyy") format.
	 * 
	 * @deprecated As of e-Gen 1.2. Use method format() instead.
	 */
	public static String formatSql(java.util.Date DATE) {
		SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy", Locale.US);

		return df.format(DATE);
	}

	/**
	 * Method for getting the Day of a date.
	 * 
	 * @deprecated As of e-Gen 1.2. Use method format() instead.
	 */
	public static int getDia(java.util.Date DATE) {
		String date = format(DATE, "dd");
		int dia = (new Integer(date)).intValue();

		return dia;
	}

	/**
	 * Method for getting the Day of a date, using a fill format.
	 * 
	 * @deprecated As of e-Gen 1.2. Use method format() instead.
	 */
	public static String getDia(java.util.Date DATE, String fill) {
		String date = format(DATE, "dd");
		int dia = (new Integer(date)).intValue();

		return FormatNumber.fillMode(dia, fill);
	}

	/**
	 * Method for getting the Month of a date.
	 * 
	 * @deprecated As of e-Gen 1.2. Use method format() instead.
	 */
	public static int getMes(java.util.Date DATE) {
		String date = format(DATE, "MM");
		int mes = (new Integer(date)).intValue();

		return mes;
	}

	/**
	 * Method for getting the Month of a date, using fill format.
	 * 
	 * @deprecated As of e-Gen 1.2. Use method format() instead.
	 */
	public static String getMes(java.util.Date DATE, String fill) {
		String date = format(DATE, "MM");
		int mes = (new Integer(date)).intValue();

		return FormatNumber.fillMode(mes, fill);
	}

	/**
	 * Method for getting the Year of a date.
	 * 
	 * @deprecated As of e-Gen 1.2. Use method format() instead.
	 */
	public static int getAno(java.util.Date DATE) {
		String date = format(DATE, "yyyy");
		int ano = (new Integer(date)).intValue();

		return ano;
	}

	/**
	 * Method for getting the Year of a date.
	 * 
	 * @deprecated As of e-Gen 1.2. Use method format() instead.
	 */
	public static String getDayOfWeek(java.util.Date DATA) {
		SimpleDateFormat df = new SimpleDateFormat("E", Locale.US);

		return df.format(DATA);
	}

	/**
	 * Method for getting the day and month of a date.
	 * 
	 * @deprecated As of e-Gen 1.2. Use method format() instead.
	 */
	public static String getDiames(java.util.Date DATE) {
		String date = format(DATE, "dd/MM");

		return date;
	}

	/**
	 * Method for getting the month and year of a date.
	 * 
	 * @deprecated As of e-Gen 1.2. Use method format() instead.
	 */
	public static String getMesano(java.util.Date DATE) {
		String date = format(DATE, "MM/yyyy");

		return date;
	}

	/**
	 * Method for getting the a date in short format ("dd/MM/yy").
	 * 
	 * @deprecated As of e-Gen 1.2. Use method format() instead.
	 */
	public static String formatShort(java.util.Date DATE) {
		String date = format(DATE, "dd/MM/yy");

		return date;
	}

	/**
	 * Method for parsing String to java.util.Date.<br>
	 * Pattern default ("dd/MM/yyyy"); Arguments: String DATE.
	 * 
	 * @throws Exception
	 * 
	 */
	public static java.util.Date parse(String DATE) throws Exception {
		return parse(DATE, "dd/MM/yyyy");
	}

	/**
	 * Method for parsing String to java.util.Date.<br>
	 * Arguments: String DATE, String PATTERN.
	 * 
	 * @throws Exception
	 * 
	 */
	public static java.util.Date parse(String DATE, String PATTERN)
			throws Exception {
		java.util.Date date = null;
		if ((PATTERN != null) && (PATTERN.length() > 0)
				&& !PATTERN.equalsIgnoreCase("null")) {
			try {
				SimpleDateFormat df = new SimpleDateFormat(PATTERN);
				if ((DATE != null) && (DATE.length() > 0)) {
					date = df.parse(DATE);
				}
			} catch (Exception e) {
				throw e;
			}
		} else {
			date = parse(DATE);
		}

		return date;
	}

	/**
	 * Method for parsing String to java.sql.Date.
	 * 
	 * @throws Exception
	 * 
	 */
	public static java.sql.Date parseDate(String DATE) throws Exception {
		java.sql.Date date = null;
		date = parseDate(DATE, "dd/MM/yyyy");

		return date;
	}

	/**
	 * Method for parsing Dates.
	 * 
	 * @deprecated As of e-Gen 1.2. Use parse() instead.
	 */
	public static java.util.Date parseDate(java.util.Date du) {
		SimpleDateFormat sqlDate = new SimpleDateFormat("yyyy-MM-dd");
		String data = sqlDate.format(du);

		return java.sql.Date.valueOf(data);
	}

	/**
	 * Method for parsing String to java.sql.Date.
	 * 
	 * @throws Exception
	 * 
	 */
	public static java.sql.Date parseDate(String DATE, String PATTERN)
			throws Exception {
		java.sql.Date date = null;
		if ((PATTERN != null) && (PATTERN.length() > 0)
				&& !PATTERN.equalsIgnoreCase("null")) {
			try {
				java.util.Date dt = parse(DATE, PATTERN);
				if (dt != null) {
					date = new java.sql.Date(dt.getTime());
				}
			} catch (Exception e) {
				throw e;
			}
		} else {
			date = parseDate(DATE);
		}

		return date;
	}

	/**
	 * Method for parsing String to java.sql.Timestamp.
	 * 
	 * @throws Exception
	 * 
	 */
	public static java.sql.Timestamp parseTimestamp(String DATE)
			throws Exception {
		java.sql.Timestamp date = null;
		date = parseTimestamp(DATE, "dd/MM/yyyy HH:mm:ss");

		return date;
	}

	/**
	 * Method for parsing String to java.sql.Timestamp.
	 * 
	 * @throws Exception
	 * 
	 */
	public static java.sql.Timestamp parseTimestamp(String DATE, String PATTERN)
			throws Exception {
		java.sql.Timestamp date = null;
		if ((PATTERN != null) && (PATTERN.length() > 0)
				&& !PATTERN.equalsIgnoreCase("null")) {
			try {
				java.util.Date dt = parse(DATE, PATTERN);
				if (dt != null) {
					date = new java.sql.Timestamp(dt.getTime());
				}
			} catch (Exception e) {
				throw e;
			}
		} else {
			date = parseTimestamp(DATE);
		}

		return date;
	}

	/**
	 * Method for parsing String to java.sql.Timestamp.
	 * 
	 * @throws Exception
	 * 
	 */
	public static java.sql.Timestamp parseTimestamp(Date DATE) throws Exception {
		java.sql.Timestamp date = null;
		date = parseTimestamp(DATE, Constantes.DATA_FORMATO);

		return date;
	}

	/**
	 * Method for parsing String to java.sql.Timestamp.
	 * 
	 * @throws Exception
	 * 
	 */
	public static java.sql.Timestamp parseTimestamp(Date dt, String PATTERN)
			throws Exception {
		java.sql.Timestamp date = null;
		if ((PATTERN != null) && (PATTERN.length() > 0)
				&& !PATTERN.equalsIgnoreCase("null")) {
			try {
				if (dt != null) {
					date = new java.sql.Timestamp(dt.getTime());
				}
			} catch (Exception e) {
				throw e;
			}
		} else {
			date = parseTimestamp(dt);
		}

		return date;
	}

	/**
	 * Method for parsing String to java.sql.Time.
	 * 
	 * @throws Exception
	 * 
	 */
	public static java.sql.Time parseTime(String DATE) throws Exception {
		java.sql.Time date = null;
		date = parseTime(DATE, "HH:mm:ss");

		return date;
	}

	/**
	 * Method for parsing String to java.sql.Time.
	 * 
	 * @throws Exception
	 * 
	 */
	public static java.sql.Time parseTime(String DATE, String PATTERN)
			throws Exception {
		java.sql.Time date = null;
		if ((PATTERN != null) && (PATTERN.length() > 0)
				&& !PATTERN.equalsIgnoreCase("null")) {
			try {
				java.util.Date dt = parse(DATE, PATTERN);
				if (dt != null) {
					date = new java.sql.Time(dt.getTime());
				}
			} catch (Exception e) {
				throw e;
			}
		} else {
			date = parseTime(DATE);
		}

		return date;
	}

	/**
	 * Method for getting the number of days between two dates.
	 */
	public static int subDate(java.util.Date BEFORE, java.util.Date AFTER) {
		long dif = (((((AFTER.getTime() - BEFORE.getTime()) / 1000) / 24) / 60) / 60);

		return (new Long(dif)).intValue();
	}

	/**
	 * Method for getting the number of days between a date and today.
	 */
	public static int subDateToday(java.util.Date DATE) {
		long dif = ((((((new java.util.Date()).getTime() - DATE.getTime()) / 1000) / 24) / 60) / 60);

		return (new Long(dif)).intValue();
	}

	/**
	 * Method for adding days in a date.
	 */
	public static java.util.Date addDate(java.util.Date DATE, int DAYS) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(DATE);
		calendar.add(Calendar.DATE, DAYS);

		return FormatDate.parseDate(calendar.getTime());
	}

	/**
	 * * Retorna data resultado da some dos dias com a data passada como
	 * parâmetro
	 */
	public static java.util.Date anoAnterior(java.util.Date DATA) {
		java.util.Date resultado = null;
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(DATA);
		int year = calendar.get(Calendar.YEAR);
		calendar.set(Calendar.YEAR, year - 1);
		resultado = (java.util.Date) FormatDate.parseDate(calendar.getTime());

		return resultado;
	}

	/**
	 * Method for getting the last month.
	 */
	public static java.util.Date mesAnterior(java.util.Date DATA) {
		java.util.Date resultado = null;
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(DATA);
		int month = calendar.get(Calendar.MONTH);
		calendar.set(Calendar.MONTH, month - 1);
		resultado = (java.util.Date) FormatDate.parseDate(calendar.getTime());

		return resultado;
	}

	/**
	 * Method for getting the first day of month.
	 */
	public static java.util.Date getFirstDayOfMonth(java.util.Date DATA) {
		java.util.Date resultado = null;
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(DATA);
		int month = calendar.get(Calendar.MONTH);
		int year = calendar.get(Calendar.YEAR);
		calendar.set(year, month, 01);
		resultado = (java.util.Date) FormatDate.parseDate(calendar.getTime());

		return resultado;
	}

	/**
	 * Method for getting the last day of month.
	 */
	public static java.util.Date getLastDayOfMonth(java.util.Date DATA) {
		java.util.Date resultado = null;
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(DATA);
		int month = calendar.get(Calendar.MONTH);
		int year = calendar.get(Calendar.YEAR);
		if (month == 0) {
			calendar.set(year, month, 31);
		} else if (month == 1) {
			calendar.set(year, month, 28);
		} else if (month == 2) {
			calendar.set(year, month, 31);
		} else if (month == 3) {
			calendar.set(year, month, 30);
		} else if (month == 4) {
			calendar.set(year, month, 31);
		} else if (month == 5) {
			calendar.set(year, month, 30);
		} else if (month == 6) {
			calendar.set(year, month, 31);
		} else if (month == 7) {
			calendar.set(year, month, 31);
		} else if (month == 8) {
			calendar.set(year, month, 30);
		} else if (month == 9) {
			calendar.set(year, month, 31);
		} else if (month == 10) {
			calendar.set(year, month, 30);
		} else if (month == 11) {
			calendar.set(year, month, 31);
		}
		resultado = (java.util.Date) FormatDate.parseDate(calendar.getTime());

		return resultado;
	}

	/**
	 * Method for getting the timestamp in "dd/MM/yyyy HH:mm:ss" format.
	 * 
	 * @throws Exception
	 */
	public static String getTimestamp() throws Exception {
		String str = "";
		try {
			SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
			str = df.format(new java.util.Date(System.currentTimeMillis()));

			return str;
		} catch (Exception e) {
			throw e;
		}
	}

	/**
	 * Method for getting the time in "HH:mm:ss" format.
	 * 
	 * @throws Exception
	 */
	public static String getTime() throws Exception {
		String str = "";
		try {
			SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");
			str = df.format(new java.util.Date(System.currentTimeMillis()));

			return str;
		} catch (Exception e) {
			throw e;
		}
	}
}
