package br.flmane.controles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;

import br.flmane.entidades.Clima;
import br.nnpe.Global;

/**
 * "molhado%" é o estado contínuo (0.0 a 1.0) que ControleClima usa para
 * interpolar os bônus/penalidades de ganho entre seco e chuva, independente
 * do clima categórico exibido. Sobe/desce à taxa constante de 1.0 unidade a
 * cada Global.DURACAO_RAMPA_MOLHADO_MS (fixo, 1min30), reversível a partir do
 * valor atual.
 */
class ControleClimaMolhadoTest {

    // tempoCicloCircuito escolhido pra dar exatamente 10 ciclos por rampa completa
    // (Global.DURACAO_RAMPA_MOLHADO_MS / TEMPO_CICLO_MS == 10), preservando os
    // mesmos números redondos usados nas asserções abaixo.
    private static final long TEMPO_CICLO_MS = Global.DURACAO_RAMPA_MOLHADO_MS / 10;

    private final int[] cicloAtual = {0};

    private ControleClima criarControle() {
        InterfaceJogo controleJogo = mock(InterfaceJogo.class);
        when(controleJogo.tempoCicloCircuito()).thenReturn(TEMPO_CICLO_MS);
        when(controleJogo.getCicloAtual()).thenAnswer(inv -> cicloAtual[0]);
        return new ControleClima(controleJogo, 20);
    }

    @Test
    void chuvaComecando_rampaSobeGradualmenteAte1() {
        cicloAtual[0] = 0;
        ControleClima controle = criarControle();
        controle.setClima(Clima.NUBLADO);
        assertEquals(0.0, controle.getMolhado(), 1e-9);

        controle.setClima(Clima.CHUVA);
        assertEquals(0.0, controle.getMolhado(), 1e-9, "no instante da transição, a rampa ainda não avançou");

        cicloAtual[0] = 5;
        assertEquals(0.5, controle.getMolhado(), 1e-9, "metade dos 10 ciclos da rampa");

        cicloAtual[0] = 10;
        assertEquals(1.0, controle.getMolhado(), 1e-9, "rampa completa");

        cicloAtual[0] = 20;
        assertEquals(1.0, controle.getMolhado(), 1e-9, "não ultrapassa 1.0 além do fim da rampa");
    }

    @Test
    void chuvaParando_rampaDesceGradualmenteAte0() {
        cicloAtual[0] = 0;
        ControleClima controle = criarControle();
        controle.setClima(Clima.CHUVA);
        cicloAtual[0] = 10;
        assertEquals(1.0, controle.getMolhado(), 1e-9);

        controle.setClima(Clima.NUBLADO);
        assertEquals(1.0, controle.getMolhado(), 1e-9, "no instante da transição, a rampa ainda não recuou");

        cicloAtual[0] = 15;
        assertEquals(0.5, controle.getMolhado(), 1e-9, "metade dos 10 ciclos da rampa de descida");

        cicloAtual[0] = 20;
        assertEquals(0.0, controle.getMolhado(), 1e-9, "rampa de descida completa");
    }

    @Test
    void transicaoSolNubladoIsolada_naoAlteraMolhado() {
        cicloAtual[0] = 0;
        ControleClima controle = criarControle();
        controle.setClima(Clima.SOL);
        assertEquals(0.0, controle.getMolhado(), 1e-9);

        cicloAtual[0] = 3;
        controle.setClima(Clima.NUBLADO);
        assertEquals(0.0, controle.getMolhado(), 1e-9);

        cicloAtual[0] = 7;
        controle.setClima(Clima.SOL);
        assertEquals(0.0, controle.getMolhado(), 1e-9);
    }

    @Test
    void reversaoNoMeioDaRampa_inverteAPartirDoValorAtualSemSaltar() {
        cicloAtual[0] = 0;
        ControleClima controle = criarControle();
        controle.setClima(Clima.NUBLADO);
        controle.setClima(Clima.CHUVA); // inicia rampa 0.0 -> 1.0 a partir do ciclo 0

        cicloAtual[0] = 4;
        assertEquals(0.4, controle.getMolhado(), 1e-9, "40% do caminho (4 de 10 ciclos)");

        controle.setClima(Clima.NUBLADO); // reverte: alvo agora é 0.0, a partir do valor atual
        assertEquals(0.4, controle.getMolhado(), 1e-9, "não salta para 1.0 nem para 0.0 antes de inverter");

        cicloAtual[0] = 6;
        assertEquals(0.2, controle.getMolhado(), 1e-9, "descendo gradualmente a partir de 0.4, na mesma taxa constante");

        cicloAtual[0] = 8;
        assertEquals(0.0, controle.getMolhado(), 1e-9, "alcança 0.0 após percorrer a distância de 0.4 na taxa constante (4 ciclos)");
    }
}
