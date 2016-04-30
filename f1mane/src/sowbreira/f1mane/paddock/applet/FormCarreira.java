package sowbreira.f1mane.paddock.applet;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.TitledBorder;

import br.nnpe.Logger;
import br.nnpe.Numero;
import br.nnpe.Util;
import sowbreira.f1mane.paddock.entidades.TOs.ClientPaddockPack;
import sowbreira.f1mane.paddock.entidades.persistencia.CarreiraDadosSrv;
import sowbreira.f1mane.paddock.servlet.ControleClassificacao;
import sowbreira.f1mane.recursos.CarregadorRecursos;
import sowbreira.f1mane.recursos.idiomas.Lang;

/**
 * @author Paulo Sobreira Criado em 27/06/2009 as 15:48:51
 */
public class FormCarreira extends JPanel {

	private JLabel labelPtsCarreira = new JLabel("Pontos Carreira:") {
		@Override
		public String getText() {
			return Lang.msg("266");
		}
	};
	JLabel ptsCarreiraVal = new JLabel("0") {
		public String getText() {
			return String.valueOf(ptsCarreira);
		}
	};
	private JLabel labelModoCarreira = new JLabel("Modo Carreira:") {
		@Override
		public String getText() {
			return Lang.msg("252");
		}
	};
	private JCheckBox modoCarreira = new JCheckBox();
	private JLabel labelNomePiloto = new JLabel("Nome Piloto:") {
		@Override
		public String getText() {
			return Lang.msg("253");
		}
	};
	private JTextField nomePiloto = new JTextField(10);
	private JLabel labelNomeCarro = new JLabel("Nome Equipe:") {
		@Override
		public String getText() {
			return Lang.msg("254");
		}
	};
	private JTextField nomeCarro = new JTextField(10);
	private JLabel labelPtsPiloto = new JLabel("Habilidade Piloto:") {
		@Override
		public String getText() {
			return Lang.msg("255");
		}
	};
	private JSpinner ptsPiloto = new JSpinner();
	private JLabel labelPtsCarro = new JLabel("Pontencia Carro:") {
		@Override
		public String getText() {
			return Lang.msg("256");
		}
	};
	private JSpinner ptsCarro = new JSpinner();

	private JLabel labelPtsAeroDimanica = new JLabel("Aero dinâmica Carro:") {
		@Override
		public String getText() {
			return Lang.msg("aerodinamicaCarro");
		}
	};
	private JSpinner ptsAeroDinamica = new JSpinner();

	private JLabel labelPtsFreio = new JLabel("Freio Carro:") {
		@Override
		public String getText() {
			return Lang.msg("freioCarro");
		}
	};
	private JSpinner ptsFreio = new JSpinner();

	private JLabel labelCor1 = new JLabel("Click 1:");
	private JLabel labelCor2 = new JLabel("Click 2:");

	private List listaCarro = new ArrayList();
	private JLabel imgCarroLado = new JLabel();
	private JLabel imgCarroCima = new JLabel();
	private String imgCarroStr = "";
	private Integer ptsCarreira = 1;

