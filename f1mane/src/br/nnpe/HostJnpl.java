package br.nnpe;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class HostJnpl {

	private static String host;

	private static String replaceHost = "{host}";

	public static void main(String[] args) throws IOException {
		host = args[0];
		System.out.println(host);
//		atualizarJnlp("f1mane.jnlp");
//		atualizarJnlp("f1mane_en.jnlp");
//		atualizarJnlp("f1maneonline_en.jnlp");
//		atualizarJnlp("f1maneonline.jnlp");
	}

	private static void atualizarJnlp(String jnlp) throws IOException {
		String file = "WebContent" + File.separator + jnlp;
		BufferedReader reader = new BufferedReader(new FileReader(file));
		String readLine = reader.readLine();
		StringBuffer buffer = new StringBuffer();
		while (readLine != null) {
			if (readLine.contains("{host}")) {
				buffer.append(readLine.replace(replaceHost, host));
			} else {
				buffer.append(readLine);
			}
			readLine = reader.readLine();
		}
		reader.close();
		FileWriter fileWriter = new FileWriter(file);
		fileWriter.write(buffer.toString());
		fileWriter.close();
	}
}
