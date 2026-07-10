package br.f1mane.entidades;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.RoundRectangle2D;

public class ObjetoConstrucao extends ObjetoDesenho {

	/** Raio dos cantos arredondados (forma aninhada e cantos da popa/ponta do barco). */
	private static final int ARCO_CANTO = 15;
	/** Margem entre forma externa e interna de todos os tipos, incluindo os dois módulos do CAMINHAO — a "borda" do objeto. */
	private static final int MARGEM_INTERNA = 10;
	/** Vão entre os dois módulos (cabine e carroceria) do CAMINHAO — metade de {@link #MARGEM_INTERNA}, separado dela para poder ser menor que a borda. */
	private static final int VAO_MODULOS_CAMINHAO = MARGEM_INTERNA / 2;

	private TipoObjetoConstrucao tipo = TipoObjetoConstrucao.QUADRADO;
	/** Percentual (0-90) do comprimento ocupado pela seção afunilada do tipo BARCO. */
	private int afunilamento = 30;
	/** Quantas vezes a forma do tipo atual é repetida; 1 = sem repetição (comportamento de sempre). */
	private int quantidadeEmpilhamento = 1;
	private DirecaoEmpilhamento direcaoEmpilhamento = DirecaoEmpilhamento.CIMA_DIREITA;
	/** Deslocamento em pixels entre uma repetição e a seguinte (não uma fração de largura/altura). */
	private int grauEmpilhamento = 10;

	private Rectangle areaTotal = new Rectangle();

	public ObjetoConstrucao() {
		setLargura(100);
		setAltura(80);
		setCorPimaria(new Color(120, 120, 140));
		setCorSecundaria(new Color(200, 200, 210));
		setTransparencia(255);
	}

	public TipoObjetoConstrucao getTipo() {
		return tipo;
	}

	public void setTipo(TipoObjetoConstrucao tipo) {
		this.tipo = tipo != null ? tipo : TipoObjetoConstrucao.QUADRADO;
	}

	public int getAfunilamento() {
		return afunilamento;
	}

	public void setAfunilamento(int afunilamento) {
		this.afunilamento = Math.max(0, Math.min(90, afunilamento));
	}

	public int getQuantidadeEmpilhamento() {
		return quantidadeEmpilhamento;
	}

	public void setQuantidadeEmpilhamento(int quantidadeEmpilhamento) {
		this.quantidadeEmpilhamento = Math.max(1, quantidadeEmpilhamento);
	}

	public DirecaoEmpilhamento getDirecaoEmpilhamento() {
		return direcaoEmpilhamento;
	}

	public void setDirecaoEmpilhamento(DirecaoEmpilhamento direcaoEmpilhamento) {
		this.direcaoEmpilhamento = direcaoEmpilhamento != null ? direcaoEmpilhamento
				: DirecaoEmpilhamento.CIMA_DIREITA;
	}

	public int getGrauEmpilhamento() {
		return grauEmpilhamento;
	}

	public void setGrauEmpilhamento(int grauEmpilhamento) {
		this.grauEmpilhamento = Math.max(0, grauEmpilhamento);
	}

	@Override
	public void desenha(Graphics2D g2d, double zoom) {
		double dx = direcaoEmpilhamento.getDx();
		double dy = direcaoEmpilhamento.getDy();
		Rectangle areaAcumulada = null;
		for (int i = 0; i < quantidadeEmpilhamento; i++) {
			int deslocamentoX = (int) Math.round(dx * grauEmpilhamento * i);
			int deslocamentoY = (int) Math.round(dy * grauEmpilhamento * i);
			desenhaFormaUnica(g2d, zoom, deslocamentoX, deslocamentoY);
			Rectangle areaRepeticao = new Rectangle(getPosicaoQuina().x + deslocamentoX,
					getPosicaoQuina().y + deslocamentoY, larguraEfetiva(), altura);
			areaAcumulada = areaAcumulada == null ? areaRepeticao : areaAcumulada.union(areaRepeticao);
		}
		this.areaTotal = areaAcumulada;
	}

	/**
	 * Largura realmente ocupada pela forma do {@link #tipo} atual. Igual a
	 * {@code largura} para a maioria dos tipos, mas {@code CAMINHAO} soma
	 * também o vão de {@link #VAO_MODULOS_CAMINHAO} entre os dois módulos
	 * (cabine + vão + carroceria), que fica fora do valor de {@code largura}
	 * em si.
	 */
	private int larguraEfetiva() {
		if (tipo == TipoObjetoConstrucao.CAMINHAO) {
			return largura + VAO_MODULOS_CAMINHAO;
		}
		return largura;
	}

	/** Largura da cabine (módulo 1) do CAMINHAO: um terço de {@code largura}, arredondado. */
	private int larguraCabineCaminhao() {
		return Math.max(1, largura / 3);
	}

	/** Largura da carroceria (módulo 2) do CAMINHAO: o dobro da cabine, absorvendo o resto de {@code largura}. */
	private int larguraCarroceriaCaminhao() {
		return Math.max(1, largura - larguraCabineCaminhao());
	}

