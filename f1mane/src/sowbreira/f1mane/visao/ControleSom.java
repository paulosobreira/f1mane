package sowbreira.f1mane.visao;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

import br.nnpe.Constantes;
import br.nnpe.Logger;
import br.nnpe.Util;
import sowbreira.f1mane.controles.InterfaceJogo;
import sowbreira.f1mane.entidades.No;
import sowbreira.f1mane.entidades.Piloto;
import sowbreira.f1mane.recursos.CarregadorRecursos;

public class ControleSom {

	private static Clip clipLargada;
	private static Clip clipReducao;
	private static Clip clipAceleracao;
	private static Clip clipReducao1;
	private static Clip clipAceleracao1;
	private static Clip clipReducao2;
	private static Clip clipAceleracao2;
	public static boolean somLigado = true;
	private static Clip clipBox;
	private static Clip clipSafetyCar;
	private static Clip clipPararBox;
	private static float volume = -15f;
	private static Piloto psAnt;
	private static No noAnterior;

	public static void main(String[] args)
			throws InterruptedException, UnsupportedAudioFileException,
			IOException, LineUnavailableException {
		iniciaVars();
		final int l1 = clipLargada.getFrameLength();
		final int l4 = clipReducao1.getFrameLength();
		final int l5 = clipAceleracao1.getFrameLength();
		Thread thread = new Thread() {
			public void run() {
				while (true) {
					try {
						Thread.sleep(30);
					} catch (InterruptedException e) {
						Logger.logarExept(e);
					}
					/*
					 * if (0 == clipReducao.getFramePosition() &&
					 * !clipReducao.isActive()) { clipReducao.start();
					 * System.out.println("clipRedo.start();");
					 * System.out.println(clipReducao.getFrameLength()); } if (0
					 * == clipAceleracao.getFramePosition() &&
					 * !clipAceleracao.isActive() &&
					 * clipReducao.getFramePosition() > 202000) {
					 * clipAceleracao.start();
					 * System.out.println("clipAcel.start();");
					 * System.out.println(clipAceleracao.getFrameLength()); }
					 */

					if (0 == clipAceleracao1.getFramePosition()
							&& !clipAceleracao1.isActive()) {
						clipAceleracao1.start();
						System.out.println("clipAcel.start();");
						System.out.println(clipAceleracao1.getFrameLength());
					}
					if (0 == clipReducao1.getFramePosition()
							&& !clipReducao1.isActive()
							&& clipAceleracao1.getFramePosition() > 700000) {
						clipReducao1.start();
						clipAceleracao1.stop();
						System.out.println("clipRedo.start();");
						System.out.println(clipReducao1.getFrameLength());
					}
				}
			};
		};
		thread.start();
	}

