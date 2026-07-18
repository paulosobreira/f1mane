package br.flmane.editor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import javax.swing.JComboBox;
import javax.swing.JPanel;

import org.junit.jupiter.api.Test;

/**
 * Editor de circuitos não carrega nenhum circuito automaticamente ao iniciar
 * — indiceCircuito permanece -1 e o combo box não fica com nenhum item
 * pré-selecionado, mesmo com circuitos disponíveis em
 * properties/circuitos.properties (que tem várias entradas neste projeto).
 *
 * Só exercita popularCircuitos()/repopularComboCircuitos() (via reflection),
 * não iniciarComNavegacao() inteiro — esse último termina em
 * srcFrame.pack()/setExtendedState(), que exige um JFrame real e foge do
 * escopo desta validação (combo + índice).
 */
class MainPanelEditorInicializacaoTest {

    private JPanel gerarTopo(MainPanelEditor editor) throws Exception {
        Method metodo = MainPanelEditor.class.getDeclaredMethod("gerarTopoNavegacaoEAcoes");
        metodo.setAccessible(true);
        return (JPanel) metodo.invoke(editor);
    }

    private void popularCircuitos(MainPanelEditor editor) throws Exception {
        Method metodo = MainPanelEditor.class.getDeclaredMethod("popularCircuitos");
        metodo.setAccessible(true);
        metodo.invoke(editor);
    }

    private int indiceCircuito(MainPanelEditor editor) throws Exception {
        Field campo = MainPanelEditor.class.getDeclaredField("indiceCircuito");
        campo.setAccessible(true);
        return (int) campo.get(editor);
    }

    @SuppressWarnings("unchecked")
    private JComboBox<Object> comboCircuito(MainPanelEditor editor) throws Exception {
        Field campo = MainPanelEditor.class.getDeclaredField("comboCircuito");
        campo.setAccessible(true);
        return (JComboBox<Object>) campo.get(editor);
    }

    @Test
    void popularCircuitos_comListaNaoVazia_naoSelecionaNenhumCircuitoNoCombo() throws Exception {
        MainPanelEditor editor = new MainPanelEditor();
        gerarTopo(editor);

        popularCircuitos(editor);

        assertFalse(comboCircuito(editor).getSelectedIndex() >= 0,
                "combo não deveria ter nenhum circuito pré-selecionado após iniciar");
    }

    @Test
    void popularCircuitos_comListaNaoVazia_indiceCircuitoContinuaMenosUm() throws Exception {
        MainPanelEditor editor = new MainPanelEditor();
        gerarTopo(editor);

        popularCircuitos(editor);

        assertEquals(-1, indiceCircuito(editor));
    }
}
