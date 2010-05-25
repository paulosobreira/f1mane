/**
 * @version 1.00 1999-09-11
 * @author Cay Horstmann
 */

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.awt.geom.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;

import sowbreira.f1mane.recursos.CarregadorRecursos;
import br.nnpe.ImageUtil;

public class CompositeTest {
	public static void main(String[] args) {
		JFrame frame = new CompositeTestFrame();
		frame.show();
	}
}

class CompositeTestFrame extends JFrame implements ActionListener,
		ChangeListener {
	public CompositeTestFrame() {
		setTitle("CompositeTest");
		setSize(400, 400);
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});

		Container contentPane = getContentPane();
		canvas = new CompositePanel();
		contentPane.add(canvas, "Center");

		ruleCombo = new JComboBox();
		ruleCombo.addItem(Color.RED);
		ruleCombo.addItem(Color.BLUE);
		ruleCombo.addItem(Color.GREEN);
		ruleCombo.addItem(Color.black);
		// ruleCombo.addItem("DST_OVER");
		// ruleCombo.addItem("SRC_IN");
		// ruleCombo.addItem("SRC_OUT");
		// ruleCombo.addItem("DST_IN");
		// ruleCombo.addItem("DST_OUT");
		ruleCombo.addActionListener(this);

		alphaSlider = new JSlider();
		alphaSlider.addChangeListener(this);
		JPanel panel = new JPanel();
		panel.add(ruleCombo);
		panel.add(new JLabel("Alpha"));
		panel.add(alphaSlider);
		contentPane.add(panel, "North");

		explanation = new JTextField();
		contentPane.add(explanation, "South");

		// canvas.setAlpha(alphaSlider.getValue());
		canvas.setRule((Color) ruleCombo.getSelectedItem());
	}

	public void stateChanged(ChangeEvent event) {
		canvas.setAlpha(alphaSlider.getValue());
	}

	public void actionPerformed(ActionEvent event) {
		canvas.setRule((Color) ruleCombo.getSelectedItem());
	}

	private CompositePanel canvas;
	private JComboBox ruleCombo;
	private JSlider alphaSlider;
	private JTextField explanation;
}

class CompositePanel extends JPanel {
	private Color color;

	public CompositePanel() {
		shape1 = new Ellipse2D.Double(100, 100, 150, 100);
		shape2 = new Rectangle2D.Double(150, 150, 150, 100);
	}

	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D) g;

		// BufferedImage image = new BufferedImage(getWidth(), getHeight(),
		// BufferedImage.TYPE_INT_ARGB);
		// Graphics2D gImage = image.createGraphics();
		ImageIcon img = new ImageIcon(CompositeTest.class
				.getResource("car.png"));
		BufferedImage srcBufferedImage = new BufferedImage(img.getIconWidth(),
				img.getIconHeight(), BufferedImage.TYPE_INT_ARGB);
		srcBufferedImage.getGraphics().drawImage(img.getImage(), 0, 0, null);
		BufferedImage bufferedImageRetorno = new BufferedImage(img
				.getIconWidth(), img.getIconHeight(),
				BufferedImage.TYPE_INT_ARGB);
		Raster srcRaster = srcBufferedImage.getData();
		WritableRaster destRaster = bufferedImageRetorno.getRaster();
		int[] argbArray = new int[4];
		for (int i = 0; i < img.getIconWidth(); i++) {
			for (int j = 0; j < img.getIconHeight(); j++) {
				argbArray = new int[4];
				argbArray = srcRaster.getPixel(i, j, argbArray);

				Color c = new Color(argbArray[0], argbArray[1], argbArray[2],
						argbArray[3]);
				if (argbArray[0] < 50 && argbArray[1] < 50 && argbArray[2] < 50) {
					continue;
				}
				argbArray[0] = (int) ((argbArray[0] + color.getRed()) / 2);
				argbArray[1] = (int) ((argbArray[1] + color.getGreen()) / 2);
				argbArray[2] = (int) ((argbArray[2] + color.getBlue()) / 2);
				// argbArray[0] = color.getRed();
				// argbArray[1] = color.getGreen();
				// argbArray[2] = color.getBlue();
				// argbArray[3] = 2;
				destRaster.setPixel(i, j, argbArray);
			}
		}
		// gImage.drawImage(bufferedImageRetorno, 180, 150, null);
		g2.drawImage(bufferedImageRetorno, 0, 0, null);
	}

	public void setRule(Color r) {
		color = r;
		repaint();
	}

	public void setAlpha(int a) {
		alpha = (float) a / 100.0F;
		repaint();
	}

	private Shape shape1;
	private Shape shape2;
	private float alpha;
	private int rule;
	private String porterDuff1; // row 1 of the rule diagram
	private String porterDuff2; // row 2 of the rule diagram
}
