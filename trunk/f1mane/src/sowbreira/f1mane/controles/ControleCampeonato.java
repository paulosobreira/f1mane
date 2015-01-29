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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URL;
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

import javax.jnlp.FileContents;
import javax.jnlp.PersistenceService;
import javax.jnlp.ServiceManager;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import sowbreira.f1mane.MainFrame;
import sowbreira.f1mane.entidades.Campeonato;
import sowbreira.f1mane.entidades.Carro;
import sowbreira.f1mane.entidades.ConstrutoresPontosCampeonato;
import sowbreira.f1mane.entidades.CorridaCampeonato;
import sowbreira.f1mane.entidades.Piloto;
import sowbreira.f1mane.entidades.PilotosPontosCampeonato;
import sowbreira.f1mane.entidades.Volta;
import sowbreira.f1mane.recursos.CarregadorRecursos;
import sowbreira.f1mane.recursos.idiomas.Lang;
import sowbreira.f1mane.visao.PainelCampeonato;
import br.nnpe.Constantes;
import br.nnpe.Logger;
import br.nnpe.Util;

public class ControleCampeonato {

	private MainFrame mainFrame;

	private Campeonato campeonato;

	private CarregadorRecursos carregadorRecursos;

	private String circuitoJogando;

	private JCheckBox semReabastacimento;

	private JCheckBox semTrocaPneu;

	private JCheckBox kers;

	private JCheckBox drs;

	public ControleCampeonato(MainFrame mainFrame) {
		carregarCircuitos();
		this.mainFrame = mainFrame;
		carregadorRecursos = new CarregadorRecursos(true);
		circuitosPilotos = carregadorRecursos.carregarTemporadasPilotos();
	}

