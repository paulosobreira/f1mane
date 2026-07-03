package br.f1mane.controles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;

import org.junit.jupiter.api.Test;

import br.f1mane.entidades.Circuito;
import br.f1mane.recursos.CarregadorRecursos;

/**
 * Todos os 37 circuitos existentes foram marcados como inativos de propósito
 * (trabalho em andamento nos backgrounds/traçados) — trava esse estado para
 * não regredir sem querer num commit futuro. ControleRecursos.carregarCircuitos()
 * já filtra por isAtivo(), então nenhum aparece na lista de seleção do jogo
 * enquanto isso for verdade.
 */
class ControleRecursosCircuitosInativosTest {

    @Test
    void todosOsCircuitosExistentes_estaoInativos() throws Exception {
        File pasta = new File("src/main/resources/circuitos");
        String[] arquivosXml = pasta.list((dir, name) -> name.endsWith(".xml"));
        assertTrue(arquivosXml != null && arquivosXml.length > 0, "esperava encontrar arquivos de circuito");
        assertEquals(37, arquivosXml.length, "contagem de circuitos mudou — revisar se este teste ainda se aplica");

        for (String arquivo : arquivosXml) {
            Circuito circuito = CarregadorRecursos.carregarCircuito(arquivo);
            assertFalse(circuito.isAtivo(), arquivo + " deveria estar inativo");
        }
    }

    @Test
    void carregarCircuitos_naoRetornaNenhumCircuitoEnquantoTodosInativos() {
        java.util.Map<String, String> circuitos = ControleRecursos.carregarCircuitos();

        assertTrue(circuitos.isEmpty(),
                "com todos os 37 circuitos inativos, a lista de seleção do jogo deveria ficar vazia");
    }
}
