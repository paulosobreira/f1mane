package br.f1mane.recursos.idiomas;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.MessageFormat;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.PropertyResourceBundle;
import java.util.Set;

import javax.swing.JOptionPane;

import br.nnpe.Logger;
import br.f1mane.paddock.servlet.JogoServidor;
import br.f1mane.recursos.CarregadorRecursos;

public class Lang {

	private static PropertyResourceBundle bundle;
	private static Map<String, PropertyResourceBundle> mapaBundle = new HashMap<String, PropertyResourceBundle>();
	private static String sufix;
	private static boolean srvgame;

	public Lang() throws IOException {
	}

	public static boolean isSrvgame() {
		return srvgame;
	}

	public static void setSrvgame(boolean srvgame) {
		Lang.srvgame = srvgame;
	}

	public static void main(String[] args) throws IOException {
		sufix = "it";
		System.out.println(Lang.msg("TIPO_PNEU_MOLE"));
	}

	public static void mudarIdioma(String sufix_) {
		sufix = sufix_;
		bundle = null;
		iniciaBundle();
	}

	public static String msg(String key) {
		if (srvgame) {
			return "¢" + key + "¢";
		}

		iniciaBundle();
		if (key == null || key.isEmpty()) {
			return "";
		}
		try {
			return bundle.getString(key);
		} catch (Exception e) {
			return key;
		}
	}

	public static String msgRest(String key) {
		iniciaBundle();
		if (key == null || key.isEmpty()) {
			return "";
		}
		try {
			return bundle.getString(key);
		} catch (Exception e) {
			return key;
		}
	}

	public static String msg(String key, Object[] strings) {
		if (srvgame) {
			StringBuilder buffer = new StringBuilder();
			buffer.append("¢").append(key);
			for (int i = 0; i < strings.length; i++) {
				buffer.append("¬");
				String stringIn = strings[i].toString();
				if (stringIn.contains("¢")) {
					buffer.append(stringIn.replace("¢", "¥"));
				} else {
					buffer.append(stringIn);
				}
			}
			buffer.append("¢");
			return buffer.toString();
		}
		iniciaBundle();
		if (key == null || key.isEmpty()) {
			return "";
		}
		try {
			MessageFormat messageFormat = new MessageFormat(
					bundle.getString(key));
			return messageFormat.format(strings);
		} catch (Exception e) {
			return key;
		}
	}

	public static String msgRest(String key, Object[] strings) {
		iniciaBundle();
		if (key == null || key.isEmpty()) {
			return "";
		}
		try {
			MessageFormat messageFormat = new MessageFormat(
					bundle.getString(key));
			return messageFormat.format(strings);
		} catch (Exception e) {
			return key;
		}
	}

	public static String decodeTexto(String string) {
		String[] array = string.split("¢");
		StringBuilder retorno = new StringBuilder();
		for (int i = 0; i < array.length; i++) {
			if (i % 2 == 1)
				retorno.append(microDecode(array[i]));
			else
				retorno.append((array[i]));
		}
		return retorno.toString();
	}

	public static String decodeTextoKey(String string, String idioma) {
		if (string == null) {
			return null;
		}
		String[] array = string.split("¢");
		StringBuilder retorno = new StringBuilder();
		for (int i = 0; i < array.length; i++) {
			if (i % 2 == 1)
				retorno.append(microDecodeKey(array[i], idioma));
			else
				retorno.append((array[i]));
		}
		return retorno.toString();
	}

	private static String microDecodeKey(String string, String idioma) {
		if (string.contains("¬")) {
			String[] sp = string.split("¬");
			String key = sp[0];
			Object[] params = new Object[sp.length - 1];
			for (int i = 1; i < sp.length; i++) {
				String msp = sp[i];
				if (msp.contains("¥")) {
					msp = Lang.decodeTexto(msp.replace("¥", "¢"));
				}
				params[i - 1] = msp;
			}
			return Lang.msgKey(key, params, idioma);
		} else {
			return Lang.msgKey(string, idioma);
		}
	}

