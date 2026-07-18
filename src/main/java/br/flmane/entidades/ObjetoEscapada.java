package br.flmane.entidades;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;
import java.util.ArrayList;
import java.util.List;

/**
 * Objeto de escapada: encadeamento de pontos clicados no editor, no mesmo
 * espírito de {@link ObjetoGuardRails} (clique esquerdo adiciona ponto,
 * clique direito finaliza), mas com validação nos dois papéis fixos das
 * pontas do encadeamento:
 * <ul>
 * <li>o PRIMEIRO ponto (nó de entrada) precisa estar perto de um nó do
 * traçado 1 ou 2 da pista (nunca do traçado central) — é esse traçado que
 * fica gravado em {@link #tracadoOrigem};</li>
 * <li>o ÚLTIMO ponto (nó de saída, definido pelo clique direito que
 * finaliza) precisa estar perto de um nó do MESMO traçado da entrada;</li>
 * <li>os pontos ENTRE os dois (o trajeto da zona de escapada em si) são
 * livres — sem validação, podem ficar em qualquer posição.</li>
 * </ul>
 * A validação em si fica no editor ({@code MainPanelEditor}), não aqui: esta
 * classe só guarda os pontos já validados e desenha o trajeto entre eles.
 * Substitui o modelo anterior (elipse solta, com largura/altura/ângulo
 * interpretados como comprimento/amplitude de uma onda). Consumido em tempo
 * de corrida por {@code Piloto.processaEscapadaAncoradaAoTracado()}, ancorado
 * ao traçado via {@code indiceEntrada}/{@code indiceSaida} e derivado em
 * {@code Circuito.gerarTracadosDeFuga()}.
 */
public class ObjetoEscapada extends ObjetoPista {

	public final static Color red = new Color(250, 0, 0, 150);

	/**
	 * Pontos em espaço LOCAL (o espaço em que foram originalmente clicados),
	 * não em espaço de tela — {@link #desenha} translada o caminho gerado
	 * para alinhar com {@link #posicaoQuina}, mesma ideia de
	 * {@link ObjetoGuardRails#getPontos()}, o que permite arrastar o objeto
	 * inteiro (via {@code posicaoQuina}) sem precisar tocar nesses pontos.
	 * {@code pontos.get(0)} é a entrada; {@code pontos.get(pontos.size()-1)}
	 * é a saída; os do meio são o trajeto livre da zona de escapada.
	 */
	private List<Point> pontos = new ArrayList<Point>();
	/** Traçado (1 ou 2) em que o primeiro ponto (entrada) foi ancorado; a saída é validada contra o mesmo traçado. */
	private int tracadoOrigem;
	/**
	 * Índice ({@code No.getIndex()}, compartilhado entre {@code pistaFull},
	 * {@code pista1Full} e {@code pista2Full}) do nó de traçado em que a
	 * entrada/saída foram ancoradas — gravado pelo editor no momento da
	 * validação (não recalculado depois). Usado por quem precisa saber ONDE
	 * ao longo da volta a zona de escapada fica (ex.: {@code TestePista}, que
	 * segue o trajeto de {@link #pontos} quando o índice da pista cai nesse
	 * intervalo).
	 */
	private int indiceEntrada = -1;
	private int indiceSaida = -1;
	private GeneralPath caminho = new GeneralPath();

	public ObjetoEscapada() {
		setCorPimaria(red);
		setCorSecundaria(Color.WHITE);
		// Espessura do traçado desenhado, no mesmo espírito da largura de
		// ObjetoGuardRails (não é mais comprimento de onda).
		setLargura(4);
	}

	public List<Point> getPontos() {
		return pontos;
	}

	public void setPontos(List<Point> pontos) {
		this.pontos = pontos;
	}

	public int getTracadoOrigem() {
		return tracadoOrigem;
	}

	public void setTracadoOrigem(int tracadoOrigem) {
		this.tracadoOrigem = tracadoOrigem;
	}

	public int getIndiceEntrada() {
		return indiceEntrada;
	}

	public void setIndiceEntrada(int indiceEntrada) {
		this.indiceEntrada = indiceEntrada;
	}

	public int getIndiceSaida() {
		return indiceSaida;
	}

	public void setIndiceSaida(int indiceSaida) {
		this.indiceSaida = indiceSaida;
	}

	/**
	 * Reconstrói {@link #caminho} a partir de {@link #pontos} — mesma ideia
	 * de {@link ObjetoGuardRails#gerar()}: chamado explicitamente ao
	 * finalizar a criação/edição no editor, e automaticamente por
	 * {@link #desenha}/{@link #obterArea()} na primeira vez (após recarregar
	 * de um XML, já que {@code caminho} não é bean property e não é
	 * persistido).
	 */
	public void gerar() {
		GeneralPath path = new GeneralPath();
		if (!pontos.isEmpty()) {
			Point primeiro = pontos.get(0);
			path.moveTo(primeiro.x, primeiro.y);
			for (int i = 1; i < pontos.size(); i++) {
				Point p = pontos.get(i);
				path.lineTo(p.x, p.y);
			}
		}
		caminho = path;
	}

