package sowbreira.f1mane.controles;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.XMLEncoder;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.border.TitledBorder;

import sowbreira.f1mane.MainFrame;
import sowbreira.f1mane.entidades.Campeonato;
import sowbreira.f1mane.entidades.Piloto;
import sowbreira.f1mane.recursos.CarregadorRecursos;
import sowbreira.f1mane.recursos.idiomas.Lang;
import br.nnpe.Logger;

public class ControleCampeonato {

	private MainFrame mainFrame;

	private Campeonato campeonato;

	public ControleCampeonato(MainFrame mainFrame) {
		carregarCircuitos();
		this.mainFrame = mainFrame;
	}

	protected Map circuitos = new HashMap();

	protected void carregarCircuitos() {
		final Properties properties = new Properties();

		try {
			properties.load(CarregadorRecursos
					.recursoComoStream("properties/pistas.properties"));

			Enumeration propName = properties.propertyNames();
			while (propName.hasMoreElements()) {
				final String name = (String) propName.nextElement();
				circuitos.put(name, properties.getProperty(name));

			}
		} catch (IOException e) {
			Logger.logarExept(e);
		}
	}

	public void criarCampeonato() throws Exception {
		final DefaultListModel defaultListModelCircuitos = new DefaultListModel();
		final DefaultListModel defaultListModelCircuitosSelecionados = new DefaultListModel();
		for (Iterator iterator = circuitos.keySet().iterator(); iterator
				.hasNext();) {
			String key = (String) iterator.next();
			defaultListModelCircuitos.addElement(key);
		}

		final JList listCircuitos = new JList(defaultListModelCircuitos);

		final JList listSelecionados = new JList(
				defaultListModelCircuitosSelecionados);
		JPanel panel1st = new JPanel(new BorderLayout());
		JPanel buttonsPanel = new JPanel(new GridLayout(6, 1));
		JButton esq = new JButton("<");
		esq.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (listSelecionados.getSelectedIndex() == -1)
					return;
				defaultListModelCircuitos
						.addElement(defaultListModelCircuitosSelecionados
								.remove(listSelecionados.getSelectedIndex()));
			}

		});
		JButton dir = new JButton(">");
		dir.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (listCircuitos.getSelectedIndex() == -1)
					return;
				defaultListModelCircuitosSelecionados
						.addElement(defaultListModelCircuitos
								.remove(listCircuitos.getSelectedIndex()));
			}

		});

		JButton esqAll = new JButton("<<");
		esqAll.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
			}

		});
		esqAll.setEnabled(false);
		JButton dirAll = new JButton(">>");
		dirAll.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
			}

		});
		dirAll.setEnabled(false);
		buttonsPanel.add(dir);
		buttonsPanel.add(esq);
		buttonsPanel.add(dirAll);
		buttonsPanel.add(esqAll);

		JButton cima = new JButton("Cima");
		cima.setEnabled(false);
		JButton baixo = new JButton("Baixo");
		baixo.setEnabled(false);
		buttonsPanel.add(cima);
		buttonsPanel.add(baixo);

		JPanel sul = new JPanel();
		sul.add(new JLabel() {
			@Override
			public String getText() {
				return Lang.msg("272");
			}
		});
		JComboBox temporadas = new JComboBox(mainFrame.getVectorTemps());
		sul.add(temporadas);

		panel1st.add(buttonsPanel, BorderLayout.CENTER);
		panel1st.add(new JScrollPane(listCircuitos) {
			@Override
			public Dimension getPreferredSize() {
				return new Dimension(150, 300);
			}
		}, BorderLayout.WEST);
		panel1st.add(new JScrollPane(listSelecionados) {
			@Override
			public Dimension getPreferredSize() {
				return new Dimension(150, 300);
			}
		}, BorderLayout.EAST);

		panel1st.add(sul, BorderLayout.SOUTH);
		JOptionPane.showMessageDialog(mainFrame, panel1st, Lang.msg("276"),
				JOptionPane.INFORMATION_MESSAGE);
		ControleJogoLocal controleJogoLocal = new ControleJogoLocal(
				(String) mainFrame.getTemporadas().get(
						temporadas.getSelectedItem()));
		DefaultListModel defaultListModelPilotosSelecionados = new DefaultListModel();
		JList listPilotosSelecionados = new JList(
				defaultListModelPilotosSelecionados);
		List tempList = new LinkedList();
		for (Iterator iter = controleJogoLocal.getPilotos().iterator(); iter
				.hasNext();) {
			Piloto piloto = (Piloto) iter.next();
			tempList.add(piloto);
		}
		Collections.sort(tempList, new Comparator() {

			@Override
			public int compare(Object o1, Object o2) {
				Piloto p1 = (Piloto) o1;
				Piloto p2 = (Piloto) o2;
				return p1.getCarro().getNome().compareTo(
						p2.getCarro().getNome());
			}

		});
		for (Iterator iterator = tempList.iterator(); iterator.hasNext();) {
			Piloto piloto = (Piloto) iterator.next();
			defaultListModelPilotosSelecionados.addElement(piloto);
		}

		final JPanel painel2nd = new JPanel(new BorderLayout());

		JPanel grid = new JPanel();

		grid.setLayout(new GridLayout(2, 2));
		grid.add(new JLabel() {

			public String getText() {
				return Lang.msg("110");
			}
		});
		JSpinner spinnerQtdeVoltas = new JSpinner();
		spinnerQtdeVoltas.setValue(new Integer(22));
		grid.add(spinnerQtdeVoltas);
		JComboBox comboBoxNivelCorrida = new JComboBox();
		comboBoxNivelCorrida.addItem(Lang.msg(ControleJogoLocal.NORMAL));
		comboBoxNivelCorrida.addItem(Lang.msg(ControleJogoLocal.FACIL));
		comboBoxNivelCorrida.addItem(Lang.msg(ControleJogoLocal.DIFICIL));
		grid.add(new JLabel() {
			public String getText() {
				return Lang.msg("212");
			}
		});
		grid.add(comboBoxNivelCorrida);

		JScrollPane scrolllistPilotosSelecionados = new JScrollPane(
				listPilotosSelecionados) {
			@Override
			public Dimension getPreferredSize() {
				return new Dimension(210, 225);
			}
		};
		scrolllistPilotosSelecionados.setBorder(new TitledBorder(Lang
				.msg("274")));
		painel2nd.add(scrolllistPilotosSelecionados, BorderLayout.CENTER);
		painel2nd.add(grid, BorderLayout.SOUTH);

		JOptionPane.showMessageDialog(mainFrame, painel2nd, Lang.msg("276"),
				JOptionPane.INFORMATION_MESSAGE);

		campeonato = new Campeonato();
		List corridas = new ArrayList();
		for (int i = 0; i < defaultListModelCircuitosSelecionados.getSize(); i++) {
			corridas.add(defaultListModelCircuitosSelecionados.get(i));
		}

		List pilotos = new ArrayList();
		for (int i = 0; i < defaultListModelCircuitosSelecionados.getSize(); i++) {
			pilotos.add(defaultListModelPilotosSelecionados.get(i).toString());
		}

		campeonato.setCorridas(corridas);

		campeonato.setPilotos(pilotos);

		campeonato.setTemporada((String) temporadas.getSelectedItem());

		campeonato.setNivel((String) comboBoxNivelCorrida.getSelectedItem());

		campeonato.setQtdeVoltas((Integer) spinnerQtdeVoltas.getValue());

	}

	public void continuarCampeonato() {
		// TODO Auto-generated method stub

	}

	public void dadosPersistencia() {
		if (campeonato != null) {
			ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
			XMLEncoder encoder = new XMLEncoder(byteArrayOutputStream);
			encoder.writeObject(campeonato);
			encoder.flush();
			System.out.println(new String(byteArrayOutputStream.toByteArray()));
		}

	}

}
