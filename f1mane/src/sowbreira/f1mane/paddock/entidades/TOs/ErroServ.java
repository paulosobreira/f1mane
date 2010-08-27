package sowbreira.f1mane.paddock.entidades.TOs;

import java.io.Serializable;

public class ErroServ implements Serializable {

	private String erroInfo;

	public ErroServ(String desc) {
		erroInfo = desc;
	}

	private String formataErro(Throwable exception) {
		StackTraceElement[] trace = exception.getStackTrace();
		StringBuffer retorno = new StringBuffer();
		retorno.append("ERRO :" + exception.getMessage() + "\n");
		retorno.append("ERRO :" + exception.getClass() + "\n");
		int size = ((trace.length > 10) ? 10 : trace.length);

		for (int i = 0; i < size; i++)
			retorno.append(trace[i] + "\n");
		return retorno.toString();

	}

	public ErroServ(Exception exception) {
		erroInfo = formataErro(exception);
	}

	public String obterErroFormatado() {
		return erroInfo;
	}

}
