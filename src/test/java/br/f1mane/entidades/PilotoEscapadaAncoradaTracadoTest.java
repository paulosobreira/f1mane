package br.f1mane.entidades;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import br.f1mane.controles.InterfaceJogo;
import br.nnpe.Global;

/**
 * Cobre a escapada ancorada ao traçado (traçado 1/2 → 4/5 via
 * {@link ObjetoEscapada}): dois testes sequenciais por causa de risco
 * (agressividade+stress, depois pneus), um único par por zona por piloto por
 * volta (mesmo resultado marcado ou não), execução independente de
 * {@code modoPilotagem} no momento, e a trava de mudança de traçado enquanto
 * marcado. A derrapagem (traçado 0 → 1/2) tem seus próprios testes em
 * {@code PilotoDerrapagemTest}.
 */
class PilotoEscapadaAncoradaTracadoTest {

    private static final int TAMANHO_PISTA = 1000;

    private InterfaceJogo controleJogo;
    private List<No> pista;
    private List<Piloto> pilotos;
    private Circuito circuito;

    @BeforeEach
    void setUp() {
        Global.FORCAR_ESCAPADA_TESTE = false;
        pista = new ArrayList<>();
        for (int i = 0; i < TAMANHO_PISTA; i++) {
            pista.add(criarNo(i, i, 100));
        }
        pilotos = new ArrayList<>();

        controleJogo = mock(InterfaceJogo.class);
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
        when(controleJogo.getRandom()).thenReturn(random);

        circuito = mock(Circuito.class);
        when(circuito.getIndiceTracado()).thenReturn(24.0);
        when(circuito.getIndiceTracadoForaPista()).thenReturn(84.0);
        when(circuito.getMultiplicadorLarguraPista()).thenReturn(1.0);
        when(circuito.getObjetos()).thenReturn(new ArrayList<>());
        when(controleJogo.getCircuito()).thenReturn(circuito);
    }

    @AfterEach
    void tearDown() {
        Global.FORCAR_ESCAPADA_TESTE = false;
    }

    private No criarNo(int index, int x, int y) {
        No no = new No();
        no.setIndex(index);
        no.setPoint(new Point(x, y));
        return no;
    }

    private Piloto criarPiloto(int index, int tracado) {
        Piloto piloto = new Piloto();
        piloto.setNome("Piloto");
        Carro carro = new Carro();
        carro.setPiloto(piloto);
        // Pneus "novos" por padrão (Carro.porcentagemDesgastePneus é 0 se nunca setado, o que
        // pareceria pneu criticamente gasto pra regra de pneus<30% — testes que querem exercitar
        // essa regra setam explicitamente um valor baixo).
        carro.setPorcentagemDesgastePneus(100);
        piloto.setCarro(carro);
        piloto.setControleJogo(controleJogo);
        piloto.setNoAtual(pista.get(index));
        piloto.setTracado(tracado);
        pilotos.add(piloto);
        return piloto;
    }

    private ObjetoEscapada criarEscapada(int tracadoOrigem, int indiceEntrada, int indiceSaida) {
        ObjetoEscapada escapada = new ObjetoEscapada();
        escapada.setTracadoOrigem(tracadoOrigem);
        escapada.setIndiceEntrada(indiceEntrada);
        escapada.setIndiceSaida(indiceSaida);
        return escapada;
    }

    private void registrarEscapada(ObjetoEscapada escapada) {
        List<ObjetoPista> objetos = new ArrayList<>();
        objetos.add(escapada);
        when(circuito.getObjetos()).thenReturn(objetos);
    }

    // ---- Teste 1: agressividade + stress ----

    @Test
    void teste1_agressivoEEstressado_dentroDaJanela_marcaQuandoFalha() {
        registrarEscapada(criarEscapada(1, 300, 360));
        Piloto piloto = criarPiloto(300, 1);
        piloto.setModoPilotagem(Piloto.AGRESSIVO);
        piloto.setStress(95);
        // habilidade padrão (0): testeHabilidadePiloto() falha sempre.

        piloto.processaEscapadaDaPista();

        assertEquals(5, piloto.getTracado(), "agressivo+estressado que falha no teste 1 deveria escapar ao alcançar a entrada");
    }

