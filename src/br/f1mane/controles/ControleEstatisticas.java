package br.f1mane.controles;

import java.awt.BorderLayout;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import br.nnpe.Constantes;
import br.nnpe.Html;
import br.nnpe.Logger;
import br.nnpe.Util;
import br.f1mane.entidades.Piloto;
import br.f1mane.entidades.Volta;
import br.f1mane.recursos.CarregadorRecursos;
import br.f1mane.recursos.idiomas.Lang;

/**
 * @author Paulo Sobreira Criado em 16/06/2007 as 13:24:54
 */
public class ControleEstatisticas {
	private JPanel painelDebug;
	private JEditorPane infoTextual;
	private JScrollPane scrollPaneTextual;
	private InterfaceJogo controleJogo;
	private Volta voltaMaisRapida;
	private static DecimalFormat mil = new DecimalFormat("000");
	public static DecimalFormat dez = new DecimalFormat("00");
	private LinkedList bufferInfo = new LinkedList();
	private LinkedList<String> allInfo = new LinkedList<String>();
	private boolean consumidorAtivo = true;
	private Thread infoConsumer;
	private long timeStampUltinfo;
	public static long maiorTempo;
	private static long inicio;
	public static long tempoAtual;

	public static void inicioMedicao() {
		inicio = System.currentTimeMillis();

	}

	public static void fimMedicao() {
		long tempo = System.currentTimeMillis() - inicio;
		tempoAtual = tempo;
		if (tempo > maiorTempo) {
			maiorTempo = tempo;
		}
	}

	/**
	 * @param controleJogo
	 */
	public ControleEstatisticas(InterfaceJogo controleJogo) {
		super();
		this.controleJogo = controleJogo;
	}

