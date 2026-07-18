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
 * Guard rails: um misto de {@link ObjetoArquibancada} (linhas finas repetidas)
 * com {@link ObjetoLivre} (desenhado clicando ponto a ponto no editor, botão
 * direito finaliza) — é um encadeamento de vetores de guardrails: cada par de
 * pontos consecutivos vira um segmento reto de barreira, e o padrão de
 * linhas finas percorre a extensão inteira do encadeamento continuamente
 * (não reinicia a cada segmento/vértice).
 */
public class ObjetoGuardRails extends ObjetoDesenho {

	/**
	 * Espessura de cada linha, em unidades de mundo (antes do zoom) — bem
	 * mais fina que a da arquibancada (10px). 1px é o mínimo possível.
	 * Editável pelo formulário de objetos; default 1 preserva a aparência de
	 * circuitos XML gravados antes desta propriedade existir.
	 */
	private int larguraLinha = 1;
	/** Vão entre linhas consecutivas — mesma proporção 1:1 da arquibancada por padrão. */
	private int vaoEntreLinhas = 1;

	/**
	 * Pontos clicados pelo usuário no editor (um por clique esquerdo,
	 * finalizado com o direito), na ordem em que formam o encadeamento —
	 * mesma ideia de {@link ObjetoLivre#getPontos()}, mas sem curva: os
	 * segmentos entre pontos consecutivos são sempre retos.
	 */
	private List<Point> pontos = new ArrayList<Point>();
	private GeneralPath caminho = new GeneralPath();
	private OrientacaoGuardRails orientacao = OrientacaoGuardRails.VERTICAL;

	public ObjetoGuardRails() {
		setLargura(2);
		setCorPimaria(new Color(220, 220, 220));
		setCorSecundaria(new Color(220, 220, 220));
		setTransparencia(255);
	}

	public List<Point> getPontos() {
		return pontos;
	}

	public void setPontos(List<Point> pontos) {
		this.pontos = pontos;
	}

	public OrientacaoGuardRails getOrientacao() {
		return orientacao;
	}

	public void setOrientacao(OrientacaoGuardRails orientacao) {
		this.orientacao = orientacao != null ? orientacao : OrientacaoGuardRails.VERTICAL;
	}

	public int getLarguraLinha() {
		return larguraLinha;
	}

	public void setLarguraLinha(int larguraLinha) {
		this.larguraLinha = Math.max(1, larguraLinha);
	}

	public int getVaoEntreLinhas() {
		return vaoEntreLinhas;
	}

	public void setVaoEntreLinhas(int vaoEntreLinhas) {
		this.vaoEntreLinhas = Math.max(0, vaoEntreLinhas);
	}

	/**
	 * Move o ponto de índice {@code indice} para {@code novaPosicao} e
	 * reconstrói {@link #caminho}, usado pelo editor ao arrastar um ponto já
	 * existente em modo de edição de pontos.
	 */
	public void moverPonto(int indice, Point novaPosicao) {
		pontos.set(indice, novaPosicao);
		gerar();
	}

