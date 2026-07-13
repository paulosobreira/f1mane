package br.f1mane.editor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.KeyEvent;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JList;

import org.junit.jupiter.api.Test;

import br.f1mane.entidades.Circuito;
import br.f1mane.entidades.ObjetoEscapada;
import br.f1mane.entidades.ObjetoLivre;
import br.f1mane.entidades.ObjetoPista;
import br.f1mane.recursos.idiomas.Lang;

/**
 * FormularioListaObjetos.unificada(): lê concatenando objetosCenario +
 * objetos, e ao gravar (atualizarCircuito, disparado pelos botões
 * Cima/Baixo/Remover) separa cada item de volta pra coleção certa conforme
 * o tipo — mesmo movendo um item por cima de itens do outro tipo na lista
 * visual. Objetos escondidos pelo filtro do editor (tipoVisivel==false)
 * nunca entram no DefaultListModel, e não podem ser apagados do Circuito só
 * por uma reordenação/remoção feita sobre o subconjunto visível.
 */
class FormularioListaObjetosUnificadaTest {

    private ObjetoLivre objetoLivre(String nome) {
        ObjetoLivre objeto = new ObjetoLivre();
        objeto.setNome(nome);
        return objeto;
    }

    private ObjetoEscapada objetoEscapada(String nome) {
        ObjetoEscapada objeto = new ObjetoEscapada();
        objeto.setNome(nome);
        return objeto;
    }

    private static void setFoco(MainPanelEditor editor, ObjetoPista objeto) throws Exception {
        Field campo = MainPanelEditor.class.getDeclaredField("focoSomenteSelecionado");
        campo.setAccessible(true);
        campo.set(editor, objeto);
    }

    @Test
    void listarObjetos_concatenaCenarioDepoisFuncao() {
        MainPanelEditor editor = new MainPanelEditor();
        Circuito circuito = new Circuito();
        ObjetoLivre cenario = objetoLivre("Cenario");
        ObjetoEscapada funcao = objetoEscapada("Funcao");
        circuito.setObjetosCenario(new ArrayList<>(List.of(cenario)));
        circuito.setObjetos(new ArrayList<>(List.of(funcao)));
        editor.setCircuito(circuito);

        FormularioListaObjetos formulario = FormularioListaObjetos.unificada(editor);
        formulario.listarObjetos();

        assertEquals(2, formulario.getDefaultListModelOP().getSize());
        assertEquals(cenario, formulario.getDefaultListModelOP().get(0));
        assertEquals(funcao, formulario.getDefaultListModelOP().get(1));
    }

    @Test
    void moverObjetoDeFuncaoEntreObjetosDeCenario_naoMudaDeColecao() {
        MainPanelEditor editor = new MainPanelEditor();
        Circuito circuito = new Circuito();
        ObjetoLivre c1 = objetoLivre("C1");
        ObjetoLivre c2 = objetoLivre("C2");
        ObjetoEscapada f1 = objetoEscapada("F1");
        circuito.setObjetosCenario(new ArrayList<>(List.of(c1, c2)));
        circuito.setObjetos(new ArrayList<>(List.of(f1)));
        editor.setCircuito(circuito);

        FormularioListaObjetos formulario = FormularioListaObjetos.unificada(editor);
        formulario.listarObjetos();
        // modelo inicial: [c1, c2, f1] -- move f1 (índice 2) uma posição pra
        // cima, terminando entre c1 e c2: [c1, f1, c2].
        formulario.selecaoProgramatica = true;
        try {
            formulario.getList().setSelectedIndex(2);
        } finally {
            formulario.selecaoProgramatica = false;
        }
        JButton cima = botaoComTexto(formulario.getObjetos(), Lang.msg("287"));
        formulario.selecaoProgramatica = true;
        try {
            cima.doClick();
        } finally {
            formulario.selecaoProgramatica = false;
        }

        assertEquals(List.of(c1, f1, c2), listaModelo(formulario));
        assertEquals(List.of(c1, c2), circuito.getObjetosCenario());
        assertEquals(List.of(f1), circuito.getObjetos());
    }

    @Test
    void removerObjetoDaListaUnica_removeDaColecaoCorreta() {
        MainPanelEditor editor = new MainPanelEditor();
        Circuito circuito = new Circuito();
        ObjetoLivre c1 = objetoLivre("C1");
        ObjetoEscapada f1 = objetoEscapada("F1");
        circuito.setObjetosCenario(new ArrayList<>(List.of(c1)));
        circuito.setObjetos(new ArrayList<>(List.of(f1)));
        editor.setCircuito(circuito);

        FormularioListaObjetos formulario = FormularioListaObjetos.unificada(editor);
        formulario.listarObjetos();
        formulario.selecaoProgramatica = true;
        try {
            formulario.getList().setSelectedIndex(0);
        } finally {
            formulario.selecaoProgramatica = false;
        }
        JButton remover = botaoComTexto(formulario.getObjetos(), Lang.msg("removerObjetoLista"));
        remover.doClick();

        assertTrue(circuito.getObjetosCenario().isEmpty());
        assertEquals(List.of(f1), circuito.getObjetos());
    }