	/**
	 * Desenha uma única instância da forma do {@link #tipo} atual, com a
	 * quina base deslocada por {@code (deslocamentoX, deslocamentoY)} —
	 * chamado em loop por {@link #desenha} para produzir o efeito de
	 * empilhamento, igual para qualquer tipo.
	 */
	private void desenhaFormaUnica(Graphics2D g2d, double zoom, int deslocamentoX, int deslocamentoY) {
		int x = getPosicaoQuina().x + deslocamentoX;
		int y = getPosicaoQuina().y + deslocamentoY;
		switch (tipo) {
		case REDONDO:
			desenhaFormaAninhada(g2d, zoom, x, y, true);
			break;
		case CAMINHAO:
			desenhaCaminhao(g2d, zoom, x, y);
			break;
		case BARCO:
			desenhaBarco(g2d, zoom, x, y);
			break;
		case QUADRADO:
		default:
			desenhaFormaAninhada(g2d, zoom, x, y, false);
			break;
		}
	}

	/** QUADRADO/REDONDO: forma externa (corPimaria) e interna com 10px de margem (corSecundaria), concêntricas. */
	private void desenhaFormaAninhada(Graphics2D g2d, double zoom, int x, int y, boolean redonda) {
		double centroX = x + largura / 2.0;
		double centroY = y + altura / 2.0;
		desenhaFormaAninhadaComPivo(g2d, zoom, x, y, largura, altura, redonda, centroX, centroY);
	}

	/**
	 * Mesma composição aninhada (externa em corPimaria, interna com 10px de
	 * margem em corSecundaria) de {@link #desenhaFormaAninhada}, mas com
	 * largura/altura e pivô de rotação explícitos — usado por
	 * {@link #desenhaCaminhao} para desenhar os dois módulos (cabine e
	 * carroceria) como "dois desenhos separados", cada um com sua própria
	 * borda arredondada e composição interna, ambos girando em torno do
	 * mesmo centro (o do caminhão como um todo, não o de cada módulo).
	 */
	private void desenhaFormaAninhadaComPivo(Graphics2D g2d, double zoom, int x, int y, int larguraForma,
			int alturaForma, boolean redonda, double centroX, double centroY) {
		Shape formaExterna = redonda ? new Ellipse2D.Double(x, y, larguraForma, alturaForma)
				: new RoundRectangle2D.Double(x, y, larguraForma, alturaForma, ARCO_CANTO, ARCO_CANTO);
		Shape formaInterna = redonda
				? new Ellipse2D.Double(x + MARGEM_INTERNA, y + MARGEM_INTERNA, larguraForma - 2 * MARGEM_INTERNA,
						alturaForma - 2 * MARGEM_INTERNA)
				: new RoundRectangle2D.Double(x + MARGEM_INTERNA, y + MARGEM_INTERNA,
						larguraForma - 2 * MARGEM_INTERNA, alturaForma - 2 * MARGEM_INTERNA, ARCO_CANTO, ARCO_CANTO);
		desenhaFormaRotacionada(g2d, zoom, formaExterna, getCorPimaria(), centroX, centroY);
		desenhaFormaRotacionada(g2d, zoom, formaInterna, getCorSecundaria(), centroX, centroY);
	}

	/**
	 * CAMINHAO: dois módulos lado a lado, cada um com a mesma composição
	 * aninhada (borda arredondada + desenho interno) de QUADRADO — a
	 * carroceria (módulo 2) tem o dobro da largura da cabine (módulo 1),
	 * ambos com a mesma altura. Largura e altura são ambas aplicadas (cabine
	 * = largura/3, carroceria = 2×largura/3), com um vão de
	 * {@link #VAO_MODULOS_CAMINHAO} entre os dois módulos — metade da margem
	 * ({@link #MARGEM_INTERNA}) usada entre forma externa e interna de cada
	 * módulo, que permanece inalterada.
	 */
	private void desenhaCaminhao(Graphics2D g2d, double zoom, int x, int y) {
		int larguraCabine = larguraCabineCaminhao();
		int larguraCarroceria = larguraCarroceriaCaminhao();
		double centroX = x + (larguraCabine + VAO_MODULOS_CAMINHAO + larguraCarroceria) / 2.0;
		double centroY = y + altura / 2.0;
		desenhaFormaAninhadaComPivo(g2d, zoom, x, y, larguraCabine, altura, false, centroX, centroY);
		desenhaFormaAninhadaComPivo(g2d, zoom, x + larguraCabine + VAO_MODULOS_CAMINHAO, y, larguraCarroceria, altura,
				false, centroX, centroY);
	}