	/**
	 * Insere {@code posicao} no encadeamento logo após o índice
	 * {@code indiceSegmento} (ou seja, entre os pontos {@code indiceSegmento}
	 * e {@code indiceSegmento + 1}), usado pelo editor para adicionar um
	 * ponto no meio de um segmento existente.
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
	 * {@link ObjetoLivre#gerar()}), e automaticamente por {@link #desenha}
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

		Shape formaExterna = new BasicStroke(Math.max(1, largura), BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND)
				.createStrokedShape(caminho);
		g2d.setColor(new Color(getCorPimaria().getRed(), getCorPimaria()
				.getGreen(), getCorPimaria().getBlue(), getTransparencia()));
		g2d.fill(escala.createTransformedShape(formaExterna));

		// VERTICAL: barras cruzando o percurso, periódicas ao longo dele (uma
		// a cada trecho de largura+vão), cada uma atravessando toda a
		// largura da barreira. HORIZONTAL: o oposto — linhas periódicas ao
		// longo da LARGURA da barreira (uma a cada trecho de largura+vão
		// através dela), cada uma correndo pelo percurso inteiro. Em ambos
		// os casos o vão é recalculado pra a quantidade de linhas encostar
		// exatamente nas duas pontas do intervalo (percurso ou largura),
		// nunca sobrando vão só numa delas — diferente da arquibancada (que
		// usa período fixo e pode deixar sobra).
		GeneralPath linhas = orientacao == OrientacaoGuardRails.HORIZONTAL
				? construirLinhasHorizontais(vertices)
				: construirLinhasVerticais(vertices);
		g2d.setColor(new Color(getCorSecundaria().getRed(), getCorSecundaria()
				.getGreen(), getCorSecundaria().getBlue(), getTransparencia()));
		g2d.fill(escala.createTransformedShape(linhas));
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
	 * Calcula, para um intervalo de comprimento {@code comprimentoTotal},
	 * quantas marcas de espessura {@link #larguraLinha} cabem nele com vão
	 * de {@link #vaoEntreLinhas} entre elas, e o vão real recalculado pra
	 * essa quantidade encostar exatamente nas duas pontas do intervalo (sem
	 * sobrar vão só numa delas). Usado tanto para distribuir as barras
	 * verticais ao longo do percurso quanto para distribuir as linhas
	 * horizontais através da largura da barreira.
	 */
	private double periodoReal(double comprimentoTotal, int quantidadeLinhas) {
		double vaoReal = quantidadeLinhas > 1
				? (comprimentoTotal - quantidadeLinhas * larguraLinha) / (quantidadeLinhas - 1)
				: 0;
		return larguraLinha + vaoReal;
	}

	private int quantidadeLinhasQueCabem(double comprimentoTotal) {
		int periodoAlvo = larguraLinha + vaoEntreLinhas;
		return Math.max(1, (int) (comprimentoTotal / periodoAlvo));
	}

	/**
	 * VERTICAL: barras cruzando o percurso, uma a cada trecho de
	 * {@link #larguraLinha}+{@link #vaoEntreLinhas} ao longo de TODO o
	 * percurso (soma de todos os segmentos), não de cada segmento
	 * isoladamente — cada barra atravessa toda a {@link #largura} da
	 * barreira, centrada no ponto do percurso.
	 */
	private GeneralPath construirLinhasVerticais(List<Point2D> vertices) {
		int quantidadeSegmentos = vertices.size() - 1;
		double[] comprimentoSegmento = new double[quantidadeSegmentos];
		double comprimentoTotal = 0;
		for (int i = 0; i < quantidadeSegmentos; i++) {
			comprimentoSegmento[i] = vertices.get(i).distance(vertices.get(i + 1));
			comprimentoTotal += comprimentoSegmento[i];
		}

		int quantidadeLinhas = quantidadeLinhasQueCabem(comprimentoTotal);
		double periodoReal = periodoReal(comprimentoTotal, quantidadeLinhas);

		GeneralPath linhas = new GeneralPath();
		int segmentoAtual = 0;
		double inicioSegmentoAtual = 0;
		for (int i = 0; i < quantidadeLinhas; i++) {
			double distanciaAlvo = i * periodoReal;
			while (segmentoAtual < quantidadeSegmentos - 1
					&& distanciaAlvo > inicioSegmentoAtual + comprimentoSegmento[segmentoAtual]) {
				inicioSegmentoAtual += comprimentoSegmento[segmentoAtual];
				segmentoAtual++;
			}
			Point2D p1 = vertices.get(segmentoAtual);
			Point2D p2 = vertices.get(segmentoAtual + 1);
			double compSeg = comprimentoSegmento[segmentoAtual];
			double t = compSeg == 0 ? 0 : (distanciaAlvo - inicioSegmentoAtual) / compSeg;
			double cx = p1.getX() + (p2.getX() - p1.getX()) * t;
			double cy = p1.getY() + (p2.getY() - p1.getY()) * t;
			double dx = compSeg == 0 ? 1 : (p2.getX() - p1.getX()) / compSeg;
			double dy = compSeg == 0 ? 0 : (p2.getY() - p1.getY()) / compSeg;
			double nx = -dy;
			double ny = dx;
			adicionaRetangulo(linhas, cx, cy, nx, ny, largura / 2.0, dx, dy, larguraLinha / 2.0);
		}
		return linhas;
	}

