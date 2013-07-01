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
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

import sowbreira.f1mane.MainFrame;
import sowbreira.f1mane.controles.InterfaceJogo;
import sowbreira.f1mane.entidades.Circuito;
import sowbreira.f1mane.entidades.No;
import sowbreira.f1mane.entidades.Piloto;
import sowbreira.f1mane.recursos.CarregadorRecursos;
import sowbreira.f1mane.recursos.idiomas.Lang;

import br.nnpe.GeoUtil;
import br.nnpe.Logger;
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

	private Object selecionado = null;

	public PainelMenuLocal(MainFrame mainFrame, InterfaceJogo controleJogo) {
		this.mainFrame = mainFrame;
		this.controleJogo = controleJogo;
		addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				processaClick(e);
				super.mouseClicked(e);
			}
		});
	}

	protected void processaClick(MouseEvent e) {
		if (corrida.contains(e.getPoint())) {
			selecionado = corrida;
			MENU = MENU_CORRIDA;
		} else if (campeonato.contains(e.getPoint())) {
			selecionado = campeonato;
		} else if (continuaCampeonato.contains(e.getPoint())) {
			selecionado = continuaCampeonato;
		} else if (sobre.contains(e.getPoint())) {
			selecionado = sobre;
		} else {
			selecionado = null;
		}
		if (corrida.equals(selecionado)) {
			try {
				if (mainFrame.verificaCriarJogo()) {
					controleJogo = mainFrame.getControleJogo();
					controleJogo.setMainFrame(mainFrame);
					controleJogo.iniciarJogo();
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
		repaint();

	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		try {
			Graphics2D g2d = (Graphics2D) g;
			setarHints(g2d);
			if (PainelCircuito.carregaBkg)
				bg = CarregadorRecursos.carregaBufferedImage("f1bg.png");
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
		int centerX = 50;
		int centerY = 100;

		String circuitoStr = (String) controleJogo.getCircuitos().get(
				"Interlagos");

		desenhaMiniCircuito(circuitoStr, g2d, centerX, centerY);
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
		String txt = Lang.msg("corrida");
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
		txt = Lang.msg("campeonato");
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
		txt = Lang.msg("continuaCampeonato");
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
		txt = Lang.msg("sobre");
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
