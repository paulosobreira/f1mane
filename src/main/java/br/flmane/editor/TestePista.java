package br.flmane.editor;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import br.nnpe.GeoUtil;
import br.nnpe.Logger;
import br.flmane.entidades.Circuito;
import br.flmane.entidades.No;
import br.flmane.entidades.ObjetoEscapada;
import br.flmane.entidades.ObjetoPista;

public class TestePista {
	protected static final long SEEP_TIME = 100;
	private final Circuito circuito;
	private Point testCar;
	public Point trazCar;
	public Point frenteCar;
	private boolean alive;
	private boolean irProBox;
	private boolean modoEscapada;
	/** Índice do nó atual do carro de teste — na pista principal (posicionaCarro/posicionaCarroConsiderandoEscapada) ou em pontosBox (posicionaCarroBox), conforme estaNoBox. */
	private int indexAtual;
	/** true só durante o trecho em que o carro de teste está percorrendo pontosBox (ver posicionaCarroBox); false na pista principal, inclusive em modo escapada. */
	private boolean estaNoBox;
	private List pontosPista;
	private List pontosBox;
	private final MainPanelEditor editor;
	private Thread testTh;

	public boolean isAlive() {
		return alive;
	}

	public TestePista(MainPanelEditor editor, Circuito circuito) {
		this.circuito = circuito;
		this.editor = editor;
	}

	public Point getTestCar() {
		return testCar;
	}

	public void setTestCar(Point testCar) {
		this.testCar = testCar;
	}

	public int getIndexAtual() {
		return indexAtual;
	}

	public boolean isEstaNoBox() {
		return estaNoBox;
	}

	public void iniciarTeste(final double multi) throws Exception {
		pontosPista = circuito.getPistaFull();
		pontosBox = circuito.getBoxFull();

		if (testTh != null) {
			alive = false;
			testTh.interrupt();
			testTh = null;
			return;
		}

		modoEscapada = false;

		testTh = new Thread(new Runnable() {
			public void run() {
				while (alive) {
					int cont = 0;
					No no;
					try {
						while (cont < pontosPista.size() && alive) {
							editor.setUltimoClicado(null);
							no = (No) pontosPista.get(cont);
							int entradaBox = circuito.getEntradaBoxIndex();
							if (irProBox
									&& (cont > (entradaBox - 100) && cont < (entradaBox + 100))) {
								int contBox = 0;
								while (contBox < pontosBox.size() && alive) {
									No noBox = (No) pontosBox.get(contBox);
									posicionaCarroBox(contBox, noBox, pontosBox);
									centralizaTestCar();
									if (No.RETA.equals(noBox.getTipo())
											|| No.LARGADA.equals(noBox
													.getTipo())) {
										contBox += ((Math.random() > .7 ? 3 : 4) * multi);

									} else if (No.CURVA_ALTA.equals(noBox
											.getTipo())) {
										contBox += ((Math.random() > .7 ? 2 : 3) * multi);

									} else {
										contBox += ((Math.random() > .7 ? 1 : 2) * multi);
									}

									Thread.sleep(SEEP_TIME);
								}

								cont = circuito.getSaidaBoxIndex();
								no = (No) pontosPista.get(cont);
								irProBox = false;

							}

							posicionaCarroConsiderandoEscapada(cont, pontosPista);
							centralizaTestCar();
							if (No.RETA.equals(no.getTipo())
									|| No.LARGADA.equals(no.getTipo())) {
								cont += ((Math.random() > .7 ? 3 : 4) * multi);

							} else if (No.CURVA_ALTA.equals(no.getTipo())) {
								cont += ((Math.random() > .7 ? 2 : 3) * multi);
							} else if (No.CURVA_BAIXA.equals(no.getTipo())) {
								cont += ((Math.random() > .7 ? 1 : 2) * multi);
							}

							Thread.sleep(SEEP_TIME);
						}
					} catch (Exception e) {
						Logger.logarExept(e);
					}
				}
			}
		});
		alive = true;
		testTh.start();
	}

	protected void posicionaCarro(int cont, No no, List lista) {
		int traz = cont - 44;
		int frente = cont + 44;

		if (traz < 0) {
			traz = (lista.size() - 1) + traz;
		}
		if (frente > lista.size()) {
			frente = frente - (lista.size() - 1);
		}

		trazCar = ((No) lista.get(traz)).getPoint();
		frenteCar = ((No) lista.get(frente)).getPoint();

		testCar = no.getPoint();
		indexAtual = cont;
		estaNoBox = false;

	}

	/**
	 * Posiciona o carro de teste no índice {@code cont}. Com
	 * {@code modoEscapada} desligado (o padrão — sempre reiniciado pra
	 * desligado ao carregar/criar um circuito e a cada início ou fim de
	 * teste, ver {@link #iniciarTeste}, {@link #pararTeste} e
	 * {@link #testarEscapada()}), o carro de teste fica sempre no traçado
	 * central, ignorando qualquer zona de escapada. Só com
	 * {@code modoEscapada} ligado é que ele passa a seguir o trajeto de um
	 * {@link ObjetoEscapada} (entrada → trajeto livre → saída) em vez da
	 * pista normal, quando o índice cai dentro do intervalo
	 * [{@code indiceEntrada}, {@code indiceSaida}] ancorado no traçado em que
	 * essa escapada foi definida — igual acontece com a pista normal e o box.
	 */
	protected void posicionaCarroConsiderandoEscapada(int cont, List pontosPista) {
		List tracadoEscapada = modoEscapada ? obterTracadoEscapadaAtivo(cont) : null;
		if (tracadoEscapada == null) {
			posicionaCarro(cont, (No) pontosPista.get(cont), pontosPista);
			return;
		}

		int traz = cont - 44;
		int frente = cont + 44;
		if (traz < 0) {
			traz = (pontosPista.size() - 1) + traz;
		}
		if (frente > pontosPista.size()) {
			frente = frente - (pontosPista.size() - 1);
		}

		trazCar = noNaListaOuFallback(tracadoEscapada, pontosPista, traz).getPoint();
		frenteCar = noNaListaOuFallback(tracadoEscapada, pontosPista, frente).getPoint();
		testCar = noNaListaOuFallback(tracadoEscapada, pontosPista, cont).getPoint();
		indexAtual = cont;
		estaNoBox = false;
	}

