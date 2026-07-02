package br.f1mane.editor;

import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import br.nnpe.Logger;
import br.f1mane.entidades.ObjetoPista;

public class FormularioObjetos {

	private final JSpinner largura = new JSpinner();
	private final JSpinner altura = new JSpinner();
	private JSpinner inicioTranparencia = new JSpinner();
	private JSpinner fimTransparencia = new JSpinner();
	private final JSpinner angulo = new JSpinner();
	private JCheckBox frente = new JCheckBox();
	private final JCheckBox transparenciaBox = new JCheckBox();
	private final JComboBox tipoComboBox = new JComboBox();
	private JLabel labelCor1 = new JLabel("Clique");
	private JLabel labelCor2 = new JLabel("Clique");
	private final JPanel panel = new JPanel(new GridLayout(9, 2));
	private final MainPanelEditor mainPanelEditor;
	private ObjetoPista objetoPista;

	public FormularioObjetos(MainPanelEditor panelPai) {
		this.mainPanelEditor = panelPai;
		for (TipoObjetoPista tipo : TipoObjetoPista.values()) {
			tipoComboBox.addItem(tipo);
		}

		panel.add(new JLabel("Tipo"));
		panel.add(tipoComboBox);

		panel.add(new JLabel("No Início Transparência"));
		panel.add(inicioTranparencia);

		panel.add(new JLabel("No Fim Transparência"));
		panel.add(fimTransparencia);

		panel.add(new JLabel("Transparencia Box"));
		panel.add(transparenciaBox);

		panel.add(new JLabel("Ângulo"));
		panel.add(angulo);

		panel.add(new JLabel("Largura"));
		panel.add(largura);

		panel.add(new JLabel("Altura"));
		panel.add(altura);

		panel.add(new JLabel("Cor Primária"));
		panel.add(labelCor1);

		panel.add(new JLabel("Cor Secundária"));
		panel.add(labelCor2);

		ChangeListener changeListener = new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				atualizaMain();

			}
		};
		inicioTranparencia.addChangeListener(changeListener);
		largura.addChangeListener(changeListener);
		altura.addChangeListener(changeListener);
		fimTransparencia.addChangeListener(changeListener);

		labelCor1.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				Color nova = JColorChooser.showDialog(panel, "Cor Primária",
						labelCor1.getBackground());
				if (nova != null) {
					setCor(nova, labelCor1);
				}
			}
		});
		labelCor2.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				Color nova = JColorChooser.showDialog(panel, "Cor Secundária",
						labelCor2.getBackground());
				if (nova != null) {
					setCor(nova, labelCor2);
				}
			}
		});
	}

	protected void atualizaMain() {
		if (objetoPista != null
				&& FormularioObjetos.this.mainPanelEditor != null) {
			formularioObjetoPista(objetoPista);
			FormularioObjetos.this.mainPanelEditor.reprocessaEscapadaSeNecessario(objetoPista);
			FormularioObjetos.this.mainPanelEditor.repaint();
		}

	}

	public void mostrarPainelModal() {
		JOptionPane.showMessageDialog(this.mainPanelEditor.getSrcFrame(),
				panel);
		if (objetoPista != null) {
			formularioObjetoPista(objetoPista);
			FormularioObjetos.this.mainPanelEditor.reprocessaEscapadaSeNecessario(objetoPista);
		}
		FormularioObjetos.this.mainPanelEditor.repaint();
	}

	public void setCor(Color color, JLabel label) {
		if (color == null) {
			return;
		}
		label.setOpaque(true);
		label.setBackground(color);
		int valor = (color.getRed() + color.getGreen() + color.getBlue()) / 2;
		if (valor > 250) {
			label.setForeground(Color.BLACK);
		} else {
			label.setForeground(Color.WHITE);
		}
		try {
			label.repaint();
		} catch (Exception e) {
			Logger.logarExept(e);
		}
		atualizaMain();
	}

	public static void main(String[] args) {
		FormularioObjetos formularioObjetos = new FormularioObjetos(null);
	}

	public JSpinner getLargura() {
		return largura;
	}

	public JSpinner getInicioTranparencia() {
		return inicioTranparencia;
	}

	public void setInicioTranparencia(JSpinner inicioTranparencia) {
		this.inicioTranparencia = inicioTranparencia;
	}

	public JSpinner getFimTransparencia() {
		return fimTransparencia;
	}

	public void setFimTransparencia(JSpinner fimTransparencia) {
		this.fimTransparencia = fimTransparencia;
	}

	public JCheckBox getFrente() {
		return frente;
	}

	public void setFrente(JCheckBox frente) {
		this.frente = frente;
	}

	public JLabel getLabelCor1() {
		return labelCor1;
	}

	public void setLabelCor1(JLabel labelCor1) {
		this.labelCor1 = labelCor1;
	}

	public JLabel getLabelCor2() {
		return labelCor2;
	}

	public void setLabelCor2(JLabel labelCor2) {
		this.labelCor2 = labelCor2;
	}

	public JComboBox getTipoComboBox() {
		return tipoComboBox;
	}

	public void objetoLivreFormulario(ObjetoPista objetoPista) {
		this.objetoPista = null;
		carregarCampos(objetoPista);
		this.objetoPista = objetoPista;
		mostrarPainelModal();
	}

	/**
	 * Popula os campos do formulário a partir do estado atual de
	 * {@code objetoPista}, para que abrir o formulário (seja para editar um
	 * objeto existente, seja logo após criar um novo com valores padrão) não
	 * sobrescreva esses valores com o estado em branco dos campos Swing.
	 */
	void carregarCampos(ObjetoPista objetoPista) {
		largura.setValue(Integer.valueOf(objetoPista.getLargura()));
		altura.setValue(Integer.valueOf(objetoPista.getAltura()));
		angulo.setValue(Integer.valueOf((int) objetoPista.getAngulo()));
		setCor(objetoPista.getCorPimaria(), labelCor1);
		setCor(objetoPista.getCorSecundaria(), labelCor2);
		frente.setSelected(objetoPista.isPintaEmcima());
		inicioTranparencia.setValue(Integer.valueOf(objetoPista.getInicioTransparencia()));
		fimTransparencia.setValue(Integer.valueOf(objetoPista.getFimTransparencia()));
		transparenciaBox.setSelected(objetoPista.isTransparenciaBox());
	}

	public void formularioObjetoPista(ObjetoPista objetoPista) {
		objetoPista.setCorPimaria(labelCor1.getBackground());
		objetoPista.setCorSecundaria(labelCor2.getBackground());
		objetoPista.setLargura(((Integer) largura.getValue()).intValue());
		objetoPista.setAltura(((Integer) altura.getValue()).intValue());
		objetoPista.setAngulo(((Integer) angulo.getValue()).doubleValue());
		objetoPista.setInicioTransparencia(
                ((Integer) getInicioTranparencia().getValue()).intValue());
		objetoPista.setFimTransparencia(
                ((Integer) getFimTransparencia().getValue()).intValue());
		objetoPista.setTransparenciaBox(transparenciaBox.isSelected());
	}
}
