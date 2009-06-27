package sowbreira.f1mane.paddock.entidades.persistencia;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Paulo Sobreira Criado em 20/10/2007 as 15:27:53
 */
public class JogadorDadosSrv implements Serializable {

	private String nome;
	private String senha;
	private String email;
	private long ptsConstrutores;
	private long ptsPiloto;
	private long ptsCarro;
	private String nomePiloto;
	private String nomeCarro;
	private boolean modoCarreira;
	private List corridas = new LinkedList();
	private long ultimoLogon;
	private long ultimaRecuperacao = 0;

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public String getSenha() {
		return senha;
	}

	public void setSenha(String senha) {
		this.senha = senha;
	}

	public long getUltimoLogon() {
		return ultimoLogon;
	}

	public void setUltimoLogon(long ultimoLogon) {
		this.ultimoLogon = ultimoLogon;
	}

	public List getCorridas() {
		return corridas;
	}

	public void setCorridas(List corridas) {
		this.corridas = corridas;
	}

	public long getUltimaRecuperacao() {
		return ultimaRecuperacao;
	}

	public void setUltimaRecuperacao(long ultimaRecuperacao) {
		this.ultimaRecuperacao = ultimaRecuperacao;
	}

	public long getPtsPiloto() {
		return ptsPiloto;
	}

	public void setPtsPiloto(long ptsPiloto) {
		this.ptsPiloto = ptsPiloto;
	}

	public long getPtsCarro() {
		return ptsCarro;
	}

	public void setPtsCarro(long ptsCarro) {
		this.ptsCarro = ptsCarro;
	}

	public String getNomePiloto() {
		return nomePiloto;
	}

	public void setNomePiloto(String nomePiloto) {
		this.nomePiloto = nomePiloto;
	}

	public String getNomeCarro() {
		return nomeCarro;
	}

	public void setNomeCarro(String nomeCarro) {
		this.nomeCarro = nomeCarro;
	}

	public long getPtsConstrutores() {
		return ptsConstrutores;
	}

	public void setPtsConstrutores(long ptsConstrutores) {
		this.ptsConstrutores = ptsConstrutores;
	}

	public boolean isModoCarreira() {
		return modoCarreira;
	}

	public void setModoCarreira(boolean modoCarreira) {
		this.modoCarreira = modoCarreira;
	}

}
