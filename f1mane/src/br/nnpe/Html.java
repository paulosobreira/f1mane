/* Html.java
 * Criado em 25/10/2005.
 */
package br.nnpe;

import java.sql.Timestamp;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author Rafael Carneiro (<a href="mailto:rafael@portaljava.com">e-mail</a>)
 * @author Paulo Sobreira
 */
public class Html {

	static int superSize = 5;
	public static final String CHECKBOX_ON = "on";

	// ITALO COmENTAR
	/**
	 * @author Sobreira
	 */
	public static boolean checkBoxMarcado(String value) {
		return ("on".equalsIgnoreCase(value) || "1".equalsIgnoreCase(value));
	}

	/**
	 * Este método recebe uma String como parâmetro e retorna uma String com uma
	 * tag HTML center (centralizando-a).
	 * 
	 * @param -
	 *            str.
	 * @return - String com código HTML.
	 */
	public static String center(String str) {
		StringBuffer buffer = new StringBuffer();

		buffer.append("<center>").append(str).append("</center>");

		return buffer.toString();
	}

	/**
	 * Este método recebe um int como parâmetro e retorna uma String com uma tag
	 * HTML center (centralizando-a).
	 * 
	 * @param -
	 *            str.
	 * @return - String com código HTML.
	 */
	public static String center(int str) {
		return center(String.valueOf(str));
	}

	/**
	 * Este método recebe uma String como parâmetro e retorna uma String com uma
	 * tag HTML bold (negrito).
	 * 
	 * @param -
	 *            str.
	 * @return - String com código HTML.
	 */
	public static String bold(String str) {
		StringBuffer buffer = new StringBuffer();

		buffer.append("<b>").append(str).append("</b>");

		return buffer.toString();
	}

	/**
	 * Método que recebe duas Strings por parâmetro e retorna a tag HTML maito.
	 * 
	 * @param email -
	 *            E-mail
	 * @param nome -
	 *            Pode ser tanto o próprio e-mail como qualquer outro nome
	 * @return
	 */
	public static String mailto(String email, String nome) {
		StringBuffer buffer = new StringBuffer();

		buffer.append("<a href=\"").append(email).append("\"").append(
				" target=\"_blank\">").append(nome).append("</a>");

		return null;
	}

	/**
	 * Tag HTML font color
	 * 
	 * @param cor -
	 *            cor da fonte
	 * @param texto -
	 *            texto para a cor
	 * @return
	 */
	public static String fontColor(String cor, String texto) {
		StringBuffer buffer = new StringBuffer();

		buffer.append("<font color='").append(cor).append("'>").append(texto)
				.append("</font>");

		return buffer.toString();
	}

	public static String azul(String texto) {
		return fontColor("BLUE", texto);
	}

	public static String verde(String texto) {
		return fontColor("GREEN", texto);
	}

	public static String red(String texto) {
		return fontColor("RED", texto);
	}

	public static String txtRedBold(String texto) {
		return bold(fontColor("RED", texto));
	}

	/**
	 * Tag HTML blink
	 * 
	 * @param texto -
	 *            texto dentro da tag blink
	 * @return
	 */
	public static String blink(String texto) {
		StringBuffer buffer = new StringBuffer();

		buffer.append("<blink>").append(texto).append("</blink>");

		return buffer.toString();
	}

	/**
	 * Tag HTML font size
	 * 
	 * @param tamanho -
	 *            tamanho da fonte
	 * @param texto -
	 *            texto ao ser alterado
	 * @return
	 */
	public static String fontSize(int tamanho, String texto) {
		StringBuffer buffer = new StringBuffer();

		buffer.append("<font size=\"").append(tamanho).append("\">").append(
				texto).append("</font>");

		return buffer.toString();
	}

	/**
	 * Gera um componete de seleção unica do tipo combobox
	 * 
	 * @param nomeComponente -
	 *            propriedade name
	 * @param conteudo -
	 *            Uma lista contendo arrays de String onde
	 *            String[]{"Label","valor"};
	 * @param selected -
	 *            posição da lista conteudo que vea estar slecionada.
	 * @return
	 */
	public static String comboBox(String nomeComponente, List conteudo,
			int selected) {
		return comboBox(nomeComponente, conteudo, selected, true);
	}

	public static String comboBox(String nomeComponente, List conteudo,
			int selected, boolean ativo) {
		StringBuffer buffer = new StringBuffer();

		buffer.append("<select name=\"" + nomeComponente + "\"");

		if (!ativo) {
			buffer.append(" disabled=\"disabled\" ");
		}

		buffer.append("/>");

		for (int i = 0; i < conteudo.size(); i++) {
			String[] strings = (String[]) conteudo.get(i);
			buffer.append("<option ");

			if (i == selected) {
				buffer.append("selected ");
			}

			buffer.append("value = \"" + strings[1] + "\">");
			buffer.append(strings[0]);
		}

		buffer.append("</select>");

		return buffer.toString();
	}

	public static String align(String campo, String align) {
		return "<div align=\"" + align + "\">" + campo + "</div>";
	}

	public static String superRed(String texto) {
		StringBuffer buffer = new StringBuffer();

		buffer.append("<font size='").append(superSize)
				.append("' color='red'>").append(texto).append("</font>");

		return buffer.toString();
	}

	public static String superOrange(String texto) {
		StringBuffer buffer = new StringBuffer();

		buffer.append("<font size='").append(superSize).append(
				"' color='#FF8C00'>").append(texto).append("</font>");

		return buffer.toString();

	}

	public static String orange(String texto) {
		return bold(fontColor("#FF8C00", texto));
	}

	public static String superBlack(String texto) {
		StringBuffer buffer = new StringBuffer();
		buffer.append("<font size='").append(superSize).append(
				"' color='black'>").append(texto).append("</font>");
		return buffer.toString();
	}

	public static String superGreen(String texto) {
		StringBuffer buffer = new StringBuffer();
		buffer.append("<font size='").append(superSize).append(
				"' color='green'>").append(texto).append("</font>");
		return buffer.toString();
	}

	public static String cinza(String texto) {
		return fontColor("Gray", texto);
	}

	public static String superDarkRed(String texto) {
		StringBuffer buffer = new StringBuffer();
		buffer.append("<font size='").append(superSize).append(
				"' color='#8B0000'>").append(texto).append("</font>");
		return buffer.toString();
	}

	public static String silver(String texto) {
		StringBuffer buffer = new StringBuffer();
		buffer.append("<font color='#B5B5B5'>").append(texto).append("</font>");
		return buffer.toString();
	}

	public static String msgClima(String texto) {
		StringBuffer buffer = new StringBuffer();
		buffer.append("<font size='").append(superSize).append(
				"' color='#4682B4'>").append(texto).append("</font>");
		return buffer.toString();
	}

	public static String superBlue(String texto) {
		StringBuffer buffer = new StringBuffer();
		buffer.append("<font size='").append(superSize).append(
				"' color='blue'>").append(texto).append("</font>");
		return buffer.toString();
	}

	public static String saftyCar(String texto) {
		StringBuffer buffer = new StringBuffer();

		buffer.append("<font size='").append(superSize).append(
				"' color='#F53D00'>").append(texto).append("</font>");
		return Html.bold(buffer.toString());
	}
}
