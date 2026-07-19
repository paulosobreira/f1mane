package br.flmane.entidades;

/**
 * Objeto de desenho: aparece na lista de cima do editor (Livre,
 * Arquibancada, Construcao, GuardRails, Pneus), participa do sistema de
 * níveis e tem dimensões/ângulo fisicamente desenhados na tela — por isso
 * largura/altura não fazem sentido menores que 1 (um objeto de tamanho zero
 * é invisível) e o ângulo não faz sentido negativo (equivalente ao ângulo
 * positivo correspondente, módulo 360). Objetos de função (Escapada,
 * Transparencia) estendem {@link ObjetoPista} diretamente e não têm essa
 * restrição — seus campos largura/altura/ângulo guardam outros significados
 * (ex.: comprimento/amplitude de onda em Escapada).
 */
public abstract class ObjetoDesenho extends ObjetoPista {

	private static final long serialVersionUID = 1L;

	@Override
	public void setLargura(int largura) {
		super.setLargura(Math.max(1, largura));
	}

	@Override
	public void setAltura(int altura) {
		super.setAltura(Math.max(1, altura));
	}

	@Override
	public void setAngulo(double angulo) {
		double normalizado = angulo % 360;
		if (normalizado < 0) {
			normalizado += 360;
		}
		super.setAngulo(normalizado);
	}
}