	/**
	 * HORIZONTAL: linhas correndo ao longo de TODO o percurso, uma a cada
	 * trecho de {@link #larguraLinha}+{@link #vaoEntreLinhas} através da
	 * {@link #largura} da barreira (perpendicular ao percurso) — o oposto
	 * de {@link #construirLinhasVerticais}: periódico na largura, contínuo
	 * no comprimento, em vez de periódico no comprimento e contínuo na
	 * largura. Cada linha é o traço de um caminho paralelo ao encadeamento
	 * original (ver {@link #construirCaminhoDeslocado}), não retângulos
	 * independentes por segmento — do jeito antigo, a normal de cada
	 * segmento diverge da do vizinho nos vértices onde o traçado muda de
	 * ângulo, e os retângulos adjacentes quebravam/se sobrepunham ali (o
	 * mesmo problema já corrigido em {@link ObjetoArquibancada#construirListras}).
	 */
	private GeneralPath construirLinhasHorizontais(List<Point2D> vertices) {
		int quantidadeLinhas = quantidadeLinhasQueCabem(largura);
		double periodoReal = periodoReal(largura, quantidadeLinhas);

		BasicStroke strokeLinha = new BasicStroke(larguraLinha, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER);
		GeneralPath linhas = new GeneralPath();
		for (int i = 0; i < quantidadeLinhas; i++) {
			double offsetCentro = -largura / 2.0 + i * periodoReal + larguraLinha / 2.0;
			GeneralPath caminhoDeslocado = construirCaminhoDeslocado(vertices, offsetCentro);
			linhas.append(strokeLinha.createStrokedShape(caminhoDeslocado), false);
		}
		return linhas;
	}

	/**
	 * Caminho paralelo a {@code vertices}, deslocado {@code offset} pixels
	 * perpendicularmente à direção do percurso. Nos vértices internos, usa a
	 * bissetriz (soma normalizada) das normais dos dois segmentos vizinhos em
	 * vez da normal de um único segmento — mantém o deslocamento contínuo
	 * através do vértice, sem o qual o caminho "quebraria" ali. Mesma lógica
	 * de {@link ObjetoArquibancada#construirCaminhoDeslocado}.
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

	/**
	 * Acrescenta em {@code destino} um retângulo centrado em {@code cx,cy},
	 * com meia-extensão {@code meiaLonga} no eixo {@code eixoLongoX,eixoLongoY}
	 * e meia-extensão {@code meiaCurta} no eixo {@code eixoCurtoX,eixoCurtoY}
	 * (ambos vetores unitários). Construído por vetores em vez de
	 * {@code AffineTransform.rotate} porque cada marca pode ter uma direção
	 * diferente (segmentos do encadeamento nem sempre têm o mesmo ângulo).
	 */
	private static void adicionaRetangulo(GeneralPath destino, double cx, double cy,
			double eixoLongoX, double eixoLongoY, double meiaLonga,
			double eixoCurtoX, double eixoCurtoY, double meiaCurta) {
		destino.moveTo(cx + eixoLongoX * meiaLonga + eixoCurtoX * meiaCurta,
				cy + eixoLongoY * meiaLonga + eixoCurtoY * meiaCurta);
		destino.lineTo(cx - eixoLongoX * meiaLonga + eixoCurtoX * meiaCurta,
				cy - eixoLongoY * meiaLonga + eixoCurtoY * meiaCurta);
		destino.lineTo(cx - eixoLongoX * meiaLonga - eixoCurtoX * meiaCurta,
				cy - eixoLongoY * meiaLonga - eixoCurtoY * meiaCurta);
		destino.lineTo(cx + eixoLongoX * meiaLonga - eixoCurtoX * meiaCurta,
				cy + eixoLongoY * meiaLonga - eixoCurtoY * meiaCurta);
		destino.closePath();
	}

	@Override
	public Rectangle obterArea() {
		return caminho.getBounds();
	}
}
