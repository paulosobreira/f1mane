package br.f1mane.controles;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import br.f1mane.entidades.Circuito;
import br.f1mane.entidades.Piloto;
import br.f1mane.entidades.Volta;
import br.nnpe.Global;

/**
 * {@code tempoMedioVoltaMs()} calcula a média das voltas já registradas do
 * líder ({@code pilotos.get(0)}), convertida para ms via
 * {@code tempoCicloCircuito()}, com fallback fixo de
 * {@code Global.TEMPO_MEDIO_VOLTA_CLIMA_MINIMO_MS} (1 minuto) antes da
 * primeira volta do líder fechar.
 *
 * O fallback já passou por duas versões anteriores, ambas descartadas:
 * {@code nosDaPista.size() * tempoCicloCircuito()} (tratava nós densos em
 * pixels como ciclos de avanço, dando ~45 minutos pro Interlagos) e depois
 * {@code Circuito.estimarTempoVoltaMs()} (fisicamente correto, mas podia
 * passar de 1 minuto — ex.: ~87.7s pro Interlagos — atrasando a primeira
 * tentativa de mudança de clima da corrida além do 1 minuto pedido).
 */
class ControleJogoLocalTempoMedioVoltaMsTest {

    private ControleJogoLocal criarControle(int ciclo) throws Exception {
        ControleJogoLocal controle = new ControleJogoLocal(1L);
        Circuito circuito = new Circuito();
        circuito.setCiclo(ciclo);
        controle.circuito = circuito;
        return controle;
    }

    @Test
    void tempoMedioVoltaMs_usaFallbackFixoDeUmMinutoSemVoltasRegistradas() throws Exception {
        ControleJogoLocal controle = criarControle(160);
        Piloto lider = new Piloto();
        controle.pilotos = new ArrayList<>(List.of(lider));

        assertEquals(Global.TEMPO_MEDIO_VOLTA_CLIMA_MINIMO_MS, controle.tempoMedioVoltaMs());
        assertEquals(60_000L, controle.tempoMedioVoltaMs(), "fallback deve garantir a primeira avaliacao em ate 1 minuto");
    }

    @Test
    void tempoMedioVoltaMs_mediaComUmaVoltaRegistrada() throws Exception {
        ControleJogoLocal controle = criarControle(100);
        Piloto lider = new Piloto();
        lider.getVoltas().add(new Volta(Long.valueOf(300)));
        controle.pilotos = new ArrayList<>(List.of(lider));

        assertEquals(300L * 100L, controle.tempoMedioVoltaMs());
    }

    @Test
    void tempoMedioVoltaMs_mediaComVariasVoltasRegistradas() throws Exception {
        ControleJogoLocal controle = criarControle(100);
        Piloto lider = new Piloto();
        lider.getVoltas().add(new Volta(Long.valueOf(200)));
        lider.getVoltas().add(new Volta(Long.valueOf(300)));
        lider.getVoltas().add(new Volta(Long.valueOf(400)));
        controle.pilotos = new ArrayList<>(List.of(lider));

        assertEquals(300L * 100L, controle.tempoMedioVoltaMs());
    }
}
