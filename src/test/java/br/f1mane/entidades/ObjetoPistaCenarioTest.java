package br.f1mane.entidades;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * Cobre os tipos de objeto de cenário de pista que nascem prontos por um
 * único clique — Arquibancada, Construcao, Pneus: devem ter defaults
 * seguros (sem NullPointerException) tanto ao consultar a área antes do
 * primeiro desenho quanto ao desenhar logo após serem instanciados, sem
 * edição prévia pelo usuário no editor. GuardRails (como ObjetoLivre) fica
 * de fora: precisa dos pontos clicados pelo usuário antes de ter uma área
 * visível, então uma instância "nua" legitimamente não desenha nada.
 */
class ObjetoPistaCenarioTest {

    private static List<ObjetoPista> objetosDeCenario() {
        return List.of(new ObjetoArquibancada(), new ObjetoConstrucao(), new ObjetoPneus());
    }

    @Test
    void obterArea_antesDoPrimeiroDesenho_naoLancaExcecao() {
        for (ObjetoPista objetoPista : objetosDeCenario()) {
            Rectangle area = assertDoesNotThrow(objetoPista::obterArea,
                    objetoPista.getClass().getSimpleName());
            assertNotNull(area, objetoPista.getClass().getSimpleName());
        }
    }

    @Test
    void desenha_logoAposCriacao_naoLancaExcecaoEProduzAreaVisivel() {
        BufferedImage buffer = new BufferedImage(2000, 2000, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = buffer.createGraphics();
        try {
            for (ObjetoPista objetoPista : objetosDeCenario()) {
                objetoPista.setPosicaoQuina(new Point(100, 100));

                assertDoesNotThrow(() -> objetoPista.desenha(g2d, 1.0),
                        objetoPista.getClass().getSimpleName());

                Rectangle area = objetoPista.obterArea();
                assertTrue(area.width > 0 && area.height > 0,
                        objetoPista.getClass().getSimpleName() + " deveria ter área visível > 0");
            }
        } finally {
            g2d.dispose();
        }
    }
}
