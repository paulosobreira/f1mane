package sowbreira.f1mane.visao;

import java.io.IOException;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

import sowbreira.f1mane.controles.InterfaceJogo;
import sowbreira.f1mane.entidades.Piloto;
import sowbreira.f1mane.recursos.CarregadorRecursos;
import br.nnpe.Logger;

public class ControleSom {

	private static AudioInputStream veloMax = null;
	private static AudioInputStream veloBaixa = null;
	private static Clip clipVeloMax;
	private static Clip clipVelo1;
	private static long lastCall;
	private static Clip clipVelo2;
	private static Clip clipVelo0;
	private static AudioInputStream veloMed;
	private static Clip clipRedo;

	public static void main(String[] args) throws InterruptedException {
		for (int i = 0; i < 100; i++) {
			Thread.sleep(120);
			somVelocidade(10);
			somVelocidade(90);
			somVelocidade(90);
			somVelocidade(90);
			somVelocidade(90);
			somVelocidade(10);
		}
	}

	public static void somVelocidade(int velocidade) {
		if (lastCall == 0) {
			lastCall = System.currentTimeMillis();
		} else if (lastCall + 1000 > System.currentTimeMillis()) {
			return;
		}
		if (velocidade == 0) {
			if (lastCall + 2000 > System.currentTimeMillis()) {
				return;
			}
			clipVelo0.setFramePosition(0);
			clipVelo0.start();
		}
		try {

			if (velocidade > 1 && velocidade < 50) {
				clipVelo0.stop();
				clipVelo1.setFramePosition(0);
				clipVelo1.start();
			} else if (velocidade > 51 && velocidade < 100) {
				clipRedo.setFramePosition(0);
				clipRedo.start();
				clipVelo2.loop(Clip.LOOP_CONTINUOUSLY);
				clipVelo2.start();
			} else if (velocidade > 101 && velocidade < 150) {
				clipRedo.setFramePosition(0);
				clipRedo.start();
				clipVelo2.loop(Clip.LOOP_CONTINUOUSLY);
				clipVelo2.start();
			} else if (velocidade > 151 && velocidade < 200) {
				clipRedo.setFramePosition(0);
				clipRedo.start();
				clipVelo2.loop(Clip.LOOP_CONTINUOUSLY);
				clipVelo2.start();
			} else if (velocidade > 200) {
				clipRedo.setFramePosition(0);
				clipRedo.start();
				clipVelo2.loop(Clip.LOOP_CONTINUOUSLY);
				clipVelo2.start();
			}
		} catch (Exception e) {
			Logger.logarExept(e);
		}
		lastCall = System.currentTimeMillis();
	}

	private static void iniciaVars() throws UnsupportedAudioFileException,
			IOException, LineUnavailableException {
		if (veloMax == null) {
			veloMax = AudioSystem.getAudioInputStream(CarregadorRecursos
					.recursoComoStream("high.wav"));
			DataLine.Info info = new DataLine.Info(Clip.class, veloMax
					.getFormat());
			clipVeloMax = (Clip) AudioSystem.getLine(info);
			clipVeloMax.open(veloMax);

		}
		if (veloMed == null) {
			veloMed = AudioSystem.getAudioInputStream(CarregadorRecursos
					.recursoComoStream("med.wav"));
			DataLine.Info info = new DataLine.Info(Clip.class, veloMed
					.getFormat());
			clipVelo2 = (Clip) AudioSystem.getLine(info);
			clipVelo2.open(veloMed);
		}
		if (veloBaixa == null) {
			veloBaixa = AudioSystem.getAudioInputStream(CarregadorRecursos
					.recursoComoStream("low.wav"));
			DataLine.Info info = new DataLine.Info(Clip.class, veloBaixa
					.getFormat());
			clipVelo1 = (Clip) AudioSystem.getLine(info);
			clipVelo1.open(veloBaixa);
		}
		if (clipVelo0 == null) {
			AudioInputStream ronco = AudioSystem
					.getAudioInputStream(CarregadorRecursos
							.recursoComoStream("ronco.wav"));
			DataLine.Info info = new DataLine.Info(Clip.class, ronco
					.getFormat());
			clipVelo0 = (Clip) AudioSystem.getLine(info);
			clipVelo0.open(ronco);
		}
		if (clipRedo == null) {
			AudioInputStream redo = AudioSystem
					.getAudioInputStream(CarregadorRecursos
							.recursoComoStream("redo.wav"));
			DataLine.Info info = new DataLine.Info(Clip.class, redo
					.getFormat());
			clipRedo = (Clip) AudioSystem.getLine(info);
			clipRedo.open(redo);
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
			InterfaceJogo controleJogo) {
		try {
			iniciaVars();
			int velocidade = pilotoSelecionado.getVelocidade();
			somVelocidade(velocidade);
		} catch (Exception e) {
			Logger.logarExept(e);
		}

		// TODO Auto-generated method stub

	}

}
