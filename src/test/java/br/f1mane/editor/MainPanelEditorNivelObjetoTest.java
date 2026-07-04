package br.f1mane.editor;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import br.f1mane.entidades.ObjetoEscapada;
import br.f1mane.entidades.ObjetoLivre;
import br.f1mane.entidades.ObjetoTransparencia;

/**
 * Atalhos PageUp/PageDown do editor: sobem/descem o nível de desenho do
 * objeto selecionado, sem limite de faixa; os objetos de função
 * (ObjetoTransparencia, ObjetoEscapada) ficam fora do sistema de níveis e
 * não são alterados.
 */
class MainPanelEditorNivelObjetoTest {

    @Test
    void pageUpPageDown_mudamONivelDoObjetoSelecionado_semLimite() {
        MainPanelEditor editor = new MainPanelEditor();
        ObjetoLivre objeto = new ObjetoLivre();
        editor.setObjetoPista(objeto);

        editor.subirNivelObjeto();
        assertEquals(1, objeto.getNivelDesenho());
        editor.subirNivelObjeto();
        assertEquals(2, objeto.getNivelDesenho(), "deveria continuar subindo além de 1");

        editor.descerNivelObjeto();
        assertEquals(1, objeto.getNivelDesenho());
        editor.descerNivelObjeto();
        assertEquals(0, objeto.getNivelDesenho());
        editor.descerNivelObjeto();
        assertEquals(-1, objeto.getNivelDesenho());
        editor.descerNivelObjeto();
        assertEquals(-2, objeto.getNivelDesenho(), "deveria continuar descendo além de -1");
    }

    @Test
    void transparencia_naoTemNivelAlterado() {
        MainPanelEditor editor = new MainPanelEditor();
        ObjetoTransparencia transparencia = new ObjetoTransparencia();
        editor.setObjetoPista(transparencia);

        editor.subirNivelObjeto();
        assertEquals(0, transparencia.getNivelDesenho());
        editor.descerNivelObjeto();
        assertEquals(0, transparencia.getNivelDesenho());
    }

    @Test
    void escapada_naoTemNivelAlterado() {
        MainPanelEditor editor = new MainPanelEditor();
        ObjetoEscapada escapada = new ObjetoEscapada();
        editor.setObjetoPista(escapada);

        editor.subirNivelObjeto();
        assertEquals(0, escapada.getNivelDesenho());
        editor.descerNivelObjeto();
        assertEquals(0, escapada.getNivelDesenho());
    }

    @Test
    void semObjetoSelecionado_naoLancaExcecao() {
        MainPanelEditor editor = new MainPanelEditor();
        editor.setObjetoPista(null);
        editor.subirNivelObjeto();
        editor.descerNivelObjeto();
    }
}
