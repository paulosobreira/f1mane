package sowbreira.f1mane.paddock.servlet;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import br.nnpe.Logger;
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
				Thread.sleep(3000);
				long timeNow = System.currentTimeMillis();
				List<SessaoCliente> clientes = controlePaddock.getDadosPaddock()
						.getClientes();
				SessaoCliente sessaoClienteRemover = null;
				for (Iterator<SessaoCliente> iter = clientes.iterator(); iter
						.hasNext();) {
					SessaoCliente sessaoCliente = iter.next();
					int intervaloAtividade = 60000;
					if(sessaoCliente.isGuest()){
						intervaloAtividade = 6000000;
					}
					if ((timeNow - sessaoCliente.getUlimaAtividade()) > intervaloAtividade) {
						sessaoClienteRemover = sessaoCliente;
						break;
					}
				}
				if (sessaoClienteRemover != null) {
					Logger.logar(
							"Remover " + sessaoClienteRemover.getNomeJogador());
					controlePaddock.removerClienteInativo(sessaoClienteRemover);
				}
				Map<SessaoCliente, JogoServidor> jogos = controlePaddock
						.getControleJogosServer().getMapaJogosCriados();
				for (Iterator<SessaoCliente> iter = jogos.keySet()
						.iterator(); iter.hasNext();) {
					SessaoCliente key = iter.next();
					JogoServidor jogoServidor = (JogoServidor) jogos.get(key);
					if ((timeNow - jogoServidor.getTempoCriacao()) > 300000) {
						jogoServidor.iniciarJogo();
					}
				}
				for (Iterator<SessaoCliente> iter = jogos.keySet()
						.iterator(); iter.hasNext();) {
					SessaoCliente key = iter.next();
					JogoServidor jogoServidor = jogos.get(key);
					for (Iterator<String> iterator = jogoServidor
							.getMapJogadoresOnline().keySet()
							.iterator(); iterator.hasNext();) {
						String nomeJogador = iterator.next();
						SessaoCliente sessaoCliente = controlePaddock
								.verificaUsuarioSessao(nomeJogador);
						if (sessaoCliente == null) {
							iterator.remove();
						}
					}
				}
			} catch (InterruptedException e) {
				Logger.logarExept(e);
			} catch (Exception e) {
				Logger.logarExept(e);
			}
		}

	}

	public boolean isAlive() {
		return alive;
	}

	public void setAlive(boolean alive) {
		this.alive = alive;
	}

}
