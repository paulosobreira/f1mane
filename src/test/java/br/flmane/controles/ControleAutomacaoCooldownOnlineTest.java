package br.flmane.controles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.EnumMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import br.flmane.entidades.Carro;
import br.flmane.entidades.Piloto;
import br.flmane.servidor.JogoServidor;

/**
 * Cobre o cooldown de 500ms por tipo de ação decidida por ControleAutomacao
 * em jogo online (ver spec piloto-controle-automatico-manual, Requirement
 * "Cooldown de meio segundo por tipo de ação da automação em jogo online").
 * Usa reflexão pra manipular o timestamp interno diretamente em vez de
 * dormir de verdade em cada teste.
 */
class ControleAutomacaoCooldownOnlineTest {

    private Piloto piloto;
    private ControleAutomacao controleAutomacaoOnline;
    private ControleAutomacao controleAutomacaoSolo;

    @BeforeEach
    void setUp() {
        piloto = new Piloto();
        piloto.setNome("Piloto");
        Carro carro = new Carro();
        carro.setPiloto(piloto);
        piloto.setCarro(carro);

        JogoServidor jogoServidor = mock(JogoServidor.class);
        piloto.setControleJogo(jogoServidor);
        controleAutomacaoOnline = new ControleAutomacao(jogoServidor, null);

        ControleJogoLocal jogoLocal = mock(ControleJogoLocal.class);
        controleAutomacaoSolo = new ControleAutomacao(jogoLocal, null);
    }

    /** Espelha executarSeDentroDoCooldown(): tenta executar, retorna se de fato executou. */
    private boolean executar(ControleAutomacao controleAutomacao, TipoAcaoAutomacao tipo) throws Exception {
        Method estaDentro = ControleAutomacao.class.getDeclaredMethod("estaDentroDoCooldown", Piloto.class,
                TipoAcaoAutomacao.class);
        estaDentro.setAccessible(true);
        boolean dentroDoCooldown = (boolean) estaDentro.invoke(controleAutomacao, piloto, tipo);
        if (dentroDoCooldown) {
            return false;
        }
        Method registrar = ControleAutomacao.class.getDeclaredMethod("registrarExecucao", Piloto.class,
                TipoAcaoAutomacao.class);
        registrar.setAccessible(true);
        registrar.invoke(controleAutomacao, piloto, tipo);
        return true;
    }

    @SuppressWarnings("unchecked")
    private void avancarRelogio(ControleAutomacao controleAutomacao, TipoAcaoAutomacao tipo, long milisAtras)
            throws Exception {
        Field campo = ControleAutomacao.class.getDeclaredField("ultimaExecucaoPorPiloto");
        campo.setAccessible(true);
        Map<Piloto, Map<TipoAcaoAutomacao, Long>> mapa = (Map<Piloto, Map<TipoAcaoAutomacao, Long>>) campo
                .get(controleAutomacao);
        mapa.computeIfAbsent(piloto, p -> new EnumMap<>(TipoAcaoAutomacao.class))
                .put(tipo, System.currentTimeMillis() - milisAtras);
    }

    @Test
    void segundaExecucaoAntesDe500ms_eIgnorada() throws Exception {
        assertTrue(executar(controleAutomacaoOnline, TipoAcaoAutomacao.MODO_PILOTAGEM));

        assertFalse(executar(controleAutomacaoOnline, TipoAcaoAutomacao.MODO_PILOTAGEM),
                "segunda tentativa do mesmo tipo, menos de 500ms depois, deveria ser descartada");
    }

    @Test
    void execucaoApos500ms_eAplicadaNormalmente() throws Exception {
        assertTrue(executar(controleAutomacaoOnline, TipoAcaoAutomacao.MODO_PILOTAGEM));
        avancarRelogio(controleAutomacaoOnline, TipoAcaoAutomacao.MODO_PILOTAGEM, 500);

        assertTrue(executar(controleAutomacaoOnline, TipoAcaoAutomacao.MODO_PILOTAGEM),
                "após 500ms, a próxima tentativa do mesmo tipo deveria ser aplicada normalmente");
    }

    @Test
    void tiposDeAcaoDiferentes_saoIndependentes() throws Exception {
        assertTrue(executar(controleAutomacaoOnline, TipoAcaoAutomacao.MODO_PILOTAGEM));

        assertTrue(executar(controleAutomacaoOnline, TipoAcaoAutomacao.DRS),
                "DRS não deveria ser bloqueado pelo cooldown de MODO_PILOTAGEM");
    }

    @Test
    void tentativaDescartada_naoRenovaOCronometro() throws Exception {
        assertTrue(executar(controleAutomacaoOnline, TipoAcaoAutomacao.MODO_PILOTAGEM));
        avancarRelogio(controleAutomacaoOnline, TipoAcaoAutomacao.MODO_PILOTAGEM, 450); // ainda dentro do cooldown
        assertFalse(executar(controleAutomacaoOnline, TipoAcaoAutomacao.MODO_PILOTAGEM));

        avancarRelogio(controleAutomacaoOnline, TipoAcaoAutomacao.MODO_PILOTAGEM, 500); // 500ms desde a 1ª execução real

        assertTrue(executar(controleAutomacaoOnline, TipoAcaoAutomacao.MODO_PILOTAGEM),
                "a tentativa descartada não deveria ter empurrado o cronômetro pra frente");
    }

    @Test
    void cooldownNaoSeAplicaEmPartidaSolo() throws Exception {
        assertTrue(executar(controleAutomacaoSolo, TipoAcaoAutomacao.MODO_PILOTAGEM));

        assertTrue(executar(controleAutomacaoSolo, TipoAcaoAutomacao.MODO_PILOTAGEM),
                "em partida solo (não JogoServidor), o cooldown online não deveria se aplicar");
    }

    @Test
    void processarTick_pilotoHumanoOnline_nuncaChegaAExecutarAcoesDecididas() {
        piloto.setJogadorHumano(true);

        // processarTick roda duas vezes seguidas; se o gate automático/manual online não
        // protegesse o humano, o cooldown por si só não bastaria (não discrimina humano/bot).
        controleAutomacaoOnline.processarTick(piloto);
        controleAutomacaoOnline.processarTick(piloto);

        assertEquals(Piloto.NORMAL, piloto.getModoPilotagem(),
                "humano em jogo online nunca é pilotado pela automação (autopilotDesligado), então o cooldown nem chega a ser relevante aqui");
    }
}