    @Test
    void teste1_precondicaoNaoSatisfeita_naoConsomeRNG_naoMarca() {
        registrarEscapada(criarEscapada(1, 300, 360));
        Piloto piloto = criarPiloto(300, 1);
        piloto.setModoPilotagem(Piloto.NORMAL);
        piloto.setStress(95);

        piloto.processaEscapadaDaPista();

        assertEquals(1, piloto.getTracado(), "sem AGRESSIVO, o teste 1 não deveria marcar mesmo com stress alto");
        verify(controleJogo.getRandom(), never()).nextDouble();
    }

    @Test
    void teste1_stressAbaixoDoLimite_naoConsomeRNG_naoMarca() {
        registrarEscapada(criarEscapada(1, 300, 360));
        Piloto piloto = criarPiloto(300, 1);
        piloto.setModoPilotagem(Piloto.AGRESSIVO);
        piloto.setStress(50);

        piloto.processaEscapadaDaPista();

        assertEquals(1, piloto.getTracado(), "AGRESSIVO com stress dentro do limite não deveria marcar pelo teste 1");
        verify(controleJogo.getRandom(), never()).nextDouble();
    }

    @Test
    void teste1_sucesso_evitaAMarcaEViraLento() {
        registrarEscapada(criarEscapada(1, 300, 360));
        Piloto piloto = criarPiloto(300, 1);
        piloto.setModoPilotagem(Piloto.AGRESSIVO);
        piloto.setStress(95);
        piloto.setHabilidade(1000);
        when(controleJogo.getRandom().nextDouble()).thenReturn(0.0);

        piloto.processaEscapadaDaPista();

        assertEquals(1, piloto.getTracado(), "sucesso no teste 1 não deveria escapar");
        assertEquals(Piloto.LENTO, piloto.getModoPilotagem(), "sucesso no teste 1 deveria virar LENTO (recompensa por quase escapar)");
    }

    // ---- Teste 2: pneus (só avaliado se o teste 1 não marcou) ----

    @Test
    void teste2_pneusBaixos_marcaQuandoFalha_tracadoOrigem1() {
        registrarEscapada(criarEscapada(1, 300, 360));
        Piloto piloto = criarPiloto(300, 1);
        piloto.setModoPilotagem(Piloto.NORMAL);
        piloto.setStress(0);
        piloto.getCarro().setPorcentagemDesgastePneus(15);
        // habilidade padrão (0): teste de habilidade falha sempre.

        piloto.processaEscapadaDaPista();

        assertEquals(5, piloto.getTracado(),
                "piloto com pneus <30% que falha no teste 2, ao alcançar a entrada ainda no traçado 1, deveria "
                        + "forçar a escapada para o traçado 5 (mudarTracado só permite voltar de 5 pra 1)");
    }

    @Test
    void teste2_pneusBaixos_marcaQuandoFalha_tracadoOrigem2() {
        registrarEscapada(criarEscapada(2, 300, 360));
        Piloto piloto = criarPiloto(300, 2);
        piloto.setModoPilotagem(Piloto.NORMAL);
        piloto.setStress(0);
        piloto.getCarro().setPorcentagemDesgastePneus(15);

        piloto.processaEscapadaDaPista();

        assertEquals(4, piloto.getTracado(),
                "piloto com pneus <30% que falha no teste 2, ao alcançar a entrada ainda no traçado 2, deveria "
                        + "forçar a escapada para o traçado 4 (mudarTracado só permite voltar de 4 pra 2)");
    }

    @Test
    void teste2_pneusNoLimiteDe30PorCento_naoMarca() {
        registrarEscapada(criarEscapada(1, 300, 360));
        Piloto piloto = criarPiloto(300, 1);
        piloto.setModoPilotagem(Piloto.NORMAL);
        piloto.setStress(0);
        piloto.getCarro().setPorcentagemDesgastePneus(30);

        piloto.processaEscapadaDaPista();

        assertEquals(1, piloto.getTracado(), "pneus exatamente em 30% não deveriam satisfazer a pré-condição (exige < 30)");
    }

