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
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

/**
 * Arquibancada: desenhada ponto a ponto no editor (um clique de cada vez,
 * botão direito finaliza), como {@link ObjetoGuardRails} — é um encadeamento
 * de segmentos retos entre pontos consecutivos, com o lance de arquibancada
 * (faixa sólida + listras internas) acompanhando toda a extensão do
 * encadeamento, não mais um único retângulo largura×altura. {@link #largura}
 * passa a ser a espessura do lance ao longo de todo o percurso (mesmo papel
 * de {@link ObjetoGuardRails#getLargura()}); altura/ângulo não têm mais
 * efeito, pelo mesmo motivo de GuardRails.
 */
public class ObjetoArquibancada extends ObjetoDesenho {

	/** Espessura (em pixels de mundo) de cada listra interna e do vão entre elas — igual à aparência original. */
	private static final int LARGURA_LISTRA = 10;
	private static final int VAO_ENTRE_LISTRAS = 10;

	/**
	 * Pontos clicados pelo usuário no editor (um por clique esquerdo,
	 * finalizado com o direito), na ordem em que formam o encadeamento —
	 * mesma ideia de {@link ObjetoGuardRails#getPontos()}.
	 */
	private List<Point> pontos = new ArrayList<Point>();
	private GeneralPath caminho = new GeneralPath();

	public ObjetoArquibancada() {
		setLargura(60);
		setCorPimaria(new Color(150, 150, 150));
		setCorSecundaria(new Color(90, 90, 90));
		setTransparencia(255);
	}

	public List<Point> getPontos() {
		return pontos;
	}

	public void setPontos(List<Point> pontos) {
		this.pontos = pontos;
	}

	/**
	 * Move o ponto de índice {@code indice} para {@code novaPosicao} e
	 * reconstrói {@link #caminho} — usado pelo editor ao arrastar um ponto já
	 * existente em modo de edição de pontos.
	 */
	public void moverPonto(int indice, Point novaPosicao) {
		pontos.set(indice, novaPosicao);
		gerar();
	}

	/**
	 * Insere {@code posicao} no encadeamento logo após o índice
	 * {@code indiceSegmento}, usado pelo editor para adicionar um ponto no
	 * meio de um segmento existente.
	 */
	public void inserirPonto(int indiceSegmento, Point posicao) {
		pontos.add(indiceSegmento + 1, posicao);
		gerar();
	}

	/**
	 * Remove o ponto de índice {@code indice}, exceto quando restariam menos
	 * de dois pontos (o mínimo pra formar um segmento) — nesse caso a
	 * remoção é ignorada.
	 */
	public void removerPonto(int indice) {
		if (pontos.size() <= 2) {
			return;
		}
		pontos.remove(indice);
		gerar();
	}

	/**
	 * Reconstrói {@link #caminho} a partir de {@link #pontos} — chamado
	 * explicitamente ao finalizar a criação no editor (como
	 * {@link ObjetoGuardRails#gerar()}), e automaticamente por {@link #desenha}
	 * na primeira vez (após recarregar de um XML, por exemplo, já que
	 * {@code caminho} não é bean property e não é persistido).
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
		if (caminho.getCurrentPoint() == null) {
			gerar();
		}
		if (posicaoQuina != null) {
			Rectangle bounds = caminho.getBounds();
			AffineTransform translacao = AffineTransform.getTranslateInstance(
					posicaoQuina.x - bounds.x, posicaoQuina.y - bounds.y);
			caminho.transform(translacao);
		}

		List<Point2D> vertices = extrairVertices(caminho);
		if (vertices.size() < 2) {
			return;
		}

		AffineTransform escala = AffineTransform.getScaleInstance(zoom, zoom);

		// CAP_BUTT: a faixa termina exatamente no primeiro/último ponto
		// clicado, sem nenhuma extensão além deles (CAP_SQUARE ainda
		// projetava meia largura pra fora das pontas, só trocando o formato
		// dessa borda indesejada de arredondada pra quadrada, sem removê-la).
		Shape formaExterna = new BasicStroke(Math.max(1, largura), BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER)
				.createStrokedShape(caminho);
		g2d.setColor(new Color(getCorPimaria().getRed(), getCorPimaria()
				.getGreen(), getCorPimaria().getBlue(), getTransparencia()));
		g2d.fill(escala.createTransformedShape(formaExterna));

		GeneralPath listras = construirListras(vertices);
		g2d.setColor(new Color(getCorSecundaria().getRed(), getCorSecundaria()
				.getGreen(), getCorSecundaria().getBlue(), getTransparencia()));
		g2d.fill(escala.createTransformedShape(listras));
	}

	private static List<Point2D> extrairVertices(GeneralPath caminho) {
		List<Point2D> vertices = new ArrayList<Point2D>();
		double[] coords = new double[6];
		for (PathIterator it = caminho.getPathIterator(null); !it.isDone(); it.next()) {
			int tipo = it.currentSegment(coords);
			if (tipo == PathIterator.SEG_MOVETO || tipo == PathIterator.SEG_LINETO) {
				vertices.add(new Point2D.Double(coords[0], coords[1]));
			}
		}
		return vertices;
	}

	/**
	 * Listras contínuas ao longo de TODO o percurso, periódicas através da
	 * {@link #largura} do lance, com espessura/vão fixos em
	 * {@link #LARGURA_LISTRA}/{@link #VAO_ENTRE_LISTRAS} pra preservar a
	 * aparência original da arquibancada. Cada listra é o traço (com juntas
	 * em esquadro/miter, como {@code formaExterna} — bordas quadradas, sem
	 * arredondamento) de um caminho paralelo ao encadeamento original,
	 * deslocado da centerline pela distância dessa listra — em vez de
	 * retângulos independentes por segmento (cuja normal diverge nos
	 * vértices, deixando vãos triangulares do lado mais aberto de uma curva
	 * acentuada), o traço de um único caminho contínuo fecha essas curvas do
	 * mesmo jeito que a faixa externa já fecha.
	 */
	private GeneralPath construirListras(List<Point2D> vertices) {
		int periodoAlvo = LARGURA_LISTRA + VAO_ENTRE_LISTRAS;
		int quantidadeListras = Math.max(1, (int) (largura / periodoAlvo));
		double vaoReal = quantidadeListras > 1
				? (largura - quantidadeListras * LARGURA_LISTRA) / (double) (quantidadeListras - 1)
				: 0;
		double periodoReal = LARGURA_LISTRA + vaoReal;

		BasicStroke strokeListra = new BasicStroke(LARGURA_LISTRA, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER);
		GeneralPath listras = new GeneralPath();
		for (int i = 0; i < quantidadeListras; i++) {
			double offsetCentro = -largura / 2.0 + i * periodoReal + LARGURA_LISTRA / 2.0;
			GeneralPath caminhoDeslocado = construirCaminhoDeslocado(vertices, offsetCentro);
			listras.append(strokeListra.createStrokedShape(caminhoDeslocado), false);
		}
		return listras;
	}

