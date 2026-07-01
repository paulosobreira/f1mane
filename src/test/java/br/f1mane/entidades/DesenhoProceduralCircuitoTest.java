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
}
