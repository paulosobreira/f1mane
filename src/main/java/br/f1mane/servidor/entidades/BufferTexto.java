package br.f1mane.servidor.entidades;

import java.io.Serializable;
import java.util.LinkedList;

/**
 * @author Paulo Sobreira Criado em 18/08/2007 as 23:56:08
 */
public class BufferTexto implements Serializable {

	private static final long serialVersionUID = 1L;

	private Long utlConsumoTexto;

	private String utlTexto;

	private final LinkedList<String> bufferinfo = new LinkedList<String>();

	public void adicionarTextoPrio(String txt) {
		if (bufferinfo.contains(txt)) {
			return;
		}
		if (bufferinfo.size() > 15) {
			bufferinfo.removeLast();
		}
		bufferinfo.addFirst(txt);

	}

	public void adicionarTexto(String txt) {
		if (bufferinfo.contains(txt)) {
			return;
		}
		if (bufferinfo.size() > 15) {
			bufferinfo.removeLast();
		}
		bufferinfo.addLast(txt);
	}

	public String consumirTexto() {
		synchronized (bufferinfo){
			if (bufferinfo.isEmpty()) {
				return "";
			}
			if (utlConsumoTexto != null
					&& (System.currentTimeMillis() - utlConsumoTexto.longValue()) < 5000) {
				return utlTexto;
			}
			utlConsumoTexto = Long.valueOf(System.currentTimeMillis());
			utlTexto = (String) bufferinfo.removeFirst();
			return utlTexto;
		}
	}

}
