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

	TRANSPARENCIA("Objeto Transparencia", ObjetoTransparencia::new, false),
	LIVRE("Objeto Livre", ObjetoLivre::new, false),
	ESCAPADA("Objeto Escapada", ObjetoEscapada::new, false),
	ARQUIBANCADA("Objeto Arquibancada", ObjetoArquibancada::new, true),
	CONSTRUCAO("Objeto Construcao", ObjetoConstrucao::new, true),
	GUARD_RAILS("Objeto Guard Rails", ObjetoGuardRails::new, true),
	PNEUS("Objeto Pneus", ObjetoPneus::new, true);

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
