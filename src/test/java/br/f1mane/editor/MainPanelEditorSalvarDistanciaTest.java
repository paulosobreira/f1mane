package br.f1mane.editor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import javax.swing.JTextField;

import org.junit.jupiter.api.Test;

import br.f1mane.entidades.Circuito;

/**
 * salvarPista() bloqueia o salvamento quando o campo de distância em
 * quilômetros não foi informado (vazio) ou é <= 0 — checagem roda antes do
 * JFileChooser/vetorizarCircuito(), então o teste de bloqueio só precisa do
 * campo distanciaKmText montado, sem o restante da UI do editor.
 */
class MainPanelEditorSalvarDistanciaTest {

    private int parseDistanciaKmOuZero(MainPanelEditor editor, String texto) throws Exception {
        Method metodo = MainPanelEditor.class.getDeclaredMethod("parseDistanciaKmOuZero", String.class);
        metodo.setAccessible(true);
        return (int) metodo.invoke(editor, texto);
    }

    private void setDistanciaKmText(MainPanelEditor editor, String texto) throws Exception {
        Field campo = MainPanelEditor.class.getDeclaredField("distanciaKmText");
        campo.setAccessible(true);
        campo.set(editor, new JTextField(texto));
    }

    @Test
    void parseDistanciaKmOuZero_comTextoVazio_retornaZero() throws Exception {
        MainPanelEditor editor = new MainPanelEditor();
        assertEquals(0, parseDistanciaKmOuZero(editor, ""));
        assertEquals(0, parseDistanciaKmOuZero(editor, "   "));
    }

    @Test
    void parseDistanciaKmOuZero_comTextoNaoNumerico_retornaZero() throws Exception {
        MainPanelEditor editor = new MainPanelEditor();
        assertEquals(0, parseDistanciaKmOuZero(editor, "abc"));
    }

    @Test
    void parseDistanciaKmOuZero_comValorValido_retornaOValor() throws Exception {
        MainPanelEditor editor = new MainPanelEditor();
        assertEquals(5, parseDistanciaKmOuZero(editor, "5"));
        assertEquals(5, parseDistanciaKmOuZero(editor, "  5  "));
    }

    @Test
    void salvar_comCampoDeDistanciaVazio_ehBloqueadoSemLancarExcecao() throws Exception {
        MainPanelEditorTestDouble editor = new MainPanelEditorTestDouble();
        editor.setCircuito(new Circuito());
        setDistanciaKmText(editor, "");

        assertDoesNotThrow(editor::salvarPista);

        assertEquals(1, editor.getAlertasDistanciaNaoInformada());
    }

    @Test
    void salvar_comDistanciaZero_ehBloqueado() throws Exception {
        MainPanelEditorTestDouble editor = new MainPanelEditorTestDouble();
        editor.setCircuito(new Circuito());
        setDistanciaKmText(editor, "0");

        editor.salvarPista();

        assertEquals(1, editor.getAlertasDistanciaNaoInformada());
    }
}