    @Test
    void teste2_precondicaoNaoSatisfeita_naoConsomeRNG() {
        registrarEscapada(criarEscapada(1, 300, 360));
        Piloto piloto = criarPiloto(300, 1);
        piloto.setModoPilotagem(Piloto.NORMAL);
        piloto.setStress(0);
        piloto.getCarro().setPorcentagemDesgastePneus(100);

        piloto.processaEscapadaDaPista();

        assertEquals(1, piloto.getTracado());
        verify(controleJogo.getRandom(), never()).nextDouble();
    }

    @Test
    void teste2_naoAvaliado_quandoTeste1JaMarcou() {
        // Se o teste 1 já marcar (falha), o teste 2 não deveria nem ser consultado — um único
        // nextDouble() consumido (o do teste 1), mesmo com pneus também abaixo de 30%. Piloto
        // ainda longe da entrada, pra não confundir com o RNG que mudarTracado() consome por
        // conta própria (cálculo de cooldown) quando a escapada é de fato executada.
        registrarEscapada(criarEscapada(1, 300, 400));
        Piloto piloto = criarPiloto(260, 1);
        piloto.setModoPilotagem(Piloto.AGRESSIVO);
        piloto.setStress(95);
        piloto.getCarro().setPorcentagemDesgastePneus(15);

        piloto.processaEscapadaDaPista();

        assertEquals(1, piloto.getTracado(), "ainda longe da entrada, só marcado");
        verify(controleJogo.getRandom(), org.mockito.Mockito.times(1)).nextDouble();
    }

    @Test
    void teste2_avaliadoNoMesmoCiclo_quandoTeste1NaoSeAplica() {
        // stress alto mas não AGRESSIVO: pré-condição do teste 1 é falsa (sem RNG), então o
        // teste 2 (pneus) é avaliado no mesmo ciclo.
        registrarEscapada(criarEscapada(1, 300, 360));
        Piloto piloto = criarPiloto(300, 1);
        piloto.setModoPilotagem(Piloto.NORMAL);
        piloto.setStress(95);
        piloto.getCarro().setPorcentagemDesgastePneus(15);

        piloto.processaEscapadaDaPista();

        assertEquals(5, piloto.getTracado(), "teste 2 deveria ter marcado o piloto no mesmo ciclo");
    }

    @Test
    void teste2_pilotoJaEmLento_nuncaMarcaPorEssaCausa() {
        // "LENTO nunca escapa": um piloto já LENTO não satisfaz a pré-condição do teste 2
        // (exige modoPilotagem != LENTO), então nunca é marcado por essa causa.
        registrarEscapada(criarEscapada(1, 300, 360));
        Piloto piloto = criarPiloto(300, 1);
        piloto.setModoPilotagem(Piloto.LENTO);
        piloto.setStress(0);
        piloto.getCarro().setPorcentagemDesgastePneus(15);

        piloto.processaEscapadaDaPista();

        assertEquals(1, piloto.getTracado(), "piloto já em LENTO nunca deveria escapar, mesmo com pneus baixos");
        verify(controleJogo.getRandom(), never()).nextDouble();
    }

    @Test
    void teste2_sucesso_evitaAMarcaEViraLento() {
        registrarEscapada(criarEscapada(1, 300, 360));
        Piloto piloto = criarPiloto(300, 1);
        piloto.setModoPilotagem(Piloto.NORMAL);
        piloto.setStress(0);
        piloto.getCarro().setPorcentagemDesgastePneus(15);
        piloto.setHabilidade(1000);
        when(controleJogo.getRandom().nextDouble()).thenReturn(0.0);

        piloto.processaEscapadaDaPista();

        assertEquals(1, piloto.getTracado());
        assertEquals(Piloto.LENTO, piloto.getModoPilotagem(), "sucesso no teste 2 deveria virar LENTO");
    }

