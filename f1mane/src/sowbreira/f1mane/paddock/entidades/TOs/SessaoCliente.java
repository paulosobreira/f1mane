package sowbreira.f1mane.paddock.entidades.TOs;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * @author paulo.sobreira
 */
@JsonInclude(Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class SessaoCliente implements Serializable {

	private static final long serialVersionUID = -1814045404166555104L;

	private long ulimaAtividade;

	private String nomeJogador;

	private String imagemJogador;

	private String token;
	
	private String email;

	private String jogoAtual;

	private String pilotoAtual;

	private int idPilotoAtual;

	private boolean guest;

	public long getUlimaAtividade() {
		return ulimaAtividade;
	}

	public String getJogoAtual() {
		return jogoAtual;
	}

	public void setJogoAtual(String jogoAtual) {
		this.jogoAtual = jogoAtual;
	}

	public String getPilotoAtual() {
		return pilotoAtual;
	}

	public void setPilotoAtual(String pilotoAtual) {
		this.pilotoAtual = pilotoAtual;
	}

	public void setUlimaAtividade(long ulimaAtividade) {
		this.ulimaAtividade = ulimaAtividade;
	}

	public String getNomeJogador() {
		return nomeJogador;
	}

	public void setNomeJogador(String apelido) {
		this.nomeJogador = apelido;
	}

	public boolean equals(Object obj) {
		SessaoCliente sessaoCliente = (SessaoCliente) obj;
		return nomeJogador.equals(sessaoCliente.getNomeJogador());
	}

	public String toString() {
		return nomeJogador;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public boolean isGuest() {
		return guest;
	}

	public void setGuest(boolean guest) {
		this.guest = guest;
	}

	public int getIdPilotoAtual() {
		return idPilotoAtual;
	}

	public void setIdPilotoAtual(int idPilotoAtual) {
		this.idPilotoAtual = idPilotoAtual;
	}

	public String getImagemJogador() {
		return imagemJogador;
	}

	public void setImagemJogador(String imagemJogador) {
		this.imagemJogador = imagemJogador;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}
	
	

}
