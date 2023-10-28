package br.f1mane.paddock.applet;

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

import br.f1mane.paddock.entidades.TOs.DadosClassificacaoCarros;
import br.f1mane.paddock.entidades.TOs.DadosClassificacaoPilotos;
import br.f1mane.recursos.idiomas.Lang;

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
				DadosClassificacaoCarros d0 = (DadosClassificacaoCarros) arg0;
				DadosClassificacaoCarros d1 = (DadosClassificacaoCarros) arg1;
				return new Long(d1.getPontos()).compareTo(new Long(d0
						.getPontos()));
			}

		});
		Collections.sort(listaPilotos, new Comparator() {

			public int compare(Object arg0, Object arg1) {
				DadosClassificacaoPilotos d0 = (DadosClassificacaoPilotos) arg0;
				DadosClassificacaoPilotos d1 = (DadosClassificacaoPilotos) arg1;
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
			DadosClassificacaoCarros dadosConstrutoresCarros = (DadosClassificacaoCarros) listaCarros
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
			DadosClassificacaoPilotos dadosConstrutoresPilotos = (DadosClassificacaoPilotos) listaPilotos
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
