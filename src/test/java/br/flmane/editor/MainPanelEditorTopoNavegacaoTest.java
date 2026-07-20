package br.flmane.editor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Component;
import java.awt.Container;
import java.awt.GridLayout;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JSpinner;

import org.junit.jupiter.api.Test;

/**
 * gerarTopoNavegacaoEAcoes(): 3 linhas, cada uma com seu próprio
 * GridBagLayout independente (colunas não compartilhadas entre linhas).
 * Linha 0 = navegação/salvar + nome do circuito/ciclo/tempo de volta/
 * distância; linha 1 = ativo/% chuva/largura da pista/noite/cores; linha 2 =
 * só controles de teste/visualização, incluindo o checkbox "Padrões"
 * (preview de preenchimento do ObjetoLivre) junto com Testar Escapada.
 */
class MainPanelEditorTopoNavegacaoTest {

    private JPanel gerarTopo() throws Exception {
        MainPanelEditor editor = new MainPanelEditor();
        Method metodo = MainPanelEditor.class.getDeclaredMethod("gerarTopoNavegacaoEAcoes");
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
    void topo_temTresLinhasIndependentes() throws Exception {
        JPanel topo = gerarTopo();

        assertInstanceOf(GridLayout.class, topo.getLayout());
        GridLayout layout = (GridLayout) topo.getLayout();
        assertEquals(1, layout.getColumns());
        assertEquals(3, layout.getRows());
        assertEquals(3, topo.getComponentCount());
        for (Component linha : topo.getComponents()) {
            assertInstanceOf(JPanel.class, linha);
        }
    }

    @Test
    void linha2_temOCheckboxDePadraoJuntoComEscapada() throws Exception {
        JPanel topo = gerarTopo();
        JPanel linha2 = (JPanel) topo.getComponent(2);

        // linha 2 já tinha Testar Pista/Ir ao Box/Testar Escapada (3
        // checkboxes) + TRACADO/mostrarBackground/mostrarObjetosDesenho (3
        // checkboxes) = 6; o checkbox "Padrões" some da linha 3 (removida) e
        // passa a viver aqui, junto com Testar Escapada.
        assertEquals(7, todosOsComponentesDoTipo(linha2, JCheckBox.class).size(),
                "linha 2 deveria ter o checkbox \"Padrões\" somado aos 6 já existentes");
    }

    @Test
    void linha0_naoTemNenhumCheckbox_ativoEFicaramNaLinha1() throws Exception {
        JPanel topo = gerarTopo();
        JPanel linha0 = (JPanel) topo.getComponent(0);

        assertTrue(todosOsComponentesDoTipo(linha0, JCheckBox.class).isEmpty(),
                "Ativo e Noite não deveriam estar na linha 0 — foram movidos pra linha 1");
    }

    @Test
    void linha1_temOsDoisCheckboxes_ativoENoite() throws Exception {
        JPanel topo = gerarTopo();
        JPanel linha1 = (JPanel) topo.getComponent(1);

        assertEquals(2, todosOsComponentesDoTipo(linha1, JCheckBox.class).size(),
                "linha 1 deveria ter exatamente Ativo e Noite");
    }

    @Test
    void larguraDaPistaSpinner_estaNaLinha1_eTemColunasSuficientesPraLerOValor() throws Exception {
        JPanel topo = gerarTopo();
        JPanel linha0 = (JPanel) topo.getComponent(0);
        JPanel linha1 = (JPanel) topo.getComponent(1);

        // Ciclo (spinner inteiro) fica na linha 0; largura da pista e ângulo
        // da largada (ambos double) são os dois spinners da linha 1.
        List<JSpinner> spinnersLinha1 = todosOsComponentesDoTipo(linha1, JSpinner.class);
        assertEquals(2, spinnersLinha1.size());
        for (JSpinner spinner : spinnersLinha1) {
            assertInstanceOf(JSpinner.DefaultEditor.class, spinner.getEditor());
            assertTrue(((JSpinner.DefaultEditor) spinner.getEditor()).getTextField().getColumns() >= 5,
                    "spinners da linha 1 precisam de largura mínima pra não cortar o valor exibido");
        }
        assertEquals(1, todosOsComponentesDoTipo(linha0, JSpinner.class).size(), "linha 0 só deveria ter o spinner de ciclo");
    }

    @Test
    void anguloLargadaSpinner_temFaixaDeZeroA360() throws Exception {
        JPanel topo = gerarTopo();
        JPanel linha1 = (JPanel) topo.getComponent(1);

        List<JSpinner> spinnersLinha1 = todosOsComponentesDoTipo(linha1, JSpinner.class);
        JSpinner anguloSpinner = spinnersLinha1.get(1);
        javax.swing.SpinnerNumberModel model = (javax.swing.SpinnerNumberModel) anguloSpinner.getModel();
        assertEquals(0.0, ((Number) model.getMinimum()).doubleValue());
        assertEquals(360.0, ((Number) model.getMaximum()).doubleValue());
    }

    @Test
    void comboDeCircuitos_temLarguraPreferidaLimitada() throws Exception {
        JPanel topo = gerarTopo();
        JPanel linha0 = (JPanel) topo.getComponent(0);

        List<JComboBox> combos = todosOsComponentesDoTipo(linha0, JComboBox.class);
        assertEquals(1, combos.size(), "linha 0 deveria ter só o combo de seleção de circuito");
        // getPreferredSize() do combo é sobrescrito pra nunca passar de 160px
        // de largura, mesmo com nomes de circuito bem compridos.
        assertTrue(combos.get(0).getPreferredSize().width <= 160);
    }
}
