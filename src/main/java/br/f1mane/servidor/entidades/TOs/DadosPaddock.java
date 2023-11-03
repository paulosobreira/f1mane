package br.f1mane.servidor.entidades.TOs;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author paulo.sobreira
 * 
 */
public class DadosPaddock implements Serializable {
	private static final long serialVersionUID = 2200481566401284586L;
	private Long dataTime;
	private final Map<String, SessaoCliente> clientes = new HashMap<String, SessaoCliente>();
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
		return new ArrayList<SessaoCliente>(clientes.values());
	}

	public List<String> getJogosCriados() {
		return jogosCriados;
	}

	public void setJogosCriados(List<String> jogosCriados) {
		this.jogosCriados = jogosCriados;
	}

	public void add(SessaoCliente sessaoCliente) {
		clientes.put(sessaoCliente.getToken(), sessaoCliente);

	}

	public void remove(SessaoCliente cliente) {
		clientes.remove(cliente.getToken());
	}

	public SessaoCliente obterPorToken(String token) {
		return clientes.get(token);
	}

}
