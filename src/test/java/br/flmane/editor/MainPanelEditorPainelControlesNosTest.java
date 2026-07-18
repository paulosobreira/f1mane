package br.flmane.editor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Component;
import java.awt.GridLayout;
import java.lang.reflect.Method;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.junit.jupiter.api.Test;

import br.flmane.recursos.idiomas.Lang;

/**
 * gerarPainelControlesNos(): grid de 1 coluna com os controles de nós de
 * pista — tipo de nó (rótulo numa linha própria, acima do combo), lado do
 * box, lado de saída do box, apagar último nó e apagar nó selecionado. Os
 * combos vão direto no grid (sem painel embrulhando), pra ocupar a linha
 * toda como os botões — ver MainPanelEditor.gerarPainelControlesNos().
 */
class MainPanelEditorPainelControlesNosTest {

    private JPanel gerarPainel() throws Exception {
        MainPanelEditor editor = new MainPanelEditor();
        Method metodo = MainPanelEditor.class.getDeclaredMethod("gerarPainelControlesNos");
        metodo.setAccessible(true);
        return (JPanel) metodo.invoke(editor);
    }

    @Test
    void painel_temSeisLinhasNoGridDeUmaColuna() throws Exception {
        JPanel painel = gerarPainel();

        assertInstanceOf(GridLayout.class, painel.getLayout());
        GridLayout layout = (GridLayout) painel.getLayout();
        assertEquals(1, layout.getColumns());
        assertEquals(6, layout.getRows());
        assertEquals(6, painel.getComponentCount());
    }

    @Test
    void rotuloTipoDeNo_ficaEmLinhaPropriaAcimaDoCombo() throws Exception {
        JPanel painel = gerarPainel();

        Component primeiraLinha = painel.getComponent(0);
        Component segundaLinha = painel.getComponent(1);

        assertInstanceOf(JLabel.class, primeiraLinha, "primeira linha deveria ser só o rótulo \"Tipo de nó\"");
        assertEquals(Lang.msg("tipoDeNo"), ((JLabel) primeiraLinha).getText());
        assertInstanceOf(JComboBox.class, segundaLinha, "segunda linha deveria ser o combo de tipo de nó, sem rótulo junto");
    }

    @Test
    void combosDeLadoDoBox_vaoDiretoNoGrid_semPainelEmbrulhando() throws Exception {
        JPanel painel = gerarPainel();

        // Linhas 2 e 3 (índices 2 e 3): lado do box / saída do box, direto
        // como filhos do grid — não embrulhados num JPanel próprio (que
        // limitaria o combo ao tamanho natural em vez de ocupar a linha toda).
        assertInstanceOf(JComboBox.class, painel.getComponent(2));
        assertInstanceOf(JComboBox.class, painel.getComponent(3));
    }

    @Test
    void ultimasDuasLinhas_saoOsBotoesDeApagar() throws Exception {
        JPanel painel = gerarPainel();

        assertInstanceOf(JButton.class, painel.getComponent(4));
        assertInstanceOf(JButton.class, painel.getComponent(5));
        assertEquals(Lang.msg("105"), ((JButton) painel.getComponent(4)).getText());
        assertEquals(Lang.msg("apagaNoListaButton"), ((JButton) painel.getComponent(5)).getText());
    }

    @Test
    void todosOsComponentes_saoFilhosDiretosDoPainel_nenhumSubPainelIntermediario() throws Exception {
        JPanel painel = gerarPainel();

        for (Component componente : painel.getComponents()) {
            assertTrue(!(componente instanceof JPanel),
                    "nenhum componente deveria estar embrulhado num sub-painel — cada um ocupa a própria linha do grid");
        }
    }
}
