package br.f1mane.editor;

import java.util.function.Supplier;

import br.f1mane.entidades.ObjetoArquibancada;
import br.f1mane.entidades.ObjetoConstrucao;
import br.f1mane.entidades.ObjetoEscapada;
import br.f1mane.entidades.ObjetoGuardRails;
import br.f1mane.entidades.ObjetoLivre;
import br.f1mane.entidades.ObjetoPista;
import br.f1mane.entidades.ObjetoPneus;
import br.f1mane.entidades.ObjetoTransparencia;
import br.f1mane.recursos.idiomas.Lang;

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
