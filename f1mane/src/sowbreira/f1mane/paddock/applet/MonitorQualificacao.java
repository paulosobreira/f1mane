package sowbreira.f1mane.paddock.applet;

import br.nnpe.Logger;

/**
 * @author Paulo Sobreira Criado em 15/08/2007 as 15:36:58
 */
public class MonitorQualificacao implements Runnable {
	private JogoCliente controleJogoClenteLocal;

	public MonitorQualificacao(JogoCliente controleJogoClenteLocal) {
		this.controleJogoClenteLocal = controleJogoClenteLocal;
	}

	public void run() {
		try {
			controleJogoClenteLocal.atualizaPainel();
			controleJogoClenteLocal.desenhaQualificacao();
		} catch (Exception e) {
			Logger.logarExept(e);
		}
	}

}
