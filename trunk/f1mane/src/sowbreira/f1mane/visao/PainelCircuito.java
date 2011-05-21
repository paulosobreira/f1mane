package sowbreira.f1mane.visao;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
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
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import sowbreira.f1mane.controles.ControleEstatisticas;
import sowbreira.f1mane.controles.InterfaceJogo;
import sowbreira.f1mane.entidades.Carro;
import sowbreira.f1mane.entidades.Circuito;
import sowbreira.f1mane.entidades.Clima;
import sowbreira.f1mane.entidades.No;
import sowbreira.f1mane.entidades.ObjetoLivre;
import sowbreira.f1mane.entidades.ObjetoPista;
import sowbreira.f1mane.entidades.ObjetoTransparencia;
import sowbreira.f1mane.entidades.Piloto;
import sowbreira.f1mane.entidades.SafetyCar;
import sowbreira.f1mane.entidades.Volta;
import sowbreira.f1mane.paddock.entidades.TOs.TravadaRoda;
import sowbreira.f1mane.recursos.CarregadorRecursos;
import sowbreira.f1mane.recursos.idiomas.Lang;
import br.nnpe.GeoUtil;
import br.nnpe.Logger;
import br.nnpe.Util;

/**
 * @author Paulo Sobreira
 */
public class PainelCircuito extends JPanel {
	private static final long serialVersionUID = -5268795362549996148L;
	private InterfaceJogo controleJogo;
	private GerenciadorVisual gerenciadorVisual;
	private Point pointDesenhaClima = new Point(10, 10);
	private Point pointDesenhaVelo = new Point(10, 60);
	private Point pointDesenhaSC = new Point(10, 85);
	private Point pointDesenhaHelmet = new Point(10, 130);
	private Point posisRec;
	private Point posisAtual;
	public final static Color luzDistProx1 = new Color(0, 255, 0, 100);
	public final static Color luzDistProx2 = new Color(255, 255, 0, 100);
	public final static Color luzApagada = new Color(255, 255, 255, 170);
	public final static Color luzAcesa = new Color(255, 0, 0, 255);
	public final static Color farol = new Color(0, 0, 0);
	public final static Color red = new Color(250, 0, 0, 150);
	public final static Color lightRed = new Color(250, 0, 0, 100);
	public final static Color gre = new Color(0, 255, 0, 150);
	public final static Color yel = new Color(255, 255, 0, 150);
	public final static Color transpMenus = new Color(255, 255, 255, 140);
	public final static Color blu = new Color(105, 105, 105, 40);
	public final static Color lightWhite = new Color(255, 255, 255, 100);
	public final static Color lightWhiteRain = new Color(255, 255, 255, 160);
	public final static Color nublado = new Color(200, 200, 200, 90);
	public final static BasicStroke strokeFaisca = new BasicStroke(1.0f,
			BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1.0f, new float[] {
					10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10,
					10, 10, 10, 10, 10, 10, 10, 10, 10 }, 0);
	public final static BasicStroke chuva1 = new BasicStroke(1.0f,
			BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1.0f, new float[] {
					10, 5, 10, 5, 10, 5, 10, 5, 10, 5, 10, 5, 10, 5, 10, 10, 5,
					10, 5, 10, 5, 10, 5, 10 }, 0);
	public final static BasicStroke chuva2 = new BasicStroke(1.0f,
			BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1.0f, new float[] {
					5, 10, 5, 10, 5, 10, 5, 10, 5, 10, 5, 10, 5, 10, 5, 10, 5,
					10, 5, 10, 5, 10, 5, 10 }, 0);
	public final static BufferedImage carroimgDano = CarregadorRecursos
			.carregaBufferedImageTranspareciaBranca("CarroLadoDef.png");
	public final static BufferedImage setaCarroCima = CarregadorRecursos
			.carregaBufferedImageTranspareciaBranca("SetaCarroCima.png", 200);
	public final static BufferedImage setaCarroBaixo = CarregadorRecursos
			.carregaBufferedImageTranspareciaBranca("SetaCarroBaixo.png", 200);
	public final static BufferedImage gridCarro = CarregadorRecursos
			.carregaBufferedImageTranspareciaPreta("GridCarro.png");
	public final static BufferedImage helmetPiloto = CarregadorRecursos
			.carregaBufferedImageTranspareciaBranca("helmet.gif");
	public final static BufferedImage scimg = CarregadorRecursos
			.carregaBufferedImageTranspareciaBranca("safetycar.gif");
	public final static BufferedImage scima = CarregadorRecursos
			.carregaBufferedImageTranspareciaBranca("sfcima.png");
	public final static BufferedImage travadaRodaImg = CarregadorRecursos
			.carregaBufferedImageTranspareciaBranca("travadaRoda.png", 150);

	private int qtdeLuzesAcesas = 5;
	private Piloto pilotQualificacao;
	private Point pointQualificacao;
	private Map mapDesenharQualificacao = new HashMap();
	private boolean desenhouQualificacao;
	private boolean desenhaInfo = true;
	public final static ImageIcon fuel = new ImageIcon(
			CarregadorRecursos.carregarImagem("fuel.gif"));
	public final static ImageIcon tyre = new ImageIcon(
			CarregadorRecursos.carregarImagem("tyre.gif"));
	private int mx;
	private int my;
	public double zoom = 1.0;
	public final static String zoomMutex = "zoomMutex";
	private Circuito circuito;
	private BasicStroke trilho = new BasicStroke(1.0f);
	private BasicStroke trilhoMiniPista = new BasicStroke(2.5f);
	private BasicStroke pista;
	private BasicStroke pistaTinta;
	private BasicStroke box;
	private int larguraPistaPixeis;
	private BasicStroke zebra;
	private Shape[] grid = new Shape[24];
	private List gridImg = new ArrayList();
	private Shape[] asfaltoGrid = new Shape[24];
	private Shape[] boxParada = new Shape[12];
	private Shape[] boxCor1 = new Shape[12];
	private Shape[] boxCor2 = new Shape[12];
	private double larguraPista = 0;
	private Rectangle limitesViewPort;
	private Set<TravadaRoda> marcasPneu = new HashSet<TravadaRoda>();
	private boolean inverterSpray;
	private Map<Piloto, Piloto> mapaFaiscas = new HashMap<Piloto, Piloto>();
	private Piloto pilotoSelecionado;
	private BufferedImage backGround;
	private TileMap tileMap[][];
	private Thread threadCarregarBkg;
	private List pistaMinimizada;
	private boolean contBox1;
	private boolean tracadoManual;
	private boolean piscaBico;
	private boolean piscaDanos;
	private boolean contBox2;
	private ArrayList boxMinimizado;
	protected Point newP;
	private Point oldP;
	protected double mouseZoom = 1;

	public Point getPosisRec() {
		return posisRec;
	}

	public void setPosisRec(Point posisRec) {
		this.posisRec = posisRec;
	}

	public Point getPosisAtual() {
		return posisAtual;
	}

	public void setPosisAtual(Point posisAtual) {
		this.posisAtual = posisAtual;
	}

	public BufferedImage getBackGround() {
		return backGround;
	}

	public void setBackGround(BufferedImage backGround) {
		this.backGround = backGround;
	}

	public void setTileMap(TileMap[][] tileMap) {
		this.tileMap = tileMap;
	}

