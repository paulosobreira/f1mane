/* Html.java
 * Criado em 25/10/2005.
 */
package br.nnpe;


/**
 * @author Rafael Carneiro (<a href="mailto:rafael@portaljava.com">e-mail</a>)
 * @author Paulo Sobreira
 */
public class Html {

	static int superSize = 4;

	/**
	 * Este método recebe uma String como parâmetro e retorna uma String com uma
	 * tag HTML bold (negrito).
	 * 
	 * @param - str.
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
	 * @param email
	 *            - E-mail
	 * @param nome
	 *            - Pode ser tanto o próprio e-mail como qualquer outro nome
	 * @return
	 */
	public static String mailto(String email, String nome) {
		StringBuffer buffer = new StringBuffer();

		buffer.append("<a href=\"").append(email).append("\"")
				.append(" target=\"_blank\">").append(nome).append("</a>");

		return null;
	}

	/**
	 * Tag HTML font color
	 * 
	 * @param cor
	 *            - cor da fonte
	 * @param texto
	 *            - texto para a cor
	 * @return
	 */
	public static String fontColor(String cor, String texto) {
		StringBuffer buffer = new StringBuffer();

		buffer.append("<font  color='").append(cor).append("'>").append(texto)
				.append("</font>");

		return buffer.toString();
	}

	public static String azul(String texto) {
		return fontColor("#2D62A8", texto);
	}

	public static String green(String texto) {
		return fontColor("#008D25", texto);
	}

	public static String red(String texto) {
		return fontColor("#FE0000", texto);
	}

	public static String txtRedBold(String texto) {
		return bold(fontColor("#FE0000", texto));
	}

	/**
	 * Tag HTML font size
	 * 
	 * @param tamanho
	 *            - tamanho da fonte
	 * @param texto
	 *            - texto ao ser alterado
	 * @return
	 */
	public static String fontSize(int tamanho, String texto) {
		StringBuffer buffer = new StringBuffer();

		buffer.append("<font  size=\"").append(tamanho).append("\">")
				.append(texto).append("</font>");

		return buffer.toString();
	}

	public static String align(String campo, String align) {
		return "<div align=\"" + align + "\">" + campo + "</div>";
	}

	public static String sansSerif(int num) {
		return sansSerif(String.valueOf(num));
	}

	public static String sansSerif(String texto) {
		StringBuffer buffer = new StringBuffer();

		buffer.append("<font face='sans-serif' >").append(texto)
				.append("</font>");

		return buffer.toString();
	}

	public static String superRed(String texto) {
		StringBuffer buffer = new StringBuffer();

		buffer.append("<font  size='").append(superSize)
				.append("' color='#FE0000'>").append(texto).append("</font>");

		return buffer.toString();
	}

	public static String orange(String texto) {
		return bold(fontColor("#FF8C00", texto));
	}

	public static String superBlack(String texto) {
		StringBuffer buffer = new StringBuffer();
		buffer.append("<font  size='").append(superSize)
				.append("' color='black'>").append(texto).append("</font>");
		return buffer.toString();
	}

	public static String superGreen(String texto) {
		StringBuffer buffer = new StringBuffer();
		buffer.append("<font  size='").append(superSize)
				.append("' color='#008D25'>").append(texto).append("</font>");
		return buffer.toString();
	}

	public static String cinza(String texto) {
		return fontColor("Gray", texto);
	}

	public static String superDarkRed(String texto) {
		StringBuffer buffer = new StringBuffer();
		buffer.append("<font  size='").append(superSize)
				.append("' color='#8B0000'>").append(texto).append("</font>");
		return buffer.toString();
	}

	public static String silver(String texto) {
		StringBuffer buffer = new StringBuffer();
		buffer.append("<font  color='#B5B5B5'>").append(texto)
				.append("</font>");
		return buffer.toString();
	}

	public static String msgClima(String texto) {
		StringBuffer buffer = new StringBuffer();
		buffer.append("<font  size='").append(superSize)
				.append("' color='#4682B4'>").append(texto).append("</font>");
		return buffer.toString();
	}

	public static String superBlue(String texto) {
		StringBuffer buffer = new StringBuffer();
		buffer.append("<font  size='").append(superSize)
				.append("' color='#2D62A8'>").append(texto).append("</font>");
		return buffer.toString();
	}

	public static String saftyCar(String texto) {
		StringBuffer buffer = new StringBuffer();

		buffer.append("<font  size='").append(superSize)
				.append("' color='FF8C00'>").append(texto).append("</font>");
		return Html.bold(buffer.toString());
	}

	public static String driveThru(String msg) {
		StringBuffer buffer = new StringBuffer();
		buffer.append("<font bgcolor='black' size='").append(superSize)
				.append("' color='white'>").append(msg).append("</font>");
		return Html.bold(buffer.toString());
	}

	public static String tagsJava2d(String info) {
		StringBuffer ret = new StringBuffer();
		info = info.replaceAll("&nbsp;", " ");
		boolean ingnora = false;
		boolean achouDigito = false;
		boolean pulaDigito = true;
		for (int i = 0; i < info.length(); i++) {
			if (info.charAt(i) == '<') {
				ingnora = true;
				continue;
			}
			if (!achouDigito && !Character.isDigit(info.charAt(i))) {
				continue;
			} else {
				achouDigito = true;
			}

			if (pulaDigito && achouDigito && Character.isDigit(info.charAt(i))) {
				continue;
			}else{
				pulaDigito = false;
			}

			if (info.charAt(i) == '>') {
				ingnora = false;
				continue;
			}
			if (!ingnora) {
				ret.append(info.charAt(i));
			}
		}
		return ret.toString();
	}
}
