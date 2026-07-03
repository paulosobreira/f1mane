package br.f1mane.entidades;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import org.junit.jupiter.api.Test;

/**
 * Cobre ObjetoPista.obterAreaClique(): a área de detecção de clique no
 * editor precisa considerar a rotação (ângulo) do objeto, já que
 * obterArea() de cada subtipo guarda o retângulo bruto (sem rotação) e a
 * rotação só é aplicada na hora de desenhar. Sem essa correção, clicar num
 * objeto rotacionado no lugar onde ele é realmente exibido não o
 * selecionava.
 */
class ObjetoPistaObterAreaCliqueTest {

    private void renderizar(ObjetoPista objeto) {
        BufferedImage imagem = new BufferedImage(10, 10, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = imagem.createGraphics();
        try {
            objeto.desenha(g2d, 1.0);
        } finally {
            g2d.dispose();
        }
    }

    private ObjetoArquibancada criarObjeto(int largura, int altura, double angulo) {
        ObjetoArquibancada objeto = new ObjetoArquibancada();
        objeto.setPosicaoQuina(new Point(0, 0));
        objeto.setLargura(largura);
        objeto.setAltura(altura);
        objeto.setAngulo(angulo);
        renderizar(objeto);
        return objeto;
    }

    @Test
    void obterAreaClique_semRotacao_retornaAreaExpandidaPelaTolerancia() {
        ObjetoArquibancada objeto = criarObjeto(100, 20, 0);

        Rectangle areaBase = objeto.obterArea();
        Rectangle areaClique = objeto.obterAreaClique();

        assertEquals(areaBase.x - 6, areaClique.x);
        assertEquals(areaBase.y - 6, areaClique.y);
        assertEquals(areaBase.width + 12, areaClique.width);
        assertEquals(areaBase.height + 12, areaClique.height);
    }

    @Test
    void obterAreaClique_comRotacao_cobreAAreaRealmenteDesenhada() {
        ObjetoArquibancada objeto = criarObjeto(100, 20, 90);

        // ponto fora do retângulo bruto (0,0,100,20) mas dentro do
        // retângulo delimitador da forma rotacionada 90° em torno do centro (50,10)
        Point ponto = new Point(45, 50);

        assertFalse(objeto.obterArea().contains(ponto),
                "o retângulo bruto (sem rotação) não deveria conter esse ponto");
        assertTrue(objeto.obterAreaClique().contains(ponto),
                "a área de clique (com rotação) deveria conter esse ponto, dentro da forma visível");
    }

    @Test
    void obterArea_naoMudaComRotacao_soObterAreaCliqueMuda() {
        ObjetoArquibancada objeto = criarObjeto(100, 20, 90);

        // obterArea() é usado também por lógica de jogo (ex.: máscara de
        // transparência do box) e não deve passar a incluir rotação.
        assertEquals(new Rectangle(0, 0, 100, 20), objeto.obterArea());
    }
}
