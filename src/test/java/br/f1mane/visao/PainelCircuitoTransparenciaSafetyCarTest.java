package br.f1mane.visao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import br.f1mane.controles.InterfaceJogo;
import br.f1mane.entidades.Circuito;
import br.f1mane.entidades.No;
import br.f1mane.entidades.ObjetoPista;
import br.f1mane.entidades.ObjetoTransparencia;
import br.f1mane.entidades.SafetyCar;

/**
 * Cobre a mudança transparencia-intervalo-safetycar-preview: o utilitário
 * {@code transparenciaAplicavel} extraído de {@code desenhaCarroCima}, e sua
 * aplicação em {@code desenharSafetyCarCima} — o safety car não deveria mais
 * ficar "furado" por um ObjetoTransparencia fora do intervalo de nó
 * configurado, igual já acontecia (corretamente) com o piloto.
 */
class PainelCircuitoTransparenciaSafetyCarTest {

    private static void setField(Object alvo, String nome, Object valor) throws Exception {
        Field campo = PainelCircuito.class.getDeclaredField(nome);
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
        circuito.setUsaBkg(true);
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

    // --- transparenciaAplicavel (5.1) ---

    private boolean transparenciaAplicavel(PainelCircuito painel, ObjetoPista objetoPista, int indexAtual,
            boolean estaNoBox) throws Exception {
        Method metodo = PainelCircuito.class.getDeclaredMethod("transparenciaAplicavel", ObjetoPista.class,
                int.class, boolean.class);
        metodo.setAccessible(true);
        return (boolean) metodo.invoke(painel, objetoPista, indexAtual, estaNoBox);
    }

    @Test
    void transparenciaAplicavel_semIntervalo_sempreAplica() throws Exception {
        Circuito circuito = circuitoVetorizado();
        PainelCircuito painel = new PainelCircuito(circuito, null);
        ObjetoTransparencia objeto = objetoComIntervalo(0, 0, new Point(500, 500), 25);

        assertTrue(transparenciaAplicavel(painel, objeto, 0, false));
        assertTrue(transparenciaAplicavel(painel, objeto, 999999, false));
    }

    @Test
    void transparenciaAplicavel_comIntervalo_soDentroDoRange() throws Exception {
        Circuito circuito = circuitoVetorizado();
        PainelCircuito painel = new PainelCircuito(circuito, null);
        ObjetoTransparencia objeto = objetoComIntervalo(100, 200, new Point(500, 500), 25);

        assertFalse(transparenciaAplicavel(painel, objeto, 99, false));
        assertTrue(transparenciaAplicavel(painel, objeto, 100, false));
        assertTrue(transparenciaAplicavel(painel, objeto, 150, false));
        assertTrue(transparenciaAplicavel(painel, objeto, 200, false));
        assertFalse(transparenciaAplicavel(painel, objeto, 201, false));
    }

    @Test
    void transparenciaAplicavel_transparenciaBox_soAplicaNoBox() throws Exception {
        Circuito circuito = circuitoVetorizado();
        PainelCircuito painel = new PainelCircuito(circuito, null);
        ObjetoTransparencia objeto = objetoComIntervalo(0, 0, new Point(500, 500), 25);
        objeto.setTransparenciaBox(true);

        assertFalse(transparenciaAplicavel(painel, objeto, 500, false));
        assertTrue(transparenciaAplicavel(painel, objeto, 500, true));
    }

    // --- desenharSafetyCarCima (5.2) ---

    /**
     * Monta um PainelCircuito operacional o suficiente para desenharSafetyCarCima:
     * scima (sprite do safety car) como um quadrado opaco, viewport/desconto
     * cobrindo qualquer coordenada usada, e o InterfaceJogo mockado retornando
     * o safetyCar informado, com getNosDaPista() apontando pra pista do circuito.
     */
    private PainelCircuito painelParaSafetyCar(Circuito circuito, SafetyCar safetyCar, boolean estaNoBox)
            throws Exception {
        InterfaceJogo jogo = mock(InterfaceJogo.class);
        when(jogo.isSafetyCarNaPista()).thenReturn(true);
        when(jogo.getSafetyCar()).thenReturn(safetyCar);
        when(jogo.getNosDaPista()).thenReturn(circuito.getPistaFull());
        List<No> nosDoBox = new ArrayList<>();
        when(jogo.getNosDoBox()).thenReturn(nosDoBox);
        when(jogo.obterPista(any(No.class))).thenReturn(estaNoBox ? nosDoBox : new ArrayList<>());
        when(jogo.isAtualizacaoSuave()).thenReturn(false);

        PainelCircuito painel = new PainelCircuito(circuito, jogo);

        BufferedImage scima = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);
        Graphics2D gScima = scima.createGraphics();
        gScima.setColor(Color.BLUE);
        gScima.fillRect(0, 0, 100, 100);
        gScima.dispose();
        setField(painel, "scima", scima);
        setField(painel, "limitesViewPort", new Rectangle(0, 0, 100000, 100000));
        setField(painel, "descontoCentraliza", new Point(0, 0));

        return painel;
    }

    private void chamaDesenharSafetyCarCima(PainelCircuito painel, Graphics2D g2d) throws Exception {
        Method metodo = PainelCircuito.class.getDeclaredMethod("desenharSafetyCarCima", Graphics2D.class);
        metodo.setAccessible(true);
        metodo.invoke(painel, g2d);
    }

