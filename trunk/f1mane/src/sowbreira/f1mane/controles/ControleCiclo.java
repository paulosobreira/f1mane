package sowbreira.f1mane.controles;

import java.util.Iterator;

import sowbreira.f1mane.entidades.Piloto;
import sowbreira.f1mane.recursos.idiomas.Lang;
import br.nnpe.Html;
import br.nnpe.Logger;

/**
 * @author Paulo Sobreira
 */
public class ControleCiclo extends Thread {
	private InterfaceJogo controleJogo;
	private ControleCorrida controleCorrida;
	private int contadorCiclos;
	private long tempoCiclo;
	private boolean processadoCilcos = true;
	private int contadorLuz = 70;

	public long getTempoCiclo() {
		return tempoCiclo;
	}

	/**
	 * @param controleJogo
	 * @param circuito
	 */
	public ControleCiclo(InterfaceJogo controleJogo,
			ControleCorrida controleCorrida, long tempoCiclo) {
		super();
		this.tempoCiclo = tempoCiclo;
		this.controleJogo = controleJogo;
		this.controleCorrida = controleCorrida;
	}

	public int getContadorCiclos() {
		return contadorCiclos;
	}

	public void setContadorCiclos(int contadorCiclos) {
		this.contadorCiclos = contadorCiclos;
	}

	public boolean isProcessadoCilcos() {
		return processadoCilcos;
	}

	public void setProcessadoCilcos(boolean alive) {
		this.processadoCilcos = alive;
	}

	public void run() {
		try {
			if (ControleJogoLocal.VALENDO) {
				controleJogo.atualizaPainel();
				Thread.sleep(tempoCiclo);
				controleJogo.desenhaQualificacao();
				// Thread.sleep(5000);
				controleJogo.infoPrioritaria(Html.superGreen(Lang.msg("001")));
				if (!controleJogo.verificaNivelJogo()
						&& controleJogo.asfaltoAbrasivo()) {
					controleJogo.infoPrioritaria(Html.red(Lang
							.msg("asfaltoAbrasivo")));
				}
				while (contadorLuz >= 0) {
					Thread.sleep(tempoCiclo);
					contadorLuz--;
					controleJogo.atualizaPainel();
					if (contadorLuz == 60) {
						controleJogo.apagarLuz();
						Thread.sleep(tempoCiclo);
					}
					if (contadorLuz == 50) {
						controleJogo.apagarLuz();
					}
					if (contadorLuz == 40) {
						controleJogo.apagarLuz();
					}
					if (contadorLuz == 30) {
						controleJogo.apagarLuz();
					}
					if (contadorLuz == 20) {
						controleJogo.apagarLuz();
					}
					if (contadorLuz == 10) {
						controleJogo.apagarLuz();
					}
				}
			}
			Logger.logar("Luzes apagadas iniciar processadoCilcos");
			while (processadoCilcos) {
				try {
					if (InterfaceJogo.VALENDO
							&& controleCorrida.isCorridaPausada()) {
						Thread.sleep(tempoCiclo);
						continue;
					}
					controleCorrida.processaVoltaSafetyCar();
					for (Iterator iter = controleJogo.getPilotos().iterator(); iter
							.hasNext();) {
						Piloto piloto = (Piloto) iter.next();

						if (piloto.decrementaParadoBox()) {
							continue;
						}

						if (piloto.getPtosBox() == 0) {
							piloto.processarCiclo(controleJogo);
						}

						if (!piloto.isDesqualificado()
								&& (piloto.isBox() || controleJogo
										.verificaNoPitLane(piloto))) {
							controleCorrida.processarPilotoBox(piloto);
						}
					}

					controleCorrida.atualizaClassificacao();
					controleCorrida.verificaFinalCorrida();
					controleJogo.atualizaPainel();
					controleJogo.verificaProgramacaoBox();
					if (InterfaceJogo.VALENDO) {
						Thread.sleep(tempoCiclo);
					} else {
						setPriority(Thread.MIN_PRIORITY);
					}
					contadorCiclos++;
				} catch (Exception e) {
					Logger.logarExept(e);
				}
			}
		} catch (Exception e) {
			Logger.logarExept(e);
		}
	}

	public static void main(String[] args) {
		Logger.logar((int) (.7 * 5));
	}
}
