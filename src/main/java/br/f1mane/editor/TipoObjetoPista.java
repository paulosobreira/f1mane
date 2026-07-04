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

public enum TipoObjetoPista {

	// Objeto Livre é "cenário" (mesma família de Arquibancada/Construcao/GuardRails/Pneus):
	// é um objeto de desenho, não de função como Escapada/Transparencia, e precisa estar em
	// objetosCenario para ser desenhado de fato em corrida (ver DesenhoProceduralCircuito).
	LIVRE("Objeto Livre", ObjetoLivre::new, true),
	ARQUIBANCADA("Objeto Arquibancada", ObjetoArquibancada::new, true),
	CONSTRUCAO("Objeto Construcao", ObjetoConstrucao::new, true),
	GUARD_RAILS("Objeto Guard Rails", ObjetoGuardRails::new, true),
	PNEUS("Objeto Pneus", ObjetoPneus::new, true),
	// Objetos de função (Escapada/Transparencia) ficam por último na ordem de
	// criação: são usados bem menos que os de cenário/desenho.
	ESCAPADA("Objeto Escapada", ObjetoEscapada::new, false),
	TRANSPARENCIA("Objeto Transparencia", ObjetoTransparencia::new, false);

	private final String descricao;
	private final Supplier<ObjetoPista> fabrica;
	private final boolean cenario;

	TipoObjetoPista(String descricao, Supplier<ObjetoPista> fabrica, boolean cenario) {
		this.descricao = descricao;
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
		return descricao;
	}
}