	/**
	 * BARCO: mesma composição aninhada (externa em corPimaria, interna em
	 * corSecundaria) dos demais tipos, mas seguindo o perfil afunilado do
	 * barco (proa em ponta arredondada, popa com cantos arredondados) em vez
	 * de um retângulo/elipse. A forma interna é uma erosão geométrica de
	 * {@link #MARGEM_INTERNA} pixels da forma externa (via
	 * {@link #calcularFormaInterna}), não uma cópia escalada
	 * percentualmente — isso mantém a borda com a mesma espessura em todo o
	 * contorno, inclusive ao longo do afunilamento (uma cópia escalada
	 * percentualmente afinava a borda perto da proa).
	 */
	private void desenhaBarco(Graphics2D g2d, double zoom, int x, int y) {
		double centroX = x + largura / 2.0;
		double centroY = y + altura / 2.0;
		GeneralPath formaExterna = construirCaminhoBarco(x, y, largura, altura);
		Shape formaInterna = calcularFormaInterna(formaExterna);
		desenhaFormaRotacionada(g2d, zoom, formaExterna, getCorPimaria(), centroX, centroY);
		desenhaFormaRotacionada(g2d, zoom, formaInterna, getCorSecundaria(), centroX, centroY);
	}

	/**
	 * Caminho de um barco: popa (esquerda) com os dois cantos arredondados,
	 * proa (direita) também arredondada na ponta (não um vértice em ângulo
	 * vivo) — todos os vértices da forma são arredondados, mesmo espírito
	 * das demais formas aninhadas.
	 */
	private GeneralPath construirCaminhoBarco(double x, double y, double largura, double altura) {
		double comprimentoAfunilado = largura * (afunilamento / 100.0);
		double inicioProaX = x + largura - comprimentoAfunilado;
		double pontaX = x + largura;
		double pontaY = y + altura / 2.0;
		double arcoCanto = Math.max(1, Math.min(ARCO_CANTO, altura / 2.0 - 1));
		// Raio da ponta limitado a uma fração do próprio afunilamento, pra não ultrapassar o início da proa.
		double arcoPonta = Math.max(1, Math.min(ARCO_CANTO, comprimentoAfunilado * 0.4));

		double distArestaProa = Math.hypot(comprimentoAfunilado, altura / 2.0);
		double uTopoX = comprimentoAfunilado / distArestaProa;
		double uTopoY = (altura / 2.0) / distArestaProa;
		double uBaseX = comprimentoAfunilado / distArestaProa;
		double uBaseY = -(altura / 2.0) / distArestaProa;

		double p1x = pontaX - uTopoX * arcoPonta;
		double p1y = pontaY - uTopoY * arcoPonta;
		double p2x = pontaX - uBaseX * arcoPonta;
		double p2y = pontaY - uBaseY * arcoPonta;

		GeneralPath path = new GeneralPath();
		path.moveTo(x, y + arcoCanto);
		path.quadTo(x, y, x + arcoCanto, y);
		path.lineTo(inicioProaX, y);
		path.lineTo(p1x, p1y);
		path.quadTo(pontaX, pontaY, p2x, p2y);
		path.lineTo(inicioProaX, y + altura);
		path.lineTo(x + arcoCanto, y + altura);
		path.quadTo(x, y + altura, x, y + altura - arcoCanto);
		path.closePath();
		return path;
	}

	/**
	 * Encolhe {@code forma} por {@link #MARGEM_INTERNA} pixels de forma
	 * geometricamente homogênea (erosão via contorno de traço arredondado),
	 * em vez de escalar largura/altura por um percentual — o que manteria
	 * uma borda mais fina perto de pontas/afunilamentos, já que a mesma
	 * redução percentual produz um deslocamento menor onde o contorno já é
	 * mais estreito.
	 */
	private Shape calcularFormaInterna(Shape forma) {
		Area interior = new Area(forma);
		BasicStroke contornoDaMargem = new BasicStroke(MARGEM_INTERNA * 2, BasicStroke.CAP_ROUND,
				BasicStroke.JOIN_ROUND);
		interior.subtract(new Area(contornoDaMargem.createStrokedShape(forma)));
		return interior;
	}

	/**
	 * Rotaciona {@code forma} por {@code angulo} em torno de {@code (centroX, centroY)}
	 * (o centro da forma como um todo, não do próprio {@code forma} — importante
	 * para peças não concêntricas como as de {@link #desenhaCaminhao}, que
	 * precisam girar juntas como um corpo rígido), escala por {@code zoom} e
	 * preenche com {@code cor}.
	 */
	private void desenhaFormaRotacionada(Graphics2D g2d, double zoom, Shape forma, Color cor, double centroX,
			double centroY) {
		double rad = Math.toRadians((double) getAngulo());
		AffineTransform affineTransform = AffineTransform.getScaleInstance(1, 1);
		affineTransform.setToRotation(rad, centroX, centroY);
		GeneralPath generalPath = new GeneralPath(forma);
		generalPath.transform(affineTransform);
		affineTransform.setToScale(zoom, zoom);
		g2d.setColor(new Color(cor.getRed(), cor.getGreen(), cor.getBlue(), getTransparencia()));
		g2d.fill(generalPath.createTransformedShape(affineTransform));
	}

	@Override
	public Rectangle obterArea() {
		return areaTotal;
	}

}
