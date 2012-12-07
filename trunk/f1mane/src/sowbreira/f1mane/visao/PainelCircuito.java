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
import sowbreira.f1mane.entidades.ObjetoPista;
import sowbreira.f1mane.entidades.ObjetoTransparencia;
import sowbreira.f1mane.entidades.Piloto;
import sowbreira.f1mane.entidades.SafetyCar;
import sowbreira.f1mane.entidades.Volta;
import sowbreira.f1mane.paddock.applet.JogoCliente;
import sowbreira.f1mane.paddock.entidades.TOs.TravadaRoda;
import sowbreira.f1mane.recursos.CarregadorRecursos;
import sowbreira.f1mane.recursos.idiomas.Lang;
import br.nnpe.GeoUtil;
import br.nnpe.Html;
import br.nnpe.Logger;
import br.nnpe.OcilaCor;
import br.nnpe.Util;

/**
 * @author Paulo Sobreira
 */
public class PainelCircuito extends JPanel {
	private static final long serialVersionUID = -5268795362549996148L;
	private InterfaceJogo controleJogo;
	private GerenciadorVisual gerenciadorVisual;
	private Point pointDesenhaClima = new Point(10, 10);
	private Point pointDesenhaVelo = new Point(5, 60);
	private Point pointDesenhaSC = new Point(350, 15);
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
	public final static Color transpMenus = new Color(255, 255, 255, 100);
	public final static Color jogador = new Color(70, 140, 255, 180);
	public final static Color oran = new Color(255, 188, 40, 180);
	public final static Color transpSel = new Color(165, 165, 165, 165);

	public final static Color blu = new Color(105, 105, 105, 40);
	public final static Color lightWhite = new Color(255, 255, 255, 100);
	public final static Color lightWhiteRain = new Color(255, 255, 255, 160);
	public final static Color nublado = new Color(200, 200, 200, 100);
	public int indiceNublado = 0;
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
	private static DecimalFormat mil = new DecimalFormat("000");
	private int larguraPistaPixeis;
	private BasicStroke pista;
	private BasicStroke pistaTinta;
	private BasicStroke box;
	private BasicStroke zebra;
	private int qtdeLuzesAcesas = 5;
	private Piloto pilotQualificacao;
	private Point pointQualificacao;
	private Map mapDesenharQualificacao = new HashMap();
	private boolean desenhouQualificacao;
	private boolean desenhaInfo = true;
	private int mx;
	private int my;
	private double zoom = 1.0;
	private double mouseZoom = 1;
	public final static String zoomMutex = "zoomMutex";
	private Circuito circuito;
	private BasicStroke trilho = new BasicStroke(1.0f);
	private BasicStroke trilhoMiniPista = new BasicStroke(2.5f);
	private Shape[] grid = new Shape[24];
	private List gridImg = new ArrayList();
	private Shape[] asfaltoGrid = new Shape[24];
	private Shape[] boxParada = new Shape[12];
	private Shape[] boxCor1 = new Shape[12];
	private Shape[] boxCor2 = new Shape[12];
	private Rectangle limitesViewPort;
	private Set<TravadaRoda> marcasPneu = new HashSet<TravadaRoda>();
	private boolean inverterSpray;
	private Map<Piloto, Piloto> mapaFaiscas = new HashMap<Piloto, Piloto>();
	private Piloto pilotoSelecionado;
	private BufferedImage backGround;
	private Thread threadCarregarBkg;
	private List pistaMinimizada;
	private ArrayList boxMinimizado;
	protected Point newP;
	private Point oldP;
	private boolean alternaPiscaSCSair;

	private RoundRectangle2D f1 = new RoundRectangle2D.Double(0, 0, 1, 1, 10,
			10);
	private RoundRectangle2D f2 = new RoundRectangle2D.Double(0, 0, 1, 1, 10,
			10);
	private RoundRectangle2D f3 = new RoundRectangle2D.Double(0, 0, 1, 1, 10,
			10);
	private RoundRectangle2D f5 = new RoundRectangle2D.Double(0, 0, 1, 1, 10,
			10);
	private RoundRectangle2D f6 = new RoundRectangle2D.Double(0, 0, 1, 1, 10,
			10);
	private RoundRectangle2D f7 = new RoundRectangle2D.Double(0, 0, 1, 1, 10,
			10);
	private RoundRectangle2D kers = new RoundRectangle2D.Double(0, 0, 1, 1, 10,
			10);
	private RoundRectangle2D drs = new RoundRectangle2D.Double(0, 0, 1, 1, 10,
			10);

	private RoundRectangle2D[] pilotosRect;

	private RoundRectangle2D porcentCombustivelTela = new RoundRectangle2D.Double(
			0, 0, 1, 1, 10, 10);
	private RoundRectangle2D menosCombust = new RoundRectangle2D.Double(0, 0,
			1, 1, 10, 10);
	private RoundRectangle2D maisCombust = new RoundRectangle2D.Double(0, 0, 1,
			1, 10, 10);
	private RoundRectangle2D menosAsa = new RoundRectangle2D.Double(0, 0, 1, 1,
			10, 10);

	private RoundRectangle2D maisAsa = new RoundRectangle2D.Double(0, 0, 1, 1,
			10, 10);
	private RoundRectangle2D normalAsa = new RoundRectangle2D.Double(0, 0, 1,
			1, 10, 10);

	private RoundRectangle2D pneuMole = new RoundRectangle2D.Double(0, 0, 1, 1,
			10, 10);

	private RoundRectangle2D pneuDuro = new RoundRectangle2D.Double(0, 0, 1, 1,
			10, 10);
	private RoundRectangle2D pneuChuva = new RoundRectangle2D.Double(0, 0, 1,
			1, 10, 10);

	private RoundRectangle2D vaiBox = new RoundRectangle2D.Double(0, 0, 1, 1,
			10, 10);

	private int porcentCombust = 50;
	private String tpPneu;
	private String tpAsa;
	private double multiminiPista;
	private Point maiorP;
	private String infoComp;
	private int infoCompCont;

	private BufferedImage carroimgDano;
	private BufferedImage setaCarroCima;
	private BufferedImage setaCarroBaixo;
	private BufferedImage gridCarro;
	private BufferedImage scimg;
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
	private ImageIcon fuel;
	private ImageIcon tyre;
	private int acionaDesenhaKers;
	private int contMostraLag;