    @Test
    void nenhumaPrecondicaoSatisfeita_naoTesta_naoEscapa() {
        registrarEscapada(criarEscapada(1, 300, 360));
        Piloto piloto = criarPiloto(300, 1);
        piloto.setModoPilotagem(Piloto.NORMAL);
        piloto.setStress(0);
        // pneus "novos" (100%) do criarPiloto — nenhuma das duas pré-condições se aplica.

        piloto.processaEscapadaDaPista();

        assertEquals(1, piloto.getTracado(), "piloto sem nenhuma pré-condição de risco não deveria escapar");
        verify(controleJogo.getRandom(), never()).nextDouble();
    }

    // ---- Cache: um único par de testes por zona por piloto por volta, qualquer que seja o desfecho ----

    @Test
    void marcado_naoETestadoDeNovo_aoAlcancarAEntrada() {
        // Marcado longe da entrada (dentro da janela); ao alcançar a entrada, nenhum novo
        // nextDouble() deveria ser consumido — só o do teste original.
        registrarEscapada(criarEscapada(1, 300, 400));
        Piloto piloto = criarPiloto(260, 1);
        piloto.setModoPilotagem(Piloto.AGRESSIVO);
        piloto.setStress(95);

        piloto.processaEscapadaDaPista();
        assertEquals(1, piloto.getTracado(), "ainda longe da entrada, só marcado, não escapa ainda");
        verify(controleJogo.getRandom(), org.mockito.Mockito.times(1)).nextDouble();

        piloto.setNoAtual(pista.get(300));
        piloto.processaEscapadaDaPista();

        assertEquals(5, piloto.getTracado(),
                "ao alcançar a entrada, força a escapada sem reteste (mudarTracado() usa RNG por conta própria pro cálculo de cooldown, então a contagem de nextDouble() não é mais verificável aqui)");
    }

    @Test
    void naoMarcado_naoETestadoDeNovo_maisPertoDaEntrada_mesmoContinuandoEmRisco() {
        // O cenário central discutido: um piloto que passa nos dois testes ao entrar na janela
        // (150 índices) não deveria ser retestado mais perto da entrada, mesmo continuando
        // agressivo+estressado o tempo todo.
        registrarEscapada(criarEscapada(1, 300, 450));
        Piloto piloto = criarPiloto(150, 1);
        piloto.setModoPilotagem(Piloto.AGRESSIVO);
        piloto.setStress(95);
        piloto.setHabilidade(1000);
        when(controleJogo.getRandom().nextDouble()).thenReturn(0.0);

        piloto.processaEscapadaDaPista();
        assertEquals(Piloto.LENTO, piloto.getModoPilotagem(), "sucesso no teste 1 deveria já ter virado LENTO");
        verify(controleJogo.getRandom(), org.mockito.Mockito.times(1)).nextDouble();

        // Volta a ficar "em risco" (AGRESSIVO), mais perto da entrada — não deveria ser
        // retestado, já que essa zona já tem resultado (não marcado) em cache nesta volta.
        piloto.setModoPilotagem(Piloto.AGRESSIVO);
        piloto.setNoAtual(pista.get(290));
        piloto.processaEscapadaDaPista();

        piloto.setNoAtual(pista.get(300));
        piloto.processaEscapadaDaPista();

        assertEquals(1, piloto.getTracado(), "não deveria escapar — o resultado 'não marcado' já estava em cache");
        verify(controleJogo.getRandom(), org.mockito.Mockito.times(1)).nextDouble();
    }

    @Test
    void testePreventivo_resetaAoMudarDeVolta_permitindoNovoTeste() {
        registrarEscapada(criarEscapada(1, 300, 360));
        Piloto piloto = criarPiloto(260, 1);
        piloto.setModoPilotagem(Piloto.AGRESSIVO);
        piloto.setStress(95);
        // habilidade padrão (0): primeiro teste (na volta 0) falha (marca) sempre.

        piloto.processaEscapadaDaPista();
        assertEquals(Piloto.AGRESSIVO, piloto.getModoPilotagem(), "marcado, não deveria ter virado LENTO");

        piloto.setNumeroVolta(1);
        piloto.setModoPilotagem(Piloto.NORMAL);
        piloto.setStress(95);
        piloto.getCarro().setPorcentagemDesgastePneus(15);
        piloto.setHabilidade(1000);
        when(controleJogo.getRandom().nextDouble()).thenReturn(0.0);

        piloto.processaEscapadaDaPista();

        assertEquals(Piloto.LENTO, piloto.getModoPilotagem(),
                "na volta seguinte, o cache deveria ter sido limpo, permitindo um novo teste (agora pelo teste 2, que passa)");
    }

