package sowbreira.f1mane.paddock.entidades.TOs;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Vector;

/**
 * @author paulo.sobreira
 * 
 */
public class DadosPaddock implements Serializable {
	private static final long serialVersionUID = 2200481566401284586L;
	private Long dataTime;
	private List<SessaoCliente> clientes = new ArrayList<SessaoCliente>();
	private List<String> jogosCriados = new ArrayList<String>();
	private String linhaChat = "";

	public Long getDataTime() {
		return dataTime;
	}

	public void setDataTime(Long dataTime) {
		this.dataTime = dataTime;
	}

	public String getLinhaChat() {
		return linhaChat;
	}

	public void setLinhaChat(String linhaChat) {
		this.linhaChat = linhaChat;
	}

	public List<SessaoCliente> getClientes() {
		return clientes;
	}

	public List<String> getJogosCriados() {
		return jogosCriados;
	}

	public void setJogosCriados(List<String> jogosCriados) {
		this.jogosCriados = jogosCriados;
	}

	public void setClientes(List<SessaoCliente> clientes) {
		this.clientes = clientes;
	}

}
