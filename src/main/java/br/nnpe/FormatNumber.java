package br.nnpe;


import java.text.DecimalFormat;

/**
 * Class: FormatNumber
 * <p>
 * 
 * This class is responsable for all number format and conversions.
 * 
 * @author Anderson Teixeira & Ronaldo Costa <anderson@lojasmaia.com.br,
 *         ronaldo@lojasmaia.com.br>
 * @version 2.2 (18-nov-2003)
 * @since 1.0 (22-oct-2002)
 * 
 */
public class FormatNumber extends Object {

	/**
	 * Method for formating a double number in customized format.<br>
	 * The format patterns can be verified in java.text.DecimalFormat.
	 */
	public static String format(double NUMBER, String FORMAT) {
		String FORMAT1 = FORMAT;
		if (FORMAT1 == null || FORMAT1.length() == 0) {
			FORMAT1 = "0.00";
		}
		DecimalFormat df = new DecimalFormat(FORMAT1);
		String number = df.format(NUMBER);
		return number;
	}

	/**
	 * Method for formating an int number in customized format.<br>
	 * The format patterns can be verified in java.text.DecimalFormat.
	 */
	public static String format(int NUMBER, String FORMAT) {
		String FORMAT1 = FORMAT;
		if (FORMAT1 == null || FORMAT1.length() == 0) {
			FORMAT1 = "000";
		}
		DecimalFormat df = new DecimalFormat(FORMAT1);
		String number = df.format(NUMBER);
		return number;
	}

	/**
	 * Method for formating a long number in customized format.<br>
	 * The format patterns can be verified in java.text.DecimalFormat.
	 */
	public static String format(long NUMBER, String FORMAT) {
		String FORMAT1 = FORMAT;
		if (FORMAT1 == null || FORMAT1.length() == 0) {
			FORMAT1 = "000";
		}
		DecimalFormat df = new DecimalFormat(FORMAT1);
		String number = df.format(NUMBER);
		return number;
	}

	/**
	 * Method for formating a java.lang.Integer number in customized format.<br>
	 * The format patterns can be verified in java.text.DecimalFormat.
	 */
	public static String format(java.lang.Integer NUMBER, String FORMAT) {
		String FORMAT1 = FORMAT;
		if (NUMBER == null) {
			return "";
		}
		if (FORMAT1 == null || FORMAT1.length() == 0) {
			FORMAT1 = "000";
		}
		DecimalFormat df = new DecimalFormat(FORMAT1);
		String number = df.format(NUMBER);
		return number;
	}

	/**
	 * Method for formating a java.lang.Double number in customized format.<br>
	 * The format patterns can be verified in java.text.DecimalFormat.
	 */
	public static String format(java.lang.Double NUMBER, String FORMAT) {
		String FORMAT1 = FORMAT;
		if (NUMBER == null) {
			return "";
		}
		if (FORMAT1 == null || FORMAT1.length() == 0) {
			FORMAT1 = "0.00";
		}
		DecimalFormat df = new DecimalFormat(FORMAT1);
		String number = df.format(NUMBER);
		return number;
	}

	/**
	 * Method for formating a java.lang.Float number in customized format.<br>
	 * The format patterns can be verified in java.text.DecimalFormat.
	 */
	public static String format(java.lang.Float NUMBER, String FORMAT) {
		String FORMAT1 = FORMAT;
		if (NUMBER == null) {
			return "";
		}
		if (FORMAT1 == null || FORMAT1.length() == 0) {
			FORMAT1 = "0.00";
		}
		DecimalFormat df = new DecimalFormat(FORMAT1);
		String number = df.format(NUMBER);
		return number;
	}

	/**
	 * Method for formating a java.lang.Double number in customized format.<br>
	 * The format patterns can be verified in java.text.DecimalFormat.
	 */
	public static String format(java.lang.Byte NUMBER, String FORMAT) {
		String FORMAT1 = FORMAT;
		if (NUMBER == null) {
			return "";
		}
		if (FORMAT1 == null || FORMAT1.length() == 0) {
			FORMAT1 = "0";
		}
		DecimalFormat df = new DecimalFormat(FORMAT1);
		String number = df.format(NUMBER);
		return number;
	}

