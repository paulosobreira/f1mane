package br.flmane.editor;

import java.util.function.Supplier;

import br.flmane.entidades.ObjetoArquibancada;
import br.flmane.entidades.ObjetoConstrucao;
import br.flmane.entidades.ObjetoEscapada;
import br.flmane.entidades.ObjetoGuardRails;
import br.flmane.entidades.ObjetoLivre;
import br.flmane.entidades.ObjetoPista;
import br.flmane.entidades.ObjetoPneus;
import br.flmane.entidades.ObjetoTransparencia;
import br.flmane.recursos.idiomas.Lang;

public enum TipoObjetoPista {

	// Objeto Livre é "cenário" (mesma família de Arquibancada/Construcao/GuardRails/Pneus):
	// é um objeto de desenho, não de função como Escapada/Transparencia, e precisa estar em
	// objetosCenario para ser desenhado de fato em corrida (ver DesenhoProceduralCircuito).
	LIVRE("objetoLivreTipo", ObjetoLivre::new, true),
	ARQUIBANCADA("objetoArquibancadaTipo", ObjetoArquibancada::new, true),
	CONSTRUCAO("objetoConstrucaoTipo", ObjetoConstrucao::new, true),
	GUARD_RAILS("objetoGuardRailsTipo", ObjetoGuardRails::new, true),
	PNEUS("objetoPneusTipo", ObjetoPneus::new, true),
	// Objetos de função (Escapada/Transparencia) ficam por último na ordem de
	// criação: são usados bem menos que os de cenário/desenho.
	ESCAPADA("objetoEscapadaTipo", ObjetoEscapada::new, false),
	TRANSPARENCIA("objetoTransparenciaTipo", ObjetoTransparencia::new, false);

	private final String chaveDescricao;
	private final Supplier<ObjetoPista> fabrica;
	private final boolean cenario;

	TipoObjetoPista(String chaveDescricao, Supplier<ObjetoPista> fabrica, boolean cenario) {
		this.chaveDescricao = chaveDescricao;
		this.fabrica = fabrica;
		this.cenario = cenario;
	}

	public ObjetoPista criar() {
		return fabrica.get();
	}

	/**
	 * Caminho inverso de {@link #criar()}: determina o {@link TipoObjetoPista}
	 * de uma instância {@link ObjetoPista} já existente, pela sua classe
	 * concreta. Lança {@link IllegalArgumentException} para uma classe não
	 * mapeada, em vez de retornar {@code null} — um tipo desconhecido deve
	 * falhar alto, não desaparecer silenciosamente de filtros/listas.
	 */
	public static TipoObjetoPista de(ObjetoPista objeto) {
		if (objeto instanceof ObjetoArquibancada) {
			return ARQUIBANCADA;
		} else if (objeto instanceof ObjetoConstrucao) {
			return CONSTRUCAO;
		} else if (objeto instanceof ObjetoGuardRails) {
			return GUARD_RAILS;
		} else if (objeto instanceof ObjetoPneus) {
			return PNEUS;
		} else if (objeto instanceof ObjetoLivre) {
			return LIVRE;
		} else if (objeto instanceof ObjetoEscapada) {
			return ESCAPADA;
		} else if (objeto instanceof ObjetoTransparencia) {
			return TRANSPARENCIA;
		}
		throw new IllegalArgumentException(
				"Nenhum TipoObjetoPista mapeado para " + objeto.getClass().getName());
	}

	/**
	 * @return true se este tipo pertence à lista de objetos de cenário
	 *         (Circuito.objetosCenario), false se pertence à lista original
	 *         (Circuito.objetos, Escapada/Transparencia).
	 */
	public boolean isCenario() {
		return cenario;
	}

	@Override
	public String toString() {
		return Lang.msg(chaveDescricao);
	}
}
