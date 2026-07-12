package br.f1mane.entidades;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import br.f1mane.controles.InterfaceJogo;
import br.f1mane.servidor.JogoServidor;
import br.nnpe.Global;

/**
 * Cobre a regra dos 3 estados de piloto automatico dentro de
 * processaMudarTracado(): online sempre manual, solo manual sempre manual,
 * solo automatico ligado exceto durante a janela manualTemporario - e
 * confirma que box, desvio de safety car e fila indiana nao dependem dessa
 * chave (continuam valendo pra ambos os modos, ou sao exclusivos de bots).
 */
class PilotoAutopilotModoTest {

    private static final int TAMANHO_PISTA = 1000;

    private List<No> criarPista() {
        List<No> pista = new ArrayList<>();
        for (int i = 0; i < TAMANHO_PISTA; i++) {
            No no = new No();
            no.setIndex(i);
            no.setPoint(new Point(i, 100));
            pista.add(no);
        }
        return pista;
    }

    private Piloto criarPiloto(InterfaceJogo controleJogo, List<No> pista, List<Piloto> pilotos, String nome,
            int index, int tracado) {
        Piloto piloto = new Piloto();
        piloto.setNome(nome);
        Carro carro = new Carro();
        carro.setPiloto(piloto);
        piloto.setCarro(carro);
        piloto.setControleJogo(controleJogo);
        piloto.setNoAtual(pista.get(index));
        piloto.setTracado(tracado);
        pilotos.add(piloto);
        return piloto;
    }

    private void configurarBase(InterfaceJogo controleJogo, List<No> pista, List<Piloto> pilotos) {
        when(controleJogo.getNosDaPista()).thenReturn(pista);
        when(controleJogo.getNosDoBox()).thenReturn(new ArrayList<>());
        when(controleJogo.getPilotos()).thenReturn(pilotos);
        when(controleJogo.getPilotosCopia()).thenReturn(pilotos);
        when(controleJogo.obterPista(any())).thenReturn(pista);
        when(controleJogo.isModoQualify()).thenReturn(false);
        when(controleJogo.isSafetyCarNaPista()).thenReturn(false);
        when(controleJogo.isAtualizacaoSuave()).thenReturn(false);
        when(controleJogo.verificaNoPitLane(any())).thenReturn(false);
        when(controleJogo.tempoCicloCircuito()).thenReturn(200L);

        GameRandom random = mock(GameRandom.class);
        when(random.intervalo(1, 2)).thenReturn(1);
        when(controleJogo.getRandom()).thenReturn(random);

        Circuito circuito = mock(Circuito.class);
        when(circuito.getIndiceTracado()).thenReturn(24.0);
        when(circuito.getLadoBoxSaidaBox()).thenReturn(2);
        when(controleJogo.getCircuito()).thenReturn(circuito);
    }

    private void configurarRetardatarioProximo(InterfaceJogo controleJogo, Piloto humano, Piloto retardatario) {
        when(controleJogo.obterCarroNaFrenteRetardatario(humano, false)).thenReturn(retardatario.getCarro());
        when(controleJogo.calculaDiffParaProximoRetardatario(humano, false)).thenReturn(10);
        when(controleJogo.calculaDiffParaProximoRetardatario(humano, true)).thenReturn(10);
        when(controleJogo.calculaDiferencaParaAnterior(humano)).thenReturn(500);
        when(controleJogo.calculaDiferencaParaProximo(humano)).thenReturn(500);
    }

    // ---- 3.1: online, jogador humano nunca desvia automaticamente de retardatario ----

    @Test
    void online_humano_nuncaDesviaAutomaticoDeRetardatario() {
        JogoServidor controleJogo = mock(JogoServidor.class);
        List<No> pista = criarPista();
        List<Piloto> pilotos = new ArrayList<>();
        configurarBase(controleJogo, pista, pilotos);
        when(controleJogo.getAutomaticoManual()).thenReturn(Global.CONTROLE_AUTOMATICO);

        Piloto humano = criarPiloto(controleJogo, pista, pilotos, "Humano", 100, 1);
        humano.setJogadorHumano(true);
        Piloto retardatario = criarPiloto(controleJogo, pista, pilotos, "Retardatario", 700, 1);
        configurarRetardatarioProximo(controleJogo, humano, retardatario);

        humano.calculaCarrosAdjacentes();
        humano.processaMudarTracado();

        assertEquals(1, humano.getTracado());
    }

