package sowbreira.f1mane.visao;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.TitledBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

import sowbreira.f1mane.MainFrame;
import sowbreira.f1mane.controles.ControleCampeonato;
import sowbreira.f1mane.entidades.Campeonato;
import sowbreira.f1mane.entidades.ConstrutoresPontosCampeonato;
import sowbreira.f1mane.entidades.CorridaCampeonato;
import sowbreira.f1mane.entidades.PilotosPontosCampeonato;
import sowbreira.f1mane.recursos.idiomas.Lang;
import br.nnpe.Util;

public class PainelCampeonato extends JPanel {

	private Campeonato campeonato;
	private SimpleDateFormat dateFormat = new SimpleDateFormat(
			"EEE, d MMM yyyy HH:mm:ss");
	private MainFrame mainFrame;

	private ControleCampeonato controleCampeonato;

	private JTable corridasTable;

	private TableModel corridasTableModel;
	private JTable pilotosTable;
	private AbstractTableModel pilotosTableModel;
	private JTable contrutoresTable;
	private AbstractTableModel contrutoresTableModel;
	private JTable desafiarTable;
	private AbstractTableModel desafiarTableModel;
	private JLabel jLabelDesafiando;

	public PainelCampeonato(ControleCampeonato controleCampeonato,
			MainFrame mainFrame) {
		super();
		this.controleCampeonato = controleCampeonato;
		this.campeonato = controleCampeonato.getCampeonato();
		controleCampeonato.geraListaPilotosPontos();
		controleCampeonato.geraListaContrutoresPontos();
		this.mainFrame = mainFrame;
		this.setLayout(new BorderLayout());

		JPanel dadosCampeonato = gerarPanelDadosCampeonato();
		JPanel listJogadoresSelecionados = gerarPanelPilotosSelecionados();
		JPanel criacao = new JPanel(new BorderLayout());
		criacao.add(dadosCampeonato, BorderLayout.WEST);

		if (!Util.isNullOrEmpty(campeonato.getNomePiloto())) {
			JPanel grid = new JPanel(new GridLayout(5, 2));
			grid.add(new JLabel() {
				@Override
				public String getText() {
					return Lang.msg("nomePiloto");
				}
			});
			grid.add(new JLabel(campeonato.getNomePiloto()));
			grid.add(new JLabel() {
				@Override
				public String getText() {
					return Lang.msg("carro");
				}
			});
			grid.add(new JLabel(campeonato.getPilotosEquipesCampeonato().get(
					campeonato.getNomePiloto())));

			grid.add(new JLabel() {
				@Override
				public String getText() {
					return Lang.msg("desafiando");
				}
			});
			jLabelDesafiando = new JLabel(campeonato.getRival()) {
				@Override
				public String getText() {
					return campeonato.getRival();
				}
			};
			grid.add(jLabelDesafiando);

			grid.add(new JLabel() {
				@Override
				public String getText() {
					return Lang.msg("vitorias");
				}
			});
			grid.add(new JLabel("" + campeonato.getVitorias()));

			grid.add(new JLabel() {
				@Override
				public String getText() {
					return Lang.msg("derrotas");
				}
			});
			grid.add(new JLabel("" + campeonato.getDerrotas()));

			JPanel campeonatoPiloto = new JPanel(new BorderLayout());
			campeonatoPiloto.setBorder(new TitledBorder("") {
				@Override
				public String getTitle() {
					return Lang.msg("campeonatoPiloto");
				}
			});
			campeonatoPiloto.add(grid, BorderLayout.CENTER);
			criacao.add(campeonatoPiloto, BorderLayout.CENTER);
		} else {
			criacao.add(listJogadoresSelecionados, BorderLayout.CENTER);
		}

		JPanel corridas = gerarPanelCorridas();

		JPanel ptsPilotos = gerarPanelPilotos();
		JPanel ptsConstrutores = gerarPanelConstrutores();
		JPanel grid = new JPanel(new GridLayout(1, 2));
		grid.add(ptsPilotos);
		grid.add(ptsConstrutores);

		JPanel panelBorder = new JPanel(new BorderLayout());
		panelBorder.add(criacao, BorderLayout.SOUTH);
		panelBorder.add(corridas, BorderLayout.NORTH);
		panelBorder.add(grid, BorderLayout.CENTER);
		this.add(panelBorder, BorderLayout.CENTER);
		JOptionPane.showMessageDialog(mainFrame, this, Lang.msg("286"),
				JOptionPane.INFORMATION_MESSAGE);
	}

