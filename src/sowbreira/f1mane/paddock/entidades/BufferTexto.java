package sowbreira.f1mane.paddock.entidades;

import java.io.Serializable;
import java.util.LinkedList;

/**
 * @author Paulo Sobreira Criado em 18/08/2007 as 23:56:08
 */
public class BufferTexto implements Serializable {

	private static final long serialVersionUID = 1L;

	private Long utlConsumoTexto;

	private String utlTexto;

	private LinkedList<String> bufferinfo = new LinkedList<String>();

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
		if (bufferinfo.isEmpty()) {
			return "";
		}
		if (utlConsumoTexto != null
				&& (System.currentTimeMillis() - utlConsumoTexto) < 5000) {
			return utlTexto;
		}
		utlConsumoTexto = System.currentTimeMillis();
		utlTexto = (String) bufferinfo.removeFirst();
		return utlTexto;
	}

}
