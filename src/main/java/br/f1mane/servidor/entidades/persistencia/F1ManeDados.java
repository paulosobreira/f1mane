package br.f1mane.servidor.entidades.persistencia;

import java.io.Serializable;
import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;

@MappedSuperclass
public abstract class F1ManeDados implements Serializable {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	protected Long id;

	@Column(nullable = false)
	private Date dataCriacao = new Date();

	@Column(nullable = false)
	private String loginCriador = "Sistema";

	public String getLoginCriador() {
		return loginCriador;
	}

	public void setLoginCriador(String loginCriador) {
		this.loginCriador = loginCriador;
	}

	public Date getDataCriacao() {
		return dataCriacao;
	}

	public void setDataCriacao(Date dataCriacao) {
		this.dataCriacao = dataCriacao;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public void setId(int id) {
		this.id = Long.valueOf((long) id);
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof F1ManeDados)) {
			return false;
		}
		F1ManeDados f1ManeDados = (F1ManeDados) obj;
		return id.equals(f1ManeDados.getId());
	}

	@Override
	public int hashCode() {
		return id.hashCode();
	}

}
