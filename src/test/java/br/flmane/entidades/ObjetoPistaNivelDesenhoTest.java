package br.flmane.entidades;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.junit.jupiter.api.Test;

/**
 * Nível de desenho dos objetos em relação à pista (nível 0): negativo
 * desenha abaixo do asfalto (quanto mais negativo, mais no fundo), 0 é logo
 * acima (padrão, comportamento antigo) e positivo desenha por cima, na
 * ordem crescente (quanto maior, mais em cima) — sem limite de faixa. Cobre
 * a ponte de compatibilidade com XMLs legados que só têm pintaEmcima, a
 * persistência e o sufixo "(nível)" no toString usado pelas listas do editor.
 */
class ObjetoPistaNivelDesenhoTest {

    @Test
    void nivelPadraoEZeroENaoTemLimiteDeFaixa() {
        ObjetoLivre objeto = new ObjetoLivre();
        assertEquals(0, objeto.getNivelDesenho());

        objeto.setNivelDesenho(-5);
        assertEquals(-5, objeto.getNivelDesenho());
        objeto.setNivelDesenho(20);
        assertEquals(20, objeto.getNivelDesenho());
        objeto.setNivelDesenho(-100);
        assertEquals(-100, objeto.getNivelDesenho());
    }

    @Test
    void nivelEPintaEmcimaFicamCoerentes() {
        ObjetoLivre objeto = new ObjetoLivre();

        objeto.setPintaEmcima(true);
        assertEquals(1, objeto.getNivelDesenho(), "pintaEmcima legado equivale ao nível 1");

        objeto.setNivelDesenho(-1);
        assertFalse(objeto.isPintaEmcima());

        objeto.setNivelDesenho(1);
        assertTrue(objeto.isPintaEmcima());

        objeto.setPintaEmcima(false);
        assertEquals(0, objeto.getNivelDesenho());
    }

    @Test
    void xmlLegadoSoComPintaEmcima_carregaNoNivel1() {
        String xmlLegado = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<java version=\"1.8\" class=\"java.beans.XMLDecoder\">\n"
                + " <object class=\"br.flmane.entidades.ObjetoArquibancada\">\n"
                + "  <void property=\"pintaEmcima\">\n"
                + "   <boolean>true</boolean>\n"
                + "  </void>\n"
                + " </object>\n"
                + "</java>";

        ObjetoPista objeto;
        try (XMLDecoder decoder = new XMLDecoder(new ByteArrayInputStream(xmlLegado.getBytes()))) {
            objeto = (ObjetoPista) decoder.readObject();
        }

        assertEquals(1, objeto.getNivelDesenho());
        assertTrue(objeto.isPintaEmcima());
    }

    @Test
    void nivelSobreviveAoRoundTripXml() {
        ObjetoLivre abaixo = new ObjetoLivre();
        abaixo.setNivelDesenho(-1);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (XMLEncoder encoder = new XMLEncoder(baos)) {
            encoder.writeObject(abaixo);
        }
        ObjetoLivre recarregado;
        try (XMLDecoder decoder = new XMLDecoder(new ByteArrayInputStream(baos.toByteArray()))) {
            recarregado = (ObjetoLivre) decoder.readObject();
        }

        assertEquals(-1, recarregado.getNivelDesenho());
        assertFalse(recarregado.isPintaEmcima());
    }

    /**
     * Como nivelDesenho e pintaEmcima são propriedades bean separadas (ambas
     * gravadas quando diferem do objeto padrão), a ordem em que o XMLDecoder
     * chama os dois setters não pode importar — setNivelDesenho sempre
     * recalcula pintaEmcima de forma coerente por último, então o valor final
     * é o mesmo nos dois sentidos.
     */
    @Test
    void nivelAltoPositivoSobreviveAoRoundTripXml_naoFicaPresoEm1() {
        ObjetoLivre acima = new ObjetoLivre();
        acima.setNivelDesenho(20);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (XMLEncoder encoder = new XMLEncoder(baos)) {
            encoder.writeObject(acima);
        }
        ObjetoLivre recarregado;
        try (XMLDecoder decoder = new XMLDecoder(new ByteArrayInputStream(baos.toByteArray()))) {
            recarregado = (ObjetoLivre) decoder.readObject();
        }

        assertEquals(20, recarregado.getNivelDesenho());
        assertTrue(recarregado.isPintaEmcima());
    }

    @Test
    void toStringMostraONivel_excetoObjetosDeFuncao() {
        ObjetoLivre objeto = new ObjetoLivre();
        objeto.setNome("Objeto 1");
        objeto.setNivelDesenho(-1);
        assertEquals("Objeto 1 ObjetoLivre (-1)", objeto.toString());

        ObjetoTransparencia transparencia = new ObjetoTransparencia();
        transparencia.setNome("Objeto 2");
        assertEquals("Objeto 2 ObjetoTransparencia", transparencia.toString(),
                "transparência é objeto de função, fora do sistema de níveis, sem sufixo");

        ObjetoEscapada escapada = new ObjetoEscapada();
        escapada.setNome("Objeto 3");
        assertEquals("Objeto 3 ObjetoEscapada", escapada.toString(),
                "escapada é objeto de função, fora do sistema de níveis, sem sufixo");
    }
}
