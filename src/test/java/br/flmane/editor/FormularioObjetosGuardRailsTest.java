package br.flmane.editor;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import br.flmane.entidades.ObjetoGuardRails;
import br.flmane.entidades.OrientacaoGuardRails;

/**
 * Espessura de linha e vão entre linhas de ObjetoGuardRails passaram a ser
 * propriedades do objeto (antes eram constantes fixas) e precisam ser
 * lidas/gravadas corretamente pelo formulário de edição.
 */
class FormularioObjetosGuardRailsTest {

    @Test
    void carregarCampos_lePropriedadesDoObjeto() {
        ObjetoGuardRails guardRails = new ObjetoGuardRails();
        guardRails.setLarguraLinha(4);
        guardRails.setVaoEntreLinhas(6);
        guardRails.setOrientacao(OrientacaoGuardRails.HORIZONTAL);

        FormularioObjetos formulario = new FormularioObjetos(null);
        formulario.carregarCampos(guardRails);
        formulario.formularioObjetoPista(guardRails);

        assertEquals(4, guardRails.getLarguraLinha());
        assertEquals(6, guardRails.getVaoEntreLinhas());
        assertEquals(OrientacaoGuardRails.HORIZONTAL, guardRails.getOrientacao());
    }

    @Test
    void carregarEGravar_semAlterarNada_preservaValoresPadrao() {
        ObjetoGuardRails guardRails = new ObjetoGuardRails();

        FormularioObjetos formulario = new FormularioObjetos(null);
        formulario.carregarCampos(guardRails);
        formulario.formularioObjetoPista(guardRails);

        assertEquals(1, guardRails.getLarguraLinha());
        assertEquals(1, guardRails.getVaoEntreLinhas());
    }
}