	public ControleCampeonato(Campeonato campeonato, MainFrame mainFrame) {
		this.campeonato = campeonato;
		this.mainFrame = mainFrame;
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
		continuarCampeonatoCache();
		if (campeonato != null) {
			int showConfirmDialog = JOptionPane.showConfirmDialog(mainFrame,
					Lang.msg("existeCampeonato"), Lang.msg("campeonatoSalvo"),
					JOptionPane.YES_NO_OPTION);
			if (JOptionPane.YES_OPTION != showConfirmDialog) {
				campeonato = null;
				return;
			}
		}
		final DefaultListModel defaultListModelCircuitos = new DefaultListModel();
		final DefaultListModel defaultListModelCircuitosSelecionados = new DefaultListModel();
		for (Iterator iterator = circuitos.keySet().iterator(); iterator
				.hasNext();) {
			String key = (String) iterator.next();
			defaultListModelCircuitos.addElement(circuitos.get(key));
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
				int size = defaultListModelCircuitosSelecionados.size();
				for (int i = 0; i < size; i++) {
					defaultListModelCircuitos
							.addElement(defaultListModelCircuitosSelecionados
									.remove(0));
				}
			}

		});
		JButton dirAll = new JButton(">>");
		dirAll.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				int size = defaultListModelCircuitos.size();
				for (int i = 0; i < size; i++) {
					defaultListModelCircuitosSelecionados
							.addElement(defaultListModelCircuitos.remove(0));
				}
			}
		});
		buttonsPanel.add(dir);
		buttonsPanel.add(esq);
		buttonsPanel.add(dirAll);
		buttonsPanel.add(esqAll);

		JButton cima = new JButton("Cima") {
			@Override
			public String getText() {
				return Lang.msg("287");
			}
		};
		cima.setEnabled(false);
		JButton baixo = new JButton("Baixo") {
			@Override
			public String getText() {
				return Lang.msg("288");
			}
		};
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
		JComboBox temporadas = new JComboBox(
				carregadorRecursos.getVectorTemps());
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
						return p1.getCarro().getNome()
								.compareTo(p2.getCarro().getNome());
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
				return Lang.msg("110",
						new String[] { String.valueOf(Constantes.MIN_VOLTAS),
								String.valueOf(Constantes.MAX_VOLTAS) });
			}
		});
		JSpinner spinnerQtdeVoltas = new JSpinner();
		spinnerQtdeVoltas.setValue(new Integer(12));
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
		panel3rd.add(panel1st);
		panel3rd.add(panel2nd);

		JOptionPane.showMessageDialog(mainFrame, panel3rd, Lang.msg("276"),
				JOptionPane.INFORMATION_MESSAGE);

		List corridas = new ArrayList();
		for (int i = 0; i < defaultListModelCircuitosSelecionados.getSize(); i++) {
			corridas.add(defaultListModelCircuitosSelecionados.get(i));
		}

		List pilotos = new ArrayList();
		Object[] pilotosSel = listPilotosSelecionados.getSelectedValues();
		for (int i = 0; i < pilotosSel.length; i++) {
			pilotos.add(pilotosSel[i].toString());
		}

		if (corridas.isEmpty()) {
			JOptionPane.showMessageDialog(mainFrame, Lang.msg("296"),
					Lang.msg("296"), JOptionPane.ERROR_MESSAGE);
			return;
		}
		Integer qtdeVolta = (Integer) spinnerQtdeVoltas.getValue();
		if (qtdeVolta == null || qtdeVolta.intValue() < Constantes.MIN_VOLTAS) {
			JOptionPane.showMessageDialog(
					mainFrame,
					Lang.msg(
							"110",
							new String[] {
									String.valueOf(Constantes.MIN_VOLTAS),
									String.valueOf(Constantes.MAX_VOLTAS) }),
					Lang.msg(
							"110",
							new String[] {
									String.valueOf(Constantes.MIN_VOLTAS),
									String.valueOf(Constantes.MAX_VOLTAS) }),
					JOptionPane.ERROR_MESSAGE);
			return;
		}
		campeonato = new Campeonato();
		campeonato.setCorridas(corridas);
		campeonato.setPilotos(pilotos);
		campeonato.setTemporada((String) temporadas.getSelectedItem());
		campeonato.setNivel(Lang.key((String) comboBoxNivelCorrida
				.getSelectedItem()));
		campeonato.setQtdeVoltas((Integer) spinnerQtdeVoltas.getValue());
		persistirEmCache();
		new PainelCampeonato(this, mainFrame);
	}

	private void persistirEmCache() {
		try {
			PersistenceService persistenceService = (PersistenceService) ServiceManager
					.lookup("javax.jnlp.PersistenceService");
			FileContents fileContents = null;
			String url = mainFrame.getCodeBase() + "campeonato";
			try {
				fileContents = persistenceService.get(new URL(url));
			} catch (Exception e) {
				persistenceService.create(new URL(url), 1048576);
				fileContents = persistenceService.get(new URL(url));
			}
			ObjectOutputStream stream = new ObjectOutputStream(
					fileContents.getOutputStream(true));
			stream.writeObject(campeonato);
			stream.flush();
		} catch (Exception e) {
			Logger.logarExept(e);
			try {
				salvarXmlEmDisco();
			} catch (Exception e2) {
				Logger.logarExept(e);
				dadosPersistencia(campeonato, mainFrame);
			}
		}

	}

	private void salvarXmlEmDisco() throws FileNotFoundException, IOException {
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		XMLEncoder encoder = new XMLEncoder(byteArrayOutputStream);
		encoder.writeObject(campeonato);
		encoder.flush();
		String save = new String(byteArrayOutputStream.toByteArray())
				+ "</java>";
		FileOutputStream fileOutputStream = new FileOutputStream(new File(
				"f1mane_save.xml"));
		fileOutputStream.write(save.getBytes());
		fileOutputStream.close();
	}

	public Campeonato continuarCampeonato() {
		continuarCampeonatoCache();
		return campeonato;
	}

	public Campeonato continuarCampeonatoXmlDisco() {
		try {
			FileInputStream fileInputStream = new FileInputStream(new File(
					"f1mane_save.xml"));
			XMLDecoder xmlDecoder = new XMLDecoder(fileInputStream);
			campeonato = (Campeonato) xmlDecoder.readObject();
			return campeonato;
		} catch (Exception e) {
			Logger.logarExept(e);
		}
		return null;
	}

	public Campeonato continuarCampeonatoXml() {
		try {
			JTextArea xmlArea = new JTextArea(30, 50);
			JScrollPane xmlPane = new JScrollPane(xmlArea);
			xmlPane.setBorder(new TitledBorder(Lang.msg("282")));
			JOptionPane.showMessageDialog(mainFrame, xmlPane, Lang.msg("281"),
					JOptionPane.INFORMATION_MESSAGE);

			if (Util.isNullOrEmpty(xmlArea.getText())) {
				return null;
			}
			ByteArrayInputStream bin = new ByteArrayInputStream(xmlArea
					.getText().getBytes());
			XMLDecoder xmlDecoder = new XMLDecoder(bin);
			campeonato = (Campeonato) xmlDecoder.readObject();
			return campeonato;
		} catch (Exception e) {
			StackTraceElement[] trace = e.getStackTrace();
			StringBuffer retorno = new StringBuffer();
			int size = ((trace.length > 10) ? 10 : trace.length);
			for (int i = 0; i < size; i++)
				retorno.append(trace[i] + "\n");
			JOptionPane.showMessageDialog(mainFrame, retorno.toString(),
					Lang.msg("283"), JOptionPane.ERROR_MESSAGE);
			Logger.logarExept(e);
		}
		return null;
	}

	public void continuarCampeonatoCache() {
		try {
			PersistenceService persistenceService = (PersistenceService) ServiceManager
					.lookup("javax.jnlp.PersistenceService");
			String url = mainFrame.getCodeBase() + "campeonato";
			FileContents fileContents = persistenceService.get(new URL(url));
			ObjectInputStream ois = new ObjectInputStream(
					fileContents.getInputStream());
			campeonato = (Campeonato) ois.readObject();
		} catch (Exception e) {
			Logger.logarExept(e);
		}

	}

	public static void dadosPersistencia(Campeonato campeonato, JFrame mainFrame) {
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
			Logger.logar(xmlArea.getText());
		}
	}

	public void processaFimCorrida(List<Piloto> pilotos) {
		List<CorridaCampeonato> corridaCampeonatoDados = new ArrayList<CorridaCampeonato>();
		int posicaoJogador = 0;
		int posicaoDesafiando = 0;
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
			if (volta != null)
				corridaCampeonato.setVoltaMaisRapida(volta
						.obterTempoVoltaFormatado());
			corridaCampeonato.setQtdeParadasBox(p.getQtdeParadasBox());
			corridaCampeonato.setDesgastePneus(String.valueOf(p.getCarro()
					.porcentagemDesgastePeneus() + "%"));
			corridaCampeonato.setCombustivelRestante(String.valueOf(p
					.getCarro().porcentagemCombustivel() + "%"));
			corridaCampeonato.setDesgasteMotor(String.valueOf(p.getCarro()
					.porcentagemDesgasteMotor() + "%"));

			if (p.getPosicao() == 1) {
				corridaCampeonato.setPontos(25);
			} else if (p.getPosicao() == 2) {
				corridaCampeonato.setPontos(18);
			} else if (p.getPosicao() == 3) {
				corridaCampeonato.setPontos(15);
			} else if (p.getPosicao() == 4) {
				corridaCampeonato.setPontos(12);
			} else if (p.getPosicao() == 5) {
				corridaCampeonato.setPontos(10);
			} else if (p.getPosicao() == 6) {
				corridaCampeonato.setPontos(8);
			} else if (p.getPosicao() == 7) {
				corridaCampeonato.setPontos(6);
			} else if (p.getPosicao() == 8) {
				corridaCampeonato.setPontos(4);
			} else if (p.getPosicao() == 9) {
				corridaCampeonato.setPontos(2);
			} else if (p.getPosicao() == 10) {
				corridaCampeonato.setPontos(1);
			} else {
				corridaCampeonato.setPontos(0);
			}
			corridaCampeonatoDados.add(corridaCampeonato);
			if (!Util.isNullOrEmpty(campeonato.getNomePiloto())
					&& campeonato.getNomePiloto().equals(p.getNome())) {
				posicaoJogador = p.getPosicao();
				campeonato.setPtsPiloto(campeonato.getPtsPiloto()
						+ campeonato.getPtsGanho());
			}
			if (!Util.isNullOrEmpty(campeonato.getRival())
					&& campeonato.getRival().equals(p.getNome())) {
				posicaoDesafiando = p.getPosicao();
			}

		}
		if (!Util.isNullOrEmpty(campeonato.getRival())) {
			if (posicaoJogador < posicaoDesafiando) {
				campeonato.setVitorias(campeonato.getVitorias() + 1);
			} else {
				campeonato.setDerrotas(campeonato.getDerrotas() + 1);
			}
		}
		campeonato.getDadosCorridas().put(circuitoJogando,
				corridaCampeonatoDados);
		verificaMudancaEquipe();
		persistirEmCache();
	}

	private void verificaMudancaEquipe() {
		int qtdeDisputas = 0;
		if (ControleJogoLocal.NORMAL.equals(campeonato.getNivel())) {
			qtdeDisputas = Util.intervalo(1, 2);
		}
		if (ControleJogoLocal.DIFICIL.equals(campeonato.getNivel())) {
			qtdeDisputas = Util.intervalo(2, 3);
		}

		if (campeonato.getVitorias() > qtdeDisputas) {
			if (campeonato.isFoiDesafiado()) {
				reiniciarDesafio();
				campeonato.setPromovidoEquipeRival(false);
			} else {
				campeonato.setPromovidoEquipeRival(true);
			}
		} else if (campeonato.getDerrotas() > qtdeDisputas) {
			String equipeRival = campeonato.getPilotosEquipesCampeonato().get(
					campeonato.getRival());
			Integer ponteciaEquipeRival = campeonato
					.getEquipesPotenciaCampeonato().get(equipeRival);
			String equipeJogador = campeonato.getPilotosEquipesCampeonato()
					.get(campeonato.getNomePiloto());
			Integer potenciaEquipeJogador = campeonato
					.getEquipesPotenciaCampeonato().get(equipeJogador);
			ponteciaEquipeRival = ponteciaEquipeRival == null ? 0
					: ponteciaEquipeRival;
			potenciaEquipeJogador = potenciaEquipeJogador == null ? 0
					: potenciaEquipeJogador;
			if (ponteciaEquipeRival > potenciaEquipeJogador) {
				reiniciarDesafio();
				campeonato.setRebaixadoEquipeRival(false);
			} else {
				campeonato.setRebaixadoEquipeRival(true);
			}
		}
	}

	public void processaMudancaEquipe() {
		campeonato.setNomePiloto(campeonato.getRival());
		reiniciarDesafio();
	}

	private void reiniciarDesafio() {
		campeonato.setRival(null);
		campeonato.setFoiDesafiado(false);
		campeonato.setVitorias(0);
		campeonato.setDerrotas(0);
	}

	public void iniciaCorrida(String circuito) {
		circuitoJogando = circuito;
		tempoInicio = System.currentTimeMillis();
	}

	public void geraListaPilotosPontos() {
		if (pilotosPontos == null)
			pilotosPontos = new ArrayList();
		pilotosPontos.clear();
		if (campeonato.getCorridas().isEmpty()) {
			return;
		}
		String circuito = (String) campeonato.getCorridas().get(0);
		List dadosCorridas = (List) campeonato.getDadosCorridas().get(circuito);
		if (dadosCorridas == null) {
			return;
		}
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
			pilotosPontosCampeonato
					.setVitorias(computaVitorias(pilotosPontosCampeonato
							.getNome()));
		}
		Collections.sort(pilotosPontos, new Comparator() {
			public int compare(Object o1, Object o2) {
				PilotosPontosCampeonato p1 = (PilotosPontosCampeonato) o1;
				PilotosPontosCampeonato p2 = (PilotosPontosCampeonato) o2;
				if (p1.getPontos() != p2.getPontos()) {
					return new Integer(p2.getPontos()).compareTo(new Integer(p1
							.getPontos()));
				} else {
					return new Integer(p2.getVitorias()).compareTo(new Integer(
							p1.getVitorias()));
				}
			}
		});

	}

	public List<PilotosPontosCampeonato> getPilotosPontos() {
		return pilotosPontos;
	}

	private int calculaPontosPiloto(String nome) {
		int pontos = 0;
		List corridas = campeonato.getCorridas();
		for (Iterator iterator = corridas.iterator(); iterator.hasNext();) {
			String corrida = (String) iterator.next();
			List dadosCorridas = (List) campeonato.getDadosCorridas().get(
					corrida);
			if (dadosCorridas == null) {
				continue;
			}
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

	public List<ConstrutoresPontosCampeonato> getContrutoresPontos() {
		return contrutoresPontos;
	}

	public void geraListaContrutoresPontos() {
		contrutoresPontos = new ArrayList();
		if (campeonato.getCorridas().isEmpty()) {
			return;
		}
		String circuito = (String) campeonato.getCorridas().get(0);
		List dadosCorridas = (List) campeonato.getDadosCorridas().get(circuito);
		if (dadosCorridas == null) {
			return;
		}
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
		Collections.sort(contrutoresPontos, new Comparator() {
			public int compare(Object o1, Object o2) {
				ConstrutoresPontosCampeonato c1 = (ConstrutoresPontosCampeonato) o1;
				ConstrutoresPontosCampeonato c2 = (ConstrutoresPontosCampeonato) o2;
				return new Integer(c2.getPontos()).compareTo(new Integer(c1
						.getPontos()));
			}
		});

	}

	private int calculaPontosConstrutores(String nome) {
		int pontos = 0;
		List corridas = campeonato.getCorridas();
		for (Iterator iterator = corridas.iterator(); iterator.hasNext();) {
			String corrida = (String) iterator.next();
			List dadosCorridas = (List) campeonato.getDadosCorridas().get(
					corrida);
			if (dadosCorridas == null) {
				continue;
			}
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

	public void detalhesCorrida() {
		if (campeonato != null)
			new PainelCampeonato(this, mainFrame);

	}

	public Integer computaVitorias(String nome) {
		int vitorias = 0;
		List corridas = campeonato.getCorridas();
		for (Iterator iterator = corridas.iterator(); iterator.hasNext();) {
			String corrida = (String) iterator.next();
			List dadosCorridas = (List) campeonato.getDadosCorridas().get(
					corrida);
			if (dadosCorridas == null) {
				continue;
			}
			for (Iterator iterator2 = dadosCorridas.iterator(); iterator2
					.hasNext();) {
				CorridaCampeonato corridaCampeonato = (CorridaCampeonato) iterator2
						.next();
				if (nome.equals(corridaCampeonato.getPiloto())
						&& corridaCampeonato.getPosicao() == 1) {
					vitorias += 1;
				}
			}
		}
		return vitorias;
	}

	public void criarCampeonatoPiloto() {
		continuarCampeonatoCache();
		if (campeonato != null) {
			int showConfirmDialog = JOptionPane.showConfirmDialog(mainFrame,
					Lang.msg("existeCampeonato"), Lang.msg("campeonatoSalvo"),
					JOptionPane.YES_NO_OPTION);
			if (JOptionPane.YES_OPTION != showConfirmDialog) {
				campeonato = null;
				return;
			}
		}
		JPanel panelPiloto = new JPanel(new GridLayout(3, 1));

		JPanel panelPilotoNome = new JPanel(new GridLayout(1, 2));
		JLabel labelPiloto = new JLabel() {
			@Override
			public String getText() {
				return Lang.msg("nomePiloto");
			}
		};
		JTextField nomePiloto = new JTextField(20);
		panelPilotoNome.add(labelPiloto);
		panelPilotoNome.add(nomePiloto);
		panelPiloto.add(panelPilotoNome);

		JPanel temporadasPanel = new JPanel(new GridLayout(1, 2));
		temporadasPanel.add(new JLabel() {
			@Override
			public String getText() {
				return Lang.msg("272");
			}
		});
		final JComboBox temporadas = new JComboBox(
				carregadorRecursos.getVectorTemps());
		temporadasPanel.add(temporadas);
		panelPiloto.add(temporadasPanel);

		JPanel nivelPanel = new JPanel(new GridLayout(1, 2));

		final JComboBox comboBoxNivelCorrida = new JComboBox();
		comboBoxNivelCorrida.addItem(Lang.msg(ControleJogoLocal.NORMAL));
		comboBoxNivelCorrida.addItem(Lang.msg(ControleJogoLocal.FACIL));
		comboBoxNivelCorrida.addItem(Lang.msg(ControleJogoLocal.DIFICIL));
		nivelPanel.add(new JLabel() {
			public String getText() {
				return Lang.msg("212");
			}
		});
		nivelPanel.add(comboBoxNivelCorrida);

		panelPiloto.add(nivelPanel);

		int showConfirmDialog = JOptionPane.showConfirmDialog(mainFrame,
				panelPiloto, Lang.msg("pilotoCampeonato"),
				JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE);

		if (JOptionPane.YES_OPTION != showConfirmDialog) {
			campeonato = null;
			return;
		}

		if (Util.isNullOrEmpty(nomePiloto.getText())) {
			JOptionPane.showMessageDialog(mainFrame, Lang.msg("nomePiloto"),
					Lang.msg("nomePiloto"), JOptionPane.ERROR_MESSAGE);
			return;
		}

		final DefaultListModel defaultListModelCircuitos = new DefaultListModel();
		final DefaultListModel defaultListModelCircuitosSelecionados = new DefaultListModel();
		for (Iterator iterator = circuitos.keySet().iterator(); iterator
				.hasNext();) {
			String key = (String) iterator.next();
			defaultListModelCircuitos.addElement(circuitos.get(key));
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
				int size = defaultListModelCircuitosSelecionados.size();
				for (int i = 0; i < size; i++) {
					defaultListModelCircuitos
							.addElement(defaultListModelCircuitosSelecionados
									.remove(0));
				}
			}

		});
		JButton dirAll = new JButton(">>");
		dirAll.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				int size = defaultListModelCircuitos.size();
				for (int i = 0; i < size; i++) {
					defaultListModelCircuitosSelecionados
							.addElement(defaultListModelCircuitos.remove(0));
				}
			}
		});
		buttonsPanel.add(dir);
		buttonsPanel.add(esq);
		buttonsPanel.add(dirAll);
		buttonsPanel.add(esqAll);

		JButton cima = new JButton("Cima") {
			@Override
			public String getText() {
				return Lang.msg("287");
			}
		};
		cima.setEnabled(false);
		JButton baixo = new JButton("Baixo") {
			@Override
			public String getText() {
				return Lang.msg("288");
			}
		};
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

		final DefaultListModel defaultListModelPilotosSelecionados = new DefaultListModel();
		JList listPilotosSelecionados = new JList(
				defaultListModelPilotosSelecionados);
		final JPanel panel2nd = new JPanel(new BorderLayout());

		JPanel grid = new JPanel();

		grid.setLayout(new GridLayout(5, 2));
		grid.add(new JLabel() {

			public String getText() {
				return Lang.msg("110",
						new String[] { String.valueOf(Constantes.MIN_VOLTAS),
								String.valueOf(Constantes.MAX_VOLTAS) });
			}
		});
		JSpinner spinnerQtdeVoltas = new JSpinner();
		spinnerQtdeVoltas.setValue(new Integer(12));
		grid.add(spinnerQtdeVoltas);
		grid.add(new JLabel() {
			@Override
			public String getText() {
				return Lang.msg("302");
			}
		});
		semReabastacimento = new JCheckBox();
		grid.add(semReabastacimento);
		grid.add(new JLabel() {
			@Override
			public String getText() {
				return Lang.msg("303");
			}
		});
		semTrocaPneu = new JCheckBox();

		grid.add(semTrocaPneu);

		grid.add(new JLabel() {
			@Override
			public String getText() {
				return Lang.msg("kers");
			}
		});
		kers = new JCheckBox();

		grid.add(kers);

		grid.add(new JLabel() {
			@Override
			public String getText() {
				return Lang.msg("drs");
			}
		});
		drs = new JCheckBox();

		grid.add(drs);

		JScrollPane scrolllistPilotosSelecionados = new JScrollPane(
				listPilotosSelecionados) {
			@Override
			public Dimension getPreferredSize() {
				return new Dimension(210, 200);
			}
		};
		scrolllistPilotosSelecionados.setBorder(new TitledBorder(Lang
				.msg("274")));

		panel2nd.add(scrolllistPilotosSelecionados, BorderLayout.CENTER);
		panel2nd.add(grid, BorderLayout.SOUTH);

		JPanel panel3rd = new JPanel();
		panel3rd.add(panel1st);
		panel3rd.add(panel2nd);

		final List tempList = new LinkedList();
		String temporarada = (String) ControleCampeonato.this.carregadorRecursos
				.getTemporadas().get(temporadas.getSelectedItem());
		tempList.addAll((Collection) circuitosPilotos.get(temporarada));
		Map<String, String> pilotosEquipesCampeonato = new HashMap<String, String>();
		Map<String, Integer> equipesPotenciaCampeonato = new HashMap<String, Integer>();
		Map<String, Integer> pilotosHabilidadeCampeonato = new HashMap<String, Integer>();
		for (Iterator iterator = tempList.iterator(); iterator.hasNext();) {
			Piloto piloto = (Piloto) iterator.next();
			pilotosEquipesCampeonato.put(piloto.getNome(), piloto.getCarro()
					.getNome());
			equipesPotenciaCampeonato.put(piloto.getCarro().getNome(), piloto
					.getCarro().getPotenciaReal());
			pilotosHabilidadeCampeonato.put(piloto.getNome(),
					piloto.getHabilidade());
		}

		Collections.sort(tempList, new Comparator() {

			@Override
			public int compare(Object o1, Object o2) {
				Piloto p1 = (Piloto) o1;
				Piloto p2 = (Piloto) o2;
				return new Integer(p2.getCarro().getPotenciaReal())
						.compareTo(new Integer(p1.getCarro().getPotenciaReal()));
			}

		});
		ArrayList listCarros = new ArrayList();
		for (Iterator iterator = tempList.iterator(); iterator.hasNext();) {
			Piloto p = (Piloto) iterator.next();
			if (!listCarros.contains(p.getCarro())) {
				listCarros.add(p.getCarro());
			}

		}

		if (ControleJogoLocal.DIFICIL.equals(Lang
				.key((String) comboBoxNivelCorrida.getSelectedItem()))) {
			Carro ult = ((Carro) listCarros.get(listCarros.size() - 1));
			for (Iterator iterator = tempList.iterator(); iterator.hasNext();) {
				Piloto piloto = (Piloto) iterator.next();
				if (!(piloto.getCarro().equals(ult))) {
					iterator.remove();
				}
			}

		} else if (ControleJogoLocal.NORMAL.equals(Lang
				.key((String) comboBoxNivelCorrida.getSelectedItem()))) {
			Carro penu = ((Carro) listCarros.get(listCarros.size() - 2));
			Carro ult = ((Carro) listCarros.get(listCarros.size() - 1));
			for (Iterator iterator = tempList.iterator(); iterator.hasNext();) {
				Piloto piloto = (Piloto) iterator.next();
				if (!(piloto.getCarro().equals(penu) || piloto.getCarro()
						.equals(ult))) {
					iterator.remove();
				}
			}
		} else if (ControleJogoLocal.FACIL.equals(Lang
				.key((String) comboBoxNivelCorrida.getSelectedItem()))) {
			Carro antP = ((Carro) listCarros.get(listCarros.size() - 3));
			Carro penu = ((Carro) listCarros.get(listCarros.size() - 2));
			Carro ult = ((Carro) listCarros.get(listCarros.size() - 1));
			for (Iterator iterator = tempList.iterator(); iterator.hasNext();) {
				Piloto piloto = (Piloto) iterator.next();
				if (!(piloto.getCarro().equals(penu)
						|| piloto.getCarro().equals(ult) || piloto.getCarro()
						.equals(antP))) {
					iterator.remove();
				}
			}
		}
		Piloto prim;
		Piloto last;
		prim = (Piloto) tempList.get(0);
		last = (Piloto) tempList.get(tempList.size() - 1);
		int ptsPiloto = 0;
		if (ControleJogoLocal.DIFICIL.equals(Lang
				.key((String) comboBoxNivelCorrida.getSelectedItem()))) {
			ptsPiloto = last.getHabilidade();
		} else if (ControleJogoLocal.NORMAL.equals(Lang
				.key((String) comboBoxNivelCorrida.getSelectedItem()))) {
			ptsPiloto = (last.getHabilidade() + prim.getHabilidade()) / 2;
		} else if (ControleJogoLocal.FACIL.equals(Lang
				.key((String) comboBoxNivelCorrida.getSelectedItem()))) {
			ptsPiloto = prim.getHabilidade();
		}

		defaultListModelPilotosSelecionados.clear();
		for (Iterator iterator = tempList.iterator(); iterator.hasNext();) {
			Piloto piloto = (Piloto) iterator.next();
			defaultListModelPilotosSelecionados.addElement(piloto);
		}

		JOptionPane.showMessageDialog(mainFrame, panel3rd, Lang.msg("276"),
				JOptionPane.INFORMATION_MESSAGE);

		List corridas = new ArrayList();
		for (int i = 0; i < defaultListModelCircuitosSelecionados.getSize(); i++) {
			corridas.add(defaultListModelCircuitosSelecionados.get(i));
		}

		if (corridas.isEmpty()) {
			JOptionPane.showMessageDialog(mainFrame, Lang.msg("296"),
					Lang.msg("296"), JOptionPane.ERROR_MESSAGE);
			return;
		}
		Integer qtdeVolta = (Integer) spinnerQtdeVoltas.getValue();
		if (qtdeVolta == null || qtdeVolta.intValue() < Constantes.MIN_VOLTAS) {
			JOptionPane.showMessageDialog(
					mainFrame,
					Lang.msg(
							"110",
							new String[] {
									String.valueOf(Constantes.MIN_VOLTAS),
									String.valueOf(Constantes.MAX_VOLTAS) }),
					Lang.msg(
							"110",
							new String[] {
									String.valueOf(Constantes.MIN_VOLTAS),
									String.valueOf(Constantes.MAX_VOLTAS) }),
					JOptionPane.ERROR_MESSAGE);
			return;
		}
		Piloto pilotoSelecionado = (Piloto) listPilotosSelecionados
				.getSelectedValue();
		if (pilotoSelecionado == null) {
			JOptionPane.showMessageDialog(mainFrame, Lang.msg("277"),
					Lang.msg("277"), JOptionPane.ERROR_MESSAGE);
			return;
		}
		pilotosEquipesCampeonato.remove(pilotoSelecionado.getNome());
		pilotosEquipesCampeonato.put(nomePiloto.getText(), pilotoSelecionado
				.getCarro().getNome());
		campeonato = new Campeonato();
		campeonato.setCorridas(corridas);
		int diffGanho = 30 / corridas.size();
		campeonato.setPtsGanho(diffGanho);
		campeonato.setNomePiloto(nomePiloto.getText());
		campeonato.setPtsPiloto(ptsPiloto);
		campeonato.setPilotosEquipesCampeonato(pilotosEquipesCampeonato);
		campeonato.setEquipesPotenciaCampeonato(equipesPotenciaCampeonato);
		campeonato.setPilotosHabilidadeCampeonato(pilotosHabilidadeCampeonato);
		campeonato.setDrs(drs.isSelected());
		campeonato.setKers(kers.isSelected());
		campeonato.setSemReabasteciemnto(semReabastacimento.isSelected());
		campeonato.setSemTrocaPneus(semTrocaPneu.isSelected());
		campeonato.setTemporada((String) temporadas.getSelectedItem());
		campeonato.setNivel(Lang.key((String) comboBoxNivelCorrida
				.getSelectedItem()));
		campeonato.setQtdeVoltas((Integer) spinnerQtdeVoltas.getValue());
		persistirEmCache();
		new PainelCampeonato(this, mainFrame);

	}

	public static void main(String[] args) {
		Campeonato campeonato = new Campeonato();
		ControleCampeonato controleCampeonato = new ControleCampeonato(
				campeonato, null);
		campeonato.setVitorias(2);
		controleCampeonato.campeonato = campeonato;
		Map pilotosEquipesCampeonato = new HashMap();
		pilotosEquipesCampeonato.put("Jogador", "Equipe Jogador");
		pilotosEquipesCampeonato.put("Desafio", "Equipe Desafio");
		campeonato.setPilotosEquipesCampeonato(pilotosEquipesCampeonato);
		campeonato.setNomePiloto("Jogador");
		campeonato.setRival("Desafio");
		controleCampeonato.processaMudancaEquipe();
	}

	public void verificaDesafioCampeonatoPiloto() {
		if (campeonato == null
				|| Util.isNullOrEmpty(campeonato.getNomePiloto())) {
			return;
		}
		if (!campeonato.isUltimaCorridaSemDesafiar()) {
			if (Util.isNullOrEmpty(campeonato.getRival())) {
				campeonato.setUltimaCorridaSemDesafiar(true);
			}
			return;
		}
		String equipePiloto = campeonato.getPilotosEquipesCampeonato().get(
				campeonato.getNomePiloto());
		String rival = null;
		String rivalEquipe = null;
		int ptenciaRivalAteAgora = 0;
		for (Iterator iterator = campeonato.getPilotosHabilidadeCampeonato()
				.keySet().iterator(); iterator.hasNext();) {
			String pilotoRival = (String) iterator.next();
			
			String equipeRival = campeonato.getPilotosEquipesCampeonato().get(
					pilotoRival);
			Integer ponteciaEquipeRival = campeonato
					.getEquipesPotenciaCampeonato().get(equipeRival);
			Integer potenciaEquipeJogador = campeonato
					.getEquipesPotenciaCampeonato().get(equipePiloto);
			ponteciaEquipeRival = ponteciaEquipeRival == null ? 0
					: ponteciaEquipeRival;
			potenciaEquipeJogador = potenciaEquipeJogador == null ? 0
					: potenciaEquipeJogador;
			if (ponteciaEquipeRival < potenciaEquipeJogador
					&& ponteciaEquipeRival > ptenciaRivalAteAgora
					&& !equipePiloto.equals(equipeRival)) {
				rival = pilotoRival;
				rivalEquipe = equipeRival;
				ptenciaRivalAteAgora = ponteciaEquipeRival;
			}
		}

		if (!Util.isNullOrEmpty(rival)) {
			campeonato.setRival(rival);
			campeonato.setFoiDesafiado(true);
			campeonato.setUltimaCorridaSemDesafiar(false);
			Logger.logar("rival " + rival);
			Logger.logar("rivalEquipe " + rivalEquipe);
		}

	}

	public Campeonato criarCampeonatoPiloto(List cirucitosCampeonato,
			String temporadaSelecionada, int numVoltasSelecionado,
			int turbulenciaSelecionado, String climaSelecionado,
			String nivelSelecionado, Piloto pilotoSelecionado, boolean kers,
			boolean drs, boolean trocaPneus, boolean reabasteciemto) {

		final List tempList = new LinkedList();
		String temporarada = (String) ControleCampeonato.this.carregadorRecursos
				.getTemporadas().get(temporadaSelecionada);
		tempList.addAll((Collection) circuitosPilotos.get(temporarada));
		Map<String, String> pilotosEquipesCampeonato = new HashMap<String, String>();
		Map<String, Integer> equipesPotenciaCampeonato = new HashMap<String, Integer>();
		Map<String, Integer> pilotosHabilidadeCampeonato = new HashMap<String, Integer>();
		for (Iterator iterator = tempList.iterator(); iterator.hasNext();) {
			Piloto piloto = (Piloto) iterator.next();
			pilotosEquipesCampeonato.put(piloto.getNome(), piloto.getCarro()
					.getNome());
			equipesPotenciaCampeonato.put(piloto.getCarro().getNome(), piloto
					.getCarro().getPotenciaReal());
			pilotosHabilidadeCampeonato.put(piloto.getNome(),
					piloto.getHabilidade());
		}

		campeonato = new Campeonato();
		campeonato.setCorridas(cirucitosCampeonato);
		int diffGanho = 30 / cirucitosCampeonato.size();
		campeonato.setPtsGanho(diffGanho);
		campeonato.setNomePiloto(pilotoSelecionado.getNome());
		campeonato.setPtsPiloto(pilotoSelecionado.getHabilidade());
		campeonato.setPilotosEquipesCampeonato(pilotosEquipesCampeonato);
		campeonato.setEquipesPotenciaCampeonato(equipesPotenciaCampeonato);
		campeonato.setPilotosHabilidadeCampeonato(pilotosHabilidadeCampeonato);
		campeonato.setDrs(drs);
		campeonato.setKers(kers);
		campeonato.setSemReabasteciemnto(reabasteciemto);
		campeonato.setSemTrocaPneus(trocaPneus);
		campeonato.setTemporada(temporadaSelecionada);
		campeonato.setNivel(nivelSelecionado);
		campeonato.setQtdeVoltas(new Integer(numVoltasSelecionado));
		campeonato.setMenuLocal(true);
		return campeonato;
	}

	public void setCampeonato(Campeonato campeonato) {
		this.campeonato = campeonato;
	}

}