	public static void somVelocidade(Piloto ps, InterfaceJogo controleJogo,
			PainelCircuito painelCircuito) {
		if (!clipLargada.isRunning()
				&& painelCircuito.getQtdeLuzesAcesas() > 0) {
			clipLargada.start();
		}
		if (clipLargada.isRunning()
				&& painelCircuito.getQtdeLuzesAcesas() <= 0) {
			clipAceleracao.start();
			clipLargada.stop();
		}
		try {
			if (ps == null) {
				return;
			}
			if (painelCircuito.getQtdeLuzesAcesas() > 0) {
				return;
			}
			if (ps.isRecebeuBanderada()) {
				if (!clipBox.isRunning()) {
					paraTudo();
					clipBox.loop(Clip.LOOP_CONTINUOUSLY);
					clipBox.start();
				}
				return;
			}
			if (nadaTocando()) {
				if (ps.getPtosBox() != 0) {
					clipBox.setFramePosition(0);
					clipBox.start();
					return;
				}
				if (ps.getNoAtual().verificaRetaOuLargada()) {
					clipAceleracao.setFramePosition(0);
					clipAceleracao.start();
				}
				if (ps.getNoAtual().verificaCruvaBaixa()) {
					clipReducao = clipReducao1;
					clipReducao.setFramePosition(0);
					clipReducao.start();
				}
				if (ps.getNoAtual().verificaCruvaAlta()) {
					clipReducao = clipReducao2;
					clipReducao.setFramePosition(0);
					clipReducao.start();
				}
			}
			if (!ps.equals(psAnt)) {
				psAnt = ps;
				noAnterior = ps.getNoAtual();
				return;
			}
			if (ps.getPtosBox() != 0) {
				clipReducao.stop();
				clipAceleracao.stop();
				if (!clipBox.isRunning() && ps.getVelocidade() != 0) {
					clipBox.setFramePosition(0);
					clipBox.start();
				}
				if ((ps.getVelocidade() == 0) && !clipPararBox.isRunning()) {
					clipBox.stop();
					clipPararBox.setFramePosition(0);
					clipPararBox.start();
				}
				if (clipPararBox.isRunning() && ps.getVelocidade() != 0) {
					clipPararBox.stop();
					clipBox.setFramePosition(Util.intervalo(500, 2000));
					clipBox.start();
				}
			} else {
				if (controleJogo.isSafetyCarNaPista()) {
					if (!clipSafetyCar.isRunning()) {
						paraTudo();
						clipSafetyCar.loop(Clip.LOOP_CONTINUOUSLY);
						clipSafetyCar.start();
					}
					return;
				} else {
					if (clipSafetyCar.isRunning()) {
						clipSafetyCar.stop();
						clipAceleracao.setFramePosition(0);
						clipAceleracao.start();
					}
				}
				if (clipBox.isRunning()) {
					clipBox.stop();
					clipAceleracao.setFramePosition(0);
					clipAceleracao.start();
				}
				if (!clipAceleracao.isRunning()
						&& (!noAnterior.verificaRetaOuLargada()
								|| noAnterior.isBox())
						&& ps.getNoAtual().verificaRetaOuLargada()) {
					clipReducao.stop();
					clipAceleracao.stop();
					clipAceleracao = Math.random() > .5
							? clipAceleracao1
							: clipAceleracao2;
					clipAceleracao.setFramePosition(0);
					clipAceleracao.start();
				}
				if (!clipReducao.isRunning()
						&& noAnterior.verificaRetaOuLargada()
						&& !ps.getNoAtual().verificaRetaOuLargada()) {
					clipBox.stop();
					clipAceleracao.stop();
					clipReducao.stop();
					if (ps.getNoAtual().verificaCruvaBaixa()) {
						clipReducao = clipReducao1;
					}
					if (ps.getNoAtual().verificaCruvaAlta()) {
						clipReducao = clipReducao2;
					}
					clipReducao.setFramePosition(0);
					clipReducao.start();
				}
			}
			noAnterior = ps.getNoAtual();
		} catch (Exception e) {
			Logger.logarExept(e);
		}
	}

