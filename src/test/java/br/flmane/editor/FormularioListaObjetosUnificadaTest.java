package br.flmane.editor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Component;
import java.awt.Container;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JScrollPane;

import org.junit.jupiter.api.Test;

import br.flmane.entidades.Circuito;
import br.flmane.entidades.ObjetoEscapada;
import br.flmane.entidades.ObjetoLivre;
import br.flmane.entidades.ObjetoPista;
import br.flmane.recursos.idiomas.Lang;

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

    /**
     * Monta um scrollPane com viewport, necessário pra deixar o
     * ListSelectionListener rodar sem supressão (centralizarPonto() precisa
     * de um viewport real) — usado pelos testes que verificam a sincronia
     * entre a seleção da lista e o objeto ativo do canvas, que só acontece
     * quando o listener roda de verdade (sem o guard selecaoProgramatica).
     */
    private static void configuraViewport(MainPanelEditor editor) throws Exception {
        JScrollPane scrollPane = new JScrollPane();
        scrollPane.getViewport().setSize(400, 400);
        scrollPane.getViewport().setViewPosition(new Point(0, 0));
        Field campo = MainPanelEditor.class.getDeclaredField("scrollPane");
        campo.setAccessible(true);
        campo.set(editor, scrollPane);
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

    /**
     * "Remover" precisa suportar seleção múltipla (útil junto com Copiar
     * Objetos, que também opera sobre a seleção múltipla) — regressão de um
     * bug em que só o índice retornado por getSelectedIndex() (o menor dos
     * selecionados) era removido, ignorando o resto da seleção.
     */
    @Test
    void removerVariosObjetosSelecionados_removeTodosDaColecaoCorreta() {
        MainPanelEditor editor = new MainPanelEditor();
        Circuito circuito = new Circuito();
        ObjetoLivre c1 = objetoLivre("C1");
        ObjetoLivre c2 = objetoLivre("C2");
        ObjetoEscapada f1 = objetoEscapada("F1");
        ObjetoEscapada f2 = objetoEscapada("F2");
        circuito.setObjetosCenario(new ArrayList<>(List.of(c1, c2)));
        circuito.setObjetos(new ArrayList<>(List.of(f1, f2)));
        editor.setCircuito(circuito);

        FormularioListaObjetos formulario = FormularioListaObjetos.unificada(editor);
        formulario.listarObjetos();
        // modelo unificado: [c1, c2, f1, f2] -- seleciona c1(0), f1(2), f2(3),
        // deixando só c2 de fora da seleção.
        formulario.selecaoProgramatica = true;
        try {
            formulario.getList().setSelectedIndices(new int[] { 0, 2, 3 });
        } finally {
            formulario.selecaoProgramatica = false;
        }
        JButton remover = botaoComTexto(formulario.getObjetos(), Lang.msg("removerObjetoLista"));
        // Remover índices intermediários dispara eventos de seleção "de
        // passagem" (a seleção remanescente muda a cada remove() do model)
        // que chamariam centralizarPonto() — suprime com o mesmo mecanismo
        // de seleção programática usado nos testes de Primeiro/Ultimo, já
        // que este editor não tem scrollPane montado.
        formulario.selecaoProgramatica = true;
        try {
            remover.doClick();
        } finally {
            formulario.selecaoProgramatica = false;
        }

        assertEquals(List.of(c2), circuito.getObjetosCenario());
        assertTrue(circuito.getObjetos().isEmpty());
        assertEquals(List.of(c2), listaModelo(formulario));
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

    /**
     * Regressão do "fantasma": selecionar um objeto no canvas (que já
     * sincroniza a lista, via selecionarNasListas) e depois selecionar um
     * objeto DIFERENTE só pela lista deixava o objeto do canvas "preso" como
     * ativo — ainda respondendo a atalhos de teclado, ainda desenhado com seu
     * próprio contorno, mesmo a lista já mostrando outro selecionado. Agora
     * a seleção da lista também sincroniza de volta o objeto ativo do canvas.
     */
    @Test
    void selecionarObjetoDiferenteNaLista_atualizaObjetoAtivoDoCanvas() throws Exception {
        MainPanelEditor editor = new MainPanelEditor();
        Circuito circuito = new Circuito();
        ObjetoLivre antigo = objetoLivre("Antigo");
        ObjetoLivre novo = objetoLivre("Novo");
        circuito.setObjetosCenario(new ArrayList<>(List.of(antigo, novo)));
        circuito.setObjetos(new ArrayList<>());
        editor.setCircuito(circuito);
        configuraViewport(editor);
        // Simula uma seleção anterior vinda de um clique direto no canvas
        // (que não passa pelo ListSelectionListener).
        editor.setObjetoPista(antigo);

        FormularioListaObjetos formulario = FormularioListaObjetos.unificada(editor);
        formulario.listarObjetos();
        formulario.getList().setSelectedIndex(1);

        assertSame(novo, editor.getObjetoPista(),
                "selecionar um objeto diferente na lista deveria atualizar o objeto ativo do canvas, sem deixar o antigo fantasma");
    }

    @Test
    void selecaoMultiplaNaLista_zeraObjetoAtivoDoCanvas() throws Exception {
        MainPanelEditor editor = new MainPanelEditor();
        Circuito circuito = new Circuito();
        ObjetoLivre a = objetoLivre("A");
        ObjetoLivre b = objetoLivre("B");
        circuito.setObjetosCenario(new ArrayList<>(List.of(a, b)));
        circuito.setObjetos(new ArrayList<>());
        editor.setCircuito(circuito);
        configuraViewport(editor);
        editor.setObjetoPista(a);

        FormularioListaObjetos formulario = FormularioListaObjetos.unificada(editor);
        formulario.listarObjetos();
        formulario.getList().setSelectedIndices(new int[] { 0, 1 });

        assertNull(editor.getObjetoPista(),
                "com seleção múltipla, nenhum objeto único deveria responder aos atalhos de edição");
    }

    @Test
    void limparSelecaoNaLista_zeraObjetoAtivoDoCanvas() throws Exception {
        MainPanelEditor editor = new MainPanelEditor();
        Circuito circuito = new Circuito();
        ObjetoLivre a = objetoLivre("A");
        circuito.setObjetosCenario(new ArrayList<>(List.of(a)));
        circuito.setObjetos(new ArrayList<>());
        editor.setCircuito(circuito);
        configuraViewport(editor);

        FormularioListaObjetos formulario = FormularioListaObjetos.unificada(editor);
        formulario.listarObjetos();
        formulario.getList().setSelectedIndex(0);
        formulario.getList().clearSelection();

        assertNull(editor.getObjetoPista());
    }

    /**
     * O canvas nunca deve criar nem manter seleção múltipla — clicar num
     * objeto no canvas sempre colapsa a seleção pra esse único objeto, mesmo
     * que uma seleção múltipla feita antes pela lista já inclua esse mesmo
     * objeto como o de MENOR índice (getSelectedIndex() só devolve o menor
     * dos selecionados numa seleção múltipla, então esse caso específico
     * "batia" com o índice procurado e o antigo código não recolhia a
     * seleção pra um único item).
     */
    @Test
    void selecionarSemCentralizar_colapsaSelecaoMultiplaMesmoQuandoObjetoJaEhOMenorIndiceSelecionado() {
        MainPanelEditor editor = new MainPanelEditor();
        Circuito circuito = new Circuito();
        ObjetoLivre a = objetoLivre("A");
        ObjetoLivre b = objetoLivre("B");
        ObjetoLivre c = objetoLivre("C");
        circuito.setObjetosCenario(new ArrayList<>(List.of(a, b, c)));
        circuito.setObjetos(new ArrayList<>());
        editor.setCircuito(circuito);

        FormularioListaObjetos formulario = FormularioListaObjetos.unificada(editor);
        formulario.listarObjetos();
        // Seleção múltipla feita pela lista: a (índice 0, o "menor") e c
        // (índice 2).
        selecionarIndicesSemCentralizar(formulario, 0, 2);

        // Clique no canvas no objeto "a" — já é o menor índice selecionado,
        // caso em que o bug antigo não recolhia a seleção.
        formulario.selecionarSemCentralizar(a);

        assertEquals(1, formulario.getList().getSelectedIndices().length,
                "clicar no canvas deveria colapsar a seleção múltipla pra um único item");
        assertEquals(0, formulario.getList().getSelectedIndex());
    }

    private static void selecionarIndicesSemCentralizar(FormularioListaObjetos formulario, int... indices) {
        formulario.selecaoProgramatica = true;
        try {
            formulario.getList().setSelectedIndices(indices);
        } finally {
            formulario.selecaoProgramatica = false;
        }
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