	/**
	 * Caminho paralelo a {@code vertices}, deslocado {@code offset} pixels
	 * perpendicularmente à direção do percurso. Nos vértices internos, usa a
	 * bissetriz (soma normalizada) das normais dos dois segmentos vizinhos
	 * em vez da normal de um único segmento — mantém o deslocamento contínuo
	 * através do vértice, sem o qual o caminho "quebraria" ali (mesma
	 * necessidade que motiva as juntas de um traço comum, mas aplicada à
	 * própria centerline antes de riscar).
	 */
	private static GeneralPath construirCaminhoDeslocado(List<Point2D> vertices, double offset) {
		int quantidadeSegmentos = vertices.size() - 1;
		double[] normalX = new double[quantidadeSegmentos];
		double[] normalY = new double[quantidadeSegmentos];
		for (int s = 0; s < quantidadeSegmentos; s++) {
			Point2D p1 = vertices.get(s);
			Point2D p2 = vertices.get(s + 1);
			double dx = p2.getX() - p1.getX();
			double dy = p2.getY() - p1.getY();
			double comp = Math.hypot(dx, dy);
			normalX[s] = comp == 0 ? 0 : -dy / comp;
			normalY[s] = comp == 0 ? 0 : dx / comp;
		}

		GeneralPath caminho = new GeneralPath();
		for (int j = 0; j < vertices.size(); j++) {
			double nx;
			double ny;
			if (j == 0) {
				nx = normalX[0];
				ny = normalY[0];
			} else if (j == quantidadeSegmentos) {
				nx = normalX[quantidadeSegmentos - 1];
				ny = normalY[quantidadeSegmentos - 1];
			} else {
				double somaX = normalX[j - 1] + normalX[j];
				double somaY = normalY[j - 1] + normalY[j];
				double comp = Math.hypot(somaX, somaY);
				nx = comp == 0 ? normalX[j] : somaX / comp;
				ny = comp == 0 ? normalY[j] : somaY / comp;
			}
			double x = vertices.get(j).getX() + nx * offset;
			double y = vertices.get(j).getY() + ny * offset;
			if (j == 0) {
				caminho.moveTo(x, y);
			} else {
				caminho.lineTo(x, y);
			}
		}
		return caminho;
	}

	@Override
	public Rectangle obterArea() {
		return caminho.getBounds();
	}

	/**
	 * {@link #obterArea()} é só o retângulo bruto da centerline (os pontos
	 * clicados), sem considerar {@link #largura} — precisa continuar assim
	 * porque {@link #desenha} e o editor (ver
	 * {@code MainPanelEditor#calculaOffsetTelaArquibancada}) usam esse
	 * retângulo bruto pra alinhar a translação com {@link #getPosicaoQuina()}.
	 * A área "real" do objeto (usada tanto pelo contorno visual de seleção
	 * quanto, via {@link #obterAreaClique()}, pra clicar/arrastar o objeto
	 * inteiro) precisa cobrir o lance visível (a faixa de espessura
	 * {@link #largura}), não só uma linha fina em cima da centerline — sem
	 * isso, o retângulo mostrado na seleção não batia com o desenho, e só
	 * era possível selecionar/arrastar o objeto clicando bem em cima dos
	 * pontos originais, não em qualquer parte do lance desenhado.
	 */
	@Override
	public Rectangle obterAreaVisual() {
		Rectangle base = obterArea();
		if (base == null) {
			return null;
		}
		int folga = (Math.max(1, largura) + 1) / 2;
		Rectangle expandido = new Rectangle(base);
		expandido.grow(folga, folga);
		return expandido;
	}
}
