package br.f1mane.recursos;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * {@link CarregadorRecursos#circuitoAtivo} lê só o terceiro campo (ativo) da
 * linha correspondente em {@code properties/circuitos.properties}, sem
 * desserializar o circuito inteiro nem tocar em nenhum XML de circuito — a
 * listagem de circuitos (menu/lobby, ver ControleRecursos.carregarCircuitos)
 * roda na abertura do jogo e não deve custar o carregamento (nem a retenção
 * em cache) de todos os circuitos, que só devem ser carregados de fato
 * quando uma corrida os usa.
 */
class CarregadorRecursosCircuitoAtivoTest {

    @Test
    void circuitoAtivo_leDeCircuitosProperties_true() {
        assertTrue(CarregadorRecursos.circuitoAtivo("albert_park_mro.xml"),
                "albert_park está com ativo=true em circuitos.properties");
    }

    @Test
    void circuitoAtivo_leDeCircuitosProperties_false() {
        assertFalse(CarregadorRecursos.circuitoAtivo("monza_mro.xml"),
                "monza está com ativo=false em circuitos.properties");
    }

    @Test
    void circuitoAtivo_arquivoInexistente_retornaFalseSemLancarExcecao() {
        assertFalse(CarregadorRecursos.circuitoAtivo("nao_existe_mro.xml"));
    }

    /**
     * Mesmo resultado do caminho pesado (carregamento completo, que também
     * popula {@code ativo} a partir de {@code circuitos.properties}), para
     * todos os circuitos: garante que a leitura leve não diverge da verdade
     * do bean.
     */
    @Test
    void circuitoAtivo_coincideComOCarregamentoCompleto() throws Exception {
        java.util.Map<String, String> todos = new java.util.Properties() {
            {
                load(CarregadorRecursos.recursoComoStream("properties/circuitos.properties"));
            }
        }.entrySet().stream().collect(java.util.stream.Collectors.toMap(
                e -> (String) e.getKey(), e -> (String) e.getValue()));

        for (String nmCircuito : todos.keySet()) {
            boolean leve = CarregadorRecursos.circuitoAtivo(nmCircuito);
            boolean completo = CarregadorRecursos.carregarCircuito(nmCircuito).isAtivo();
            org.junit.jupiter.api.Assertions.assertEquals(completo, leve,
                    nmCircuito + ": leitura leve do ativo deveria coincidir com a desserialização completa");
        }
    }
}