	@Override
	public void desenha(Graphics2D g2d, double zoom) {
		if (pontos.size() < 2) {
			return;
		}
		if (caminho.getCurrentPoint() == null) {
			gerar();
		}
		if (posicaoQuina != null) {
			Rectangle bounds = caminho.getBounds();
			AffineTransform translacao = AffineTransform.getTranslateInstance(
					posicaoQuina.x - bounds.x, posicaoQuina.y - bounds.y);
			caminho.transform(translacao);
		}
		AffineTransform escala = AffineTransform.getScaleInstance(zoom, zoom);
		Shape caminhoNaTela = escala.createTransformedShape(caminho);

		g2d.setColor(getCorPimaria() != null ? getCorPimaria() : red);
		g2d.setStroke(new BasicStroke(Math.max(1, getLargura())));
		g2d.draw(caminhoNaTela);

		// Marcadores só na entrada e na saída (primeiro/último ponto do
		// caminho JÁ transladado/escalado), extraídos via PathIterator: o
		// primeiro SEG_MOVETO é a entrada, o último SEG_LINETO é a saída,
		// qualquer que seja a quantidade de pontos livres no meio.
		Point entradaTela = null;
		Point saidaTela = null;
		double[] coords = new double[6];
		for (PathIterator it = caminhoNaTela.getPathIterator(null); !it.isDone(); it.next()) {
			int tipo = it.currentSegment(coords);
			if (tipo == PathIterator.SEG_MOVETO) {
				entradaTela = new Point((int) Math.round(coords[0]), (int) Math.round(coords[1]));
			} else if (tipo == PathIterator.SEG_LINETO) {
				saidaTela = new Point((int) Math.round(coords[0]), (int) Math.round(coords[1]));
			}
		}
		g2d.setColor(getCorSecundaria() != null ? getCorSecundaria() : Color.WHITE);
		desenhaMarcador(g2d, entradaTela);
		desenhaMarcador(g2d, saidaTela);
	}

	private void desenhaMarcador(Graphics2D g2d, Point posTela) {
		if (posTela == null) {
			return;
		}
		g2d.fillOval(posTela.x - 4, posTela.y - 4, 8, 8);
	}

	/**
	 * {@link #pontos} deslocados por {@link #posicaoQuina} (mesma translação
	 * aplicada a {@link #caminho} em {@link #desenha}), em espaço absoluto de
	 * circuito (sem zoom) — {@code null} se houver menos de 2 pontos. Útil
	 * para quem precisa da posição atual do trajeto sem desenhar (ex.:
	 * overlay de debug de {@code PainelCircuito}, que desenha uma cópia
	 * deslocada pela câmera).
	 */
	public List<Point> obterPontosAbsolutos() {
		if (pontos.size() < 2) {
			return null;
		}
		if (caminho.getCurrentPoint() == null) {
			gerar();
		}
		if (posicaoQuina != null) {
			Rectangle bounds = caminho.getBounds();
			AffineTransform translacao = AffineTransform.getTranslateInstance(
					posicaoQuina.x - bounds.x, posicaoQuina.y - bounds.y);
			caminho.transform(translacao);
		}
		List<Point> resultado = new ArrayList<Point>();
		double[] coords = new double[6];
		for (PathIterator it = caminho.getPathIterator(null); !it.isDone(); it.next()) {
			int tipo = it.currentSegment(coords);
			if (tipo == PathIterator.SEG_MOVETO || tipo == PathIterator.SEG_LINETO) {
				resultado.add(new Point((int) Math.round(coords[0]), (int) Math.round(coords[1])));
			}
		}
		return resultado;
	}

	@Override
	public Rectangle obterArea() {
		if (caminho.getCurrentPoint() == null) {
			gerar();
		}
		return caminho.getBounds();
	}

	/**
	 * {@link #obterArea()} é só o retângulo bruto dos vértices clicados, sem
	 * considerar {@link #largura} — mesmo motivo de
	 * {@link ObjetoArquibancada#obterAreaVisual()}: sem essa folga, só era
	 * possível selecionar/arrastar o objeto clicando bem em cima de um dos
	 * pontos originais, não em qualquer parte do traçado desenhado.
	 */
	@Override
	public Rectangle obterAreaVisual() {
		Rectangle base = obterArea();
		if (base == null) {
			return null;
		}
		int folga = (Math.max(1, getLargura()) + 1) / 2;
		Rectangle expandido = new Rectangle(base);
		expandido.grow(folga, folga);
		return expandido;
	}

	public static void main(String[] args) {
	}

	/** Objeto de função (fica na listinha de baixo do editor): fora do sistema de níveis, sem sufixo "(nível)". */
	@Override
	public String toString() {
		return getNome() + " " + getClass().getSimpleName();
	}
}
