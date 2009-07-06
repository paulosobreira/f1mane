package br.nnpe;

import java.io.Serializable;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Stores dates and perform date arithmetic.
 * 
 * This is another date class, but more convenient that <tt>java.util.Date</tt>
 * or <tt>java.util.Calendar</tt>
 * 
 * @version 1.20 5 Oct 1998
 * @author Cay Horstmann e Paulo Sobreira
 */
public class Dia implements Cloneable, Serializable {
	private static final long serialVersionUID = 5884622348312431542L;

	public static int SUNDAY = 1;

	public static int MONDAY = 2;

	public static int TUESDAY = 3;

	public static int WEDNESDAY = 4;

	public static int THURSDAY = 5;

	public static int FRIDAY = 6;

	public static int SATURDAY = 7;

	public static final Dia diaNulo = new Dia(0, 0, 0);

	public final static DecimalFormat prec4 = new DecimalFormat("0000");

	public final static DecimalFormat prec2 = new DecimalFormat("00");

	/** @serial */
	private int day;

	/** @serial */
	private int month;

	/** @serial */
	private int year;

	/**
	 * Constructs today's date
	 */
	public Dia() {
		GregorianCalendar todaysDate = new GregorianCalendar();
		year = todaysDate.get(Calendar.YEAR);
		month = todaysDate.get(Calendar.MONTH) + 1;
		day = todaysDate.get(Calendar.DAY_OF_MONTH);
	}

	public Dia(String data) {
		this(System.currentTimeMillis());

		if (!isNullOrEmpty(data)) {
			Timestamp timestamp;

			try {
				timestamp = converteStringTimestamp(data);

				GregorianCalendar todaysDate = new GregorianCalendar();
				todaysDate.setTimeInMillis(timestamp.getTime());
				year = todaysDate.get(Calendar.YEAR);
				month = todaysDate.get(Calendar.MONTH) + 1;
				day = todaysDate.get(Calendar.DAY_OF_MONTH);
			} catch (Exception e) {
				Logger.logar("Erro criando Dia para string : " + data);
			}
		}
	}

	/**
	 * 
	 * Se o valor for igual a 10 usar dd/MM/yyyy caso não, usar yyyy-MM-dd
	 * HH:mm:ss.mmm
	 */
	public static Timestamp converteStringTimestamp(String valor)
			throws Exception {
		if (valor == null) {
			return null;
		}

		if ((valor.indexOf('/') != -1) && (valor.length() < 10)) {
			DecimalFormat format = new DecimalFormat("00");
			String[] parts = valor.split("/");
			StringBuffer buffer = new StringBuffer();

			for (int i = 0; i < parts.length; i++) {
				buffer.append(format.format(Long.parseLong(parts[i])));

				if (i < (parts.length - 1)) {
					buffer.append("/");
				}
			}

			valor = buffer.toString();
		}

		return ((valor.length() == 10) ? new java.sql.Timestamp(
				parseDate(valor).getTime()) : parseTimestamp(valor,
				"yyyy-MM-dd HH:mm:ss"));
	}

	public static java.sql.Date parseDate(String DATE) throws Exception {
		java.sql.Date date = null;
		date = parseDate(DATE, "dd/MM/yyyy");

		return date;
	}

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

	public static java.util.Date parse(String DATE) throws Exception {
		return parse(DATE, "dd/MM/yyyy");
	}

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

	public static java.sql.Timestamp parseTimestamp(String DATE)
			throws Exception {
		java.sql.Timestamp date = null;
		date = parseTimestamp(DATE, "dd/MM/yyyy HH:mm:ss");

		return date;
	}

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

	public static boolean isNullOrEmpty(String campo) {
		return ((campo == null) || "".equals(campo));
	}

	public Dia(int day, int month, int year) {
		this.day = day;
		this.month = month;
		this.year = year;
	}

	public Dia(Timestamp timestamp) {
		if (timestamp == null) {
			return;
		}

		GregorianCalendar todaysDate = new GregorianCalendar();
		todaysDate.setTimeInMillis(timestamp.getTime());
		year = todaysDate.get(Calendar.YEAR);
		month = todaysDate.get(Calendar.MONTH) + 1;
		day = todaysDate.get(Calendar.DAY_OF_MONTH);
	}

