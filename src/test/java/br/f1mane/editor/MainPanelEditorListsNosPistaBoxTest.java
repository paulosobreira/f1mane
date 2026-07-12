package br.f1mane.editor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JSplitPane;

import org.junit.jupiter.api.Test;

/**
 * gerarListsNosPistaBox(): lista de nós da pista e lista de nós do box
 * dentro de um JSplitPane vertical redimensionável, em vez do GridLayout(2,1)
 * de altura fixa anterior — mesmo padrão de gerarSecaoObjetos()/splitListas.
 */
class MainPanelEditorListsNosPistaBoxTest {

    private JPanel gerarListas() throws Exception {
        MainPanelEditor editor = new MainPanelEditor();
        Method metodo = MainPanelEditor.class.getDeclaredMethod("gerarListsNosPistaBox");
        metodo.setAccessible(true);
        return (JPanel) metodo.invoke(editor);
    }

    private static <T> List<T> todosOsComponentesDoTipo(Container raiz, Class<T> tipo) {
        List<T> encontrados = new ArrayList<>();
        for (Component filho : raiz.getComponents()) {
            if (tipo.isInstance(filho)) {
                encontrados.add(tipo.cast(filho));
            }
            if (filho instanceof Container) {
                encontrados.addAll(todosOsComponentesDoTipo((Container) filho, tipo));
            }
        }
        return encontrados;
    }

    @Test
    void painel_temUmJSplitPaneVerticalNoCentro() throws Exception {
        JPanel painel = gerarListas();

        assertInstanceOf(BorderLayout.class, painel.getLayout());
        Component centro = ((BorderLayout) painel.getLayout()).getLayoutComponent(BorderLayout.CENTER);
        assertInstanceOf(JSplitPane.class, centro);
        assertEquals(JSplitPane.VERTICAL_SPLIT, ((JSplitPane) centro).getOrientation());
    }

    @Test
    void splitPane_ehRedimensionavel_comProporcaoInicialSimetrica() throws Exception {
        JPanel painel = gerarListas();
        JSplitPane split = (JSplitPane) ((BorderLayout) painel.getLayout()).getLayoutComponent(BorderLayout.CENTER);

        assertEquals(0.5, split.getResizeWeight());
        assertTrue(split.isOneTouchExpandable(), "divisor deveria ser arrastável/expansível (oneTouchExpandable)");
    }

    @Test
    void ambasAsListasDeNos_continuamPresentesDentroDoSplitPane() throws Exception {
        JPanel painel = gerarListas();

        List<JList> listas = todosOsComponentesDoTipo(painel, JList.class);
        assertEquals(2, listas.size(), "deveria continuar tendo exatamente as duas listas (pista e box)");
    }
}
