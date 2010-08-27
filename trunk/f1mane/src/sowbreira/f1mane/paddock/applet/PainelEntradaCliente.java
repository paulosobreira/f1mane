package sowbreira.f1mane.paddock.applet;

import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import sowbreira.f1mane.MainFrame;
import sowbreira.f1mane.controles.ControleJogoLocal;
import sowbreira.f1mane.entidades.Carro;
import sowbreira.f1mane.entidades.Clima;
import sowbreira.f1mane.entidades.Piloto;
import sowbreira.f1mane.paddock.entidades.TOs.DadosCriarJogo;
import sowbreira.f1mane.paddock.entidades.persistencia.Campeonato;
import sowbreira.f1mane.recursos.idiomas.Lang;
import br.nnpe.Constantes;
import br.nnpe.Util;

/**
 * @author Paulo Sobreira Criado em 29/07/2007 as 17:41:24
 */
public class PainelEntradaCliente {
	private JSpinner spinnerQtdeVoltas;
	private JTextField nomeJogador;
	private JTextField nomeCampeonato;
	private JComboBox comboBoxPilotoSelecionado;
	private JComboBox comboBoxCircuito;
	private JComboBox comboBoxNivelCorrida;
	private JComboBox comboBoxClimaInicial;
	private JComboBox comboBoxPneuInicial;
	private JComboBox comboBoxAsa;
	private JSpinner spinnerCombustivelInicial;
	private JSlider spinnerDificuldadeUltrapassagem;
	private JSlider spinnerTempoCiclo;
	private JSpinner spinnerSkillPadraoPilotos;
	private JSpinner spinnerPotenciaPadraoCarros;
	protected JCheckBox semTrocaPneu;
	protected JCheckBox semReabastacimento;
	private List pilotos;
	private Map circuitos;
	private MainFrame mainFrame;
	private DadosCriarJogo dadosCriarJogo;
	private String nomeCriador;
	private Campeonato campeonato;

	public void setCampeonato(Campeonato campeonato) {
		this.campeonato = campeonato;
	}

	public PainelEntradaCliente(List pilotos, Map circuitos,
			MainFrame mainFrame, String nomeCriador) {
		this.pilotos = pilotos;
		this.circuitos = circuitos;
		this.mainFrame = mainFrame;
		this.nomeCriador = nomeCriador;
	}

