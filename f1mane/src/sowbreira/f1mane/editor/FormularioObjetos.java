package sowbreira.f1mane.editor;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import sowbreira.f1mane.recursos.idiomas.Lang;

public class FormularioObjetos {

	private JTextField largura = new JTextField(10);
	private JTextField altura = new JTextField(10);
	private JTextField angulo = new JTextField(10);
	private JTextField transparencia = new JTextField();
	private JCheckBox frente = new JCheckBox();
	private JComboBox tipoComboBox = new JComboBox();
	private JLabel labelCor1 = new JLabel("Clique");
	private JLabel labelCor2 = new JLabel("Clique");
	private JPanel panel = new JPanel(new GridLayout(8, 2));
	public final static String objetoLivre = "Objeto Livre";

	public FormularioObjetos() {
		tipoComboBox.addItem(objetoLivre);
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
	}

	public void mostrarPainel(Component pai) {
		JOptionPane.showConfirmDialog(pai, panel);
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

	}

	public static void main(String[] args) {
		FormularioObjetos formularioObjetos = new FormularioObjetos();
		formularioObjetos.mostrarPainel(null);
	}
}
