package br.f1mane.editor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.awt.Point;
import java.awt.event.MouseEvent;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;

import org.junit.jupiter.api.Test;

import br.f1mane.entidades.Circuito;
import br.f1mane.entidades.No;
import br.f1mane.entidades.ObjetoEscapada;

/**
 * Reproduz o fluxo real do botão "Teste Pista": criar a escapada por clique
 * e SÓ DEPOIS revetorizar o circuito (é exatamente o que
 * {@code MainPanelEditor.vetorizarCircuito()} faz sempre que o checkbox
 * "Teste Pista" é ligado, antes de {@code TestePista.iniciarTeste}) — para
 * achar se a revetorização invalida os índices gravados na escapada.
 */
class MainPanelEditorEscapadaRevetorizarTest {

    private Circuito circuitoVetorizado() {
        Circuito circuito = new Circuito();
        List<No> pista = new ArrayList<>();
        pista.add(criarNo(1000, 1000));
        pista.add(criarNo(4000, 1000));
        pista.add(criarNo(4000, 4000));
        pista.add(criarNo(1000, 4000));
        circuito.setPista(pista);
        circuito.setBox(new ArrayList<>());
        circuito.setMultiplicadorLarguraPista(1.5);
        circuito.vetorizarPista(9, 1.5);
        return circuito;
    }

    private No criarNo(int x, int y) {
        No no = new No();
        no.setPoint(new Point(x, y));
        no.setTipo(No.RETA);
        return no;
    }

    private static void ativarMouseListener(MainPanelEditor editor) throws Exception {
        Method metodo = MainPanelEditor.class.getDeclaredMethod("adicionaEventosMouse", JFrame.class);
        metodo.setAccessible(true);
        metodo.invoke(editor, (JFrame) null);
        editor.formularioListaObjetosFuncao = new FormularioListaObjetos(editor);
    }

    private static void setField(MainPanelEditor editor, String nome, Object valor) throws Exception {
        Field campo = MainPanelEditor.class.getDeclaredField(nome);
        campo.setAccessible(true);
        campo.set(editor, valor);
    }

    private static void clicar(MainPanelEditor editor, Point ponto, int botao) {
        MouseEvent evento = new MouseEvent(editor, MouseEvent.MOUSE_CLICKED, System.currentTimeMillis(), 0,
                ponto.x, ponto.y, 1, botao == MouseEvent.BUTTON3, botao);
        editor.dispatchEvent(evento);
    }

    @Test
    void escapadaCriadaPorClique_continuaAtivaAposRevetorizarComOsMesmosParametros() throws Exception {
        Circuito circuito = circuitoVetorizado();
        MainPanelEditorTestDouble editor = new MainPanelEditorTestDouble();
        editor.setCircuito(circuito);
        ativarMouseListener(editor);

        ObjetoEscapada escapada = new ObjetoEscapada();
        editor.setObjetoPista(escapada);
        setField(editor, "desenhandoObjetoLivre", true);
        setField(editor, "posicionaObjetoPista", true);

        No noEntradaAntes = circuito.getPista1Full().get(300);
        Point pontoLivre = new Point(2500, 2500);
        No noSaidaAntes = circuito.getPista1Full().get(500);

        clicar(editor, noEntradaAntes.getPoint(), MouseEvent.BUTTON1);
        clicar(editor, pontoLivre, MouseEvent.BUTTON1);
        clicar(editor, noSaidaAntes.getPoint(), MouseEvent.BUTTON3);

        int indiceEntradaGravado = escapada.getIndiceEntrada();
        int indiceSaidaGravado = escapada.getIndiceSaida();

        // Exatamente o que o checkbox "Teste Pista" faz antes de iniciar:
        // revetoriza com os MESMOS parâmetros (nada no traçado bruto mudou).
        circuito.vetorizarPista(9, 1.5);

        assertEquals(indiceEntradaGravado, escapada.getIndiceEntrada(),
                "revetorizar não deveria mudar os índices já gravados na escapada (são valores fixos, não recalculados)");
        assertEquals(indiceSaidaGravado, escapada.getIndiceSaida());

        TestePista testePista = new TestePista(editor, circuito);
        testePista.testarEscapada();
        List<No> pontosPista = circuito.getPistaFull();

        testePista.posicionaCarroConsiderandoEscapada(indiceEntradaGravado, pontosPista);
        assertEquals(escapada.getPontos().get(0), testePista.getTestCar(),
                "depois de revetorizar, o índice de entrada ainda deveria ativar a escapada");

        int indiceMeio = (indiceEntradaGravado + indiceSaidaGravado) / 2;
        testePista.posicionaCarroConsiderandoEscapada(indiceMeio, pontosPista);
        assertNotEquals(pontosPista.get(indiceMeio).getPoint(), testePista.getTestCar(),
                "no meio da zona, depois de revetorizar, o carro ainda deveria estar seguindo o trajeto da escapada");
    }
}
