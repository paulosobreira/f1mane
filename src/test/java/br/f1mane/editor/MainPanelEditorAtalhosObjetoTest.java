package br.f1mane.editor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import br.f1mane.entidades.Circuito;
import br.f1mane.entidades.ObjetoLivre;
import br.f1mane.entidades.ObjetoPista;

/**
 * Atalho Delete: apaga o objeto selecionado, em qualquer das duas listas do
 * circuito (objetos ou objetosCenario) — "vale em todos os cenários", ou
 * seja, não é restrito a uma das duas.
 */
class MainPanelEditorAtalhosObjetoTest {

    @Test
    void apagar_removeObjetoSelecionadoDaListaObjetos() {
        MainPanelEditor editor = new MainPanelEditor();
        Circuito circuito = new Circuito();
        ObjetoLivre objeto = new ObjetoLivre();
        List<ObjetoPista> objetos = new ArrayList<>();
        objetos.add(objeto);
        circuito.setObjetos(objetos);
        editor.setCircuito(circuito);
        editor.setObjetoPista(objeto);

        editor.apagarObjetoSelecionado();

        assertTrue(circuito.getObjetos().isEmpty());
        assertNull(editor.getObjetoPista());
    }

    @Test
    void apagar_removeObjetoSelecionadoDaListaObjetosCenario() {
        MainPanelEditor editor = new MainPanelEditor();
        Circuito circuito = new Circuito();
        ObjetoLivre objeto = new ObjetoLivre();
        List<ObjetoPista> objetosCenario = new ArrayList<>();
        objetosCenario.add(objeto);
        circuito.setObjetosCenario(objetosCenario);
        editor.setCircuito(circuito);
        editor.setObjetoPista(objeto);

        editor.apagarObjetoSelecionado();

        assertTrue(circuito.getObjetosCenario().isEmpty());
        assertNull(editor.getObjetoPista());
    }

    @Test
    void apagar_naoMexeNosOutrosObjetosDaLista() {
        MainPanelEditor editor = new MainPanelEditor();
        Circuito circuito = new Circuito();
        ObjetoLivre alvo = new ObjetoLivre();
        alvo.setNome("Alvo");
        ObjetoLivre outro = new ObjetoLivre();
        outro.setNome("Outro");
        List<ObjetoPista> objetos = new ArrayList<>();
        objetos.add(outro);
        objetos.add(alvo);
        circuito.setObjetos(objetos);
        editor.setCircuito(circuito);
        editor.setObjetoPista(alvo);

        editor.apagarObjetoSelecionado();

        assertEquals(1, circuito.getObjetos().size());
        assertEquals("Outro", circuito.getObjetos().get(0).getNome());
    }

    @Test
    void apagar_semObjetoSelecionado_naoLancaExcecaoENaoAlteraCircuito() {
        MainPanelEditor editor = new MainPanelEditor();
        Circuito circuito = new Circuito();
        ObjetoLivre objeto = new ObjetoLivre();
        List<ObjetoPista> objetos = new ArrayList<>();
        objetos.add(objeto);
        circuito.setObjetos(objetos);
        editor.setCircuito(circuito);
        editor.setObjetoPista(null);

        editor.apagarObjetoSelecionado();

        assertFalse(circuito.getObjetos().isEmpty());
    }
}
