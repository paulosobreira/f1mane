package sowbreira.f1mane.recursos.idiomas;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.MessageFormat;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Locale;
import java.util.PropertyResourceBundle;
import java.util.Set;

import javax.swing.JOptionPane;

import sowbreira.f1mane.recursos.CarregadorRecursos;

public class Lang {

	private static PropertyResourceBundle bundle;
	private static String sufix;
	
	public Lang() throws IOException {
	}

	public static void main(String[] args) throws IOException {
		new Lang();
		System.out.println(msg("TesteFormat", new Object[] { 123, 312 }));
	}

	public static void mudarIdioma(String sufix_) {
		sufix = sufix_;
		synchronized (bundle) {
			bundle = null;
			iniciaBundle();
		}
	}

	public static String msg(String key) {
		iniciaBundle();
		if (key == null || "".equals(key)) {
			return "";
		}
		return bundle.getString(key);
	}

	public static String msg(String key, Object[] strings) {
		iniciaBundle();
		if (key == null || "".equals(key)) {
			return "";
		}
		MessageFormat messageFormat = new MessageFormat(bundle.getString(key));
		return messageFormat.format(strings);
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
				String load = "idiomas/mensagens_" + sufix + ".properties";
				InputStream inputStream = CarregadorRecursos
						.recursoComoStream(load);
				validaProperties(inputStream);
				inputStream = CarregadorRecursos.recursoComoStream(load);
				bundle = new PropertyResourceBundle(inputStream);
				if (bundle == null) {
					load = "idiomas/mensagens.properties_pt";
					inputStream = CarregadorRecursos.recursoComoStream(load);
					validaProperties(inputStream);
					inputStream = CarregadorRecursos.recursoComoStream(load);
					bundle = new PropertyResourceBundle(inputStream);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, e.getMessage(),
					"Message Properties Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	private static void validaProperties(InputStream inputStream)
			throws Exception {
		BufferedReader bufferedReader = new BufferedReader(
				new InputStreamReader(inputStream));
		Set keys = new HashSet();
		Set values = new HashSet();
		String line = bufferedReader.readLine();
		while (line != null) {
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
}
