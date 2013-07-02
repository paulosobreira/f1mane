package sowbreira.f1mane.paddock.applet;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableRowSorter;

import br.nnpe.Logger;

import sowbreira.f1mane.paddock.entidades.TOs.DadosConstrutoresCarros;
import sowbreira.f1mane.paddock.entidades.TOs.DadosConstrutoresPilotos;
import sowbreira.f1mane.paddock.entidades.TOs.DadosJogador;
import sowbreira.f1mane.paddock.entidades.persistencia.CorridasDadosSrv;
import sowbreira.f1mane.recursos.idiomas.Lang;

/**
 * @author Paulo Sobreira Criado em 21/10/2007 as 18:09:46
 */
public class FormClassificacao extends JPanel {
	private JTable posicoesTable;
	private List listaDadosJogador;
	private SimpleDateFormat dateFormat = new SimpleDateFormat(
			"EEE, d MMM yyyy HH:mm:ss");
	private DecimalFormat decimalFormat = new DecimalFormat("00");
	private DecimalFormat decimalFormatGeral = new DecimalFormat("0000");
	private DecimalFormat decimalFormatGeralBig = new DecimalFormat(
			"0000000000");
	private String nomeJogador;
	private Integer anoClassificacao;
	private JTable carrosTable;
	private JTable piltosTable;
	private List listaCarros;
	private List listaPilotos;
	private TableRowSorter sorter;

	/**
	 * @param listaDadosJogador
	 * @param cliente
	 */
	public FormClassificacao(List listaDadosJogador,
			final ControlePaddockCliente controlePaddockCliente,
			List listaCarros, List listaPilotos) {
		super();
		this.listaDadosJogador = listaDadosJogador;
		JPanel classificacao = new JPanel();
		TableModel model = new TableModel();
		posicoesTable = new JTable(model);
		sorter = new TableRowSorter(model);
		posicoesTable.setRowSorter(sorter);
		sorter.setComparator(1, new Comparator() {
			public int compare(Object o1, Object o2) {
				String d1 = (String) o1;
				String d2 = (String) o2;
				try {
					return dateFormat.parse(d1).compareTo(dateFormat.parse(d2));
				} catch (ParseException e) {
					Logger.logarExept(e);
					return 0;
				}
			}
		});
		posicoesTable.addMouseListener(new MouseAdapter() {

			public void mouseClicked(MouseEvent e) {
				super.mouseClicked(e);
				if (e.getClickCount() == 2) {
					TableModel model = (TableModel) posicoesTable.getModel();

					nomeJogador = (String) model.getValueAt(sorter
							.convertRowIndexToModel(posicoesTable
									.getSelectedRow()), 0);
					int ret = JOptionPane.showConfirmDialog(
							FormClassificacao.this, Lang.msg("129")
									+ nomeJogador, Lang.msg("130"),
							JOptionPane.YES_NO_OPTION);
					if (ret == JOptionPane.YES_OPTION) {
						gerarListagemCorrida(controlePaddockCliente
								.obterListaCorridas(nomeJogador,
										anoClassificacao));
					}
				}
			}

		});
		// posicoesTable.getColumn("Piloto").setMinWidth(100);
		classificacao.setLayout(new BorderLayout());
		classificacao.add(new JScrollPane(posicoesTable), BorderLayout.CENTER);
		posicoesTable
				.setPreferredScrollableViewportSize(new Dimension(600, 155));
		JLabel label = new JLabel(
				"Facil = Pts/2 , Normal = Pts Normal , Dificil = Pts * 2.") {
			@Override
			public String getText() {
				return Lang.msg("131");
			}
		};
		classificacao.add(label, BorderLayout.SOUTH);

		Collections.sort(listaCarros, new Comparator() {

			public int compare(Object arg0, Object arg1) {
				DadosConstrutoresCarros d0 = (DadosConstrutoresCarros) arg0;
				DadosConstrutoresCarros d1 = (DadosConstrutoresCarros) arg1;
				return new Long(d1.getPontos()).compareTo(new Long(d0
						.getPontos()));
			}

		});
		Collections.sort(listaPilotos, new Comparator() {

			public int compare(Object arg0, Object arg1) {
				DadosConstrutoresPilotos d0 = (DadosConstrutoresPilotos) arg0;
				DadosConstrutoresPilotos d1 = (DadosConstrutoresPilotos) arg1;
				return new Long(d1.getPontos()).compareTo(new Long(d0
						.getPontos()));
			}

		});
		this.listaCarros = listaCarros;
		this.listaPilotos = listaPilotos;

		TableModelCarros tableModelCarros = new TableModelCarros();
		carrosTable = new JTable(tableModelCarros);
		carrosTable.setAutoCreateRowSorter(true);
		carrosTable.setPreferredScrollableViewportSize(new Dimension(300, 155));
		TableModelPilotos tableModelPilotos = new TableModelPilotos();
		piltosTable = new JTable(tableModelPilotos);
		piltosTable.setAutoCreateRowSorter(true);
		piltosTable.setPreferredScrollableViewportSize(new Dimension(300, 155));
		JPanel construtores = new JPanel();
		construtores.setLayout(new GridLayout(1, 2));
		construtores.add(new JScrollPane(carrosTable));
		construtores.add(new JScrollPane(piltosTable));
		setLayout(new BorderLayout());
		add(classificacao, BorderLayout.CENTER);
		add(construtores, BorderLayout.SOUTH);

	}

	public Integer getAnoClassificacao() {
		return anoClassificacao;
	}

	public void setAnoClassificacao(Integer anoClassificacao) {
		this.anoClassificacao = anoClassificacao;
	}

