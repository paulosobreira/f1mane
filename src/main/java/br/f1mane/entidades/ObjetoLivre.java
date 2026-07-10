package br.f1mane.entidades;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ObjetoLivre extends ObjetoDesenho {
	/** Tamanho (em unidades de mundo, antes do zoom) da célula da grade do padrão de preenchimento. */
	private static final int PASSO_PADRAO_LOCAL = Carro.ALTURA;
	/** Semente fixa: a dispersão da brita é "aleatória" mas sempre a mesma entre renderizações. */
	private static final long SEMENTE_BRITA = 20260703L;
	/** Semente fixa da dispersão (posição e tamanho) da vegetação, densa e simples. */
	private static final long SEMENTE_VEGETACAO = 20260710L;
	/** Vegetação densa: touceiras bem maiores (célula ~1.6x) e com tamanho bem variado entre si. */
	private static final double FATOR_PASSO_VEGETACAO_DENSA = 1.6;
	private static final double VARIACAO_TAMANHO_VEGETACAO_DENSA = 0.6;
	/** Vegetação simples: mantém o tamanho original da célula, com variação apenas leve entre as marcas. */
	private static final double FATOR_PASSO_VEGETACAO_SIMPLES = 1.0;
	private static final double VARIACAO_TAMANHO_VEGETACAO_SIMPLES = 0.15;

	/**
	 * Campo legado (polígono de linhas retas), mantido apenas para leitura de
	 * circuitos XML existentes. Novos objetos e edições passam a usar
	 * {@link #vertices}; ver {@link #obterVerticesEfetivos()}.
	 */
	private List<Point> pontos = new ArrayList<Point>();
	private List<PontoCurva> vertices = new ArrayList<PontoCurva>();
	private TipoObjetoLivre tipo = TipoObjetoLivre.POLIGONO_SIMPLES;
	GeneralPath generalPath = new GeneralPath();

	public ObjetoLivre() {
		setCorPimaria(new Color(120, 120, 120));
		setCorSecundaria(new Color(60, 60, 60));
		setTransparencia(255);
	}

	public TipoObjetoLivre getTipo() {
		return tipo;
	}

	public void setTipo(TipoObjetoLivre tipo) {
		this.tipo = tipo != null ? tipo : TipoObjetoLivre.POLIGONO_SIMPLES;
	}

	public List<Point> getPontos() {
		return pontos;
	}

	public void setPontos(List<Point> pontos) {
		this.pontos = pontos;
	}

	public List<PontoCurva> getVertices() {
		return vertices;
	}

	public void setVertices(List<PontoCurva> vertices) {
		this.vertices = vertices;
	}

	/**
	 * {@code vertices} se não estiver vazio; caso contrário, sintetiza uma
	 * lista equivalente a partir do {@code pontos} legado (haste nula em cada
	 * vértice, ou seja, segmentos retos) sem alterar o estado do objeto — a
	 * migração efetiva de {@code pontos} para {@code vertices} só acontece
	 * quando o objeto é editado (ver {@link #inicializarVerticesSeNecessario()}).
	 */
	private List<PontoCurva> obterVerticesEfetivos() {
		if (!vertices.isEmpty()) {
			return vertices;
		}
		List<PontoCurva> sintetizados = new ArrayList<PontoCurva>();
		for (Point ponto : pontos) {
			sintetizados.add(new PontoCurva(new Point(ponto)));
		}
		return sintetizados;
	}

	/**
	 * Copia {@code pontos} (legado) para {@code vertices} se este ainda
	 * estiver vazio, tornando {@code vertices} a fonte de verdade a partir
	 * daqui — chamado pelo editor ao iniciar a edição de pontos/hastes de um
	 * objeto legado.
	 */
	public void inicializarVerticesSeNecessario() {
		if (!vertices.isEmpty() || pontos.isEmpty()) {
			return;
		}
		for (Point ponto : pontos) {
			vertices.add(new PontoCurva(new Point(ponto)));
		}
	}

	public void gerar() {
		List<PontoCurva> pts = obterVerticesEfetivos();
		GeneralPath path = new GeneralPath();
		if (!pts.isEmpty()) {
			Point primeiro = pts.get(0).getPosicao();
			path.moveTo(primeiro.x, primeiro.y);
			for (int i = 0; i < pts.size(); i++) {
				PontoCurva atual = pts.get(i);
				PontoCurva proximo = pts.get((i + 1) % pts.size());
				Point controleSaida = atual.getControleSaida();
				Point controleEntrada = proximo.getControleEntrada();
				Point destino = proximo.getPosicao();
				path.curveTo(controleSaida.x, controleSaida.y, controleEntrada.x, controleEntrada.y,
						destino.x, destino.y);
			}
			path.closePath();
		}
		generalPath = path;
	}

	@Override
	public void desenha(Graphics2D g2d, double zoom) {
		if (generalPath.getCurrentPoint() == null) {
			// generalPath não é bean property, então XMLEncoder/XMLDecoder não
			// o persistem: ao recarregar um circuito o path chega vazio e
			// precisa ser regenerado a partir dos pontos/vértices salvos.
			gerar();
		}
		if (posicaoQuina != null) {
			Rectangle bounds = generalPath.getBounds();
			AffineTransform translacao = AffineTransform.getTranslateInstance(
					posicaoQuina.x - bounds.x, posicaoQuina.y - bounds.y);
			generalPath.transform(translacao);
		}

		// Centro (em espaço de mundo, antes do zoom) usado como pivô de
		// rotação do PADRÃO (ver desenhaComClipSemAntialiasing) — escalado
		// por zoom, é o centro da própria forma em espaço de tela. A
		// silhueta em si (formaZoomLocal) nunca é rotacionada: angulo afeta
		// só o conteúdo do padrão desenhado dentro dela, não o contorno do
		// objeto livre.
		Rectangle boundsMundo = generalPath.getBounds();
		double pivoX = boundsMundo.getCenterX() * zoom;
		double pivoY = boundsMundo.getCenterY() * zoom;

		// Forma em espaço de tela (zoom aplicado), sem rotação — é a
		// silhueta desenhada (fill de fundo) E a base do clip do padrão; o
		// padrão em si é que é rotacionado via transform do próprio
		// Graphics2D em desenhaComClipSemAntialiasing.
		Shape formaZoomLocal = AffineTransform.getScaleInstance(zoom, zoom).createTransformedShape(generalPath);

		g2d.setColor(new Color(getCorPimaria().getRed(), getCorPimaria()
				.getGreen(), getCorPimaria().getBlue(), getTransparencia()));
		g2d.fill(formaZoomLocal);

		if (tipo == TipoObjetoLivre.BRITA) {
			desenhaBrita(g2d, formaZoomLocal, pivoX, pivoY, zoom);
		} else if (tipo == TipoObjetoLivre.VEGETACAO_DENSA || tipo == TipoObjetoLivre.VEGETACAO_SIMPLES) {
			desenhaPadraoVegetacao(g2d, formaZoomLocal, pivoX, pivoY, zoom);
		} else if (tipo == TipoObjetoLivre.LISTRADO) {
			desenhaPadraoListrado(g2d, formaZoomLocal, pivoX, pivoY, zoom);
		} else if (tipo == TipoObjetoLivre.XADREZ) {
			desenhaPadraoXadrez(g2d, formaZoomLocal, pivoX, pivoY, zoom);
		} else if (tipo != TipoObjetoLivre.POLIGONO_SIMPLES) {
			desenhaPadraoEmGrade(g2d, formaZoomLocal, pivoX, pivoY, zoom);
		}
	}

	/**
	 * Retângulo (quadrado) usado como base dos laços de geração dos padrões:
	 * maior que o bounding box de {@code formaLocal}, com metade do lado
	 * igual ao raio do círculo circunscrito desse bounding box (a diagonal),
	 * centrado no mesmo centro. Qualquer ponto do bounding box original
	 * está a, no máximo, esse raio de distância do centro — então, mesmo
	 * depois de o padrão ser rotacionado por {@link #getAngulo()} em torno
	 * do mesmo centro (ver {@link #desenhaComClipSemAntialiasing}), a grade
	 * gerada continua cobrindo toda a área da forma original (o clip, fixo,
	 * é quem recorta o excesso), em vez de deixar cantos sem padrão quando
	 * o bounding box (não quadrado) gira e "encolhe" a cobertura em alguma
	 * direção.
	 */
	private static Rectangle areaCoberturaPadrao(Shape formaLocal) {
		Rectangle bounds = formaLocal.getBounds();
		double raio = Math.hypot(bounds.width, bounds.height) / 2.0;
		int lado = (int) Math.ceil(raio * 2);
		int cx = (int) Math.round(bounds.getCenterX());
		int cy = (int) Math.round(bounds.getCenterY());
		return new Rectangle(cx - lado / 2, cy - lado / 2, lado, lado);
	}

	/**
	 * Sobrepõe, restrito à área da forma, o padrão de ondas da água: grade
	 * alinhada de passo fixo (linhas alternadas com meio-passo de
	 * deslocamento), sem aleatoriedade — o único tipo que ainda usa a grade
	 * regular (vegetação passou a usar dispersão embaralhada, ver
	 * {@link #desenhaPadraoVegetacao}).
	 */
	private void desenhaPadraoEmGrade(Graphics2D g2d, Shape formaLocal, double pivoX, double pivoY, double zoom) {
		desenhaComClipSemAntialiasing(g2d, formaLocal, pivoX, pivoY, () -> {
			Rectangle bounds = areaCoberturaPadrao(formaLocal);
			int passo = Math.max(4, (int) Math.round(PASSO_PADRAO_LOCAL * zoom));
			g2d.setColor(new Color(getCorSecundaria().getRed(), getCorSecundaria().getGreen(),
					getCorSecundaria().getBlue(), getTransparencia()));
			int linha = 0;
			for (int y = bounds.y; y < bounds.y + bounds.height; y += passo) {
				int deslocamentoLinha = (linha % 2 == 0) ? 0 : passo / 2;
				for (int x = bounds.x - passo; x < bounds.x + bounds.width + passo; x += passo) {
					desenhaPrimitivaPadrao(g2d, x + deslocamentoLinha, y + passo / 2, Math.max(2, passo / 5));
				}
				linha++;
			}
		});
	}

	/**
	 * Vegetação (densa e simples): touceiras espalhadas em posições
	 * pseudo-aleatórias dentro de cada célula (não alinhadas em grade, mesmo
	 * espírito da {@link #desenhaBrita}) e com tamanho levemente sorteado a
	 * cada marca. A densa usa célula maior (touceiras maiores) e uma faixa de
	 * variação de tamanho bem mais ampla que a simples, que fica quase
	 * uniforme. Semente fixa: determinístico entre renderizações sucessivas.
	 */
	private void desenhaPadraoVegetacao(Graphics2D g2d, Shape formaLocal, double pivoX, double pivoY, double zoom) {
		desenhaComClipSemAntialiasing(g2d, formaLocal, pivoX, pivoY, () -> {
			Rectangle bounds = areaCoberturaPadrao(formaLocal);
			boolean densa = tipo == TipoObjetoLivre.VEGETACAO_DENSA;
			double fatorPasso = densa ? FATOR_PASSO_VEGETACAO_DENSA : FATOR_PASSO_VEGETACAO_SIMPLES;
			double variacaoTamanho = densa ? VARIACAO_TAMANHO_VEGETACAO_DENSA : VARIACAO_TAMANHO_VEGETACAO_SIMPLES;
			int passo = Math.max(4, (int) Math.round(PASSO_PADRAO_LOCAL * fatorPasso * zoom));
			g2d.setColor(new Color(getCorSecundaria().getRed(), getCorSecundaria().getGreen(),
					getCorSecundaria().getBlue(), getTransparencia()));
			Random random = new Random(SEMENTE_VEGETACAO);
			for (int y = bounds.y - passo; y < bounds.y + bounds.height + passo; y += passo) {
				for (int x = bounds.x - passo; x < bounds.x + bounds.width + passo; x += passo) {
					int deslocX = random.nextInt(passo);
					int deslocY = random.nextInt(passo);
					double fatorTamanho = 1.0 - variacaoTamanho + random.nextDouble() * (2 * variacaoTamanho);
					int raio = Math.max(2, (int) Math.round((passo / 5.0) * fatorTamanho));
					// Touceiras da vegetação densa são mais "encorpadas": traço
					// proporcionalmente mais grosso além de maior, não só mais longo.
					// A simples usa o traço-base (1px) que desenhaComClipSemAntialiasing
					// já deixou ajustado — nunca o traço herdado de fora.
					if (densa) {
						g2d.setStroke(new BasicStroke(Math.max(1f, raio / 2f)));
					}
					desenhaPrimitivaPadrao(g2d, x + deslocX, y + deslocY, raio);
				}
			}
		});
	}

	private void desenhaPrimitivaPadrao(Graphics2D g2d, int cx, int cy, int raio) {
		switch (tipo) {
		case VEGETACAO_SIMPLES:
			g2d.drawLine(cx - raio, cy + raio, cx + raio, cy - raio);
			break;
		case VEGETACAO_DENSA:
			g2d.drawLine(cx - raio, cy, cx + raio, cy);
			g2d.drawLine(cx, cy - raio, cx, cy + raio);
			g2d.drawLine(cx - raio, cy + raio, cx, cy - raio / 2);
			break;
		case AGUA:
			g2d.drawArc(cx - raio * 2, cy - raio, raio * 4, raio * 2, 0, 180);
			break;
		default:
			break;
		}
	}

	/**
	 * Brita: pontos pequenos espalhados em posições pseudo-aleatórias (não
	 * alinhados em grade) e em densidade bem maior que os outros padrões. A
	 * semente é fixa e a ordem de varredura é sempre a mesma, então o
	 * resultado é determinístico entre renderizações sucessivas.
	 */
	private void desenhaBrita(Graphics2D g2d, Shape formaLocal, double pivoX, double pivoY, double zoom) {
		desenhaComClipSemAntialiasing(g2d, formaLocal, pivoX, pivoY, () -> {
			Rectangle bounds = areaCoberturaPadrao(formaLocal);
			int passoBase = Math.max(4, (int) Math.round(PASSO_PADRAO_LOCAL * zoom));
			int celula = Math.max(2, passoBase / 4);
			int diametro = Math.max(1, celula / 4);
			g2d.setColor(new Color(getCorSecundaria().getRed(), getCorSecundaria().getGreen(),
					getCorSecundaria().getBlue(), getTransparencia()));
			Random random = new Random(SEMENTE_BRITA);
			for (int y = bounds.y - celula; y < bounds.y + bounds.height + celula; y += celula) {
				for (int x = bounds.x - celula; x < bounds.x + bounds.width + celula; x += celula) {
					int deslocX = random.nextInt(celula);
					int deslocY = random.nextInt(celula);
					g2d.fillOval(x + deslocX, y + deslocY, diametro, diametro);
				}
			}
		});
	}

	/**
	 * Listrado: listras retas paralelas ao eixo X local, largura igual à
	 * metade do passo de grade, alternando corSecundaria/vazio. Como o
	 * desenho roda dentro do sistema de coordenadas já rotacionado por
	 * {@link #desenhaComClipSemAntialiasing}, as listras acompanham o
	 * ângulo do objeto automaticamente, sem cálculo de rotação aqui.
	 */
	private void desenhaPadraoListrado(Graphics2D g2d, Shape formaLocal, double pivoX, double pivoY, double zoom) {
		desenhaComClipSemAntialiasing(g2d, formaLocal, pivoX, pivoY, () -> {
			Rectangle bounds = areaCoberturaPadrao(formaLocal);
			int passo = Math.max(4, (int) Math.round(PASSO_PADRAO_LOCAL * zoom));
			int larguraListra = Math.max(1, passo / 2);
			g2d.setColor(new Color(getCorSecundaria().getRed(), getCorSecundaria().getGreen(),
					getCorSecundaria().getBlue(), getTransparencia()));
			for (int y = bounds.y - passo; y < bounds.y + bounds.height + passo; y += passo) {
				g2d.fillRect(bounds.x - passo, y, bounds.width + passo * 2, larguraListra);
			}
		});
	}

	/**
	 * Xadrez: grade de células quadradas do mesmo passo dos demais padrões,
	 * preenchendo com corSecundaria quando {@code (coluna + linha)} é par —
	 * mesmo mecanismo de rotação via transform do {@link #desenhaPadraoListrado}.
	 */
	private void desenhaPadraoXadrez(Graphics2D g2d, Shape formaLocal, double pivoX, double pivoY, double zoom) {
		desenhaComClipSemAntialiasing(g2d, formaLocal, pivoX, pivoY, () -> {
			Rectangle bounds = areaCoberturaPadrao(formaLocal);
			int passo = Math.max(4, (int) Math.round(PASSO_PADRAO_LOCAL * zoom));
			int coluna = 0;
			g2d.setColor(new Color(getCorSecundaria().getRed(), getCorSecundaria().getGreen(),
					getCorSecundaria().getBlue(), getTransparencia()));
			for (int x = bounds.x - passo; x < bounds.x + bounds.width + passo; x += passo) {
				int linha = 0;
				for (int y = bounds.y - passo; y < bounds.y + bounds.height + passo; y += passo) {
					if ((coluna + linha) % 2 == 0) {
						g2d.fillRect(x, y, passo, passo);
					}
					linha++;
				}
				coluna++;
			}
		});
	}

	/**
	 * Aplica o clip da forma (em espaço LOCAL, não rotacionado — a mesma
	 * silhueta fixa desenhada pelo fill de fundo) e roda {@code desenho},
	 * restaurando clip/traço/antialiasing/transform originais ao final. O
	 * clip é aplicado ANTES de rotacionar o sistema de coordenadas do
	 * {@code Graphics2D}: como o clip fica fixo em espaço de dispositivo uma
	 * vez definido (não acompanha mudanças de transform depois), isso trava
	 * o padrão dentro do contorno real e fixo do objeto. Só DEPOIS o
	 * {@code Graphics2D} é rotacionado em torno de {@code (pivoX, pivoY)}
	 * pelo {@code angulo} do objeto — então toda primitiva desenhada dentro
	 * de {@code desenho} (usando {@code forma}/seus bounds diretamente, sem
	 * cálculo de rotação) sai rotacionada na tela, mas sempre recortada pela
	 * silhueta fixa. Resultado: o ângulo gira o CONTEÚDO do padrão (grade,
	 * dispersão, listras, xadrez), não o contorno do objeto livre em si.
	 * <p>
	 * Desliga a antialiasing só durante esse trecho (restaurando o valor
	 * anterior depois): um {@code fill()} anterior na mesma forma (o
	 * preenchimento de fundo, chamado por {@link #desenha}) seguido de
	 * {@code clip()}/desenho na MESMA forma com antialiasing ligado faz o
	 * Java2D simplesmente não pintar mais nada dentro do clip —
	 * peculiaridade que só aparece com formas grandes e com segmentos
	 * autointerceptantes (comuns depois de várias edições de vértice no
	 * editor), mas a antialiasing do preenchimento em si já rodou antes
	 * disso, então a borda da forma continua suave.
	 * <p>
	 * Também fixa um traço-base (1px) antes de rodar {@code desenho},
	 * restaurando o traço anterior ao final: os padrões de água e vegetação
	 * simples desenham linhas/arcos finos sem nunca setar o próprio traço, e
	 * por isso herdavam o que sobrava de outro desenho no mesmo Graphics2D
	 * (ex.: o traço grosso do box da pista) — o que fazia o padrão aparecer
	 * "errado" (traços grossos/borrados) só quando o objeto ficava num nível
	 * de desenho posicionado depois desse outro desenho na mesma imagem,
	 * dando a falsa impressão de que nivelDesenho influenciava o padrão.
	 */
	private void desenhaComClipSemAntialiasing(Graphics2D g2d, Shape forma, double pivoX, double pivoY,
			Runnable desenho) {
		AffineTransform transformAnterior = g2d.getTransform();
		Shape clipAnterior = g2d.getClip();
		Stroke strokeAnterior = g2d.getStroke();
		Object antialiasingAnterior = g2d.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
		g2d.clip(forma);
		double rad = Math.toRadians((double) getAngulo());
		g2d.rotate(rad, pivoX, pivoY);
		g2d.setStroke(new BasicStroke(1f));
		try {
			desenho.run();
		} finally {
			g2d.setTransform(transformAnterior);
			g2d.setClip(clipAnterior);
			g2d.setStroke(strokeAnterior);
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
					antialiasingAnterior != null ? antialiasingAnterior : RenderingHints.VALUE_ANTIALIAS_DEFAULT);
		}
	}

	@Override
	public Rectangle obterArea() {
		return generalPath.getBounds();
	}

	/**
	 * Diferente do padrão da classe base, {@code ObjetoLivre} não rotaciona
	 * a silhueta pelo {@code angulo} (ver {@link #desenha} — só o padrão
	 * interno gira), então a área de clique também não deve rotacionar, sob
	 * pena de não coincidir com a forma realmente desenhada na tela.
	 */
	@Override
	public Rectangle obterAreaClique() {
		Rectangle base = obterArea();
		if (base == null) {
			return null;
		}
		Rectangle expandido = new Rectangle(base);
		expandido.grow(TOLERANCIA_CLIQUE_PX, TOLERANCIA_CLIQUE_PX);
		return expandido;
	}

	/** Forma vetorial atual (após {@link #gerar()}), pública para inspeção/edição no editor. */
	public Shape getForma() {
		return generalPath;
	}
}