	/**
	 * Procura, entre os {@link ObjetoEscapada} do circuito, um cujo intervalo
	 * [{@code indiceEntrada}, {@code indiceSaida}] cubra {@code index} — esse
	 * intervalo é ancorado a nós reais do traçado em que a escapada foi
	 * definida (gravado pelo editor na validação de entrada/saída, ver
	 * {@code MainPanelEditor.noMaisProximoTracado1e2}/{@code noMaisProximoDoTracado}),
	 * então só ativa quando o carro está passando pelo trecho da pista onde
	 * essa escapada realmente existe. Se encontrar, retorna o traçado de
	 * escapada (lista de nós do mesmo tamanho da pista, com {@code null} fora
	 * da zona, interpolado ao longo do trajeto de pontos da escapada); senão,
	 * {@code null}.
	 */
	private List<No> obterTracadoEscapadaAtivo(int index) {
		List<ObjetoPista> objetos = circuito.getObjetos();
		if (objetos == null) {
			return null;
		}
		List pontosPistaAtual = circuito.getPistaFull();
		int tamanho = pontosPistaAtual != null ? pontosPistaAtual.size() : 0;
		for (ObjetoPista objetoPista : objetos) {
			if (!(objetoPista instanceof ObjetoEscapada)) {
				continue;
			}
			ObjetoEscapada escapada = (ObjetoEscapada) objetoPista;
			int indiceEntrada = escapada.getIndiceEntrada();
			int indiceSaida = escapada.getIndiceSaida();
			if (indiceEntrada < 0 || indiceSaida <= indiceEntrada) {
				continue;
			}
			if (index < indiceEntrada || index > indiceSaida) {
				continue;
			}
			return construirTracadoEscapada(escapada, tamanho);
		}
		return null;
	}

	/**
	 * Constrói uma lista de nós do tamanho da pista, com {@code null} fora do
	 * intervalo [{@code indiceEntrada}, {@code indiceSaida}] de
	 * {@code escapada} e, dentro dele, um nó por índice interpolado (por
	 * comprimento de arco) ao longo do trajeto de {@link ObjetoEscapada#getPontos()}
	 * — o índice {@code indiceEntrada} cai exatamente no primeiro ponto
	 * (entrada) e {@code indiceSaida} no último (saída).
	 */
	private List<No> construirTracadoEscapada(ObjetoEscapada escapada, int tamanho) {
		List<No> resultado = new ArrayList<No>(Collections.<No>nCopies(tamanho, null));
		List<Point> pontos = escapada.obterPontosAbsolutos();
		int indiceEntrada = escapada.getIndiceEntrada();
		int indiceSaida = escapada.getIndiceSaida();
		if (pontos == null || pontos.size() < 2) {
			return resultado;
		}
		int comprimento = indiceSaida - indiceEntrada;
		for (int passo = 0; passo <= comprimento; passo++) {
			int index = indiceEntrada + passo;
			if (index < 0 || index >= tamanho) {
				continue;
			}
			double t = (double) passo / (double) comprimento;
			No no = new No();
			no.setPoint(GeoUtil.pontoNoTrajeto(pontos, t));
			no.setTipo(No.RETA);
			no.setIndex(index);
			resultado.set(index, no);
		}
		return resultado;
	}

	/**
	 * Lê o nó no índice informado em {@code preferida}; se estiver fora dos
	 * limites da zona de escapada ({@code null} ali, ex.: nas bordas da
	 * janela de 44 nós usada para calcular a orientação do carro), usa
	 * {@code fallback} (a pista normal) nesse índice.
	 */
	private No noNaListaOuFallback(List preferida, List fallback, int index) {
		if (index >= 0 && index < preferida.size() && preferida.get(index) != null) {
			return (No) preferida.get(index);
		}
		return (No) fallback.get(index);
	}

	protected void posicionaCarroBox(int cont, No no, List lista) {
		int traz = cont - 44;
		int frente = cont + 44;

		if (traz < 0) {
			traz = 0;
		}
		if (frente > lista.size()) {
			frente = (lista.size() - 1);
		}

		trazCar = ((No) lista.get(traz)).getPoint();
		frenteCar = ((No) lista.get(frente)).getPoint();

		testCar = no.getPoint();
		indexAtual = cont;
		estaNoBox = true;

	}

public void testarBox() {
		irProBox = !irProBox;
	}

	public boolean isIrProBox() {
		return irProBox;
	}

	public void testarEscapada() {
		modoEscapada = !modoEscapada;
	}

	public boolean isModoEscapada() {
		return modoEscapada;
	}

	protected void centralizaTestCar() {
		editor.centralizarPonto(testCar);
	}

	public void pararTeste() {
		modoEscapada = false;
		if (testTh != null) {
			alive = false;
			testTh.interrupt();
			testTh = null;
		}
	}
}