	private void desefiar() {
		final ArrayList<Object[]> list = new ArrayList<Object[]>();
		Map<String, String> pilotosEquipesCampeonato = campeonato
				.getPilotosEquipesCampeonato();
		Map<String, Integer> pilotosHabilidadeCampeonato = campeonato
				.getPilotosHabilidadeCampeonato();
		for (Iterator iterator = pilotosEquipesCampeonato.keySet().iterator(); iterator
				.hasNext();) {
			String nmPiloto = (String) iterator.next();
			if (campeonato.getNomePiloto().equals(nmPiloto)) {
				continue;
			}
			String carro = pilotosEquipesCampeonato.get(nmPiloto);
			Integer habilidade = pilotosHabilidadeCampeonato.get(nmPiloto);
			list.add(new Object[] { nmPiloto, carro, habilidade });
		}
		Collections.sort(list, new Comparator() {
			@Override
			public int compare(Object o1, Object o2) {
				Object[] ob1 = (Object[]) o1;
				Object[] ob2 = (Object[]) o2;
				Integer i1 = (Integer) ob1[2];
				Integer i2 = (Integer) ob2[2];
				return i2.compareTo(i1);
			}
		});

		desafiarTable = new JTable();
		desafiarTableModel = new AbstractTableModel() {

			@Override
			public Object getValueAt(int rowIndex, int columnIndex) {
				if (rowIndex < 0) {
					return null;
				}
				Object[] objects = list.get(rowIndex);
				return objects[columnIndex];
			}

			@Override
			public int getRowCount() {
				return list.size();
			}

			@Override
			public int getColumnCount() {
				return 3;
			}

			@Override
			public String getColumnName(int columnIndex) {
				switch (columnIndex) {
				case 0:
					return Lang.msg("nomePiloto");
				case 1:
					return Lang.msg("carro");
				case 2:
					return Lang.msg("habilidade");
				default:
					return "";
				}

			}
		};
		desafiarTable.setModel(desafiarTableModel);
		JScrollPane jScrollPane = new JScrollPane(desafiarTable) {
			@Override
			public Dimension getPreferredSize() {
				return new Dimension(300, 400);
			}
		};
		JPanel jPanel = new JPanel();
		jPanel.setBorder(new TitledBorder("desafiar") {
			@Override
			public String getTitle() {
				return Lang.msg("desafiar");
			}
		});
		jPanel.add(jScrollPane);
		int ret = JOptionPane.showConfirmDialog(this, jPanel,
				Lang.msg("desafiar", new String[] { "fulano" }),
				JOptionPane.YES_NO_OPTION);
		if (ret == JOptionPane.YES_OPTION) {
			String desafiar = (String) desafiarTableModel.getValueAt(
					desafiarTable.getSelectedRow(), 0);
			if (Util.isNullOrEmpty(desafiar)) {
				JOptionPane.showMessageDialog(
						this,
						Lang.msg("nenhumDesafio",
								new String[] { campeonato.getRival() }),
						"Erro", JOptionPane.INFORMATION_MESSAGE);
				return;
			}
			if (campeonato.getVitorias() != 0 || campeonato.getDerrotas() != 0) {
				JOptionPane.showMessageDialog(
						this,
						Lang.msg("jaDesafiando",
								new String[] { campeonato.getRival() }),
						"Erro", JOptionPane.ERROR_MESSAGE);
				return;
			}
			campeonato.setRival(desafiar);
			jLabelDesafiando.updateUI();
		}

	}

