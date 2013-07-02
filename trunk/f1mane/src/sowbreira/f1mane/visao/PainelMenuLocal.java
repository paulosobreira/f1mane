package sowbreira.f1mane.visao;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

import sowbreira.f1mane.MainFrame;
import sowbreira.f1mane.controles.InterfaceJogo;
import sowbreira.f1mane.entidades.Circuito;
import sowbreira.f1mane.entidades.Clima;
import sowbreira.f1mane.entidades.No;
import sowbreira.f1mane.entidades.Piloto;
import sowbreira.f1mane.recursos.CarregadorRecursos;
import sowbreira.f1mane.recursos.idiomas.Lang;

import br.nnpe.GeoUtil;
import br.nnpe.ImageUtil;
import br.nnpe.Logger;
import br.nnpe.OcilaCor;
import br.nnpe.Util;

public class PainelMenuLocal extends JPanel {
	public static BufferedImage bg;

	public static String MENU_PRINCIPAL = "MENU_PRINCIPAL";

	public static String MENU_CORRIDA = "MENU_CORRIDA";

	private MainFrame mainFrame;
	private InterfaceJogo controleJogo;

	private String MENU = MENU_PRINCIPAL;

	public final static Color lightWhite = new Color(200, 200, 200, 100);

	public final static Color yel = new Color(255, 255, 0, 150);

	private RoundRectangle2D corrida = new RoundRectangle2D.Double(0, 0, 1, 1,
			10, 10);
	private RoundRectangle2D campeonato = new RoundRectangle2D.Double(0, 0, 1,
			1, 10, 10);

	private RoundRectangle2D continuaCampeonato = new RoundRectangle2D.Double(
			0, 0, 1, 1, 10, 10);

	private RoundRectangle2D sobre = new RoundRectangle2D.Double(0, 0, 1, 1,
			10, 10);

	private RoundRectangle2D proxPista = new RoundRectangle2D.Double(0, 0, 1,
			1, 10, 10);

	private RoundRectangle2D antePista = new RoundRectangle2D.Double(0, 0, 1,
			1, 10, 10);

	private RoundRectangle2D pistaRect = new RoundRectangle2D.Double(0, 0, 1,
			1, 10, 10);

	private RoundRectangle2D menosVoltasRect = new RoundRectangle2D.Double(0,
			0, 1, 1, 10, 10);

	private RoundRectangle2D maisVoltasRect = new RoundRectangle2D.Double(0, 0,
			1, 1, 10, 10);

	private RoundRectangle2D menosTurbulenciaRect = new RoundRectangle2D.Double(
			0, 0, 1, 1, 10, 10);

