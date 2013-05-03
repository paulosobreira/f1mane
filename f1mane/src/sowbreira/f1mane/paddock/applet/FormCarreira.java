package sowbreira.f1mane.paddock.applet;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

import sowbreira.f1mane.recursos.CarregadorRecursos;
import sowbreira.f1mane.recursos.idiomas.Lang;
import br.nnpe.ImageUtil;
import br.nnpe.Logger;

/**
 * @author Paulo Sobreira Criado em 27/06/2009 as 15:48:51
 */
public class FormCarreira extends JPanel {

	private JLabel labelPtsCarreira = new JLabel("Pontos Carreira:") {
		@Override
		public String getText() {
			return Lang.msg("266");
		}
	};
	JLabel ptsCarreiraVal = new JLabel("0") {
		public String getText() {
			return String.valueOf(ptsCarreira);
		}
	};
	private JLabel labelModoCarreira = new JLabel("Modo Carreira:") {
		@Override
		public String getText() {
			return Lang.msg("252");
		}
	};
	private JCheckBox modoCarreira = new JCheckBox();
	private JLabel labelNomePiloto = new JLabel("Nome Piloto:") {
		@Override
		public String getText() {
			return Lang.msg("253");
		}
	};
	private JTextField nomePiloto = new JTextField(10);
	private JLabel labelNomeCarro = new JLabel("Nome Equipe:") {
		@Override
		public String getText() {
			return Lang.msg("254");
		}
	};
	private JTextField nomeCarro = new JTextField(10);
	private JLabel labelPtsPiloto = new JLabel("Habilidade Piloto:") {
		@Override
		public String getText() {
			return Lang.msg("255");
		}
	};
	private JSpinner ptsPiloto = new JSpinner();
	private JLabel labelPtsCarro = new JLabel("Pontencia Carro:") {
		@Override
		public String getText() {
			return Lang.msg("256");
		}
	};
	private JSpinner ptsCarro = new JSpinner();
	private JLabel labelCor1 = new JLabel("Cor da equipe 1:");
	private JLabel labelCor2 = new JLabel("Cor da equipe 2:");

	private List listaCarro = new ArrayList();
	private JLabel imgCarroLado = new JLabel();
	private JLabel imgCarroCima = new JLabel();
	private String imgCarroStr = "";
	private int ptsCarreira = 1;

