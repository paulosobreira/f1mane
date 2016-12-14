package sowbreira.f1mane.paddock.entidades.TOs;

import java.io.Serializable;

/**
 * @author Paulo Sobreira Criado em 12/08/2007 as 13:02:01
 */
public class MsgSrv implements Serializable{
	private String messageString;
	private String versao;

	/**
	 * @param messageString
	 */
	public MsgSrv(String messageString) {
		super();
		this.messageString = messageString;
	}

	public MsgSrv(String messageString, String versao) {
		super();
		this.messageString = messageString;
		this.versao = versao;
	}

	public String getMessageString() {
		return messageString;
	}

	public void setMessageString(String messageString) {
		this.messageString = messageString;
	}

	public String getVersao() {
		return versao;
	}

	public void setVersao(String versao) {
		this.versao = versao;
	}

}
