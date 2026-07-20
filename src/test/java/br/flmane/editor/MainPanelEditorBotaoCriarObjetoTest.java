package br.flmane.editor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import java.awt.GridLayout;
import java.lang.reflect.Method;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.junit.jupiter.api.Test;

import br.flmane.recursos.idiomas.Lang;

/**
 * gerarBotaoCriarObjeto(): botões Criar Objeto/Copiar Cor/Colar Cor/Copiar
 * Objetos/Colar Objetos seguidos da legenda dos atalhos de teclado de objeto
 * (Insert/Delete/Page Up/Page Down) — mesmos atalhos ativados em
 * EditorCircuitos.ativarKeysEditor().
 */
class MainPanelEditorBotaoCriarObjetoTest {

    private JPanel gerarPainel() throws Exception {
        MainPanelEditor editor = new MainPanelEditor();
        Method metodo = MainPanelEditor.class.getDeclaredMethod("gerarBotaoCriarObjeto");
        metodo.setAccessible(true);
        return (JPanel) metodo.invoke(editor);
    }

    @Test
    void painel_temCincoBotoesMaisQuatroLinhasDeLegenda() throws Exception {
        JPanel painel = gerarPainel();

        assertInstanceOf(GridLayout.class, painel.getLayout());
        GridLayout layout = (GridLayout) painel.getLayout();
        assertEquals(1, layout.getColumns());
        assertEquals(5, layout.getRows());
        assertEquals(5, painel.getComponentCount());
    }

    @Test
    void primeirosCincoComponentes_saoOsBotoesDeObjetoCorECopiarColarObjetos() throws Exception {
        JPanel painel = gerarPainel();

        assertInstanceOf(JButton.class, painel.getComponent(0));
        assertInstanceOf(JButton.class, painel.getComponent(1));
        assertInstanceOf(JButton.class, painel.getComponent(2));
        assertInstanceOf(JButton.class, painel.getComponent(3));
        assertInstanceOf(JButton.class, painel.getComponent(4));
        assertEquals(Lang.msg("criarObjeto"), ((JButton) painel.getComponent(0)).getText());
        assertEquals(Lang.msg("copiarCor"), ((JButton) painel.getComponent(1)).getText());
        assertEquals(Lang.msg("colarCor"), ((JButton) painel.getComponent(2)).getText());
        assertEquals(Lang.msg("copiarObjetos"), ((JButton) painel.getComponent(3)).getText());
        assertEquals(Lang.msg("colarObjetos"), ((JButton) painel.getComponent(4)).getText());
    }

}
