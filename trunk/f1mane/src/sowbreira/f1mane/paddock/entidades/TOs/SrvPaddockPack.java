package sowbreira.f1mane.paddock.entidades.TOs;

import java.io.Serializable;
import java.util.List;

/**
 * @author Paulo Sobreira Criado em 28/07/2007 as 15:55:15
 */
public class SrvPaddockPack implements Serializable {

	private static final long serialVersionUID = 1162929434479206667L;
	private SessaoCliente sessaoCliente;
	private DadosPaddock dadosPaddock;
	private DetalhesJogo detalhesJogo;
	private DadosCriarJogo dadosCriarJogo;
	private String nomeJogoCriado;
	private String abandonar;
	private List listaDadosJogador;
	private List listaCorridasJogador;
	private List listaConstrutoresCarros;
	private List listaConstrutoresPilotos;

	public SrvPaddockPack() {
	}

	public DadosPaddock getDadosPaddock() {
		return dadosPaddock;
	}

	public String getAbandonar() {
		return abandonar;
	}

	public void setAbandonar(String abandonar) {
		this.abandonar = abandonar;
	}

	public DadosCriarJogo getDadosCriarJogo() {
		return dadosCriarJogo;
	}

	public void setDadosCriarJogo(DadosCriarJogo dadosCriarJogo) {
		this.dadosCriarJogo = dadosCriarJogo;
	}

	public DetalhesJogo getDetalhesJogo() {
		return detalhesJogo;
	}

	public String getNomeJogoCriado() {
		return nomeJogoCriado;
	}

	public void setNomeJogoCriado(String nomeJogoCriado) {
		this.nomeJogoCriado = nomeJogoCriado;
	}

	public void setDetalhesJogo(DetalhesJogo detalhesJogo) {
		this.detalhesJogo = detalhesJogo;
	}

	public void setDadosPaddock(DadosPaddock dadosPaddock) {
		this.dadosPaddock = dadosPaddock;
	}

	public SessaoCliente getSessaoCliente() {
		return sessaoCliente;
	}

	public void setSessaoCliente(SessaoCliente sessaoCliente) {
		this.sessaoCliente = sessaoCliente;
	}

	public List getListaDadosJogador() {
		return listaDadosJogador;
	}

	public void setListaDadosJogador(List listaDadosJogador) {
		this.listaDadosJogador = listaDadosJogador;
	}

	public List getListaCorridasJogador() {
		return listaCorridasJogador;
	}

	public void setListaCorridasJogador(List listaCorridasJogador) {
		this.listaCorridasJogador = listaCorridasJogador;
	}

	public List getListaConstrutoresCarros() {
		return listaConstrutoresCarros;
	}

	public void setListaConstrutoresCarros(List listaConstrutoresCarros) {
		this.listaConstrutoresCarros = listaConstrutoresCarros;
	}

	public List getListaConstrutoresPilotos() {
		return listaConstrutoresPilotos;
	}

	public void setListaConstrutoresPilotos(List listaConstrutoresPilotos) {
		this.listaConstrutoresPilotos = listaConstrutoresPilotos;
	}

}
