package sowbreira.f1mane.visao;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.JPanel;

import sowbreira.f1mane.controles.ControleEstatisticas;
import sowbreira.f1mane.controles.InterfaceJogo;
import sowbreira.f1mane.entidades.Carro;
import sowbreira.f1mane.entidades.Piloto;
import sowbreira.f1mane.entidades.SafetyCar;
import sowbreira.f1mane.entidades.Volta;
import sowbreira.f1mane.recursos.CarregadorRecursos;
import sowbreira.f1mane.recursos.idiomas.Lang;

/**
 * @author Paulo Sobreira
 */
public class PainelCircuito extends JPanel {
	private static final long serialVersionUID = -5268795362549996148L;
	private BufferedImage backGround;
	private InterfaceJogo controleJogo;
	private GerenciadorVisual gerenciadorVisual;
	private Point pointDesenhaClima = new Point(10, 60);
	public final static Color luzDistProx1 = new Color(0, 255, 0, 100);
	public final static Color luzDistProx2 = new Color(255, 255, 0, 100);
	public final static Color luzApagada = new Color(255, 255, 255, 170);
	public final static Color luzAcesa = new Color(255, 0, 0, 255);
	public final static Color farol = new Color(0, 0, 0);
	public final static Color red = new Color(250, 0, 0, 150);
	public final static Color gre = new Color(0, 255, 0, 150);
	public final static Color yel = new Color(255, 255, 0, 150);
	private int qtdeLuzesAcesas = 5;
	private Piloto pilotQualificacao;
	private Point pointQualificacao;
	private Map mapDesenharQualificacao = new HashMap();
	private boolean desenhaQualificacao;
	private boolean desenhaPosVelo = true;
	private ImageIcon fuel;
	private ImageIcon tyre;

	public boolean isDesenhaPosVelo() {
		return desenhaPosVelo;
	}

	public void setDesenhaPosVelo(boolean desenhaPosVelo) {
		this.desenhaPosVelo = desenhaPosVelo;
	}

