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

	private String id;

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


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((token == null) ? 0 : token.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SessaoCliente other = (SessaoCliente) obj;
		if (token == null) {
			if (other.token != null)
				return false;
		} else if (!token.equals(other.token))
			return false;
		return true;
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

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

}