	private RoundRectangle2D maisTurbulenciaRect = new RoundRectangle2D.Double(
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

	private RoundRectangle2D drsRect = new RoundRectangle2D.Double(0, 0, 1, 1,
			10, 10);

	private RoundRectangle2D kersRect = new RoundRectangle2D.Double(0, 0, 1, 1,
			10, 10);

	private RoundRectangle2D trocaPneusRect = new RoundRectangle2D.Double(0, 0,
			1, 1, 10, 10);

	private RoundRectangle2D reabasteciemtoRect = new RoundRectangle2D.Double(
			0, 0, 1, 1, 10, 10);

	private Object selecionado = null;

	private String circuitoSelecionado = null;

	private int numVoltasSelecionado = 12;

	private int turbulenciaSelecionado = 250;

	private String climaSelecionado = Clima.SOL;

	private String nivelSelecionado = InterfaceJogo.NORMAL;

	private boolean kers = true;

	private boolean drs = true;

	private boolean trocaPneus = true;

	private boolean reabasteciemto = false;

	private BufferedImage setaCarroCima;
	private BufferedImage setaCarroBaixo;
	private BufferedImage setaCarroEsquerda;
	private BufferedImage setaCarroDireita;

	private BufferedImage sol;
	private BufferedImage chuva;
	private BufferedImage nublado;

	public PainelMenuLocal(MainFrame mainFrame, InterfaceJogo controleJogo) {
		this.mainFrame = mainFrame;
		this.controleJogo = controleJogo;
		addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				processaClick(e);
				super.mouseClicked(e);
			}
		});

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
	}

	protected void processaClick(MouseEvent e) {
		if (MENU.equals(MENU_PRINCIPAL) && corrida.contains(e.getPoint())) {
			selecionado = corrida;
			MENU = MENU_CORRIDA;
		}
		if (MENU.equals(MENU_PRINCIPAL) && campeonato.contains(e.getPoint())) {
			selecionado = campeonato;
		}
		if (MENU.equals(MENU_PRINCIPAL)
				&& continuaCampeonato.contains(e.getPoint())) {
			selecionado = continuaCampeonato;
		}
		if (MENU.equals(MENU_PRINCIPAL) && sobre.contains(e.getPoint())) {
			selecionado = sobre;
		}

		if (corrida.equals(selecionado)) {
			try {
				if (mainFrame.verificaCriarJogo()) {
					controleJogo = mainFrame.getControleJogo();
					controleJogo.setMainFrame(mainFrame);
					// controleJogo.iniciarJogo();
				}
			} catch (Exception e1) {
				e1.printStackTrace();
				Logger.logarExept(e1);
			}
		}
		if (campeonato.equals(selecionado)) {
			try {
				if (mainFrame.verificaCriarJogo()) {
					controleJogo = mainFrame.getControleJogo();
					controleJogo.criarCampeonatoPiloto();
				}

			} catch (Exception e1) {
				e1.printStackTrace();
				Logger.logarExept(e1);
			}
		}
		if (continuaCampeonato.equals(selecionado)) {
			try {
				if (mainFrame.verificaCriarJogo()) {
					controleJogo = mainFrame.getControleJogo();
					controleJogo.continuarCampeonato();
				}

			} catch (Exception e1) {
				e1.printStackTrace();
				Logger.logarExept(e1);
			}
		}
		if (sobre.equals(selecionado)) {
			try {
				mainFrame.mostraSobre();
			} catch (Exception e1) {
				e1.printStackTrace();
				Logger.logarExept(e1);
			}
		}
		if (proxPista.contains(e.getPoint())) {
			selecionaProximaPista();
		}
		if (antePista.contains(e.getPoint())) {
			selecionaPistaAnterior();
		}
		if (maisVoltasRect.contains(e.getPoint())) {
			maisVoltas();
		}
		if (menosVoltasRect.contains(e.getPoint())) {
			menosVoltas();
		}
		if (solRect.contains(e.getPoint())) {
			climaSelecionado = Clima.SOL;
		}
		if (nubladoRect.contains(e.getPoint())) {
			climaSelecionado = Clima.NUBLADO;
		}
		if (chuvaRect.contains(e.getPoint())) {
			climaSelecionado = Clima.CHUVA;
		}

		if (facilRect.contains(e.getPoint())) {
			nivelSelecionado = InterfaceJogo.FACIL;
		}
		if (normalRect.contains(e.getPoint())) {
			nivelSelecionado = InterfaceJogo.NORMAL;
		}
		if (dificilRect.contains(e.getPoint())) {
			nivelSelecionado = InterfaceJogo.DIFICIL;
		}

		if (maisTurbulenciaRect.contains(e.getPoint())) {
			maisTurbulencia();
		}
		if (menosTurbulenciaRect.contains(e.getPoint())) {
			menosTurbulencia();
		}

		if (drsRect.contains(e.getPoint())) {
			drs = !drs;
		}

		if (kersRect.contains(e.getPoint())) {
			kers = !kers;
		}

		if (trocaPneusRect.contains(e.getPoint())) {
			trocaPneus = !trocaPneus;
		}

		if (reabasteciemtoRect.contains(e.getPoint())) {
			reabasteciemto = !reabasteciemto;
		}

		repaint();

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
			desenhaMenuPrincipalSelecao(g2d);
			desenhaMenuCorridaSelecao(g2d);
		} catch (Exception e) {
			Logger.logarExept(e);
		}

	}

	private void desenhaMenuCorridaSelecao(Graphics2D g2d) {
		if (!MENU.equals(MENU_CORRIDA)) {
			return;
		}
		int x = (int) (getWidth() / 2);
		int y = (int) (getHeight() / 2);

		x -= 490;
		y -= 280;

		desenhaSeletorCircuito(g2d, x, y);

		desenhaSeletorNumeroVoltas(g2d, x + 80, y + 220);

		desenhaClima(g2d, x + 90, y + 280);

		desenhaNivelCorrida(g2d, x + 40, y + 340);

		desenhaTurbulencia(g2d, x + 60, y + 400);

		desenhaDrsKersPneusReabastecimento(g2d, x + 40, y + 460);

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

		String turbulencia = Lang.msg("turbulencia");
		int tamTurbulencia = Util.calculaLarguraText(turbulencia, g2d) + 10;
		g2d.setColor(lightWhite);
		g2d.fillRoundRect(x - 15, y - 12, tamTurbulencia, 32, 10, 10);

		int porcetTurbulencia = turbulenciaSelecionado / 5;

		int tamTurbulenciaSelecionado = porcetTurbulencia * tamTurbulencia
				/ 100;
		g2d.setColor(yel);
		g2d.drawRoundRect(x - 15, y - 12, tamTurbulenciaSelecionado, 32, 10, 10);

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

		String numVoltasStr = "" + numVoltasSelecionado + " "
				+ Lang.msg("voltas");
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
		antePista.setFrame(centerX, centerY - 25, 30, 30);
		g2d.fill(antePista);
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
		int larguraTexto = 350;// Util.larguraTexto(txt, g2d);
		pistaRect.setFrame(centerX, centerY - 25, larguraTexto + 10, 30);
		g2d.setColor(lightWhite);
		g2d.fill(pistaRect);
		g2d.setColor(Color.BLACK);
		g2d.drawString(txt.toUpperCase(), centerX + 5, centerY);

		centerX += larguraTexto + 20;

		g2d.setColor(lightWhite);
		proxPista.setFrame(centerX, centerY - 25, 30, 30);
		g2d.fill(proxPista);
		g2d.drawImage(setaCarroDireita, centerX - 45, centerY - 52, null);
		centerX += 40;
		centerX -= (80 + larguraTexto);
		desenhaMiniCircuito(nmCircuitoMRO, g2d, centerX, centerY);
		g2d.setFont(fontOri);
	}

	protected void desenhaMiniCircuito(String circuitoStr, Graphics2D g2d,
			int x, int y) {
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
			p.x += x;
			p.y += y;
			if (!pistaMinimizada.contains(p)) {
				map.put(p, no);
				pistaMinimizada.add(p);
			}

		}
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
				g2d.drawLine(o.x + oldP.x, o.y + oldP.y, o.x + p.x, o.y + p.y);
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
		g2d.drawLine(o.x + oldP.x, o.y + oldP.y, o.x + p0.x, o.y + p0.y);

		ArrayList boxMinimizado = new ArrayList();
		List box = circuito.getBox();
		for (Iterator iterator = box.iterator(); iterator.hasNext();) {
			No no = (No) iterator.next();
			Point p = new Point(no.getX(), no.getY());
			p.x /= doubleMulti;
			p.y /= doubleMulti;
			p.x += x;
			p.y += y;
			if (!boxMinimizado.contains(p))
				boxMinimizado.add(p);
		}
		g2d.setStroke(new BasicStroke(2.0f));
		oldP = null;
		g2d.setColor(Color.lightGray);
		for (Iterator iterator = boxMinimizado.iterator(); iterator.hasNext();) {
			Point p = (Point) iterator.next();
			if (oldP != null) {
				g2d.drawLine(o.x + oldP.x, o.y + oldP.y, o.x + p.x, o.y + p.y);
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
		corrida.setFrame(centerX, centerY - 25, larguraTexto + 10, 30);
		g2d.fill(corrida);
		if (corrida.equals(selecionado)) {
			g2d.setColor(yel);
			g2d.draw(corrida);
		}
		g2d.setColor(Color.BLACK);
		g2d.drawString(txt, centerX + 5, centerY);

		centerY += 40;

		g2d.setColor(lightWhite);
		txt = Lang.msg("campeonato").toUpperCase();
		larguraTexto = Util.larguraTexto(txt, g2d);
		campeonato.setFrame(centerX, centerY - 25, larguraTexto + 10, 30);
		g2d.fill(campeonato);
		if (campeonato.equals(selecionado)) {
			g2d.setColor(yel);
			g2d.draw(campeonato);
		}
		g2d.setColor(Color.BLACK);
		g2d.drawString(txt, centerX + 5, centerY);

		centerY += 40;

		g2d.setColor(lightWhite);
		txt = Lang.msg("continuaCampeonato").toUpperCase();
		larguraTexto = Util.larguraTexto(txt, g2d);
		continuaCampeonato.setFrame(centerX, centerY - 25, larguraTexto + 10,
				30);
		g2d.fill(continuaCampeonato);
		if (continuaCampeonato.equals(selecionado)) {
			g2d.setColor(yel);
			g2d.draw(continuaCampeonato);
		}
		g2d.setColor(Color.BLACK);
		g2d.drawString(txt, centerX + 5, centerY);

		centerY += 40;

		g2d.setColor(lightWhite);
		txt = Lang.msg("sobre").toUpperCase();
		larguraTexto = Util.larguraTexto(txt, g2d);
		sobre.setFrame(centerX, centerY - 25, larguraTexto + 10, 30);
		g2d.fill(sobre);
		if (sobre.equals(selecionado)) {
			g2d.setColor(yel);
			g2d.draw(sobre);
		}
		g2d.setColor(Color.BLACK);
		g2d.drawString(txt, centerX + 5, centerY);

		g2d.setFont(fontOri);
	}
}
