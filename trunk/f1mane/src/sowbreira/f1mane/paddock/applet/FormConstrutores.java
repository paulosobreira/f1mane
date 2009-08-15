package sowbreira.f1mane.paddock.applet;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import sowbreira.f1mane.paddock.entidades.TOs.DadosConstrutoresCarros;
import sowbreira.f1mane.paddock.entidades.TOs.DadosConstrutoresPilotos;
import sowbreira.f1mane.recursos.idiomas.Lang;

/**
 * @author Paulo Sobreira Criado em 21/06/2009 as 18:39:46
 */
public class FormConstrutores extends JPanel {
	private JTable carrosTable;
	private JTable piltosTable;
	private List listaCarros;
	private List listaPilotos;
	private DecimalFormat decimalFormatGeral = new DecimalFormat("00000");

	public static void main(String[] args) {
		FormConstrutores formContrutores = new FormConstrutores(
				new ArrayList(), new ArrayList());
		JFrame frame = new JFrame();
		frame.getContentPane().add(formContrutores);
		frame.setVisible(true);
	}

	public FormConstrutores(List listaCarros, List listaPilotos) {
		super();
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
		carrosTable.setPreferredScrollableViewportSize(new Dimension(300, 355));
		TableModelPilotos tableModelPilotos = new TableModelPilotos();
		piltosTable = new JTable(tableModelPilotos);
		piltosTable.setAutoCreateRowSorter(true);
		piltosTable.setPreferredScrollableViewportSize(new Dimension(300, 355));
		setLayout(new GridLayout(1, 2));
		add(new JScrollPane(carrosTable));
		add(new JScrollPane(piltosTable));
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