	public Dia(Date data) {
		this(data.getTime());
	}

	public Dia(long time) {
		GregorianCalendar todaysDate = new GregorianCalendar();
		todaysDate.setTimeInMillis(time);
		year = todaysDate.get(Calendar.YEAR);
		month = todaysDate.get(Calendar.MONTH) + 1;
		day = todaysDate.get(Calendar.DAY_OF_MONTH);
	}

	public int getCompetencia() {
		return zeroOuInt(prec4.format(getYear()) + ""
				+ prec2.format(getMonth()));
	}

	public static int zeroOuInt(Object obj) {
		try {
			if (obj instanceof String) {
				String string = (String) obj;

				return (((string == null) || "".equals(string)) ? 0 : Integer
						.parseInt(string));
			}

			return (((obj == null)) ? 0 : Integer.parseInt((String) obj));
		} catch (Exception e) {
			return 0;
		}
	}

	public void advancedMonth() {
		int mesAtual = getMonth();
		int mes = getMonth();
		while (mes == mesAtual) {
			advance(1);
			mesAtual = getMonth();
		}
	}

	public Boolean isDayOfWeek() {
		int dia = getDayOfWeek();
		if (dia > 1 && dia < 7) {
			return true;
		} else {
			return false;
		}
	}

	public int getDayOfWeek() {
		Dia dia = new Dia(day, month, year);
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(dia.toTimestamp().getTime());
		return calendar.get(Calendar.DAY_OF_WEEK);
	}

	/**
	 * Com a hora atual
	 * 
	 * @return
	 */
	public Timestamp toTimestamp() {
		Calendar calendar = Calendar.getInstance();
		calendar.set(year, month - 1, day);

		return new Timestamp(calendar.getTimeInMillis());
	}

	public Timestamp toTimestampIniDia() {
		Calendar calendar = Calendar.getInstance();
		calendar.set(year, month - 1, day);
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		return new Timestamp(calendar.getTimeInMillis());
	}

	public Timestamp toTimestampFimDia() {
		Calendar calendar = Calendar.getInstance();
		calendar.set(year, month - 1, day);
		calendar.set(Calendar.HOUR_OF_DAY, 23);
		calendar.set(Calendar.MINUTE, 59);
		calendar.set(Calendar.SECOND, 59);
		calendar.set(Calendar.MILLISECOND, 999);
		return new Timestamp(calendar.getTimeInMillis());
	}

	public static void main(String[] args) {
		Dia d = new Dia(1, 5, 2008);
		Dia di = new Dia(1, 4, 2009);

		if (di.getMonth() >= d.getMonth()) {
			Logger.logar(di.getMonth() - d.getMonth());
		} else {
			int a = (12 - d.getMonth()) + di.getMonth();
			Logger.logar(a);
		}

		// ontem.advance(-1);
		// Logger.logar(hj.daysBetween(ontem));
		// Logger.logar(hj.toInt());
		// Logger.logar(ontem.toInt());
		//
		// Logger.logar(hj.maiorQue(ontem));
		// Logger.logar(ontem.maiorQue(hj));
		// Logger.logar(hj.maiorQue(hj));
		// Logger.logar("-------------------------------------------------");
		// Logger.logar(hj.maiorIgualA(ontem));
		// Logger.logar(ontem.maiorIgualA(hj));
		// Logger.logar(hj.maiorIgualA(hj));
		// Logger.logar("-------------------------------------------------");
		// Logger.logar(hj.menorQue(ontem));
		// Logger.logar(ontem.menorQue(hj));
		// Logger.logar(hj.menorQue(hj));
		// Logger.logar("-------------------------------------------------");
		// Logger.logar(hj.menorIgualA(ontem));
		// Logger.logar(ontem.menorIgualA(hj));
		// Logger.logar(hj.menorIgualA(hj));
	}