    // ---- 3.2: solo manual, jogador humano nunca desvia automaticamente de retardatario ----

    @Test
    void soloManual_humano_nuncaDesviaAutomaticoDeRetardatario() {
        InterfaceJogo controleJogo = mock(InterfaceJogo.class);
        List<No> pista = criarPista();
        List<Piloto> pilotos = new ArrayList<>();
        configurarBase(controleJogo, pista, pilotos);
        when(controleJogo.getAutomaticoManual()).thenReturn(Global.CONTROLE_MANUAL);

        Piloto humano = criarPiloto(controleJogo, pista, pilotos, "Humano", 100, 1);
        humano.setJogadorHumano(true);
        Piloto retardatario = criarPiloto(controleJogo, pista, pilotos, "Retardatario", 700, 1);
        configurarRetardatarioProximo(controleJogo, humano, retardatario);

        humano.calculaCarrosAdjacentes();
        humano.processaMudarTracado();

        assertEquals(1, humano.getTracado());
    }

    // ---- 3.3: solo automatico, desvia com manualTemporario zerado; nao desvia durante a janela ----

    @Test
    void soloAutomatico_humano_desviaAutomaticoDeRetardatarioSemEntradaRecente() {
        InterfaceJogo controleJogo = mock(InterfaceJogo.class);
        List<No> pista = criarPista();
        List<Piloto> pilotos = new ArrayList<>();
        configurarBase(controleJogo, pista, pilotos);
        when(controleJogo.getAutomaticoManual()).thenReturn(Global.CONTROLE_AUTOMATICO);

        Piloto humano = criarPiloto(controleJogo, pista, pilotos, "Humano", 100, 1);
        humano.setJogadorHumano(true);
        Piloto retardatario = criarPiloto(controleJogo, pista, pilotos, "Retardatario", 700, 1);
        configurarRetardatarioProximo(controleJogo, humano, retardatario);

        humano.calculaCarrosAdjacentes();
        humano.processaMudarTracado();

        assertEquals(0, humano.getTracado());
    }

    @Test
    void soloAutomatico_humano_naoDesviaDuranteJanelaManualTemporario() {
        InterfaceJogo controleJogo = mock(InterfaceJogo.class);
        List<No> pista = criarPista();
        List<Piloto> pilotos = new ArrayList<>();
        configurarBase(controleJogo, pista, pilotos);
        when(controleJogo.getAutomaticoManual()).thenReturn(Global.CONTROLE_AUTOMATICO);

        Piloto humano = criarPiloto(controleJogo, pista, pilotos, "Humano", 100, 1);
        humano.setJogadorHumano(true);
        humano.setManualTemporario();
        Piloto retardatario = criarPiloto(controleJogo, pista, pilotos, "Retardatario", 700, 1);
        configurarRetardatarioProximo(controleJogo, humano, retardatario);

        humano.calculaCarrosAdjacentes();
        humano.processaMudarTracado();

        assertEquals(1, humano.getTracado());
    }

    // ---- 3.4: box independe do modo (manual local e online) ----

    @Test
    void soloManual_humano_snapDeBoxContinuaAutomatico() {
        InterfaceJogo controleJogo = mock(InterfaceJogo.class);
        List<No> pista = criarPista();
        List<Piloto> pilotos = new ArrayList<>();
        configurarBase(controleJogo, pista, pilotos);
        when(controleJogo.getAutomaticoManual()).thenReturn(Global.CONTROLE_MANUAL);
        when(controleJogo.verificaSaidaBox(any())).thenReturn(true);

        Piloto humano = criarPiloto(controleJogo, pista, pilotos, "Humano", 100, 0);
        humano.setJogadorHumano(true);
        humano.setBoxSaiuNestaVolta(true);

        humano.processaMudarTracado();

        assertEquals(2, humano.getTracado());
    }

