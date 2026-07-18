package br.flmane.servidor.entidades.TOs;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

public class ErroServ implements Serializable {

	@JsonProperty
	private final String erroInfo;

	public ErroServ(String desc) {
		erroInfo = desc;
	}

	private String formataErro(Throwable exception) {
		StackTraceElement[] trace = exception.getStackTrace();
		StringBuilder retorno = new StringBuilder();
		retorno.append("ERRO :").append(exception.getMessage()).append("\n");
		retorno.append("ERRO :").append(exception.getClass()).append("\n");
		int size = ((trace.length > 10) ? 10 : trace.length);

		for (int i = 0; i < size; i++)
			retorno.append(trace[i]).append("\n");
		return retorno.toString();

	}

	public ErroServ(Exception exception) {
		erroInfo = formataErro(exception);
	}

	public String obterErroFormatado() {
		return erroInfo;
	}

}