	public PainelCircuito(InterfaceJogo jogo,
			GerenciadorVisual gerenciadorVisual) {
		controleJogo = jogo;
		Logger.logar("controleJogo = jogo()");
		this.gerenciadorVisual = gerenciadorVisual;
		Logger.logar("this.gerenciadorVisual = gerenciadorVisual;");
		addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				// Logger.logar("Pontos Editor :" + e.getX() + " - "
				// + e.getY());
				Piloto pilotoJogador = controleJogo.getPilotoJogador();
				if (pilotoJogador != null && pilotoJogador.getP1() != null
						&& pilotoJogador.getP2() != null) {
					Point p = new Point(Util.inte(e.getPoint().x / zoom),
							Util.inte(e.getPoint().y / zoom));

					double menor = Integer.MAX_VALUE;
					int pos = 0;
					double p0p = GeoUtil.distaciaEntrePontos(
							pilotoJogador.getP0(), p);
					double p1p = GeoUtil.distaciaEntrePontos(
							pilotoJogador.getP1(), p);
					double p2p = GeoUtil.distaciaEntrePontos(
							pilotoJogador.getP2(), p);
					if (p0p < menor) {
						menor = p0p;
						pos = 0;
					}
					if (p1p < menor) {
						menor = p1p;
						pos = 1;
					}
					if (p2p < menor) {
						menor = p2p;
						pos = 2;
					}
					if ((pos == 2 && pilotoJogador.getTracado() == 1)
							|| (pos == 1 && pilotoJogador.getTracado() == 2)) {
						pos = 0;
					}
					controleJogo.mudarPos(pos);
				}
				super.mouseClicked(e);
			}
		});
		circuito = controleJogo.getCircuito();
		if (backGround == null) {
			try {

				carregaBackGround();
				if (gerenciadorVisual.getVdp() == GerenciadorVisual.VDP2) {
					carregaTileMap();
				}
			} catch (Error e) {
				System.gc();
				e.printStackTrace();
			}
		}
		larguraPista = circuito.getMultiplicadorLarguraPista();
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
		Logger.logar("Antes atualizaVarZoom");
		atualizaVarZoom();
	}

	private void carregaTileMap() {
		int largura = backGround.getWidth() / TileMap.LADO;
		int altura = backGround.getHeight() / TileMap.LADO;
		tileMap = new TileMap[TileMap.LADO][TileMap.LADO];
		for (int i = 0; i < TileMap.LADO; i++) {
			for (int j = 0; j < TileMap.LADO; j++) {
				BufferedImage bufferedImage = new BufferedImage(largura,
						altura, backGround.getType());
				Raster srcRaster = backGround.getData(new Rectangle(
						i * largura, j * altura, largura, altura));
				WritableRaster destRaster = bufferedImage.getRaster();
				int[] argbArray = new int[4];
				for (int x = (i * largura); x < (largura + (i * largura)); x++) {
					for (int y = (j * altura); y < (altura + (j * altura)); y++) {
						argbArray = new int[4];
						argbArray = srcRaster.getPixel(x, y, argbArray);
						destRaster.setPixel((x - (i * largura)),
								(y - (j * altura)), argbArray);
					}
				}
				tileMap[i][j] = new TileMap();
				tileMap[i][j].setBackGround(bufferedImage);
				bufferedImage.setAccelerationPriority(1);
			}
		}
		backGround = null;
	}

	public void carregaBackGround() {
		// if (true) {
		// return;
		// }
		Logger.logar("carregaBackGround()");
		try {
			if (!(threadCarregarBkg != null && threadCarregarBkg.isAlive()))
				backGround = CarregadorRecursos.carregaBackGround(
						circuito.getBackGround(), this, circuito);
		} catch (Exception e) {
			backGround = null;
		}
		if (backGround == null) {
			Logger.logar("Download Imagem");
			if (threadCarregarBkg == null || !threadCarregarBkg.isAlive()) {
				Runnable runnable = new Runnable() {
					@Override
					public void run() {
						backGround = controleJogo.carregaBackGround(circuito
								.getBackGround());
						if (backGround != null)
							backGround.setAccelerationPriority(1);
						threadCarregarBkg = null;
					}
				};
				threadCarregarBkg = new Thread(runnable);
				threadCarregarBkg.setPriority(Thread.MIN_PRIORITY);
				threadCarregarBkg.start();
			}
		} else {
			backGround.setAccelerationPriority(1);
		}

	}

	public int getQtdeLuzesAcesas() {
		return qtdeLuzesAcesas;
	}

	protected void paintComponent(Graphics g) {
		synchronized (zoomMutex) {
			super.paintComponent(g);
			try {
				// System.out.println("mouseZoom " + mouseZoom + " zoom " +
				// zoom);
				if (Math.abs(mouseZoom - zoom) < 0.01) {
					zoom = mouseZoom;
				}
				if (mouseZoom > zoom) {
					zoom += 0.01;
					atualizaVarZoom();
				}
				if (mouseZoom < zoom) {
					zoom -= 0.01;
					atualizaVarZoom();
				}
				Graphics2D g2d = (Graphics2D) g;
				setarHints(g2d);
				limitesViewPort = (Rectangle) limitesViewPort();
				pilotoSelecionado = gerenciadorVisual
						.obterPilotoSecionadoTabela(controleJogo
								.getPilotoSelecionado());
				ControleSom.processaSom(pilotoSelecionado, controleJogo, this);
				desenhaBackGround(g2d);

				if (!circuito.isUsaBkg()) {
					setStrokeCoresLegado(g2d);
					desenhaObjetosBaixoLegado(g2d);
					desenhaTintaPistaEZebraLegado(g2d);
					desenhaPistaLegado(g2d);
					desenhaPistaBoxLegado(g2d);
					desenhaLargadaLegado(g2d);
					desenhaBoxesLegado(g2d);
					g2d.setStroke(trilho);
				} else if (backGround == null) {
					// setStrokeCoresLegado(g2d);
					// desenhaTintaPistaEZebraLegado(g2d);
					// desenhaPistaLegado(g2d);
					// desenhaPistaBoxLegado(g2d);
					// desenhaLargadaLegado(g2d);
					// desenhaBoxesLegado(g2d);
					// g2d.setStroke(trilho);
				}
				desenhaGrid(g2d);
				desenhaMarcasPeneuPista(g2d);
				desenhaPiloto(g2d);
				if (!circuito.isUsaBkg()) {
					desenhaObjetosCimaLegado(g2d);
				}
				if (!desenhouQualificacao) {
					desenhaQualificacao(g2d);
				}
				desenharSafetyCar(g2d);
				desenharFarois(g2d);

				desenhaChuva(g2d);
				desenharClima(g2d);
				desenhaInfoAdd(g2d);
				desenhaContadorVoltas(g2d);
				desenhaMiniPista(g2d);
			} catch (Exception e) {
				Logger.logarExept(e);
			}
		}

	}

	private void desenhaKers(Graphics2D g2d) {
		if (pilotoSelecionado == null) {
			return;
		}
		int cargaKers = pilotoSelecionado.getCarro().getCargaKers() / 2;
		int y = 150;
		g2d.setColor(red);
		g2d.fillRoundRect(limitesViewPort.x + 100, limitesViewPort.y + y, 20,
				50, 5, 5);
		g2d.setColor(gre);
		g2d.fillRoundRect(limitesViewPort.x + 100, limitesViewPort.y + y
				+ (50 - cargaKers), 20, cargaKers, 5, 5);

		if (pilotoSelecionado.getCargaKersVisual() != pilotoSelecionado
				.getCarro().getCargaKers()) {
			g2d.setColor(Color.YELLOW);
			pilotoSelecionado.setCargaKersVisual(pilotoSelecionado.getCarro()
					.getCargaKers());
			g2d.drawRoundRect(limitesViewPort.x + 100, limitesViewPort.y + y,
					20, 50, 5, 5);
		} else {
			g2d.setColor(Color.WHITE);
		}
		g2d.drawString("+", limitesViewPort.x + 107, limitesViewPort.y + y + 10);
		g2d.drawString("-", limitesViewPort.x + 108, limitesViewPort.y + y + 45);

	}

	private void setStrokeCoresLegado(Graphics2D g2d) {
		if (larguraPistaPixeis == 0)
			larguraPistaPixeis = Util.inte(176 * larguraPista * zoom);
		if (pista == null)
			pista = new BasicStroke(larguraPistaPixeis, BasicStroke.CAP_ROUND,
					BasicStroke.JOIN_ROUND);
		if (pistaTinta == null)
			pistaTinta = new BasicStroke(Util.inte(larguraPistaPixeis * 1.05),
					BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
		if (box == null)
			box = new BasicStroke(Util.inte(larguraPistaPixeis * .4),
					BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
		if (zebra == null)
			zebra = new BasicStroke(Util.inte(larguraPistaPixeis * 1.05),
					BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10f,
					new float[] { 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10,
							10 }, 0);
		if (circuito.getCorFundo() != null) {
			g2d.setColor(circuito.getCorFundo());
			g2d.fill(limitesViewPort);
		}
	}

	private void desenhaBackGround(Graphics2D g2d) {
		if (circuito.isUsaBkg()) {
			if (gerenciadorVisual.getVdp() == GerenciadorVisual.VDP1) {
				if (backGround == null) {
					carregaBackGround();
				}
				if (backGround != null) {
					AffineTransform affineTransform = AffineTransform
							.getScaleInstance(zoom, zoom);
					AffineTransformOp affineTransformOp = new AffineTransformOp(
							affineTransform, AffineTransformOp.TYPE_BILINEAR);
					BufferedImage subimage = null;
					int diffX = 0;
					int diffY = 0;
					try {
						if (limitesViewPort != null && backGround != null) {
							Rectangle rectangle = new Rectangle(
									Util.inte(limitesViewPort.getX() / zoom),
									Util.inte(limitesViewPort.getY() / zoom),
									Util.inte(limitesViewPort.getWidth() / zoom),
									Util.inte(limitesViewPort.getHeight()
											/ zoom));
							if ((rectangle.x + rectangle.getWidth()) > backGround
									.getWidth()) {
								diffX = Util.inte((rectangle.x + rectangle
										.getWidth()) - backGround.getWidth());
								rectangle.x -= diffX;
							}
							if ((rectangle.y + rectangle.getHeight()) > backGround
									.getHeight()) {
								diffY = Util.inte((rectangle.y + rectangle
										.getHeight()) - backGround.getHeight());
								rectangle.y -= diffY;
							}
							subimage = backGround.getSubimage(rectangle.x,
									rectangle.y, rectangle.width,
									rectangle.height);
						}
					} catch (Exception e) {
						Logger.logarExept(e);
						subimage = backGround;
					}
					BufferedImage drawBuffer = null;
					if (zoom == 1) {
						drawBuffer = subimage;
					} else {
						drawBuffer = new BufferedImage(
								(int) (limitesViewPort.getWidth()),
								(int) (limitesViewPort.getHeight()),
								backGround.getType());
						affineTransformOp.filter(subimage, drawBuffer);
					}

					if (drawBuffer == null) {
						drawBuffer = backGround;
					}
					if (drawBuffer != null) {
						drawBuffer.setAccelerationPriority(1);
						int newX = Util.inte(limitesViewPort.getX());
						int newY = Util.inte(limitesViewPort.getY());
						g2d.drawImage(drawBuffer, newX, newY, null);
					}

				}
			}

			if (gerenciadorVisual.getVdp() == GerenciadorVisual.VDP2) {
				if (tileMap == null) {
					carregaTileMap();
				}
				for (int i = 0; i < TileMap.LADO; i++) {
					for (int j = 0; j < TileMap.LADO; j++) {
						if (tileMap[i][j] != null) {
							BufferedImage bd = tileMap[i][j].getBackGround();
							Point2D point = new Point2D.Double(
									Util.double2Decimal(i * bd.getWidth()
											* zoom), Util.double2Decimal(j
											* bd.getHeight() * zoom));
							Rectangle2D rectangle = new Rectangle2D.Double(
									point.getX(), point.getY(),
									Util.double2Decimal(bd.getWidth() * zoom),
									Util.double2Decimal(bd.getHeight() * zoom));
							if (limitesViewPort.intersects(rectangle)) {
								if (zoom != tileMap[i][j].getZoom()) {
									AffineTransform affineTransform = AffineTransform
											.getScaleInstance(zoom, zoom);
									AffineTransformOp affineTransformOp = new AffineTransformOp(
											affineTransform,
											AffineTransformOp.TYPE_BILINEAR);
									BufferedImage drawBuffer = new BufferedImage(
											Util.inte(Util.double2Decimal(bd
													.getWidth() * zoom)),
											Util.inte(Util.double2Decimal(bd
													.getHeight() * zoom)),
											bd.getType());
									drawBuffer.setAccelerationPriority(1);
									affineTransformOp.filter(bd, drawBuffer);
									tileMap[i][j]
											.setBackGroundZoomed(drawBuffer);
									tileMap[i][j].setZoom(zoom);
									g2d.drawImage(drawBuffer,
											Util.inte(point.getX()),
											Util.inte(point.getY()), null);
								} else {
									g2d.drawImage(
											tileMap[i][j].getBackGroundZoomed(),
											Util.inte(point.getX()),
											Util.inte(point.getY()), null);
								}
								// g2d.setColor(this.yel);
								// g2d.fill(rectangle);
							}
						}
					}
				}
			}
		}

	}

	private void desenhaMiniPista(Graphics2D g2d) {
		if (!desenhaInfo) {
			return;
		}
		if (limitesViewPort == null) {
			return;
		}
		g2d.setColor(Color.LIGHT_GRAY);
		Point o = new Point(limitesViewPort.x + 5, limitesViewPort.y
				+ limitesViewPort.height - 200);
		// g2d.fillOval(limitesViewPort.x + 5, limitesViewPort.y + 330, 10, 10);
		double doubleMulti = circuito.getMultiplciador() * 3;
		if (pistaMinimizada == null) {
			pistaMinimizada = new ArrayList();
			List pista = circuito.getPista();

			for (Iterator iterator = pista.iterator(); iterator.hasNext();) {
				No no = (No) iterator.next();
				Point p = new Point(no.getX(), no.getY());
				p.x /= doubleMulti;
				p.y /= doubleMulti;
				if (!pistaMinimizada.contains(p))
					pistaMinimizada.add(p);
			}

		}

		if (boxMinimizado == null) {
			boxMinimizado = new ArrayList();
			List box = circuito.getBox();
			for (Iterator iterator = box.iterator(); iterator.hasNext();) {
				No no = (No) iterator.next();
				Point p = new Point(no.getX(), no.getY());
				p.x /= doubleMulti;
				p.y /= doubleMulti;
				if (!boxMinimizado.contains(p))
					boxMinimizado.add(p);
			}

		}

		Point oldP = null;
		g2d.setStroke(trilhoMiniPista);
		for (Iterator iterator = pistaMinimizada.iterator(); iterator.hasNext();) {
			Point p = (Point) iterator.next();
			if (oldP != null) {
				g2d.drawLine(o.x + oldP.x, o.y + oldP.y, o.x + p.x, o.y + p.y);
			}
			oldP = p;
		}
		Point p0 = (Point) pistaMinimizada.get(0);
		g2d.drawLine(o.x + oldP.x, o.y + oldP.y, o.x + p0.x, o.y + p0.y);

		oldP = null;
		g2d.setStroke(trilhoMiniPista);
		g2d.setColor(Color.gray);
		for (Iterator iterator = boxMinimizado.iterator(); iterator.hasNext();) {
			Point p = (Point) iterator.next();
			if (oldP != null) {
				g2d.drawLine(o.x + oldP.x, o.y + oldP.y, o.x + p.x, o.y + p.y);
			}
			oldP = p;
		}

		Piloto lider = (Piloto) controleJogo.getPilotos().get(0);
		Font fontOri = g2d.getFont();
		g2d.setFont(new Font(fontOri.getName(), fontOri.getStyle(), 8));

		List pilotos = controleJogo.getPilotos();
		for (Iterator iterator = pilotos.iterator(); iterator.hasNext();) {
			Piloto piloto = (Piloto) iterator.next();
			Point point = piloto.getNoAtual().getPoint();
			Point p = new Point(point.x, point.y);
			p.x /= doubleMulti;
			p.y /= doubleMulti;
			if (piloto.equals(pilotoSelecionado)) {
				g2d.setColor(PainelTabelaPosicoes.jogador);
			} else if (piloto.equals(lider)) {
				g2d.setColor(PainelTabelaPosicoes.otros);
			} else if (controleJogo.verirficaDesafiandoCampeonato(piloto)) {
				g2d.setColor(lightRed);
			} else {
				g2d.setColor(Color.LIGHT_GRAY);
			}
			g2d.fillOval(o.x + p.x - 5, o.y + p.y - 5, 10, 10);
			g2d.setColor(Color.BLACK);
			g2d.drawString("" + piloto.getPosicao(),
					o.x + p.x - ((piloto.getPosicao() < 10) ? 3 : 5), o.y + p.y
							+ 3);
		}
		g2d.setFont(fontOri);
		if (pilotoSelecionado != null && pilotoSelecionado.isJogadorHumano()
				&& posisRec != null) {
			g2d.setColor(red);
			g2d.fillOval(o.x + Util.inte(posisRec.x / doubleMulti),
					o.y + Util.inte(posisRec.y / doubleMulti),
					Util.inte(5 * zoom), Util.inte(5 * zoom));
		}
	}

	public static void main(String[] args) {
		System.out.println(4341 / 21);
	}

	private void desenhaObjetosBaixoLegado(Graphics2D g2d) {
		if (circuito == null) {
			return;
		}
		if (circuito.getObjetos() == null) {
			return;
		}
		for (ObjetoPista objetoPista : circuito.getObjetos()) {
			if (objetoPista.isPintaEmcima())
				continue;
			if (zoom < .7
					&& !(objetoPista instanceof ObjetoLivre)
					&& (objetoPista.getAltura() < 2 || objetoPista.getLargura() < 2)) {
				continue;
			}
			objetoPista.desenha(g2d, zoom);
		}

	}

	private void desenhaObjetosCimaLegado(Graphics2D g2d) {
		if (circuito == null) {
			return;
		}
		if (circuito.getObjetos() == null) {
			return;
		}
		for (ObjetoPista objetoPista : circuito.getObjetos()) {
			if (!objetoPista.isPintaEmcima())
				continue;
			if (zoom < .7
					&& !(objetoPista instanceof ObjetoLivre)
					&& (objetoPista.getAltura() < 2 || objetoPista.getLargura() < 2)) {
				continue;
			}
			objetoPista.desenha(g2d, zoom);
		}

	}

	private void desenhaMarcasPeneuPista(Graphics2D g2d) {
		if (limitesViewPort == null || zoom < 0.3) {
			return;
		}
		synchronized (marcasPneu) {
			for (TravadaRoda travadaRoda : marcasPneu) {
				No noAtual = controleJogo.obterNoPorId(travadaRoda.getIdNo());
				Point p = noAtual.getPoint();
				if (!limitesViewPort.contains(new Point2D.Double(p.x * zoom,
						p.y * zoom))) {
					continue;
				}
				List<ObjetoPista> objetos = circuito.getObjetos();
				if (objetos != null) {
					boolean travadaNaTransparencia = false;
					for (Iterator iterator = objetos.iterator(); iterator
							.hasNext();) {
						ObjetoPista objetoPista = (ObjetoPista) iterator.next();
						if (objetoPista instanceof ObjetoTransparencia) {
							ObjetoTransparencia objetoTransparencia = (ObjetoTransparencia) objetoPista;
							Rectangle rectangle = new Rectangle(Carro.LARGURA,
									Carro.LARGURA);
							rectangle.setLocation(new Point(p.x - Carro.LARGURA
									/ 2, p.y - Carro.LARGURA / 2));
							if (objetoTransparencia.obterArea().intersects(
									rectangle)) {
								travadaNaTransparencia = true;
								break;
							}
						} else {
							continue;
						}
					}
					if (travadaNaTransparencia) {
						continue;
					}
				}
				int width = (int) (travadaRodaImg.getWidth());
				int height = (int) (travadaRodaImg.getHeight());
				List lista = controleJogo.obterNosPista();

				if (lista == null) {
					return;
				}
				int cont = noAtual.getIndex();

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
				double calculaAngulo = GeoUtil.calculaAngulo(frenteCar,
						trazCar, 0);
				Rectangle2D rectangle = new Rectangle2D.Double(
						(p.x - Carro.MEIA_LARGURA), (p.y - Carro.MEIA_ALTURA),
						Carro.LARGURA, Carro.ALTURA);
				Point p1 = GeoUtil.calculaPonto(
						calculaAngulo,
						Util.inte(Carro.ALTURA
								* controleJogo.getCircuito()
										.getMultiplicadorLarguraPista()),
						new Point(Util.inte(rectangle.getCenterX()), Util
								.inte(rectangle.getCenterY())));
				Point p2 = GeoUtil.calculaPonto(
						calculaAngulo + 180,
						Util.inte(Carro.ALTURA
								* controleJogo.getCircuito()
										.getMultiplicadorLarguraPista()),
						new Point(Util.inte(rectangle.getCenterX()), Util
								.inte(rectangle.getCenterY())));
				if (travadaRoda.getTracado() == 0) {
					carx = p.x - w2;
					cary = p.y - h2;
				}
				if (travadaRoda.getTracado() == 1) {
					carx = Util.inte((p1.x - w2));
					cary = Util.inte((p1.y - h2));
				}
				if (travadaRoda.getTracado() == 2) {
					carx = Util.inte((p2.x - w2));
					cary = Util.inte((p2.y - h2));
				}
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
				op.filter(travadaRodaImg, zoomBuffer);
				AffineTransformOp op2 = new AffineTransformOp(afZoom,
						AffineTransformOp.TYPE_BILINEAR);
				op2.filter(zoomBuffer, rotateBuffer);
				g2d.drawImage(rotateBuffer, Util.inte(carx * zoom),
						Util.inte(cary * zoom), null);
			}
		}
	}

	public boolean isDesenhouQualificacao() {
		return desenhouQualificacao;
	}

	public void setDesenhouQualificacao(boolean desenhouQualificacao) {
		this.desenhouQualificacao = desenhouQualificacao;
	}

	private void desenhaPiloto(Graphics2D g2d) {

		for (int i = controleJogo.getPilotos().size() - 1; i > -1; i--) {
			Piloto piloto = (Piloto) controleJogo.getPilotos().get(i);
			if (piloto.getCarro().isRecolhido() || piloto.getNoAtual() == null
					|| piloto.getCarro().isPaneSeca()) {
				continue;
			}
			desenhaCarro(g2d, piloto);
			piloto.centralizaCarro(controleJogo);
			if (Logger.ativo) {
				AffineTransform afZoom = new AffineTransform();
				afZoom.setToScale(zoom, zoom);
				if (piloto.getCentro() != null) {
					Rectangle centro = piloto.getCentro();
					g2d.draw(afZoom.createTransformedShape(centro));
				}
				if (piloto.getDiateira() != null) {
					g2d.draw(afZoom.createTransformedShape(piloto.getDiateira()));
				}
				if (piloto.getTrazeira() != null) {
					g2d.draw(afZoom.createTransformedShape(piloto.getTrazeira()));
				}

			}
			Point p = new Point(Util.inte((piloto.getCarX() - 2) * zoom),
					Util.inte((piloto.getCarY() - 2) * zoom));
			if (limitesViewPort.contains(p)) {
				g2d.setColor(piloto.getCarro().getCor1());
				g2d.fillOval(p.x, p.y, 8, 8);
				desenhaTipoPneu(piloto, g2d);
				if (piloto != pilotoSelecionado) {
					desenhaNomePilotoNaoSelecionado(piloto, g2d);
				}
			}

		}

	}

	public Shape limitesViewPort() {
		JScrollPane scrollPane = gerenciadorVisual.getScrollPane();
		if (scrollPane == null) {
			return null;
		}
		Rectangle rectangle = scrollPane.getViewport().getBounds();
		rectangle.x = scrollPane.getViewport().getViewPosition().x;
		rectangle.y = scrollPane.getViewport().getViewPosition().y;
		// if (Logger.ativo && limitesViewPort != null) {
		// rectangle = new Rectangle(limitesViewPort.x + 100,
		// limitesViewPort.y + 100, limitesViewPort.width - 100,
		// limitesViewPort.height - 100);
		// }
		return rectangle;
	}

	private void desenhaCarro(Graphics2D g2d, Piloto piloto) {
		if (zoom < 0.3) {
			return;
		}
		BufferedImage carroCima = controleJogo.obterCarroCima(piloto);

		if (carroCima == null || piloto.getCarro().isPaneSeca()) {
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
		List lista = piloto.obterPista(controleJogo);

		if (lista == null) {
			return;
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
		boolean ultimoAngulo = false;
		if (traz < 0) {
			traz = (lista.size() - 1) + traz;
			ultimoAngulo = true;
		}
		if (frente > (lista.size() - 1)) {
			frente = (frente - (lista.size() - 1)) - 1;
			ultimoAngulo = true;
		}

		Point trazCar = ((No) lista.get(traz)).getPoint();
		trazCar = new Point(trazCar.x, trazCar.y);
		Point frenteCar = ((No) lista.get(frente)).getPoint();
		frenteCar = new Point(frenteCar.x, frenteCar.y);
		double calculaAngulo = GeoUtil.calculaAngulo(frenteCar, trazCar, 0);
		if (piloto.getAngulo() != null && ultimoAngulo) {
			calculaAngulo = piloto.getAngulo();
		}
		piloto.setAngulo(calculaAngulo);
		Rectangle2D rectangle = new Rectangle2D.Double(
				(p.x - Carro.MEIA_LARGURA), (p.y - Carro.MEIA_ALTURA),
				Carro.LARGURA, Carro.ALTURA);
		Point p1 = GeoUtil.calculaPonto(
				calculaAngulo,
				Util.inte(Carro.ALTURA
						* controleJogo.getCircuito()
								.getMultiplicadorLarguraPista()),
				new Point(Util.inte(rectangle.getCenterX()), Util
						.inte(rectangle.getCenterY())));
		Point p2 = GeoUtil.calculaPonto(
				calculaAngulo + 180,
				Util.inte(Carro.ALTURA
						* controleJogo.getCircuito()
								.getMultiplicadorLarguraPista()),
				new Point(Util.inte(rectangle.getCenterX()), Util
						.inte(rectangle.getCenterY())));
		piloto.setP1(p1);
		piloto.setP2(p2);
		if (piloto.getTracado() == 0) {
			carx = p.x - w2;
			cary = p.y - h2;
			if (piloto.verificaColisaoCarroFrente(controleJogo)) {
				piloto.setIndiceTracado(0);
			}
			int indTracado = piloto.getIndiceTracado();

			if (indTracado != 0 && piloto.getTracadoAntigo() != 0) {
				List drawBresenhamLine = null;
				if (piloto.getTracadoAntigo() == 1) {
					drawBresenhamLine = GeoUtil.drawBresenhamLine(p1.x, p1.y,
							p.x, p.y);
				}
				if (piloto.getTracadoAntigo() == 2) {
					drawBresenhamLine = GeoUtil.drawBresenhamLine(p2.x, p2.y,
							p.x, p.y);
				}

				int indice = drawBresenhamLine.size() - indTracado;
				if (indice <= 0) {
					indice = 0;
				}
				if (indice >= drawBresenhamLine.size()) {
					indice = drawBresenhamLine.size() - 1;
				}

				Point pReta = (Point) drawBresenhamLine.get(indice);
				carx = pReta.x - w2;
				cary = pReta.y - h2;
			}
		}
		if (piloto.getTracado() == 1) {
			carx = Util.inte((p1.x - w2));
			cary = Util.inte((p1.y - h2));
			int indTracado = piloto.getIndiceTracado();
			if (indTracado != 1 && piloto.getTracadoAntigo() != 1) {
				List drawBresenhamLine = null;
				if (piloto.getTracadoAntigo() == 0) {
					drawBresenhamLine = GeoUtil.drawBresenhamLine(p.x, p.y,
							p1.x, p1.y);
				}
				if (piloto.getTracadoAntigo() == 2) {
					drawBresenhamLine = GeoUtil.drawBresenhamLine(p2.x, p2.y,
							p1.x, p1.y);
				}

				int indice = drawBresenhamLine.size() - indTracado;
				if (indice <= 0) {
					indice = 0;
				}
				if (indice >= drawBresenhamLine.size()) {
					indice = drawBresenhamLine.size() - 1;
				}

				Point pReta = (Point) drawBresenhamLine.get(indice);
				carx = pReta.x - w2;
				cary = pReta.y - h2;
			}
		}
		if (piloto.getTracado() == 2) {
			carx = Util.inte((p2.x - w2));
			cary = Util.inte((p2.y - h2));
			int indTracado = piloto.getIndiceTracado();
			if (indTracado != 0 && piloto.getTracadoAntigo() != 2) {
				List drawBresenhamLine = null;
				if (piloto.getTracadoAntigo() == 0) {
					drawBresenhamLine = GeoUtil.drawBresenhamLine(p.x, p.y,
							p2.x, p2.y);
				}
				if (piloto.getTracadoAntigo() == 1) {
					drawBresenhamLine = GeoUtil.drawBresenhamLine(p1.x, p1.y,
							p2.x, p2.y);
				}

				int indice = drawBresenhamLine.size() - indTracado;
				if (indice <= 0) {
					indice = 0;
				}
				if (indice >= drawBresenhamLine.size()) {
					indice = drawBresenhamLine.size() - 1;
				}

				Point pReta = (Point) drawBresenhamLine.get(indice);
				carx = pReta.x - w2;
				cary = pReta.y - h2;
			}
		}
		piloto.setCarX(carx);
		piloto.setCarY(cary);

		if (piloto.isAgressivo()
				&& piloto.getCarro().getGiro() == Carro.GIRO_MAX_VAL
				&& (piloto.getNoAtual().verificaCruvaAlta() || piloto
						.getNoAtual().verificaCruvaBaixa())
				&& Math.random() > .5) {
			if (piloto.getNoAtual().verificaCruvaAlta())
				calculaAngulo += Util.intervalo(-7.5, 7.5);
			if (piloto.getNoAtual().verificaCruvaBaixa())
				calculaAngulo += Util.intervalo(-15.0, 15.0);
		}

		double rad = Math.toRadians((double) calculaAngulo);
		AffineTransform afZoom = new AffineTransform();
		AffineTransform afRotate = new AffineTransform();
		afZoom.setToScale(zoom, zoom);
		afRotate.setToRotation(rad, w2, h2);

		BufferedImage rotateBuffer = new BufferedImage(width, width,
				BufferedImage.TYPE_INT_ARGB);
		BufferedImage zoomBuffer = new BufferedImage(width, height,
				BufferedImage.TYPE_INT_ARGB);
		AffineTransformOp opRotate = new AffineTransformOp(afRotate,
				AffineTransformOp.TYPE_BILINEAR);
		opRotate.filter(carroCima, zoomBuffer);
		AffineTransformOp opZoom = new AffineTransformOp(afZoom,
				AffineTransformOp.TYPE_BILINEAR);
		opZoom.filter(zoomBuffer, rotateBuffer);
		int carroX = Util.inte(carx * zoom);
		int carroY = Util.inte(cary * zoom);

		if (piloto.isJogadorHumano() && piloto.getSetaCima() != 0) {
			if (piloto.getSetaCima() % 2 == 0) {
				BufferedImage rotateBufferSetaCima = new BufferedImage(width,
						width, BufferedImage.TYPE_INT_ARGB);
				BufferedImage zoomBufferSetaCima = new BufferedImage(width,
						height, BufferedImage.TYPE_INT_ARGB);
				opRotate.filter(setaCarroCima, zoomBufferSetaCima);
				opZoom.filter(zoomBufferSetaCima, rotateBufferSetaCima);
				g2d.drawImage(rotateBufferSetaCima, carroX, carroY, null);
			}
			piloto.setSetaCima(piloto.getSetaCima() - 1);
		}

		if (piloto.isJogadorHumano() && piloto.getSetaBaixo() != 0) {
			if (piloto.getSetaBaixo() % 2 == 0) {
				BufferedImage rotateBufferSetaBaixo = new BufferedImage(width,
						width, BufferedImage.TYPE_INT_ARGB);
				BufferedImage zoomBufferSetaBaixo = new BufferedImage(width,
						height, BufferedImage.TYPE_INT_ARGB);
				opRotate.filter(setaCarroBaixo, zoomBufferSetaBaixo);
				opZoom.filter(zoomBufferSetaBaixo, rotateBufferSetaBaixo);
				g2d.drawImage(rotateBufferSetaBaixo, carroX, carroY, null);
			}
			piloto.setSetaBaixo(piloto.getSetaBaixo() - 1);
		}
		boolean naoDesenhaEfeitos = false;
		if (circuito.isUsaBkg() && circuito.getObjetos() != null) {
			for (ObjetoPista objetoPista : circuito.getObjetos()) {
				if (!(objetoPista instanceof ObjetoTransparencia))
					continue;
				if (objetoPista.isPintaEmcima()
						&& controleJogo.obterPista(piloto) != controleJogo
								.getNosDoBox()) {
					continue;
				}
				if (objetoPista.getAltura() != 0
						&& objetoPista.getLargura() != 0) {
					int indexNoAtual = noAtual.getIndex();
					if (objetoPista.getAltura() > indexNoAtual
							|| objetoPista.getLargura() < indexNoAtual) {
						continue;
					}
				}
				ObjetoTransparencia objetoTransparencia = (ObjetoTransparencia) objetoPista;
				Graphics2D gImage = rotateBuffer.createGraphics();
				objetoTransparencia.desenhaCarro(gImage, zoom, carx, cary);
				if (objetoTransparencia.obterArea().contains(p)) {
					naoDesenhaEfeitos = true;
				}
			}
		}
		g2d.drawImage(rotateBuffer, carroX, carroY, null);
		if (naoDesenhaEfeitos) {
			return;
		}
		/**
		 * Chuva e Faiscas
		 */
		if (piloto.getDiateira() == null || piloto.getCentro() == null
				|| piloto.getTrazeira() == null) {
			piloto.centralizaCarro(controleJogo);
		}
		Point frenteP = new Point((int) piloto.getDiateira().getCenterX(),
				(int) piloto.getDiateira().getCenterY());
		Point centroP = new Point((int) piloto.getCentro().getCenterX(),
				(int) piloto.getCentro().getCenterY());
		List centroDiantera = GeoUtil.drawBresenhamLine(centroP, frenteP);
		Point eixoDianteras = (Point) centroDiantera
				.get(centroDiantera.size() / 2);
		if (eixoDianteras == null) {
			eixoDianteras = frenteP;
		}
		double eixo = piloto.getDiateira().getWidth() / 2;
		if (controleJogo.isChovendo() && piloto.getVelocidade() != 0
				&& !piloto.isDesqualificado()) {
			g2d.setColor(lightWhiteRain);
			for (int i = 0; i < 30; i++) {
				if (i % (Math.random() > 0.5 ? 3 : 2) == 0) {
					continue;
				}
				int eixoDiatero = (int) (eixo * 0.7);
				Point origem = new Point((int) Util.intervalo(eixoDianteras.x
						- eixoDiatero, eixoDianteras.x + eixoDiatero),
						(int) Util.intervalo(eixoDianteras.y - eixoDiatero,
								eixoDianteras.y + eixoDiatero));

				Point dest = new Point((int) Util.intervalo(
						piloto.getTrazeira().getX() - Util.intervalo(2.5, 6),
						(int) piloto.getTrazeira().getX()
								+ piloto.getTrazeira().getWidth()
								+ Util.intervalo(2.5, 6)),
						(int) Util.intervalo(
								piloto.getTrazeira().getY()
										- Util.intervalo(2.5, 6), piloto
										.getTrazeira().getY()
										+ piloto.getTrazeira().getHeight()
										+ Util.intervalo(2.5, 6)));
				double max = 4;
				if (piloto.getNoAtual().verificaCruvaAlta())
					max = 2;
				if (piloto.getNoAtual().verificaCruvaBaixa()
						|| piloto.getPtosBox() != 0)
					max = 1;
				Point destN = GeoUtil.calculaPonto(
						GeoUtil.calculaAngulo(origem, dest, 90),
						(int) Util.intervalo(width * .25, width * max), origem);

				g2d.drawLine(Util.inte(origem.x * zoom),
						Util.inte(origem.y * zoom), Util.inte(destN.x * zoom),
						Util.inte(destN.y * zoom));
			}
		}
		g2d.setStroke(trilho);
		if (piloto.isAgressivo()
				&& piloto.getCarro().getGiro() == Carro.GIRO_MAX_VAL
				&& !controleJogo.isChovendo() && piloto.getVelocidade() != 0
				&& Math.random() > .955) {
			mapaFaiscas.put(piloto, piloto);
			g2d.setColor(Color.YELLOW);
			g2d.setStroke(strokeFaisca);
			for (int i = 0; i < 15; i++) {
				Point origem = new Point((int) Util.intervalo(eixoDianteras.x
						- eixo, eixoDianteras.x + eixo), (int) Util.intervalo(
						eixoDianteras.y - eixo, eixoDianteras.y + eixo));

				Point dest = new Point((int) Util.intervalo(piloto
						.getTrazeira().getX() - Util.intervalo(2.5, 15), piloto
						.getTrazeira().getX()
						+ piloto.getTrazeira().getWidth()
						+ Util.intervalo(2.5, 15)), (int) Util.intervalo(
						piloto.getTrazeira().getY() - Util.intervalo(2.5, 15),
						piloto.getTrazeira().getY()
								+ piloto.getTrazeira().getHeight()
								+ Util.intervalo(2.5, 15)));
				Point destN = GeoUtil.calculaPonto(
						GeoUtil.calculaAngulo(origem, dest, 90),
						(int) Util.intervalo(width * .2, width), origem);
				Point2D.Double trazCarD = new Point2D.Double(piloto
						.getTrazeira().getCenterX(), piloto.getTrazeira()
						.getCenterY());
				g2d.fillOval(Util.inte(trazCarD.x * zoom),
						Util.inte(trazCarD.y * zoom), Util.inte(5 * zoom),
						Util.inte(5 * zoom));
				g2d.drawLine(Util.inte(dest.x * zoom),
						Util.inte(dest.y * zoom), Util.inte(destN.x * zoom),
						Util.inte(destN.y * zoom));
			}
			g2d.setStroke(trilho);
		} else {
			mapaFaiscas.put(piloto, null);
		}

		/**
		 * DEBUG
		 */
		if (Logger.ativo) {
			GeneralPath generalPath = new GeneralPath(rectangle);

			AffineTransform affineTransformRect = AffineTransform
					.getScaleInstance(zoom, zoom);
			affineTransformRect.setToRotation(rad, rectangle.getCenterX(),
					rectangle.getCenterY());
			g2d.setColor(new Color(255, 0, 0, 140));
			g2d.setColor(Color.BLACK);
			Point2D.Double frenteCarD = new Point2D.Double(piloto.getDiateira()
					.getCenterX(), piloto.getDiateira().getCenterY());
			Point2D.Double trazCarD = new Point2D.Double(piloto.getTrazeira()
					.getCenterX(), piloto.getTrazeira().getCenterY());
			g2d.setColor(Color.GREEN);
			g2d.fillOval(Util.inte(frenteCarD.x * zoom),
					Util.inte(frenteCarD.y * zoom), Util.inte(5 * zoom),
					Util.inte(5 * zoom));
			g2d.fillOval(Util.inte(trazCarD.x * zoom),
					Util.inte(trazCarD.y * zoom), Util.inte(5 * zoom),
					Util.inte(5 * zoom));
			if (posisAtual != null) {
				g2d.setColor(Color.MAGENTA);
				g2d.fillOval(Util.inte(posisAtual.x * zoom),
						Util.inte(posisAtual.y * zoom), Util.inte(5 * zoom),
						Util.inte(5 * zoom));
			}
			if (posisRec != null) {
				g2d.setColor(Color.CYAN);
				g2d.fillOval(Util.inte(posisRec.x * zoom),
						Util.inte(posisRec.y * zoom), Util.inte(5 * zoom),
						Util.inte(5 * zoom));
			}
		}

	}

	private void desenhaBoxesLegado(Graphics2D g2d) {
		if (limitesViewPort == null) {
			return;
		}
		for (int i = 0; i < 12; i++) {
			if (boxParada[i] == null) {
				break;
			}

			if (!limitesViewPort.intersects(boxParada[i].getBounds2D())) {
				continue;
			}
			if (i > (controleJogo.getCarrosBox().size() - 1)) {
				break;
			}
			g2d.setColor(Color.LIGHT_GRAY);
			g2d.fill(boxParada[i]);

			Carro carro = (Carro) controleJogo.getCarrosBox().get(i);
			g2d.setColor(carro.getCor1());
			g2d.fill(boxCor1[i]);
			g2d.setColor(carro.getCor2());
			g2d.fill(boxCor2[i]);
		}
	}

	private void desenhaGrid(Graphics2D g2d) {
		if (limitesViewPort == null) {
			return;
		}
		for (int i = 0; i < 24; i++) {
			if (grid[i] == null
					|| !limitesViewPort.intersects(grid[i].getBounds2D())) {
				continue;
			}
			if (circuito != null && circuito.isUsaBkg()) {
				BufferedImage buffer = (BufferedImage) gridImg.get(i);
				double meix = (gridCarro.getWidth() / 2) * zoom;
				double meiy = (gridCarro.getHeight() / 2) * zoom;
				g2d.drawImage(buffer,
						(int) (grid[i].getBounds().getCenterX() - meix),
						(int) (grid[i].getBounds().getCenterY() - meiy), null);
			} else {
				g2d.setColor(Color.white);
				g2d.fill(grid[i]);
				g2d.setColor(Color.lightGray);
				g2d.fill(asfaltoGrid[i]);
			}

		}

	}

	private void desenhaLargadaLegado(Graphics2D g2d) {
		No n1 = (No) circuito.getPistaFull().get(0);
		No n2 = (No) circuito.getPistaFull().get(20);
		Point p1 = new Point(Util.inte(n1.getPoint().x * zoom), Util.inte(n1
				.getPoint().y * zoom));
		Point p2 = new Point(Util.inte(n2.getPoint().x * zoom), Util.inte(n2
				.getPoint().y * zoom));
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

	private void desenhaPistaBoxLegado(Graphics2D g2d) {
		g2d.setColor(Color.LIGHT_GRAY);
		g2d.setStroke(box);
		No oldNo = null;
		for (Iterator iter = circuito.getBoxKey().iterator(); iter.hasNext();) {
			No no = (No) iter.next();
			if (oldNo == null) {
				oldNo = no;
			} else {
				g2d.drawLine(Util.inte(oldNo.getX() * zoom),
						Util.inte(oldNo.getY() * zoom),
						Util.inte(no.getX() * zoom),
						Util.inte(no.getY() * zoom));

				oldNo = no;
			}
		}
		No noFinal = (No) circuito.getBoxKey().get(
				circuito.getBoxKey().size() - 1);

		g2d.drawLine(Util.inte(oldNo.getX() * zoom),
				Util.inte(oldNo.getY() * zoom),
				Util.inte(noFinal.getX() * zoom),
				Util.inte(noFinal.getY() * zoom));
	}

	private void desenhaTintaPistaEZebraLegado(Graphics2D g2d) {
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
				g2d.drawLine(Util.inte(oldNo.getX() * zoom),
						Util.inte(oldNo.getY() * zoom),
						Util.inte(no.getX() * zoom),
						Util.inte(no.getY() * zoom));
				if (No.CURVA_ALTA.equals(oldNo.getTipo())
						|| No.CURVA_BAIXA.equals(oldNo.getTipo())) {
					g2d.setColor(Color.RED);
					g2d.setStroke(zebra);
					g2d.drawLine(Util.inte(oldNo.getX() * zoom),
							Util.inte(oldNo.getY() * zoom),
							Util.inte(no.getX() * zoom),
							Util.inte(no.getY() * zoom));

				}
				oldNo = no;
			}
		}
		No noFinal = (No) circuito.getPistaKey().get(0);
		g2d.drawLine(Util.inte(oldNo.getX() * zoom),
				Util.inte(oldNo.getY() * zoom),
				Util.inte(noFinal.getX() * zoom),
				Util.inte(noFinal.getY() * zoom));
	}

	protected void atualizaVarZoom() {
		Logger.logar("atualizaVarZoom() " + zoom);
		if (!circuito.isUsaBkg()) {
			larguraPistaPixeis = Util.inte(176 * larguraPista * zoom);
			pista = new BasicStroke(larguraPistaPixeis, BasicStroke.CAP_ROUND,
					BasicStroke.JOIN_ROUND);
			pistaTinta = new BasicStroke(Util.inte(larguraPistaPixeis * 1.05),
					BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
			box = new BasicStroke(Util.inte(larguraPistaPixeis * .4),
					BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
			zebra = new BasicStroke(Util.inte(larguraPistaPixeis * 1.05),
					BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10f,
					new float[] { 10, 10 }, 0);
			gerarBoxes();
		}
		gerarGrid();
	}

	private void gerarBoxes() {
		int paradas = circuito.getParadaBoxIndex();
		for (int i = 0; i < 12; i++) {
			int iP = paradas + Util.inte(Carro.LARGURA * 1.5 * i)
					+ Carro.LARGURA;
			No n1 = (No) circuito.getBoxFull().get(iP - Carro.MEIA_LARGURA);
			No nM = (No) circuito.getBoxFull().get(iP);
			No n2 = (No) circuito.getBoxFull().get(iP + Carro.MEIA_LARGURA);
			Point p1 = new Point(Util.inte(n1.getPoint().x * zoom),
					Util.inte(n1.getPoint().y * zoom));
			Point pm = new Point(Util.inte(nM.getPoint().x * zoom),
					Util.inte(nM.getPoint().y * zoom));
			Point p2 = new Point(Util.inte(n2.getPoint().x * zoom),
					Util.inte(n2.getPoint().y * zoom));
			double calculaAngulo = GeoUtil.calculaAngulo(p1, p2, 0);
			Rectangle2D rectangle = new Rectangle2D.Double(
					(pm.x - (Carro.MEIA_LARGURA)),
					(pm.y - (Carro.MEIA_ALTURA)), (Carro.LARGURA),
					(Carro.ALTURA));

			Point cima = GeoUtil.calculaPonto(
					calculaAngulo,
					Util.inte(Carro.ALTURA
							* controleJogo.getCircuito()
									.getMultiplicadorLarguraPista() * zoom),
					new Point(Util.inte(rectangle.getCenterX()), Util
							.inte(rectangle.getCenterY())));
			Point baixo = GeoUtil.calculaPonto(
					calculaAngulo + 180,
					Util.inte(Carro.ALTURA
							* controleJogo.getCircuito()
									.getMultiplicadorLarguraPista() * zoom),
					new Point(Util.inte(rectangle.getCenterX()), Util
							.inte(rectangle.getCenterY())));
			Point cimaBoxC1 = GeoUtil.calculaPonto(
					calculaAngulo,
					Util.inte((Carro.ALTURA) * 3.5 * zoom),
					new Point(Util.inte(rectangle.getCenterX()), Util
							.inte(rectangle.getCenterY())));
			Point baixoBoxC1 = GeoUtil.calculaPonto(
					calculaAngulo + 180,
					Util.inte((Carro.ALTURA) * 3.3 * zoom),
					new Point(Util.inte(rectangle.getCenterX()), Util
							.inte(rectangle.getCenterY())));
			Point cimaBoxC2 = GeoUtil.calculaPonto(
					calculaAngulo,
					Util.inte((Carro.ALTURA) * 3.5 * zoom),
					new Point(Util.inte(rectangle.getCenterX()), Util
							.inte(rectangle.getCenterY())));
			Point baixoBoxC2 = GeoUtil.calculaPonto(
					calculaAngulo + 180,
					Util.inte((Carro.ALTURA) * 3.3 * zoom),
					new Point(Util.inte(rectangle.getCenterX()), Util
							.inte(rectangle.getCenterY())));

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
			affineTransformRect.setToRotation(rad, retC1.getCenterX(),
					retC1.getCenterY());
			boxCor1[i] = generalPath
					.createTransformedShape(affineTransformRect);

			generalPath = new GeneralPath(retC2);
			affineTransformRect.setToRotation(rad, retC2.getCenterX(),
					retC2.getCenterY());
			boxCor2[i] = generalPath
					.createTransformedShape(affineTransformRect);
		}

	}

	private void gerarGrid() {
		gridImg.clear();
		for (int i = 0; i < 24; i++) {
			int iP = 50 + Util.inte((Carro.LARGURA * 0.8) * i);
			No n1 = (No) circuito.getPistaFull().get(
					circuito.getPistaFull().size() - iP - Carro.MEIA_LARGURA);
			No nM = (No) circuito.getPistaFull().get(
					circuito.getPistaFull().size() - iP);
			No n2 = (No) circuito.getPistaFull().get(
					circuito.getPistaFull().size() - iP + Carro.MEIA_LARGURA);
			Point p1 = new Point(Util.inte(n1.getPoint().x * zoom),
					Util.inte(n1.getPoint().y * zoom));
			Point pm = new Point(Util.inte(nM.getPoint().x * zoom),
					Util.inte(nM.getPoint().y * zoom));
			Point p2 = new Point(Util.inte(n2.getPoint().x * zoom),
					Util.inte(n2.getPoint().y * zoom));
			double calculaAngulo = GeoUtil.calculaAngulo(p1, p2, 0);
			Rectangle2D rectangle = new Rectangle2D.Double(
					(pm.x - (Carro.MEIA_LARGURA)),
					(pm.y - (Carro.MEIA_ALTURA)), (Carro.LARGURA),
					(Carro.ALTURA));

			Point cima = GeoUtil.calculaPonto(
					calculaAngulo,
					Util.inte(Carro.ALTURA
							* controleJogo.getCircuito()
									.getMultiplicadorLarguraPista() * zoom),
					new Point(Util.inte(rectangle.getCenterX()), Util
							.inte(rectangle.getCenterY())));
			Point baixo = GeoUtil.calculaPonto(
					calculaAngulo + 180,
					Util.inte(Carro.ALTURA
							* controleJogo.getCircuito()
									.getMultiplicadorLarguraPista() * zoom),
					new Point(Util.inte(rectangle.getCenterX()), Util
							.inte(rectangle.getCenterY())));
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
			boolean naoDesenha = false;
			if (circuito.getObjetos() != null) {
				for (Iterator iterator = circuito.getObjetos().iterator(); iterator
						.hasNext();) {
					ObjetoPista objetoPista = (ObjetoPista) iterator.next();
					if (objetoPista instanceof ObjetoTransparencia) {
						ObjetoTransparencia objetoTransparencia = (ObjetoTransparencia) objetoPista;
						if (objetoTransparencia.obterArea().intersects(
								generalPath.getBounds())) {
							naoDesenha = true;
						}
					} else {
						continue;
					}
				}
			}
			if (naoDesenha) {
				grid[i] = null;
			} else
				grid[i] = generalPath
						.createTransformedShape(affineTransformRect);
			if (circuito != null && circuito.isUsaBkg()) {
				AffineTransform afZoom = new AffineTransform();
				AffineTransform afRotate = new AffineTransform();
				afZoom.setToScale(zoom, zoom);
				rad = Math.toRadians((double) calculaAngulo + 180);
				int width = Util.inte(gridCarro.getWidth() * zoom);
				int height = Util.inte(gridCarro.getHeight() * zoom);
				afRotate.setToRotation(rad, width / 2, height / 2);
				BufferedImage rotateBuffer = new BufferedImage(width, height,
						BufferedImage.TYPE_INT_ARGB);
				BufferedImage zoomBuffer = new BufferedImage(width, height,
						BufferedImage.TYPE_INT_ARGB);
				AffineTransformOp op = new AffineTransformOp(afZoom,
						AffineTransformOp.TYPE_BILINEAR);
				op.filter(gridCarro, zoomBuffer);
				AffineTransformOp op2 = new AffineTransformOp(afRotate,
						AffineTransformOp.TYPE_BILINEAR);
				op2.filter(zoomBuffer, rotateBuffer);
				gridImg.add(rotateBuffer);
			}

			iP += 5;
			n1 = (No) circuito.getPistaFull().get(
					circuito.getPistaFull().size() - iP - Carro.MEIA_LARGURA);
			nM = (No) circuito.getPistaFull().get(
					circuito.getPistaFull().size() - iP);
			n2 = (No) circuito.getPistaFull().get(
					circuito.getPistaFull().size() - iP + Carro.MEIA_LARGURA);
			p1 = new Point(Util.inte(n1.getPoint().x * zoom), Util.inte(n1
					.getPoint().y * zoom));
			pm = new Point(Util.inte(nM.getPoint().x * zoom), Util.inte(nM
					.getPoint().y * zoom));
			p2 = new Point(Util.inte(n2.getPoint().x * zoom), Util.inte(n2
					.getPoint().y * zoom));
			calculaAngulo = GeoUtil.calculaAngulo(p1, p2, 0);
			rectangle = new Rectangle2D.Double((pm.x - (Carro.MEIA_LARGURA)),
					(pm.y - (Carro.MEIA_ALTURA)), (Carro.LARGURA),
					(Carro.ALTURA));

			cima = GeoUtil.calculaPonto(
					calculaAngulo,
					Util.inte(Carro.ALTURA
							* controleJogo.getCircuito()
									.getMultiplicadorLarguraPista() * zoom),
					new Point(Util.inte(rectangle.getCenterX()), Util
							.inte(rectangle.getCenterY())));
			baixo = GeoUtil.calculaPonto(
					calculaAngulo + 180,
					Util.inte(Carro.ALTURA
							* controleJogo.getCircuito()
									.getMultiplicadorLarguraPista() * zoom),
					new Point(Util.inte(rectangle.getCenterX()), Util
							.inte(rectangle.getCenterY())));
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
		if (!oldp.equals(p)) {
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
		PainelCircuito.this.newP = newP;
		Point oldp = scrollPane.getViewport().getViewPosition();
		PainelCircuito.this.oldP = oldp;
		if (circuito.isUsaBkg() && backGround != null
				&& limitesViewPort != null) {
			synchronized (backGround) {
				if ((p.x + limitesViewPort.width) > (backGround.getWidth() * zoom)) {
					p.x = Util.inte((backGround.getWidth() * zoom)
							- limitesViewPort.width);
				}
				if ((p.y + limitesViewPort.height) > (backGround.getHeight() * zoom)) {
					p.y = Util.inte((backGround.getHeight() * zoom)
							- limitesViewPort.height);
				}
			}
		}
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

	private void desenhaPistaLegado(Graphics2D g2d) {
		No oldNo = null;
		g2d.setColor(Color.LIGHT_GRAY);
		g2d.setStroke(pista);
		for (Iterator iter = circuito.getPistaKey().iterator(); iter.hasNext();) {
			No no = (No) iter.next();
			if (oldNo == null) {
				oldNo = no;
			} else {
				g2d.drawLine(Util.inte(oldNo.getX() * zoom),
						Util.inte(oldNo.getY() * zoom),
						Util.inte(no.getX() * zoom),
						Util.inte(no.getY() * zoom));

				oldNo = no;
			}
		}

		No noFinal = (No) circuito.getPistaKey().get(0);
		g2d.drawLine(Util.inte(oldNo.getX() * zoom),
				Util.inte(oldNo.getY() * zoom),
				Util.inte(noFinal.getX() * zoom),
				Util.inte(noFinal.getY() * zoom));
	}

	private void desenhaChuva(Graphics2D g2d) {
		if ((Clima.NUBLADO.equals(controleJogo.getClima()) || Clima.CHUVA
				.equals(controleJogo.getClima())) && limitesViewPort() != null) {
			g2d.setColor(nublado);
			g2d.fill(limitesViewPort().getBounds());
		}
		if (!controleJogo.isChovendo())
			return;
		Point p1 = new Point(0, 0);
		Point p2 = new Point(0, 0);
		g2d.setColor(lightWhiteRain);
		for (int i = 0; i < limitesViewPort.getWidth(); i += 20) {
			for (int j = 0; j < limitesViewPort.getHeight(); j += 20) {
				if (Math.random() > .8) {

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

	private void desenhaFaiscaLateral(Graphics2D g2d, Point p) {
		if (p == null) {
			return;
		}
		if (Math.random() > .5) {
			return;
		}
		Color color = g2d.getColor();
		g2d.setColor(Color.YELLOW);
		for (int i = 0; i < 7; i++) {
			if (Math.random() > .5) {
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
		if ((pilotoSelecionado != null)) {
			desenhaNomePilotoSelecionado(pilotoSelecionado, g2d);
			if (controleJogo.getNumVoltaAtual() > 0)
				desenhaCarroSelecionado(pilotoSelecionado, g2d);
			desenhaProblemasCarroSelecionado(pilotoSelecionado, g2d);
			if (controleJogo.getCircuito() != null
					&& (controleJogo.getCircuito().isNoite() || controleJogo
							.getCircuito().isUsaBkg())) {
				g2d.setColor(transpMenus);
				g2d.fillRoundRect(limitesViewPort.x
						+ (limitesViewPort.width - 130), limitesViewPort.y + 2,
						128, 420, 10, 10);
			} else {
				g2d.setColor(blu);
				g2d.fillRoundRect(limitesViewPort.x
						+ (limitesViewPort.width - 110), limitesViewPort.y + 2,
						105, 240, 10, 10);
			}
			g2d.setColor(Color.black);
			int ptoOri = limitesViewPort.x + limitesViewPort.width - 120;
			int yBase = limitesViewPort.y;
			yBase += 15;
			g2d.drawString(
					Lang.msg(pilotoSelecionado.getCarro().getTipoPneu()),
					ptoOri, yBase);
			yBase += 15;
			g2d.drawString(Lang.msg(pilotoSelecionado.getCarro().getAsa()),
					ptoOri, yBase);
			yBase += 15;
			g2d.drawString(
					Lang.msg("068") + pilotoSelecionado.getQtdeParadasBox(),
					ptoOri, yBase);
			yBase += 15;
			if (pilotoSelecionado.isBox()) {
				if (contBox1) {
					g2d.setColor(yel);
					g2d.fillRoundRect(ptoOri - 5, yBase - 12, 90, 16, 10, 10);
					g2d.setColor(Color.black);
				}
				contBox1 = !contBox1;
			}
			g2d.drawString(
					Lang.msg("069")
							+ (pilotoSelecionado.isBox() ? Lang.msg("SIM")
									: Lang.msg("NAO")), ptoOri, yBase);

			String plider = "";
			if (pilotoSelecionado.getPosicao() == 1) {
				plider = Lang.msg("Lider");
				g2d.setColor(Color.BLUE);
			} else if (controleJogo.verificaCampeonatoComRival()) {
				plider = controleJogo
						.calculaSegundosParaRival(pilotoSelecionado);
				if (plider.startsWith("-")) {
					g2d.setColor(Color.BLUE);
				} else {
					g2d.setColor(Color.RED);
				}
			} else {
				controleJogo.calculaSegundosParaLider(pilotoSelecionado);
				plider = pilotoSelecionado.getSegundosParaLider();
			}

			yBase += 15;

			g2d.drawString(
					(controleJogo.verificaCampeonatoComRival() ? Lang
							.msg("rival") : Lang.msg("070")) + plider, ptoOri,
					yBase);
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
			g2d.setColor(Color.black);
			if (!pilotoSelecionado.isAutoPos()) {
				if (tracadoManual) {
					g2d.setColor(yel);
					g2d.fillRoundRect(ptoOri - 5, yBase - 12, 120, 16, 10, 10);
					g2d.setColor(Color.black);
				}
				tracadoManual = !tracadoManual;
				g2d.drawString(Lang.msg("tracadoManual"), ptoOri, yBase);
			} else {
				g2d.drawString(Lang.msg("tracadoAutomatico"), ptoOri, yBase);
			}

			yBase += 15;
			g2d.setColor(PainelTabelaPosicoes.jogador);
			int largura = 0;
			String msg = "F9 : " + pilotoSelecionado.getNome();
			for (int i = 0; i < msg.length(); i++) {
				largura += g2d.getFontMetrics().charWidth(msg.charAt(i));
			}
			g2d.fillRoundRect(ptoOri - 5, yBase - 12, largura + 10, 16, 10, 10);
			g2d.setColor(Color.black);
			g2d.drawString(Lang.msg("301",
					new String[] { pilotoSelecionado.getNome() }), ptoOri,
					yBase);

			yBase += 15;
			g2d.setColor(Color.black);
			msg = "F10 : "
					+ Lang.msg("som")
					+ (ControleSom.somLigado ? Lang.msg("SIM") : Lang
							.msg("NAO"));
			g2d.drawString(msg, ptoOri, yBase);

			yBase += 15;
			g2d.drawString(Lang.msg("265"), ptoOri, yBase);
			yBase += 15;
			g2d.setColor(Color.black);
			g2d.drawString(Lang.msg("078"), ptoOri, yBase);

			yBase += 15;

			if (pilotoSelecionado.getCarro().getCargaKers() > 0
					&& pilotoSelecionado.isAtivarKers()
					&& pilotoSelecionado.getCarro().getCargaKers() > 0) {
				g2d.setColor(gre);
				g2d.fillRoundRect(ptoOri - 5, yBase - 12, 120, 16, 10, 10);
			}

			g2d.setColor(Color.black);
			msg = "K : "
					+ Lang.msg("kers")
					+ " "
					+ (pilotoSelecionado.getCarro().getCargaKers() > 0
							&& pilotoSelecionado.isAtivarKers()
							&& pilotoSelecionado.getCarro().getCargaKers() > 0 ? Lang
							.msg("SIM") : Lang.msg("NAO"));
			g2d.drawString(msg, ptoOri, yBase);

			yBase += 15;

			if (controleJogo.isDrs()
					&& Carro.MENOS_ASA.equals(pilotoSelecionado.getCarro()
							.getAsa())) {
				g2d.setColor(gre);
				g2d.fillRoundRect(ptoOri - 5, yBase - 12, 120, 16, 10, 10);
			}

			g2d.setColor(Color.black);
			msg = "D : "
					+ Lang.msg("drs")
					+ " "
					+ (controleJogo.isDrs()
							&& Carro.MENOS_ASA.equals(pilotoSelecionado
									.getCarro().getAsa()) ? Lang.msg("SIM")
							: Lang.msg("NAO"));
			g2d.drawString(msg, ptoOri, yBase);

			yBase += 15;
			g2d.drawString(Lang.msg("220"), ptoOri, yBase);

			if ((pilotoSelecionado.getNumeroVolta() > 0)) {
				Volta voltaPiloto = controleJogo
						.obterMelhorVolta(pilotoSelecionado);

				if (voltaPiloto != null) {
					g2d.setColor(Color.BLUE);
					yBase += 15;
					g2d.drawString(
							Lang.msg("079")
									+ voltaPiloto.obterTempoVoltaFormatado(),
							ptoOri, yBase);
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
					color = new Color(contVolta * 20, contVolta * 20,
							contVolta * 20);
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
		if (pilotoSelecionado.getCarro().getDurabilidadeAereofolio() <= 3) {
			g2d.setColor(Color.yellow);
			// bico
			g2d.drawImage(carroimgDano, limitesViewPort.x + 65,
					limitesViewPort.y + 10, null);
			if (piscaBico) {
				piscaBico = !piscaBico;
				return;
			}
			piscaBico = !piscaBico;
			g2d.fillOval(limitesViewPort.x + 68, limitesViewPort.y + 26, 15, 15);

		}
		if ((dano == null || "".equals(dano))
				&& motor > 25
				&& porcentComb > 25
				&& pneus > 25
				&& pilotoSelecionado.getCarro().getTemperaturaMotor() != pilotoSelecionado
						.getCarro().getPotencia() / 2)
			return;

		g2d.drawImage(carroimgDano, limitesViewPort.x + 65,
				limitesViewPort.y + 10, null);
		if (piscaDanos) {
			piscaDanos = !piscaDanos;
			return;
		}
		piscaDanos = !piscaDanos;
		if (porcentComb <= 25) {
			g2d.drawImage(fuel.getImage(),
					limitesViewPort.x + carroimgDano.getWidth() + 15,
					limitesViewPort.y + 10, null);
		}

		if (Carro.PERDEU_AEREOFOLIO.equals(pilotoSelecionado.getCarro()
				.getDanificado())) {

			g2d.setColor(Color.red);
			// bico
			g2d.fillOval(limitesViewPort.x + 68, limitesViewPort.y + 26, 15, 15);
		}

		if (Carro.PNEU_FURADO.equals(pilotoSelecionado.getCarro()
				.getDanificado())) {
			g2d.setColor(Color.red);
			// Roda diantera
			g2d.fillOval(limitesViewPort.x + 88, limitesViewPort.y + 24, 18, 18);
			// Roda trazeira
			g2d.fillOval(limitesViewPort.x + 192, limitesViewPort.y + 24, 18,
					18);
		} else if (pneus <= 25) {
			g2d.setColor(yel);
			// Roda diantera
			g2d.fillOval(limitesViewPort.x + 88, limitesViewPort.y + 24, 18, 18);
			// Roda trazeira
			g2d.fillOval(limitesViewPort.x + 192, limitesViewPort.y + 24, 18,
					18);
		}
		if (Carro.EXPLODIU_MOTOR.equals(pilotoSelecionado.getCarro()
				.getDanificado())) {
			g2d.setColor(Color.red);
			// motor
			g2d.fillOval(limitesViewPort.x + 158, limitesViewPort.y + 12, 15,
					15);
		} else if (motor <= 25
				|| pilotoSelecionado.getCarro().getTemperaturaMotor() == pilotoSelecionado
						.getCarro().getPotencia() / 2) {
			g2d.setColor(yel);
			g2d.fillOval(limitesViewPort.x + 158, limitesViewPort.y + 12, 15,
					15);
		}
		if (Carro.BATEU_FORTE.equals(pilotoSelecionado.getCarro()
				.getDanificado())) {
			g2d.setColor(Color.red);
			// motor
			g2d.fillRoundRect(limitesViewPort.x + 75, limitesViewPort.y + 18,
					135, 20, 15, 15);
		}

	}

	private void desenhaContadorVoltas(Graphics2D g2d) {
		g2d.setColor(luzApagada);
		String txt = controleJogo.getCircuito().getNome() + " "
				+ controleJogo.getNumVoltaAtual() + "/"
				+ controleJogo.totalVoltasCorrida();

		int largura = 0;
		for (int i = 0; i < txt.length(); i++) {
			largura += g2d.getFontMetrics().charWidth(txt.charAt(i));
		}
		g2d.fillRoundRect(limitesViewPort.x + (limitesViewPort.width / 2)
				- (largura / 2), limitesViewPort.y + 10, largura + 10, 20, 15,
				15);
		g2d.setColor(Color.BLACK);
		g2d.drawString(txt,
				(limitesViewPort.x + (limitesViewPort.width / 2) + 6)
						- (largura / 2), limitesViewPort.y + 24);
		if (circuito.isUsaBkg() && backGround == null) {
			txt = Lang.msg("carregandoBackground");
			largura = 0;
			for (int i = 0; i < txt.length(); i++) {
				largura += g2d.getFontMetrics().charWidth(txt.charAt(i));
			}
			g2d.setColor(luzApagada);
			g2d.fillRoundRect(limitesViewPort.x + (limitesViewPort.width / 2)
					- (largura / 2), limitesViewPort.y + 30, largura + 10, 20,
					15, 15);
			g2d.setColor(Color.BLACK);
			g2d.drawString(txt, (limitesViewPort.x
					+ (limitesViewPort.width / 2) + 6)
					- (largura / 2), limitesViewPort.y + 44);

		}
	}

	private void desenhaQualificacao(Graphics2D g2d) {
		if (pilotQualificacao == null) {
			return;
		}
		if (pointQualificacao == null) {
			return;
		}
		if (circuito != null && circuito.isUsaBkg() && backGround == null) {
			return;
		}
		BufferedImage carroimg = controleJogo.obterCarroLado(pilotQualificacao);
		if (circuito != null && circuito.isUsaBkg()) {
			g2d.setColor(transpMenus);
			g2d.fillRoundRect(pointQualificacao.x - 5, pointQualificacao.y - 5,
					carroimg.getWidth() + 5, carroimg.getHeight() + 5, 15, 15);
		}
		g2d.drawImage(carroimg, null, pointQualificacao.x, pointQualificacao.y);
		int newY = limitesViewPort.y;
		for (Iterator iter = mapDesenharQualificacao.keySet().iterator(); iter
				.hasNext();) {
			Piloto piloto = (Piloto) iter.next();
			Point point = (Point) mapDesenharQualificacao.get(piloto);
			carroimg = controleJogo.obterCarroLado(piloto);
			newY = carroimg.getHeight() > 36 ? point.y
					- (carroimg.getHeight() - 36) : point.y;
			if (circuito != null && circuito.isUsaBkg()) {
				g2d.setColor(transpMenus);
				g2d.fillRoundRect(limitesViewPort.x + point.x - 5,
						limitesViewPort.y + newY - 5, carroimg.getWidth() + 5,
						carroimg.getHeight() + 5, 15, 15);
			}
			g2d.drawImage(carroimg, null, limitesViewPort.x + point.x,
					limitesViewPort.y + newY);
			String txt = piloto.getNome()
					+ " - "
					+ ControleEstatisticas.formatarTempo(
							piloto.getCiclosVoltaQualificacao(),
							controleJogo.getTempoCiclo());

			int maior = txt.length();

			Color c2 = piloto.getCarro().getCor2();
			if (c2 != null) {
				c2 = c2.brighter();
				g2d.setColor(new Color(c2.getRed(), c2.getGreen(),
						c2.getBlue(), 170));
			}
			Point pt = null;
			if (piloto.getPosicao() % 2 == 0) {
				pt = new Point(point.x + 120, point.y + 20);

			} else {
				pt = new Point(point.x - 120, point.y + 20);
			}
			g2d.fillRoundRect(limitesViewPort.x + pt.x - 10, limitesViewPort.y
					+ pt.y - 15, maior * 7, 20, 15, 15);

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

	private void desenhaCarroSelecionado(Piloto psel, Graphics2D g2d) {
		BufferedImage carroimg = null;
		int carSelX = limitesViewPort.x;
		int carSelY = limitesViewPort.y + limitesViewPort.height - 35;
		int bounce = 0;
		int newY = 0;
		Carro carroFrente = controleJogo.obterCarroNaFrente(psel);
		if (carroFrente != null) {
			carroimg = controleJogo.obterCarroLado(carroFrente.getPiloto());
			carSelX += carroimg.getWidth() / 2;
			bounce = calculaBounce(carroFrente);
			double diff = controleJogo.calculaSegundosParaProximoDouble(psel);
			int dstX = limitesViewPort.x + (limitesViewPort.width / 4);
			int dstY = carSelY + 20;
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
			g2d.setColor(this.transpMenus);
			g2d.fillRoundRect(carSelX - 5, carSelY - 5,
					carroimg.getWidth() + 5, carroimg.getHeight() + 5, 15, 15);

			if (diff >= 3) {
				g2d.setColor(gre);
			} else if (diff < 3 && diff > 1) {
				g2d.setColor(yel);
			} else if (diff <= 1) {
				g2d.setColor(red);
			}
			if (Math.random() > 0.5) {
				carSelX += bounce;
			} else {
				carSelX -= bounce;
			}
			newY = carroimg.getHeight() > 36 ? carSelY
					- (carroimg.getHeight() - 36) : carSelY;
			if (!carroFrente.getPiloto().isDesqualificado()
					&& mapaFaiscas.get(carroFrente.getPiloto()) != null) {
				desenhaFaiscaLateral(g2d,
						new Point(carSelX + carroimg.getWidth() - 10, newY
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
		carroimg = controleJogo.obterCarroLado(psel);
		carSelX = limitesViewPort.x + (limitesViewPort.width / 2)
				- (carroimg.getWidth() / 2);
		carSelY = limitesViewPort.y + limitesViewPort.height - 35;
		bounce = calculaBounce(psel.getCarro());
		g2d.setColor(this.transpMenus);
		g2d.fillRoundRect(carSelX - 5, carSelY - 5, carroimg.getWidth() + 5,
				carroimg.getHeight() + 5, 15, 15);
		if (Math.random() > 0.5) {
			carSelX += bounce;
		} else {
			carSelX -= bounce;
		}
		newY = carroimg.getHeight() > 36 ? carSelY
				- (carroimg.getHeight() - 36) : carSelY;
		if (!psel.isDesqualificado() && mapaFaiscas.get(psel) != null) {
			desenhaFaiscaLateral(g2d, new Point(carSelX + carroimg.getWidth()
					- 10, newY + carroimg.getHeight() / 2));
		}

		g2d.drawImage(carroimg, null, carSelX, newY);

		Carro carroAtraz = controleJogo.obterCarroAtraz(psel);
		if (carroAtraz != null) {
			carroimg = controleJogo.obterCarroLado(carroAtraz.getPiloto());
			carSelX = limitesViewPort.x + limitesViewPort.width
					+ -carroimg.getWidth() - carroimg.getWidth() / 2;

			bounce = calculaBounce(carroAtraz);

			int dstX = limitesViewPort.x + limitesViewPort.width
					+ -(limitesViewPort.width / 3);
			int dstY = carSelY + 20;
			double diff = controleJogo
					.calculaSegundosParaProximoDouble(carroAtraz.getPiloto());

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
			g2d.setColor(this.transpMenus);
			g2d.fillRoundRect(carSelX - 5, carSelY - 5,
					carroimg.getWidth() + 5, carroimg.getHeight() + 5, 15, 15);
			if (diff >= 3) {
				g2d.setColor(gre);
			} else if (diff < 3 && diff > 1) {
				g2d.setColor(yel);
			} else if (diff <= 1) {
				g2d.setColor(red);
			}
			if (Math.random() > 0.5) {
				carSelX += bounce;
			} else {
				carSelX -= bounce;
			}
			newY = carroimg.getHeight() > 36 ? carSelY
					- (carroimg.getHeight() - 36) : carSelY;
			if (!carroAtraz.getPiloto().isDesqualificado()
					&& mapaFaiscas.get(carroAtraz.getPiloto()) != null) {
				desenhaFaiscaLateral(g2d,
						new Point(carSelX + carroimg.getWidth() - 10, newY
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
			if (controleJogo.isSafetyCarNaPista()) {
				if (circuito != null && circuito.isUsaBkg()) {
					g2d.setColor(transpMenus);
					g2d.fillRoundRect(limitesViewPort.x
							+ (pointDesenhaSC.x - 5), limitesViewPort.y
							+ (pointDesenhaSC.y - 5), scimg.getWidth() + 10,
							scimg.getHeight() + 10, 15, 15);
				}
				g2d.drawImage(
						scimg,
						limitesViewPort.x
								+ (pointDesenhaSC.x + (Math.random() > 0.5 ? 1
										: -1)),
						(limitesViewPort.y + pointDesenhaSC.y + (Math.random() > 0.5 ? -1
								: 0)), null);
			}

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
			Point p1 = GeoUtil.calculaPonto(
					calculaAngulo,
					Util.inte(Carro.ALTURA
							* controleJogo.getCircuito()
									.getMultiplicadorLarguraPista()),
					new Point(Util.inte(rectangle.getCenterX()), Util
							.inte(rectangle.getCenterY())));
			Point p2 = GeoUtil.calculaPonto(
					calculaAngulo + 180,
					Util.inte(Carro.ALTURA
							* controleJogo.getCircuito()
									.getMultiplicadorLarguraPista()),
					new Point(Util.inte(rectangle.getCenterX()), Util
							.inte(rectangle.getCenterY())));
			if (safetyCar == null) {
				return;
			}
			if (safetyCar.getTracado() == 0) {
				carx = p.x - w2;
				cary = p.y - h2;
			}
			if (safetyCar.getTracado() == 1) {
				carx = Util.inte((p1.x - w2));
				cary = Util.inte((p1.y - h2));
			}
			if (safetyCar.getTracado() == 2) {
				carx = Util.inte((p2.x - w2));
				cary = Util.inte((p2.y - h2));
			}

			// carx = p.x - w2;
			// cary = p.y - h2;
			scx = carx + w2;
			scy = cary + h2;
			if (zoom > 0.3) {
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
				g2d.drawImage(rotateBuffer, Util.inte(carx * zoom),
						Util.inte(cary * zoom), null);
			}

			if (safetyCar.getNoAtual() == null) {
				return;
			}
			g2d.setColor(Color.LIGHT_GRAY);
			g2d.fillOval(Util.inte((Util.inte(scx * zoom) - 2)),
					Util.inte((Util.inte(scy * zoom) - 2)), 8, 8);
			if (!safetyCar.isVaiProBox()) {
				if (Math.random() > .5) {
					g2d.setColor(Color.YELLOW);
				} else {
					g2d.setColor(Color.BLACK);
				}
			} else
				g2d.setColor(Color.BLACK);
			g2d.drawOval(Util.inte((Util.inte(scx * zoom) - 2)),
					Util.inte((Util.inte(scy * zoom) - 2)), 8, 8);

		}
	}

	private void desenharClima(Graphics2D g2d) {
		if (!desenhaInfo) {
			return;
		}
		if (qtdeLuzesAcesas > 0) {
			return;
		}
		ImageIcon icon = (ImageIcon) gerenciadorVisual.getImgClima().getIcon();
		if (icon != null && pointDesenhaClima != null) {
			g2d.setColor(transpMenus);
			g2d.fillRoundRect(limitesViewPort.x + pointDesenhaClima.x - 5,
					pointDesenhaClima.y + limitesViewPort.y - 5,
					icon.getIconWidth() + 10, icon.getIconHeight() + 10, 15, 15);
			g2d.drawImage(icon.getImage(), limitesViewPort.x
					+ pointDesenhaClima.x, pointDesenhaClima.y
					+ limitesViewPort.y, null);
		}
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

		g2d.drawOval(Util.inte((piloto.getCarX() - 2) * zoom),
				Util.inte((piloto.getCarY() - 2) * zoom), 8, 8);

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
		String velo2 = null;
		if (ps.getVelocidade() == 1) {
			velo = null;
		}
		if (Logger.ativo) {

			int dist = ps.calculaDiffParaProximo(controleJogo);
			if (ps.getPosicao() == 1) {
				dist = 0;
			}
			velo = "M " + ps.getNovoModificador() + " I "
					+ ps.getNoAtual().getIndex() + " G "
					+ (int) (ps.getGanho()) + " V " + ps.getVelocidade()
					+ " D " + dist + " S " + ps.getStress() + " A "
					+ ps.getCarro().getDurabilidadeAereofolio() + " BX "
					+ ps.getPtosBox();
			velo2 = " DP " + ps.calculaDiffParaProximo(controleJogo) + " DA "
					+ ps.calculaDiffParaAnterior(controleJogo) + " K "
					+ ps.getCarro().getCargaKers() + " P "
					+ controleJogo.percetagemDeVoltaCompletada(ps) + " T "
					+ ps.getCarro().getTemperaturaMotor() + " IT "
					+ ps.getIndiceTracado() + " TR " + ps.getTracado() + " VT "
					+ ps.getTracadoAntigo();

		}

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

		g2d.fillRoundRect(Util.inte((pt.x * zoom) + 14),
				Util.inte((pt.y * zoom) - 50), largura, 25, 15, 15);
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
		g2d.drawLine(Util.inte((pt.x * zoom + 4)), Util.inte(pt.y * zoom),
				Util.inte((pt.x * zoom) + 13), Util.inte((pt.y * zoom) - 28));
		/**
		 * moldura inferior
		 */
		if (desenhaInfo && controleJogo.getNumVoltaAtual() > 0) {

			if (Carro.GIRO_MIN_VAL == ps.getCarro().getGiro()) {
				desenBarraGiro(g2d, true, gre, 5);
			}
			if (Carro.GIRO_NOR_VAL == ps.getCarro().getGiro()) {
				desenBarraGiro(g2d, false, gre, 5);
				desenBarraGiro(g2d, true, yel, 35);
			}
			if (Carro.GIRO_MAX_VAL == ps.getCarro().getGiro()) {
				desenBarraGiro(g2d, false, gre, 5);
				desenBarraGiro(g2d, false, yel, 35);
				desenBarraGiro(g2d, true, red, 65);
			}
		}
		if (controleJogo.isKers() && desenhaInfo
				&& controleJogo.getNumVoltaAtual() > 0)
			desenhaKers(g2d);
		if (controleJogo.isDrs() && desenhaInfo
				&& controleJogo.getNumVoltaAtual() > 0)
			desenhaDRS(g2d);

		if (ps.isBox()) {
			if (controleJogo.getCircuito() != null
					&& (controleJogo.getCircuito().isNoite() || controleJogo
							.getCircuito().isUsaBkg())) {
				g2d.setColor(transpMenus);
				g2d.fillRoundRect(limitesViewPort.x + 3,
						limitesViewPort.y + 238, 70, 90, 10, 10);
			}
			g2d.setColor(Color.BLACK);
			if (contBox2) {
				g2d.drawImage(fuel.getImage(), limitesViewPort.x + 5,
						limitesViewPort.y + 240, null);
			}
			Integer percent = ps.getQtdeCombustBox();
			if (percent != null && ps.isJogadorHumano())
				g2d.drawString(percent + "%", limitesViewPort.x + 5,
						limitesViewPort.y + 280);
			if (contBox2) {
				g2d.drawImage(tyre.getImage(), limitesViewPort.x + 5,
						limitesViewPort.y + 285, null);
			}
			contBox2 = !contBox2;
			String tpPneu = ps.getTipoPneuBox();
			if (tpPneu != null && ps.isJogadorHumano())
				g2d.drawString(Lang.msg(tpPneu), limitesViewPort.x + 5,
						limitesViewPort.y + 325);
		}

		if (desenhaInfo && velo != null) {
			g2d.setColor(c1);
			largura = 0;
			for (int i = 0; i < velo.length(); i++) {
				largura += g2d.getFontMetrics().charWidth(velo.charAt(i));
			}
			if (Logger.ativo) {
				if (velo2 != null) {
					int largura2 = 0;
					for (int i = 0; i < velo2.length(); i++) {
						largura2 += g2d.getFontMetrics().charWidth(
								velo2.charAt(i));
					}
					g2d.fillRoundRect(limitesViewPort.x + pointDesenhaVelo.x,
							limitesViewPort.y + pointDesenhaVelo.y + 163,
							largura2 + 10, 15, 15, 15);
				}

				g2d.fillRoundRect(limitesViewPort.x + pointDesenhaVelo.x,
						limitesViewPort.y + pointDesenhaVelo.y + 143,
						largura + 10, 15, 15, 15);

			} else
				g2d.fillRoundRect(limitesViewPort.x + pointDesenhaVelo.x,
						limitesViewPort.y + pointDesenhaVelo.y + 143,
						largura + 10, 15, 15, 15);
			valor = (c1.getRed() + c1.getGreen() + c1.getBlue()) / 2;
			if (valor > 200) {
				g2d.setColor(Color.BLACK);
			} else {
				g2d.setColor(Color.WHITE);
			}
			g2d.drawString(velo, limitesViewPort.x + pointDesenhaVelo.x + 3,
					limitesViewPort.y + pointDesenhaVelo.y + 155);
			if (velo2 != null) {
				g2d.drawString(velo2, limitesViewPort.x + pointDesenhaVelo.x
						+ 3, limitesViewPort.y + pointDesenhaVelo.y + 175);
			}
		}

	}

	private void desenhaDRS(Graphics2D g2d) {
		Font fontOri = g2d.getFont();
		g2d.setFont(new Font(fontOri.getName(), Font.BOLD, fontOri.getSize()));
		if (Carro.MENOS_ASA.equals(pilotoSelecionado.getCarro().getAsa())) {
			g2d.setColor(gre);
		} else {
			g2d.setColor(lightWhite);
		}
		g2d.fillRoundRect(limitesViewPort.x + pointDesenhaVelo.x + 74,
				limitesViewPort.y + pointDesenhaVelo.y
						+ (Logger.ativo ? 168 : 143), 34, 15, 15, 15);
		g2d.setColor(Color.BLACK);
		g2d.drawString("DRS", limitesViewPort.x + pointDesenhaVelo.x + 80,
				limitesViewPort.y + pointDesenhaVelo.y
						+ (Logger.ativo ? 180 : 154));
		g2d.setFont(fontOri);

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
		Point pt = new Point(ps.getCarX(), ps.getCarY());
		Color c2 = ps.getCarro().getCor2();
		Color c1 = ps.getCarro().getCor1();
		if (c2 != null) {
			g2d.setColor(new Color(c2.getRed(), c2.getGreen(), c2.getBlue(),
					100));
		}

		if (ps.getPosicao() % 2 == 0) {
			g2d.fillRoundRect(Util.inte((pt.x * zoom) - 3),
					Util.inte((pt.y * zoom) - 16), ps.getNome().length() * 7,
					18, 15, 15);
			int valor = (c2.getRed() + c2.getGreen() + c2.getBlue()) / 2;
			if (valor > 250) {
				g2d.setColor(Color.BLACK);
			} else {
				g2d.setColor(Color.WHITE);
			}
			g2d.drawString(ps.getNome(), Util.inte((ps.getCarX() * zoom) - 2),
					Util.inte((ps.getCarY() * zoom) - 3));
		} else {
			g2d.fillRoundRect(Util.inte((pt.x * zoom) - 3),
					Util.inte((pt.y * zoom) + 4), ps.getNome().length() * 7,
					18, 15, 15);
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

	public void adicionatrvadaRoda(TravadaRoda travadaRoda) {
		No noAtual = controleJogo.obterNoPorId(travadaRoda.getIdNo());
		if (marcasPneu.size() > 100 || noAtual == null) {
			return;
		}
		synchronized (marcasPneu) {
			marcasPneu.add(travadaRoda);
		}
	}

}
