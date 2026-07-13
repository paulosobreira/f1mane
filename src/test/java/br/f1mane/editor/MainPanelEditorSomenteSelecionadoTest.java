package br.f1mane.editor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JCheckBox;

import org.junit.jupiter.api.Test;

import br.f1mane.entidades.Circuito;
import br.f1mane.entidades.ObjetoLivre;
import br.f1mane.entidades.ObjetoPista;

/**
 * Modo "Somente Selecionado" do painel de filtro: desabilitado sem seleção,
 * ao marcar restringe a lista única ao objeto em foco (ignorando o filtro
 * por tipo), ao desmarcar restaura o filtro anterior, e é encerrado
 * automaticamente se o objeto em foco for removido — via o mesmo
 * ListSelectionListener que detecta a seleção sumindo, sem um caminho
 * separado dedicado a essa remoção específica.
 */
class MainPanelEditorSomenteSelecionadoTest {

    private static void chamarSemArgumentos(MainPanelEditor editor, String metodo) throws Exception {
        Method m = MainPanelEditor.class.getDeclaredMethod(metodo);
        m.setAccessible(true);
        m.invoke(editor);
    }

    private static JCheckBox campoCheckbox(MainPanelEditor editor, String nome) throws Exception {
        Field campo = MainPanelEditor.class.getDeclaredField(nome);
        campo.setAccessible(true);
        return (JCheckBox) campo.get(editor);
    }

    private static Object campoValor(MainPanelEditor editor, String nome) throws Exception {
        Field campo = MainPanelEditor.class.getDeclaredField(nome);
        campo.setAccessible(true);
        return campo.get(editor);
    }

    private static void setField(MainPanelEditor editor, String nome, Object valor) throws Exception {
        Field campo = MainPanelEditor.class.getDeclaredField(nome);
        campo.setAccessible(true);
        campo.set(editor, valor);
    }

    private ObjetoLivre objetoLivre(String nome) {
        ObjetoLivre objeto = new ObjetoLivre();
        objeto.setNome(nome);
        return objeto;
    }

    /**
     * gerarSecaoObjetos() não depende de srcFrame/janela montada — monta
     * formularioListaObjetos, o painel de filtro completo (com o
     * ListSelectionListener que liga a lista ao modo "Somente Selecionado")
     * e popula os checkboxes de tipo, tudo isolado da UI real.
     */
    private MainPanelEditor editorMontado(List<ObjetoPista> objetosCenario) throws Exception {
        MainPanelEditor editor = new MainPanelEditor();
        Circuito circuito = new Circuito();
        circuito.setObjetosCenario(new ArrayList<>(objetosCenario));
        circuito.setObjetos(new ArrayList<>());
        editor.setCircuito(circuito);
        chamarSemArgumentos(editor, "gerarSecaoObjetos");
        return editor;
    }

    @Test
    void checkboxDesabilitadoSemSelecao() throws Exception {
        MainPanelEditor editor = editorMontado(List.of(objetoLivre("A")));
        JCheckBox somenteSelecionado = campoCheckbox(editor, "somenteSelecionadoCheck");
        assertFalse(somenteSelecionado.isEnabled());
    }

    @Test
    void marcarComSelecao_mostraSoOObjetoSelecionado() throws Exception {
        ObjetoLivre a = objetoLivre("A");
        ObjetoLivre b = objetoLivre("B");
        MainPanelEditor editor = editorMontado(List.of(a, b));

        editor.formularioListaObjetos.selecionarSemCentralizar(a);
        JCheckBox somenteSelecionado = campoCheckbox(editor, "somenteSelecionadoCheck");
        assertTrue(somenteSelecionado.isEnabled(), "com 'a' selecionado, o checkbox deveria estar habilitado");
        somenteSelecionado.doClick();

        assertEquals(1, editor.formularioListaObjetos.getDefaultListModelOP().getSize());
        assertEquals(a, editor.formularioListaObjetos.getDefaultListModelOP().get(0));
        assertEquals(a, campoValor(editor, "focoSomenteSelecionado"));
    }

    @Test
    void desmarcar_restauraFiltroPorTipoAnterior() throws Exception {
        ObjetoLivre a = objetoLivre("A");
        ObjetoLivre b = objetoLivre("B");
        MainPanelEditor editor = editorMontado(List.of(a, b));

        editor.formularioListaObjetos.selecionarSemCentralizar(a);
        JCheckBox somenteSelecionado = campoCheckbox(editor, "somenteSelecionadoCheck");
        somenteSelecionado.doClick();
        somenteSelecionado.doClick();

        assertEquals(2, editor.formularioListaObjetos.getDefaultListModelOP().getSize());
        assertNull(campoValor(editor, "focoSomenteSelecionado"));
    }

    @Test
    void removerObjetoEmFoco_encerraModoAutomaticamente() throws Exception {
        ObjetoLivre a = objetoLivre("A");
        ObjetoLivre b = objetoLivre("B");
        MainPanelEditor editor = editorMontado(List.of(a, b));

        editor.formularioListaObjetos.selecionarSemCentralizar(a);
        JCheckBox somenteSelecionado = campoCheckbox(editor, "somenteSelecionadoCheck");
        somenteSelecionado.doClick();

        // apagarObjetoSelecionado() usa o campo "objetoPista" (o alvo dos
        // atalhos de teclado/canvas), distinto da seleção da JList.
        setField(editor, "objetoPista", a);
        editor.apagarObjetoSelecionado();

        assertFalse(somenteSelecionado.isSelected(), "remover o objeto em foco deveria desmarcar Somente Selecionado");
        assertNull(campoValor(editor, "focoSomenteSelecionado"));
        assertEquals(1, editor.formularioListaObjetos.getDefaultListModelOP().getSize());
        assertEquals(b, editor.formularioListaObjetos.getDefaultListModelOP().get(0));
    }
}
