package sowbreira.f1mane.paddock.servlet;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import sowbreira.f1mane.paddock.entidades.TOs.SessaoCliente;

/**
 * @author Paulo Sobreira Criado em 25/08/2007 as 11:22:46
 */
public class MonitorAtividade implements Runnable {

	private ControlePaddockServidor controlePaddock;
	private boolean alive = true;

	public MonitorAtividade(ControlePaddockServidor controlePaddock) {
		this.controlePaddock = controlePaddock;
	}

	/**
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		while (alive) {
			try {
				sleep((5000 + ((int) Math.random() * 1000)));
				long timeNow = System.currentTimeMillis();
				List clientes = controlePaddock.getDadosPaddock().getClientes();
				SessaoCliente sessaoClienteRemover = null;
				for (Iterator iter = clientes.iterator(); iter.hasNext();) {
					SessaoCliente sessaoCliente = (SessaoCliente) iter.next();
					if ((timeNow - sessaoCliente.getUlimaAtividade()) > 150000) {
						sessaoClienteRemover = sessaoCliente;
						break;
					}
				}
				if (sessaoClienteRemover != null) {
					controlePaddock.removerClienteInativo(sessaoClienteRemover);
				}
				Map jogos = controlePaddock.getControleJogosServer()
						.getMapaJogosCriados();
				for (Iterator iter = jogos.keySet().iterator(); iter.hasNext();) {
					SessaoCliente key = (SessaoCliente) iter.next();
					JogoServidor jogoServidor = (JogoServidor) jogos.get(key);
					if ((timeNow - jogoServidor.getTempoCriacao()) > 300000) {
						jogoServidor.iniciarJogo();
					}
				}
				synchronized (jogos) {
					for (Iterator iter = jogos.keySet().iterator(); iter
							.hasNext();) {
						SessaoCliente key = (SessaoCliente) iter.next();
						JogoServidor jogoServidor = (JogoServidor) jogos
								.get(key);
						for (Iterator iterator = jogoServidor
								.getMapJogadoresOnline().keySet().iterator(); iterator
								.hasNext();) {
							String nomeJogador = (String) iterator.next();
							SessaoCliente sessaoCliente = controlePaddock
									.verificaUsuarioSessao(nomeJogador);
							if (sessaoCliente == null) {
								iterator.remove();
							}
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

	private void sleep(long l) {
		try {
			Thread.sleep(l);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}

	public boolean isAlive() {
		return alive;
	}

	public void setAlive(boolean alive) {
		this.alive = alive;
	}

}
