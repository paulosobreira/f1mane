package sowbreira.f1mane.visao;

import java.io.IOException;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

import sowbreira.f1mane.recursos.CarregadorRecursos;
import br.nnpe.Logger;

public class ControleSom {

	private static AudioInputStream veloMax = null;
	private static AudioInputStream veloBaixa = null;
	private static Clip clipVeloMax;
	private static Clip clipVeloBaixa;
	private static long lastCall;
	private static Clip clipVeloMed;
	private static AudioInputStream veloMed;

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
		} else if (lastCall + 100 > System.currentTimeMillis()) {
			return;
		}

		try {
			iniciaVars();
			if (velocidade < 60) {
				if (clipVeloBaixa.isRunning())
					clipVeloBaixa.stop(); // Stop the player if it is still
				clipVeloBaixa.setFramePosition(0); // rewind to the beginning
				clipVeloBaixa.start();
			} else if (velocidade > 61 && velocidade < 200) {
				if (clipVeloMed.isRunning())
					clipVeloMed.stop(); // Stop the player if it is still
				clipVeloMed.setFramePosition(0); // rewind to the beginning
				clipVeloMed.start();
			} else {
				if (clipVeloMax.isRunning())
					clipVeloMax.stop(); // Stop the player if it is still
				clipVeloMax.setFramePosition(0); // rewind to the beginning
				clipVeloMax.start();

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
			clipVeloMed = (Clip) AudioSystem.getLine(info);
			clipVeloMed.open(veloMed);
		}
		if (veloBaixa == null) {
			veloBaixa = AudioSystem.getAudioInputStream(CarregadorRecursos
					.recursoComoStream("low.wav"));
			DataLine.Info info = new DataLine.Info(Clip.class, veloBaixa
					.getFormat());
			clipVeloBaixa = (Clip) AudioSystem.getLine(info);
			clipVeloBaixa.open(veloBaixa);
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

}
