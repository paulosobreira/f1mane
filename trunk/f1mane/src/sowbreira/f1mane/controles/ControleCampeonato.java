package sowbreira.f1mane.controles;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import sowbreira.f1mane.MainFrame;
import sowbreira.f1mane.recursos.CarregadorRecursos;
import br.nnpe.Logger;

public class ControleCampeonato {

	private MainFrame mainFrame;

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

	public void criarCampeonato() {
		final DefaultListModel defaultListModelCircuitos = new DefaultListModel();
		final DefaultListModel defaultListModelCircuitosSelecionados = new DefaultListModel();
		for (Iterator iterator = circuitos.keySet().iterator(); iterator
				.hasNext();) {
			String key = (String) iterator.next();
			defaultListModelCircuitos.addElement(key);

			// vectorCircuitosSelecionados.add(circuitos.get(key));
		}

		final JList listCircuitos = new JList(defaultListModelCircuitos);

		final JList listSelecionados = new JList(
				defaultListModelCircuitosSelecionados);
		JPanel panelCircuitos = new JPanel(new BorderLayout()) {
			@Override
			public Dimension getPreferredSize() {
				return new Dimension(300, 500);
			}
		};
		panelCircuitos.add(listCircuitos, BorderLayout.WEST);
		panelCircuitos.add(listSelecionados, BorderLayout.EAST);
		JPanel buttonsPanel = new JPanel(new GridLayout(4, 1));
		JButton esq = new JButton("<");
		esq.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				defaultListModelCircuitos
						.addElement(defaultListModelCircuitosSelecionados
								.remove(listSelecionados.getSelectedIndex()));
			}

		});
		JButton dir = new JButton(">");
		dir.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				defaultListModelCircuitosSelecionados
						.addElement(defaultListModelCircuitos
								.remove(listCircuitos.getSelectedIndex()));
			}

		});
		buttonsPanel.add(esq);
		buttonsPanel.add(dir);

		JButton esqAll = new JButton("<<");
		esqAll.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				defaultListModelCircuitos
						.addElement(defaultListModelCircuitosSelecionados
								.remove(listSelecionados.getSelectedIndex()));
			}

		});
		JButton dirAll = new JButton(">>");
		dirAll.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				defaultListModelCircuitosSelecionados
						.addElement(defaultListModelCircuitos
								.remove(listCircuitos.getSelectedIndex()));
			}

		});
		buttonsPanel.add(esqAll);
		buttonsPanel.add(dirAll);

		panelCircuitos.add(buttonsPanel, BorderLayout.CENTER);
		JOptionPane.showMessageDialog(mainFrame, panelCircuitos);
	}

	public void continuarCampeonato() {
		// TODO Auto-generated method stub

	}

	public void dadosPersistencia() {
		// TODO Auto-generated method stub

	}

}
