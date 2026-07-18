package br.flmane.entidades;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.awt.image.BufferedImage;
import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import br.flmane.recursos.CarregadorRecursos;
import br.nnpe.Global;

/**
 * Cobre, de ponta a ponta e sem depender do editor Swing (não há display
 * interativo disponível neste ambiente de execução):
 * - round-trip de um objeto de cenário via XMLEncoder/XMLDecoder, confirmando
 *   que a serialização reflexiva já usada para circuitos funciona para os
 *   novos tipos sem mudança de schema;
 * - a flag Global.MODO_HOMENAGEM alternando entre ler o
 *   jpg estático e gerar a imagem em memória em CarregadorRecursos.
 *
 * Validação manual ainda pendente (fora do alcance deste teste): criar os
 * quatro tipos pela UI do editor, editar cor/tamanho pelo formulário, e
 * observar visualmente a corrida com a flag ativa.
 */
class CircuitoObjetosCenarioIntegrationTest {

    private static final String CIRCUITO_TESTE = "albert_park_mro.xml";

    @AfterEach
    void resetFlag() {
        Global.MODO_HOMENAGEM = false;
    }

    @Test
    void objetoDeCenario_sobrevivePersistenciaReflexiva() throws Exception {
        Circuito circuito = CarregadorRecursos.carregarCircuito(CIRCUITO_TESTE);
        circuito.vetorizarPista();

        List<ObjetoPista> objetosCenario = new ArrayList<>();
        ObjetoArquibancada arquibancada = new ObjetoArquibancada();
        arquibancada.setNome("Arquibancada Teste");
        arquibancada.setPosicaoQuina(new java.awt.Point(500, 500));
        objetosCenario.add(arquibancada);
        circuito.setObjetosCenario(objetosCenario);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (XMLEncoder encoder = new XMLEncoder(baos)) {
            encoder.writeObject(circuito);
        }

        Circuito recarregado;
        try (XMLDecoder decoder = new XMLDecoder(new ByteArrayInputStream(baos.toByteArray()))) {
            recarregado = (Circuito) decoder.readObject();
        }

        assertNotNull(recarregado.getObjetosCenario());
        ObjetoPista objetoRecarregado = recarregado.getObjetosCenario().stream()
                .filter(o -> o instanceof ObjetoArquibancada)
                .findFirst()
                .orElseGet(() -> fail("ObjetoArquibancada não sobreviveu ao round-trip XMLEncoder/XMLDecoder"));
        assertEquals(arquibancada.getLargura(), objetoRecarregado.getLargura());
        assertEquals(arquibancada.getAltura(), objetoRecarregado.getAltura());
        assertEquals(arquibancada.getCorPimaria(), objetoRecarregado.getCorPimaria());
        assertEquals(500, objetoRecarregado.getPosicaoQuina().x);
    }

    @Test
    void flagDesativada_carregaBackGroundJogo_leJpgEstatico() throws Exception {
        Global.MODO_HOMENAGEM = false;
        Circuito circuito = CarregadorRecursos.carregarCircuito(CIRCUITO_TESTE);
        circuito.vetorizarPista();

        BufferedImage esperado = CarregadorRecursos.carregaBackGround(circuito.getBackGround(), null, circuito);
        BufferedImage obtido = CarregadorRecursos.carregaBackGroundJogo(circuito.getBackGround(), null, circuito);

        assertNotNull(obtido);
        assertEquals(esperado.getWidth(), obtido.getWidth());
        assertEquals(esperado.getHeight(), obtido.getHeight());
    }

    @Test
    void flagAtivada_carregaBackGroundJogo_geraImagemEmMemoria() throws Exception {
        Circuito circuito = CarregadorRecursos.carregarCircuito(CIRCUITO_TESTE);
        circuito.vetorizarPista();
        List<ObjetoPista> objetosCenario = new ArrayList<>();
        ObjetoPneus pneus = new ObjetoPneus();
        pneus.setPosicaoQuina(new java.awt.Point(500, 500));
        objetosCenario.add(pneus);
        circuito.setObjetosCenario(objetosCenario);

        Global.MODO_HOMENAGEM = true;
        BufferedImage gerada = CarregadorRecursos.carregaBackGroundJogo(circuito.getBackGround(), null, circuito);

        assertNotNull(gerada);
        assertTrue(gerada.getWidth() > 0 && gerada.getHeight() > 0);
    }
}
