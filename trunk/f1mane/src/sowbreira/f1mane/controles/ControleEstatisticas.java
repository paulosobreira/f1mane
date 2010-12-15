package sowbreira.f1mane.controles;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.swing.SwingUtilities;

import sowbreira.f1mane.entidades.Piloto;
import sowbreira.f1mane.entidades.Volta;
import sowbreira.f1mane.recursos.CarregadorRecursos;
import sowbreira.f1mane.recursos.idiomas.Lang;
import br.nnpe.Html;
import br.nnpe.Logger;
import br.nnpe.Util;

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
		diff /= controleJogo.getCircuito().getMultiplciador();
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
		verificaVoltaMaisRapidaPiloto(piloto);
		piloto.setUltimaVolta(volta);
		volta = new Volta();
		volta.setCiclosInicio(System.currentTimeMillis());
		piloto.setVoltaAtual(volta);
	}

	private void verificaVoltaMaisRapidaPiloto(Piloto piloto) {
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
				controleJogo.info(Html.green(Lang.msg("022", new String[] {
						Html.bold(piloto.getNome()),
						voltaAtual.obterTempoVoltaFormatado() })));
			}
		}
		if (controleJogo.isSafetyCarNaPista()) {
			voltaAtual.setVoltaSafetyCar(true);
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
			controleJogo.infoPrioritaria(Html.superGreen(Lang.msg("023",
					new String[] { Html.bold(piloto.getNome()),
							voltaMaisRapida.obterTempoVoltaFormatado() })));
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
			return (minu) + ":" + dez.format(Math.abs(seg)) + "."
					+ mil.format(Math.abs(mili));
		else
			return seg + "." + mil.format(Math.abs(mili));
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
			return Util.inte(diff
					/ controleJogo.getCircuito().getMultiplciador());
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
				try {
					controleJogo.adicionarInfoDireto(Html
							.azul(Lang.msg("000", new Object[] { controleJogo
									.totalVoltasCorrida() })));
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
					Logger.logarExept(e);
				}
			}
		});
		infoConsumer.start();
	}

	public void info(String info, boolean prioritaria) {
		Logger.logar(info);
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

	public void tabelaComparativa() {
		String tabela = carrgaTabelaComparativa();
		List pilotos = new ArrayList();
		for (Iterator iterator = controleJogo.getPilotos().iterator(); iterator
				.hasNext();) {
			Piloto piloto = (Piloto) iterator.next();
			if (piloto.isJogadorHumano() && !piloto.isDesqualificado()) {
				pilotos.add(piloto);
			}

		}

		if (pilotos.isEmpty()) {
			for (Iterator iterator = controleJogo.getPilotos().iterator(); iterator
					.hasNext();) {
				Piloto piloto = (Piloto) iterator.next();
				if (piloto.getPosicao() < 9) {
					pilotos.add(piloto);
				}

			}
		}
		Collections.shuffle(pilotos);
		Piloto pilotoSel = (Piloto) pilotos.get(0);
		Piloto pilotoComp = null;
		if (pilotoSel.getPosicao() == 1) {
			pilotoComp = controleJogo.obterCarroAtraz(pilotoSel).getPiloto();
		} else {
			pilotoComp = controleJogo.obterCarroNaFrente(pilotoSel).getPiloto();
		}
		if (pilotoComp.getVoltas().size() < 3
				|| pilotoSel.getVoltas().size() < 3) {
			return;
		}
		if (pilotoSel.getPosicao() == 1) {
			tabela = preencherTabela(pilotoSel, pilotoComp, tabela);
		} else {
			tabela = preencherTabela(pilotoComp, pilotoSel, tabela);
		}
		if (tabela != null && !pilotoSel.entrouNoBox())
			controleJogo.info(tabela);
	}

	private String preencherTabela(Piloto piloto1, Piloto piloto2, String tabela) {
		tabela = tabela.replaceAll("piloto1", Html.sansSerif(piloto1.getNome()
				+ " " + piloto1.getPosicao()));
		tabela = tabela.replaceAll("piloto2", Html.sansSerif(piloto2.getNome()
				+ " " + piloto2.getPosicao()));
		tabela = tabela.replaceAll("volta1", Html.sansSerif(Lang.msg("081")
				+ piloto2.getNumeroVolta()));
		tabela = tabela.replaceAll("volta2", Html.sansSerif(Lang.msg("081")
				+ (piloto2.getNumeroVolta() - 1)));
		tabela = tabela.replaceAll("volta3", Html.sansSerif(Lang.msg("081")
				+ (piloto2.getNumeroVolta() - 2)));
		for (int i = 1; i < 4; i++) {
			int gap = piloto1.getNumeroVolta() - piloto2.getNumeroVolta();
			Volta vp1 = (Volta) piloto1.getVoltas().get(
					piloto1.getVoltas().size() - i - gap);
			if (vp1.isVoltaBox() || vp1.isVoltaSafetyCar()) {
				return null;
			}
			tabela = tabela.replaceAll("p1_v" + i, Html.sansSerif(vp1
					.obterTempoVoltaFormatado()));
			Volta vp2 = (Volta) piloto2.getVoltas().get(
					piloto2.getVoltas().size() - i);
			if (vp2.isVoltaBox() || vp2.isVoltaSafetyCar()) {
				return null;
			}
			tabela = tabela.replaceAll("p2_v" + i, Html.sansSerif(vp2
					.obterTempoVoltaFormatado()));
			long diff = (long) (vp2.obterTempoVolta() - vp1.obterTempoVolta());
			if (diff < 0)
				tabela = tabela.replaceAll("cor" + i, "#80FF00");
			else
				tabela = tabela.replaceAll("cor" + i, "#FFFF00");
			tabela = tabela.replaceAll("diff_v" + i, Html
					.sansSerif(formatarTempo(diff)));
		}
		return tabela;
	}

	private String carrgaTabelaComparativa() {
		try {
			InputStreamReader inputStreamReader = new InputStreamReader(
					CarregadorRecursos.recursoComoStream("tabela.html"));
			BufferedReader bufferedReader = new BufferedReader(
					inputStreamReader);
			StringBuffer buffer = new StringBuffer();
			String line;
			line = bufferedReader.readLine();
			while (line != null) {
				buffer.append(line);
				line = bufferedReader.readLine();
			}
			return buffer.toString();
		} catch (IOException e) {
			Logger.logarExept(e);
		}
		return "";
	}
}
