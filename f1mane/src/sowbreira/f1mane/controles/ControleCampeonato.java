package sowbreira.f1mane.controles;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.border.TitledBorder;

import sowbreira.f1mane.MainFrame;
import sowbreira.f1mane.entidades.Campeonato;
import sowbreira.f1mane.entidades.Circuito;
import sowbreira.f1mane.entidades.ConstrutoresPontosCampeonato;
import sowbreira.f1mane.entidades.CorridaCampeonato;
import sowbreira.f1mane.entidades.Piloto;
import sowbreira.f1mane.entidades.PilotosPontosCampeonato;
import sowbreira.f1mane.entidades.Volta;
import sowbreira.f1mane.recursos.CarregadorRecursos;
import sowbreira.f1mane.recursos.idiomas.Lang;
import sowbreira.f1mane.visao.PainelCampeonato;
import br.nnpe.Logger;

public class ControleCampeonato {

	private MainFrame mainFrame;

	private Campeonato campeonato;

	private CarregadorRecursos carregadorRecursos;

	private String circuitoJogando;

	public ControleCampeonato(MainFrame mainFrame) {
		carregarCircuitos();
		this.mainFrame = mainFrame;
		carregadorRecursos = new CarregadorRecursos();
		circuitosPilotos = carregadorRecursos.carregarTemporadasPilotos();
	}

	public Campeonato getCampeonato() {
		return campeonato;
	}

	protected Map circuitos = new HashMap();

	protected Map circuitosPilotos = new HashMap();

	private long tempoInicio;

	private List pilotosPontos;

	private ArrayList contrutoresPontos;

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

		JPanel temporadasPanel = new JPanel(new GridLayout(1, 2));
		temporadasPanel.add(new JLabel() {
			@Override
			public String getText() {
				return Lang.msg("272");
			}
		});
		JComboBox temporadas = new JComboBox(carregadorRecursos
				.getVectorTemps());
		temporadasPanel.add(temporadas);

