package sowbreira.f1mane.controles;

import java.text.DecimalFormat;
import java.util.Iterator;
import java.util.LinkedList;

import sowbreira.f1mane.entidades.Piloto;
import sowbreira.f1mane.entidades.Volta;
import br.nnpe.Html;

/**
 * @author Paulo Sobreira Criado em 16/06/2007 as 13:24:54
 */
public class ControleEstatisticas {
	private InterfaceJogo controleJogo;
	private Volta voltaMaisRapida;
	private DecimalFormat milesismos = new DecimalFormat(".000");
	private static DecimalFormat mil = new DecimalFormat("000");
	private static DecimalFormat dez = new DecimalFormat("00");
	private LinkedList bufferInfo = new LinkedList();
	private boolean consumidorAtivo = true;
	private Thread infoConsumer;

	/**
	 * @param controleJogo
	 */
	public ControleEstatisticas(InterfaceJogo controleJogo) {
		super();
		this.controleJogo = controleJogo;
	}

	public String calculaSegundosParaLider(Piloto pilotoSelecionado, long tempo) {
		Piloto lider = (Piloto) controleJogo.getPilotos().get(0);
		int diff = lider.getPtosPista() - pilotoSelecionado.getPtosPista();
		String ret = milesismos.format((diff / Double.parseDouble(String
				.valueOf(tempo))) * 3.0)
				+ "s";
		pilotoSelecionado.setSegundosParaLider(ret);
		return ret;
	}

	public void processaVoltaRapida(Piloto piloto) {
		if (piloto.getVoltaAtual() == null) {
			Volta volta = new Volta();
			volta.setCiclosInicio(System.currentTimeMillis());
			piloto.setVoltaAtual(volta);

			return;
		}

		Volta volta = piloto.getVoltaAtual();
		volta.setCiclosFim(System.currentTimeMillis());

		controleJogo.verificaVoltaMaisRapidaCorrida(piloto);
		verificaVoltaMaisrapidaPiloto(piloto);
		piloto.setUltimaVolta(volta);
		volta = new Volta();
		volta.setCiclosInicio(System.currentTimeMillis());
		piloto.setVoltaAtual(volta);
	}

	private void verificaVoltaMaisrapidaPiloto(Piloto piloto) {
		Volta voltaAtual = piloto.getVoltaAtual();
		boolean teveMelhor = false;

		for (Iterator iter = piloto.getVoltas().iterator(); iter.hasNext();) {
			Volta volta = (Volta) iter.next();

			if (voltaAtual.obterTempoVolta() > volta.obterTempoVolta()) {
				teveMelhor = true;
			}
		}

		if (!teveMelhor && !controleJogo.isSafetyCarNaPista()) {
			if ((piloto.getPosicao() < 9)) {
				controleJogo.info(Html.azul((Html.bold(piloto.getNome()
						+ " faz a sua melhor volta "
						+ voltaAtual.obterTempoVoltaFormatado()))));
			}
		}

		piloto.getVoltas().add(voltaAtual);
	}

	public void verificaVoltaMaisRapida(Piloto piloto) {
		if (voltaMaisRapida == null) {
			voltaMaisRapida = piloto.getVoltaAtual();
		}

		if (voltaMaisRapida.obterTempoVolta() > piloto.getVoltaAtual()
				.obterTempoVolta()) {
			voltaMaisRapida = piloto.getVoltaAtual();
			controleJogo.infoPrioritaria(Html.superBlue(Html.bold(piloto
					.getNome()
					+ " faz a volta mais rapida da corrida "
					+ voltaMaisRapida.obterTempoVoltaFormatado())));
		}
	}

	public Volta getVoltaMaisRapida() {
		return voltaMaisRapida;
	}

	public static String formatarTempo(long fullnum) {

		long minu = (fullnum / 60000);
		long seg = ((fullnum - (minu * 60000)) / 1000);
		long mili = fullnum - ((minu * 60000) + (seg * 1000));
		if (minu > 0)
			return (minu) + ":" + dez.format(seg) + "." + mil.format(mili);
		else
			return seg + "." + mil.format(mili);
	}

	public static String formatarTempo(int ciclos, long tempoCiclo) {
		return formatarTempo(tempoCiclo * ciclos);
	}

	public String calculaSegundosParaProximo(Piloto psel, long tempo) {
		int diff = calculaDiferencaParaProximo(psel);
		String ret = milesismos.format((diff / Double.parseDouble(String
				.valueOf(tempo))) * 3.0)
				+ "s";

		return ret;
	}

	public double calculaSegundosParaProximoDouble(Piloto psel, long tempo) {
		int diff = calculaDiferencaParaProximo(psel);
		return (diff / Double.parseDouble(String.valueOf(tempo))) * 3.0;

	}

	public int calculaDiferencaParaProximo(Piloto psel) {
		int pos = psel.getPosicao() - 2;
		if (pos < 0) {
			return Integer.MAX_VALUE;
		}
		if (pos > controleJogo.getPilotos().size() - 1) {
			return Integer.MAX_VALUE;
		}
		Piloto piloto = (Piloto) controleJogo.getPilotos().get(pos);
		if (piloto != null) {
			int diff = piloto.getPtosPista() - psel.getPtosPista();
			return diff;
		}
		return Integer.MAX_VALUE;
	}

	public void zerarMelhorVolta() {
		voltaMaisRapida = null;

	}

	public void inicializarThreadConsumidoraInfo(final long delay) {
		bufferInfo.clear();
		infoConsumer = new Thread(new Runnable() {
			public void run() {
				controleJogo.adicionarInfoDireto(Html.azul(ControleIdiomas
						.obterMsg("000", new Object[] { controleJogo
								.totalVoltasCorrida() })));
				try {
					while (consumidorAtivo) {
						controleJogo.atulizaTabelaPosicoes();

						synchronized (bufferInfo) {
							if (!bufferInfo.isEmpty()) {
								Object object = bufferInfo.iterator().next();
								controleJogo
										.adicionarInfoDireto((String) object);
								bufferInfo.remove(object);

							}
						}

						if (ControleJogoLocal.VALENDO) {
							Thread.sleep(delay);
						} else {
							infoConsumer.setPriority(Thread.MIN_PRIORITY);
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		infoConsumer.start();
	}

	public void info(String info, boolean prioritaria) {
		if (InterfaceJogo.VALENDO) {
			synchronized (bufferInfo) {
				if (bufferInfo.contains(info)) {
					return;
				}

				if (prioritaria) {
					bufferInfo.addFirst(info);
				} else {
					bufferInfo.add(info);
				}
			}
		} else {
			System.out.println(info);
		}
	}

	public void info(String info) {
		info(info, false);
	}

	protected void finalize() throws Throwable {
		super.finalize();
		if (infoConsumer != null) {
			infoConsumer.interrupt();
		}
	}

	public boolean isConsumidorAtivo() {
		return consumidorAtivo;
	}

	public void setConsumidorAtivo(boolean consumidorAtivo) {
		this.consumidorAtivo = consumidorAtivo;
	}

}
