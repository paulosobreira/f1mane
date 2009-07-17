package sowbreira.f1mane.paddock.servlet;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.MulticastSocket;
import java.net.SocketException;

import br.nnpe.BufferFinito;
import br.nnpe.Logger;

/**
 * @author Paulo Sobreira Criado em 17/07/2009 as 09:57:02
 */
public class UDPListener implements Runnable {
	byte[] buffer = new byte[100]; // Cria um buffer local
	BufferFinito bufferFinito;
	DatagramPacket pacote;
	DatagramSocket datagramSocket;
	MulticastSocket multicastSocket;
	long udpCont;
	private boolean alive = true;

	public boolean isAlive() {
		return alive;
	}

	public void setAlive(boolean alive) {
		this.alive = alive;
	}

	public UDPListener(BufferFinito pilhaComandos, int porta)
			throws SocketException {
		/**
		 * Multicast
		 */
		pacote = new DatagramPacket(buffer, buffer.length);
		datagramSocket = new DatagramSocket(porta);
		this.bufferFinito = pilhaComandos;
	}

	public void run() {
		while (alive) {
			try {
				datagramSocket.receive(pacote);
			} catch (IOException e) {
				Logger.logarExept(e);
			}

			String comando = new String(pacote.getData(), 0, pacote.getLength());

			if ((comando == null) || "".equals(comando)) {
				continue;
			}
			System.out.println(comando);
			bufferFinito.add(comando);
		}
	}
}