    // ---- Execução independe de modoPilotagem no momento (marcado ignora mudança de modo depois) ----

    @Test
    void marcado_mudarParaLentoDepoisDeMarcado_naoEvitaMaisAEscapada() {
        registrarEscapada(criarEscapada(1, 300, 360));
        Piloto piloto = criarPiloto(260, 1);
        piloto.setModoPilotagem(Piloto.AGRESSIVO);
        piloto.setStress(95);

        piloto.processaEscapadaDaPista();
        assertEquals(1, piloto.getTracado(), "ainda longe da entrada, só marcado");

        // Piloto (ou outra lógica) muda pra LENTO depois de já marcado.
        piloto.setModoPilotagem(Piloto.LENTO);
        piloto.setNoAtual(pista.get(300));
        piloto.processaEscapadaDaPista();

        assertEquals(5, piloto.getTracado(), "mudar pra LENTO depois de marcado não deveria mais evitar a escapada");
    }

    @Test
    void pilotoEmLento_antesDeSerMarcado_nuncaEscapa() {
        registrarEscapada(criarEscapada(1, 300, 360));
        Piloto piloto = criarPiloto(300, 1);
        piloto.setModoPilotagem(Piloto.LENTO);
        piloto.setStress(95);

        piloto.processaEscapadaDaPista();

        assertEquals(1, piloto.getTracado(), "piloto já em LENTO antes de qualquer teste nunca deveria escapar");
    }

    // ---- Trava de mudança de traçado enquanto marcado ----

    @Test
    void marcado_mudarTracadoPorOutraVia_eRejeitado() {
        registrarEscapada(criarEscapada(1, 300, 400));
        Piloto piloto = criarPiloto(260, 1);
        piloto.setModoPilotagem(Piloto.AGRESSIVO);
        piloto.setStress(95);

        piloto.processaEscapadaDaPista();
        assertEquals(1, piloto.getTracado(), "marcado, ainda longe da entrada");

        boolean mudou = piloto.mudarTracado(0);

        assertFalse(mudou, "mudarTracado deveria ser rejeitado enquanto o piloto estiver marcado pra escapar");
        assertEquals(1, piloto.getTracado(), "traçado não deveria ter mudado");
    }

    @Test
    void marcado_travaELiberadaAoCumprirAEscapada() {
        registrarEscapada(criarEscapada(1, 300, 360));
        Piloto piloto = criarPiloto(300, 1);
        piloto.setModoPilotagem(Piloto.AGRESSIVO);
        piloto.setStress(95);

        piloto.processaEscapadaDaPista();
        assertEquals(5, piloto.getTracado(), "deveria ter escapado");

        // Simula o traçado já ter sido efetivado (fora da animação); tentativa normal de
        // mudança de traçado deveria funcionar de novo. Traçado 5 só pode voltar pro 1
        // (mudarTracado bloqueia 5 -> {0,2}), então o alvo de teste é 1, não 2.
        piloto.setIndiceTracado(0);
        boolean mudou = piloto.mudarTracado(1, true);

        assertTrue(mudou, "trava deveria ter sido liberada assim que a escapada foi cumprida (traçado 4/5)");
    }

    // ---- Janela de detecção: 150 índices ----

    @Test
    void janela_exatosCentoECinquentaIndices_jaRecebeOTeste() {
        registrarEscapada(criarEscapada(1, 450, 500));
        Piloto piloto = criarPiloto(300, 1);
        piloto.setModoPilotagem(Piloto.AGRESSIVO);
        piloto.setStress(95);

        piloto.processaEscapadaDaPista();

        verify(controleJogo.getRandom(), org.mockito.Mockito.times(1)).nextDouble();
    }