	public boolean maiorQue(Dia dia) {
		int value = daysBetween(dia);

		if (value <= 0) {
			return false;
		} else {
			return true;
		}
	}

	public boolean menorQue(Dia dia) {
		int value = daysBetween(dia);

		if (value >= 0) {
			return false;
		} else {
			return true;
		}
	}

	public boolean maiorIgualA(Dia dia) {
		int value = daysBetween(dia);

		if (value < 0) {
			return false;
		} else {
			return true;
		}
	}

	public boolean menorIgualA(Dia dia) {
		int value = daysBetween(dia);

		if (value > 0) {
			return false;
		} else {
			return true;
		}
	}

	public String getMesDescricao(int mes) {
		String m = "";
		if (mes == 1) {
			m = "JANEIRO";
		} else if (mes == 2) {
			m = "FEVEREIRO";
		} else if (mes == 3) {
			m = "MARÇO";
		} else if (mes == 4) {
			m = "ABRIL";
		} else if (mes == 5) {
			m = "MAIO";
		} else if (mes == 6) {
			m = "JUNHO";
		} else if (mes == 7) {
			m = "JULHO";
		} else if (mes == 8) {
			m = "AGOSTO";
		} else if (mes == 9) {
			m = "SETEMBRO";
		} else if (mes == 10) {
			m = "OUTUBRO";
		} else if (mes == 11) {
			m = "NOVEMBRO";
		} else if (mes == 12) {
			m = "DEZEMBRO";
		}

		return m;
	}

	public int getMesNum(String desc) {
		int m = 0;
		if (desc.equalsIgnoreCase("JANEIRO")) {
			m = 1;
		} else if (desc.equalsIgnoreCase("FEVEREIRO")) {
			m = 2;
		} else if (desc.equalsIgnoreCase("MARÇO")) {
			m = 3;
		} else if (desc.equalsIgnoreCase("ABRIL")) {
			m = 4;
		} else if (desc.equalsIgnoreCase("MAIO")) {
			m = 5;
		} else if (desc.equalsIgnoreCase("JUNHO")) {
			m = 6;
		} else if (desc.equalsIgnoreCase("JULHO")) {
			m = 7;
		} else if (desc.equalsIgnoreCase("AGOSTO")) {
			m = 8;
		} else if (desc.equalsIgnoreCase("SETEMBRO")) {
			m = 9;
		} else if (desc.equalsIgnoreCase("OUTUBRO")) {
			m = 10;
		} else if (desc.equalsIgnoreCase("NOVEMBRO")) {
			m = 11;
		} else if (desc.equalsIgnoreCase("DEZEMBRO")) {
			m = 12;
		}

		return m;
	}

	/**
	 * Advances this day by n days. For example. d.advance(30) adds thirdy days
	 * to d
	 * 
	 * @param n
	 *            the number of days by which to change this day (can be < 0)
	 */
	public void advance(int n) {
		fromJulian(toJulian() + n);
	}

	/**
	 * Gets the day of the month
	 * 
	 * @return the day of the month (1...31)
	 */
	public int getDay() {
		return day;
	}

	/**
	 * Gets the month
	 * 
	 * @return the month (1...12)
	 */
	public int getMonth() {
		return month;
	}

	/**
	 * Gets the year
	 * 
	 * @return the year (counting from 0, <i>not</i> from 1900)
	 */
	public int getYear() {
		return year;
	}

	/**
	 * Gets the weekday
	 */
	public int weekday() {
		return ((toJulian() + 1) % 7) + 1;
	}

	/**
	 * The number of days between this and day parameter
	 * 
	 * @param b
	 *            any date
	 * @return the number of days between this and day parameter and b (> 0 if
	 *         this day comes after b)
	 */
	public int daysBetween(Dia b) {
		return toJulian() - b.toJulian();
	}

	/**
	 * A string representation of the day
	 * 
	 * @return a string representation of the day
	 */
	public String toString() {
		DecimalFormat df = new DecimalFormat("00");

		return df.format(day) + "/" + df.format(month) + "/" + year;
	}

