package sowbreira.f1mane.paddock.entidades.TOs;

import java.io.Serializable;

import sowbreira.f1mane.entidades.Volta;

/**
 * @author Paulo Sobreira Criado em 21/08/2007 as 21:08:26
 */
public class DadosParciais implements Serializable {
	public int[] pilotsPonts = new int[22];
	public Volta melhorVolta;
	public Volta peselMelhorVolta;
	public Volta peselUltima;
	public int voltaAtual;
	public int pselCombust;
	public String pselTpPneus;
	public int pselCombustBox;
	public String pselTpPneusBox;
	public int pselVelocidade;
	public int pselPneus;
	public int pselMotor;
	public int pselParadas;
	public int pselGiro;
	public boolean pselBox;
	public String clima;
	public String texto;
	public String nomeJogador;
	public String estado;
	public String dano;
	public int pselMaxPneus;
	public String pselAsaBox;
	public String pselAsa;
}
