package br.flmane.editor;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.awt.Point;
import java.awt.event.MouseEvent;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JScrollPane;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import br.flmane.entidades.Circuito;
import br.flmane.entidades.ObjetoConstrucao;
import br.flmane.entidades.ObjetoPista;

/**
 * Regressão de um {@code NullPointerException} real, capturado em produção
 * (usuário posicionando um {@code ObjetoConstrucao} por clique único no
 * circuito SPA-Francorchamps):
 *
 * <pre>
 * java.lang.NullPointerException: Cannot invoke
 * "ObjetoPista.setNome(String)" because "this.this$0.objetoPista" is null
 *     at MainPanelEditor$44.clickEditarObjetos(MainPanelEditor.java:2338)
 * </pre>
 *
 * A causa: no ramo genérico de posicionamento por clique único
 * ({@code posicionaObjetoPista && objetoPista != null}), o objeto era
 * adicionado à lista, depois {@code formularioListaObjetos.listarObjetos()}
 * era chamado — que limpa e repopula o {@code DefaultListModel}, passando
 * por um instante de "seleção vazia" (documentado em
 * {@code FormularioListaObjetos.listarObjetos()}). O
 * {@code ListSelectionListener} de {@code FormularioListaObjetos} reage a
 * isso chamando {@code editor.setObjetoPista(null)} — zerando o campo
 * {@code objetoPista} ANTES do {@code setNome(...)} seguinte rodar. O objeto
 * recém-criado ficava no circuito, mas sem nome (bug do "Objeto 70: null"
 * relatado pelo usuário) — ou, se {@code setNome} rodasse antes do garbage
 * collector permitir a JVM otimizar a leitura do campo, lançava o NPE acima.
 * Todos os outros fluxos de criação (Livre/GuardRails/Arquibancada/Escapada/
 * colar/Pneus) já evitavam isso usando uma variável local em vez do campo
 * {@code objetoPista} depois de {@code listarObjetos()} — só este ramo
 * genérico (usado por ObjetoConstrucao, entre outros) ainda usava o campo
 * diretamente.
 */
class MainPanelEditorCriarObjetoConstrucaoCliqueTest {

    @BeforeEach
    void limparMemoria() {
        MemoriaPropriedadesObjeto.limparParaTeste();
    }

    private MainPanelEditor editorPronto() throws Exception {
        MainPanelEditor editor = new MainPanelEditor();
        Circuito circuito = new Circuito();
        circuito.setObjetosCenario(new ArrayList<>());
        circuito.setObjetos(new ArrayList<>());
        editor.setCircuito(circuito);
        editor.formularioListaObjetos = FormularioListaObjetos.unificada(editor);
        editor.formularioListaObjetos.listarObjetos();
        ativarMouseListener(editor);
        configuraViewport(editor);
        return editor;
    }

    private static void ativarMouseListener(MainPanelEditor editor) throws Exception {
        Method metodo = MainPanelEditor.class.getDeclaredMethod("adicionaEventosMouse", JFrame.class);
        metodo.setAccessible(true);
        metodo.invoke(editor, (JFrame) null);
    }

    /**
     * Necessário pro ListSelectionListener rodar sem supressão: selecionar o
     * objeto existente (ver {@link #criaEselecionaObjetoExistente}) dispara
     * {@code centralizarPonto()}, que precisa de um viewport real — mesmo
     * helper usado por {@code FormularioListaObjetosUnificadaTest}.
     */
    private static void configuraViewport(MainPanelEditor editor) throws Exception {
        JScrollPane scrollPane = new JScrollPane();
        scrollPane.getViewport().setSize(400, 400);
        scrollPane.getViewport().setViewPosition(new Point(0, 0));
        Field campo = MainPanelEditor.class.getDeclaredField("scrollPane");
        campo.setAccessible(true);
        campo.set(editor, scrollPane);
    }

    private static void setField(MainPanelEditor editor, String nome, Object valor) throws Exception {
        Field campo = MainPanelEditor.class.getDeclaredField(nome);
        campo.setAccessible(true);
        campo.set(editor, valor);
    }

    private static void clicarEsquerdo(MainPanelEditor editor, Point ponto) {
        MouseEvent evento = new MouseEvent(editor, MouseEvent.MOUSE_CLICKED, System.currentTimeMillis(), 0,
                ponto.x, ponto.y, 1, false, MouseEvent.BUTTON1);
        editor.dispatchEvent(evento);
    }

    /**
     * Reproduz a pré-condição real: já existe um objeto selecionado na lista
     * ANTES do clique de posicionamento (ex.: o usuário tinha acabado de
     * clicar em outro objeto), para que a limpeza do model dentro de
     * {@code listarObjetos()} realmente transicione de "1 selecionado" pra
     * "nada selecionado" — é essa transição que dispara o
     * {@code ListSelectionListener} com seleção vazia.
     */
    private static void criaEselecionaObjetoExistente(MainPanelEditor editor) throws Exception {
        ObjetoConstrucao existente = new ObjetoConstrucao();
        existente.setPosicaoQuina(new Point(500, 500));
        existente.setNome("Objeto 1");
        editor.getCircuito().getObjetosCenario().add(existente);
        editor.formularioListaObjetos.listarObjetos();
        editor.formularioListaObjetos.getList().setSelectedIndex(0);
    }

    @Test
    void cliqueUnico_posicionaObjetoConstrucao_semLancarNullPointerException() throws Exception {
        MainPanelEditor editor = editorPronto();
        criaEselecionaObjetoExistente(editor);

        ObjetoConstrucao novoObjeto = new ObjetoConstrucao();
        setField(editor, "objetoPista", novoObjeto);
        setField(editor, "posicionaObjetoPista", true);
        setField(editor, "criandoObjetoCenario", true);

        assertDoesNotThrow(() -> clicarEsquerdo(editor, new Point(200, 200)));

        var cenario = editor.getCircuito().getObjetosCenario();
        assertEquals(2, cenario.size(), "o objeto novo deveria ter sido adicionado ao circuito");
        ObjetoPista adicionado = cenario.get(1);
        assertNotNull(adicionado.getNome(), "o objeto novo não deveria ficar sem nome (bug \"Objeto N: null\")");
        assertEquals("Objeto 2", adicionado.getNome());
    }
}