	private static void iniciaVars() throws UnsupportedAudioFileException,
			IOException, LineUnavailableException {
		if (clipBox == null) {
			InputStream bufferedIn = new BufferedInputStream(
					CarregadorRecursos.recursoComoStream("entroubox.wav"));
			AudioInputStream box = AudioSystem.getAudioInputStream(bufferedIn);
			DataLine.Info info = new DataLine.Info(Clip.class, box.getFormat());
			clipBox = (Clip) AudioSystem.getLine(info);
			clipBox.open(box);
			FloatControl gainControl = (FloatControl) clipBox
					.getControl(FloatControl.Type.MASTER_GAIN);
			gainControl.setValue(volume);

		}
		if (clipSafetyCar == null) {
			InputStream bufferedIn = new BufferedInputStream(
					CarregadorRecursos.recursoComoStream("entroubox.wav"));
			AudioInputStream box = AudioSystem.getAudioInputStream(bufferedIn);
			DataLine.Info info = new DataLine.Info(Clip.class, box.getFormat());
			clipSafetyCar = (Clip) AudioSystem.getLine(info);
			clipSafetyCar.open(box);
			FloatControl gainControl = (FloatControl) clipSafetyCar
					.getControl(FloatControl.Type.MASTER_GAIN);
			gainControl.setValue(volume);

		}
		if (clipPararBox == null) {
			InputStream bufferedIn = new BufferedInputStream(
					CarregadorRecursos.recursoComoStream("pararbox.wav"));
			AudioInputStream box = AudioSystem.getAudioInputStream(bufferedIn);
			DataLine.Info info = new DataLine.Info(Clip.class, box.getFormat());
			clipPararBox = (Clip) AudioSystem.getLine(info);
			clipPararBox.open(box);
			FloatControl gainControl = (FloatControl) clipPararBox
					.getControl(FloatControl.Type.MASTER_GAIN);
			gainControl.setValue(volume);

		}
		if (clipLargada == null) {
			InputStream bufferedIn = new BufferedInputStream(
					CarregadorRecursos.recursoComoStream("largada.wav"));
			AudioInputStream veloBaixa = AudioSystem
					.getAudioInputStream(bufferedIn);
			DataLine.Info info = new DataLine.Info(Clip.class,
					veloBaixa.getFormat());
			clipLargada = (Clip) AudioSystem.getLine(info);
			clipLargada.open(veloBaixa);
			FloatControl gainControl = (FloatControl) clipLargada
					.getControl(FloatControl.Type.MASTER_GAIN);
			gainControl.setValue(volume);
		}
		if (clipReducao1 == null) {
			InputStream bufferedIn = new BufferedInputStream(
					CarregadorRecursos.recursoComoStream("reducao1.wav"));
			AudioInputStream redo = AudioSystem.getAudioInputStream(bufferedIn);
			DataLine.Info info = new DataLine.Info(Clip.class,
					redo.getFormat());
			clipReducao1 = (Clip) AudioSystem.getLine(info);
			clipReducao1.open(redo);
			FloatControl gainControl = (FloatControl) clipReducao1
					.getControl(FloatControl.Type.MASTER_GAIN);
			gainControl.setValue(volume);
		}
		if (clipAceleracao1 == null) {
			InputStream bufferedIn = new BufferedInputStream(
					CarregadorRecursos.recursoComoStream("aceleracao1.wav"));
			AudioInputStream acel = AudioSystem.getAudioInputStream(bufferedIn);
			DataLine.Info info = new DataLine.Info(Clip.class,
					acel.getFormat());
			clipAceleracao1 = (Clip) AudioSystem.getLine(info);
			clipAceleracao1.open(acel);
			FloatControl gainControl = (FloatControl) clipAceleracao1
					.getControl(FloatControl.Type.MASTER_GAIN);
			gainControl.setValue(volume);
		}
		if (clipReducao2 == null) {
			InputStream bufferedIn = new BufferedInputStream(
					CarregadorRecursos.recursoComoStream("reducao2.wav"));
			AudioInputStream redo = AudioSystem.getAudioInputStream(bufferedIn);
			DataLine.Info info = new DataLine.Info(Clip.class,
					redo.getFormat());
			clipReducao2 = (Clip) AudioSystem.getLine(info);
			clipReducao2.open(redo);
			FloatControl gainControl = (FloatControl) clipReducao2
					.getControl(FloatControl.Type.MASTER_GAIN);
			gainControl.setValue(volume);
		}
		if (clipAceleracao2 == null) {
			InputStream bufferedIn = new BufferedInputStream(
					CarregadorRecursos.recursoComoStream("aceleracao2.wav"));
			AudioInputStream acel = AudioSystem.getAudioInputStream(bufferedIn);
			DataLine.Info info = new DataLine.Info(Clip.class,
					acel.getFormat());
			clipAceleracao2 = (Clip) AudioSystem.getLine(info);
			clipAceleracao2.open(acel);
			FloatControl gainControl = (FloatControl) clipAceleracao2
					.getControl(FloatControl.Type.MASTER_GAIN);
			gainControl.setValue(volume);
		}
		if (clipAceleracao == null) {
			clipAceleracao = Math.random() > .5
					? clipAceleracao1
					: clipAceleracao2;
		}
		if (clipReducao == null) {
			clipReducao = Math.random() > .5 ? clipReducao1 : clipReducao2;
		}
	}

	public static void processaSom(Piloto pilotoSelecionado,
			InterfaceJogo controleJogo, PainelCircuito painelCircuito) {
		try {
			if (!somLigado) {
				return;
			}
			if (!controleJogo.getMainFrame().isVisible()) {
				return;
			}
			if (controleJogo.isJogoPausado()) {
				paraTudo();
				return;
			}
			iniciaVars();
			somVelocidade(pilotoSelecionado, controleJogo, painelCircuito);
		} catch (Exception e) {
			Logger.logarExept(e);
			paraTudo();
		}
	}

	public static void ligaDesligaSom() {
		somLigado = !somLigado;
		if (!somLigado) {
			paraTudo();
		}

	}

	public static boolean nadaTocando() {
		return !clipLargada.isRunning() && !clipReducao.isRunning()
				&& !clipAceleracao.isRunning() && !clipBox.isRunning()
				&& !clipPararBox.isRunning();
	}

	public static void paraTudo() {
		if (clipLargada != null) {
			clipLargada.stop();
		}
		if (clipReducao != null) {
			clipReducao.stop();
		}
		if (clipAceleracao != null) {
			clipAceleracao.stop();
		}
		if (clipBox != null) {
			clipBox.stop();
		}
		if (clipPararBox != null) {
			clipPararBox.stop();
		}
	}

}
