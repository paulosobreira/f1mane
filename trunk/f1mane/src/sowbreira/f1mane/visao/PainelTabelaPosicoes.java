package sowbreira.f1mane.visao;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import sowbreira.f1mane.controles.InterfaceJogo;
import sowbreira.f1mane.entidades.Piloto;
import sowbreira.f1mane.recursos.idiomas.Lang;

/**
 * @author Paulo Sobreira Criado Em 15:27:33
 */
public class PainelTabelaPosicoes extends JPanel {
	private static final long serialVersionUID = 1L;
	private JTable posicoesTable;
	public InterfaceJogo interfaceJogo;
	private Piloto pilotoSelecionado;
	private Piloto[] pilotosId = new Piloto[24];
	private int larguraPainel = 130;
	private int alturaPainel = 390;
	public static final String mutex = "mutex";
	public static final String Coluna1 = "F1-MANE";
	public final static Color foraCorrida = new Color(250, 50, 50, 100);
	public final static Color jogador = new Color(60, 130, 255, 100);
	public final static Color otros = new Color(255, 188, 31, 100);

	private boolean isNullEmpt(String val) {
		return (val == null || "".equals(val));

	}

	public PainelTabelaPosicoes(final InterfaceJogo interfaceJogo) {
		this.interfaceJogo = interfaceJogo;
		List pilotosList = interfaceJogo.getPilotos();
		setaPilotos(pilotosList);

		TableModel model = new TableModel();
		posicoesTable = new JTable(model) {
			public Component prepareRenderer(TableCellRenderer renderer,
					int row, int col) {
				Component comp = super.prepareRenderer(renderer, row, col);
				JComponent jcomp = (JComponent) comp;
				Piloto piloto = obterPilotoSecionadoTabela(pilotoSelecionado);
				if (comp == jcomp) {
					if (piloto != null && piloto.isJogadorHumano()) {
						jcomp.setToolTipText(piloto.getNomeJogador());
					} else {
						jcomp.setToolTipText(Lang.msg("225"));
					}
				}
				return comp;
			}
		};
		TableColumn colorColumn1 = posicoesTable.getColumn(Coluna1);
		DefaultTableCellRenderer colorRenderer = new DefaultTableCellRenderer() {
			protected void setValue(Object value) {
				if (!(value instanceof String)) {
					return;
				}
				String val = (String) value;
				String[] splits = val.split("-");
				if (splits.length < 1) {
					return;
				}
				if (splits[0] == null || "".equals(splits[0])) {
					super.setValue("");
					return;
				}
				int pos = Integer.parseInt(splits[0]) - 1;
				if ((pos < 0 || pos > 23)) {
					return;
				}
				if (pilotosId[pos] != null && pilotosId[pos].isJogadorHumano()
						&& !pilotosId[pos].isDesqualificado()) {
					if (pilotosId[pos] != null
							&& pilotosId[pos].equals(interfaceJogo
									.getPilotoJogador())) {
						setBackground(jogador);
					} else {
						setBackground(otros);
					}

				} else if (pilotosId[pos] != null
						&& pilotosId[pos].isDesqualificado()) {
					setBackground(foraCorrida);
				} else {
					setBackground(Color.WHITE);
				}
				super.setValue(value);
			}
		};
		colorColumn1.setCellRenderer(colorRenderer);
		add(new JScrollPane(posicoesTable));
		posicoesTable.setPreferredScrollableViewportSize(new Dimension(
				larguraPainel, alturaPainel));
	}

	public void setaPilotos(List pilotosList) {
		for (int i = 0; i < pilotosList.size(); i++) {
			Piloto piloto = (Piloto) pilotosList.get(i);
			pilotosId[i] = piloto;
		}
	}

	public JTable getPosicoesTable() {
		return posicoesTable;
	}

	public void atulizaTabelaPosicoes(List pilotosList, Piloto pilotoSelec) {
		synchronized (mutex) {

			int col = posicoesTable.getSelectedColumn();
			int row = posicoesTable.getSelectedRow();

			for (int i = 0; i < pilotosList.size(); i++) {
				Piloto piloto = (Piloto) pilotosList.get(i);
				pilotosId[i] = piloto;
			}

			((TableModel) posicoesTable.getModel()).fireTableDataChanged();

			if (pilotoSelec != null && pilotoSelec.getNome() != null) {
				TableModel model = (TableModel) posicoesTable.getModel();

				for (int i = 0; i < model.getRowCount(); i++) {
					for (int j = 0; j < model.getColumnCount(); j++) {
						Object object = model.getValueAt(i, j);
						if (object == null || "".equals(object))
							continue;
						if (object instanceof String) {
							String nomePiloto = ((String) object).split("-")[1];

							if (pilotoSelec.getNome().equals(nomePiloto)) {
								col = j;
								row = i;

								break;
							}
						}
					}
				}
			}

			if ((col > -1) && (row > -1)
					&& (col < posicoesTable.getColumnCount())
					&& (row < posicoesTable.getRowCount())) {
				posicoesTable.setColumnSelectionInterval(col, col);
				posicoesTable.setRowSelectionInterval(row, row);
			}
		}
	}

	public Piloto obterPilotoSecionadoTabela(
			Piloto pilotoSelecionadoAnteriormente) {
		synchronized (mutex) {
			int col = posicoesTable.getSelectedColumn();
			int row = posicoesTable.getSelectedRow();
			Object object = null;

			if ((col < 0) || (row < 0)) {
				return pilotoSelecionadoAnteriormente;
			}

			object = ((TableModel) posicoesTable.getModel()).getValueAt(row,
					col);
			if (object == null || "".equals(object)) {
				return null;
			}

			if (!(object instanceof String)) {
				return pilotoSelecionadoAnteriormente;
			}

			String nomePiloto = (String) object;

			if ((nomePiloto == null) || "".equals(nomePiloto)) {
				return pilotoSelecionadoAnteriormente;
			}

			nomePiloto = nomePiloto.split("-")[1];

			for (int i = 0; i < pilotosId.length; i++) {
				if (pilotosId[i].getNome().equals(nomePiloto)) {
					return pilotosId[i];
				}
			}

			return pilotoSelecionadoAnteriormente;
		}
	}

	private class TableModel extends AbstractTableModel {

		public int getColumnCount() {
			return 1;
		}

		public int getRowCount() {
			return 24;
		}

		public Object getValueAt(int rowIndex, int columnIndex) {
			synchronized (mutex) {
				switch (columnIndex) {
				case 0:

					Piloto p = pilotosId[rowIndex];
					if (p == null) {
						return "";
					}

					String nome = "";
					if (p.isJogadorHumano()) {
						if (!isNullEmpt(p.getNomeJogador())) {
							nome = p.getNomeJogador();
						}
					}

					return (rowIndex + 1) + "-" + p.getNome() + "-"
							+ p.getNumeroVolta() + " " + nome;

				default:
					return "";
				}
			}
		}

		public String getColumnName(int column) {
			switch (column) {
			case 0:
				return Coluna1;

			default:
				return "";
			}
		}

	}

	public int getLarguraPainel() {
		return larguraPainel;
	}

	public void setLarguraPainel(int larguraPainel) {
		this.larguraPainel = larguraPainel;
	}

	public int getAlturaPainel() {
		return alturaPainel;
	}

	public void setAlturaPainel(int alturaPainel) {
		this.alturaPainel = alturaPainel;
	}
}
