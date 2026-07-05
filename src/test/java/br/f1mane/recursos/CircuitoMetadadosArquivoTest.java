package br.f1mane.recursos;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Color;
import java.awt.Point;
import java.beans.XMLEncoder;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import br.f1mane.entidades.Circuito;
import br.f1mane.entidades.No;
import br.f1mane.entidades.ObjetoConstrucao;
import br.f1mane.entidades.ObjetoPista;

/**
 * Cobre a divisão da persistência de {@link Circuito} em dois arquivos
 * ({@code _mro_meta.xml} com metadados leves + traçado autorado;
 * {@code _mro.xml} com objetos/objetosCenario), a remoção de {@code ativo}
 * do XML em favor de {@code circuitos.properties}, o descarte dos campos
 * derivados da persistência (recalculados por {@code vetorizarPista()}), e a
 * reescrita linha a linha de {@code circuitos.properties}.
 */
class CircuitoMetadadosArquivoTest {

    private Circuito circuitoCompleto() {
        Circuito circuito = new Circuito();
        circuito.setNome("Circuito Teste");
        circuito.setAtivo(true);
        circuito.setNoite(true);
        circuito.setUsaBkg(true);
        circuito.setProbalidadeChuva(20);
        circuito.setLadoBox(1);
        circuito.setLadoBoxSaidaBox(2);
        circuito.setCorFundo(new Color(1, 2, 3));
        circuito.setCorAsfalto(new Color(4, 5, 6));
        circuito.setCorBox1(Color.LIGHT_GRAY);
        circuito.setCorBox2(Color.GRAY);
        circuito.setCorZebra1(Color.WHITE);
        circuito.setCorZebra2(Color.RED);
        circuito.setMultiplicadorLarguraPista(1.4);
        circuito.setCiclo(200);
        circuito.setDistanciaKm(5.278);

        // Loop grande o suficiente (e box deslocado para o meio de um dos
        // lados, não perto do nó inicial) para vetorizarPista()/
        // gerarTracado1e2Box() não estourarem índice negativo ao subtrair
        // Piloto.METADE_CARRO perto das bordas do traçado interpolado.
        List<No> pista = new ArrayList<>();
        No n1 = new No();
        n1.setPoint(new Point(0, 0));
        n1.setTipo(No.LARGADA);
        pista.add(n1);
        No n2 = new No();
        n2.setPoint(new Point(1000, 0));
        n2.setTipo(No.RETA);
        pista.add(n2);
        No n3 = new No();
        n3.setPoint(new Point(1000, 1000));
        n3.setTipo(No.CURVA_ALTA);
        pista.add(n3);
        No n4 = new No();
        n4.setPoint(new Point(0, 1000));
        n4.setTipo(No.CURVA_BAIXA);
        pista.add(n4);
        circuito.setPista(pista);

        List<No> box = new ArrayList<>();
        No b1 = new No();
        b1.setPoint(new Point(1000, 400));
        b1.setTipo(No.BOX);
        box.add(b1);
        No b2 = new No();
        b2.setPoint(new Point(1000, 500));
        b2.setTipo(No.BOX);
        box.add(b2);
        No b3 = new No();
        b3.setPoint(new Point(1000, 600));
        b3.setTipo(No.BOX);
        box.add(b3);
        circuito.setBox(box);

        List<ObjetoPista> objetosCenario = new ArrayList<>();
        objetosCenario.add(new ObjetoConstrucao());
        circuito.setObjetosCenario(objetosCenario);
        circuito.setObjetos(new ArrayList<ObjetoPista>());

        return circuito;
    }

