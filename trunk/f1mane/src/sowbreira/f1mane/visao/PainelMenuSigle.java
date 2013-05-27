package sowbreira.f1mane.visao;

import java.awt.Graphics;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

import sowbreira.f1mane.MainFrame;
import sowbreira.f1mane.controles.InterfaceJogo;
import sowbreira.f1mane.recursos.CarregadorRecursos;

import br.nnpe.Logger;

public class PainelMenuSigle extends JPanel {
	public static BufferedImage bg;
	private MainFrame mainFrame;
	private InterfaceJogo controleJogo;

	public PainelMenuSigle(MainFrame mainFrame, InterfaceJogo controleJogo) {
		this.mainFrame = mainFrame;
		this.controleJogo = controleJogo;
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		try {
			if (PainelCircuito.carregaBkg)
				bg = CarregadorRecursos.carregaBufferedImage("f1bg.png");
			if (bg != null)
				g.drawImage(bg, 0, 0, null);
		} catch (Exception e) {
			Logger.logarExept(e);
		}
	}

}
