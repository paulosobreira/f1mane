package br.f1mane.entidades;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.awt.Color;
import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.junit.jupiter.api.Test;

import br.f1mane.recursos.CarregadorRecursos;

/**
 * Cobre os novos campos de cor customizável de box e zebra em Circuito:
 * valor padrão nulo, compatibilidade com XML existente sem essas
 * propriedades, e persistência via XMLEncoder/XMLDecoder quando definidas.
 */
class CircuitoCoresBoxZebraTest {

    private static final String CIRCUITO_EXISTENTE = "albert_park_mro.xml";

    @Test
    void circuitoRecemCriado_coresDeBoxEZebraSaoNulas() {
        Circuito circuito = new Circuito();

        assertNull(circuito.getCorBox1());
        assertNull(circuito.getCorBox2());
        assertNull(circuito.getCorZebra1());
        assertNull(circuito.getCorZebra2());
    }

    @Test
    void xmlExistenteSemAsNovasPropriedades_carregaComCoresNulas() throws Exception {
        Circuito circuito = CarregadorRecursos.carregarCircuito(CIRCUITO_EXISTENTE);

        assertNull(circuito.getCorBox1());
        assertNull(circuito.getCorBox2());
        assertNull(circuito.getCorZebra1());
        assertNull(circuito.getCorZebra2());
    }

    @Test
    void coresDeBoxEZebra_sobrevivemAoRoundTripXmlEncoderDecoder() throws Exception {
        Circuito circuito = new Circuito();
        Color box1 = new Color(10, 200, 30);
        Color box2 = new Color(220, 15, 90);
        Color zebra1 = new Color(11, 22, 33);
        Color zebra2 = new Color(44, 55, 66);
        circuito.setCorBox1(box1);
        circuito.setCorBox2(box2);
        circuito.setCorZebra1(zebra1);
        circuito.setCorZebra2(zebra2);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (XMLEncoder encoder = new XMLEncoder(baos)) {
            encoder.writeObject(circuito);
        }

        Circuito recarregado;
        try (XMLDecoder decoder = new XMLDecoder(new ByteArrayInputStream(baos.toByteArray()))) {
            recarregado = (Circuito) decoder.readObject();
        }

        assertEquals(box1, recarregado.getCorBox1());
        assertEquals(box2, recarregado.getCorBox2());
        assertEquals(zebra1, recarregado.getCorZebra1());
        assertEquals(zebra2, recarregado.getCorZebra2());
    }
}
