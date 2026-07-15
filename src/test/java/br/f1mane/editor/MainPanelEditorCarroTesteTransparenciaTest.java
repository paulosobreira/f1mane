package br.f1mane.editor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import br.f1mane.entidades.Circuito;
import br.f1mane.entidades.No;
import br.f1mane.entidades.ObjetoPista;
import br.f1mane.entidades.ObjetoTransparencia;

/**
 * Cobre a mudança transparencia-intervalo-safetycar-preview:
 * MainPanelEditor.desenhaCarroTeste passa a respeitar inicioTransparencia/
 * fimTransparencia/transparenciaBox igual ao piloto e ao safety car, em vez
 * de aplicar o recorte de qualquer ObjetoTransparencia do circuito sempre.
 */
class MainPanelEditorCarroTesteTransparenciaTest {

    private static void setField(Object alvo, Class<?> classe, String nome, Object valor) throws Exception {
        Field campo = classe.getDeclaredField(nome);
        campo.setAccessible(true);
        campo.set(alvo, valor);
    }

    private Circuito circuitoVetorizado() {
        Circuito circuito = new Circuito();
        List<No> pista = new ArrayList<>();
        pista.add(no(1000, 1000));
        pista.add(no(4000, 1000));
        pista.add(no(4000, 4000));
        pista.add(no(1000, 4000));
        circuito.setPista(pista);
        circuito.setBox(new ArrayList<>());
        circuito.setMultiplicadorLarguraPista(1.5);
        circuito.vetorizarPista();
        return circuito;
    }

    private No no(int x, int y) {
        No no = new No();
        no.setPoint(new Point(x, y));
        no.setTipo(No.RETA);
        return no;
    }

    private ObjetoTransparencia objetoComIntervalo(int inicio, int fim, Point centro, int meioLado) {
        ObjetoTransparencia objeto = new ObjetoTransparencia();
        List<Point> pontos = new ArrayList<>();
        pontos.add(new Point(centro.x - meioLado, centro.y - meioLado));
        pontos.add(new Point(centro.x + meioLado, centro.y - meioLado));
        pontos.add(new Point(centro.x + meioLado, centro.y + meioLado));
        pontos.add(new Point(centro.x - meioLado, centro.y + meioLado));
        objeto.setPontos(pontos);
        objeto.gerar();
        objeto.setPosicaoQuina(objeto.obterArea().getLocation());
        objeto.setInicioTransparencia(inicio);
        objeto.setFimTransparencia(fim);
        return objeto;
    }

    private MainPanelEditor editorComCarroDeTeste(Circuito circuito, int indexAtual, boolean estaNoBox)
            throws Exception {
        MainPanelEditor editor = new MainPanelEditor();
        editor.setCircuito(circuito);

        BufferedImage carroCima = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = carroCima.createGraphics();
        g.setColor(Color.BLUE);
        g.fillRect(0, 0, 100, 100);
        g.dispose();
        setField(editor, MainPanelEditor.class, "carroCima", carroCima);

        TestePista testePista = new TestePista(editor, circuito);
        List<No> pistaFull = circuito.getPistaFull();
        No noTeste = pistaFull.get(Math.min(indexAtual, pistaFull.size() - 1));
        if (estaNoBox) {
            testePista.posicionaCarroBox(indexAtual, noTeste, pistaFull);
        } else {
            testePista.posicionaCarro(indexAtual, noTeste, pistaFull);
        }
        testePista.setTestCar(new Point(500, 500));
        setField(editor, MainPanelEditor.class, "testePista", testePista);

        return editor;
    }

    private BufferedImage renderizaCarroTeste(MainPanelEditor editor) throws Exception {
        Method metodo = MainPanelEditor.class.getDeclaredMethod("desenhaCarroTeste", Graphics2D.class);
        metodo.setAccessible(true);
        BufferedImage saida = new BufferedImage(1000, 1000, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = saida.createGraphics();
        try {
            metodo.invoke(editor, g2d);
        } finally {
            g2d.dispose();
        }
        return saida;
    }

    @Test
    void desenhaCarroTeste_dentroDoIntervalo_aplicaORecorte() throws Exception {
        Circuito circuito = circuitoVetorizado();
        ObjetoTransparencia objeto = objetoComIntervalo(400, 600, new Point(500, 500), 25);
        List<ObjetoPista> objetos = new ArrayList<>();
        objetos.add(objeto);
        circuito.setObjetos(objetos);

        MainPanelEditor editor = editorComCarroDeTeste(circuito, 500, false);

        BufferedImage saida = renderizaCarroTeste(editor);

        int alfaNoCentro = (saida.getRGB(500, 500) >>> 24);
        assertEquals(0, alfaNoCentro, "dentro do intervalo, o carro de teste deveria ter o recorte aplicado");
    }

    @Test
    void desenhaCarroTeste_foraDoIntervalo_naoAplicaORecorte() throws Exception {
        Circuito circuito = circuitoVetorizado();
        ObjetoTransparencia objeto = objetoComIntervalo(100, 200, new Point(500, 500), 25);
        List<ObjetoPista> objetos = new ArrayList<>();
        objetos.add(objeto);
        circuito.setObjetos(objetos);

        MainPanelEditor editor = editorComCarroDeTeste(circuito, 500, false);

        BufferedImage saida = renderizaCarroTeste(editor);

        int alfaNoCentro = (saida.getRGB(500, 500) >>> 24);
        assertTrue(alfaNoCentro > 0,
                "fora do intervalo, o carro de teste não deveria ter o recorte de transparência aplicado");
    }

    @Test
    void desenhaCarroTeste_transparenciaBox_soAplicaComCarroNoBox() throws Exception {
        Circuito circuito = circuitoVetorizado();
        ObjetoTransparencia objeto = objetoComIntervalo(0, 0, new Point(500, 500), 25);
        objeto.setTransparenciaBox(true);
        List<ObjetoPista> objetos = new ArrayList<>();
        objetos.add(objeto);
        circuito.setObjetos(objetos);

        MainPanelEditor editorForaDoBox = editorComCarroDeTeste(circuito, 500, false);
        BufferedImage saidaForaDoBox = renderizaCarroTeste(editorForaDoBox);
        assertTrue((saidaForaDoBox.getRGB(500, 500) >>> 24) > 0,
                "objeto restrito ao box não deveria aplicar o recorte com o carro de teste fora do box");

        MainPanelEditor editorNoBox = editorComCarroDeTeste(circuito, 500, true);
        BufferedImage saidaNoBox = renderizaCarroTeste(editorNoBox);
        assertEquals(0, (saidaNoBox.getRGB(500, 500) >>> 24),
                "objeto restrito ao box deveria aplicar o recorte com o carro de teste no box");
    }
}
