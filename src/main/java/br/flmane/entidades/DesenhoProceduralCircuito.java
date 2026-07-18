package br.flmane.entidades;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import br.nnpe.GeoUtil;
import br.nnpe.Util;

/**
 * Desenho procedural do circuito (traçado da pista/zebra/box e objetos de
 * cenário), extraído do editor de circuitos (br.flmane.editor.MainPanelEditor,
 * modo "sem imagem de fundo") para ser reutilizável fora do editor — em
 * particular, para gerar em memória a imagem de fundo usada em corrida, no
 * lugar do arquivo estático circuitos/*_mro.jpg.
 */
public final class DesenhoProceduralCircuito {

	/** Cor padrão do asfalto, usada quando {@code Circuito.corAsfalto} não foi definida. */
	public static final Color COR_PISTA = new Color(192, 192, 192);
	private static final int MARGEM_IMAGEM = 500;
	/** Mesma cor de {@code PainelCircuito.transpMenus}, duplicada aqui pra não criar dependência de entidades para visao. */
	private static final Color TRANSP_MENUS = new Color(255, 255, 255, 160);

	private DesenhoProceduralCircuito() {
	}

	/**
	 * Desenha pista, zebra e box do circuito no {@code Graphics2D} informado,
	 * na escala {@code zoom} — sem os objetos de cenário. É o mesmo desenho
	 * usado pelo editor de circuitos quando está em modo "sem imagem de
	 * fundo" (não confundir com {@link #desenha}, que inclui os objetos e é
	 * usado para gerar a imagem de fundo da corrida).
	 */
	public static void desenhaPistaZebraEBox(Graphics2D g2d, Circuito circuito, double zoom) {
		int larguraPistaPixeis = Util.inteiro(Carro.LARGURA * 1.5
				* circuito.getMultiplicadorLarguraPista() * zoom);
		BasicStroke pista = new BasicStroke(larguraPistaPixeis, BasicStroke.CAP_ROUND,
				BasicStroke.JOIN_ROUND);
		BasicStroke pistaTinta = new BasicStroke(Util.inteiro(larguraPistaPixeis * 1.05),
				BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
		BasicStroke box = new BasicStroke(Util.inteiro(larguraPistaPixeis * .4),
				BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
		// Borda branca do box: margem de .05 (mesma proporção que zebra já
		// usa sobre pista, 1.05 vs 1.0) somada ao próprio stroke do box
		// (.4) — .5 chegava a produzir uma margem 2x mais larga que a da
		// pista principal (bug relatado), além de não bater com
		// PainelCircuito (usado pra gerar a imagem de fundo da corrida).
		BasicStroke boxBorda = new BasicStroke(Util.inteiro(larguraPistaPixeis * .45),
				BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
		BasicStroke zebra = new BasicStroke(Util.inteiro(larguraPistaPixeis * 1.05),
				BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10f, new float[]{10, 10}, 0);

		desenhaTintaPistaEZebra(g2d, circuito, zoom, pistaTinta, zebra);
		desenhaPista(g2d, circuito, zoom, pista);
		desenhaTintaPistaBox(g2d, circuito, zoom, pista, boxBorda);
		desenhaPistaBox(g2d, circuito, zoom, box);
	}

	/** Quantidade máxima de vagas de box desenhadas quando todas cabem no espaço disponível. */
	private static final int MAX_VAGAS_BOX = 12;
	/**
	 * Espaçamento entre os centros de vagas vizinhas, em larguras de carro —
	 * o mesmo valor fixo sempre usado quando há espaço (nunca é reduzido
	 * pra caber mais vagas; ver {@link #desenhaVagasBox}).
	 */
	private static final double MULTI_VAGAS_BOX = 1.5;
	/** Espaço mínimo, em pixels de tela, mantido entre vagas vizinhas além do espaçamento padrão. */
	private static final double ESPACO_MINIMO_ENTRE_VAGAS_PIXELS = 2;
	/**
	 * Escala visual da "retC1" (a maior das três caixas desenhadas por
	 * vaga, a garagem) — reduzida em 30% (fica com 70% do tamanho
	 * original). É só um ajuste de desenho: não muda o espaçamento nem a
	 * quantidade de vagas calculados em {@link #desenhaVagasBox}.
	 */
	private static final double ESCALA_CAIXA_MAIOR_BOX = 0.7;

	/**
	 * Desenha os quadrados semitransparentes que marcam as vagas de box
	 * (paradas de pit stop), extraído de
	 * br.flmane.editor.MainPanelEditor.desenhaBoxes pra ficar disponível
	 * também na imagem gerada em memória para a corrida (ver {@link #desenha}
	 * e {@code CarregadorRecursos.carregaBackGroundJogo}) — antes essas
	 * marcações só apareciam no editor, nunca no jogo quando
	 * {@code Global.MODO_HOMENAGEM} está ativa.
	 * <p>
	 * O espaçamento entre vagas ({@link #MULTI_VAGAS_BOX}, mais uma folga
	 * mínima de {@link #ESPACO_MINIMO_ENTRE_VAGAS_PIXELS}px) nunca é
	 * reduzido — em vez de espremer até {@link #MAX_VAGAS_BOX} vagas no
	 * espaço entre o início e o fim da zona de parada de box (o que as
	 * fazia se sobrepor quando esse espaço era curto), desenha só a
	 * quantidade de vagas que cabe nesse espaço com o espaçamento normal.
	 * Se todas couberem, desenha todas.
	 *
	 * @param modoEditor quando {@code true} (chamado a partir do editor),
	 *                   também desenha as bolinhas indicadoras de qual lado
	 *                   (1 ou 2) o box está configurado — informação útil só
	 *                   para quem está editando o traçado, nunca deve ir para
	 *                   a imagem final da corrida ({@code false}). Convenção
	 *                   a seguir para qualquer futuro elemento visual que
	 *                   deva existir só no preview do editor, não na corrida.
	 */
	public static void desenhaVagasBox(Graphics2D g2d, Circuito circuito, double zoom, boolean modoEditor) {
		if (circuito.getBoxFull() == null || circuito.getBoxFull().isEmpty() || circuito.getParadaBoxIndex() == 0) {
			return;
		}
		// Espaçamento fixo (em larguras de carro) entre os centros de vagas
		// vizinhas, já somando a folga mínima em pixels de tela (convertida
		// pra largura de carro, na escala atual de zoom). Não muda com a
		// quantidade de vagas que cabe no espaço disponível.
		double multi = MULTI_VAGAS_BOX + (ESPACO_MINIMO_ENTRE_VAGAS_PIXELS / zoom) / Carro.LARGURA;

		// corBox1/corBox2 do circuito substituem o ciano/magenta padrão quando
		// definidas, mantendo a mesma transparência (150) das cores fixas.
		Color corRetC1 = circuito.getCorBox1() != null
				? new Color(circuito.getCorBox1().getRed(), circuito.getCorBox1().getGreen(),
						circuito.getCorBox1().getBlue(), 150)
				: new Color(0, 255, 255, 150);
		Color corRect = circuito.getCorBox2() != null
				? new Color(circuito.getCorBox2().getRed(), circuito.getCorBox2().getGreen(),
						circuito.getCorBox2().getBlue(), 150)
				: new Color(255, 0, 255, 150);

		int quantidadeVagas = MAX_VAGAS_BOX;
		if (circuito.getFimParadaBoxIndex() != 0) {
			No ini = circuito.getBoxFull().get(circuito.getParadaBoxIndex());
			No fim = circuito.getBoxFull().get(circuito.getFimParadaBoxIndex());
			int distaciaInicioFim = GeoUtil.distaciaEntrePontos(ini.getPoint(), fim.getPoint());

			// Reduz a quantidade de vagas desenhadas (nunca o espaçamento)
			// até que todas as vagas candidatas caibam no espaço
			// disponível. Se as MAX_VAGAS_BOX vagas já cabem, quantidadeVagas
			// continua no máximo.
			quantidadeVagas = 1;
			for (int candidata = MAX_VAGAS_BOX; candidata >= 1; candidata--) {
				double espacoNecessario = Carro.LARGURA * multi * (candidata - 1) + Carro.LARGURA;
				if (espacoNecessario <= distaciaInicioFim) {
					quantidadeVagas = candidata;
					break;
				}
			}
		}

		int paradas = circuito.getParadaBoxIndex();
		for (int i = 0; i < quantidadeVagas; i++) {
			int iP = paradas + Util.inteiro(Carro.LARGURA * multi * i) + Carro.LARGURA;
			int n1Idx = iP - Carro.MEIA_LARGURA;
			int n2Idx = iP + Carro.MEIA_LARGURA;
			if (n1Idx >= circuito.getBoxFull().size()) {
				continue;
			}
			if (iP >= circuito.getBoxFull().size()) {
				continue;
			}
			if (n2Idx >= circuito.getBoxFull().size()) {
				continue;
			}
			No n1 = circuito.getBoxFull().get(n1Idx);
			No nM = circuito.getBoxFull().get(iP);
			No n2 = circuito.getBoxFull().get(n2Idx);
			Point p1 = new Point(Util.inteiro(n1.getPoint().x * zoom), Util.inteiro(n1.getPoint().y * zoom));
			Point pm = new Point(Util.inteiro(nM.getPoint().x * zoom), Util.inteiro(nM.getPoint().y * zoom));
			Point p2 = new Point(Util.inteiro(n2.getPoint().x * zoom), Util.inteiro(n2.getPoint().y * zoom));
			double calculaAngulo = GeoUtil.calculaAngulo(p1, p2, 0);
			Rectangle2D rectangle = new Rectangle2D.Double((pm.x - (Carro.MEIA_LARGURA)), (pm.y - (Carro.MEIA_ALTURA)),
					(Carro.LARGURA), (Carro.ALTURA));

			Point cima = GeoUtil.calculaPonto(calculaAngulo,
					Util.inteiro(Carro.ALTURA * circuito.getMultiplicadorLarguraPista() * zoom),
					new Point(Util.inteiro(rectangle.getCenterX()), Util.inteiro(rectangle.getCenterY())));
			Point baixo = GeoUtil.calculaPonto(calculaAngulo + 180,
					Util.inteiro(Carro.ALTURA * circuito.getMultiplicadorLarguraPista() * zoom),
					new Point(Util.inteiro(rectangle.getCenterX()), Util.inteiro(rectangle.getCenterY())));
			Point cimaBoxC1 = GeoUtil.calculaPonto(calculaAngulo, Util.inteiro((Carro.ALTURA) * 3.5 * zoom),
					new Point(Util.inteiro(rectangle.getCenterX()), Util.inteiro(rectangle.getCenterY())));
			Point baixoBoxC1 = GeoUtil.calculaPonto(calculaAngulo + 180, Util.inteiro((Carro.ALTURA) * 3.2 * zoom),
					new Point(Util.inteiro(rectangle.getCenterX()), Util.inteiro(rectangle.getCenterY())));
			Point cimaBoxC2 = GeoUtil.calculaPonto(calculaAngulo, Util.inteiro((Carro.ALTURA) * 3.5 * zoom),
					new Point(Util.inteiro(rectangle.getCenterX()), Util.inteiro(rectangle.getCenterY())));
			Point baixoBoxC2 = GeoUtil.calculaPonto(calculaAngulo + 180, Util.inteiro((Carro.ALTURA) * 3.2 * zoom),
					new Point(Util.inteiro(rectangle.getCenterX()), Util.inteiro(rectangle.getCenterY())));

			RoundRectangle2D retC1;
			RoundRectangle2D retC2;
			if (circuito.getLadoBox() == 1) {
				rectangle = new Rectangle2D.Double((cima.x - (Carro.MEIA_LARGURA * zoom)),
						(cima.y - (Carro.MEIA_ALTURA * zoom)), (Carro.LARGURA * zoom), (Carro.ALTURA * zoom));
				retC1 = new RoundRectangle2D.Double((cimaBoxC1.x - (Carro.LARGURA * zoom * ESCALA_CAIXA_MAIOR_BOX)),
						(cimaBoxC1.y - (Carro.ALTURA * zoom * ESCALA_CAIXA_MAIOR_BOX)),
						(Carro.LARGURA * 2 * zoom * ESCALA_CAIXA_MAIOR_BOX),
						(Carro.ALTURA * 3 * zoom * ESCALA_CAIXA_MAIOR_BOX), 5, 5);
				retC2 = new RoundRectangle2D.Double((cimaBoxC2.x - (Carro.MEIA_LARGURA * zoom)),
						(cimaBoxC2.y + (Carro.MEIA_ALTURA * zoom)), (Carro.LARGURA * zoom), (Carro.ALTURA * zoom), 5,
						5);
			} else {
				rectangle = new Rectangle2D.Double((baixo.x - (Carro.MEIA_LARGURA * zoom)),
						(baixo.y - (Carro.MEIA_ALTURA * zoom)), (Carro.LARGURA * zoom), (Carro.ALTURA * zoom));
				retC1 = new RoundRectangle2D.Double((baixoBoxC1.x - (Carro.LARGURA * zoom * ESCALA_CAIXA_MAIOR_BOX)),
						(baixoBoxC1.y - (Carro.ALTURA * zoom * ESCALA_CAIXA_MAIOR_BOX)),
						(Carro.LARGURA * 2 * zoom * ESCALA_CAIXA_MAIOR_BOX),
						(Carro.ALTURA * 3 * zoom * ESCALA_CAIXA_MAIOR_BOX), 5, 5);
				retC2 = new RoundRectangle2D.Double((baixoBoxC2.x - (Carro.MEIA_LARGURA * zoom)),
						(baixoBoxC2.y + (Carro.MEIA_ALTURA * zoom)), (Carro.LARGURA * zoom), (Carro.ALTURA * zoom), 5,
						5);
			}

			GeneralPath generalPath = new GeneralPath(rectangle);
			AffineTransform affineTransformRect = AffineTransform.getScaleInstance(zoom, zoom);
			double rad = Math.toRadians(calculaAngulo);
			affineTransformRect.setToRotation(rad, rectangle.getCenterX(), rectangle.getCenterY());
			g2d.setColor(corRect);
			g2d.fill(generalPath.createTransformedShape(affineTransformRect));
			generalPath = new GeneralPath(retC1);
			affineTransformRect.setToRotation(rad, retC1.getCenterX(), retC1.getCenterY());
			g2d.setColor(corRetC1);
			g2d.fill(generalPath.createTransformedShape(affineTransformRect));

			generalPath = new GeneralPath(retC2);
			affineTransformRect.setToRotation(rad, retC2.getCenterX(), retC2.getCenterY());
			g2d.setColor(corRect);
			g2d.fill(generalPath.createTransformedShape(affineTransformRect));

			if (modoEditor) {
				// Bolinhas indicando qual lado (1 ou 2) o box está configurado —
				// só fazem sentido para quem está editando o traçado.
				if (circuito.getLadoBox() == 1)
					g2d.setColor(Color.BLUE);
				else
					g2d.setColor(TRANSP_MENUS);
				g2d.fillOval((int) cimaBoxC1.x, (int) cimaBoxC1.y, 10, 10);

				if (circuito.getLadoBox() == 2)
					g2d.setColor(Color.BLUE);
				else
					g2d.setColor(TRANSP_MENUS);
				g2d.fillOval((int) baixoBoxC1.x, (int) baixoBoxC1.y, 10, 10);
			}
		}
	}

	/**
	 * Desenha pista, zebra, box, vagas de box e objetos de cenário
	 * (Circuito.objetosCenario) do circuito no {@code Graphics2D} informado,
	 * na escala {@code zoom}. {@code Circuito.objetos} (Escapada/Transparencia)
	 * não faz parte deste desenho — esses dois já têm tratamento próprio em
	 * tempo real (debug e máscara de boxes, respectivamente) e não devem
	 * ficar gravados de forma permanente na imagem.
	 * <p>
	 * Só é usado para gerar a imagem final de corrida — nunca é chamado pelo
	 * preview do editor —, então elementos "só de editor" (ver parâmetro
	 * {@code modoEditor} de {@link #desenhaVagasBox}) são sempre omitidos.
	 */
	public static void desenha(Graphics2D g2d, Circuito circuito, double zoom) {
		List<Integer> niveis = niveisDesenhoOrdenados(circuito);
		for (Integer nivel : niveis) {
			if (nivel < 0) {
				desenhaObjetos(g2d, circuito, zoom, nivel);
			}
		}
		desenhaPistaZebraEBox(g2d, circuito, zoom);
		desenhaLinhaDeLargada(g2d, circuito, zoom);
		desenhaVagasBox(g2d, circuito, zoom, false);
		desenhaObjetos(g2d, circuito, zoom, 0);
		for (Integer nivel : niveis) {
			if (nivel > 0) {
				desenhaObjetos(g2d, circuito, zoom, nivel);
			}
		}
	}

	/** Quantidade de quadrados do xadrez cobrindo a largura da pista na linha de largada. */
	private static final int LARGADA_COLUNAS = 6;
	/**
	 * Fator aplicado ao tamanho do quadrado pra obter a espessura da faixa ao
	 * longo da direção da pista — reduzido pela metade sucessivas vezes a
	 * pedido, sem afetar a largura que cobre a pista (perpendicular).
	 */
	private static final double LARGADA_ESPESSURA_FATOR = 0.25;
	/**
	 * Alongamento da célula no sentido perpendicular à pista (across-track,
	 * o "sentido da pista" pedido) em relação ao lado ao longo da pista —
	 * 1.0 seria um quadrado perfeito.
	 */
	private static final double LARGADA_ALONGAMENTO = 2.0;

	/**
	 * Desenha a linha de largada em padrão quadriculado (preto/branco),
	 * perpendicular à direção local da pista no nó {@code No.LARGADA},
	 * cobrindo a largura da pista naquele ponto. Não desenha nada (sem
	 * lançar exceção) se o circuito não tiver um nó de largada em
	 * {@code pistaKey} ou não houver nó vizinho em {@code pistaFull} pra
	 * estimar a direção. Sem offset de centralização — usado pelo editor de
	 * circuitos (sem imagem de fundo) e pela geração de imagem em memória
	 * (zoom 1, sem viewport). Ver {@link #desenhaLinhaDeLargada(Graphics2D, Circuito, double, Point)}
	 * para o caso com viewport (usado por {@code PainelCircuito}).
	 */
	public static void desenhaLinhaDeLargada(Graphics2D g2d, Circuito circuito, double zoom) {
		desenhaLinhaDeLargada(g2d, circuito, zoom, new Point(0, 0));
	}

	/**
	 * Mesmo desenho de {@link #desenhaLinhaDeLargada(Graphics2D, Circuito, double)},
	 * mas descontando {@code descontoCentraliza} das coordenadas do circuito
	 * antes de aplicar o zoom — mesma convenção usada por
	 * {@code PainelCircuito} pra desenhar em cima do viewport com scroll/zoom.
	 */
	public static void desenhaLinhaDeLargada(Graphics2D g2d, Circuito circuito, double zoom,
			Point descontoCentraliza) {
		No noLargada = localizaNoLargada(circuito.getPistaKey());
		if (noLargada == null) {
			return;
		}
		No noVizinho = localizaVizinhoNaPistaFull(circuito.getPistaFull(), noLargada);
		if (noVizinho == null) {
			return;
		}

		double anguloPista = GeoUtil.calculaAngulo(noLargada.getPoint(), noVizinho.getPoint(), 0);
		int larguraPistaPixeis = Util.inteiro(Carro.LARGURA * 1.5
				* circuito.getMultiplicadorLarguraPista() * zoom);
		// Lado base já reduzido pela espessura pedida — é a dimensão ao longo
		// da pista (fixa); o passo entre colunas (perpendicular) parte desse
		// mesmo lado, alongado por LARGADA_ALONGAMENTO.
		double ladoBase = (larguraPistaPixeis / (double) LARGADA_COLUNAS) * LARGADA_ESPESSURA_FATOR;
		double ladoAoLongo = ladoBase;
		double ladoPerpendicular = ladoBase * LARGADA_ALONGAMENTO;
		int numColunas = Math.max(1, Util.inteiro(larguraPistaPixeis / ladoPerpendicular));
		double rad = Math.toRadians(anguloPista);
		Point centro = new Point(
				Util.inteiro((noLargada.getX() - descontoCentraliza.x) * zoom),
				Util.inteiro((noLargada.getY() - descontoCentraliza.y) * zoom));

		for (int coluna = 0; coluna < numColunas; coluna++) {
			double deslocamentoPerpendicular = (coluna - (numColunas - 1) / 2.0) * ladoPerpendicular;
			Point centroColuna = GeoUtil.calculaPonto(anguloPista, Util.inteiro(deslocamentoPerpendicular), centro);
			for (int linha = 0; linha < 2; linha++) {
				double deslocamentoAoLongo = (linha - 0.5) * ladoAoLongo;
				Point centroQuadrado = GeoUtil.calculaPonto(anguloPista + 90, Util.inteiro(deslocamentoAoLongo),
						centroColuna);
				g2d.setColor((coluna + linha) % 2 == 0 ? Color.BLACK : Color.WHITE);
				Rectangle2D quadrado = new Rectangle2D.Double(
						centroQuadrado.x - ladoAoLongo / 2.0, centroQuadrado.y - ladoPerpendicular / 2.0,
						ladoAoLongo, ladoPerpendicular);
				GeneralPath path = new GeneralPath(quadrado);
				AffineTransform transform = AffineTransform.getRotateInstance(rad, centroQuadrado.x, centroQuadrado.y);
				g2d.fill(path.createTransformedShape(transform));
			}
		}
	}

	private static No localizaNoLargada(List<No> pistaKey) {
		if (pistaKey == null) {
			return null;
		}
		for (No no : pistaKey) {
			if (No.LARGADA.equals(no.getTipo())) {
				return no;
			}
		}
		return null;
	}

	/**
	 * O nó de largada em {@code pistaKey} é o ponto inicial de um segmento
	 * inteiro de {@code pistaFull} com o mesmo tipo (ver
	 * {@code Circuito.converterPointNo}), então localizar por tipo em
	 * {@code pistaFull} não seria único — em vez disso, localiza o nó de
	 * {@code pistaFull} na mesma posição do nó de largada (o primeiro ponto
	 * do Bresenham de cada segmento coincide com o nó-chave de origem) e
	 * retorna o próximo, pra estimar a direção local da pista.
	 */
	private static No localizaVizinhoNaPistaFull(List<No> pistaFull, No noLargada) {
		if (pistaFull == null || pistaFull.isEmpty()) {
			return null;
		}
		for (int i = 0; i < pistaFull.size(); i++) {
			if (pistaFull.get(i).getPoint().equals(noLargada.getPoint())) {
				return pistaFull.get((i + 1) % pistaFull.size());
			}
		}
		return null;
	}

	/**
	 * Níveis de desenho distintos em uso pelos objetos de cenário do
	 * circuito, em ordem crescente — sem limite de faixa, então percorre
	 * exatamente os níveis presentes, não um intervalo fixo.
	 */
	private static List<Integer> niveisDesenhoOrdenados(Circuito circuito) {
		java.util.TreeSet<Integer> niveis = new java.util.TreeSet<Integer>();
		if (circuito.getObjetosCenario() != null) {
			for (ObjetoPista objetoPista : circuito.getObjetosCenario()) {
				niveis.add(objetoPista.getNivelDesenho());
			}
		}
		return new ArrayList<Integer>(niveis);
	}

	/**
	 * Gera, em memória, a imagem de fundo do circuito na escala real
	 * (zoom = 1), com dimensões calculadas a partir dos limites dos nós de
	 * pista e box. Requer que {@code circuito.vetorizarPista(...)} já tenha
	 * sido chamado (pistaKey/boxKey populados).
	 */
	public static BufferedImage geraImagem(Circuito circuito) {
		int maxX = 0;
		int maxY = 0;
		for (No no : circuito.getPistaKey()) {
			maxX = Math.max(maxX, no.getX());
			maxY = Math.max(maxY, no.getY());
		}
		for (No no : circuito.getBoxKey()) {
			maxX = Math.max(maxX, no.getX());
			maxY = Math.max(maxY, no.getY());
		}
		BufferedImage image = new BufferedImage(maxX + MARGEM_IMAGEM, maxY + MARGEM_IMAGEM,
				BufferedImage.TYPE_INT_RGB);
		Graphics2D g2d = image.createGraphics();
		Util.setarHints(g2d);
		g2d.setColor(circuito.getCorFundo() != null ? circuito.getCorFundo() : Color.WHITE);
		g2d.fillRect(0, 0, image.getWidth(), image.getHeight());
		desenha(g2d, circuito, 1.0);
		g2d.dispose();
		return image;
	}

	private static void desenhaPista(Graphics2D g2d, Circuito circuito, double zoom,
			BasicStroke stroke) {
		if (circuito.getPistaKey() == null || circuito.getPistaKey().isEmpty()) {
			return;
		}
		g2d.setColor(circuito.getCorAsfalto() != null ? circuito.getCorAsfalto() : COR_PISTA);
		g2d.setStroke(stroke);
		No oldNo = null;
		for (Iterator<No> iter = circuito.getPistaKey().iterator(); iter.hasNext(); ) {
			No no = iter.next();
			if (oldNo != null) {
				g2d.drawLine(Util.inteiro(oldNo.getX() * zoom), Util.inteiro(oldNo.getY() * zoom),
						Util.inteiro(no.getX() * zoom), Util.inteiro(no.getY() * zoom));
			}
			oldNo = no;
		}
		No noFinal = circuito.getPistaKey().get(0);
		g2d.drawLine(Util.inteiro(oldNo.getX() * zoom), Util.inteiro(oldNo.getY() * zoom),
				Util.inteiro(noFinal.getX() * zoom), Util.inteiro(noFinal.getY() * zoom));
	}

	/**
	 * @param fechado true fecha o caminho de volta ao primeiro nó (caso da
	 *                pista, que é um loop); false deixa o caminho aberto, do
	 *                primeiro ao último nó só (caso do box, que é um
	 *                trajeto de entrada até a saída, não um loop).
	 */
	private static GeneralPath construirCaminho(List<No> nos, double zoom, boolean fechado) {
		GeneralPath caminho = new GeneralPath();
		No oldNo = null;
		for (No no : nos) {
			int x = Util.inteiro(no.getX() * zoom);
			int y = Util.inteiro(no.getY() * zoom);
			if (oldNo == null) {
				caminho.moveTo(x, y);
			} else {
				caminho.lineTo(x, y);
			}
			oldNo = no;
		}
		if (fechado) {
			No noInicial = nos.get(0);
			caminho.lineTo(Util.inteiro(noInicial.getX() * zoom), Util.inteiro(noInicial.getY() * zoom));
		}
		return caminho;
	}

	/**
	 * Borda branca ao longo do traçado do box, no mesmo espírito de
	 * desenhaTintaPistaEZebra() pintar a borda da pista — mas, diferente
	 * dela, suprimida (via Area.subtract) nos trechos em que a borda do box
	 * cairia dentro da área já pintada pelo traçado da pista, pra não
	 * desenhar branco por cima do asfalto/zebra da pista.
	 */
	private static void desenhaTintaPistaBox(Graphics2D g2d, Circuito circuito, double zoom,
			BasicStroke pista, BasicStroke boxBorda) {
		if (circuito.getPistaKey() == null || circuito.getPistaKey().isEmpty()
				|| circuito.getBoxKey() == null || circuito.getBoxKey().isEmpty()) {
			return;
		}
		Shape formaPista = pista.createStrokedShape(construirCaminho(circuito.getPistaKey(), zoom, true));
		Shape formaBordaBox = boxBorda.createStrokedShape(construirCaminho(circuito.getBoxKey(), zoom, false));

		Area areaBorda = new Area(formaBordaBox);
		areaBorda.subtract(new Area(formaPista));

		g2d.setColor(Color.WHITE);
		g2d.fill(areaBorda);
	}

	private static void desenhaPistaBox(Graphics2D g2d, Circuito circuito, double zoom,
			BasicStroke stroke) {
		g2d.setColor(circuito.getCorAsfalto() != null ? circuito.getCorAsfalto() : COR_PISTA);
		g2d.setStroke(stroke);
		No oldNo = null;
		for (Iterator<No> iter = circuito.getBoxKey().iterator(); iter.hasNext(); ) {
			No no = iter.next();
			if (oldNo != null) {
				g2d.drawLine(Util.inteiro(oldNo.getX() * zoom), Util.inteiro(oldNo.getY() * zoom),
						Util.inteiro(no.getX() * zoom), Util.inteiro(no.getY() * zoom));
			}
			oldNo = no;
		}
		if (circuito.getBoxKey() != null && !circuito.getBoxKey().isEmpty()) {
			No noFinal = circuito.getBoxKey().get(circuito.getBoxKey().size() - 1);
			g2d.drawLine(Util.inteiro(oldNo.getX() * zoom), Util.inteiro(oldNo.getY() * zoom),
					Util.inteiro(noFinal.getX() * zoom), Util.inteiro(noFinal.getY() * zoom));
		}
	}

	private static void desenhaTintaPistaEZebra(Graphics2D g2d, Circuito circuito, double zoom,
			BasicStroke pistaTinta, BasicStroke zebra) {
		if (circuito.getPistaKey() == null || circuito.getPistaKey().isEmpty()) {
			return;
		}
		// As cores customizadas valem só para a faixa de zebra das curvas —
		// corZebra1 como fundo sólido e corZebra2 como listras — cada uma com
		// seu próprio fallback (branco/vermelho). A tinta de borda no resto
		// da pista é sempre branca, independente das cores customizadas.
		Color corZebra1 = circuito.getCorZebra1() != null ? circuito.getCorZebra1() : Color.WHITE;
		Color corZebra2 = circuito.getCorZebra2() != null ? circuito.getCorZebra2() : Color.RED;
		No oldNo = null;
		for (Iterator<No> iter = circuito.getPistaKey().iterator(); iter.hasNext(); ) {
			No no = iter.next();
			if (oldNo != null) {
				boolean curva = No.CURVA_ALTA.equals(oldNo.getTipo()) || No.CURVA_BAIXA.equals(oldNo.getTipo());
				g2d.setColor(curva ? corZebra1 : Color.WHITE);
				g2d.setStroke(pistaTinta);
				g2d.drawLine(Util.inteiro(oldNo.getX() * zoom), Util.inteiro(oldNo.getY() * zoom),
						Util.inteiro(no.getX() * zoom), Util.inteiro(no.getY() * zoom));
				if (curva) {
					g2d.setColor(corZebra2);
					g2d.setStroke(zebra);
					g2d.drawLine(Util.inteiro(oldNo.getX() * zoom), Util.inteiro(oldNo.getY() * zoom),
							Util.inteiro(no.getX() * zoom), Util.inteiro(no.getY() * zoom));
				}
			}
			oldNo = no;
		}
		No noFinal = circuito.getPistaKey().get(0);
		g2d.setColor(Color.WHITE);
		g2d.setStroke(pistaTinta);
		g2d.drawLine(Util.inteiro(oldNo.getX() * zoom), Util.inteiro(oldNo.getY() * zoom),
				Util.inteiro(noFinal.getX() * zoom), Util.inteiro(noFinal.getY() * zoom));
	}

	private static void desenhaObjetos(Graphics2D g2d, Circuito circuito, double zoom, int nivel) {
		if (circuito.getObjetosCenario() == null) {
			return;
		}
		for (ObjetoPista objetoPista : circuito.getObjetosCenario()) {
			if (objetoPista.getNivelDesenho() != nivel) {
				continue;
			}
			objetoPista.desenha(g2d, zoom);
		}
	}
}
