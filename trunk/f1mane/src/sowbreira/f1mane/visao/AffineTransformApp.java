package sowbreira.f1mane.visao;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import sowbreira.f1mane.recursos.CarregadorRecursos;

public class AffineTransformApp extends JFrame {
	DisplayPanel displayPanel;

	JComboBox scaleXval, scaleYval, shearXval, shearYval;

	String[] scaleValues = { "0.10", "0.25", "0.50", "0.75", "1.00", "1.25",
			"1.50", "1.75", "2.00" };

	String[] shearValues = { "0.00", "0.25", "0.50", "0.75", "1.00" };

	public AffineTransformApp() {
		super();
		Container container = getContentPane();

		displayPanel = new DisplayPanel();
		container.add(displayPanel);

		JPanel panel = new JPanel();
		panel.setLayout(new GridLayout(2, 4, 5, 5));
		scaleXval = new JComboBox(scaleValues);
		scaleXval.setSelectedItem("1.00");
		scaleXval.addActionListener(new ComboBoxListener());
		scaleYval = new JComboBox(scaleValues);
		scaleYval.setSelectedItem("1.00");
		scaleYval.addActionListener(new ComboBoxListener());

		shearXval = new JComboBox(shearValues);
		shearXval.setSelectedItem("0.00");
		shearXval.addActionListener(new ComboBoxListener());
		shearYval = new JComboBox(shearValues);
		shearYval.setSelectedItem("0.00");
		shearYval.addActionListener(new ComboBoxListener());

		panel.add(new JLabel("Scale X value:"));
		panel.add(scaleXval);
		panel.add(new JLabel("Scale Y value:"));
		panel.add(scaleYval);
		panel.add(new JLabel("Shear X value:"));
		panel.add(shearXval);
		panel.add(new JLabel("Shear Y value:"));
		panel.add(shearYval);

		container.add(BorderLayout.SOUTH, panel);

		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});
		setSize(displayPanel.getWidth(), displayPanel.getHeight() + 10);
		setVisible(true);
	}

	public static void main(String arg[]) {
		new AffineTransformApp();
	}

	class ComboBoxListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			JComboBox temp = (JComboBox) e.getSource();

			if (temp == scaleXval) {
				displayPanel.scalex = Double.parseDouble((String) temp
						.getSelectedItem());
				displayPanel.applyValue(true, false);
				displayPanel.applyFilter();
				displayPanel.repaint();
			} else if (temp == scaleYval) {
				displayPanel.scaley = Double.parseDouble((String) temp
						.getSelectedItem());
				displayPanel.applyValue(true, false);
				displayPanel.applyFilter();
				displayPanel.repaint();
			} else if (temp == shearXval) {
				displayPanel.shearx = Double.parseDouble((String) temp
						.getSelectedItem());
				displayPanel.applyValue(false, true);
				displayPanel.applyFilter();
				displayPanel.repaint();
			} else if (temp == shearYval) {
				displayPanel.sheary = Double.parseDouble((String) temp
						.getSelectedItem());
				displayPanel.applyValue(false, true);
				displayPanel.applyFilter();
				displayPanel.repaint();
			}
		}
	}
}

class DisplayPanel extends JLabel {
	Image displayImage;

	BufferedImage biSrc, biDest;

	BufferedImage bi;

	Graphics2D big;

	AffineTransform transform;

	double scalex = 1.0;

	double scaley = 1.0;

	double shearx = 1.0;

	double sheary = 1.0;

	DisplayPanel() {
		setBackground(Color.black);
		loadImage();
		setSize(displayImage.getWidth(this), displayImage.getWidth(this)); // panel
		createBufferedImages();

		transform = new AffineTransform();
	}

	public void loadImage() {
		displayImage = new ImageIcon(
				CarregadorRecursos.carregarImagem("emirados.JPG")).getImage();

		MediaTracker mt = new MediaTracker(this);
		mt.addImage(displayImage, 1);
		try {
			mt.waitForAll();
		} catch (Exception e) {
			System.out.println("Exception while loading.");
		}

		if (displayImage.getWidth(this) == -1) {
			System.out.println(" Missing .jpg file");
			System.exit(0);
		}
	}

	public void createBufferedImages() {
		biSrc = new BufferedImage(displayImage.getWidth(this),
				displayImage.getHeight(this), BufferedImage.TYPE_INT_RGB);

		big = biSrc.createGraphics();
		big.drawImage(displayImage, 0, 0, this);

		bi = biSrc;

		biDest = new BufferedImage(displayImage.getWidth(this),
				displayImage.getHeight(this), BufferedImage.TYPE_INT_RGB);
	}

	public void applyValue(boolean scale, boolean shear) {
		if (scale) {
			transform.setToScale(scalex, scaley);
			scale = false;
		} else if (shear) {
			transform.setToShear(shearx, sheary);
			shear = false;
		}
	}

	public void applyFilter() {
		AffineTransformOp op = new AffineTransformOp(transform, null);
		Graphics2D biDestG2D = biDest.createGraphics();
		biDestG2D
				.clearRect(0, 0, biDest.getWidth(this), biDest.getHeight(this));
		op.filter(biSrc, biDest);
		bi = biDest;
	}

	public void reset() {
		big.setColor(Color.black);
		big.clearRect(0, 0, bi.getWidth(this), bi.getHeight(this));
		big.drawImage(displayImage, 0, 0, this);
	}

	public void update(Graphics g) {
		g.clearRect(0, 0, getWidth(), getHeight());
		paintComponent(g);
	}

	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2D = (Graphics2D) g;
		g2D.drawImage(bi, 0, 0, this);
	}
}