	private long calculaDiferenca(Piloto frente, Piloto piloto) {
		long diff = frente.getPtosPista() - piloto.getPtosPista();
		return diff;
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
		controleJogo.descontaTempoPausado(volta);
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

		for (Iterator iter = piloto.getVoltasCopy().iterator(); iter.hasNext();) {
			Volta volta = (Volta) iter.next();

			if (voltaAtual.obterTempoVolta() > volta.obterTempoVolta()) {
				teveMelhor = true;
			}
		}

		if (!teveMelhor && !controleJogo.isSafetyCarNaPista()) {
			if (controleJogo.verificaInfoRelevante(piloto)) {

				controleJogo.info(Html.verde(Lang.msg("022",
						new String[]{piloto.nomeJogadorFormatado(),
								Html.negrito(piloto.getNome()),
								voltaAtual.getTempoVoltaFormatado()})));
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
			controleJogo.infoPrioritaria(Html.verde(Lang.msg("023",
					new String[]{piloto.nomeJogadorFormatado(),
							Html.negrito(piloto.getNome()), voltaMaisRapida
									.getTempoVoltaFormatado()})));
		}
	}

	public Volta getVoltaMaisRapida() {
		return voltaMaisRapida;
	}

	public static String formatarTempo(Long value) {
		if (value == null) {
			return null;
		}
		long minu = (value / 60000);
		long seg = ((value - (minu * 60000)) / 1000);
		long mili = value - ((minu * 60000) + (seg * 1000));
		if (minu > 0)
			return (minu) + ":" + dez.format(Math.abs(seg)) + "."
					+ mil.format(Math.abs(mili));
		else
			return seg + "." + mil.format(Math.abs(mili));
	}

	public double calculaDiferencaParaProximoDouble(Piloto psel) {
		return new Double(calculaDiferencaParaProximo(psel));

	}

	public int calculaDiferencaParaProximo(Piloto psel) {
		int pos = psel.getPosicao() - 2;
		if (pos < 0) {
			return Integer.MAX_VALUE;
		}
		List<Piloto> pilotosCopia = controleJogo.getPilotosCopia();
		if (pos > pilotosCopia.size() - 1) {
			return Integer.MAX_VALUE;
		}
		Piloto piloto = (Piloto) pilotosCopia.get(pos);
		if (piloto != null) {
			long diff = calculaDiferenca(piloto, psel);
			return Util.inteiro(diff);
		}
		return Integer.MAX_VALUE;
	}

	public int calculaDiferencaParaAnterior(Piloto psel) {
		int pos = psel.getPosicao();
		if (pos < 0) {
			return Integer.MAX_VALUE;
		}
		if (pos > controleJogo.getPilotos().size() - 1) {
			return Integer.MAX_VALUE;
		}
		Piloto piloto = (Piloto) controleJogo.getPilotos().get(pos);
		if (piloto != null) {
			long diff = calculaDiferenca(psel, piloto);
			return Util.inteiro(diff);
		}
		return Integer.MAX_VALUE;
	}

	public void zerarMelhorVolta() {
		voltaMaisRapida = null;

	}

	public void inicializarThreadConsumidoraInfo() {
		bufferInfo.clear();
		infoConsumer = new Thread(new Runnable() {
			public void run() {
				try {
					controleJogo.adicionarInfoDireto(Html.verde(Lang.msg("000",
							new Object[]{controleJogo.totalVoltasCorrida()})));
					boolean interruput = false;
					while (!interruput && consumidorAtivo) {
						try {
							controleJogo.atulizaTabelaPosicoes();
							try {
								if (!bufferInfo.isEmpty()) {
									Object object = bufferInfo.iterator()
											.next();
									controleJogo.adicionarInfoDireto(
											(String) object);
									bufferInfo.remove(object);
								}
							} catch (Exception e) {
								Logger.logarExept(e);
							}
							Thread.sleep(1000);
						} catch (Exception e) {
							interruput = true;
							Logger.logarExept(e);
						}
					}
				} catch (Exception e) {
					Logger.logarExept(e);
				}
			}
		});
		infoConsumer.start();
	}

	public static void main(String[] args) {
		// List allInfo = new ArrayList();
		// allInfo.add("1");
		// allInfo.add("2");
		// allInfo.add("3");
		// allInfo.add("4");
		// allInfo.add("5");
		// allInfo.add("6");
		// allInfo.add("7");
		// if (allInfo.size() > 5) {
		// for (int i = allInfo.size() - 1; i > allInfo.size() - 6; i--) {
		// }
		// }
		System.out.println(formatarTempo(1342l));
	}

	public void info(String info, boolean prioritaria) {
		if(info==null) {
			return;
		}
		if (controleJogo.isModoQualify()) {
			return;
		}
		if (bufferInfo.contains(info)) {
			return;
		}
		int limMin = -1;
		if (allInfo.size() > 5) {
			limMin = allInfo.size() - 6; 
		}
		for (int i = allInfo.size() - 1; i > limMin; i--) {
			if (info.equals(allInfo.get(i))) {
				return;
			}
		}
		timeStampUltinfo = System.currentTimeMillis();
		if (prioritaria) {
			bufferInfo.addFirst(info);
		} else {
			bufferInfo.add(info);
		}
		allInfo.add(info);
	}

	public void info(String info) {
		if (controleJogo.isModoQualify()) {
			return;
		}
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
		List<Piloto> pilotos = new ArrayList<Piloto>();
		for (Iterator<Piloto> iterator = controleJogo.getPilotosCopia()
				.iterator(); iterator.hasNext();) {
			Piloto piloto = iterator.next();
			if (piloto.isJogadorHumano() && !piloto.isDesqualificado()) {
				pilotos.add(piloto);
			}

		}

		if (pilotos.isEmpty()) {
			for (Iterator<Piloto> iterator = controleJogo.getPilotosCopia()
					.iterator(); iterator.hasNext();) {
				Piloto piloto = iterator.next();
				if (piloto.getPosicao() < 9) {
					pilotos.add(piloto);
				}

			}
		}
		Collections.shuffle(pilotos);
		Piloto pilotoSel = (Piloto) pilotos.get(0);
		Piloto pilotoComp = null;
		if (pilotoSel.getPosicao() == 1) {
			pilotoComp = controleJogo.obterCarroAtras(pilotoSel).getPiloto();
		} else {
			pilotoComp = controleJogo.obterCarroNaFrente(pilotoSel).getPiloto();
		}
		if (pilotoComp.getVoltasCopy().size() < 3
				|| pilotoSel.getVoltas().size() < 3) {
			return;
		}
		if (pilotoSel.getPosicao() == 1) {
			tabela = preencherTabela(pilotoSel, pilotoComp, tabela);
		} else {
			tabela = preencherTabela(pilotoComp, pilotoSel, tabela);
		}
		if (tabela != null && !pilotoSel.entrouNoBox()) {
			controleJogo.info(tabela);
		}
	}

	private String preencherTabela(Piloto piloto1, Piloto piloto2,
			String tabela) {
		tabela = tabela.replaceAll("piloto1",
				piloto1.getNomeAbreviado() + " " + piloto1.getPosicao());
		tabela = tabela.replaceAll("piloto2",
				piloto2.getNomeAbreviado() + " " + piloto2.getPosicao());
		tabela = tabela.replaceAll("volta1",
				Lang.msg("081") + (piloto2.getNumeroVolta()));
		tabela = tabela.replaceAll("volta2",
				Lang.msg("081") + (piloto2.getNumeroVolta() - 1));
		tabela = tabela.replaceAll("volta3",
				Lang.msg("081") + (piloto2.getNumeroVolta() - 2));
		for (int i = 1; i < 4; i++) {
			int gap = piloto1.getNumeroVolta() - piloto2.getNumeroVolta();
			List<Volta> voltasP1 = piloto1.getVoltasCopy();
			List<Volta> voltasP2 = piloto2.getVoltasCopy();
			int index = voltasP1.size() - i - gap;
			if (index < 0) {
				index = 0;
			}
			if (index > (voltasP1.size() - 1)) {
				return null;
			}
			Volta vp1 = (Volta) voltasP1.get(index);
			if (vp1.isVoltaBox() || vp1.isVoltaSafetyCar()) {
				return null;
			}
			tabela = tabela.replaceAll("p1_v" + i,
					(vp1.getTempoVoltaFormatado()));
			Volta vp2 = (Volta) voltasP2
					.get(voltasP2.size() - i);
			if (vp2.isVoltaBox() || vp2.isVoltaSafetyCar()) {
				return null;
			}
			tabela = tabela.replaceAll("p2_v" + i,
					(vp2.getTempoVoltaFormatado()));
			long diff = (long) (vp2.obterTempoVolta() - vp1.obterTempoVolta());
			if (diff < 0) {
				tabela = tabela.replaceAll("cor" + i, "#80FF00");
				String subs = formatarTempo(diff);
				tabela = tabela.replaceAll("diff_v" + i,
						(subs.startsWith("-") ? subs : "-" + subs));
			} else {
				tabela = tabela.replaceAll("cor" + i, "#FFFF00");
				tabela = tabela.replaceAll("diff_v" + i, (formatarTempo(diff)));
			}

		}
		return tabela;
	}

	public boolean verificaInfoRelevante(Piloto piloto) {
		if (timeStampUltinfo != 0
				&& (System.currentTimeMillis() - timeStampUltinfo) < 3000) {
			return false;
		}
		if (piloto.isJogadorHumano()) {
			return true;
		}
		if (piloto.getPosicao() == 1) {
			return true;
		}
		if (controleJogo.verificaCampeonatoComRival()
				&& piloto.equals(controleJogo.obterRivalCampeonato())) {
			return true;
		}

		return false;
	}

	private String carrgaTabelaComparativa() {
		try {
			InputStreamReader inputStreamReader = new InputStreamReader(
					CarregadorRecursos.recursoComoStream("tabela.html"));
			BufferedReader bufferedReader = new BufferedReader(
					inputStreamReader);
			StringBuilder buffer = new StringBuilder();
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

	public int calculaDiffParaProximoRetardatario(Piloto piloto,
			boolean analisaTracado) {
		List<Piloto> pilotos = controleJogo.getPilotosCopia();
		int menorDistancia = Util.inteiro(Integer.MAX_VALUE);
		if (piloto.getPtosBox() != 0) {
			return menorDistancia;
		}
		if (piloto.getNoAtual() == null) {
			return menorDistancia;
		}
		int indexAtual = piloto.getNoAtual().getIndex();
		for (Iterator iterator = pilotos.iterator(); iterator.hasNext();) {
			Piloto pilotoFrente = (Piloto) iterator.next();
			if (pilotoFrente.equals(piloto)) {
				continue;
			}
			if (piloto.verificaNaoPrecisaDesviar(pilotoFrente)) {
				continue;
			}
			if (pilotoFrente.getPtosBox() != 0) {
				continue;
			}
			if (pilotoFrente.getTracado() == 4
					|| pilotoFrente.getTracado() == 5) {
				continue;
			}
			if (pilotoFrente.getNoAtual() == null) {
				continue;
			}
			if (analisaTracado
					&& pilotoFrente.getTracado() != piloto.getTracado()) {
				continue;
			}
			int indexFrente = pilotoFrente.getNoAtual().getIndex();
			if (indexFrente > indexAtual
					&& (indexFrente - indexAtual) < menorDistancia) {
				menorDistancia = (indexFrente - indexAtual);
			}

			int tamPista = controleJogo.getCircuito().getPistaFull().size();
			int diffTAmPista = tamPista - indexAtual;
			if (indexFrente < indexAtual
					&& (indexFrente + diffTAmPista) < menorDistancia) {
				menorDistancia = (indexFrente + diffTAmPista);
			}
			List obterPista = controleJogo.obterPista(piloto.getNoAtual());
			if (obterPista == null) {
				continue;
			}
			indexFrente += controleJogo.obterPista(piloto.getNoAtual()).size();
			if (indexFrente > indexAtual
					&& (indexFrente - indexAtual) < menorDistancia) {
				menorDistancia = (indexFrente - indexAtual);
			}
		}

		return Util.inteiro(menorDistancia);

	}

	public JPanel getPainelDebug() {
		if (painelDebug == null) {
			gerarPainelDebug();
		}
		return painelDebug;
	}

	private void gerarPainelDebug() {
		painelDebug = new JPanel(new BorderLayout());
		infoTextual = new JEditorPane("text/html", "");
		infoTextual.setEditable(false);
		scrollPaneTextual = new JScrollPane(infoTextual);
		painelDebug.setLayout(new BorderLayout());
		painelDebug.add(scrollPaneTextual, BorderLayout.CENTER);
	}

	public void atualizaInfoDebug() {
		StringBuilder buffer = new StringBuilder();

		if (controleJogo != null) {
			controleJogo.atualizaInfoDebug(buffer);
		}

		if (controleJogo != null
				&& controleJogo.getPilotoSelecionado() != null) {
			controleJogo.getPilotoSelecionado().atualizaInfoDebug(buffer);
		}

		if (controleJogo != null && controleJogo.getPilotoSelecionado() != null
				&& controleJogo.getPilotoSelecionado().getCarro() != null) {
			controleJogo.getPilotoSelecionado().getCarro()
					.atualizaInfoDebug(buffer);
		}

		final StringReader reader = new StringReader((buffer.toString()));
		Runnable doInfo = new Runnable() {
			public void run() {
				try {
					infoTextual.read(reader, "");
				} catch (IOException e) {
					Logger.logarExept(e);
				}
			}
		};
		SwingUtilities.invokeLater(doInfo);

	}

	public String calculaSegundosParaProximo(Piloto psel, int diferenca) {
		return formatarTempo(diferecaParaSegundos(diferenca));
	}

	public String calculaSegundosParaProximo(Piloto psel) {
		int diff = calculaDiferencaParaProximo(psel);
		return calculaSegundosParaProximo(psel, diff);
	}

	public String calculaSegundosParaLider(Piloto pilotoSelecionado) {
		Piloto lider = (Piloto) controleJogo.getPilotosCopia().get(0);
		long diff = calculaDiferenca(lider, pilotoSelecionado);
		String ret = formatarTempo(diferecaParaSegundos(diff));
		pilotoSelecionado.setSegundosParaLider(ret);
		return ret;
	}

	public String calculaSegundosParaRival(Piloto pilotoSelecionado,
			Piloto rival, long tempo) {
		long diff = calculaDiferenca(rival, pilotoSelecionado);
		String ret = formatarTempo(diferecaParaSegundos(diff));
		return ret;
	}

	private Long diferecaParaSegundos(long diff) {
		return Math
				.round((diff / new Double(Util.intervalo(30, 40)).doubleValue())
						* Constantes.CICLO);
	}
}
