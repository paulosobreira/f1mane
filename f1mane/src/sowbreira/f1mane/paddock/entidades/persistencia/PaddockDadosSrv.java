package sowbreira.f1mane.paddock.entidades.persistencia;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

/**
 * @author Paulo Sobreira Criado em 20/10/2007 as 14:18:24
 */
public class PaddockDadosSrv implements Serializable {
	private Map jogadoresMap = new Hashtable();
	private long lastSave = System.currentTimeMillis();

	public Map getJogadoresMap() {
		return jogadoresMap;
	}

	public void setJogadoresMap(Map jogadoresMap) {
		this.jogadoresMap = jogadoresMap;
	}

	public long getLastSave() {
		return lastSave;
	}

	public void setLastSave(long lastSave) {
		this.lastSave = lastSave;
	}
}
