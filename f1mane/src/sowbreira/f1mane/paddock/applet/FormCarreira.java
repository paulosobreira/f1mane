package sowbreira.f1mane.paddock.applet;

import java.awt.GridLayout;

import javax.swing.JCheckBox;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.WindowConstants;

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
	private JLabel labelModoCarreira = new JLabel("Modo Carreira:");
	private JCheckBox modoCarreira = new JCheckBox();
	private JLabel labelNomePiloto = new JLabel("Nome Piloto:");
	private JTextField nomePiloto = new JTextField(10);
	private JLabel labelNomeCarro = new JLabel("Nome Equipe:");
	private JTextField nomeCarro = new JTextField(10);
	private JLabel labelPtsPiloto = new JLabel("Habilidade Piloto:");
	private JSpinner ptsPiloto = new JSpinner();
	private JLabel labelPtsCarro = new JLabel("Pontencia Carro:");
	private JSpinner ptsCarro = new JSpinner();
	private int ptsCarreira = 1;

	public FormCarreira() {
		setLayout(new GridLayout(3, 4));
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
}
