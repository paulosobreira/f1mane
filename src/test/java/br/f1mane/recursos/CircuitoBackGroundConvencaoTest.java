package br.f1mane.recursos;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.junit.jupiter.api.Test;

import br.f1mane.entidades.Circuito;

/**
 * O nome do jpg de referência do circuito deixou de ser uma propriedade
 * gravada no XML: agora é derivado pelo mesmo nome-base do arquivo XML do
 * circuito ("albert_park_mro.xml" -> "albert_park_mro.jpg"), atribuído em
 * {@link CarregadorRecursos#carregarCircuito} — sempre, quando a geração
 * procedural da imagem está ativa (o nome é só a chave usada pelo cliente
 * web, já que os jpg ficam fora do jar final), ou só quando o jpg existir,
 * no caminho legado sem a geração procedural.
 * {@code Circuito.definirBackGroundPorConvencao} de propósito não segue o
 * padrão JavaBeans "set..." — sem um setter pareado, XMLEncoder não volta a
 * gravar essa propriedade no XML.
 */
class CircuitoBackGroundConvencaoTest {

    @Test
    void carregarCircuito_derivaBackGroundPeloNomeDoXml() throws Exception {
        Circuito circuito = CarregadorRecursos.carregarCircuito("albert_park_mro.xml");

        assertEquals("albert_park_mro.jpg", circuito.getBackGround());
    }

    @Test
    void carregarCircuito_indianapoles_resolveOJpgRenomeado() throws Exception {
        Circuito circuito = CarregadorRecursos.carregarCircuito("indianapoles_mro.xml");

        assertEquals("indianapoles_mro.jpg", circuito.getBackGround());
    }

    @Test
    void xmlDosCircuitos_naoTemMaisAPropriedadeBackGround() throws Exception {
        java.io.InputStream xml = CarregadorRecursos.recursoComoStream("circuitos/albert_park_mro.xml");
        String conteudo = new String(xml.readAllBytes());
        assertFalse(conteudo.contains("backGround"),
                "o XML do circuito não deveria mais referenciar o jpg de fundo");
    }

    @Test
    void definirBackGroundPorConvencao_naoSobrevomAoRoundTripXmlEncoderDecoder() throws Exception {
        Circuito circuito = new Circuito();
        circuito.definirBackGroundPorConvencao("qualquer_coisa.jpg");
        assertEquals("qualquer_coisa.jpg", circuito.getBackGround());

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (XMLEncoder encoder = new XMLEncoder(baos)) {
            encoder.writeObject(circuito);
        }
        String xml = baos.toString();
        assertFalse(xml.contains("backGround"),
                "sem setter no padrão JavaBeans, XMLEncoder não deveria persistir backGround");

        Circuito recarregado;
        try (XMLDecoder decoder = new XMLDecoder(new ByteArrayInputStream(baos.toByteArray()))) {
            recarregado = (Circuito) decoder.readObject();
        }
        assertNull(recarregado.getBackGround(),
                "sem persistência, o valor não deveria sobreviver ao round-trip");
    }
}