    private String codificar(Circuito circuito) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (XMLEncoder encoder = new XMLEncoder(baos)) {
            encoder.writeObject(circuito);
        }
        return baos.toString();
    }

    @Test
    void copiaParaArquivoMetadados_naoContemObjetosNemAtivoNemCamposDerivados() {
        Circuito circuito = circuitoCompleto();
        circuito.vetorizarPista();

        String xml = codificar(circuito.copiaParaArquivoMetadados());

        assertTrue(xml.contains("property=\"pista\""), "meta deveria conter pista");
        assertTrue(xml.contains("property=\"box\""), "meta deveria conter box");
        assertTrue(xml.contains("property=\"nome\""), "meta deveria conter nome");
        assertTrue(xml.contains("property=\"corFundo\""), "meta deveria conter corFundo");
        assertTrue(xml.contains("property=\"ciclo\""), "meta deveria conter ciclo");
        assertTrue(xml.contains("property=\"distanciaKm\""), "meta deveria conter distanciaKm");
        assertFalse(xml.contains("property=\"ativo\""), "meta não deveria conter ativo");
        assertFalse(xml.contains("property=\"objetosCenario\""), "meta não deveria conter objetosCenario");
        assertFalse(xml.contains("property=\"pistaKey\""), "meta não deveria conter pistaKey");
        assertFalse(xml.contains("property=\"boxKey\""), "meta não deveria conter boxKey");
        assertFalse(xml.contains("property=\"escapeMap\""), "meta não deveria conter escapeMap");
        assertFalse(xml.contains("property=\"escapeList\""), "meta não deveria conter escapeList");
    }

    @Test
    void copiaParaArquivoObjetos_contemSoObjetosENaoMetadados() {
        Circuito circuito = circuitoCompleto();
        circuito.vetorizarPista();

        String xml = codificar(circuito.copiaParaArquivoObjetos());

        assertTrue(xml.contains("property=\"objetosCenario\""), "objetos deveria conter objetosCenario");
        assertFalse(xml.contains("property=\"pista\""), "objetos não deveria conter pista");
        assertFalse(xml.contains("property=\"box\""), "objetos não deveria conter box");
        assertFalse(xml.contains("property=\"nome\""), "objetos não deveria conter nome de circuito");
        assertFalse(xml.contains("property=\"ativo\""), "objetos não deveria conter ativo");
        assertFalse(xml.contains("property=\"corFundo\""), "objetos não deveria conter corFundo");
        assertFalse(xml.contains("property=\"pistaKey\""), "objetos não deveria conter pistaKey");
        assertFalse(xml.contains("property=\"escapeMap\""), "objetos não deveria conter escapeMap");
        assertFalse(xml.contains("property=\"ciclo\""), "objetos não deveria conter ciclo");
        assertFalse(xml.contains("property=\"distanciaKm\""), "objetos não deveria conter distanciaKm");
    }

    @Test
    void carregarCircuito_mesclaOsDoisArquivosEJaSaiVetorizado() throws Exception {
        Circuito circuito = CarregadorRecursos.carregarCircuito("albert_park_mro.xml");

        assertEquals("Albert Park", circuito.getNome());
        assertTrue(circuito.isAtivo(), "albert_park está ativo=true em circuitos.properties");
        assertFalse(circuito.getPista().isEmpty());
        assertFalse(circuito.getBox().isEmpty());
        assertFalse(circuito.getObjetosCenario() == null || circuito.getObjetosCenario().isEmpty(),
                "circuito real deveria ter objetos de cenário");
        assertFalse(circuito.getPistaFull().isEmpty(), "carregarCircuito deveria já sair vetorizado");
        assertFalse(circuito.getBoxFull().isEmpty(), "carregarCircuito deveria já sair vetorizado");
    }

    @Test
    void carregarCircuito_formatoAntigoSemArquivoDeMetadados_aindaCarregaCorretamente() throws Exception {
        Circuito circuito = CarregadorRecursos.carregarCircuito("fixture_formato_antigo_mro.xml");

        assertEquals("Fixture Formato Antigo", circuito.getNome());
        assertEquals(4, circuito.getPista().size());
        assertEquals(3, circuito.getBox().size());
        assertEquals(1, circuito.getObjetosCenario().size());
        assertFalse(circuito.getPistaFull().isEmpty(), "carregarCircuito deveria vetorizar mesmo no formato antigo");
        // O fixture grava ativo=true no XML, mas circuitos.properties não tem
        // entrada para ele: ativo passa a vir exclusivamente de
        // circuitos.properties, então o valor do XML é ignorado.
        assertFalse(circuito.isAtivo(),
                "sem entrada em circuitos.properties, ativo deveria ser false mesmo com ativo=true no XML antigo");
    }

    @Test
    void carregarMetadadosCircuito_naoPrecisaDoArquivoDeObjetos() throws Exception {
        Circuito circuito = CarregadorRecursos.carregarMetadadosCircuito("albert_park_mro.xml");

        assertEquals("Albert Park", circuito.getNome());
        assertFalse(circuito.getPista().isEmpty());
        assertTrue(circuito.isAtivo());
    }

    @Test
    void vetorizarPista_dentroDeCarregarCircuito_bateComCalculoManual() throws Exception {
        Circuito circuitoCarregado = CarregadorRecursos.carregarCircuito("albert_park_mro.xml");

        Circuito circuitoManual = new Circuito();
        circuitoManual.setPista(circuitoCarregado.getPista());
        circuitoManual.setBox(circuitoCarregado.getBox());
        circuitoManual.setObjetos(circuitoCarregado.getObjetos());
        circuitoManual.setObjetosCenario(circuitoCarregado.getObjetosCenario());
        circuitoManual.setMultiplicadorLarguraPista(circuitoCarregado.getMultiplicadorLarguraPista());
        circuitoManual.vetorizarPista();

        assertEquals(circuitoCarregado.getPistaFull().size(), circuitoManual.getPistaFull().size());
        assertEquals(circuitoCarregado.getBoxFull().size(), circuitoManual.getBoxFull().size());
        assertEquals(circuitoCarregado.getEscapeMap().size(), circuitoManual.getEscapeMap().size());
    }

    @Test
    void atualizarAtivoEmCircuitosProperties_alteraSoALinhaAlvo(@TempDir File tempDir) throws Exception {
        File arquivo = new File(tempDir, "circuitos.properties");
        try (FileWriter writer = new FileWriter(arquivo)) {
            writer.write("circuitoA_mro.xml=Circuito A,false\n");
            writer.write("circuitoB_mro.xml=Circuito B,true\n");
            writer.write("circuitoC_mro.xml=Circuito C,false\n");
        }

        CarregadorRecursos.atualizarAtivoEmCircuitosProperties(arquivo, "circuitoB_mro.xml", false);

        List<String> linhas = lerLinhas(arquivo);
        assertEquals("circuitoA_mro.xml=Circuito A,false", linhas.get(0));
        assertEquals("circuitoB_mro.xml=Circuito B,false", linhas.get(1));
        assertEquals("circuitoC_mro.xml=Circuito C,false", linhas.get(2));

        CarregadorRecursos.atualizarAtivoEmCircuitosProperties(arquivo, "circuitoA_mro.xml", true);

        linhas = lerLinhas(arquivo);
        assertEquals("circuitoA_mro.xml=Circuito A,true", linhas.get(0));
        assertEquals("circuitoB_mro.xml=Circuito B,false", linhas.get(1), "linha de B não deveria mudar");
        assertEquals("circuitoC_mro.xml=Circuito C,false", linhas.get(2), "linha de C não deveria mudar");
    }

    private List<String> lerLinhas(File arquivo) throws Exception {
        List<String> linhas = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(arquivo))) {
            String linha;
            while ((linha = reader.readLine()) != null) {
                linhas.add(linha);
            }
        }
        return linhas;
    }
}
