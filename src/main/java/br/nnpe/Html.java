/* Html.java
 * Criado em 25/10/2005.
 */
package br.nnpe;

/**
 * @author Rafael Carneiro (<a href="mailto:rafael@portaljava.com">e-mail</a>)
 * @author Paulo Sobreira
 */
public class Html {

	static final int superSize = 4;

	public static String negrito(String str) {
		StringBuilder buffer = new StringBuilder();

		buffer.append("<font  size=\"").append(superSize).append("\">")
				.append("<b>").append(str).append("</b>").append("</font>");

		return buffer.toString();
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
		StringBuilder buffer = new StringBuilder();

		buffer.append("<font  color='").append(cor).append("'>").append(texto)
				.append("</font>");

		return buffer.toString();
	}

	public static String txtRedBold(String texto) {
		return negrito(fontColor("#FE0000", texto));
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
		StringBuilder buffer = new StringBuilder();

		buffer.append("<font  size=\"").append(tamanho).append("\">")
				.append(texto).append("</font>");

		return buffer.toString();
	}

	public static String align(String campo, String align) {
		return "<div align=\"" + align + "\">" + campo + "</div>";
	}

	public static String vermelho(String texto) {
		StringBuilder buffer = new StringBuilder();

		buffer.append("<font face='sans-serif' size='").append(superSize)
				.append("' color='#FE0000'>").append(texto).append("</font>");

		return buffer.toString();
	}

	public static String laranja(String texto) {
		StringBuilder buffer = new StringBuilder();
		buffer.append("<font face='sans-serif'  size='").append(superSize)
				.append("' color='#C87800'>").append(texto).append("</font>");
		return buffer.toString();

	}

	public static String preto(String texto) {
		StringBuilder buffer = new StringBuilder();
		buffer.append("<font face='sans-serif'  size='").append(superSize)
				.append("' color='black'>").append(texto).append("</font>");
		return buffer.toString();
	}

	public static String verde(String texto) {
		StringBuilder buffer = new StringBuilder();
		buffer.append("<font face='sans-serif' size='").append(superSize)
				.append("' color='#008D25'>").append(texto).append("</font>");
		return buffer.toString();
	}

	public static String vinho(String texto) {
		StringBuilder buffer = new StringBuilder();
		buffer.append("<font face='sans-serif'  size='").append(superSize)
				.append("' color='#8B0000'>").append(texto).append("</font>");
		return buffer.toString();
	}

	public static String msgClima(String texto) {
		StringBuilder buffer = new StringBuilder();
		buffer.append("<font face='sans-serif' size='").append(superSize)
				.append("' color='#4682B4'>").append(texto).append("</font>");
		return buffer.toString();
	}

	public static String azul(String texto) {
		StringBuilder buffer = new StringBuilder();
		buffer.append("<font face='sans-serif'  size='").append(superSize)
				.append("' color='#2D62A8'>").append(texto).append("</font>");
		return buffer.toString();
	}

	public static String saftyCar(String texto) {
		StringBuilder buffer = new StringBuilder();

		buffer.append("<font face='sans-serif' size='").append(superSize)
				.append("' color='C87800'>").append(texto).append("</font>");
		return Html.negrito(buffer.toString());
	}

	public static String driveThru(String msg) {
		StringBuilder buffer = new StringBuilder();
		buffer.append("<font face='sans-serif' bgcolor='black' size='")
				.append(superSize).append("' color='white'>").append(msg)
				.append("</font>");
		return Html.negrito(buffer.toString());
	}

	public static String tagsJava2d(String info) {
		StringBuilder ret = new StringBuilder();
		String info1 = info.replaceAll("&nbsp;", " ");
		boolean ingnora = false;
		boolean achouDigito = false;
		boolean pulaDigito = true;
		for (int i = 0; i < info1.length(); i++) {
			if (info1.charAt(i) == '<') {
				ingnora = true;
				continue;
			}
			if (!achouDigito && !Character.isDigit(info1.charAt(i))) {
				continue;
			} else {
				achouDigito = true;
			}

			if (pulaDigito && Character.isDigit(info1.charAt(i))) {
				continue;
			} else {
				pulaDigito = false;
			}

			if (info1.charAt(i) == '>') {
				ingnora = false;
				continue;
			}
			if (!ingnora) {
				ret.append(info1.charAt(i));
			}
		}
		return ret.toString();
	}

}
