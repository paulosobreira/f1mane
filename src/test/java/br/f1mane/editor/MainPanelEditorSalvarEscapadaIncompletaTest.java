package br.f1mane.editor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import br.f1mane.entidades.Circuito;
import br.f1mane.entidades.ObjetoEscapada;
import br.f1mane.entidades.ObjetoPista;

/**
 * salvarPista() bloqueia o salvamento quando existe uma ObjetoEscapada sem
 * ponto de saída ancorado (indiceSaida == -1) — a checagem roda antes de
 * qualquer outro campo/JFileChooser, então o teste de bloqueio não precisa
 * montar a UI completa do editor (pistaJList, testePista etc.).
 */
class MainPanelEditorSalvarEscapadaIncompletaTest {

    private boolean existeEscapadaIncompleta(MainPanelEditor editor) throws Exception {
        Method metodo = MainPanelEditor.class.getDeclaredMethod("existeEscapadaIncompleta");
        metodo.setAccessible(true);
        return (boolean) metodo.invoke(editor);
    }

    @Test
    void salvar_comEscapadaSemSaidaDefinida_ehBloqueadoEChamaAlerta() throws Exception {
        MainPanelEditorTestDouble editor = new MainPanelEditorTestDouble();
        Circuito circuito = new Circuito();
        ObjetoEscapada incompleta = new ObjetoEscapada();
        incompleta.setIndiceEntrada(3);
        // indiceSaida nunca definido: continua -1
        circuito.setObjetos(new ArrayList<>(List.of((ObjetoPista) incompleta)));
        editor.setCircuito(circuito);

        editor.salvarPista();

        assertEquals(1, editor.getAlertasEscapadaIncompleta());
    }

    @Test
    void existeEscapadaIncompleta_comTodasCompletas_retornaFalse() throws Exception {
        MainPanelEditor editor = new MainPanelEditor();
        Circuito circuito = new Circuito();
        ObjetoEscapada completa = new ObjetoEscapada();
        completa.setIndiceEntrada(3);
        completa.setIndiceSaida(7);
        circuito.setObjetos(new ArrayList<>(List.of((ObjetoPista) completa)));
        editor.setCircuito(circuito);

        assertFalse(existeEscapadaIncompleta(editor));
    }

    @Test
    void existeEscapadaIncompleta_semNenhumaEscapada_retornaFalse() throws Exception {
        MainPanelEditor editor = new MainPanelEditor();
        Circuito circuito = new Circuito();
        circuito.setObjetos(new ArrayList<>());
        editor.setCircuito(circuito);

        assertFalse(existeEscapadaIncompleta(editor));
    }

    @Test
    void existeEscapadaIncompleta_comUmaCompletaEUmaIncompleta_retornaTrue() throws Exception {
        MainPanelEditor editor = new MainPanelEditor();
        Circuito circuito = new Circuito();
        ObjetoEscapada completa = new ObjetoEscapada();
        completa.setIndiceEntrada(1);
        completa.setIndiceSaida(2);
        ObjetoEscapada incompleta = new ObjetoEscapada();
        incompleta.setIndiceEntrada(5);
        circuito.setObjetos(new ArrayList<>(List.of((ObjetoPista) completa, (ObjetoPista) incompleta)));
        editor.setCircuito(circuito);

        assertTrue(existeEscapadaIncompleta(editor));
    }
}
