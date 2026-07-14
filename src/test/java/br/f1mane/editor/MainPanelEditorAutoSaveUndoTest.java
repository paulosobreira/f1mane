package br.f1mane.editor;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Color;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JTextField;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import br.f1mane.entidades.Circuito;
import br.f1mane.entidades.No;
import br.f1mane.entidades.ObjetoEscapada;
import br.f1mane.entidades.ObjetoLivre;
import br.f1mane.entidades.ObjetoPista;

/**
 * Cobre o salvamento automático ao incluir um objeto (com backup do estado
 * anterior) e o desfazer (Ctrl+Z) que restaura esse backup — ver capability
 * editor-autosave-undo. Usa o mesmo padrão de disparo de MouseEvent real das
 * demais suítes do editor (ex.: {@code MainPanelEditorEscapadaCliqueTest}),
 * evitando qualquer diálogo Swing real via {@link MainPanelEditorTestDouble}.
 */
class MainPanelEditorAutoSaveUndoTest {

    @TempDir
    Path tempDir;

    private File arquivoObjetos;
    private File arquivoMeta;

    @BeforeEach
    void configurarArquivos() {
        arquivoObjetos = tempDir.resolve("teste_mro.xml").toFile();
        arquivoMeta = tempDir.resolve("teste_mro_meta.xml").toFile();
    }

    @AfterEach
    void limparArquivos() {
        arquivoObjetos.delete();
        arquivoMeta.delete();
        new File(tempDir.toFile(), "teste_mro.xml.bak").delete();
        new File(tempDir.toFile(), "teste_mro_meta.xml.bak").delete();
    }

    private Circuito circuitoVetorizadoComLargada() {
        Circuito circuito = new Circuito();
        List<No> pista = new ArrayList<>();
        pista.add(criarNo(1000, 1000, No.LARGADA));
        pista.add(criarNo(4000, 1000, No.RETA));
        pista.add(criarNo(4000, 4000, No.RETA));
        pista.add(criarNo(1000, 4000, No.RETA));
        circuito.setPista(pista);
        circuito.setBox(new ArrayList<>());
        circuito.setMultiplicadorLarguraPista(1.5);
        circuito.vetorizarPista(9, 1.5);
        return circuito;
    }

    private No criarNo(int x, int y, Color tipo) {
        No no = new No();
        no.setPoint(new Point(x, y));
        no.setTipo(tipo);
        return no;
    }

    /**
     * Monta o mínimo de UI necessário para {@code vetorizarCircuito(false)}
     * (chamado por {@code gravarCircuitoEmDisco}) não precisar de uma janela
     * real: lista de nós da pista (com um nó de largada), combos de lado do
     * box e os campos de texto lidos por {@code sincronizarCamposNoCircuito}.
     */
    private MainPanelEditorTestDouble criarEditorProntoParaGravar(Circuito circuito, File file) throws Exception {
        MainPanelEditorTestDouble editor = new MainPanelEditorTestDouble();
        editor.setCircuito(circuito);
        ativarMouseListener(editor);

        DefaultListModel<No> modeloPista = new DefaultListModel<>();
        for (No no : circuito.getPista()) {
            modeloPista.addElement(no);
        }
        setField(editor, "pistaJList", new JList<>(modeloPista));
        setField(editor, "ladoBoxCombo", new JComboBox<String>(new String[] {"1", "2"}));
        setField(editor, "ladoBoxSaidaBoxCombo", new JComboBox<String>(new String[] {"1", "2"}));
        setField(editor, "testePista", new TestePista(editor, circuito));
        // Numa sessão real, esse campo só fica sincronizado com o circuito depois da
        // primeira vetorizarCircuito() rodada por aplicarCircuitoCarregadoNaUI() ao
        // carregar o circuito; aqui pulamos esse carregamento completo (ver
        // restaurarCircuitoDoBackup()/desfazerUltimaInclusao() sobre não depender de
        // srcFrame), então precisa ser setado manualmente pro mesmo valor do circuito.
        setField(editor, "multiplicadorLarguraPista", circuito.getMultiplicadorLarguraPista());
        setField(editor, "nomePistaText", new JTextField("Circuito Teste"));
        setField(editor, "probalidadeChuvaText", new JTextField("0"));
        setField(editor, "distanciaKmText", new JTextField(""));
        setField(editor, "file", file);
        return editor;
    }

    private static void ativarMouseListener(MainPanelEditor editor) throws Exception {
        Method metodo = MainPanelEditor.class.getDeclaredMethod("adicionaEventosMouse", JFrame.class);
        metodo.setAccessible(true);
        metodo.invoke(editor, (JFrame) null);
        editor.formularioListaObjetos = FormularioListaObjetos.unificada(editor);
    }

