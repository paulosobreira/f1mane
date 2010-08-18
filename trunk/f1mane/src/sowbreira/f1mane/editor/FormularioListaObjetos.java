package sowbreira.f1mane.editor;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import sowbreira.f1mane.entidades.Circuito;
import sowbreira.f1mane.entidades.ObjetoPista;

public class FormularioListaObjetos {

	private MainPanelEditorVetorizado editorVetorizado;
	private DefaultListModel defaultListModelOP;
	private JFrame frame = new JFrame();

	public FormularioListaObjetos(MainPanelEditorVetorizado editorVetorizado) {
		this.editorVetorizado = editorVetorizado;
		defaultListModelOP = new DefaultListModel();
		final JList list = new JList(defaultListModelOP);
		JPanel main = new JPanel(new BorderLayout());
		JPanel botoes = new JPanel(new GridLayout(3, 1));
		main.add(new JScrollPane(list), BorderLayout.CENTER);
		main.add(botoes, BorderLayout.SOUTH);
		JButton cima = new JButton("Cima");
		cima.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int sel = list.getSelectedIndex();
				if (sel == -1 || sel == 0)
					return;
				ObjetoPista objetoPista = (ObjetoPista) defaultListModelOP
						.remove(sel);
				defaultListModelOP.add(sel - 1, objetoPista);
				list.setSelectedIndex(sel - 1);
				atualizarCircuito();
			}
		});
		JButton baixo = new JButton("Baixo");
		baixo.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int sel = list.getSelectedIndex();
				if (sel == -1 || sel == defaultListModelOP.getSize() - 1)
					return;
				ObjetoPista objetoPista = (ObjetoPista) defaultListModelOP
						.remove(sel);
				defaultListModelOP.add(sel + 1, objetoPista);
				list.setSelectedIndex(sel + 1);
				atualizarCircuito();
			}
		});
		JButton remover = new JButton("Remover");
		remover.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int sel = list.getSelectedIndex();
				if (sel == -1)
					return;
				defaultListModelOP.remove(sel);
				atualizarCircuito();
			}
		});

		botoes.add(cima);
		botoes.add(baixo);
		botoes.add(remover);
		frame.add(main);
	}

	public void mostrarPainel() {
		List<ObjetoPista> objetoPista = editorVetorizado.getCircuito()
				.getObjetos();
		if (objetoPista == null) {
			JOptionPane.showMessageDialog(editorVetorizado.getSrcFrame(),
					"Sem Objetos");
			return;
		}
		defaultListModelOP.clear();
		for (ObjetoPista op : objetoPista) {
			defaultListModelOP.addElement(op);
		}
		Point location = this.editorVetorizado.getSrcFrame().getLocation();
		frame.setLocation(new Point(location.x
				+ this.editorVetorizado.getSrcFrame().getWidth(), location.y));
		frame.setSize(250, 400);
		frame.setVisible(true);
	}

	protected void atualizarCircuito() {
		Circuito circuito = editorVetorizado.getCircuito();
		List<ObjetoPista> objetos = circuito.getObjetos();
		objetos.clear();
		for (int i = 0; i < defaultListModelOP.getSize(); i++) {
			objetos.add((ObjetoPista) defaultListModelOP.getElementAt(i));
		}
		editorVetorizado.repaint();
	}

	public static void main(String[] args) {
		FormularioListaObjetos formularioListaObjetos = new FormularioListaObjetos(
				null);
	}
}
