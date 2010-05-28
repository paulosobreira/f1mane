package sowbreira.f1mane.visao;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import sowbreira.f1mane.controles.ControleEstatisticas;
import sowbreira.f1mane.controles.InterfaceJogo;
import sowbreira.f1mane.entidades.Carro;
import sowbreira.f1mane.entidades.Circuito;
import sowbreira.f1mane.entidades.No;
import sowbreira.f1mane.entidades.Piloto;
import sowbreira.f1mane.entidades.SafetyCar;
import sowbreira.f1mane.entidades.Volta;
import sowbreira.f1mane.recursos.CarregadorRecursos;
import sowbreira.f1mane.recursos.idiomas.Lang;
import br.nnpe.GeoUtil;
import br.nnpe.ImageUtil;
import br.nnpe.Logger;
import br.nnpe.Util;

/**
 * @author Paulo Sobreira
 */
public class PainelCircuito extends JPanel {
	private static final long serialVersionUID = -5268795362549996148L;
	private InterfaceJogo controleJogo;
	private GerenciadorVisual gerenciadorVisual;
	private Point pointDesenhaClima = new Point(10, 60);
	private Point pointDesenhaVelo = new Point(10, 60);
	private Point pointDesenhaSC = new Point(10, 85);
	private Point pointDesenhaHelmet = new Point(10, 130);
	public final static Color luzDistProx1 = new Color(0, 255, 0, 100);
	public final static Color luzDistProx2 = new Color(255, 255, 0, 100);
	public final static Color luzApagada = new Color(255, 255, 255, 170);
	public final static Color luzAcesa = new Color(255, 0, 0, 255);
	public final static Color farol = new Color(0, 0, 0);
	public final static Color red = new Color(250, 0, 0, 150);
	public final static Color gre = new Color(0, 255, 0, 150);
	public final static Color yel = new Color(255, 255, 0, 150);
	public final static Color blu = new Color(105, 105, 105, 40);
	public final static BufferedImage carroimgDano = CarregadorRecursos
			.carregaBufferedImageTranspareciaBranca("CarroLado.png");
	public final static BufferedImage helmetPiloto = CarregadorRecursos
			.carregaBufferedImageTranspareciaBranca("helmet.gif");
	public final static BufferedImage scimg = CarregadorRecursos
			.carregaBufferedImageTranspareciaBranca("safetycar.gif");
	public final static BufferedImage scima = CarregadorRecursos
			.carregaBufferedImageTranspareciaBranca("sfcima.png");
	private int qtdeLuzesAcesas = 5;
	private Piloto pilotQualificacao;
	private Point pointQualificacao;
	private Map mapDesenharQualificacao = new HashMap();
	private boolean desenhouQualificacao;
	private boolean desenhaInfo = true;
	public final static ImageIcon fuel = new ImageIcon(CarregadorRecursos
			.carregarImagem("fuel.jpg"));
	public final static ImageIcon tyre = new ImageIcon(CarregadorRecursos
			.carregarImagem("tyre.jpg"));
	private int mx;
	private int my;
	public double zoom = 1;
	private Circuito circuito;
	private BasicStroke trilho = new BasicStroke(1.0f);
	private BasicStroke pista;
	private BasicStroke pistaTinta;
	private BasicStroke box;
	private int larguraPistaPixeis;
	private BasicStroke zebra;
	private Shape[] grid = new Shape[24];
	private Shape[] asfaltoGrid = new Shape[24];
	private Shape[] boxParada = new Shape[12];
	private Shape[] boxCor1 = new Shape[12];
	private Shape[] boxCor2 = new Shape[12];
	private double larguraPista = 0;
	private Rectangle limitesViewPort;
	private long lastDraw;
	private String mutex = "mutex";

