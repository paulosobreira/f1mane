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
import br.nnpe.Logger;

public class Lang {

	private static PropertyResourceBundle bundle;
	private static String sufix;
	private static boolean srvgame;
	private static String mutex = "";

	public Lang() throws IOException {
	}

	public static boolean isSrvgame() {
		return srvgame;
	}

	public static void setSrvgame(boolean srvgame) {
		Lang.srvgame = srvgame;
	}

	public static void main(String[] args) throws IOException {
		new Lang();
		// Logger.logar(msg("TesteFormat", new Object[] { 123, 312 }));
		// String[] array = "asd¢111¢qweqw¢22¢werwer ¢3¢¢4¢".split("¢");
		// for (int i = 0; i < array.length; i++) {
		// if (i % 2 == 1)
		// Logger.logar(array[i]);
		// }
		srvgame = true;
		String enc = Lang.msg("003", new String[] { "S.Vettel", "8.218", "0",
				Lang.msg("TIPO_PNEU_MOLE") });
		Logger.logar("enc : " + enc);
		sufix = "en";
		srvgame = false;
		Logger.logar("dec : " + decodeTexto(enc));
		System.out
				.println(decodeTexto("<b><font  color='#FF8C00'>¢003¬S.Vettel¬8.218¬0¬¢TIPO_PNEU_MOLE¢¢</font></b>"));
		Locale locale = Locale.getDefault();

		Logger.logar(locale.getLanguage());
	}

	public static void mudarIdioma(String sufix_) {
		synchronized (mutex) {
			sufix = sufix_;
			synchronized (mutex) {
				bundle = null;
				iniciaBundle();
			}
		}
	}

	public static String msg(String key) {
		synchronized (mutex) {
			if (srvgame) {
				return "¢" + key + "¢";
			}

			iniciaBundle();
			if (key == null || "".equals(key)) {
				return "";
			}
			try {
				return bundle.getString(key);
			} catch (Exception e) {
				return key;
			}

		}
	}

	public static String msg(String key, Object[] strings) {
		synchronized (mutex) {
			if (srvgame) {
				StringBuffer buffer = new StringBuffer();
				buffer.append("¢" + key);
				for (int i = 0; i < strings.length; i++) {
					buffer.append("¬");
					String stringIn = strings[i].toString();
					if (stringIn.contains("¢")) {
						buffer.append(stringIn.replace("¢", "£"));
					} else {
						buffer.append(stringIn);
					}
				}
				buffer.append("¢");
				return buffer.toString();
			}
			iniciaBundle();
			if (key == null || "".equals(key)) {
				return "";
			}
			try {
				MessageFormat messageFormat = new MessageFormat(bundle
						.getString(key));
				return messageFormat.format(strings);
			} catch (Exception e) {
				return key;
			}
		}
	}

	public static String decodeTexto(String string) {
		synchronized (mutex) {
			String[] array = string.split("¢");
			StringBuffer retorno = new StringBuffer();
			for (int i = 0; i < array.length; i++) {
				if (i % 2 == 1)
					retorno.append(microDecode(array[i]));
				else
					retorno.append((array[i]));
			}
			return retorno.toString();
		}
	}

	private static String microDecode(String string) {
		if (string.contains("¬")) {
			String[] sp = string.split("¬");
			String key = sp[0];
			Object[] params = new Object[sp.length - 1];
			for (int i = 1; i < sp.length; i++) {
				String msp = sp[i];
				if (msp.contains("£")) {
					msp = Lang.decodeTexto(msp.replace("£", "¢"));
				}
				params[i - 1] = msp;
			}
			return Lang.msg(key, params);
		} else {
			return Lang.msg(string);
		}
	}

	public static String key(String mensagen) {
		synchronized (mutex) {
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
				if (inputStream == null) {
					load = "idiomas/mensagens_en.properties";
					inputStream = CarregadorRecursos.recursoComoStream(load);
				}
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
			Logger.logarExept(e);
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

}
