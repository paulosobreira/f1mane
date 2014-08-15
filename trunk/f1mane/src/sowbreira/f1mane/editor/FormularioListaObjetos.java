package sowbreira.f1mane.editor;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.Transient;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import sowbreira.f1mane.entidades.Circuito;
import sowbreira.f1mane.entidades.ObjetoPista;

public class FormularioListaObjetos {

	private MainPanelEditor editor;
	private DefaultListModel defaultListModelOP;
	private JList list;
	private JFrame frame = new JFrame();
	private JPanel objetos;

	public DefaultListModel getDefaultListModelOP() {
		return defaultListModelOP;
	}

	public void setDefaultListModelOP(DefaultListModel defaultListModelOP) {
		this.defaultListModelOP = defaultListModelOP;
	}

	public JList getList() {
		return list;
	}

	public void setList(JList list) {
		this.list = list;
	}

	public FormularioListaObjetos(MainPanelEditor editor) {
		this.editor = editor;
		defaultListModelOP = new DefaultListModel();
		list = new JList(defaultListModelOP);
		list.addListSelectionListener(new ListSelectionListener() {

			@Override
			public void valueChanged(ListSelectionEvent e) {
				FormularioListaObjetos.this.editor.repaint();
				FormularioListaObjetos.this.editor.desSelecionaNosPista();
				int selectedIndex = list.getSelectedIndex();
				if (selectedIndex > -1
						&& selectedIndex < defaultListModelOP.size()) {
					ObjetoPista objetoPista = (ObjetoPista) defaultListModelOP
							.get(selectedIndex);
					FormularioListaObjetos.this.editor
							.centralizarPonto(objetoPista.getPosicaoQuina());
				}

			}
		});
		objetos = new JPanel(new BorderLayout()) {
			@Override
			@Transient
			public Dimension getPreferredSize() {
				return new Dimension(super.getPreferredSize().height, 160);
			}
		};
		JPanel botoes = new JPanel(new GridLayout(3, 1));
		objetos.add(new JScrollPane(list), BorderLayout.CENTER);
		objetos.add(botoes, BorderLayout.SOUTH);
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
				if (sel == -1 || sel >= defaultListModelOP.getSize() - 1)
					return;
				ObjetoPista objetoPista = (ObjetoPista) defaultListModelOP
						.remove(sel);
				defaultListModelOP.add(sel + 1, objetoPista);
				list.setSelectedIndex(sel + 1);
				atualizarCircuito();
			}
		});
		JButton primeiro = new JButton("Primeiro");
		primeiro.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int sel = list.getSelectedIndex();
				if (sel == -1 || sel == 0)
					return;
				ObjetoPista objetoPista = (ObjetoPista) defaultListModelOP
						.remove(sel);
				defaultListModelOP.add(0, objetoPista);
				list.setSelectedIndex(0);
				atualizarCircuito();
			}
		});
		JButton ultimo = new JButton("Ultimo");
		ultimo.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int sel = list.getSelectedIndex();
				if (sel == -1 || sel >= defaultListModelOP.getSize() - 1)
					return;
				ObjetoPista objetoPista = (ObjetoPista) defaultListModelOP
						.remove(sel);
				defaultListModelOP.add(defaultListModelOP.getSize(),
						objetoPista);
				list.setSelectedIndex(defaultListModelOP.getSize() - 1);
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

		JButton editar = new JButton("Editar");
		editar.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int sel = list.getSelectedIndex();
				if (sel == -1)
					return;
				ObjetoPista objetoPista = (ObjetoPista) list.getSelectedValue();
				FormularioObjetos formularioObjetos = new FormularioObjetos(
						FormularioListaObjetos.this.editor);
				formularioObjetos.objetoLivreFormulario(objetoPista);
			}
		});

		botoes.add(cima);
		botoes.add(baixo);
		botoes.add(primeiro);
		botoes.add(ultimo);
		botoes.add(editar);
		botoes.add(remover);
		frame.add(objetos);
	}

	public void listarObjetos() {
		List<ObjetoPista> objetoPista = editor.getCircuito().getObjetos();
		if (objetoPista != null) {
			defaultListModelOP.clear();
			for (ObjetoPista op : objetoPista) {
				defaultListModelOP.addElement(op);
			}
		}
	}

	protected void atualizarCircuito() {
		Circuito circuito = editor.getCircuito();
		List<ObjetoPista> objetos = circuito.getObjetos();
		objetos.clear();
		for (int i = 0; i < defaultListModelOP.getSize(); i++) {
			objetos.add((ObjetoPista) defaultListModelOP.getElementAt(i));
		}
		editor.repaint();
	}

	public JPanel getObjetos() {
		return objetos;
	}

	public void setObjetos(JPanel objetos) {
		this.objetos = objetos;
	}

	public static void main(String[] args) {
		FormularioListaObjetos formularioListaObjetos = new FormularioListaObjetos(
				null);
	}
}
