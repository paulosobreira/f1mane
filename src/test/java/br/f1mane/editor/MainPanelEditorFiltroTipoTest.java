package br.f1mane.editor;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import br.f1mane.entidades.Circuito;
import br.f1mane.entidades.ObjetoArquibancada;
import br.f1mane.entidades.ObjetoPista;

/**
 * Filtro por tipo (tiposVisiveis): cumulativo e independente do checkbox
 * global "Objetos" (desenhaObjetosDesenho) — desmarcar um tipo esconde os
 * objetos desse tipo tanto do desenho do canvas quanto da seleção por
 * clique, sem remover nada do circuito nem afetar outros tipos.
 */
class MainPanelEditorFiltroTipoTest {

    @SuppressWarnings("unchecked")
    private static void ocultarTipo(MainPanelEditor editor, TipoObjetoPista tipo) throws Exception {
        Field campo = MainPanelEditor.class.getDeclaredField("tiposVisiveis");
        campo.setAccessible(true);
        Map<TipoObjetoPista, Boolean> tiposVisiveis = (Map<TipoObjetoPista, Boolean>) campo.get(editor);
        tiposVisiveis.put(tipo, false);
    }

    @SuppressWarnings("unchecked")
    private static void mostrarTipo(MainPanelEditor editor, TipoObjetoPista tipo) throws Exception {
        Field campo = MainPanelEditor.class.getDeclaredField("tiposVisiveis");
        campo.setAccessible(true);
        Map<TipoObjetoPista, Boolean> tiposVisiveis = (Map<TipoObjetoPista, Boolean>) campo.get(editor);
        tiposVisiveis.put(tipo, true);
    }

    private static void setDesenhaObjetosDesenho(MainPanelEditor editor, boolean valor) throws Exception {
        Field campo = MainPanelEditor.class.getDeclaredField("desenhaObjetosDesenho");
        campo.setAccessible(true);
        campo.set(editor, valor);
    }

    private ObjetoArquibancada objetoEm(int x, int y, Color cor) {
        ObjetoArquibancada objeto = new ObjetoArquibancada();
        // Arquibancada é desenhada por um encadeamento de pontos (ver
        // ObjetoArquibancada): sem pontos, não há área visível.
        List<Point> pontos = new ArrayList<>();
        pontos.add(new Point(0, 0));
        pontos.add(new Point(0, 20));
        objeto.setPontos(pontos);
        objeto.gerar();
        objeto.setPosicaoQuina(new Point(x, y));
        objeto.setLargura(20);
        objeto.setCorPimaria(cor);
        return objeto;
    }

    @SuppressWarnings("unchecked")
    private BufferedImage renderiza(MainPanelEditor editor) throws Exception {
        Method todosObjetos = MainPanelEditor.class.getDeclaredMethod("todosObjetos");
        todosObjetos.setAccessible(true);
        List<ObjetoPista> todos = (List<ObjetoPista>) todosObjetos.invoke(editor);

        Method metodo = MainPanelEditor.class.getDeclaredMethod(
                "desenhaObjetosNivel", Graphics2D.class, int.class, List.class);
        metodo.setAccessible(true);
        BufferedImage imagem = new BufferedImage(400, 400, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = imagem.createGraphics();
        try {
            metodo.invoke(editor, g2d, 0, todos);
        } finally {
            g2d.dispose();
        }
        return imagem;
    }

    private static boolean imagemContemCor(BufferedImage imagem, Color cor) {
        int alvo = cor.getRGB();
        for (int y = 0; y < imagem.getHeight(); y++) {
            for (int x = 0; x < imagem.getWidth(); x++) {
                if (imagem.getRGB(x, y) == alvo) {
                    return true;
                }
            }
        }
        return false;
    }

    @Test
    void tipoDesmarcado_naoAparaceNoDesenho() throws Exception {
        MainPanelEditor editor = new MainPanelEditor();
        Circuito circuito = new Circuito();
        ObjetoArquibancada objeto = objetoEm(100, 100, new Color(170, 20, 90));
        circuito.setObjetosCenario(new ArrayList<>(List.of(objeto)));
        editor.setCircuito(circuito);

        ocultarTipo(editor, TipoObjetoPista.ARQUIBANCADA);
        BufferedImage imagem = renderiza(editor);

        assertFalse(imagemContemCor(imagem, new Color(170, 20, 90)),
                "objeto de um tipo desmarcado no filtro não deveria aparecer no desenho");
    }

    @Test
    void tipoMarcado_apareceNormalmente() throws Exception {
        MainPanelEditor editor = new MainPanelEditor();
        Circuito circuito = new Circuito();
        ObjetoArquibancada objeto = objetoEm(100, 100, new Color(170, 20, 90));
        circuito.setObjetosCenario(new ArrayList<>(List.of(objeto)));
        editor.setCircuito(circuito);

        BufferedImage imagem = renderiza(editor);

        assertTrue(imagemContemCor(imagem, new Color(170, 20, 90)),
                "sem filtro ativo, o objeto deveria aparecer normalmente");
    }

    @Test
    void filtroPorTipoECheckboxGlobal_saoCumulativos() throws Exception {
        MainPanelEditor editor = new MainPanelEditor();
        Circuito circuito = new Circuito();
        Color cor = new Color(170, 20, 90);
        ObjetoArquibancada objeto = objetoEm(100, 100, cor);
        circuito.setObjetosCenario(new ArrayList<>(List.of(objeto)));
        editor.setCircuito(circuito);

        // checkbox global "Objetos" desligado: some, mesmo com o tipo
        // ainda marcado no filtro.
        setDesenhaObjetosDesenho(editor, false);
        assertFalse(imagemContemCor(renderiza(editor), cor));

        // reativa o global, mas desmarca o filtro por tipo: continua escondido.
        setDesenhaObjetosDesenho(editor, true);
        ocultarTipo(editor, TipoObjetoPista.ARQUIBANCADA);
        assertFalse(imagemContemCor(renderiza(editor), cor));

        // os dois desligados ao mesmo tempo: continua escondido (cumulativo,
        // não um cancela o outro).
        setDesenhaObjetosDesenho(editor, false);
        assertFalse(imagemContemCor(renderiza(editor), cor));

        // com os dois ligados de novo, o objeto volta a aparecer.
        setDesenhaObjetosDesenho(editor, true);
        mostrarTipo(editor, TipoObjetoPista.ARQUIBANCADA);
        assertTrue(imagemContemCor(renderiza(editor), cor));
    }

    @Test
    void objetoComTipoEscondido_naoEhSelecionavelPorCliqueNoCanvas() throws Exception {
        MainPanelEditor editor = new MainPanelEditor();
        Circuito circuito = new Circuito();
        ObjetoArquibancada objeto = objetoEm(100, 100, new Color(170, 20, 90));
        circuito.setObjetosCenario(new ArrayList<>(List.of(objeto)));
        editor.setCircuito(circuito);
        // obterArea() de ObjetoArquibancada só reflete a forma real depois
        // da primeira chamada a desenha() (ver comentário de
        // estaVisivelNoViewport em MainPanelEditor) -- renderiza uma vez
        // antes de testar o clique.
        renderiza(editor);

        assertNotNull(editor.encontraObjetoPista(new Point(105, 105)),
                "sem filtro ativo, o clique deveria encontrar o objeto normalmente");

        ocultarTipo(editor, TipoObjetoPista.ARQUIBANCADA);

        assertNull(editor.encontraObjetoPista(new Point(105, 105)),
                "objeto de um tipo escondido pelo filtro não deveria ser selecionável por clique no canvas");
    }
}
