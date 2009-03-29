package sowbreira.f1mane.controles;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Enumeration;
import java.util.PropertyResourceBundle;

import sowbreira.f1mane.recursos.CarregadorRecursos;

public class ControleIdiomas {

	private static PropertyResourceBundle bundle;

	public ControleIdiomas() throws IOException {
		bundle = new PropertyResourceBundle(CarregadorRecursos
				.recursoComoStream("idiomas/mensagens.properties"));
	}

	public static void main(String[] args) throws IOException {
		new ControleIdiomas();
		System.out.println(obterMsg("TesteFormat", new Object[] { 123, 312 }));
	}

	public static String obterMsg(String key) {
		iniciaBundle();
		return bundle.getString(key);
	}

	public static String obterMsg(String key, Object[] strings) {
		iniciaBundle();
		MessageFormat messageFormat = new MessageFormat(bundle.getString(key));
		return messageFormat.format(strings);
	}

	public static String obterKey(String mensagen) {
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
				bundle = new PropertyResourceBundle(CarregadorRecursos
						.recursoComoStream("idiomas/mensagens.properties"));
			} catch (IOException e) {
				e.printStackTrace();
			}
	}
}
