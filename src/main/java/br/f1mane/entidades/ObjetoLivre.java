package br.f1mane.entidades;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import br.f1mane.recursos.CarregadorRecursos;
import br.nnpe.Global;

public class ObjetoLivre extends ObjetoDesenho {
	/** Tamanho (em unidades de mundo, antes do zoom) da célula da grade do padrão de preenchimento. */
	private static final int PASSO_PADRAO_LOCAL = Carro.ALTURA;
	/** Semente fixa: a dispersão da brita é "aleatória" mas sempre a mesma entre renderizações. */
	private static final long SEMENTE_BRITA = 20260703L;
	/** Semente fixa da dispersão (posição e tamanho) da vegetação, densa e simples. */
	private static final long SEMENTE_VEGETACAO = 20260710L;
	/**
	 * Vegetação densa: touceiras bem maiores (célula ~1.6x) e com tamanho bem
	 * variado entre si. Densidade (touceiras por área, na GRADE DE
	 * CANDIDATAS — a densidade final desenhada é menor, já que
	 * {@link #sobrepoeTouceiraAceita} e {@link #cabeInteiraNaSilhueta}
	 * descartam parte delas) é proporcional a 1/fatorPasso² — histórico:
	 * dividir por sqrt(3) triplicou a densidade original; multiplicar por
	 * sqrt(2) reduziu isso à metade (1.5x); dividir de novo por sqrt(2)
	 * dobrou essa versão, voltando a 3x a densidade original — a versão em
	 * uso, agora com a grade mais apertada preenchendo as folgas deixadas
	 * pelas rejeições de sobreposição/borda. Mesma proporção touceira/célula
	 * de antes (raio acompanha o passo).
	 */
	private static final double FATOR_PASSO_VEGETACAO_DENSA = 1.6 / Math.sqrt(3);
	/**
	 * Vegetação simples: densidade (marcas por área) proporcional a
	 * 1/fatorPasso² — dividir por sqrt(2) dobra a densidade original (a
	 * pedido do usuário; sem checagem de sobreposição entre marcas, ao
	 * contrário da densa, então as relvas/arbustos podem se intersectar
	 * livremente, o que é aceitável aqui).
	 */
	private static final double FATOR_PASSO_VEGETACAO_SIMPLES = 1.0 / Math.sqrt(2);
	private static final double VARIACAO_TAMANHO_VEGETACAO_SIMPLES = 0.15;
	/**
	 * Sprites de vegetação simples: relva (`vegetacaoRelvaN.png`, pintada com
	 * {@code corPimaria}) e arbusto (`vegetacaoArbustoN.png`, pintado com
	 * {@code corSecundaria}), originados da pasta de referência
	 * `vegetacao-simples/` (nomes `relva-cor1-N`/`arbusto-cor2-N` já indicam
	 * qual cor cada um usa) e convertidos para template em tons de cinza,
	 * mesmo espírito dos sprites de árvore da vegetação densa. Cada marca
	 * sorteia relva OU arbusto (nunca os dois juntos, diferente da árvore
	 * tronco+copa) e uma variante entre as 4 de cada — SEM esticamento: a
	 * proporção largura/altura nativa do sprite é sempre preservada (ver
	 * {@link #desenhaMarcaSimples}), diferente da copa da vegetação densa.
	 */
	private static final int QUANTIDADE_RELVA_SIMPLES = 4;
	private static final int QUANTIDADE_ARBUSTO_SIMPLES = 4;
	/**
	 * Tamanho de cada marca de vegetação simples (maior dimensão do sprite,
	 * preservada a proporção nativa), relativo ao tamanho de referência de
	 * UMA árvore de vegetação densa (a largura da copa, {@code raioCopa*2},
	 * calculada com os fatores de densidade da densa independente do tipo
	 * que está sendo desenhado no momento — ver {@link #desenhaPadraoVegetacao}).
	 */
	private static final double FATOR_TAMANHO_VEGETACAO_SIMPLES = 1.0 / 3.0;
	/**
	 * Modo de preview (checkbox "Padrões" desmarcado): a marca única
	 * centralizada é ampliada pra {@code FATOR_TAMANHO_EXEMPLO_PREVIEW}
	 * vezes o tamanho de referência da vegetação DENSA (não o tamanho normal
	 * de cada tipo), pra exemplificar melhor como o padrão fica — um único
	 * exemplo grande, fácil de examinar, em vez do tamanho minúsculo que a
	 * marca teria dentro do preenchimento completo de verdade.
	 */
	private static final double FATOR_TAMANHO_EXEMPLO_PREVIEW = 3.0;
	/**
	 * Sprites de árvore vista de LADO usados pela vegetação densa: tronco e
	 * copa são arquivos separados em {@code src/main/resources/png/}
	 * (`vegetacaoCauleN.png`/`vegetacaoCopaN.png`, N=1..quantidade),
	 * originados da pasta de referência `vegetacao densa/` trazida ao
	 * projeto e convertidos para template em tons de cinza (mesmo brilho
	 * por pixel do original, matiz/saturação removidos) — ver
	 * {@link br.f1mane.recursos.CarregadorRecursos#pintarMonocromatico}, que
	 * repinta cada template com {@code corPimaria} (tronco) ou
	 * {@code corSecundaria} (copa) preservando o shading original. Cada
	 * touceira sorteia um tronco e uma copa independentemente (variação de
	 * COMBINAÇÃO, não só de modelo isolado), mas todas as árvores têm o
	 * MESMO tamanho entre si — sem variação de escala, diferente da vista de
	 * topo anterior (vista lateral não tem a mesma justificativa de
	 * profundidade).
	 */
	private static final int QUANTIDADE_CAULES_ARVORE_LATERAL = 5;
	private static final int QUANTIDADE_COPAS_ARVORE_LATERAL = 6;
	/** Metade da largura da copa (e raio usado no anti-sobreposição), relativo ao passo da grade. */
	private static final double FATOR_RAIO_COPA_ARVORE_LATERAL = 0.55;
	/** Altura do tronco, relativa ao raio da copa. */
	private static final double FATOR_ALTURA_TRONCO_ARVORE_LATERAL = 0.5;
	/** Largura do tronco, relativa ao raio da copa. */
	private static final double FATOR_LARGURA_TRONCO_ARVORE_LATERAL = 0.28;

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

