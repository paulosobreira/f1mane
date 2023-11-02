package br.f1mane.paddock.applet;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.image.BufferedImage;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import br.nnpe.Constantes;
import br.nnpe.Logger;
import br.f1mane.MainFrame;
import br.f1mane.controles.ControleJogoLocal;
import br.f1mane.controles.ControleRecursos;
import sowbreira.f1mane.entidades.Carro;
import sowbreira.f1mane.entidades.Circuito;
import sowbreira.f1mane.entidades.Clima;
import sowbreira.f1mane.entidades.No;
import sowbreira.f1mane.entidades.Piloto;
import br.f1mane.paddock.entidades.TOs.DadosCriarJogo;
import br.f1mane.paddock.entidades.persistencia.CampeonatoSrv;
import br.f1mane.recursos.CarregadorRecursos;
import br.f1mane.recursos.idiomas.Lang;

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
	private JComboBox comboTemporada;
	private JSlider sliderCombustivelInicial;
	private JSlider sliderDificuldadeUltrapassagem;
	private JSpinner spinnerSkillPadraoPilotos;
	private JSpinner spinnerPotenciaPadraoCarros;
	protected JCheckBox trocaPneu;
	protected JCheckBox reabastecimento;
	protected JCheckBox kers;
	protected JCheckBox drs;
	private List pilotos;
	private final Map circuitos;
	private final MainFrame mainFrame;
	private DadosCriarJogo dadosCriarJogo;
	private final String nomeCriador;
	private CampeonatoSrv campeonato;

	public void setCampeonato(CampeonatoSrv campeonato) {
		this.campeonato = campeonato;
	}

	public PainelEntradaCliente(MainFrame mainFrame, String nomeCriador) {
		this.circuitos = ControleRecursos.carregarCircuitos();
		this.mainFrame = mainFrame;
		this.nomeCriador = nomeCriador;
		carregaComboTemporadas();
	}

	public PainelEntradaCliente(List pilotos, Map circuitos, MainFrame mainFrame, String nomeCriador) {
		this.pilotos = pilotos;
		this.circuitos = circuitos;
		this.mainFrame = mainFrame;
		this.nomeCriador = nomeCriador;
		carregaComboTemporadas();
	}

	private void carregaComboTemporadas() {
		CarregadorRecursos carregadorRecursos = CarregadorRecursos.getCarregadorRecursos(false);
		carregadorRecursos.carregarTemporadasPilotos();
		Vector<String> vectorTemps = carregadorRecursos.getVectorTemps();
		Collections.sort(vectorTemps, new Comparator<String>() {
			@Override
			public int compare(String o1, String o2) {
				return Integer.valueOf(o2).compareTo(Integer.valueOf(o1));
			}
		});
		comboTemporada = new JComboBox(vectorTemps);
	}

	private void gerarPainelCriarJogo(JPanel painelInicio) {
		painelInicio.setLayout(new GridLayout(5, 2, 5, 5));
		JLabel label = new JLabel() {
			public String getText() {
				return Lang.msg("110",
						new String[] { String.valueOf(Constantes.MIN_VOLTAS), String.valueOf(Constantes.MAX_VOLTAS) });
			}
		};

		spinnerQtdeVoltas = new JSpinner();
		spinnerQtdeVoltas.setValue(new Integer(12));
		JPanel pNome = new JPanel(new GridLayout(1, 2));
		pNome.add(new JLabel() {

			public String getText() {
				return Lang.msg("111");
			}

		});
		nomeJogador = new JTextField(nomeCriador);
		nomeJogador.setEditable(false);
		pNome.add(nomeJogador);
		painelInicio.add(pNome);

		JPanel pCamp = new JPanel(new GridLayout(1, 2));

		pCamp.add(new JLabel("Campeonato:") {
			@Override
			public String getText() {
				return Lang.msg("nomeCampeonato");
			}
		});
		nomeCampeonato = new JTextField();
		nomeCampeonato.setEditable(false);
		pCamp.add(nomeCampeonato);

		painelInicio.add(pCamp);

		comboBoxClimaInicial = new JComboBox();
		comboBoxClimaInicial.addItem(new Clima(Clima.SOL));
		comboBoxClimaInicial.addItem(new Clima(Clima.NUBLADO));
		comboBoxClimaInicial.addItem(new Clima(Clima.CHUVA));
		JPanel pCp = new JPanel(new GridLayout(1, 2));

		pCp.add(new JLabel("Clima :") {
			@Override
			public String getText() {
				return Lang.msg("123");
			}
		});
		pCp.add(comboBoxClimaInicial);

		painelInicio.add(pCp);

		comboBoxNivelCorrida = new JComboBox();
		comboBoxNivelCorrida.addItem(Lang.msg(ControleJogoLocal.NORMAL));
		comboBoxNivelCorrida.addItem(Lang.msg(ControleJogoLocal.FACIL));
		comboBoxNivelCorrida.addItem(Lang.msg(ControleJogoLocal.DIFICIL));

		JPanel pNv = new JPanel(new GridLayout(1, 2));

		pNv.add(new JLabel("Nivel da corrida :") {
			@Override
			public String getText() {
				return Lang.msg("212");
			}
		});
		pNv.add(comboBoxNivelCorrida);
		painelInicio.add(pNv);

		JPanel pVolt = new JPanel(new GridLayout(1, 2));

		pVolt.add(label);
		pVolt.add(spinnerQtdeVoltas);
		painelInicio.add(pVolt);

		JPanel jDiff = new JPanel();

		jDiff.add(new JLabel("Dificuldade Utrapassagem :") {
			@Override
			public String getText() {
				return Lang.msg("124");
			}
		});
		sliderDificuldadeUltrapassagem = new JSlider(000, 500);
		sliderDificuldadeUltrapassagem.setValue(new Integer(250));
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
		sliderDificuldadeUltrapassagem.setLabelTable(labelTable);
		sliderDificuldadeUltrapassagem.setPaintLabels(true);
		jDiff.add(sliderDificuldadeUltrapassagem);
		painelInicio.add(jDiff);
		JPanel pCiclo = new JPanel();

		JPanel p1 = new JPanel(new GridLayout(1, 2));

		p1.add(new JLabel() {
			@Override
			public String getText() {
				return Lang.msg("302");
			}
		});
		reabastecimento = new JCheckBox();
		p1.add(reabastecimento);
		painelInicio.add(p1);

		JPanel p2 = new JPanel(new GridLayout(1, 2));

		p2.add(new JLabel() {
			@Override
			public String getText() {
				return Lang.msg("303");
			}
		});
		trocaPneu = new JCheckBox();
		trocaPneu.setSelected(true);
		p2.add(trocaPneu);
		painelInicio.add(p2);

		JPanel p3 = new JPanel(new GridLayout(1, 2));
		p3.add(new JLabel() {
			@Override
			public String getText() {
				return Lang.msg("kers");
			}
		});
		kers = new JCheckBox();
		kers.setSelected(true);
		p3.add(kers);
		painelInicio.add(p3);

		JPanel p4 = new JPanel(new GridLayout(1, 2));
		p4.add(new JLabel() {
			@Override
			public String getText() {
				return Lang.msg("drs");
			}
		});
		drs = new JCheckBox();
		drs.setSelected(true);
		p4.add(drs);
		painelInicio.add(p4);

		spinnerSkillPadraoPilotos = new JSpinner();
		spinnerSkillPadraoPilotos.setValue(new Integer(0));
		spinnerPotenciaPadraoCarros = new JSpinner();
		spinnerPotenciaPadraoCarros.setValue(new Integer(0));

	}

	public static void main(String[] args) {
		PainelEntradaCliente painelEntradaCliente = new PainelEntradaCliente(null, "teste");
		painelEntradaCliente.gerarDadosCriarJogo(new DadosCriarJogo());
		// String t = "t2015";
		// System.out.println(t.substring(1, t.length()));
	}

	public boolean gerarDadosCriarJogo(DadosCriarJogo dadosCriarJogo) {
		this.dadosCriarJogo = dadosCriarJogo;
		JPanel painelInicio = new JPanel();
		gerarPainelCriarJogo(painelInicio);
		JPanel painelMostrar = new JPanel(new BorderLayout());
		painelMostrar.add(gerarSeletorTemporadaCircuito(), BorderLayout.NORTH);
		painelMostrar.add(painelInicio, BorderLayout.CENTER);
		setaCampeonato();

		int ret = JOptionPane.showConfirmDialog(mainFrame, painelMostrar, Lang.msg("127"), JOptionPane.YES_NO_OPTION);
		if (ret != JOptionPane.YES_OPTION) {
			return false;
		}

		Integer qtdeVoltas = (Integer) spinnerQtdeVoltas.getValue();
		if (qtdeVoltas < 12) {
			spinnerQtdeVoltas.setValue(new Integer(12));
		}
		preecherDadosCriarJogo();
		return true;
	}

	private void setaCampeonato() {
		if (campeonato != null) {
			spinnerQtdeVoltas.setValue(campeonato.getQtdeVoltas());
			spinnerQtdeVoltas.setEnabled(false);
			comboBoxNivelCorrida.setSelectedItem(Lang.msg(campeonato.getNivel()));
			comboBoxNivelCorrida.setEnabled(false);
			reabastecimento.setSelected(campeonato.isReabastecimento());
			reabastecimento.setEnabled(false);
			trocaPneu.setSelected(campeonato.isTrocaPneus());
			trocaPneu.setEnabled(false);
			nomeCampeonato.setText(campeonato.getNome());
			// comboBoxCircuito.setSelectedItem(campeonato.getCircuitoAtual());
			comboBoxCircuito.setEnabled(false);
			kers.setSelected(campeonato.isErs());
			kers.setEnabled(false);
			drs.setSelected(campeonato.isDrs());
			drs.setEnabled(false);
		}
	}

	public JComboBox getComboBoxCircuito() {
		return comboBoxCircuito;
	}

	public Component gerarSeletorTemporadaCircuito() {
		JPanel grid = new JPanel(new GridLayout(2, 2));
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

		grid.add(new JLabel() {
			public String getText() {
				return Lang.msg("272") + " : ";
			}

		});
		grid.add(comboTemporada);

		grid.add(new JLabel() {
			public String getText() {
				return Lang.msg("121");
			}
		});
		grid.add(comboBoxCircuito);
		final JLabel circuitosLabel = new JLabel() {
			public Dimension getPreferredSize() {
				return new Dimension(400, 150);
			}
		};
		comboBoxCircuito.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent arg0) {
				if (arg0.getStateChange() == ItemEvent.SELECTED) {
					desenhaMiniCircuito(circuitosLabel);
				}
			}
		});

		desenhaMiniCircuito(circuitosLabel);

		JPanel retPanel = new JPanel(new BorderLayout());
		retPanel.add(grid, BorderLayout.NORTH);
		retPanel.add(circuitosLabel, BorderLayout.CENTER);

		return retPanel;
	}

	protected void desenhaMiniCircuito(JLabel circuitosLabel) {
		BufferedImage bufferedImage = new BufferedImage(400, 150, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2d = (Graphics2D) bufferedImage.getGraphics();
		setarHints(g2d);
		g2d.setStroke(new BasicStroke(3.0f));
		g2d.setColor(Color.BLACK);
		String circuitoStr = (String) circuitos.get(comboBoxCircuito.getSelectedItem());
		CarregadorRecursos carregadorRecursos = CarregadorRecursos.getCarregadorRecursos(false);
		ObjectInputStream ois;
		Circuito circuito = null;
		try {
			ois = new ObjectInputStream(carregadorRecursos.getClass().getResourceAsStream(circuitoStr));
			circuito = (Circuito) ois.readObject();
		} catch (Exception e) {
			Logger.logarExept(e);
		}

		List pista = circuito.getPista();
		ArrayList pistaMinimizada = new ArrayList();
		double doubleMulti = 30;
		Map map = new HashMap();
		for (Iterator iterator = pista.iterator(); iterator.hasNext();) {
			No no = (No) iterator.next();
			Point p = new Point(no.getX(), no.getY());
			p.x /= doubleMulti;
			p.y /= doubleMulti;
			if (!pistaMinimizada.contains(p)) {
				map.put(p, no);
				pistaMinimizada.add(p);
			}

		}
		Point o = new Point(10, 10);
		Point oldP = null;
		No ultNo = null;
		for (Iterator iterator = pistaMinimizada.iterator(); iterator.hasNext();) {
			Point p = (Point) iterator.next();
			if (oldP != null) {
				No no = (No) map.get(oldP);
				if (no.verificaCurvaBaixa()) {
					g2d.setColor(Color.red);
				} else if (no.verificaCurvaAlta()) {
					g2d.setColor(Color.orange);
				} else if (no.verificaRetaOuLargada()) {
					g2d.setColor(new Color(0, 200, 0));
				}
				g2d.drawLine(o.x + oldP.x, o.y + oldP.y, o.x + p.x, o.y + p.y);
			}
			oldP = p;
			ultNo = (No) map.get(oldP);
		}
		Point p0 = (Point) pistaMinimizada.get(0);
		if (ultNo.verificaCurvaBaixa()) {
			g2d.setColor(Color.red);
		} else if (ultNo.verificaCurvaAlta()) {
			g2d.setColor(Color.orange);
		} else if (ultNo.verificaRetaOuLargada()) {
			g2d.setColor(new Color(0, 200, 0));
		}
		g2d.drawLine(o.x + oldP.x, o.y + oldP.y, o.x + p0.x, o.y + p0.y);

		ArrayList boxMinimizado = new ArrayList();
		List box = circuito.getBox();
		for (Iterator iterator = box.iterator(); iterator.hasNext();) {
			No no = (No) iterator.next();
			Point p = new Point(no.getX(), no.getY());
			p.x /= doubleMulti;
			p.y /= doubleMulti;
			if (!boxMinimizado.contains(p))
				boxMinimizado.add(p);
		}
		g2d.setStroke(new BasicStroke(2.0f));
		oldP = null;
		g2d.setColor(Color.lightGray);
		for (Iterator iterator = boxMinimizado.iterator(); iterator.hasNext();) {
			Point p = (Point) iterator.next();
			if (oldP != null) {
				g2d.drawLine(o.x + oldP.x, o.y + oldP.y, o.x + p.x, o.y + p.y);
			}
			oldP = p;
		}

		circuitosLabel.setIcon(new ImageIcon(bufferedImage));

	}

	private void setarHints(Graphics2D g2d) {
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		g2d.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
		g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
	}

	private void preecherDadosCriarJogo() {
		Integer qtdeVoltas = (Integer) spinnerQtdeVoltas.getValue();
		if (qtdeVoltas.intValue() >= Constantes.MAX_VOLTAS) {
			qtdeVoltas = new Integer(Constantes.MAX_VOLTAS);
		}
		if (qtdeVoltas.intValue() <= Constantes.MIN_VOLTAS) {
			qtdeVoltas = new Integer(Constantes.MIN_VOLTAS);
		}
		String temporada = (String) comboTemporada.getSelectedItem();
		dadosCriarJogo.setTemporada("t" + temporada);
		dadosCriarJogo.setQtdeVoltas(qtdeVoltas);
		dadosCriarJogo.setDiffultrapassagem((Integer) sliderDificuldadeUltrapassagem.getValue());
		String circuitoSelecionado = (String) comboBoxCircuito.getSelectedItem();
		dadosCriarJogo.setCircuitoSelecionado(circuitoSelecionado);
		dadosCriarJogo.setNivelCorrida(Lang.key(comboBoxNivelCorrida.getSelectedItem().toString()));
		dadosCriarJogo.setClima(((Clima) comboBoxClimaInicial.getSelectedItem()).getClima());
		dadosCriarJogo.setReabastecimento(reabastecimento.isSelected());
		dadosCriarJogo.setTrocaPneu(trocaPneu.isSelected());
		dadosCriarJogo.setErs(kers.isSelected());
		dadosCriarJogo.setDrs(drs.isSelected());
	}

	private void preecherDadosEntrarJogo(DadosCriarJogo dadosParticiparJogo) {
		String tpPneu = Lang.key(comboBoxPneuInicial.getSelectedItem().toString());
		Piloto piloto = (Piloto) comboBoxPilotoSelecionado.getSelectedItem();
		String asa = Lang.key((String) comboBoxAsa.getSelectedItem());
		Integer combustivel = (Integer) sliderCombustivelInicial.getValue();
		if (combustivel.intValue() > 100) {
			combustivel = new Integer(100);
		}
		if (combustivel.intValue() < 10) {
			combustivel = new Integer(10);
		}
		dadosParticiparJogo.setTpPneu(tpPneu);
		dadosParticiparJogo.setAsa(asa);
		dadosParticiparJogo.setCombustivel(combustivel);
		dadosParticiparJogo.setPiloto(piloto.getNome());
		dadosParticiparJogo.setIdPiloto(piloto.getId());
	}

	public DadosCriarJogo getDadosCriarJogo() {
		return dadosCriarJogo;
	}

	public boolean gerarDadosEntrarJogo(DadosCriarJogo dadosParticiparJogo, JPanel panelJogoCriado, String circuito,
			String temporada, String clima) {
		JPanel painelInicio = new JPanel();
		gerarPainelParticiparJogo(painelInicio, clima);
		JPanel panel = new JPanel(new BorderLayout());
		panel.add(panelJogoCriado, BorderLayout.NORTH);
		panel.add(gerarSeletorTemporadaCircuito());
		comboBoxCircuito.setSelectedItem(circuito);
		comboBoxCircuito.setEnabled(false);
		comboTemporada.setSelectedItem(temporada);
		comboTemporada.setEnabled(false);
		painelInicio.setBorder(new TitledBorder("Configuração do carro") {
			@Override
			public String getTitle() {
				return Lang.msg("233");
			}
		});
		panel.add(painelInicio, BorderLayout.SOUTH);
		int ret = JOptionPane.showConfirmDialog(mainFrame, panel, Lang.msg("127"), JOptionPane.YES_NO_OPTION);
		if (ret != JOptionPane.YES_OPTION) {
			return false;
		}
		preecherDadosEntrarJogo(dadosParticiparJogo);
		return true;
	}

	private void gerarPainelParticiparJogo(JPanel painelInicio, String clima) {
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

		if (Clima.CHUVA.equals(clima)) {
			comboBoxPneuInicial.setSelectedItem(Lang.msg(Carro.TIPO_PNEU_CHUVA));
			comboBoxAsa.setSelectedItem(Lang.msg(Carro.MAIS_ASA));
		}

		JLabel qtdeComustivel = new JLabel(Lang.msg("011"));
		sliderCombustivelInicial = new JSlider(0, 100);
		Hashtable labelTable = new Hashtable();
		labelTable.put(new Integer(000), new JLabel("") {
			@Override
			public String getText() {
				return Lang.msg("MENOS");
			}
		});
		labelTable.put(new Integer(100), new JLabel("") {
			@Override
			public String getText() {
				return Lang.msg("MAIS");
			}
		});
		sliderCombustivelInicial.setLabelTable(labelTable);
		sliderCombustivelInicial.setPaintLabels(true);

		sliderCombustivelInicial.setValue(new Integer(50));

		painelInicio.add(tipoPneu);
		painelInicio.add(comboBoxPneuInicial);
		painelInicio.add(tipoAsa);
		painelInicio.add(comboBoxAsa);
		painelInicio.add(qtdeComustivel);
		painelInicio.add(sliderCombustivelInicial);

	}

	public String getTemporada() {
		String temporada = (String) comboTemporada.getSelectedItem();
		return "t" + temporada;
	}

	public JComboBox getComboTemporada() {
		return comboTemporada;
	}

}
