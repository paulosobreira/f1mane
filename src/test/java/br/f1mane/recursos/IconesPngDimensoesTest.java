package br.f1mane.recursos;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.stream.Stream;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Contrato de dimensões dos ícones de HUD (openspec: icones-hud-genericos).
 *
 * O rendering Swing desenha a maioria destes PNGs em tamanho natural e o
 * cliente web (canvas) também; os pneus grandes ainda passam por resizes
 * relativos (geraResize 0.3 / 0.15). Qualquer regeneração via
 * utilitarios/gerar_icones.py que altere as dimensões quebraria o HUD
 * silenciosamente — este teste trava o contrato.
 *
 * Também exige canal alfa: carregaBufferedImageTransparecia copia as 4
 * bandas do raster e um PNG sem alfa ficaria invisível.
 */
public class IconesPngDimensoesTest {

    static Stream<Arguments> icones() {
        return Stream.of(
                Arguments.of("png/pneuMole.png", 142, 142),
                Arguments.of("png/pneuDuro.png", 142, 142),
                Arguments.of("png/pneuChuva.png", 142, 142),
                Arguments.of("png/pneuMoleMenor.png", 44, 44),
                Arguments.of("png/pneuDuroMenor.png", 44, 44),
                Arguments.of("png/pneuChuvaMenor.png", 44, 44),
                Arguments.of("png/sol.png", 35, 35),
                Arguments.of("png/lua.png", 35, 35),
                Arguments.of("png/nublado.png", 35, 35),
                Arguments.of("png/chuva.png", 35, 35),
                Arguments.of("png/fuel.png", 25, 25),
                Arguments.of("png/sfcima.png", 90, 90),
                Arguments.of("png/farois-apagados.png", 150, 89),
                Arguments.of("png/farois1.png", 150, 89),
                Arguments.of("png/farois2.png", 150, 89),
                Arguments.of("png/farois3.png", 150, 89),
                Arguments.of("png/farois4.png", 150, 89),
                Arguments.of("png/farois.png", 150, 89));
    }

    @ParameterizedTest(name = "{0} deve ser {1}x{2} com canal alfa")
    @MethodSource("icones")
    void dimensoesECanalAlfa(String recurso, int largura, int altura)
            throws IOException {
        URL url = getClass().getClassLoader().getResource(recurso);
        assertNotNull(url, "recurso ausente no classpath: " + recurso);
        BufferedImage img = ImageIO.read(url);
        assertNotNull(img, "falha ao decodificar: " + recurso);
        assertEquals(largura, img.getWidth(), recurso + " largura");
        assertEquals(altura, img.getHeight(), recurso + " altura");
        assertTrue(img.getColorModel().hasAlpha(),
                recurso + " deve ter canal alfa (RGBA)");
    }

    @Test
    void tyrePngRemovidoDoClasspath() {
        assertNull(getClass().getClassLoader().getResource("png/tyre.png"),
                "png/tyre.png era um asset órfão e foi removido; "
                        + "não deve voltar ao classpath");
    }
}
