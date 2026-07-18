package br.flmane.editor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import br.flmane.entidades.ObjetoArquibancada;
import br.flmane.entidades.ObjetoCirculo;
import br.flmane.entidades.ObjetoConstrucao;
import br.flmane.entidades.ObjetoEscapada;
import br.flmane.entidades.ObjetoGuardRails;
import br.flmane.entidades.ObjetoLivre;
import br.flmane.entidades.ObjetoPneus;
import br.flmane.entidades.ObjetoTransparencia;

class TipoObjetoPistaTest {

    @Test
    void de_arquibancada() {
        assertEquals(TipoObjetoPista.ARQUIBANCADA, TipoObjetoPista.de(new ObjetoArquibancada()));
    }

    @Test
    void de_construcao() {
        assertEquals(TipoObjetoPista.CONSTRUCAO, TipoObjetoPista.de(new ObjetoConstrucao()));
    }

    @Test
    void de_guardRails() {
        assertEquals(TipoObjetoPista.GUARD_RAILS, TipoObjetoPista.de(new ObjetoGuardRails()));
    }

    @Test
    void de_pneus() {
        assertEquals(TipoObjetoPista.PNEUS, TipoObjetoPista.de(new ObjetoPneus()));
    }

    @Test
    void de_livre() {
        assertEquals(TipoObjetoPista.LIVRE, TipoObjetoPista.de(new ObjetoLivre()));
    }

    @Test
    void de_escapada() {
        assertEquals(TipoObjetoPista.ESCAPADA, TipoObjetoPista.de(new ObjetoEscapada()));
    }

    @Test
    void de_transparencia() {
        assertEquals(TipoObjetoPista.TRANSPARENCIA, TipoObjetoPista.de(new ObjetoTransparencia()));
    }

    @Test
    void de_classeNaoMapeada_lancaExcecao() {
        assertThrows(IllegalArgumentException.class, () -> TipoObjetoPista.de(new ObjetoCirculo()));
    }
}
