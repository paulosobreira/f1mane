package sowbreira.f1mane.paddock.applet;

import java.awt.GridLayout;

import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

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
	private JLabel labelModoCarreira = new JLabel("Modo Carreira:");
	private JCheckBox modoCarreira = new JCheckBox();
	private JLabel labelNomePiloto = new JLabel("Nome Piloto:");
	private JTextField nomePiloto = new JTextField(20);
	private JLabel labelNomeCarro = new JLabel("Nome Carro:");
	private JTextField nomeCarro = new JTextField(20);
	private JLabel labelPtsPiloto = new JLabel("Pontos Piloto:");
	private JSpinner ptsPiloto = new JSpinner();
	private JLabel labelPtsCarro = new JLabel("Pontos Carro:");
	private JSpinner ptsCarro = new JSpinner();
	private long ptsCarreira = 10;

	public FormCarreira() {
		setLayout(new GridLayout(3, 4));
		add(labelPtsCarreira);
		add(ptsCarreiraVal);
		add(labelModoCarreira);
		add(modoCarreira);

		add(labelNomePiloto);
		add(nomePiloto);
		add(labelPtsPiloto);
		add(ptsPiloto);

		add(labelNomeCarro);
		add(nomeCarro);
		add(labelPtsCarro);
		add(ptsCarro);
		ptsCarro.getModel().addChangeListener(new ChangeListener() {

			public void stateChanged(ChangeEvent e) {

			}

		});
	}

	public static void main(String[] args) {
		FormCarreira formCarreira = new FormCarreira();
		JFrame frame = new JFrame();
		frame.getContentPane().add(formCarreira);
		frame.setVisible(true);
	}

	public long getPtsCarreira() {
		return ptsCarreira;
	}

	public void setPtsCarreira(long ptsCarreira) {
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

}