	/**
	 * Method for formating a java.lang.Double number in customized format.<br>
	 * The format patterns can be verified in java.text.DecimalFormat.
	 */
	public static String format(java.lang.Short NUMBER, String FORMAT) {
		String FORMAT1 = FORMAT;
		if (NUMBER == null) {
			return "";
		}
		if (FORMAT1 == null || FORMAT1.length() == 0) {
			FORMAT1 = "0";
		}
		DecimalFormat df = new DecimalFormat(FORMAT1);
		String number = df.format(NUMBER);
		return number;
	}

	/**
	 * Method for formating a java.lang.Long number in customized format.<br>
	 * The format patterns can be verified in java.text.DecimalFormat.
	 */
	public static String format(java.lang.Long NUMBER, String FORMAT) {
		String FORMAT1 = FORMAT;
		if (NUMBER == null) {
			return "";
		}
		if (FORMAT1 == null || FORMAT1.length() == 0) {
			FORMAT1 = "000";
		}
		DecimalFormat df = new DecimalFormat(FORMAT1);
		String number = df.format(NUMBER);
		return number;
	}

	/**
	 * Method for formating a java.math.BigDecimal number in customized format.<br>
	 * The format patterns can be verified in java.text.DecimalFormat.
	 */
	public static String format(java.math.BigDecimal NUMBER, String FORMAT) {
		String FORMAT1 = FORMAT;
		if (NUMBER == null) {
			return "";
		}
		if (FORMAT1 == null || FORMAT1.length() == 0) {
			FORMAT1 = "0.00";
		}
		DecimalFormat df = new DecimalFormat(FORMAT1);
		String number = df.format(NUMBER);
		return number;
	}

	/**
	 * Method for formating a double number in "0.00" format.
	 */
	public static String format(double NUMBER) {
		return format(NUMBER, "0.00");
	}

	/**
	 * Method for formating an int number in "000" format.
	 */
	public static String format(int NUMBER) {
		return format(NUMBER, "000");
	}

	/**
	 * Method for formating a long number in "000" format.
	 */
	public static String format(long NUMBER) {
		return format(NUMBER, "000");
	}

	/**
	 * Method for formating a java.lang.Double number in "0.00" format.
	 */
	public static String format(java.lang.Double NUMBER) {
		return format(NUMBER, "0.00");
	}

	/**
	 * Method for formating a java.lang.Float number in "0.00" format.
	 */
	public static String format(java.lang.Float NUMBER) {
		return format(NUMBER, "0.00");
	}

	/**
	 * Method for formating a java.lang.Integer number in "000" format.
	 */
	public static String format(java.lang.Integer NUMBER) {
		return format(NUMBER, "000");
	}

	/**
	 * Method for formating a java.lang.Byte number in "000" format.
	 */
	public static String format(java.lang.Byte NUMBER) {
		return format(NUMBER, "000");
	}

	/**
	 * Method for formating a java.lang.Short number in "000" format.
	 */
	public static String format(java.lang.Short NUMBER) {
		return format(NUMBER, "000");
	}

	/**
	 * Method for formating a java.lang.Long number in "000" format.
	 */
	public static String format(java.lang.Long NUMBER) {
		return format(NUMBER, "000");
	}

	/**
	 * Method for formating a java.math.BigDecimal number in "0.00" format.
	 */
	public static String format(java.math.BigDecimal NUMBER) {
		return format(NUMBER, "0.00");
	}

	/**
	 * Method for formating a double number in Currency ($ #,##0.00) format.
	 */
	public static String currency(double NUMBER) {
		String number = format(NUMBER, "#,##0.00");
		return "$ " + number;
	}

	/**
	 * Method for formating a double number in Percent (##0.00000) format.
	 */
	public static String percent(double NUMBER) {
		String number = format(NUMBER, "##0.00000");
		return number;
	}

	/**
	 * Method for parse an int number.<br>
	 * parameter: String
	 */
	public static int parseInt(String NUMBER) {
		String NUMBER1 = NUMBER;
		int resultado = 0;
		if (NUMBER1 != null && NUMBER1.length() > 0) {
			NUMBER1 = NUMBER1.trim();
			NUMBER1 = checkInt(NUMBER1);
			if (NUMBER1.indexOf(".") > 0) {
				NUMBER1 = NUMBER1.substring(0, NUMBER1.indexOf("."));
			}
			try {
				resultado = (new Integer(NUMBER1)).intValue();
			} catch (Exception e) {
				resultado = 0;
			}
		}
		return resultado;
	}

