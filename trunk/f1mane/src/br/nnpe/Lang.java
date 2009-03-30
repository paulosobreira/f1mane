package br.nnpe;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.MessageFormat;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.PropertyResourceBundle;
import java.util.Set;

import javax.swing.JOptionPane;

import sowbreira.f1mane.recursos.CarregadorRecursos;

public class Lang {

	private static PropertyResourceBundle bundle;

	public Lang() throws IOException {
		bundle = new PropertyResourceBundle(CarregadorRecursos
				.recursoComoStream("idiomas/mensagens.properties"));
	}

	public static void main(String[] args) throws IOException {
		new Lang();
		System.out.println(msg("TesteFormat", new Object[] { 123, 312 }));
	}

	public static String msg(String key) {
		iniciaBundle();
		return bundle.getString(key);
	}

	public static String msg(String key, Object[] strings) {
		iniciaBundle();
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
		if (bundle == null)
			try {
				validaProperties();
				bundle = new PropertyResourceBundle(CarregadorRecursos
						.recursoComoStream("idiomas/mensagens.properties"));
			} catch (Exception e) {
				e.printStackTrace();
				JOptionPane.showMessageDialog(null, "Message Properties Error",
						"Message Properties Error", JOptionPane.ERROR_MESSAGE);
			}
	}

	private static void validaProperties() throws Exception {
		BufferedReader bufferedReader = new BufferedReader(
				new InputStreamReader(CarregadorRecursos
						.recursoComoStream("idiomas/mensagens.properties")));
		Set keys = new HashSet();
		Set values = new HashSet();
		String line = bufferedReader.readLine();
		while (line != null) {
			String[] splits = line.split("=");
			if (keys.contains(splits[0])) {
				throw new Exception("Repeated Key" + splits[0]);
			} else {
				keys.add(splits[0]);
			}
			if (values.contains(splits[1])) {
				throw new Exception("Repeated Value" + splits[1]);
			} else {
				values.add(splits[1]);
			}
			line = bufferedReader.readLine();
		}

	}
}
