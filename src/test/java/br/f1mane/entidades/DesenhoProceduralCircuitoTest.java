package br.f1mane.entidades;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * Cobre a geração em memória da imagem de fundo do circuito: deve incluir os
 * objetos de cenário (Arquibancada, Construcao, GuardRails, Pneus) e excluir
 * Escapada/Transparencia, sem lançar exceção.
 */
class DesenhoProceduralCircuitoTest {

    private Circuito circuitoDeTeste() {
        Circuito circuito = new Circuito();

        List<No> pista = new ArrayList<>();
        pista.add(criarNo(1000, 1000, No.RETA));
        pista.add(criarNo(2000, 1000, No.CURVA_ALTA));
        pista.add(criarNo(2000, 2000, No.RETA));
        pista.add(criarNo(1000, 2000, No.CURVA_BAIXA));
        circuito.setPista(pista);
        circuito.setBox(new ArrayList<>());

        circuito.vetorizarPista(9, 1.5);

        List<ObjetoPista> objetosCenario = new ArrayList<>();
        objetosCenario.add(new ObjetoArquibancada());
        objetosCenario.add(new ObjetoConstrucao());
        objetosCenario.add(new ObjetoGuardRails());
        objetosCenario.add(new ObjetoPneus());
        for (ObjetoPista objetoPista : objetosCenario) {
            objetoPista.setPosicaoQuina(new Point(1200, 1200));
        }
        circuito.setObjetosCenario(objetosCenario);

        List<ObjetoPista> objetos = new ArrayList<>();
        objetos.add(new ObjetoEscapada());
        objetos.add(new ObjetoTransparencia());
        for (ObjetoPista objetoPista : objetos) {
            objetoPista.setPosicaoQuina(new Point(1200, 1200));
        }
        circuito.setObjetos(objetos);

        return circuito;
    }

    private No criarNo(int x, int y, java.awt.Color tipo) {
        No no = new No();
        no.setPoint(new Point(x, y));
        no.setTipo(tipo);
        return no;
    }

