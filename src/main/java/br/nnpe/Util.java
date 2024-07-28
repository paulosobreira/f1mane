package br.nnpe;

import java.awt.Color;
import java.awt.Graphics2D;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Blob;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.PropertyResourceBundle;
import java.util.Set;
import java.util.Vector;

import javax.swing.JFormattedTextField;
import javax.swing.text.MaskFormatter;

import org.hibernate.Session;

public class Util {
	public static Locale loc_brasil = new Locale("pt", "BR");
	public static final Vector conectivos;
	public static final DecimalFormat decimalFormat = new DecimalFormat("0.00");
	public static final String MASCARA_CPF = "###.###.###-##";
	public static final String MASCARA_CNPJ = "##.###.###/####-##";

	public static boolean substVogais = true;

	static {
		conectivos = new Vector();
		conectivos.addElement("DE");
		conectivos.addElement("E");
		conectivos.addElement("DA");
		conectivos.addElement("DO");
		conectivos.addElement("DOS");
		conectivos.addElement("DAS");
	}

	private static final Set<Class<?>> WRAPPER_TYPES = getWrapperTypes();

	public static boolean isWrapperType(Class<?> clazz) {
		return !WRAPPER_TYPES.contains(clazz);
	}

	private static Set<Class<?>> getWrapperTypes() {
		Set<Class<?>> ret = new HashSet<Class<?>>();
		ret.add(Boolean.class);
		ret.add(Character.class);
		ret.add(Byte.class);
		ret.add(Short.class);
		ret.add(Integer.class);
		ret.add(Long.class);
		ret.add(Float.class);
		ret.add(Double.class);
		ret.add(Void.class);
		ret.add(String.class);
		return ret;
	}

	public static int calculaLarguraText(String txt, Graphics2D g2d) {
		int largura = 0;
		for (int i = 0; i < txt.length(); i++) {
			largura += g2d.getFontMetrics().charWidth(txt.charAt(i));
		}
		return largura;
	}

	public static List removePersistBag(List list, Session session) {
		List ret = new LinkedList();
		for (Iterator iterator = list.iterator(); iterator.hasNext();) {
			Object object = (Object) iterator.next();
			if (session != null) {
				session.evict(object);
			}
			ret.add(object);
		}
		return ret;
	}

	public static int inteiro(int d) {
		return d;
	}

	public static int inteiro(double d) {
		return (int) Math.round(d);
	}

	// returns a deep copy of an object
	static public Object deepCopy(Object oldObj) throws Exception {
		ObjectOutputStream oos = null;
		ObjectInputStream ois = null;
		try {
			ByteArrayOutputStream bos = new ByteArrayOutputStream(); // A
			oos = new ObjectOutputStream(bos); // B
			// serialize and pass the object
			oos.writeObject(oldObj); // C
			oos.flush(); // D
			ByteArrayInputStream bin = new ByteArrayInputStream(
					bos.toByteArray()); // E
			ois = new ObjectInputStream(bin); // F
			// return the new object
			return ois.readObject(); // G
		} catch (Exception e) {
			System.out.println("Exception in ObjectCloner = " + e);
			throw (e);
		} finally {
			oos.close();
			ois.close();
		}
	}

	public static boolean isNullOrEmpty(String campo) {
		return ((campo == null) || campo.trim().isEmpty());
	}

	public static Date alteraData(Date date, int qtdDias) {
		java.util.Calendar calendar = java.util.Calendar.getInstance();
		calendar.setTime(date);
		calendar.add(Calendar.DATE, qtdDias);
		return new Date(calendar.getTimeInMillis());
	}

	public static boolean validaIntervaloData(Dia dataInicial, Dia dataFinal) {
        return !dataInicial.maiorQue(dataFinal);
	}

	public static boolean validaIntervaloData(java.sql.Date dataInicial,
			java.sql.Date dataFinal) {
        return dataInicial.compareTo(dataFinal) <= 0;
	}

	public static void validaIntervaloData(String dataInicial, String dataFinal)
			throws Exception {
		if (!validaIntervaloData(dataInicial, dataFinal,
				Constantes.DATA_FORMATO)) {
			throw new Exception("Intervalo de datas invalido");
		}
	}

	public static boolean validaIntervaloData(String dataInicial,
			String dataFinal, String formato) throws Exception {
		java.sql.Date dt1 = FormatDate.parseDate(dataInicial, formato);
		java.sql.Date dt2 = FormatDate.parseDate(dataFinal, formato);

		return validaIntervaloData(dt1, dt2);
	}

