package sowbreira.f1mane.visao;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JPanel;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.PropertyUtils;

import sowbreira.f1mane.MainFrame;
import sowbreira.f1mane.controles.ControleJogoLocal;
import sowbreira.f1mane.controles.InterfaceJogo;
import sowbreira.f1mane.entidades.Carro;
import sowbreira.f1mane.entidades.Circuito;
import sowbreira.f1mane.entidades.Clima;
import sowbreira.f1mane.entidades.No;
import sowbreira.f1mane.entidades.Piloto;
import sowbreira.f1mane.recursos.CarregadorRecursos;
import sowbreira.f1mane.recursos.idiomas.Lang;
import br.nnpe.ImageUtil;
import br.nnpe.Logger;
import br.nnpe.Util;

public class PainelMenuLocal extends JPanel {

	public static BufferedImage bg;

	public static String MENU_PRINCIPAL = "MENU_PRINCIPAL";

	public static String MENU_CORRIDA = "MENU_CORRIDA";

	public static String MENU_NOVO_CAMPEONATO_PILOTOS = "MENU_NOVO_CAMPEONATO_PILOTOS";

	public static String MENU_QUALIFICACAO = "MENU_QUALIFICACAO";

	private static final String MENU_CORRIDA_CAMPEONATO_PILOTOS = "MENU_CORRIDA_CAMPEONATO_PILOTOS";

	private String MENU = MENU_PRINCIPAL;

	private MainFrame mainFrame;

	private InterfaceJogo controleJogo;

	public final static Color lightWhite = new Color(200, 200, 200, 100);

	public final static Color lightWhite2 = new Color(255, 255, 255, 160);

	public final static Color yel = new Color(255, 255, 0, 150);

	public final static Color blu = new Color(105, 105, 105, 40);

	private RoundRectangle2D corridaRect = new RoundRectangle2D.Double(0, 0, 1,
			1, 10, 10);
	private RoundRectangle2D campeonatoRect = new RoundRectangle2D.Double(0, 0,
			1, 1, 10, 10);

	private RoundRectangle2D continuaCampeonatoRect = new RoundRectangle2D.Double(
			0, 0, 1, 1, 10, 10);

	private RoundRectangle2D sobreRect = new RoundRectangle2D.Double(0, 0, 1,
			1, 10, 10);

	private RoundRectangle2D proxPistaRect = new RoundRectangle2D.Double(0, 0,
			1, 1, 10, 10);

	private RoundRectangle2D antePistaRect = new RoundRectangle2D.Double(0, 0,
			1, 1, 10, 10);

	private RoundRectangle2D addPistaRect = new RoundRectangle2D.Double(0, 0,
			1, 1, 10, 10);

	private RoundRectangle2D remPistaRect = new RoundRectangle2D.Double(0, 0,
			1, 1, 10, 10);

	private RoundRectangle2D proxTemporadaRect = new RoundRectangle2D.Double(0,
			0, 1, 1, 10, 10);

	private RoundRectangle2D anteTemporadaRect = new RoundRectangle2D.Double(0,
			0, 1, 1, 10, 10);

	private RoundRectangle2D pistaRect = new RoundRectangle2D.Double(0, 0, 1,
			1, 10, 10);

	private RoundRectangle2D menosVoltasRect = new RoundRectangle2D.Double(0,
			0, 1, 1, 10, 10);

	private RoundRectangle2D maisVoltasRect = new RoundRectangle2D.Double(0, 0,
			1, 1, 10, 10);

	private RoundRectangle2D menosTurbulenciaRect = new RoundRectangle2D.Double(
			0, 0, 1, 1, 10, 10);

	private RoundRectangle2D menosCombustivelRect = new RoundRectangle2D.Double(
			0, 0, 1, 1, 10, 10);

	private RoundRectangle2D maisTurbulenciaRect = new RoundRectangle2D.Double(
			0, 0, 1, 1, 10, 10);

	private RoundRectangle2D maisCombustivelRect = new RoundRectangle2D.Double(
			0, 0, 1, 1, 10, 10);

	private RoundRectangle2D numVoltasRect = new RoundRectangle2D.Double(0, 0,
			1, 1, 10, 10);

	private RoundRectangle2D solRect = new RoundRectangle2D.Double(0, 0, 1, 1,
			10, 10);

	private RoundRectangle2D chuvaRect = new RoundRectangle2D.Double(0, 0, 1,
			1, 10, 10);

	private RoundRectangle2D nubladoRect = new RoundRectangle2D.Double(0, 0, 1,
			1, 10, 10);

	private RoundRectangle2D facilRect = new RoundRectangle2D.Double(0, 0, 1,
			1, 10, 10);

	private RoundRectangle2D normalRect = new RoundRectangle2D.Double(0, 0, 1,
			1, 10, 10);

	private RoundRectangle2D dificilRect = new RoundRectangle2D.Double(0, 0, 1,
			1, 10, 10);

	private RoundRectangle2D pneuMoleRect = new RoundRectangle2D.Double(0, 0,
			1, 1, 10, 10);

	private RoundRectangle2D pneuDuroRect = new RoundRectangle2D.Double(0, 0,
			1, 1, 10, 10);

	private RoundRectangle2D pneuChuvaRect = new RoundRectangle2D.Double(0, 0,
			1, 1, 10, 10);

	private RoundRectangle2D maisAsaRect = new RoundRectangle2D.Double(0, 0, 1,
			1, 10, 10);

	private RoundRectangle2D normalAsaRect = new RoundRectangle2D.Double(0, 0,
			1, 1, 10, 10);

	private RoundRectangle2D menosAsaRect = new RoundRectangle2D.Double(0, 0,
			1, 1, 10, 10);

	private RoundRectangle2D drsRect = new RoundRectangle2D.Double(0, 0, 1, 1,
			10, 10);

	private RoundRectangle2D kersRect = new RoundRectangle2D.Double(0, 0, 1, 1,
			10, 10);

	private RoundRectangle2D trocaPneusRect = new RoundRectangle2D.Double(0, 0,
			1, 1, 10, 10);

	private RoundRectangle2D reabasteciemtoRect = new RoundRectangle2D.Double(
			0, 0, 1, 1, 10, 10);

	private RoundRectangle2D proximoMenuRect = new RoundRectangle2D.Double(0,
			0, 1, 1, 10, 10);

	private RoundRectangle2D anteriroMenuRct = new RoundRectangle2D.Double(0,
			0, 1, 1, 10, 10);

	private String circuitoSelecionado = null;

	private String temporadaSelecionada = null;

	private int numVoltasSelecionado = 12;

	private int turbulenciaSelecionado = 250;

	private int combustivelSelecionado = 70;

	private String asaSelecionado = Carro.ASA_NORMAL;

	private String pneuSelecionado = Carro.TIPO_PNEU_MOLE;

	private String climaSelecionado = Clima.SOL;

	private String nivelSelecionado = InterfaceJogo.NORMAL;

	private Piloto pilotoSelecionado;

	private boolean kers = true;

	private boolean drs = true;

	private boolean trocaPneus = true;

	private boolean reabasteciemto = false;

	private boolean desenhaCarregando = false;

	private CarregadorRecursos carregadorRecursos;

	private BufferedImage setaCarroCima;
	private BufferedImage setaCarroBaixo;
	private BufferedImage setaCarroEsquerda;
	private BufferedImage setaCarroDireita;

	private BufferedImage sol;
	private BufferedImage chuva;
	private BufferedImage nublado;

	private BufferedImage pneuMoleImg;
	private BufferedImage pneuDuroImg;
	private BufferedImage pneuChuvaImg;

	private List temporadas;
	private List cirucitosCampeonato = new ArrayList();
	private Map circuitosPilotos;

	private List<RoundRectangle2D> pilotosRect;

