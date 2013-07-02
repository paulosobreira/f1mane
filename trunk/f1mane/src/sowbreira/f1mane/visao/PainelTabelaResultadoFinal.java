package sowbreira.f1mane.visao;

import java.awt.Dimension;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import sowbreira.f1mane.controles.ControleCorrida;
import sowbreira.f1mane.entidades.Piloto;
import sowbreira.f1mane.entidades.Volta;
import sowbreira.f1mane.recursos.idiomas.Lang;

/**
 * @author Paulo Sobreira Criado Em 17/05/2007 10:35:33
 */
public class PainelTabelaResultadoFinal extends JPanel {
	private static final long serialVersionUID = 1L;
	private JTable posicoesTable;
	private Piloto[] pilotos = new Piloto[24];

	public PainelTabelaResultadoFinal(List pilotosList, boolean modoApplet) {
		for (int i = 0; i < pilotosList.size(); i++) {
			Piloto piloto = (Piloto) pilotosList.get(i);
			pilotos[i] = piloto;
		}

		TableModel model = new TableModel();
		posicoesTable = new JTable(model);
		add(new JScrollPane(posicoesTable));
		posicoesTable
				.setPreferredScrollableViewportSize(new Dimension(800, 385));
	}

	public JTable getPosicoesTable() {
		return posicoesTable;
	}

	public Piloto[] getPilotos() {
		return pilotos;
	}

	private class TableModel extends AbstractTableModel {
		public int getColumnCount() {
			return 12;
		}

		public int getRowCount() {
			return 24;
		}

		public Object getValueAt(int rowIndex, int columnIndex) {
			Piloto p = pilotos[rowIndex];
			if (p == null) {
				return "";
			}

			switch (columnIndex) {
			case 0:
				return String.valueOf(p.getPosicao());

			case 1:
				return p.getNome();

			case 2:
				return p.getCarro().getNome();

			case 3:
				return Lang.msg(p.getCarro().getTipoPneu());

			case 4:
				return String.valueOf(p.getNumeroVolta());

			case 5:
				Volta volta = p.obterVoltaMaisRapida();
				if (volta == null) {
					return "";
				}
				return volta.obterTempoVoltaFormatado();

			case 6:
				return String.valueOf(p.getQtdeParadasBox());

			case 7:
				return String.valueOf(p.getCarro().porcentagemDesgastePeneus()
						+ "%");

			case 8:
				return String.valueOf(p.getCarro().porcentagemCombustivel()
						+ "%");
			case 9:
				return String.valueOf(p.getCarro().porcentagemDesgasteMotor()
						+ "%");
			case 10:
				return p.getNomeJogador();
			case 11:
				return ControleCorrida.calculaPontos25(p);

			default:
				return "";
			}
		}

		public String getColumnName(int column) {
			switch (column) {
			case 0:
				return Lang.msg("160");
			case 1:
				return Lang.msg("153");
			case 2:
				return Lang.msg("277");
			case 3:
				return Lang.msg("264");
			case 4:
				return Lang.msg("voltas");
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
				return Lang.msg("162");
			case 11:
				return Lang.msg("161");
			default:
				return "";
			}
		}
	}
}
