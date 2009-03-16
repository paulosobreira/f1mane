package sowbreira.f1mane.paddock.entidades;

import java.util.LinkedList;
import java.util.List;

/**
 * @author Paulo Sobreira Criado em 18/08/2007 as 23:56:08
 */
public class BufferTexto {

	private LinkedList bufferinfo = new LinkedList();

	public void adicionarTextoPrio(String txt) {
		if (bufferinfo.contains(txt)) {
			return;
		}
		if (bufferinfo.size() > 15) {
			bufferinfo.removeLast();
		}
		bufferinfo.addLast(txt);

	}

	public void adicionarTexto(String txt) {
		if (bufferinfo.contains(txt)) {
			return;
		}
		if (bufferinfo.size() > 15) {
			bufferinfo.removeLast();
		}
		bufferinfo.addFirst(txt);
	}

	public String consumirTexto() {
		if (bufferinfo.isEmpty()) {
			return "";
		}

		return (String) bufferinfo.removeFirst();
	}

}
