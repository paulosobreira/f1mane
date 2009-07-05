package sowbreira.f1mane.paddock.applet;

import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.WindowConstants;

import sowbreira.f1mane.recursos.idiomas.Lang;

/**
 * @author Paulo Sobreira Criado em 27/06/2009 as 15:48:51
 */
public class FormCarreira extends JPanel {

	private JLabel labelPtsCarreira = new JLabel("Pontos Carreira:");
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

	private int ptsCarreira = 1;

	public FormCarreira() {
		setLayout(new GridLayout(4, 4));
		add(labelModoCarreira);
		add(modoCarreira);
		add(labelPtsCarreira);
		add(ptsCarreiraVal);

		add(labelNomePiloto);
		add(nomePiloto);
		add(labelPtsPiloto);
		add(ptsPiloto);

		add(labelNomeCarro);
		add(nomeCarro);
		add(labelPtsCarro);
		add(ptsCarro);

		add(labelCor1);
		add(labelCor2);
		add(new JLabel());
		add(new JLabel());
		labelCor1.setOpaque(true);
		labelCor1.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent e) {
				Color color = JColorChooser.showDialog(FormCarreira.this,
						"Escolha uma cor", Color.WHITE);
				setCor1(color);
			}

		});
		labelCor2.setOpaque(true);
		labelCor2.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent e) {
				Color color = JColorChooser.showDialog(FormCarreira.this,
						"Escolha uma cor", Color.WHITE);
				setCor2(color);
			}

		});

		ptsPiloto.setModel(new CarreiraSpinnerModel());
		ptsPiloto.setValue(new Integer(990));
		ptsCarro.setModel(new CarreiraSpinnerModel());
		ptsCarro.setValue(new Integer(690));
		JFormattedTextField tfptsCarro = ((JSpinner.DefaultEditor) ptsCarro
				.getEditor()).getTextField();
		tfptsCarro.setEditable(false);
		JFormattedTextField tfptsPiloto = ((JSpinner.DefaultEditor) ptsPiloto
				.getEditor()).getTextField();
		tfptsPiloto.setEditable(false);
	}

	public static void main(String[] args) {
		FormCarreira formCarreira = new FormCarreira();
		JFrame frame = new JFrame();
		frame.getContentPane().add(formCarreira);
		formCarreira.setCor1(Color.BLUE);
		frame.setVisible(true);
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
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
						inc = 1;
						if (val == 700) {
							inc = 5;
						}
					} else if (nexVal >= 700 && nexVal < 800) {
						inc = 5;
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
					ptsCarreiraVal.repaint();
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
		labelCor1.repaint();

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
		labelCor2.repaint();

	}
}