    @Test
    void janela_centoECinquentaEUmIndices_aindaNaoRecebeOTeste() {
        registrarEscapada(criarEscapada(1, 451, 500));
        Piloto piloto = criarPiloto(300, 1);
        piloto.setModoPilotagem(Piloto.AGRESSIVO);
        piloto.setStress(95);

        piloto.processaEscapadaDaPista();

        verify(controleJogo.getRandom(), never()).nextDouble();
    }

    // ---- FORCAR_ESCAPADA_TESTE ----

    @Test
    void flagGlobalDeTeste_comprometePilotoNormalNaoEstressado() {
        Global.FORCAR_ESCAPADA_TESTE = true;
        registrarEscapada(criarEscapada(1, 300, 360));
        Piloto piloto = criarPiloto(300, 1);
        piloto.setModoPilotagem(Piloto.NORMAL);
        piloto.setStress(0);

        piloto.processaEscapadaDaPista();

        assertEquals(5, piloto.getTracado(), "com a flag ativa, mesmo um piloto normal/não estressado deveria escapar, sem nenhum teste");
    }

    @Test
    void flagGlobalDeTeste_forcaEscapadaMesmoComPilotoEmLento() {
        Global.FORCAR_ESCAPADA_TESTE = true;
        registrarEscapada(criarEscapada(1, 300, 360));
        Piloto piloto = criarPiloto(300, 1);
        piloto.setModoPilotagem(Piloto.LENTO);
        piloto.setStress(0);

        piloto.processaEscapadaDaPista();

        assertEquals(5, piloto.getTracado(), "com a flag ativa, mesmo um piloto em modo LENTO deveria escapar ao alcançar a entrada");
    }

    @Test
    void flagGlobalDeTeste_naoDispensaExigenciaDePosicaoNoTracado0() {
        Global.FORCAR_ESCAPADA_TESTE = true;
        registrarEscapada(criarEscapada(1, 300, 360));
        Piloto piloto = criarPiloto(260, 0);
        piloto.setModoPilotagem(Piloto.NORMAL);
        piloto.setStress(0);

        piloto.processaEscapadaDaPista();

        assertEquals(0, piloto.getTracado(), "no traçado 0 não há zona de escapada relevante, mesmo com a flag de teste ativa");
    }

    @Test
    void flagGlobalDeTeste_foraDaNovaJanelaDe150_naoEscapaAinda() {
        Global.FORCAR_ESCAPADA_TESTE = true;
        registrarEscapada(criarEscapada(1, 500, 600));
        Piloto piloto = criarPiloto(300, 1);
        piloto.setModoPilotagem(Piloto.NORMAL);
        piloto.setStress(0);

        piloto.processaEscapadaDaPista();

        assertEquals(1, piloto.getTracado(), "a 200 índices (>150) da entrada, a flag ainda não deveria comprometer o piloto");
    }

    // ---- Jogador humano em modo manual ----

    @Test
    void jogadorHumanoEmModoManual_naoRecebeTesteDeHabilidade_escapaNormalmente() {
        registrarEscapada(criarEscapada(1, 300, 500));
        when(controleJogo.getAutomaticoManual()).thenReturn(Global.CONTROLE_MANUAL);
        Piloto piloto = criarPiloto(300, 1);
        piloto.setJogadorHumano(true);
        piloto.setModoPilotagem(Piloto.AGRESSIVO);
        piloto.setStress(95);
        // habilidade máxima + nextDouble()=0.0 passaria no teste se ele fosse aplicado.
        piloto.setHabilidade(1000);
        when(controleJogo.getRandom().nextDouble()).thenReturn(0.0);

        piloto.processaEscapadaDaPista();

        assertEquals(5, piloto.getTracado(),
                "jogador humano em modo manual não recebe o teste de habilidade automático — escapa normalmente, mesmo com habilidade máxima");
        assertEquals(Piloto.AGRESSIVO, piloto.getModoPilotagem(), "não deveria ter sido mudado pra LENTO automaticamente");
    }

