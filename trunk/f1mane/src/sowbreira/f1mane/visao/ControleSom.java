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

	public static void main(String[] args) throws InterruptedException {
	}

	public static void somVelocidade(Piloto ps, InterfaceJogo controleJogo,
			PainelCircuito painelCircuito) {
		if (!somLigado) {
			return;
		}
		if (ps == null) {
			return;
		}
		int velocidade = ps.getVelocidade();
		if (lastCall == 0) {
			lastCall = System.currentTimeMillis();
		} else if (lastCall + (500 + controleJogo.getTempoCiclo()) > System
				.currentTimeMillis()) {
			return;
		}
		if (!roncoClip.isRunning() && velocidade == 0
				&& painelCircuito.getQtdeLuzesAcesas() < 5) {
			roncoClip.loop(3);
			roncoClip.start();
		}
		try {

			if (ps.getPtosBox() != 0) {
				clipLargada.stop();
				clipVeloMed.stop();
				clipVeloMax.stop();
				clipVeloMaxFinal.stop();
				clipBox.loop(Clip.LOOP_CONTINUOUSLY);
				clipBox.start();
			} else if (painelCircuito.getQtdeLuzesAcesas() <= 0
					&& velocidade > 0 && velocidade < 60) {
				roncoClip.stop();
				clipVeloMed.stop();
				clipVeloMax.stop();
				clipVeloMaxFinal.stop();
				clipLargada.loop(Clip.LOOP_CONTINUOUSLY);
				clipLargada.start();
			} else if (!clipVeloMed.isRunning() && velocidade > 60
					&& velocidade < 200) {
				clipVeloMax.stop();
				clipVeloMaxFinal.stop();
				clipBox.stop();
				clipVeloMed.loop(Clip.LOOP_CONTINUOUSLY);
				clipVeloMed.start();
			} else if (!clipVeloMax.isRunning() && velocidade >= 200
					&& velocidade < 300) {
				clipLargada.stop();
				clipVeloMed.stop();
				clipBox.stop();
				clipVeloMaxFinal.stop();
				clipVeloMax.loop(Clip.LOOP_CONTINUOUSLY);
				clipVeloMax.start();
			} else if (!clipVeloMaxFinal.isRunning() && velocidade >= 300) {
				clipLargada.stop();
				clipVeloMed.stop();
				clipVeloMax.stop();
				clipBox.stop();
				clipVeloMaxFinal.loop(Clip.LOOP_CONTINUOUSLY);
				clipVeloMaxFinal.start();
			}
			if (!clipAcel.isRunning()
					&& (ps.getVelocidade() - ps.getVelocidadeAnterior()) > 5) {
				clipAcel.setFramePosition(0);
				clipAcel.start();
			}
			if (!clipRedo.isRunning()
					&& (ps.getVelocidade() - ps.getVelocidadeAnterior()) > -5) {
				clipRedo.setFramePosition(0);
				clipRedo.start();
			}
		} catch (Exception e) {
			Logger.logarExept(e);
		}
		lastCall = System.currentTimeMillis();
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

		}

		if (clipVeloMaxFinal == null) {
			AudioInputStream veloMaxFinal = AudioSystem
					.getAudioInputStream(CarregadorRecursos
							.recursoComoStream("highMax.wav"));
			DataLine.Info info = new DataLine.Info(Clip.class, veloMaxFinal
					.getFormat());
			clipVeloMaxFinal = (Clip) AudioSystem.getLine(info);
			clipVeloMaxFinal.open(veloMaxFinal);

		}
		if (clipVeloMax == null) {
			AudioInputStream veloMax = AudioSystem
					.getAudioInputStream(CarregadorRecursos
							.recursoComoStream("high.wav"));
			DataLine.Info info = new DataLine.Info(Clip.class, veloMax
					.getFormat());
			clipVeloMax = (Clip) AudioSystem.getLine(info);
			clipVeloMax.open(veloMax);

		}
		if (clipVeloMed == null) {
			AudioInputStream veloMed = AudioSystem
					.getAudioInputStream(CarregadorRecursos
							.recursoComoStream("med.wav"));
			DataLine.Info info = new DataLine.Info(Clip.class, veloMed
					.getFormat());
			clipVeloMed = (Clip) AudioSystem.getLine(info);
			clipVeloMed.open(veloMed);
		}
		if (clipLargada == null) {
			AudioInputStream veloBaixa = AudioSystem
					.getAudioInputStream(CarregadorRecursos
							.recursoComoStream("largada.wav"));
			DataLine.Info info = new DataLine.Info(Clip.class, veloBaixa
					.getFormat());
			clipLargada = (Clip) AudioSystem.getLine(info);
			clipLargada.open(veloBaixa);
		}
		if (roncoClip == null) {
			AudioInputStream ronco = AudioSystem
					.getAudioInputStream(CarregadorRecursos
							.recursoComoStream("ronco.wav"));
			DataLine.Info info = new DataLine.Info(Clip.class, ronco
					.getFormat());
			roncoClip = (Clip) AudioSystem.getLine(info);
			roncoClip.open(ronco);
		}
		if (clipRedo == null) {
			AudioInputStream redo = AudioSystem
					.getAudioInputStream(CarregadorRecursos
							.recursoComoStream("redo.wav"));
			DataLine.Info info = new DataLine.Info(Clip.class, redo.getFormat());
			clipRedo = (Clip) AudioSystem.getLine(info);
			clipRedo.open(redo);
		}
		if (clipAcel == null) {
			AudioInputStream acel = AudioSystem
					.getAudioInputStream(CarregadorRecursos
							.recursoComoStream("acel.wav"));
			DataLine.Info info = new DataLine.Info(Clip.class, acel.getFormat());
			clipAcel = (Clip) AudioSystem.getLine(info);
			clipAcel.open(acel);
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
			iniciaVars();
			somVelocidade(pilotoSelecionado, controleJogo, painelCircuito);
		} catch (Exception e) {
			Logger.logarExept(e);
		}

		// TODO Auto-generated method stub

	}

	public static void ligaDesligaSom() {
		somLigado = !somLigado;
		if (!somLigado) {
			paraTudo();
		}

	}

	public static void paraTudo() {
		clipVeloMax.stop();
		clipVeloMaxFinal.stop();
		clipLargada.stop();
		clipVeloMed.stop();
		roncoClip.stop();
		clipRedo.stop();
		clipAcel.stop();
		clipBox.stop();
	}

}