    @Test
    void online_humano_snapDeBoxContinuaAutomatico() {
        JogoServidor controleJogo = mock(JogoServidor.class);
        List<No> pista = criarPista();
        List<Piloto> pilotos = new ArrayList<>();
        configurarBase(controleJogo, pista, pilotos);
        when(controleJogo.getAutomaticoManual()).thenReturn(Global.CONTROLE_AUTOMATICO);
        when(controleJogo.verificaSaidaBox(any())).thenReturn(true);

        Piloto humano = criarPiloto(controleJogo, pista, pilotos, "Humano", 100, 0);
        humano.setJogadorHumano(true);
        humano.setBoxSaiuNestaVolta(true);

        humano.processaMudarTracado();

        assertEquals(2, humano.getTracado());
    }

    // ---- 3.5: desvio de safety car independe do modo (manual local e online) ----

    @Test
    void soloManual_humano_desviaDeCarroBatidoSobSafetyCar() {
        InterfaceJogo controleJogo = mock(InterfaceJogo.class);
        List<No> pista = criarPista();
        List<Piloto> pilotos = new ArrayList<>();
        configurarBase(controleJogo, pista, pilotos);
        when(controleJogo.getAutomaticoManual()).thenReturn(Global.CONTROLE_MANUAL);
        when(controleJogo.isSafetyCarNaPista()).thenReturn(true);

        Piloto humano = criarPiloto(controleJogo, pista, pilotos, "Humano", 450, 1);
        humano.setJogadorHumano(true);
        Piloto batido = criarPiloto(controleJogo, pista, pilotos, "Batido", 500, 1);
        when(controleJogo.getPilotoBateu()).thenReturn(batido);

        humano.processaMudarTracado();

        assertEquals(0, humano.getTracado());
    }

    @Test
    void online_humano_desviaDeCarroBatidoSobSafetyCar() {
        JogoServidor controleJogo = mock(JogoServidor.class);
        List<No> pista = criarPista();
        List<Piloto> pilotos = new ArrayList<>();
        configurarBase(controleJogo, pista, pilotos);
        when(controleJogo.getAutomaticoManual()).thenReturn(Global.CONTROLE_AUTOMATICO);
        when(controleJogo.isSafetyCarNaPista()).thenReturn(true);

        Piloto humano = criarPiloto(controleJogo, pista, pilotos, "Humano", 450, 1);
        humano.setJogadorHumano(true);
        Piloto batido = criarPiloto(controleJogo, pista, pilotos, "Batido", 500, 1);
        when(controleJogo.getPilotoBateu()).thenReturn(batido);

        humano.processaMudarTracado();

        assertEquals(0, humano.getTracado());
    }

    // ---- 3.6: fila indiana e exclusiva de bots, nunca afeta o jogador humano ----

    @Test
    void humano_nuncaEscapaDeFilaIndiana() {
        InterfaceJogo controleJogo = mock(InterfaceJogo.class);
        List<No> pista = criarPista();
        List<Piloto> pilotos = new ArrayList<>();
        configurarBase(controleJogo, pista, pilotos);
        when(controleJogo.getAutomaticoManual()).thenReturn(Global.CONTROLE_AUTOMATICO);

        Piloto humano = criarPiloto(controleJogo, pista, pilotos, "Humano", 100, 0);
        humano.setJogadorHumano(true);
        Piloto frente = criarPiloto(controleJogo, pista, pilotos, "Frente", 140, 0);
        humano.setColisao(frente);
        humano.setGanho(5);
        for (int i = 0; i < 8; i++) {
            humano.processaPenalidadeColisao();
        }

        assertFalse(humano.tentarEscaparFilaIndiana());
        assertEquals(0, humano.getTracado());
    }

    @Test
    void bot_continuaEscapandoDeFilaIndianaNormalmente() {
        InterfaceJogo controleJogo = mock(InterfaceJogo.class);
        List<No> pista = criarPista();
        List<Piloto> pilotos = new ArrayList<>();
        configurarBase(controleJogo, pista, pilotos);
        when(controleJogo.getAutomaticoManual()).thenReturn(Global.CONTROLE_AUTOMATICO);

        Piloto bot = criarPiloto(controleJogo, pista, pilotos, "Bot", 100, 0);
        Piloto frente = criarPiloto(controleJogo, pista, pilotos, "Frente", 140, 0);
        bot.setColisao(frente);
        bot.setGanho(5);
        for (int i = 0; i < 8; i++) {
            bot.processaPenalidadeColisao();
        }

        assertTrue(bot.tentarEscaparFilaIndiana());
        assertEquals(1, bot.getTracado());
    }

