package sowbreira.f1mane.paddock.applet;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

import sowbreira.f1mane.controles.ControleJogoLocal;
import sowbreira.f1mane.entidades.ConstrutoresPontosCampeonato;
import sowbreira.f1mane.entidades.PilotosPontosCampeonato;
import sowbreira.f1mane.paddock.entidades.Comandos;
import sowbreira.f1mane.paddock.entidades.TOs.ClientPaddockPack;
import sowbreira.f1mane.paddock.entidades.persistencia.Campeonato;
import sowbreira.f1mane.paddock.entidades.persistencia.CorridaCampeonato;
import sowbreira.f1mane.paddock.entidades.persistencia.DadosCorridaCampeonato;
import sowbreira.f1mane.recursos.CarregadorRecursos;
import sowbreira.f1mane.recursos.idiomas.Lang;
import br.nnpe.Constantes;
import br.nnpe.Logger;
import br.nnpe.Util;

public class ControleCampeonatoCliente {

	private Component compPai;

	private Campeonato campeonato;

	private CarregadorRecursos carregadorRecursos;

	private String circuitoJogando;

	private String campeonatoSelecionado;

	private ControlePaddockCliente controlePaddockCliente;

	public ControleCampeonatoCliente(Component c,
			ControlePaddockCliente controlePaddockCliente) {
		carregarCircuitos();
		this.compPai = c;
		this.controlePaddockCliente = controlePaddockCliente;
		carregadorRecursos = new CarregadorRecursos(true);
		circuitosPilotos = carregadorRecursos.carregarTemporadasPilotos();
	}

	public Component getCompPai() {
		return compPai;
	}

	public Campeonato getCampeonato() {
		return campeonato;
	}

	protected Map circuitos = new HashMap();

	protected Map circuitosPilotos = new HashMap();

	private long tempoInicio;

	private List pilotosPontos;

	private ArrayList contrutoresPontos;

	private DefaultListModel defaultListModelCircuitos;

	private DefaultListModel defaultListModelCircuitosSelecionados;

	private JList listCircuitos;

	private JList listSelecionados;

	private JComboBox temporadas;

	private JSpinner spinnerQtdeVoltas;

	private JCheckBox semReabastacimento;

	private JCheckBox semTrocaPneu;

	private JCheckBox kers;

	private JCheckBox drs;

	private JTextField nomeCampeonato;

	private ArrayList jogadoresPontos;

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
		defaultListModelCircuitos = new DefaultListModel();
		defaultListModelCircuitosSelecionados = new DefaultListModel();
		for (Iterator iterator = circuitos.keySet().iterator(); iterator
				.hasNext();) {
			String key = (String) iterator.next();
			defaultListModelCircuitos.addElement(circuitos.get(key));
		}

		listCircuitos = new JList(defaultListModelCircuitos);