	public PainelCircuito(InterfaceJogo jogo,
			GerenciadorVisual gerenciadorVisual) {
		controleJogo = jogo;
		this.gerenciadorVisual = gerenciadorVisual;
		// setDoubleBuffered(false);
		addMouseListener(new MouseAdapter() {

			public void mouseClicked(MouseEvent e) {
				// Logger.logar("Pontos Editor :" + e.getX() + " - "
				// + e.getY());
				super.mouseClicked(e);

			}

		});

		circuito = controleJogo.getCircuito();
		larguraPista = 1.1;
		List l = circuito.getPistaFull();
		for (Iterator iterator = l.iterator(); iterator.hasNext();) {
			No no = (No) iterator.next();
			Point point = no.getPoint();
			if (point.x > mx) {
				mx = point.x;
			}
			if (point.y > my) {
				my = point.y;
			}

		}
		mx += 300;
		my += 300;
		atualizaVarZoom();
		// controleJogo.getMainFrame().pack();
	}

	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		long agora = System.currentTimeMillis();
		if (agora < lastDraw) {
			return;
		}
		lastDraw = agora;
		synchronized (mutex) {

			Graphics2D g2d = (Graphics2D) g;
			setarHints(g2d);
			limitesViewPort = (Rectangle) limitesViewPort();
			if (larguraPistaPixeis == 0)
				larguraPistaPixeis = Util.inte(176 * larguraPista * zoom);
			if (pista == null)
				pista = new BasicStroke(larguraPistaPixeis,
						BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
			if (pistaTinta == null)
				pistaTinta = new BasicStroke(Util
						.inte(larguraPistaPixeis * 1.05),
						BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
			if (box == null)
				box = new BasicStroke(Util.inte(larguraPistaPixeis * .4),
						BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
			if (zebra == null)
				zebra = new BasicStroke(Util.inte(larguraPistaPixeis * 1.05),
						BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10f,
						new float[] { 10, 10, 10, 10, 10, 10, 10, 10, 10, 10,
								10, 10 }, 0);
			desenhaTintaPistaEZebra(g2d);
			desenhaPista(g2d);
			desenhaPistaBox(g2d);
			desenhaLargada(g2d);
			desenhaGrid(g2d);
			desenhaBoxes(g2d);
			g2d.setStroke(trilho);
			desenhaPiloto(g2d);
			if (!desenhouQualificacao) {
				desenhaQualificacao(g2d);
			}
			desenharSafetyCar(g2d);
			desenhaContadorVoltas(g2d);
			desenharFarois(g2d);
			desenharClima(g2d);
			desenhaInfoAdd(g2d);
			desenhaChuva(g2d);
			// if (limitesViewPort != null) {
			// limitesViewPort.width -= 100;
			// limitesViewPort.height -= 100;
			//
			// g2d.draw(limitesViewPort);
			// }
		}
	}

	public boolean isDesenhouQualificacao() {
		return desenhouQualificacao;
	}

	public void setDesenhouQualificacao(boolean desenhouQualificacao) {
		this.desenhouQualificacao = desenhouQualificacao;
	}

	private void desenhaPiloto(Graphics2D g2d) {
		Piloto pilotoSelecionado = gerenciadorVisual
				.obterPilotoSecionadoTabela(controleJogo.getPilotoSelecionado());

		for (int i = controleJogo.getPilotos().size() - 1; i > -1; i--) {
			Piloto piloto = (Piloto) controleJogo.getPilotos().get(i);
			if (piloto.getCarro().isRecolhido() || piloto.getNoAtual() == null) {
				continue;
			}
			desenhaCarro(g2d, piloto);
			g2d.setColor(piloto.getCarro().getCor1());
			g2d.fillOval(Util.inte((piloto.getCarX() - 2) * zoom), Util
					.inte((piloto.getCarY() - 2) * zoom), 8, 8);
			desenhaTipoPneu(piloto, g2d);
			if (piloto != pilotoSelecionado) {
				desenhaNomePilotoNaoSelecionado(piloto, g2d);
			}
		}

		if ((pilotoSelecionado != null)) {
			desenhaNomePilotoSelecionado(pilotoSelecionado, g2d);
			desenhaCarroSelecionado(pilotoSelecionado, g2d);
			desenhaProblemasCarroSelecionado(pilotoSelecionado, g2d);
		}

	}

	public Shape limitesViewPort() {
		JScrollPane scrollPane = gerenciadorVisual.getScrollPane();
		if (scrollPane == null) {
			return null;
		}
		Rectangle rectangle = scrollPane.getViewport().getBounds();
		// rectangle.width -= 100;
		// rectangle.height -= 100;
		rectangle.x = scrollPane.getViewport().getViewPosition().x;
		rectangle.y = scrollPane.getViewport().getViewPosition().y;
		return rectangle;
	}

	private void desenhaCarro(Graphics2D g2d, Piloto piloto) {
		if (zoom < 0.2) {
			return;
		}
		BufferedImage carroCima = piloto.obterCarroCima();
		if (carroCima == null) {
			return;
		}
		No noAtual = piloto.getNoAtual();
		Point p = noAtual.getPoint();
		if (!limitesViewPort
				.contains(new Point2D.Double(p.x * zoom, p.y * zoom))) {
			return;
		}

		g2d.setColor(Color.black);
		g2d.setStroke(trilho);
		List lista = controleJogo.getNosDaPista();
		if (piloto.getPtosBox() > 0) {
			lista = controleJogo.getNosDoBox();
		}

		int cont = noAtual.getIndex();

		int width = (int) (carroCima.getWidth());
		int height = (int) (carroCima.getHeight());
		int w2 = width / 2;
		int h2 = height / 2;
		int carx = p.x - w2;
		int cary = p.y - h2;

		int traz = cont - 44;
		int frente = cont + 44;

		if (traz < 0) {
			traz = (lista.size() - 1) + traz;
		}
		if (frente > (lista.size() - 1)) {
			frente = (frente - (lista.size() - 1)) - 1;
		}

		Point trazCar = ((No) lista.get(traz)).getPoint();
		Point frenteCar = ((No) lista.get(frente)).getPoint();
		double calculaAngulo = GeoUtil.calculaAngulo(frenteCar, trazCar, 0);
		Rectangle2D rectangle = new Rectangle2D.Double(
				(p.x - Carro.MEIA_LARGURA), (p.y - Carro.MEIA_ALTURA),
				Carro.LARGURA, Carro.ALTURA);
		Point p1 = GeoUtil.calculaPonto(calculaAngulo, Util
				.inte(Carro.ALTURA * 1.2), new Point(Util.inte(rectangle
				.getCenterX()), Util.inte(rectangle.getCenterY())));
		g2d.setColor(Color.black);
		Point p2 = GeoUtil.calculaPonto(calculaAngulo + 180, Util
				.inte(Carro.ALTURA * 1.2), new Point(Util.inte(rectangle
				.getCenterX()), Util.inte(rectangle.getCenterY())));
		if (piloto.getTracado() == 0) {
			carx = p.x - w2;
			cary = p.y - h2;
		}
		if (piloto.getTracado() == 1) {
			carx = Util.inte((p1.x - w2));
			cary = Util.inte((p1.y - h2));
		}
		if (piloto.getTracado() == 2) {
			carx = Util.inte((p2.x - w2));
			cary = Util.inte((p2.y - h2));
		}
		piloto.setCarX(carx);
		piloto.setCarY(cary);
		// if (calculaAngulo == piloto.getAnguloRotacaoCarro()
		// && zoom == piloto.getZoom()) {
		// g2d.drawImage(piloto.getUltimaRotacaoCarro(), Util.inte(carx), Util
		// .inte(cary), null);
		// return;
		// }
		// piloto.setAnguloRotacaoCarro(calculaAngulo);
		// piloto.setZoom(zoom);
		double rad = Math.toRadians((double) calculaAngulo);
		AffineTransform afZoom = new AffineTransform();
		AffineTransform afRotate = new AffineTransform();
		afZoom.setToScale(zoom, zoom);
		afRotate.setToRotation(rad, w2, h2);

		BufferedImage rotateBuffer = new BufferedImage(width, width,
				BufferedImage.TYPE_INT_ARGB);
		BufferedImage zoomBuffer = new BufferedImage(width, height,
				BufferedImage.TYPE_INT_ARGB);
		AffineTransformOp op = new AffineTransformOp(afRotate,
				AffineTransformOp.TYPE_BILINEAR);
		op.filter(carroCima, zoomBuffer);
		AffineTransformOp op2 = new AffineTransformOp(afZoom,
				AffineTransformOp.TYPE_BILINEAR);
		op2.filter(zoomBuffer, rotateBuffer);
		// piloto.setUltimaRotacaoCarro(rotateBuffer);
		g2d.drawImage(rotateBuffer, Util.inte(carx * zoom), Util.inte(cary
				* zoom), null);

		// DEbug
		// GeneralPath generalPath = new GeneralPath(rectangle);
		//
		// AffineTransform affineTransformRect =
		// AffineTransform.getScaleInstance(
		// zoom, zoom);
		// affineTransformRect.setToRotation(rad, rectangle.getCenterX(),
		// rectangle.getCenterY());
		// g2d.setColor(new Color(255, 0, 0, 140));
		piloto.obterArea(controleJogo);
		// g2d.draw(piloto.obterArea(controleJogo));

		// g2d.fillOval(Util.inte(frenteCar.x * zoom), Util.inte(frenteCar.y
		// * zoom), Util.inte(5 * zoom), Util.inte(5 * zoom));
		// g2d.fillOval(Util.inte(trazCar.x * zoom), Util.inte(trazCar.y *
		// zoom),
		// Util.inte(5 * zoom), Util.inte(5 * zoom));

	}

	private void desenhaBoxes(Graphics2D g2d) {
		for (int i = 0; i < 12; i++) {
			if (boxParada[i] == null) {
				break;
			}
			if (!limitesViewPort.intersects(boxParada[i].getBounds2D())) {
				continue;
			}
			g2d.setColor(Color.LIGHT_GRAY);
			g2d.fill(boxParada[i]);
			if (i >= controleJogo.getCarros().size()) {
				break;
			}
			Carro carro = (Carro) controleJogo.getCarrosBox().get(i);
			g2d.setColor(carro.getCor1());
			g2d.fill(boxCor1[i]);
			g2d.setColor(carro.getCor2());
			g2d.fill(boxCor2[i]);
		}
	}

	private void desenhaGrid(Graphics2D g2d) {
		for (int i = 0; i < 24; i++) {

			if (!limitesViewPort.intersects(grid[i].getBounds2D())) {
				continue;
			}
			g2d.setColor(Color.white);
			g2d.fill(grid[i]);
			g2d.setColor(Color.lightGray);
			g2d.fill(asfaltoGrid[i]);

		}

	}

	private void desenhaLargada(Graphics2D g2d) {
		No n1 = (No) circuito.getPistaFull().get(0);
		No n2 = (No) circuito.getPistaFull().get(20);
		Point p1 = new Point(Util.inte(n1.getPoint().x * zoom), Util.inte(n1
				.getPoint().y
				* zoom));
		Point p2 = new Point(Util.inte(n2.getPoint().x * zoom), Util.inte(n2
				.getPoint().y
				* zoom));
		double larguraZebra = (larguraPistaPixeis * 0.01);
		RoundRectangle2D rectangle = new RoundRectangle2D.Double(
				(p1.x - (larguraZebra / 2)), (p1.y - (larguraPistaPixeis / 2)),
				larguraZebra, larguraPistaPixeis, 5 * zoom, 5 * zoom);
		double calculaAngulo = GeoUtil.calculaAngulo(p1, p2, 0);
		double rad = Math.toRadians((double) calculaAngulo);
		GeneralPath generalPath = new GeneralPath(rectangle);
		AffineTransform affineTransformRect = AffineTransform.getScaleInstance(
				zoom, zoom);
		affineTransformRect.setToRotation(rad, rectangle.getCenterX(),
				rectangle.getCenterY());
		g2d.setColor(Color.white);
		g2d.fill(generalPath.createTransformedShape(affineTransformRect));

	}

	private void desenhaPistaBox(Graphics2D g2d) {
		g2d.setColor(Color.LIGHT_GRAY);
		g2d.setStroke(box);
		No oldNo = null;
		for (Iterator iter = circuito.getBoxKey().iterator(); iter.hasNext();) {
			No no = (No) iter.next();
			if (oldNo == null) {
				oldNo = no;
			} else {
				g2d.drawLine(Util.inte(oldNo.getX() * zoom), Util.inte(oldNo
						.getY()
						* zoom), Util.inte(no.getX() * zoom), Util.inte(no
						.getY()
						* zoom));

				oldNo = no;
			}
		}
		No noFinal = (No) circuito.getBoxKey().get(
				circuito.getBoxKey().size() - 1);

		g2d.drawLine(Util.inte(oldNo.getX() * zoom), Util.inte(oldNo.getY()
				* zoom), Util.inte(noFinal.getX() * zoom), Util.inte(noFinal
				.getY()
				* zoom));
	}

	private void desenhaTintaPistaEZebra(Graphics2D g2d) {
		g2d.setColor(Color.WHITE);
		g2d.setStroke(pistaTinta);

		No oldNo = null;
		int cont = 0;
		for (Iterator iter = circuito.getPistaKey().iterator(); iter.hasNext();) {
			No no = (No) iter.next();
			if (oldNo == null) {
				oldNo = no;
			} else {
				g2d.setColor(Color.WHITE);
				g2d.setStroke(pistaTinta);
				g2d.drawLine(Util.inte(oldNo.getX() * zoom), Util.inte(oldNo
						.getY()
						* zoom), Util.inte(no.getX() * zoom), Util.inte(no
						.getY()
						* zoom));
				if (No.CURVA_ALTA.equals(oldNo.getTipo())
						|| No.CURVA_BAIXA.equals(oldNo.getTipo())) {
					g2d.setColor(Color.RED);
					g2d.setStroke(zebra);
					g2d.drawLine(Util.inte(oldNo.getX() * zoom), Util
							.inte(oldNo.getY() * zoom), Util.inte(no.getX()
							* zoom), Util.inte(no.getY() * zoom));

				}
				oldNo = no;
			}
		}
		No noFinal = (No) circuito.getPistaKey().get(0);
		g2d.drawLine(Util.inte(oldNo.getX() * zoom), Util.inte(oldNo.getY()
				* zoom), Util.inte(noFinal.getX() * zoom), Util.inte(noFinal
				.getY()
				* zoom));
	}

	protected void atualizaVarZoom() {
		larguraPistaPixeis = Util.inte(176 * larguraPista * zoom);
		pista = new BasicStroke(larguraPistaPixeis, BasicStroke.CAP_ROUND,
				BasicStroke.JOIN_ROUND);
		pistaTinta = new BasicStroke(Util.inte(larguraPistaPixeis * 1.05),
				BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
		box = new BasicStroke(Util.inte(larguraPistaPixeis * .4),
				BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
		zebra = new BasicStroke(Util.inte(larguraPistaPixeis * 1.05),
				BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10f, new float[] {
						10, 10 }, 0);
		gerarGrid();
		gerarBoxes();

		// limitesViewPort = (Rectangle) limitesViewPort();
	}

	private void gerarBoxes() {
		int paradas = circuito.getParadaBoxIndex();
		for (int i = 0; i < 12; i++) {
			int iP = paradas + Util.inte(Carro.LARGURA * 2 * i) + Carro.LARGURA;
			No n1 = (No) circuito.getBoxFull().get(iP - Carro.MEIA_LARGURA);
			No nM = (No) circuito.getBoxFull().get(iP);
			No n2 = (No) circuito.getBoxFull().get(iP + Carro.MEIA_LARGURA);
			Point p1 = new Point(Util.inte(n1.getPoint().x * zoom), Util
					.inte(n1.getPoint().y * zoom));
			Point pm = new Point(Util.inte(nM.getPoint().x * zoom), Util
					.inte(nM.getPoint().y * zoom));
			Point p2 = new Point(Util.inte(n2.getPoint().x * zoom), Util
					.inte(n2.getPoint().y * zoom));
			double calculaAngulo = GeoUtil.calculaAngulo(p1, p2, 0);
			Rectangle2D rectangle = new Rectangle2D.Double(
					(pm.x - (Carro.MEIA_LARGURA)),
					(pm.y - (Carro.MEIA_ALTURA)), (Carro.LARGURA),
					(Carro.ALTURA));

			Point cima = GeoUtil.calculaPonto(calculaAngulo, Util
					.inte(Carro.ALTURA * 1.2 * zoom), new Point(Util
					.inte(rectangle.getCenterX()), Util.inte(rectangle
					.getCenterY())));
			Point baixo = GeoUtil.calculaPonto(calculaAngulo + 180, Util
					.inte(Carro.ALTURA * 1.2 * zoom), new Point(Util
					.inte(rectangle.getCenterX()), Util.inte(rectangle
					.getCenterY())));
			Point cimaBoxC1 = GeoUtil.calculaPonto(calculaAngulo, Util
					.inte((Carro.ALTURA) * 4 * zoom), new Point(Util
					.inte(rectangle.getCenterX()), Util.inte(rectangle
					.getCenterY())));
			Point baixoBoxC1 = GeoUtil.calculaPonto(calculaAngulo + 180, Util
					.inte((Carro.ALTURA) * 3 * zoom), new Point(Util
					.inte(rectangle.getCenterX()), Util.inte(rectangle
					.getCenterY())));
			Point cimaBoxC2 = GeoUtil.calculaPonto(calculaAngulo, Util
					.inte((Carro.ALTURA) * 4 * zoom), new Point(Util
					.inte(rectangle.getCenterX()), Util.inte(rectangle
					.getCenterY())));
			Point baixoBoxC2 = GeoUtil.calculaPonto(calculaAngulo + 180, Util
					.inte((Carro.ALTURA) * 3 * zoom), new Point(Util
					.inte(rectangle.getCenterX()), Util.inte(rectangle
					.getCenterY())));

			RoundRectangle2D retC1 = null;
			RoundRectangle2D retC2 = null;
			if (circuito.getLadoBox() == 1) {
				rectangle = new Rectangle2D.Double(
						(cima.x - (Carro.MEIA_LARGURA * zoom)),
						(cima.y - (Carro.MEIA_ALTURA * zoom)),
						(Carro.LARGURA * zoom), (Carro.ALTURA * zoom));
				retC1 = new RoundRectangle2D.Double(
						(cimaBoxC1.x - (Carro.LARGURA * zoom)),
						(cimaBoxC1.y - (Carro.ALTURA * zoom)),
						(Carro.LARGURA * 2 * zoom), (Carro.ALTURA * 3 * zoom),
						5, 5);
				retC2 = new RoundRectangle2D.Double(
						(cimaBoxC2.x - (Carro.MEIA_LARGURA * zoom)),
						(cimaBoxC2.y + (Carro.MEIA_ALTURA * zoom)),
						(Carro.LARGURA * zoom), (Carro.ALTURA * zoom), 5, 5);
			} else if (circuito.getLadoBox() == 2) {
				rectangle = new Rectangle2D.Double(
						(baixo.x - (Carro.MEIA_LARGURA * zoom)),
						(baixo.y - (Carro.MEIA_ALTURA * zoom)),
						(Carro.LARGURA * zoom), (Carro.ALTURA * zoom));
				retC1 = new RoundRectangle2D.Double(
						(baixoBoxC1.x - (Carro.LARGURA * zoom)),
						(baixoBoxC1.y - (Carro.ALTURA * zoom)),
						(Carro.LARGURA * 2 * zoom), (Carro.ALTURA * 3 * zoom),
						5, 5);
				retC2 = new RoundRectangle2D.Double(
						(baixoBoxC2.x - (Carro.MEIA_LARGURA * zoom)),
						(baixoBoxC2.y + (Carro.MEIA_ALTURA * zoom)),
						(Carro.LARGURA * zoom), (Carro.ALTURA * zoom), 5, 5);
			}

			GeneralPath generalPath = new GeneralPath(rectangle);

			AffineTransform affineTransformRect = AffineTransform
					.getScaleInstance(zoom, zoom);
			double rad = Math.toRadians((double) calculaAngulo);
			affineTransformRect.setToRotation(rad, rectangle.getCenterX(),
					rectangle.getCenterY());
			boxParada[i] = generalPath
					.createTransformedShape(affineTransformRect);
			generalPath = new GeneralPath(retC1);
			affineTransformRect.setToRotation(rad, retC1.getCenterX(), retC1
					.getCenterY());
			boxCor1[i] = generalPath
					.createTransformedShape(affineTransformRect);

			generalPath = new GeneralPath(retC2);
			affineTransformRect.setToRotation(rad, retC2.getCenterX(), retC2
					.getCenterY());
			boxCor2[i] = generalPath
					.createTransformedShape(affineTransformRect);

		}

	}

	private void gerarGrid() {
		for (int i = 0; i < 24; i++) {
			int iP = 50 + Util.inte(Carro.LARGURA * i);
			No n1 = (No) circuito.getPistaFull().get(
					circuito.getPistaFull().size() - iP - Carro.MEIA_LARGURA);
			No nM = (No) circuito.getPistaFull().get(
					circuito.getPistaFull().size() - iP);
			No n2 = (No) circuito.getPistaFull().get(
					circuito.getPistaFull().size() - iP + Carro.MEIA_LARGURA);
			Point p1 = new Point(Util.inte(n1.getPoint().x * zoom), Util
					.inte(n1.getPoint().y * zoom));
			Point pm = new Point(Util.inte(nM.getPoint().x * zoom), Util
					.inte(nM.getPoint().y * zoom));
			Point p2 = new Point(Util.inte(n2.getPoint().x * zoom), Util
					.inte(n2.getPoint().y * zoom));
			double calculaAngulo = GeoUtil.calculaAngulo(p1, p2, 0);
			Rectangle2D rectangle = new Rectangle2D.Double(
					(pm.x - (Carro.MEIA_LARGURA)),
					(pm.y - (Carro.MEIA_ALTURA)), (Carro.LARGURA),
					(Carro.ALTURA));

			Point cima = GeoUtil.calculaPonto(calculaAngulo, Util
					.inte(Carro.ALTURA * 1.2 * zoom), new Point(Util
					.inte(rectangle.getCenterX()), Util.inte(rectangle
					.getCenterY())));
			Point baixo = GeoUtil.calculaPonto(calculaAngulo + 180, Util
					.inte(Carro.ALTURA * 1.2 * zoom), new Point(Util
					.inte(rectangle.getCenterX()), Util.inte(rectangle
					.getCenterY())));
			if (i % 2 == 0) {
				rectangle = new Rectangle2D.Double(
						(cima.x - (Carro.MEIA_LARGURA * zoom)),
						(cima.y - (Carro.MEIA_ALTURA * zoom)),
						(Carro.LARGURA * zoom), (Carro.ALTURA * zoom));
			} else {
				rectangle = new Rectangle2D.Double(
						(baixo.x - (Carro.MEIA_LARGURA * zoom)),
						(baixo.y - (Carro.MEIA_ALTURA * zoom)),
						(Carro.LARGURA * zoom), (Carro.ALTURA * zoom));
			}

			GeneralPath generalPath = new GeneralPath(rectangle);

			AffineTransform affineTransformRect = AffineTransform
					.getScaleInstance(zoom, zoom);
			double rad = Math.toRadians((double) calculaAngulo);
			affineTransformRect.setToRotation(rad, rectangle.getCenterX(),
					rectangle.getCenterY());
			grid[i] = generalPath.createTransformedShape(affineTransformRect);

			iP += 5;
			n1 = (No) circuito.getPistaFull().get(
					circuito.getPistaFull().size() - iP - Carro.MEIA_LARGURA);
			nM = (No) circuito.getPistaFull().get(
					circuito.getPistaFull().size() - iP);
			n2 = (No) circuito.getPistaFull().get(
					circuito.getPistaFull().size() - iP + Carro.MEIA_LARGURA);
			p1 = new Point(Util.inte(n1.getPoint().x * zoom), Util.inte(n1
					.getPoint().y
					* zoom));
			pm = new Point(Util.inte(nM.getPoint().x * zoom), Util.inte(nM
					.getPoint().y
					* zoom));
			p2 = new Point(Util.inte(n2.getPoint().x * zoom), Util.inte(n2
					.getPoint().y
					* zoom));
			calculaAngulo = GeoUtil.calculaAngulo(p1, p2, 0);
			rectangle = new Rectangle2D.Double((pm.x - (Carro.MEIA_LARGURA)),
					(pm.y - (Carro.MEIA_ALTURA)), (Carro.LARGURA),
					(Carro.ALTURA));

			cima = GeoUtil.calculaPonto(calculaAngulo, Util.inte(Carro.ALTURA
					* 1.2 * zoom), new Point(Util.inte(rectangle.getCenterX()),
					Util.inte(rectangle.getCenterY())));
			baixo = GeoUtil.calculaPonto(calculaAngulo + 180, Util
					.inte(Carro.ALTURA * 1.2 * zoom), new Point(Util
					.inte(rectangle.getCenterX()), Util.inte(rectangle
					.getCenterY())));
			if (i % 2 == 0) {
				rectangle = new Rectangle2D.Double(
						(cima.x - (Carro.MEIA_LARGURA * zoom)),
						(cima.y - (Carro.MEIA_ALTURA * zoom)),
						(Carro.LARGURA * zoom), (Carro.ALTURA * zoom));
			} else {
				rectangle = new Rectangle2D.Double(
						(baixo.x - (Carro.MEIA_LARGURA * zoom)),
						(baixo.y - (Carro.MEIA_ALTURA * zoom)),
						(Carro.LARGURA * zoom), (Carro.ALTURA * zoom));
			}

			generalPath = new GeneralPath(rectangle);

			affineTransformRect = AffineTransform.getScaleInstance(zoom, zoom);
			rad = Math.toRadians((double) calculaAngulo);
			affineTransformRect.setToRotation(rad, rectangle.getCenterX(),
					rectangle.getCenterY());
			asfaltoGrid[i] = generalPath
					.createTransformedShape(affineTransformRect);
		}

	}

	public void centralizarPontoDireto(Point pin) {
		final JScrollPane scrollPane = gerenciadorVisual.getScrollPane();
		final Point p = new Point((int) (pin.x * zoom)
				- (scrollPane.getViewport().getWidth() / 2),
				(int) (pin.y * zoom)
						- (scrollPane.getViewport().getHeight() / 2));
		if (p.x < 0) {
			p.x = 1;
		}
		double maxX = ((getWidth() * zoom) - scrollPane.getViewport()
				.getWidth());
		if (p.x > maxX) {
			p.x = Util.inte(maxX) - 1;
		}
		if (p.y < 0) {
			p.y = 1;
		}
		double maxY = ((getHeight() * zoom) - (scrollPane.getViewport()
				.getHeight()));
		if (p.y > maxY) {
			p.y = Util.inte(maxY) - 1;
		}
		Point oldp = scrollPane.getViewport().getViewPosition();
		if (oldp.equals(p)) {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					repaint();
				}
			});
		} else {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					scrollPane.getViewport().setViewPosition(p);
				}
			});
		}
	}

	public void centralizarPonto(Point pin) {
		final JScrollPane scrollPane = gerenciadorVisual.getScrollPane();
		Rectangle rectangle = (Rectangle) limitesViewPort();
		if (rectangle == null)
			return;
		Point ori = new Point((int) rectangle.getCenterX() - 25,
				(int) rectangle.getCenterY() - 25);
		Point des = new Point((int) (pin.x * zoom), (int) (pin.y * zoom));
		final List reta = GeoUtil.drawBresenhamLine(ori, des);
		Point p = des;
		if (!reta.isEmpty()) {
			int cont = reta.size() / Util.inte(3 / zoom);
			for (int i = cont; i < reta.size(); i += cont) {
				p = (Point) reta.get(i);
				if (rectangle.contains(p)) {
					p.x -= ((rectangle.width - 50) / 2);
					p.y -= ((rectangle.height - 50) / 2);
					break;
				}
			}
		}
		if (p.x < 0) {
			p.x = 1;
		}
		if (p.y < 0) {
			p.y = 1;
		}
		int largMax = (int) ((getWidth()) - scrollPane.getViewport().getWidth());
		if (p.x > largMax) {
			p.x = largMax - 1;
		}
		int altMax = (int) ((getHeight()) - (scrollPane.getViewport()
				.getHeight()));
		if (p.y > altMax) {
			p.y = altMax - 1;
		}
		final Point newP = p;
		Point oldp = scrollPane.getViewport().getViewPosition();
		int dst = (int) GeoUtil.distaciaEntrePontos(oldp.x, oldp.y, p.x, p.y);
		if (dst == 0) {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					repaint();
				}
			});
		} else {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					repaint();
					scrollPane.getViewport().setViewPosition(newP);

				}
			});

		}
	}

	private void desenhaPista(Graphics2D g2d) {
		No oldNo = null;
		g2d.setColor(Color.LIGHT_GRAY);
		g2d.setStroke(pista);
		for (Iterator iter = circuito.getPistaKey().iterator(); iter.hasNext();) {
			No no = (No) iter.next();
			if (oldNo == null) {
				oldNo = no;
			} else {
				g2d.drawLine(Util.inte(oldNo.getX() * zoom), Util.inte(oldNo
						.getY()
						* zoom), Util.inte(no.getX() * zoom), Util.inte(no
						.getY()
						* zoom));

				oldNo = no;
			}
		}

		No noFinal = (No) circuito.getPistaKey().get(0);
		g2d.drawLine(Util.inte(oldNo.getX() * zoom), Util.inte(oldNo.getY()
				* zoom), Util.inte(noFinal.getX() * zoom), Util.inte(noFinal
				.getY()
				* zoom));
	}

	private void desenhaChuva(Graphics2D g2d) {
		if (!controleJogo.isChovendo())
			return;
		Point p1 = new Point(0, 0);
		Point p2 = new Point(0, 0);
		for (int i = 0; i < limitesViewPort.getWidth(); i += 20) {
			for (int j = 0; j < limitesViewPort.getHeight(); j += 20) {
				if (Math.random() > .8) {
					g2d.setColor(Color.DARK_GRAY);
					p1 = new Point(i + 10, j + 10);
					p2 = new Point(i + 15, j + 20);
					// if (!(limitesViewPort.contains(p1) && limitesViewPort
					// .contains(p2)))
					// continue;
					g2d.drawLine(p1.x + limitesViewPort.x, p1.y
							+ limitesViewPort.y, p2.x + limitesViewPort.x, p2.y
							+ limitesViewPort.y);
				}
			}
		}

	}

	private void desenhaFaisca(Graphics2D g2d, Point p) {
		if (p == null) {
			return;
		}
		// p = new Point(150, 150);
		// g2d.drawOval(p.x, p.y, 2, 2);
		Color color = g2d.getColor();
		for (int i = 0; i < 7; i++) {
			if (Math.random() > .5) {
				g2d.setColor(Color.YELLOW);
				int valx = Util.intervalo(5, 15);
				int valy = Util.intervalo(-5, 15);
				g2d.drawLine(p.x + valx, p.y + valy, p.x + i * valx, p.y + valy
						- Util.intervalo(10, 20));
			}
		}
		g2d.setColor(color);
	}

	private void desenhaInfoAdd(Graphics2D g2d) {
		if (!desenhaInfo) {
			return;
		}
		Piloto pilotoSelecionado = gerenciadorVisual
				.obterPilotoSecionadoTabela(controleJogo.getPilotoSelecionado());
		if (pilotoSelecionado != null) {
			g2d.setColor(blu);
			g2d.fillRoundRect(
					limitesViewPort.x + (limitesViewPort.width - 110),
					limitesViewPort.y + 2, 105, 240, 10, 10);
			g2d.setColor(Color.black);
			int ptoOri = limitesViewPort.x + limitesViewPort.width - 100;
			int yBase = limitesViewPort.y;
			yBase += 15;
			g2d.drawString(
					Lang.msg(pilotoSelecionado.getCarro().getTipoPneu()),
					ptoOri, yBase);
			yBase += 15;
			g2d.drawString(Lang.msg(pilotoSelecionado.getCarro().getAsa()),
					ptoOri, yBase);
			yBase += 15;
			g2d.drawString(Lang.msg("068")
					+ pilotoSelecionado.getQtdeParadasBox(), ptoOri, yBase);
			if (pilotoSelecionado.isBox()) {
				g2d.setColor(red);
			}
			yBase += 15;
			g2d.drawString(Lang.msg("069")
					+ (pilotoSelecionado.isBox() ? Lang.msg("SIM") : Lang
							.msg("NAO")), ptoOri, yBase);
			String plider = "";
			if (pilotoSelecionado.getPosicao() == 1) {
				plider = Lang.msg("Lider");
				g2d.setColor(Color.BLUE);
			} else {
				controleJogo.calculaSegundosParaLider(pilotoSelecionado);
				plider = pilotoSelecionado.getSegundosParaLider();
				g2d.setColor(red);
			}

			yBase += 15;

			g2d.drawString(Lang.msg("070") + plider, limitesViewPort.x
					+ (limitesViewPort.width - 100), yBase);
			yBase += 15;
			if (Carro.GIRO_MIN_VAL == pilotoSelecionado.getCarro().getGiro()
					&& qtdeLuzesAcesas <= 0) {
				g2d.setColor(gre);
				g2d.fillRoundRect(ptoOri - 5, yBase - 12, 90, 16, 10, 10);
				g2d.setColor(Color.black);
			} else {
				g2d.setColor(Color.black);
			}
			g2d.drawString(Lang.msg("071"), ptoOri, yBase);
			yBase += 15;
			if (Carro.GIRO_NOR_VAL == pilotoSelecionado.getCarro().getGiro()
					&& qtdeLuzesAcesas <= 0) {
				g2d.setColor(yel);
				g2d.fillRoundRect(ptoOri - 5, yBase - 12, 90, 16, 10, 10);
				g2d.setColor(Color.black);
			} else {
				g2d.setColor(Color.black);
			}
			g2d.drawString(Lang.msg("072"), ptoOri, yBase);
			yBase += 15;
			if (Carro.GIRO_MAX_VAL == pilotoSelecionado.getCarro().getGiro()
					&& qtdeLuzesAcesas <= 0) {
				g2d.setColor(red);
				g2d.fillRoundRect(ptoOri - 5, yBase - 12, 90, 16, 10, 10);
				g2d.setColor(Color.black);
			} else {
				g2d.setColor(Color.black);
			}
			g2d.drawString(Lang.msg("073"), ptoOri, yBase);
			g2d.setColor(Color.black);
			yBase += 15;
			g2d.drawString(Lang.msg("074"), ptoOri, yBase);
			yBase += 15;
			if (Piloto.LENTO.equals(pilotoSelecionado.getModoPilotagem())
					&& qtdeLuzesAcesas <= 0) {
				g2d.setColor(gre);
				g2d.fillRoundRect(ptoOri - 5, yBase - 12, 90, 16, 10, 10);
				g2d.setColor(Color.black);
			} else {
				g2d.setColor(Color.black);
			}
			g2d.drawString(Lang.msg("075"), ptoOri, yBase);
			yBase += 15;
			if (Piloto.NORMAL.equals(pilotoSelecionado.getModoPilotagem())
					&& qtdeLuzesAcesas <= 0) {
				g2d.setColor(yel);
				g2d.fillRoundRect(ptoOri - 5, yBase - 12, 90, 16, 10, 10);
				g2d.setColor(Color.black);
			} else {
				g2d.setColor(Color.black);
			}
			g2d.drawString(Lang.msg("076"), ptoOri, yBase);
			yBase += 15;
			if (Piloto.AGRESSIVO.equals(pilotoSelecionado.getModoPilotagem())
					&& qtdeLuzesAcesas <= 0) {
				g2d.setColor(red);
				g2d.fillRoundRect(ptoOri - 5, yBase - 12, 90, 16, 10, 10);
				g2d.setColor(Color.black);
			} else {
				g2d.setColor(Color.black);
			}
			g2d.drawString(Lang.msg("077"), ptoOri, yBase);
			yBase += 15;
			if (gerenciadorVisual.isProgamaBox()) {
				g2d.setColor(Color.blue);
			} else {
				g2d.setColor(Color.black);
			}
			g2d.setColor(PainelTabelaPosicoes.jogador);
			g2d.fillRoundRect(ptoOri - 5, yBase - 12, 100, 16, 10, 10);
			g2d.setColor(Color.black);
			g2d.drawString(Lang.msg("301", new String[] { pilotoSelecionado
					.getNome() }), ptoOri, yBase);
			yBase += 15;
			g2d.drawString(Lang.msg("265"), ptoOri, yBase);
			yBase += 15;
			g2d.setColor(Color.black);
			g2d.drawString(Lang.msg("078"), ptoOri, yBase);
			yBase += 15;
			g2d.drawString(Lang.msg("220"), ptoOri, yBase);

			if ((pilotoSelecionado.getNumeroVolta() > 0)) {
				Volta voltaPiloto = controleJogo
						.obterMelhorVolta(pilotoSelecionado);

				if (voltaPiloto != null) {
					g2d.setColor(Color.BLUE);
					yBase += 15;
					g2d.drawString(Lang.msg("079")
							+ voltaPiloto.obterTempoVoltaFormatado(), ptoOri,
							yBase);
				}
				g2d.setColor(Color.black);
				yBase += 15;
				g2d.drawString(Lang.msg("080"), ptoOri, yBase);
				yBase += 15;
				int contAlt = yBase;
				int contVolta = 1;
				List voltas = pilotoSelecionado.getVoltas();
				Color color = new Color(1, 1, 1);
				for (int i = voltas.size() - 1; i > -1; i--) {
					Volta volta = (Volta) voltas.get(i);
					if (volta.obterTempoVolta() == 0) {
						continue;
					}
					g2d.setColor(color);
					g2d.drawString(volta.obterTempoVoltaFormatado(), ptoOri,
							contAlt);
					contAlt += 15;
					contVolta++;
					color = new Color(contVolta * 30, contVolta * 30,
							contVolta * 30);
					if (contVolta > 5) {
						break;
					}
				}
			}
		}
	}

	private void desenhaProblemasCarroSelecionado(Piloto pilotoSelecionado,
			Graphics2D g2d) {
		if (qtdeLuzesAcesas > 0) {
			return;
		}
		String dano = pilotoSelecionado.getCarro().getDanificado();
		int pneus = pilotoSelecionado.getCarro().porcentagemDesgastePeneus();
		int porcentComb = pilotoSelecionado.getCarro().porcentagemCombustivel();
		int motor = pilotoSelecionado.getCarro().porcentagemDesgasteMotor();
		if (pilotoSelecionado.getStress() > 85) {
			g2d.drawImage(helmetPiloto, limitesViewPort.x
					+ pointDesenhaHelmet.x + (Math.random() > 0.5 ? 1 : -1),
					limitesViewPort.y + pointDesenhaHelmet.y
							+ (Math.random() > 0.5 ? -1 : 0), null);
		}
		if ((dano == null || "".equals(dano)) && motor > 10 && porcentComb > 10
				&& pneus > 10)
			return;

		g2d.drawImage(carroimgDano, limitesViewPort.x + 5,
				limitesViewPort.y + 10, null);
		if (Math.random() > .5) {
			return;
		}
		if (porcentComb <= 10) {
			g2d.drawImage(fuel.getImage(), limitesViewPort.x + 5,
					limitesViewPort.y + 240, null);
		}

		if (Carro.PERDEU_AEREOFOLIO.equals(pilotoSelecionado.getCarro()
				.getDanificado())) {

			g2d.setColor(Color.red);
			// bico
			g2d.fillOval(limitesViewPort.x + 8, limitesViewPort.y + 26, 15, 15);
		}
		if (Carro.PNEU_FURADO.equals(pilotoSelecionado.getCarro()
				.getDanificado())) {
			g2d.setColor(Color.red);
			// Roda diantera
			g2d
					.fillOval(limitesViewPort.x + 28, limitesViewPort.y + 24,
							18, 18);
			// Roda trazeira
			g2d.fillOval(limitesViewPort.x + 132, limitesViewPort.y + 24, 18,
					18);
		} else if (pneus <= 10) {
			g2d.setColor(yel);
			// Roda diantera
			g2d
					.fillOval(limitesViewPort.x + 28, limitesViewPort.y + 24,
							18, 18);
			// Roda trazeira
			g2d.fillOval(limitesViewPort.x + 132, limitesViewPort.y + 24, 18,
					18);
		}
		if (Carro.EXPLODIU_MOTOR.equals(pilotoSelecionado.getCarro()
				.getDanificado())) {
			g2d.setColor(Color.red);
			// motor
			g2d
					.fillOval(limitesViewPort.x + 98, limitesViewPort.y + 12,
							15, 15);
		} else if (motor <= 10) {
			g2d.setColor(yel);
			g2d
					.fillOval(limitesViewPort.x + 98, limitesViewPort.y + 12,
							15, 15);
		}
		if (Carro.BATEU_FORTE.equals(pilotoSelecionado.getCarro()
				.getDanificado())) {
			g2d.setColor(Color.red);
			// motor
			g2d.fillRoundRect(limitesViewPort.x + 15, limitesViewPort.y + 18,
					135, 20, 15, 15);
		}

	}

	private void desenhaContadorVoltas(Graphics2D g2d) {
		g2d.setColor(luzApagada);
		g2d.fillRoundRect(limitesViewPort.x + (limitesViewPort.width / 2),
				limitesViewPort.y + 10, 40, 20, 15, 15);
		g2d.setColor(Color.BLACK);
		g2d.drawString(controleJogo.getNumVoltaAtual() + "/"
				+ controleJogo.totalVoltasCorrida(), limitesViewPort.x
				+ (limitesViewPort.width / 2) + 6, limitesViewPort.y + 24);
	}

	private void desenhaQualificacao(Graphics2D g2d) {
		if (pilotQualificacao == null) {
			return;
		}
		if (pointQualificacao == null) {
			return;
		}
		BufferedImage carroimg = CarregadorRecursos
				.carregaImgCarro(pilotQualificacao.getCarro().getImg());
		g2d.drawImage(carroimg, null, pointQualificacao.x, pointQualificacao.y);
		int newY = limitesViewPort.y;
		synchronized (mapDesenharQualificacao) {

			for (Iterator iter = mapDesenharQualificacao.keySet().iterator(); iter
					.hasNext();) {
				Piloto piloto = (Piloto) iter.next();
				Point point = (Point) mapDesenharQualificacao.get(piloto);
				carroimg = CarregadorRecursos
						.carregaBufferedImageTranspareciaBranca(piloto
								.getCarro().getImg());
				newY = carroimg.getHeight() > 36 ? point.y
						- (carroimg.getHeight() - 36) : point.y;
				g2d.drawImage(carroimg, null, limitesViewPort.x + point.x,
						limitesViewPort.y + newY);
				String txt = piloto.getNome()
						+ " - "
						+ ControleEstatisticas.formatarTempo(piloto
								.getCiclosVoltaQualificacao(), controleJogo
								.getTempoCiclo());

				int maior = txt.length();

				Color c2 = piloto.getCarro().getCor2();
				if (c2 != null) {
					c2 = c2.brighter();
					g2d.setColor(new Color(c2.getRed(), c2.getGreen(), c2
							.getBlue(), 170));
				}
				Point pt = null;
				if (piloto.getPosicao() % 2 == 0) {
					pt = new Point(point.x + 120, point.y + 20);

				} else {
					pt = new Point(point.x - 120, point.y + 20);
				}
				g2d.fillRoundRect(limitesViewPort.x + pt.x - 10,
						limitesViewPort.y + pt.y - 15, maior * 7, 20, 15, 15);

				int valor = (c2.getRed() + c2.getGreen() + c2.getBlue()) / 2;
				if (valor > 200) {
					g2d.setColor(Color.BLACK);
				} else {
					g2d.setColor(Color.WHITE);
				}
				g2d.drawString(txt, limitesViewPort.x + pt.x, limitesViewPort.y
						+ +pt.y);
			}
		}
	}

	private void desenhaCarroSelecionado(Piloto psel, Graphics2D g2d) {
		if (!desenhaInfo) {
			return;
		}
		BufferedImage carroimg = null;
		int carSelX = limitesViewPort.x;
		int carSelY = limitesViewPort.y + limitesViewPort.height - 35;
		int bounce = 0;
		int newY = 0;
		Carro carroFrente = controleJogo.obterCarroNaFrente(psel);
		if (carroFrente != null) {
			carroimg = CarregadorRecursos.carregaImgCarro(carroFrente.getImg());
			carSelX += carroimg.getWidth() / 2;

			bounce = calculaBounce(carroFrente);
			if (Math.random() > 0.5) {
				carSelX += bounce;
			} else {
				carSelX -= bounce;
			}
			double diff = controleJogo.calculaSegundosParaProximoDouble(psel);
			int dstX = limitesViewPort.x + (limitesViewPort.width / 4);
			int dstY = carSelY + 20;
			if (diff >= 3) {
				g2d.setColor(gre);
			} else if (diff < 3 && diff > 1) {
				g2d.setColor(yel);
			} else if (diff <= 1) {
				g2d.setColor(red);
			}
			int halfCarWidth = carroimg.getWidth() / 3;
			if (diff > 3) {
				carSelX += (30 - halfCarWidth * 2);
				dstX += 25;
			} else if (diff < 3 && diff > 1) {
				carSelX += (50 - halfCarWidth);
				dstX += 45;
			} else if (diff <= 1 && diff > .5) {
				carSelX += (90 - halfCarWidth);
				dstX += 75;
			} else if (diff <= .5) {
				carSelX += (120 - halfCarWidth);
				dstX += 90;
			}
			newY = carroimg.getHeight() > 36 ? carSelY
					- (carroimg.getHeight() - 36) : carSelY;
			if (!carroFrente.getPiloto().isDesqualificado()
					&& carroFrente.getPiloto().isAgressivo()
					&& carroFrente.getGiro() == Carro.GIRO_MAX_VAL) {
				desenhaFaisca(g2d, new Point(
						carSelX + carroimg.getWidth() - 10, newY
								+ carroimg.getHeight() / 2));
			}
			g2d.drawImage(carroimg, null, carSelX, newY);

			g2d.fillRoundRect(dstX - 2, dstY - 12, 60, 15, 10, 10);
			if (diff >= 3) {
				g2d.setColor(Color.BLACK);
			} else if (diff < 3 && diff > 1) {
				g2d.setColor(Color.BLACK);
			} else if (diff <= 1) {
				g2d.setColor(Color.WHITE);
			}
			String val = controleJogo.calculaSegundosParaProximo(psel);
			if (val != null) {
				g2d.drawString("  " + val, dstX, dstY);
			}

		}
		carroimg = CarregadorRecursos.carregaImgCarro(psel.getCarro().getImg());
		carSelX = limitesViewPort.x + (limitesViewPort.width / 2)
				- (carroimg.getWidth() / 2);
		carSelY = limitesViewPort.y + limitesViewPort.height - 35;
		bounce = calculaBounce(psel.getCarro());

		if (Math.random() > 0.5) {
			carSelX += bounce;
		} else {
			carSelX -= bounce;
		}
		newY = carroimg.getHeight() > 36 ? carSelY
				- (carroimg.getHeight() - 36) : carSelY;
		if (!psel.isDesqualificado()
				&& psel.getCarro().getPiloto().isAgressivo()
				&& psel.getCarro().getGiro() == Carro.GIRO_MAX_VAL) {
			desenhaFaisca(g2d, new Point(carSelX + carroimg.getWidth() - 10,
					newY + carroimg.getHeight() / 2));
		}
		g2d.drawImage(carroimg, null, carSelX, newY);

		Carro carroAtraz = controleJogo.obterCarroAtraz(psel);
		if (carroAtraz != null) {
			carroimg = CarregadorRecursos.carregaImgCarro(carroAtraz.getImg());
			carSelX = limitesViewPort.x + limitesViewPort.width
					+ -carroimg.getWidth() - carroimg.getWidth() / 2;
			bounce = calculaBounce(carroAtraz);
			if (Math.random() > 0.5) {
				carSelX += bounce;
			} else {
				carSelX -= bounce;
			}
			int dstX = limitesViewPort.x + limitesViewPort.width
					+ -(limitesViewPort.width / 3);
			int dstY = carSelY + 20;
			double diff = controleJogo
					.calculaSegundosParaProximoDouble(carroAtraz.getPiloto());

			if (diff >= 3) {
				g2d.setColor(gre);
			} else if (diff < 3 && diff > 1) {
				g2d.setColor(yel);
			} else if (diff <= 1) {
				g2d.setColor(red);
			}
			int halfCarWidth = carroimg.getWidth() / 3;
			if (diff >= 3) {
				carSelX -= (30 - halfCarWidth * 2);
				dstX -= 20;
			} else if (diff < 3 && diff > 1) {
				carSelX -= (50 - halfCarWidth);
				dstX -= 40;
			} else if (diff <= 1 && diff > .5) {
				carSelX -= (70 - halfCarWidth);
				dstX -= 60;
			} else if (diff <= .5) {
				carSelX -= (110 - halfCarWidth);
				dstX -= 70;
			}
			newY = carroimg.getHeight() > 36 ? carSelY
					- (carroimg.getHeight() - 36) : carSelY;
			if (!carroAtraz.getPiloto().isDesqualificado()
					&& carroAtraz.getPiloto().isAgressivo()
					&& carroAtraz.getGiro() == Carro.GIRO_MAX_VAL) {
				desenhaFaisca(g2d, new Point(
						carSelX + carroimg.getWidth() - 10, newY
								+ carroimg.getHeight() / 2));
			}
			g2d.drawImage(carroimg, null, carSelX, newY);

			g2d.fillRoundRect(dstX - 2, dstY - 12, 60, 15, 10, 10);
			if (diff >= 3) {
				g2d.setColor(Color.BLACK);
			} else if (diff < 3 && diff > 1) {
				g2d.setColor(Color.BLACK);
			} else if (diff <= 1) {
				g2d.setColor(Color.WHITE);
			}
			String val = controleJogo.calculaSegundosParaProximo(carroAtraz
					.getPiloto());
			if (val != null) {
				g2d.drawString("  " + val, dstX, dstY);
			}
		}

	}

	private int calculaBounce(Carro carro) {
		if (carro.getPiloto().isDesqualificado()) {
			return 0;
		}
		if (qtdeLuzesAcesas > 0 || carro.getPiloto().isBox()) {
			return Math.random() > .7 ? 1 : 0;
		} else if (carro.getPiloto().isAgressivo() == false) {
			return Math.random() > .5 ? 1 : 0;
		} else if (carro.getPiloto().isAgressivo() == true
				&& carro.getGiro() != Carro.GIRO_MAX_VAL) {
			return Math.random() > .5 ? 2 : 1;
		} else if (carro.getPiloto().isAgressivo() == true
				&& carro.getGiro() == Carro.GIRO_MAX_VAL) {
			return Math.random() > .5 ? 3 : 2;
		}
		return 0;
	}

	private void desenharSafetyCar(Graphics2D g2d) {
		int scx, scy;
		if (controleJogo.isSafetyCarNaPista()) {
			SafetyCar safetyCar = controleJogo.getSafetyCar();

			No noAtual = safetyCar.getNoAtual();
			Point p = noAtual.getPoint();
			if (!limitesViewPort.contains(new Point2D.Double(p.x * zoom, p.y
					* zoom))) {
				return;
			}

			g2d.setColor(Color.black);
			g2d.setStroke(trilho);
			List lista = controleJogo.getNosDaPista();

			int cont = noAtual.getIndex();

			int width = (int) (scima.getWidth());
			int height = (int) (scima.getHeight());
			int w2 = width / 2;
			int h2 = height / 2;
			int carx = p.x - w2;
			int cary = p.y - h2;

			int traz = cont - 44;
			int frente = cont + 44;

			if (traz < 0) {
				traz = (lista.size() - 1) + traz;
			}
			if (frente > (lista.size() - 1)) {
				frente = (frente - (lista.size() - 1)) - 1;
			}

			Point trazCar = ((No) lista.get(traz)).getPoint();
			Point frenteCar = ((No) lista.get(frente)).getPoint();
			double calculaAngulo = GeoUtil.calculaAngulo(frenteCar, trazCar, 0);
			Rectangle2D rectangle = new Rectangle2D.Double(
					(p.x - Carro.MEIA_LARGURA), (p.y - Carro.MEIA_ALTURA),
					Carro.LARGURA, Carro.ALTURA);
			carx = p.x - w2;
			cary = p.y - h2;
			scx = carx + w2;
			scy = cary + h2;
			if (zoom > 0.2) {
				double rad = Math.toRadians((double) calculaAngulo);
				AffineTransform afZoom = new AffineTransform();
				AffineTransform afRotate = new AffineTransform();
				afZoom.setToScale(zoom, zoom);
				afRotate.setToRotation(rad, w2, h2);

				BufferedImage rotateBuffer = new BufferedImage(width, width,
						BufferedImage.TYPE_INT_ARGB);
				BufferedImage zoomBuffer = new BufferedImage(width, height,
						BufferedImage.TYPE_INT_ARGB);
				AffineTransformOp op = new AffineTransformOp(afRotate,
						AffineTransformOp.TYPE_BILINEAR);
				op.filter(scima, zoomBuffer);
				AffineTransformOp op2 = new AffineTransformOp(afZoom,
						AffineTransformOp.TYPE_BILINEAR);
				op2.filter(zoomBuffer, rotateBuffer);
				g2d.drawImage(rotateBuffer, Util.inte(carx * zoom), Util
						.inte(cary * zoom), null);
			}
			if (!controleJogo.isSafetyCarVaiBox()) {
				g2d
						.drawImage(scimg, limitesViewPort.x
								+ (pointDesenhaSC.x + (Math.random() > 0.5 ? 1
										: -1)), (limitesViewPort.y
								+ pointDesenhaSC.y + (Math.random() > 0.5 ? -1
								: 0)), null);
			}

			if (safetyCar == null) {
				return;
			}
			if (safetyCar.getNoAtual() == null) {
				return;
			}
			g2d.setColor(Color.LIGHT_GRAY);
			g2d.fillOval(Util.inte((Util.inte(scx * zoom) - 2)), Util
					.inte((Util.inte(scy * zoom) - 2)), 8, 8);
			if (!safetyCar.isVaiProBox()) {
				if (Math.random() > .5) {
					g2d.setColor(Color.YELLOW);
				} else {
					g2d.setColor(Color.BLACK);
				}
			} else
				g2d.setColor(Color.BLACK);
			g2d.drawOval(Util.inte((Util.inte(scx * zoom) - 2)), Util
					.inte((Util.inte(scy * zoom) - 2)), 8, 8);

		}
	}

	private void desenharClima(Graphics2D g2d) {

		ImageIcon icon = (ImageIcon) gerenciadorVisual.getImgClima().getIcon();
		if (icon != null && pointDesenhaClima != null)
			g2d.drawImage(icon.getImage(), limitesViewPort.x
					+ pointDesenhaClima.x, pointDesenhaClima.y
					+ limitesViewPort.y, null);

	}

	public Point getPointDesenhaClima() {
		return pointDesenhaClima;
	}

	public void setPointDesenhaClima(Point pointDesenhaClima) {
		this.pointDesenhaClima = pointDesenhaClima;
	}

	private void desenharFarois(Graphics2D g2d) {

		if (qtdeLuzesAcesas <= 0) {
			return;
		}
		int xIni = 5;
		int yIni = 5;
		/**
		 * 1ª luz
		 */
		g2d.setColor(farol);
		g2d.fillRoundRect(limitesViewPort.x + xIni, limitesViewPort.y + yIni,
				20, 50, 15, 15);
		g2d.setColor(luzApagada);
		g2d.fillOval(limitesViewPort.x + xIni + 3,
				limitesViewPort.y + yIni + 5, 14, 14);
		if (qtdeLuzesAcesas > 0) {
			g2d.setColor(luzAcesa);
		} else {
			g2d.setColor(luzApagada);
		}
		g2d.fillOval(limitesViewPort.x + xIni + 3, limitesViewPort.y + yIni
				+ 30, 14, 14);
		xIni += 25;
		g2d.setColor(farol);
		g2d.fillRoundRect(limitesViewPort.x + xIni, limitesViewPort.y + yIni,
				20, 50, 15, 15);
		g2d.setColor(luzApagada);
		g2d.fillOval(limitesViewPort.x + xIni + 3,
				limitesViewPort.y + yIni + 5, 14, 14);
		if (qtdeLuzesAcesas > 1) {
			g2d.setColor(luzAcesa);
		} else {
			g2d.setColor(luzApagada);
		}
		g2d.fillOval(limitesViewPort.x + xIni + 3, limitesViewPort.y + yIni
				+ 30, 14, 14);
		xIni += 25;
		g2d.setColor(farol);
		g2d.fillRoundRect(limitesViewPort.x + xIni, limitesViewPort.y + yIni,
				20, 50, 15, 15);
		g2d.setColor(luzApagada);
		g2d.fillOval(limitesViewPort.x + xIni + 3,
				limitesViewPort.y + yIni + 5, 14, 14);
		if (qtdeLuzesAcesas > 2) {
			g2d.setColor(luzAcesa);
		} else {
			g2d.setColor(luzApagada);
		}
		g2d.fillOval(limitesViewPort.x + xIni + 3, limitesViewPort.y + yIni
				+ 30, 14, 14);
		xIni += 25;
		g2d.setColor(farol);
		g2d.fillRoundRect(limitesViewPort.x + xIni, limitesViewPort.y + yIni,
				20, 50, 15, 15);
		g2d.setColor(luzApagada);
		g2d.fillOval(limitesViewPort.x + xIni + 3,
				limitesViewPort.y + yIni + 5, 14, 14);
		if (qtdeLuzesAcesas > 3) {
			g2d.setColor(luzAcesa);
		} else {
			g2d.setColor(luzApagada);
		}
		g2d.fillOval(limitesViewPort.x + xIni + 3, limitesViewPort.y + yIni
				+ 30, 14, 14);
		xIni += 25;
		g2d.setColor(farol);
		g2d.fillRoundRect(limitesViewPort.x + xIni, limitesViewPort.y + yIni,
				20, 50, 15, 15);
		g2d.setColor(luzApagada);
		g2d.fillOval(limitesViewPort.x + xIni + 3,
				limitesViewPort.y + yIni + 5, 14, 14);
		if (qtdeLuzesAcesas > 4) {
			g2d.setColor(luzAcesa);
		} else {
			g2d.setColor(luzApagada);
		}
		g2d.fillOval(limitesViewPort.x + xIni + 3, limitesViewPort.y + yIni
				+ 30, 14, 14);
	}

	private void desenhaTipoPneu(Piloto piloto, Graphics g2d) {
		if (Carro.TIPO_PNEU_MOLE.equals(piloto.getCarro().getTipoPneu())) {
			if (Math.random() > .5)
				g2d.setColor(Color.GRAY);
			else
				g2d.setColor(Color.DARK_GRAY);
		} else {
			if (Math.random() > .5)
				g2d.setColor(Color.DARK_GRAY);
			else
				g2d.setColor(Color.BLACK);

		}

		g2d.drawOval(Util.inte((piloto.getCarX() - 2) * zoom), Util
				.inte((piloto.getCarY() - 2) * zoom), 8, 8);

	}

	private void desenhaNomePilotoSelecionado(Piloto ps, Graphics2D g2d) {
		if (ps == null)
			return;
		if (ps.getNoAtual() == null)
			return;
		if (ps.getCarro() == null)
			return;
		String txt1 = ps.getNome() + "-" + ps.getCarro().getNome();

		String dano = ((ps.getCarro().getDanificado() == null) ? "" : Lang
				.msg(ps.getCarro().getDanificado()));

		String agressivo = (ps.isAgressivo() ? Lang.msg("AGRESSIVO") : Lang
				.msg("NORMAL"));

		String intel = (ps.isJogadorHumano() ? ps.getNomeJogador() : "IA");
		String txt2 = intel + " " + agressivo + " " + dano;
		String velo = "~" + ps.getVelocidade() + " Km/h";

		int maior = 0;
		if (txt1.length() > maior) {
			maior = txt1.length();
		}
		if (txt2.length() > maior) {
			maior = txt2.length();
		}
		Color c2 = ps.getCarro().getCor2();
		Color c1 = ps.getCarro().getCor1();
		if (c2 != null) {
			c2 = c2.brighter();
			g2d.setColor(new Color(c2.getRed(), c2.getGreen(), c2.getBlue(),
					200));
		}

		Point pt = new Point(ps.getCarX(), ps.getCarY());// ps.getNoAtual().getPoint();
		int largura = maior * 7;
		/**
		 * moldura superior Balão info piloto
		 */

		g2d.fillRoundRect(Util.inte((pt.x * zoom) + 14), Util
				.inte((pt.y * zoom) - 50), largura, 25, 15, 15);
		int valor = (c2.getRed() + c2.getGreen() + c2.getBlue()) / 2;
		if (valor > 250) {
			g2d.setColor(Color.BLACK);
		} else {
			g2d.setColor(Color.WHITE);
		}
		int xTxt = Util.inte(pt.x * zoom) + 15;
		if (txt1 != null)
			g2d.drawString(txt1, xTxt + 2, Util.inte((pt.y * zoom) - 38));
		if (txt2 != null)
			g2d.drawString(txt2, xTxt + 2, Util.inte((pt.y * zoom - 28)));
		g2d.setColor(Color.BLACK);
		g2d.drawLine(Util.inte((pt.x * zoom + 4)), Util.inte(pt.y * zoom), Util
				.inte((pt.x * zoom) + 13), Util.inte((pt.y * zoom) - 28));
		/**
		 * moldura inferior
		 */
		if (desenhaInfo) {

			if (Carro.GIRO_MIN_VAL == ps.getCarro().getGiro()) {
				desenBarraGiro(g2d, true, gre, 5);
				g2d.setColor(Color.BLACK);
				g2d.drawString(Lang.msg("Min"), limitesViewPort.x + 9,
						limitesViewPort.y + 195);
			}
			if (Carro.GIRO_NOR_VAL == ps.getCarro().getGiro()) {
				desenBarraGiro(g2d, false, gre, 5);
				desenBarraGiro(g2d, true, yel, 35);
				g2d.setColor(Color.BLACK);
				g2d.drawString(Lang.msg("Nor"), limitesViewPort.x + 39,
						limitesViewPort.y + 195);

			}
			if (Carro.GIRO_MAX_VAL == ps.getCarro().getGiro()) {
				desenBarraGiro(g2d, false, gre, 5);
				desenBarraGiro(g2d, false, yel, 35);
				desenBarraGiro(g2d, true, red, 65);
				g2d.setColor(Color.BLACK);
				g2d.drawString(Lang.msg("Max"), limitesViewPort.x + 69,
						limitesViewPort.y + 195);
			}
		}
		if (ps.isBox()) {

			g2d.drawImage(fuel.getImage(), limitesViewPort.x + 5,
					limitesViewPort.y + 240, null);
			g2d.setColor(Color.BLACK);
			Integer percent = ps.getQtdeCombustBox();
			if (percent != null && ps.isJogadorHumano())
				g2d.drawString(percent + "%", limitesViewPort.x + 5,
						limitesViewPort.y + 280);
			g2d.drawImage(tyre.getImage(), limitesViewPort.x + 5,
					limitesViewPort.y + 285, null);
			String tpPneu = ps.getTipoPneuBox();
			if (tpPneu != null && ps.isJogadorHumano())
				g2d.drawString(Lang.msg(tpPneu), limitesViewPort.x + 5,
						limitesViewPort.y + 325);
		}

		if (desenhaInfo && velo != null) {
			g2d.setColor(c1);
			g2d.fillRoundRect(limitesViewPort.x + pointDesenhaVelo.x,
					limitesViewPort.y + pointDesenhaVelo.y + 143, 70, 15, 15,
					15);
			valor = (c1.getRed() + c1.getGreen() + c1.getBlue()) / 2;
			if (valor > 200) {
				g2d.setColor(Color.BLACK);
			} else {
				g2d.setColor(Color.WHITE);
			}
			g2d.drawString(velo, limitesViewPort.x + pointDesenhaVelo.x + 3,
					limitesViewPort.y + pointDesenhaVelo.y + 155);
		}

	}

	private void desenBarraGiro(Graphics g2d, boolean varia, Color cor,
			int inico) {
		g2d.setColor(cor);
		int incremetAlt = 0;
		if (gre.equals(cor)) {
			incremetAlt = 10;
		} else if (yel.equals(cor)) {
			incremetAlt = 28;
		} else if (red.equals(cor)) {
			incremetAlt = 46;
		}
		int y = 200;
		g2d.fillRoundRect(limitesViewPort.x + inico, limitesViewPort.y + y
				- incremetAlt, 4, incremetAlt, 15, 15);
		incremetAlt += 3;
		g2d.fillRoundRect(limitesViewPort.x + inico + 5, limitesViewPort.y + y
				- incremetAlt, 4, incremetAlt, 15, 15);
		incremetAlt += 3;
		g2d.fillRoundRect(limitesViewPort.x + inico + 10, limitesViewPort.y + y
				- incremetAlt, 4, incremetAlt, 15, 15);
		incremetAlt += 3;
		if (varia) {
			int val = 1 + (int) (Math.random() * 3);
			switch (val) {
			case 1:
				g2d.fillRoundRect(limitesViewPort.x + inico + 15,
						limitesViewPort.y + y - incremetAlt, 4, incremetAlt,
						15, 15);
				incremetAlt += 3;
				break;
			case 2:
				g2d.fillRoundRect(limitesViewPort.x + inico + 15,
						limitesViewPort.y + y - incremetAlt, 4, incremetAlt,
						15, 15);
				incremetAlt += 3;
				g2d.fillRoundRect(limitesViewPort.x + inico + 20,
						limitesViewPort.y + y - incremetAlt, 4, incremetAlt,
						15, 15);
				incremetAlt += 3;
				break;
			case 3:
				g2d.fillRoundRect(limitesViewPort.x + inico + 15,
						limitesViewPort.y + y - incremetAlt, 4, incremetAlt,
						15, 15);
				incremetAlt += 3;
				g2d.fillRoundRect(limitesViewPort.x + inico + 20,
						limitesViewPort.y + y - incremetAlt, 4, incremetAlt,
						15, 15);
				incremetAlt += 3;
				g2d.fillRoundRect(limitesViewPort.x + inico + 25,
						limitesViewPort.y + y - incremetAlt, 4, incremetAlt,
						15, 15);
				incremetAlt += 3;
				break;
			default:
				break;
			}
		} else {
			g2d.fillRoundRect(limitesViewPort.x + inico + 15, limitesViewPort.y
					+ y - incremetAlt, 4, incremetAlt, 15, 15);
			incremetAlt += 3;
			g2d.fillRoundRect(limitesViewPort.x + inico + 20, limitesViewPort.y
					+ y - incremetAlt, 4, incremetAlt, 15, 15);
			incremetAlt += 3;
			g2d.fillRoundRect(limitesViewPort.x + inico + 25, limitesViewPort.y
					+ y - incremetAlt, 4, incremetAlt, 15, 15);
			incremetAlt += 3;
		}

	}

	private void desenhaNomePilotoNaoSelecionado(Piloto ps, Graphics g2d) {
		Color c2 = ps.getCarro().getCor2();
		Color c1 = ps.getCarro().getCor1();
		if (c2 != null) {
			c2 = c2.brighter();
			g2d.setColor(new Color(c2.getRed(), c2.getGreen(), c2.getBlue(),
					200));
		}

		Point pt = new Point(ps.getCarX(), ps.getCarY());

		if (ps.getPosicao() % 2 == 0) {
			g2d.fillRoundRect(Util.inte((pt.x * zoom) - 3), Util
					.inte((pt.y * zoom) - 16), ps.getNome().length() * 7, 18,
					15, 15);
			int valor = (c2.getRed() + c2.getGreen() + c2.getBlue()) / 2;
			if (valor > 250) {
				g2d.setColor(Color.BLACK);
			} else {
				g2d.setColor(Color.WHITE);
			}
			g2d.drawString(ps.getNome(), Util.inte((ps.getCarX() * zoom) - 2),
					Util.inte((ps.getCarY() * zoom) - 3));
		} else {
			g2d.fillRoundRect(Util.inte((pt.x * zoom) - 3), Util
					.inte((pt.y * zoom) + 4), ps.getNome().length() * 7, 18,
					15, 15);
			int valor = (c2.getRed() + c2.getGreen() + c2.getBlue()) / 2;
			if (valor > 250) {
				g2d.setColor(Color.BLACK);
			} else {
				g2d.setColor(Color.WHITE);
			}
			g2d.drawString(ps.getNome(), Util.inte((ps.getCarX() * zoom) - 2),
					Util.inte((ps.getCarY() * zoom) + 17));
		}

	}

	private void setarHints(Graphics2D g2d) {
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setRenderingHint(RenderingHints.KEY_RENDERING,
				RenderingHints.VALUE_RENDER_QUALITY);
		g2d.setRenderingHint(RenderingHints.KEY_DITHERING,
				RenderingHints.VALUE_DITHER_ENABLE);
		g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
				RenderingHints.VALUE_INTERPOLATION_BILINEAR);
	}

	public Dimension getPreferredSize() {
		return new Dimension(Util.inte((mx + 1000)), Util.inte((my + 1000)));
	}

	public Dimension getMinimumSize() {
		return super.getPreferredSize();
	}

	public void apagarLuz() {
		qtdeLuzesAcesas--;
	}

	public void definirDesenhoQualificacao(Piloto piloto, Point point) {
		this.pilotQualificacao = piloto;
		this.pointQualificacao = point;

	}

	public void setMapDesenharQualificacao(Map desenharQualificacao) {
		this.mapDesenharQualificacao = desenharQualificacao;
	}

	public Map getMapDesenharQualificacao() {
		return mapDesenharQualificacao;
	}

	public boolean isDesenhaInfo() {
		return desenhaInfo;
	}

	public void setDesenhaInfo(boolean desenhaPosVelo) {
		this.desenhaInfo = desenhaPosVelo;
	}
}