    @Test
    void jogadorHumanoEmModoManual_seJaEstiverEmLento_naoEscapa() {
        registrarEscapada(criarEscapada(1, 300, 360));
        when(controleJogo.getAutomaticoManual()).thenReturn(Global.CONTROLE_MANUAL);
        Piloto piloto = criarPiloto(300, 1);
        piloto.setJogadorHumano(true);
        piloto.setModoPilotagem(Piloto.LENTO);
        piloto.setStress(95);

        piloto.processaEscapadaDaPista();

        assertEquals(1, piloto.getTracado(),
                "se o próprio jogador já colocou o carro em modo LENTO, ele continua não escapando — a tarefa era dele, e ele cumpriu");
    }

    @Test
    void jogadorHumanoEmModoAutomatico_continuaRecebendoOTesteDeHabilidade() {
        registrarEscapada(criarEscapada(1, 300, 360));
        when(controleJogo.getAutomaticoManual()).thenReturn(Global.CONTROLE_AUTOMATICO);
        Piloto piloto = criarPiloto(300, 1);
        piloto.setJogadorHumano(true);
        piloto.setModoPilotagem(Piloto.AGRESSIVO);
        piloto.setStress(95);
        piloto.setHabilidade(1000);
        when(controleJogo.getRandom().nextDouble()).thenReturn(0.0);

        piloto.processaEscapadaDaPista();

        assertEquals(1, piloto.getTracado(),
                "jogador humano em modo AUTOMATICO (não manual) continua recebendo o teste de habilidade, como um piloto de IA");
        assertEquals(Piloto.LENTO, piloto.getModoPilotagem());
    }

    // ---- Entrada já passada (tolerância de salto de ciclo) e zona alcançável ----

    @Test
    void carroDentroDaZonaMuitoAlemDaEntrada_naoForcaEscapada_soZonasAindaAlcancaveis() {
        registrarEscapada(criarEscapada(1, 200, 700));
        Piloto piloto = criarPiloto(550, 1);
        piloto.setModoPilotagem(Piloto.AGRESSIVO);
        piloto.setStress(95);

        piloto.processaEscapadaDaPista();

        assertEquals(1, piloto.getTracado(), "350 índices além da entrada é longe demais (além da tolerância de 150) — essa zona já foi perdida");
    }

    @Test
    void carroComSaltoGrandeDeGanho_140IndicesAlemDaEntrada_aindaForcaEscapada() {
        registrarEscapada(criarEscapada(1, 200, 700));
        Piloto piloto = criarPiloto(340, 1);
        piloto.setModoPilotagem(Piloto.AGRESSIVO);
        piloto.setStress(95);

        piloto.processaEscapadaDaPista();

        assertEquals(5, piloto.getTracado(),
                "140 índices além da entrada (dentro da tolerância de 150) ainda deveria forçar a escapada");
    }

    @Test
    void semEscapadaNoTracadoAtual_naoFazNada() {
        registrarEscapada(criarEscapada(2, 300, 360));
        Piloto piloto = criarPiloto(280, 1);
        piloto.setModoPilotagem(Piloto.AGRESSIVO);
        piloto.setStress(95);

        piloto.processaEscapadaDaPista();

        assertEquals(1, piloto.getTracado(), "só há escapada no traçado 2; piloto no traçado 1 não deveria ser afetado");
    }

    // ---- Retorno da escapada (traçado de fuga 4/5 → origem), inalterado por esta mudança ----

    @Test
    void saidaDaEscapada_dentroDe100IndicesDoFim_comHabilidade_voltaParaOTracadoDeOrigem1() {
        registrarEscapada(criarEscapada(1, 200, 360));
        Piloto piloto = criarPiloto(280, 5);
        piloto.setHabilidade(1000);
        when(controleJogo.getRandom().nextDouble()).thenReturn(0.0);

        piloto.processaEscapadaDaPista();

        assertEquals(1, piloto.getTracado(),
                "a 80 índices do fim (indiceSaida=360), com o teste de habilidade bem-sucedido, deveria voltar pro traçado de origem (1), nunca pro 2");
    }

    @Test
    void saidaDaEscapada_dentroDe100IndicesDoFim_comHabilidade_voltaParaOTracadoDeOrigem2() {
        registrarEscapada(criarEscapada(2, 200, 360));
        Piloto piloto = criarPiloto(280, 4);
        piloto.setHabilidade(1000);
        when(controleJogo.getRandom().nextDouble()).thenReturn(0.0);

        piloto.processaEscapadaDaPista();

        assertEquals(2, piloto.getTracado(),
                "a 80 índices do fim (indiceSaida=360), com o teste de habilidade bem-sucedido, deveria voltar pro traçado de origem (2), nunca pro 1");
    }

