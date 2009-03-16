package sowbreira.f1mane.paddock.entidades.persistencia;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Paulo Sobreira Criado em 20/10/2007 as 15:27:53
 */
public class JogadorDadosSrv {

	private String nome;
	private String senha;
	private String email;
	private List corridas = new ArrayList();
	private long ultimoLogon;

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

}