		// VEGETACAO_DENSA e VEGETACAO_SIMPLES não preenchem fundo: corPimaria
		// e corSecundaria passam a colorir só as marcas individuais (tronco/
		// copa da árvore, ou relva/arbusto — ver desenhaPadraoVegetacao),
		// deixando a área transparente onde não há marca. Preenchimento
		// sólido de corPimaria pra ambas seria idêntico ao usado pela relva,
		// tornando a relva invisível contra o próprio fundo. Os demais tipos
		// continuam com o fundo sólido de sempre.
		if (tipo != TipoObjetoLivre.VEGETACAO_DENSA && tipo != TipoObjetoLivre.VEGETACAO_SIMPLES) {
			g2d.setColor(new Color(getCorPimaria().getRed(), getCorPimaria()
					.getGreen(), getCorPimaria().getBlue(), getTransparencia()));
			g2d.fill(formaZoomLocal);
		}

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

		// Modo de preview (checkbox "Padrão" desmarcado no editor): só uma
		// marca de exemplo é desenhada dentro da forma (ver os métodos de
		// padrão acima), então a borda real do objeto livre deixa de ficar
		// óbvia — sobretudo em VEGETACAO_DENSA, que nem preenche fundo.
		// Contorno magenta (cor fixa, não relacionada a corPimaria/
		// corSecundaria) desenhado por cima exemplifica até onde vai a
		// forma. Some quando o padrão completo está ligado (a própria
		// grade/dispersão já deixa a extensão da forma óbvia) e nunca
		// aparece em POLIGONO_SIMPLES (o checkbox não afeta esse tipo).
		if (tipo != TipoObjetoLivre.POLIGONO_SIMPLES && !Global.padraoObjetoLivreCompleto) {
			Color corAnterior = g2d.getColor();
			Stroke strokeAnterior = g2d.getStroke();
			g2d.setColor(Color.MAGENTA);
			g2d.setStroke(new BasicStroke(2f));
			g2d.draw(formaZoomLocal);
			g2d.setColor(corAnterior);
			g2d.setStroke(strokeAnterior);
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
		// Modo de preview (checkbox "Padrões" desmarcado): diferente da
		// vegetação (densa/simples, que ganham um exemplo ampliado), os
		// demais padrões (água aqui, brita/listrado/xadrez análogo) não
		// desenham nenhuma marca de exemplo — só a borda magenta já
		// desenhada por ObjetoLivre.desenha() marca a área.
		if (!Global.padraoObjetoLivreCompleto) {
			return;
		}
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
	 * espírito da {@link #desenhaBrita}). Semente fixa: determinístico entre
	 * renderizações sucessivas.
	 * <p>
	 * A simples sorteia, por marca, relva ({@code corPimaria}) OU arbusto
	 * ({@code corSecundaria}) — nunca os dois juntos — e uma variante entre
	 * as 4 de cada, desenhando o sprite correspondente SEM esticamento (ver
	 * {@link #desenhaMarcaSimples}), com uma leve variação de escala
	 * uniforme entre marcas (ver {@code VARIACAO_TAMANHO_VEGETACAO_SIMPLES}
	 * — mantém a proporção, só aumenta/diminui a marca inteira).
	 * <p>
	 * A densa desenha árvores vistas de LADO a partir de sprites (tronco em
	 * {@code corPimaria}, copa em {@code corSecundaria} — ver
	 * {@link #desenhaArvoreSeCouber}), sorteando um tronco e uma copa
	 * INDEPENDENTEMENTE ({@link #QUANTIDADE_CAULES_ARVORE_LATERAL} x
	 * {@link #QUANTIDADE_COPAS_ARVORE_LATERAL} combinações possíveis). A
	 * altura da árvore é sempre a mesma (sem variação de escala — vista
	 * lateral não tem a mesma justificativa de profundidade que a vista de
	 * topo anterior), mas a LARGURA da copa sorteia, por árvore, um fator de
	 * esticamento entre a proporção original do sprite (mais estreita/fina)
	 * e o esticamento total até preencher o envelope quadrado (mais larga) —
	 * ver {@link #desenhaArvoreSeCouber}. Árvores candidatas que se
	 * sobreporiam a uma já aceita
	 * (ver {@link #sobrepoeTouceiraAceita}) ou que ficariam cortadas pela
	 * borda da forma (ver {@link #cabeInteiraNaSilhueta}) são descartadas —
	 * a posição simplesmente fica sem árvore, em vez de desenhar uma se
	 * interceptando com a vizinha ou "quebrada" no contorno do objeto (a
	 * simples não tem essa checagem — marcas menores, sem o mesmo pedido).
	 * O descarte não pula nenhum sorteio do {@code random}, então a
	 * sequência consumida — e portanto o resultado — continua idêntica
	 * entre renderizações.
	 */
	private void desenhaPadraoVegetacao(Graphics2D g2d, Shape formaLocal, double pivoX, double pivoY, double zoom) {
		boolean densa = tipo == TipoObjetoLivre.VEGETACAO_DENSA;
		Area areaSilhueta = densa ? new Area(formaLocal) : null;
		AffineTransform rotacaoConteudo = densa
				? AffineTransform.getRotateInstance(Math.toRadians((double) getAngulo()), pivoX, pivoY)
				: null;
		List<double[]> touceirasAceitas = densa ? new ArrayList<double[]>() : null;
		// Tamanho de referência da vegetação simples é derivado do tamanho
		// da árvore de vegetação DENSA (largura da copa), sempre com os
		// fatores da densa — independente de qual tipo está sendo desenhado
		// agora — pra "1/3 do tamanho do padrão da vegetação densa" não
		// mudar se um dia o passo/densidade da própria simples mudar.
		int passoDensaReferencia = Math.max(4, (int) Math.round(PASSO_PADRAO_LOCAL * FATOR_PASSO_VEGETACAO_DENSA * zoom));
		double tamanhoBaseSimples = (passoDensaReferencia * FATOR_RAIO_COPA_ARVORE_LATERAL * 2)
				* FATOR_TAMANHO_VEGETACAO_SIMPLES;
		desenhaComClipSemAntialiasing(g2d, formaLocal, pivoX, pivoY, () -> {
			Rectangle bounds = areaCoberturaPadrao(formaLocal);
			double fatorPasso = densa ? FATOR_PASSO_VEGETACAO_DENSA : FATOR_PASSO_VEGETACAO_SIMPLES;
			int passo = Math.max(4, (int) Math.round(PASSO_PADRAO_LOCAL * fatorPasso * zoom));
			double raioCopa = passo * FATOR_RAIO_COPA_ARVORE_LATERAL;
			double alturaTronco = raioCopa * FATOR_ALTURA_TRONCO_ARVORE_LATERAL;
			double larguraTronco = raioCopa * FATOR_LARGURA_TRONCO_ARVORE_LATERAL;
			if (!Global.padraoObjetoLivreCompleto) {
				double cx = bounds.getCenterX();
				double cy = bounds.getCenterY();
				if (densa) {
					desenhaArvoreSeCouber(g2d, cx, cy, raioCopa * FATOR_TAMANHO_EXEMPLO_PREVIEW,
							alturaTronco * FATOR_TAMANHO_EXEMPLO_PREVIEW, larguraTronco * FATOR_TAMANHO_EXEMPLO_PREVIEW,
							0, 0, 1.0, rotacaoConteudo, areaSilhueta);
				} else {
					double tamanhoDensaReferencia = passoDensaReferencia * FATOR_RAIO_COPA_ARVORE_LATERAL * 2;
					desenhaMarcaSimples(g2d, cx, cy, tamanhoDensaReferencia * FATOR_TAMANHO_EXEMPLO_PREVIEW, true, 0);
				}
				return;
			}
			Random random = new Random(SEMENTE_VEGETACAO);
			for (int y = bounds.y - passo; y < bounds.y + bounds.height + passo; y += passo) {
				for (int x = bounds.x - passo; x < bounds.x + bounds.width + passo; x += passo) {
					int deslocX = random.nextInt(passo);
					int deslocY = random.nextInt(passo);
					if (densa) {
						int indiceCaule = random.nextInt(QUANTIDADE_CAULES_ARVORE_LATERAL);
						int indiceCopa = random.nextInt(QUANTIDADE_COPAS_ARVORE_LATERAL);
						double fatorEsticamentoCopa = random.nextDouble();
						double cx = x + deslocX;
						double cy = y + deslocY;
						if (sobrepoeTouceiraAceita(touceirasAceitas, cx, cy, raioCopa)) {
							continue;
						}
						boolean desenhou = desenhaArvoreSeCouber(g2d, cx, cy, raioCopa, alturaTronco, larguraTronco,
								indiceCaule, indiceCopa, fatorEsticamentoCopa, rotacaoConteudo, areaSilhueta);
						if (desenhou) {
							touceirasAceitas.add(new double[] { cx, cy, raioCopa });
						}
					} else {
						boolean ehRelva = random.nextBoolean();
						int variante = random.nextInt(ehRelva ? QUANTIDADE_RELVA_SIMPLES : QUANTIDADE_ARBUSTO_SIMPLES);
						double fatorTamanho = 1.0 - VARIACAO_TAMANHO_VEGETACAO_SIMPLES
								+ random.nextDouble() * (2 * VARIACAO_TAMANHO_VEGETACAO_SIMPLES);
						double cx = x + deslocX;
						double cy = y + deslocY;
						desenhaMarcaSimples(g2d, cx, cy, tamanhoBaseSimples * fatorTamanho, ehRelva, variante);
					}
				}
			}
		});
	}

	/**
	 * Desenha uma marca de vegetação simples (relva em {@code corPimaria} ou
	 * arbusto em {@code corSecundaria}) centrada em {@code (cx, cy)}, SEM
	 * esticar o sprite: a proporção largura/altura nativa é sempre
	 * preservada, escalando a MAIOR dimensão nativa para {@code tamanho} e a
	 * outra proporcionalmente — diferente da copa da vegetação densa, que
	 * pode esticar. "Evitar esticamento" foi um pedido explícito do usuário
	 * pra ver como a vegetação simples fica na proporção original dos
	 * sprites antes de considerar variar isso também.
	 */
	private void desenhaMarcaSimples(Graphics2D g2d, double cx, double cy, double tamanho, boolean ehRelva,
			int variante) {
		String caminho = (ehRelva ? "png/vegetacaoRelva" : "png/vegetacaoArbusto") + (variante + 1) + ".png";
		Color cor = ehRelva ? getCorPimaria() : getCorSecundaria();
		double proporcao = CarregadorRecursos.obterProporcaoLarguraAltura(caminho);
		double largura = proporcao >= 1.0 ? tamanho : tamanho * proporcao;
		double altura = proporcao >= 1.0 ? tamanho / proporcao : tamanho;
		int w = Math.max(1, (int) Math.round(largura));
		int h = Math.max(1, (int) Math.round(altura));

		Composite compositeAnterior = g2d.getComposite();
		Object interpolacaoAnterior = g2d.getRenderingHint(RenderingHints.KEY_INTERPOLATION);
		if (getTransparencia() < 255) {
			g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, getTransparencia() / 255f));
		}
		g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

		BufferedImage sprite = CarregadorRecursos.pintarMonocromatico(caminho, cor);
		g2d.drawImage(sprite, (int) Math.round(cx - w / 2.0), (int) Math.round(cy - h / 2.0), w, h, null);

		g2d.setComposite(compositeAnterior);
		g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
				interpolacaoAnterior != null ? interpolacaoAnterior : RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
	}

	/**
	 * {@code true} se o círculo (centro, raio) da candidata invade o círculo
	 * de alguma touceira já aceita — suficiente pra garantir que árvores de
	 * vegetação densa nunca se sobreponham entre si. Comparado em espaço
	 * LOCAL (antes da rotação do conteúdo): como a rotação é uma isometria em
	 * torno do mesmo pivô pra todas as touceiras, distâncias entre centros
	 * não mudam, então não é preciso rotacionar nada aqui.
	 */
	private boolean sobrepoeTouceiraAceita(List<double[]> touceirasAceitas, double cx, double cy, double raio) {
		for (double[] outra : touceirasAceitas) {
			double distancia = Math.hypot(cx - outra[0], cy - outra[1]);
			if (distancia < raio + outra[2]) {
				return true;
			}
		}
		return false;
	}

	/**
	 * {@code true} se {@code formaFinal} (já na posição/rotação em que
	 * efetivamente aparece na tela) cai inteiramente dentro de
	 * {@code areaSilhueta} (a silhueta fixa e não rotacionada do objeto) —
	 * usado pra descartar árvores que o clip cortaria pela metade na borda
	 * do objeto livre, em vez de deixá-las aparecer "quebradas".
	 */
	private boolean cabeInteiraNaSilhueta(Shape formaFinal, Area areaSilhueta) {
		Area foraDaSilhueta = new Area(formaFinal);
		foraDaSilhueta.subtract(areaSilhueta);
		return foraDaSilhueta.isEmpty();
	}

	/**
	 * Desenha uma árvore vista de lado centrada em {@code (cx, cy)} a partir
	 * dos sprites {@code vegetacaoCaule<indiceCaule+1>.png} (tronco, pintado
	 * com {@code corPimaria}) e {@code vegetacaoCopa<indiceCopa+1>.png}
	 * (copa, pintada com {@code corSecundaria}) — ver
	 * {@link CarregadorRecursos#pintarMonocromatico}. Cada sprite é
	 * esticado para preencher exatamente o mesmo retângulo-envelope que a
	 * geometria procedural anterior usava (tronco: {@code larguraTronco} x
	 * {@code alturaTronco}; copa: {@code raioCopa*2} x a altura restante),
	 * preservando o tamanho uniforme entre árvores independente da
	 * combinação de tronco/copa sorteada, EXCETO pela largura da copa: em
	 * vez de sempre esticar o sprite pra preencher o envelope quadrado
	 * inteiro, {@code fatorEsticamentoCopa} (0–1, sorteado por árvore)
	 * interpola entre a largura NATURAL do sprite (preservando a proporção
	 * original — mais estreita/fina, já que os sprites de referência são
	 * mais altos que largos) em {@code fatorEsticamentoCopa=0} e a largura
	 * do envelope cheio (esticamento total, mesmo comportamento de antes)
	 * em {@code fatorEsticamentoCopa=1} — a pedido do usuário, que gostou do
	 * esticamento mas quis variedade entre copas mais finas e mais largas.
	 * A copa sempre fica centralizada no mesmo {@code cx}, qualquer que seja
	 * a largura sorteada. A verificação de contenção usa o envelope CHEIO
	 * (mais conservadora que o necessário quando a largura real é menor,
	 * nunca insuficiente). Só desenha se a silhueta combinada (os dois
	 * retângulos), já rotacionada pelo conteúdo do padrão, couber inteira
	 * dentro de {@code areaSilhueta} — ver {@link #cabeInteiraNaSilhueta}.
	 * Quando {@code areaSilhueta}/{@code rotacaoConteudo} são {@code null}
	 * (não deveria acontecer fora de {@code VEGETACAO_DENSA}), desenha sem
	 * checar contenção. Retorna {@code true} se a árvore foi desenhada.
	 */
	private boolean desenhaArvoreSeCouber(Graphics2D g2d, double cx, double cy, double raioCopa,
			double alturaTronco, double larguraTronco, int indiceCaule, int indiceCopa,
			double fatorEsticamentoCopa, AffineTransform rotacaoConteudo, Area areaSilhueta) {
		double alturaTotal = alturaTronco + raioCopa * 2;
		double topoCopa = cy - alturaTotal / 2.0;
		double baseTronco = cy + alturaTotal / 2.0;
		double topoTronco = baseTronco - alturaTronco;

		Rectangle2D.Double retTronco = new Rectangle2D.Double(cx - larguraTronco / 2, topoTronco, larguraTronco,
				alturaTronco);
		Rectangle2D.Double retCopa = new Rectangle2D.Double(cx - raioCopa, topoCopa, raioCopa * 2,
				topoTronco - topoCopa);

		if (areaSilhueta != null && rotacaoConteudo != null) {
			Area areaArvore = new Area(retTronco);
			areaArvore.add(new Area(retCopa));
			if (!cabeInteiraNaSilhueta(rotacaoConteudo.createTransformedShape(areaArvore), areaSilhueta)) {
				return false;
			}
		}

		Composite compositeAnterior = g2d.getComposite();
		Object interpolacaoAnterior = g2d.getRenderingHint(RenderingHints.KEY_INTERPOLATION);
		if (getTransparencia() < 255) {
			g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, getTransparencia() / 255f));
		}
		// BILINEAR (não a reamostragem progressiva por halvings de
		// pintarModeloV2): os sprites já vêm cacheados na resolução nativa
		// por CarregadorRecursos.pintarMonocromatico (só recolore, não
		// redimensiona), então quem escala pra cada árvore é o próprio
		// drawImage — precisa ser barato porque roda a cada árvore/frame,
		// diferente da qualidade one-shot usada pros sprites de carro.
		g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

		int wTronco = Math.max(1, (int) Math.round(larguraTronco));
		int hTronco = Math.max(1, (int) Math.round(alturaTronco));
		BufferedImage spriteTronco = CarregadorRecursos.pintarMonocromatico(
				"png/vegetacaoCaule" + (indiceCaule + 1) + ".png", getCorPimaria());
		g2d.drawImage(spriteTronco, (int) Math.round(retTronco.x), (int) Math.round(retTronco.y), wTronco, hTronco,
				null);

		String caminhoCopa = "png/vegetacaoCopa" + (indiceCopa + 1) + ".png";
		int hCopa = Math.max(1, (int) Math.round(retCopa.height));
		double proporcaoNativaCopa = CarregadorRecursos.obterProporcaoLarguraAltura(caminhoCopa);
		double larguraNatural = hCopa * proporcaoNativaCopa;
		double larguraEsticada = Math.max(larguraNatural,
				larguraNatural + fatorEsticamentoCopa * (retCopa.width - larguraNatural));
		int wCopa = Math.max(1, (int) Math.round(Math.min(larguraEsticada, retCopa.width)));
		int xCopa = (int) Math.round(cx - wCopa / 2.0);
		BufferedImage spriteCopa = CarregadorRecursos.pintarMonocromatico(caminhoCopa, getCorSecundaria());
		g2d.drawImage(spriteCopa, xCopa, (int) Math.round(retCopa.y), wCopa, hCopa, null);

		g2d.setComposite(compositeAnterior);
		g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
				interpolacaoAnterior != null ? interpolacaoAnterior : RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
		return true;
	}

	private void desenhaPrimitivaPadrao(Graphics2D g2d, int cx, int cy, int raio) {
		switch (tipo) {
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
		// Modo de preview: sem marca de exemplo pra brita — só a borda
		// magenta (ver ObjetoLivre.desenha()) marca a área.
		if (!Global.padraoObjetoLivreCompleto) {
			return;
		}
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
		// Modo de preview: sem marca de exemplo pro listrado — só a borda
		// magenta (ver ObjetoLivre.desenha()) marca a área.
		if (!Global.padraoObjetoLivreCompleto) {
			return;
		}
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
		// Modo de preview: sem marca de exemplo pro xadrez — só a borda
		// magenta (ver ObjetoLivre.desenha()) marca a área.
		if (!Global.padraoObjetoLivreCompleto) {
			return;
		}
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