	public FormCarreira() {
		JPanel panel = new JPanel();
		panel.setLayout(new GridLayout(3, 4));

		panel.add(labelModoCarreira);
		panel.add(modoCarreira);
		panel.add(labelPtsCarreira);
		panel.add(ptsCarreiraVal);

		panel.add(labelNomePiloto);
		panel.add(nomePiloto);
		panel.add(labelPtsPiloto);
		panel.add(ptsPiloto);

		panel.add(labelNomeCarro);
		panel.add(nomeCarro);
		panel.add(labelPtsCarro);
		panel.add(ptsCarro);

		JPanel panel2 = new JPanel();
		panel2.setLayout(new GridLayout(1, 2));

		panel2.add(labelCor1);
		panel2.add(labelCor2);

		JPanel panel3 = new JPanel();
		panel3.setLayout(new GridLayout(1, 2));
		panel3.add(imgCarroCima);
		panel3.add(imgCarroLado);
		labelCor1.setOpaque(true);
		labelCor1.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent e) {
				Color color = JColorChooser.showDialog(FormCarreira.this,
						"Escolha uma cor", Color.WHITE);
				setCor1(color);
				gerarCarroLado();
				gerarCarroCima();
			}

		});
		labelCor2.setOpaque(true);
		labelCor2.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent e) {
				Color color = JColorChooser.showDialog(FormCarreira.this,
						"Escolha uma cor", Color.WHITE);
				setCor2(color);
				gerarCarroLado();
				gerarCarroCima();
				FormCarreira.this.repaint();
			}

		});

		ptsPiloto.setModel(new CarreiraSpinnerModel());
		ptsCarro.setModel(new CarreiraSpinnerModel());
		JFormattedTextField tfptsCarro = ((JSpinner.DefaultEditor) ptsCarro
				.getEditor()).getTextField();
		tfptsCarro.setEditable(false);
		JFormattedTextField tfptsPiloto = ((JSpinner.DefaultEditor) ptsPiloto
				.getEditor()).getTextField();
		tfptsPiloto.setEditable(false);
		setLayout(new BorderLayout());
		add(panel, BorderLayout.CENTER);
		JPanel panel4 = new JPanel(new BorderLayout());
		panel4.add(panel2, BorderLayout.CENTER);
		panel4.add(panel3, BorderLayout.SOUTH);
		add(panel4, BorderLayout.SOUTH);
		gerarCarroLado();
		gerarCarroCima();
	}

	protected void gerarCarroCima() {
		BufferedImage carroLado = CarregadorRecursos
				.carregaImagem("CarroCima.png");
		BufferedImage cor1 = CarregadorRecursos.gerarCoresCarros(labelCor1
				.getBackground(), "CarroCimaC1.png");
		BufferedImage cor2 = CarregadorRecursos.gerarCoresCarros(labelCor2
				.getBackground(), "CarroCimaC2.png");
		Graphics graphics = carroLado.getGraphics();
		graphics.drawImage(cor1, 0, 0, null);
		graphics.drawImage(cor2, 0, 0, null);
		graphics.dispose();
		imgCarroCima.setIcon(new ImageIcon(carroLado));

	}

	protected void gerarCarroLado() {
		BufferedImage carroLado = CarregadorRecursos
				.carregaImagem("CarroLado.png");
		BufferedImage cor1 = CarregadorRecursos.gerarCoresCarros(labelCor1
				.getBackground(), "CarroLadoC1.png");
		BufferedImage cor2 = CarregadorRecursos.gerarCoresCarros(labelCor2
				.getBackground(), "CarroLadoC2.png");
		Graphics graphics = carroLado.getGraphics();
		graphics.drawImage(cor1, 0, 0, null);
		graphics.drawImage(cor2, 0, 0, null);
		graphics.dispose();
		imgCarroLado.setIcon(new ImageIcon(carroLado));
	}

	public static void main(String[] args) {
		FormCarreira formCarreira = new FormCarreira();
		JOptionPane.showMessageDialog(null, formCarreira);
	}

	public int getPtsCarreira() {
		return ptsCarreira;
	}

	public void setPtsCarreira(int ptsCarreira) {
		this.ptsCarreira = ptsCarreira;
	}

	public JTextField getNomePiloto() {
		return nomePiloto;
	}

	public JTextField getNomeCarro() {
		return nomeCarro;
	}

	public JSpinner getPtsPiloto() {
		return ptsPiloto;
	}

	public JSpinner getPtsCarro() {
		return ptsCarro;
	}

	public JCheckBox getModoCarreira() {
		return modoCarreira;
	}

	private class CarreiraSpinnerModel extends SpinnerNumberModel {
		@Override
		public void setValue(Object value) {
			int val = (Integer) getValue();
			if (val == 0) {
				super.setValue(value);
			} else {
				int nexVal = (Integer) value;
				if (val != nexVal && nexVal >= 600 && nexVal <= 999) {
					int inc = 0;
					if (nexVal >= 600 && nexVal < 700) {
						inc = 2;
						if (val == 700) {
							inc = 4;
						}
					} else if (nexVal >= 700 && nexVal < 800) {
						inc = 4;
						if (val == 800) {
							inc = 10;
						}
					} else if (nexVal >= 800 && nexVal < 900) {
						inc = 10;
						if (val == 900) {
							inc = 50;
						}
					} else if (nexVal >= 900 && nexVal < 999) {
						inc = 50;
					}
					if ((nexVal - val) > 0) {
						if ((ptsCarreira - inc) >= 0) {
							ptsCarreira -= inc;
						} else {
							setValue(val);
							return;
						}
					} else {
						ptsCarreira += inc;
					}
					try {
						ptsCarreiraVal.repaint();
					} catch (Exception e) {
						Logger.logarExept(e);
					}
					super.setValue(value);
				}
			}
		}
	}

	public JLabel getLabelCor1() {
		return labelCor1;
	}

	public JLabel getLabelCor2() {
		return labelCor2;
	}

	public void setCor1(Color color) {
		labelCor1.setBackground(color);
		int valor = (color.getRed() + color.getGreen() + color.getBlue()) / 2;
		if (valor > 250) {
			labelCor1.setForeground(Color.BLACK);
		} else {
			labelCor1.setForeground(Color.WHITE);
		}
		try {
			labelCor1.repaint();
		} catch (Exception e) {
			Logger.logarExept(e);
		}

	}

	public Color getCor1() {
		return labelCor1.getBackground();
	}

	public Color getCor2() {
		return labelCor2.getBackground();
	}

	public void setCor2(Color color) {
		labelCor2.setBackground(color);
		int valor = (color.getRed() + color.getGreen() + color.getBlue()) / 2;
		if (valor > 250) {
			labelCor2.setForeground(Color.BLACK);
		} else {
			labelCor2.setForeground(Color.WHITE);
		}
		try {
			labelCor2.repaint();
		} catch (Exception e) {
			Logger.logarExept(e);
		}

	}

	public String getImgCarroStr() {
		return imgCarroStr;
	}

	public void setImgCarroStr(String imgCarroStr) {
		this.imgCarroStr = imgCarroStr;
	}
}