	public FormCarreira() {

		JPanel panelNorte = new JPanel();
		panelNorte.setBorder(new TitledBorder("") {
			public String getTitle() {
				return Lang.msg("dadosEquipe");
			}
		});
		panelNorte.setLayout(new GridLayout(3, 2));
		panelNorte.add(labelModoCarreira);
		panelNorte.add(modoCarreira);

		panelNorte.add(labelNomePiloto);
		panelNorte.add(nomePiloto);

		panelNorte.add(labelNomeCarro);
		panelNorte.add(nomeCarro);

		JPanel panelSul = new JPanel();
		panelSul.setLayout(new GridLayout(5, 2));
		panelSul.setBorder(new TitledBorder("") {
			public String getTitle() {
				return Lang.msg("distribuicaoPontos");
			}
		});

		panelSul.add(labelPtsCarreira);
		panelSul.add(ptsCarreiraVal);

		panelSul.add(labelPtsPiloto);
		panelSul.add(ptsPiloto);

		panelSul.add(labelPtsCarro);
		panelSul.add(ptsCarro);

		panelSul.add(labelPtsAeroDimanica);
		panelSul.add(ptsAeroDinamica);

		panelSul.add(labelPtsFreio);
		panelSul.add(ptsFreio);

		JPanel panelLabel = new JPanel();
		panelLabel.setLayout(new GridLayout(1, 2));

		panelLabel.add(labelCor1);
		panelLabel.add(labelCor2);

		JPanel panelCarro = new JPanel();
		panelCarro.setLayout(new GridLayout(1, 2));
		panelCarro.add(imgCarroCima);
		panelCarro.add(imgCarroLado);
		labelCor1.setOpaque(true);
		labelCor1.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				setaCor1Carro();
			}

		});
		imgCarroCima.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				setaCor1Carro();
			}

		});

		labelCor2.setOpaque(true);
		labelCor2.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent e) {
				setaCor2Carro();
			}

		});
		imgCarroLado.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent e) {
				setaCor2Carro();
			}

		});
		ptsPiloto.setModel(new CarreiraSpinnerModel());
		ptsCarro.setModel(new CarreiraSpinnerModel());
		ptsAeroDinamica.setModel(new CarreiraSpinnerModel());
		ptsFreio.setModel(new CarreiraSpinnerModel());
		JFormattedTextField tfptsAeroDinamica = ((JSpinner.DefaultEditor) ptsAeroDinamica.getEditor()).getTextField();
		tfptsAeroDinamica.setEditable(false);
		JFormattedTextField tfptsFreio = ((JSpinner.DefaultEditor) ptsFreio.getEditor()).getTextField();
		tfptsFreio.setEditable(false);
		JFormattedTextField tfptsCarro = ((JSpinner.DefaultEditor) ptsCarro.getEditor()).getTextField();
		tfptsCarro.setEditable(false);
		JFormattedTextField tfptsPiloto = ((JSpinner.DefaultEditor) ptsPiloto.getEditor()).getTextField();
		tfptsPiloto.setEditable(false);
		JPanel panelCentro = new JPanel(new BorderLayout());
		panelCentro.setBorder(new TitledBorder("") {
			public String getTitle() {
				return Lang.msg("corEquipeCarro");
			}
		});
		panelCentro.add(panelLabel, BorderLayout.CENTER);
		panelCentro.add(panelCarro, BorderLayout.SOUTH);
		setLayout(new BorderLayout());
		add(panelNorte, BorderLayout.NORTH);
		add(panelCentro, BorderLayout.CENTER);
		add(panelSul, BorderLayout.SOUTH);
		gerarCarroLado();
		gerarCarroCima();
	}

	protected void gerarCarroCima() {
		BufferedImage carroLado = CarregadorRecursos.carregaImagem("CarroCima.png");
		BufferedImage cor1 = CarregadorRecursos.gerarCoresCarros(labelCor1.getBackground(), "CarroCimaC1.png");
		BufferedImage cor2 = CarregadorRecursos.gerarCoresCarros(labelCor2.getBackground(), "CarroCimaC2.png");
		Graphics graphics = carroLado.getGraphics();
		graphics.drawImage(cor1, 0, 0, null);
		graphics.drawImage(cor2, 0, 0, null);
		graphics.dispose();
		imgCarroCima.setIcon(new ImageIcon(carroLado));

	}

	protected void gerarCarroLado() {
		BufferedImage carroLado = CarregadorRecursos.carregaImagem("CarroLado.png");
		BufferedImage cor1 = CarregadorRecursos.gerarCoresCarros(labelCor1.getBackground(), "CarroLadoC1.png");
		BufferedImage cor2 = CarregadorRecursos.gerarCoresCarros(labelCor2.getBackground(), "CarroLadoC2.png");
		Graphics graphics = carroLado.getGraphics();
		graphics.drawImage(cor1, 0, 0, null);
		graphics.drawImage(cor2, 0, 0, null);
		graphics.dispose();
		imgCarroLado.setIcon(new ImageIcon(carroLado));
	}

	public static void main(String[] args) {
		FormCarreira formCarreira = new FormCarreira();
		int ptsCarreira = 3;//Util.intervalo(1000, 5000);
		formCarreira.ptsCarreira = ptsCarreira;
		formCarreira.ptsAeroDinamica.setValue(600);
		formCarreira.ptsCarro.setValue(850);
		formCarreira.ptsFreio.setValue(600);
		formCarreira.ptsPiloto.setValue(850);
		JOptionPane.showMessageDialog(null, formCarreira);
		CarreiraDadosSrv carreiraDadosSrv = new CarreiraDadosSrv();
		carreiraDadosSrv.setPtsCarro(850);
		carreiraDadosSrv.setPtsAerodinamica(600);
		carreiraDadosSrv.setPtsFreio(600);
		carreiraDadosSrv.setPtsPiloto(850);
		carreiraDadosSrv.setPtsConstrutores(ptsCarreira);

		System.out.println(ControleClassificacao.validadeDistribucaoPontos(carreiraDadosSrv,
				(Integer) formCarreira.getPtsAeroDinamica().getValue(), (Integer) formCarreira.getPtsCarro().getValue(),
				(Integer) formCarreira.getPtsFreio().getValue(), (Integer) formCarreira.getPtsPiloto().getValue(),
				formCarreira.getPtsCarreira()));
	}

	public int getPtsCarreira() {
		return ptsCarreira;
	}

	public void setPtsCarreira(int ptsCarreira) {
		this.ptsCarreira = ptsCarreira;
	}

	public JTextField getNomePiloto() {
		return nomePiloto;
	}

	public JTextField getNomeCarro() {
		return nomeCarro;
	}

	public JSpinner getPtsPiloto() {
		return ptsPiloto;
	}

	public JSpinner getPtsCarro() {
		return ptsCarro;
	}

	public JCheckBox getModoCarreira() {
		return modoCarreira;
	}

	private class CarreiraSpinnerModel extends SpinnerNumberModel {
		@Override
		public void setValue(Object value) {
			int val = (Integer) getValue();
			if (val == 0) {
				super.setValue(value);
			} else {
				int nexVal = (Integer) value;
				if (val != nexVal && nexVal >= 600 && nexVal <= 999) {
					Numero numero = new Numero(ptsCarreira);
					if (Util.processaValorPontosCarreira(val, nexVal, numero)) {
						ptsCarreira = numero.getNumero().intValue();
						try {
							ptsCarreiraVal.repaint();
						} catch (Exception e) {
							Logger.logarExept(e);
						}
						super.setValue(value);
					}
				}
			}
		}
	}

	public JLabel getLabelCor1() {
		return labelCor1;
	}

	public JLabel getLabelCor2() {
		return labelCor2;
	}

	public void setCor1(Color color) {
		if (color == null) {
			return;
		}
		labelCor1.setBackground(color);
		int valor = (color.getRed() + color.getGreen() + color.getBlue()) / 2;
		if (valor > 250) {
			labelCor1.setForeground(Color.BLACK);
		} else {
			labelCor1.setForeground(Color.WHITE);
		}
		try {
			labelCor1.repaint();
		} catch (Exception e) {
			Logger.logarExept(e);
		}

	}

	public Color getCor1() {
		return labelCor1.getBackground();
	}

	public Color getCor2() {
		return labelCor2.getBackground();
	}

	public void setCor2(Color color) {
		if (color == null) {
			return;
		}
		labelCor2.setBackground(color);
		int valor = (color.getRed() + color.getGreen() + color.getBlue()) / 2;
		if (valor > 250) {
			labelCor2.setForeground(Color.BLACK);
		} else {
			labelCor2.setForeground(Color.WHITE);
		}
		try {
			labelCor2.repaint();
		} catch (Exception e) {
			Logger.logarExept(e);
		}

	}

	public String getImgCarroStr() {
		return imgCarroStr;
	}

	public void setImgCarroStr(String imgCarroStr) {
		this.imgCarroStr = imgCarroStr;
	}

	public JSpinner getPtsAeroDinamica() {
		return ptsAeroDinamica;
	}

	public JSpinner getPtsFreio() {
		return ptsFreio;
	}

	private void setaCor1Carro() {
		Color color = JColorChooser.showDialog(FormCarreira.this, "Escolha uma cor", Color.WHITE);
		setCor1(color);
		gerarCarroLado();
		gerarCarroCima();
	}

	private void setaCor2Carro() {
		Color color = JColorChooser.showDialog(FormCarreira.this, "Escolha uma cor", Color.WHITE);
		setCor2(color);
		gerarCarroLado();
		gerarCarroCima();
		FormCarreira.this.repaint();
	}

}
