package sowbreira.f1mane.paddock.applet;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

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
	private String nomeJogador;

	/**
	 * @param listaDadosJogador
	 * @param cliente
	 */
	public FormClassificacao(List listaDadosJogador,
			final ControlePaddockCliente controlePaddockCliente) {
		super();
		this.listaDadosJogador = listaDadosJogador;
		TableModel model = new TableModel();
		posicoesTable = new JTable(model);
		posicoesTable.setAutoCreateRowSorter(true);
		posicoesTable.addMouseListener(new MouseAdapter() {

			public void mouseClicked(MouseEvent e) {
				super.mouseClicked(e);
				if (e.getClickCount() == 2) {
					TableModel model = (TableModel) posicoesTable.getModel();
					nomeJogador = (String) model.getValueAt(posicoesTable
							.getSelectedRow(), 0);
					int ret = JOptionPane.showConfirmDialog(
							FormClassificacao.this, Lang.msg("129")
									+ nomeJogador, Lang.msg("130"),
							JOptionPane.YES_NO_OPTION);
					if (ret == JOptionPane.YES_OPTION) {
						gerarListagemCorrida(controlePaddockCliente
								.obterListaCorridas(nomeJogador));
					}
				}
			}

		});
		// posicoesTable.getColumn("Piloto").setMinWidth(100);
		setLayout(new BorderLayout());
		add(new JScrollPane(posicoesTable), BorderLayout.CENTER);
		posicoesTable
				.setPreferredScrollableViewportSize(new Dimension(600, 355));
		JLabel label = new JLabel(
				"Corridas no modo facil e com menos 10 voltas não geram pontuação.") {
			@Override
			public String getText() {
				// TODO Auto-generated method stub
				return Lang.msg("131");
			}
		};
		add(label, BorderLayout.SOUTH);
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
			return 10;
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
				return Lang.msg("159");
			case 8:
				return Lang.msg("160");
			case 9:
				return Lang.msg("161");
			default:
				return "";
			}
		}
	}

	private class TableModel extends AbstractTableModel {
		public int getColumnCount() {
			return 5;
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

			default:
				return "";
			}
		}
	}

}
