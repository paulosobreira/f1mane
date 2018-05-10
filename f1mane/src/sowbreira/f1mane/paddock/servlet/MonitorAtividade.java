package sowbreira.f1mane.paddock.servlet;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import br.nnpe.Constantes;
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
				Thread.sleep(5000);
				long timeNow = System.currentTimeMillis();
				removeClientesIniativos(timeNow);
				Map<SessaoCliente, JogoServidor> jogos = controlePaddock
						.getControleJogosServer().getMapaJogosCriados();
				iniciaJogos(timeNow, jogos);
				removeJogadoresSemSessao(jogos);
				removeJogosSemJogadores(jogos);
			} catch (InterruptedException e) {
				Logger.logarExept(e);
			} catch (Exception e) {
				Logger.logarExept(e);
			}
		}

	}

	private void removeJogosSemJogadores(
			Map<SessaoCliente, JogoServidor> jogos) {
		for (Iterator<SessaoCliente> iter = jogos.keySet().iterator(); iter
				.hasNext();) {
			SessaoCliente key = iter.next();
			JogoServidor jogoServidor = jogos.get(key);
			if (jogoServidor.getMapJogadoresOnline().isEmpty()) {
				if (!jogoServidor.isCorridaTerminada()) {
					Logger.logar("removeJogosSemJogadores "
							+ jogoServidor.getCircuito().getNome() + " - "
							+ jogoServidor.getTemporada());
				}
				jogoServidor.encerraCorrida();
			}
		}
	}

	public void removeJogadoresSemSessao(
			Map<SessaoCliente, JogoServidor> jogos) {
		for (Iterator<SessaoCliente> iter = jogos.keySet().iterator(); iter
				.hasNext();) {
			SessaoCliente key = iter.next();
			JogoServidor jogoServidor = jogos.get(key);
			for (Iterator<String> iterator = jogoServidor
					.getMapJogadoresOnline().keySet().iterator(); iterator
							.hasNext();) {
				String tokenJogador = iterator.next();
				SessaoCliente sessaoCliente = controlePaddock
						.verificaUsuarioSessao(tokenJogador);
				if (sessaoCliente == null) {
					iterator.remove();
				}
			}
		}
	}

	public void iniciaJogos(long timeNow,
			Map<SessaoCliente, JogoServidor> jogos) throws Exception {
		for (Iterator<SessaoCliente> iter = jogos.keySet().iterator(); iter
				.hasNext();) {
			SessaoCliente key = iter.next();
			JogoServidor jogoServidor = (JogoServidor) jogos.get(key);
			if ((timeNow - jogoServidor
					.getTempoCriacao()) > (Constantes.SEGUNDOS_PARA_INICIAR_CORRRIDA
							* 1000)) {
				jogoServidor.iniciarJogo();
			}
		}
	}

	public void removeClientesIniativos(long timeNow) {
		List<SessaoCliente> clientes = controlePaddock.getDadosPaddock()
				.getClientes();
		SessaoCliente sessaoClienteRemover = null;
		for (Iterator<SessaoCliente> iter = clientes.iterator(); iter
				.hasNext();) {
			SessaoCliente sessaoCliente = iter.next();
			int intervaloAtividade = 200000;
			if ((timeNow
					- sessaoCliente.getUlimaAtividade()) > intervaloAtividade) {
				sessaoClienteRemover = sessaoCliente;
				break;
			}
		}
		if (sessaoClienteRemover != null) {
			Logger.logar("Remover " + sessaoClienteRemover.getNomeJogador());
			controlePaddock.removerCliente(sessaoClienteRemover);
		}
	}

	public boolean isAlive() {
		return alive;
	}

	public void setAlive(boolean alive) {
		this.alive = alive;
	}

}