	private void gerarListagemCorrida(List corridas) {
		TableModelCorridas tableModelCorridas = new TableModelCorridas(corridas);
		JTable corridasTable = new JTable(tableModelCorridas);
		corridasTable.setAutoCreateRowSorter(true);
		JPanel panel = new JPanel();
		panel.add(new JScrollPane(corridasTable));
		corridasTable
				.setPreferredScrollableViewportSize(new Dimension(900, 355));
		JOptionPane.showMessageDialog(this, panel, Lang.msg("132")
				+ nomeJogador, JOptionPane.PLAIN_MESSAGE);
	}

	private class TableModelCorridas extends AbstractTableModel {
		List corridas;

		/**
		 * @param corridas
		 */
		public TableModelCorridas(List corridas) {
			super();
			this.corridas = corridas;
		}

		public int getColumnCount() {
			return 12;
		}

		public int getRowCount() {
			return corridas.size();
		}

		public Object getValueAt(int rowIndex, int columnIndex) {
			CorridasDadosSrv corridasDadosSrv = (CorridasDadosSrv) corridas
					.get(rowIndex);

			switch (columnIndex) {
			case 0:
				return corridasDadosSrv.getCircuito();
			case 1:
				return corridasDadosSrv.getPiloto();
			case 2:
				return corridasDadosSrv.getCarro();
			case 3:
				return dateFormat.format(new Date(corridasDadosSrv
						.getTempoInicio()));
			case 4:
				return dateFormat.format(new Date(corridasDadosSrv
						.getTempoFim()));
			case 5:
				return corridasDadosSrv.getPorcentConcluida() + "%";
			case 6:
				return corridasDadosSrv.isMudouCarro() ? Lang.msg("SIM") : Lang
						.msg("NAO");
			case 7:
				return decimalFormat.format(corridasDadosSrv.getNumVoltas());
			case 8:
				return decimalFormat.format(corridasDadosSrv.getPosicao());
			case 9:
				return decimalFormat.format(corridasDadosSrv.getPontos());
			case 10:
				return Lang.msg(corridasDadosSrv.getNivel());
			case 11:
				return Lang.msg(corridasDadosSrv.getTemporada());
			default:
				return "";
			}
		}

		public String getColumnName(int column) {
			switch (column) {
			case 0:
				return Lang.msg("152");
			case 1:
				return Lang.msg("153");
			case 2:
				return Lang.msg("154");
			case 3:
				return Lang.msg("155");
			case 4:
				return Lang.msg("156");
			case 5:
				return Lang.msg("157");
			case 6:
				return Lang.msg("158");
			case 7:
				return Lang.msg("voltas");
			case 8:
				return Lang.msg("160");
			case 9:
				return Lang.msg("161");
			case 10:
				return Lang.msg("245");
			case 11:
				return Lang.msg("251");
			default:
				return "";
			}
		}
	}

	private class TableModel extends AbstractTableModel {
		public int getColumnCount() {
			return 6;
		}

		public int getRowCount() {
			return listaDadosJogador.size();
		}

		public Object getValueAt(int rowIndex, int columnIndex) {
			DadosJogador dadosJogador = (DadosJogador) listaDadosJogador
					.get(rowIndex);

			switch (columnIndex) {
			case 0:
				return dadosJogador.getNome();

			case 1:
				return dateFormat
						.format(new Date(dadosJogador.getUltimoAceso()));

			case 2:
				return decimalFormatGeral.format(dadosJogador.getPontos());
			case 3:
				return decimalFormatGeral.format(dadosJogador.getCorridas());
			case 4:
				long div = dadosJogador.getCorridas() > 0 ? dadosJogador
						.getCorridas() : 1;
				return decimalFormatGeral
						.format(dadosJogador.getPontos() / div);
			case 5:
				return decimalFormatGeralBig.format(dadosJogador.getPontos()
						* dadosJogador.getCorridas());

			default:
				return "";
			}
		}

		public String getColumnName(int column) {
			switch (column) {
			case 0:
				return Lang.msg("162");
			case 1:
				return Lang.msg("163");
			case 2:
				return Lang.msg("161");
			case 3:
				return Lang.msg("165");
			case 4:
				return Lang.msg("166");
			case 5:
				return Lang.msg("ranking");
			default:
				return "";
			}
		}
	}

	private class TableModelCarros extends AbstractTableModel {
		public int getColumnCount() {
			return 2;
		}

		public int getRowCount() {
			return listaCarros.size();
		}

		public Object getValueAt(int rowIndex, int columnIndex) {
			DadosConstrutoresCarros dadosConstrutoresCarros = (DadosConstrutoresCarros) listaCarros
					.get(rowIndex);

			switch (columnIndex) {
			case 0:
				return dadosConstrutoresCarros.getNome();
			case 1:
				return decimalFormatGeral.format(dadosConstrutoresCarros
						.getPontos());
			default:
				return "";
			}
		}

		public String getColumnName(int column) {
			switch (column) {
			case 0:
				return Lang.msg("154");
			case 1:
				return Lang.msg("161");
			default:
				return "";
			}
		}
	}

	private class TableModelPilotos extends AbstractTableModel {
		public int getColumnCount() {
			return 2;
		}

		public int getRowCount() {
			return listaPilotos.size();
		}

		public Object getValueAt(int rowIndex, int columnIndex) {
			DadosConstrutoresPilotos dadosConstrutoresPilotos = (DadosConstrutoresPilotos) listaPilotos
					.get(rowIndex);

			switch (columnIndex) {
			case 0:
				return dadosConstrutoresPilotos.getNome();
			case 1:
				return decimalFormatGeral.format(dadosConstrutoresPilotos
						.getPontos());
			default:
				return "";
			}
		}

		public String getColumnName(int column) {
			switch (column) {
			case 0:
				return Lang.msg("153");
			case 1:
				return Lang.msg("161");
			default:
				return "";
			}
		}
	}

}
