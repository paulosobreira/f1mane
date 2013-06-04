package sowbreira.f1mane.visao;

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

import javax.swing.JPanel;

import sowbreira.f1mane.MainFrame;
import sowbreira.f1mane.controles.InterfaceJogo;
import sowbreira.f1mane.entidades.Piloto;
import sowbreira.f1mane.recursos.CarregadorRecursos;
import sowbreira.f1mane.recursos.idiomas.Lang;

import br.nnpe.GeoUtil;
import br.nnpe.Logger;
import br.nnpe.Util;

public class PainelMenuSigle extends JPanel {
	public static BufferedImage bg;
	private MainFrame mainFrame;
	private InterfaceJogo controleJogo;
	public final static Color lightWhite = new Color(255, 255, 255, 100);

	public final static Color yel = new Color(255, 255, 0, 150);

	private RoundRectangle2D corrida = new RoundRectangle2D.Double(0, 0, 1, 1,
			10, 10);
	private RoundRectangle2D campeonato = new RoundRectangle2D.Double(0, 0, 1,
			1, 10, 10);

	private Object selecionado = null;

	public PainelMenuSigle(MainFrame mainFrame, InterfaceJogo controleJogo) {
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
		}
		if (campeonato.contains(e.getPoint())) {
			selecionado = campeonato;
		}
		if (corrida.equals(selecionado)) {
			try {
				if (mainFrame.verificaCriarJogo()) {
					controleJogo = mainFrame.getControleJogo(); 
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
			desenhaMenuSelecao(g2d);
		} catch (Exception e) {
			Logger.logarExept(e);
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

	private void desenhaMenuSelecao(Graphics2D g2d) {
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

		g2d.setFont(fontOri);
	}
}
