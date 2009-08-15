package sowbreira.f1mane.visao;

import java.awt.GridLayout;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JSpinner;

import sowbreira.f1mane.entidades.Carro;
import sowbreira.f1mane.recursos.idiomas.Lang;
import sowbreira.f1mane.recursos.idiomas.LangVO;

/**
 * @author Paulo Sobreira Criado em 14/08/2009 as 19:36:36
 */
public class ProgamacaoBox {

	private JComboBox boxPneuParada1;
	private JCheckBox ative1;
	private JComboBox comboBoxAsaParada1;
	private JSlider sliderPercentCombustParada1;
	private JSpinner spinnerNumVoltaParada1;
	private JCheckBox ative2;
	private JComboBox boxPneuParada2;
	private JComboBox comboBoxAsaParada2;
	private JSlider sliderPercentCombustParada2;
	private JSpinner spinnerNumVoltaParada2;
	private JCheckBox ative3;
	private JComboBox boxPneuParada3;
	private JComboBox comboBoxAsaParada3;
	private JSlider sliderPercentCombustParada3;
	private JSpinner spinnerNumVoltaParada3;
	private JPanel painel;

	public JCheckBox getAtive1() {
		return ative1;
	}

	public JCheckBox getAtive2() {
		return ative2;
	}

	public JComboBox getBoxPneuParada2() {
		return boxPneuParada2;
	}

	public JComboBox getComboBoxAsaParada2() {
		return comboBoxAsaParada2;
	}

	public JSlider getSliderPercentCombustParada2() {
		return sliderPercentCombustParada2;
	}

	public JSpinner getSpinnerNumVoltaParada2() {
		return spinnerNumVoltaParada2;
	}

	public JCheckBox getAtive3() {
		return ative3;
	}

	public JComboBox getBoxPneuParada3() {
		return boxPneuParada3;
	}

	public JComboBox getComboBoxAsaParada3() {
		return comboBoxAsaParada3;
	}

	public JSlider getSliderPercentCombustParada3() {
		return sliderPercentCombustParada3;
	}

	public JSpinner getSpinnerNumVoltaParada3() {
		return spinnerNumVoltaParada3;
	}