	/**
	 * Usar somente para pesquisa. Testado só no postgres.
	 * 
	 * @return
	 */
	public String getDataBanco() {
		return "'" + year + "-" + prec2.format(month) + "-" + prec2.format(day)
				+ "%'";
	}

	public String getDataBanco23h() {
		return "'" + year + "-" + prec2.format(month) + "-" + prec2.format(day)
				+ " 23:59:59.999'";
	}

	public String getDataBancoAnoMes() {
		return "'" + year + "-" + prec2.format(month) + "%'";
	}

	public int toInt() {
		return Integer.parseInt("" + year + prec2.format(month)
				+ prec2.format(day));
	}

	/**
	 * Makes a bitwise copy of a Day object
	 * 
	 * @return a bitwise copy of a Day object
	 */
	public Object clone() {
		try {
			return super.clone();
		} catch (CloneNotSupportedException e) { // this shouldn't happen,
			// since we are Cloneable

			return null;
		}
	}

	/**
	 * Compares this Day against another object
	 * 
	 * @param obj
	 *            another object
	 * @return true if the other object is identical to this Day object
	 */
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}

		if (!getClass().equals(obj.getClass())) {
			return false;
		}

		Dia b = (Dia) obj;

		return (day == b.day) && (month == b.month) && (year == b.year);
	}

	/**
	 * Computes the number of days between two dates
	 * 
	 * @return true iff this is a valid date
	 */
	private boolean isValid() {
		Dia t = new Dia();
		t.fromJulian(this.toJulian());

		return (t.day == day) && (t.month == month) && (t.year == year);
	}

	/**
	 * @return The Julian day number that begins at noon of this day Positive
	 *         year signifies A.D., negative year B.C. Remember that the year
	 *         after 1 B.C. was 1 A.D.
	 * 
	 * A convenient reference point is that May 23, 1968 noon is Julian day
	 * 2440000.
	 * 
	 * Julian day 0 is a Monday.
	 * 
	 * This algorithm is from Press et al., Numerical Recipes in C, 2nd ed.,
	 * Cambridge University Press 1992
	 */
	private int toJulian() {
		int jy = year;

		if (year < 0) {
			jy++;
		}

		int jm = month;

		if (month > 2) {
			jm++;
		} else {
			jy--;
			jm += 13;
		}

		int jul = (int) (java.lang.Math.floor(365.25 * jy)
				+ java.lang.Math.floor(30.6001 * jm) + day + 1720995.0);

		int IGREG = 15 + (31 * (10 + (12 * 1582)));

		// Gregorian Calendar adopted Oct. 15, 1582
		if ((day + (31 * (month + (12 * year)))) >= IGREG) // change over to
		// Gregorian
		// calendar
		{
			int ja = (int) (0.01 * jy);
			jul += (2 - ja + (int) (0.25 * ja));
		}

		return jul;
	}

	/**
	 * Converts a Julian day to a calendar date
	 * 
	 * This algorithm is from Press et al., Numerical Recipes in C, 2nd ed.,
	 * Cambridge University Press 1992
	 * 
	 * @param j
	 *            the Julian date
	 */
	private void fromJulian(int j) {
		int ja = j;

		int JGREG = 2299161;

		/*
		 * the Julian date of the adoption of the Gregorian calendar
		 */
		if (j >= JGREG) /*
						 * cross-over to Gregorian Calendar produces this
						 * correction
						 */{
			int jalpha = (int) (((float) (j - 1867216) - 0.25) / 36524.25);
			ja += ((1 + jalpha) - (int) (0.25 * jalpha));
		}

		int jb = ja + 1524;
		int jc = (int) (6680.0 + (((float) (jb - 2439870) - 122.1) / 365.25));
		int jd = (int) ((365 * jc) + (0.25 * jc));
		int je = (int) ((jb - jd) / 30.6001);
		day = jb - jd - (int) (30.6001 * je);
		month = je - 1;

		if (month > 12) {
			month -= 12;
		}

		year = jc - 4715;

		if (month > 2) {
			--year;
		}

		if (year <= 0) {
			--year;
		}
	}
}