package br.f1mane.paddock.entidades.TOs;

import java.io.Serializable;

/**
 * @author Paulo Sobreira Criado em 12/08/2007 as 13:02:01
 */
public class MsgSrv implements Serializable {
	private String messageString;

	/**
	 * @param messageString
	 */
	public MsgSrv(String messageString) {
		super();
		this.messageString = messageString;
	}

	public String getMessageString() {
		return messageString;
	}

	public void setMessageString(String messageString) {
		this.messageString = messageString;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((messageString == null) ? 0 : messageString.hashCode());
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
		MsgSrv other = (MsgSrv) obj;
		if (messageString == null) {
            return other.messageString == null;
		} else return messageString.equals(other.messageString);
    }

}