	public PainelCircuito(InterfaceJogo jogo,
			GerenciadorVisual gerenciadorVisual) {
		carregaRecursos();
		controleJogo = jogo;
		this.gerenciadorVisual = gerenciadorVisual;
		pilotosRect = new RoundRectangle2D.Double[controleJogo.getPilotos()
				.size()];

		for (int i = 0; i < pilotosRect.length; i++) {
			pilotosRect[i] = new RoundRectangle2D.Double(0, 0, 1, 1, 10, 10);
		}

		addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				controleJogo.getMainFrame().requestFocus();
				if (!verificaComando(e)) {
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
				e.printStackTrace();
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
		Logger.logar("Antes atualizaVarZoom");
		gerarGrid();

	}

	private void carregaRecursos() {
		carroimgDano = CarregadorRecursos
				.carregaBufferedImageTranspareciaBranca("CarroLadoDef.png", 225);
		setaCarroCima = CarregadorRecursos
				.carregaBufferedImageTranspareciaBranca("SetaCarroCima.png",
						200);
		setaCarroBaixo = CarregadorRecursos
				.carregaBufferedImageTranspareciaBranca("SetaCarroBaixo.png",
						200);
		gridCarro = CarregadorRecursos.carregaBufferedImageTranspareciaPreta(
				"GridCarro.png", 100);
		scimg = CarregadorRecursos
				.carregaBufferedImageTranspareciaBranca("safetycar.gif");
		scima = CarregadorRecursos
				.carregaBufferedImageTranspareciaBranca("sfcima.png");
		travadaRodaImg0 = CarregadorRecursos
				.carregaBufferedImageTranspareciaBranca("travadaRoda0.png",
						150, 100);
		travadaRodaImg1 = CarregadorRecursos
				.carregaBufferedImageTranspareciaBranca("travadaRoda1.png",
						150, 100);
		travadaRodaImg2 = CarregadorRecursos
				.carregaBufferedImageTranspareciaBranca("travadaRoda2.png",
						150, 100);
		carroCimaFreiosD1 = CarregadorRecursos
				.carregaBufferedImageTransparecia("CarroCimaFreiosD1.png", null);
		carroCimaFreiosD2 = CarregadorRecursos
				.carregaBufferedImageTransparecia("CarroCimaFreiosD2.png", null);
		carroCimaFreiosD3 = CarregadorRecursos
				.carregaBufferedImageTransparecia("CarroCimaFreiosD3.png", null);
		carroCimaFreiosD4 = CarregadorRecursos
				.carregaBufferedImageTransparecia("CarroCimaFreiosD4.png", null);
		carroCimaFreiosD5 = CarregadorRecursos
				.carregaBufferedImageTransparecia("CarroCimaFreiosD5.png", null);
		carroCimaFreiosE1 = CarregadorRecursos
				.carregaBufferedImageTransparecia("CarroCimaFreiosE1.png", null);
		carroCimaFreiosE2 = CarregadorRecursos
				.carregaBufferedImageTransparecia("CarroCimaFreiosE2.png", null);
		carroCimaFreiosE3 = CarregadorRecursos
				.carregaBufferedImageTransparecia("CarroCimaFreiosE3.png", null);
		carroCimaFreiosE4 = CarregadorRecursos
				.carregaBufferedImageTransparecia("CarroCimaFreiosE4.png", null);
		carroCimaFreiosE5 = CarregadorRecursos
				.carregaBufferedImageTransparecia("CarroCimaFreiosE5.png", null);
		fuel = new ImageIcon(CarregadorRecursos.carregarImagem("fuel.gif"));
		tyre = new ImageIcon(CarregadorRecursos.carregarImagem("tyre.gif"));

	}

	protected boolean verificaComando(MouseEvent e) {
		if (f1.contains(e.getPoint())) {
			controleJogo.mudarGiroMotor(Carro.GIRO_MIN);
			return true;
		}
		if (f2.contains(e.getPoint())) {
			controleJogo.mudarGiroMotor(Carro.GIRO_NOR);
			return true;
		}
		if (f3.contains(e.getPoint())) {
			controleJogo.mudarGiroMotor(Carro.GIRO_MAX);
			return true;
		}

		if (f5.contains(e.getPoint())) {
			controleJogo.mudarModoPilotagem(Piloto.LENTO);
			return true;
		}
		if (f6.contains(e.getPoint())) {
			controleJogo.mudarModoPilotagem(Piloto.NORMAL);
			return true;
		}
		if (f7.contains(e.getPoint())) {
			controleJogo.mudarModoPilotagem(Piloto.AGRESSIVO);
			return true;
		}

		if (kers.contains(e.getPoint())) {
			controleJogo.mudarModoKers();
			return true;
		}
		if (drs.contains(e.getPoint())) {
			controleJogo.mudarModoDRS();
			return true;
		}
		if (menosCombust.contains(e.getPoint()) && porcentCombust > 0) {
			if (pilotoSelecionado != null && pilotoSelecionado.isBox()) {
				controleJogo.mudarModoBox();
			}
			porcentCombust -= 10;
			return true;
		}

		if (maisCombust.contains(e.getPoint()) && porcentCombust < 100) {
			if (pilotoSelecionado != null && pilotoSelecionado.isBox()) {
				controleJogo.mudarModoBox();
			}
			porcentCombust += 10;
			return true;
		}

		if (maisAsa.contains(e.getPoint())) {
			if (pilotoSelecionado != null && pilotoSelecionado.isBox()) {
				controleJogo.mudarModoBox();
			}
			tpAsa = Carro.MAIS_ASA;
			return true;
		}
		if (normalAsa.contains(e.getPoint())) {
			if (pilotoSelecionado != null && pilotoSelecionado.isBox()) {
				controleJogo.mudarModoBox();
			}
			tpAsa = Carro.ASA_NORMAL;
			return true;
		}
		if (menosAsa.contains(e.getPoint())) {
			if (pilotoSelecionado != null && pilotoSelecionado.isBox()) {
				controleJogo.mudarModoBox();
			}
			tpAsa = Carro.MENOS_ASA;
			return true;
		}

		if (pneuMole.contains(e.getPoint())) {
			if (pilotoSelecionado != null && pilotoSelecionado.isBox()) {
				controleJogo.mudarModoBox();
			}
			tpPneu = Carro.TIPO_PNEU_MOLE;
			return true;
		}
		if (pneuDuro.contains(e.getPoint())) {
			if (pilotoSelecionado != null && pilotoSelecionado.isBox()) {
				controleJogo.mudarModoBox();
			}
			tpPneu = Carro.TIPO_PNEU_DURO;
			return true;
		}
		if (pneuChuva.contains(e.getPoint())) {
			if (pilotoSelecionado != null && pilotoSelecionado.isBox()) {
				controleJogo.mudarModoBox();
			}
			tpPneu = Carro.TIPO_PNEU_CHUVA;
			return true;
		}
		if (vaiBox.contains(e.getPoint())) {
			mudarModoBox();
		}

		for (int i = 0; i < pilotosRect.length; i++) {
			if (pilotosRect[i].contains(e.getPoint())) {
				pilotoSelecionado = controleJogo.getPilotos().get(i);
				controleJogo.selecionouPiloto(pilotoSelecionado);
				return true;
			}
		}

		return false;
	}

	public void mudarModoBox() {
		if (Util.isNullOrEmpty(tpAsa)) {
			tpAsa = Carro.ASA_NORMAL;
		}
		if (Util.isNullOrEmpty(tpPneu)) {
			tpPneu = Carro.TIPO_PNEU_DURO;
		}
		if (pilotoSelecionado != null && pilotoSelecionado.isBox()) {
			controleJogo.mudarModoBox();
		} else {
			controleJogo.setBoxJogadorHumano(tpPneu, porcentCombust, tpAsa);
			controleJogo.mudarModoBox();
		}
	}

	public void carregaBackGround() {
		if (!Logger.carregaBkg) {
			return;
		}
		if (!InterfaceJogo.VALENDO) {
			return;
		}
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
				if (Math.abs(mouseZoom - zoom) < 0.01) {
					zoom = mouseZoom;
				}
				if (mouseZoom > zoom) {
					zoom += 0.01;
					gerarGrid();
				}
				if (mouseZoom < zoom) {
					zoom -= 0.01;
					gerarGrid();
				}
				Graphics2D g2d = (Graphics2D) g;
				setarHints(g2d);
				limitesViewPort = (Rectangle) limitesViewPort();
				desenhaBackGround(g2d);
				desenhaContadorVoltas(g2d);
				if (controleJogo.getNumVoltaAtual() == 0
						&& !desenhouQualificacao) {
					desenhaQualificacao(g2d);
					return;
				}
				ControleSom.processaSom(pilotoSelecionado, controleJogo, this);
				desenhaGrid(g2d);
				iniciaPilotoSelecionado();
				desenhaMarcasPeneuPista(g2d);
				desenhaPiloto(g2d);
				desenhaBarrasPilotoCarro(g2d);
				desenharSafetyCar(g2d);
				desenharFarois(g2d);
				desenhaChuva(g2d);
				desenharClima(g2d);
				desenhaListaPilotos(g2d);
				desenhaLag(g2d);
				desenhaInfoPilotoSelecionado(g2d);
				desenhaMiniPista(g2d);
				desenhaNarracao(g2d);
				desenhaTabelaComparativa(g2d);
				desenhaControles(g2d);
				desenhaControlesBox(g2d);
				desenhaKers(g2d);
				desenhaDRS(g2d);
				desenhaVelocidade(g2d);

				desenhaNomePilotoSelecionado(pilotoSelecionado, g2d);
				desenhaNomePilotoSelecionadoCarroCima(pilotoSelecionado, g2d);
				desenhaCarrosLado(pilotoSelecionado, g2d);
				if (controleJogo.getNumVoltaAtual() > 0) {
					desenhaProblemasCarroSelecionado(pilotoSelecionado, g2d);
				}
				desenhaDebugIinfo(g2d);
			} catch (Exception e) {
				Logger.logarExept(e);
			}
		}

	}

	private void desenhaLag(Graphics2D g2d) {
		if (controleJogo.verificaLag()) {
			int largura = 0;
			String msg = "LAG";
			if (contMostraLag >= 0 && contMostraLag < 20) {
				int lag = controleJogo.getLag();
				if (lag > 9999) {
					lag = 9999;
				}
				msg = mil.format(controleJogo.getLag());
			} else if (contMostraLag > 20) {
				contMostraLag = -20;
			}

			contMostraLag++;
			for (int i = 0; i < msg.length(); i++) {
				largura += g2d.getFontMetrics().charWidth(msg.charAt(i));
			}

			Point pointDesenhaLag = new Point(limitesViewPort.x
					+ (limitesViewPort.width) - 75, Util.inte(limitesViewPort.y
					+ limitesViewPort.getHeight() - 90));
			g2d.setColor(transpMenus);
			g2d.fillRoundRect(pointDesenhaLag.x, pointDesenhaLag.y, 65, 35, 15,
					15);
			Font fontOri = g2d.getFont();
			g2d.setFont(new Font(fontOri.getName(), Font.BOLD, 28));
			g2d.setColor(OcilaCor.geraOcila("lag", red));
			g2d.drawString(msg, pointDesenhaLag.x + 2, pointDesenhaLag.y + 26);
			g2d.setFont(fontOri);
		}

	}

	private void desenhaNarracao(Graphics2D g2d) {
		if (!desenhaInfo) {
			return;
		}
		if (limitesViewPort == null) {
			return;
		}
		g2d.setColor(Color.LIGHT_GRAY);
		Point o = new Point(limitesViewPort.x + 5, limitesViewPort.y
				+ limitesViewPort.height - 195);
		g2d.setColor(transpMenus);
		g2d.fillRoundRect(o.x, o.y, 420, 110, 20, 20);
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
					if (info.contains("4682B4")) {
						g2d.setColor(new Color(0, 0, 121));
					}
					if (info.contains("FE0000")) {
						g2d.setColor(new Color(121, 0, 0));
					}
					if (info.contains("008D25")) {
						g2d.setColor(new Color(0, 80, 0));
					}
					info = Html.tagsJava2d(info);
					g2d.drawString("" + info, o.x + 4, o.y + (20 * (cont++)));
					g2d.setColor(Color.black);
				}
				indemax--;
				if (cont > 5) {
					break;
				}
			}
		}
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

		Point o = new Point(limitesViewPort.x + limitesViewPort.width - 580,
				limitesViewPort.y + limitesViewPort.height - 150);

		Color bkg = transpMenus;
		Color fonte = Color.black;

		RoundRectangle2D rectanglePos = new RoundRectangle2D.Double(o.x, o.y,
				100, 20, 15, 15);
		g2d.setColor(bkg);
		g2d.fill(rectanglePos);
		g2d.setColor(fonte);
		g2d.drawString(nmPilotoFrente, o.x + 5, o.y + 16);

		rectanglePos = new RoundRectangle2D.Double(o.x, o.y + 22, 100, 20, 15,
				15);
		g2d.setColor(bkg);
		g2d.fill(rectanglePos);
		g2d.setColor(fonte);
		g2d.drawString(nmPilotoTraz, o.x + 5, o.y + 38);

		rectanglePos = new RoundRectangle2D.Double(o.x + 102, o.y - 22, 100,
				20, 15, 15);
		g2d.setColor(bkg);
		g2d.fill(rectanglePos);
		g2d.setColor(fonte);
		g2d.drawString(volta1, o.x + 107, o.y - 8);

		rectanglePos = new RoundRectangle2D.Double(o.x + 102, o.y, 100, 20, 15,
				15);
		g2d.setColor(bkg);
		g2d.fill(rectanglePos);
		g2d.setColor(fonte);
		g2d.drawString(t1PilotoFrente, o.x + 117, o.y + 16);

		rectanglePos = new RoundRectangle2D.Double(o.x + 102, o.y + 22, 100,
				20, 15, 15);
		g2d.setColor(bkg);
		g2d.fill(rectanglePos);
		g2d.setColor(fonte);
		g2d.drawString(t1PilotoTraz, o.x + 117, o.y + 38);

		rectanglePos = new RoundRectangle2D.Double(o.x + 102, o.y + 44, 100,
				20, 15, 15);
		if (t1Diff.startsWith("-")) {
			g2d.setColor(gre);
		} else {
			g2d.setColor(yel);
		}
		g2d.fill(rectanglePos);
		g2d.setColor(fonte);
		g2d.drawString(t1Diff, o.x + 117, o.y + 60);

		rectanglePos = new RoundRectangle2D.Double(o.x + 204, o.y - 22, 100,
				20, 15, 15);
		g2d.setColor(bkg);
		g2d.fill(rectanglePos);
		g2d.setColor(fonte);
		g2d.drawString(volta2, o.x + 209, o.y - 8);

		rectanglePos = new RoundRectangle2D.Double(o.x + 204, o.y, 100, 20, 15,
				15);
		g2d.setColor(bkg);
		g2d.fill(rectanglePos);
		g2d.setColor(fonte);
		g2d.drawString(t2PilotoFrente, o.x + 219, o.y + 16);

		rectanglePos = new RoundRectangle2D.Double(o.x + 204, o.y + 22, 100,
				20, 15, 15);
		g2d.setColor(bkg);
		g2d.fill(rectanglePos);
		g2d.setColor(fonte);
		g2d.drawString(t2PilotoTraz, o.x + 219, o.y + 38);

		rectanglePos = new RoundRectangle2D.Double(o.x + 204, o.y + 44, 100,
				20, 15, 15);
		if (t2Diff.startsWith("-")) {
			g2d.setColor(gre);
		} else {
			g2d.setColor(yel);
		}
		g2d.fill(rectanglePos);
		g2d.setColor(fonte);
		g2d.drawString(t2Diff, o.x + 219, o.y + 60);

		rectanglePos = new RoundRectangle2D.Double(o.x + 306, o.y - 22, 100,
				20, 15, 15);
		g2d.setColor(bkg);
		g2d.fill(rectanglePos);
		g2d.setColor(fonte);
		g2d.drawString(volta3, o.x + 311, o.y - 8);

		rectanglePos = new RoundRectangle2D.Double(o.x + 306, o.y, 100, 20, 15,
				15);
		g2d.setColor(bkg);
		g2d.fill(rectanglePos);
		g2d.setColor(fonte);
		g2d.drawString(t3PilotoFrente, o.x + 321, o.y + 16);

		rectanglePos = new RoundRectangle2D.Double(o.x + 306, o.y + 22, 100,
				20, 15, 15);
		g2d.setColor(bkg);
		g2d.fill(rectanglePos);
		g2d.setColor(fonte);
		g2d.drawString(t3PilotoTraz, o.x + 321, o.y + 38);

		rectanglePos = new RoundRectangle2D.Double(o.x + 306, o.y + 44, 100,
				20, 15, 15);
		if (t3Diff.startsWith("-")) {
			g2d.setColor(gre);
		} else {
			g2d.setColor(yel);
		}
		g2d.fill(rectanglePos);
		g2d.setColor(fonte);
		g2d.drawString(t3Diff, o.x + 321, o.y + 60);
		infoCompCont--;
	}

	private void desenhaDebugIinfo(Graphics2D g2d) {

		if (!Logger.ativo) {
			return;
		}
		if (pilotoSelecionado == null) {
			return;
		}
		int ptoOri = limitesViewPort.x + 250;
		int yBase = limitesViewPort.y + 100;

		g2d.setColor(yel);
		g2d.fillRoundRect(ptoOri - 5, yBase - 12, 160, 15, 10, 10);
		g2d.setColor(Color.black);
		g2d.drawString("PtosPista() " + pilotoSelecionado.getPtosPista(),
				ptoOri, yBase);

		yBase += 20;

		g2d.setColor(yel);
		g2d.fillRoundRect(ptoOri - 5, yBase - 12, 160, 15, 10, 10);
		g2d.setColor(Color.black);
		g2d.drawString(
				"NovoModificador() " + pilotoSelecionado.getNovoModificador(),
				ptoOri, yBase);

		yBase += 20;

		g2d.setColor(yel);
		g2d.fillRoundRect(ptoOri - 5, yBase - 12, 160, 15, 10, 10);
		g2d.setColor(Color.black);
		g2d.drawString("Index() " + pilotoSelecionado.getNoAtual().getIndex(),
				ptoOri, yBase);

		yBase += 20;

		g2d.setColor(yel);
		g2d.fillRoundRect(ptoOri - 5, yBase - 12, 160, 15, 10, 10);
		g2d.setColor(Color.black);
		g2d.drawString(
				"Ganho() "
						+ Util.formatNumber("#,##0.00",
								pilotoSelecionado.getGanho()), ptoOri, yBase);

		yBase += 20;

		g2d.setColor(yel);
		g2d.fillRoundRect(ptoOri - 5, yBase - 12, 160, 15, 10, 10);
		g2d.setColor(Color.black);
		g2d.drawString(
				"Aceleracao() "
						+ Util.formatNumber("#,##0.00",
								pilotoSelecionado.getAceleracao()), ptoOri,
				yBase);

		yBase += 20;

		g2d.setColor(yel);
		g2d.fillRoundRect(ptoOri - 5, yBase - 12, 160, 15, 10, 10);
		g2d.setColor(Color.black);
		g2d.drawString("Stress() " + pilotoSelecionado.getStress(), ptoOri,
				yBase);

		yBase += 20;

		g2d.setColor(yel);
		g2d.fillRoundRect(ptoOri - 5, yBase - 12, 160, 15, 10, 10);
		g2d.setColor(Color.black);
		g2d.drawString("DurabilidadeAereofolio() "
				+ pilotoSelecionado.getCarro().getDurabilidadeAereofolio(),
				ptoOri, yBase);

		yBase += 20;

		g2d.setColor(yel);
		g2d.fillRoundRect(ptoOri - 5, yBase - 12, 160, 15, 10, 10);
		g2d.setColor(Color.black);
		g2d.drawString("getPtosBox() " + pilotoSelecionado.getPtosBox(),
				ptoOri, yBase);

		yBase += 20;

		g2d.setColor(yel);
		g2d.fillRoundRect(ptoOri - 5, yBase - 12, 160, 15, 10, 10);
		g2d.setColor(Color.black);
		g2d.drawString(
				"DiffParaProximo() "
						+ pilotoSelecionado
								.calculaDiffParaProximo(controleJogo), ptoOri,
				yBase);

		yBase += 20;

		g2d.setColor(yel);
		g2d.fillRoundRect(ptoOri - 5, yBase - 12, 160, 15, 10, 10);
		g2d.setColor(Color.black);
		g2d.drawString(
				"DiffParaAnterior() "
						+ pilotoSelecionado
								.calculaDiffParaAnterior(controleJogo), ptoOri,
				yBase);

		yBase += 20;

		g2d.setColor(yel);
		g2d.fillRoundRect(ptoOri - 5, yBase - 12, 160, 15, 10, 10);
		g2d.setColor(Color.black);
		g2d.drawString("CargaKers() "
				+ pilotoSelecionado.getCarro().getCargaKers(), ptoOri, yBase);

		yBase += 20;
		g2d.setColor(yel);
		g2d.fillRoundRect(ptoOri - 5, yBase - 12, 160, 15, 10, 10);
		g2d.setColor(Color.black);
		g2d.drawString(
				"%DeVoltaCompletada() "
						+ controleJogo
								.percetagemDeVoltaCompletada(pilotoSelecionado),
				ptoOri, yBase);

		yBase += 20;
		g2d.setColor(yel);
		g2d.fillRoundRect(ptoOri - 5, yBase - 12, 160, 15, 10, 10);
		g2d.setColor(Color.black);
		g2d.drawString("TemperaturaMotor() "
				+ pilotoSelecionado.getCarro().getTemperaturaMotor(), ptoOri,
				yBase);

		yBase += 20;
		g2d.setColor(yel);
		g2d.fillRoundRect(ptoOri - 5, yBase - 12, 160, 15, 10, 10);
		g2d.setColor(Color.black);
		g2d.drawString(
				"TempMax() " + pilotoSelecionado.getCarro().getTempMax(),
				ptoOri, yBase);

		yBase += 20;
		g2d.setColor(yel);
		g2d.fillRoundRect(ptoOri - 5, yBase - 12, 160, 15, 10, 10);
		g2d.setColor(Color.black);
		g2d.drawString(
				"IndiceTracado() " + pilotoSelecionado.getIndiceTracado(),
				ptoOri, yBase);

		yBase += 20;
		g2d.setColor(yel);
		g2d.fillRoundRect(ptoOri - 5, yBase - 12, 160, 15, 10, 10);
		g2d.setColor(Color.black);
		g2d.drawString("Tracado() " + pilotoSelecionado.getTracado(), ptoOri,
				yBase);

		yBase += 20;
		g2d.setColor(yel);
		g2d.fillRoundRect(ptoOri - 5, yBase - 12, 160, 15, 10, 10);
		g2d.setColor(Color.black);
		g2d.drawString(
				"TracadoAntigo() " + pilotoSelecionado.getTracadoAntigo(),
				ptoOri, yBase);

		yBase += 20;
		g2d.setColor(yel);
		g2d.fillRoundRect(ptoOri - 5, yBase - 12, 160, 15, 10, 10);
		g2d.setColor(Color.black);
		g2d.drawString("FreiandoReta() " + pilotoSelecionado.isFreiandoReta(),
				ptoOri, yBase);

		if (controleJogo.isSafetyCarNaPista()) {

			yBase += 20;
			g2d.setColor(yel);
			g2d.fillRoundRect(ptoOri - 5, yBase - 12, 160, 15, 10, 10);
			g2d.setColor(Color.black);
			g2d.drawString("Ptos SC  "
					+ controleJogo.getSafetyCar().getPtosPista(), ptoOri, yBase);

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
			if (!ps.equals(pilotoSelecionado)) {
				pilotoSelecionado = ps;
				controleJogo.getMainFrame().requestFocus();
			}
		}
	}

	private void desenhaControlesBox(Graphics2D g2d) {
		if (!controleJogo.isSemReabastacimento()) {
			Font fontOri = g2d.getFont();
			g2d.setFont(new Font(fontOri.getName(), Font.BOLD, 28));

			int x = limitesViewPort.x + (limitesViewPort.width / 2) - 20;
			int y = limitesViewPort.y + limitesViewPort.height - 25;
			g2d.setColor(transpMenus);
			String combst = "" + porcentCombust + "%";
			int tamCombust = Util.calculaLarguraText(combst, g2d);
			porcentCombustivelTela.setFrame(x - 15, y - 12, tamCombust + 5, 32);
			if (pilotoSelecionado != null && pilotoSelecionado.isBox()) {
				g2d.setColor(OcilaCor.geraOcila("selCombBox", transpSel));
				g2d.fill(porcentCombustivelTela);
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

			x = limitesViewPort.x + (limitesViewPort.width / 2) - 20;
			x += 72;

			String mais = "+";
			int tamMais = Util.calculaLarguraText(mais, g2d);
			maisCombust.setFrame(x - 17, y - 6, tamMais + 5, 22);
			g2d.draw(maisCombust);
			g2d.drawString(mais, x - 13, y + 16);

			g2d.setFont(fontOri);
		}
		if (!controleJogo.isDrs()) {
			desenhaControleAsaBox(g2d);
		}
		if (!controleJogo.isSemTrocaPneu()) {
			desenhaControlePneuBox(g2d);
		}
		desenhaComandoIrBox(g2d);
	}

	private void desenhaComandoIrBox(Graphics2D g2d) {
		int x = limitesViewPort.x + (limitesViewPort.width / 2) + 90;
		int y = limitesViewPort.y + 5;
		Font fontOri = g2d.getFont();
		g2d.setFont(new Font(fontOri.getName(), Font.BOLD, 28));

		String txtBox = Lang.msg("078");
		Color bg = transpMenus;
		if (pilotoSelecionado != null && pilotoSelecionado.isBox()) {
			bg = OcilaCor.geraOcila("vaiBox", transpSel);
			txtBox = Lang.msg("boxConfimado");
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

	private void desenhaControlePneuBox(Graphics2D g2d) {
		int x = limitesViewPort.x + (limitesViewPort.width / 2) + 80;
		int y = limitesViewPort.y + limitesViewPort.height - 25;

		boolean moleSel = false;
		if (pilotoSelecionado != null && qtdeLuzesAcesas <= 0
				&& Carro.TIPO_PNEU_MOLE.equals(tpPneu)) {
			if (pilotoSelecionado.isBox()) {
				g2d.setColor(OcilaCor.geraOcila("selPneuBox", transpSel));
			} else {
				g2d.setColor(transpSel);
			}
			moleSel = true;
		} else {
			g2d.setColor(transpMenus);
		}
		String strMole = Lang.msg("pneuMole");
		int tamMole = Util.calculaLarguraText(strMole, g2d);
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
		if (pilotoSelecionado != null && qtdeLuzesAcesas <= 0
				&& Carro.TIPO_PNEU_DURO.equals(tpPneu)) {
			if (pilotoSelecionado.isBox()) {
				g2d.setColor(OcilaCor.geraOcila("selPneuBox", transpSel));
			} else {
				g2d.setColor(transpSel);
			}
			duroSel = true;
		} else {
			g2d.setColor(transpMenus);
		}
		String strDuro = Lang.msg("pneuDuro");
		int tamDuro = Util.calculaLarguraText(strDuro, g2d);
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
		if (pilotoSelecionado != null && qtdeLuzesAcesas <= 0
				&& Carro.TIPO_PNEU_CHUVA.equals(tpPneu)) {
			if (pilotoSelecionado.isBox()) {
				g2d.setColor(OcilaCor.geraOcila("selPneuBox", transpSel));
			} else {
				g2d.setColor(transpSel);
			}
			chuvaSel = true;
		} else {
			g2d.setColor(transpMenus);
		}
		String strChuva = Lang.msg("pneuChuva");
		int tamChuva = Util.calculaLarguraText(strChuva, g2d);
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

	private void desenhaControleAsaBox(Graphics2D g2d) {
		int x = limitesViewPort.x + (limitesViewPort.width / 2) - 110;
		int y = limitesViewPort.y + limitesViewPort.height - 25;
		boolean mensoSel = false;
		if (pilotoSelecionado != null && qtdeLuzesAcesas <= 0
				&& Carro.MENOS_ASA.equals(tpAsa)) {
			if (pilotoSelecionado.isBox()) {
				g2d.setColor(OcilaCor.geraOcila("selAsaBox", transpSel));
			} else {
				g2d.setColor(transpSel);
			}
			mensoSel = true;
		} else {
			g2d.setColor(transpMenus);
		}
		String strMenos = Lang.msg("MENOS");
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

		x -= (tamMenos + 20);

		boolean norSel = false;
		if (pilotoSelecionado != null && qtdeLuzesAcesas <= 0
				&& Carro.ASA_NORMAL.equals(tpAsa)) {
			if (pilotoSelecionado.isBox()) {
				g2d.setColor(OcilaCor.geraOcila("selAsaBox", transpSel));
			} else {
				g2d.setColor(transpSel);
			}
			norSel = true;
		} else {
			g2d.setColor(transpMenus);
		}
		String strNormal = Lang.msg("NORMAL");
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

		x -= (tamNormal);
		boolean maisSel = false;
		if (pilotoSelecionado != null && qtdeLuzesAcesas <= 0
				&& Carro.MAIS_ASA.equals(tpAsa)) {
			if (pilotoSelecionado.isBox()) {
				g2d.setColor(OcilaCor.geraOcila("selAsaBox", transpSel));
			} else {
				g2d.setColor(transpSel);
			}
			maisSel = true;
		} else {
			g2d.setColor(transpMenus);
		}
		String strMais = Lang.msg("MAIS");
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
		if (limitesViewPort == null) {
			return;
		}

		desenhaControlesGiro(g2d);
		desenhaControlesPiloto(g2d);
	}

	private void desenhaControlesPiloto(Graphics2D g2d) {
		Point o = new Point(limitesViewPort.x + limitesViewPort.width - 100,
				limitesViewPort.y + limitesViewPort.height - 25);
		int x = o.x;
		int y = o.y;

		String strF5 = Lang.msg("075");
		int tamF5 = Util.calculaLarguraText(strF5, g2d);
		f5.setFrame(x, y, tamF5 + 10, 20);
		if (pilotoSelecionado != null
				&& Piloto.LENTO.equals(pilotoSelecionado.getModoPilotagem())
				&& qtdeLuzesAcesas <= 0) {
			g2d.setColor(transpSel);
			g2d.fill(f5);
			Stroke stroke = g2d.getStroke();
			g2d.setStroke(trilhoMiniPista);
			g2d.setColor(gre);
			g2d.draw(f5);
			g2d.setStroke(stroke);
		} else {
			g2d.setColor(transpMenus);
			g2d.fill(f5);
		}

		g2d.setColor(Color.black);
		g2d.drawString(strF5, x + 5, y + 16);

		desenhaControleKers(g2d, x, y, tamF5);

		x -= (tamF5);

		String strF6 = Lang.msg("076");
		int tamF6 = Util.calculaLarguraText(strF6, g2d);
		f6.setFrame(x, y, tamF6 + 10, 20);
		if (pilotoSelecionado != null
				&& Piloto.NORMAL.equals(pilotoSelecionado.getModoPilotagem())
				&& qtdeLuzesAcesas <= 0) {
			g2d.setColor(transpSel);
			g2d.fill(f6);
			Stroke stroke = g2d.getStroke();
			g2d.setStroke(trilhoMiniPista);
			g2d.setColor(yel);
			g2d.draw(f6);
			g2d.setStroke(stroke);
		} else {
			g2d.setColor(transpMenus);
			g2d.fill(f6);
		}

		g2d.setColor(Color.black);
		g2d.drawString(strF6, x + 5, y + 16);

		x -= (tamF6 + 30);

		String strF7 = Lang.msg("077");
		int tamF7 = Util.calculaLarguraText(strF7, g2d);
		f7.setFrame(x, y, tamF7 + 10, 20);
		if (pilotoSelecionado != null
				&& Piloto.AGRESSIVO
						.equals(pilotoSelecionado.getModoPilotagem())
				&& qtdeLuzesAcesas <= 0) {
			g2d.setColor(transpSel);
			g2d.fill(f7);
			Stroke stroke = g2d.getStroke();
			g2d.setStroke(trilhoMiniPista);
			g2d.setColor(red);
			g2d.draw(f7);
			g2d.setStroke(stroke);
		} else {
			g2d.setColor(transpMenus);
			g2d.fill(f7);
		}
		g2d.setColor(Color.black);
		g2d.drawString(strF7, x + 5, y + 16);
	}

	private void desenhaControleKers(Graphics2D g2d, int x, int y, int tamF5) {
		if (!controleJogo.isKers()) {
			return;
		}

		String msgKers = "K : " + Lang.msg("kers");

		int tamKers = Util.calculaLarguraText(msgKers, g2d);

		int xkers = x - (tamKers - tamF5);

		kers.setFrame(xkers, y - 25, tamKers + 10, 20);
		if (pilotoSelecionado != null
				&& pilotoSelecionado.getCarro().getCargaKers() > 0
				&& pilotoSelecionado.isAtivarKers()
				&& pilotoSelecionado.getCarro().getCargaKers() > 0) {
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
		Point o = new Point(limitesViewPort.x + 10, limitesViewPort.y
				+ limitesViewPort.height - 25);
		int x = o.x;
		int y = o.y;

		desenhaControleDrs(g2d, x, y);

		String strF1 = Lang.msg("071");
		int tamF1 = Util.calculaLarguraText(strF1, g2d);
		f1.setFrame(x, y, tamF1 + 10, 20);

		if (pilotoSelecionado != null
				&& Carro.GIRO_MIN_VAL == pilotoSelecionado.getCarro().getGiro()
				&& qtdeLuzesAcesas <= 0) {
			g2d.setColor(transpSel);
			g2d.fill(f1);
			Stroke stroke = g2d.getStroke();
			g2d.setStroke(trilhoMiniPista);
			g2d.setColor(gre);
			g2d.draw(f1);
			g2d.setStroke(stroke);
		} else {
			g2d.setColor(transpMenus);
			g2d.fill(f1);
		}

		g2d.setColor(Color.black);
		g2d.drawString(strF1, x + 5, y + 16);

		x += (tamF1 + 15);

		String strF2 = Lang.msg("072");
		int tamF2 = Util.calculaLarguraText(strF2, g2d);
		f2.setFrame(x, y, tamF2 + 10, 20);
		if (pilotoSelecionado != null
				&& Carro.GIRO_NOR_VAL == pilotoSelecionado.getCarro().getGiro()
				&& qtdeLuzesAcesas <= 0) {
			g2d.setColor(transpSel);
			g2d.fill(f2);
			Stroke stroke = g2d.getStroke();
			g2d.setStroke(trilhoMiniPista);
			g2d.setColor(yel);
			g2d.draw(f2);
			g2d.setStroke(stroke);
		} else {
			g2d.setColor(transpMenus);
			g2d.fill(f2);
		}

		g2d.setColor(Color.black);
		g2d.drawString(strF2, x + 5, y + 16);

		x += (tamF2 + 15);

		String strF3 = Lang.msg("073");
		int tamF3 = Util.calculaLarguraText(strF3, g2d);
		f3.setFrame(x, y, tamF3 + 10, 20);
		if (pilotoSelecionado != null
				&& Carro.GIRO_MAX_VAL == pilotoSelecionado.getCarro().getGiro()
				&& qtdeLuzesAcesas <= 0) {
			g2d.setColor(transpSel);
			g2d.fill(f3);
			Stroke stroke = g2d.getStroke();
			g2d.setStroke(trilhoMiniPista);
			g2d.setColor(red);
			g2d.draw(f3);
			g2d.setStroke(stroke);
		} else {
			g2d.setColor(transpMenus);
			g2d.fill(f3);
		}
		g2d.setColor(Color.black);
		g2d.drawString(strF3, x + 5, y + 16);
	}

	private void desenhaControleDrs(Graphics2D g2d, int x, int y) {
		if (!controleJogo.isDrs()) {
			return;
		}

		String msgDrs = "D : " + Lang.msg("drs");

		int tamDrs = Util.calculaLarguraText(msgDrs, g2d);
		drs.setFrame(x, y - 25, tamDrs + 10, 20);
		if (pilotoSelecionado != null
				&& Carro.MENOS_ASA
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
		if (!(controleJogo.isKers() && desenhaInfo && controleJogo
				.getNumVoltaAtual() > 0)) {
			return;
		}
		if (pilotoSelecionado == null) {
			return;
		}
		int cargaKers = pilotoSelecionado.getCarro().getCargaKers() / 2;
		int y = 100;
		g2d.setColor(red);
		g2d.fillRoundRect(limitesViewPort.x + 170, limitesViewPort.y + y, 20,
				50, 5, 5);
		g2d.setColor(gre);
		g2d.fillRoundRect(limitesViewPort.x + 170, limitesViewPort.y + y
				+ (50 - cargaKers), 20, cargaKers, 5, 5);

		if (pilotoSelecionado.getCargaKersVisual() != pilotoSelecionado
				.getCarro().getCargaKers()) {
			acionaDesenhaKers = 35;
		} else {
			g2d.setColor(Color.WHITE);
		}

		if (acionaDesenhaKers > 0) {
			acionaDesenhaKers--;
			g2d.setColor(OcilaCor.geraOcila("acionaDesenhaKers", Color.YELLOW));
			g2d.drawRoundRect(limitesViewPort.x + 170, limitesViewPort.y + y,
					20, 50, 5, 5);
		}

		pilotoSelecionado.setCargaKersVisual(pilotoSelecionado.getCarro()
				.getCargaKers());
		g2d.drawString("+", limitesViewPort.x + 177, limitesViewPort.y + y + 10);
		g2d.drawString("-", limitesViewPort.x + 178, limitesViewPort.y + y + 45);

	}

	private void desenhaBackGround(Graphics2D g2d) {
		if (backGround == null) {
			carregaBackGround();
		}
		if (backGround == null) {
			desenhaBackGroundComStrokes(g2d);
		} else {
			AffineTransform affineTransform = AffineTransform.getScaleInstance(
					zoom, zoom);
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
							Util.inte(limitesViewPort.getHeight() / zoom));
					if ((rectangle.x + rectangle.getWidth()) > backGround
							.getWidth()) {
						diffX = Util.inte((rectangle.x + rectangle.getWidth())
								- backGround.getWidth());
						rectangle.x -= diffX;
					}
					if ((rectangle.y + rectangle.getHeight()) > backGround
							.getHeight()) {
						diffY = Util.inte((rectangle.y + rectangle.getHeight())
								- backGround.getHeight());
						rectangle.y -= diffY;
					}
					subimage = backGround.getSubimage(rectangle.x, rectangle.y,
							rectangle.width, rectangle.height);
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

	private void desenhaBackGroundComStrokes(Graphics2D g2d) {
		int larguraPistaPixeisLoc = Util.inte(176
				* circuito.getMultiplicadorLarguraPista() * zoom);
		if (larguraPistaPixeisLoc != larguraPistaPixeis) {
			larguraPistaPixeis = larguraPistaPixeisLoc;
			pista = new BasicStroke(larguraPistaPixeis, BasicStroke.CAP_ROUND,
					BasicStroke.JOIN_ROUND);
			pistaTinta = new BasicStroke(Util.inte(larguraPistaPixeis * 1.05),
					BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
			box = new BasicStroke(Util.inte(larguraPistaPixeis * .4),
					BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
			zebra = new BasicStroke(Util.inte(larguraPistaPixeis * 1.05),
					BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10f,
					new float[] { 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10,
							10 }, 0);
			gerarBoxes();
		}
		desenhaTintaPistaEZebra(g2d);
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
		g2d.setColor(Color.LIGHT_GRAY);
		Point o = new Point(limitesViewPort.x + 5, limitesViewPort.y + 315);
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
			multiminiPista = (GeoUtil.distaciaEntrePontos(o, maiorP) / 100.0);
			if (multiminiPista < 30) {
				multiminiPista = 30;
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
		for (Iterator iterator = pistaMinimizada.iterator(); iterator.hasNext();) {
			Point p = (Point) iterator.next();
			if (oldP != null) {
				g2d.drawLine(o.x + oldP.x, o.y + oldP.y, o.x + p.x, o.y + p.y);
			}
			oldP = p;
		}
		g2d.setStroke(stroke);
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
		g2d.setStroke(stroke);
		Piloto lider = (Piloto) controleJogo.getPilotos().get(0);
		Font fontOri = g2d.getFont();
		g2d.setFont(new Font(fontOri.getName(), fontOri.getStyle(), 8));

		List pilotos = controleJogo.getPilotos();
		for (Iterator iterator = pilotos.iterator(); iterator.hasNext();) {
			Piloto piloto = (Piloto) iterator.next();
			Point point = piloto.getNoAtual().getPoint();
			Point p = new Point(point.x, point.y);
			p.x /= multiminiPista;
			p.y /= multiminiPista;
			if (piloto.equals(pilotoSelecionado)) {
				g2d.setColor(jogador);
			} else if (piloto.equals(lider)) {
				g2d.setColor(oran);
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
			g2d.fillOval(
					o.x + Util.inte(posisRec.getPoint().x / multiminiPista),
					o.y + Util.inte(posisRec.getPoint().y / multiminiPista),
					Util.inte(5 * zoom), Util.inte(5 * zoom));
		}
		if (controleJogo.isSafetyCarNaPista()) {
			SafetyCar safetyCar = controleJogo.getSafetyCar();
			Point point = safetyCar.getNoAtual().getPoint();
			Point p = new Point(point.x, point.y);
			p.x /= multiminiPista;
			p.y /= multiminiPista;
			g2d.setColor(lightRed);
			if (!controleJogo.isSafetyCarVaiBox()) {
				if (alternaPiscaSCSair) {
					g2d.setColor(yel);
				}
				alternaPiscaSCSair = !alternaPiscaSCSair;
			}
			g2d.fillOval(o.x + p.x - 5, o.y + p.y - 5, 10, 10);
			g2d.setColor(Color.BLACK);
			g2d.drawString("sc", (o.x + p.x) - 6, o.y + p.y + 3);
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
				int width = (int) (travadaRodaImg0.getWidth());
				int height = (int) (travadaRodaImg0.getHeight());
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
				switch (travadaRoda.getTipo()) {
				case 0:
					op.filter(travadaRodaImg0, zoomBuffer);
					break;
				case 1:
					op.filter(travadaRodaImg1, zoomBuffer);
					break;
				case 2:
					op.filter(travadaRodaImg2, zoomBuffer);
					break;
				default:
					break;
				}

				AffineTransformOp op2 = new AffineTransformOp(afZoom,
						AffineTransformOp.TYPE_BILINEAR);
				op2.filter(zoomBuffer, rotateBuffer);
				g2d.drawImage(rotateBuffer, Util.inte(carx * zoom),
						Util.inte(cary * zoom), null);

			}
		}
	}

	public double getMouseZoom() {
		return mouseZoom;
	}

	public void setMouseZoom(double mouseZoom) {
		this.mouseZoom = mouseZoom;
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
			desenhaCarroCima(g2d, piloto);
			piloto.centralizaDianteiraTrazeiraCarro(controleJogo);
			if (Logger.ativo || !Logger.carregaBkg) {
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

	private void desenhaCarroCima(Graphics2D g2d, Piloto piloto) {
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
		Stroke stroke = g2d.getStroke();
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

		boolean rabeadaAgressivo = piloto.isAgressivo()
				&& piloto.getCarro().getGiro() == Carro.GIRO_MAX_VAL
				&& (piloto.getNoAtual().verificaCruvaAlta() || piloto
						.getNoAtual().verificaCruvaBaixa())
				&& Math.random() > .5;
		boolean rabeadaPneuErrado = piloto.getCarro()
				.verificaPneusIncompativeisClima(controleJogo)
				&& Math.random() > .5;

		if (rabeadaAgressivo || rabeadaPneuErrado) {
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
		if (circuito.getObjetos() != null) {
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

		if (Logger.carregaBkg)
			g2d.drawImage(rotateBuffer, carroX, carroY, null);
		if (naoDesenhaEfeitos) {
			return;
		}
		desenhaTravaRodaCarroCima(g2d, piloto, width, height, carx, cary,
				afZoom, afRotate);
		desenhaChuvaFaiscasCarroCima(g2d, piloto, width);
		desenhaAjudaPistaCarroCima(g2d, piloto);
		desenhaDebugCarroCima(g2d, piloto, noAtual, rectangle, rad);
		g2d.setStroke(stroke);

	}

	private void desenhaTravaRodaCarroCima(Graphics2D g2d, Piloto piloto,
			int width, int height, int carx, int cary, AffineTransform afZoom,
			AffineTransform afRotate) {
		/**
		 * Travada Roda
		 */
		if (piloto.decContTravouRodas() && Math.random() > 0.7) {

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
	}

	private void desenhaChuvaFaiscasCarroCima(Graphics2D g2d, Piloto piloto,
			int width) {
		/**
		 * Chuva e Faiscas
		 */
		if (piloto.getDiateira() == null || piloto.getCentro() == null
				|| piloto.getTrazeira() == null) {
			piloto.centralizaDianteiraTrazeiraCarro(controleJogo);
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
		double qtdeGotas = indiceNublado / 2000.0;
		if ((controleJogo.isChovendo() || (Clima.NUBLADO.equals(controleJogo
				.getClima())))
				&& piloto.getVelocidade() != 0
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

				Point destN = GeoUtil
						.calculaPonto(
								GeoUtil.calculaAngulo(origem, dest, 90),
								(int) (Util.intervalo(width * .25, width * max) * qtdeGotas),
								origem);

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
	}

	private void desenhaAjudaPistaCarroCima(Graphics2D g2d, Piloto piloto) {
		/**
		 * Desenha Ajuda Pista
		 */
		if (piloto.equals(pilotoSelecionado) && posisRec != null) {
			if (posisRec.verificaRetaOuLargada()) {
				g2d.setColor(new Color(100, 255, 100, 70));
			} else if (posisRec.verificaCruvaAlta()) {
				g2d.setColor(new Color(255, 255, 100, 70));
			} else if (posisRec.verificaCruvaBaixa()) {
				g2d.setColor(new Color(255, 100, 100, 70));
			} else {
				g2d.setColor(new Color(100, 100, 100, 70));
			}
			Point frenteCarD = posisRec.getPoint();
			g2d.fillOval(Util.inte((frenteCarD.x - 5) * zoom),
					Util.inte((frenteCarD.y - 5) * zoom), Util.inte(15 * zoom),
					Util.inte(15 * zoom));
		}
	}

	private void desenhaDebugCarroCima(Graphics2D g2d, Piloto piloto,
			No noAtual, Rectangle2D rectangle, double rad) {
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
				g2d.fillOval(Util.inte(posisRec.getPoint().x * zoom),
						Util.inte(posisRec.getPoint().y * zoom),
						Util.inte(5 * zoom), Util.inte(5 * zoom));
			}
			int indexAtual = noAtual.getIndex();
			if (indexAtual + 100 < (controleJogo.getNosDaPista().size() - 1)) {
				g2d.setColor(Color.YELLOW);
				No no = controleJogo.getNosDaPista().get(indexAtual + 100);
				g2d.fillOval(Util.inte(no.getX() * zoom),
						Util.inte(no.getY() * zoom), Util.inte(5 * zoom),
						Util.inte(5 * zoom));
			}

		}
	}

	private void desenhaFumacaTravarRodas(int width, int height,
			AffineTransform afRotate, AffineTransform afZoom, double carx,
			double cary, Graphics g2d, BufferedImage img) {
		BufferedImage rotateBuffer = new BufferedImage(width, width,
				BufferedImage.TYPE_INT_ARGB);
		BufferedImage zoomBuffer = new BufferedImage(width, height,
				BufferedImage.TYPE_INT_ARGB);
		AffineTransformOp op = new AffineTransformOp(afRotate,
				AffineTransformOp.TYPE_BILINEAR);
		op.filter(img, zoomBuffer);
		AffineTransformOp op2 = new AffineTransformOp(afZoom,
				AffineTransformOp.TYPE_BILINEAR);
		op2.filter(zoomBuffer, rotateBuffer);
		g2d.drawImage(rotateBuffer, Util.inte(carx * zoom),
				Util.inte(cary * zoom), null);

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
				/*
				 * Grid
				 */
				boolean naoDesenha = false;
				if (circuito.getObjetos() != null) {
					for (Iterator iterator = circuito.getObjetos().iterator(); iterator
							.hasNext();) {
						ObjetoPista objetoPista = (ObjetoPista) iterator.next();
						if (objetoPista instanceof ObjetoTransparencia) {
							ObjetoTransparencia objetoTransparencia = (ObjetoTransparencia) objetoPista;
							if (objetoTransparencia.obterArea().intersects(
									grid[i].getBounds())) {
								naoDesenha = true;
								break;
							}
							Point borda = new Point((int) (grid[i].getBounds()
									.getCenterX() - meix), (int) (grid[i]
									.getBounds().getCenterY() - meiy));
							if (objetoTransparencia.obterArea().contains(borda)) {
								naoDesenha = true;
								break;
							}
						} else {
							continue;
						}
					}
				}
				if (!naoDesenha)
					g2d.drawImage(buffer, (int) (grid[i].getBounds()
							.getCenterX() - meix), (int) (grid[i].getBounds()
							.getCenterY() - meiy), null);
			} else {
				g2d.setColor(Color.white);
				g2d.fill(grid[i]);
				g2d.setColor(Color.lightGray);
				g2d.fill(asfaltoGrid[i]);
			}

		}

	}

	public void gerarGrid() {
		synchronized (gridImg) {
			gridImg.clear();
			for (int i = 0; i < 24; i++) {
				int iP = 50 + Util.inte((Carro.LARGURA * 0.8) * i);
				No n1 = (No) circuito.getPistaFull().get(
						circuito.getPistaFull().size() - iP
								- Carro.MEIA_LARGURA);
				No nM = (No) circuito.getPistaFull().get(
						circuito.getPistaFull().size() - iP);
				No n2 = (No) circuito.getPistaFull().get(
						circuito.getPistaFull().size() - iP
								+ Carro.MEIA_LARGURA);
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

				Point cima = GeoUtil
						.calculaPonto(calculaAngulo,
								Util.inte(Carro.ALTURA
										* controleJogo.getCircuito()
												.getMultiplicadorLarguraPista()
										* zoom),
								new Point(Util.inte(rectangle.getCenterX()),
										Util.inte(rectangle.getCenterY())));
				Point baixo = GeoUtil
						.calculaPonto(calculaAngulo + 180, Util
								.inte(Carro.ALTURA
										* controleJogo.getCircuito()
												.getMultiplicadorLarguraPista()
										* zoom),
								new Point(Util.inte(rectangle.getCenterX()),
										Util.inte(rectangle.getCenterY())));
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
					BufferedImage rotateBuffer = new BufferedImage(width,
							height, BufferedImage.TYPE_INT_ARGB);
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
						circuito.getPistaFull().size() - iP
								- Carro.MEIA_LARGURA);
				nM = (No) circuito.getPistaFull().get(
						circuito.getPistaFull().size() - iP);
				n2 = (No) circuito.getPistaFull().get(
						circuito.getPistaFull().size() - iP
								+ Carro.MEIA_LARGURA);
				p1 = new Point(Util.inte(n1.getPoint().x * zoom), Util.inte(n1
						.getPoint().y * zoom));
				pm = new Point(Util.inte(nM.getPoint().x * zoom), Util.inte(nM
						.getPoint().y * zoom));
				p2 = new Point(Util.inte(n2.getPoint().x * zoom), Util.inte(n2
						.getPoint().y * zoom));
				calculaAngulo = GeoUtil.calculaAngulo(p1, p2, 0);
				rectangle = new Rectangle2D.Double(
						(pm.x - (Carro.MEIA_LARGURA)),
						(pm.y - (Carro.MEIA_ALTURA)), (Carro.LARGURA),
						(Carro.ALTURA));

				cima = GeoUtil
						.calculaPonto(calculaAngulo,
								Util.inte(Carro.ALTURA
										* controleJogo.getCircuito()
												.getMultiplicadorLarguraPista()
										* zoom),
								new Point(Util.inte(rectangle.getCenterX()),
										Util.inte(rectangle.getCenterY())));
				baixo = GeoUtil
						.calculaPonto(calculaAngulo + 180, Util
								.inte(Carro.ALTURA
										* controleJogo.getCircuito()
												.getMultiplicadorLarguraPista()
										* zoom),
								new Point(Util.inte(rectangle.getCenterX()),
										Util.inte(rectangle.getCenterY())));
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

				affineTransformRect = AffineTransform.getScaleInstance(zoom,
						zoom);
				rad = Math.toRadians((double) calculaAngulo);
				affineTransformRect.setToRotation(rad, rectangle.getCenterX(),
						rectangle.getCenterY());
				asfaltoGrid[i] = generalPath
						.createTransformedShape(affineTransformRect);
			}
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

	private void desenhaChuva(Graphics2D g2d) {
		if (Clima.NUBLADO.equals(controleJogo.getClima())) {
			if (Math.random() > 0.7) {
				indiceNublado++;
				if (indiceNublado > 1000) {
					indiceNublado = 1000;
				}
			}
			int alfaNub = indiceNublado / 10;
			g2d.setColor(new Color(nublado.getRed(), nublado.getGreen(),
					nublado.getBlue(), alfaNub));
			g2d.fill(limitesViewPort().getBounds());
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
			g2d.fill(limitesViewPort().getBounds());
		}

		if ((Clima.CHUVA.equals(controleJogo.getClima()))
				&& limitesViewPort() != null) {
			indiceNublado += 10;
			if (indiceNublado > 2000) {
				indiceNublado = 2000;
			}
			int alfaNub = indiceNublado / 20;
			g2d.setColor(new Color(nublado.getRed(), nublado.getGreen(),
					nublado.getBlue(), alfaNub));
			g2d.fill(limitesViewPort().getBounds());
		}
		if (Clima.SOL.equals(controleJogo.getClima()))
			return;
		Point p1 = new Point(0, 0);
		Point p2 = new Point(0, 0);
		g2d.setColor(lightWhiteRain);
		double qtdeGotas = indiceNublado / 2000.0;
		// if (controleJogo.isChovendo()) {
		// qtdeGotas = 0;
		// }
		if (Math.random() > qtdeGotas) {
			return;
		}
		for (int i = 0; i < limitesViewPort.getWidth(); i += 20) {
			if (Math.random() > qtdeGotas) {
				continue;
			}
			for (int j = 0; j < limitesViewPort.getHeight(); j += 20) {
				if (Math.random() > qtdeGotas) {
					continue;
				}
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

	private void desenhaListaPilotos(Graphics2D g2d) {
		if (!desenhaInfo) {
			return;
		}
		int x = limitesViewPort.x + limitesViewPort.width - 165;
		int y = limitesViewPort.y + 5;

		List<Piloto> pilotos = controleJogo.getPilotos();
		int tamNome = 90;
		for (int i = 0; i < pilotos.size(); i++) {
			Piloto piloto = pilotos.get(i);
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
			}
			RoundRectangle2D rectanglePos = new RoundRectangle2D.Double(x, y,
					25, 20, 15, 15);
			g2d.setColor(bkg);
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

			RoundRectangle2D rectangleVol = new RoundRectangle2D.Double(x + 35
					+ pilotosRect[i].getWidth(), y, 25, 20, 15, 15);
			g2d.setColor(bkg);
			g2d.fill(rectangleVol);
			g2d.setColor(fonte);
			g2d.drawString("" + piloto.getNumeroVolta(),
					Util.inte(x + 40 + pilotosRect[i].getWidth()), y + 16);

			if (piloto.equals(pilotoSelecionado)) {
				g2d.setColor(yel);
				Stroke stroke = g2d.getStroke();
				g2d.setStroke(trilhoMiniPista);
				g2d.draw(rectanglePos);
				g2d.draw(pilotosRect[i]);
				g2d.draw(rectangleVol);
				g2d.setStroke(stroke);
			}

			y += 23;
		}

	}

	private void desenhaInfoPilotoSelecionado(Graphics2D g2d) {
		if (pilotoSelecionado == null) {
			return;
		}
		if (!desenhaInfo) {
			return;
		}
		if (controleJogo.getNumVoltaAtual() < 1) {
			return;
		}

		int ptoOri = limitesViewPort.x + 10;
		int yBase = limitesViewPort.y + 7;

		g2d.setColor(transpMenus);
		g2d.fillRoundRect(ptoOri - 5, yBase - 2, 105, 90, 10, 10);
		g2d.setColor(Color.black);
		yBase += 10;
		g2d.drawString(Lang.msg(pilotoSelecionado.getCarro().getTipoPneu()),
				ptoOri, yBase);

		yBase += 15;
		g2d.drawString(Lang.msg(pilotoSelecionado.getCarro().getAsa()), ptoOri,
				yBase);
		yBase += 15;
		g2d.drawString(Lang.msg("068") + pilotoSelecionado.getQtdeParadasBox(),
				ptoOri, yBase);
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
			controleJogo.calculaSegundosParaLider(pilotoSelecionado);
			plider = pilotoSelecionado.getSegundosParaLider();
		}
		yBase += 15;
		g2d.setColor(Color.black);
		g2d.drawString(Lang.msg(controleJogo.getNivelCorrida()), ptoOri, yBase);

		yBase += 15;
		g2d.setColor(Color.black);
		g2d.drawString(Lang.msg("074"), ptoOri, yBase);
		yBase += 15;
		if (!pilotoSelecionado.isAutoPos()) {
			g2d.setColor(OcilaCor.geraOcila("tracadoManual", yel));
			g2d.fillRoundRect(ptoOri - 5, yBase - 12, 105, 16, 10, 10);
			g2d.setColor(Color.black);
			g2d.drawString(Lang.msg("tracadoManual"), ptoOri, yBase);
		} else {
			g2d.drawString(Lang.msg("tracadoAutomatico"), ptoOri, yBase);
		}
		ptoOri = limitesViewPort.x + limitesViewPort.width - 275;
		yBase = limitesViewPort.y + 7;

		g2d.setColor(transpMenus);
		g2d.fillRoundRect(ptoOri - 5, yBase - 2, 105, 75, 10, 10);

		yBase += 10;
		g2d.setColor(Color.black);
		g2d.drawString(Lang.msg("220"), ptoOri, yBase);

		yBase += 15;
		String msg = "F10 : " + Lang.msg("som")
				+ (ControleSom.somLigado ? Lang.msg("SIM") : Lang.msg("NAO"));
		g2d.drawString(msg, ptoOri, yBase);

		yBase += 15;
		g2d.drawString(Lang.msg("265"), ptoOri, yBase);
		yBase += 15;
		g2d.drawString(
				Lang.msg("081") + ": " + pilotoSelecionado.getNumeroVolta(),
				ptoOri, yBase);
		yBase += 15;
		g2d.drawString(
				(controleJogo.verificaCampeonatoComRival() ? Lang.msg("rival")
						: Lang.msg("070")) + plider, ptoOri, yBase);

		if ((pilotoSelecionado.getNumeroVolta() > 1)) {
			Volta voltaPiloto = controleJogo
					.obterMelhorVolta(pilotoSelecionado);
			if (voltaPiloto != null) {
				yBase += 17;
				g2d.setColor(transpMenus);
				g2d.fillRoundRect(ptoOri - 5, yBase - 12, 105, 16, 10, 10);
				g2d.setColor(new Color(0, 0, 111));
				g2d.drawString(
						Lang.msg("079")
								+ voltaPiloto.obterTempoVoltaFormatado(),
						ptoOri, yBase);
			}

			Volta voltaCorrida = controleJogo.obterMelhorVolta();
			if (voltaCorrida != null) {
				yBase += 17;
				g2d.setColor(transpMenus);
				g2d.fillRoundRect(ptoOri - 5, yBase - 12, 105, 16, 10, 10);
				g2d.setColor(Color.black);
				g2d.drawString(
						Lang.msg("290") + ":"
								+ voltaCorrida.obterTempoVoltaFormatado(),
						ptoOri, yBase);
			}

			yBase += 17;
			g2d.setColor(transpMenus);
			g2d.fillRoundRect(ptoOri - 5, yBase - 12, 105, 16, 10, 10);
			g2d.setColor(Color.black);
			g2d.drawString(Lang.msg("080"), ptoOri, yBase);

			yBase += 17;
			int contAlt = yBase;
			int contVolta = 1;
			List voltas = pilotoSelecionado.getVoltas();
			int cont = 1;
			for (int i = voltas.size() - 1; i > -1; i--) {
				Volta volta = (Volta) voltas.get(i);
				if (volta.obterTempoVolta() == 0) {
					continue;
				}
				g2d.setColor(transpMenus);
				g2d.fillRoundRect(ptoOri - 5, contAlt - 12, 105, 16, 10, 10);
				g2d.setColor(Color.black);
				g2d.drawString(pilotoSelecionado.getNumeroVolta() - (cont++)
						+ " - " + volta.obterTempoVoltaFormatado(), ptoOri,
						contAlt);
				contAlt += 17;
				contVolta++;
				if (contVolta > 5) {
					break;
				}
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
		if (pilotoSelecionado == null) {
			return;
		}
		int pneus = pilotoSelecionado.getCarro().porcentagemDesgastePeneus();
		int porcentComb = pilotoSelecionado.getCarro().porcentagemCombustivel();
		int motor = pilotoSelecionado.getCarro().porcentagemDesgasteMotor();

		int durabilidade = InterfaceJogo.DUR_AREO_NORMAL;
		if (InterfaceJogo.FACIL_NV == controleJogo.getNiveljogo()) {
			durabilidade = InterfaceJogo.DUR_AREO_FACIL;
		}
		if (InterfaceJogo.DIFICIL_NV == controleJogo.getNiveljogo()) {
			durabilidade = InterfaceJogo.DUR_AREO_DIFICIL;
		}
		String dano = null;
		if (pilotoSelecionado != null) {
			dano = pilotoSelecionado.getCarro().getDanificado();
		}

		if ((dano == null || "".equals(dano))
				&& motor > 25
				&& porcentComb > 25
				&& pilotoSelecionado.getCarro().getDurabilidadeAereofolio() >= durabilidade
				&& pneus > 25
				&& pilotoSelecionado.getCarro().getTemperaturaMotor() < pilotoSelecionado
						.getCarro().getTempMax())
			return;

		g2d.setColor(this.transpMenus);
		g2d.fillRoundRect(limitesViewPort.x + 180, limitesViewPort.y + 5,
				carroimgDano.getWidth() + 5, carroimgDano.getHeight() + 5, 15,
				15);
		g2d.drawImage(carroimgDano, limitesViewPort.x + 185,
				limitesViewPort.y + 10, null);

		if (pilotoSelecionado.getCarro().getDurabilidadeAereofolio() < durabilidade
				&& !Carro.PERDEU_AEREOFOLIO.equals(pilotoSelecionado.getCarro()
						.getDanificado())) {
			// bico
			g2d.setColor(OcilaCor.geraOcila("bicoAmarelo", Color.yellow));
			g2d.fillOval(limitesViewPort.x + 188, limitesViewPort.y + 26, 15,
					15);
		}

		if (porcentComb <= 25) {
			g2d.drawImage(fuel.getImage(),
					limitesViewPort.x + carroimgDano.getWidth() + 135,
					limitesViewPort.y + 10, null);
		}

		if (Carro.PERDEU_AEREOFOLIO.equals(pilotoSelecionado.getCarro()
				.getDanificado())) {
			// bico
			g2d.setColor(Color.red);
			g2d.setColor(OcilaCor.geraOcila("bicoVermelho", Color.red));
			g2d.fillOval(limitesViewPort.x + 188, limitesViewPort.y + 26, 15,
					15);
		}

		if (Carro.PNEU_FURADO.equals(pilotoSelecionado.getCarro()
				.getDanificado())) {
			g2d.setColor(OcilaCor.geraOcila("peneuFurado", Color.red));
			// Roda diantera
			g2d.fillOval(limitesViewPort.x + 208, limitesViewPort.y + 24, 18,
					18);
			// Roda trazeira
			g2d.fillOval(limitesViewPort.x + 312, limitesViewPort.y + 24, 18,
					18);
		} else if (pneus <= 25) {
			g2d.setColor(OcilaCor.geraOcila("peneuGastos", Color.yellow));
			// Roda diantera
			g2d.fillOval(limitesViewPort.x + 208, limitesViewPort.y + 24, 18,
					18);
			// Roda trazeira
			g2d.fillOval(limitesViewPort.x + 312, limitesViewPort.y + 24, 18,
					18);
		}
		if (Carro.EXPLODIU_MOTOR.equals(pilotoSelecionado.getCarro()
				.getDanificado())) {
			g2d.setColor(OcilaCor.geraOcila("explodioMotor", Color.red));
			// motor
			g2d.fillOval(limitesViewPort.x + 278, limitesViewPort.y + 12, 15,
					15);
		} else if (motor <= 25
				|| pilotoSelecionado.getCarro().getTemperaturaMotor() == pilotoSelecionado
						.getCarro().getTempMax()) {
			g2d.setColor(OcilaCor.geraOcila("motorGasto", Color.yellow));
			g2d.fillOval(limitesViewPort.x + 278, limitesViewPort.y + 12, 15,
					15);
		}
		if (Carro.BATEU_FORTE.equals(pilotoSelecionado.getCarro()
				.getDanificado())) {
			g2d.setColor(OcilaCor.geraOcila("bateuForte", Color.red));
			g2d.fillRoundRect(limitesViewPort.x + 195, limitesViewPort.y + 18,
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
		if (!desenhaInfo) {
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

	private void desenhaCarrosLado(Piloto psel, Graphics2D g2d) {
		if (!desenhaInfo) {
			return;
		}
		if (pilotoSelecionado == null) {
			return;
		}
		if (controleJogo.getNumVoltaAtual() < 1) {
			return;
		}
		if (!Logger.carregaBkg) {
			return;
		}

		BufferedImage carroimg = null;
		int carSelX = limitesViewPort.x;
		int carSelY = limitesViewPort.y + limitesViewPort.height - 75;
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
			carSelX += (120 - halfCarWidth);
			dstX += 90;
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
		carSelY = limitesViewPort.y + limitesViewPort.height - 75;
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
			carSelX -= (110 - halfCarWidth);
			dstX -= 70;
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

				if (circuito.isUsaBkg() && circuito.getObjetos() != null) {
					for (ObjetoPista objetoPista : circuito.getObjetos()) {
						if (!(objetoPista instanceof ObjetoTransparencia))
							continue;
						if (objetoPista.isPintaEmcima()) {
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
						objetoTransparencia.desenhaCarro(gImage, zoom, carx,
								cary);

					}
				}
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

			g2d.setStroke(stroke);
		}
	}

	private void desenharClima(Graphics2D g2d) {
		if (!desenhaInfo) {
			return;
		}
		if (qtdeLuzesAcesas > 0) {
			return;
		}
		if (controleJogo.getNiveljogo() == InterfaceJogo.DIFICIL_NV) {
			return;
		}
		ImageIcon icon = (ImageIcon) gerenciadorVisual.getImgClima().getIcon();
		if (icon != null && pointDesenhaClima != null) {
			g2d.setColor(transpMenus);
			g2d.fillRoundRect(limitesViewPort.x + pointDesenhaClima.x + 105,
					pointDesenhaClima.y + limitesViewPort.y - 5,
					icon.getIconWidth() + 10, icon.getIconHeight() + 10, 15, 15);
			g2d.drawImage(icon.getImage(), limitesViewPort.x
					+ pointDesenhaClima.x + 110, pointDesenhaClima.y
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
		String piloto = ps.getNome();
		String carro = ps.getCarro().getNome();

		if (!Util.isNullOrEmpty(ps.getNomeJogador())) {
			piloto += " " + ps.getNomeJogador();
		}
		Color c2 = ps.getCarro().getCor2();
		if (c2 != null) {
			c2 = c2.brighter();
			g2d.setColor(new Color(c2.getRed(), c2.getGreen(), c2.getBlue(),
					200));
		}
		Point pt = new Point(limitesViewPort.x + 5, limitesViewPort.y + 140);
		if (!Util.isNullOrEmpty(piloto)) {
			int larguraTxtPiloto = Util.calculaLarguraText(piloto, g2d) + 15;
			g2d.fillRoundRect(pt.x, pt.y, larguraTxtPiloto, 14, 15, 15);
		}
		if (!Util.isNullOrEmpty(carro)) {
			int larguraTxtCarro = Util.calculaLarguraText(carro, g2d) + 15;
			g2d.fillRoundRect(pt.x, pt.y + 16, larguraTxtCarro, 14, 15, 15);

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

	private void desenhaNomePilotoSelecionadoCarroCima(Piloto ps, Graphics2D g2d) {
		if (ps == null)
			return;
		if (ps.getNoAtual() == null)
			return;
		if (ps.getCarro() == null)
			return;
		String txt1 = ps.getNome() + "-" + ps.getCarro().getNome();
		String agressivo = (ps.isAgressivo() ? Lang.msg("AGRESSIVO") : Lang
				.msg("NORMAL"));
		String intel = (ps.isJogadorHumano() ? ps.isAutoPos() ? "A " : "M "
				: "IA ");
		String txt2 = intel + " " + agressivo;

		Color c2 = ps.getCarro().getCor2();
		if (c2 != null) {
			c2 = c2.brighter();
			g2d.setColor(new Color(c2.getRed(), c2.getGreen(), c2.getBlue(),
					200));
		}
		if (!Util.isNullOrEmpty(pilotoSelecionado.getCarro().getDanificado())) {
			txt1 = Lang.msg(pilotoSelecionado.getCarro().getDanificado());
			txt2 = null;
		}

		Point pt = new Point(ps.getCarX(), ps.getCarY());
		if (!Util.isNullOrEmpty(txt1)) {
			int larguraTxt1 = Util.calculaLarguraText(txt1, g2d);
			g2d.fillRoundRect(Util.inte((pt.x * zoom) + 14),
					Util.inte((pt.y * zoom) - 50), larguraTxt1 + 7, 14, 15, 15);
		}
		if (!Util.isNullOrEmpty(txt2)) {
			Color bg = g2d.getColor();
			int larguraTxt2 = Util.calculaLarguraText(txt2, g2d);
			int xBase = Util.inte((pt.x * zoom) + 14);
			int yBase = Util.inte((pt.y * zoom) - 35);
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
			g2d.fillRoundRect(xBase - 2, yBase, larguraTxt2 + 8, 14, 15, 15);
			xBase += larguraTxt2 + 15;
			if (Piloto.AGRESSIVO.equals(pilotoSelecionado.getModoPilotagem())) {
				g2d.setColor(OcilaCor.geraOcila("miniPilotoMax", red));
				g2d.fillRoundRect(xBase, yBase, 7, 15, 4, 4);
				xBase += 8;
				g2d.setColor(yel);
				g2d.fillRoundRect(xBase, yBase + 5, 7, 10, 3, 3);
				xBase += 8;
				g2d.setColor(gre);
				g2d.fillRoundRect(xBase, yBase + 10, 7, 5, 4, 4);
			} else if (Piloto.NORMAL.equals(pilotoSelecionado
					.getModoPilotagem())) {
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
		int xTxt = Util.inte(pt.x * zoom) + 15;
		if (!Util.isNullOrEmpty(txt1)) {
			g2d.drawString(txt1, xTxt + 2, Util.inte((pt.y * zoom) - 38));
		}
		if (!Util.isNullOrEmpty(txt2)) {
			g2d.drawString(txt2, xTxt + 28, Util.inte((pt.y * zoom - 24)));
		}
		g2d.setColor(transpMenus);
		g2d.drawLine(Util.inte((pt.x * zoom + 4)), Util.inte(pt.y * zoom),
				Util.inte((pt.x * zoom) + 13), Util.inte((pt.y * zoom) - 40));
	}

	private void desenhaVelocidade(Graphics2D g2d) {
		if (pilotoSelecionado == null) {
			return;
		}
		if (!desenhaInfo) {
			return;
		}
		if (controleJogo.getNumVoltaAtual() < 1) {
			return;
		}
		Piloto ps = pilotoSelecionado;

		int velocidade = (controleJogo.isSafetyCarNaPista() ? ps
				.getVelocidade() / 2 : ps.getVelocidade());
		String velo = "~" + velocidade + " Km/h";
		if (ps.getVelocidade() == 1) {
			return;
		}
		g2d.setColor(transpMenus);
		g2d.fillRoundRect(limitesViewPort.x + pointDesenhaVelo.x,
				limitesViewPort.y + pointDesenhaVelo.y + 40, 160, 35, 15, 15);
		Font fontOri = g2d.getFont();
		g2d.setFont(new Font(fontOri.getName(), Font.BOLD, 28));

		g2d.setColor(OcilaCor.porcentVermelho100Verde0((100 * ps
				.getVelocidade() / 330)));
		g2d.drawString(velo, limitesViewPort.x + pointDesenhaVelo.x + 2,
				limitesViewPort.y + pointDesenhaVelo.y + 67);
		g2d.setFont(fontOri);
	}

	private void desenhaBarrasDeGiro(Piloto ps, Graphics2D g2d) {
		if (!desenhaInfo) {
			return;
		}
		if (ps == null) {
			return;
		}
		if (controleJogo.getNumVoltaAtual() > 0) {
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
	}

	private void desenhaBarrasPilotoCarro(Graphics2D g2d) {
		if (!desenhaInfo) {
			return;
		}
		if (pilotoSelecionado == null) {
			return;
		}

		if (controleJogo.getNumVoltaAtual() < 1) {
			return;
		}
		Stroke stroke = g2d.getStroke();
		int x = limitesViewPort.x;
		int y = limitesViewPort.y + 200;
		Font fontOri = g2d.getFont();
		g2d.setFont(new Font(fontOri.getName(), Font.BOLD, 28));

		int porcentComb = pilotoSelecionado.getCarro().porcentagemCombustivel();
		g2d.setColor(transpMenus);
		g2d.fillRoundRect(limitesViewPort.x + 3, y - 26, 2 * porcentComb, 30,
				10, 10);
		g2d.setStroke(trilhoMiniPista);
		g2d.drawRoundRect(limitesViewPort.x + 3, y - 26, 200, 30, 10, 10);
		g2d.setStroke(stroke);
		g2d.setColor(transpMenus);
		g2d.setColor(OcilaCor.porcentVerde100Vermelho0(porcentComb));
		g2d.drawString(Lang.msg("215") + " " + porcentComb + "%", x + 5, y);

		y += 35;
		int pneus = pilotoSelecionado.getCarro().porcentagemDesgastePeneus();
		g2d.setColor(transpMenus);
		g2d.fillRoundRect(limitesViewPort.x + 3, y - 26, 2 * pneus, 30, 10, 10);
		g2d.setStroke(trilhoMiniPista);
		g2d.drawRoundRect(limitesViewPort.x + 3, y - 26, 200, 30, 10, 10);
		g2d.setStroke(stroke);
		g2d.setColor(OcilaCor.porcentVerde100Vermelho0(pneus));
		g2d.drawString(Lang.msg("216") + " " + pneus + "%", x + 5, y);

		y += 35;

		int motor = pilotoSelecionado.getCarro().porcentagemDesgasteMotor();
		g2d.setColor(transpMenus);
		g2d.fillRoundRect(limitesViewPort.x + 3, y - 26, 2 * motor, 30, 10, 10);
		g2d.setStroke(trilhoMiniPista);
		g2d.drawRoundRect(limitesViewPort.x + 3, y - 26, 200, 30, 10, 10);
		g2d.setStroke(stroke);
		g2d.setColor(OcilaCor.porcentVerde100Vermelho0(motor));
		g2d.drawString(Lang.msg("217") + " " + motor + "%", x + 5, y);

		y += 35;

		int stress = pilotoSelecionado.getStress();
		g2d.setColor(transpMenus);
		g2d.fillRoundRect(limitesViewPort.x + 3, y - 26, 2 * stress, 30, 10, 10);
		g2d.setStroke(trilhoMiniPista);
		g2d.drawRoundRect(limitesViewPort.x + 3, y - 26, 200, 30, 10, 10);
		g2d.setStroke(stroke);
		g2d.setColor(OcilaCor.porcentVermelho100Verde0(stress));
		g2d.drawString(Lang.msg("153") + " " + stress + "%", x + 5, y);
		g2d.setFont(fontOri);
	}

	private void desenhaDRS(Graphics2D g2d) {
		if (!(controleJogo.isDrs() && desenhaInfo && controleJogo
				.getNumVoltaAtual() > 0)) {
			return;
		}

		Font fontOri = g2d.getFont();
		g2d.setFont(new Font(fontOri.getName(), Font.BOLD, fontOri.getSize()));
		if (Carro.MENOS_ASA.equals(pilotoSelecionado.getCarro().getAsa())) {
			g2d.setColor(gre);
		} else {
			if (pilotoSelecionado.getNoAtual() != null
					&& pilotoSelecionado.getNoAtual().verificaRetaOuLargada()
					&& pilotoSelecionado.getNumeroVolta() > 0
					&& (controleJogo.obterCarroNaFrente(pilotoSelecionado) != null && controleJogo
							.obterCarroNaFrente(pilotoSelecionado).getPiloto()
							.getPtosBox() == 0)
					&& pilotoSelecionado.getPtosBox() == 0
					&& controleJogo
							.calculaSegundosParaProximoDouble(pilotoSelecionado) < 0.7) {
				g2d.setColor(OcilaCor.geraOcila("podeUsarDRS", yel));
			} else {
				g2d.setColor(lightWhite);
			}
		}
		g2d.fillRoundRect(limitesViewPort.x + 165, limitesViewPort.y + 155, 34,
				15, 15, 15);
		g2d.setColor(Color.BLACK);
		g2d.drawString("DRS", limitesViewPort.x + 170, limitesViewPort.y + 167);
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
		if (marcasPneu.size() > 500 || noAtual == null) {
			return;
		}
		synchronized (marcasPneu) {
			marcasPneu.add(travadaRoda);
		}
	}

	private void desenhaPista(Graphics2D g2d) {
		No oldNo = null;
		g2d.setColor(Color.LIGHT_GRAY);
		Stroke stroke = g2d.getStroke();
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
		g2d.setStroke(stroke);
	}

	private void desenhaTintaPistaEZebra(Graphics2D g2d) {
		g2d.setColor(Color.WHITE);
		Stroke stroke = g2d.getStroke();
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
		g2d.setStroke(stroke);
	}

	private void desenhaBoxes(Graphics2D g2d) {
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

	private void gerarBoxes() {
		int paradas = circuito.getParadaBoxIndex();
		for (int i = 0; i < 12; i++) {
			int iP = paradas + Util.inte(Carro.LARGURA * 2 * i) + Carro.LARGURA;
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

	public int getPorcentCombust() {
		return porcentCombust;
	}

	public String getTpPneu() {
		return tpPneu;
	}

	public String getTpAsa() {
		return tpAsa;
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
}
