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
import java.awt.Stroke;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import br.nnpe.Constantes;
import br.nnpe.GeoUtil;
import br.nnpe.Html;
import br.nnpe.ImageUtil;
import br.nnpe.Logger;
import br.nnpe.OcilaCor;
import br.nnpe.Util;
import sowbreira.f1mane.MainFrame;
import sowbreira.f1mane.controles.ControleCorrida;
import sowbreira.f1mane.controles.ControleEstatisticas;
import sowbreira.f1mane.controles.InterfaceJogo;
import sowbreira.f1mane.entidades.Carro;
import sowbreira.f1mane.entidades.Circuito;
import sowbreira.f1mane.entidades.Clima;
import sowbreira.f1mane.entidades.No;
import sowbreira.f1mane.entidades.NoWrapper;
import sowbreira.f1mane.entidades.ObjetoEscapada;
import sowbreira.f1mane.entidades.ObjetoPista;
import sowbreira.f1mane.entidades.ObjetoTransparencia;
import sowbreira.f1mane.entidades.Piloto;
import sowbreira.f1mane.entidades.PilotoSuave;
import sowbreira.f1mane.entidades.SafetyCar;
import sowbreira.f1mane.entidades.Volta;
import sowbreira.f1mane.paddock.applet.JogoCliente;
import sowbreira.f1mane.paddock.entidades.TOs.TravadaRoda;
import sowbreira.f1mane.recursos.CarregadorRecursos;
import sowbreira.f1mane.recursos.idiomas.Lang;

/**
 * @author Paulo Sobreira
 */
public class PainelCircuito {

	public static boolean desenhaBkg = true;
	public static boolean desenhaPista = true;
	public static boolean desenhaImagens = true;

	private boolean verControles = true;
	private boolean carragandoBkg = false;
	private boolean desenhouQualificacao;
	private boolean desenhouCreditos;
	private boolean desenhaInfo = true;
	private boolean backGroundZoomPronto;
	private boolean alternaPiscaSCSair;

	private Thread threadCarregarBkg;
	private Thread threadCarregarBkgZoom;

	private Point pontoCentralizado;
	private Point pontoCentralizadoOld;

