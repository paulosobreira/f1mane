package br.flmane.controles;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;

import br.flmane.entidades.Clima;
import br.flmane.entidades.GameRandom;
import br.nnpe.Global;

/**
 * {@code ThreadMudancaClima} dorme um valor aleatório entre 0 e
 * {@code Global.ATRASO_MAX_MUDANCA_CLIMA_MS} (1 minuto fixo) antes de efetivar
 * a mudança de clima sorteada — não depende mais do tempo médio de volta.
 */
class ThreadMudancaClimaDisparoTest {

    @Test
    void run_dormeComLimiteSuperiorFixoDeUmMinuto() throws Exception {
        InterfaceJogo controleJogo = mock(InterfaceJogo.class);
        GameRandom random = mock(GameRandom.class);
        when(controleJogo.getRandom()).thenReturn(random);
        when(random.intervalo(anyInt(), anyInt())).thenReturn(0);

        ControleClima controleClima = new ControleClima(controleJogo, 20);
        controleClima.setClima(Clima.NUBLADO);

        new ThreadMudancaClima(controleClima).run();

        verify(random).intervalo(0, (int) Global.ATRASO_MAX_MUDANCA_CLIMA_MS);
    }

    /**
     * Regressão: {@code processada} só era marcado no fim do caminho de sucesso.
     * Se run() lançasse qualquer exceção (ex.: InterruptedException por
     * ControleClima.matarThreads() interrompendo o sleep), a thread ficava presa
     * em isProcessada()==false pra sempre — e como ControleClima só cria uma nova
     * ThreadMudancaClima quando a anterior termina de processar, uma única falha
     * travava processaPossivelMudancaClima() em "ADIADA" pelo resto da corrida,
     * volta após volta.
     */
    @Test
    void run_falhaComExcecao_aindaAssimMarcaProcessada() throws Exception {
        InterfaceJogo controleJogo = mock(InterfaceJogo.class);
        GameRandom random = mock(GameRandom.class);
        when(controleJogo.getRandom()).thenReturn(random);
        when(random.intervalo(anyInt(), anyInt())).thenThrow(new RuntimeException("falha simulada"));

        ControleClima controleClima = new ControleClima(controleJogo, 20);
        controleClima.setClima(Clima.NUBLADO);

        ThreadMudancaClima thread = new ThreadMudancaClima(controleClima);
        thread.run();

        assertTrue(thread.isProcessada(),
                "mesmo com excecao, deve marcar processada=true, senao ControleClima trava em ADIADA pra sempre");
    }
}