    @Test
    void saidaDaEscapada_semSucessoNoTesteDeHabilidade_continuaNoTracadoDeFuga() {
        registrarEscapada(criarEscapada(1, 200, 360));
        Piloto piloto = criarPiloto(280, 5);

        piloto.processaEscapadaDaPista();

        assertEquals(5, piloto.getTracado(), "sem sucesso no teste de habilidade, deveria continuar no traçado de fuga");
    }

    @Test
    void saidaDaEscapada_foraDaJanelaDe100IndicesDoFim_naoTentaVoltarMesmoComHabilidade() {
        registrarEscapada(criarEscapada(1, 200, 500));
        Piloto piloto = criarPiloto(280, 5);
        piloto.setHabilidade(1000);
        when(controleJogo.getRandom().nextDouble()).thenReturn(0.0);

        piloto.processaEscapadaDaPista();

        assertEquals(5, piloto.getTracado(),
                "a 220 índices do fim (indiceSaida=500, >100), não deveria nem tentar voltar, mesmo com habilidade máxima");
    }

    // ---- Redução de velocidade/modo/giro no traçado de fuga ----

    @Test
    void velocidadeEModoReduzidosDuranteATracado4() {
        Piloto piloto = criarPiloto(320, 4);
        piloto.setModoPilotagem(Piloto.AGRESSIVO);
        piloto.getCarro().setGiro(Carro.GIRO_MAX_VAL);
        piloto.setGanho(100);

        piloto.processaEscapadaDaPista();

        assertEquals(60, piloto.getGanho(), "ganho deveria ser reduzido a 0.6x durante a escapada");
        assertEquals(Carro.GIRO_MIN_VAL, piloto.getCarro().getGiro(), "giro deveria ser travado no mínimo durante a escapada");
        assertEquals(Piloto.LENTO, piloto.getModoPilotagem(), "modo deveria ser travado em LENTO durante a escapada");
    }

    @Test
    void aoVoltarDoTracadoDeFugaParaOTracadoDeOrigem_modoEGiroSaoRestauradosAoNormal() {
        Piloto piloto = criarPiloto(320, 5);
        piloto.setModoPilotagem(Piloto.AGRESSIVO);
        piloto.getCarro().setGiro(Carro.GIRO_MAX_VAL);

        piloto.processaEscapadaDaPista();
        assertEquals(Piloto.LENTO, piloto.getModoPilotagem(), "deveria estar em LENTO enquanto no traçado de fuga");
        assertEquals(Carro.GIRO_MIN_VAL, piloto.getCarro().getGiro());

        // Simula o retorno já ter acontecido (processaSaidaDaEscapada mudou o traçado).
        piloto.setTracado(2);
        piloto.processaEscapadaDaPista();

        assertEquals(Piloto.AGRESSIVO, piloto.getModoPilotagem(), "modo deveria voltar ao que era antes de escapar, não ficar travado em LENTO");
        assertEquals(Carro.GIRO_MAX_VAL, piloto.getCarro().getGiro(), "giro deveria voltar ao que era antes de escapar, não ficar travado no mínimo");
    }

    // ---- Volta 1 usa exatamente a mesma regra das demais voltas ----

    @Test
    void volta1_mesmaRegraDeQualquerOutraVolta_escapaNormalmente() {
        when(controleJogo.getNumVoltaAtual()).thenReturn(1);
        registrarEscapada(criarEscapada(1, 300, 360));
        Piloto piloto = criarPiloto(300, 1);
        piloto.setModoPilotagem(Piloto.AGRESSIVO);
        piloto.setStress(95);

        piloto.processaEscapadaDaPista();

        assertEquals(5, piloto.getTracado(),
                "não deveria haver diferença de regra entre a volta 1 e as demais — agressivo+estressado na entrada escapa igual em qualquer volta");
    }
}