    @Test
    void desenharSafetyCarCima_dentroDoIntervalo_aplicaORecorteDeTransparencia() throws Exception {
        Circuito circuito = circuitoVetorizado();
        No noSafetyCar = no(500, 500);
        noSafetyCar.setIndex(5000);
        SafetyCar safetyCar = new SafetyCar();
        safetyCar.setNoAtual(noSafetyCar);
        safetyCar.setTracado(0);
        // Evita depender de gerenciadorVisual (não usado neste teste, fora do escopo do filtro de transparência).
        safetyCar.setVaiProBox(true);

        ObjetoTransparencia objeto = objetoComIntervalo(4000, 6000, new Point(500, 500), 25);
        List<ObjetoPista> objetos = new ArrayList<>();
        objetos.add(objeto);
        circuito.setObjetos(objetos);

        PainelCircuito painel = painelParaSafetyCar(circuito, safetyCar, false);

        BufferedImage saida = new BufferedImage(1000, 1000, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = saida.createGraphics();
        try {
            chamaDesenharSafetyCarCima(painel, g2d);
        } finally {
            g2d.dispose();
        }

        int alfaNoCentro = (saida.getRGB(500, 500) >>> 24);
        assertEquals(0, alfaNoCentro,
                "dentro do intervalo configurado, o centro do safety car deveria ficar transparente (recorte aplicado)");
    }

    @Test
    void desenharSafetyCarCima_foraDoIntervalo_naoAplicaORecorte() throws Exception {
        Circuito circuito = circuitoVetorizado();
        No noSafetyCar = no(500, 500);
        noSafetyCar.setIndex(5000);
        SafetyCar safetyCar = new SafetyCar();
        safetyCar.setNoAtual(noSafetyCar);
        safetyCar.setTracado(0);
        // Evita depender de gerenciadorVisual (não usado neste teste, fora do escopo do filtro de transparência).
        safetyCar.setVaiProBox(true);

        // Intervalo bem longe do índice 5000 do safety car.
        ObjetoTransparencia objeto = objetoComIntervalo(100, 200, new Point(500, 500), 25);
        List<ObjetoPista> objetos = new ArrayList<>();
        objetos.add(objeto);
        circuito.setObjetos(objetos);

        PainelCircuito painel = painelParaSafetyCar(circuito, safetyCar, false);

        BufferedImage saida = new BufferedImage(1000, 1000, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = saida.createGraphics();
        try {
            chamaDesenharSafetyCarCima(painel, g2d);
        } finally {
            g2d.dispose();
        }

        int alfaNoCentro = (saida.getRGB(500, 500) >>> 24);
        assertTrue(alfaNoCentro > 0,
                "fora do intervalo configurado, o safety car não deveria ter o recorte de transparência aplicado");
    }

    /**
     * Regressão: objetos novos criados no editor nascem com nivelDesenho=100
     * por padrão (ver mudança editor-marcadores-transparencia), o que torna
     * isPintaEmcima() true. O safety car não deve deixar de aplicar o
     * recorte de transparência por causa disso — mesma paridade com o
     * piloto (desenhaCarroCima), que nunca checou isPintaEmcima().
     */
    @Test
    void desenharSafetyCarCima_objetoComNivelDesenhoPadrao100_aindaAplicaORecorte() throws Exception {
        Circuito circuito = circuitoVetorizado();
        No noSafetyCar = no(500, 500);
        noSafetyCar.setIndex(5000);
        SafetyCar safetyCar = new SafetyCar();
        safetyCar.setNoAtual(noSafetyCar);
        safetyCar.setTracado(0);
        safetyCar.setVaiProBox(true);

        ObjetoTransparencia objeto = objetoComIntervalo(4000, 6000, new Point(500, 500), 25);
        objeto.setNivelDesenho(100);
        List<ObjetoPista> objetos = new ArrayList<>();
        objetos.add(objeto);
        circuito.setObjetos(objetos);

        PainelCircuito painel = painelParaSafetyCar(circuito, safetyCar, false);

        BufferedImage saida = new BufferedImage(1000, 1000, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = saida.createGraphics();
        try {
            chamaDesenharSafetyCarCima(painel, g2d);
        } finally {
            g2d.dispose();
        }

        int alfaNoCentro = (saida.getRGB(500, 500) >>> 24);
        assertEquals(0, alfaNoCentro,
                "nivelDesenho=100 (padrão de objeto novo) não deveria impedir o recorte de transparência do safety car");
    }

    @Test
    void desenharSafetyCarCima_transparenciaBox_naoAplicaForaDoBox() throws Exception {
        Circuito circuito = circuitoVetorizado();
        No noSafetyCar = no(500, 500);
        noSafetyCar.setIndex(5000);
        SafetyCar safetyCar = new SafetyCar();
        safetyCar.setNoAtual(noSafetyCar);
        safetyCar.setTracado(0);
        // Evita depender de gerenciadorVisual (não usado neste teste, fora do escopo do filtro de transparência).
        safetyCar.setVaiProBox(true);

        ObjetoTransparencia objeto = objetoComIntervalo(0, 0, new Point(500, 500), 25);
        objeto.setTransparenciaBox(true);
        List<ObjetoPista> objetos = new ArrayList<>();
        objetos.add(objeto);
        circuito.setObjetos(objetos);

        PainelCircuito painel = painelParaSafetyCar(circuito, safetyCar, false);

        BufferedImage saida = new BufferedImage(1000, 1000, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = saida.createGraphics();
        try {
            chamaDesenharSafetyCarCima(painel, g2d);
        } finally {
            g2d.dispose();
        }

        int alfaNoCentro = (saida.getRGB(500, 500) >>> 24);
        assertTrue(alfaNoCentro > 0,
                "objeto restrito ao box não deveria aplicar o recorte quando o safety car não está no box");
    }
}
