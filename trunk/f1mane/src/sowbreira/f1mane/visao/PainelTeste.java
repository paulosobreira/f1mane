package sowbreira.f1mane.visao;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

import sowbreira.f1mane.recursos.CarregadorRecursos;

public class PainelTeste {

	static BufferedImage bg = CarregadorRecursos
			.carregaBufferedImageTransparecia("istambul_mro.jpg", null);
	static double zoom = 1;
	static int x, y;
	static double shx, shy;
	static BufferedImage desenha = null;
	static private double theta;

	public static void main(String[] args) {
		shx = 0;
		shy = 0;
		JFrame frame = new JFrame();
		final JPanel jPanel = new JPanel() {
			@Override
			protected void paintComponent(Graphics g) {
				super.paintComponent(g);
				if (desenha != null)
					g.drawImage(desenha, 1, 1, null);
			}
		};
		frame.getContentPane().add(jPanel);
		frame.setSize(new Dimension(500, 500));
		frame.setVisible(true);
		frame.addKeyListener(new KeyAdapter() {

			@Override
			public void keyPressed(KeyEvent e) {

				if (e.getKeyCode() == KeyEvent.VK_UP) {
					y -= 15;
				}
				if (e.getKeyCode() == KeyEvent.VK_DOWN) {
					y += 15;
				}
				if (e.getKeyCode() == KeyEvent.VK_LEFT) {
					x -= 15;
				}
				if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
					x += 15;
				}

				if (e.getKeyCode() == KeyEvent.VK_EQUALS) {
					zoom += 0.01;
				}
				if (e.getKeyCode() == KeyEvent.VK_MINUS) {
					zoom -= 0.01;
				}

				int width = (int) (jPanel.getWidth() / (zoom <= 1 ? zoom : 1));
				int height = (int) (jPanel.getHeight() / (zoom <= 1 ? zoom : 1));

				while (x + width > bg.getWidth()) {
					x--;
				}
				while (y + height > bg.getHeight()) {
					y--;
				}

				if (width > bg.getWidth()) {
					width = bg.getWidth();
				}

				if (height > bg.getHeight()) {
					height = bg.getHeight();
				}

				if (x < 0) {
					x = 0;
				}
				if (y < 0) {
					y = 0;
				}
				System.out.println("shx" + shx);
				System.out.println("shy" + shy);
				BufferedImage sub = bg.getSubimage(x, y, width, height);
				BufferedImage dst = new BufferedImage(width, height,
						BufferedImage.TYPE_INT_ARGB);
				BufferedImage dst2 = new BufferedImage(width, height,
						BufferedImage.TYPE_INT_ARGB);
				AffineTransform affineTransform = AffineTransform
						.getScaleInstance(zoom, zoom);
				if (e.getKeyCode() == KeyEvent.VK_A) {
					shx += 0.01;
				}
				if (e.getKeyCode() == KeyEvent.VK_Z) {
					shx -= 0.01;
				}
				if (e.getKeyCode() == KeyEvent.VK_S) {
					shy += 0.01;
				}
				if (e.getKeyCode() == KeyEvent.VK_X) {
					shy -= 0.01;
				}

				if (e.getKeyCode() == KeyEvent.VK_Q) {
					theta += 0.01;
				}
				if (e.getKeyCode() == KeyEvent.VK_W) {
					theta -= 0.01;
				}
				affineTransform.setToScale(shx, shy);

				AffineTransformOp affineTransformOp = new AffineTransformOp(
						affineTransform, AffineTransformOp.TYPE_BILINEAR);
				affineTransformOp.filter(sub, dst);

				AffineTransform affineTransform2 = AffineTransform
						.getScaleInstance(zoom, zoom);

				affineTransform2.setToRotation(theta);
				AffineTransformOp affineTransformOp2 = new AffineTransformOp(
						affineTransform2, AffineTransformOp.TYPE_BILINEAR);
				affineTransformOp2.filter(dst, dst2);

				desenha = dst2;
				jPanel.repaint();
				super.keyPressed(e);
			}
		});
		BufferedImage sub = bg.getSubimage(x, y, jPanel.getWidth(),
				jPanel.getHeight());
		desenha = sub;
		jPanel.repaint();
		frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

	}

}
