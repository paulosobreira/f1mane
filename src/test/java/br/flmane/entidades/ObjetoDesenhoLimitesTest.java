package br.flmane.entidades;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.junit.jupiter.api.Test;

/**
 * Objetos de desenho (Livre, Arquibancada, Construcao, GuardRails, Pneus)
 * não aceitam largura/altura menor que 1 nem ângulo negativo — largura ou
 * altura zero deixaria o objeto invisível, e ângulo negativo não faz
 * sentido visualmente. Objetos de função (Escapada, Transparencia) usam
 * largura/altura/ângulo com outros significados (comprimento/amplitude de
 * onda em Escapada) e continuam sem essa restrição.
 */
class ObjetoDesenhoLimitesTest {

    @Test
    void larguraEAlturaNuncaFicamMenorQueUm() {
        ObjetoLivre objeto = new ObjetoLivre();

        objeto.setLargura(0);
        assertEquals(1, objeto.getLargura());
        objeto.setLargura(-50);
        assertEquals(1, objeto.getLargura());
        objeto.setAltura(0);
        assertEquals(1, objeto.getAltura());
        objeto.setAltura(-10);
        assertEquals(1, objeto.getAltura());

        objeto.setLargura(200);
        assertEquals(200, objeto.getLargura(), "valores positivos continuam livres");
    }

    @Test
    void anguloNuncaFicaNegativo() {
        ObjetoLivre objeto = new ObjetoLivre();

        objeto.setAngulo(-1);
        assertEquals(0, objeto.getAngulo());
        objeto.setAngulo(-90);
        assertEquals(0, objeto.getAngulo());

        objeto.setAngulo(270);
        assertEquals(270, objeto.getAngulo(), "valores positivos continuam livres");
    }

    @Test
    void restricaoValeParaTodosOsCincoTiposDeDesenho() {
        for (ObjetoPista objeto : new ObjetoPista[] {
                new ObjetoLivre(), new ObjetoArquibancada(), new ObjetoConstrucao(),
                new ObjetoGuardRails(), new ObjetoPneus() }) {
            objeto.setLargura(-5);
            objeto.setAltura(-5);
            objeto.setAngulo(-5);
            assertEquals(1, objeto.getLargura(), objeto.getClass().getSimpleName());
            assertEquals(1, objeto.getAltura(), objeto.getClass().getSimpleName());
            assertEquals(0.0, objeto.getAngulo(), objeto.getClass().getSimpleName());
        }
    }

    /** Escapada/Transparencia são objetos de função: sem essa restrição, mesmo com valores negativos. */
    @Test
    void objetosDeFuncao_naoTemRestricaoDeLarguraAlturaAngulo() {
        ObjetoEscapada escapada = new ObjetoEscapada();
        escapada.setLargura(-5);
        escapada.setAltura(-5);
        escapada.setAngulo(-5);
        assertEquals(-5, escapada.getLargura());
        assertEquals(-5, escapada.getAltura());
        assertEquals(-5.0, escapada.getAngulo());

        ObjetoTransparencia transparencia = new ObjetoTransparencia();
        transparencia.setLargura(-5);
        transparencia.setAltura(-5);
        transparencia.setAngulo(-5);
        assertEquals(-5, transparencia.getLargura());
        assertEquals(-5, transparencia.getAltura());
        assertEquals(-5.0, transparencia.getAngulo());
    }

    /**
     * XMLDecoder chama os setters normalmente (dispatch virtual), então
     * circuitos legados com largura/altura/ângulo inválidos (zero ou
     * negativos) para objetos de desenho se autocorrigem ao carregar.
     */
    @Test
    void valoresInvalidosLegados_saoCorrigidosAoRecarregarXml() {
        ObjetoArquibancada original = new ObjetoArquibancada();
        // contorna o próprio setter pra simular um estado legado impossível hoje
        original.altura = -20;
        original.largura = 0;

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (XMLEncoder encoder = new XMLEncoder(baos)) {
            encoder.writeObject(original);
        }
        ObjetoArquibancada recarregado;
        try (XMLDecoder decoder = new XMLDecoder(new ByteArrayInputStream(baos.toByteArray()))) {
            recarregado = (ObjetoArquibancada) decoder.readObject();
        }

        assertEquals(1, recarregado.getLargura());
        assertEquals(1, recarregado.getAltura());
    }
}
