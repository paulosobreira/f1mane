package sowbreira.f1mane.paddock.entidades.TOs;

import java.io.Serializable;

public class ErroServ implements Serializable {

	private Exception exception;

	public ErroServ(String desc) {
		exception = new Exception(desc);
	}

	public ErroServ(Exception exception) {
		super();
		this.exception = exception;
	}

	public String obterErroFormatado() {
		StackTraceElement[] trace = exception.getStackTrace();
		StringBuffer retorno = new StringBuffer();
		retorno.append("ERRO NO SERVIDOR :" + exception.getMessage() + "\n");
		int size = ((trace.length > 10) ? 10 : trace.length);

		for (int i = 0; i < size; i++)
			retorno.append(trace[i] + "\n");
		return retorno.toString();
	}

}
