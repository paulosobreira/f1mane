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
 * Cobre o fluxo real de EDITAR (arrastar) um ponto de um ObjetoEscapada já
 * criado — mousePressed (pega o marcador) → mouseDragged (arrasto ao vivo) →
 * mouseReleased (valida e finaliza) — via MouseEvent de verdade, não
 * manipulação direta de campos. Cobre especificamente que, ao terminar de
 * editar o nó de entrada/saída (soltar o botão), os novos indiceEntrada/
 * indiceSaida ficam gravados corretamente, já que é isso que o TestePista
 * usa para reconhecer a zona sem precisar recarregar o circuito.
 */
class MainPanelEditorEscapadaArrastarPontoTest {

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
        editor.formularioListaObjetos = FormularioListaObjetos.unificada(editor);
    }

    private static void setField(MainPanelEditor editor, String nome, Object valor) throws Exception {
        Field campo = MainPanelEditor.class.getDeclaredField(nome);
        campo.setAccessible(true);
        campo.set(editor, valor);
    }

    private static Object getField(MainPanelEditor editor, String nome) throws Exception {
        Field campo = MainPanelEditor.class.getDeclaredField(nome);
        campo.setAccessible(true);
        return campo.get(editor);
    }

    private static void enviarEvento(MainPanelEditor editor, int tipo, Point ponto, int botao) {
        MouseEvent evento = new MouseEvent(editor, tipo, System.currentTimeMillis(), 0,
                ponto.x, ponto.y, 1, false, botao);
        editor.dispatchEvent(evento);
    }

    private static void clicar(MainPanelEditor editor, Point ponto, int botao) {
        enviarEvento(editor, MouseEvent.MOUSE_CLICKED, ponto, botao);
    }

    /** Cria e finaliza uma escapada válida via clique real, igual ao fluxo do usuário. */
    private ObjetoEscapada criarEscapadaPorClique(MainPanelEditor editor, No noEntrada, Point pontoLivre, No noSaida)
            throws Exception {
        ObjetoEscapada escapada = new ObjetoEscapada();
        editor.setObjetoPista(escapada);
        setField(editor, "desenhandoObjetoLivre", true);
        setField(editor, "posicionaObjetoPista", true);

        clicar(editor, noEntrada.getPoint(), MouseEvent.BUTTON1);
        clicar(editor, pontoLivre, MouseEvent.BUTTON1);
        clicar(editor, noSaida.getPoint(), MouseEvent.BUTTON3);
        return escapada;
    }

    @Test
    void arrastarONoDeEntrada_gravaONovoIndiceEntrada() throws Exception {
        Circuito circuito = circuitoVetorizado();
        MainPanelEditorTestDouble editor = new MainPanelEditorTestDouble();
        editor.setCircuito(circuito);
        ativarMouseListener(editor);

        No noEntradaOriginal = circuito.getPista1Full().get(300);
        No noSaidaOriginal = circuito.getPista1Full().get(500);
        ObjetoEscapada escapada = criarEscapadaPorClique(editor, noEntradaOriginal, new Point(2500, 2500),
                noSaidaOriginal);

        // Liga o modo de edição de pontos, como o botão "Editar Pontos" do
        // menu de contexto faria.
        editor.iniciarEdicaoPontosEscapada(escapada);

        No novoNoEntrada = circuito.getPista1Full().get(320);
        Point pontoEntradaNaTela = novoNoEntrada.getPoint();

        // mousePressed sobre o marcador de entrada (posição ATUAL, antes de mover).
        enviarEvento(editor, MouseEvent.MOUSE_PRESSED, noEntradaOriginal.getPoint(), MouseEvent.BUTTON1);
        assertEquals(0, getField(editor, "indicePontoEscapadaArrastando"),
                "mousePressed sobre o marcador de entrada deveria iniciar o arrasto do índice 0");

        // mouseDragged até a nova posição.
        enviarEvento(editor, MouseEvent.MOUSE_DRAGGED, pontoEntradaNaTela, MouseEvent.BUTTON1);

        // mouseReleased finaliza e valida.
        enviarEvento(editor, MouseEvent.MOUSE_RELEASED, pontoEntradaNaTela, MouseEvent.BUTTON1);

        assertEquals(novoNoEntrada.getPoint(), escapada.getPontos().get(0),
                "depois de soltar, o primeiro ponto deveria estar na nova posição validada");
        assertEquals(novoNoEntrada.getIndex(), escapada.getIndiceEntrada(),
                "depois de soltar o nó de entrada, indiceEntrada deveria ser atualizado para o novo nó — "
                        + "sem isso, o TestePista continua usando o índice antigo até um reload");
        assertEquals(noSaidaOriginal.getIndex(), escapada.getIndiceSaida(),
                "índice de saída não deveria ser afetado por mover a entrada");
    }

    @Test
    void arrastarONoDeSaida_gravaONovoIndiceSaida() throws Exception {
        Circuito circuito = circuitoVetorizado();
        MainPanelEditorTestDouble editor = new MainPanelEditorTestDouble();
        editor.setCircuito(circuito);
        ativarMouseListener(editor);

        No noEntradaOriginal = circuito.getPista1Full().get(300);
        No noSaidaOriginal = circuito.getPista1Full().get(500);
        ObjetoEscapada escapada = criarEscapadaPorClique(editor, noEntradaOriginal, new Point(2500, 2500),
                noSaidaOriginal);

        editor.iniciarEdicaoPontosEscapada(escapada);

        No novoNoSaida = circuito.getPista1Full().get(520);

        enviarEvento(editor, MouseEvent.MOUSE_PRESSED, noSaidaOriginal.getPoint(), MouseEvent.BUTTON1);
        assertEquals(2, getField(editor, "indicePontoEscapadaArrastando"),
                "mousePressed sobre o marcador de saída (índice 2: entrada, livre, saída) deveria iniciar esse arrasto");

        enviarEvento(editor, MouseEvent.MOUSE_DRAGGED, novoNoSaida.getPoint(), MouseEvent.BUTTON1);
        enviarEvento(editor, MouseEvent.MOUSE_RELEASED, novoNoSaida.getPoint(), MouseEvent.BUTTON1);

        assertEquals(novoNoSaida.getPoint(), escapada.getPontos().get(2));
        assertEquals(novoNoSaida.getIndex(), escapada.getIndiceSaida(),
                "depois de soltar o nó de saída, indiceSaida deveria ser atualizado para o novo nó");
        assertEquals(noEntradaOriginal.getIndex(), escapada.getIndiceEntrada());
    }

    @Test
    void depoisDeEditarONoDeEntrada_testePistaReconheceANovaZonaImediatamente() throws Exception {
        Circuito circuito = circuitoVetorizado();
        MainPanelEditorTestDouble editor = new MainPanelEditorTestDouble();
        editor.setCircuito(circuito);
        ativarMouseListener(editor);

        No noEntradaOriginal = circuito.getPista1Full().get(300);
        No noSaidaOriginal = circuito.getPista1Full().get(500);
        ObjetoEscapada escapada = criarEscapadaPorClique(editor, noEntradaOriginal, new Point(2500, 2500),
                noSaidaOriginal);

        editor.iniciarEdicaoPontosEscapada(escapada);
        No novoNoEntrada = circuito.getPista1Full().get(320);
        enviarEvento(editor, MouseEvent.MOUSE_PRESSED, noEntradaOriginal.getPoint(), MouseEvent.BUTTON1);
        enviarEvento(editor, MouseEvent.MOUSE_DRAGGED, novoNoEntrada.getPoint(), MouseEvent.BUTTON1);
        enviarEvento(editor, MouseEvent.MOUSE_RELEASED, novoNoEntrada.getPoint(), MouseEvent.BUTTON1);
        editor.encerrarEdicaoPontosEscapada();

        // Sem revetorizar nem recarregar nada — TestePista lê circuito.getObjetos() ao vivo.
        TestePista testePista = new TestePista(editor, circuito);
        testePista.testarEscapada();
        List<No> pontosPista = circuito.getPistaFull();

        testePista.posicionaCarroConsiderandoEscapada(novoNoEntrada.getIndex(), pontosPista);
        assertEquals(escapada.getPontos().get(0), testePista.getTestCar(),
                "no novo índice de entrada (depois da edição), a escapada já deveria ativar sem precisar recarregar");

        // O índice ANTIGO de entrada não deveria mais ativar a escapada
        // (ficou fora do intervalo [novoIndiceEntrada, indiceSaida]) — a
        // menos que por coincidência ainda caia dentro do intervalo, o que
        // não é o caso aqui (320 > 300).
        testePista.posicionaCarroConsiderandoEscapada(noEntradaOriginal.getIndex(), pontosPista);
        assertNotEquals(escapada.getPontos().get(0), testePista.getTestCar(),
                "o índice antigo de entrada não deveria mais ativar a escapada depois de mover o nó pra frente");
    }
}