    private static void setField(MainPanelEditor editor, String nome, Object valor) throws Exception {
        Field campo = MainPanelEditor.class.getDeclaredField(nome);
        campo.setAccessible(true);
        campo.set(editor, valor);
    }

    private static boolean invocarPrivadoBoolean(MainPanelEditor editor, String nome) throws Exception {
        Method metodo = MainPanelEditor.class.getDeclaredMethod(nome);
        metodo.setAccessible(true);
        return (boolean) metodo.invoke(editor);
    }

    private static void clicar(MainPanelEditor editor, Point ponto, int botao) {
        MouseEvent evento = new MouseEvent(editor, MouseEvent.MOUSE_CLICKED, System.currentTimeMillis(), 0,
                ponto.x, ponto.y, 1, botao == MouseEvent.BUTTON3, botao);
        editor.dispatchEvent(evento);
    }

    /** Finaliza uma ObjetoEscapada por clique (entrada / meio / saída), disparando o mesmo ponto de inclusão que os outros tipos de objeto usam. */
    private void incluirObjetoPorClique(MainPanelEditorTestDouble editor, Circuito circuito) {
        ObjetoEscapada escapada = new ObjetoEscapada();
        editor.setObjetoPista(escapada);
        try {
            Field desenhando = MainPanelEditor.class.getDeclaredField("desenhandoObjetoLivre");
            desenhando.setAccessible(true);
            desenhando.set(editor, true);
            Field posiciona = MainPanelEditor.class.getDeclaredField("posicionaObjetoPista");
            posiciona.setAccessible(true);
            posiciona.set(editor, true);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        No noEntrada = circuito.getPista1Full().get(300);
        Point pontoLivre = new Point(2500, 2500);
        No noSaida = circuito.getPista1Full().get(500);

        clicar(editor, noEntrada.getPoint(), MouseEvent.BUTTON1);
        clicar(editor, pontoLivre, MouseEvent.BUTTON1);
        clicar(editor, noSaida.getPoint(), MouseEvent.BUTTON3);
    }

    @Test
    void incluirObjeto_comFileAssociado_gravaArquivosSemBackupNaPrimeiraVez() throws Exception {
        Circuito circuito = circuitoVetorizadoComLargada();
        MainPanelEditorTestDouble editor = criarEditorProntoParaGravar(circuito, arquivoObjetos);

        assertFalse(arquivoObjetos.exists(), "arquivo principal não deveria existir antes da inclusão");

        incluirObjetoPorClique(editor, circuito);

        assertTrue(arquivoObjetos.exists(), "autosave deveria ter gravado o arquivo de objetos");
        assertTrue(arquivoMeta.exists(), "autosave deveria ter gravado o arquivo de metadados");
        assertFalse(new File(tempDir.toFile(), "teste_mro.xml.bak").exists(),
                "sem estado anterior em disco, não deveria ter sido criado backup");
        assertTrue(circuito.getObjetos() != null && !circuito.getObjetos().isEmpty(),
                "o objeto incluído deveria estar no circuito em memória");
    }

    @Test
    void copiarObjeto_altC_tambemDisparaAutoSave() throws Exception {
        Circuito circuito = circuitoVetorizadoComLargada();
        MainPanelEditorTestDouble editor = criarEditorProntoParaGravar(circuito, arquivoObjetos);

        ObjetoLivre original = new ObjetoLivre();
        List<Point> pontosObjeto = new ArrayList<>();
        pontosObjeto.add(new Point(1500, 1500));
        pontosObjeto.add(new Point(1600, 1500));
        pontosObjeto.add(new Point(1550, 1580));
        original.setPontos(pontosObjeto);
        original.gerar();
        original.setPosicaoQuina(original.obterArea().getLocation());
        List<ObjetoPista> objetosCenario = new ArrayList<>();
        objetosCenario.add(original);
        circuito.setObjetosCenario(objetosCenario);
        editor.setObjetoPista(original);

        assertFalse(arquivoObjetos.exists(), "arquivo principal não deveria existir antes da cópia");

        editor.copiarObjeto();

        assertTrue(arquivoObjetos.exists(),
                "copiar objeto via Alt+C deveria disparar o salvamento automático, igual a criar um objeto novo");
        assertEquals(2, circuito.getObjetosCenario().size(), "deveria ter o original mais a cópia na mesma lista");
    }

    @Test
    void incluirObjeto_semFileAssociado_naoGravaMasIncluiEmMemoria() throws Exception {
        Circuito circuito = circuitoVetorizadoComLargada();
        MainPanelEditorTestDouble editor = criarEditorProntoParaGravar(circuito, null);

        assertDoesNotThrow(() -> incluirObjetoPorClique(editor, circuito));

        assertFalse(arquivoObjetos.exists(), "sem file associado, autosave não deveria gravar nada em disco");
        assertTrue(circuito.getObjetos() != null && !circuito.getObjetos().isEmpty(),
                "o objeto deveria continuar sendo incluído normalmente em memória");
    }

    @Test
    void autoSalvarComBackup_chamadoDuasVezes_backupReflecteApenasOEstadoAnteriorAoUltimo() throws Exception {
        Files.write(arquivoObjetos.toPath(), "ESTADO-INICIAL".getBytes(StandardCharsets.UTF_8));
        Files.write(arquivoMeta.toPath(), "ESTADO-INICIAL-META".getBytes(StandardCharsets.UTF_8));

        Circuito circuito = circuitoVetorizadoComLargada();
        MainPanelEditorTestDouble editor = criarEditorProntoParaGravar(circuito, arquivoObjetos);

        Method autoSalvar = MainPanelEditor.class.getDeclaredMethod("autoSalvarComBackup");
        autoSalvar.setAccessible(true);
        autoSalvar.invoke(editor);

        String estadoAposPrimeiroSave = new String(Files.readAllBytes(arquivoObjetos.toPath()), StandardCharsets.UTF_8);
        String backupAposPrimeiroSave = new String(
                Files.readAllBytes(new File(tempDir.toFile(), "teste_mro.xml.bak").toPath()), StandardCharsets.UTF_8);
        assertEquals("ESTADO-INICIAL", backupAposPrimeiroSave,
                "primeiro backup deveria conter o estado gravado manualmente antes de qualquer autosave");

        autoSalvar.invoke(editor);

        String backupAposSegundoSave = new String(
                Files.readAllBytes(new File(tempDir.toFile(), "teste_mro.xml.bak").toPath()), StandardCharsets.UTF_8);
        assertEquals(estadoAposPrimeiroSave, backupAposSegundoSave,
                "backup após o segundo autosave deveria refletir o estado gravado pelo primeiro, não o estado inicial");
    }

    @Test
    void desfazer_restauraArquivosEModeloEmMemoria() throws Exception {
        Circuito circuito = circuitoVetorizadoComLargada();
        MainPanelEditorTestDouble editor = criarEditorProntoParaGravar(circuito, arquivoObjetos);

        // Primeira gravação (sem backup ainda, como no teste acima) estabelece o "estado 0" em disco.
        Method autoSalvar = MainPanelEditor.class.getDeclaredMethod("autoSalvarComBackup");
        autoSalvar.setAccessible(true);
        autoSalvar.invoke(editor);
        byte[] estadoAntesDaInclusao = Files.readAllBytes(arquivoObjetos.toPath());

        incluirObjetoPorClique(editor, circuito);
        assertTrue(circuito.getObjetos() != null && !circuito.getObjetos().isEmpty(),
                "objeto deveria ter sido incluído antes do desfazer");

        boolean restaurou = invocarPrivadoBoolean(editor, "restaurarCircuitoDoBackup");

        assertTrue(restaurou, "deveria ter encontrado backup e restaurado");
        byte[] estadoRestaurado = Files.readAllBytes(arquivoObjetos.toPath());
        assertEquals(new String(estadoAntesDaInclusao, StandardCharsets.UTF_8),
                new String(estadoRestaurado, StandardCharsets.UTF_8),
                "arquivo principal deveria voltar a ter o conteúdo de antes da inclusão do objeto");
        Circuito circuitoRestaurado = editor.getCircuito();
        assertTrue(circuitoRestaurado.getObjetos() == null || circuitoRestaurado.getObjetos().isEmpty(),
                "circuito recarregado a partir do backup não deveria conter o objeto incluído depois dele");
    }

    @Test
    void desfazer_semBackupDisponivel_naoLancaExcecaoNemAlteraNada() throws Exception {
        Circuito circuito = circuitoVetorizadoComLargada();
        MainPanelEditorTestDouble editor = criarEditorProntoParaGravar(circuito, arquivoObjetos);

        // Nenhum autosave ocorreu ainda: nem o arquivo principal nem o backup existem.
        assertFalse(arquivoObjetos.exists());

        boolean restaurou = assertDoesNotThrow(() -> invocarPrivadoBoolean(editor, "restaurarCircuitoDoBackup"));

        assertFalse(restaurou, "sem backup, não deveria restaurar nada");
        assertFalse(arquivoObjetos.exists(), "nenhum arquivo deveria ter sido criado por uma restauração sem backup");
    }
}