    // ---- 6: modoPilotagem/giro do jogador humano manual — exceções sempre-ativas continuam,
    //         decisão proativa da IA (processaIAnovoIndex/modoIADefesaAtaque) não afeta ----

    @Test
    void humanoManual_eForcadoParaLentoAoSerUltrapassadoComoRetardatario() {
        InterfaceJogo controleJogo = mock(InterfaceJogo.class);
        List<No> pista = criarPista();
        List<Piloto> pilotos = new ArrayList<>();
        configurarBase(controleJogo, pista, pilotos);
        when(controleJogo.getAutomaticoManual()).thenReturn(Global.CONTROLE_MANUAL);
        when(controleJogo.isCorridaTerminada()).thenReturn(false);

        Piloto humano = criarPiloto(controleJogo, pista, pilotos, "Humano", 100, 1);
        humano.setJogadorHumano(true);
        humano.setModoPilotagem(Piloto.AGRESSIVO);
        humano.getCarro().setGiro(Carro.GIRO_MAX_VAL);
        humano.setNumeroVolta(1);
        humano.setPtosPista(500);

        Piloto bot = criarPiloto(controleJogo, pista, pilotos, "Bot", 90, 1);
        bot.setNumeroVolta(2);
        bot.setPtosPista(1000);

        bot.desviaPilotoNaFrente(bot, humano);

        assertEquals(Piloto.LENTO, humano.getModoPilotagem(),
                "desvio de retardatário continua forçando LENTO no humano manual — exceção sempre-ativa, não protegida pela chave automático/manual");
        assertEquals(Carro.GIRO_MIN_VAL, humano.getCarro().getGiro());
    }

    @Test
    void humanoManual_eForcadoParaLentoPelaBandeirada() {
        InterfaceJogo controleJogo = mock(InterfaceJogo.class);
        List<No> pista = criarPista();
        List<Piloto> pilotos = new ArrayList<>();
        configurarBase(controleJogo, pista, pilotos);
        when(controleJogo.getAutomaticoManual()).thenReturn(Global.CONTROLE_MANUAL);

        Piloto humano = criarPiloto(controleJogo, pista, pilotos, "Humano", 100, 1);
        humano.setJogadorHumano(true);
        humano.setModoPilotagem(Piloto.AGRESSIVO);
        humano.getCarro().setGiro(Carro.GIRO_MAX_VAL);
        humano.setRecebeuBanderada(true);

        // processaMudancaRegime() é privado — a bandeirada é o próprio gatilho de entrada dele
        // dentro do ciclo normal; aqui exercitamos via reflexão pra isolar só essa regra.
        try {
            java.lang.reflect.Method metodo = Piloto.class.getDeclaredMethod("processaMudancaRegime");
            metodo.setAccessible(true);
            metodo.invoke(humano);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }

        assertEquals(Piloto.LENTO, humano.getModoPilotagem(),
                "bandeirada continua forçando LENTO no humano manual — exceção sempre-ativa, não protegida pela chave automático/manual");
        assertEquals(Carro.GIRO_MIN_VAL, humano.getCarro().getGiro());
    }

    @Test
    void humanoManual_processaIAnovoIndex_naoAlteraModoPilotagemNemGiro() {
        InterfaceJogo controleJogo = mock(InterfaceJogo.class);
        List<No> pista = criarPista();
        List<Piloto> pilotos = new ArrayList<>();
        configurarBase(controleJogo, pista, pilotos);
        when(controleJogo.getAutomaticoManual()).thenReturn(Global.CONTROLE_MANUAL);

        Piloto humano = criarPiloto(controleJogo, pista, pilotos, "Humano", 100, 1);
        humano.setJogadorHumano(true);
        humano.setModoPilotagem(Piloto.AGRESSIVO);
        humano.getCarro().setGiro(Carro.GIRO_MAX_VAL);

        try {
            java.lang.reflect.Method metodo = Piloto.class.getDeclaredMethod("processaIAnovoIndex");
            metodo.setAccessible(true);
            metodo.invoke(humano);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }

        assertEquals(Piloto.AGRESSIVO, humano.getModoPilotagem(),
                "processaIAnovoIndex() retorna cedo pra humano manual (autopilotDesligado()) — único caminho realmente coberto pela chave automático/manual");
        assertEquals(Carro.GIRO_MAX_VAL, humano.getCarro().getGiro());
    }
}