	private static final long serialVersionUID = -5268795362549996148L;
	private InterfaceJogo controleJogo;
	private GerenciadorVisual gerenciadorVisual;
	private Point pointDesenhaVelo = new Point(5, 60);
	private Point pontoClicado = null;
	private No posisRec;
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
	public final static Color yelListaJogadores = new Color(255, 255, 0, 180);
	public final static Color transpMenus = new Color(255, 255, 255, 160);
	public final static Color jogador = new Color(70, 140, 255, 180);
	public final static Color oran = new Color(255, 188, 40, 180);
	public final static Color transpSel = new Color(165, 165, 165, 180);
	public final static Color ver = new Color(255, 10, 10, 150);
	public final static Color blu = new Color(105, 105, 105, 40);
	public final static Color bluQualy = new Color(105, 105, 205);
	public final static Color lightWhiteRain = new Color(255, 255, 255, 160);
	public final static Color nublado = new Color(200, 200, 200, 100);
	public int indiceNublado = 0;
	public final static BasicStroke strokeFaisca = new BasicStroke(1.0f,
			BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1.0f,
			new float[]{10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10,
					10, 10, 10, 10, 10, 10, 10, 10, 10, 10},
			0);
	public final static BasicStroke chuva1 = new BasicStroke(1.0f,
			BasicStroke.CAP_BUTT,
			BasicStroke.JOIN_MITER, 1.0f, new float[]{10, 5, 10, 5, 10, 5, 10,
					5, 10, 5, 10, 5, 10, 5, 10, 10, 5, 10, 5, 10, 5, 10, 5, 10},
			0);
	public final static BasicStroke chuva2 = new BasicStroke(1.0f,
			BasicStroke.CAP_BUTT,
			BasicStroke.JOIN_MITER, 1.0f, new float[]{5, 10, 5, 10, 5, 10, 5,
					10, 5, 10, 5, 10, 5, 10, 5, 10, 5, 10, 5, 10, 5, 10, 5, 10},
			0);
	private BasicStroke trilho = new BasicStroke(1.0f);
	private BasicStroke strikeMarcacao = new BasicStroke(4,
			BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
	private BasicStroke trilhoMiniPista = new BasicStroke(2.5f);
	private BasicStroke borda = new BasicStroke(5.5f);
	private BasicStroke pista;
	private BasicStroke pistaTinta;
	private BasicStroke box;
	private BasicStroke zebra;

	private static DecimalFormat mil = new DecimalFormat("000");
	public static DecimalFormat df4 = new DecimalFormat("00.0000");
	private int larguraPistaPixeis;

	private int qtdeLuzesAcesas = 5;
	private int mx;
	private int my;
	private double zoom = 1;
	private double mouseZoom = 1;
	private Circuito circuito;
	private Shape[] grid = new Shape[24];
	private List gridImg = new ArrayList();
	private Shape[] asfaltoGrid = new Shape[24];
	private Shape[] boxParada = new Shape[12];
	private Shape[] boxCor1 = new Shape[12];
	private Shape[] boxCor2 = new Shape[12];
	private Rectangle limitesViewPort;
	private Rectangle limitesViewPortFull;
	private Map<Piloto, Piloto> mapaFaiscas = new HashMap<Piloto, Piloto>();
	private Map<Piloto, BufferedImage> capacetesResultadoFinal = new HashMap<Piloto, BufferedImage>();
	private Piloto pilotoSelecionado;
	private List<Piloto> pilotosList;
	private BufferedImage backGround;
	private BufferedImage backGroundZoom;

	private List pistaMinimizada;
	private ArrayList boxMinimizado;
	private List ptosPilotosDesQualy = new ArrayList();

	private boolean exibeResultadoFinal;

	private RoundRectangle2D f1_A = new RoundRectangle2D.Double(0, 0, 1, 1, 0,
			0);
	private RoundRectangle2D f2_S = new RoundRectangle2D.Double(0, 0, 1, 1, 0,
			0);
	private RoundRectangle2D f3_D = new RoundRectangle2D.Double(0, 0, 1, 1, 0,
			0);
	private RoundRectangle2D f5_Z = new RoundRectangle2D.Double(0, 0, 1, 1, 0,
			0);
	private RoundRectangle2D f6_X = new RoundRectangle2D.Double(0, 0, 1, 1, 0,
			0);
	private RoundRectangle2D f7_C = new RoundRectangle2D.Double(0, 0, 1, 1, 0,
			0);
	private RoundRectangle2D kers = new RoundRectangle2D.Double(0, 0, 1, 1, 0,
			0);
	private RoundRectangle2D drs = new RoundRectangle2D.Double(0, 0, 1, 1, 0,
			0);

	private RoundRectangle2D[] pilotosRect;

	private RoundRectangle2D porcentCombustivelTela = new RoundRectangle2D.Double(
			0, 0, 1, 1, 0, 0);
	private RoundRectangle2D menosCombust = new RoundRectangle2D.Double(0, 0, 1,
			1, 0, 0);
	private RoundRectangle2D maisCombust = new RoundRectangle2D.Double(0, 0, 1,
			1, 0, 0);
	private RoundRectangle2D menosAsa = new RoundRectangle2D.Double(0, 0, 1, 1,
			0, 0);

	private RoundRectangle2D maisAsa = new RoundRectangle2D.Double(0, 0, 1, 1,
			0, 0);
	private RoundRectangle2D normalAsa = new RoundRectangle2D.Double(0, 0, 1, 1,
			0, 0);

	private RoundRectangle2D pneuMole = new RoundRectangle2D.Double(0, 0, 1, 1,
			0, 0);

	private RoundRectangle2D pneuDuro = new RoundRectangle2D.Double(0, 0, 1, 1,
			0, 0);
	private RoundRectangle2D pneuChuva = new RoundRectangle2D.Double(0, 0, 1, 1,
			0, 0);

	private RoundRectangle2D vaiBox = new RoundRectangle2D.Double(0, 0, 1, 1, 0,
			0);

	private RoundRectangle2D voltaMenuPrincipalRect = new RoundRectangle2D.Double(
			0, 0, 1, 1, 0, 0);

	private RoundRectangle2D fps = new RoundRectangle2D.Double(0, 0, 1, 1, 0,
			0);

	private RoundRectangle2D ajuda = new RoundRectangle2D.Double(0, 0, 1, 1, 0,
			0);

	private RoundRectangle2D som = new RoundRectangle2D.Double(0, 0, 1, 1, 0,
			0);

	private double multiminiPista;
	private Point maiorP;
	private String infoComp;
	private int infoCompCont;

	private BufferedImage carroimgDano;
	private BufferedImage pneuMoleImg;
	private BufferedImage pneuDuroImg;
	private BufferedImage pneuChuvaImg;
	private BufferedImage pneuMoleImgMini;
	private BufferedImage pneuDuroImgMini;
	private BufferedImage pneuChuvaImgMini;
	private BufferedImage setaCarroCima;
	private BufferedImage setaCarroBaixo;
	private BufferedImage maisAsaIco;
	private BufferedImage menosAsaIco;
	private BufferedImage normalAsaIco;
	private BufferedImage gridCarro;
	private BufferedImage scima;
	private BufferedImage travadaRodaImg0;
	private BufferedImage travadaRodaImg1;
	private BufferedImage travadaRodaImg2;
	private BufferedImage carroCimaFreiosD1;
	private BufferedImage carroCimaFreiosD2;
	private BufferedImage carroCimaFreiosD3;
	private BufferedImage carroCimaFreiosD4;
	private BufferedImage carroCimaFreiosD5;
	private BufferedImage carroCimaFreiosE1;
	private BufferedImage carroCimaFreiosE2;
	private BufferedImage carroCimaFreiosE3;
	private BufferedImage carroCimaFreiosE4;
	private BufferedImage carroCimaFreiosE5;

	private BufferedImage iconLua;
	private BufferedImage iconSol;
	private BufferedImage iconNublado;
	private BufferedImage iconChuva;
	private BufferedImage fuel;
	private int acionaDesenhaKers;
	private int contMostraLag;
	private String climaAnterior;
	private double zoomGrid;

	private Point descontoCentraliza;
	private Point centroP;
	private Point frenteP;
	private AffineTransform translateBoxes;
	private int dezporSuave;
	private AffineTransform affineTransformBG;
	private AffineTransformOp affineTransformOpBG;
	private AffineTransform translateGrid;
	private AffineTransform translateDebug;
	private int informaMudancaClima;
	private int contMostraFPS;
	private AffineTransform afZoomDebug;
	protected int larguraBGZoom;
	protected int alturaBGZoom;
	protected int threadCarregarBkgZoomRodando;
	private BufferedImage rotateBufferTravarRodas;
	private BufferedImage zoomBufferTravarRodas;
	private Integer contDesenhaPilotosQualify;
	private Font fonteBarrasPilotoCarro;
	private Font fontDRS;
	private Font fontVoltarMenuPrincipal;
	private Font fontAjudaControles;
	private Font desenhaAjudaComandoPiloto;
	private Font fontVelocidade;
	private List pistaFull;
	private List boxFull;
	private int entradaBoxIndex;
	private int saidaBoxIndex;
	private List<NoWrapper> boxWrapperFull;
	private List<NoWrapper> pistaWrapperFull;
	private java.awt.geom.RoundRectangle2D.Double rectanglePos;
	private java.awt.geom.RoundRectangle2D.Double rectangleVol;
	private int interpolacao = AffineTransformOp.TYPE_BILINEAR;
	private java.awt.geom.Rectangle2D.Double rectangleMarcasPneuPista;
	private java.awt.geom.Rectangle2D.Double rectangleGerarGrid;
	private java.awt.geom.Rectangle2D.Double rectangleSafetyCarCima;
	private java.awt.geom.Rectangle2D.Double rectangleGerarBoxes;
	private int contImgFundo;
	public static boolean efeitosLigados = true;

	public PainelCircuito(InterfaceJogo jogo,
			GerenciadorVisual gerenciadorVisual) {
		carregaRecursos();
		controleJogo = jogo;
		this.gerenciadorVisual = gerenciadorVisual;
		pilotosRect = new RoundRectangle2D.Double[controleJogo.getPilotosCopia()
				.size()];
		for (int i = 0; i < pilotosRect.length; i++) {
			pilotosRect[i] = new RoundRectangle2D.Double(0, 0, 1, 1, 0, 0);
		}
		MouseListener[] mouseListeners = controleJogo.getMainFrame()
				.getMouseListeners();
		for (int i = 0; i < mouseListeners.length; i++) {
			controleJogo.getMainFrame().removeMouseListener(mouseListeners[i]);
		}
		Logger.logar(
				"controleJogo.getMainFrame().addMouseListener(new MouseAdapter() {");
		controleJogo.getMainFrame().addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				controleJogo.getMainFrame().requestFocus();
				if (!verificaComando(e)) {
					Piloto pilotoJogador = controleJogo.getPilotoJogador();
					if (pilotoJogador != null && pilotoJogador.getPtosBox() == 0
							&& pilotoJogador.getP1() != null
							&& pilotoJogador.getP2() != null) {
						Point p = new Point(e.getPoint().x, e.getPoint().y);
						pontoClicado = p;
						double menor = Integer.MAX_VALUE;
						int pos = 0;
						double p0p = GeoUtil.distaciaEntrePontos(new Point(
								pilotoJogador.getP0().x - descontoCentraliza.x,
								pilotoJogador.getP0().y - descontoCentraliza.y),
								p);
						double p1p = GeoUtil.distaciaEntrePontos(new Point(
								pilotoJogador.getP1().x - descontoCentraliza.x,
								pilotoJogador.getP1().y - descontoCentraliza.y),
								p);
						double p2p = GeoUtil.distaciaEntrePontos(new Point(
								pilotoJogador.getP2().x - descontoCentraliza.x,
								pilotoJogador.getP2().y - descontoCentraliza.y),
								p);
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
								|| (pos == 1
										&& pilotoJogador.getTracado() == 2)) {
							pos = 0;
						}
						controleJogo.mudarTracado(pos);
					}
				}
				super.mouseClicked(e);
			}
		});
		circuito = controleJogo.getCircuito();
		if (backGround == null) {
			try {
				carregaBackGround();
			} catch (Error e) {
				System.gc();
				Logger.logarExept(e);
			}
		}
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
		gerarGrid();
		pistaFull = controleJogo.getCircuito().getPistaFull();
		boxFull = controleJogo.getCircuito().getBoxFull();
		entradaBoxIndex = controleJogo.getCircuito().getEntradaBoxIndex();
		saidaBoxIndex = controleJogo.getCircuito().getSaidaBoxIndex();
		boxWrapperFull = controleJogo.getBoxWrapperFull();
		pistaWrapperFull = controleJogo.getPistaWrapperFull();
	}

	protected void render() {
		try {
			Graphics2D g2d = controleJogo.getMainFrame().obterGraficos();
			if (g2d == null) {
				return;
			}
			descontoCentraliza();
			limitesViewPort = (Rectangle) limitesViewPort();
			limitesViewPortFull = (Rectangle) limitesViewPortFull();
			setarHints(g2d);
			processaZoom();
			desenhaBackGround(g2d);
			if (!desenhouCreditos) {
				return;
			}
			desenhaQualificacao(g2d);
			if (!desenhouQualificacao) {
				return;
			}
			desenhaGrid(g2d);
			iniciaPilotoSelecionado();
			desenhaMarcacaoParaCurva(g2d);
			desenhaCarros(g2d);
			desenharSafetyCarCima(g2d);
			desenhaChuva(g2d);
			desenhaBarraPilotos(g2d);
			desenhaContadorVoltas(g2d);
			desenharFarois(g2d);
			desenhaNomePilotoSelecionado(pilotoSelecionado, g2d);
			desenhaBarrasPilotoCarro(g2d);
			desenharClima(g2d);
			desenharTpPneuPsel(g2d);
			desenharTpAsaPsel(g2d);
			desenhaCapacetePsel(g2d);
			desenhaComandoIrBox(g2d);
			desenhaFPS(g2d);
			desenhaAjuda(g2d);
			desenhaLag(g2d);
			desenhaInfoPilotoSelecionado(g2d);
			desenhaMiniPista(g2d);
			desenhaNarracao(g2d);
			desenhaTabelaComparativa(g2d);
			desenhaCarrosLado(pilotoSelecionado, g2d);
			desenhaControles(g2d);
			desenhaControlesBox(g2d);
			desenhaKers(g2d);
			desenhaDRS(g2d);
			desenhaVelocidade(g2d);
			desenhaResultadoFinal(g2d);
			desenhaAjudaControles(g2d);
			desenhaProblemasCarroSelecionado(pilotoSelecionado, g2d);
			desenhaVoltarMenuPrincipal(g2d);
			desenhaDebugIinfo(g2d);
		} catch (Exception e) {
			Logger.logarExept(e);
		}
	}

	private void desenhaCapacetePsel(Graphics2D g2d) {
		if (!desenhaInfo) {
			return;
		}
		if (pilotoSelecionado == null) {
			return;
		}
		if (isExibeResultadoFinal()) {
			return;
		}
		BufferedImage obterCapacete = controleJogo
				.obterCapacete(pilotoSelecionado);
		if (obterCapacete != null && desenhaImagens) {
			g2d.setColor(transpMenus);
			g2d.fillRoundRect(limitesViewPort.x + 110, 10 + limitesViewPort.y,
					obterCapacete.getWidth() + 5, 45, 0, 0);
			g2d.drawImage(obterCapacete, null, limitesViewPort.x + 115,
					limitesViewPort.y + 12);
		}

	}

	private void desenharTpAsaPsel(Graphics2D g2d) {
		if (!desenhaInfo) {
			return;
		}
		if (pilotoSelecionado == null) {
			return;
		}
		if (isExibeResultadoFinal()) {
			return;
		}

		Carro carro = pilotoSelecionado.getCarro();
		BufferedImage asa = null;

		if (Carro.MAIS_ASA.equals(carro.getAsa())) {
			asa = maisAsaIco;
		}

		if (Carro.MENOS_ASA.equals(carro.getAsa())) {
			asa = menosAsaIco;
		}

		if (Carro.ASA_NORMAL.equals(carro.getAsa())) {
			asa = normalAsaIco;
		}

		if (asa == null) {
			return;
		}

		g2d.setColor(transpMenus);
		g2d.fillRoundRect(limitesViewPort.x + 5, 10 + limitesViewPort.y,
				asa.getWidth() + 5, 45, 0, 0);
		if (desenhaImagens)
			g2d.drawImage(asa, limitesViewPort.x + 7,
					10 + limitesViewPort.y + 5, null);

	}

	private void carregaRecursos() {
		carroimgDano = CarregadorRecursos.carregaImagem("CarroLadoDef.png");
		setaCarroCima = CarregadorRecursos
				.carregaBufferedImageTranspareciaBranca("SetaCarroCima.png",
						200);
		setaCarroBaixo = CarregadorRecursos
				.carregaBufferedImageTranspareciaBranca("SetaCarroBaixo.png",
						200);

		maisAsaIco = CarregadorRecursos
				.carregaBufferedImageTransparecia("maisAsa.png", null);;
		menosAsaIco = CarregadorRecursos
				.carregaBufferedImageTransparecia("menosAsa.png", null);;
		normalAsaIco = CarregadorRecursos
				.carregaBufferedImageTransparecia("normalAsa.png", null);;

		gridCarro = CarregadorRecursos
				.carregaBufferedImageTransparecia("GridCarro.png");
		scima = CarregadorRecursos
				.carregaBufferedImageTranspareciaBranca("sfcima.png");
		travadaRodaImg0 = CarregadorRecursos
				.carregaBufferedImageTranspareciaBranca("travadaRoda0.png", 150,
						100);
		travadaRodaImg1 = CarregadorRecursos
				.carregaBufferedImageTranspareciaBranca("travadaRoda1.png", 150,
						100);
		travadaRodaImg2 = CarregadorRecursos
				.carregaBufferedImageTranspareciaBranca("travadaRoda2.png", 150,
						100);
		carroCimaFreiosD1 = CarregadorRecursos.carregaBufferedImageTransparecia(
				"CarroCimaFreiosD1.png", null);
		carroCimaFreiosD2 = CarregadorRecursos.carregaBufferedImageTransparecia(
				"CarroCimaFreiosD2.png", null);
		carroCimaFreiosD3 = CarregadorRecursos.carregaBufferedImageTransparecia(
				"CarroCimaFreiosD3.png", null);
		carroCimaFreiosD4 = CarregadorRecursos.carregaBufferedImageTransparecia(
				"CarroCimaFreiosD4.png", null);
		carroCimaFreiosD5 = CarregadorRecursos.carregaBufferedImageTransparecia(
				"CarroCimaFreiosD5.png", null);
		carroCimaFreiosE1 = CarregadorRecursos.carregaBufferedImageTransparecia(
				"CarroCimaFreiosE1.png", null);
		carroCimaFreiosE2 = CarregadorRecursos.carregaBufferedImageTransparecia(
				"CarroCimaFreiosE2.png", null);
		carroCimaFreiosE3 = CarregadorRecursos.carregaBufferedImageTransparecia(
				"CarroCimaFreiosE3.png", null);
		carroCimaFreiosE4 = CarregadorRecursos.carregaBufferedImageTransparecia(
				"CarroCimaFreiosE4.png", null);
		carroCimaFreiosE5 = CarregadorRecursos.carregaBufferedImageTransparecia(
				"CarroCimaFreiosE5.png", null);
		pneuMoleImg = ImageUtil.geraResize(CarregadorRecursos
				.carregaBufferedImageTransparecia("pneu_mole.png", null), 0.3);
		pneuDuroImg = ImageUtil.geraResize(CarregadorRecursos
				.carregaBufferedImageTransparecia("pneu-duro.png", null), 0.3);
		pneuChuvaImg = ImageUtil.geraResize(CarregadorRecursos
				.carregaBufferedImageTransparecia("pneu-chuva.png", null), 0.3);

		pneuMoleImgMini = ImageUtil.geraResize(CarregadorRecursos
				.carregaBufferedImageTransparecia("pneu_mole.png", null), 0.15);
		pneuDuroImgMini = ImageUtil.geraResize(CarregadorRecursos
				.carregaBufferedImageTransparecia("pneu-duro.png", null), 0.15);
		pneuChuvaImgMini = ImageUtil.geraResize(CarregadorRecursos
				.carregaBufferedImageTransparecia("pneu-chuva.png", null),
				0.15);

		fuel = CarregadorRecursos.carregaBufferedImage("fuel.png");

		iconLua = CarregadorRecursos.carregaBufferedImage("lua.png");
		iconSol = CarregadorRecursos.carregaBufferedImage("sol.png");
		iconNublado = CarregadorRecursos.carregaBufferedImage("nublado.png");
		iconChuva = CarregadorRecursos.carregaBufferedImage("chuva.png");

	}

	protected boolean verificaComando(MouseEvent e) {
		if (f1_A.contains(e.getPoint())) {
			controleJogo.mudarGiroMotor(Carro.GIRO_MIN);
			return true;
		}
		if (f2_S.contains(e.getPoint())) {
			controleJogo.mudarGiroMotor(Carro.GIRO_NOR);
			return true;
		}
		if (f3_D.contains(e.getPoint())) {
			controleJogo.mudarGiroMotor(Carro.GIRO_MAX);
			return true;
		}

		if (f5_Z.contains(e.getPoint())) {
			controleJogo.mudarModoPilotagem(Piloto.LENTO);
			return true;
		}
		if (f6_X.contains(e.getPoint())) {
			controleJogo.mudarModoPilotagem(Piloto.NORMAL);
			return true;
		}
		if (f7_C.contains(e.getPoint())) {
			controleJogo.mudarModoPilotagem(Piloto.AGRESSIVO);
			return true;
		}

		if (kers.contains(e.getPoint())
				&& controleJogo.getPilotoJogador().getPtosBox() == 0) {
			controleJogo.mudarModoKers();
			return true;
		}
		if (drs.contains(e.getPoint())
				&& controleJogo.getPilotoJogador().getPtosBox() == 0) {
			controleJogo.mudarModoDRS();
			return true;
		}

		int porcentCombust = 50;
		String tpPneu = Carro.TIPO_PNEU_DURO;
		String tpAsa = Carro.ASA_NORMAL;

		if (controleJogo.getPilotoJogador() != null) {
			porcentCombust = controleJogo.getPilotoJogador()
					.getQtdeCombustBox();
			tpPneu = controleJogo.getPilotoJogador().getTipoPneuBox();
			tpAsa = controleJogo.getPilotoJogador().getAsaBox();
		}

		if (menosCombust.contains(e.getPoint()) && porcentCombust > 0
				&& controleJogo.getPilotoJogador().getPtosBox() == 0) {
			porcentCombust -= 10;
			controleJogo.setBoxJogadorHumano(tpPneu, porcentCombust, tpAsa);
			return true;
		}

		if (maisCombust.contains(e.getPoint()) && porcentCombust < 100
				&& controleJogo.getPilotoJogador().getPtosBox() == 0) {
			porcentCombust += 10;
			controleJogo.setBoxJogadorHumano(tpPneu, porcentCombust, tpAsa);
			return true;
		}

		if (maisAsa.contains(e.getPoint())
				&& controleJogo.getPilotoJogador().getPtosBox() == 0) {
			tpAsa = Carro.MAIS_ASA;
			controleJogo.setBoxJogadorHumano(tpPneu, porcentCombust, tpAsa);
			return true;
		}
		if (normalAsa.contains(e.getPoint())
				&& controleJogo.getPilotoJogador().getPtosBox() == 0) {
			tpAsa = Carro.ASA_NORMAL;
			controleJogo.setBoxJogadorHumano(tpPneu, porcentCombust, tpAsa);
			return true;
		}
		if (menosAsa.contains(e.getPoint())
				&& controleJogo.getPilotoJogador().getPtosBox() == 0) {
			tpAsa = Carro.MENOS_ASA;
			controleJogo.setBoxJogadorHumano(tpPneu, porcentCombust, tpAsa);
			return true;
		}

		if (pneuMole.contains(e.getPoint())
				&& controleJogo.getPilotoJogador().getPtosBox() == 0) {
			tpPneu = Carro.TIPO_PNEU_MOLE;
			controleJogo.setBoxJogadorHumano(tpPneu, porcentCombust, tpAsa);
			return true;
		}
		if (pneuDuro.contains(e.getPoint())
				&& controleJogo.getPilotoJogador().getPtosBox() == 0) {
			tpPneu = Carro.TIPO_PNEU_DURO;
			controleJogo.setBoxJogadorHumano(tpPneu, porcentCombust, tpAsa);
			return true;
		}
		if (pneuChuva.contains(e.getPoint())
				&& controleJogo.getPilotoJogador().getPtosBox() == 0) {
			tpPneu = Carro.TIPO_PNEU_CHUVA;
			controleJogo.setBoxJogadorHumano(tpPneu, porcentCombust, tpAsa);
			return true;
		}
		if (vaiBox.contains(e.getPoint())
				&& controleJogo.getPilotoJogador().getPtosBox() == 0) {
			mudarModoBox();
		}

		for (int i = 0; i < pilotosRect.length; i++) {
			if (pilotosRect[i].contains(e.getPoint())) {
				pilotoSelecionado = pilotosList.get(i);
				controleJogo.selecionouPiloto(pilotoSelecionado);
				return true;
			}
		}
		if (voltaMenuPrincipalRect.contains(e.getPoint())) {
			controleJogo.voltaMenuPrincipal();
			return true;
		}
		if (fps.contains(e.getPoint())) {
			gerenciadorVisual.mudaLimiteFps();
			return true;
		}
		if (ajuda.contains(e.getPoint())) {
			verControles = !verControles;
			return true;
		}
		if (som.contains(e.getPoint())) {
			ControleSom.ligaDesligaSom();
			return true;
		}

		return false;
	}

	public void mudarModoBox() {
		controleJogo.mudarModoBox();
	}

	public void carregaBackGround() {
		if (!desenhaBkg) {
			return;
		}
		try {
			if (backGround == null && !carragandoBkg) {
				backGround = CarregadorRecursos.carregaBackGround(
						circuito.getBackGround(), null, circuito);
			}
		} catch (Exception e) {
			Logger.logarExept(e);
			backGround = null;
		}
		if (backGround == null) {
			if (!carragandoBkg) {
				Runnable runnable = new Runnable() {
					@Override
					public void run() {
						carragandoBkg = true;
						backGround = controleJogo
								.carregaBackGround(circuito.getBackGround());
						if (backGround != null) {
							backGround.setAccelerationPriority(1);
						}
						carragandoBkg = false;
					}
				};
				threadCarregarBkg = new Thread(runnable);
				threadCarregarBkg.setPriority(Thread.MIN_PRIORITY);
				threadCarregarBkg.start();
			}
		}
	}

	private void atualizacaoSuave(PilotoSuave piloto) {
		if (!controleJogo.isAtualizacaoSuave()) {
			piloto.setNoAtualSuave(piloto.getNoAtual());
			return;
		}

		if (piloto.getNoAtual().equals(piloto.getNoAnterior())) {
			return;
		}

		List<No> nos;
		No noAtual = piloto.getNoAtual();

		No noAtualSuave = piloto.getNoAtualSuave();
		if (noAtualSuave == null) {
			noAtualSuave = noAtual;
		}

		NoWrapper noAtualWrapper = new NoWrapper(noAtual);
		NoWrapper noAtualSuaveWrapper = new NoWrapper(noAtualSuave);

		boolean boxContainsNoAtual = boxWrapperFull.contains(noAtualWrapper);
		boolean boxContainsNoAtualSuave = boxWrapperFull
				.contains(noAtualSuaveWrapper);
		boolean pistaContainsNoAtualSuave = pistaWrapperFull
				.contains(noAtualSuaveWrapper);
		boolean pistaContainsNoAtual = pistaWrapperFull
				.contains(noAtualWrapper);

		if (boxContainsNoAtual && boxContainsNoAtualSuave) {
			nos = boxFull;
		} else if (boxContainsNoAtual && pistaContainsNoAtualSuave) {
			nos = pistaFull;
		} else if (boxContainsNoAtualSuave && pistaContainsNoAtual) {
			nos = boxFull;
		} else {
			nos = pistaFull;
		}

		int diff = noAtual.getIndex() - noAtualSuave.getIndex();
		if (boxContainsNoAtual && pistaContainsNoAtualSuave) {
			diff = noAtual.getIndex()
					+ (controleJogo.getCircuito().getEntradaBoxIndex()
							- noAtualSuave.getIndex());

		}
		if (boxContainsNoAtualSuave && pistaContainsNoAtual) {
			diff = (noAtual.getIndex()
					- (controleJogo.getCircuito().getSaidaBoxIndex())
					+ (boxFull.size() - noAtualSuave.getIndex()));
		}

		if (diff < 0) {
			diff = (noAtual.getIndex() + nos.size()) - noAtualSuave.getIndex();
		}
		int ganhoSuave = 0;
		if (noAtual.verificaRetaOuLargada()) {
			ganhoSuave = (gerenciadorVisual.getFpsLimite() == 30.0) ? 7 : 3;
		} else if (noAtual.verificaCurvaAlta()) {
			ganhoSuave = (gerenciadorVisual.getFpsLimite() == 30.0) ? 5 : 2;
		} else if (noAtual.verificaCurvaBaixa() || noAtualSuave.isBox()) {
			ganhoSuave = (gerenciadorVisual.getFpsLimite() == 30.0) ? 3 : 1;
		}

		if (controleJogo.isSafetyCarNaPista()) {
			if (diff < 40) {
				ganhoSuave--;
			}
			if (diff < 60) {
				ganhoSuave--;
			}
		}
		if (controleJogo instanceof JogoCliente) {
			// if (false) {
			if (diff < 100) {
				ganhoSuave = 1;
			}else if (diff < 150) {
				ganhoSuave--;
			}
			if (diff > 500) {
				ganhoSuave++;
			}
			if (diff > 600) {
				ganhoSuave++;
			}
			if (diff > 700) {
				ganhoSuave++;
			}
		} else {
			if (diff < 50) {
				ganhoSuave = 1;
			} else if (diff < 100) {
				ganhoSuave--;
			}
			if (diff > 200) {
				ganhoSuave += 1;
			}
			if (diff > 250) {
				ganhoSuave += 1;
			}
			if (diff > 300) {
				ganhoSuave += 1;
			}
		}

		int ganhoSuaveAnt = piloto.getGanhoSuave();

		if (piloto instanceof Piloto && ((Piloto) piloto).isJogadorHumano()) {
			Logger.logar("diff " + diff + " ganhoSuave " + ganhoSuave
					+ " ganhoSuaveAnt " + ganhoSuaveAnt);
		}
		if (ganhoSuave > ganhoSuaveAnt) {
			ganhoSuave = ganhoSuaveAnt + 1;
		}
		if (ganhoSuave <= ganhoSuaveAnt) {
			ganhoSuave = ganhoSuaveAnt - 1;
		}

		if (noAtualSuave.verificaRetaOuLargada()
				&& ganhoSuaveAnt > ganhoSuave) {
			ganhoSuave = ganhoSuaveAnt;
		}
		
		if (ganhoSuave <= 0) {
			ganhoSuave = 0;
		}
		piloto.setGanhoSuave(ganhoSuave);
		if (boxContainsNoAtual && pistaContainsNoAtualSuave
				&& noAtualSuave.getIndex() < entradaBoxIndex) {
			nos = pistaFull;
		}

		if (pistaContainsNoAtual && boxContainsNoAtualSuave) {
			nos = boxFull;
		}

		int index = noAtualSuave.getIndex() + ganhoSuave;

		if (boxContainsNoAtual && noAtualSuave.getIndex() >= entradaBoxIndex) {
			nos = boxFull;
			index = 0;
		}

		if (pistaContainsNoAtual && boxContainsNoAtualSuave
				&& index > (nos.size() - 5)) {
			nos = pistaFull;
			index = saidaBoxIndex + 5;
		}

		if (index >= nos.size()) {
			index = index - nos.size();
		}
		if (index >= nos.size()) {
			index = -1;
		} else {
			noAtualSuave = nos.get(index);
		}

		if (diff > 1000) {
			if (piloto instanceof Piloto
					&& ((Piloto) piloto).isJogadorHumano()) {
				Logger.logar("atualizacaoSuave diff > 1000 " + diff);
			}
			noAtualSuave = noAtual;
		}

		piloto.setNoAtualSuave(noAtualSuave);
	}

	public int getQtdeLuzesAcesas() {
		return qtdeLuzesAcesas;
	}

	private void desenhaMarcacaoParaCurva(Graphics2D g2d) {
		if (isExibeResultadoFinal() || controleJogo.isJogoPausado()
				|| pilotoSelecionado == null
				|| pilotoSelecionado.getPtosBox() != 0) {
			return;
		}
		if (controleJogo.getNiveljogo() != InterfaceJogo.FACIL_NV) {
			return;
		}
		No noReal = pilotoSelecionado.getNoAtual();
		No noSuave = pilotoSelecionado.getNoAtualSuave();
		if (noReal == null || noSuave == null) {
			return;
		}
		int index = noSuave.getIndex();
		desenhaMarcacaoNo(g2d, noReal, index + 250);
		desenhaMarcacaoNo(g2d, noReal, index + 225);
		desenhaMarcacaoNo(g2d, noReal, index + 200);
		desenhaMarcacaoNo(g2d, noReal, index + 175);

	}

	private void desenhaMarcacaoNo(Graphics2D g2d, No noReal, int index) {
		int index2 = index - 15;

		if (index > controleJogo.getNosDaPista().size()) {
			index -= controleJogo.getNosDaPista().size();
		}
		if (index2 > controleJogo.getNosDaPista().size()) {
			index2 -= controleJogo.getNosDaPista().size();
		}
		No no = controleJogo.obterNoPorId(index);
		No no2 = controleJogo.obterNoPorId(index2);
		if (no == null || no2 == null) {
			return;
		}
		if (noReal.verificaRetaOuLargada()) {
			g2d.setColor(OcilaCor.geraOcila("desenhaMarcaoNo1", Color.GREEN));
		} else if (noReal.verificaCurvaAlta()) {
			g2d.setColor(OcilaCor.geraOcila("desenhaMarcaoNo2", Color.YELLOW));
		} else if (noReal.verificaCurvaBaixa()) {
			g2d.setColor(OcilaCor.geraOcila("desenhaMarcaoNo3", Color.RED));
		} else {
			return;
		}
		Stroke stroke = g2d.getStroke();
		g2d.setStroke(strikeMarcacao);

		double calculaAngulo = GeoUtil.calculaAngulo(no2.getPoint(),
				no.getPoint(), 0);
		Point p1 = GeoUtil.calculaPonto(calculaAngulo, 15, no2.getPoint());
		Point p2 = GeoUtil.calculaPonto(calculaAngulo + 180, 15,
				no2.getPoint());
		int x = no.getX();
		int y = no.getY();
		g2d.drawLine((int) ((x - descontoCentraliza.x) * zoom),
				(int) ((y - descontoCentraliza.y) * zoom),
				(int) ((p1.x - descontoCentraliza.x) * zoom),
				(int) ((p1.y - descontoCentraliza.y) * zoom));
		g2d.drawLine((int) ((x - descontoCentraliza.x) * zoom),
				(int) ((y - descontoCentraliza.y) * zoom),
				(int) ((p2.x - descontoCentraliza.x) * zoom),
				(int) ((p2.y - descontoCentraliza.y) * zoom));
		// p1 = pilotoSelecionado.getP1();
		// p2 = pilotoSelecionado.getP2();
		//
		// g2d.setColor(Color.ORANGE);
		// g2d.fillOval((int) ((p1.x - descontoCentraliza.x) * zoom),
		// (int) ((p1.y - descontoCentraliza.y) * zoom), 0, 0);
		//
		// g2d.setColor(Color.CYAN);
		// g2d.fillOval((int) ((p2.x - descontoCentraliza.x) * zoom),
		// (int) ((p2.y - descontoCentraliza.y) * zoom), 10, 10);

		g2d.setStroke(stroke);
	}

	private void processaZoom() {
		if (Math.abs(mouseZoom - zoom) < 0.01) {
			zoom = mouseZoom;
		}
		if (mouseZoom > zoom) {
			zoom += 0.01;
		}
		if (mouseZoom < zoom) {
			zoom -= 0.01;
		}
		if (mouseZoom == zoom && zoomGrid != zoom) {
			gerarGrid();
			zoomGrid = zoom;
		}
	}

	private void desenhaVoltarMenuPrincipal(Graphics2D g2d) {
		if (!isExibeResultadoFinal() && !controleJogo.isJogoPausado()) {
			voltaMenuPrincipalRect.setFrame(0, 0, 1, 1);
			return;
		}
		if (controleJogo instanceof JogoCliente) {
			return;
		}
		int x = limitesViewPort.x + (int) (limitesViewPort.getWidth() / 2);
		int y = limitesViewPort.y + (int) (limitesViewPort.getHeight() / 2)
				- 50;
		if (isExibeResultadoFinal()) {
			y = limitesViewPort.y + (int) (limitesViewPort.getHeight()) - 20;
		}
		Font fontOri = g2d.getFont();
		if (fontVoltarMenuPrincipal == null) {
			fontVoltarMenuPrincipal = new Font(fontOri.getName(), Font.BOLD,
					28);
		}
		g2d.setFont(fontVoltarMenuPrincipal);
		g2d.setColor(transpMenus);
		String txt = Lang.msg("voltarMenuPrincipal").toUpperCase();
		int larguraTexto = Util.larguraTexto(txt, g2d);
		int desl = larguraTexto / 2;
		voltaMenuPrincipalRect.setFrame(x - desl, y - 25, larguraTexto + 10,
				30);
		g2d.fill(voltaMenuPrincipalRect);
		g2d.setColor(Color.BLACK);
		g2d.drawString(txt, x - desl + 5, y);
		g2d.setFont(fontOri);

	}

	private void desenhaAjudaControles(Graphics2D g2d) {
		if (!isVerControles()) {
			return;
		}
		if (controleJogo.isCorridaTerminada()) {
			return;
		}
		if (pilotoSelecionado != null
				&& !pilotoSelecionado.equals(controleJogo.getPilotoJogador())) {
			return;
		}
		int x = limitesViewPort.x + (int) (limitesViewPort.width / 3.5);
		int y = limitesViewPort.y + (int) (limitesViewPort.height / 2);
		int xOri = x;
		int yOri = y;

		if (controleJogo.getNiveljogo() != InterfaceJogo.DIFICIL_NV) {
			y -= 150;

			x -= 25;

			int lagura = desenhaAjudaComandoPiloto(g2d, x, y,
					"Q : " + Lang.msg("GIRO_MIN").toUpperCase());
			x += lagura + 10;
			lagura = desenhaAjudaComandoPiloto(g2d, x, y,
					"W : " + Lang.msg("GIRO_NOR").toUpperCase());
			x += lagura + 10;
			desenhaAjudaComandoPiloto(g2d, x, y,
					"E : " + Lang.msg("GIRO_MAX").toUpperCase());
		}
		x = xOri;
		y = yOri;

		desenhaAjudaComandoPiloto(g2d, x, y, f1_A, "A");
		x += 40;
		desenhaAjudaComandoPiloto(g2d, x, y, f2_S, "S");
		x += 40;
		desenhaAjudaComandoPiloto(g2d, x, y, f3_D, "D");

		y += 40;
		x += 20;

		desenhaAjudaComandoPiloto(g2d, x, y, f5_Z, "Z");
		x += 40;
		desenhaAjudaComandoPiloto(g2d, x, y, f6_X, "X");
		x += 40;
		desenhaAjudaComandoPiloto(g2d, x, y, f7_C, "C");

		x = xOri + 300;
		y = yOri + 20;

		Font fontOri = g2d.getFont();
		if (fontAjudaControles == null) {
			fontAjudaControles = new Font(fontOri.getName(), Font.BOLD, 28);
		}

		/**
		 * Esquerda
		 */

		double rad = Math.toRadians(270);
		AffineTransform afRotate = new AffineTransform();
		afRotate.setToRotation(rad, setaCarroCima.getWidth() / 2,
				setaCarroCima.getHeight() / 2);
		AffineTransformOp opRotate = new AffineTransformOp(afRotate,
				interpolacao);
		BufferedImage rotateBufferSetaCima = new BufferedImage(
				setaCarroCima.getWidth(), setaCarroCima.getWidth(),
				BufferedImage.TYPE_INT_ARGB);
		opRotate.filter(setaCarroCima, rotateBufferSetaCima);
		g2d.drawImage(rotateBufferSetaCima, x, y - 30, null);

		x += 40;

		g2d.setFont(fontAjudaControles);
		g2d.setColor(transpMenus);
		g2d.fillRoundRect(x, y, 30, 30, 0, 0);
		g2d.setColor(Color.BLACK);
		g2d.drawString("\u2190", x, y + 25);
		g2d.setStroke(trilhoMiniPista);

		x += 40;

		/**
		 * Baixo
		 */
		if (controleJogo.isKers()) {
			Stroke stroke = g2d.getStroke();
			g2d.setFont(fontAjudaControles);
			g2d.setColor(transpMenus);
			g2d.fillRoundRect(x, y, 30, 30, 0, 0);
			g2d.setColor(Color.BLACK);
			g2d.drawString("\u2193", x + 5, y + 25);
			g2d.setStroke(trilhoMiniPista);
			g2d.setColor(yel);
			// g2d.drawLine(x + 15, y + 30, x + 140, y + 60);
			// g2d.drawLine(x + 140, y + 60,
			g2d.drawLine(x + 15, y + 35,
					(int) (kers.getX() + (kers.getWidth() / 2)),
					(int) kers.getY() - 5);
			g2d.setStroke(stroke);
		}

		x += 40;
		/**
		 * Direita
		 */
		g2d.setFont(fontAjudaControles);
		g2d.setColor(transpMenus);
		g2d.fillRoundRect(x, y, 30, 30, 0, 0);
		g2d.setColor(Color.BLACK);
		g2d.drawString("\u2192", x + 5, y + 25);

		rad = Math.toRadians(90);
		afRotate = new AffineTransform();
		afRotate.setToRotation(rad, setaCarroCima.getWidth() / 2,
				setaCarroCima.getHeight() / 2);
		opRotate = new AffineTransformOp(afRotate, interpolacao);
		rotateBufferSetaCima = new BufferedImage(setaCarroCima.getWidth(),
				setaCarroCima.getWidth(), BufferedImage.TYPE_INT_ARGB);
		opRotate.filter(setaCarroCima, rotateBufferSetaCima);
		g2d.drawImage(rotateBufferSetaCima, x - 15, y - 25, null);

		x -= 40;

		y -= 40;

		/**
		 * cima
		 */
		if (controleJogo.isDrs()) {
			Stroke stroke = g2d.getStroke();
			g2d.setColor(transpMenus);
			g2d.fillRoundRect(x, y, 30, 30, 0, 0);
			g2d.setColor(Color.BLACK);
			g2d.drawString("\u2191", x + 5, y + 25);
			g2d.setStroke(trilhoMiniPista);
			g2d.setColor(yel);
			g2d.drawLine(x + 15, y - 5, x - 500, y - 5);
			g2d.drawLine(x - 500, y - 5,
					(int) (drs.getX() + (drs.getWidth() / 2)),
					(int) drs.getY() - 5);
			g2d.setStroke(stroke);
		}

		y -= 180;
		x -= 30;
		Stroke stroke = g2d.getStroke();
		if (qtdeLuzesAcesas <= 0) {
			g2d.setColor(transpMenus);
			g2d.fillRoundRect(x, y, 60, 30, 0, 0);
			g2d.setColor(Color.BLACK);
			g2d.drawString("B : ", x + 5, y + 25);
			g2d.setStroke(trilhoMiniPista);
			g2d.setColor(yel);
			g2d.drawLine(x + 30, y - 5,
					(int) (vaiBox.getX() + (vaiBox.getWidth() / 2)),
					(int) (vaiBox.getY() + vaiBox.getHeight()) + 5);

		}
		if (controleJogo.isJogoPausado()) {
			x = xOri - 50;
			g2d.setColor(transpMenus);
			g2d.fillRoundRect(x, y, 60, 30, 0, 0);
			g2d.setColor(Color.BLACK);
			g2d.drawString("Esc", x + 5, y + 25);
			g2d.setStroke(trilhoMiniPista);
			g2d.setColor(yel);
			g2d.drawLine(x + 30, y - 5,
					(int) (ajuda.getX() + (ajuda.getWidth() / 2)),
					(int) (ajuda.getY() + ajuda.getHeight()) + 5);

		}

		x += ajuda.getWidth() + 50;
		String txt = Lang.msg("som").toUpperCase();
		int larguraTexto = Util.larguraTexto(txt, g2d) + 10;
		if (ControleSom.somLigado) {
			g2d.setColor(OcilaCor.geraOcila("mrkSom", yel));
			g2d.fillRoundRect(x, y, larguraTexto, 30, 0, 0);
		} else {
			g2d.setColor(transpMenus);
			g2d.fillRoundRect(x, y, larguraTexto, 30, 0, 0);
		}
		g2d.setColor(Color.BLACK);
		som.setFrame(x, y, larguraTexto, 30);
		g2d.drawString(txt, x + 5, y + 25);
		g2d.setStroke(trilhoMiniPista);

		g2d.setStroke(stroke);

		g2d.setFont(fontOri);

	}

	private void desenhaAjudaComandoPiloto(Graphics2D g2d, int x, int y,
			RoundRectangle2D rect, String txt) {
		Font fontOri = g2d.getFont();
		Stroke stroke = g2d.getStroke();
		if (desenhaAjudaComandoPiloto == null) {
			desenhaAjudaComandoPiloto = new Font(fontOri.getName(), Font.BOLD,
					28);
		}
		g2d.setFont(desenhaAjudaComandoPiloto);
		g2d.setColor(transpMenus);
		g2d.fillRoundRect(x, y, 30, 30, 0, 0);
		g2d.setColor(Color.BLACK);
		g2d.drawString(txt, x + 5, y + 25);
		g2d.setStroke(trilhoMiniPista);
		g2d.setColor(yel);
		g2d.drawLine(x + 15, y + 35,
				(int) (rect.getX() + (rect.getWidth() / 2)),
				(int) rect.getY() - 5);
		g2d.setStroke(stroke);
		g2d.setFont(fontOri);
	}

	private int desenhaAjudaComandoPiloto(Graphics2D g2d, int x, int y,
			String txt) {
		Font fontOri = g2d.getFont();
		if (desenhaAjudaComandoPiloto == null) {
			desenhaAjudaComandoPiloto = new Font(fontOri.getName(), Font.BOLD,
					28);
		}
		g2d.setFont(desenhaAjudaComandoPiloto);
		g2d.setColor(transpMenus);
		int larguraTexto = Util.larguraTexto(txt, g2d) + 10;
		g2d.fillRoundRect(x, y, larguraTexto, 30, 0, 0);
		g2d.setColor(Color.BLACK);
		g2d.drawString(txt, x + 5, y + 25);
		g2d.setFont(fontOri);
		return larguraTexto;
	}

	private void desenhaResultadoFinal(Graphics2D g2d) {
		if (!isExibeResultadoFinal()) {
			return;
		}
		ControleSom.somLigado = false;
		int x = limitesViewPort.x + (limitesViewPort.width / 2) - 450;
		int y = limitesViewPort.y + (limitesViewPort.height / 2) - 260;
		int xOri = x;
		int yTitulo = y - 25;
		Font fontOri = g2d.getFont();
		Font fontNegrito = new Font(fontOri.getName(), Font.BOLD,
				fontOri.getSize());
		Font fontMaior = new Font(fontOri.getName(), Font.BOLD, 16);
		g2d.setFont(fontMaior);
		for (int i = 0; i < pilotosList.size(); i++) {
			Piloto piloto = pilotosList.get(i);

			Color corBorda = null;

			if (piloto.isJogadorHumano()
					&& controleJogo.getPilotoJogador().equals(piloto)) {
				corBorda = OcilaCor.geraOcila("mrkSelBlu", bluQualy);

			}
			if (piloto.isJogadorHumano()
					&& !controleJogo.getPilotoJogador().equals(piloto)) {
				corBorda = OcilaCor.geraOcila("mrkSelOran", Color.ORANGE);

			}
			if (controleJogo.verirficaDesafiandoCampeonato(piloto)) {
				corBorda = OcilaCor.geraOcila("mrkDesaf", Color.ORANGE);
			}

			/**
			 * capacete
			 */
			BufferedImage cap = capacetesResultadoFinal.get(piloto);
			if (cap == null) {
				cap = ImageUtil.geraResize(ImageUtil
						.copiaImagem(controleJogo.obterCapacete(piloto)), 0.5);
				capacetesResultadoFinal.put(piloto, cap);
			}
			if (cap != null && desenhaImagens)
				g2d.drawImage(cap, x, y, null);

			x += 30;

			/**
			 * Posicao
			 */
			if (i == 0) {
				g2d.setFont(fontNegrito);
				g2d.setColor(transpMenus);
				g2d.fillRoundRect(x, yTitulo, 30, 20, 0, 0);
				g2d.setColor(Color.BLACK);
				g2d.drawString("" + Lang.msg("pos"), x + 2, yTitulo + 16);
				g2d.setFont(fontMaior);
			}
			g2d.setColor(transpMenus);
			g2d.fillRoundRect(x, y, 30, 20, 0, 0);
			g2d.setColor(Color.BLACK);
			g2d.drawString("" + piloto.getPosicao(), x + 5, y + 16);
			if (corBorda != null) {
				desenhaBordaResultadoFinal(g2d, x, y, 30, 20, corBorda);
			}

			x += 35;
			/**
			 * Piloto
			 */
			if (i == 0) {
				g2d.setFont(fontNegrito);
				g2d.setColor(transpMenus);
				g2d.fillRoundRect(x, yTitulo, 140, 20, 0, 0);
				g2d.setColor(Color.BLACK);
				g2d.drawString("" + Lang.msg("153").toUpperCase(), x + 50,
						yTitulo + 16);
				g2d.setFont(fontMaior);
			}

			g2d.setColor(transpMenus);
			g2d.fillRoundRect(x, y, 140, 20, 0, 0);
			g2d.setColor(Color.BLACK);
			g2d.drawString(piloto.getNome(), x + 10, y + 16);

			if (corBorda != null) {
				desenhaBordaResultadoFinal(g2d, x, y, 140, 20, corBorda);
			}

			x += 150;
			/**
			 * Equipe
			 */
			if (i == 0) {
				g2d.setFont(fontNegrito);
				g2d.setColor(transpMenus);
				g2d.fillRoundRect(x, yTitulo, 160, 20, 0, 0);
				g2d.setColor(Color.BLACK);
				g2d.drawString("" + Lang.msg("277").toUpperCase(), x + 50,
						yTitulo + 16);
				g2d.setFont(fontMaior);
			}
			g2d.setColor(transpMenus);
			g2d.fillRoundRect(x, y, 160, 20, 0, 0);
			g2d.setColor(Color.BLACK);
			g2d.drawString(piloto.getCarro().getNome(), x + 10, y + 16);
			if (corBorda != null) {
				desenhaBordaResultadoFinal(g2d, x, y, 160, 20, corBorda);
			}
			x += 170;

			/**
			 * Pneus
			 */
			BufferedImage pneu = null;
			if (Carro.TIPO_PNEU_MOLE.equals(piloto.getCarro().getTipoPneu())) {
				pneu = pneuMoleImgMini;
			}
			if (Carro.TIPO_PNEU_DURO.equals(piloto.getCarro().getTipoPneu())) {
				pneu = pneuDuroImgMini;
			}
			if (Carro.TIPO_PNEU_CHUVA.equals(piloto.getCarro().getTipoPneu())) {
				pneu = pneuChuvaImgMini;
			}
			g2d.drawImage(pneu, x, y, null);

			x += 30;

			Volta volta = piloto.obterVoltaMaisRapida();

			String melhorVolta = "";
			if (volta != null) {
				melhorVolta = volta.getTempoVoltaFormatado();
			}

			/**
			 * melhorVolta
			 */
			if (i == 0) {
				g2d.setFont(fontNegrito);
				g2d.setColor(transpMenus);
				g2d.fillRoundRect(x, yTitulo, 80, 20, 0, 0);
				g2d.setColor(Color.BLACK);
				g2d.drawString("" + Lang.msg("278").toUpperCase(), x + 10,
						yTitulo + 16);
				g2d.setFont(fontMaior);
			}
			g2d.setColor(transpMenus);
			g2d.fillRoundRect(x, y, 80, 20, 0, 0);
			g2d.setColor(Color.BLACK);
			g2d.drawString(melhorVolta, x + 5, y + 16);

			if (corBorda != null) {
				desenhaBordaResultadoFinal(g2d, x, y, 80, 20, corBorda);
			}

			x += 90;
			/**
			 * Paradas
			 */
			if (i == 0) {
				g2d.setFont(fontNegrito);
				g2d.setColor(transpMenus);
				g2d.fillRoundRect(x, yTitulo, 40, 20, 0, 0);
				g2d.setColor(Color.BLACK);
				g2d.drawString("" + Lang.msg("147").toUpperCase(), x + 5,
						yTitulo + 16);
				g2d.setFont(fontMaior);
			}
			g2d.setColor(transpMenus);
			g2d.fillRoundRect(x, y, 40, 20, 0, 0);
			g2d.setColor(Color.BLACK);
			g2d.drawString("" + piloto.getQtdeParadasBox(), x + 10, y + 16);

			if (corBorda != null) {
				desenhaBordaResultadoFinal(g2d, x, y, 40, 20, corBorda);
			}

			x += 45;

			/**
			 * %Pneus
			 */
			if (i == 0) {
				g2d.setFont(fontNegrito);
				g2d.setColor(transpMenus);
				g2d.fillRoundRect(x, yTitulo, 50, 20, 0, 0);
				g2d.setColor(Color.BLACK);
				g2d.drawString("" + Lang.msg("216").toUpperCase(), x + 5,
						yTitulo + 16);
				g2d.setFont(fontMaior);
			}
			g2d.setColor(transpMenus);
			g2d.fillRoundRect(x, y, 50, 20, 0, 0);
			g2d.setColor(Color.BLACK);
			int porcentagemDesgastePneus = piloto.getCarro()
					.getPorcentagemDesgastePneus();
			if (porcentagemDesgastePneus < 0) {
				porcentagemDesgastePneus = 0;
			}
			g2d.drawString("" + porcentagemDesgastePneus + "%", x + 12, y + 16);

			if (corBorda != null) {
				desenhaBordaResultadoFinal(g2d, x, y, 50, 20, corBorda);
			}

			x += 55;

			/**
			 * %Combustivel
			 */
			if (i == 0) {
				g2d.setFont(fontNegrito);
				g2d.setColor(transpMenus);
				g2d.fillRoundRect(x, yTitulo, 50, 20, 0, 0);
				g2d.setColor(Color.BLACK);
				g2d.drawString("" + Lang.msg("215").toUpperCase(), x + 5,
						yTitulo + 16);
				g2d.setFont(fontMaior);
			}
			g2d.setColor(transpMenus);
			g2d.fillRoundRect(x, y, 50, 20, 0, 0);
			g2d.setColor(Color.BLACK);
			int porcentagemCombustivel = piloto.getCarro()
					.getPorcentagemCombustivel();
			if (porcentagemCombustivel < 0) {
				porcentagemCombustivel = 0;
			}
			g2d.drawString("" + porcentagemCombustivel + "%", x + 12, y + 16);

			if (corBorda != null) {
				desenhaBordaResultadoFinal(g2d, x, y, 50, 20, corBorda);
			}

			x += 55;

			/**
			 * %Motor
			 */
			if (i == 0) {
				g2d.setFont(fontNegrito);
				g2d.setColor(transpMenus);
				g2d.fillRoundRect(x, yTitulo, 50, 20, 0, 0);
				g2d.setColor(Color.BLACK);
				g2d.drawString("" + Lang.msg("217").toUpperCase(), x + 2,
						yTitulo + 16);
				g2d.setFont(fontMaior);
			}
			g2d.setColor(transpMenus);
			g2d.fillRoundRect(x, y, 50, 20, 0, 0);
			g2d.setColor(Color.BLACK);
			int porcentagemDesgasteMotor = piloto.getCarro()
					.getPorcentagemDesgasteMotor();
			if (porcentagemDesgasteMotor < 0) {
				porcentagemDesgasteMotor = 0;
			}
			g2d.drawString("" + porcentagemDesgasteMotor + "%", x + 12, y + 16);

			if (corBorda != null) {
				desenhaBordaResultadoFinal(g2d, x, y, 50, 20, corBorda);
			}

			x += 55;

			/**
			 * Pontos
			 */
			if (i == 0) {
				g2d.setFont(fontNegrito);
				g2d.setColor(transpMenus);
				g2d.fillRoundRect(x, yTitulo, 50, 20, 0, 0);
				g2d.setColor(Color.BLACK);
				g2d.drawString("" + Lang.msg("161").toUpperCase(), x + 2,
						yTitulo + 16);
				g2d.setFont(fontMaior);
			}
			g2d.setColor(transpMenus);
			g2d.fillRoundRect(x, y, 50, 20, 0, 0);
			g2d.setColor(Color.BLACK);
			g2d.drawString("" + ControleCorrida.calculaPontos25(piloto), x + 15,
					y + 16);

			if (corBorda != null) {
				desenhaBordaResultadoFinal(g2d, x, y, 50, 20, corBorda);
			}

			x += 55;

			/**
			 * Dif
			 */
			if (i == 0) {
				g2d.setFont(fontNegrito);
				g2d.setColor(transpMenus);
				g2d.fillRoundRect(x, yTitulo, 50, 20, 0, 0);
				g2d.setColor(Color.BLACK);
				g2d.drawString("" + Lang.msg("diff").toUpperCase(), x + 18,
						yTitulo + 16);
				g2d.setFont(fontMaior);
			}
			int diff = (piloto.getPosicaoInicial() - piloto.getPosicao());
			g2d.setColor(transpMenus);
			g2d.fillRoundRect(x, y, 50, 20, 0, 0);
			g2d.setFont(new Font(fontOri.getName(), Font.BOLD, 18));
			if (diff > 0) {
				g2d.setColor(gre);
				g2d.drawString(" \u2191", x + 5, y + 16);
			} else if (diff < 0) {
				g2d.setColor(red);
				g2d.drawString(" \u2193", x + 5, y + 16);
			}
			g2d.setFont(fontMaior);
			g2d.setColor(Color.BLACK);
			g2d.drawString("" + diff, x + 20, y + 16);

			x += 55;

			/**
			 * vantagem
			 */
			if (i == 0) {
				g2d.setFont(fontNegrito);
				g2d.setColor(transpMenus);
				g2d.fillRoundRect(x, yTitulo, 80, 20, 0, 0);
				g2d.setColor(Color.BLACK);
				g2d.drawString("" + Lang.msg("vantagem").toUpperCase(), x + 10,
						yTitulo + 16);
				g2d.setFont(fontMaior);
			}
			g2d.setColor(transpMenus);
			g2d.fillRoundRect(x, y, 80, 20, 0, 0);
			g2d.setColor(Color.BLACK);
			String vantagem = piloto.getVantagem();
			if (piloto.getVantagem() == null || piloto
					.getPosicao() == controleJogo.getPilotosCopia().size()) {
				vantagem = "";
			}
			g2d.drawString(vantagem, x + 5, y + 16);

			if (corBorda != null) {
				desenhaBordaResultadoFinal(g2d, x, y, 80, 20, corBorda);
			}

			y += 24;
			x = xOri;
		}

		g2d.setFont(fontOri);
	}

	private void desenhaBordaResultadoFinal(Graphics2D g2d, int x, int y,
			int xLarg, int yLarg, Color corBorda) {
		Stroke stroke = g2d.getStroke();
		g2d.setColor(corBorda);
		g2d.setStroke(borda);
		g2d.drawRoundRect(x - 2, y - 2, xLarg + 3, yLarg + 3, 0, 0);
		g2d.setStroke(stroke);
	}

	private void desenhaFPS(Graphics2D g2d) {
		String msg = "FPS";
		if (contMostraFPS >= 0 && contMostraFPS < 200) {

			msg = "  " + gerenciadorVisual.getFps();
		} else if (contMostraFPS > 200) {
			contMostraFPS = -20;
		}
		contMostraFPS++;
		int x = limitesViewPort.x + (limitesViewPort.width) - 70;
		int y = Util
				.inteiro(limitesViewPort.y + limitesViewPort.getHeight() - 90);
		g2d.setColor(transpMenus);
		fps.setFrame(x, y, 65, 35);
		g2d.fill(fps);
		Font fontOri = g2d.getFont();
		g2d.setFont(new Font(fontOri.getName(), Font.BOLD, 28));
		g2d.setColor(OcilaCor.porcentVerde100Vermelho0(
				Util.inteiro(gerenciadorVisual.getFps() * 1.6)));
		g2d.drawString(msg, x + 2, y + 26);
		g2d.setFont(fontOri);
	}

	private void desenhaAjuda(Graphics2D g2d) {
		if (isExibeResultadoFinal()) {
			return;
		}
		String msg = " ? ";
		g2d.setColor(transpMenus);
		int x = limitesViewPort.x + (limitesViewPort.width / 2) - 130;
		int y = limitesViewPort.y + 5;
		ajuda.setFrame(x, y, 35, 35);
		if (verControles) {
			g2d.setColor(OcilaCor.geraOcila("desenhaAjuda", yel));
		} else {
			g2d.setColor(transpMenus);
		}
		g2d.fill(ajuda);
		Font fontOri = g2d.getFont();
		g2d.setFont(new Font(fontOri.getName(), Font.BOLD, 28));
		g2d.setColor(Color.BLACK);
		g2d.drawString(msg, x + 2, y + 26);
		g2d.setFont(fontOri);
	}

	private void desenhaLag(Graphics2D g2d) {
		if (controleJogo.getLag() > 50) {
			String msg = "LAG";
			int lag = controleJogo.getLag();
			if (contMostraLag >= 0 && contMostraLag < 200) {
				if (lag > 999) {
					lag = 999;
				}
				msg = " " + mil.format(lag);
			} else if (contMostraLag > 200) {
				contMostraLag = -20;
			}
			contMostraLag++;
			int x = limitesViewPort.x + (limitesViewPort.width) - 70;
			int y = Util.inteiro(
					limitesViewPort.y + limitesViewPort.getHeight() - 130);
			g2d.setColor(transpMenus);
			g2d.fillRoundRect(x, y, 65, 35, 0, 0);
			Font fontOri = g2d.getFont();
			g2d.setFont(new Font(fontOri.getName(), Font.BOLD, 28));
			g2d.setColor(OcilaCor.porcentVermelho100Verde0(lag / 3));
			g2d.drawString(msg, x + 2, y + 26);
			g2d.setFont(fontOri);
		}

	}

	private void desenhaNarracao(Graphics2D g2d) {

		if (!desenhaInfo) {
			return;
		}
		if (isExibeResultadoFinal()) {
			return;
		}
		if (limitesViewPort == null) {
			return;
		}
		if (verificaComponeteNaParteInferior()) {
			return;
		}
		int x = limitesViewPort.x + 5;
		int y = limitesViewPort.y + limitesViewPort.height - 195;
		Font fontOri = g2d.getFont();
		g2d.setColor(transpMenus);
		g2d.fillRoundRect(x, y, 420, 110, 0, 0);
		g2d.setColor(Color.BLACK);
		if (controleJogo.listaInfo() != null
				&& !controleJogo.listaInfo().isEmpty()) {
			List listaInfo = controleJogo.listaInfo();
			int indemax = listaInfo.size() - 1;
			int cont = 1;
			while (indemax > -1) {
				String info = (String) controleJogo.listaInfo().get(indemax);
				if (info.contains("<table>")) {
					if (!info.equals(infoComp)) {
						infoComp = info;
						infoCompCont = 1000;
					}
				} else {
					if (info.contains("FF8C00")) {
						g2d.setColor(new Color(190, 80, 0));
					}
					if (info.contains("4682B4")) {
						g2d.setColor(new Color(45, 98, 168));
					}
					if (info.contains("FE0000")) {
						g2d.setColor(new Color(121, 0, 0));
					}
					if (info.contains("008D25")) {
						g2d.setColor(new Color(0, 80, 0));
					}
					if (info.contains("2D62A8")) {
						g2d.setColor(new Color(0, 0, 100));
					}
					info = Html.tagsJava2d(info);
					g2d.setFont(new Font(fontOri.getName(), Font.BOLD,
							fontOri.getSize()));
					int c = (cont++);
					g2d.drawString("" + info, x + 4, y + (20 * c));
				}
				indemax--;
				if (cont > 5) {
					break;
				}
			}
			g2d.setFont(fontOri);
		}

	}

	private boolean verificaComponeteNaParteInferior() {
		return desenhaBkg && !(backGround != null && ((limitesViewPort.y
				+ limitesViewPort.height)) < (backGround.getHeight() * 0.99)
						* zoom);
	}

	public static void main(String[] args) {
	}

	private static String geraLabelVoltaTabelaComparativa(String parte,
			String volta1) {
		for (int i = 0; i < parte.length(); i++) {
			if (parte.charAt(i) == '<') {
				break;
			}
			volta1 += parte.charAt(i);
		}
		return volta1;
	}

	private void desenhaTabelaComparativa(Graphics2D g2d) {
		if (infoCompCont < 0 || Util.isNullOrEmpty(infoComp)) {
			return;
		}
		if (isExibeResultadoFinal()) {
			return;
		}
		if (verificaComponeteNaParteInferior()) {
			return;
		}
		String parte[] = infoComp.split("<font face='sans-serif' >");
		String volta1 = "";
		volta1 = geraLabelVoltaTabelaComparativa(parte[1], volta1);
		String volta2 = "";
		volta2 = geraLabelVoltaTabelaComparativa(parte[2], volta2);
		String volta3 = "";
		volta3 = geraLabelVoltaTabelaComparativa(parte[3], volta3);
		String nmPilotoFrente = "";
		nmPilotoFrente = geraLabelVoltaTabelaComparativa(parte[4],
				nmPilotoFrente);
		String t1PilotoFrente = "";
		t1PilotoFrente = geraLabelVoltaTabelaComparativa(parte[5],
				t1PilotoFrente);
		String t2PilotoFrente = "";
		t2PilotoFrente = geraLabelVoltaTabelaComparativa(parte[6],
				t2PilotoFrente);
		String t3PilotoFrente = "";
		t3PilotoFrente = geraLabelVoltaTabelaComparativa(parte[7],
				t3PilotoFrente);

		String nmPilotoTraz = "";
		nmPilotoTraz = geraLabelVoltaTabelaComparativa(parte[8], nmPilotoTraz);
		String t1PilotoTraz = "";
		t1PilotoTraz = geraLabelVoltaTabelaComparativa(parte[9], t1PilotoTraz);
		String t2PilotoTraz = "";
		t2PilotoTraz = geraLabelVoltaTabelaComparativa(parte[10], t2PilotoTraz);
		String t3PilotoTraz = "";
		t3PilotoTraz = geraLabelVoltaTabelaComparativa(parte[11], t3PilotoTraz);
		String t1Diff = "";
		t1Diff = geraLabelVoltaTabelaComparativa(parte[12], t1Diff);
		String t2Diff = "";
		t2Diff = geraLabelVoltaTabelaComparativa(parte[13], t2Diff);
		String t3Diff = "";
		t3Diff = geraLabelVoltaTabelaComparativa(parte[14], t3Diff);

		int x = limitesViewPort.x + limitesViewPort.width - 580;
		int y = limitesViewPort.y + limitesViewPort.height - 150;

		Color bkg = transpMenus;
		Color fonte = Color.black;

		RoundRectangle2D rectanglePos = new RoundRectangle2D.Double(x, y, 100,
				20, 0, 0);
		g2d.setColor(bkg);
		g2d.fill(rectanglePos);
		g2d.setColor(fonte);
		g2d.drawString(nmPilotoFrente, x + 5, y + 16);

		rectanglePos = new RoundRectangle2D.Double(x, y + 22, 100, 20, 0, 0);
		g2d.setColor(bkg);
		g2d.fill(rectanglePos);
		g2d.setColor(fonte);
		g2d.drawString(nmPilotoTraz, x + 5, y + 38);

		rectanglePos = new RoundRectangle2D.Double(x + 102, y - 22, 100, 20, 0,
				0);
		g2d.setColor(bkg);
		g2d.fill(rectanglePos);
		g2d.setColor(fonte);
		g2d.drawString(volta1, x + 107, y - 8);

		rectanglePos = new RoundRectangle2D.Double(x + 102, y, 100, 20, 0, 0);
		g2d.setColor(bkg);
		g2d.fill(rectanglePos);
		g2d.setColor(fonte);
		g2d.drawString(t1PilotoFrente, x + 117, y + 16);

		rectanglePos = new RoundRectangle2D.Double(x + 102, y + 22, 100, 20, 0,
				0);
		g2d.setColor(bkg);
		g2d.fill(rectanglePos);
		g2d.setColor(fonte);
		g2d.drawString(t1PilotoTraz, x + 117, y + 38);

		rectanglePos = new RoundRectangle2D.Double(x + 102, y + 44, 100, 20, 0,
				0);
		if (t1Diff.startsWith("-")) {
			g2d.setColor(gre);
		} else {
			g2d.setColor(yel);
		}
		g2d.fill(rectanglePos);
		g2d.setColor(fonte);
		g2d.drawString(t1Diff, x + 117, y + 60);

		rectanglePos = new RoundRectangle2D.Double(x + 204, y - 22, 100, 20, 0,
				0);
		g2d.setColor(bkg);
		g2d.fill(rectanglePos);
		g2d.setColor(fonte);
		g2d.drawString(volta2, x + 209, y - 8);

		rectanglePos = new RoundRectangle2D.Double(x + 204, y, 100, 20, 0, 0);
		g2d.setColor(bkg);
		g2d.fill(rectanglePos);
		g2d.setColor(fonte);
		g2d.drawString(t2PilotoFrente, x + 219, y + 16);

		rectanglePos = new RoundRectangle2D.Double(x + 204, y + 22, 100, 20, 0,
				0);
		g2d.setColor(bkg);
		g2d.fill(rectanglePos);
		g2d.setColor(fonte);
		g2d.drawString(t2PilotoTraz, x + 219, y + 38);

		rectanglePos = new RoundRectangle2D.Double(x + 204, y + 44, 100, 20, 0,
				0);
		if (t2Diff.startsWith("-")) {
			g2d.setColor(gre);
		} else {
			g2d.setColor(yel);
		}
		g2d.fill(rectanglePos);
		g2d.setColor(fonte);
		g2d.drawString(t2Diff, x + 219, y + 60);

		rectanglePos = new RoundRectangle2D.Double(x + 306, y - 22, 100, 20, 0,
				0);
		g2d.setColor(bkg);
		g2d.fill(rectanglePos);
		g2d.setColor(fonte);
		g2d.drawString(volta3, x + 311, y - 8);

		rectanglePos = new RoundRectangle2D.Double(x + 306, y, 100, 20, 0, 0);
		g2d.setColor(bkg);
		g2d.fill(rectanglePos);
		g2d.setColor(fonte);
		g2d.drawString(t3PilotoFrente, x + 321, y + 16);

		rectanglePos = new RoundRectangle2D.Double(x + 306, y + 22, 100, 20, 0,
				0);
		g2d.setColor(bkg);
		g2d.fill(rectanglePos);
		g2d.setColor(fonte);
		g2d.drawString(t3PilotoTraz, x + 321, y + 38);

		rectanglePos = new RoundRectangle2D.Double(x + 306, y + 44, 100, 20, 0,
				0);
		if (t3Diff.startsWith("-")) {
			g2d.setColor(gre);
		} else {
			g2d.setColor(yel);
		}
		g2d.fill(rectanglePos);
		g2d.setColor(fonte);
		g2d.drawString(t3Diff, x + 321, y + 60);
		infoCompCont--;
	}

	private void desenhaDebugIinfo(Graphics2D g2d) {
		if (!Logger.ativo) {
			return;
		}
		if (pilotoSelecionado == null) {
			return;
		}
		int altura = (int) (Carro.LARGURA * 5 * zoom);
		int mAltura = (int) (altura / 2 * zoom);
		List<Point> escapeList = circuito.getEscapeList();

		if (escapeList == null) {
			escapeList = new ArrayList<Point>();
		}

		if (escapeList.isEmpty()) {
			List<ObjetoPista> objetos = circuito.getObjetos();
			if (objetos != null) {
				for (Iterator iterator = objetos.iterator(); iterator
						.hasNext();) {
					ObjetoPista objetoPista = (ObjetoPista) iterator.next();
					if (objetoPista instanceof ObjetoEscapada) {
						ObjetoEscapada objetoEscapada = (ObjetoEscapada) objetoPista;
						escapeList.add(objetoEscapada.centro());
					}
				}
			}
		}

		if (escapeList != null) {
			for (Iterator iterator = escapeList.iterator(); iterator
					.hasNext();) {
				Point point = (Point) iterator.next();
				g2d.setColor(ver);
				g2d.fillOval(
						(int) ((point.x - descontoCentraliza.x) * zoom)
								- mAltura,
						(int) ((point.y - descontoCentraliza.y) * zoom)
								- mAltura,
						altura, altura);
			}
		}

		if (pontoClicado != null) {
			g2d.setColor(ver);
			g2d.fillOval(pontoClicado.x, pontoClicado.y, 10, 10);

		}

	}

	private void iniciaPilotoSelecionado() {
		if (controleJogo instanceof JogoCliente) {
			Piloto ps = controleJogo.getPilotoSelecionado();
			if (ps == null) {
				controleJogo.selecionaPilotoJogador();
				ps = controleJogo.getPilotoSelecionado();
				controleJogo.selecionouPiloto(ps);
			}
			if (pilotoSelecionado == null) {
				controleJogo.selecionouPiloto(ps);
				pilotoSelecionado = ps;
				controleJogo.getMainFrame().requestFocus();

			}
			if (ps != null && !ps.equals(pilotoSelecionado)) {
				pilotoSelecionado = ps;
				controleJogo.getMainFrame().requestFocus();
			}
		}
	}

	private void desenhaControlesBox(Graphics2D g2d) {
		if (isExibeResultadoFinal()) {
			return;
		}
		if (isVerControles()) {
			return;
		}
		if (pilotoSelecionado != null
				&& !pilotoSelecionado.equals(controleJogo.getPilotoJogador())) {
			return;
		}
		if (!pilotoSelecionado.isBox()) {
			return;
		}

		int altura = 100;

		if (!controleJogo.isSemReabastacimento()) {
			desenhaControlesReabastecimentoBox(g2d, altura);
			altura += 50;
		}
		if (!controleJogo.isSemTrocaPneu()) {
			desenhaControlePneuBox(g2d, altura);
			altura += 50;
		}
		if (!controleJogo.isDrs() || controleJogo.isChovendo()) {
			desenhaControleAsaBox(g2d, altura);
		}
	}

	private void desenhaControlesReabastecimentoBox(Graphics2D g2d,
			int altura) {
		int porcentCombust = 50;

		if (controleJogo.getPilotoJogador() != null) {
			porcentCombust = controleJogo.getPilotoJogador()
					.getQtdeCombustBox();
		}

		Font fontOri = g2d.getFont();
		g2d.setFont(new Font(fontOri.getName(), Font.BOLD, 28));

		int x = limitesViewPort.x + (limitesViewPort.width / 2);
		int y = limitesViewPort.y + altura;
		g2d.setColor(transpMenus);
		String combst = Lang.msg("Combustivel") + " " + porcentCombust + "%";
		int tamCombust = Util.calculaLarguraText(combst, g2d);
		x -= (tamCombust / 2) - 20;
		porcentCombustivelTela.setFrame(x - 15, y - 12, tamCombust + 5, 32);
		if (pilotoSelecionado != null && pilotoSelecionado.isBox()) {
			Stroke stroke = g2d.getStroke();
			g2d.setStroke(trilhoMiniPista);
			g2d.setColor(yel);
			g2d.draw(porcentCombustivelTela);
			g2d.setStroke(stroke);
			g2d.setColor(transpMenus);
			g2d.drawString(combst, x - 10, y + 15);
		} else {
			g2d.draw(porcentCombustivelTela);
			g2d.drawString(combst, x - 10, y + 15);
		}

		g2d.setColor(transpMenus);
		x -= 20;

		String menos = "-";
		int tamMenos = Util.calculaLarguraText(menos, g2d);
		menosCombust.setFrame(x - 16, y - 6, tamMenos + 6, 22);
		g2d.draw(menosCombust);
		g2d.drawString(menos, x - 14, y + 15);

		x += tamCombust + 35;

		String mais = "+";
		int tamMais = Util.calculaLarguraText(mais, g2d);
		maisCombust.setFrame(x - 17, y - 6, tamMais + 5, 22);
		g2d.draw(maisCombust);
		g2d.drawString(mais, x - 13, y + 16);

		g2d.setFont(fontOri);
	}

	private void desenhaComandoIrBox(Graphics2D g2d) {
		if (isExibeResultadoFinal()) {
			return;
		}
		if (qtdeLuzesAcesas > 0) {
			return;
		}
		if (pilotoSelecionado != null
				&& !pilotoSelecionado.equals(controleJogo.getPilotoJogador())) {
			return;
		}
		int x = limitesViewPort.x + (limitesViewPort.width / 2) - 45;
		int y = limitesViewPort.y + 40;
		Font fontOri = g2d.getFont();
		g2d.setFont(new Font(fontOri.getName(), Font.BOLD, 28));

		String txtBox = Lang.msg("078");
		Color bg = transpMenus;
		if (pilotoSelecionado != null && pilotoSelecionado.isBox()) {
			bg = OcilaCor.geraOcila("vaiBox", transpSel);
		}
		g2d.setColor(bg);
		int tam = Util.calculaLarguraText(txtBox, g2d);
		vaiBox.setFrame(x, y, tam + 10, 30);
		g2d.fill(vaiBox);
		if (pilotoSelecionado != null && pilotoSelecionado.isBox()) {
			g2d.setColor(yel);
			Stroke stroke = g2d.getStroke();
			g2d.setStroke(trilhoMiniPista);
			g2d.draw(vaiBox);
			g2d.setStroke(stroke);
		}
		g2d.setColor(transpMenus);
		g2d.drawString(txtBox, x + 5, y + 25);
		g2d.setFont(fontOri);
	}

	private void desenhaControlePneuBox(Graphics2D g2d, int altura) {
		int x = limitesViewPort.x + (limitesViewPort.width / 2) - 170;
		int y = limitesViewPort.y + altura;

		String tpPneu = Carro.TIPO_PNEU_DURO;

		if (controleJogo.getPilotoJogador() != null) {
			tpPneu = controleJogo.getPilotoJogador().getTipoPneuBox();
		}

		boolean moleSel = false;
		if (pilotoSelecionado != null && Carro.TIPO_PNEU_MOLE.equals(tpPneu)) {
			g2d.setColor(transpSel);
			moleSel = true;
		} else {
			g2d.setColor(transpMenus);
		}
		String strMole = Lang.msg("TIPO_PNEU_MOLE");
		int tamMole = Util.calculaLarguraText(strMole, g2d);
		if (desenhaImagens)
			g2d.drawImage(pneuMoleImg, x, y - 10, null);
		x += pneuChuvaImg.getWidth() + 2;
		pneuMole.setFrame(x, y, tamMole + 10, 20);
		g2d.fill(pneuMole);
		if (moleSel) {
			g2d.setColor(yel);
			Stroke stroke = g2d.getStroke();
			g2d.setStroke(trilhoMiniPista);
			g2d.draw(pneuMole);
			g2d.setStroke(stroke);
		}
		g2d.setColor(Color.black);
		g2d.drawString(strMole, x + 5, y + 16);

		x += (tamMole + 15);
		boolean duroSel = false;
		if (pilotoSelecionado != null && Carro.TIPO_PNEU_DURO.equals(tpPneu)) {
			g2d.setColor(transpSel);
			duroSel = true;
		} else {
			g2d.setColor(transpMenus);
		}
		String strDuro = Lang.msg("TIPO_PNEU_DURO");
		int tamDuro = Util.calculaLarguraText(strDuro, g2d);
		if (desenhaImagens)
			g2d.drawImage(pneuDuroImg, x, y - 10, null);
		x += pneuDuroImg.getWidth() + 2;
		pneuDuro.setFrame(x, y, tamDuro + 10, 20);
		g2d.fill(pneuDuro);
		if (duroSel) {
			g2d.setColor(yel);
			Stroke stroke = g2d.getStroke();
			g2d.setStroke(trilhoMiniPista);
			g2d.draw(pneuDuro);
			g2d.setStroke(stroke);
		}

		g2d.setColor(Color.black);
		g2d.drawString(strDuro, x + 5, y + 16);
		x += (tamDuro + 15);
		boolean chuvaSel = false;
		if (pilotoSelecionado != null && Carro.TIPO_PNEU_CHUVA.equals(tpPneu)) {
			g2d.setColor(transpSel);
			chuvaSel = true;
		} else {
			g2d.setColor(transpMenus);
		}
		String strChuva = Lang.msg("TIPO_PNEU_CHUVA");
		int tamChuva = Util.calculaLarguraText(strChuva, g2d);
		if (desenhaImagens)
			g2d.drawImage(pneuChuvaImg, x, y - 10, null);
		x += pneuChuvaImg.getWidth() + 2;
		pneuChuva.setFrame(x, y, tamChuva + 10, 20);
		g2d.fill(pneuChuva);
		if (chuvaSel) {
			g2d.setColor(yel);
			Stroke stroke = g2d.getStroke();
			g2d.setStroke(trilhoMiniPista);
			g2d.draw(pneuChuva);
			g2d.setStroke(stroke);
		}
		g2d.setColor(Color.black);
		g2d.drawString(strChuva, x + 5, y + 16);

	}

	private void desenhaControleAsaBox(Graphics2D g2d, int altura) {
		int x = limitesViewPort.x + (limitesViewPort.width / 2) - 170;
		int y = limitesViewPort.y + altura;

		String tpAsa = Carro.ASA_NORMAL;
		if (controleJogo.isDrs()) {
			tpAsa = Carro.MAIS_ASA;
		}

		if (controleJogo.getPilotoJogador() != null) {
			tpAsa = controleJogo.getPilotoJogador().getAsaBox();
		}

		boolean mensoSel = false;
		if (pilotoSelecionado != null && Carro.MENOS_ASA.equals(tpAsa)) {
			g2d.setColor(transpSel);
			mensoSel = true;
		} else {
			g2d.setColor(transpMenus);
		}
		if (desenhaImagens)
			g2d.drawImage(menosAsaIco, x, y - (menosAsaIco.getHeight() / 3),
					null);
		x += menosAsaIco.getWidth() + 2;
		String strMenos = Lang.msg("MENOS_ASA");
		int tamMenos = Util.calculaLarguraText(strMenos, g2d);
		menosAsa.setFrame(x, y, tamMenos + 10, 20);
		g2d.fill(menosAsa);
		if (mensoSel) {
			g2d.setColor(yel);
			Stroke stroke = g2d.getStroke();
			g2d.setStroke(trilhoMiniPista);
			g2d.draw(menosAsa);
			g2d.setStroke(stroke);
		}
		g2d.setColor(Color.black);
		g2d.drawString(strMenos, x + 5, y + 16);

		x += (tamMenos + 20);

		boolean norSel = false;
		if (pilotoSelecionado != null && Carro.ASA_NORMAL.equals(tpAsa)) {
			g2d.setColor(transpSel);
			norSel = true;
		} else {
			g2d.setColor(transpMenus);
		}
		if (desenhaImagens)
			g2d.drawImage(normalAsaIco, x, y - (normalAsaIco.getHeight() / 3),
					null);
		x += normalAsaIco.getWidth() + 2;
		String strNormal = Lang.msg("ASA_NORMAL");
		int tamNormal = Util.calculaLarguraText(strNormal, g2d);
		normalAsa.setFrame(x, y, tamNormal + 10, 20);
		g2d.fill(normalAsa);

		if (norSel) {
			g2d.setColor(yel);
			Stroke stroke = g2d.getStroke();
			g2d.setStroke(trilhoMiniPista);
			g2d.draw(normalAsa);
			g2d.setStroke(stroke);
		}

		g2d.setColor(Color.black);
		g2d.drawString(strNormal, x + 5, y + 16);

		x += (tamNormal + 20);
		boolean maisSel = false;
		if (pilotoSelecionado != null && Carro.MAIS_ASA.equals(tpAsa)) {
			g2d.setColor(transpSel);
			maisSel = true;
		} else {
			g2d.setColor(transpMenus);
		}
		if (desenhaImagens)
			g2d.drawImage(maisAsaIco, x, y - (maisAsaIco.getHeight() / 3),
					null);
		x += maisAsaIco.getWidth() + 2;

		String strMais = Lang.msg("MAIS_ASA");
		int tamMais = Util.calculaLarguraText(strMais, g2d);
		maisAsa.setFrame(x, y, tamMais + 10, 20);
		g2d.fill(maisAsa);

		if (maisSel) {
			g2d.setColor(yel);
			Stroke stroke = g2d.getStroke();
			g2d.setStroke(trilhoMiniPista);
			g2d.draw(maisAsa);
			g2d.setStroke(stroke);
		}

		g2d.setColor(Color.black);
		g2d.drawString(strMais, x + 5, y + 16);

	}

	private void desenhaControles(Graphics2D g2d) {
		if (!desenhaInfo) {
			return;
		}
		if (isExibeResultadoFinal()) {
			return;
		}
		if (limitesViewPort == null) {
			return;
		}
		if (pilotoSelecionado != null
				&& !pilotoSelecionado.equals(controleJogo.getPilotoJogador())) {
			return;
		}
		desenhaControlesGiro(g2d);
		desenhaControlesPiloto(g2d);
	}

	private void desenhaControlesPiloto(Graphics2D g2d) {
		int x = limitesViewPort.x + limitesViewPort.width;
		int y = limitesViewPort.y + limitesViewPort.height - 25;

		String strAgressivo = Lang.msg("077");
		int tamAgressivo = Util.calculaLarguraText(strAgressivo, g2d);
		x -= (tamAgressivo + 15);
		f7_C.setFrame(x, y, tamAgressivo + 10, 20);
		if (pilotoSelecionado != null && Piloto.AGRESSIVO
				.equals(pilotoSelecionado.getModoPilotagem())) {
			g2d.setColor(transpSel);
			g2d.fill(f7_C);
			Stroke stroke = g2d.getStroke();
			g2d.setStroke(trilhoMiniPista);
			g2d.setColor(red);
			g2d.draw(f7_C);
			g2d.setStroke(stroke);
		} else {
			g2d.setColor(transpMenus);
			g2d.fill(f7_C);
		}
		g2d.setColor(Color.black);
		g2d.drawString(strAgressivo, x + 5, y + 16);
		desenhaControleKers(g2d, x, y, tamAgressivo);

		String strNormal = Lang.msg("076");
		int tamNormal = Util.calculaLarguraText(strNormal, g2d);
		x -= (tamNormal + 15);
		f6_X.setFrame(x, y, tamNormal + 10, 20);
		if (pilotoSelecionado != null
				&& Piloto.NORMAL.equals(pilotoSelecionado.getModoPilotagem())) {
			g2d.setColor(transpSel);
			g2d.fill(f6_X);
			Stroke stroke = g2d.getStroke();
			g2d.setStroke(trilhoMiniPista);
			g2d.setColor(yel);
			g2d.draw(f6_X);
			g2d.setStroke(stroke);
		} else {
			g2d.setColor(transpMenus);
			g2d.fill(f6_X);
		}

		g2d.setColor(Color.black);
		g2d.drawString(strNormal, x + 5, y + 16);

		String strConservador = Lang.msg("075");
		int tamConservador = Util.calculaLarguraText(strConservador, g2d);
		x -= (tamConservador + 15);

		f5_Z.setFrame(x, y, tamConservador + 10, 20);
		if (pilotoSelecionado != null
				&& Piloto.LENTO.equals(pilotoSelecionado.getModoPilotagem())) {
			g2d.setColor(transpSel);
			g2d.fill(f5_Z);
			Stroke stroke = g2d.getStroke();
			g2d.setStroke(trilhoMiniPista);
			g2d.setColor(gre);
			g2d.draw(f5_Z);
			g2d.setStroke(stroke);
		} else {
			g2d.setColor(transpMenus);
			g2d.fill(f5_Z);
		}

		g2d.setColor(Color.black);
		g2d.drawString(strConservador, x + 5, y + 16);

	}

	private void desenhaControleKers(Graphics2D g2d, int x, int y, int tamF5) {
		if (!controleJogo.isKers()) {
			return;
		}

		String msgKers = "\u2193 : " + Lang.msg("kers");

		int tamKers = Util.calculaLarguraText(msgKers, g2d);

		int xkers = x - (tamKers - tamF5);

		kers.setFrame(xkers, y - 25, tamKers + 10, 20);
		if (pilotoSelecionado != null
				&& pilotoSelecionado.getCarro().getCargaErs() > 0
				&& pilotoSelecionado.isAtivarErs()
				&& pilotoSelecionado.getCarro().getCargaErs() > 0) {
			g2d.setColor(transpSel);
			g2d.fill(kers);
			Stroke stroke = g2d.getStroke();
			g2d.setStroke(trilhoMiniPista);
			g2d.setColor(gre);
			g2d.draw(kers);
			g2d.setStroke(stroke);
		} else {
			g2d.setColor(transpMenus);
			g2d.fill(kers);
		}
		g2d.setColor(Color.black);
		g2d.drawString(msgKers, xkers + 5, y - 10);
	}

	private void desenhaControlesGiro(Graphics2D g2d) {

		int x = limitesViewPort.x + 5;
		int y = limitesViewPort.y + limitesViewPort.height - 25;

		desenhaControleDrs(g2d, x, y);

		String strF1 = Lang.msg("071");
		int tamF1 = Util.calculaLarguraText(strF1, g2d);
		f1_A.setFrame(x, y, tamF1 + 10, 20);

		if (pilotoSelecionado != null && Carro.GIRO_MIN_VAL == pilotoSelecionado
				.getCarro().getGiro()) {
			g2d.setColor(transpSel);
			g2d.fill(f1_A);
			Stroke stroke = g2d.getStroke();
			g2d.setStroke(trilhoMiniPista);
			g2d.setColor(gre);
			g2d.draw(f1_A);
			g2d.setStroke(stroke);
		} else {
			g2d.setColor(transpMenus);
			g2d.fill(f1_A);
		}

		g2d.setColor(Color.black);
		g2d.drawString(strF1, x + 5, y + 16);

		x += (tamF1 + 15);

		String strF2 = Lang.msg("072");
		int tamF2 = Util.calculaLarguraText(strF2, g2d);
		f2_S.setFrame(x, y, tamF2 + 10, 20);
		if (pilotoSelecionado != null && Carro.GIRO_NOR_VAL == pilotoSelecionado
				.getCarro().getGiro()) {
			g2d.setColor(transpSel);
			g2d.fill(f2_S);
			Stroke stroke = g2d.getStroke();
			g2d.setStroke(trilhoMiniPista);
			g2d.setColor(yel);
			g2d.draw(f2_S);
			g2d.setStroke(stroke);
		} else {
			g2d.setColor(transpMenus);
			g2d.fill(f2_S);
		}

		g2d.setColor(Color.black);
		g2d.drawString(strF2, x + 5, y + 16);

		x += (tamF2 + 15);

		String strF3 = Lang.msg("073");
		int tamF3 = Util.calculaLarguraText(strF3, g2d);
		f3_D.setFrame(x, y, tamF3 + 10, 20);
		if (pilotoSelecionado != null && Carro.GIRO_MAX_VAL == pilotoSelecionado
				.getCarro().getGiro()) {
			g2d.setColor(transpSel);
			g2d.fill(f3_D);
			Stroke stroke = g2d.getStroke();
			g2d.setStroke(trilhoMiniPista);
			g2d.setColor(red);
			g2d.draw(f3_D);
			g2d.setStroke(stroke);
		} else {
			g2d.setColor(transpMenus);
			g2d.fill(f3_D);
		}
		g2d.setColor(Color.black);
		g2d.drawString(strF3, x + 5, y + 16);
	}

	private void desenhaControleDrs(Graphics2D g2d, int x, int y) {
		if (!controleJogo.isDrs()) {
			return;
		}

		String msgDrs = "\u2191 : " + Lang.msg("drs");

		int tamDrs = Util.calculaLarguraText(msgDrs, g2d);
		drs.setFrame(x, y - 25, tamDrs + 10, 20);
		if (pilotoSelecionado != null && Carro.MENOS_ASA
				.equals(pilotoSelecionado.getCarro().getAsa())) {
			g2d.setColor(transpSel);
			g2d.fill(drs);
			Stroke stroke = g2d.getStroke();
			g2d.setStroke(trilhoMiniPista);
			g2d.setColor(gre);
			g2d.draw(drs);
			g2d.setStroke(stroke);
		} else {
			g2d.setColor(transpMenus);
			g2d.fill(drs);
		}
		g2d.setColor(Color.black);
		g2d.drawString(msgDrs, x + 5, y - 10);
	}

	private void desenhaKers(Graphics2D g2d) {
		if (!(controleJogo.isKers() && desenhaInfo)) {
			return;
		}
		if (pilotoSelecionado == null) {
			return;
		}
		if (isExibeResultadoFinal()) {
			return;
		}
		if (controleJogo.getNiveljogo() == InterfaceJogo.DIFICIL_NV
				&& !pilotoSelecionado.equals(controleJogo.getPilotoJogador())) {
			return;
		}
		int cargaKers = pilotoSelecionado.getCarro().getCargaErs() / 2;
		int y = 60;
		g2d.setColor(transpMenus);
		g2d.fillRoundRect(limitesViewPort.x + 175, limitesViewPort.y + y, 20,
				50, 0, 0);
		g2d.setColor(gre);
		g2d.fillRoundRect(limitesViewPort.x + 175,
				limitesViewPort.y + y + (50 - cargaKers), 20, cargaKers, 0, 0);

		if (pilotoSelecionado.getCargaKersVisual() != pilotoSelecionado
				.getCarro().getCargaErs()) {
			acionaDesenhaKers = 35;
		} else {
			g2d.setColor(Color.WHITE);
		}

		if (acionaDesenhaKers > 0) {
			acionaDesenhaKers--;
			g2d.setColor(OcilaCor.geraOcila("acionaDesenhaKers", Color.YELLOW));
			g2d.drawRoundRect(limitesViewPort.x + 175, limitesViewPort.y + y,
					20, 50, 0, 0);
		}

		pilotoSelecionado
				.setCargaErsVisual(pilotoSelecionado.getCarro().getCargaErs());
		g2d.drawString("+", limitesViewPort.x + 180,
				limitesViewPort.y + y + 10);
		g2d.drawString("-", limitesViewPort.x + 183,
				limitesViewPort.y + y + 45);

	}

	private void desenhaBackGround(Graphics2D g2d) {
		if (!desenhaBkg) {
			desenhaBackGroundComStrokes(g2d);
			return;
		}
		if (backGround == null) {
			carregaBackGround();
		}
		if (backGround == null) {
			desenhaBackGroundComStrokes(g2d);
		} else {
			carregaBackGroundZoom();
			if (desenhaImagens) {
				g2d.setColor(Color.BLACK);
			} else {
				g2d.setColor(Color.LIGHT_GRAY);
			}
			g2d.fillRect(0, 0, (int) limitesViewPortFull.getWidth(),
					(int) limitesViewPortFull.getHeight());
			BufferedImage subimage = null;
			BufferedImage drawBuffer = null;
			Rectangle rectangle = null;
			int diffX = 0;
			int diffY = 0;
			try {
				if (backGround != null) {
					BufferedImage bg = backGround;
					int largura = Util
							.inteiro(limitesViewPortFull.getWidth() / zoom);
					int altura = Util
							.inteiro(limitesViewPortFull.getHeight() / zoom);

					int x = descontoCentraliza.x;
					int y = descontoCentraliza.y;

					int bgWidth = bg.getWidth();
					int bgHeight = bg.getHeight();

					if (backGroundZoomPronto) {
						x *= zoom;
						y *= zoom;
						bgWidth = larguraBGZoom;
						bgHeight = alturaBGZoom;
						bg = backGroundZoom;
					}

					if (x <= 0) {
						diffX += (x * -1);
						x = 0;
					}
					if (y < 0) {
						diffY += (y * -1);
						y = 0;
					}

					int maxLarg = (x + largura);
					int maxAlt = (y + altura);

					if (maxLarg >= bgWidth) {
						largura -= (maxLarg - bgWidth);
					}

					if (maxAlt >= bgHeight) {
						altura -= (maxAlt - bgHeight);
					}

					if ((x + largura) >= bgWidth) {
						x -= ((x + largura) - bgWidth);
					}
					if ((y + altura) >= bgHeight) {
						y -= (y + altura) - bgHeight;
					}

					if (x <= 0) {
						x = 0;
					}
					if (y < 0) {
						y = 0;
					}

					if (largura > bg.getWidth()) {
						largura = bg.getWidth();
					}

					if (altura > bg.getHeight()) {
						altura = bg.getHeight();
					}

					rectangle = new Rectangle(x, y, largura, altura);

					subimage = bg.getSubimage(rectangle.x, rectangle.y,
							rectangle.width, rectangle.height);

				}
			} catch (Exception e) {
				Logger.logarExept(e);
				subimage = backGround;
			}

			if (zoom == 1 || backGroundZoomPronto) {
				drawBuffer = subimage;
			} else {
				if (drawBuffer == null
						|| drawBuffer.getWidth() != limitesViewPortFull
								.getWidth()
						|| drawBuffer.getHeight() != limitesViewPortFull
								.getHeight()) {
					drawBuffer = new BufferedImage(
							(int) (limitesViewPortFull.getWidth()),
							(int) (limitesViewPortFull.getHeight()),
							backGround.getType());
				}
				if (subimage != null) {
					if (affineTransformBG == null
							|| affineTransformBG.getScaleX() != zoom) {
						affineTransformBG = AffineTransform
								.getScaleInstance(zoom, zoom);
						affineTransformOpBG = new AffineTransformOp(
								affineTransformBG, interpolacao);
					}
					affineTransformOpBG.filter(subimage, drawBuffer);
				}
			}

			if (drawBuffer != null && desenhaImagens) {
				drawBuffer.setAccelerationPriority(1);
				int newX = Util
						.inteiro(limitesViewPortFull.getX() + (diffX * zoom));
				int newY = Util
						.inteiro(limitesViewPortFull.getY() + (diffY * zoom));
				if (backGroundZoomPronto) {
					newX = Util.inteiro(limitesViewPortFull.getX() + (diffX));
					newY = Util.inteiro(limitesViewPortFull.getY() + (diffY));
				}

				g2d.drawImage(drawBuffer, newX, newY, null);
			}
		}
	}

	private void carregaBackGroundZoom() {
		if (threadCarregarBkgZoomRodando > 1) {
			threadCarregarBkgZoom.interrupt();
		}
		if (backGround == null) {
			return;
		}
		if (zoom == mouseZoom) {
			return;
		}
		if (carreganadoBackGroundZoom()) {
			return;
		}
		threadCarregarBkgZoom = new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					threadCarregarBkgZoomRodando++;
					Thread.sleep(10000);
					if (zoom != mouseZoom || zoom == 1) {
						backGroundZoomPronto = false;
						return;
					}
					larguraBGZoom = Util.inteiro(backGround.getWidth() * zoom);
					alturaBGZoom = Util.inteiro(backGround.getHeight() * zoom);

					AffineTransform scaleInstance = AffineTransform
							.getScaleInstance(zoom, zoom);
					AffineTransformOp affineTransformOp = new AffineTransformOp(
							scaleInstance, interpolacao);

					if (backGroundZoom == null) {
						backGroundZoom = new BufferedImage(
								backGround.getWidth(), backGround.getHeight(),
								backGround.getType());

					}
					affineTransformOp.filter(backGround, backGroundZoom);
					backGroundZoomPronto = true;
				} catch (InterruptedException e) {
					Logger.logarExept(e);
				} finally {
					threadCarregarBkgZoomRodando--;
				}

			}
		});
		threadCarregarBkgZoom.setPriority(Thread.MIN_PRIORITY);
		if (threadCarregarBkgZoomRodando == 0) {
			threadCarregarBkgZoom.start();
		}
	}

	private boolean carreganadoBackGroundZoom() {
		if (threadCarregarBkgZoom != null && threadCarregarBkgZoom.isAlive()) {
			return true;
		}
		return false;
	}

	private void desenhaBackGroundComStrokes(Graphics2D g2d) {
		if (!desenhaPista) {
			return;
		}
		g2d.setColor(Color.white);
		g2d.fillRect(0, 0, (int) limitesViewPortFull.getWidth(),
				(int) limitesViewPortFull.getHeight());
		int larguraPistaPixeisLoc = Util
				.inteiro(100 * circuito.getMultiplicadorLarguraPista() * zoom);
		if (larguraPistaPixeisLoc != larguraPistaPixeis) {
			larguraPistaPixeis = larguraPistaPixeisLoc;
			pista = new BasicStroke(larguraPistaPixeis, BasicStroke.CAP_ROUND,
					BasicStroke.JOIN_ROUND);
			pistaTinta = new BasicStroke(Util.inteiro(larguraPistaPixeis),
					BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
			box = new BasicStroke(Util.inteiro(larguraPistaPixeis * .4),
					BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
			zebra = new BasicStroke(Util.inteiro(larguraPistaPixeis * 1.05),
					BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10f,
					new float[]{10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10},
					0);
			gerarBoxes();
		}
		desenhaTintaPistaZebra(g2d);
		desenhaPista(g2d);
		desenhaPistaBox(g2d);
		desenhaBoxes(g2d);
	}

	private void desenhaMiniPista(Graphics2D g2d) {
		if (!desenhaInfo) {
			return;
		}
		if (limitesViewPort == null) {
			return;
		}
		if (isExibeResultadoFinal()) {
			return;
		}
		g2d.setColor(Color.LIGHT_GRAY);
		int x = limitesViewPort.x + 5;
		int y = limitesViewPort.y + 280;
		if (pistaMinimizada == null) {
			maiorP = new Point(0, 0);
			pistaMinimizada = new ArrayList();
			multiminiPista = 0;
			List pista = circuito.getPista();
			for (Iterator iterator = pista.iterator(); iterator.hasNext();) {
				No no = (No) iterator.next();
				Point p = new Point(no.getX(), no.getY());
				if (p.x > maiorP.x) {
					maiorP.x = p.x;
				}
				if (p.y > maiorP.y) {
					maiorP.x = p.y;
				}
			}
			multiminiPista = (GeoUtil.distaciaEntrePontos(x, y, maiorP.x,
					maiorP.y) / 100.0);
			if (multiminiPista < 20) {
				multiminiPista = 20;
			}
			for (Iterator iterator = pista.iterator(); iterator.hasNext();) {
				No no = (No) iterator.next();
				Point p = new Point(no.getX(), no.getY());
				p.x /= multiminiPista;
				p.y /= multiminiPista;
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
				p.x /= multiminiPista;
				p.y /= multiminiPista;
				if (!boxMinimizado.contains(p))
					boxMinimizado.add(p);
			}

		}

		Point oldP = null;
		Stroke stroke = g2d.getStroke();
		g2d.setStroke(trilhoMiniPista);
		for (Iterator iterator = pistaMinimizada.iterator(); iterator
				.hasNext();) {
			Point p = (Point) iterator.next();
			if (oldP != null) {
				g2d.drawLine(x + oldP.x, y + oldP.y, x + p.x, y + p.y);
			}
			oldP = p;
		}
		g2d.setStroke(stroke);
		Point p0 = (Point) pistaMinimizada.get(0);
		g2d.drawLine(x + oldP.x, y + oldP.y, x + p0.x, y + p0.y);

		oldP = null;

		g2d.setStroke(trilhoMiniPista);
		g2d.setColor(Color.gray);
		for (Iterator iterator = boxMinimizado.iterator(); iterator
				.hasNext();) {
			Point p = (Point) iterator.next();
			if (oldP != null) {
				g2d.drawLine(x + oldP.x, y + oldP.y, x + p.x, y + p.y);
			}
			oldP = p;
		}
		g2d.setStroke(stroke);
		Piloto lider = (Piloto) pilotosList.get(0);
		Font fontOri = g2d.getFont();
		g2d.setFont(new Font(fontOri.getName(), fontOri.getStyle(), 8));

		List pilotos = pilotosList;
		for (Iterator iterator = pilotos.iterator(); iterator.hasNext();) {
			Piloto piloto = (Piloto) iterator.next();
			Point point = piloto.getNoAtual().getPoint();
			int xL = point.x;
			int yL = point.y;

			xL /= multiminiPista;
			yL /= multiminiPista;

			g2d.setColor(Color.LIGHT_GRAY);

			if (piloto.equals(pilotoSelecionado)) {
				g2d.setColor(jogador);
			}

			if (piloto.equals(lider)) {
				g2d.setColor(gre);
			} else if (controleJogo.verirficaDesafiandoCampeonato(piloto)
					|| (piloto.isJogadorHumano()
							&& !piloto.equals(pilotoSelecionado))) {
				g2d.setColor(oran);
			}

			g2d.fillOval(x + xL - 5, y + yL - 5, 10, 10);
			g2d.setColor(Color.BLACK);
			g2d.drawString("" + piloto.getPosicao(),
					x + xL - ((piloto.getPosicao() < 10) ? 3 : 5), y + yL + 3);
		}
		g2d.setFont(fontOri);
		if (pilotoSelecionado != null && pilotoSelecionado.isJogadorHumano()
				&& posisRec != null) {
			g2d.setColor(red);
			g2d.fillOval(
					x + Util.inteiro(posisRec.getPoint().x / multiminiPista),
					y + Util.inteiro(posisRec.getPoint().y / multiminiPista),
					Util.inteiro(5 * zoom), Util.inteiro(5 * zoom));
		}
		if (controleJogo.isSafetyCarNaPista()) {
			SafetyCar safetyCar = controleJogo.getSafetyCar();
			Point point = safetyCar.getNoAtual().getPoint();
			int xS = point.x;
			int yS = point.y;
			xS /= multiminiPista;
			yS /= multiminiPista;
			g2d.setColor(lightRed);
			if (!controleJogo.isSafetyCarVaiBox()) {
				if (alternaPiscaSCSair) {
					g2d.setColor(yel);
				}
				alternaPiscaSCSair = !alternaPiscaSCSair;
			}
			g2d.fillOval(x + xS - 5, y + yS - 5, 10, 10);
			g2d.setColor(Color.BLACK);
			g2d.drawString("sc", (x + xS) - 6, y + yS + 3);
		}
	}

	private void desenhaMarcasPneuPista(TravadaRoda travadaRoda) {
		if (controleJogo == null) {
			return;
		}
		No noAtual = controleJogo.obterNoPorId(travadaRoda.getIdNo());
		if (noAtual == null) {
			return;
		}
		if (circuito == null) {
			return;
		}
		Point p = noAtual.getPoint();
		List<ObjetoPista> objetos = circuito.getObjetos();
		if (objetos == null) {
			return;
		}
		boolean travadaNaTransparencia = false;
		for (Iterator<ObjetoPista> iterator = objetos.iterator(); iterator
				.hasNext();) {
			ObjetoPista objetoPista = iterator.next();
			if (objetoPista instanceof ObjetoTransparencia) {
				ObjetoTransparencia objetoTransparencia = (ObjetoTransparencia) objetoPista;
				Rectangle rectangle = new Rectangle(Carro.LARGURA,
						Carro.LARGURA);
				rectangle.setLocation(p.x - Carro.LARGURA / 2,
						p.y - Carro.LARGURA / 2);
				if (objetoTransparencia.obterArea().intersects(rectangle)) {
					travadaNaTransparencia = true;
					break;
				}
			} else {
				continue;
			}
		}
		if (travadaNaTransparencia) {
			return;
		}
		int width = (int) (travadaRodaImg0.getWidth());
		int height = (int) (travadaRodaImg0.getHeight());
		List<No> lista = controleJogo.obterNosPista();

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
		double calculaAngulo = GeoUtil.calculaAngulo(frenteCar, trazCar, 0);
		if (rectangleMarcasPneuPista == null) {
			rectangleMarcasPneuPista = new Rectangle2D.Double(
					(p.x - Carro.MEIA_LARGURA), (p.y - Carro.MEIA_ALTURA),
					Carro.LARGURA, Carro.ALTURA);
		} else {
			rectangleMarcasPneuPista.setFrame((p.x - Carro.MEIA_LARGURA),
					(p.y - Carro.MEIA_ALTURA), Carro.LARGURA, Carro.ALTURA);
		}

		Point p1 = GeoUtil.calculaPonto(calculaAngulo,
				Util.inteiro(Carro.ALTURA * controleJogo.getCircuito()
						.getMultiplicadorLarguraPista()),
				new Point(Util.inteiro(rectangleMarcasPneuPista.getCenterX()),
						Util.inteiro(rectangleMarcasPneuPista.getCenterY())));
		Point p2 = GeoUtil.calculaPonto(calculaAngulo + 180,
				Util.inteiro(Carro.ALTURA * controleJogo.getCircuito()
						.getMultiplicadorLarguraPista()),
				new Point(Util.inteiro(rectangleMarcasPneuPista.getCenterX()),
						Util.inteiro(rectangleMarcasPneuPista.getCenterY())));
		if (travadaRoda.getTracado() == 0) {
			carx = p.x - w2;
			cary = p.y - h2;
		}
		if (travadaRoda.getTracado() == 1) {
			carx = Util.inteiro((p1.x - w2));
			cary = Util.inteiro((p1.y - h2));
		}
		if (travadaRoda.getTracado() == 2) {
			carx = Util.inteiro((p2.x - w2));
			cary = Util.inteiro((p2.y - h2));
		}

		double rad = Math.toRadians((double) calculaAngulo);
		AffineTransform afRotate = new AffineTransform();
		afRotate.setToRotation(rad, w2, h2);

		BufferedImage rotateBuffer = new BufferedImage(width, width,
				BufferedImage.TYPE_INT_ARGB);
		AffineTransformOp op = new AffineTransformOp(afRotate, interpolacao);
		switch (travadaRoda.getTipo()) {
			case 0 :
				op.filter(travadaRodaImg0, rotateBuffer);
				break;
			case 1 :
				op.filter(travadaRodaImg1, rotateBuffer);
				break;
			case 2 :
				op.filter(travadaRodaImg2, rotateBuffer);
				break;
			default :
				break;
		}
		BufferedImage travada = rotateBuffer;
		Graphics graphics = null;
		if (backGround != null) {
			graphics = backGround.getGraphics();
		} else {
			graphics = controleJogo.getMainFrame().obterGraficos();
		}

		graphics.drawImage(travada, Util.inteiro((carx)), Util.inteiro((cary)),
				null);
	}

	public double getMouseZoom() {
		return mouseZoom;
	}

	public void setMouseZoom(double mouseZoom) {
		if (this.mouseZoom != mouseZoom) {
			porcessaMudancaZoom();
		}
		this.mouseZoom = mouseZoom;
	}

	private void porcessaMudancaZoom() {
		backGroundZoomPronto = false;
	}

	public boolean isDesenhouQualificacao() {
		return desenhouQualificacao;
	}

	public void setDesenhouQualificacao(boolean desenhouQualificacao) {
		this.desenhouQualificacao = desenhouQualificacao;
	}

	private void desenhaCarros(Graphics2D g2d) {
		if (isExibeResultadoFinal()) {
			return;
		}
		for (int i = pilotosList.size() - 1; i > -1; i--) {
			Piloto piloto = pilotosList.get(i);
			No noAtual = piloto.getNoAtualSuave();
			if (noAtual == null) {
				noAtual = piloto.getNoAtual();
			}
			atualizacaoSuave(piloto);
			if (!limitesViewPort.contains(
					((noAtual.getX() - descontoCentraliza.x) * zoom),
					((noAtual.getY() - descontoCentraliza.y) * zoom))) {
				continue;
			}
			if (Logger.ativo && piloto.isJogadorHumano()
					&& piloto.getNoAtualSuave() != null) {
				noAtual = piloto.getNoAtual();
				g2d.setColor(OcilaCor.geraOcila("CompReal", Color.ORANGE));
				g2d.fillOval(
						(int) ((noAtual.getX() - descontoCentraliza.x) * zoom),
						(int) ((noAtual.getY() - descontoCentraliza.y) * zoom),
						10, 10);
			}
			centralizaCarroDesenhar(controleJogo, piloto);
			desenhaCarroCima(g2d, piloto);
			if (piloto.equals(pilotoSelecionado)
					|| piloto.getCarro().isPaneSeca()
					|| piloto.getCarro().isRecolhido()
					|| !limitesViewPort.contains(
							((noAtual.getX() - descontoCentraliza.x) * zoom),
							((noAtual.getY() - descontoCentraliza.y) * zoom))) {
				continue;
			}
			desenhaNomePilotoNaoSelecionado(piloto, g2d);
		}
		desenhaNomePilotoSelecionado(g2d, pilotoSelecionado);
	}

	private void desenhaBarraPilotos(Graphics2D g2d) {
		if (isExibeResultadoFinal()) {
			return;
		}
		int x = limitesViewPort.x + limitesViewPort.width - 165;
		int y = limitesViewPort.y + 5;
		int tamNome = 90;

		for (int i = pilotosList.size() - 1; i > -1; i--) {
			int inverter = pilotosList.size() - i - 1;
			desenhaBarraListaPiloto(g2d, x, y, tamNome, inverter,
					(Piloto) pilotosList.get(inverter));
			y += 23;
		}
	}

	public Rectangle2D centralizaCarroDesenhar(InterfaceJogo controleJogo,
			Piloto piloto) {
		if (controleJogo.isModoQualify()) {
			return null;
		}
		if (piloto.getNoAnterior() != null && piloto.getDiateira() != null
				&& piloto.getCentro() != null && piloto.getTrazeira() != null
				&& !piloto.emMovimento()) {
			return null;
		}
		No noAtual = piloto.getNoAtual();
		if (piloto.getNoAtualSuave() != null) {
			noAtual = piloto.getNoAtualSuave();
		}
		int cont = noAtual.getIndex();
		List lista = controleJogo.obterPista(noAtual);
		if (lista == null) {
			return null;
		}
		Point p = noAtual.getPoint();
		int carx = p.x;
		int cary = p.y;
		int traz = cont - Piloto.MEIAENVERGADURA;
		int frente = cont + Piloto.MEIAENVERGADURA;
		if (traz < 0) {
			if (controleJogo.getNosDoBox().size() == lista.size()) {
				traz = 0;
			} else {
				traz = (lista.size() - 1) + traz;
			}
		}
		if (frente > (lista.size() - 1)) {
			if (controleJogo.getNosDoBox().size() == lista.size()) {
				frente = lista.size() - 1;
			} else {
				frente = (frente - (lista.size() - 1)) - 1;
			}
		}

		Point trazCar = ((No) lista.get(traz)).getPoint();
		trazCar = new Point(trazCar.x, trazCar.y);
		Point frenteCar = ((No) lista.get(frente)).getPoint();
		frenteCar = new Point(frenteCar.x, frenteCar.y);
		double calculaAngulo = GeoUtil.calculaAngulo(frenteCar, trazCar, 0);
		piloto.setAngulo(calculaAngulo);
		Rectangle2D rectangle = new Rectangle2D.Double(
				(p.x - Carro.MEIA_LARGURA_CIMA), (p.y - Carro.MEIA_ALTURA_CIMA),
				Carro.LARGURA_CIMA, Carro.ALTURA_CIMA);
		Point p1 = GeoUtil.calculaPonto(calculaAngulo,
				Util.inteiro(Carro.ALTURA * controleJogo.getCircuito()
						.getMultiplicadorLarguraPista()),
				new Point(Util.inteiro(rectangle.getCenterX()),
						Util.inteiro(rectangle.getCenterY())));
		Point p2 = GeoUtil.calculaPonto(calculaAngulo + 180,
				Util.inteiro(Carro.ALTURA * controleJogo.getCircuito()
						.getMultiplicadorLarguraPista()),
				new Point(Util.inteiro(rectangle.getCenterX()),
						Util.inteiro(rectangle.getCenterY())));
		Point p5 = GeoUtil.calculaPonto(calculaAngulo,
				Util.inteiro(Carro.ALTURA * 3
						* controleJogo.getCircuito()
								.getMultiplicadorLarguraPista()),
				new Point(Util.inteiro(rectangle.getCenterX()),
						Util.inteiro(rectangle.getCenterY())));
		Point p4 = GeoUtil.calculaPonto(calculaAngulo + 180,
				Util.inteiro(Carro.ALTURA * 3
						* controleJogo.getCircuito()
								.getMultiplicadorLarguraPista()),
				new Point(Util.inteiro(rectangle.getCenterX()),
						Util.inteiro(rectangle.getCenterY())));

		piloto.setP1(p1);
		piloto.setP2(p2);
		piloto.setP5(p5);
		piloto.setP4(p4);
		if (piloto.getTracado() == 0) {
			carx = p.x;
			cary = p.y;
			int indTracado = piloto.getIndiceTracado();
			if (indTracado > 0 && piloto.getTracadoAntigo() != 0) {
				List drawBresenhamLine = null;
				if (piloto.getTracadoAntigo() == 1) {
					drawBresenhamLine = GeoUtil.drawBresenhamLine(p1.x, p1.y,
							p.x, p.y);
				}
				if (piloto.getTracadoAntigo() == 2) {
					drawBresenhamLine = GeoUtil.drawBresenhamLine(p2.x, p2.y,
							p.x, p.y);
				}
				if (piloto.getTracadoAntigo() == 5) {
					drawBresenhamLine = GeoUtil.drawBresenhamLine(p5.x, p5.y,
							p.x, p.y);
				}
				if (piloto.getTracadoAntigo() == 4) {
					drawBresenhamLine = GeoUtil.drawBresenhamLine(p4.x, p4.y,
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
				carx = pReta.x;
				cary = pReta.y;
			}
		}
		if (piloto.getTracado() == 1) {
			carx = Util.inteiro((p1.x));
			cary = Util.inteiro((p1.y));
			int indTracado = piloto.getIndiceTracado();
			if (indTracado > 0 && piloto.getTracadoAntigo() != 1) {
				List drawBresenhamLine = null;
				if (piloto.getTracadoAntigo() == 0) {
					drawBresenhamLine = GeoUtil.drawBresenhamLine(p.x, p.y,
							p1.x, p1.y);
				}
				if (piloto.getTracadoAntigo() == 2) {
					drawBresenhamLine = GeoUtil.drawBresenhamLine(p2.x, p2.y,
							p1.x, p1.y);
				}
				if (piloto.getTracadoAntigo() == 4) {
					drawBresenhamLine = GeoUtil.drawBresenhamLine(p4.x, p4.y,
							p1.x, p1.y);
				}
				if (piloto.getTracadoAntigo() == 5) {
					drawBresenhamLine = GeoUtil.drawBresenhamLine(p5.x, p5.y,
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
				carx = pReta.x;
				cary = pReta.y;
			}
		}

		if (piloto.getTracado() == 5) {
			carx = Util.inteiro((p5.x));
			cary = Util.inteiro((p5.y));
			int indTracado = piloto.getIndiceTracado();
			if (indTracado > 0 && piloto.getTracadoAntigo() != 5) {
				List drawBresenhamLine = null;
				if (piloto.getTracadoAntigo() == 0) {
					drawBresenhamLine = GeoUtil.drawBresenhamLine(p.x, p.y,
							p5.x, p5.y);
				}
				if (piloto.getTracadoAntigo() == 1) {
					drawBresenhamLine = GeoUtil.drawBresenhamLine(p1.x, p1.y,
							p5.x, p5.y);
				}
				if (piloto.getTracadoAntigo() == 2) {
					drawBresenhamLine = GeoUtil.drawBresenhamLine(p2.x, p2.y,
							p5.x, p5.y);
				}
				int indice = drawBresenhamLine.size() - indTracado;
				if (indice <= 0) {
					indice = 0;
				}
				if (indice >= drawBresenhamLine.size()) {
					indice = drawBresenhamLine.size() - 1;
				}

				Point pReta = (Point) drawBresenhamLine.get(indice);
				carx = pReta.x;
				cary = pReta.y;
			}
		}

		if (piloto.getTracado() == 2) {
			carx = Util.inteiro((p2.x));
			cary = Util.inteiro((p2.y));
			int indTracado = piloto.getIndiceTracado();
			if (indTracado > 0 && piloto.getTracadoAntigo() != 2) {
				List drawBresenhamLine = null;
				if (piloto.getTracadoAntigo() == 0) {
					drawBresenhamLine = GeoUtil.drawBresenhamLine(p.x, p.y,
							p2.x, p2.y);
				}
				if (piloto.getTracadoAntigo() == 1) {
					drawBresenhamLine = GeoUtil.drawBresenhamLine(p1.x, p1.y,
							p2.x, p2.y);
				}

				if (piloto.getTracadoAntigo() == 4) {
					drawBresenhamLine = GeoUtil.drawBresenhamLine(p4.x, p4.y,
							p2.x, p2.y);
				}
				if (piloto.getTracadoAntigo() == 5) {
					drawBresenhamLine = GeoUtil.drawBresenhamLine(p5.x, p5.y,
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
				carx = pReta.x;
				cary = pReta.y;
			}
		}

		if (piloto.getTracado() == 4) {
			carx = Util.inteiro((p4.x));
			cary = Util.inteiro((p4.y));
			int indTracado = piloto.getIndiceTracado();
			if (indTracado > 0 && piloto.getTracadoAntigo() != 4) {
				List drawBresenhamLine = null;
				if (piloto.getTracadoAntigo() == 0) {
					drawBresenhamLine = GeoUtil.drawBresenhamLine(p.x, p.y,
							p4.x, p4.y);
				}
				if (piloto.getTracadoAntigo() == 1) {
					drawBresenhamLine = GeoUtil.drawBresenhamLine(p1.x, p1.y,
							p4.x, p4.y);
				}
				if (piloto.getTracadoAntigo() == 2) {
					drawBresenhamLine = GeoUtil.drawBresenhamLine(p2.x, p2.y,
							p4.x, p4.y);
				}
				int indice = drawBresenhamLine.size() - indTracado;
				if (indice <= 0) {
					indice = 0;
				}
				if (indice >= drawBresenhamLine.size()) {
					indice = drawBresenhamLine.size() - 1;
				}

				Point pReta = (Point) drawBresenhamLine.get(indice);
				carx = pReta.x;
				cary = pReta.y;
			}
		}

		piloto.setCarX(carx);
		piloto.setCarY(cary);

		rectangle = new Rectangle2D.Double(
				(carx - Carro.MEIA_ALTURA * Carro.FATOR_AREA_CARRO),
				(cary - Carro.MEIA_ALTURA * Carro.FATOR_AREA_CARRO),
				Carro.ALTURA * Carro.FATOR_AREA_CARRO,
				Carro.ALTURA * Carro.FATOR_AREA_CARRO);

		piloto.setCentro(rectangle.getBounds());

		trazCar = GeoUtil.calculaPonto(calculaAngulo + 90,
				Util.inteiro(Piloto.MEIAENVERGADURA), new Point(carx, cary));

		Rectangle2D trazRec = new Rectangle2D.Double(
				(trazCar.x - Carro.MEIA_ALTURA * Carro.FATOR_AREA_CARRO),
				(trazCar.y - Carro.MEIA_ALTURA * Carro.FATOR_AREA_CARRO),
				Carro.ALTURA * Carro.FATOR_AREA_CARRO,
				Carro.ALTURA * Carro.FATOR_AREA_CARRO);
		piloto.setTrazeira(trazRec.getBounds());

		frenteCar = GeoUtil.calculaPonto(calculaAngulo + 270,
				Util.inteiro(Piloto.MEIAENVERGADURA), new Point(carx, cary));

		Rectangle2D frenteRec = new Rectangle2D.Double(
				(frenteCar.x - Carro.MEIA_ALTURA * Carro.FATOR_AREA_CARRO),
				(frenteCar.y - Carro.MEIA_ALTURA * Carro.FATOR_AREA_CARRO),
				Carro.ALTURA * Carro.FATOR_AREA_CARRO,
				Carro.ALTURA * Carro.FATOR_AREA_CARRO);
		piloto.setDiateira(frenteRec.getBounds());

		return rectangle;
	}

	private void desenhaNomePilotoSelecionado(Graphics2D g2d, Piloto piloto) {
		if (pilotoSelecionado == null) {
			return;
		}
		int x = Util.inteiro(
				((piloto.getCarX() - 2) - descontoCentraliza.x) * zoom);
		int y = Util.inteiro(
				((piloto.getCarY() - 2) - descontoCentraliza.y) * zoom);

		g2d.setColor(piloto.getCarro().getCor1());
		marcaCorPilotoJogador(g2d, piloto);
		g2d.fillOval(x, y, 8, 8);
		g2d.setColor(new Color(piloto.getCarro().getCor2().getRed(),
				piloto.getCarro().getCor2().getGreen(),
				piloto.getCarro().getCor2().getBlue(), 175));
		marcaCorPilotoJogador(g2d, piloto);
		Stroke stroke = g2d.getStroke();
		g2d.setStroke(trilho);

		int x2 = Util
				.inteiro((piloto.getCarX() - 3 - descontoCentraliza.x) * zoom);
		int y2 = Util
				.inteiro((piloto.getCarY() - 3 - descontoCentraliza.y) * zoom);
		g2d.drawOval(x2, y2, 8, 8);
		g2d.setStroke(stroke);
		desenhaNomePilotoSelecionadoCarroCima(piloto, g2d, x, y);
	}

	private void marcaCorPilotoJogador(Graphics2D g2d, Piloto piloto) {
		if (piloto.equals(controleJogo.getPilotoJogador())
				&& qtdeLuzesAcesas != 0) {
			g2d.setColor(OcilaCor.geraOcila("mrkSel", yel));
		}

	}

	public Shape limitesViewPort() {
		int x = 10;
		int y = 35;

		MainFrame mainFrame = controleJogo.getMainFrame();
		Rectangle rectangle = new Rectangle(x, y,
				(int) (mainFrame.getWidth() - 20),
				(int) (mainFrame.getHeight() - 40));
		return rectangle;
	}

	public Shape limitesViewPortFull() {
		int x = 0;
		int y = 0;

		MainFrame mainFrame = controleJogo.getMainFrame();
		Rectangle rectangle = new Rectangle(x, y, (int) (mainFrame.getWidth()),
				(int) (mainFrame.getHeight()));
		return rectangle;
	}

	private void desenhaCarroCima(Graphics2D g2d, Piloto piloto) {
		if (zoom < 0.5) {
			return;
		}
		if (piloto == null) {
			return;
		}
		if (piloto.getCarro() == null) {
			return;
		}
		if (piloto.getAngulo() == null) {
			return;
		}
		if (descontoCentraliza == null) {
			return;
		}
		if (controleJogo == null) {
			return;
		}
		if (gerenciadorVisual.getFps() < 20) {
			boolean desenha = false;
			if (pilotoSelecionado.equals(piloto)) {
				desenha = true;
			}
			if (pilotoSelecionado.getCarroPilotoFrente() != null
					&& piloto.equals(pilotoSelecionado.getCarroPilotoFrente()
							.getPiloto())) {
				desenha = true;
			}
			if (pilotoSelecionado.getCarroPilotoAtras() != null
					&& piloto.equals(pilotoSelecionado.getCarroPilotoAtras()
							.getPiloto())) {
				desenha = true;
			}
			if (!desenha) {
				return;
			}
		}
		String danificado = piloto.getCarro().getDanificado();
		if (Carro.PANE_SECA.equals(danificado)
				|| Carro.EXPLODIU_MOTOR.equals(danificado)) {
			return;
		}
		if (piloto.getCarro().isRecolhido()) {
			return;
		}
		No noAtual = piloto.getNoAtual();
		if (piloto.getNoAtualSuave() != null) {
			noAtual = piloto.getNoAtualSuave();
		}
		if (noAtual == null) {
			Logger.logar("desenhaCarroCima noAtual == null");
			return;
		}
		BufferedImage carroCima = controleJogo.obterCarroCima(piloto);
		if (carroCima == null) {
			return;
		}
		Point p = noAtual.getPoint();
		g2d.setColor(Color.black);
		Stroke stroke = g2d.getStroke();
		g2d.setStroke(trilho);
		Double calculaAngulo = piloto.getAngulo();
		int carX = (piloto.getCarX() - Carro.MEIA_LARGURA_CIMA);
		int carY = (piloto.getCarY() - Carro.MEIA_LARGURA_CIMA);
		calculaAngulo = processaOcilacaoAngulo(piloto, calculaAngulo, noAtual);

		int width = Carro.LARGURA_CIMA;
		int height = Carro.ALTURA_CIMA;
		int w2 = Carro.MEIA_LARGURA_CIMA;
		int h2 = Carro.MEIA_LARGURA_CIMA;

		double rad = Math.toRadians((double) calculaAngulo);

		int imagemCarroX = Util.inteiro((carX - descontoCentraliza.x) * zoom);
		int imagemCarroY = Util.inteiro((carY - descontoCentraliza.y) * zoom);

		AffineTransform afZoom = new AffineTransform();
		AffineTransform afRotate = new AffineTransform();
		afZoom.setToScale(zoom, zoom);
		afRotate.setToRotation(rad, w2, h2);

		BufferedImage rotateBuffer = new BufferedImage(width, height,
				BufferedImage.TYPE_INT_ARGB);

		AffineTransformOp opRotate = new AffineTransformOp(afRotate,
				interpolacao);
		AffineTransformOp opZoom = new AffineTransformOp(afZoom, interpolacao);
		BufferedImage zoomBuffer = new BufferedImage(width, height,
				BufferedImage.TYPE_INT_ARGB);
		opRotate.filter(carroCima, zoomBuffer);
		opZoom.filter(zoomBuffer, rotateBuffer);
		desenhaSetasCarroCima(g2d, piloto, width, height, imagemCarroX,
				imagemCarroY, opRotate, opZoom);
		boolean naoDesenhaEfeitos = false;
		boolean temTransparencia = false;
		int indexNoAtual = noAtual.getIndex();
		if (circuito.getObjetos() != null) {
			for (ObjetoPista objetoPista : circuito.getObjetos()) {
				if (!(objetoPista instanceof ObjetoTransparencia))
					continue;
				ObjetoTransparencia objetoTransparencia = (ObjetoTransparencia) objetoPista;
				if (objetoPista.isTransparenciaBox() && controleJogo.obterPista(
						piloto.getNoAtual()) != controleJogo.getNosDoBox()) {
					continue;
				}
				if (objetoPista.getInicioTransparencia() != 0
						&& objetoPista.getFimTransparencia() != 0) {
					int inicio = objetoPista.getInicioTransparencia();
					int fim = objetoPista.getFimTransparencia();
					if (indexNoAtual < inicio || indexNoAtual > fim) {
						continue;
					}
				}
				Rectangle obterArea = objetoTransparencia.obterArea();
				if (!limitesViewPort.contains(
						((objetoTransparencia.getPosicaoQuina().x
								- descontoCentraliza.x) * zoom),
						((objetoTransparencia.getPosicaoQuina().y
								- descontoCentraliza.y) * zoom))
						&& !limitesViewPort.contains(
								(((objetoTransparencia.getPosicaoQuina().x
										+ objetoTransparencia.getLargura())
										- descontoCentraliza.x) * zoom),
								((objetoTransparencia.getPosicaoQuina().y
										- descontoCentraliza.y) * zoom))
						&& !limitesViewPort.contains(
								((objetoTransparencia.getPosicaoQuina().x
										- descontoCentraliza.x) * zoom),
								(((objetoTransparencia.getPosicaoQuina().y
										+ objetoTransparencia.getAltura())
										- descontoCentraliza.y) * zoom))
						&& !limitesViewPort.contains(
								(((objetoTransparencia.getPosicaoQuina().x
										+ objetoTransparencia.getLargura())
										- descontoCentraliza.x) * zoom),
								(((objetoTransparencia.getPosicaoQuina().y
										+ objetoTransparencia.getAltura())
										- descontoCentraliza.y) * zoom))) {
					continue;
				}
				Graphics2D gImage = rotateBuffer.createGraphics();
				objetoTransparencia.desenhaCarro(gImage, zoom, carX, carY);
				if (obterArea.contains(p)) {
					piloto.setNaoDesenhaEfeitos(
							piloto.getNaoDesenhaEfeitos() + 1);
					if (piloto.getNaoDesenhaEfeitos() > 10) {
						naoDesenhaEfeitos = true;
					}
					temTransparencia = true;
				}
			}
			if (!temTransparencia) {
				piloto.setNaoDesenhaEfeitos(0);
			}
		}
		if (desenhaImagens)
			g2d.drawImage(rotateBuffer, imagemCarroX, imagemCarroY, null);
		if (naoDesenhaEfeitos) {
			g2d.setStroke(stroke);
			return;
		}
		if (!temTransparencia) {
			desenhaFumacaTravaRodaCarroCima(g2d, piloto, width, height, carX,
					carY, afZoom, afRotate);
			desenhaAjudaPistaCarroCima(g2d, piloto);
		}
		desenhaChuvaFaiscasCarroCima(g2d, piloto, width);
		desenhaDebugCarroCima(g2d, piloto, rad);
		g2d.setStroke(stroke);
	}

	private void desenhaSetasCarroCima(Graphics2D g2d, Piloto piloto, int width,
			int height, int imagemCarroX, int imagemCarroY,
			AffineTransformOp opRotate, AffineTransformOp opZoom) {
		if (piloto.isJogadorHumano() && piloto.getSetaCima() != 0) {
			if (piloto.getSetaCima() % 2 == 0) {
				BufferedImage rotateBufferSetaCima = new BufferedImage(width,
						height, BufferedImage.TYPE_INT_ARGB);
				BufferedImage zoomBufferSetaCima = new BufferedImage(width,
						height, BufferedImage.TYPE_INT_ARGB);
				opRotate.filter(setaCarroCima, zoomBufferSetaCima);
				opZoom.filter(zoomBufferSetaCima, rotateBufferSetaCima);
				if (desenhaImagens)
					g2d.drawImage(rotateBufferSetaCima, imagemCarroX,
							imagemCarroY, null);
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
				if (desenhaImagens)
					g2d.drawImage(rotateBufferSetaBaixo, imagemCarroX,
							imagemCarroY, null);
			}
			piloto.setSetaBaixo(piloto.getSetaBaixo() - 1);
		}
	}

	private void descontoCentraliza() {
		int x = 0;
		int y = 0;

		if (pontoCentralizado != null) {
			MainFrame mainFrame = controleJogo.getMainFrame();
			x = (int) (pontoCentralizado.x
					- ((mainFrame.getWidth() / 2) / zoom));
			y = (int) (pontoCentralizado.y
					- ((mainFrame.getHeight() / 2) / zoom));
		}
		if (descontoCentraliza == null) {
			descontoCentraliza = new Point(x, y);
		} else {
			descontoCentraliza.x = x;
			descontoCentraliza.y = y;
		}
	}

	private Double processaOcilacaoAngulo(Piloto piloto, Double calculaAngulo,
			No noAtual) {
		if (controleJogo.isJogoPausado() || piloto.isDesqualificado()) {
			return calculaAngulo;
		}

		double variacao1 = 5.0;
		double variacao2 = 10.0;
		if (Clima.NUBLADO.equals(controleJogo.getClima())) {
			variacao1 = 10.0;
			variacao2 = 15.0;
		}
		if (controleJogo.isChovendo()) {
			variacao1 = 15.0;
			variacao2 = 20.0;
		}

		boolean rabeadaAgressivo = piloto.isAgressivo()
				&& piloto.getCarro().getGiro() == Carro.GIRO_MAX_VAL
				&& (noAtual.verificaCurvaAlta() || noAtual.verificaCurvaBaixa())
				&& Math.random() > .9;
		boolean rabeadaPneuErrado = piloto.getCarro()
				.verificaPneusIncompativeisClima(controleJogo)
				&& Math.random() > .95;

		if (rabeadaAgressivo || rabeadaPneuErrado) {
			if (noAtual.verificaCurvaAlta())
				calculaAngulo += Util.intervalo(-variacao1, variacao1);
			if (noAtual.verificaCurvaBaixa())
				calculaAngulo += Util.intervalo(-variacao2, variacao2);
		}
		if ((piloto.getTracado() == 4 || piloto.getTracado() == 5)
				&& Math.random() > 0.9) {
			calculaAngulo += Util.intervalo(-20, 20);
		}
		return calculaAngulo;
	}

	private void desenhaFumacaTravaRodaCarroCima(Graphics2D g2d, Piloto piloto,
			int width, int height, int carx, int cary, AffineTransform afZoom,
			AffineTransform afRotate) {

		/**
		 * Travada Roda
		 */
		if (piloto.decContTravouRodas() && Math.random() > 0.7) {
			double distancia = piloto.getDistanciaDerrapada();
			Point pontoDerrapada = piloto.getPontoDerrapada();
			if (pontoDerrapada != null
					&& distancia < (2 * Carro.RAIO_DERRAPAGEM)) {
				int ladoDerrapa = controleJogo
						.obterLadoDerrapa(piloto.getPontoDerrapada());
				if (ladoDerrapa == 5) {
					if (Math.random() > 0.5) {
						desenhaFumacaTravarRodas(width, height, afRotate,
								afZoom, carx, cary, g2d, carroCimaFreiosD1);
					} else {
						desenhaFumacaTravarRodas(width, height, afRotate,
								afZoom, carx, cary, g2d, carroCimaFreiosD2);
					}
					if (Math.random() > 0.5) {
						desenhaFumacaTravarRodas(width, height, afRotate,
								afZoom, carx, cary, g2d, carroCimaFreiosD3);
					} else {
						desenhaFumacaTravarRodas(width, height, afRotate,
								afZoom, carx, cary, g2d, carroCimaFreiosD4);
					}
					if (Math.random() > 0.5) {
						desenhaFumacaTravarRodas(width, height, afRotate,
								afZoom, carx, cary, g2d, carroCimaFreiosD5);
					} else {
						desenhaFumacaTravarRodas(width, height, afRotate,
								afZoom, carx, cary, g2d, carroCimaFreiosD1);
					}

				}
				if (ladoDerrapa == 4) {
					if (Math.random() > 0.5) {
						desenhaFumacaTravarRodas(width, height, afRotate,
								afZoom, carx, cary, g2d, carroCimaFreiosE1);
					} else {
						desenhaFumacaTravarRodas(width, height, afRotate,
								afZoom, carx, cary, g2d, carroCimaFreiosE2);
					}
					if (Math.random() > 0.5) {
						desenhaFumacaTravarRodas(width, height, afRotate,
								afZoom, carx, cary, g2d, carroCimaFreiosE3);
					} else {
						desenhaFumacaTravarRodas(width, height, afRotate,
								afZoom, carx, cary, g2d, carroCimaFreiosE4);
					}
					if (Math.random() > 0.5) {
						desenhaFumacaTravarRodas(width, height, afRotate,
								afZoom, carx, cary, g2d, carroCimaFreiosE5);
					} else {
						desenhaFumacaTravarRodas(width, height, afRotate,
								afZoom, carx, cary, g2d, carroCimaFreiosE1);
					}
				}
			} else if (Math.random() > 0.7) {
				desenhaFumacaTravarRodasRandom(g2d, width, height, carx, cary,
						afZoom, afRotate);
			}
		}
	}

	private void desenhaFumacaTravarRodasRandom(Graphics2D g2d, int width,
			int height, int carx, int cary, AffineTransform afZoom,
			AffineTransform afRotate) {
		if (Math.random() > 0.5) {
			desenhaFumacaTravarRodas(width, height, afRotate, afZoom, carx,
					cary, g2d, carroCimaFreiosD1);
		} else {
			desenhaFumacaTravarRodas(width, height, afRotate, afZoom, carx,
					cary, g2d, carroCimaFreiosE1);
		}
		if (Math.random() > 0.5) {
			desenhaFumacaTravarRodas(width, height, afRotate, afZoom, carx,
					cary, g2d, carroCimaFreiosD2);
		} else {
			desenhaFumacaTravarRodas(width, height, afRotate, afZoom, carx,
					cary, g2d, carroCimaFreiosE2);
		}
		if (Math.random() > 0.5) {
			desenhaFumacaTravarRodas(width, height, afRotate, afZoom, carx,
					cary, g2d, carroCimaFreiosD3);
		} else {
			desenhaFumacaTravarRodas(width, height, afRotate, afZoom, carx,
					cary, g2d, carroCimaFreiosE3);
		}
		if (Math.random() > 0.5) {
			desenhaFumacaTravarRodas(width, height, afRotate, afZoom, carx,

					cary, g2d, carroCimaFreiosD4);
		} else {
			desenhaFumacaTravarRodas(width, height, afRotate, afZoom, carx,
					cary, g2d, carroCimaFreiosE4);
		}
		if (Math.random() > 0.5) {
			desenhaFumacaTravarRodas(width, height, afRotate, afZoom, carx,
					cary, g2d, carroCimaFreiosD5);
		} else {
			desenhaFumacaTravarRodas(width, height, afRotate, afZoom, carx,
					cary, g2d, carroCimaFreiosE5);
		}
	}

	private void desenhaChuvaFaiscasCarroCima(Graphics2D g2d, Piloto piloto,
			int width) {
		if (piloto.isDesqualificado()) {
			return;
		}
		/**
		 * Chuva e Faiscas
		 */
		if (piloto.getDiateira() == null || piloto.getCentro() == null
				|| piloto.getTrazeira() == null) {
			centralizaCarroDesenhar(controleJogo, piloto);
		}
		if (piloto.getDiateira() == null || piloto.getCentro() == null
				|| piloto.getTrazeira() == null) {
			return;
		}
		if (frenteP == null) {
			frenteP = new Point((int) (piloto.getDiateira().getCenterX()),
					(int) (piloto.getDiateira().getCenterY()));
		} else {
			frenteP.x = (int) (piloto.getDiateira().getCenterX());
			frenteP.y = (int) (piloto.getDiateira().getCenterY());
		}
		if (centroP == null) {
			centroP = new Point((int) (piloto.getCentro().getCenterX()),
					(int) (piloto.getCentro().getCenterY()));
		} else {
			centroP.x = (int) (piloto.getCentro().getCenterX());
			centroP.y = (int) (piloto.getCentro().getCenterY());

		}
		List centroDiantera = GeoUtil.drawBresenhamLine(centroP, frenteP);
		Point eixoDianteras = (Point) centroDiantera
				.get(centroDiantera.size() / 2);
		if (eixoDianteras == null) {
			eixoDianteras = frenteP;
		}
		double eixo = piloto.getDiateira().getWidth() / 2;
		desenhaChuvaCarroCima(g2d, piloto, width, eixoDianteras, eixo);
		desenhaFaiscasCarroCima(g2d, piloto, width, eixoDianteras, eixo);
	}

	private void desenhaChuvaCarroCima(Graphics2D g2d, Piloto piloto, int width,
			Point eixoDianteras, double eixo) {
		if (controleJogo.isJogoPausado()) {
			return;
		}
		double qtdeGotas = indiceNublado / 3000.0;
		if (controleJogo.isChovendo() && piloto.getVelocidade() != 0
				&& !piloto.isDesqualificado()) {
			g2d.setColor(lightWhiteRain);
			for (int i = 0; i < 30; i++) {
				if (i % (Math.random() > 0.5 ? 3 : 2) == 0) {
					continue;
				}
				if (Math.random() > qtdeGotas) {
					continue;
				}
				int eixoDiatero = (int) (eixo * 0.3);
				Point origem = new Point(
						(int) Util.intervalo(eixoDianteras.x - eixoDiatero,
								eixoDianteras.x + eixoDiatero),
						(int) Util.intervalo(eixoDianteras.y - eixoDiatero,
								eixoDianteras.y + eixoDiatero));

				Point dest = new Point(
						(int) Util.intervalo(
								piloto.getTrazeira().getX()
										- Util.intervalo(2.5, 6),
								(int) piloto.getTrazeira().getX()
										+ piloto.getTrazeira().getWidth()
										+ Util.intervalo(2.5, 6)),
						(int) Util.intervalo(
								piloto.getTrazeira().getY()
										- Util.intervalo(2.5, 6),
								piloto.getTrazeira().getY()
										+ piloto.getTrazeira().getHeight()
										+ Util.intervalo(2.5, 6)));
				double max = 6.0 * (piloto.getVelocidade() / 320.0);

				Point destN = GeoUtil.calculaPonto(
						GeoUtil.calculaAngulo(origem, dest, 90),
						(int) (Util.intervalo(width * .25, width * max)
								* qtdeGotas),
						origem);

				g2d.drawLine(
						Util.inteiro((origem.x - descontoCentraliza.x) * zoom),
						Util.inteiro((origem.y - descontoCentraliza.y) * zoom),
						Util.inteiro((destN.x - descontoCentraliza.x) * zoom),
						Util.inteiro((destN.y - descontoCentraliza.y) * zoom));
			}
		}
		g2d.setStroke(trilho);
	}

	private void desenhaFaiscasCarroCima(Graphics2D g2d, Piloto piloto,
			int width, Point eixoDianteras, double eixo) {
		if (controleJogo.isJogoPausado()) {
			return;
		}
		if (piloto.isFaiscas()) {
			mapaFaiscas.put(piloto, piloto);
			g2d.setColor(Color.YELLOW);
			g2d.setStroke(strokeFaisca);
			for (int i = 0; i < 15; i++) {
				Point origem = new Point(
						(int) Util.intervalo(eixoDianteras.x - eixo,
								eixoDianteras.x + eixo),
						(int) Util.intervalo(eixoDianteras.y - eixo,
								eixoDianteras.y + eixo));

				Point dest = new Point(
						(int) Util.intervalo(
								piloto.getTrazeira().getX()
										- Util.intervalo(2.5, 15),
								piloto.getTrazeira().getX()
										+ piloto.getTrazeira().getWidth()
										+ Util.intervalo(2.5, 15)),
						(int) Util.intervalo(
								piloto.getTrazeira().getY()
										- Util.intervalo(2.5, 15),
								piloto.getTrazeira().getY()
										+ piloto.getTrazeira().getHeight()
										+ Util.intervalo(2.5, 15)));
				Point destN = GeoUtil.calculaPonto(
						GeoUtil.calculaAngulo(origem, dest, 90),
						(int) Util.intervalo(width * .2, width), origem);
				Point2D.Double trazCarD = new Point2D.Double(
						piloto.getTrazeira().getCenterX(),
						piloto.getTrazeira().getCenterY());
				g2d.fillOval(
						Util.inteiro(
								(trazCarD.x - descontoCentraliza.x) * zoom),
						Util.inteiro(
								(trazCarD.y - descontoCentraliza.y) * zoom),
						Util.inteiro(5 * zoom), Util.inteiro(5 * zoom));
				g2d.drawLine(
						Util.inteiro((dest.x - descontoCentraliza.x) * zoom),
						Util.inteiro((dest.y - descontoCentraliza.y) * zoom),
						Util.inteiro((destN.x - descontoCentraliza.x) * zoom),
						Util.inteiro((destN.y - descontoCentraliza.y) * zoom));
			}
			g2d.setStroke(trilho);
		} else {
			mapaFaiscas.put(piloto, null);
		}
	}

	private void desenhaAjudaPistaCarroCima(Graphics2D g2d, Piloto piloto) {
		/**
		 * Desenha Ajuda Pista
		 */
		if (piloto.equals(pilotoSelecionado) && posisRec != null) {
			if (posisRec.verificaRetaOuLargada()) {
				g2d.setColor(new Color(100, 255, 100, 70));
			} else if (posisRec.verificaCurvaAlta()) {
				g2d.setColor(new Color(255, 255, 100, 70));
			} else if (posisRec.verificaCurvaBaixa()) {
				g2d.setColor(new Color(255, 100, 100, 70));
			} else {
				g2d.setColor(new Color(100, 100, 100, 70));
			}
			Point frenteCarD = posisRec.getPoint();
			g2d.fillOval(
					Util.inteiro(
							(frenteCarD.x - 5 - descontoCentraliza.x) * zoom),
					Util.inteiro(
							(frenteCarD.y - 5 - descontoCentraliza.y) * zoom),
					Util.inteiro(15 * zoom), Util.inteiro(15 * zoom));
		}
	}

	private void desenhaDebugCarroCima(Graphics2D g2d, Piloto piloto,
			double rad) {
		/**
		 * DEBUG
		 */
		if (!Logger.ativo) {
			return;
		}
		if (piloto.getDiateira() == null || piloto.getCentro() == null
				|| piloto.getTrazeira() == null) {
			return;
		}
		g2d.setColor(new Color(255, 0, 0, 140));
		g2d.setColor(Color.BLACK);

		Point2D.Double frenteCarD = new Point2D.Double(
				piloto.getDiateira().getCenterX() - descontoCentraliza.x,
				piloto.getDiateira().getCenterY() - descontoCentraliza.y);
		Point2D.Double trazCarD = new Point2D.Double(
				piloto.getTrazeira().getCenterX() - descontoCentraliza.x,
				piloto.getTrazeira().getCenterY() - descontoCentraliza.y);
		g2d.setColor(Color.GREEN);
		g2d.fillOval(Util.inteiro(frenteCarD.x * zoom),
				Util.inteiro(frenteCarD.y * zoom), Util.inteiro(5 * zoom),
				Util.inteiro(5 * zoom));
		g2d.fillOval(Util.inteiro(trazCarD.x * zoom),
				Util.inteiro(trazCarD.y * zoom), Util.inteiro(5 * zoom),
				Util.inteiro(5 * zoom));
		if (posisAtual != null) {
			g2d.setColor(Color.MAGENTA);
			g2d.fillOval(
					Util.inteiro((posisAtual.x - descontoCentraliza.x) * zoom),
					Util.inteiro((posisAtual.y - descontoCentraliza.y) * zoom),
					Util.inteiro(5 * zoom), Util.inteiro(5 * zoom));
		}
		if (posisRec != null) {
			g2d.setColor(Color.CYAN);
			g2d.fillOval(
					Util.inteiro((posisRec.getPoint().x - descontoCentraliza.x)
							* zoom),
					Util.inteiro((posisRec.getPoint().y - descontoCentraliza.y)
							* zoom),
					Util.inteiro(5 * zoom), Util.inteiro(5 * zoom));
		}
		if (afZoomDebug == null)
			afZoomDebug = new AffineTransform();
		if (translateDebug == null) {
			translateDebug = new AffineTransform();
		}
		afZoomDebug.setToScale(zoom, zoom);
		translateDebug.setToTranslation(-descontoCentraliza.x * zoom,
				-descontoCentraliza.y * zoom);
		if (piloto.getCentro() != null) {
			Shape transformedShape = afZoomDebug
					.createTransformedShape(piloto.getCentro());
			transformedShape = translateDebug
					.createTransformedShape(transformedShape);
			if (piloto.isColisaoCentro()) {
				g2d.setColor(Color.YELLOW);
			}
			g2d.draw(transformedShape);
		}
		if (piloto.getDiateira() != null) {
			Shape transformedShape = afZoomDebug
					.createTransformedShape(piloto.getDiateira());
			transformedShape = translateDebug
					.createTransformedShape(transformedShape);
			if (piloto.isColisaoDiantera()) {
				g2d.setColor(Color.YELLOW);
			}
			g2d.draw(transformedShape);
		}
		if (piloto.getTrazeira() != null) {
			Shape transformedShape = afZoomDebug
					.createTransformedShape(piloto.getTrazeira());
			transformedShape = translateDebug
					.createTransformedShape(transformedShape);
			g2d.draw(transformedShape);
		}
	}

	private void desenhaFumacaTravarRodas(int width, int height,
			AffineTransform afRotate, AffineTransform afZoom, double carx,
			double cary, Graphics g2d, BufferedImage img) {

		if (rotateBufferTravarRodas == null)
			rotateBufferTravarRodas = new BufferedImage(width, width,
					BufferedImage.TYPE_INT_ARGB);
		if (zoomBufferTravarRodas == null)
			zoomBufferTravarRodas = new BufferedImage(width, height,
					BufferedImage.TYPE_INT_ARGB);
		AffineTransformOp op = new AffineTransformOp(afRotate, interpolacao);
		op.filter(img, zoomBufferTravarRodas);
		AffineTransformOp op2 = new AffineTransformOp(afZoom, interpolacao);
		op2.filter(zoomBufferTravarRodas, rotateBufferTravarRodas);
		if (desenhaImagens)
			g2d.drawImage(rotateBufferTravarRodas,
					Util.inteiro((carx - descontoCentraliza.x) * zoom),
					Util.inteiro((cary - descontoCentraliza.y) * zoom), null);

	}

	public List getGridImgCopia() {
		List copy = new ArrayList();
		try {
			copy.addAll(gridImg);
		} catch (Exception e) {
			return new ArrayList();
		}
		return copy;
	}

	private void desenhaGrid(Graphics2D g2d) {
		if (mouseZoom != zoomGrid) {
			return;
		}
		if (limitesViewPort == null) {
			return;
		}
		List gridCopy = getGridImgCopia();
		if (gridCopy.isEmpty()) {
			return;
		}
		if (translateGrid == null)
			translateGrid = new AffineTransform();
		translateGrid.setToTranslation(-descontoCentraliza.x * zoom,
				-descontoCentraliza.y * zoom);
		for (int i = 0; i < 24; i++) {
			Shape shapeGrid = translateGrid.createTransformedShape(grid[i]);
			Shape shapeAsfalto = translateGrid
					.createTransformedShape(asfaltoGrid[i]);
			if (grid[i] == null
					|| !limitesViewPort.intersects(shapeGrid.getBounds2D())) {
				continue;
			}
			if (circuito != null && circuito.isUsaBkg()) {
				if (i >= gridCopy.size()) {
					continue;
				}

				BufferedImage buffer = (BufferedImage) gridCopy.get(i);
				double meix = (gridCarro.getWidth() / 2) * zoom;
				double meiy = (gridCarro.getHeight() / 2) * zoom;
				int x = Util
						.inteiro((shapeGrid.getBounds().getCenterX() - meix));
				int y = Util
						.inteiro((shapeGrid.getBounds().getCenterY() - meiy));
				g2d.drawImage(buffer, x, y, null);
			} else {
				g2d.setColor(Color.white);
				g2d.fill(shapeGrid);
				g2d.setColor(Color.lightGray);
				g2d.fill(shapeAsfalto);
			}

		}

	}

	public void gerarGrid() {
		gridImg.clear();
		for (int i = 0; i < 24; i++) {
			int iP = 50 + Util.inteiro((Carro.LARGURA * 0.8) * i);
			No n1 = (No) circuito.getPistaFull().get(
					circuito.getPistaFull().size() - iP - Carro.MEIA_LARGURA);
			No nM = (No) circuito.getPistaFull()
					.get(circuito.getPistaFull().size() - iP);
			No n2 = (No) circuito.getPistaFull().get(
					circuito.getPistaFull().size() - iP + Carro.MEIA_LARGURA);
			Point p1 = new Point(Util.inteiro(n1.getPoint().x * zoom),
					Util.inteiro(n1.getPoint().y * zoom));
			Point pm = new Point(Util.inteiro(nM.getPoint().x * zoom),
					Util.inteiro(nM.getPoint().y * zoom));
			Point p2 = new Point(Util.inteiro(n2.getPoint().x * zoom),
					Util.inteiro(n2.getPoint().y * zoom));
			double calculaAngulo = GeoUtil.calculaAngulo(p1, p2, 0);
			if (rectangleGerarGrid == null) {
				rectangleGerarGrid = new Rectangle2D.Double(
						(pm.x - (Carro.MEIA_LARGURA)),
						(pm.y - (Carro.MEIA_ALTURA)), (Carro.LARGURA),
						(Carro.ALTURA));
			} else {
				rectangleGerarGrid.setFrame((pm.x - (Carro.MEIA_LARGURA)),
						(pm.y - (Carro.MEIA_ALTURA)), (Carro.LARGURA),
						(Carro.ALTURA));
			}
			Point cima = GeoUtil.calculaPonto(calculaAngulo,
					Util.inteiro(
							Carro.ALTURA
									* controleJogo.getCircuito()
											.getMultiplicadorLarguraPista()
									* zoom),
					new Point(Util.inteiro(rectangleGerarGrid.getCenterX()),
							Util.inteiro(rectangleGerarGrid.getCenterY())));
			Point baixo = GeoUtil
					.calculaPonto(
							calculaAngulo + 180, Util
									.inteiro(Carro.ALTURA
											* controleJogo.getCircuito()
													.getMultiplicadorLarguraPista()
											* zoom),
							new Point(
									Util.inteiro(
											rectangleGerarGrid.getCenterX()),
									Util.inteiro(
											rectangleGerarGrid.getCenterY())));
			if (i % 2 == 0) {
				rectangleGerarGrid = new Rectangle2D.Double(
						(cima.x - (Carro.MEIA_LARGURA * zoom)),
						(cima.y - (Carro.MEIA_ALTURA * zoom)),
						(Carro.LARGURA * zoom), (Carro.ALTURA * zoom));
			} else {
				rectangleGerarGrid = new Rectangle2D.Double(
						(baixo.x - (Carro.MEIA_LARGURA * zoom)),
						(baixo.y - (Carro.MEIA_ALTURA * zoom)),
						(Carro.LARGURA * zoom), (Carro.ALTURA * zoom));
			}

			GeneralPath generalPath = new GeneralPath(rectangleGerarGrid);

			AffineTransform affineTransformRect = AffineTransform
					.getScaleInstance(zoom, zoom);
			double rad = Math.toRadians((double) calculaAngulo);
			affineTransformRect.setToRotation(rad,
					rectangleGerarGrid.getCenterX(),
					rectangleGerarGrid.getCenterY());
			boolean naoDesenha = false;
			if (circuito.getObjetos() != null) {
				for (Iterator iterator = circuito.getObjetos()
						.iterator(); iterator.hasNext();) {
					ObjetoPista objetoPista = (ObjetoPista) iterator.next();
					if (objetoPista instanceof ObjetoTransparencia) {
						ObjetoTransparencia objetoTransparencia = (ObjetoTransparencia) objetoPista;
						if (objetoTransparencia.obterArea()
								.intersects(generalPath.getBounds())) {
							naoDesenha = true;
						}
						if (objetoTransparencia.obterArea()
								.contains(n1.getPoint())
								|| objetoTransparencia.obterArea()
										.contains(nM.getPoint())
								|| objetoTransparencia.obterArea()
										.contains(n2.getPoint())) {
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
				int width = Util.inteiro(gridCarro.getWidth() * zoom);
				int height = Util.inteiro(gridCarro.getHeight() * zoom);
				afRotate.setToRotation(rad, width / 2, height / 2);
				BufferedImage rotateBuffer = new BufferedImage(width, height,
						BufferedImage.TYPE_INT_ARGB);
				BufferedImage zoomBuffer = new BufferedImage(width, height,
						BufferedImage.TYPE_INT_ARGB);
				AffineTransformOp op = new AffineTransformOp(afZoom,
						interpolacao);
				op.filter(gridCarro, zoomBuffer);
				AffineTransformOp op2 = new AffineTransformOp(afRotate,
						interpolacao);
				op2.filter(zoomBuffer, rotateBuffer);
				gridImg.add(rotateBuffer);
			} else {

				iP += 5;
				n1 = (No) circuito.getPistaFull()
						.get(circuito.getPistaFull().size() - iP
								- Carro.MEIA_LARGURA);
				nM = (No) circuito.getPistaFull()
						.get(circuito.getPistaFull().size() - iP);
				n2 = (No) circuito.getPistaFull()
						.get(circuito.getPistaFull().size() - iP
								+ Carro.MEIA_LARGURA);
				p1 = new Point(Util.inteiro(n1.getPoint().x * zoom),
						Util.inteiro(n1.getPoint().y * zoom));
				pm = new Point(Util.inteiro(nM.getPoint().x * zoom),
						Util.inteiro(nM.getPoint().y * zoom));
				p2 = new Point(Util.inteiro(n2.getPoint().x * zoom),
						Util.inteiro(n2.getPoint().y * zoom));
				calculaAngulo = GeoUtil.calculaAngulo(p1, p2, 0);
				rectangleGerarGrid = new Rectangle2D.Double(
						(pm.x - (Carro.MEIA_LARGURA)),
						(pm.y - (Carro.MEIA_ALTURA)), (Carro.LARGURA),
						(Carro.ALTURA));

				cima = GeoUtil.calculaPonto(calculaAngulo,
						Util.inteiro(
								Carro.ALTURA
										* controleJogo.getCircuito()
												.getMultiplicadorLarguraPista()
										* zoom),
						new Point(Util.inteiro(rectangleGerarGrid.getCenterX()),
								Util.inteiro(rectangleGerarGrid.getCenterY())));
				baixo = GeoUtil
						.calculaPonto(
								calculaAngulo + 180, Util
										.inteiro(Carro.ALTURA
												* controleJogo.getCircuito()
														.getMultiplicadorLarguraPista()
												* zoom),
								new Point(
										Util.inteiro(rectangleGerarGrid
												.getCenterX()),
										Util.inteiro(rectangleGerarGrid
												.getCenterY())));
				if (i % 2 == 0) {
					rectangleGerarGrid = new Rectangle2D.Double(
							(cima.x - (Carro.MEIA_LARGURA * zoom)),
							(cima.y - (Carro.MEIA_ALTURA * zoom)),
							(Carro.LARGURA * zoom), (Carro.ALTURA * zoom));
				} else {
					rectangleGerarGrid = new Rectangle2D.Double(
							(baixo.x - (Carro.MEIA_LARGURA * zoom)),
							(baixo.y - (Carro.MEIA_ALTURA * zoom)),
							(Carro.LARGURA * zoom), (Carro.ALTURA * zoom));
				}

				generalPath = new GeneralPath(rectangleGerarGrid);

				affineTransformRect = AffineTransform.getScaleInstance(zoom,
						zoom);
				rad = Math.toRadians((double) calculaAngulo);
				affineTransformRect.setToRotation(rad,
						rectangleGerarGrid.getCenterX(),
						rectangleGerarGrid.getCenterY());
				asfaltoGrid[i] = generalPath
						.createTransformedShape(affineTransformRect);
			}
		}
	}

	public void centralizarPonto(Point p) {
		pontoCentralizado = p;
		if (pontoCentralizadoOld != null) {
			List reta = GeoUtil.drawBresenhamLine(pontoCentralizadoOld,
					pontoCentralizado);
			int dezpor = (int) (reta.size() * 2.5
					/ (double) gerenciadorVisual.getFps());
			if (dezpor > dezporSuave) {
				dezporSuave++;
			}
			if (dezpor < dezporSuave) {
				dezporSuave--;
			}
			if (dezporSuave >= reta.size()) {
				dezporSuave = reta.size() - 1;
			}
			if (dezporSuave < 0) {
				dezporSuave = 0;
			}
			pontoCentralizado = (Point) reta.get(dezporSuave);
		}
		pontoCentralizadoOld = pontoCentralizado;
	}

	private void desenhaChuva(Graphics2D g2d) {

		if (controleJogo.isJogoPausado()) {
			int alfaNub = indiceNublado / 10;
			g2d.setColor(new Color(nublado.getRed(), nublado.getGreen(),
					nublado.getBlue(), alfaNub));
			g2d.fill(limitesViewPortFull.getBounds());
			return;
		}

		if (!controleJogo.getClima().equals(climaAnterior)) {
			climaAnterior = controleJogo.getClima();
		}
		int indiceNubladoAntesPausa = indiceNublado;

		if (Clima.NUBLADO.equals(controleJogo.getClima())) {
			if (Clima.CHUVA.equals(climaAnterior)) {
				if (Math.random() > 0.7) {
					indiceNublado--;
					if (indiceNublado < 0) {
						indiceNublado = 0;
					}
				}
			} else {
				if (Math.random() > 0.9) {
					if (indiceNublado > 700) {
						indiceNublado--;
					} else {
						indiceNublado++;
					}
				}
			}
			int alfaNub = indiceNublado / 10;
			g2d.setColor(new Color(nublado.getRed(), nublado.getGreen(),
					nublado.getBlue(), alfaNub));
			g2d.fill(limitesViewPortFull.getBounds());
		}
		if (Clima.SOL.equals(controleJogo.getClima())) {
			if (Math.random() > 0.7) {
				indiceNublado--;
				if (indiceNublado < 0) {
					indiceNublado = 0;
				}
			}
			int alfaNub = indiceNublado / 10;
			g2d.setColor(new Color(nublado.getRed(), nublado.getGreen(),
					nublado.getBlue(), alfaNub));
			g2d.fill(limitesViewPortFull.getBounds());
		}

		if ((Clima.CHUVA.equals(controleJogo.getClima()))
				&& limitesViewPort() != null) {
			if (indiceNublado > 1500 && Math.random() > 0.9) {
				indiceNublado++;
				if (indiceNublado > 2000) {
					indiceNublado = 2000;
				}
			} else if (Math.random() > 0.7 && indiceNublado <= 1500) {
				indiceNublado++;
			}
			if (indiceNublado < 700) {
				indiceNublado = 700;
			}
			int alfaNub = indiceNublado / 10;
			g2d.setColor(new Color(nublado.getRed(), nublado.getGreen(),
					nublado.getBlue(), alfaNub));
			g2d.fill(limitesViewPortFull.getBounds());
		}
		if (indiceNublado <= 0)
			return;
		g2d.setColor(lightWhiteRain);
		desenhaGotasDeChuva(g2d);
		if (controleJogo.isJogoPausado()) {
			indiceNublado = indiceNubladoAntesPausa;
		}
	}

	private void desenhaGotasDeChuva(Graphics2D g2d) {
		if (!Clima.CHUVA.equals(controleJogo.getClima())) {
			return;
		}
		Point p1;
		Point p2;
		double qtdeGotas = indiceNublado / 2000.0;
		if (Math.random() > qtdeGotas) {
			return;
		}
		for (int i = 0; i < limitesViewPortFull.getWidth(); i += 20) {
			if (Math.random() > qtdeGotas) {
				continue;
			}
			for (int j = 0; j < limitesViewPortFull.getHeight(); j += 20) {
				if (Math.random() > qtdeGotas) {
					continue;
				}
				if (Math.random() > .8) {

					p1 = new Point(i + 10, j + 10);
					p2 = new Point(i + 15, j + 20);
					if (!(limitesViewPortFull.contains(p1)
							&& limitesViewPortFull.contains(p2)))
						continue;
					g2d.drawLine(p1.x + limitesViewPortFull.x,
							p1.y + limitesViewPortFull.y,
							p2.x + limitesViewPortFull.x,
							p2.y + limitesViewPortFull.y);
				}
			}
		}
	}

	private void desenhaFaiscaLateral(Graphics2D g2d, Point p) {
		if (p == null) {
			return;
		}
		if (controleJogo.isJogoPausado()) {
			return;
		}
		if (Math.random() > .9) {
			return;
		}
		Color color = g2d.getColor();
		g2d.setColor(Color.YELLOW);
		for (int i = 0; i < 7; i++) {
			if (Math.random() > .5) {
				int valx = Util.intervalo(5, 15);
				int valy = Util.intervalo(-5, 15);
				g2d.drawLine(p.x + valx, p.y + valy, p.x + i * valx,
						p.y + valy - Util.intervalo(10, 20));
			}
		}
		g2d.setColor(color);
	}

	private void desenhaBarraListaPiloto(Graphics2D g2d, int x, int y,
			int tamNome, int i, Piloto piloto) {
		if (isExibeResultadoFinal()) {
			return;
		}
		Color bkg = transpMenus;
		Color fonte = Color.black;
		if (controleJogo.getPilotoJogador() != null
				&& controleJogo.getPilotoJogador().equals(piloto)) {
			bkg = jogador;
			fonte = Color.white;
			if (pilotoSelecionado == null) {
				pilotoSelecionado = piloto;
			}
		} else if (piloto.isJogadorHumano()
				&& !controleJogo.getPilotoJogador().equals(piloto)) {
			bkg = oran;
			fonte = Color.white;
		} else if (piloto.isDesqualificado()) {
			bkg = red;
			fonte = Color.white;
		} else if (controleJogo.verirficaDesafiandoCampeonato(piloto)) {
			bkg = oran;
			fonte = Color.white;
		}
		g2d.setColor(bkg);
		if (rectanglePos == null) {
			rectanglePos = new RoundRectangle2D.Double(x, y, 25, 20, 0, 0);
		} else {
			rectanglePos.setFrame(x, y, 25, 20);
		}
		g2d.fill(rectanglePos);
		g2d.setColor(fonte);
		if (piloto.getNoAtual() != null && piloto.getNoAtual().isBox()) {
			g2d.drawString("P", x + 5, y + 16);
		} else {
			g2d.drawString("" + (i + 1), x + 5, y + 16);
		}

		String nmPiloto = piloto.getNome();
		pilotosRect[i].setFrame(x + 30, y, tamNome + 10, 20);
		g2d.setColor(bkg);
		g2d.fill(pilotosRect[i]);
		g2d.setColor(fonte);
		g2d.drawString(nmPiloto, x + 35, y + 16);

		if (rectangleVol == null) {
			rectangleVol = new RoundRectangle2D.Double(
					x + 35 + pilotosRect[i].getWidth(), y, 25, 20, 0, 0);
		} else {
			rectangleVol.setFrame(x + 35 + pilotosRect[i].getWidth(), y, 25,
					20);
		}

		g2d.setColor(bkg);
		g2d.fill(rectangleVol);
		g2d.setColor(fonte);
		g2d.drawString(
				"" + (piloto.getNumeroVolta() < 0
						? 0
						: piloto.getNumeroVolta()),
				Util.inteiro(x + 40 + pilotosRect[i].getWidth()), y + 16);

		if (piloto.equals(pilotoSelecionado)) {
			g2d.setColor(yel);
			Stroke stroke = g2d.getStroke();
			g2d.setStroke(trilhoMiniPista);
			g2d.draw(rectanglePos);
			g2d.draw(pilotosRect[i]);
			g2d.draw(rectangleVol);
			g2d.setStroke(stroke);
		}
	}

	private void desenhaInfoPilotoSelecionado(Graphics2D g2d) {
		if (pilotoSelecionado == null) {
			return;
		}
		if (!desenhaInfo) {
			return;
		}
		if (isExibeResultadoFinal()) {
			return;
		}

		String plider = "";
		if (pilotoSelecionado.getPosicao() == 1) {
			plider = Lang.msg("Lider");
			g2d.setColor(new Color(0, 0, 121));
		} else if (controleJogo.verificaCampeonatoComRival()) {
			plider = controleJogo.calculaSegundosParaRival(pilotoSelecionado);
			g2d.setColor(Color.black);
			if (plider.startsWith("-")) {
				g2d.setColor(new Color(0, 0, 121));
			} else {
				g2d.setColor(new Color(121, 0, 0));
			}
		} else {
			g2d.setColor(Color.black);
			plider = pilotoSelecionado.getSegundosParaLider();
		}

		int ptoOri = limitesViewPort.x + limitesViewPort.width - 275;
		int yBase = limitesViewPort.y + 7;

		g2d.setColor(transpMenus);
		g2d.fillRoundRect(ptoOri - 5, yBase, 105, 50, 0, 0);

		g2d.setColor(Color.BLACK);

		yBase += 15;
		g2d.drawString(
				Lang.msg("081") + ": "
						+ (pilotoSelecionado.getNumeroVolta() <= 0
								? 0
								: pilotoSelecionado.getNumeroVolta()),
				ptoOri, yBase);
		yBase += 15;
		g2d.drawString(Lang.msg("068") + pilotoSelecionado.getQtdeParadasBox(),
				ptoOri, yBase);

		yBase += 15;
		g2d.drawString(
				(controleJogo.verificaCampeonatoComRival()
						? Lang.msg("rival")
						: Lang.msg("070")) + plider,
				ptoOri, yBase);

		if ((pilotoSelecionado.getNumeroVolta() > 0)) {
			Volta voltaPiloto = controleJogo
					.obterMelhorVolta(pilotoSelecionado);
			if (voltaPiloto != null) {
				yBase += 18;
				g2d.setColor(transpMenus);
				g2d.fillRoundRect(ptoOri - 5, yBase - 12, 105, 16, 0, 0);
				g2d.setColor(new Color(0, 0, 111));
				g2d.drawString(
						Lang.msg("079") + voltaPiloto.getTempoVoltaFormatado(),
						ptoOri, yBase);
			}

			Volta voltaCorrida = controleJogo.obterMelhorVolta();
			if (voltaCorrida != null) {
				yBase += 17;
				g2d.setColor(transpMenus);
				g2d.fillRoundRect(ptoOri - 5, yBase - 12, 105, 16, 0, 0);
				g2d.setColor(Color.black);
				g2d.drawString(
						Lang.msg("corrida") + ":"
								+ voltaCorrida.getTempoVoltaFormatado(),
						ptoOri, yBase);
			}

			yBase += 17;
			g2d.setColor(transpMenus);
			g2d.fillRoundRect(ptoOri - 5, yBase - 12, 105, 16, 0, 0);
			g2d.setColor(Color.black);
			g2d.drawString(Lang.msg("080"), ptoOri, yBase);

			yBase += 17;
			int contAlt = yBase;
			List<String> voltas = pilotoSelecionado.getUltimas5Voltas();
			for (Iterator<String> iterator = voltas.iterator(); iterator
					.hasNext();) {
				String volta = iterator.next();
				if (volta == null) {
					continue;
				}
				g2d.setColor(transpMenus);
				g2d.fillRoundRect(ptoOri - 5, contAlt - 12, 105, 16, 0, 0);
				g2d.setColor(Color.black);
				g2d.drawString(volta, ptoOri, contAlt);
				contAlt += 17;
			}
		}
	}

	private void desenhaProblemasCarroSelecionado(Piloto pilotoSelecionado,
			Graphics2D g2d) {
		if (qtdeLuzesAcesas > 0) {
			return;
		}
		if (!desenhaInfo) {
			return;
		}
		if (isExibeResultadoFinal()) {
			return;
		}
		if (pilotoSelecionado == null) {
			return;
		}
		if (controleJogo.getNiveljogo() == InterfaceJogo.DIFICIL_NV
				&& !pilotoSelecionado.equals(controleJogo.getPilotoJogador())) {
			return;
		}
		int pneus = pilotoSelecionado.getCarro().getPorcentagemDesgastePneus();
		int porcentComb = pilotoSelecionado.getCarro()
				.getPorcentagemCombustivel();
		String dano = null;
		if (pilotoSelecionado != null) {
			dano = pilotoSelecionado.getCarro().getDanificado();
		}
		if ((dano == null || "".equals(dano)) && pneus > 25 && porcentComb > 25
				&& !pilotoSelecionado.isAlertaAerefolio()
				&& !pilotoSelecionado.isAlertaMotor()) {
			return;
		}
		g2d.setColor(this.transpMenus);
		g2d.fillRoundRect(limitesViewPort.x + 175, limitesViewPort.y + 5,
				carroimgDano.getWidth() + 5, carroimgDano.getHeight() + 5, 0,
				0);
		if (desenhaImagens)
			g2d.drawImage(carroimgDano, limitesViewPort.x + 180,
					limitesViewPort.y + 10, null);

		if (pilotoSelecionado.isAlertaAerefolio() && !Carro.PERDEU_AEREOFOLIO
				.equals(pilotoSelecionado.getCarro().getDanificado())) {
			// bico
			g2d.setColor(OcilaCor.geraOcila("bicoAmarelo", Color.yellow));
			g2d.fillOval(limitesViewPort.x + 183, limitesViewPort.y + 26, 15,
					15);
		}

		if (porcentComb <= 25 && desenhaImagens) {
			g2d.drawImage(fuel,
					limitesViewPort.x + carroimgDano.getWidth() + 130,
					limitesViewPort.y + 10, null);
		}

		if (Carro.PERDEU_AEREOFOLIO
				.equals(pilotoSelecionado.getCarro().getDanificado())) {
			// bico
			g2d.setColor(Color.red);
			g2d.setColor(OcilaCor.geraOcila("bicoVermelho", Color.red));
			g2d.fillOval(limitesViewPort.x + 183, limitesViewPort.y + 26, 15,
					15);
		}

		if (Carro.PNEU_FURADO
				.equals(pilotoSelecionado.getCarro().getDanificado())) {
			g2d.setColor(OcilaCor.geraOcila("pneuFurado", Color.red));
			// Roda diantera
			g2d.fillOval(limitesViewPort.x + 203, limitesViewPort.y + 24, 18,
					18);
			// Roda trazeira
			g2d.fillOval(limitesViewPort.x + 307, limitesViewPort.y + 24, 18,
					18);
		} else if (pneus <= 25) {
			g2d.setColor(OcilaCor.geraOcila("pneuGastos", Color.yellow));
			// Roda diantera
			g2d.fillOval(limitesViewPort.x + 203, limitesViewPort.y + 24, 18,
					18);
			// Roda trazeira
			g2d.fillOval(limitesViewPort.x + 307, limitesViewPort.y + 24, 18,
					18);
		}
		if (Carro.EXPLODIU_MOTOR
				.equals(pilotoSelecionado.getCarro().getDanificado())) {
			g2d.setColor(OcilaCor.geraOcila("explodioMotor", Color.red));
			// motor
			g2d.fillOval(limitesViewPort.x + 273, limitesViewPort.y + 12, 15,
					15);
		} else if (pilotoSelecionado.isAlertaMotor()) {
			g2d.setColor(OcilaCor.geraOcila("motorGasto", Color.yellow));
			g2d.fillOval(limitesViewPort.x + 273, limitesViewPort.y + 12, 15,
					15);
		}
		if (Carro.BATEU_FORTE
				.equals(pilotoSelecionado.getCarro().getDanificado())) {
			g2d.setColor(OcilaCor.geraOcila("bateuForte", Color.red));
			g2d.fillRoundRect(limitesViewPort.x + 190, limitesViewPort.y + 18,
					135, 20, 0, 0);
		}

	}

	private void desenhaContadorVoltas(Graphics2D g2d) {
		g2d.setColor(luzApagada);
		String txt = Util.substVogais(controleJogo.getCircuito().getNome())
				+ " " + controleJogo.getNumVoltaAtual() + "/"
				+ controleJogo.totalVoltasCorrida();

		int largura = 0;
		for (int i = 0; i < txt.length(); i++) {
			largura += g2d.getFontMetrics().charWidth(txt.charAt(i));
		}
		g2d.fillRoundRect(
				limitesViewPort.x + (limitesViewPort.width / 2) - (largura / 2),
				limitesViewPort.y + 10, largura + 10, 20, 0, 0);
		g2d.setColor(Color.BLACK);
		g2d.drawString(txt,
				(limitesViewPort.x + (limitesViewPort.width / 2) + 6)
						- (largura / 2),
				limitesViewPort.y + 24);
		if (circuito.isUsaBkg() && backGround == null
				&& contImgFundo < 100000000) {
			txt = Lang.msg("carregandoBackground");
			largura = 0;
			for (int i = 0; i < txt.length(); i++) {
				largura += g2d.getFontMetrics().charWidth(txt.charAt(i));
			}
			g2d.setColor(OcilaCor.geraOcila("carregandoBackground", yel));
			g2d.fillRoundRect(
					limitesViewPort.x + (limitesViewPort.width / 2)
							- (largura / 2),
					limitesViewPort.y + 234, largura + 10, 20, 0, 0);
			g2d.setColor(Color.BLACK);
			g2d.drawString(txt,
					(limitesViewPort.x + (limitesViewPort.width / 2) + 6)
							- (largura / 2),
					limitesViewPort.y + 248);
			contImgFundo++;

		}
	}

	private void desenhaQualificacao(Graphics2D g2d) {
		if (!desenhaInfo) {
			return;
		}
		if (desenhouQualificacao) {
			return;
		}
		List<Piloto> pilotos = pilotosList;
		if (ptosPilotosDesQualy.isEmpty()) {
			int iniY1 = 50;
			int iniY2 = 60;
			int midPainel = 0;
			if (limitesViewPort != null)
				midPainel = Util.inteiro(limitesViewPort.width / 3);
			else {
				midPainel = 500;
			}
			for (int i = 0; i < pilotos.size(); i++) {
				Piloto piloto = (Piloto) pilotos.get(i);
				if (piloto.getPosicao() % 2 == 0) {
					ptosPilotosDesQualy.add(new Point(midPainel + 270, iniY2));
					iniY2 += 40;
				} else {
					ptosPilotosDesQualy.add(new Point(midPainel - 100, iniY1));
					iniY1 += 40;
				}
			}
		}
		if (contDesenhaPilotosQualify == null) {
			contDesenhaPilotosQualify = pilotos.size() * 15;
		}
		contDesenhaPilotosQualify--;
		for (int i = 0; i < pilotos.size(); i++) {
			if ((i) * 15 < contDesenhaPilotosQualify) {
				continue;
			}
			Piloto piloto = (Piloto) pilotos.get(i);
			Point point = (Point) ptosPilotosDesQualy.get(i);
			desenhaPilotoQualify(g2d, piloto, point.x, point.y);
		}
	}

	private void desenhaPilotoQualify(Graphics2D g2d, Piloto piloto, int x,
			int y) {
		BufferedImage carroimg;
		int newY;
		carroimg = controleJogo.obterCarroLado(piloto);
		controleJogo.obterCarroCima(piloto);
		newY = carroimg.getHeight() > 36 ? y - (carroimg.getHeight() - 36) : y;
		g2d.setColor(transpMenus);
		g2d.fillRoundRect(limitesViewPort.x + x - 5,
				limitesViewPort.y + newY - 5, carroimg.getWidth() + 5,
				carroimg.getHeight() + 5, 0, 0);
		int carSelX = x;
		int carSelY = newY + 30;
		if (desenhaImagens)
			g2d.drawImage(carroimg, null, carSelX, carSelY);

		String nomeTempo = piloto.getPosicao() + " - " + piloto.getNome()
				+ " - " + ControleEstatisticas
						.formatarTempo(piloto.getCiclosVoltaQualificacao());

		int maior = nomeTempo.length();

		Color c2 = piloto.getCarro().getCor2();
		if (c2 != null) {
			c2 = c2.brighter();
			g2d.setColor(
					new Color(c2.getRed(), c2.getGreen(), c2.getBlue(), 170));
		}
		Point pt = null;
		if (piloto.getPosicao() % 2 == 0) {
			pt = new Point(x + 190, y + 20);

		} else {
			pt = new Point(x - 170, y + 20);

		}
		g2d.fillRoundRect(limitesViewPort.x + pt.x - 10,
				limitesViewPort.y + pt.y - 15, maior * 7, 20, 0, 0);
		Stroke stroke = g2d.getStroke();
		boolean desenhaBorda = false;
		if (piloto.isJogadorHumano()
				&& controleJogo.getPilotoJogador().equals(piloto)) {
			g2d.setColor(OcilaCor.geraOcila("mrkSelBlu", bluQualy));
			desenhaBorda = true;

		}
		if (piloto.isJogadorHumano()
				&& !controleJogo.getPilotoJogador().equals(piloto)) {
			g2d.setColor(OcilaCor.geraOcila("mrkSelOran", Color.ORANGE));
			desenhaBorda = true;

		}
		if (controleJogo.verirficaDesafiandoCampeonato(piloto)) {
			g2d.setColor(OcilaCor.geraOcila("mrkDesaf", Color.ORANGE));
			desenhaBorda = true;

		}
		if (desenhaBorda) {
			g2d.setStroke(borda);
			g2d.drawRoundRect(limitesViewPort.x + pt.x - 10,
					limitesViewPort.y + pt.y - 15, maior * 7, 20, 0, 0);
			g2d.setStroke(stroke);
		}

		int valor = (c2.getRed() + c2.getGreen() + c2.getBlue()) / 2;
		if (valor > 200) {
			g2d.setColor(Color.BLACK);
		} else {
			g2d.setColor(Color.WHITE);
		}
		g2d.drawString(nomeTempo, limitesViewPort.x + pt.x,
				limitesViewPort.y + +pt.y);

		BufferedImage tpPneu = obterNomeImgTipoPneu(piloto.getCarro());
		if (tpPneu != null) {
			if (piloto.getPosicao() % 2 == 0) {
				if (desenhaImagens)
					g2d.drawImage(tpPneu, null, carSelX - (tpPneu.getWidth()),
							carSelY);
				BufferedImage cap = controleJogo.obterCapacete(piloto);
				if (cap != null && desenhaImagens) {
					g2d.drawImage(cap, null,
							carSelX - (cap.getWidth() + tpPneu.getWidth()),
							carSelY);
				}
			} else {
				pt = new Point(x - 120, y + 20);
				carSelX += 140;
				BufferedImage cap = controleJogo.obterCapacete(piloto);
				if (desenhaImagens)
					g2d.drawImage(tpPneu, null, carSelX + (tpPneu.getWidth()),
							carSelY);
				if (cap != null && desenhaImagens) {
					g2d.drawImage(cap, null,
							carSelX + cap.getWidth() + tpPneu.getWidth() - 10,
							carSelY);
				}
			}
		}
	}

	private void desenhaCarrosLado(Piloto psel, Graphics2D g2d) {
		if (!desenhaInfo) {
			return;
		}
		if (pilotoSelecionado == null) {
			return;
		}
		if (isExibeResultadoFinal()) {
			return;
		}
		BufferedImage carroimg = null;
		int carSelX = limitesViewPort.x;
		int carSelY = limitesViewPort.y + limitesViewPort.height - 75;
		int bounce = 0;
		int newY = 0;
		Carro carroFrente = psel.getCarroPilotoFrente();
		if (carroFrente != null) {
			carroimg = controleJogo.obterCarroLado(carroFrente.getPiloto());
			carSelX += (carroimg.getWidth() + 10) / 2;
			bounce = calculaBounceCarroLado(carroFrente);
			int diferencaParaProximo = psel.getDiferencaParaProximo()
					/ Constantes.CICLO;
			int dstX = limitesViewPort.x + (limitesViewPort.width / 4);

			int dstY = carSelY + 20;
			int halfCarWidth = carroimg.getWidth() / 3;
			carSelX += (130 - halfCarWidth);
			dstX += 105;
			BufferedImage tpPneu = obterNomeImgTipoPneu(carroFrente);
			if (tpPneu != null && desenhaImagens
					&& controleJogo.mostraTipoPneuAdversario()) {
				g2d.drawImage(tpPneu, null, carSelX - (tpPneu.getWidth() + 5),
						carSelY);
				BufferedImage cap = controleJogo
						.obterCapacete(carroFrente.getPiloto());
				if (cap != null && desenhaImagens) {
					g2d.drawImage(cap, null,
							carSelX - (cap.getWidth() + tpPneu.getWidth() + 5),
							carSelY);
				}
			}

			g2d.setColor(this.transpMenus);
			g2d.fillRoundRect(carSelX - 5, carSelY - 5, carroimg.getWidth() + 5,
					carroimg.getHeight() + 5, 0, 0);

			if (diferencaParaProximo >= 3.0) {
				g2d.setColor(gre);
			} else if (diferencaParaProximo < 3.0
					&& diferencaParaProximo > 1.0) {
				g2d.setColor(yel);
			} else if (diferencaParaProximo <= 1.0) {
				g2d.setColor(red);
			}
			if (Math.random() > 0.5) {
				carSelX += bounce;
			} else {
				carSelX -= bounce;
			}
			newY = carroimg.getHeight() > 36
					? carSelY - (carroimg.getHeight() - 36)
					: carSelY;
			if (!carroFrente.getPiloto().isDesqualificado()
					&& psel.getPtosBox() == 0
					&& mapaFaiscas.get(carroFrente.getPiloto()) != null) {
				desenhaFaiscaLateral(g2d,
						new Point(carSelX + carroimg.getWidth() - 10,
								newY + carroimg.getHeight() / 2));
			}
			if (desenhaImagens)
				g2d.drawImage(carroimg, null, carSelX, newY);

			g2d.fillRoundRect(dstX + 2, dstY - 12, 50, 15, 0, 0);
			if (diferencaParaProximo >= 3.0) {
				g2d.setColor(Color.BLACK);
			} else if (diferencaParaProximo < 3.0
					&& diferencaParaProximo > 1.0) {
				g2d.setColor(Color.BLACK);
			} else if (diferencaParaProximo <= 1.0) {
				g2d.setColor(Color.WHITE);
			}
			String val = psel.getCalculaSegundosParaProximo();
			if (val != null && val.length() < 8) {
				g2d.drawString("  " + val, dstX, dstY);
			}

		}
		carroimg = controleJogo.obterCarroLado(psel);
		carSelX = limitesViewPort.x + (limitesViewPort.width / 2)
				- (carroimg.getWidth() / 2);
		carSelY = limitesViewPort.y + limitesViewPort.height - 75;
		bounce = calculaBounceCarroLado(psel.getCarro());
		g2d.setColor(this.transpMenus);
		g2d.fillRoundRect(carSelX - 5, carSelY - 5, carroimg.getWidth() + 5,
				carroimg.getHeight() + 5, 0, 0);
		if (Math.random() > 0.5) {
			carSelX += bounce;
		} else {
			carSelX -= bounce;
		}
		newY = carroimg.getHeight() > 36
				? carSelY - (carroimg.getHeight() - 36)
				: carSelY;
		if (!psel.isDesqualificado() && mapaFaiscas.get(psel) != null) {
			desenhaFaiscaLateral(g2d,
					new Point(carSelX + carroimg.getWidth() - 10,
							newY + carroimg.getHeight() / 2));
		}
		if (desenhaImagens)
			g2d.drawImage(carroimg, null, carSelX, newY);

		Carro carroAtras = psel.getCarroPilotoAtras();
		if (carroAtras != null) {
			carroimg = controleJogo.obterCarroLado(carroAtras.getPiloto());
			carSelX = limitesViewPort.x + limitesViewPort.width
					+ -(carroimg.getWidth() + 10)
					- (carroimg.getWidth() + 10) / 2;

			bounce = calculaBounceCarroLado(carroAtras);

			int dstX = limitesViewPort.x + limitesViewPort.width
					+ -(limitesViewPort.width / 3);
			int dstY = carSelY + 20;
			double diferencaParaProximo = carroAtras.getPiloto()
					.getDiferencaParaProximo() / Constantes.CICLO;

			int halfCarWidth = carroimg.getWidth() / 3;
			carSelX -= (125 - halfCarWidth);
			dstX -= 80;
			BufferedImage tpPneu = obterNomeImgTipoPneu(carroAtras);
			if (tpPneu != null && desenhaImagens
					&& controleJogo.mostraTipoPneuAdversario()) {
				g2d.drawImage(tpPneu, null, carSelX + carroimg.getWidth() + 5,
						carSelY);
				BufferedImage cap = controleJogo
						.obterCapacete(carroAtras.getPiloto());
				if (cap != null && desenhaImagens) {
					g2d.drawImage(cap, null, (carSelX + carroimg.getWidth()
							+ tpPneu.getWidth() + 5), carSelY);
				}
			}
			g2d.setColor(this.transpMenus);
			g2d.fillRoundRect(carSelX - 5, carSelY - 5, carroimg.getWidth() + 5,
					carroimg.getHeight() + 5, 0, 0);
			if (diferencaParaProximo >= 3.0) {
				g2d.setColor(gre);
			} else if (diferencaParaProximo < 3.0
					&& diferencaParaProximo > 1.0) {
				g2d.setColor(yel);
			} else if (diferencaParaProximo <= 1.0) {
				g2d.setColor(red);
			}
			if (Math.random() > 0.5) {
				carSelX += bounce;
			} else {
				carSelX -= bounce;
			}
			newY = carroimg.getHeight() > 36
					? carSelY - (carroimg.getHeight() - 36)
					: carSelY;
			if (!carroAtras.getPiloto().isDesqualificado()
					&& mapaFaiscas.get(carroAtras.getPiloto()) != null) {
				desenhaFaiscaLateral(g2d,
						new Point(carSelX + carroimg.getWidth() - 10,
								newY + carroimg.getHeight() / 2));
			}
			if (desenhaImagens)
				g2d.drawImage(carroimg, null, carSelX, newY);

			g2d.fillRoundRect(dstX + 2, dstY - 12, 50, 15, 0, 0);
			if (diferencaParaProximo >= 3) {
				g2d.setColor(Color.BLACK);
			} else if (diferencaParaProximo < 3 && diferencaParaProximo > 1) {
				g2d.setColor(Color.BLACK);
			} else if (diferencaParaProximo <= 1) {
				g2d.setColor(Color.WHITE);
			}
			String val = psel.getCalculaSegundosParaAnterior();
			if (val != null && val.length() < 8) {
				g2d.drawString("  " + val, dstX, dstY);
			}
		}

	}

	private BufferedImage obterNomeImgTipoPneu(Carro carro) {
		if (Carro.TIPO_PNEU_DURO.equals(carro.getTipoPneu())) {
			return pneuDuroImg;
		} else if (Carro.TIPO_PNEU_MOLE.equals(carro.getTipoPneu())) {
			return pneuMoleImg;
		} else if (Carro.TIPO_PNEU_CHUVA.equals(carro.getTipoPneu())) {
			return pneuChuvaImg;
		}
		return null;
	}

	private int calculaBounceCarroLado(Carro carro) {
		if (controleJogo.isJogoPausado()) {
			return 0;
		}
		if (carro.getPiloto().isDesqualificado()) {
			return 0;
		}
		if (qtdeLuzesAcesas > 0 || carro.getPiloto().isBox()) {
			return 0;
		} else if (carro.getPiloto().isAgressivo() == false) {
			return Math.random() > .5 ? 1 : 0;
		} else if (carro.getPiloto().isAgressivo() == true
				&& carro.getGiro() != Carro.GIRO_MAX_VAL) {
			return 1;
		} else if (carro.getPiloto().isAgressivo() == true
				&& carro.getGiro() == Carro.GIRO_MAX_VAL) {
			return Math.random() > .5 ? 2 : 1;
		}
		return 0;
	}

	private void desenharSafetyCarCima(Graphics2D g2d) {
		int scx, scy;
		if (!controleJogo.isSafetyCarNaPista()) {
			return;
		}
		SafetyCar safetyCar = controleJogo.getSafetyCar();
		if (safetyCar == null) {
			return;
		}
		if (safetyCar.isEsperando()) {
			return;
		}
		atualizacaoSuave(safetyCar);
		No noAtual = safetyCar.getNoAtualSuave();
		if (noAtual == null) {
			noAtual = safetyCar.getNoAtual();
		}
		Point p = noAtual.getPoint();

		if (!limitesViewPort.contains(
				((noAtual.getX() - descontoCentraliza.x) * zoom),
				((noAtual.getY() - descontoCentraliza.y) * zoom))) {
			return;
		}

		g2d.setColor(Color.black);
		Stroke stroke = g2d.getStroke();
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
		if (rectangleSafetyCarCima == null) {
			rectangleSafetyCarCima = new Rectangle2D.Double(
					(p.x - Carro.MEIA_LARGURA), (p.y - Carro.MEIA_ALTURA),
					Carro.LARGURA, Carro.ALTURA);
		} else {
			rectangleSafetyCarCima.setFrame((p.x - Carro.MEIA_LARGURA),
					(p.y - Carro.MEIA_ALTURA), Carro.LARGURA, Carro.ALTURA);
		}
		Point p1 = GeoUtil.calculaPonto(calculaAngulo,
				Util.inteiro(Carro.ALTURA * controleJogo.getCircuito()
						.getMultiplicadorLarguraPista()),
				new Point(Util.inteiro(rectangleSafetyCarCima.getCenterX()),
						Util.inteiro(rectangleSafetyCarCima.getCenterY())));
		Point p2 = GeoUtil.calculaPonto(calculaAngulo + 180,
				Util.inteiro(Carro.ALTURA * controleJogo.getCircuito()
						.getMultiplicadorLarguraPista()),
				new Point(Util.inteiro(rectangleSafetyCarCima.getCenterX()),
						Util.inteiro(rectangleSafetyCarCima.getCenterY())));

		if (safetyCar.getTracado() == 0) {
			carx = p.x - w2;
			cary = p.y - h2;
		}
		if (safetyCar.getTracado() == 1) {
			carx = Util.inteiro((p1.x - w2));
			cary = Util.inteiro((p1.y - h2));
		}
		if (safetyCar.getTracado() == 2) {
			carx = Util.inteiro((p2.x - w2));
			cary = Util.inteiro((p2.y - h2));
		}

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
					interpolacao);
			op.filter(scima, zoomBuffer);
			AffineTransformOp op2 = new AffineTransformOp(afZoom, interpolacao);
			op2.filter(zoomBuffer, rotateBuffer);

			if (circuito.isUsaBkg() && circuito.getObjetos() != null) {
				for (ObjetoPista objetoPista : circuito.getObjetos()) {
					if (!(objetoPista instanceof ObjetoTransparencia))
						continue;
					if (objetoPista.isPintaEmcima()) {
						continue;
					}
					if (!limitesViewPort.contains(
							((objetoPista.getPosicaoQuina().x
									- descontoCentraliza.x) * zoom),
							((objetoPista.getPosicaoQuina().y
									- descontoCentraliza.y) * zoom))) {
						continue;
					}
					ObjetoTransparencia objetoTransparencia = (ObjetoTransparencia) objetoPista;
					Graphics2D gImage = rotateBuffer.createGraphics();
					objetoTransparencia.desenhaCarro(gImage, zoom, carx, cary);

				}
			}
			carx = Util.inteiro((carx - descontoCentraliza.x) * zoom);
			cary = Util.inteiro((cary - descontoCentraliza.y) * zoom);
			if (desenhaImagens)
				g2d.drawImage(rotateBuffer, Util.inteiro(carx),
						Util.inteiro(cary), null);
		}

		g2d.setColor(Color.LIGHT_GRAY);
		g2d.fillOval(Util.inteiro(carx + (w2 * zoom)),
				Util.inteiro(cary + (h2 * zoom)), 8, 8);
		if (!safetyCar.isVaiProBox()) {
			if (Math.random() > .5) {
				g2d.setColor(Color.YELLOW);
			} else {
				g2d.setColor(Color.BLACK);
			}
		} else
			g2d.setColor(Color.BLACK);
		g2d.drawOval(Util.inteiro(carx + (w2 * zoom)),
				Util.inteiro(cary + (h2 * zoom)), 8, 8);

		g2d.setStroke(stroke);

	}

	private void desenharClima(Graphics2D g2d) {
		if (!desenhaInfo) {
			return;
		}
		if (isExibeResultadoFinal()) {
			return;
		}
		if (controleJogo.getNiveljogo() == InterfaceJogo.DIFICIL_NV) {
			return;
		}
		BufferedImage icon = null;

		String clima = controleJogo.getClima();
		if (Clima.SOL.equals(clima)) {
			icon = iconSol;
			if (controleJogo.getCircuito() != null
					&& controleJogo.getCircuito().isNoite()) {
				icon = iconLua;
			}
		}
		if (Clima.CHUVA.equals(clima))
			icon = iconChuva;
		if (Clima.NUBLADO.equals(clima))
			icon = iconNublado;

		int x = limitesViewPort.x + (limitesViewPort.width / 2) + 110;

		if (icon != null) {
			g2d.setColor(transpMenus);
			if (informaMudancaClima > 0) {
				g2d.setColor(OcilaCor.geraOcila("informaMudancaClima", yel));
				informaMudancaClima--;
			}
			g2d.fillRoundRect(x, limitesViewPort.y + 5, icon.getWidth() + 10,
					icon.getHeight() + 10, 0, 0);
			if (desenhaImagens)
				g2d.drawImage(icon, x + 5, 10 + limitesViewPort.y, null);
		}
	}

	private void desenharTpPneuPsel(Graphics2D g2d) {
		if (!desenhaInfo) {
			return;
		}
		if (pilotoSelecionado == null) {
			return;
		}
		if (isExibeResultadoFinal()) {
			return;
		}

		Carro carro = pilotoSelecionado.getCarro();
		BufferedImage tpPneu = obterNomeImgTipoPneu(carro);
		if (tpPneu == null) {
			return;
		}

		g2d.setColor(transpMenus);
		g2d.fillRoundRect(limitesViewPort.x + 55, 10 + limitesViewPort.y,
				tpPneu.getWidth() + 10, tpPneu.getHeight() + 2, 0, 0);
		if (desenhaImagens)
			g2d.drawImage(tpPneu, limitesViewPort.x + 60,
					10 + limitesViewPort.y + 2, null);

	}

	private void desenharFarois(Graphics2D g2d) {

		if (qtdeLuzesAcesas <= 0) {
			return;
		}
		int xIni = Util.inteiro(limitesViewPort.width / 2) - 50;
		int yIni = 50;
		/**
		 * 1ª luz
		 */
		g2d.setColor(farol);
		g2d.fillRoundRect(limitesViewPort.x + xIni, limitesViewPort.y + yIni,
				20, 50, 0, 0);
		g2d.setColor(luzApagada);
		g2d.fillOval(limitesViewPort.x + xIni + 3, limitesViewPort.y + yIni + 5,
				14, 14);
		if (qtdeLuzesAcesas > 0) {
			g2d.setColor(Color.WHITE);
			g2d.fillOval(limitesViewPort.x + xIni + 3,
					limitesViewPort.y + yIni + 30, 14, 14);
			g2d.setColor(OcilaCor.geraOcila("farol0", lightRed));
			g2d.fillOval(limitesViewPort.x + xIni + 3,
					limitesViewPort.y + yIni + 30, 14, 14);
		} else {
			g2d.setColor(luzApagada);
		}
		g2d.fillOval(limitesViewPort.x + xIni + 3,
				limitesViewPort.y + yIni + 30, 14, 14);
		xIni += 25;
		g2d.setColor(farol);
		g2d.fillRoundRect(limitesViewPort.x + xIni, limitesViewPort.y + yIni,
				20, 50, 0, 0);
		g2d.setColor(luzApagada);
		g2d.fillOval(limitesViewPort.x + xIni + 3, limitesViewPort.y + yIni + 5,
				14, 14);
		if (qtdeLuzesAcesas > 1) {
			g2d.setColor(Color.WHITE);
			g2d.fillOval(limitesViewPort.x + xIni + 3,
					limitesViewPort.y + yIni + 30, 14, 14);
			g2d.setColor(OcilaCor.geraOcila("farol1", lightRed));
			g2d.fillOval(limitesViewPort.x + xIni + 3,
					limitesViewPort.y + yIni + 30, 14, 14);
		} else {
			g2d.setColor(luzApagada);

		}
		g2d.fillOval(limitesViewPort.x + xIni + 3,
				limitesViewPort.y + yIni + 30, 14, 14);
		xIni += 25;
		g2d.setColor(farol);
		g2d.fillRoundRect(limitesViewPort.x + xIni, limitesViewPort.y + yIni,
				20, 50, 0, 0);
		g2d.setColor(luzApagada);
		g2d.fillOval(limitesViewPort.x + xIni + 3, limitesViewPort.y + yIni + 5,
				14, 14);

		if (qtdeLuzesAcesas > 2) {
			g2d.setColor(Color.WHITE);
			g2d.fillOval(limitesViewPort.x + xIni + 3,
					limitesViewPort.y + yIni + 30, 14, 14);
			g2d.setColor(OcilaCor.geraOcila("farol2", lightRed));
			g2d.fillOval(limitesViewPort.x + xIni + 3,
					limitesViewPort.y + yIni + 30, 14, 14);
		} else {
			g2d.setColor(luzApagada);
		}
		g2d.fillOval(limitesViewPort.x + xIni + 3,
				limitesViewPort.y + yIni + 30, 14, 14);
		xIni += 25;
		g2d.setColor(farol);
		g2d.fillRoundRect(limitesViewPort.x + xIni, limitesViewPort.y + yIni,
				20, 50, 0, 0);
		g2d.setColor(luzApagada);
		g2d.fillOval(limitesViewPort.x + xIni + 3, limitesViewPort.y + yIni + 5,
				14, 14);

		if (qtdeLuzesAcesas > 3) {
			g2d.setColor(Color.WHITE);
			g2d.fillOval(limitesViewPort.x + xIni + 3,
					limitesViewPort.y + yIni + 30, 14, 14);
			g2d.setColor(OcilaCor.geraOcila("farol3", lightRed));
			g2d.fillOval(limitesViewPort.x + xIni + 3,
					limitesViewPort.y + yIni + 30, 14, 14);
		} else {
			g2d.setColor(luzApagada);
		}
		g2d.fillOval(limitesViewPort.x + xIni + 3,
				limitesViewPort.y + yIni + 30, 14, 14);
		xIni += 25;
		g2d.setColor(farol);
		g2d.fillRoundRect(limitesViewPort.x + xIni, limitesViewPort.y + yIni,
				20, 50, 0, 0);
		g2d.setColor(luzApagada);
		g2d.fillOval(limitesViewPort.x + xIni + 3, limitesViewPort.y + yIni + 5,
				14, 14);

		if (qtdeLuzesAcesas > 4) {
			g2d.setColor(Color.WHITE);
			g2d.fillOval(limitesViewPort.x + xIni + 3,
					limitesViewPort.y + yIni + 30, 14, 14);
			g2d.setColor(OcilaCor.geraOcila("farol4", lightRed));
			g2d.fillOval(limitesViewPort.x + xIni + 3,
					limitesViewPort.y + yIni + 30, 14, 14);
		} else {
			g2d.setColor(luzApagada);
		}
		g2d.fillOval(limitesViewPort.x + xIni + 3,
				limitesViewPort.y + yIni + 30, 14, 14);
	}

	private void desenhaNomePilotoSelecionado(Piloto ps, Graphics2D g2d) {
		if (isExibeResultadoFinal()) {
			return;
		}
		if (ps == null)
			return;
		if (ps.getNoAtual() == null)
			return;
		if (ps.getCarro() == null)
			return;

		Point pt = new Point(limitesViewPort.x + 5, limitesViewPort.y + 60);
		String piloto = ps.getNome();
		String carro = ps.getCarro().getNome();

		if (!Util.isNullOrEmpty(ps.getNomeJogador())) {
			piloto += " " + ps.getNomeJogador();
		}
		Color c2 = ps.getCarro().getCor2();
		if (c2 != null) {
			c2 = c2.brighter();
			g2d.setColor(
					new Color(c2.getRed(), c2.getGreen(), c2.getBlue(), 200));
		}
		int largura = 0;
		if (!Util.isNullOrEmpty(piloto)) {
			int larguraTxtPiloto = Util.calculaLarguraText(piloto, g2d) + 15;
			g2d.fillRoundRect(pt.x, pt.y, larguraTxtPiloto, 14, 0, 0);
			largura = larguraTxtPiloto;
		}
		if (!Util.isNullOrEmpty(carro)) {
			int larguraTxtCarro = Util.calculaLarguraText(carro, g2d) + 15;
			g2d.fillRoundRect(pt.x, pt.y + 16, larguraTxtCarro, 14, 0, 0);
			if (largura < larguraTxtCarro) {
				largura = larguraTxtCarro;
			}
		}
		int valor = (c2.getRed() + c2.getGreen() + c2.getBlue()) / 2;
		if (valor > 250) {
			g2d.setColor(Color.BLACK);
		} else {
			g2d.setColor(Color.WHITE);
		}
		if (!Util.isNullOrEmpty(piloto)) {
			g2d.drawString(piloto, pt.x + 5, pt.y + 11);
		}
		if (!Util.isNullOrEmpty(carro)) {
			g2d.drawString(carro, pt.x + 5, pt.y + 27);
		}

	}

	private void desenhaNomePilotoSelecionadoCarroCima(Piloto ps,
			Graphics2D g2d, int x, int y) {
		if (ps == null)
			return;
		if (ps.getNoAtual() == null)
			return;
		if (ps.getCarro() == null)
			return;
		String intel = (ps.isJogadorHumano()
				? ps.isAutoPos() ? "A " : "M "
				: "IA ");
		String txtDraw = intel + " " + ps.getNome();

		Color c2 = ps.getCarro().getCor2();
		if (c2 != null) {
			c2 = c2.brighter();
			g2d.setColor(
					new Color(c2.getRed(), c2.getGreen(), c2.getBlue(), 200));
		}
		if (!Util.isNullOrEmpty(pilotoSelecionado.getCarro().getDanificado())) {
			txtDraw = Lang.msg(pilotoSelecionado.getCarro().getDanificado());
		}

		if (!Util.isNullOrEmpty(txtDraw)) {
			Color bg = g2d.getColor();
			int larguraTxt2 = Util.calculaLarguraText(txtDraw, g2d);
			int xBase = Util.inteiro((x) - 20);
			int yBase = Util.inteiro((y) - 35);
			if (Carro.GIRO_MIN_VAL == pilotoSelecionado.getCarro().getGiro()) {
				g2d.setColor(OcilaCor.geraOcila("miniMotorMin", gre));
				g2d.fillRoundRect(xBase, yBase + 10, 7, 5, 2, 2);
				xBase += 24;
			} else if (Carro.GIRO_NOR_VAL == pilotoSelecionado.getCarro()
					.getGiro()) {
				g2d.setColor(gre);
				g2d.fillRoundRect(xBase, yBase + 10, 7, 5, 2, 2);
				g2d.setColor(OcilaCor.geraOcila("miniMotorNorm", yel));
				xBase += 8;
				g2d.fillRoundRect(xBase, yBase + 5, 7, 10, 3, 3);
				xBase += 16;
			} else if (Carro.GIRO_MAX_VAL == pilotoSelecionado.getCarro()
					.getGiro()) {
				g2d.setColor(gre);
				g2d.fillRoundRect(xBase, yBase + 10, 7, 5, 2, 2);
				g2d.setColor(yel);
				xBase += 8;
				g2d.fillRoundRect(xBase, yBase + 5, 7, 10, 3, 3);
				g2d.setColor(OcilaCor.geraOcila("miniMotorMax", red));
				xBase += 8;
				g2d.fillRoundRect(xBase, yBase, 7, 15, 4, 4);
				xBase += 8;
			}

			g2d.setColor(bg);
			xBase += 5;
			g2d.fillRoundRect(xBase - 2, yBase, larguraTxt2 + 8, 14, 0, 0);
			xBase += larguraTxt2 + 10;
			if (Piloto.AGRESSIVO.equals(pilotoSelecionado.getModoPilotagem())) {
				g2d.setColor(OcilaCor.geraOcila("miniPilotoMax", red));
				g2d.fillRoundRect(xBase, yBase, 7, 15, 4, 4);
				xBase += 8;
				g2d.setColor(yel);
				g2d.fillRoundRect(xBase, yBase + 5, 7, 10, 3, 3);
				xBase += 8;
				g2d.setColor(gre);
				g2d.fillRoundRect(xBase, yBase + 10, 7, 5, 4, 4);
			} else if (Piloto.NORMAL
					.equals(pilotoSelecionado.getModoPilotagem())) {
				xBase += 8;
				g2d.setColor(OcilaCor.geraOcila("miniPilotoNor", yel));
				g2d.fillRoundRect(xBase, yBase + 5, 7, 10, 3, 3);
				xBase += 8;
				g2d.setColor(gre);
				g2d.fillRoundRect(xBase, yBase + 10, 7, 5, 2, 2);
			} else if (Piloto.LENTO
					.equals(pilotoSelecionado.getModoPilotagem())) {
				xBase += 16;
				g2d.setColor(OcilaCor.geraOcila("miniPilotoMin", gre));
				g2d.fillRoundRect(xBase, yBase + 10, 7, 5, 2, 2);
			}
		}
		int valor = (c2.getRed() + c2.getGreen() + c2.getBlue()) / 2;
		if (valor > 250) {
			g2d.setColor(Color.BLACK);
		} else {
			g2d.setColor(Color.WHITE);
		}
		int xTxt = Util.inteiro(x - 19);
		if (!Util.isNullOrEmpty(txtDraw)) {
			g2d.drawString(txtDraw, xTxt + 28, Util.inteiro((y - 24)));
		}
		g2d.setColor(transpMenus);
		g2d.drawLine(Util.inteiro((x + 4)), Util.inteiro(y),
				Util.inteiro((x) + 28), Util.inteiro((y - 24)));
	}

	private void desenhaVelocidade(Graphics2D g2d) {
		if (pilotoSelecionado == null) {
			return;
		}
		if (!desenhaInfo) {
			return;
		}
		if (isExibeResultadoFinal()) {
			return;
		}

		Piloto ps = pilotoSelecionado;
		Stroke stroke = g2d.getStroke();

		if (qtdeLuzesAcesas > 0) {
			ps.setVelocidadeExibir(0);
			ps.setVelocidade(0);
		}
		int velocidade = ps.getVelocidadeExibir();

		if (qtdeLuzesAcesas > 0 || pilotoSelecionado.isDesqualificado()) {
			velocidade = 0;
		}

		String velo = "~" + velocidade + " Km/h";

		if (pilotoSelecionado.getPtosBox() != 0) {
			velo = " Box";
		}
		if (controleJogo.isSafetyCarNaPista()) {
			velo = " Safety Car ";
		}

		g2d.setColor(transpMenus);
		g2d.fillRoundRect(limitesViewPort.x + pointDesenhaVelo.x,
				limitesViewPort.y + pointDesenhaVelo.y + 35, 160, 35, 0, 0);
		Color corVelocidade = OcilaCor
				.porcentVermelho100Verde0((100 * velocidade / 330));
		g2d.setStroke(trilhoMiniPista);

		if (controleJogo.isSafetyCarNaPista()) {
			if (!controleJogo.getSafetyCar().isVaiProBox()) {
				g2d.setColor(OcilaCor.geraOcila("desenhaAjuda", yel));
			} else {
				g2d.setColor(transpMenus);
			}
		} else {
			g2d.setColor(corVelocidade);
		}
		g2d.drawRoundRect(limitesViewPort.x + pointDesenhaVelo.x,
				limitesViewPort.y + pointDesenhaVelo.y + 35, 160, 35, 0, 0);
		g2d.setStroke(stroke);
		Font fontOri = g2d.getFont();
		if (fontVelocidade == null) {
			fontVelocidade = new Font(fontOri.getName(), Font.BOLD, 28);
		}
		g2d.setFont(fontVelocidade);

		g2d.setColor(Color.BLACK);
		g2d.drawString(velo, limitesViewPort.x + pointDesenhaVelo.x + 2,
				limitesViewPort.y + pointDesenhaVelo.y + 62);
		g2d.setFont(fontOri);
	}

	private void desenhaBarrasDeGiro(Piloto ps, Graphics2D g2d) {
		if (!desenhaInfo) {
			return;
		}
		if (ps == null) {
			return;
		}
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

	private void desenhaBarrasPilotoCarro(Graphics2D g2d) {
		if (!desenhaInfo) {
			return;
		}
		if (pilotoSelecionado == null) {
			return;
		}
		if (isExibeResultadoFinal()) {
			return;
		}
		if (controleJogo.getNiveljogo() == InterfaceJogo.DIFICIL_NV
				&& !pilotoSelecionado.equals(controleJogo.getPilotoJogador())) {
			return;
		}
		Stroke stroke = g2d.getStroke();
		int x = limitesViewPort.x;
		int y = limitesViewPort.y + 165;
		Font fontOri = g2d.getFont();
		if (fonteBarrasPilotoCarro == null) {
			fonteBarrasPilotoCarro = new Font(fontOri.getName(), Font.BOLD, 28);
		}
		g2d.setFont(fonteBarrasPilotoCarro);
		int porcentComb = pilotoSelecionado.getCarro()
				.getPorcentagemCombustivel();
		Color corComb = OcilaCor.porcentVerde100Vermelho0(porcentComb);
		g2d.setColor(transpMenus);
		g2d.fillRoundRect(limitesViewPort.x + 5, y - 26, 2 * porcentComb, 30, 0,
				0);
		g2d.setStroke(trilhoMiniPista);
		g2d.setColor(corComb);
		g2d.drawRoundRect(limitesViewPort.x + 5, y - 26, 200, 30, 0, 0);
		g2d.setStroke(stroke);
		g2d.setColor(Color.BLACK);
		g2d.drawString(Lang.msg("215") + " " + porcentComb + "%", x + 5, y);
		y += 35;
		int pneus = pilotoSelecionado.getCarro().getPorcentagemDesgastePneus();
		Color corPneus = OcilaCor.porcentVerde100Vermelho0(pneus);
		g2d.setColor(transpMenus);
		g2d.fillRoundRect(limitesViewPort.x + 5, y - 26, 2 * pneus, 30, 0, 0);
		g2d.setStroke(trilhoMiniPista);
		g2d.setColor(corPneus);
		g2d.drawRoundRect(limitesViewPort.x + 5, y - 26, 200, 30, 0, 0);
		g2d.setStroke(stroke);
		g2d.setColor(Color.BLACK);
		g2d.drawString(Lang.msg("216") + " " + pneus + "%", x + 5, y);
		y += 35;
		int motor = pilotoSelecionado.getCarro().getPorcentagemDesgasteMotor();
		Color corMotor = OcilaCor.porcentVerde100Vermelho0(motor);
		g2d.setColor(transpMenus);
		g2d.fillRoundRect(limitesViewPort.x + 5, y - 26, 2 * motor, 30, 0, 0);
		g2d.setStroke(trilhoMiniPista);
		g2d.setColor(corMotor);
		g2d.drawRoundRect(limitesViewPort.x + 5, y - 26, 200, 30, 0, 0);
		g2d.setStroke(stroke);
		g2d.setColor(Color.BLACK);
		g2d.drawString(Lang.msg("217") + " " + motor + "%", x + 5, y);
		y += 35;
		int stress = pilotoSelecionado.getStress();
		Color corStress = OcilaCor.porcentVermelho100Verde0(stress);
		g2d.setColor(transpMenus);
		g2d.fillRoundRect(limitesViewPort.x + 5, y - 26, 2 * stress, 30, 10,
				10);
		g2d.setStroke(trilhoMiniPista);
		g2d.setColor(corStress);
		g2d.drawRoundRect(limitesViewPort.x + 5, y - 26, 200, 30, 0, 0);
		g2d.setStroke(stroke);
		g2d.setColor(Color.BLACK);
		g2d.drawString(Lang.msg("153") + " " + stress + "%", x + 5, y);
		g2d.setFont(fontOri);
	}

	private void desenhaDRS(Graphics2D g2d) {
		if (!(controleJogo.isDrs() && desenhaInfo)
				|| pilotoSelecionado == null) {
			return;
		}
		if (isExibeResultadoFinal()) {
			return;
		}
		if (controleJogo.getNiveljogo() == InterfaceJogo.DIFICIL_NV
				&& !pilotoSelecionado.equals(controleJogo.getPilotoJogador())) {
			return;
		}
		Font fontOri = g2d.getFont();
		if (fontDRS == null) {
			fontDRS = new Font(fontOri.getName(), Font.BOLD, fontOri.getSize());
		}
		g2d.setFont(fontDRS);
		if (Carro.MENOS_ASA.equals(pilotoSelecionado.getCarro().getAsa())) {
			g2d.setColor(gre);
		} else {
			if (pilotoSelecionado.isPodeUsarDRS()) {
				g2d.setColor(OcilaCor.geraOcila("podeUsarDRS", yel));
			} else {
				g2d.setColor(transpMenus);
			}
		}
		g2d.fillRoundRect(limitesViewPort.x + 170, limitesViewPort.y + 115, 34,
				15, 0, 0);
		g2d.setColor(Color.BLACK);
		g2d.drawString("DRS", limitesViewPort.x + 173, limitesViewPort.y + 127);
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
		int y = 175;
		g2d.fillRoundRect(limitesViewPort.x + inico,
				limitesViewPort.y + y - incremetAlt, 4, incremetAlt, 0, 0);
		incremetAlt += 3;
		g2d.fillRoundRect(limitesViewPort.x + inico + 5,
				limitesViewPort.y + y - incremetAlt, 4, incremetAlt, 0, 0);
		incremetAlt += 3;
		g2d.fillRoundRect(limitesViewPort.x + inico + 10,
				limitesViewPort.y + y - incremetAlt, 4, incremetAlt, 0, 0);
		incremetAlt += 3;
		if (varia) {
			int val = 1 + (int) (Math.random() * 3);
			switch (val) {
				case 1 :
					g2d.fillRoundRect(limitesViewPort.x + inico + 15,
							limitesViewPort.y + y - incremetAlt, 4, incremetAlt,
							0, 0);
					incremetAlt += 3;
					break;
				case 2 :
					g2d.fillRoundRect(limitesViewPort.x + inico + 15,
							limitesViewPort.y + y - incremetAlt, 4, incremetAlt,
							0, 0);
					incremetAlt += 3;
					g2d.fillRoundRect(limitesViewPort.x + inico + 20,
							limitesViewPort.y + y - incremetAlt, 4, incremetAlt,
							0, 0);
					incremetAlt += 3;
					break;
				case 3 :
					g2d.fillRoundRect(limitesViewPort.x + inico + 15,
							limitesViewPort.y + y - incremetAlt, 4, incremetAlt,
							0, 0);
					incremetAlt += 3;
					g2d.fillRoundRect(limitesViewPort.x + inico + 20,
							limitesViewPort.y + y - incremetAlt, 4, incremetAlt,
							0, 0);
					incremetAlt += 3;
					g2d.fillRoundRect(limitesViewPort.x + inico + 25,
							limitesViewPort.y + y - incremetAlt, 4, incremetAlt,
							0, 0);
					incremetAlt += 3;
					break;
				default :
					break;
			}
		} else {
			g2d.fillRoundRect(limitesViewPort.x + inico + 15,
					limitesViewPort.y + y - incremetAlt, 4, incremetAlt, 15,
					15);
			incremetAlt += 3;
			g2d.fillRoundRect(limitesViewPort.x + inico + 20,
					limitesViewPort.y + y - incremetAlt, 4, incremetAlt, 15,
					15);
			incremetAlt += 3;
			g2d.fillRoundRect(limitesViewPort.x + inico + 25,
					limitesViewPort.y + y - incremetAlt, 4, incremetAlt, 15,
					15);
			incremetAlt += 3;
		}

	}

	private void desenhaNomePilotoNaoSelecionado(Piloto ps, Graphics2D g2d) {
		int x = Util
				.inteiro(((ps.getCarX() - 2) - descontoCentraliza.x) * zoom);
		int y = Util
				.inteiro(((ps.getCarY() - 2) - descontoCentraliza.y) * zoom);
		g2d.setColor(ps.getCarro().getCor1());
		marcaCorPilotoJogador(g2d, ps);
		g2d.fillOval(x, y, 8, 8);
		g2d.setColor(new Color(ps.getCarro().getCor2().getRed(),
				ps.getCarro().getCor2().getGreen(),
				ps.getCarro().getCor2().getBlue(), 175));
		marcaCorPilotoJogador(g2d, ps);
		Stroke stroke = g2d.getStroke();
		g2d.setStroke(trilho);
		Point p2 = new Point(
				Util.inteiro((ps.getCarX() - 3 - descontoCentraliza.x) * zoom),
				Util.inteiro((ps.getCarY() - 3 - descontoCentraliza.y) * zoom));
		g2d.drawOval(Util.inteiro((p2.x)), Util.inteiro((p2.y)), 8, 8);
		g2d.setStroke(stroke);

		Color c2 = ps.getCarro().getCor2();
		if (c2 != null) {
			g2d.setColor(
					new Color(c2.getRed(), c2.getGreen(), c2.getBlue(), 100));
		}

		g2d.fillRoundRect(Util.inteiro((x) - 3), Util.inteiro((y) - 16),
				ps.getNome().length() * 7, 18, 0, 0);
		int valor = (c2.getRed() + c2.getGreen() + c2.getBlue()) / 2;
		if (valor > 250) {
			g2d.setColor(Color.BLACK);
		} else {
			g2d.setColor(Color.WHITE);
		}
		g2d.drawString(ps.getNome(), Util.inteiro((x) - 2),
				Util.inteiro((y) - 3));
	}

	private void setarHints(Graphics2D g2d) {
		if (efeitosLigados) {
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_ON);
			g2d.setRenderingHint(RenderingHints.KEY_DITHERING,
					RenderingHints.VALUE_DITHER_ENABLE);
			g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
					RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
			g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
					RenderingHints.VALUE_INTERPOLATION_BILINEAR);
			g2d.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING,
					RenderingHints.VALUE_COLOR_RENDER_QUALITY);
			g2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION,
					RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
			g2d.setRenderingHint(RenderingHints.KEY_RENDERING,
					RenderingHints.VALUE_RENDER_QUALITY);
		} else {
			g2d.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING,
					RenderingHints.VALUE_COLOR_RENDER_SPEED);
			g2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION,
					RenderingHints.VALUE_ALPHA_INTERPOLATION_SPEED);
			g2d.setRenderingHint(RenderingHints.KEY_RENDERING,
					RenderingHints.VALUE_RENDER_SPEED);
		}
	}

	public Dimension getPreferredSize() {
		return new Dimension(Util.inteiro((mx + 1000)),
				Util.inteiro((my + 1000)));
	}

	public void apagarLuz() {
		if (qtdeLuzesAcesas <= 0) {
			return;
		}
		if (qtdeLuzesAcesas <= 1) {
			setVerControles(false);
		}
		qtdeLuzesAcesas--;
	}

	public boolean isDesenhaInfo() {
		return desenhaInfo;
	}

	public void setDesenhaInfo(boolean desenhaPosVelo) {
		this.desenhaInfo = desenhaPosVelo;
	}

	public void adicionatrvadaRoda(TravadaRoda travadaRoda) {
		desenhaMarcasPneuPista(travadaRoda);
	}

	private void desenhaPista(Graphics2D g2d) {
		No oldNo = null;
		g2d.setColor(Color.LIGHT_GRAY);
		Stroke stroke = g2d.getStroke();
		g2d.setStroke(pista);
		for (Iterator iter = circuito.getPistaKey().iterator(); iter
				.hasNext();) {
			No no = (No) iter.next();
			if (oldNo == null) {
				oldNo = no;
			} else {
				g2d.drawLine(
						Util.inteiro(
								(oldNo.getX() - descontoCentraliza.x) * zoom),
						Util.inteiro(
								(oldNo.getY() - descontoCentraliza.y) * zoom),
						Util.inteiro((no.getX() - descontoCentraliza.x) * zoom),
						Util.inteiro(
								(no.getY() - descontoCentraliza.y) * zoom));

				oldNo = no;
			}
		}

		No noFinal = (No) circuito.getPistaKey().get(0);
		g2d.drawLine(Util.inteiro((oldNo.getX() - descontoCentraliza.x) * zoom),
				Util.inteiro((oldNo.getY() - descontoCentraliza.y) * zoom),
				Util.inteiro((noFinal.getX() - descontoCentraliza.x) * zoom),
				Util.inteiro((noFinal.getY() - descontoCentraliza.y) * zoom));
		g2d.setStroke(stroke);
	}

	private void desenhaPistaBox(Graphics2D g2d) {
		g2d.setColor(Color.LIGHT_GRAY);
		Stroke stroke = g2d.getStroke();
		g2d.setStroke(box);
		No oldNo = null;
		for (Iterator iter = circuito.getBoxKey().iterator(); iter.hasNext();) {
			No no = (No) iter.next();
			if (oldNo == null) {
				oldNo = no;
			} else {
				g2d.drawLine(
						Util.inteiro(
								(oldNo.getX() - descontoCentraliza.x) * zoom),
						Util.inteiro(
								(oldNo.getY() - descontoCentraliza.y) * zoom),
						Util.inteiro((no.getX() - descontoCentraliza.x) * zoom),
						Util.inteiro(
								(no.getY() - descontoCentraliza.y) * zoom));

				oldNo = no;
			}
		}
		No noFinal = (No) circuito.getBoxKey()
				.get(circuito.getBoxKey().size() - 1);

		g2d.drawLine(Util.inteiro((oldNo.getX() - descontoCentraliza.x) * zoom),
				Util.inteiro((oldNo.getY() - descontoCentraliza.y) * zoom),
				Util.inteiro((noFinal.getX() - descontoCentraliza.x) * zoom),
				Util.inteiro((noFinal.getY() - descontoCentraliza.y) * zoom));
		g2d.setStroke(stroke);
	}

	private void desenhaTintaPistaZebra(Graphics2D g2d) {
		g2d.setColor(Color.WHITE);
		Stroke stroke = g2d.getStroke();
		g2d.setStroke(pistaTinta);

		No oldNo = null;
		int cont = 0;
		for (Iterator iter = circuito.getPistaKey().iterator(); iter
				.hasNext();) {
			No no = (No) iter.next();
			if (oldNo == null) {
				oldNo = no;
			} else {
				g2d.setColor(Color.WHITE);
				g2d.setStroke(pistaTinta);
				g2d.drawLine(
						Util.inteiro(
								(oldNo.getX() - descontoCentraliza.x) * zoom),
						Util.inteiro(
								(oldNo.getY() - descontoCentraliza.y) * zoom),
						Util.inteiro((no.getX() - descontoCentraliza.x) * zoom),
						Util.inteiro(
								(no.getY() - descontoCentraliza.y) * zoom));
				if (No.CURVA_ALTA.equals(oldNo.getTipo())
						|| No.CURVA_BAIXA.equals(oldNo.getTipo())) {
					g2d.setColor(Color.RED);
					g2d.setStroke(zebra);
					g2d.drawLine(
							Util.inteiro((oldNo.getX() - descontoCentraliza.x)
									* zoom),
							Util.inteiro((oldNo.getY() - descontoCentraliza.y)
									* zoom),
							Util.inteiro(
									(no.getX() - descontoCentraliza.x) * zoom),
							Util.inteiro(
									(no.getY() - descontoCentraliza.y) * zoom));
				}
				oldNo = no;
			}
		}
		No noFinal = (No) circuito.getPistaKey().get(0);
		g2d.drawLine(Util.inteiro((oldNo.getX() - descontoCentraliza.x) * zoom),
				Util.inteiro((oldNo.getY() - descontoCentraliza.y) * zoom),
				Util.inteiro((noFinal.getX() - descontoCentraliza.x) * zoom),
				Util.inteiro((noFinal.getY() - descontoCentraliza.y) * zoom));
		g2d.setStroke(stroke);
	}

	private void desenhaBoxes(Graphics2D g2d) {
		if (limitesViewPort == null) {
			return;
		}
		if (translateBoxes == null)
			translateBoxes = new AffineTransform();
		translateBoxes.setToTranslation(-descontoCentraliza.x * zoom,
				-descontoCentraliza.y * zoom);
		for (int i = 0; i < 12; i++) {
			if (boxParada[i] == null) {
				break;
			}
			if (i > (controleJogo.getCarrosBox().size() - 1)) {
				break;
			}
			g2d.setColor(Color.LIGHT_GRAY);

			g2d.fill(translateBoxes.createTransformedShape(boxParada[i]));

			Carro carro = (Carro) controleJogo.getCarrosBox().get(i);
			g2d.setColor(carro.getCor1());
			g2d.fill(translateBoxes.createTransformedShape(boxCor1[i]));
			g2d.setColor(carro.getCor2());
			g2d.fill(translateBoxes.createTransformedShape(boxCor2[i]));
		}
	}

	private void gerarBoxes() {
		int paradas = circuito.getParadaBoxIndex();
		for (int i = 0; i < 12; i++) {
			int iP = paradas + Util.inteiro(Carro.LARGURA * 2 * i)
					+ Carro.LARGURA;
			int ip1 = iP - Carro.MEIA_LARGURA;
			if (ip1 >= circuito.getBoxFull().size()) {
				continue;
			}
			No n1 = (No) circuito.getBoxFull().get(ip1);
			int ip0 = iP;
			if (ip0 >= circuito.getBoxFull().size()) {
				continue;
			}
			No nM = (No) circuito.getBoxFull().get(ip0);
			int ip2 = iP + Carro.MEIA_LARGURA;
			if (ip2 >= circuito.getBoxFull().size()) {
				continue;
			}
			No n2 = (No) circuito.getBoxFull().get(ip2);
			Point p1 = new Point(Util.inteiro(n1.getPoint().x * zoom),
					Util.inteiro(n1.getPoint().y * zoom));
			Point pm = new Point(Util.inteiro(nM.getPoint().x * zoom),
					Util.inteiro(nM.getPoint().y * zoom));
			Point p2 = new Point(Util.inteiro(n2.getPoint().x * zoom),
					Util.inteiro(n2.getPoint().y * zoom));
			double calculaAngulo = GeoUtil.calculaAngulo(p1, p2, 0);
			if (rectangleGerarBoxes == null) {
				rectangleGerarBoxes = new Rectangle2D.Double(
						(pm.x - (Carro.MEIA_LARGURA)),
						(pm.y - (Carro.MEIA_ALTURA)), (Carro.LARGURA),
						(Carro.ALTURA));
			} else {
				rectangleGerarBoxes.setFrame((pm.x - (Carro.MEIA_LARGURA)),
						(pm.y - (Carro.MEIA_ALTURA)), (Carro.LARGURA),
						(Carro.ALTURA));
			}

			Point cima = GeoUtil.calculaPonto(calculaAngulo,
					Util.inteiro(
							Carro.ALTURA
									* controleJogo.getCircuito()
											.getMultiplicadorLarguraPista()
									* zoom),
					new Point(Util.inteiro(rectangleGerarBoxes.getCenterX()),
							Util.inteiro(rectangleGerarBoxes.getCenterY())));
			Point baixo = GeoUtil
					.calculaPonto(
							calculaAngulo + 180, Util
									.inteiro(Carro.ALTURA
											* controleJogo.getCircuito()
													.getMultiplicadorLarguraPista()
											* zoom),
							new Point(
									Util.inteiro(
											rectangleGerarBoxes.getCenterX()),
									Util.inteiro(
											rectangleGerarBoxes.getCenterY())));
			Point cimaBoxC1 = GeoUtil.calculaPonto(calculaAngulo,
					Util.inteiro((Carro.ALTURA) * 3.5 * zoom),
					new Point(Util.inteiro(rectangleGerarBoxes.getCenterX()),
							Util.inteiro(rectangleGerarBoxes.getCenterY())));
			Point baixoBoxC1 = GeoUtil.calculaPonto(calculaAngulo + 180,
					Util.inteiro((Carro.ALTURA) * 3.3 * zoom),
					new Point(Util.inteiro(rectangleGerarBoxes.getCenterX()),
							Util.inteiro(rectangleGerarBoxes.getCenterY())));
			Point cimaBoxC2 = GeoUtil.calculaPonto(calculaAngulo,
					Util.inteiro((Carro.ALTURA) * 3.5 * zoom),
					new Point(Util.inteiro(rectangleGerarBoxes.getCenterX()),
							Util.inteiro(rectangleGerarBoxes.getCenterY())));
			Point baixoBoxC2 = GeoUtil.calculaPonto(calculaAngulo + 180,
					Util.inteiro((Carro.ALTURA) * 3.3 * zoom),
					new Point(Util.inteiro(rectangleGerarBoxes.getCenterX()),
							Util.inteiro(rectangleGerarBoxes.getCenterY())));

			RoundRectangle2D retC1 = null;
			RoundRectangle2D retC2 = null;
			if (circuito.getLadoBox() == 1) {
				rectangleGerarBoxes = new Rectangle2D.Double(
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
						(Carro.LARGURA * zoom), (Carro.ALTURA * zoom), 0, 0);
			} else if (circuito.getLadoBox() == 2) {
				rectangleGerarBoxes = new Rectangle2D.Double(
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
						(Carro.LARGURA * zoom), (Carro.ALTURA * zoom), 0, 0);
			}

			GeneralPath generalPath = new GeneralPath(rectangleGerarBoxes);

			AffineTransform affineTransformRect = AffineTransform
					.getScaleInstance(zoom, zoom);
			double rad = Math.toRadians((double) calculaAngulo);
			affineTransformRect.setToRotation(rad,
					rectangleGerarBoxes.getCenterX(),
					rectangleGerarBoxes.getCenterY());
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

	public No getPosisRec() {
		return posisRec;
	}

	public void setPosisRec(No posisRec) {
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

	public Piloto getPilotoSelecionado() {
		return pilotoSelecionado;
	}

	public void setPilotoSelecionado(Piloto pilotoSelecionado) {
		this.pilotoSelecionado = pilotoSelecionado;
	}

	public boolean isExibeResultadoFinal() {
		return exibeResultadoFinal;
	}

	public void setExibeResultadoFinal(boolean exibeResultadoFinal) {
		this.exibeResultadoFinal = exibeResultadoFinal;
	}

	public boolean isVerControles() {
		return verControles;
	}

	public void setVerControles(boolean verControles) {
		this.verControles = verControles;
	}

	public boolean isDesenhouCreditos() {
		return desenhouCreditos;
	}

	public void setDesenhouCreditos(boolean desenhouCreditos) {
		this.desenhouCreditos = desenhouCreditos;
	}

	public void informaMudancaClima() {
		informaMudancaClima = 500;

	}

	public boolean desenhouPilotosQualificacao() {
		return contDesenhaPilotosQualify != null
				&& contDesenhaPilotosQualify <= 0;
	}

	public Point getDescontoCentraliza() {
		return descontoCentraliza;
	}

	public double getZoom() {
		return zoom;
	}

	public List<Piloto> getPilotosList() {
		return pilotosList;
	}

	public void setPilotosList(List<Piloto> pilotosList) {
		this.pilotosList = pilotosList;
	}

	public static void ligaDesligaEfeitos() {
		efeitosLigados = !efeitosLigados;
	}

}
