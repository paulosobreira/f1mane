package br.f1mane.editor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Point;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import org.junit.jupiter.api.Test;

/**
 * Pan por seta (esquerda/direita/cima/baixo) não redesenha a camada de
 * objetos a cada passo: um único javax.swing.Timer (0,5s, não repetitivo),
 * compartilhado entre as quatro direções, é reiniciado a cada passo, e
 * {@code paintComponent} só desenha os objetos do circuito quando esse timer
 * NÃO está rodando ({@link MainPanelEditor#deveDesenharObjetos()}).
 * <p>
 * Diferente de uma primeira versão desta feature: o {@code repaint()} em si
 * NÃO é pulado durante o pan — é chamado a cada passo, normalmente. Pular o
 * repaint quebrava desenhaInfo()/desenhaControles() (overlays fixos ao canto
 * da viewport, recalculados a cada frame a partir de limitesViewPort()):
 * como eles não são conteúdo estático em coordenadas de circuito, o
 * blit-scroll da JViewport arrastava a posição desenhada no frame anterior
 * junto com o resto do canvas, em vez de mantê-los presos ao canto — um
 * artefato visual sobre o fundo. Só a iteração sobre os objetos do circuito
 * (o custo que motivou o adiamento) é pulada durante o gesto; o resto do
 * frame é redesenhado normalmente a cada passo.
 */
class MainPanelEditorScrollDebounceTest {

    private static class EditorComContadorDeRepaint extends MainPanelEditor {
        int repaints;

        @Override
        public void repaint() {
            repaints++;
        }
    }

    /**
     * Monta o scrollPane com {@code editor} como view (como em produção,
     * {@code gerarLayout}) — sem uma view real, {@code JViewport} não tem
     * como calcular {@code getViewSize()} e clampa qualquer
     * {@code setViewPosition} de volta pra (0,0).
     */
    private static void configuraViewport(MainPanelEditor editor) throws Exception {
        editor.setPreferredSize(new java.awt.Dimension(10000, 10000));
        JScrollPane scrollPane = new JScrollPane(editor, JScrollPane.VERTICAL_SCROLLBAR_NEVER,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getViewport().setSize(400, 400);
        scrollPane.getViewport().setViewPosition(new Point(100, 100));
        Field campo = MainPanelEditor.class.getDeclaredField("scrollPane");
        campo.setAccessible(true);
        campo.set(editor, scrollPane);
    }

    private static Timer timerDebounce(MainPanelEditor editor) throws Exception {
        Field campo = MainPanelEditor.class.getDeclaredField("timerRedesenhoObjetosPosScroll");
        campo.setAccessible(true);
        return (Timer) campo.get(editor);
    }

    private static boolean deveDesenharObjetos(MainPanelEditor editor) throws Exception {
        Method metodo = MainPanelEditor.class.getDeclaredMethod("deveDesenharObjetos");
        metodo.setAccessible(true);
        return (Boolean) metodo.invoke(editor);
    }

    /** Roda um Runnable na EDT e drena a fila (inclusive invokeLater aninhado, como o de esquerda()/direita()/...). */
    private static void executarEDrenarEdt(Runnable acao) throws Exception {
        SwingUtilities.invokeAndWait(acao::run);
        SwingUtilities.invokeAndWait(() -> {
        });
    }

    @Test
    void esquerda_moveViewportImediatamenteEIniciaDebounceDosObjetos() throws Exception {
        MainPanelEditor editor = new MainPanelEditor();
        configuraViewport(editor);
        Timer timer = timerDebounce(editor);
        assertFalse(timer.isRunning(), "timer não deveria estar rodando antes de qualquer pan");
        assertTrue(deveDesenharObjetos(editor), "sem pan em andamento, os objetos devem ser desenhados normalmente");

        executarEDrenarEdt(editor::esquerda);

        Field scrollPaneField = MainPanelEditor.class.getDeclaredField("scrollPane");
        scrollPaneField.setAccessible(true);
        JScrollPane scrollPane = (JScrollPane) scrollPaneField.get(editor);
        assertEquals(60, scrollPane.getViewport().getViewPosition().x,
                "viewport deve se mover imediatamente (100 - 40)");

        assertTrue(timer.isRunning(), "pan por seta deve iniciar o timer de debounce dos objetos");
        assertEquals(500, timer.getDelay(), "atraso do redesenho dos objetos deve ser de 0,5 segundos");
        assertFalse(timer.isRepeats(), "o redesenho dos objetos deve disparar uma única vez por período de pan parado");
        assertFalse(deveDesenharObjetos(editor), "com o pan em andamento, o desenho dos objetos deve ficar suspenso");
    }

    @Test
    void passosSucessivosDePan_compartilhamOMesmoTimerEContinuamAgendados() throws Exception {
        MainPanelEditor editor = new MainPanelEditor();
        configuraViewport(editor);
        Timer timer = timerDebounce(editor);

        executarEDrenarEdt(editor::direita);
        assertTrue(timer.isRunning());

        executarEDrenarEdt(editor::cima);
        Timer timerAposSegundoPasso = timerDebounce(editor);
        assertTrue(timerAposSegundoPasso == timer, "as quatro direções devem compartilhar a mesma instância de timer");
        assertTrue(timerAposSegundoPasso.isRunning(), "um novo passo de pan deve manter o timer agendado (restart), não deixá-lo expirar");
        assertFalse(deveDesenharObjetos(editor), "objetos continuam suspensos enquanto o gesto está em andamento");
    }

    @Test
    void todoPassoDePan_repintaOFrameNormalmente() throws Exception {
        // repaint() precisa ser chamado a cada passo (não só no primeiro) —
        // é o que mantém desenhaInfo()/desenhaControles() presos ao canto da
        // viewport a cada frame, evitando o artefato descrito na classe.
        EditorComContadorDeRepaint editor = new EditorComContadorDeRepaint();
        configuraViewport(editor);
        int antesDoGesto = editor.repaints;

        executarEDrenarEdt(editor::esquerda);
        assertEquals(antesDoGesto + 1, editor.repaints, "primeiro passo do gesto deve repintar o frame");

        executarEDrenarEdt(editor::direita);
        assertEquals(antesDoGesto + 2, editor.repaints, "passos seguintes do mesmo gesto também devem repintar o frame");
    }

    @Test
    void timerNaoRodando_deveDesenharObjetosEhVerdadeiro() throws Exception {
        MainPanelEditor editor = new MainPanelEditor();
        assertTrue(deveDesenharObjetos(editor));
    }
}