	private void gerarPainelCriarJogo(JPanel painelInicio) {
		painelInicio.setLayout(new GridLayout(14, 2, 5, 5));
		JLabel label = new JLabel() {
			public String getText() {
				return Lang.msg("110");
			}
		};
		painelInicio.add(label);
		spinnerQtdeVoltas = new JSpinner();
		spinnerQtdeVoltas.setValue(new Integer(12));
		if (!ControleJogoLocal.VALENDO) {
			spinnerQtdeVoltas.setValue(new Integer(30));
		}

		painelInicio.add(spinnerQtdeVoltas);

		painelInicio.add(new JLabel() {

			public String getText() {
				return Lang.msg("111");
			}

		});
		nomeJogador = new JTextField(nomeCriador);
		nomeJogador.setEditable(false);
		painelInicio.add(nomeJogador);

		painelInicio.add(new JLabel("Campeonato:") {
			@Override
			public String getText() {
				return Lang.msg("nomeCampeonato");
			}
		});
		nomeCampeonato = new JTextField();
		nomeCampeonato.setEditable(false);
		painelInicio.add(nomeCampeonato);

		comboBoxPilotoSelecionado = new JComboBox();
		for (Iterator iter = pilotos.iterator(); iter.hasNext();) {
			Piloto piloto = (Piloto) iter.next();
			comboBoxPilotoSelecionado.addItem(piloto);
		}

		painelInicio.add(new JLabel("Selecionar Piloto :") {
			@Override
			public String getText() {
				return Lang.msg("120");
			}
		});
		painelInicio.add(comboBoxPilotoSelecionado);

		comboBoxCircuito = new JComboBox();
		List circuitosList = new ArrayList();
		for (Iterator iter = circuitos.keySet().iterator(); iter.hasNext();) {
			String key = (String) iter.next();
			circuitosList.add(key);
		}
		Collections.shuffle(circuitosList);
		for (Iterator iterator = circuitosList.iterator(); iterator.hasNext();) {
			String object = (String) iterator.next();
			comboBoxCircuito.addItem(object);
		}

		painelInicio.add(new JLabel("Selecionar Circuito :") {
			@Override
			public String getText() {
				return Lang.msg("121");
			}
		});
		painelInicio.add(comboBoxCircuito);

		comboBoxNivelCorrida = new JComboBox();
		comboBoxNivelCorrida.addItem(Lang.msg(ControleJogoLocal.NORMAL));
		comboBoxNivelCorrida.addItem(Lang.msg(ControleJogoLocal.FACIL));
		comboBoxNivelCorrida.addItem(Lang.msg(ControleJogoLocal.DIFICIL));
		painelInicio.add(new JLabel("Nivel da corrida :") {
			@Override
			public String getText() {
				return Lang.msg("212");
			}
		});
		painelInicio.add(comboBoxNivelCorrida);

		comboBoxClimaInicial = new JComboBox();
		comboBoxClimaInicial.addItem(new Clima(Clima.SOL));
		comboBoxClimaInicial.addItem(new Clima(Clima.NUBLADO));
		comboBoxClimaInicial.addItem(new Clima(Clima.CHUVA));

		painelInicio.add(new JLabel("Clima :") {
			@Override
			public String getText() {
				return Lang.msg("123");
			}
		});
		painelInicio.add(comboBoxClimaInicial);

		JLabel tipoPneu = new JLabel(Lang.msg("009"));
		comboBoxPneuInicial = new JComboBox();
		comboBoxPneuInicial.addItem(Lang.msg(Carro.TIPO_PNEU_MOLE));
		comboBoxPneuInicial.addItem(Lang.msg(Carro.TIPO_PNEU_DURO));
		comboBoxPneuInicial.addItem(Lang.msg(Carro.TIPO_PNEU_CHUVA));

		JLabel qtdeComustivel = new JLabel(Lang.msg("011")) {
			@Override
			public String getText() {
				return Lang.msg("011");
			}
		};
		spinnerCombustivelInicial = new JSpinner();
		spinnerCombustivelInicial.setValue(new Integer(100));

		JLabel tipoAsa = new JLabel(Lang.msg("010")) {
			@Override
			public String getText() {
				return Lang.msg("010");
			}
		};
		comboBoxAsa = new JComboBox();
		comboBoxAsa.addItem(Lang.msg(Carro.ASA_NORMAL));
		comboBoxAsa.addItem(Lang.msg(Carro.MAIS_ASA));
		comboBoxAsa.addItem(Lang.msg(Carro.MENOS_ASA));

		painelInicio.add(tipoPneu);
		painelInicio.add(comboBoxPneuInicial);
		painelInicio.add(tipoAsa);
		painelInicio.add(comboBoxAsa);
		painelInicio.add(qtdeComustivel);
		painelInicio.add(spinnerCombustivelInicial);

		painelInicio.add(new JLabel("Dificuldade Utrapassagem (0-999):") {
			@Override
			public String getText() {
				return Lang.msg("124");
			}
		});
		spinnerDificuldadeUltrapassagem = new JSlider(000, 500);
		spinnerDificuldadeUltrapassagem.setValue(new Integer(Util.intervalo(
				000, 500)));
		Hashtable labelTable = new Hashtable();
		labelTable.put(new Integer(000), new JLabel("") {
			@Override
			public String getText() {
				return Lang.msg("FACIL");
			}
		});
		labelTable.put(new Integer(500), new JLabel("") {
			@Override
			public String getText() {
				return Lang.msg("DIFICIL");
			}
		});
		spinnerDificuldadeUltrapassagem.setLabelTable(labelTable);
		spinnerDificuldadeUltrapassagem.setPaintLabels(true);
		painelInicio.add(spinnerDificuldadeUltrapassagem);

		painelInicio.add(new JLabel("Tempo Ciclo (50ms-150ms):") {
			@Override
			public String getText() {
				return Lang.msg("126");
			}
		});
		spinnerTempoCiclo = new JSlider(100, 200);
		spinnerTempoCiclo.setValue(new Integer(Util.intervalo(100, 200)));
		labelTable = new Hashtable();
		labelTable.put(new Integer(100), new JLabel("") {
			@Override
			public String getText() {
				return Lang.msg("RAPIDOS");
			}
		});
		labelTable.put(new Integer(200), new JLabel("") {
			@Override
			public String getText() {
				return Lang.msg("LENTOS");
			}
		});
		spinnerTempoCiclo.setLabelTable(labelTable);
		spinnerTempoCiclo.setPaintLabels(true);
		painelInicio.add(spinnerTempoCiclo);

		painelInicio.add(new JLabel() {
			@Override
			public String getText() {
				return Lang.msg("302");
			}
		});
		semReabastacimento = new JCheckBox();
		painelInicio.add(semReabastacimento);
		painelInicio.add(new JLabel() {
			@Override
			public String getText() {
				return Lang.msg("303");
			}
		});
		semTrocaPneu = new JCheckBox();

		painelInicio.add(semTrocaPneu);

		spinnerSkillPadraoPilotos = new JSpinner();
		spinnerSkillPadraoPilotos.setValue(new Integer(0));
		spinnerPotenciaPadraoCarros = new JSpinner();
		spinnerPotenciaPadraoCarros.setValue(new Integer(0));

		if (campeonato != null) {
			spinnerQtdeVoltas.setValue(campeonato.getQtdeVoltas());
			spinnerQtdeVoltas.setEnabled(false);
			comboBoxNivelCorrida.setSelectedItem(Lang
					.msg(campeonato.getNivel()));
			comboBoxNivelCorrida.setEnabled(false);
			semReabastacimento.setSelected(campeonato.isSemReabasteciemnto());
			semReabastacimento.setEnabled(false);
			semTrocaPneu.setSelected(campeonato.isSemTrocaPneus());
			semTrocaPneu.setEnabled(false);
			nomeCampeonato.setText(campeonato.getNome());
		}

	}