	public static boolean validaIntervaloData(Timestamp data_inicio,
			Timestamp data_fim) throws Exception {
		String inicio = FormatDate.format(data_inicio);
		String fim = FormatDate.format(data_fim);

		return validaIntervaloData(inicio, fim, Constantes.DATA_FORMATO);
	}

	public static String removeQuotes(String text) {
		StringBuilder ret = new StringBuilder();

		if (text != null) {
			for (int i = 0; i < text.length(); i++) {
				if (text.substring(i, i + 1).equals("\"")) {
					ret.append("'");
				} else {
					ret.append(text.substring(i, i + 1));
				}
			}
		}

		return ret.toString();
	}

	public static String formatNumber(String patern, double number) {
		DecimalFormat form = new DecimalFormat(patern);

		return form.format(number);
	}

	public static InputStream getBinaryStream(ResultSet rs, String fieldName)
			throws Exception {
		try {
			Blob blob = rs.getBlob(fieldName);

			return blob.getBinaryStream();
		} catch (Exception e) {
			throw new Exception("getBinaryStream", e);
		}
	}

	public static String hex(byte[] array) {
		StringBuilder sb = new StringBuilder();

		for (int i = 0; i < array.length; ++i) {
			sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100)
					.substring(1, 3));
		}

		return sb.toString();
	}

	public static String md5(String message)
			throws NoSuchAlgorithmException, UnsupportedEncodingException {
		MessageDigest md = MessageDigest.getInstance("MD5");

		return hex(md.digest(message.getBytes("CP1252")));
	}

	/**
	 * Substitui o ponto de uma String por uma outra qualquer.
	 * 
	 * @param string
	 * @param replacement
	 * @return
	 */
	public static String substituirPonto(String string, String replacement) {
		StringBuilder buffer = new StringBuilder();

		for (int i = 0; i < string.length(); i++) {
			char value = string.charAt(i);

			if (value == '.') {
				buffer.append(replacement);
			} else {
				buffer.append(value);
			}
		}

		return buffer.toString();
	}

	/**
	 * Retona o valor Integer caso diferente de zero.
	 */
	public static Integer integerOrNull(int num) {
		if (num != 0) {
			return new Integer(num);
		}

		return null;
	}

	/**
	 * Retona o valor Double caso diferente de zero.
	 */
	public static Double doubleOrNull(double num) {
		if (num != 0) {
			return new Double(num);
		}

		return null;
	}

	public static double doubleOr0(Double num) {
		if (num == null) {
			return 0;
		}

		return double2Decimal(num.doubleValue());
	}

	public static int intOr0(Integer i) {
		if (i == null) {
			return 0;
		} else {
			return i.intValue();
		}
	}

	public static int intOr0(String i) {
		try {
			return Integer.parseInt(i);
		} catch (Exception e) {
		}
		return 0;
	}

	public static boolean isNumero(String num) {
		try {
			Integer.parseInt(num);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		System.out.println(md5("f1mane"));
	}

	public static int intervalo(int val1, int val2) {

		return (val1 + (int) (Math.random() * (1 + val2 - val1)));
	}

	public static double intervalo(double val1, double val2) {
		return (val1 + (Math.random() * (0.1 + val2 - val1)));
	}

	public static double double2Decimal(double dob) {
		String doubleValue = String.valueOf(dob);

		if (doubleValue.indexOf("E-") != -1) {
			return 0;
		}

		return Double.parseDouble(decimalFormat.format(dob).replace(',', '.'));
	}

	public static void serializarEmArquivo(Object o) {
		String filename = o.getClass().getName();
		File file = new File(filename);

		try {
			FileOutputStream fileOutputStream = new FileOutputStream(file);

			ObjectOutputStream objectOutputStream = new ObjectOutputStream(
					fileOutputStream);
			objectOutputStream.writeObject(o);
			fileOutputStream.flush();
		} catch (Exception e) {
			Logger.logarExept(e);
		}
	}

	public static String mascarar(String valor, String mascara)
			throws ParseException {
		MaskFormatter formatter = new MaskFormatter(mascara);
		JFormattedTextField textField = new JFormattedTextField();
		formatter.install(textField);
		textField.setText(valor);

		return textField.getText();
	}

	public static String mascararCpf(String cpf) throws ParseException {
		return mascarar(cpf, MASCARA_CPF);
	}

	public static String mascararCnpj(String cnpj) throws ParseException {
		return mascarar(cnpj, MASCARA_CNPJ);
	}

	public static String substVogais(String name) {
		if(!substVogais){
			return name;
		}
		StringBuilder retorno = new StringBuilder();
		boolean subst = false;
		for (int i = 0; i < name.length(); i++) {
			if (i == (name.length() - 1)) {
				retorno.append(name.charAt(i));
			} else if (name.charAt(i) == 'a' && !subst) {
				retorno.append('e');
				subst = true;
			} else if (name.charAt(i) == 'e' && !subst) {
				retorno.append('i');
				subst = true;
			} else if (name.charAt(i) == 'i' && !subst) {
				retorno.append('a');
				subst = true;
			} else if (name.charAt(i) == 'o' && !subst) {
				retorno.append('u');
				subst = true;
			} else if (name.charAt(i) == 'u' && !subst) {
				retorno.append('o');
				subst = true;
			}
			/*
			 * else if (name.charAt(i) == 'A' && !subst) { retorno.append('E');
			 * subst = true; } else if (name.charAt(i) == 'E' && !subst) {
			 * retorno.append('I'); subst = true; } else if (name.charAt(i) ==
			 * 'I' && !subst) { retorno.append('A'); subst = true; } else if
			 * (name.charAt(i) == 'O' && !subst) { retorno.append('U'); subst =
			 * true; } else if (name.charAt(i) == 'U' && !subst) {
			 * retorno.append('O'); subst = true; }
			 */
			else {
				// subst = true;
				retorno.append(name.charAt(i));
			}

		}
		return retorno.toString();
	}

	public static int larguraTexto(String msg, Graphics2D g2d) {
		int largura = 0;
		for (int i = 0; i < msg.length(); i++) {
			if (g2d == null) {
				largura += 7;
			} else {
				largura += g2d.getFontMetrics().charWidth(msg.charAt(i));
			}
		}
		return largura;
	}

	public static boolean processaValorPontosCarreira(int valorAutal,
			int proximoValor, Numero numero) {
		Double numeroVal = numero.getNumero();
		int inc = 0;
		if (proximoValor < 600) {
			inc = 1;
			if (valorAutal == 600) {
				inc = 2;
			}
		} else if (proximoValor < 700) {
			inc = 2;
			if (valorAutal == 700) {
				inc = 10;
			}
		} else if (proximoValor < 800) {
			inc = 10;
			if (valorAutal == 800) {
				inc = 20;
			}
		} else if (proximoValor < 900) {
			inc = 20;
			if (valorAutal == 900) {
				inc = 50;
			}
		} else if (proximoValor < 999) {
			inc = 50;
		}
		if ((proximoValor - valorAutal) > 0) {
			numeroVal = Double.valueOf(numeroVal.doubleValue() - inc);
			numero.setNumero(numeroVal);
            return (numeroVal.doubleValue() - inc) >= 0;
		} else {
			numeroVal = Double.valueOf(numeroVal.doubleValue() + inc);
			numero.setNumero(numeroVal);
		}
        return numeroVal.doubleValue() > 0;
	}
	public static Color criarCorAleatoria() {

		int intervalo = Util.intervalo(1, 10);

		switch (intervalo) {
			case 1 :
				return Color.RED;
			case 2 :
				return Color.BLUE;
			case 3 :
				return Color.GREEN;
			case 4 :
				return Color.YELLOW;
			case 5 :
				return Color.ORANGE;
			case 6 :
				return Color.BLACK;
			case 7 :
				return Color.GRAY;
			case 8 :
				return Color.DARK_GRAY;
			case 9 :
				return Color.LIGHT_GRAY;
			case 10 :
				return Color.WHITE;

			default :
				break;
		}

		return Color.WHITE;
	}

	public static String extrairNumeros(String string) {
		return string.replaceAll("\\D+", "");
	}

	public static Long extrairNumerosLong(String string) {
		try {
			return new Long(extrairNumeros(string));
		} catch (Exception e) {
		}
		return null;
	}

	public static Map<String, String> bundle2Map(
			PropertyResourceBundle bundle) {
		Map<String, String> map = new HashMap<String, String>();

		Enumeration<String> keys = bundle.getKeys();
		while (keys.hasMoreElements()) {
			String key = keys.nextElement();
			map.put(key, bundle.getString(key));
		}

		return map;
	}
	public static Color hex2Rgb(String colorStr) {
		return new Color(Integer.valueOf(colorStr.substring(1, 3), 16).intValue(),
                Integer.valueOf(colorStr.substring(3, 5), 16).intValue(),
                Integer.valueOf(colorStr.substring(5, 7), 16).intValue());
	}

	public static String rgb2hex(Color c) {
		return String.format("%02x%02x%02x", Integer.valueOf(c.getRed()), Integer.valueOf(c.getGreen()),
                Integer.valueOf(c.getBlue()));
	}
}
