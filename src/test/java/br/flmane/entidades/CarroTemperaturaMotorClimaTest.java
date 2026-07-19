package br.flmane.entidades;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;

import org.junit.jupiter.api.Test;

import br.flmane.controles.InterfaceJogo;

/**
 * Taxa de aquecimento do motor (giro máximo) passou a variar com o clima:
 * NUBLADO mantém o incremento padrão de hoje (+1/ciclo, inalterado). SOL
 * esquenta um pouco mais rápido (30% de chance de +2). CHUVA esquenta mais
 * devagar (40% de chance de pular o incremento, +0).
 */
class CarroTemperaturaMotorClimaTest {

    private void invocaProcessaTemperaturaMotor(Carro carro) throws Exception {
        Method metodo = Carro.class.getDeclaredMethod("processaTemperaturaMotor");
        metodo.setAccessible(true);
        metodo.invoke(carro);
    }

    private Carro criarCarroGiroMax(String clima, double valorAleatorioSorteado) {
        InterfaceJogo controleJogo = mock(InterfaceJogo.class);
        GameRandom random = mock(GameRandom.class);
        when(controleJogo.getRandom()).thenReturn(random);
        when(random.nextDouble()).thenReturn(valorAleatorioSorteado);
        when(controleJogo.getClima()).thenReturn(clima);

        Piloto piloto = new Piloto();
        piloto.setControleJogo(controleJogo);
        Carro carro = new Carro();
        carro.setPiloto(piloto);
        piloto.setCarro(carro);
        carro.setGiro(Carro.GIRO_MAX_VAL);
        carro.setTempMax(100);
        carro.setTemperaturaMotor(0);
        return carro;
    }

    @Test
    void nublado_incrementoPadraoDeUmPontoInalterado() throws Exception {
        Carro carro = criarCarroGiroMax(Clima.NUBLADO, 0.0);

        invocaProcessaTemperaturaMotor(carro);

        assertEquals(1, carro.getTemperaturaMotor());
    }

    @Test
    void sol_comSorteioAbaixoDoLimiar_esquentaDoisPontos() throws Exception {
        Carro carro = criarCarroGiroMax(Clima.SOL, 0.1);

        invocaProcessaTemperaturaMotor(carro);

        assertEquals(2, carro.getTemperaturaMotor());
    }

    @Test
    void sol_comSorteioAcimaDoLimiar_esquentaUmPontoComoPadrao() throws Exception {
        Carro carro = criarCarroGiroMax(Clima.SOL, 0.9);

        invocaProcessaTemperaturaMotor(carro);

        assertEquals(1, carro.getTemperaturaMotor());
    }

    @Test
    void chuva_comSorteioAbaixoDoLimiar_pulaOAquecimentoNesseCiclo() throws Exception {
        Carro carro = criarCarroGiroMax(Clima.CHUVA, 0.1);

        invocaProcessaTemperaturaMotor(carro);

        assertEquals(0, carro.getTemperaturaMotor());
    }

    @Test
    void chuva_comSorteioAcimaDoLimiar_esquentaUmPontoComoPadrao() throws Exception {
        Carro carro = criarCarroGiroMax(Clima.CHUVA, 0.9);

        invocaProcessaTemperaturaMotor(carro);

        assertEquals(1, carro.getTemperaturaMotor());
    }
}