	public static void main(String[] args) {
		PainelEntradaCliente painelEntradaCliente = new PainelEntradaCliente(
				new ArrayList(), new Hashtable(), null, "teste");
		painelEntradaCliente.gerarDadosCriarJogo(new DadosCriarJogo());
	}

	public boolean gerarDadosCriarJogo(DadosCriarJogo dadosCriarJogo) {
		this.dadosCriarJogo = dadosCriarJogo;
		JPanel painelInicio = new JPanel();
		gerarPainelCriarJogo(painelInicio);

		int ret = JOptionPane.showConfirmDialog(mainFrame, painelInicio,
				"Setup Inicial", JOptionPane.YES_NO_OPTION);
		if (ret != JOptionPane.YES_OPTION) {
			return false;
		}
		while ((((Integer) spinnerQtdeVoltas.getValue()).intValue() == 10)
				|| (((Integer) spinnerCombustivelInicial.getValue()).intValue() == 0)) {
			JOptionPane.showMessageDialog(mainFrame, Lang.msg("128"), Lang
					.msg("128"), JOptionPane.INFORMATION_MESSAGE);
			ret = JOptionPane.showConfirmDialog(mainFrame, painelInicio,
					"Setup Inicial", JOptionPane.YES_NO_OPTION);
			spinnerQtdeVoltas.requestFocus();
			if (ret == JOptionPane.NO_OPTION) {
				return false;
			}
		}
		preecherDados();
		return true;
	}

	private void preecherDados() {
		Integer qtdeVoltas = (Integer) spinnerQtdeVoltas.getValue();
		if (qtdeVoltas.intValue() >= Constantes.MAX_VOLTAS) {
			qtdeVoltas = new Integer(Constantes.MAX_VOLTAS);
		}
		if (qtdeVoltas.intValue() <= Constantes.MIN_VOLTAS) {
			qtdeVoltas = new Integer(Constantes.MIN_VOLTAS);
		}
		dadosCriarJogo.setQtdeVoltas(qtdeVoltas);
		dadosCriarJogo
				.setDiffultrapassagem((Integer) spinnerDificuldadeUltrapassagem
						.getValue());
		Integer integerTempoCiclo = (Integer) spinnerTempoCiclo.getValue();
		if (integerTempoCiclo.intValue() <= Constantes.MIN_CICLO) {
			integerTempoCiclo = new Integer(Constantes.MIN_CICLO);
		}
		if (integerTempoCiclo.intValue() >= Constantes.MAX_CICLO) {
			integerTempoCiclo = new Integer(Constantes.MAX_CICLO);
		}
		dadosCriarJogo.setTempoCiclo(integerTempoCiclo);
		Integer habilidade = (Integer) spinnerSkillPadraoPilotos.getValue();
		if (habilidade.intValue() > 99) {
			habilidade = new Integer(99);
		}
		dadosCriarJogo.setHabilidade(habilidade);
		Integer potencia = (Integer) spinnerPotenciaPadraoCarros.getValue();
		if (potencia.intValue() > 999) {
			potencia = new Integer(999);
		}
		dadosCriarJogo.setPotencia(potencia);
		String circuitoSelecionado = (String) comboBoxCircuito
				.getSelectedItem();
		dadosCriarJogo.setCircuitoSelecionado(circuitoSelecionado);
		dadosCriarJogo.setNivelCorrida(Lang.key(comboBoxNivelCorrida
				.getSelectedItem().toString()));
		dadosCriarJogo.setClima((Clima) comboBoxClimaInicial.getSelectedItem());
		dadosCriarJogo.setSemReabastecimento(semReabastacimento.isSelected());
		dadosCriarJogo.setSemTrocaPeneu(semTrocaPneu.isSelected());
		preecherDadosCriarJogo(dadosCriarJogo);
	}

