package br.f1mane.editor;

import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import br.nnpe.Logger;
import br.f1mane.recursos.idiomas.Lang;
import br.f1mane.entidades.DirecaoEmpilhamento;
import br.f1mane.entidades.ObjetoConstrucao;
import br.f1mane.entidades.ObjetoDesenho;
import br.f1mane.entidades.ObjetoEscapada;
import br.f1mane.entidades.ObjetoGuardRails;
import br.f1mane.entidades.ObjetoLivre;
import br.f1mane.entidades.ObjetoPista;
import br.f1mane.entidades.ObjetoTransparencia;
import br.f1mane.entidades.OrientacaoGuardRails;
import br.f1mane.entidades.TipoObjetoConstrucao;
import br.f1mane.entidades.TipoObjetoLivre;

public class FormularioObjetos {

	private final JSpinner largura = new JSpinner();
	private final JSpinner altura = new JSpinner();
	private JSpinner inicioTranparencia = new JSpinner();
	private JSpinner fimTransparencia = new JSpinner();
	private final JSpinner angulo = new JSpinner();
	/** Sem limite de faixa, igual ao atalho PageUp/PageDown; não se aplica a ObjetoTransparencia. */
	private final JSpinner nivelDesenho = new JSpinner(
			new SpinnerNumberModel(0, Integer.MIN_VALUE, Integer.MAX_VALUE, 1));
	private final JCheckBox transparenciaBox = new JCheckBox();
	private final JComboBox<TipoObjetoLivre> tipoObjetoLivreCombo = new JComboBox<TipoObjetoLivre>(
			TipoObjetoLivre.values());
	private final JComboBox<OrientacaoGuardRails> orientacaoGuardRailsCombo = new JComboBox<OrientacaoGuardRails>(
			OrientacaoGuardRails.values());
	private final JSpinner larguraLinhaGuardRails = new JSpinner(new SpinnerNumberModel(1, 1, 100, 1));
	private final JSpinner vaoEntreLinhasGuardRails = new JSpinner(new SpinnerNumberModel(1, 0, 100, 1));
	private final JComboBox<TipoObjetoConstrucao> tipoObjetoConstrucaoCombo = new JComboBox<TipoObjetoConstrucao>(
			TipoObjetoConstrucao.values());
	private final JSpinner afunilamentoSpinner = new JSpinner(new SpinnerNumberModel(30, 0, 90, 1));
	private final JSpinner quantidadeEmpilhamentoSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 20, 1));
	private final JComboBox<DirecaoEmpilhamento> direcaoEmpilhamentoCombo = new JComboBox<DirecaoEmpilhamento>(
			DirecaoEmpilhamento.values());
	private final JSpinner grauEmpilhamentoSpinner = new JSpinner(new SpinnerNumberModel(10, 0, 1000, 1));
	private JLabel labelCor1 = new JLabel(Lang.msg("cliqueParaCor"));
	private JLabel labelCor2 = new JLabel(Lang.msg("cliqueParaCor"));
	private final JLabel labelLegendaCor1 = new JLabel(Lang.msg("corDeFundo"));
	private final JLabel labelLegendaCor2 = new JLabel(Lang.msg("corDePadrao"));
	private final JPanel panel = new JPanel(new GridLayout(1, 2));
	private final MainPanelEditor mainPanelEditor;
	private ObjetoPista objetoPista;

	public FormularioObjetos(MainPanelEditor panelPai) {
		this.mainPanelEditor = panelPai;

		ChangeListener changeListener = new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				atualizaMain();

			}
		};
		inicioTranparencia.addChangeListener(changeListener);
		largura.addChangeListener(changeListener);
		altura.addChangeListener(changeListener);
		fimTransparencia.addChangeListener(changeListener);
		nivelDesenho.addChangeListener(changeListener);
		larguraLinhaGuardRails.addChangeListener(changeListener);
		vaoEntreLinhasGuardRails.addChangeListener(changeListener);
		afunilamentoSpinner.addChangeListener(changeListener);
		quantidadeEmpilhamentoSpinner.addChangeListener(changeListener);
		grauEmpilhamentoSpinner.addChangeListener(changeListener);

		labelCor1.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				Color nova = JColorChooser.showDialog(panel, Lang.msg("corDeFundo"),
						labelCor1.getBackground());
				if (nova != null) {
					setCor(nova, labelCor1);
				}
			}
		});
		labelCor2.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				Color nova = JColorChooser.showDialog(panel, Lang.msg("corDePadrao"),
						labelCor2.getBackground());
				if (nova != null) {
					setCor(nova, labelCor2);
				}
			}
		});
	}

	/**
	 * Reconstrói o painel só com os campos relevantes para o tipo de
	 * {@code objetoPista}: ObjetoTransparencia usa apenas as propriedades de
	 * transparência (mais tamanho/ângulo); ObjetoLivre usa tamanho/ângulo, as
	 * duas cores, o seletor de padrão e o nível; ObjetoEscapada usa
	 * tamanho/ângulo e as duas cores, sem nível (objeto de função, fora do
	 * sistema de níveis, como Transparência); os demais tipos (objetos de
	 * desenho, como Arquibancada/Construcao/GuardRails/Pneus) usam
	 * tamanho/ângulo, as duas cores e o nível.
	 */
	private void montarPainelParaTipo(ObjetoPista objetoPista) {
		panel.removeAll();
		// Objetos de desenho não aceitam largura/altura menor que 1 nem ângulo
		// negativo (ver ObjetoDesenho); objetos de função (Escapada,
		// Transparencia) continuam sem essa restrição no spinner.
		boolean objetoDeDesenho = objetoPista instanceof ObjetoDesenho;
		int larguraMinima = objetoDeDesenho ? 1 : 0;
		int anguloMinimo = objetoDeDesenho ? 0 : -360;
		largura.setModel(new SpinnerNumberModel(Math.max(larguraMinima, objetoPista.getLargura()),
				larguraMinima, 10000, 1));
		altura.setModel(new SpinnerNumberModel(Math.max(larguraMinima, objetoPista.getAltura()),
				larguraMinima, 10000, 1));
		angulo.setModel(new SpinnerNumberModel(Math.max(anguloMinimo, (int) objetoPista.getAngulo()),
				anguloMinimo, 360, 1));
		if (objetoPista instanceof ObjetoTransparencia) {
			panel.setLayout(new GridLayout(6, 2));
			panel.add(new JLabel(Lang.msg("noInicioTransparencia")));
			panel.add(inicioTranparencia);
			panel.add(new JLabel(Lang.msg("noFimTransparencia")));
			panel.add(fimTransparencia);
			panel.add(new JLabel(Lang.msg("transparenciaBox")));
			panel.add(transparenciaBox);
			panel.add(new JLabel(Lang.msg("altura")));
			panel.add(altura);
			panel.add(new JLabel(Lang.msg("largura")));
			panel.add(largura);
			panel.add(new JLabel(Lang.msg("angulo")));
			panel.add(angulo);
		} else if (objetoPista instanceof ObjetoLivre) {
			// Largura/Altura não fazem sentido para ObjetoLivre: sua área vem
			// dos vértices/pontos desenhados (ver ObjetoLivre.obterArea()),
			// não desses campos, que ficam sem efeito no desenho.
			panel.setLayout(new GridLayout(5, 2));
			panel.add(new JLabel(Lang.msg("angulo")));
			panel.add(angulo);
			panel.add(labelLegendaCor1);
			panel.add(labelCor1);
			panel.add(labelLegendaCor2);
			panel.add(labelCor2);
			panel.add(new JLabel(Lang.msg("197")));
			panel.add(tipoObjetoLivreCombo);
			panel.add(new JLabel(Lang.msg("nivelDesenhoLabel")));
			panel.add(nivelDesenho);
		} else if (objetoPista instanceof ObjetoGuardRails) {
			// GuardRails é desenhado ponto a ponto no editor (clique a clique,
			// botão direito finaliza), como ObjetoLivre: Altura e Ângulo não
			// fazem sentido (cada segmento do encadeamento calcula o próprio
			// ângulo a partir dos pontos), mas Largura continua valendo — é a
			// espessura da barreira ao longo de todo o encadeamento.
			panel.setLayout(new GridLayout(7, 2));
			panel.add(new JLabel(Lang.msg("largura")));
			panel.add(largura);
			panel.add(labelLegendaCor1);
			panel.add(labelCor1);
			panel.add(labelLegendaCor2);
			panel.add(labelCor2);
			panel.add(new JLabel(Lang.msg("orientacao")));
			panel.add(orientacaoGuardRailsCombo);
			panel.add(new JLabel(Lang.msg("espessuraLinha")));
			panel.add(larguraLinhaGuardRails);
			panel.add(new JLabel(Lang.msg("vaoEntreLinhas")));
			panel.add(vaoEntreLinhasGuardRails);
			panel.add(new JLabel(Lang.msg("nivelDesenhoLabel")));
			panel.add(nivelDesenho);
		} else if (objetoPista instanceof ObjetoEscapada) {
			panel.setLayout(new GridLayout(5, 2));
			panel.add(new JLabel(Lang.msg("angulo")));
			panel.add(angulo);
			panel.add(new JLabel(Lang.msg("largura")));
			panel.add(largura);
			panel.add(new JLabel(Lang.msg("altura")));
			panel.add(altura);
			panel.add(labelLegendaCor1);
			panel.add(labelCor1);
			panel.add(labelLegendaCor2);
			panel.add(labelCor2);
		} else if (objetoPista instanceof ObjetoConstrucao) {
			// Afunilamento só faz sentido para o tipo BARCO; empilhamento
			// (quantidade/direção/grau) é transversal a qualquer tipo, então
			// aparece sempre, independente do tipo selecionado.
			boolean mostraAfunilamento = ((ObjetoConstrucao) objetoPista).getTipo() == TipoObjetoConstrucao.BARCO;
			panel.setLayout(new GridLayout(mostraAfunilamento ? 11 : 10, 2));
			panel.add(new JLabel(Lang.msg("angulo")));
			panel.add(angulo);
			panel.add(new JLabel(Lang.msg("largura")));
			panel.add(largura);
			panel.add(new JLabel(Lang.msg("altura")));
			panel.add(altura);
			panel.add(labelLegendaCor1);
			panel.add(labelCor1);
			panel.add(labelLegendaCor2);
			panel.add(labelCor2);
			panel.add(new JLabel(Lang.msg("tipoLabel")));
			panel.add(tipoObjetoConstrucaoCombo);
			if (mostraAfunilamento) {
				panel.add(new JLabel(Lang.msg("afunilamento")));
				panel.add(afunilamentoSpinner);
			}
			panel.add(new JLabel(Lang.msg("qtdEmpilhamento")));
			panel.add(quantidadeEmpilhamentoSpinner);
			panel.add(new JLabel(Lang.msg("direcaoEmpilhamento")));
			panel.add(direcaoEmpilhamentoCombo);
			panel.add(new JLabel(Lang.msg("grauEmpilhamento")));
			panel.add(grauEmpilhamentoSpinner);
			panel.add(new JLabel(Lang.msg("nivelDesenhoLabel")));
			panel.add(nivelDesenho);
		} else {
			panel.setLayout(new GridLayout(6, 2));
			panel.add(new JLabel(Lang.msg("angulo")));
			panel.add(angulo);
			panel.add(new JLabel(Lang.msg("largura")));
			panel.add(largura);
			panel.add(new JLabel(Lang.msg("altura")));
			panel.add(altura);
			panel.add(labelLegendaCor1);
			panel.add(labelCor1);
			panel.add(labelLegendaCor2);
			panel.add(labelCor2);
			panel.add(new JLabel(Lang.msg("nivelDesenhoLabel")));
			panel.add(nivelDesenho);
		}
		panel.revalidate();
		panel.repaint();
	}

	protected void atualizaMain() {
		if (objetoPista != null
				&& FormularioObjetos.this.mainPanelEditor != null) {
			formularioObjetoPista(objetoPista);
			FormularioObjetos.this.mainPanelEditor.reprocessaEscapadaSeNecessario(objetoPista);
			FormularioObjetos.this.mainPanelEditor.repaint();
		}

	}

	public void mostrarPainelModal() {
		JOptionPane.showMessageDialog(this.mainPanelEditor.getSrcFrame(),
				panel);
		if (objetoPista != null) {
			formularioObjetoPista(objetoPista);
			FormularioObjetos.this.mainPanelEditor.reprocessaEscapadaSeNecessario(objetoPista);
		}
		FormularioObjetos.this.mainPanelEditor.repaint();
	}

	public void setCor(Color color, JLabel label) {
		if (color == null) {
			return;
		}
		label.setOpaque(true);
		label.setBackground(color);
		int valor = (color.getRed() + color.getGreen() + color.getBlue()) / 2;
		if (valor > 250) {
			label.setForeground(Color.BLACK);
		} else {
			label.setForeground(Color.WHITE);
		}
		try {
			label.repaint();
		} catch (Exception e) {
			Logger.logarExept(e);
		}
		atualizaMain();
	}

	public static void main(String[] args) {
		FormularioObjetos formularioObjetos = new FormularioObjetos(null);
	}

	public JSpinner getLargura() {
		return largura;
	}

	public JSpinner getInicioTranparencia() {
		return inicioTranparencia;
	}

	public void setInicioTranparencia(JSpinner inicioTranparencia) {
		this.inicioTranparencia = inicioTranparencia;
	}

	public JSpinner getFimTransparencia() {
		return fimTransparencia;
	}

	public void setFimTransparencia(JSpinner fimTransparencia) {
		this.fimTransparencia = fimTransparencia;
	}

	public JLabel getLabelCor1() {
		return labelCor1;
	}

	public void setLabelCor1(JLabel labelCor1) {
		this.labelCor1 = labelCor1;
	}

	public JLabel getLabelCor2() {
		return labelCor2;
	}

	public void setLabelCor2(JLabel labelCor2) {
		this.labelCor2 = labelCor2;
	}

	public void objetoLivreFormulario(ObjetoPista objetoPista) {
		this.objetoPista = null;
		carregarCampos(objetoPista);
		this.objetoPista = objetoPista;
		mostrarPainelModal();
	}

	/**
	 * Reconstrói o painel para o tipo de {@code objetoPista} (só os campos
	 * relevantes) e popula esses campos a partir do estado atual do objeto,
	 * para que abrir o formulário (seja para editar um objeto existente,
	 * seja logo após criar um novo com valores padrão) não sobrescreva
	 * esses valores com o estado em branco dos campos Swing.
	 */
	void carregarCampos(ObjetoPista objetoPista) {
		montarPainelParaTipo(objetoPista);
		angulo.setValue(Integer.valueOf((int) objetoPista.getAngulo()));
		largura.setValue(Integer.valueOf(objetoPista.getLargura()));
		altura.setValue(Integer.valueOf(objetoPista.getAltura()));
		nivelDesenho.setValue(Integer.valueOf(objetoPista.getNivelDesenho()));
		if (objetoPista instanceof ObjetoTransparencia) {
			inicioTranparencia.setValue(Integer.valueOf(objetoPista.getInicioTransparencia()));
			fimTransparencia.setValue(Integer.valueOf(objetoPista.getFimTransparencia()));
			transparenciaBox.setSelected(objetoPista.isTransparenciaBox());
		} else {
			setCor(objetoPista.getCorPimaria(), labelCor1);
			setCor(objetoPista.getCorSecundaria(), labelCor2);
			if (objetoPista instanceof ObjetoLivre) {
				tipoObjetoLivreCombo.setSelectedItem(((ObjetoLivre) objetoPista).getTipo());
			}
			if (objetoPista instanceof ObjetoGuardRails) {
				ObjetoGuardRails guardRails = (ObjetoGuardRails) objetoPista;
				orientacaoGuardRailsCombo.setSelectedItem(guardRails.getOrientacao());
				larguraLinhaGuardRails.setValue(Integer.valueOf(guardRails.getLarguraLinha()));
				vaoEntreLinhasGuardRails.setValue(Integer.valueOf(guardRails.getVaoEntreLinhas()));
			}
			if (objetoPista instanceof ObjetoConstrucao) {
				ObjetoConstrucao objetoConstrucao = (ObjetoConstrucao) objetoPista;
				tipoObjetoConstrucaoCombo.setSelectedItem(objetoConstrucao.getTipo());
				afunilamentoSpinner.setValue(Integer.valueOf(objetoConstrucao.getAfunilamento()));
				quantidadeEmpilhamentoSpinner.setValue(Integer.valueOf(objetoConstrucao.getQuantidadeEmpilhamento()));
				direcaoEmpilhamentoCombo.setSelectedItem(objetoConstrucao.getDirecaoEmpilhamento());
				grauEmpilhamentoSpinner.setValue(Integer.valueOf(objetoConstrucao.getGrauEmpilhamento()));
			}
		}
	}

	public void formularioObjetoPista(ObjetoPista objetoPista) {
		objetoPista.setAngulo(((Integer) angulo.getValue()).doubleValue());
		objetoPista.setLargura(((Integer) largura.getValue()).intValue());
		objetoPista.setAltura(((Integer) altura.getValue()).intValue());
		if (objetoPista instanceof ObjetoTransparencia) {
			objetoPista.setInicioTransparencia(((Integer) getInicioTranparencia().getValue()).intValue());
			objetoPista.setFimTransparencia(((Integer) getFimTransparencia().getValue()).intValue());
			objetoPista.setTransparenciaBox(transparenciaBox.isSelected());
		} else {
			objetoPista.setCorPimaria(labelCor1.getBackground());
			objetoPista.setCorSecundaria(labelCor2.getBackground());
			if (!(objetoPista instanceof ObjetoEscapada)) {
				objetoPista.setNivelDesenho(((Integer) nivelDesenho.getValue()).intValue());
			}
			if (objetoPista instanceof ObjetoLivre) {
				((ObjetoLivre) objetoPista).setTipo((TipoObjetoLivre) tipoObjetoLivreCombo.getSelectedItem());
			}
			if (objetoPista instanceof ObjetoGuardRails) {
				ObjetoGuardRails guardRails = (ObjetoGuardRails) objetoPista;
				guardRails.setOrientacao((OrientacaoGuardRails) orientacaoGuardRailsCombo.getSelectedItem());
				guardRails.setLarguraLinha(((Integer) larguraLinhaGuardRails.getValue()).intValue());
				guardRails.setVaoEntreLinhas(((Integer) vaoEntreLinhasGuardRails.getValue()).intValue());
			}
			if (objetoPista instanceof ObjetoConstrucao) {
				ObjetoConstrucao objetoConstrucao = (ObjetoConstrucao) objetoPista;
				objetoConstrucao.setTipo((TipoObjetoConstrucao) tipoObjetoConstrucaoCombo.getSelectedItem());
				objetoConstrucao.setAfunilamento(((Integer) afunilamentoSpinner.getValue()).intValue());
				objetoConstrucao
						.setQuantidadeEmpilhamento(((Integer) quantidadeEmpilhamentoSpinner.getValue()).intValue());
				objetoConstrucao.setDirecaoEmpilhamento((DirecaoEmpilhamento) direcaoEmpilhamentoCombo.getSelectedItem());
				objetoConstrucao.setGrauEmpilhamento(((Integer) grauEmpilhamentoSpinner.getValue()).intValue());
			}
		}
		// Guarda os valores atuais como "última configuração" desta classe,
		// pra o próximo objeto criado deste mesmo tipo já nascer com eles.
		MemoriaPropriedadesObjeto.lembrar(objetoPista);
	}
}