		listSelecionados = new JList(defaultListModelCircuitosSelecionados);
		JPanel panelCircuitos = new JPanel(new BorderLayout());
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
		cima.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int sel = listCircuitos.getSelectedIndex();
				if (!(sel == -1 || sel == 0)) {
					Object object = defaultListModelCircuitos.remove(sel);
					defaultListModelCircuitos.add(sel - 1, object);
					listCircuitos.setSelectedIndex(sel - 1);
				}
				sel = listSelecionados.getSelectedIndex();
				if (!(sel == -1 || sel == 0)) {
					Object object = defaultListModelCircuitosSelecionados
							.remove(sel);
					defaultListModelCircuitosSelecionados.add(sel - 1, object);
					listSelecionados.setSelectedIndex(sel - 1);
				}
			}
		});
		JButton baixo = new JButton("Baixo") {
			@Override
			public String getText() {
				return Lang.msg("288");
			}
		};
		baixo.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int sel = listCircuitos.getSelectedIndex();
				if (!(sel == -1 || sel >= defaultListModelCircuitos.getSize() - 1)) {
					Object object = (Object) defaultListModelCircuitos
							.remove(sel);
					defaultListModelCircuitos.add(sel + 1, object);
					listCircuitos.setSelectedIndex(sel + 1);
				}
				sel = listSelecionados.getSelectedIndex();
				if (!(sel == -1 || sel >= defaultListModelCircuitosSelecionados
						.getSize() - 1)) {
					Object object = (Object) defaultListModelCircuitosSelecionados
							.remove(sel);
					defaultListModelCircuitosSelecionados.add(sel + 1, object);
					listSelecionados.setSelectedIndex(sel + 1);
				}
			}
		});
		buttonsPanel.add(cima);
		buttonsPanel.add(baixo);

		panelCircuitos.add(buttonsPanel, BorderLayout.CENTER);
		panelCircuitos.add(new JScrollPane(listCircuitos) {
			@Override
			public Dimension getPreferredSize() {
				return new Dimension(150, 100);
			}
		}, BorderLayout.WEST);
		panelCircuitos.add(new JScrollPane(listSelecionados) {
			@Override
			public Dimension getPreferredSize() {
				return new Dimension(150, 100);
			}
		}, BorderLayout.EAST);

		JPanel grid = new JPanel();
		grid.setLayout(new GridLayout(8, 1, 2, 2));
		grid.add(new JLabel() {
			@Override
			public String getText() {
				return Lang.msg("nomeCampeonato");
			}
		});
		nomeCampeonato = new JTextField();
		grid.add(nomeCampeonato);
		grid.add(new JLabel() {
			@Override
			public String getText() {
				return Lang.msg("272");
			}
		});
		temporadas = new JComboBox(carregadorRecursos.getVectorTemps());
		grid.add(temporadas);

		grid.add(new JLabel() {
			public String getText() {
				return Lang.msg("110", new String[] {
						String.valueOf(Constantes.MIN_VOLTAS),
						String.valueOf(Constantes.MAX_VOLTAS) });
			}
		});
		spinnerQtdeVoltas = new JSpinner();
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

		JPanel panelTela = new JPanel(new BorderLayout());
		panelTela.add(panelCircuitos, BorderLayout.CENTER);
		panelTela.add(grid, BorderLayout.EAST);

		JOptionPane.showMessageDialog(compPai, panelTela, Lang.msg("276"),
				JOptionPane.INFORMATION_MESSAGE);

		List corridas = new ArrayList();
		for (int i = 0; i < defaultListModelCircuitosSelecionados.getSize(); i++) {
			corridas.add(defaultListModelCircuitosSelecionados.get(i));
		}

		if (corridas.isEmpty()) {
			JOptionPane.showMessageDialog(compPai, Lang.msg("296"), Lang
					.msg("296"), JOptionPane.ERROR_MESSAGE);
			return;
		}
		Integer qtdeVolta = (Integer) spinnerQtdeVoltas.getValue();
		if (qtdeVolta == null || qtdeVolta.intValue() < Constantes.MIN_VOLTAS) {
			JOptionPane.showMessageDialog(compPai, Lang.msg("110",
					new String[] { String.valueOf(Constantes.MIN_VOLTAS),
							String.valueOf(Constantes.MAX_VOLTAS) }), Lang.msg(
					"110", new String[] {
							String.valueOf(Constantes.MIN_VOLTAS),
							String.valueOf(Constantes.MAX_VOLTAS) }),
					JOptionPane.ERROR_MESSAGE);
			return;
		}
		campeonato = new Campeonato();
		campeonato.setTemporada((String) temporadas.getSelectedItem());
		campeonato.setNivel(Lang.key((String) comboBoxNivelCorrida
				.getSelectedItem()));
		campeonato.setSemReabasteciemnto(this.semReabastacimento.isSelected());
		campeonato.setSemTrocaPneus(this.semTrocaPneu.isSelected());
		campeonato.setKers(this.kers.isSelected());
		campeonato.setDrs(this.drs.isSelected());

		campeonato.setQtdeVoltas((Integer) spinnerQtdeVoltas.getValue());
		campeonato.setNome(nomeCampeonato.getText());
		for (int i = 0; i < corridas.size(); i++) {
			CorridaCampeonato corridaCampeonato = new CorridaCampeonato();
			corridaCampeonato.setCampeonato(campeonato);
			corridaCampeonato.setNomeCircuito((String) corridas.get(i));
			if (controlePaddockCliente != null) {
				corridaCampeonato.setLoginCriador(controlePaddockCliente
						.getSessaoCliente().getNomeJogador());
			}
			campeonato.getCorridaCampeonatos().add(corridaCampeonato);
		}
		if (controlePaddockCliente != null) {
			campeonato.setLoginCriador(controlePaddockCliente
					.getSessaoCliente().getNomeJogador());
			ClientPaddockPack clientPaddockPack = new ClientPaddockPack(
					Comandos.CRIAR_CAMPEONATO, controlePaddockCliente
							.getSessaoCliente());
			clientPaddockPack.setDataObject(campeonato);
			controlePaddockCliente.enviarObjeto(clientPaddockPack);
			clientPaddockPack = new ClientPaddockPack();
			clientPaddockPack.setComando(Comandos.OBTER_CAMPEONATO);
			clientPaddockPack.setDataObject(campeonato.getNome());
			Object ret = controlePaddockCliente.enviarObjeto(clientPaddockPack);
			if (controlePaddockCliente.retornoNaoValido(ret)) {
				return;
			}
			if (ret == null) {
				JOptionPane.showMessageDialog(this.compPai, Lang.msg("062"),
						"Erro", JOptionPane.ERROR_MESSAGE);
				return;
			}
			this.campeonato = (Campeonato) ret;
			new PainelCampeonato(this);
		}
	}

	public void iniciaCorrida(String circuito) {
		circuitoJogando = circuito;
		tempoInicio = System.currentTimeMillis();
	}

	public List getPilotosPontos() {
		return pilotosPontos;
	}

	public ArrayList getContrutoresPontos() {
		return contrutoresPontos;
	}

	public static void main(String[] args) throws Exception {
		ControleCampeonatoCliente controleCampeonato = new ControleCampeonatoCliente(
				null, null);
		controleCampeonato.criarCampeonato();
	}

	public void verCampeonato() {
		campeonatoSelecionado = null;
		ClientPaddockPack clientPaddockPack = new ClientPaddockPack();
		clientPaddockPack.setComando(Comandos.LISTAR_CAMPEONATOS);
		Object ret = controlePaddockCliente.enviarObjeto(clientPaddockPack);
		if (controlePaddockCliente.retornoNaoValido(ret)) {
			return;
		}
		if (ret == null) {
			JOptionPane.showMessageDialog(this.compPai, Lang.msg("062"),
					"Erro", JOptionPane.ERROR_MESSAGE);
			return;
		}
		JPanel campeonastosPanel = gerarPanelCampeonatos((List) ret);
		JOptionPane.showMessageDialog(this.compPai, campeonastosPanel, Lang
				.msg("listaCampeonatos"), JOptionPane.INFORMATION_MESSAGE);
		carregaCampeonatoSelecionado(this.compPai);
	}

	private JPanel gerarPanelCampeonatos(final List campeonatos) {
		final JTable corridasTable = new JTable();

		final TableModel corridasTableModel = new AbstractTableModel() {

			@Override
			public Object getValueAt(int rowIndex, int columnIndex) {
				Object[] value = (Object[]) campeonatos.get(rowIndex);

				switch (columnIndex) {
				case 0:
					return value[0];
				case 1:
					return value[1];
				case 2:
					boolean val = (Boolean) value[2];
					return val ? Lang.msg("sim") : Lang.msg("nao");
				case 3:
					return new SimpleDateFormat("dd/MM/yyyy").format(value[3]);
				default:
					return "";
				}
			}

			@Override
			public int getRowCount() {
				if (campeonatos == null) {
					return 0;
				}
				return campeonatos.size();
			}

			@Override
			public int getColumnCount() {
				return 4;
			}

			@Override
			public String getColumnName(int columnIndex) {

				switch (columnIndex) {
				case 0:
					return Lang.msg("nomeCampeonato");
				case 1:
					return Lang.msg("donoCampeonato");
				case 2:
					return Lang.msg("campeonatoConcluido");
				case 3:
					return Lang.msg("criadoEm");

				default:
					return "";
				}

			}
		};
		corridasTable.addMouseListener(new MouseAdapter() {

			public void mouseClicked(MouseEvent e) {
				super.mouseClicked(e);
				campeonatoSelecionado = (String) corridasTableModel.getValueAt(
						corridasTable.getSelectedRow(), 0);
				if (e.getClickCount() == 2) {
					carregaCampeonatoSelecionado(corridasTable);
				}
			}

		});

		corridasTable.setModel(corridasTableModel);
		JScrollPane jScrollPane = new JScrollPane(corridasTable) {
			@Override
			public Dimension getPreferredSize() {
				return new Dimension(620, 150);
			}
		};
		JPanel jPanel = new JPanel();
		jPanel.setBorder(new TitledBorder("Campeonato") {
			@Override
			public String getTitle() {
				return Lang.msg("cliqueDuploCarregarCampeonato");
			}
		});
		jPanel.add(jScrollPane);
		return jPanel;
	}

	protected void carregaCampeonatoSelecionado(Component comp) {
		if (Util.isNullOrEmpty(campeonatoSelecionado)) {
			return;
		}
		int ret = JOptionPane.showConfirmDialog(comp, Lang.msg(
				"carregarCampeonato", new String[] { campeonatoSelecionado }),
				Lang.msg("detelhesCampeonato"), JOptionPane.YES_NO_OPTION);
		if (ret == JOptionPane.YES_OPTION) {
			ClientPaddockPack clientPaddockPack = new ClientPaddockPack();
			clientPaddockPack.setComando(Comandos.OBTER_CAMPEONATO);
			clientPaddockPack.setDataObject(campeonatoSelecionado);
			Object retorno = controlePaddockCliente
					.enviarObjeto(clientPaddockPack);
			if (controlePaddockCliente.retornoNaoValido(ret)) {
				return;
			}
			if (retorno == null) {
				JOptionPane.showMessageDialog(
						ControleCampeonatoCliente.this.compPai,
						Lang.msg("062"), "Erro", JOptionPane.ERROR_MESSAGE);
				return;
			}
			ControleCampeonatoCliente.this.campeonato = (Campeonato) retorno;
			new PainelCampeonato(ControleCampeonatoCliente.this);

		}
	}

	public void proximaCorrida() {
		CorridaCampeonato corridaCampeonatoProx = null;
		for (CorridaCampeonato corridaCampeonato : campeonato
				.getCorridaCampeonatos()) {
			if (corridaCampeonato.getTempoInicio() == null) {
				corridaCampeonatoProx = corridaCampeonato;
				break;
			}

		}
		if (corridaCampeonatoProx == null) {
			Logger.logar("campeonato acabado");
		}
		controlePaddockCliente.criarJogo(campeonato, corridaCampeonatoProx
				.getNomeCircuito());
	}

	public void geraListaPilotosPontos() {
		pilotosPontos = new ArrayList();
		if (campeonato.getCorridaCampeonatos().isEmpty()) {
			return;
		}
		for (CorridaCampeonato corridaCampeonato : campeonato
				.getCorridaCampeonatos()) {

			List dadosCorridas = (List) corridaCampeonato
					.getDadosCorridaCampeonatos();
			if (dadosCorridas == null) {
				return;
			}
			for (Iterator iterator = dadosCorridas.iterator(); iterator
					.hasNext();) {
				DadosCorridaCampeonato dadosCorridaCampeonato = (DadosCorridaCampeonato) iterator
						.next();
				PilotosPontosCampeonato pilotosPontosCampeonato = new PilotosPontosCampeonato();
				pilotosPontosCampeonato.setNome(dadosCorridaCampeonato
						.getPiloto());
				if (pilotosPontos.contains(pilotosPontosCampeonato)) {
					continue;
				}
				pilotosPontos.add(pilotosPontosCampeonato);
			}
			for (Iterator iterator = pilotosPontos.iterator(); iterator
					.hasNext();) {
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
						return new Integer(p2.getPontos())
								.compareTo(new Integer(p1.getPontos()));
					} else {
						return new Integer(p2.getVitorias())
								.compareTo(new Integer(p1.getVitorias()));
					}
				}
			});
		}
	}

	private int calculaPontosConstrutores(String nome) {
		int pontos = 0;
		List corridas = campeonato.getCorridaCampeonatos();
		for (Iterator iterator = corridas.iterator(); iterator.hasNext();) {
			CorridaCampeonato corridaCampeonato = (CorridaCampeonato) iterator
					.next();
			List dadosCorridas = (List) corridaCampeonato
					.getDadosCorridaCampeonatos();
			if (dadosCorridas == null) {
				continue;
			}
			for (Iterator iterator2 = dadosCorridas.iterator(); iterator2
					.hasNext();) {
				DadosCorridaCampeonato dadosCorridaCampeonato = (DadosCorridaCampeonato) iterator2
						.next();
				if (nome.equals(dadosCorridaCampeonato.getCarro())) {
					pontos += dadosCorridaCampeonato.getPontos();
				}
			}
		}
		return pontos;
	}

	public Integer computaVitorias(String nome) {
		int vitorias = 0;
		List corridas = campeonato.getCorridaCampeonatos();
		for (Iterator iterator = corridas.iterator(); iterator.hasNext();) {
			CorridaCampeonato corridaCampeonato = (CorridaCampeonato) iterator
					.next();
			List dadosCorridas = (List) corridaCampeonato
					.getDadosCorridaCampeonatos();
			if (dadosCorridas == null) {
				continue;
			}
			for (Iterator iterator2 = dadosCorridas.iterator(); iterator2
					.hasNext();) {
				DadosCorridaCampeonato dadosCorridaCampeonato = (DadosCorridaCampeonato) iterator2
						.next();
				if (nome.equals(dadosCorridaCampeonato.getPiloto())
						&& dadosCorridaCampeonato.getPosicao() == 1) {
					vitorias += 1;
				}
			}
		}
		return vitorias;
	}

	public Integer computaVitoriasJogador(String nome) {
		int vitorias = 0;
		List corridas = campeonato.getCorridaCampeonatos();
		for (Iterator iterator = corridas.iterator(); iterator.hasNext();) {
			CorridaCampeonato corridaCampeonato = (CorridaCampeonato) iterator
					.next();
			List dadosCorridas = (List) corridaCampeonato
					.getDadosCorridaCampeonatos();
			if (dadosCorridas == null) {
				continue;
			}
			for (Iterator iterator2 = dadosCorridas.iterator(); iterator2
					.hasNext();) {
				DadosCorridaCampeonato dadosCorridaCampeonato = (DadosCorridaCampeonato) iterator2
						.next();
				if (nome.equals(dadosCorridaCampeonato.getJogador())
						&& dadosCorridaCampeonato.getPosicao() == 1) {
					vitorias += 1;
				}
			}
		}
		return vitorias;
	}

	public void geraListaContrutoresPontos() {
		contrutoresPontos = new ArrayList();
		if (campeonato.getCorridaCampeonatos().isEmpty()) {
			return;
		}
		for (CorridaCampeonato corridaCampeonato : campeonato
				.getCorridaCampeonatos()) {

			List dadosCorridas = (List) corridaCampeonato
					.getDadosCorridaCampeonatos();
			if (dadosCorridas == null) {
				return;
			}
			for (Iterator iterator = dadosCorridas.iterator(); iterator
					.hasNext();) {
				DadosCorridaCampeonato dadosCorridaCampeonato = (DadosCorridaCampeonato) iterator
						.next();
				ConstrutoresPontosCampeonato construtoresPontosCampeonato = new ConstrutoresPontosCampeonato();

				construtoresPontosCampeonato
						.setNomeEquipe(dadosCorridaCampeonato.getCarro());
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
	}

	private int calculaPontosPiloto(String nome) {
		int pontos = 0;
		List corridas = campeonato.getCorridaCampeonatos();
		for (Iterator iterator = corridas.iterator(); iterator.hasNext();) {
			CorridaCampeonato corridaCampeonato = (CorridaCampeonato) iterator
					.next();
			List dadosCorridas = (List) corridaCampeonato
					.getDadosCorridaCampeonatos();
			if (dadosCorridas == null) {
				continue;
			}
			for (Iterator iterator2 = dadosCorridas.iterator(); iterator2
					.hasNext();) {
				DadosCorridaCampeonato dadosCorridaCampeonato = (DadosCorridaCampeonato) iterator2
						.next();
				if (nome.equals(dadosCorridaCampeonato.getPiloto())) {
					pontos += dadosCorridaCampeonato.getPontos();
				}
			}
		}
		return pontos;
	}

	private int calculaPontosJogador(String nome) {
		int pontos = 0;
		List corridas = campeonato.getCorridaCampeonatos();
		for (Iterator iterator = corridas.iterator(); iterator.hasNext();) {
			CorridaCampeonato corridaCampeonato = (CorridaCampeonato) iterator
					.next();
			List dadosCorridas = (List) corridaCampeonato
					.getDadosCorridaCampeonatos();
			if (dadosCorridas == null) {
				continue;
			}
			for (Iterator iterator2 = dadosCorridas.iterator(); iterator2
					.hasNext();) {
				DadosCorridaCampeonato dadosCorridaCampeonato = (DadosCorridaCampeonato) iterator2
						.next();
				if (nome.equals(dadosCorridaCampeonato.getJogador())) {
					pontos += dadosCorridaCampeonato.getPontos();
				}
			}
		}
		return pontos;
	}

	public void geraListaJogadoresPontos() {
		jogadoresPontos = new ArrayList();
		if (campeonato.getCorridaCampeonatos().isEmpty()) {
			return;
		}
		for (CorridaCampeonato corridaCampeonato : campeonato
				.getCorridaCampeonatos()) {

			List dadosCorridas = (List) corridaCampeonato
					.getDadosCorridaCampeonatos();
			if (dadosCorridas == null) {
				return;
			}
			for (Iterator iterator = dadosCorridas.iterator(); iterator
					.hasNext();) {
				DadosCorridaCampeonato dadosCorridaCampeonato = (DadosCorridaCampeonato) iterator
						.next();
				if (Util.isNullOrEmpty(dadosCorridaCampeonato.getJogador())) {
					continue;
				}
				PilotosPontosCampeonato pilotosPontosCampeonato = new PilotosPontosCampeonato();
				pilotosPontosCampeonato.setNome(dadosCorridaCampeonato
						.getJogador());
				if (jogadoresPontos.contains(pilotosPontosCampeonato)) {
					continue;
				}
				jogadoresPontos.add(pilotosPontosCampeonato);
			}
			for (Iterator iterator = jogadoresPontos.iterator(); iterator
					.hasNext();) {
				PilotosPontosCampeonato pilotosPontosCampeonato = (PilotosPontosCampeonato) iterator
						.next();
				pilotosPontosCampeonato
						.setPontos(calculaPontosJogador(pilotosPontosCampeonato
								.getNome()));
				pilotosPontosCampeonato
						.setVitorias(computaVitoriasJogador(pilotosPontosCampeonato
								.getNome()));
			}
			Collections.sort(jogadoresPontos, new Comparator() {
				public int compare(Object o1, Object o2) {
					PilotosPontosCampeonato p1 = (PilotosPontosCampeonato) o1;
					PilotosPontosCampeonato p2 = (PilotosPontosCampeonato) o2;
					if (p1.getPontos() != p2.getPontos()) {
						return new Integer(p2.getPontos())
								.compareTo(new Integer(p1.getPontos()));
					} else {
						return new Integer(p2.getVitorias())
								.compareTo(new Integer(p1.getVitorias()));
					}
				}
			});
		}

	}

	public ArrayList getJogadoresPontos() {
		return jogadoresPontos;
	}

	public boolean verificaCampeonatoConcluido() {
		List<CorridaCampeonato> corridaCampeonatos = campeonato
				.getCorridaCampeonatos();
		for (CorridaCampeonato corridaCampeonato : corridaCampeonatos) {
			if (corridaCampeonato.getTempoFim() == null) {
				return false;
			}
		}
		return true;
	}

}