	public static String msgKey(String key, Object[] strings, String idioma) {
		PropertyResourceBundle bundle = null;
		if (idioma == null) {
			iniciaBundle();
			bundle = Lang.bundle;
		} else {
			bundle = mapaBundle.get(idioma);
		}
		if (key == null || key.isEmpty()) {
			return "";
		}
		try {
			MessageFormat messageFormat = new MessageFormat(
					bundle.getString(key));
			return messageFormat.format(strings);
		} catch (Exception e) {
			return key;
		}
	}

	public static String msgKey(String key, String idioma) {
		PropertyResourceBundle bundle = null;
		if (idioma == null) {
			iniciaBundle();
			bundle = Lang.bundle;
		} else {
			bundle = mapaBundle.get(idioma);
		}
		if (key == null || key.isEmpty()) {
			return "";
		}
		try {
			return bundle.getString(key);
		} catch (Exception e) {
			return key;
		}
	}

	private static String microDecode(String string) {
		if (string.contains("¬")) {
			String[] sp = string.split("¬");
			String key = sp[0];
			Object[] params = new Object[sp.length - 1];
			for (int i = 1; i < sp.length; i++) {
				String msp = sp[i];
				if (msp.contains("¥")) {
					msp = Lang.decodeTexto(msp.replace("¥", "¢"));
				}
				params[i - 1] = msp;
			}
			return Lang.msg(key, params);
		} else {
			return Lang.msg(string);
		}
	}

	public static String key(String mensagen) {
		iniciaBundle();
		Enumeration enumeration = bundle.getKeys();
		while (enumeration.hasMoreElements()) {
			String key = (String) enumeration.nextElement();
			String msg = bundle.getString(key);
			if (msg.equals(mensagen)) {
				return key;
			}
		}
		return "";
	}

	private static void iniciaBundle() {
		try {
			if (bundle == null) {
				Locale locale = Locale.getDefault();
				if (locale != null && sufix == null) {
					sufix = locale.getLanguage();
				}
				bundle = carregraBundleMensagens(sufix);
				if (bundle == null) {
					Logger.logar("iniciaBundle sufix " + sufix);
					return;
				}
			}
		} catch (Exception e) {
			Logger.logarExept(e);
		}
	}

	public static PropertyResourceBundle carregraBundleMensagens(String sufix)
			throws Exception, IOException {
		PropertyResourceBundle propertyResourceBundle = mapaBundle.get(sufix);
		if (propertyResourceBundle == null) {
			String load = "idiomas/mensagens_" + sufix + ".properties";
			InputStream inputStream = CarregadorRecursos.getCarregadorRecursos(false)
					.recursoComoStream(load);
			if (inputStream == null) {
				Logger.logar("inputStream == null para " + load);
				return null;
			}
			validaProperties(inputStream);
			inputStream = CarregadorRecursos.recursoComoStream(load);
			propertyResourceBundle = new PropertyResourceBundle(inputStream);
			mapaBundle.put(sufix, propertyResourceBundle);
		}
		return propertyResourceBundle;
	}

	private static void validaProperties(InputStream inputStream)
			throws Exception {
		BufferedReader bufferedReader = new BufferedReader(
				new InputStreamReader(inputStream));
		Set keys = new HashSet();
		Set values = new HashSet();
		String line = bufferedReader.readLine();
		while (line != null && line.contains("=")) {
			String[] splits = line.split("=");
			if (keys.contains(splits[0])) {
				throw new Exception("Repeated Key : " + splits[0]);
			} else {
				keys.add(splits[0]);
			}
			if (values.contains(splits[1])) {
				throw new Exception("Repeated Value : " + splits[1]);
			} else {
				values.add(splits[1]);
			}
			line = bufferedReader.readLine();
		}

	}

	public static String getSufix() {
		return sufix;
	}

}