	private JPanel gerarPanelConstrutores() {

		contrutoresTable = new JTable();
		contrutoresTableModel = new AbstractTableModel() {

			@Override
			public Object getValueAt(int rowIndex, int columnIndex) {
				if (controleCampeonato.getContrutoresPontos() == null
						|| controleCampeonato.getContrutoresPontos().isEmpty()) {
					return "";
				}
				ConstrutoresPontosCampeonato construtoresPontosCampeonato = (ConstrutoresPontosCampeonato) controleCampeonato
						.getContrutoresPontos().get(rowIndex);
				switch (columnIndex) {
				case 0:
					return construtoresPontosCampeonato.getNomeEquipe();
				case 1:
					return new Integer(construtoresPontosCampeonato.getPontos());
				default:
					return "";
				}

			}

			@Override
			public int getRowCount() {
				if (controleCampeonato.getContrutoresPontos() == null
						|| controleCampeonato.getContrutoresPontos().isEmpty()) {
					return 0;
				}
				return controleCampeonato.getContrutoresPontos().size();
			}

			@Override
			public int getColumnCount() {
				return 2;
			}

			@Override
			public String getColumnName(int columnIndex) {

				switch (columnIndex) {
				case 0:
					/* "Equipe" */
					return Lang.msg("277");
				case 1:
					/* "Pontos" */
					return Lang.msg("161");
				default:
					return "";
				}

			}
		};
		contrutoresTable.setModel(contrutoresTableModel);
		JScrollPane jScrollPane = new JScrollPane(contrutoresTable) {
			@Override
			public Dimension getPreferredSize() {
				return new Dimension(300, 150);
			}
		};
		JPanel jPanel = new JPanel();
		jPanel.setBorder(new TitledBorder("Construtores ") {
			@Override
			public String getTitle() {
				return Lang.msg("222");
			}
		});
		jPanel.add(jScrollPane);
		return jPanel;
	}

	private JPanel gerarPanelPilotos() {

		pilotosTable = new JTable() {
		};
		pilotosTableModel = new AbstractTableModel() {

			@Override
			public Object getValueAt(int rowIndex, int columnIndex) {
				if (controleCampeonato.getPilotosPontos() == null
						|| controleCampeonato.getPilotosPontos().isEmpty()) {
					return "";
				}
				PilotosPontosCampeonato pilotosPontosCampeonato = (PilotosPontosCampeonato) controleCampeonato
						.getPilotosPontos().get(rowIndex);
				switch (columnIndex) {
				case 0:
					return pilotosPontosCampeonato.getNome();
				case 1:
					return new Integer(pilotosPontosCampeonato.getPontos());
				case 2:
					return new Integer(pilotosPontosCampeonato.getVitorias());
				default:
					return "";
				}

			}

			@Override
			public int getRowCount() {
				if (controleCampeonato.getPilotosPontos() == null
						|| controleCampeonato.getPilotosPontos().isEmpty()) {
					return 0;
				}

				return controleCampeonato.getPilotosPontos().size();
			}

			@Override
			public int getColumnCount() {
				return 3;
			}

			@Override
			public String getColumnName(int columnIndex) {

				switch (columnIndex) {
				case 0:
					/* Piloto */
					return Lang.msg("153");
				case 1:
					/* "Pontos" */
					return Lang.msg("161");
				case 2:
					/* "Vitorias" */
					return Lang.msg("289");
				default:
					return "";
				}

			}
		};
		pilotosTable.setModel(pilotosTableModel);
		JScrollPane jScrollPane = new JScrollPane(pilotosTable) {
			@Override
			public Dimension getPreferredSize() {
				return new Dimension(300, 150);
			}
		};
		JPanel jPanel = new JPanel();
		jPanel.setBorder(new TitledBorder("Pilotos ") {
			@Override
			public String getTitle() {
				return Lang.msg("294");
			}
		});
		jPanel.add(jScrollPane);
		return jPanel;
	}