	public PainelMenuLocal(MainFrame mainFrame, InterfaceJogo controleJogo) {
		this.mainFrame = mainFrame;
		this.controleJogo = controleJogo;
		addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				processaClick(e);
				repaint();
				super.mouseClicked(e);
			}
		});

		iniciaRecursos();

	}

	protected void processaClick(MouseEvent e) {
		if (MENU.equals(MENU_PRINCIPAL) && corridaRect.contains(e.getPoint())) {
			MENU = MENU_CORRIDA;
			resetaRects();
			return;
		}
		if (MENU.equals(MENU_PRINCIPAL)
				&& campeonatoRect.contains(e.getPoint())) {
			MENU = MENU_NOVO_CAMPEONATO_PILOTOS;
			resetaRects();
			return;
		}
		if (continuaCampeonatoRect.contains(e.getPoint())) {
			try {
				if (mainFrame.verificaCriarJogo()) {
					controleJogo = mainFrame.getControleJogo();
					controleJogo.continuarCampeonato();
				}

			} catch (Exception e1) {
				e1.printStackTrace();
				Logger.logarExept(e1);
			}
			return;
		}
		if (sobreRect.contains(e.getPoint())) {
			try {
				mainFrame.mostraSobre();
			} catch (Exception e1) {
				e1.printStackTrace();
				Logger.logarExept(e1);
			}
			return;
		}

		if (proxPistaRect.contains(e.getPoint())) {
			selecionaProximaPista();
			return;
		}
		if (antePistaRect.contains(e.getPoint())) {
			selecionaPistaAnterior();
			return;
		}
		if (maisVoltasRect.contains(e.getPoint())) {
			maisVoltas();
			return;
		}
		if (menosVoltasRect.contains(e.getPoint())) {
			menosVoltas();
			return;
		}
		if (solRect.contains(e.getPoint())) {
			climaSelecionado = Clima.SOL;
			return;
		}
		if (nubladoRect.contains(e.getPoint())) {
			climaSelecionado = Clima.NUBLADO;
			return;
		}
		if (chuvaRect.contains(e.getPoint())) {
			climaSelecionado = Clima.CHUVA;
			return;
		}
		if (facilRect.contains(e.getPoint())) {
			nivelSelecionado = InterfaceJogo.FACIL;
			return;
		}
		if (normalRect.contains(e.getPoint())) {
			nivelSelecionado = InterfaceJogo.NORMAL;
			return;
		}
		if (dificilRect.contains(e.getPoint())) {
			nivelSelecionado = InterfaceJogo.DIFICIL;
			return;
		}
		if (pneuMoleRect.contains(e.getPoint())) {
			pneuSelecionado = Carro.TIPO_PNEU_MOLE;
			return;
		}
		if (pneuDuroRect.contains(e.getPoint())) {
			pneuSelecionado = Carro.TIPO_PNEU_DURO;
			return;
		}
		if (pneuChuvaRect.contains(e.getPoint())) {
			pneuSelecionado = Carro.TIPO_PNEU_CHUVA;
			return;
		}

		if (menosAsaRect.contains(e.getPoint())) {
			asaSelecionado = Carro.MENOS_ASA;
			return;
		}

		if (maisAsaRect.contains(e.getPoint())) {
			asaSelecionado = Carro.MAIS_ASA;
			return;
		}

		if (normalAsaRect.contains(e.getPoint())) {
			asaSelecionado = Carro.ASA_NORMAL;
			return;
		}

		if (maisTurbulenciaRect.contains(e.getPoint())) {
			maisTurbulencia();
			return;
		}
		if (menosTurbulenciaRect.contains(e.getPoint())) {
			menosTurbulencia();
			return;
		}

		if (maisCombustivelRect.contains(e.getPoint())) {
			maisCombustivel();
			return;
		}
		if (menosCombustivelRect.contains(e.getPoint())) {
			menosCombustivel();
			return;
		}

		if (drsRect.contains(e.getPoint())) {
			drs = !drs;
			return;
		}

		if (kersRect.contains(e.getPoint())) {
			kers = !kers;
			return;
		}

		if (trocaPneusRect.contains(e.getPoint())) {
			trocaPneus = !trocaPneus;
			return;
		}

		if (reabasteciemtoRect.contains(e.getPoint())) {
			reabasteciemto = !reabasteciemto;
			return;
		}
		if (proxTemporadaRect.contains(e.getPoint())) {
			selecionaProximaTemporada();
			return;
		}
		if (anteTemporadaRect.contains(e.getPoint())) {
			selecionaAnteTemporada();
			return;
		}
		if (proximoMenuRect.contains(e.getPoint())) {
			selecionaProximoMenu();
			return;
		}
		if (anteriroMenuRct.contains(e.getPoint())) {
			selecionaAnteMenu();
			return;
		}

		for (int i = 0; i < 24; i++) {
			if (pilotosRect.get(i).contains(e.getPoint())) {
				String temporada = "t" + temporadaSelecionada;
				List pilotos = litasPilotosTemporada(temporada);
				pilotoSelecionado = (Piloto) pilotos.get(i);
				return;
			}
		}

		if (addPistaRect.contains(e.getPoint())) {
			adicionaPistaCampeonato();
			return;
		}
		if (remPistaRect.contains(e.getPoint())) {
			removePistaCampeonato();
			return;
		}

	}

	private void iniciaRecursos() {
		setaCarroCima = CarregadorRecursos
				.carregaBufferedImageTranspareciaBranca("SetaCarroCima.png",
						200);
		setaCarroBaixo = CarregadorRecursos
				.carregaBufferedImageTranspareciaBranca("SetaCarroBaixo.png",
						200);
		/**
		 * Esquerda
		 */

		double rad = Math.toRadians(270);
		AffineTransform afRotate = new AffineTransform();
		afRotate.setToRotation(rad, setaCarroCima.getWidth() / 2,
				setaCarroCima.getHeight() / 2);
		AffineTransformOp opRotate = new AffineTransformOp(afRotate,
				AffineTransformOp.TYPE_BILINEAR);
		BufferedImage rotateBufferSetaCima = new BufferedImage(
				setaCarroCima.getWidth(), setaCarroCima.getWidth(),
				BufferedImage.TYPE_INT_ARGB);
		opRotate.filter(setaCarroCima, rotateBufferSetaCima);

		setaCarroEsquerda = rotateBufferSetaCima;

		rad = Math.toRadians(90);
		afRotate = new AffineTransform();
		afRotate.setToRotation(rad, setaCarroCima.getWidth() / 2,
				setaCarroCima.getHeight() / 2);
		opRotate = new AffineTransformOp(afRotate,
				AffineTransformOp.TYPE_BILINEAR);
		rotateBufferSetaCima = new BufferedImage(setaCarroCima.getWidth(),
				setaCarroCima.getWidth(), BufferedImage.TYPE_INT_ARGB);
		opRotate.filter(setaCarroCima, rotateBufferSetaCima);

		setaCarroDireita = rotateBufferSetaCima;

		sol = CarregadorRecursos.carregaBufferedImageTransparecia(
				"clima/sol.gif", null);
		nublado = CarregadorRecursos.carregaBufferedImageTransparecia(
				"clima/nublado.gif", null);
		chuva = CarregadorRecursos.carregaBufferedImageTransparecia(
				"clima/chuva.gif", null);

		pneuMoleImg = ImageUtil.geraResize(CarregadorRecursos
				.carregaBufferedImageTransparecia("pneu_mole.png", null), 0.3);
		pneuDuroImg = ImageUtil.geraResize(CarregadorRecursos
				.carregaBufferedImageTransparecia("pneu-duro.png", null), 0.3);
		pneuChuvaImg = ImageUtil.geraResize(CarregadorRecursos
				.carregaBufferedImageTransparecia("pneu-chuva.png", null), 0.3);

		carregadorRecursos = new CarregadorRecursos(true);
		circuitosPilotos = carregadorRecursos.carregarTemporadasPilotos();
		temporadas = carregadorRecursos.getVectorTemps();
		Collections.reverse(temporadas);
		pilotosRect = new ArrayList<RoundRectangle2D>();
		for (int i = 0; i < 24; i++) {
			pilotosRect.add(new RoundRectangle2D.Double(0, 0, 1, 1, 10, 10));
		}
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		try {
			Graphics2D g2d = (Graphics2D) g;
			setarHints(g2d);
			if (PainelCircuito.carregaBkg) {
				if (MENU.equals(MENU_PRINCIPAL))
					bg = ImageUtil.gerarFade(CarregadorRecursos
							.carregaBufferedImage("bg-monaco.png"), 25);
				if (MENU.equals(MENU_CORRIDA))
					bg = CarregadorRecursos.carregaBufferedImage("f1bg.png");
			}
			if (bg != null) {
				int centerX = getWidth() / 2;
				int centerY = getHeight() / 2;
				int bgX = bg.getWidth() / 2;
				int bgY = bg.getHeight() / 2;
				g.drawImage(bg, centerX - bgX, centerY - bgY, null);
			}
			if (desenhaCarregando) {
				desenhaCarregando(g2d);
				return;
			}
			desenhaMenuPrincipalSelecao(g2d);
			desenhaMenuCorridaSelecao(g2d);
			desenhaMenuNovoCampeonato(g2d);
			desenhaMenuQualificacao(g2d);
		} catch (Exception e) {
			e.printStackTrace();
			Logger.logarExept(e);
		}

	}

	private void removePistaCampeonato() {
		cirucitosCampeonato.remove(circuitoSelecionado);
		selecionaPistaAnterior();
	}

	private void adicionaPistaCampeonato() {
		if (!cirucitosCampeonato.contains(circuitoSelecionado)) {
			cirucitosCampeonato.add(circuitoSelecionado);
		}
		selecionaProximaPista();

	}

	private void desenhaMenuNovoCampeonato(Graphics2D g2d) throws IOException {
		if (!MENU.equals(MENU_NOVO_CAMPEONATO_PILOTOS)) {
			return;
		}
		int x = (int) (getWidth() / 2);
		int y = (int) (getHeight() / 2);

		x -= 490;
		y -= 285;

		desenhaSeletorCircuito(g2d, x, y);

		desenhaAdicionaRemoverPistas(g2d, x + 30, y + 220);

		desenhaSeletorNumeroVoltas(g2d, x + 40, y + 280);

		desenhaNivelCorrida(g2d, x + 40, y + 340);

		desenhaDrsKersPneusReabastecimento(g2d, x + 40, y + 400);

		desenhaAnteriroProximo(g2d, x + 350, y + 600);

		desenhaCircuitosSelecionados(g2d, x + 480, y - 50);

		desenhaTemporadas(g2d, x + 580, y + 280, true);

	}

	private void desenhaCircuitosSelecionados(Graphics2D g2d, int x, int y) {

		int cont = 0;
		for (int i = 0; i < cirucitosCampeonato.size(); i++) {
			if (i % 10 == 0) {
				cont = 0;
			}
			int novoY = y + cont * 25;
			int novoX = 0;
			if (i > 9) {
				novoX = 190;
			}
			if (i > 19) {
				novoX = 380;
			}
			String circuito = (String) cirucitosCampeonato.get(i);
			desenhaNomeCircuito(g2d, x + novoX, novoY + 35, i, circuito);
			cont++;
		}
	}

	private void desenhaNomeCircuito(Graphics2D g2d, int x, int y, int i,
			String circuito) {
		Font fontOri = g2d.getFont();
		g2d.setFont(new Font(fontOri.getName(), Font.BOLD, 14));
		circuito = (i + 1) + " " + circuito.toUpperCase();
		int tamNmPiloto = Util.calculaLarguraText(circuito, g2d);
		g2d.setColor(lightWhite);
		g2d.fillRoundRect(x - 15, y, tamNmPiloto + 10, 18, 10, 10);
		g2d.setColor(Color.BLACK);
		g2d.drawString(circuito, x - 10, y + 15);
		g2d.setFont(fontOri);

	}

	private void desenhaAdicionaRemoverPistas(Graphics2D g2d, int x, int y) {
		g2d.setColor(lightWhite);
		remPistaRect.setFrame(x, y - 25, 30, 30);
		g2d.fill(remPistaRect);
		g2d.drawImage(setaCarroEsquerda, x - 15, y - 55, null);
		x += 40;

		Font fontOri = g2d.getFont();
		g2d.setFont(new Font(fontOri.getName(), Font.BOLD, 28));
		String txt = Lang.msg("adicionaRemove").toUpperCase();
		int larguraTexto = 300;
		g2d.setColor(lightWhite);
		g2d.fillRoundRect(x, y - 25, larguraTexto + 20, 30, 10, 10);
		g2d.setColor(Color.BLACK);
		int incX = (320 - Util.larguraTexto(txt, g2d)) / 2;
		g2d.drawString(txt.toUpperCase(), x + incX, y);

		x += larguraTexto + 30;

		g2d.setColor(lightWhite);
		addPistaRect.setFrame(x, y - 25, 30, 30);
		g2d.fill(addPistaRect);
		g2d.drawImage(setaCarroDireita, x - 45, y - 52, null);
		g2d.setFont(fontOri);

	}

	private void desenhaCarregando(Graphics2D g2d) {
		int x = (int) (getWidth() / 2);
		int y = (int) (getHeight() / 2);
		Font fontOri = g2d.getFont();
		g2d.setFont(new Font(fontOri.getName(), Font.BOLD, 28));
		g2d.setColor(lightWhite);
		String txt = Lang.msg("carregando").toUpperCase();
		int larguraTexto = Util.larguraTexto(txt, g2d);
		int desl = larguraTexto / 2;
		corridaRect.setFrame(x - desl, y - 25, larguraTexto + 10, 30);
		g2d.fill(corridaRect);
		g2d.setColor(Color.BLACK);
		g2d.drawString(txt, x - desl + 5, y);
		g2d.setFont(fontOri);

	}

	private void desenhaMenuQualificacao(Graphics2D g2d) {
		if (!MENU.equals(MENU_QUALIFICACAO)) {
			return;
		}
		int x = (int) (getWidth() / 2);
		int y = (int) (getHeight() / 2);

		x -= 490;
		y -= 285;

		desenhaAnteriroProximo(g2d, x + 350, y + 600);

		desenhaCircuitoSelecionado(g2d, x + 350, y);

		desenhaTemporadaPilotoSelecionado(g2d, x + 350, y + 230);

		desenhaCombustivel(g2d, x + 490, y + 350);

		desenhaTipoPneu(g2d, x + 490, y + 460);

		desenhaTipoAsa(g2d, x + 490, y + 520);

	}

	private void desenhaTemporadaPilotoSelecionado(Graphics2D g2d, int x, int y) {
		BufferedImage imageCarro = controleJogo
				.obterCarroLado(pilotoSelecionado);
		String temporada = "t" + temporadaSelecionada;
		controleJogo.setTemporada(temporada);
		BufferedImage capacete = controleJogo.obterCapacete(pilotoSelecionado);

		x += 40;
		Font fontOri = g2d.getFont();
		g2d.setFont(new Font(fontOri.getName(), Font.BOLD, 28));
		String txt = temporadaSelecionada.replaceAll("\\*", "");
		int larguraTexto = 120;
		g2d.setColor(lightWhite);
		g2d.fillRoundRect(x, y - 25, larguraTexto + 20, 30, 15, 15);
		g2d.setColor(Color.BLACK);
		g2d.drawString(txt.toUpperCase(),
				x + (130 - Util.larguraTexto(txt, g2d)) / 2, y);

		desenaImClimaSelecionado(g2d, x + larguraTexto + 30, y);

		x -= 80;
		y += 40;

		txt = pilotoSelecionado.getCarro().getNome().toUpperCase();
		larguraTexto = Util.larguraTexto(txt, g2d);
		Color c = corRectPiloto(g2d, pilotoSelecionado, 1);
		g2d.fillRoundRect(x, y - 25, larguraTexto + 20, 30, 15, 15);
		corTxtPiloto(g2d, c);
		g2d.drawString(txt, x + 10, y);

		int xCarro = x + larguraTexto + 50;

		if (PainelCircuito.desenhaImagens)
			g2d.drawImage(imageCarro, xCarro, y - 35, null);

		y += 40;

		txt = pilotoSelecionado.getNome().toUpperCase();
		larguraTexto = Util.larguraTexto(txt, g2d);
		c = corRectPiloto(g2d, pilotoSelecionado, 2);
		g2d.fillRoundRect(x, y - 25, larguraTexto + 20, 30, 15, 15);
		corTxtPiloto(g2d, c);
		g2d.drawString(txt, x + 10, y);

		if (capacete != null && PainelCircuito.desenhaImagens)
			g2d.drawImage(capacete,
					xCarro + imageCarro.getWidth() - capacete.getWidth(),
					y - 35, null);

		g2d.setFont(fontOri);
	}

	private void desenaImClimaSelecionado(Graphics2D g2d, int x, int y) {
		g2d.setColor(lightWhite);
		g2d.fillRoundRect(x, y - 25, 35, 30, 15, 15);
		if (Clima.SOL.equals(climaSelecionado)) {
			g2d.drawImage(sol, x, y - 25, null);
		}
		if (Clima.NUBLADO.equals(climaSelecionado)) {
			g2d.drawImage(nublado, x, y - 25, null);
		}
		if (Clima.CHUVA.equals(climaSelecionado)) {
			g2d.drawImage(chuva, x, y - 25, null);
		}
	}

	private void desenhaCircuitoSelecionado(Graphics2D g2d, int x, int y) {
		if (circuitoSelecionado == null) {
			circuitoSelecionado = (String) controleJogo.getCircuitos().keySet()
					.iterator().next();
		}
		String nmCircuitoMRO = (String) controleJogo.getCircuitos().get(
				circuitoSelecionado);

		Font fontOri = g2d.getFont();
		g2d.setFont(new Font(fontOri.getName(), Font.BOLD, 28));
		String txt = circuitoSelecionado;
		int larguraTexto = 350;
		pistaRect.setFrame(x, y - 25, larguraTexto + 20, 30);
		g2d.setColor(lightWhite);
		g2d.fill(pistaRect);
		g2d.setColor(Color.BLACK);
		int incX = (320 - Util.larguraTexto(txt, g2d)) / 2;
		g2d.drawString(txt.toUpperCase(), x + incX, y);
		desenhaMiniCircuito(nmCircuitoMRO, g2d, x, y);

		g2d.setFont(fontOri);
	}

	private void corTxtPiloto(Graphics2D g2d, Color c) {
		int valor = (c.getRed() + c.getGreen() + c.getBlue()) / 2;
		if (valor > 250) {
			g2d.setColor(Color.BLACK);
		} else {
			g2d.setColor(Color.WHITE);
		}
	}

	private Color corRectPiloto(Graphics2D g2d, Piloto ps, int i) {
		Color c = null;
		if (i == 2) {
			c = ps.getCarro().getCor2();
		}
		if (i == 1) {
			c = ps.getCarro().getCor1();
		}
		if (c != null) {
			c = c.brighter();
			g2d.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), 200));
		}
		return c;
	}

	private void menosCombustivel() {
		if (combustivelSelecionado > 10) {
			combustivelSelecionado -= 10;
		}

	}

	private void maisCombustivel() {
		if (combustivelSelecionado < 100) {
			combustivelSelecionado += 10;
		}

	}

	private void selecionaAnteMenu() {
		resetaRects();
		if (MENU.equals(MENU_CORRIDA)) {
			MENU = MENU_PRINCIPAL;
			return;
		}
		if (MENU.equals(MENU_QUALIFICACAO)) {
			MENU = MENU_CORRIDA;
			return;
		}
		if (MENU.equals(MENU_NOVO_CAMPEONATO_PILOTOS)) {
			MENU = MENU_PRINCIPAL;
			return;
		}
	}

	public static void main(String[] args) throws IOException, Exception {
		System.out.println(0 % 10);
	}

	private void resetaRects() {
		try {
			Map mapVo = BeanUtils.describe(this);
			for (Iterator iter = mapVo.keySet().iterator(); iter.hasNext();) {
				String propriedade = (String) iter.next();

				if (mapVo.keySet().contains(propriedade)) {
					Class propriedadeTipo = PropertyUtils.getPropertyType(this,
							propriedade);
					Object property = PropertyUtils.getProperty(this,
							propriedade);
					if (RoundRectangle2D.class.equals(propriedadeTipo)) {
						RoundRectangle2D rectangle2d = (RoundRectangle2D) property;
						rectangle2d.setFrame(0, 0, 1, 1);
					}
				}
			}
			resetPilotosRect();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private void selecionaProximoMenu() {
		resetaRects();
		if (MENU.equals(MENU_CORRIDA)) {
			MENU = MENU_QUALIFICACAO;
			return;
		}
		if (MENU.equals(MENU_QUALIFICACAO)) {
			try {
				desenhaCarregando = true;
				paintImmediately(getVisibleRect());
				if (mainFrame.verificaCriarJogo()) {
					controleJogo = mainFrame.getControleJogo();
					controleJogo.setMainFrame(mainFrame);

					controleJogo.iniciarJogoMenuLocal(circuitoSelecionado,
							temporadaSelecionada, numVoltasSelecionado,
							turbulenciaSelecionado, climaSelecionado,
							nivelSelecionado, pilotoSelecionado, kers, drs,
							trocaPneus, reabasteciemto, combustivelSelecionado,
							asaSelecionado, pneuSelecionado);
				}
			} catch (Exception e1) {
				e1.printStackTrace();
				Logger.logarExept(e1);
			}
			return;
		}
		if (MENU.equals(MENU_NOVO_CAMPEONATO_PILOTOS)) {
			try {
				if (cirucitosCampeonato.isEmpty()) {
					cirucitosCampeonato.add(circuitoSelecionado);
				}
				if (mainFrame.verificaCriarJogo()) {
					controleJogo = mainFrame.getControleJogo();
					controleJogo.setMainFrame(mainFrame);
					controleJogo.criarCampeonatoPiloto(cirucitosCampeonato,
							temporadaSelecionada, numVoltasSelecionado,
							turbulenciaSelecionado, climaSelecionado,
							nivelSelecionado, pilotoSelecionado, kers, drs,
							trocaPneus, reabasteciemto);
				}
				MENU = MENU_CORRIDA_CAMPEONATO_PILOTOS;
			} catch (Exception e1) {
				e1.printStackTrace();
				Logger.logarExept(e1);
			}
			return;
		}
	}

	private void selecionaAnteTemporada() {
		resetPilotosRect();
		pilotoSelecionado = null;
		Object objectAnt = null;
		for (int i = temporadas.size() - 1; i > -1; i--) {
			Object object = (Object) temporadas.get(i);
			if (temporadaSelecionada != null
					&& temporadaSelecionada.equals(objectAnt)) {
				temporadaSelecionada = (String) object;
				break;
			}
			objectAnt = object;

		}
	}

	private void selecionaProximaTemporada() {
		resetPilotosRect();
		pilotoSelecionado = null;
		Object objectAnt = null;
		for (Iterator iterator = temporadas.iterator(); iterator.hasNext();) {
			Object object = (Object) iterator.next();
			if (temporadaSelecionada != null
					&& temporadaSelecionada.equals(objectAnt)) {
				temporadaSelecionada = (String) object;
				break;
			}
			objectAnt = object;
		}
	}

	private void resetPilotosRect() {
		for (int i = 0; i < 24; i++) {
			pilotosRect.get(i).setFrame(0, 0, 1, 1);
		}
	}

	private void menosTurbulencia() {
		if (turbulenciaSelecionado > 0) {
			turbulenciaSelecionado -= 50;
		}
	}

	private void maisTurbulencia() {
		if (turbulenciaSelecionado < 500) {
			turbulenciaSelecionado += 50;
		}

	}

	private void menosVoltas() {
		if (numVoltasSelecionado > 12) {
			numVoltasSelecionado--;
		}

	}

	private void maisVoltas() {
		if (numVoltasSelecionado < 73) {
			numVoltasSelecionado++;
		}

	}

	private void selecionaPistaAnterior() {
		Set keySet = controleJogo.getCircuitos().keySet();
		List list = new ArrayList(keySet);
		Object objectAnt = null;
		for (int i = list.size() - 1; i > -1; i--) {
			Object object = (Object) list.get(i);
			if (circuitoSelecionado != null
					&& circuitoSelecionado.equals(objectAnt)) {
				circuitoSelecionado = (String) object;
				break;
			}
			objectAnt = object;

		}
	}

	private void selecionaProximaPista() {
		Set keySet = controleJogo.getCircuitos().keySet();
		Object objectAnt = null;
		for (Iterator iterator = keySet.iterator(); iterator.hasNext();) {
			Object object = (Object) iterator.next();
			if (circuitoSelecionado != null
					&& circuitoSelecionado.equals(objectAnt)) {
				circuitoSelecionado = (String) object;
				break;
			}
			objectAnt = object;
		}

	}

	private void desenhaMenuCorridaSelecao(Graphics2D g2d) throws Exception {
		if (!MENU.equals(MENU_CORRIDA)) {
			return;
		}
		int x = (int) (getWidth() / 2);
		int y = (int) (getHeight() / 2);

		x -= 490;
		y -= 285;

		desenhaSeletorCircuito(g2d, x, y);

		desenhaClima(g2d, x + 40, y + 200);

		desenhaSeletorNumeroVoltas(g2d, x + 40, y + 280);

		desenhaTurbulencia(g2d, x + 40, y + 340);

		desenhaNivelCorrida(g2d, x + 40, y + 400);

		desenhaDrsKersPneusReabastecimento(g2d, x + 40, y + 460);

		desenhaTemporadas(g2d, x + 580, y, false);

		desenhaAnteriroProximo(g2d, x + 150, y + 600);

	}

	private void desenhaAnteriroProximo(Graphics2D g2d, int x, int y) {
		Font fontOri = g2d.getFont();
		g2d.setFont(new Font(fontOri.getName(), Font.BOLD, 24));
		String anteriorTxt = Lang.msg("anterior").toUpperCase();
		int larguraTexto = Util.larguraTexto(anteriorTxt, g2d);
		anteriroMenuRct.setFrame(x, y - 25, larguraTexto + 10, 30);
		g2d.setColor(lightWhite);
		g2d.fill(anteriroMenuRct);
		g2d.setColor(Color.BLACK);
		g2d.drawString(anteriorTxt, x, y);
		x += (larguraTexto + 40);
		String proximoTxt = Lang.msg("proximo").toUpperCase();
		larguraTexto = Util.larguraTexto(proximoTxt, g2d);
		proximoMenuRect.setFrame(x, y - 25, larguraTexto + 10, 30);
		g2d.setColor(lightWhite);
		g2d.fill(proximoMenuRect);
		g2d.setColor(Color.BLACK);
		g2d.drawString(proximoTxt, x, y);
		g2d.setFont(fontOri);
	}

	private void desenhaTemporadas(Graphics2D g2d, int x, int y,
			boolean campeonato) throws IOException {
		g2d.setColor(lightWhite);
		anteTemporadaRect.setFrame(x, y - 25, 30, 30);
		g2d.fill(anteTemporadaRect);
		g2d.drawImage(setaCarroEsquerda, x - 15, y - 55, null);
		x += 40;

		if (temporadaSelecionada == null) {
			temporadaSelecionada = (String) temporadas
					.get(temporadas.size() - 1);
		}

		Font fontOri = g2d.getFont();
		g2d.setFont(new Font(fontOri.getName(), Font.BOLD, 28));
		String txt = temporadaSelecionada.replaceAll("\\*", "");
		int larguraTexto = 120;
		g2d.setColor(lightWhite);
		g2d.fillRoundRect(x, y - 25, larguraTexto + 20, 30, 15, 15);
		g2d.setColor(Color.BLACK);
		g2d.drawString(txt.toUpperCase(),
				x + (130 - Util.larguraTexto(txt, g2d)) / 2, y);

		x += larguraTexto + 30;

		g2d.setColor(lightWhite);
		proxTemporadaRect.setFrame(x, y - 25, 30, 30);
		g2d.fill(proxTemporadaRect);
		g2d.drawImage(setaCarroDireita, x - 45, y - 52, null);
		x += 40;
		x -= (80 + larguraTexto);
		g2d.setFont(fontOri);
		String temporada = "t" + temporadaSelecionada;
		List pilotos = litasPilotosTemporada(temporada);
		y += 8;

		int limite = 0;

		if (campeonato) {
			if (InterfaceJogo.FACIL.equals(nivelSelecionado)) {
				limite = pilotos.size() - 6;
			}
			if (InterfaceJogo.NORMAL.equals(nivelSelecionado)) {
				limite = pilotos.size() - 4;
			}
			if (InterfaceJogo.DIFICIL.equals(nivelSelecionado)) {
				limite = pilotos.size() - 2;
			}
		}

		if (limite < 0) {
			limite = 0;
		}
		for (int i = 0; i < limite; i++) {
			pilotos.remove(0);
		}
		if (pilotoSelecionado == null) {
			pilotoSelecionado = (Piloto) pilotos.get(0);
		}
		for (int i = 0; i < pilotos.size(); i++) {
			Piloto piloto = (Piloto) pilotos.get(i);
			BufferedImage imageCarro = controleJogo.obterCarroLado(piloto);
			controleJogo.setTemporada(temporada);
			BufferedImage capacete = controleJogo.obterCapacete(piloto);
			int novoY = y + i * 24;
			if (i % 2 == 0) {
				if (PainelCircuito.desenhaImagens) {
					g2d.drawImage(imageCarro, x - 100, novoY, null);
					if (capacete != null)
						g2d.drawImage(capacete, x - 150, novoY, null);
				}
			} else {
				if (PainelCircuito.desenhaImagens) {
					g2d.drawImage(imageCarro, x + 100, novoY, null);
					if (capacete != null)
						g2d.drawImage(capacete, x + 290, novoY, null);
				}
			}

		}
		for (int i = 0; i < pilotos.size(); i++) {
			Piloto piloto = (Piloto) pilotos.get(i);
			int novoY = y + i * 24;
			if (i % 2 == 0) {
				desenhaNomePiloto(g2d, x - 80, novoY + 35, i + limite, piloto);
			} else {
				desenhaNomePiloto(g2d, x + 120, novoY + 35, i + limite, piloto);
			}
		}
	}

	private List litasPilotosTemporada(String temporada) {
		List pilotos = new ArrayList(
				(Collection) circuitosPilotos.get(temporada));
		Collections.sort(pilotos, new Comparator() {

			@Override
			public int compare(Object o1, Object o2) {
				Piloto p1 = (Piloto) o1;
				Piloto p2 = (Piloto) o2;

				return new Integer(p2.getCarro().getPotenciaReal())
						.compareTo(new Integer(p1.getCarro().getPotenciaReal()));
			}

		});
		return pilotos;
	}

	private void desenhaNomePiloto(Graphics2D g2d, int x, int y, int i,
			Piloto piloto) {
		Font fontOri = g2d.getFont();
		g2d.setFont(new Font(fontOri.getName(), Font.BOLD, fontOri.getSize()));
		RoundRectangle2D pilotoRect = pilotosRect.get(i);
		String nmPilotoStr = (piloto.getNome() + " " + piloto.getCarro()
				.getNome()).toUpperCase();
		int tamNmPiloto = Util.calculaLarguraText(nmPilotoStr, g2d);
		pilotoRect.setFrame(x - 15, y, tamNmPiloto + 10, 18);
		Color c = corRectPiloto(g2d, piloto, 1);
		if (!piloto.equals(pilotoSelecionado)) {
			g2d.setColor(lightWhite);
		}
		g2d.fill(pilotoRect);
		if (piloto.equals(pilotoSelecionado)) {
			g2d.setColor(corRectPiloto(g2d, piloto, 2));
			g2d.draw(pilotoRect);
		}
		corTxtPiloto(g2d, c);
		if (!piloto.equals(pilotoSelecionado)) {
			g2d.setColor(Color.BLACK);
		}
		g2d.drawString(nmPilotoStr, x - 10, y + 15);
		g2d.setFont(fontOri);
	}

	private void desenhaDrsKersPneusReabastecimento(Graphics2D g2d, int x, int y) {
		Font fontOri = g2d.getFont();
		g2d.setFont(new Font(fontOri.getName(), Font.BOLD, 28));

		int xOri = x;

		String drsTxt = Lang.msg("drs").toUpperCase();
		int tamDrs = Util.calculaLarguraText(drsTxt, g2d);
		drsRect.setFrame(x - 15, y - 12, tamDrs + 10, 32);
		g2d.setColor(lightWhite);
		g2d.fill(drsRect);
		if (this.drs) {
			g2d.setColor(yel);
			g2d.draw(drsRect);
		}
		g2d.setColor(Color.BLACK);
		g2d.drawString(drsTxt, x - 10, y + 15);

		x += (tamDrs + 30);

		String reabasteciemtoTxt = Lang.msg("reabasteciemto").toUpperCase();
		int tamReabasteciemto = Util.calculaLarguraText(reabasteciemtoTxt, g2d);
		reabasteciemtoRect.setFrame(x - 15, y - 12, tamReabasteciemto + 10, 32);
		g2d.setColor(lightWhite);
		g2d.fill(reabasteciemtoRect);
		if (reabasteciemto) {
			g2d.setColor(yel);
			g2d.draw(reabasteciemtoRect);
		}
		g2d.setColor(Color.BLACK);
		g2d.drawString(reabasteciemtoTxt, x - 10, y + 15);

		x = xOri;

		y += 50;

		String kersTxt = Lang.msg("kers").toUpperCase();
		int tamKers = Util.calculaLarguraText(kersTxt, g2d);
		kersRect.setFrame(x - 15, y - 12, tamKers + 10, 32);
		g2d.setColor(lightWhite);
		g2d.fill(kersRect);
		if (this.kers) {
			g2d.setColor(yel);
			g2d.draw(kersRect);
		}
		g2d.setColor(Color.BLACK);
		g2d.drawString(kersTxt, x - 10, y + 15);

		x += (tamKers + 30);

		String trocaPneusTxt = Lang.msg("trocaPneus").toUpperCase();
		int tamTrocaPneus = Util.calculaLarguraText(trocaPneusTxt, g2d);
		trocaPneusRect.setFrame(x - 15, y - 12, tamTrocaPneus + 10, 32);
		g2d.setColor(lightWhite);
		g2d.fill(trocaPneusRect);
		if (trocaPneus) {
			g2d.setColor(yel);
			g2d.draw(trocaPneusRect);
		}
		g2d.setColor(Color.BLACK);
		g2d.drawString(trocaPneusTxt, x - 10, y + 15);

		g2d.setFont(fontOri);
	}

	private void desenhaTurbulencia(Graphics2D g2d, int x, int y) {
		Font fontOri = g2d.getFont();
		g2d.setFont(new Font(fontOri.getName(), Font.BOLD, 28));

		String menos = "-";
		int tamMenos = Util.calculaLarguraText(menos, g2d);
		menosTurbulenciaRect.setFrame(x - 16, y - 6, tamMenos + 6, 22);
		g2d.setColor(lightWhite);
		g2d.fill(menosTurbulenciaRect);
		g2d.setColor(Color.BLACK);
		g2d.drawString(menos, x - 14, y + 15);

		x += 20;

		String turbulencia = Lang.msg("turbulencia").toUpperCase();
		int tamTurbulencia = Util.calculaLarguraText(turbulencia, g2d) + 10;
		g2d.setColor(lightWhite);
		g2d.fillRoundRect(x - 15, y - 12, tamTurbulencia, 32, 10, 10);

		int porcetTurbulencia = turbulenciaSelecionado / 5;

		int tamTurbulenciaSelecionado = porcetTurbulencia * tamTurbulencia
				/ 100;
		g2d.setColor(yel);
		g2d.drawRoundRect(x - 15, y - 12, tamTurbulenciaSelecionado, 32, 10, 10);
		g2d.setColor(blu);
		g2d.fillRoundRect(x - 15, y - 12, tamTurbulenciaSelecionado, 32, 10, 10);

		g2d.setColor(Color.BLACK);
		g2d.drawString(turbulencia, x - 10, y + 15);

		x += (tamTurbulencia + 15);

		String mais = "+";
		int tamMais = Util.calculaLarguraText(mais, g2d);
		maisTurbulenciaRect.setFrame(x - 17, y - 6, tamMais + 5, 22);
		g2d.setColor(lightWhite);
		g2d.fill(maisTurbulenciaRect);
		g2d.setColor(Color.BLACK);
		g2d.drawString(mais, x - 13, y + 16);

		g2d.setFont(fontOri);
	}

	private void desenhaCombustivel(Graphics2D g2d, int x, int y) {
		Font fontOri = g2d.getFont();
		g2d.setFont(new Font(fontOri.getName(), Font.BOLD, 28));

		String menos = "-";
		String combustivel = Lang.msg("combustivelbar").toUpperCase();
		String mais = "+";
		int tamMais = Util.calculaLarguraText(mais, g2d);
		int tamMenos = Util.calculaLarguraText(menos, g2d);
		int porcetCombustivel = combustivelSelecionado;
		int tamCombustivel = Util.calculaLarguraText(combustivel, g2d) + 10;
		int tamCombustivelSelecionado = porcetCombustivel * tamCombustivel
				/ 100;

		int somTam = tamMais + tamMenos + tamCombustivel + 35;

		x -= (somTam / 2);

		menosCombustivelRect.setFrame(x - 16, y - 6, tamMenos + 6, 22);
		g2d.setColor(lightWhite);
		g2d.fill(menosCombustivelRect);
		g2d.setColor(Color.BLACK);
		g2d.drawString(menos, x - 14, y + 15);

		x += 20;

		g2d.setColor(lightWhite);
		g2d.fillRoundRect(x - 15, y - 12, tamCombustivel, 32, 10, 10);

		g2d.setColor(yel);
		g2d.drawRoundRect(x - 15, y - 12, tamCombustivelSelecionado, 32, 10, 10);
		g2d.setColor(blu);
		g2d.fillRoundRect(x - 15, y - 12, tamCombustivelSelecionado, 32, 10, 10);

		g2d.setColor(Color.BLACK);
		g2d.drawString(combustivel, x - 10, y + 15);

		x += (tamCombustivel + 15);

		maisCombustivelRect.setFrame(x - 17, y - 6, tamMais + 5, 22);
		g2d.setColor(lightWhite);
		g2d.fill(maisCombustivelRect);
		g2d.setColor(Color.BLACK);
		g2d.drawString(mais, x - 13, y + 16);

		g2d.setFont(fontOri);
	}

	private void desenhaNivelCorrida(Graphics2D g2d, int x, int y) {
		Font fontOri = g2d.getFont();
		g2d.setFont(new Font(fontOri.getName(), Font.BOLD, 28));

		String facil = Lang.msg(InterfaceJogo.FACIL).toUpperCase();
		int tamFacil = Util.calculaLarguraText(facil, g2d);
		facilRect.setFrame(x - 15, y - 12, tamFacil + 10, 32);
		g2d.setColor(lightWhite);
		g2d.fill(facilRect);
		if (InterfaceJogo.FACIL.equals(nivelSelecionado)) {
			g2d.setColor(yel);
			g2d.draw(facilRect);
		}
		g2d.setColor(Color.BLACK);
		g2d.drawString(facil, x - 10, y + 15);

		x += (tamFacil + 15);

		String normal = Lang.msg(InterfaceJogo.NORMAL).toUpperCase();
		int tamNormal = Util.calculaLarguraText(normal, g2d);
		normalRect.setFrame(x - 15, y - 12, tamNormal + 10, 32);
		g2d.setColor(lightWhite);
		g2d.fill(normalRect);
		if (InterfaceJogo.NORMAL.equals(nivelSelecionado)) {
			g2d.setColor(yel);
			g2d.draw(normalRect);
		}
		g2d.setColor(Color.BLACK);
		g2d.drawString(normal, x - 10, y + 15);

		x += (tamNormal + 15);

		String dificil = Lang.msg(InterfaceJogo.DIFICIL).toUpperCase();
		int tamDificil = Util.calculaLarguraText(dificil, g2d);
		dificilRect.setFrame(x - 15, y - 12, tamDificil + 10, 32);
		g2d.setColor(lightWhite);
		g2d.fill(dificilRect);
		if (InterfaceJogo.DIFICIL.equals(nivelSelecionado)) {
			g2d.setColor(yel);
			g2d.draw(dificilRect);
		}
		g2d.setColor(Color.BLACK);
		g2d.drawString(dificil, x - 10, y + 15);

		g2d.setFont(fontOri);
	}

	private void desenhaTipoPneu(Graphics2D g2d, int x, int y) {
		Font fontOri = g2d.getFont();
		g2d.setFont(new Font(fontOri.getName(), Font.BOLD, 28));

		String mole = Lang.msg(Carro.TIPO_PNEU_MOLE).toUpperCase();
		int tamMole = Util.calculaLarguraText(mole, g2d);

		String chuva = Lang.msg(Carro.TIPO_PNEU_CHUVA).toUpperCase();
		int tamChuva = Util.calculaLarguraText(chuva, g2d);

		String duro = Lang.msg(Carro.TIPO_PNEU_DURO).toUpperCase();
		int tamDuro = Util.calculaLarguraText(duro, g2d);

		int somaTam = tamMole + tamDuro + tamChuva + 30;

		x -= somaTam / 2;

		pneuMoleRect.setFrame(x - 15, y - 12, tamMole + 10, 32);
		g2d.setColor(lightWhite);
		g2d.fill(pneuMoleRect);
		if (Carro.TIPO_PNEU_MOLE.equals(pneuSelecionado)) {
			g2d.setColor(yel);
			g2d.draw(pneuMoleRect);
		}
		g2d.setColor(Color.BLACK);
		g2d.drawString(mole, x - 10, y + 15);

		desenhaImgTipoPneu(g2d, x, y, tamMole, pneuMoleImg);

		x += (tamMole + 15);

		pneuDuroRect.setFrame(x - 15, y - 12, tamDuro + 10, 32);
		g2d.setColor(lightWhite);
		g2d.fill(pneuDuroRect);
		if (Carro.TIPO_PNEU_DURO.equals(pneuSelecionado)) {
			g2d.setColor(yel);
			g2d.draw(pneuDuroRect);
		}
		g2d.setColor(Color.BLACK);
		g2d.drawString(duro, x - 10, y + 15);

		desenhaImgTipoPneu(g2d, x, y, tamDuro, pneuDuroImg);

		x += (tamDuro + 15);

		pneuChuvaRect.setFrame(x - 15, y - 12, tamChuva + 10, 32);
		g2d.setColor(lightWhite);
		g2d.fill(pneuChuvaRect);
		if (Carro.TIPO_PNEU_CHUVA.equals(pneuSelecionado)) {
			g2d.setColor(yel);
			g2d.draw(pneuChuvaRect);
		}
		g2d.setColor(Color.BLACK);
		g2d.drawString(chuva, x - 10, y + 15);

		desenhaImgTipoPneu(g2d, x, y, tamChuva, pneuChuvaImg);

		g2d.setFont(fontOri);
	}

	private void desenhaTipoAsa(Graphics2D g2d, int x, int y) {
		Font fontOri = g2d.getFont();
		g2d.setFont(new Font(fontOri.getName(), Font.BOLD, 28));

		String mais = Lang.msg(Carro.MAIS_ASA).toUpperCase();
		int tamMais = Util.calculaLarguraText(mais, g2d);
		String normal = Lang.msg(Carro.ASA_NORMAL).toUpperCase();
		int tamDuro = Util.calculaLarguraText(normal, g2d);
		String menos = Lang.msg(Carro.MENOS_ASA).toUpperCase();
		int tamMenos = Util.calculaLarguraText(menos, g2d);

		int somaTam = tamMais + tamDuro + tamMenos + 30;

		x -= somaTam / 2;

		maisAsaRect.setFrame(x - 15, y - 12, tamMais + 10, 32);
		g2d.setColor(lightWhite);
		g2d.fill(maisAsaRect);
		if (Carro.MAIS_ASA.equals(asaSelecionado)) {
			g2d.setColor(yel);
			g2d.draw(maisAsaRect);
		}
		g2d.setColor(Color.BLACK);
		g2d.drawString(mais, x - 10, y + 15);

		x += (tamMais + 15);

		normalAsaRect.setFrame(x - 15, y - 12, tamDuro + 10, 32);
		g2d.setColor(lightWhite);
		g2d.fill(normalAsaRect);
		if (Carro.ASA_NORMAL.equals(asaSelecionado)) {
			g2d.setColor(yel);
			g2d.draw(normalAsaRect);
		}
		g2d.setColor(Color.BLACK);
		g2d.drawString(normal, x - 10, y + 15);

		x += (tamDuro + 15);

		menosAsaRect.setFrame(x - 15, y - 12, tamMenos + 10, 32);
		g2d.setColor(lightWhite);
		g2d.fill(menosAsaRect);
		if (Carro.MENOS_ASA.equals(asaSelecionado)) {
			g2d.setColor(yel);
			g2d.draw(menosAsaRect);
		}
		g2d.setColor(Color.BLACK);
		g2d.drawString(menos, x - 10, y + 15);

		g2d.setFont(fontOri);
	}

	private void desenhaImgTipoPneu(Graphics2D g2d, int x, int y, int tam,
			BufferedImage pneuImg) {
		g2d.setColor(lightWhite);
		int deslX = (tam / 2) - (pneuImg.getWidth() / 2);
		g2d.fillRoundRect(x + deslX, y - 65, pneuImg.getWidth() + 10,
				pneuImg.getHeight() + 2, 15, 15);
		if (PainelCircuito.desenhaImagens)
			g2d.drawImage(pneuImg, x + deslX + 5, y - 65, null);
	}

	private void desenhaClima(Graphics2D g2d, int x, int y) {
		solRect.setFrame(x, y, 35, 30);
		g2d.setColor(lightWhite);
		g2d.fill(solRect);
		if (Clima.SOL.equals(climaSelecionado)) {
			g2d.setColor(yel);
			g2d.draw(solRect);
		}
		g2d.drawImage(sol, x, y, null);

		x += 60;

		nubladoRect.setFrame(x, y, 35, 30);
		g2d.setColor(lightWhite);
		g2d.fill(nubladoRect);
		if (Clima.NUBLADO.equals(climaSelecionado)) {
			g2d.setColor(yel);
			g2d.draw(nubladoRect);
		}
		g2d.drawImage(nublado, x, y, null);

		x += 60;

		chuvaRect.setFrame(x, y, 35, 30);
		g2d.setColor(lightWhite);
		g2d.fill(chuvaRect);
		if (Clima.CHUVA.equals(climaSelecionado)) {
			g2d.setColor(yel);
			g2d.draw(chuvaRect);
		}
		g2d.drawImage(chuva, x, y, null);
	}

	private void desenhaSeletorNumeroVoltas(Graphics2D g2d, int x, int y) {
		Font fontOri = g2d.getFont();
		g2d.setFont(new Font(fontOri.getName(), Font.BOLD, 28));

		String menos = "-";
		int tamMenos = Util.calculaLarguraText(menos, g2d);
		menosVoltasRect.setFrame(x - 16, y - 6, tamMenos + 6, 22);
		g2d.setColor(lightWhite);
		g2d.fill(menosVoltasRect);
		g2d.setColor(Color.BLACK);
		g2d.drawString(menos, x - 14, y + 15);

		x += 20;

		String numVoltasStr = (numVoltasSelecionado + " " + Lang.msg("voltas"))
				.toUpperCase();
		int tamVoltas = Util.calculaLarguraText(numVoltasStr, g2d);
		numVoltasRect.setFrame(x - 15, y - 12, tamVoltas + 10, 32);
		g2d.setColor(lightWhite);
		g2d.fill(numVoltasRect);
		g2d.setColor(Color.BLACK);
		g2d.drawString(numVoltasStr, x - 10, y + 15);

		x += (tamVoltas + 15);

		String mais = "+";
		int tamMais = Util.calculaLarguraText(mais, g2d);
		maisVoltasRect.setFrame(x - 17, y - 6, tamMais + 5, 22);
		g2d.setColor(lightWhite);
		g2d.fill(maisVoltasRect);
		g2d.setColor(Color.BLACK);
		g2d.drawString(mais, x - 13, y + 16);

		g2d.setFont(fontOri);
	}

	private void desenhaSeletorCircuito(Graphics2D g2d, int centerX, int centerY) {
		g2d.setColor(lightWhite);
		antePistaRect.setFrame(centerX, centerY - 25, 30, 30);
		g2d.fill(antePistaRect);
		g2d.drawImage(setaCarroEsquerda, centerX - 15, centerY - 55, null);
		centerX += 40;

		if (circuitoSelecionado == null) {
			circuitoSelecionado = (String) controleJogo.getCircuitos().keySet()
					.iterator().next();
		}
		String nmCircuitoMRO = (String) controleJogo.getCircuitos().get(
				circuitoSelecionado);

		Font fontOri = g2d.getFont();
		g2d.setFont(new Font(fontOri.getName(), Font.BOLD, 28));
		String txt = circuitoSelecionado;
		int larguraTexto = 350;
		pistaRect.setFrame(centerX, centerY - 25, larguraTexto + 20, 30);
		g2d.setColor(lightWhite);
		g2d.fill(pistaRect);
		g2d.setColor(Color.BLACK);
		int incX = (320 - Util.larguraTexto(txt, g2d)) / 2;
		g2d.drawString(txt.toUpperCase(), centerX + incX, centerY);
		desenhaMiniCircuito(nmCircuitoMRO, g2d, centerX, centerY);

		centerX += larguraTexto + 30;

		g2d.setColor(lightWhite);
		proxPistaRect.setFrame(centerX, centerY - 25, 30, 30);
		g2d.fill(proxPistaRect);
		g2d.drawImage(setaCarroDireita, centerX - 45, centerY - 52, null);
		g2d.setFont(fontOri);

	}

	protected void desenhaMiniCircuito(String circuitoStr, Graphics2D g2d,
			int x, int y) {

		int maxLagura = 0;
		g2d.setStroke(new BasicStroke(3.0f));
		g2d.setColor(Color.BLACK);
		CarregadorRecursos carregadorRecursos = new CarregadorRecursos(false);
		ObjectInputStream ois;
		Circuito circuito = null;
		try {
			ois = new ObjectInputStream(carregadorRecursos.getClass()
					.getResourceAsStream(circuitoStr));
			circuito = (Circuito) ois.readObject();
			circuito.vetorizarPista();
		} catch (Exception e) {
			e.printStackTrace();
		}

		List pista = circuito.getPista();
		ArrayList pistaMinimizada = new ArrayList();
		double doubleMulti = 25;
		Map map = new HashMap();
		for (Iterator iterator = pista.iterator(); iterator.hasNext();) {
			No no = (No) iterator.next();
			Point p = new Point(no.getX(), no.getY());
			p.x /= doubleMulti;
			p.y /= doubleMulti;
			if (p.x > maxLagura) {
				maxLagura = p.x;
			}
			if (!pistaMinimizada.contains(p)) {
				map.put(p, no);
				pistaMinimizada.add(p);
			}

		}

		ArrayList boxMinimizado = new ArrayList();
		List box = circuito.getBox();
		for (Iterator iterator = box.iterator(); iterator.hasNext();) {
			No no = (No) iterator.next();
			Point p = new Point(no.getX(), no.getY());
			p.x /= doubleMulti;
			p.y /= doubleMulti;
			if (p.x > maxLagura) {
				maxLagura = p.x;
			}
			if (!boxMinimizado.contains(p))
				boxMinimizado.add(p);
		}

		int incX = (320 - maxLagura) / 2;

		Point o = new Point(10, 10);
		Point oldP = null;
		No ultNo = null;
		for (Iterator iterator = pistaMinimizada.iterator(); iterator.hasNext();) {
			Point p = (Point) iterator.next();
			if (oldP != null) {
				No no = (No) map.get(oldP);
				if (no.verificaCruvaBaixa()) {
					g2d.setColor(Color.red);
				} else if (no.verificaCruvaAlta()) {
					g2d.setColor(Color.orange);
				} else if (no.verificaRetaOuLargada()) {
					g2d.setColor(new Color(0, 200, 0));
				}
				g2d.drawLine(o.x + oldP.x + incX + x, o.y + oldP.y + y, o.x
						+ p.x + incX + x, o.y + p.y + y);
			}
			oldP = p;
			ultNo = (No) map.get(oldP);
		}
		Point p0 = (Point) pistaMinimizada.get(0);
		if (ultNo.verificaCruvaBaixa()) {
			g2d.setColor(Color.red);
		} else if (ultNo.verificaCruvaAlta()) {
			g2d.setColor(Color.orange);
		} else if (ultNo.verificaRetaOuLargada()) {
			g2d.setColor(new Color(0, 200, 0));
		}
		g2d.drawLine(o.x + oldP.x + incX + x, o.y + oldP.y + y, o.x + p0.x
				+ incX + x, o.y + p0.y + y);

		g2d.setStroke(new BasicStroke(2.0f));
		oldP = null;
		g2d.setColor(Color.lightGray);
		for (Iterator iterator = boxMinimizado.iterator(); iterator.hasNext();) {
			Point p = (Point) iterator.next();
			if (oldP != null) {
				g2d.drawLine(o.x + oldP.x + incX + x, o.y + oldP.y + y, o.x
						+ p.x + incX + x, o.y + p.y + y);
			}
			oldP = p;
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

	private void desenhaMenuPrincipalSelecao(Graphics2D g2d) {
		if (!MENU.equals(MENU_PRINCIPAL)) {
			return;
		}
		int centerX = (int) (getWidth() / 2.3);
		int centerY = (int) (getHeight() / 2.5);

		centerX -= 50;

		Font fontOri = g2d.getFont();
		g2d.setFont(new Font(fontOri.getName(), Font.BOLD, 28));

		g2d.setColor(lightWhite);
		String txt = Lang.msg("corrida").toUpperCase();
		int larguraTexto = Util.larguraTexto(txt, g2d);
		corridaRect.setFrame(centerX, centerY - 25, larguraTexto + 10, 30);
		g2d.fill(corridaRect);
		g2d.setColor(Color.BLACK);
		g2d.drawString(txt, centerX + 5, centerY);

		centerY += 40;

		g2d.setColor(lightWhite);
		txt = Lang.msg("campeonato").toUpperCase();
		larguraTexto = Util.larguraTexto(txt, g2d);
		campeonatoRect.setFrame(centerX, centerY - 25, larguraTexto + 10, 30);
		g2d.fill(campeonatoRect);
		g2d.setColor(Color.BLACK);
		g2d.drawString(txt, centerX + 5, centerY);

		centerY += 40;

		g2d.setColor(lightWhite);
		txt = Lang.msg("continuaCampeonato").toUpperCase();
		larguraTexto = Util.larguraTexto(txt, g2d);
		continuaCampeonatoRect.setFrame(centerX, centerY - 25,
				larguraTexto + 10, 30);
		g2d.fill(continuaCampeonatoRect);
		g2d.setColor(Color.BLACK);
		g2d.drawString(txt, centerX + 5, centerY);

		centerY += 40;

		g2d.setColor(lightWhite);
		txt = Lang.msg("sobre").toUpperCase();
		larguraTexto = Util.larguraTexto(txt, g2d);
		sobreRect.setFrame(centerX, centerY - 25, larguraTexto + 10, 30);
		g2d.fill(sobreRect);
		g2d.setColor(Color.BLACK);
		g2d.drawString(txt, centerX + 5, centerY);

		g2d.setFont(fontOri);
	}

	public RoundRectangle2D getCorridaRect() {
		return corridaRect;
	}

	public void setCorridaRect(RoundRectangle2D corridaRect) {
		this.corridaRect = corridaRect;
	}

	public RoundRectangle2D getCampeonatoRect() {
		return campeonatoRect;
	}

	public void setCampeonatoRect(RoundRectangle2D campeonatoRect) {
		this.campeonatoRect = campeonatoRect;
	}

	public RoundRectangle2D getContinuaCampeonatoRect() {
		return continuaCampeonatoRect;
	}

	public void setContinuaCampeonatoRect(
			RoundRectangle2D continuaCampeonatoRect) {
		this.continuaCampeonatoRect = continuaCampeonatoRect;
	}

	public RoundRectangle2D getSobreRect() {
		return sobreRect;
	}

	public void setSobreRect(RoundRectangle2D sobreRect) {
		this.sobreRect = sobreRect;
	}

	public RoundRectangle2D getProxPistaRect() {
		return proxPistaRect;
	}

	public void setProxPistaRect(RoundRectangle2D proxPistaRect) {
		this.proxPistaRect = proxPistaRect;
	}

	public RoundRectangle2D getAntePistaRect() {
		return antePistaRect;
	}

	public void setAntePistaRect(RoundRectangle2D antePistaRect) {
		this.antePistaRect = antePistaRect;
	}

	public RoundRectangle2D getProxTemporadaRect() {
		return proxTemporadaRect;
	}

	public void setProxTemporadaRect(RoundRectangle2D proxTemporadaRect) {
		this.proxTemporadaRect = proxTemporadaRect;
	}

	public RoundRectangle2D getAnteTemporadaRect() {
		return anteTemporadaRect;
	}

	public void setAnteTemporadaRect(RoundRectangle2D anteTemporadaRect) {
		this.anteTemporadaRect = anteTemporadaRect;
	}

	public RoundRectangle2D getPistaRect() {
		return pistaRect;
	}

	public void setPistaRect(RoundRectangle2D pistaRect) {
		this.pistaRect = pistaRect;
	}

	public RoundRectangle2D getMenosVoltasRect() {
		return menosVoltasRect;
	}

	public void setMenosVoltasRect(RoundRectangle2D menosVoltasRect) {
		this.menosVoltasRect = menosVoltasRect;
	}

	public RoundRectangle2D getMaisVoltasRect() {
		return maisVoltasRect;
	}

	public void setMaisVoltasRect(RoundRectangle2D maisVoltasRect) {
		this.maisVoltasRect = maisVoltasRect;
	}

	public RoundRectangle2D getMenosTurbulenciaRect() {
		return menosTurbulenciaRect;
	}

	public void setMenosTurbulenciaRect(RoundRectangle2D menosTurbulenciaRect) {
		this.menosTurbulenciaRect = menosTurbulenciaRect;
	}

	public RoundRectangle2D getMaisTurbulenciaRect() {
		return maisTurbulenciaRect;
	}

	public void setMaisTurbulenciaRect(RoundRectangle2D maisTurbulenciaRect) {
		this.maisTurbulenciaRect = maisTurbulenciaRect;
	}

	public RoundRectangle2D getNumVoltasRect() {
		return numVoltasRect;
	}

	public void setNumVoltasRect(RoundRectangle2D numVoltasRect) {
		this.numVoltasRect = numVoltasRect;
	}

	public RoundRectangle2D getSolRect() {
		return solRect;
	}

	public void setSolRect(RoundRectangle2D solRect) {
		this.solRect = solRect;
	}

	public RoundRectangle2D getChuvaRect() {
		return chuvaRect;
	}

	public void setChuvaRect(RoundRectangle2D chuvaRect) {
		this.chuvaRect = chuvaRect;
	}

	public RoundRectangle2D getNubladoRect() {
		return nubladoRect;
	}

	public void setNubladoRect(RoundRectangle2D nubladoRect) {
		this.nubladoRect = nubladoRect;
	}

	public RoundRectangle2D getFacilRect() {
		return facilRect;
	}

	public void setFacilRect(RoundRectangle2D facilRect) {
		this.facilRect = facilRect;
	}

	public RoundRectangle2D getNormalRect() {
		return normalRect;
	}

	public void setNormalRect(RoundRectangle2D normalRect) {
		this.normalRect = normalRect;
	}

	public RoundRectangle2D getDificilRect() {
		return dificilRect;
	}

	public void setDificilRect(RoundRectangle2D dificilRect) {
		this.dificilRect = dificilRect;
	}

	public RoundRectangle2D getDrsRect() {
		return drsRect;
	}

	public void setDrsRect(RoundRectangle2D drsRect) {
		this.drsRect = drsRect;
	}

	public RoundRectangle2D getKersRect() {
		return kersRect;
	}

	public void setKersRect(RoundRectangle2D kersRect) {
		this.kersRect = kersRect;
	}

	public RoundRectangle2D getTrocaPneusRect() {
		return trocaPneusRect;
	}

	public void setTrocaPneusRect(RoundRectangle2D trocaPneusRect) {
		this.trocaPneusRect = trocaPneusRect;
	}

	public RoundRectangle2D getReabasteciemtoRect() {
		return reabasteciemtoRect;
	}

	public void setReabasteciemtoRect(RoundRectangle2D reabasteciemtoRect) {
		this.reabasteciemtoRect = reabasteciemtoRect;
	}

	public RoundRectangle2D getProximoMenuRect() {
		return proximoMenuRect;
	}

	public void setProximoMenuRect(RoundRectangle2D proximoMenuRect) {
		this.proximoMenuRect = proximoMenuRect;
	}

	public RoundRectangle2D getAnteriroMenuRct() {
		return anteriroMenuRct;
	}

	public void setAnteriroMenuRct(RoundRectangle2D anteriroMenuRct) {
		this.anteriroMenuRct = anteriroMenuRct;
	}

	public List<RoundRectangle2D> getPilotosRect() {
		return pilotosRect;
	}

	public void setPilotosRect(List<RoundRectangle2D> pilotosRect) {
		this.pilotosRect = pilotosRect;
	}

}