	private void preecherDadosCriarJogo(DadosCriarJogo dadosParticiparJogo) {
		String tpPnueu = Lang.key(comboBoxPneuInicial.getSelectedItem()
				.toString());
		Piloto piloto = (Piloto) comboBoxPilotoSelecionado.getSelectedItem();
		String asa = Lang.key((String) comboBoxAsa.getSelectedItem());
		Integer combustivel = (Integer) spinnerCombustivelInicial.getValue();
		if (combustivel.intValue() > 100) {
			combustivel = new Integer(100);
		}
		if (combustivel.intValue() < 10) {
			combustivel = new Integer(10);
		}
		dadosParticiparJogo.setSenha(nomeCampeonato.getText());
		dadosParticiparJogo.setTpPnueu(tpPnueu);
		dadosParticiparJogo.setAsa(asa);
		dadosParticiparJogo.setCombustivel(combustivel);
		dadosParticiparJogo.setPiloto(piloto.getNome());

	}

	public DadosCriarJogo getDadosCriarJogo() {
		return dadosCriarJogo;
	}

	public boolean gerarDadosEntrarJogo(DadosCriarJogo dadosParticiparJogo,
			JPanel panelJogoCriado) {
		JPanel painelInicio = new JPanel();
		gerarPainelParticiparJogo(painelInicio);
		JPanel panel = new JPanel(new GridLayout(2, 1));
		panel.add(panelJogoCriado);
		painelInicio.setBorder(new TitledBorder("Configuração do carro") {
			@Override
			public String getTitle() {
				return Lang.msg("233");
			}
		});
		panel.add(painelInicio);
		int ret = JOptionPane.showConfirmDialog(mainFrame, panel,
				"Setup Inicial", JOptionPane.YES_NO_OPTION);
		if (ret == JOptionPane.NO_OPTION) {
			return false;
		}
		preecherDadosCriarJogo(dadosParticiparJogo);
		return true;
	}

	private void gerarPainelParticiparJogo(JPanel painelInicio) {
		painelInicio.setLayout(new GridLayout(4, 2));

		comboBoxPilotoSelecionado = new JComboBox();
		for (Iterator iter = pilotos.iterator(); iter.hasNext();) {
			Piloto piloto = (Piloto) iter.next();
			comboBoxPilotoSelecionado.addItem(piloto);
		}

		painelInicio.add(new JLabel("Selecionar Piloto :") {
			@Override
			public String getText() {
				return Lang.msg("120");
			}
		});
		painelInicio.add(comboBoxPilotoSelecionado);

		comboBoxCircuito = new JComboBox();
		for (Iterator iter = circuitos.keySet().iterator(); iter.hasNext();) {
			String key = (String) iter.next();
			comboBoxCircuito.addItem(key);
		}

		JLabel tipoPneu = new JLabel(Lang.msg("009"));
		comboBoxPneuInicial = new JComboBox();
		comboBoxPneuInicial.addItem(Lang.msg(Carro.TIPO_PNEU_MOLE));
		comboBoxPneuInicial.addItem(Lang.msg(Carro.TIPO_PNEU_DURO));
		comboBoxPneuInicial.addItem(Lang.msg(Carro.TIPO_PNEU_CHUVA));
		JLabel tipoAsa = new JLabel(Lang.msg("010"));
		comboBoxAsa = new JComboBox();
		comboBoxAsa.addItem(Lang.msg(Carro.ASA_NORMAL));
		comboBoxAsa.addItem(Lang.msg(Carro.MAIS_ASA));
		comboBoxAsa.addItem(Lang.msg(Carro.MENOS_ASA));
		JLabel qtdeComustivel = new JLabel(Lang.msg("011"));
		spinnerCombustivelInicial = new JSpinner();
		spinnerCombustivelInicial.setValue(new Integer(100));

		painelInicio.add(tipoPneu);
		painelInicio.add(comboBoxPneuInicial);
		painelInicio.add(tipoAsa);
		painelInicio.add(comboBoxAsa);
		painelInicio.add(qtdeComustivel);
		painelInicio.add(spinnerCombustivelInicial);

	}
}
