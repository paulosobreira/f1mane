package sowbreira.f1mane.editor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import br.nnpe.Util;

import sowbreira.f1mane.entidades.ObjetoPista;
import sowbreira.f1mane.recursos.idiomas.Lang;

public class FormularioObjetos {

	private JSpinner largura = new JSpinner();
	private JSpinner altura = new JSpinner();
	private JSpinner angulo = new JSpinner();
	private JSpinner transparencia = new JSpinner();
	private JCheckBox frente = new JCheckBox();
	private JComboBox tipoComboBox = new JComboBox();
	private JLabel labelCor1 = new JLabel("Clique");
	private JLabel labelCor2 = new JLabel("Clique");
	private JPanel panel = new JPanel(new GridLayout(9, 2));
	private MainPanelEditorVetorizado mainPanelEditorVetorizado;
	private ObjetoPista objetoPista;
	private JFrame frame;
	public final static String OBJETO_LIVRE = "Objeto Livre";
	public final static String OBJETO_PNEUS = "Objeto Pneus";
	public final static String OBJETO_CONSTRUCAO = "Objeto Construção";
	public final static String OBJETO_CIRCULO = "Objeto Circulo";
	public final static String OBJETO_ARQUIBANCADA = "Objeto Arquibancada";

	public FormularioObjetos(MainPanelEditorVetorizado panelPai) {
		this.mainPanelEditorVetorizado = panelPai;
		tipoComboBox.addItem(OBJETO_PNEUS);
		tipoComboBox.addItem(OBJETO_CIRCULO);
		tipoComboBox.addItem(OBJETO_CONSTRUCAO);
		tipoComboBox.addItem(OBJETO_ARQUIBANCADA);
		tipoComboBox.addItem(OBJETO_LIVRE);
		panel.add(new JLabel("Tipo"));
		panel.add(tipoComboBox);

		panel.add(new JLabel("Altura"));
		panel.add(altura);

		panel.add(new JLabel("Largura"));
		panel.add(largura);

		panel.add(new JLabel("Angulo"));
		panel.add(angulo);

		panel.add(new JLabel("Transparencia"));
		panel.add(transparencia);

		panel.add(new JLabel("Desenha Frente"));
		panel.add(frente);

		panel.add(new JLabel("Cor Primaria"));
		panel.add(labelCor1);
		labelCor1.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				Color color = JColorChooser.showDialog(panel, Lang
						.msg("escolhaCor"), Color.WHITE);
				setCor(color, labelCor1);
			}
		});

		panel.add(new JLabel("Cor Secundaria"));
		panel.add(labelCor2);

		labelCor2.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				super.mouseClicked(e);
				Color color = JColorChooser.showDialog(panel, Lang
						.msg("escolhaCor"), Color.WHITE);
				setCor(color, labelCor2);
			}
		});

		JButton buttonMover = new JButton("Mover/Parar");
		panel.add(buttonMover);
		buttonMover.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (objetoPista != null) {
					mainPanelEditorVetorizado
							.setMoverObjetoPista(!mainPanelEditorVetorizado
									.isMoverObjetoPista());
					mainPanelEditorVetorizado.setObjetoPista(objetoPista);
				}
			}
		});

		JButton buttonAtualiza = new JButton("Atualiza");
		panel.add(buttonAtualiza);
		buttonAtualiza.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (objetoPista != null)
					formularioObjetoPista(objetoPista);
				FormularioObjetos.this.mainPanelEditorVetorizado.repaint();
			}
		});
		ChangeListener changeListener = new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				atualizaMain();

			}
		};
		altura.addChangeListener(changeListener);
		largura.addChangeListener(changeListener);
		angulo.addChangeListener(changeListener);
		transparencia.addChangeListener(changeListener);
		transparencia.setValue(new Integer(255));
		frame = new JFrame();
		frame.getContentPane().setLayout(new BorderLayout());
		frame.getContentPane().add(panel);

	}

	protected void atualizaMain() {
		if (objetoPista != null
				&& FormularioObjetos.this.mainPanelEditorVetorizado != null) {
			formularioObjetoPista(objetoPista);
			FormularioObjetos.this.mainPanelEditorVetorizado.repaint();
		}

	}

	public void mostrarPainel() {
		Point location = this.mainPanelEditorVetorizado.getSrcFrame()
				.getLocation();
		frame.setLocation(new Point(location.x
				+ this.mainPanelEditorVetorizado.getSrcFrame().getWidth(),
				location.y));
		frame.setSize(250, 400);
		if (objetoPista != null)
			frame.setTitle(objetoPista.getNome());
		else
			frame.setTitle("Novo Objeto");
		frame.setVisible(true);
	}

	public void mostrarPainelModal() {
		JOptionPane.showMessageDialog(this.mainPanelEditorVetorizado
				.getSrcFrame(), panel);
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
		formularioObjetos.mostrarPainel();
	}

	public JSpinner getLargura() {
		return largura;
	}

	public JSpinner getAltura() {
		return altura;
	}

	public JSpinner getAngulo() {
		return angulo;
	}

	public JSpinner getTransparencia() {
		return transparencia;
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
		altura.setValue(objetoPista.getAltura());
		largura.setValue(objetoPista.getLargura());
		setCor(objetoPista.getCorPimaria(), labelCor1);
		setCor(objetoPista.getCorSecundaria(), labelCor2);
		transparencia.setValue(objetoPista.getTransparencia());
		frente.setSelected(objetoPista.isPintaEmcima());
		angulo.setValue(new Integer((int) objetoPista.getAngulo()));
		this.objetoPista = objetoPista;
		mostrarPainel();

	}

	public void formularioObjetoPista(ObjetoPista objetoPista) {
		objetoPista.setCorPimaria(getLabelCor1().getBackground());
		objetoPista.setCorSecundaria(getLabelCor2().getBackground());
		objetoPista.setAngulo((Integer) angulo.getValue());
		objetoPista.setPintaEmcima(getFrente().isSelected());
		objetoPista.setAltura((Integer) getAltura().getValue());
		objetoPista.setLargura((Integer) getLargura().getValue());
		objetoPista.setTransparencia((Integer) transparencia.getValue());
	}
}