	private JPanel gerarPanelCorridas() {
		corridasTable = new JTable() {
		};
		corridasTable.addMouseListener(new MouseAdapter() {

			public void mouseClicked(MouseEvent e) {
				super.mouseClicked(e);
				if (e.getClickCount() == 2) {
					String corrida = (String) corridasTableModel.getValueAt(
							corridasTable.getSelectedRow(), 0);
					int ret = JOptionPane.showConfirmDialog(corridasTable,
							Lang.msg("300", new String[] { corrida }),
							Lang.msg("299"), JOptionPane.YES_NO_OPTION);
					if (ret == JOptionPane.YES_OPTION) {
						gerarPainelDetalhesCorrida(corrida, corridasTable);
					}
				}
			}

		});

		corridasTableModel = new AbstractTableModel() {

			@Override
			public Object getValueAt(int rowIndex, int columnIndex) {
				String circuito = (String) controleCampeonato.getCampeonato()
						.getCorridas().get(rowIndex);
				List dadosCorridas = (List) controleCampeonato.getCampeonato()
						.getDadosCorridas().get(circuito);
				CorridaCampeonato corridaCampeonato = null;
				if (dadosCorridas != null) {
					for (Iterator iterator = dadosCorridas.iterator(); iterator
							.hasNext();) {
						CorridaCampeonato ccTemp = (CorridaCampeonato) iterator
								.next();
						if (ccTemp.getPosicao() == 1) {
							corridaCampeonato = ccTemp;
						}
					}
				}

				switch (columnIndex) {
				case 0:
					return circuito;
				case 1:
					if (corridaCampeonato == null) {
						return "";
					}
					return dateFormat.format(new Date(corridaCampeonato
							.getTempoInicio()));
				case 2:
					if (corridaCampeonato == null) {
						return "";
					}
					return dateFormat.format(new Date(corridaCampeonato
							.getTempoFim()));
				case 3:
					if (corridaCampeonato == null) {
						return "";
					}
					return corridaCampeonato.getPiloto();

				default:
					return "";
				}

			}

			@Override
			public int getRowCount() {
				return controleCampeonato.getCampeonato().getCorridas().size();
			}

			@Override
			public int getColumnCount() {
				return 4;
			}

			@Override
			public String getColumnName(int columnIndex) {

				switch (columnIndex) {
				case 0:
					/* Corrida */
					return Lang.msg("corrida");
				case 1:
					/* Inicio */
					return Lang.msg("155");
				case 2:
					/* Fim */
					return Lang.msg("156");
				case 3:
					/* Vencedor */
					return Lang.msg("291");
				default:
					return "";
				}

			}
		};
		corridasTable.setModel(corridasTableModel);
		JScrollPane jScrollPane = new JScrollPane(corridasTable) {
			@Override
			public Dimension getPreferredSize() {
				return new Dimension(600, 150);
			}
		};
		JPanel jPanel = new JPanel();
		jPanel.setBorder(new TitledBorder("Campeonato") {
			@Override
			public String getTitle() {
				return Lang.msg("268");
			}
		});
		jPanel.add(jScrollPane);
		return jPanel;
	}

	protected void gerarPainelDetalhesCorrida(final String corrida, JTable table) {
		JTable detCorridaTable = new JTable();
		AbstractTableModel detCorridaTableModel = new AbstractTableModel() {

			@Override
			public Object getValueAt(int rowIndex, int columnIndex) {
				List dets = (List) campeonato.getDadosCorridas().get(corrida);
				if (dets == null) {
					return "";
				}
				CorridaCampeonato cc = (CorridaCampeonato) dets.get(rowIndex);

				switch (columnIndex) {
				case 0:
					return cc.getPosicao();
				case 1:
					return cc.getPiloto();
				case 2:
					return cc.getCarro();
				case 3:
					return Lang.msg(cc.getTpPneu());
				case 4:
					return cc.getNumVoltas();
				case 5:
					return cc.getVoltaMaisRapida();
				case 6:
					return cc.getQtdeParadasBox();
				case 7:
					return cc.getDesgastePneus();
				case 8:
					return cc.getCombustivelRestante();
				case 9:
					return cc.getDesgasteMotor();
				case 10:
					return cc.getPontos();
				default:
					return "";

				}

			}

			@Override
			public int getRowCount() {
				List dets = (List) campeonato.getDadosCorridas().get(corrida);
				if (dets == null)
					return 0;
				return dets.size();
			}

			@Override
			public int getColumnCount() {
				return 11;
			}

			public String getColumnName(int column) {
				switch (column) {
				// POsição
				case 0:
					return Lang.msg("160");
					// Piloto
				case 1:
					return Lang.msg("153");
					// Equipe
				case 2:
					return Lang.msg("277");
					// Tp Pneu
				case 3:
					return Lang.msg("264");
					// Voltas
				case 4:
					return Lang.msg("voltas");
					// MElhor
				case 5:
					return Lang.msg("278");
				case 6:
					return Lang.msg("146");
				case 7:
					return Lang.msg("216");
				case 8:
					return Lang.msg("215");
				case 9:
					return Lang.msg("217");
				case 10:
					return Lang.msg("161");
				default:
					return "";
				}
			}
		};
		detCorridaTable.setModel(detCorridaTableModel);
		JScrollPane jScrollPane = new JScrollPane(detCorridaTable) {
			@Override
			public Dimension getPreferredSize() {
				return new Dimension(650, 350);
			}
		};
		JPanel jPanel = new JPanel();
		jPanel.setBorder(new TitledBorder("Detalhes da Corrida") {
			@Override
			public String getTitle() {
				return Lang.msg("299");
			}
		});
		jPanel.add(jScrollPane);
		JOptionPane.showMessageDialog(corridasTable, jPanel,
				Lang.msg("300", new String[] { corrida }),
				JOptionPane.INFORMATION_MESSAGE);
	}

