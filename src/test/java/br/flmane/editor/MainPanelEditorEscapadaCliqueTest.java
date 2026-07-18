package br.flmane.editor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Point;
import java.awt.event.MouseEvent;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;

import org.junit.jupiter.api.Test;

import br.flmane.entidades.Circuito;
import br.flmane.entidades.No;
import br.flmane.entidades.ObjetoEscapada;

/**
 * Cobre o fluxo real de cliques do editor para criar um ObjetoEscapada
 * (dispara MouseEvent de verdade no MouseAdapter registrado, não apenas os
 * helpers de validação isoladamente): o primeiro clique esquerdo (entrada) e
 * o clique direito que finaliza (saída) precisam validar contra o traçado; os
 * cliques esquerdos entre eles (trajeto livre da zona de escapada) NÃO devem
 * ser validados nem rejeitados, só acrescentados como estão — regressão para
 * um bug em que o segundo clique (livre) era incorretamente validado como se
 * fosse a entrada.
 */
class MainPanelEditorEscapadaCliqueTest {

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
        circuito.vetorizarPista();
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
        // Finalizar a criação de um ObjetoEscapada chama
        // formularioListaObjetos.listarObjetos(); sem passar pela janela
        // completa do editor esse campo (pacote-privado, pensado para injeção
        // em teste) fica null.
        editor.formularioListaObjetos = FormularioListaObjetos.unificada(editor);
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
    void cliquesLivresNoMeioNaoSaoValidados_soEntradaESaida() throws Exception {
        Circuito circuito = circuitoVetorizado();
        MainPanelEditorTestDouble editor = new MainPanelEditorTestDouble();
        editor.setCircuito(circuito);
        ativarMouseListener(editor);

        ObjetoEscapada escapada = new ObjetoEscapada();
        editor.setObjetoPista(escapada);
        setField(editor, "desenhandoObjetoLivre", true);
        setField(editor, "posicionaObjetoPista", true);

        No noEntrada = circuito.getPista1Full().get(300);
        // Bem longe de qualquer nó do traçado 1/2 (centro do quadrado da
        // pista) — se estivesse sendo validado como entrada, seria rejeitado.
        Point pontoLivre = new Point(2500, 2500);
        No noSaida = circuito.getPista1Full().get(500);

        clicar(editor, noEntrada.getPoint(), MouseEvent.BUTTON1);
        clicar(editor, pontoLivre, MouseEvent.BUTTON1);
        clicar(editor, noSaida.getPoint(), MouseEvent.BUTTON3);

        assertEquals(3, escapada.getPontos().size(),
                "deveria ter 3 pontos: entrada validada, ponto livre no meio, saída validada");
        assertEquals(noEntrada.getPoint(), escapada.getPontos().get(0), "primeiro ponto deveria ser a entrada validada");
        assertEquals(pontoLivre, escapada.getPontos().get(1),
                "ponto do meio deveria ser exatamente o clique bruto, sem validação nem ajuste");
        assertEquals(noSaida.getPoint(), escapada.getPontos().get(2), "último ponto deveria ser a saída validada");
        assertEquals(1, escapada.getTracadoOrigem());
        assertEquals(noEntrada.getIndex(), escapada.getIndiceEntrada(),
                "índice de entrada deveria ser gravado para o carro de teste conseguir achar a zona depois");
        assertEquals(noSaida.getIndex(), escapada.getIndiceSaida(),
                "índice de saída deveria ser gravado para o carro de teste conseguir achar a zona depois");
        assertTrue(circuito.getObjetos().contains(escapada), "objeto deveria ter sido finalizado e adicionado ao circuito");
    }

    @Test
    void cliqueDeEntradaLongeDoTracado_naoAdicionaPonto() throws Exception {
        Circuito circuito = circuitoVetorizado();
        MainPanelEditorTestDouble editor = new MainPanelEditorTestDouble();
        editor.setCircuito(circuito);
        ativarMouseListener(editor);

        ObjetoEscapada escapada = new ObjetoEscapada();
        editor.setObjetoPista(escapada);
        setField(editor, "desenhandoObjetoLivre", true);
        setField(editor, "posicionaObjetoPista", true);

        clicar(editor, new Point(2500, 2500), MouseEvent.BUTTON1);

        assertTrue(escapada.getPontos().isEmpty(),
                "clique de entrada longe do traçado não deveria adicionar nenhum ponto");
        assertEquals(1, editor.getAlertasPontoEscapadaInvalido(),
                "deveria ter disparado o alerta de ponto inválido (via double, sem abrir diálogo Swing real)");
    }

    @Test
    void cliqueDeSaidaNoTracadoErrado_naoFinaliza() throws Exception {
        Circuito circuito = circuitoVetorizado();
        MainPanelEditorTestDouble editor = new MainPanelEditorTestDouble();
        editor.setCircuito(circuito);
        ativarMouseListener(editor);

        ObjetoEscapada escapada = new ObjetoEscapada();
        editor.setObjetoPista(escapada);
        setField(editor, "desenhandoObjetoLivre", true);
        setField(editor, "posicionaObjetoPista", true);

        No noEntradaTracado1 = circuito.getPista1Full().get(300);
        No noSaidaTracado2 = circuito.getPista2Full().get(500);

        clicar(editor, noEntradaTracado1.getPoint(), MouseEvent.BUTTON1);
        clicar(editor, noSaidaTracado2.getPoint(), MouseEvent.BUTTON3);

        assertEquals(1, escapada.getPontos().size(),
                "saída no traçado errado não deveria ser aceita nem finalizar o objeto");
        assertTrue(circuito.getObjetos() == null || !circuito.getObjetos().contains(escapada),
                "objeto não deveria ter sido adicionado ao circuito");
        assertEquals(1, editor.getAlertasPontoEscapadaInvalido(),
                "deveria ter disparado o alerta de ponto inválido (via double, sem abrir diálogo Swing real)");
    }
}