	public PainelCircuito(InterfaceJogo jogo,
			GerenciadorVisual gerenciadorVisual) {
		controleJogo = jogo;
		this.gerenciadorVisual = gerenciadorVisual;
		fuel = new ImageIcon(CarregadorRecursos.carregarImagem("fuel.jpg"));
		tyre = new ImageIcon(CarregadorRecursos.carregarImagem("tyre.jpg"));
		addMouseListener(new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent e) {
				// System.out.println("Pontos Editor :" + e.getX() + " - "
				// + e.getY());
				super.mouseClicked(e);

			}

		});
	}

	public void setMapDesenharQualificacao(Map desenharQualificacao) {
		this.mapDesenharQualificacao = desenharQualificacao;
	}

	public boolean isDesenhaQualificacao() {
		return desenhaQualificacao;
	}

	public void setDesenhaQualificacao(boolean desenhaQualificacao) {
		this.desenhaQualificacao = desenhaQualificacao;
	}

	public Map getMapDesenharQualificacao() {
		return mapDesenharQualificacao;
	}

	public BufferedImage getBackGround() {
		return backGround;
	}

	public void setBackGround(BufferedImage backGround) {
		this.backGround = backGround;
	}

	protected void paintComponent(Graphics g) {
		super.paintComponent(g);

		Graphics2D g2d = (Graphics2D) g;
		setarHints(g2d);
		g2d.drawImage(backGround, 0, 0, null);
		desenhaContadorVoltas(g2d);
		desenharFarois(g2d);
		desenharClima(g2d);
		desenhaInfoAdd(g2d);
		if (desenhaQualificacao) {
			desenhaQualificacao(g2d);
		} else {
			Piloto pilotoSelecionado = gerenciadorVisual
					.obterPilotoSecionadoTabela(controleJogo
							.getPilotoSelecionado());

			for (int i = controleJogo.getPilotos().size() - 1; i > -1; i--) {
				Piloto piloto = (Piloto) controleJogo.getPilotos().get(i);
				if (piloto.getCarro().isRecolhido()
						|| piloto.getNoAtual() == null) {
					continue;
				}

				g2d.setColor(piloto.getCarro().getCor1());
				g2d.fillOval(piloto.getNoAtual().getX() - 2, piloto
						.getNoAtual().getY() - 2, 8, 8);
				desenhaTipoPneu(piloto, g2d);
				if (piloto != pilotoSelecionado) {
					desenhaNomePilotoNaoSelecionado(piloto, g2d);
				}
			}
			desenharSafetyCar(g2d);
			if ((pilotoSelecionado != null)) {
				desenhaNomePilotoSelecionado(pilotoSelecionado, g2d);
				desenhaCarroSelecionado(pilotoSelecionado, g2d);
				desenhaProblemasCarroSelecionado(pilotoSelecionado, g2d);
			} 
		}
	}

	private void desenhaInfoAdd(Graphics2D g2d) {
		Piloto pilotoSelecionado = gerenciadorVisual
				.obterPilotoSecionadoTabela(controleJogo.getPilotoSelecionado());
		if (pilotoSelecionado != null) {
			g2d.setColor(Color.black);
			int ptoOri = getWidth() - 100;
			int yBase = 0;
			yBase += 15;
			g2d.drawString(
					Lang.msg(pilotoSelecionado.getCarro().getTipoPneu()),
					ptoOri, yBase);
			yBase += 15;
			g2d.drawString(Lang.msg(pilotoSelecionado.getCarro().getAsa()),
					ptoOri, yBase);
			yBase += 15;
			g2d.drawString(Lang.msg("068")
					+ pilotoSelecionado.getQtdeParadasBox(), ptoOri, yBase);
			if (pilotoSelecionado.isBox()) {
				g2d.setColor(red);
			}
			yBase += 15;
			g2d.drawString(Lang.msg("069")
					+ (pilotoSelecionado.isBox() ? Lang.msg("SIM") : Lang
							.msg("NAO")), ptoOri, yBase);
			String plider = "";
			if (pilotoSelecionado.getPosicao() == 1) {
				plider = Lang.msg("Lider");
				g2d.setColor(Color.BLUE);
			} else {
				controleJogo.calculaSegundosParaLider(pilotoSelecionado);
				plider = pilotoSelecionado.getSegundosParaLider();
				g2d.setColor(red);
			}

			yBase += 15;

			g2d.drawString(Lang.msg("070") + plider, getWidth() - 100, yBase);
			yBase += 15;
			if (Carro.GIRO_MIN_VAL == pilotoSelecionado.getCarro().getGiro()
					&& qtdeLuzesAcesas <= 0) {
				g2d.setColor(gre);
				g2d.fillRoundRect(ptoOri - 5, yBase - 12, 90, 16, 10, 10);
				g2d.setColor(Color.black);
			} else {
				g2d.setColor(Color.black);
			}
			g2d.drawString(Lang.msg("071"), ptoOri, yBase);
			yBase += 15;
			if (Carro.GIRO_NOR_VAL == pilotoSelecionado.getCarro().getGiro()
					&& qtdeLuzesAcesas <= 0) {
				g2d.setColor(yel);
				g2d.fillRoundRect(ptoOri - 5, yBase - 12, 90, 16, 10, 10);
				g2d.setColor(Color.black);
			} else {
				g2d.setColor(Color.black);
			}
			g2d.drawString(Lang.msg("072"), ptoOri, yBase);
			yBase += 15;
			if (Carro.GIRO_MAX_VAL == pilotoSelecionado.getCarro().getGiro()
					&& qtdeLuzesAcesas <= 0) {
				g2d.setColor(red);
				g2d.fillRoundRect(ptoOri - 5, yBase - 12, 90, 16, 10, 10);
				g2d.setColor(Color.black);
			} else {
				g2d.setColor(Color.black);
			}
			g2d.drawString(Lang.msg("073"), ptoOri, yBase);
			g2d.setColor(Color.black);
			yBase += 15;
			g2d.drawString(Lang.msg("074"), ptoOri, yBase);
			yBase += 15;
			if (Lang.msg(Piloto.LENTO).equals(
					gerenciadorVisual.getModoPiloto().getSelectedItem())
					&& qtdeLuzesAcesas <= 0
					&& pilotoSelecionado.isJogadorHumano()) {
				g2d.setColor(gre);
				g2d.fillRoundRect(ptoOri - 5, yBase - 12, 90, 16, 10, 10);
				g2d.setColor(Color.black);
			} else {
				g2d.setColor(Color.black);
			}
			g2d.drawString(Lang.msg("075"), ptoOri, yBase);
			yBase += 15;
			if (Lang.msg(Piloto.NORMAL).equals(
					gerenciadorVisual.getModoPiloto().getSelectedItem())
					&& qtdeLuzesAcesas <= 0
					&& pilotoSelecionado.isJogadorHumano()) {
				g2d.setColor(yel);
				g2d.fillRoundRect(ptoOri - 5, yBase - 12, 90, 16, 10, 10);
				g2d.setColor(Color.black);
			} else {
				g2d.setColor(Color.black);
			}
			g2d.drawString(Lang.msg("076"), ptoOri, yBase);
			yBase += 15;
			if (Lang.msg(Piloto.AGRESSIVO).equals(
					gerenciadorVisual.getModoPiloto().getSelectedItem())
					&& qtdeLuzesAcesas <= 0
					&& pilotoSelecionado.isJogadorHumano()) {
				g2d.setColor(red);
				g2d.fillRoundRect(ptoOri - 5, yBase - 12, 90, 16, 10, 10);
				g2d.setColor(Color.black);
			} else {
				g2d.setColor(Color.black);
			}
			g2d.drawString(Lang.msg("077"), ptoOri, yBase);
			yBase += 15;
			g2d.setColor(Color.black);
			g2d.drawString(Lang.msg("078"), ptoOri, yBase);

			if ((pilotoSelecionado.getNumeroVolta() > 0)) {
				Volta voltaPiloto = controleJogo
						.obterMelhorVolta(pilotoSelecionado);

				if (voltaPiloto != null) {
					g2d.setColor(Color.BLUE);
					yBase += 15;
					g2d.drawString(Lang.msg("079")
							+ voltaPiloto.obterTempoVoltaFormatado(), ptoOri,
							yBase);
				}
				g2d.setColor(Color.black);
				yBase += 15;
				g2d.drawString(Lang.msg("080"), ptoOri, yBase);
				yBase += 15;
				int contAlt = yBase;
				int contVolta = 1;
				List voltas = pilotoSelecionado.getVoltas();
				Color color = new Color(1, 1, 1);
				for (int i = voltas.size() - 1; i > -1; i--) {
					Volta volta = (Volta) voltas.get(i);
					if (volta.obterTempoVolta() == 0) {
						continue;
					}
					g2d.setColor(color);
					g2d.drawString(volta.obterTempoVoltaFormatado(), ptoOri,
							contAlt);
					contAlt += 15;
					contVolta++;
					color = new Color(contVolta * 30, contVolta * 30,
							contVolta * 30);
					if (contVolta > 5) {
						break;
					}

				}
			}

		}

	}

	private void desenhaProblemasCarroSelecionado(Piloto pilotoSelecionado,
			Graphics2D g2d) {
		if (qtdeLuzesAcesas > 0) {
			return;
		}
		String dano = pilotoSelecionado.getCarro().getDanificado();
		int pneus = pilotoSelecionado.getCarro().porcentagemDesgastePeneus();
		int porcentComb = pilotoSelecionado.getCarro().porcentagemCombustivel();
		int motor = pilotoSelecionado.getCarro().porcentagemDesgasteMotor();
		if ((dano == null || "".equals(dano)) && motor > 10 && porcentComb > 10
				&& pneus > 10)
			return;
		BufferedImage carroimg = CarregadorRecursos
				.carregaImgCarro(pilotoSelecionado.getCarro().getImg());
		g2d.drawImage(carroimg, 5, 10, null);
		if (Math.random() > .5) {
			return;
		}
		if (porcentComb <= 10) {
			g2d.drawImage(fuel.getImage(), 5, 240, null);
		}

		if (Carro.PERDEU_AEREOFOLIO.equals(pilotoSelecionado.getCarro()
				.getDanificado())) {
			g2d.setColor(Color.red);
			// bico
			g2d.fillOval(10, 26, 15, 15);
		}
		if (Carro.PNEU_FURADO.equals(pilotoSelecionado.getCarro()
				.getDanificado())) {
			g2d.setColor(Color.red);
			// Roda diantera
			g2d.fillOval(32, 28, 15, 15);
			// Roda trazeira
			g2d.fillOval(128, 28, 15, 15);
		} else if (pneus <= 10) {
			g2d.setColor(yel);
			// Roda diantera
			g2d.fillOval(32, 28, 15, 15);
			// Roda trazeira
			g2d.fillOval(128, 28, 15, 15);
		}
		if (Carro.EXPLODIU_MOTOR.equals(pilotoSelecionado.getCarro()
				.getDanificado())) {
			g2d.setColor(Color.red);
			// motor
			g2d.fillOval(98, 12, 15, 15);
		} else if (motor <= 10) {
			g2d.setColor(yel);
			g2d.fillOval(98, 12, 15, 15);
		}
		if (Carro.BATEU_FORTE.equals(pilotoSelecionado.getCarro()
				.getDanificado())) {
			g2d.setColor(Color.red);
			// motor
			g2d.fillRoundRect(15, 18, 135, 20, 15, 15);
		}

	}

	private void desenhaContadorVoltas(Graphics2D g2d) {
		g2d.setColor(luzApagada);
		g2d.fillRoundRect(getWidth() / 2, 10, 40, 20, 15, 15);
		g2d.setColor(Color.BLACK);
		g2d.drawString(controleJogo.getNumVoltaAtual() + "/"
				+ controleJogo.totalVoltasCorrida(), (getWidth() / 2) + 6, 24);
	}

	private void desenhaQualificacao(Graphics2D g2d) {
		BufferedImage carroimg = CarregadorRecursos
				.carregaImgCarro(pilotQualificacao.getCarro().getImg());
		g2d.drawImage(carroimg, null, pointQualificacao.x, pointQualificacao.y);
		synchronized (mapDesenharQualificacao) {
			for (Iterator iter = mapDesenharQualificacao.keySet().iterator(); iter
					.hasNext();) {
				Piloto piloto = (Piloto) iter.next();
				Point point = (Point) mapDesenharQualificacao.get(piloto);
				carroimg = CarregadorRecursos
						.carregaBufferedImageTranspareciaBranca(piloto
								.getCarro().getImg());
				g2d.drawImage(carroimg, null, point.x, point.y);
				String txt = piloto.getNome()
						+ " - "
						+ ControleEstatisticas.formatarTempo(piloto
								.getCiclosVoltaQualificacao(), controleJogo
								.getTempoCiclo());

				int maior = txt.length();

				Color c2 = piloto.getCarro().getCor2();
				if (c2 != null) {
					c2 = c2.brighter();
					g2d.setColor(new Color(c2.getRed(), c2.getGreen(), c2
							.getBlue(), 170));
				}
				Point pt = null;
				if (piloto.getPosicao() % 2 == 0) {
					pt = new Point(point.x + 120, point.y + 20);

				} else {
					pt = new Point(point.x - 120, point.y + 20);
				}
				g2d.fillRoundRect(pt.x - 10, pt.y - 15, maior * 7, 20, 15, 15);

				int valor = (c2.getRed() + c2.getGreen() + c2.getBlue()) / 2;
				if (valor > 200) {
					g2d.setColor(Color.BLACK);
				} else {
					g2d.setColor(Color.WHITE);
				}
				g2d.drawString(txt, pt.x, pt.y);

			}
		}
	}

	private void desenhaCarroSelecionado(Piloto psel, Graphics2D g2d) {
		BufferedImage carroimg = CarregadorRecursos.carregaImgCarro(psel
				.getCarro().getImg());
		int carSelX = getWidth() / 2 - carroimg.getWidth() / 2;
		int carSelY = getHeight() - 35;
		int bounce = calculaBounce(psel.getCarro());

		if (Math.random() > 0.5) {
			carSelX += bounce;
		} else {
			carSelX -= bounce;
		}
		g2d.drawImage(carroimg, null, carSelX, carSelY);
		Carro carroFrente = controleJogo.obterCarroNaFrente(psel);
		if (carroFrente != null) {
			carroimg = CarregadorRecursos.carregaImgCarro(carroFrente.getImg());
			carSelX = carroimg.getWidth() / 2;
			bounce = calculaBounce(carroFrente);
			if (Math.random() > 0.5) {
				carSelX += bounce;
			} else {
				carSelX -= bounce;
			}
			double diff = controleJogo.calculaSegundosParaProximoDouble(psel);
			int dstX = getWidth() / 4;
			int dstY = carSelY + 20;
			if (diff >= 3) {
				g2d.setColor(gre);
			} else if (diff < 3 && diff > 1) {
				g2d.setColor(yel);
			} else if (diff <= 1) {
				g2d.setColor(red);
			}

			if (diff < 3 && diff > 2.5) {
				carSelX += 30;
				dstX += 25;
			} else if (diff <= 2.5 && diff > 1) {
				carSelX += 50;
				dstX += 45;
			} else if (diff <= 1 && diff > .5) {
				carSelX += 90;
				dstX += 75;
			} else if (diff <= .5) {
				carSelX += 120;
				dstX += 90;
			}

			g2d.drawImage(carroimg, null, carSelX, carSelY);
			g2d.fillRoundRect(dstX - 2, dstY - 12, 60, 15, 10, 10);
			if (diff >= 3) {
				g2d.setColor(Color.BLACK);
			} else if (diff < 3 && diff > 1) {
				g2d.setColor(Color.BLACK);
			} else if (diff <= 1) {
				g2d.setColor(Color.WHITE);
			}
			String val = controleJogo.calculaSegundosParaProximo(psel);
			if (val != null) {
				g2d.drawString("  " + val, dstX, dstY);
			}

		}
		Carro carroAtraz = controleJogo.obterCarroAtraz(psel);
		if (carroAtraz != null) {
			carroimg = CarregadorRecursos.carregaImgCarro(carroAtraz.getImg());
			carSelX = getWidth() - carroimg.getWidth() - carroimg.getWidth()
					/ 2;
			bounce = calculaBounce(carroAtraz);
			if (Math.random() > 0.5) {
				carSelX += bounce;
			} else {
				carSelX -= bounce;
			}
			int dstX = getWidth() - getWidth() / 3;
			int dstY = carSelY + 20;
			double diff = controleJogo
					.calculaSegundosParaProximoDouble(carroAtraz.getPiloto());

			if (diff >= 3) {
				g2d.setColor(gre);
			} else if (diff < 3 && diff > 1) {
				g2d.setColor(yel);
			} else if (diff <= 1) {
				g2d.setColor(red);
			}

			if (diff < 3 && diff > 2.5) {
				carSelX -= 30;
				dstX -= 20;
			} else if (diff <= 2.5 && diff > 1) {
				carSelX -= 50;
				dstX -= 40;
			} else if (diff <= 1 && diff > .5) {
				carSelX -= 70;
				dstX -= 60;
			} else if (diff <= .5) {
				carSelX -= 110;
				dstX -= 70;
			}
			g2d.drawImage(carroimg, null, carSelX, carSelY);
			g2d.fillRoundRect(dstX - 2, dstY - 12, 60, 15, 10, 10);
			if (diff >= 3) {
				g2d.setColor(Color.BLACK);
			} else if (diff < 3 && diff > 1) {
				g2d.setColor(Color.BLACK);
			} else if (diff <= 1) {
				g2d.setColor(Color.WHITE);
			}
			String val = controleJogo.calculaSegundosParaProximo(carroAtraz
					.getPiloto());
			if (val != null) {
				g2d.drawString("  " + val, dstX, dstY);
			}
		}

	}

	private int calculaBounce(Carro carro) {
		if (carro.getPiloto().isAgressivo() == false) {
			return 1;
		} else if (carro.getPiloto().isAgressivo() == true
				&& carro.getGiro() != Carro.GIRO_MAX_VAL) {
			return 2;
		} else if (carro.getPiloto().isAgressivo() == true
				&& carro.getGiro() == Carro.GIRO_MAX_VAL) {
			return 3;
		}
		return 0;
	}

	private void desenharSafetyCar(Graphics2D g2d) {
		if (controleJogo.isSafetyCarNaPista()) {
			SafetyCar safetyCar = controleJogo.getSafetyCar();
			if (safetyCar == null) {
				return;
			}
			if (safetyCar.getNoAtual() == null) {
				return;
			}
			g2d.setColor(Color.LIGHT_GRAY);
			g2d.fillOval(safetyCar.getNoAtual().getX() - 2, safetyCar
					.getNoAtual().getY() - 2, 8, 8);
			if (!safetyCar.isVaiProBox()) {
				if (Math.random() > .5) {
					g2d.setColor(Color.YELLOW);
				} else {
					g2d.setColor(Color.BLACK);
				}
			} else
				g2d.setColor(Color.BLACK);
			g2d.drawOval(safetyCar.getNoAtual().getX() - 2, safetyCar
					.getNoAtual().getY() - 2, 8, 8);

		}
	}

	private void desenharClima(Graphics2D g2d) {

		ImageIcon icon = (ImageIcon) gerenciadorVisual.getImgClima().getIcon();
		if (icon != null && pointDesenhaClima != null)
			g2d.drawImage(icon.getImage(), pointDesenhaClima.x,
					pointDesenhaClima.y, null);

	}

	public Point getPointDesenhaClima() {
		return pointDesenhaClima;
	}

	public void setPointDesenhaClima(Point pointDesenhaClima) {
		this.pointDesenhaClima = pointDesenhaClima;
	}

	private void desenharFarois(Graphics2D g2d) {

		if (qtdeLuzesAcesas <= 0) {
			return;
		}
		int xIni = 5;
		int yIni = 5;
		/**
		 * 1ª luz
		 */
		g2d.setColor(farol);
		g2d.fillRoundRect(xIni, yIni, 20, 50, 15, 15);
		g2d.setColor(luzApagada);
		g2d.fillOval(xIni + 3, yIni + 5, 14, 14);
		if (qtdeLuzesAcesas > 0) {
			g2d.setColor(luzAcesa);
		} else {
			g2d.setColor(luzApagada);
		}
		g2d.fillOval(xIni + 3, yIni + 30, 14, 14);
		xIni += 25;
		g2d.setColor(farol);
		g2d.fillRoundRect(xIni, yIni, 20, 50, 15, 15);
		g2d.setColor(luzApagada);
		g2d.fillOval(xIni + 3, yIni + 5, 14, 14);
		if (qtdeLuzesAcesas > 1) {
			g2d.setColor(luzAcesa);
		} else {
			g2d.setColor(luzApagada);
		}
		g2d.fillOval(xIni + 3, yIni + 30, 14, 14);
		xIni += 25;
		g2d.setColor(farol);
		g2d.fillRoundRect(xIni, yIni, 20, 50, 15, 15);
		g2d.setColor(luzApagada);
		g2d.fillOval(xIni + 3, yIni + 5, 14, 14);
		if (qtdeLuzesAcesas > 2) {
			g2d.setColor(luzAcesa);
		} else {
			g2d.setColor(luzApagada);
		}
		g2d.fillOval(xIni + 3, yIni + 30, 14, 14);
		xIni += 25;
		g2d.setColor(farol);
		g2d.fillRoundRect(xIni, yIni, 20, 50, 15, 15);
		g2d.setColor(luzApagada);
		g2d.fillOval(xIni + 3, yIni + 5, 14, 14);
		if (qtdeLuzesAcesas > 3) {
			g2d.setColor(luzAcesa);
		} else {
			g2d.setColor(luzApagada);
		}
		g2d.fillOval(xIni + 3, yIni + 30, 14, 14);
		xIni += 25;
		g2d.setColor(farol);
		g2d.fillRoundRect(xIni, yIni, 20, 50, 15, 15);
		g2d.setColor(luzApagada);
		g2d.fillOval(xIni + 3, yIni + 5, 14, 14);
		if (qtdeLuzesAcesas > 4) {
			g2d.setColor(luzAcesa);
		} else {
			g2d.setColor(luzApagada);
		}
		g2d.fillOval(xIni + 3, yIni + 30, 14, 14);
	}

	private void desenhaTipoPneu(Piloto piloto, Graphics g2d) {
		if (Carro.TIPO_PNEU_MOLE.equals(piloto.getCarro().getTipoPneu())) {
			if (Math.random() > .5)
				g2d.setColor(Color.GRAY);
			else
				g2d.setColor(Color.DARK_GRAY);
		} else {
			if (Math.random() > .5)
				g2d.setColor(Color.DARK_GRAY);
			else
				g2d.setColor(Color.BLACK);

		}

		g2d.drawOval(piloto.getNoAtual().getX() - 2,
				piloto.getNoAtual().getY() - 2, 8, 8);

	}

	private void desenhaNomePilotoSelecionado(Piloto ps, Graphics2D g2d) {
		if (ps == null)
			return;
		if (ps.getNoAtual() == null)
			return;
		if (ps.getCarro() == null)
			return;
		String txt1 = ps.getNome() + "-" + ps.getCarro().getNome();

		String dano = ((ps.getCarro().getDanificado() == null) ? "" : Lang
				.msg(ps.getCarro().getDanificado()));

		String agressivo = (ps.isAgressivo() ? Lang.msg("AGRESSIVO") : Lang
				.msg("NORMAL"));

		String intel = (ps.isJogadorHumano() ? ps.getNomeJogador() : "IA");
		String txt2 = intel + " " + agressivo + " " + dano;
		String velo = "~" + ps.getVelocidade() + " Km/h";

		int maior = 0;
		if (txt1.length() > maior) {
			maior = txt1.length();
		}
		if (txt2.length() > maior) {
			maior = txt2.length();
		}
		Color c2 = ps.getCarro().getCor2();
		Color c1 = ps.getCarro().getCor1();
		if (c2 != null) {
			c2 = c2.brighter();
			g2d.setColor(new Color(c2.getRed(), c2.getGreen(), c2.getBlue(),
					200));
		}

		Point pt = ps.getNoAtual().getPoint();
		int largura = maior * 7;
		/**
		 * moldura superior
		 */
		g2d.fillRoundRect((pt.x + 14), pt.y - 25, largura, 25, 15, 15);
		/**
		 * moldura inferior
		 */
		if (desenhaPosVelo) {

			if (Carro.GIRO_MIN_VAL == ps.getCarro().getGiro()) {
				desenBarraGiro(g2d, true, gre, 5);
				g2d.setColor(Color.BLACK);
				g2d.drawString(Lang.msg("Min"), 9, 195);
			}
			if (Carro.GIRO_NOR_VAL == ps.getCarro().getGiro()) {
				desenBarraGiro(g2d, false, gre, 5);
				desenBarraGiro(g2d, true, yel, 35);
				g2d.setColor(Color.BLACK);
				g2d.drawString(Lang.msg("Nor"), 39, 195);

			}
			if (Carro.GIRO_MAX_VAL == ps.getCarro().getGiro()) {
				desenBarraGiro(g2d, false, gre, 5);
				desenBarraGiro(g2d, false, yel, 35);
				desenBarraGiro(g2d, true, red, 65);
				g2d.setColor(Color.BLACK);
				g2d.drawString(Lang.msg("Max"), 69, 195);
			}
		}
		if (ps.isBox()) {

			g2d.drawImage(fuel.getImage(), 5, 240, null);
			g2d.setColor(Color.BLACK);
			Integer percent = ps.getQtdeCombustBox();
			if (percent != null && ps.isJogadorHumano())
				g2d.drawString(percent + "%", 5, 280);
			g2d.drawImage(tyre.getImage(), 5, 285, null);
			String tpPneu = ps.getTipoPneuBox();
			if (tpPneu != null && ps.isJogadorHumano())
				g2d.drawString(Lang.msg(tpPneu), 5, 325);
		}
		int valor = (c2.getRed() + c2.getGreen() + c2.getBlue()) / 2;
		if (valor > 200) {
			g2d.setColor(Color.BLACK);
		} else {
			g2d.setColor(Color.WHITE);
		}
		int xTxt = (pt.x + 15);
		if (txt1 != null)
			g2d.drawString(txt1, xTxt + 2, pt.y - 13);
		if (txt2 != null)
			g2d.drawString(txt2, xTxt + 2, pt.y - 3);
		if (desenhaPosVelo) {
			g2d.setColor(c1);
			g2d.fillRoundRect(5, 100, 70, 15, 15, 15);
			valor = (c1.getRed() + c1.getGreen() + c1.getBlue()) / 2;
			if (valor > 200) {
				g2d.setColor(Color.BLACK);
			} else {
				g2d.setColor(Color.WHITE);
			}
			if (velo != null)
				g2d.drawString(velo, 10, 112);
		}

		g2d.setColor(Color.BLACK);
		g2d.drawLine(pt.x + 4, pt.y, pt.x + 13, pt.y - 10);

	}

	private void desenBarraGiro(Graphics g2d, boolean varia, Color cor,
			int inico) {
		g2d.setColor(cor);
		int incremetAlt = 0;
		if (gre.equals(cor)) {
			incremetAlt = 10;
		} else if (yel.equals(cor)) {
			incremetAlt = 28;
		} else if (red.equals(cor)) {
			incremetAlt = 46;
		}
		int y = 200;
		g2d.fillRoundRect(inico, y - incremetAlt, 4, incremetAlt, 15, 15);
		incremetAlt += 3;
		g2d.fillRoundRect(inico + 5, y - incremetAlt, 4, incremetAlt, 15, 15);
		incremetAlt += 3;
		g2d.fillRoundRect(inico + 10, y - incremetAlt, 4, incremetAlt, 15, 15);
		incremetAlt += 3;
		if (varia) {
			int val = 1 + (int) (Math.random() * 3);
			switch (val) {
			case 1:
				g2d.fillRoundRect(inico + 15, y - incremetAlt, 4, incremetAlt,
						15, 15);
				incremetAlt += 3;
				break;
			case 2:
				g2d.fillRoundRect(inico + 15, y - incremetAlt, 4, incremetAlt,
						15, 15);
				incremetAlt += 3;
				g2d.fillRoundRect(inico + 20, y - incremetAlt, 4, incremetAlt,
						15, 15);
				incremetAlt += 3;
				break;
			case 3:
				g2d.fillRoundRect(inico + 15, y - incremetAlt, 4, incremetAlt,
						15, 15);
				incremetAlt += 3;
				g2d.fillRoundRect(inico + 20, y - incremetAlt, 4, incremetAlt,
						15, 15);
				incremetAlt += 3;
				g2d.fillRoundRect(inico + 25, y - incremetAlt, 4, incremetAlt,
						15, 15);
				incremetAlt += 3;
				break;
			default:
				break;
			}
		} else {
			g2d.fillRoundRect(inico + 15, y - incremetAlt, 4, incremetAlt, 15,
					15);
			incremetAlt += 3;
			g2d.fillRoundRect(inico + 20, y - incremetAlt, 4, incremetAlt, 15,
					15);
			incremetAlt += 3;
			g2d.fillRoundRect(inico + 25, y - incremetAlt, 4, incremetAlt, 15,
					15);
			incremetAlt += 3;
		}

	}

	private void desenhaNomePilotoNaoSelecionado(Piloto piloto, Graphics g2d) {
		g2d.setColor(Color.DARK_GRAY);
		if (piloto.getPosicao() % 2 == 0) {
			g2d.drawString(piloto.getNome(), piloto.getNoAtual().getX() - 2,
					piloto.getNoAtual().getY() - 3);
		} else {
			g2d.drawString(piloto.getNome(), piloto.getNoAtual().getX() - 2,
					piloto.getNoAtual().getY() + 17);
		}

	}

	private void setarHints(Graphics2D g2d) {
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setRenderingHint(RenderingHints.KEY_RENDERING,
				RenderingHints.VALUE_RENDER_QUALITY);
		g2d.setRenderingHint(RenderingHints.KEY_DITHERING,
				RenderingHints.VALUE_DITHER_ENABLE);
		g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
				RenderingHints.VALUE_INTERPOLATION_BILINEAR);
	}

	public Dimension getPreferredSize() {
		return new Dimension(backGround.getWidth(), backGround.getHeight());
	}

	public Dimension getMinimumSize() {
		return super.getPreferredSize();
	}

	public void apagarLuz() {
		qtdeLuzesAcesas--;
	}

	public void definirDesenhoQualificacao(Piloto piloto, Point point) {
		this.pilotQualificacao = piloto;
		this.pointQualificacao = point;

	}

}
