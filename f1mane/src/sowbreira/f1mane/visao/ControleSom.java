package sowbreira.f1mane.visao;

import java.io.IOException;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

import sowbreira.f1mane.controles.InterfaceJogo;
import sowbreira.f1mane.entidades.Piloto;
import sowbreira.f1mane.recursos.CarregadorRecursos;
import br.nnpe.Logger;
import br.nnpe.Util;

public class ControleSom {

	private static Clip clipVeloMax;
	private static Clip clipLargada;
	private static Clip clipVeloMed;
	private static Clip roncoClip;
	private static Clip clipRedo;
	private static Clip clipAcel;
	private static long lastCall;
	public static boolean somLigado = false;
	private static Clip clipVeloMaxFinal;
	private static Clip clipBox;
	private static float volume = -20f;

	public static void main(String[] args) throws InterruptedException,
			UnsupportedAudioFileException, IOException,
			LineUnavailableException {
		iniciaVars();
		final double frameLength = (double) clipVeloMax.getFrameLength();
		System.out.println("frameLength" + frameLength);
		Thread thread = new Thread() {
			public void run() {
				while (true) {
					try {
						Thread.sleep(10);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					System.out
							.println((clipVeloMax.getFramePosition() / frameLength));
				}
			};
		};
		thread.start();
		clipVeloMax.start();
	}

	public static void somVelocidade(Piloto ps, InterfaceJogo controleJogo,
			PainelCircuito painelCircuito) {
		int velocidade = ps.getVelocidade();
		if (lastCall == 0) {
			lastCall = System.currentTimeMillis();
		} else if (lastCall + (20 + controleJogo.getTempoCiclo()) > System
				.currentTimeMillis()) {
			return;
		}
		if (!roncoClip.isRunning() && velocidade == 0
				&& painelCircuito.getQtdeLuzesAcesas() < 5) {
			roncoClip.loop(2);
			roncoClip.start();
		}
		if (painelCircuito.getQtdeLuzesAcesas() <= 0) {
			roncoClip.stop();
		}
		try {
			if (ps.getPtosBox() != 0) {
				clipBox.loop(Clip.LOOP_CONTINUOUSLY);
				clipBox.start();
			} else {
				clipBox.stop();
			}
			if (!tocandoClip() && painelCircuito.getQtdeLuzesAcesas() <= 0
					&& velocidade > 0 && velocidade <= 60) {
				clipLargada.setFramePosition(0);
				clipLargada.start();
			} else if (!tocandoClip() && velocidade > 60) {
				int inter = Util.intervalo(0, 2);
				if (inter == 0 && Math.random() > 0.5) {
					inter = Util.intervalo(1, 2);
				}
				switch (inter) {
				case 0:
					clipVeloMed.setFramePosition(0);
					clipVeloMed.start();
					break;
				case 1:
					clipVeloMax.setFramePosition(0);
					clipVeloMax.start();
					break;
				case 2:
					clipVeloMaxFinal.setFramePosition(0);
					clipVeloMaxFinal.start();
					break;

				default:
					break;
				}

			}

			int diffVelo = (ps.getVelocidade() - ps.getVelocidadeAnterior());
			if (Math.random() > 0.5 && !tocandoClip() && !clipAcel.isRunning()
					&& ps.getPtosBox() == 0
					&& painelCircuito.getQtdeLuzesAcesas() <= 0
					&& diffVelo > 40) {
				clipAcel.setFramePosition(0);
				clipAcel.start();
			}
			if (Math.random() > 0.5 && !tocandoClip() && !clipRedo.isRunning()
					&& ps.getPtosBox() == 0
					&& painelCircuito.getQtdeLuzesAcesas() <= 0
					&& diffVelo < -40) {
				clipRedo.setFramePosition(0);
				clipRedo.start();
			}
			if (ps.getVelocidade() != 1)
				ps.setVelocidadeAnterior(ps.getVelocidade());
		} catch (Exception e) {
			Logger.logarExept(e);
		}
		lastCall = System.currentTimeMillis();
	}

	private static boolean tocandoClip() {

		boolean tocando = false;

		double frameLength = (double) clipLargada.getFrameLength();
		double val = (clipLargada.getFramePosition() / frameLength);
		if (val > 0 && val < 1) {
			tocando = true;
		}

		frameLength = (double) clipVeloMax.getFrameLength();
		val = (clipVeloMax.getFramePosition() / frameLength);
		if (val > 0.1 && val < Util.intervalo(0.7, 0.9)) {
			tocando = true;
		}

		frameLength = (double) clipVeloMaxFinal.getFrameLength();
		val = (clipVeloMaxFinal.getFramePosition() / frameLength);
		if (val > 0.1 && val < Util.intervalo(0.7, 0.9)) {
			tocando = true;
		}

		frameLength = (double) clipVeloMed.getFrameLength();
		val = (clipVeloMed.getFramePosition() / frameLength);
		if (val > 0.1 && val < Util.intervalo(0.7, 0.9)) {
			tocando = true;
		}
		return tocando;
	}

	private static void iniciaVars() throws UnsupportedAudioFileException,
			IOException, LineUnavailableException {
		if (clipBox == null) {
			AudioInputStream box = AudioSystem
					.getAudioInputStream(CarregadorRecursos
							.recursoComoStream("box.wav"));
			DataLine.Info info = new DataLine.Info(Clip.class, box.getFormat());
			clipBox = (Clip) AudioSystem.getLine(info);
			clipBox.open(box);
			FloatControl gainControl = (FloatControl) clipBox
					.getControl(FloatControl.Type.MASTER_GAIN);
			gainControl.setValue(volume);

		}

		if (clipVeloMaxFinal == null) {
			AudioInputStream veloMaxFinal = AudioSystem
					.getAudioInputStream(CarregadorRecursos
							.recursoComoStream("highMax.wav"));
			DataLine.Info info = new DataLine.Info(Clip.class,
					veloMaxFinal.getFormat());
			clipVeloMaxFinal = (Clip) AudioSystem.getLine(info);
			clipVeloMaxFinal.open(veloMaxFinal);
			FloatControl gainControl = (FloatControl) clipVeloMaxFinal
					.getControl(FloatControl.Type.MASTER_GAIN);
			gainControl.setValue(volume);

		}
		if (clipVeloMax == null) {
			AudioInputStream veloMax = AudioSystem
					.getAudioInputStream(CarregadorRecursos
							.recursoComoStream("high.wav"));
			DataLine.Info info = new DataLine.Info(Clip.class,
					veloMax.getFormat());
			clipVeloMax = (Clip) AudioSystem.getLine(info);
			clipVeloMax.open(veloMax);
			FloatControl gainControl = (FloatControl) clipVeloMax
					.getControl(FloatControl.Type.MASTER_GAIN);
			gainControl.setValue(volume);

		}
		if (clipVeloMed == null) {
			AudioInputStream veloMed = AudioSystem
					.getAudioInputStream(CarregadorRecursos
							.recursoComoStream("med.wav"));
			DataLine.Info info = new DataLine.Info(Clip.class,
					veloMed.getFormat());
			clipVeloMed = (Clip) AudioSystem.getLine(info);
			clipVeloMed.open(veloMed);
			FloatControl gainControl = (FloatControl) clipVeloMed
					.getControl(FloatControl.Type.MASTER_GAIN);
			gainControl.setValue(volume);
		}
		if (clipLargada == null) {
			AudioInputStream veloBaixa = AudioSystem
					.getAudioInputStream(CarregadorRecursos
							.recursoComoStream("largada.wav"));
			DataLine.Info info = new DataLine.Info(Clip.class,
					veloBaixa.getFormat());
			clipLargada = (Clip) AudioSystem.getLine(info);
			clipLargada.open(veloBaixa);
			FloatControl gainControl = (FloatControl) clipLargada
					.getControl(FloatControl.Type.MASTER_GAIN);
			gainControl.setValue(volume);
		}
		if (roncoClip == null) {
			AudioInputStream ronco = AudioSystem
					.getAudioInputStream(CarregadorRecursos
							.recursoComoStream("ronco.wav"));
			DataLine.Info info = new DataLine.Info(Clip.class,
					ronco.getFormat());
			roncoClip = (Clip) AudioSystem.getLine(info);
			roncoClip.open(ronco);
			FloatControl gainControl = (FloatControl) roncoClip
					.getControl(FloatControl.Type.MASTER_GAIN);
			gainControl.setValue(volume);
		}
		if (clipRedo == null) {
			AudioInputStream redo = AudioSystem
					.getAudioInputStream(CarregadorRecursos
							.recursoComoStream("redo.wav"));
			DataLine.Info info = new DataLine.Info(Clip.class, redo.getFormat());
			clipRedo = (Clip) AudioSystem.getLine(info);
			clipRedo.open(redo);
			FloatControl gainControl = (FloatControl) clipRedo
					.getControl(FloatControl.Type.MASTER_GAIN);
			gainControl.setValue(-10f);
		}
		if (clipAcel == null) {
			AudioInputStream acel = AudioSystem
					.getAudioInputStream(CarregadorRecursos
							.recursoComoStream("acel.wav"));
			DataLine.Info info = new DataLine.Info(Clip.class, acel.getFormat());
			clipAcel = (Clip) AudioSystem.getLine(info);
			clipAcel.open(acel);
			FloatControl gainControl = (FloatControl) clipAcel
					.getControl(FloatControl.Type.MASTER_GAIN);
			gainControl.setValue(volume);
		}
	}

	/**
	 * Plays audio from the given audio input stream.
	 * 
	 * @throws LineUnavailableException
	 * @throws IOException
	 */
	public static void playAudioStream(AudioInputStream audioInputStream)
			throws LineUnavailableException, IOException {

	} // playAudioStream

	public static void processaSom(Piloto pilotoSelecionado,
			InterfaceJogo controleJogo, PainelCircuito painelCircuito) {
		try {
			if (!somLigado) {
				return;
			}
			if (pilotoSelecionado == null) {
				return;
			}
			iniciaVars();
			somVelocidade(pilotoSelecionado, controleJogo, painelCircuito);
		} catch (Exception e) {
			paraTudo();
			Logger.logarExept(e);
		}
	}

	public static void ligaDesligaSom() {
		somLigado = !somLigado;
		if (!somLigado) {
			paraTudo();
		}

	}

	public static void paraTudo() {
		if (clipVeloMax != null)
			clipVeloMax.stop();
		if (clipVeloMaxFinal != null)
			clipVeloMaxFinal.stop();
		if (clipLargada != null)
			clipLargada.stop();
		if (clipVeloMed != null)
			clipVeloMed.stop();
		if (roncoClip != null)
			roncoClip.stop();
		if (clipRedo != null)
			clipRedo.stop();
		if (clipAcel != null)
			clipAcel.stop();
		if (clipBox != null)
			clipBox.stop();
	}

}