    @Test
    void desenha_naoLancaExcecao() {
        Circuito circuito = circuitoDeTeste();
        BufferedImage buffer = new BufferedImage(3000, 3000, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = buffer.createGraphics();
        try {
            assertDoesNotThrow(() -> DesenhoProceduralCircuito.desenha(g2d, circuito, 1.0));
        } finally {
            g2d.dispose();
        }
    }

    @Test
    void geraImagem_produzImagemComDimensaoMaiorQueZero() {
        Circuito circuito = circuitoDeTeste();

        BufferedImage imagem = assertDoesNotThrow(() -> DesenhoProceduralCircuito.geraImagem(circuito));

        assertTrue(imagem.getWidth() > 0);
        assertTrue(imagem.getHeight() > 0);
    }

    @Test
    void geraImagem_respeitaCorFundoECorAsfaltoDoCircuito() {
        Circuito circuito = circuitoDeTeste();
        Color fundo = new Color(10, 20, 30);
        Color asfalto = new Color(60, 70, 80);
        circuito.setCorFundo(fundo);
        circuito.setCorAsfalto(asfalto);

        BufferedImage imagem = DesenhoProceduralCircuito.geraImagem(circuito);

        assertEquals(fundo.getRGB(), imagem.getRGB(0, 0));
        assertEquals(asfalto.getRGB(), imagem.getRGB(1500, 1000));
    }

    // ---- zebra e box customizáveis (circuito-cores-box-zebra) ----

    @Test
    void desenhaPistaZebraEBox_usaCorZebraCustomizadaQuandoDefinida() {
        Circuito circuito = circuitoDeTeste();
        Color zebra1 = new Color(11, 22, 33);
        Color zebra2 = new Color(44, 55, 66);
        circuito.setCorZebra1(zebra1);
        circuito.setCorZebra2(zebra2);

        BufferedImage imagem = new BufferedImage(3000, 3000, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = imagem.createGraphics();
        try {
            DesenhoProceduralCircuito.desenhaPistaZebraEBox(g2d, circuito, 1.0);
        } finally {
            g2d.dispose();
        }

        assertTrue(imagemContemCor(imagem, zebra1), "esperava encontrar corZebra1 customizada na imagem");
        assertTrue(imagemContemCor(imagem, zebra2), "esperava encontrar corZebra2 customizada na imagem");
        assertTrue(!imagemContemCor(imagem, Color.RED), "não deveria mais usar o vermelho padrão");
    }

    @Test
    void desenhaPistaZebraEBox_semCoresDefinidas_usaBrancoVermelhoPadrao() {
        Circuito circuito = circuitoDeTeste();

        BufferedImage imagem = new BufferedImage(3000, 3000, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = imagem.createGraphics();
        try {
            DesenhoProceduralCircuito.desenhaPistaZebraEBox(g2d, circuito, 1.0);
        } finally {
            g2d.dispose();
        }

        assertTrue(imagemContemCor(imagem, Color.WHITE), "esperava fallback para branco");
        assertTrue(imagemContemCor(imagem, Color.RED), "esperava fallback para vermelho");
    }

    @Test
    void desenhaPistaZebraEBox_apenasUmaCorDefinida_mantemFallbackNasDuas() {
        Circuito circuito = circuitoDeTeste();
        circuito.setCorZebra1(new Color(11, 22, 33));
        // corZebra2 permanece null: fallback deve valer para as duas, não misturar customizada com padrão

        BufferedImage imagem = new BufferedImage(3000, 3000, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = imagem.createGraphics();
        try {
            DesenhoProceduralCircuito.desenhaPistaZebraEBox(g2d, circuito, 1.0);
        } finally {
            g2d.dispose();
        }

        assertTrue(imagemContemCor(imagem, Color.WHITE), "esperava fallback para branco quando só uma cor está definida");
        assertTrue(imagemContemCor(imagem, Color.RED), "esperava fallback para vermelho quando só uma cor está definida");
        assertTrue(!imagemContemCor(imagem, new Color(11, 22, 33)), "não deveria usar corZebra1 sozinha sem corZebra2");
    }

    // ---- box customizável (circuito-cores-box-zebra) ----

    private Circuito circuitoComBoxDeTeste() {
        Circuito circuito = new Circuito();

        List<No> pista = new ArrayList<>();
        pista.add(criarNo(1000, 1000, No.RETA));
        pista.add(criarNo(2000, 1000, No.CURVA_ALTA));
        pista.add(criarNo(2000, 2000, No.RETA));
        pista.add(criarNo(1000, 2000, No.CURVA_BAIXA));
        circuito.setPista(pista);

        // Posicionado perto do canto (2000,2000) da pista (não perto do início do
        // loop, índice 0) para que o índice de entrada do box calculado por
        // vetorizarPista fique longe o bastante do início de pistaFull.
        List<No> box = new ArrayList<>();
        box.add(criarNo(2100, 1900, No.RETA));
        box.add(criarNo(2100, 2400, No.PARADA_BOX));
        box.add(criarNo(2600, 2400, No.RETA));
        circuito.setBox(box);

        circuito.vetorizarPista(9, 1.5);
        return circuito;
    }

    @Test
    void desenhaVagasBox_usaCorBoxCustomizadaQuandoDefinida() {
        Circuito circuito = circuitoComBoxDeTeste();
        Color box1 = new Color(10, 200, 30);
        Color box2 = new Color(220, 15, 90);
        circuito.setCorBox1(box1);
        circuito.setCorBox2(box2);

        BufferedImage imagem = new BufferedImage(3000, 3000, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = imagem.createGraphics();
        try {
            DesenhoProceduralCircuito.desenhaVagasBox(g2d, circuito, 1.0);
        } finally {
            g2d.dispose();
        }

        assertTrue(imagemContemCor(imagem, corSobrePreto(box1, 150)),
                "esperava encontrar corBox1 customizada (com a mesma transparência) na imagem");
        assertTrue(imagemContemCor(imagem, corSobrePreto(box2, 150)),
                "esperava encontrar corBox2 customizada (com a mesma transparência) na imagem");
    }

    @Test
    void desenhaVagasBox_semCoresDefinidas_usaCianoMagentaPadrao() {
        Circuito circuito = circuitoComBoxDeTeste();

        BufferedImage imagem = new BufferedImage(3000, 3000, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = imagem.createGraphics();
        try {
            DesenhoProceduralCircuito.desenhaVagasBox(g2d, circuito, 1.0);
        } finally {
            g2d.dispose();
        }

        assertTrue(imagemContemCor(imagem, corSobrePreto(Color.CYAN, 150)), "esperava fallback para ciano");
        assertTrue(imagemContemCor(imagem, corSobrePreto(Color.MAGENTA, 150)), "esperava fallback para magenta");
    }

    /** Varre a imagem inteira procurando um pixel com exatamente essa cor (RGB, ignora alpha). */
    private static boolean imagemContemCor(BufferedImage imagem, Color cor) {
        int alvo = cor.getRGB() & 0xFFFFFF;
        for (int y = 0; y < imagem.getHeight(); y++) {
            for (int x = 0; x < imagem.getWidth(); x++) {
                if ((imagem.getRGB(x, y) & 0xFFFFFF) == alvo) {
                    return true;
                }
            }
        }
        return false;
    }

    /** Combina {@code cor} com o alpha informado sobre um fundo preto opaco, usando o mesmo compositing do Graphics2D. */
    private static Color corSobrePreto(Color cor, int alpha) {
        BufferedImage sonda = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = sonda.createGraphics();
        try {
            g2d.setColor(Color.BLACK);
            g2d.fillRect(0, 0, 1, 1);
            g2d.setColor(new Color(cor.getRed(), cor.getGreen(), cor.getBlue(), alpha));
            g2d.fillRect(0, 0, 1, 1);
        } finally {
            g2d.dispose();
        }
        return new Color(sonda.getRGB(0, 0));
    }
}