	/**
	 * Method for parse a java.langInteger number.<br>
	 * parameter: String
	 */
	public static Integer parseIntegerWrapper(String NUMBER) {
		String NUMBER1 = NUMBER;
		Integer resultado = null;
		if (NUMBER1 != null && NUMBER1.length() > 0) {
			NUMBER1 = NUMBER1.trim();
			NUMBER1 = checkInt(NUMBER1);
			if (NUMBER1.indexOf(".") > 0) {
				NUMBER1 = NUMBER1.substring(0, NUMBER1.indexOf("."));
			}
			try {
				resultado = new Integer(NUMBER1);
			} catch (Exception e) {
				resultado = null;
			}
		}
		return resultado;
	}

	/**
	 * Method for parse an long number.<br>
	 * parameter: String
	 */
	public static long parseLong(String NUMBER) {
		String NUMBER1 = NUMBER;
		long resultado = 0;
		if (NUMBER1 != null && NUMBER1.length() > 0) {
			NUMBER1 = NUMBER1.trim();
			NUMBER1 = checkInt(NUMBER1);
			if (NUMBER1.indexOf(".") > 0) {
				NUMBER1 = NUMBER1.substring(0, NUMBER1.indexOf("."));
			}
			try {
				resultado = (new Long(NUMBER1)).longValue();
			} catch (Exception e) {
				resultado = 0;
			}
		}
		return resultado;
	}

	/**
	 * Method for parse an java.lang.Uong number.<br>
	 * parameter: String
	 */
	public static Long parseLongWrapper(String NUMBER) {
		String NUMBER1 = NUMBER;
		Long resultado = null;
		if (NUMBER1 != null && NUMBER1.length() > 0) {
			NUMBER1 = NUMBER1.trim();
			NUMBER1 = checkInt(NUMBER1);
			if (NUMBER1.indexOf(".") > 0) {
				NUMBER1 = NUMBER1.substring(0, NUMBER1.indexOf("."));
			}
			try {
				resultado = new Long(NUMBER1);
			} catch (Exception e) {
				resultado = null;
			}
		}
		return resultado;
	}

	/**
	 * Method for parse an long number.<br>
	 * parameter: String
	 */
	public static java.math.BigDecimal parseBigDecimal(String NUMBER) {
		String NUMBER1 = NUMBER;
		java.math.BigDecimal resultado = null;
		if (NUMBER1 != null && NUMBER1.length() > 0) {
			NUMBER1 = NUMBER1.trim();
			NUMBER1 = checkDouble(NUMBER1);
			try {
				resultado = new java.math.BigDecimal(NUMBER1);
			} catch (Exception e) {
				resultado = null;
			}
		}
		return resultado;
	}

	/**
	 * Method for parse an int number.<br>
	 * parameter: double
	 */
	public static int parseInt(double NUMBER) {
		try {
			Double D = new Double(NUMBER);
			return D.intValue();
		} catch (Exception e) {
			return 0;
		}
	}

	/**
	 * Method for parse a double number.<br>
	 * parameter: String
	 */
	public static double parseDouble(String NUMBER) {
		String NUMBER1 = NUMBER;
		double resultado = 0;
		if (NUMBER1 != null && NUMBER1.length() > 0) {
			NUMBER1 = NUMBER1.trim();
			NUMBER1 = checkDouble(NUMBER1);
			try {
				resultado = (new Double(NUMBER1)).doubleValue();
			} catch (Exception e) {
				resultado = 0;
			}
		}
		return resultado;
	}

	/**
	 * Method for parse a java.lang.Double number.<br>
	 * parameter: String
	 */
	public static Double parseDoubleWrapper(String NUMBER) {
		String NUMBER1 = NUMBER;
		Double resultado = null;
		if (NUMBER1 != null && NUMBER1.length() > 0) {
			NUMBER1 = NUMBER1.trim();
			NUMBER1 = checkDouble(NUMBER1);
			try {
				resultado = new Double(NUMBER1);
			} catch (Exception e) {
				resultado = null;
			}
		}
		return resultado;
	}

	public static String fillMode(String valor, String fill) {
		if (valor.length() < fill.length()) {
			return fill.substring(0, fill.length() - valor.length())
					+ valor.trim();
		}
		return valor;
	}

	public static String fillMode(double valor, String fill) {
		String texto = "" + (int) valor;
		return (fillMode(texto, fill));
	}

	public static String fillMode(java.lang.Integer valor, String fill) {
		String texto = "" + (Integer) valor;
		return (fillMode(texto, fill));
	}