    @Test
    void deleteNaLista_comSelecao_removeDaColecaoCorreta() {
        MainPanelEditor editor = new MainPanelEditor();
        Circuito circuito = new Circuito();
        ObjetoLivre c1 = objetoLivre("C1");
        ObjetoEscapada f1 = objetoEscapada("F1");
        circuito.setObjetosCenario(new ArrayList<>(List.of(c1)));
        circuito.setObjetos(new ArrayList<>(List.of(f1)));
        editor.setCircuito(circuito);

        FormularioListaObjetos formulario = FormularioListaObjetos.unificada(editor);
        formulario.listarObjetos();
        formulario.selecaoProgramatica = true;
        try {
            formulario.getList().setSelectedIndex(0);
        } finally {
            formulario.selecaoProgramatica = false;
        }

        pressionarDelete(formulario.getList());

        assertTrue(circuito.getObjetosCenario().isEmpty());
        assertEquals(List.of(f1), circuito.getObjetos());
    }

    @Test
    void deleteNaLista_semSelecao_naoAlteraNada() {
        MainPanelEditor editor = new MainPanelEditor();
        Circuito circuito = new Circuito();
        ObjetoLivre c1 = objetoLivre("C1");
        circuito.setObjetosCenario(new ArrayList<>(List.of(c1)));
        circuito.setObjetos(new ArrayList<>());
        editor.setCircuito(circuito);

        FormularioListaObjetos formulario = FormularioListaObjetos.unificada(editor);
        formulario.listarObjetos();

        pressionarDelete(formulario.getList());

        assertEquals(List.of(c1), circuito.getObjetosCenario());
        assertEquals(1, formulario.getDefaultListModelOP().getSize());
    }

    /**
     * Invoca diretamente os KeyListener registrados na lista, em vez de
     * {@code dispatchEvent} — em ambiente headless (sem foco/peer real), o
     * roteamento padrão de KeyEvent via KeyboardFocusManager não entrega o
     * evento aos listeners, então chamamos {@code keyPressed} diretamente,
     * como o próprio AWT faria internamente com um componente focado.
     */
    private static void pressionarDelete(JList lista) {
        KeyEvent evento = new KeyEvent(lista, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0,
                KeyEvent.VK_DELETE, KeyEvent.CHAR_UNDEFINED);
        for (java.awt.event.KeyListener listener : lista.getKeyListeners()) {
            listener.keyPressed(evento);
        }
    }

    @Test
    void objetoEscondidoPeloFiltro_naoEhApagadoAoRemoverUmVisivel() throws Exception {
        MainPanelEditor editor = new MainPanelEditor();
        Circuito circuito = new Circuito();
        ObjetoLivre visivel = objetoLivre("Visivel");
        ObjetoLivre escondido1 = objetoLivre("Escondido1");
        ObjetoLivre escondido2 = objetoLivre("Escondido2");
        circuito.setObjetosCenario(new ArrayList<>(List.of(escondido1, visivel, escondido2)));
        circuito.setObjetos(new ArrayList<>());
        editor.setCircuito(circuito);

        FormularioListaObjetos formulario = FormularioListaObjetos.unificada(editor);
        // "Somente Selecionado" com foco em "visivel" esconde todo o resto
        // do filtro (tipoVisivel só retorna true pro objeto em foco).
        setFoco(editor, visivel);
        formulario.listarObjetos();

        assertEquals(1, formulario.getDefaultListModelOP().getSize());
        assertEquals(visivel, formulario.getDefaultListModelOP().get(0));

        // Remover o único item visível não deve apagar os escondidos do
        // circuito, mesmo eles não estando no DefaultListModel no momento
        // da escrita.
        formulario.selecaoProgramatica = true;
        try {
            formulario.getList().setSelectedIndex(0);
        } finally {
            formulario.selecaoProgramatica = false;
        }
        JButton remover = botaoComTexto(formulario.getObjetos(), Lang.msg("removerObjetoLista"));
        remover.doClick();

        assertEquals(List.of(escondido1, escondido2), circuito.getObjetosCenario());
    }

    private static List<ObjetoPista> listaModelo(FormularioListaObjetos formulario) {
        List<ObjetoPista> lista = new ArrayList<>();
        for (int i = 0; i < formulario.getDefaultListModelOP().getSize(); i++) {
            lista.add((ObjetoPista) formulario.getDefaultListModelOP().get(i));
        }
        return lista;
    }

    private static JButton botaoComTexto(Component raiz, String texto) {
        if (raiz instanceof JButton && texto.equals(((JButton) raiz).getText())) {
            return (JButton) raiz;
        }
        if (raiz instanceof Container) {
            for (Component filho : ((Container) raiz).getComponents()) {
                JButton encontrado = botaoComTexto(filho, texto);
                if (encontrado != null) {
                    return encontrado;
                }
            }
        }
        return null;
    }
}