	public ProgamacaoBox() {
		ative1 = new JCheckBox();
		boxPneuParada1 = new JComboBox();
		boxPneuParada1.addItem(new LangVO(Carro.TIPO_PNEU_MOLE));
		boxPneuParada1.addItem(new LangVO(Carro.TIPO_PNEU_DURO));
		boxPneuParada1.addItem(new LangVO(Carro.TIPO_PNEU_CHUVA));

		sliderPercentCombustParada1 = new JSlider(0, 100);
		sliderPercentCombustParada1.setPaintTicks(true);
		sliderPercentCombustParada1.setMajorTickSpacing(10);
		sliderPercentCombustParada1.setValue(new Integer(50));

		comboBoxAsaParada1 = new JComboBox();
		comboBoxAsaParada1.addItem(new LangVO(Carro.ASA_NORMAL));
		comboBoxAsaParada1.addItem(new LangVO(Carro.MAIS_ASA));
		comboBoxAsaParada1.addItem(new LangVO(Carro.MENOS_ASA));

		spinnerNumVoltaParada1 = new JSpinner();

		ative2 = new JCheckBox();
		boxPneuParada2 = new JComboBox();
		boxPneuParada2.addItem(new LangVO(Carro.TIPO_PNEU_MOLE));
		boxPneuParada2.addItem(new LangVO(Carro.TIPO_PNEU_DURO));
		boxPneuParada2.addItem(new LangVO(Carro.TIPO_PNEU_CHUVA));

		sliderPercentCombustParada2 = new JSlider(0, 100);
		sliderPercentCombustParada2.setPaintTicks(true);
		sliderPercentCombustParada2.setMajorTickSpacing(10);
		sliderPercentCombustParada2.setValue(new Integer(50));

		comboBoxAsaParada2 = new JComboBox();
		comboBoxAsaParada2.addItem(new LangVO(Carro.ASA_NORMAL));
		comboBoxAsaParada2.addItem(new LangVO(Carro.MAIS_ASA));
		comboBoxAsaParada2.addItem(new LangVO(Carro.MENOS_ASA));

		spinnerNumVoltaParada2 = new JSpinner();

		ative3 = new JCheckBox();
		boxPneuParada3 = new JComboBox();
		boxPneuParada3.addItem(new LangVO(Carro.TIPO_PNEU_MOLE));
		boxPneuParada3.addItem(new LangVO(Carro.TIPO_PNEU_DURO));
		boxPneuParada3.addItem(new LangVO(Carro.TIPO_PNEU_CHUVA));

		sliderPercentCombustParada3 = new JSlider(0, 100);
		sliderPercentCombustParada3.setPaintTicks(true);
		sliderPercentCombustParada3.setMajorTickSpacing(10);
		sliderPercentCombustParada3.setValue(new Integer(50));

		comboBoxAsaParada3 = new JComboBox();
		comboBoxAsaParada3.addItem(new LangVO(Carro.ASA_NORMAL));
		comboBoxAsaParada3.addItem(new LangVO(Carro.MAIS_ASA));
		comboBoxAsaParada3.addItem(new LangVO(Carro.MENOS_ASA));

		spinnerNumVoltaParada3 = new JSpinner();

		GridLayout gridLayout = new GridLayout(4, 5);
		GridLayout gridLayoutMini = new GridLayout(1, 2);
		painel = new JPanel();
		painel.setLayout(gridLayout);

		painel.add(new JLabel() {

			public String getText() {
				return Lang.msg("263");
			}
		});
		painel.add(new JLabel() {

			public String getText() {
				return Lang.msg("082");
			}
		});
		painel.add(new JLabel() {

			public String getText() {
				return Lang.msg("264");
			}
		});
		painel.add(new JLabel() {

			public String getText() {
				return Lang.msg("083");
			}
		});
		painel.add(new JLabel() {

			public String getText() {
				return Lang.msg("084");
			}
		});

		JPanel mini1 = new JPanel();
		mini1.add(new JLabel("1"));
		mini1.add(ative1);
		mini1.setLayout(gridLayoutMini);
		painel.add(mini1);
		painel.add(spinnerNumVoltaParada1);
		painel.add(boxPneuParada1);
		painel.add(sliderPercentCombustParada1);
		painel.add(comboBoxAsaParada1);

		JPanel mini2 = new JPanel();
		mini2.add(new JLabel("2"));
		mini2.setLayout(gridLayoutMini);
		mini2.add(ative2);
		painel.add(mini2);
		painel.add(spinnerNumVoltaParada2);
		painel.add(boxPneuParada2);
		painel.add(sliderPercentCombustParada2);
		painel.add(comboBoxAsaParada2);

		JPanel mini3 = new JPanel();
		mini3.add(new JLabel("3"));
		mini3.add(ative3);
		mini3.setLayout(gridLayoutMini);
		painel.add(mini3);
		painel.add(spinnerNumVoltaParada3);
		painel.add(boxPneuParada3);
		painel.add(sliderPercentCombustParada3);
		painel.add(comboBoxAsaParada3);

	}

	public JPanel getPainel() {
		return painel;
	}

	public void setPainel(JPanel painel) {
		this.painel = painel;
	}

	public JComboBox getBoxPneuParada1() {
		return boxPneuParada1;
	}

	public JComboBox getComboBoxAsaParada1() {
		return comboBoxAsaParada1;
	}

	public JSlider getSliderPercentCombustParada1() {
		return sliderPercentCombustParada1;
	}

	public JSpinner getSpinnerNumVoltaParada1() {
		return spinnerNumVoltaParada1;
	}

	public static void main(String[] args) {
		ProgamacaoBox box = new ProgamacaoBox();
		JOptionPane.showMessageDialog(null, box.getPainel());

	}
}