	private JPanel gerarPanelPilotosSelecionados() {
		DefaultListModel jogListModel = new DefaultListModel();
		for (Iterator iterator = campeonato.getPilotos().iterator(); iterator
				.hasNext();) {
			String jogador = (String) iterator.next();
			jogListModel.addElement(jogador);

		}
		// if (!Util.isNullOrEmpty(campeonato.getNomePiloto())) {
		// jogListModel.addElement(campeonato.getNomePiloto());
		// }
		JList jogadores = new JList(jogListModel);
		jogadores.setEnabled(false);
		jogadores.setLayoutOrientation(JList.HORIZONTAL_WRAP);
		JScrollPane jogPane = new JScrollPane(jogadores) {
			@Override
			public Dimension getPreferredSize() {
				return new Dimension(360, 140);
			}
		};
		jogPane.setAlignmentX(LEFT_ALIGNMENT);
		jogPane.setBorder(new TitledBorder("Campeonato") {
			@Override
			public String getTitle() {
				return Lang.msg("295");
			}
		});
		JPanel p2 = new JPanel();
		p2.add(jogPane, BorderLayout.CENTER);
		return p2;
	}

	private JPanel gerarPanelDadosCampeonato() {
		JPanel p1 = new JPanel();
		p1.setLayout(new GridLayout(7, 2));
		p1.setBorder(new TitledBorder("") {
			@Override
			public String getTitle() {
				return Lang.msg("dadosCampeonato");
			}
		});

		p1.add(new JLabel() {
			@Override
			public String getText() {
				return Lang.msg("284");
			}
		});
		p1.add(new JLabel(campeonato.getTemporada()));

		p1.add(new JLabel() {
			public String getText() {
				return Lang.msg("191");
			}
		});
		p1.add(new JLabel(campeonato.getNivel()));

		p1.add(new JLabel() {

			public String getText() {
				return Lang.msg("285");
			}
		});
		p1.add(new JLabel(campeonato.getQtdeVoltas().toString()));

		p1.add(new JLabel() {
			@Override
			public String getText() {
				return Lang.msg("302");
			}
		});
		p1.add(new JLabel() {
			@Override
			public String getText() {
				return campeonato.isSemReabasteciemnto() ? Lang.msg("SIM")
						: Lang.msg("NAO");
			}
		});

		p1.add(new JLabel() {
			@Override
			public String getText() {
				return Lang.msg("303");
			}
		});
		p1.add(new JLabel() {
			@Override
			public String getText() {
				return campeonato.isSemTrocaPneus() ? Lang.msg("SIM") : Lang
						.msg("NAO");
			}
		});

		p1.add(new JLabel() {
			@Override
			public String getText() {
				return Lang.msg("kers");
			}
		});
		p1.add(new JLabel() {
			@Override
			public String getText() {
				return campeonato.isKers() ? Lang.msg("SIM") : Lang.msg("NAO");
			}
		});

		p1.add(new JLabel() {
			@Override
			public String getText() {
				return Lang.msg("drs");
			}
		});
		p1.add(new JLabel() {
			@Override
			public String getText() {
				return campeonato.isDrs() ? Lang.msg("SIM") : Lang.msg("NAO");
			}
		});

		return p1;
	}
}
