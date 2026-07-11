package br.f1mane.entidades;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.awt.Point;
import java.lang.reflect.Method;

import org.junit.jupiter.api.Test;

import br.f1mane.controles.InterfaceJogo;

/**
 * Cobre o bug relatado pelo usuário: o piloto no traçado de fuga (4/5)
 * parecia rápido demais mesmo com a redução de 0.4x sobre o ganho
 * (Piloto.processaEscapadaDaPista). Causa raiz: {@code calculaModificadorPrincipal()}
 * escolhe a "escada" de valores de ganho (reta: 30-50; curva baixa: 10-20)
 * pelo tipo do nó ATUAL do piloto — que vem sempre da pista principal (mesmo
 * índice, independente do traçado lateral), nunca do nó interpolado da
 * própria escapada. Em zonas onde a pista principal é majoritariamente reta
 * (ex.: uma das 3 zonas de Interlagos, 72% reta), a escapada herdava a
 * escada mais rápida do jogo. Corrigido forçando a escada de curva baixa
 * (a mais lenta) sempre que {@code getTracado()} é 4 ou 5, independente do
 * tipo do nó da pista principal naquele índice.
 */
class PilotoGanhoTracadoDeFugaTest {

    private Piloto criarPiloto(int tracado, java.awt.Color tipoNo) {
        InterfaceJogo controleJogo = mock(InterfaceJogo.class);
        // seed=1 faz GameRandom.nextDouble() sempre retornar 0.5 (determinístico) — ver GameRandom.java.
        GameRandom random = new GameRandom(1);
        when(controleJogo.getRandom()).thenReturn(random);
        when(controleJogo.isChovendo()).thenReturn(false);
        when(controleJogo.isErs()).thenReturn(false);

        Piloto piloto = new Piloto();
        piloto.setControleJogo(controleJogo);
        Carro carro = new Carro();
        carro.setPiloto(piloto);
        // potencia/aerodinamica/freios em 0 e habilidade máxima: isola exatamente a escolha da
        // escada (reta vs curva baixa), sem os testes de carro "subindo de nível" a escada.
        carro.setPotencia(0);
        carro.setAerodinamica(0);
        carro.setFreios(0);
        piloto.setCarro(carro);
        piloto.setHabilidade(1000);
        piloto.setModoPilotagem(Piloto.LENTO);
        carro.setGiro(Carro.GIRO_MIN_VAL);
        piloto.setTracado(tracado);
        No no = new No();
        no.setIndex(500);
        no.setPoint(new Point(500, 100));
        no.setTipo(tipoNo);
        piloto.setNoAtual(no);
        return piloto;
    }

    private int chamarCalculaModificadorPrincipal(Piloto piloto) throws Exception {
        Method m = Piloto.class.getDeclaredMethod("calculaModificadorPrincipal");
        m.setAccessible(true);
        return (int) m.invoke(piloto);
    }

    @Test
    void noTracadoDeFuga_forcaEscadaDeCurvaBaixa_mesmoComNoDaPistaPrincipalSendoReta() throws Exception {
        Piloto piloto = criarPiloto(4, No.RETA);

        int ganho = chamarCalculaModificadorPrincipal(piloto);

        assertEquals(10, ganho,
                "no traçado de fuga, mesmo com o nó da pista principal marcado como reta, deveria usar a escada de curva baixa (10-20), não a de reta (30-50)");
    }

    @Test
    void tracado5_tambemForcaEscadaDeCurvaBaixa() throws Exception {
        Piloto piloto = criarPiloto(5, No.RETA);

        int ganho = chamarCalculaModificadorPrincipal(piloto);

        assertEquals(10, ganho, "traçado 5 (fuga) também deveria forçar a escada de curva baixa");
    }

    @Test
    void foraDoTracadoDeFuga_continuaUsandoATipoDoNoDaPistaPrincipal_semAlteracao() throws Exception {
        // Regressão: fora do traçado 4/5, o comportamento não deveria mudar — continua usando o
        // tipo real do nó (aqui, reta, escada 30-50).
        Piloto piloto = criarPiloto(1, No.RETA);

        int ganho = chamarCalculaModificadorPrincipal(piloto);

        assertEquals(30, ganho,
                "fora do traçado de fuga, o nó reta da pista principal deveria continuar usando a escada de reta (30-50), sem alteração");
    }

}