	/*
	 * Only PDF
	 */
	public static String fillMode(int TEXT, int LENGTH, String CODE) {
		return fillMode(TEXT + "", LENGTH, CODE);
	}

	public static String fillMode(double TEXT, int LENGTH, String CODE) {
		return fillMode(TEXT + "", LENGTH, CODE);
	}

	public static String fillMode(String TEXT, int LENGTH, String CODE) {
		StringBuilder code = new StringBuilder(TEXT);
		if (code.length() > 0) {
			if (code.length() > LENGTH) {
				code = new StringBuilder(code.substring(0, LENGTH));
			} else if (code.length() < LENGTH) {
				int c = LENGTH - code.length();
				for (int i = 0; i < c; i++) {
					code.append(CODE);
				}
			}
		}
		return code.toString();
	}

	public static Integer toInteger(String NUMBER) {
		String NUMBER1 = NUMBER;
		Integer resultado = null;
		if (NUMBER1 != null && NUMBER1.length() > 0) {
			NUMBER1 = NUMBER1.trim();
			NUMBER1 = checkInt(NUMBER1);
			resultado = new Integer(NUMBER1);
		}
		return resultado;
	}

	public static Integer toInt(String NUMBER) {
		String NUMBER1 = NUMBER;
		Integer resultado = null;
		if (NUMBER1 != null && NUMBER1.length() > 0) {
			NUMBER1 = NUMBER1.trim();
			NUMBER1 = checkInt(NUMBER1);
			resultado = new Integer(NUMBER1);
		}
		return resultado;
	}

	public static Double toDouble(String NUMBER) {
		String NUMBER1 = NUMBER;
		Double resultado = null;
		if (NUMBER1 != null && NUMBER1.length() > 0) {
			NUMBER1 = NUMBER1.trim();
			NUMBER1 = checkDouble(NUMBER1);
			resultado = new Double(NUMBER1);
		}
		return resultado;
	}

	public static Long toLong(String NUMBER) {
		String NUMBER1 = NUMBER;
		Long resultado = null;
		if (NUMBER1 != null && NUMBER1.length() > 0) {
			NUMBER1 = NUMBER1.trim();
			NUMBER1 = checkInt(NUMBER1);
			resultado = new Long(NUMBER1);
		}
		return resultado;
	}

	public static java.math.BigDecimal toBigDecimal(String NUMBER) {
		String NUMBER1 = NUMBER;
		java.math.BigDecimal resultado = null;
		if (NUMBER1 != null && NUMBER1.length() > 0) {
			NUMBER1 = NUMBER1.trim();
			NUMBER1 = checkDouble(NUMBER1);
			resultado = new java.math.BigDecimal(NUMBER1);
		}
		return resultado;
	}

	/**
	 * Method for checking a double number format.
	 */
	private static String checkDouble(String NUMBER) {
		String NUMBER1 = NUMBER;
		if (NUMBER1 != null && NUMBER1.length() > 0) {
			if (NUMBER1.indexOf(",") != -1 || NUMBER1.indexOf(".") != -1) {
				int limit = NUMBER1.length() - 4;
				StringBuilder new_number = new StringBuilder();
				for (int i = 0; i < NUMBER1.length(); i++) {
					if (NUMBER1.charAt(i) == '.' || NUMBER1.charAt(i) == ',') {
						if (i > limit) {
							new_number.append(".");
						} else if (NUMBER1.substring(i + 1).indexOf(".") == -1
								&& NUMBER1.substring(i + 1).indexOf(",") == -1) {
							new_number.append(".");
						}
					} else {
						new_number.append(NUMBER1.charAt(i));
					}
				}
				NUMBER1 = new_number.toString();
			}
		}
		return NUMBER1;
	}

	/**
	 * Method for checking a int number format.
	 */
	private static String checkInt(String NUMBER) {
		String NUMBER1 = NUMBER;
		if (NUMBER1 != null && NUMBER1.length() > 0) {
			if (NUMBER1.indexOf(",") != -1 || NUMBER1.indexOf(".") != -1) {
				int limit = NUMBER1.length() - 4;
				StringBuilder new_number = new StringBuilder();
				for (int i = 0; i < NUMBER1.length(); i++) {
					if (!(NUMBER1.charAt(i) == '.' || NUMBER1.charAt(i) == ',')) {
						new_number.append(NUMBER1.charAt(i));
					}
				}
				NUMBER1 = new_number.toString();
			}
		}
		return NUMBER1;
	}
}
