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
import br.f1mane.entidades.ObjetoLivre;
import br.f1mane.entidades.ObjetoPista;
import br.f1mane.entidades.ObjetoTransparencia;
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
	private JLabel labelCor1 = new JLabel("Clique");
	private JLabel labelCor2 = new JLabel("Clique");
	private final JLabel labelLegendaCor1 = new JLabel("Cor de Fundo");
	private final JLabel labelLegendaCor2 = new JLabel("Cor de Padrão");
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

		labelCor1.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				Color nova = JColorChooser.showDialog(panel, "Cor de Fundo",
						labelCor1.getBackground());
				if (nova != null) {
					setCor(nova, labelCor1);
				}
			}
		});
		labelCor2.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				Color nova = JColorChooser.showDialog(panel, "Cor de Padrão",
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
	 * transparência (mais tamanho/ângulo); ObjetoLivre usa tamanho/ângulo,
	 * as duas cores e o seletor de padrão; os demais tipos usam
	 * tamanho/ângulo e as duas cores, sem padrão nem transparência.
	 */
	private void montarPainelParaTipo(ObjetoPista objetoPista) {
		panel.removeAll();
		if (objetoPista instanceof ObjetoTransparencia) {
			panel.setLayout(new GridLayout(6, 2));
			panel.add(new JLabel("No Início Transparência"));
			panel.add(inicioTranparencia);
			panel.add(new JLabel("No Fim Transparência"));
			panel.add(fimTransparencia);
			panel.add(new JLabel("Transparencia Box"));
			panel.add(transparenciaBox);
			panel.add(new JLabel("Altura"));
			panel.add(altura);
			panel.add(new JLabel("Largura"));
			panel.add(largura);
			panel.add(new JLabel("Ângulo"));
			panel.add(angulo);
		} else if (objetoPista instanceof ObjetoLivre) {
			panel.setLayout(new GridLayout(7, 2));
			panel.add(new JLabel("Ângulo"));
			panel.add(angulo);
			panel.add(new JLabel("Largura"));
			panel.add(largura);
			panel.add(new JLabel("Altura"));
			panel.add(altura);
			panel.add(labelLegendaCor1);
			panel.add(labelCor1);
			panel.add(labelLegendaCor2);
			panel.add(labelCor2);
			panel.add(new JLabel("Padrão"));
			panel.add(tipoObjetoLivreCombo);
			panel.add(new JLabel("Nível"));
			panel.add(nivelDesenho);
		} else {
			panel.setLayout(new GridLayout(6, 2));
			panel.add(new JLabel("Ângulo"));
			panel.add(angulo);
			panel.add(new JLabel("Largura"));
			panel.add(largura);
			panel.add(new JLabel("Altura"));
			panel.add(altura);
			panel.add(labelLegendaCor1);
			panel.add(labelCor1);
			panel.add(labelLegendaCor2);
			panel.add(labelCor2);
			panel.add(new JLabel("Nível"));
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
			objetoPista.setNivelDesenho(((Integer) nivelDesenho.getValue()).intValue());
			if (objetoPista instanceof ObjetoLivre) {
				((ObjetoLivre) objetoPista).setTipo((TipoObjetoLivre) tipoObjetoLivreCombo.getSelectedItem());
			}
		}
		// Guarda os valores atuais como "última configuração" desta classe,
		// pra o próximo objeto criado deste mesmo tipo já nascer com eles.
		MemoriaPropriedadesObjeto.lembrar(objetoPista);
	}
}
