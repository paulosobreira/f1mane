package sowbreira.f1mane.editor;

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

import sowbreira.f1mane.entidades.ObjetoPista;
import sowbreira.f1mane.recursos.idiomas.Lang;

public class FormularioObjetos {

	private JSpinner largura = new JSpinner();
	private JSpinner inicioTranparencia = new JSpinner();
	private JSpinner fimTransparencia = new JSpinner();
	private JSpinner angulo = new JSpinner();
	private JCheckBox frente = new JCheckBox();
	private JCheckBox transparenciaBox = new JCheckBox();
	private JComboBox tipoComboBox = new JComboBox();
	private JLabel labelCor1 = new JLabel("Clique");
	private JLabel labelCor2 = new JLabel("Clique");
	private JPanel panel = new JPanel(new GridLayout(5, 2));
	private MainPanelEditor mainPanelEditor;
	private ObjetoPista objetoPista;
	protected static final String OBJETO_TRANSPARENCIA = "Objeto Transparencia";
	protected static final String OBJETO_ESCAPADA = "Objeto Escapada";

	public FormularioObjetos(MainPanelEditor panelPai) {
		this.mainPanelEditor = panelPai;
		tipoComboBox.addItem(OBJETO_TRANSPARENCIA);
		tipoComboBox.addItem(OBJETO_ESCAPADA);
		
		panel.add(new JLabel("Tipo"){
			@Override
			public String getText() {
				return Lang.msg("tipo");
			}
		});
		panel.add(tipoComboBox);

		panel.add(new JLabel("No Inicio Transparencia") {
			@Override
			public String getText() {
				return Lang.msg("noInicioTransparencia");
			}
		});
		panel.add(inicioTranparencia);

		panel.add(new JLabel("No Fim Transparencia") {
			@Override
			public String getText() {
				return Lang.msg("noFimTransparencia");
			}
		});
		panel.add(fimTransparencia);
		
	
		panel.add(new JLabel(){
			@Override
			public String getText() {
				return Lang.msg("transparenciaBox");
			}
		});
		panel.add(transparenciaBox);


		panel.add(new JLabel("Angulo"){
			@Override
			public String getText() {
				return Lang.msg("angulorRotacao");
			}
		});
		panel.add(angulo);

		// panel.add(new JLabel("Transparencia"));
		// panel.add(transparencia);
		//
		// panel.add(new JLabel("Desenha Frente/Nos Box"));
		// panel.add(frente);

		// panel.add(new JLabel("Cor Primaria"));
		// panel.add(labelCor1);
		// labelCor1.addMouseListener(new MouseAdapter() {
		// @Override
		// public void mouseClicked(MouseEvent e) {
		// Color color = JColorChooser.showDialog(panel,
		// Lang.msg("escolhaCor"), Color.WHITE);
		// setCor(color, labelCor1);
		// }
		// });
		//
		// panel.add(new JLabel("Cor Secundaria"));
		// panel.add(labelCor2);
		//
		// labelCor2.addMouseListener(new MouseAdapter() {
		// @Override
		// public void mouseClicked(MouseEvent e) {
		// super.mouseClicked(e);
		// Color color = JColorChooser.showDialog(panel,
		// Lang.msg("escolhaCor"), Color.WHITE);
		// setCor(color, labelCor2);
		// }
		// });

		ChangeListener changeListener = new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				atualizaMain();

			}
		};
		inicioTranparencia.addChangeListener(changeListener);
		largura.addChangeListener(changeListener);
		fimTransparencia.addChangeListener(changeListener);
	}

	protected void atualizaMain() {
		if (objetoPista != null
				&& FormularioObjetos.this.mainPanelEditor != null) {
			formularioObjetoPista(objetoPista);
			FormularioObjetos.this.mainPanelEditor.repaint();
		}

	}

	public void mostrarPainelModal() {
		JOptionPane
				.showMessageDialog(this.mainPanelEditor.getSrcFrame(), panel);
		if (objetoPista != null)
			formularioObjetoPista(objetoPista);
		FormularioObjetos.this.mainPanelEditor.repaint();
	}

	public void setCor(Color color, JLabel label) {
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
			e.printStackTrace();
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
		largura.setValue(objetoPista.getLargura());
		setCor(objetoPista.getCorPimaria(), labelCor1);
		setCor(objetoPista.getCorSecundaria(), labelCor2);
		frente.setSelected(objetoPista.isPintaEmcima());
		inicioTranparencia.setValue(objetoPista.getInicioTransparencia());
		fimTransparencia.setValue(objetoPista.getFimTransparencia());
		transparenciaBox.setSelected(objetoPista.isTransparenciaBox());
		this.objetoPista = objetoPista;
		mostrarPainelModal();
	}

	public void formularioObjetoPista(ObjetoPista objetoPista) {
		objetoPista.setCorPimaria(getLabelCor1().getBackground());
		objetoPista.setCorSecundaria(getLabelCor2().getBackground());
		objetoPista.setAngulo((Integer) angulo.getValue());
		objetoPista.setInicioTransparencia((Integer) getInicioTranparencia().getValue());
		objetoPista.setFimTransparencia((Integer) getFimTransparencia().getValue());
		objetoPista.setTransparenciaBox(transparenciaBox.isSelected());
	}
}