		final DefaultListModel defaultListModelPilotosSelecionados = new DefaultListModel();
		JList listPilotosSelecionados = new JList(
				defaultListModelPilotosSelecionados);
		final List tempList = new LinkedList();
		temporadas.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent arg0) {
				tempList.clear();
				String temporarada = (String) ControleCampeonato.this.carregadorRecursos
						.getTemporadas().get(arg0.getItem());
				tempList.addAll((Collection) circuitosPilotos.get(temporarada));
				Collections.sort(tempList, new Comparator() {

					@Override
					public int compare(Object o1, Object o2) {
						Piloto p1 = (Piloto) o1;
						Piloto p2 = (Piloto) o2;
						return p1.getCarro().getNome().compareTo(
								p2.getCarro().getNome());
					}

				});
				defaultListModelPilotosSelecionados.clear();
				for (Iterator iterator = tempList.iterator(); iterator
						.hasNext();) {
					Piloto piloto = (Piloto) iterator.next();
					defaultListModelPilotosSelecionados.addElement(piloto);
				}

			}
		});
		temporadas.setSelectedIndex(1);
		temporadas.setSelectedIndex(0);
		final JPanel panel2nd = new JPanel(new BorderLayout());

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
		panel2nd.add(temporadasPanel, BorderLayout.NORTH);
		panel2nd.add(scrolllistPilotosSelecionados, BorderLayout.CENTER);
		panel2nd.add(grid, BorderLayout.SOUTH);

		JPanel panel3rd = new JPanel();
		// panel3rd.setLayout(new GridLayout(1, 2));
		panel3rd.add(panel1st);
		panel3rd.add(panel2nd);

		JOptionPane.showMessageDialog(mainFrame, panel3rd, Lang.msg("276"),
				JOptionPane.INFORMATION_MESSAGE);

		campeonato = new Campeonato();
		List corridas = new ArrayList();
		for (int i = 0; i < defaultListModelCircuitosSelecionados.getSize(); i++) {
			corridas.add(defaultListModelCircuitosSelecionados.get(i));
		}

		List pilotos = new ArrayList();
		Object[] pilotosSel = listPilotosSelecionados.getSelectedValues();
		for (int i = 0; i < pilotosSel.length; i++) {
			pilotos.add(pilotosSel[i].toString());
		}
		campeonato.setCorridas(corridas);

		campeonato.setPilotos(pilotos);

		campeonato.setTemporada((String) temporadas.getSelectedItem());

		campeonato.setNivel(Lang.key((String) comboBoxNivelCorrida
				.getSelectedItem()));

		campeonato.setQtdeVoltas((Integer) spinnerQtdeVoltas.getValue());

		new PainelCampeonato(this, mainFrame);

	}

	public void continuarCampeonato() {
		try {
			JTextArea xmlArea = new JTextArea(30, 50);
			JScrollPane xmlPane = new JScrollPane(xmlArea);
			xmlPane.setBorder(new TitledBorder(Lang.msg("282")));
			JOptionPane.showMessageDialog(mainFrame, xmlPane, Lang.msg("281"),
					JOptionPane.INFORMATION_MESSAGE);

			ByteArrayInputStream bin = new ByteArrayInputStream(xmlArea
					.getText().getBytes());
			XMLDecoder xmlDecoder = new XMLDecoder(bin);
			campeonato = (Campeonato) xmlDecoder.readObject();
		} catch (Exception e) {
			StackTraceElement[] trace = e.getStackTrace();
			StringBuffer retorno = new StringBuffer();
			int size = ((trace.length > 10) ? 10 : trace.length);
			for (int i = 0; i < size; i++)
				retorno.append(trace[i] + "\n");
			JOptionPane.showMessageDialog(mainFrame, retorno.toString(), Lang
					.msg("283"), JOptionPane.ERROR_MESSAGE);
			Logger.logarExept(e);
		}
		new PainelCampeonato(this, mainFrame);
	}

	public void dadosPersistencia() {
		if (campeonato != null) {
			ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
			XMLEncoder encoder = new XMLEncoder(byteArrayOutputStream);
			encoder.writeObject(campeonato);
			encoder.flush();
			JTextArea xmlArea = new JTextArea(30, 50);
			xmlArea.setText(new String(byteArrayOutputStream.toByteArray())
					+ "</java>");
			xmlArea.setEditable(false);
			xmlArea.setSelectionStart(0);
			xmlArea.setSelectionEnd(xmlArea.getCaretPosition());
			JScrollPane xmlPane = new JScrollPane(xmlArea);
			xmlPane.setBorder(new TitledBorder(Lang.msg("280")));
			JOptionPane.showMessageDialog(mainFrame, xmlPane, Lang.msg("281"),
					JOptionPane.INFORMATION_MESSAGE);
		}
	}

	public void processaFimCorrida(List pilotos) {
		List corridaCampeonatoDados = new ArrayList();
		for (Iterator iterator = pilotos.iterator(); iterator.hasNext();) {
			Piloto p = (Piloto) iterator.next();
			CorridaCampeonato corridaCampeonato = new CorridaCampeonato();
			corridaCampeonato.setTempoInicio(tempoInicio);
			corridaCampeonato.setTempoFim(System.currentTimeMillis());
			corridaCampeonato.setPosicao(p.getPosicao());
			corridaCampeonato.setPiloto(p.getNome());
			corridaCampeonato.setCarro(p.getCarro().getNome());
			corridaCampeonato.setTpPneu(p.getCarro().getTipoPneu());
			corridaCampeonato.setNumVoltas(p.getNumeroVolta());
			Volta volta = p.obterVoltaMaisRapida();
			corridaCampeonato.setVoltaMaisRapida(volta
					.obterTempoVoltaFormatado());
			corridaCampeonato.setQtdeParadasBox(p.getQtdeParadasBox());
			corridaCampeonato.setDesgastePneus(String.valueOf(p.getCarro()
					.porcentagemDesgastePeneus()
					+ "%"));
			corridaCampeonato.setCombustivelRestante(String.valueOf(p
					.getCarro().porcentagemCombustivel()
					+ "%"));
			corridaCampeonato.setDesgasteMotor(String.valueOf(p.getCarro()
					.porcentagemDesgasteMotor()
					+ "%"));

			if (p.getPosicao() == 1) {
				corridaCampeonato.setPontos(10);
			} else if (p.getPosicao() == 2) {
				corridaCampeonato.setPontos(8);
			} else if (p.getPosicao() == 3) {
				corridaCampeonato.setPontos(6);
			} else if (p.getPosicao() == 4) {
				corridaCampeonato.setPontos(5);
			} else if (p.getPosicao() == 5) {
				corridaCampeonato.setPontos(4);
			} else if (p.getPosicao() == 6) {
				corridaCampeonato.setPontos(3);
			} else if (p.getPosicao() == 7) {
				corridaCampeonato.setPontos(2);
			} else if (p.getPosicao() == 8) {
				corridaCampeonato.setPontos(1);
			} else {
				corridaCampeonato.setPontos(0);
			}
			corridaCampeonatoDados.add(corridaCampeonato);
		}
		campeonato.getDadosCorridas().put(circuitoJogando,
				corridaCampeonatoDados);
	}

	public void iniciaCorrida(String circuito) {
		circuitoJogando = circuito;
		tempoInicio = System.currentTimeMillis();
	}

	public void geraListaPilotosPontos() {
		pilotosPontos = new ArrayList();
		if (campeonato.getCorridas().isEmpty()) {
			return;
		}
		String circuito = (String) campeonato.getCorridas().get(0);
		List dadosCorridas = (List) campeonato.getDadosCorridas().get(circuito);
		for (Iterator iterator = dadosCorridas.iterator(); iterator.hasNext();) {
			CorridaCampeonato corridaCampeonato = (CorridaCampeonato) iterator
					.next();
			PilotosPontosCampeonato pilotosPontosCampeonato = new PilotosPontosCampeonato();
			pilotosPontosCampeonato.setNome(corridaCampeonato.getPiloto());
			pilotosPontos.add(pilotosPontosCampeonato);
		}
		for (Iterator iterator = pilotosPontos.iterator(); iterator.hasNext();) {
			PilotosPontosCampeonato pilotosPontosCampeonato = (PilotosPontosCampeonato) iterator
					.next();
			pilotosPontosCampeonato
					.setPontos(calculaPontosPiloto(pilotosPontosCampeonato
							.getNome()));

		}
	}

	public List getPilotosPontos() {
		return pilotosPontos;
	}

	private int calculaPontosPiloto(String nome) {
		int pontos = 0;
		List corridas = campeonato.getCorridas();
		for (Iterator iterator = corridas.iterator(); iterator.hasNext();) {
			String corrida = (String) iterator.next();
			List dadosCorridas = (List) campeonato.getDadosCorridas().get(
					corrida);
			for (Iterator iterator2 = dadosCorridas.iterator(); iterator2
					.hasNext();) {
				CorridaCampeonato corridaCampeonato = (CorridaCampeonato) iterator2
						.next();
				if (nome.equals(corridaCampeonato.getPiloto())) {
					pontos += corridaCampeonato.getPontos();
				}
			}
		}
		return pontos;
	}

	public ArrayList getContrutoresPontos() {
		return contrutoresPontos;
	}

	public void geraListaContrutoresPontos() {
		contrutoresPontos = new ArrayList();
		if (campeonato.getCorridas().isEmpty()) {
			return;
		}
		String circuito = (String) campeonato.getCorridas().get(0);
		List dadosCorridas = (List) campeonato.getDadosCorridas().get(circuito);
		for (Iterator iterator = dadosCorridas.iterator(); iterator.hasNext();) {
			CorridaCampeonato corridaCampeonato = (CorridaCampeonato) iterator
					.next();
			ConstrutoresPontosCampeonato construtoresPontosCampeonato = new ConstrutoresPontosCampeonato();

			construtoresPontosCampeonato.setNomeEquipe(corridaCampeonato
					.getCarro());
			if (!contrutoresPontos.contains(construtoresPontosCampeonato)) {
				contrutoresPontos.add(construtoresPontosCampeonato);
			}
		}
		for (Iterator iterator = contrutoresPontos.iterator(); iterator
				.hasNext();) {
			ConstrutoresPontosCampeonato construtoresPontosCampeonato = (ConstrutoresPontosCampeonato) iterator
					.next();
			construtoresPontosCampeonato
					.setPontos(calculaPontosConstrutores(construtoresPontosCampeonato
							.getNomeEquipe()));

		}

	}

	private int calculaPontosConstrutores(String nome) {
		int pontos = 0;
		List corridas = campeonato.getCorridas();
		for (Iterator iterator = corridas.iterator(); iterator.hasNext();) {
			String corrida = (String) iterator.next();
			List dadosCorridas = (List) campeonato.getDadosCorridas().get(
					corrida);
			for (Iterator iterator2 = dadosCorridas.iterator(); iterator2
					.hasNext();) {
				CorridaCampeonato corridaCampeonato = (CorridaCampeonato) iterator2
						.next();
				if (nome.equals(corridaCampeonato.getCarro())) {
					pontos += corridaCampeonato.getPontos();
				}
			}
		}
		return pontos;
	}
}
