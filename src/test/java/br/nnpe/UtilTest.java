package br.nnpe;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/**
 * Cobre Util.processaValorPontosCarreira, responsável pelo custo/refund de
 * pontos por faixa de nível no modo carreira. Regressão do bug de pontos
 * infinitos no nível 999 corrigido no commit f47e7a74.
 */
class UtilTest {

    @Test
    void upgrade998Para999_debita50Pontos() {
        Numero numero = new Numero(1000);

        Util.processaValorPontosCarreira(998, 999, numero);

        assertEquals(950.0, numero.getNumero());
    }

    @Test
    void downgrade999Para998_credita50Pontos() {
        Numero numero = new Numero(1000);

        Util.processaValorPontosCarreira(999, 998, numero);

        assertEquals(1050.0, numero.getNumero());
    }

    @Test
    void cicloDowngradeUpgradeNoNivel999_naoAlteraSaldo() {
        Numero numero = new Numero(1000);

        Util.processaValorPontosCarreira(999, 998, numero);
        Util.processaValorPontosCarreira(998, 999, numero);

        assertEquals(1000.0, numero.getNumero());
    }

    @ParameterizedTest(name = "{0}->{1} custa {2} pontos")
    @CsvSource({
            // valorAtual, proximoValor, custoEsperado
            "100, 101, 1",
            "599, 600, 2",
            "600, 601, 2",
            "699, 700, 10",
            "700, 701, 10",
            "799, 800, 20",
            "800, 801, 20",
            "899, 900, 50",
            "900, 901, 50",
            "998, 999, 50"
    })
    void upgrade_seguePorFaixaDeCusto(int valorAtual, int proximoValor, double custoEsperado) {
        Numero numero = new Numero(2000);

        Util.processaValorPontosCarreira(valorAtual, proximoValor, numero);

        assertEquals(2000.0 - custoEsperado, numero.getNumero());
    }

    @ParameterizedTest(name = "{0}->{1} devolve {2} pontos")
    @CsvSource({
            "101, 100, 1",
            // downgrade saindo exatamente de uma centena cheia (600/700/800/900)
            // custa o mesmo da faixa superior, simetricamente ao upgrade que entrou nela
            "600, 599, 2",
            "601, 600, 2",
            "700, 699, 10",
            "701, 700, 10",
            "800, 799, 20",
            "801, 800, 20",
            "900, 899, 50",
            "901, 900, 50",
            "999, 998, 50"
    })
    void downgrade_seguePorFaixaDeRefund(int valorAtual, int proximoValor, double refundEsperado) {
        Numero numero = new Numero(2000);

        Util.processaValorPontosCarreira(valorAtual, proximoValor, numero);

        assertEquals(2000.0 + refundEsperado, numero.getNumero());
    }
}
