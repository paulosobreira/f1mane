package br.f1mane.controles;

import java.util.Iterator;

import br.nnpe.Constantes;
import br.nnpe.Html;
import br.nnpe.Logger;
import sowbreira.f1mane.entidades.Piloto;
import br.f1mane.recursos.idiomas.Lang;

/**
 * @author Paulo Sobreira
 */
public class ControleCiclo extends Thread {
	private InterfaceJogo controleJogo;
	private ControleCorrida controleCorrida;
	private int contadorCiclos;
	private boolean processadoCilcos = true;

	/**
	 * @param controleJogo
	 * @param circuito
	 */
	public ControleCiclo(InterfaceJogo controleJogo,
			ControleCorrida controleCorrida) {
		super();
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
			controleJogo.desenhaQualificacao();
			controleJogo.infoPrioritaria(Html.verde(Lang.msg("001")));
			Thread.sleep(2000);
			controleJogo.apagarLuz();
			Thread.sleep(1000);
			controleJogo.apagarLuz();
			Thread.sleep(1000);
			controleJogo.apagarLuz();
			Thread.sleep(1000);
			controleJogo.apagarLuz();
			Thread.sleep(1000);
			controleJogo.apagarLuz();
			Logger.logar("Luzes apagadas iniciar processadoCilcos");
			boolean interrupt = false;
			while (!interrupt && processadoCilcos) {
				try {
					if (controleCorrida.isCorridaPausada()) {
						Thread.sleep(Constantes.CICLO);
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
							piloto.calculaVelocidadeExibir(controleJogo);
							controleJogo.calculaSegundosParaLider(piloto);
						}
						if (!piloto.isDesqualificado()
								&& (piloto.isBox() || controleJogo
										.verificaNoPitLane(piloto))) {
							controleCorrida.processarPilotoBox(piloto);
							piloto.calculaVelocidadeExibir(controleJogo);
						}
						piloto.processaAlertaMotor(controleJogo);
						piloto.processaAlertaAerefolio(controleJogo);
					}
					controleCorrida.atualizaClassificacao();
					controleCorrida.verificaFinalCorrida();
					controleJogo.atualizaIndexTracadoPilotos();
					Thread.sleep(Constantes.CICLO);
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
