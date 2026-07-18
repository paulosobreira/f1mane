package br.flmane.entidades;

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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import br.flmane.controles.ControleEscapada;
import br.flmane.controles.InterfaceJogo;
import br.nnpe.Global;

/**
 * Cobre a escapada ancorada ao traçado (traçado 1/2 → 4/5 via
 * {@link ObjetoEscapada}): dois testes sequenciais por causa de risco
 * (stress, depois pneus), nenhum dos dois exige {@code AGRESSIVO} nem exclui
 * {@code LENTO} (sem exceção pro jogador humano em modo manual), um único par
 * por zona por piloto por volta (mesmo resultado marcado ou não), execução
 * independente de {@code modoPilotagem} no momento, sem recompensa de
 * {@code LENTO} em caso de sucesso, e a trava de mudança de traçado enquanto
 * marcado. A derrapagem (traçado 0 → 1/2) tem seus próprios testes em
 * {@code PilotoDerrapagemTest}.
 * <p>
 * Nos testes de "sucesso" (skill test passa, não marca), o carro precisa de
 * {@code potencia}/{@code freios} explicitamente altos além de
 * {@code habilidade}: {@link br.flmane.entidades.Piloto#testeHabilidadePilotoCarro()}
 * e {@link br.flmane.entidades.Piloto#testeHabilidadePilotoFreios()} também
 * dependem de {@code Carro.testePotencia()}/{@code Carro.testeFreios()}, que
 * ficam em 0 por padrão em {@link #criarPiloto(int, int)} — sem isso, o teste
 * de habilidade falha sempre, mesmo com {@code habilidade=1000}.
 */
class PilotoEscapadaAncoradaTracadoTest {

    private static final int TAMANHO_PISTA = 1000;

    private InterfaceJogo controleJogo;
    private ControleEscapada controleEscapada;
    private List<No> pista;
    private List<Piloto> pilotos;
    private Circuito circuito;

    @BeforeEach
    void setUp() {
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

        controleEscapada = new ControleEscapada(controleJogo);
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
        // Potência/freios altos por padrão — sem isso, testeHabilidadePilotoCarro()/Freios() falham
        // sempre (Carro.testePotencia()/testeFreios() ficam em 0 por padrão), mascarando cenários de
        // "sucesso no teste de habilidade". Testes que querem exercitar o caminho de falha zeram
        // explicitamente.
        carro.setPotencia(1000);
        carro.setFreios(1000);
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

    // ---- Teste 1: stress (não exige AGRESSIVO, não exclui LENTO) ----

    @Test
    void teste1_agressivoEEstressado_dentroDaJanela_marcaQuandoFalha() {
        registrarEscapada(criarEscapada(1, 300, 360));
        Piloto piloto = criarPiloto(300, 1);
        piloto.setModoPilotagem(Piloto.AGRESSIVO);
        piloto.setStress(95);
        // habilidade padrão (0): testeHabilidadePiloto() falha sempre.

        controleEscapada.processaEscapadaDaPista(piloto);

        assertEquals(5, piloto.getTracado(), "agressivo+estressado que falha no teste 1 deveria escapar ao alcançar a entrada");
    }

    @Test
    void teste1_naoExigeAgressivo_marcaComQualquerModoSeStressAlto() {
        registrarEscapada(criarEscapada(1, 300, 360));
        Piloto piloto = criarPiloto(300, 1);
        piloto.setModoPilotagem(Piloto.NORMAL);
        piloto.setStress(95);
        // habilidade/potência padrão (0): testeHabilidadePilotoCarro() falha sempre.

        controleEscapada.processaEscapadaDaPista(piloto);

        assertEquals(5, piloto.getTracado(), "teste 1 não exige mais AGRESSIVO: stress acima do limite já basta pra marcar, em qualquer modo");
    }

    @Test
    void teste1_stressAbaixoDoLimite_naoConsomeRNG_naoMarca() {
        registrarEscapada(criarEscapada(1, 300, 360));
        Piloto piloto = criarPiloto(300, 1);
        piloto.setModoPilotagem(Piloto.AGRESSIVO);
        piloto.setStress(50);

        controleEscapada.processaEscapadaDaPista(piloto);

        assertEquals(1, piloto.getTracado(), "AGRESSIVO com stress dentro do limite não deveria marcar pelo teste 1");
        verify(controleJogo.getRandom(), never()).nextDouble();
    }

    @Test
    void teste1_sucesso_evitaAMarca_semRecompensaDeLento() {
        registrarEscapada(criarEscapada(1, 300, 360));
        Piloto piloto = criarPiloto(300, 1);
        piloto.setModoPilotagem(Piloto.AGRESSIVO);
        piloto.setStress(95);
        piloto.setHabilidade(1000);
        piloto.getCarro().setPotencia(1000);
        when(controleJogo.getRandom().nextDouble()).thenReturn(0.0);

        controleEscapada.processaEscapadaDaPista(piloto);

        assertEquals(1, piloto.getTracado(), "sucesso no teste 1 não deveria escapar");
        assertEquals(Piloto.AGRESSIVO, piloto.getModoPilotagem(), "sucesso no teste 1 não dá mais recompensa de LENTO (removida no tuning)");
    }

    // ---- Teste 2: pneus (só avaliado se o teste 1 não marcou) ----

    @Test
    void teste2_pneusBaixos_marcaQuandoFalha_tracadoOrigem1() {
        registrarEscapada(criarEscapada(1, 300, 360));
        Piloto piloto = criarPiloto(300, 1);
        piloto.setModoPilotagem(Piloto.NORMAL);
        piloto.setStress(70);
        piloto.getCarro().setPorcentagemDesgastePneus(15);
        // habilidade padrão (0): teste de habilidade falha sempre.

        controleEscapada.processaEscapadaDaPista(piloto);

        assertEquals(5, piloto.getTracado(),
                "piloto com pneus <30% que falha no teste 2, ao alcançar a entrada ainda no traçado 1, deveria "
                        + "forçar a escapada para o traçado 5 (mudarTracado só permite voltar de 5 pra 1)");
    }

    @Test
    void teste2_pneusBaixos_marcaQuandoFalha_tracadoOrigem2() {
        registrarEscapada(criarEscapada(2, 300, 360));
        Piloto piloto = criarPiloto(300, 2);
        piloto.setModoPilotagem(Piloto.NORMAL);
        piloto.setStress(70);
        piloto.getCarro().setPorcentagemDesgastePneus(15);

        controleEscapada.processaEscapadaDaPista(piloto);

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

        controleEscapada.processaEscapadaDaPista(piloto);

        assertEquals(1, piloto.getTracado(), "pneus exatamente em 30% não deveriam satisfazer a pré-condição (exige < 30)");
    }

    @Test
    void teste2_precondicaoNaoSatisfeita_naoConsomeRNG() {
        registrarEscapada(criarEscapada(1, 300, 360));
        Piloto piloto = criarPiloto(300, 1);
        piloto.setModoPilotagem(Piloto.NORMAL);
        piloto.setStress(0);
        piloto.getCarro().setPorcentagemDesgastePneus(100);

        controleEscapada.processaEscapadaDaPista(piloto);

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

        controleEscapada.processaEscapadaDaPista(piloto);

        assertEquals(1, piloto.getTracado(), "ainda longe da entrada, só marcado");
        // 2 chamadas: testePotencia() (sucede, potência alta) + testeHabilidadePiloto() (falha, habilidade padrão 0).
        verify(controleJogo.getRandom(), org.mockito.Mockito.times(2)).nextDouble();
    }

    @Test
    void teste2_avaliadoNoMesmoCiclo_quandoTeste1NaoSeAplica() {
        // stress dentro da janela do teste 2 (>=70) mas abaixo do limite do teste 1 (>90):
        // pré-condição do teste 1 é falsa (sem RNG), então o teste 2 (pneus) é avaliado no
        // mesmo ciclo.
        registrarEscapada(criarEscapada(1, 300, 360));
        Piloto piloto = criarPiloto(300, 1);
        piloto.setModoPilotagem(Piloto.NORMAL);
        piloto.setStress(70);
        piloto.getCarro().setPorcentagemDesgastePneus(15);

        controleEscapada.processaEscapadaDaPista(piloto);

        assertEquals(5, piloto.getTracado(), "teste 2 deveria ter marcado o piloto no mesmo ciclo");
    }

    @Test
    void teste2_pilotoJaEmLento_naoExcluiMaisDoTeste2_marcaSeFalhar() {
        // LENTO não é mais excluído do teste 2 (removido no tuning): pneus baixos + stress
        // ainda marcam, mesmo já em LENTO.
        registrarEscapada(criarEscapada(1, 300, 360));
        Piloto piloto = criarPiloto(300, 1);
        piloto.setModoPilotagem(Piloto.LENTO);
        piloto.setStress(70);
        piloto.getCarro().setPorcentagemDesgastePneus(15);
        // habilidade/freios padrão (0): testeHabilidadePilotoFreios() falha sempre.

        controleEscapada.processaEscapadaDaPista(piloto);

        assertEquals(5, piloto.getTracado(), "teste 2 não exclui mais LENTO: pneus baixos + stress ainda marcam, mesmo já em LENTO");
    }

    @Test
    void teste2_sucesso_evitaAMarca_semRecompensaDeLento() {
        registrarEscapada(criarEscapada(1, 300, 360));
        Piloto piloto = criarPiloto(300, 1);
        piloto.setModoPilotagem(Piloto.NORMAL);
        piloto.setStress(70);
        piloto.getCarro().setPorcentagemDesgastePneus(15);
        piloto.setHabilidade(1000);
        piloto.getCarro().setFreios(1000);
        when(controleJogo.getRandom().nextDouble()).thenReturn(0.0);

        controleEscapada.processaEscapadaDaPista(piloto);

        assertEquals(1, piloto.getTracado());
        assertEquals(Piloto.NORMAL, piloto.getModoPilotagem(), "sucesso no teste 2 não dá mais recompensa de LENTO (removida no tuning)");
    }

    @Test
    void nenhumaPrecondicaoSatisfeita_naoTesta_naoEscapa() {
        registrarEscapada(criarEscapada(1, 300, 360));
        Piloto piloto = criarPiloto(300, 1);
        piloto.setModoPilotagem(Piloto.NORMAL);
        piloto.setStress(0);
        // pneus "novos" (100%) do criarPiloto — nenhuma das duas pré-condições se aplica.

        controleEscapada.processaEscapadaDaPista(piloto);

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

        controleEscapada.processaEscapadaDaPista(piloto);
        assertEquals(1, piloto.getTracado(), "ainda longe da entrada, só marcado, não escapa ainda");
        // 2 chamadas: testePotencia() (sucede, potência alta) + testeHabilidadePiloto() (falha, habilidade padrão 0).
        verify(controleJogo.getRandom(), org.mockito.Mockito.times(2)).nextDouble();

        piloto.setNoAtual(pista.get(300));
        controleEscapada.processaEscapadaDaPista(piloto);

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
        piloto.getCarro().setPotencia(1000);
        when(controleJogo.getRandom().nextDouble()).thenReturn(0.0);

        controleEscapada.processaEscapadaDaPista(piloto);
        assertEquals(1, piloto.getTracado(), "sucesso no teste 1: não deveria ter escapado ainda");
        assertEquals(Piloto.AGRESSIVO, piloto.getModoPilotagem(), "sucesso no teste 1 não dá mais recompensa de LENTO");
        verify(controleJogo.getRandom(), org.mockito.Mockito.times(2)).nextDouble();

        // Volta a ficar "em risco" (AGRESSIVO), mais perto da entrada — não deveria ser
        // retestado, já que essa zona já tem resultado (não marcado) em cache nesta volta.
        piloto.setModoPilotagem(Piloto.AGRESSIVO);
        piloto.setNoAtual(pista.get(290));
        controleEscapada.processaEscapadaDaPista(piloto);

        piloto.setNoAtual(pista.get(300));
        controleEscapada.processaEscapadaDaPista(piloto);

        assertEquals(1, piloto.getTracado(), "não deveria escapar — o resultado 'não marcado' já estava em cache");
        verify(controleJogo.getRandom(), org.mockito.Mockito.times(2)).nextDouble();
    }

    @Test
    void testePreventivo_resetaAoMudarDeVolta_permitindoNovoTeste() {
        registrarEscapada(criarEscapada(1, 300, 360));
        Piloto piloto = criarPiloto(260, 1);
        piloto.setModoPilotagem(Piloto.AGRESSIVO);
        piloto.setStress(95);
        // habilidade padrão (0): primeiro teste (na volta 0) marca sempre, com 2 chamadas de RNG
        // (testePotencia() sucede, potência alta do fixture; testeHabilidadePiloto() falha, habilidade padrão 0).

        controleEscapada.processaEscapadaDaPista(piloto);
        verify(controleJogo.getRandom(), org.mockito.Mockito.times(2)).nextDouble();

        // Nova volta: stress dentro da janela do teste 2 (>=70) mas abaixo da do teste 1 (>90),
        // pneus baixos, e agora com habilidade/freios altos pra passar no teste 2 (2 chamadas de
        // RNG a mais: testeFreios() + testeHabilidadePiloto()).
        piloto.setNumeroVolta(1);
        piloto.setModoPilotagem(Piloto.NORMAL);
        piloto.setStress(70);
        piloto.getCarro().setPorcentagemDesgastePneus(15);
        piloto.setHabilidade(1000);
        piloto.getCarro().setFreios(1000);
        when(controleJogo.getRandom().nextDouble()).thenReturn(0.0);

        controleEscapada.processaEscapadaDaPista(piloto);

        verify(controleJogo.getRandom(), org.mockito.Mockito.times(4)).nextDouble();
        assertEquals(Piloto.NORMAL, piloto.getModoPilotagem(),
                "na volta seguinte, o cache deveria ter sido limpo, permitindo um novo teste (agora pelo teste 2, que passa, sem recompensa de LENTO)");
    }

    // ---- Execução independe de modoPilotagem no momento (marcado ignora mudança de modo depois) ----

    @Test
    void marcado_mudarParaLentoDepoisDeMarcado_naoEvitaMaisAEscapada() {
        registrarEscapada(criarEscapada(1, 300, 360));
        Piloto piloto = criarPiloto(260, 1);
        piloto.setModoPilotagem(Piloto.AGRESSIVO);
        piloto.setStress(95);

        controleEscapada.processaEscapadaDaPista(piloto);
        assertEquals(1, piloto.getTracado(), "ainda longe da entrada, só marcado");

        // Piloto (ou outra lógica) muda pra LENTO depois de já marcado.
        piloto.setModoPilotagem(Piloto.LENTO);
        piloto.setNoAtual(pista.get(300));
        controleEscapada.processaEscapadaDaPista(piloto);

        assertEquals(5, piloto.getTracado(), "mudar pra LENTO depois de marcado não deveria mais evitar a escapada");
    }

    @Test
    void pilotoEmLento_naoExcluiMaisDoTeste1_escapaSeFalhar() {
        // LENTO não é mais excluído do teste 1 (removido no tuning): stress acima do limite
        // ainda marca, mesmo já em LENTO.
        registrarEscapada(criarEscapada(1, 300, 360));
        Piloto piloto = criarPiloto(300, 1);
        piloto.setModoPilotagem(Piloto.LENTO);
        piloto.setStress(95);
        // habilidade/potência padrão (0): testeHabilidadePilotoCarro() falha sempre.

        controleEscapada.processaEscapadaDaPista(piloto);

        assertEquals(5, piloto.getTracado(), "teste 1 não exclui mais LENTO: stress acima do limite ainda marca, mesmo já em LENTO");
    }

    // ---- Trava de mudança de traçado enquanto marcado ----

    @Test
    void marcado_mudarTracadoPorOutraVia_eRejeitado() {
        registrarEscapada(criarEscapada(1, 300, 400));
        Piloto piloto = criarPiloto(260, 1);
        piloto.setModoPilotagem(Piloto.AGRESSIVO);
        piloto.setStress(95);

        controleEscapada.processaEscapadaDaPista(piloto);
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

        controleEscapada.processaEscapadaDaPista(piloto);
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

        controleEscapada.processaEscapadaDaPista(piloto);

        // 2 chamadas: testePotencia() (sucede, potência alta) + testeHabilidadePiloto() (falha, habilidade padrão 0).
        verify(controleJogo.getRandom(), org.mockito.Mockito.times(2)).nextDouble();
    }

    @Test
    void janela_centoECinquentaEUmIndices_aindaNaoRecebeOTeste() {
        registrarEscapada(criarEscapada(1, 451, 500));
        Piloto piloto = criarPiloto(300, 1);
        piloto.setModoPilotagem(Piloto.AGRESSIVO);
        piloto.setStress(95);

        controleEscapada.processaEscapadaDaPista(piloto);

        verify(controleJogo.getRandom(), never()).nextDouble();
    }

    // ---- Jogador humano (manual ou automático): sem exceção, mesma regra da IA ----

    @Test
    void jogadorHumanoEmModoManual_semExcecao_escapaQuandoFalhaNoTeste() {
        registrarEscapada(criarEscapada(1, 300, 500));
        when(controleJogo.getAutomaticoManual()).thenReturn(Global.CONTROLE_MANUAL);
        Piloto piloto = criarPiloto(300, 1);
        piloto.setJogadorHumano(true);
        piloto.setModoPilotagem(Piloto.AGRESSIVO);
        piloto.setStress(95);
        // habilidade/potência padrão (0): testeHabilidadePilotoCarro() falha sempre, igual pra IA.

        controleEscapada.processaEscapadaDaPista(piloto);

        assertEquals(5, piloto.getTracado(),
                "jogador humano em modo manual não tem exceção: escapa ao falhar no teste, igual a um piloto de IA");
        assertEquals(Piloto.AGRESSIVO, piloto.getModoPilotagem(), "não há mais recompensa de LENTO");
    }

    @Test
    void jogadorHumanoEmModoManual_semExcecao_evitaAMarcaComSucessoNoTeste() {
        registrarEscapada(criarEscapada(1, 300, 360));
        when(controleJogo.getAutomaticoManual()).thenReturn(Global.CONTROLE_MANUAL);
        Piloto piloto = criarPiloto(300, 1);
        piloto.setJogadorHumano(true);
        piloto.setModoPilotagem(Piloto.AGRESSIVO);
        piloto.setStress(95);
        piloto.setHabilidade(1000);
        piloto.getCarro().setPotencia(1000);
        when(controleJogo.getRandom().nextDouble()).thenReturn(0.0);

        controleEscapada.processaEscapadaDaPista(piloto);

        assertEquals(1, piloto.getTracado(),
                "sucesso no teste também evita a marca pro jogador humano em modo manual, sem exceção");
    }

    @Test
    void jogadorHumanoEmModoAutomatico_mesmaRegraDoManual() {
        registrarEscapada(criarEscapada(1, 300, 500));
        when(controleJogo.getAutomaticoManual()).thenReturn(Global.CONTROLE_AUTOMATICO);
        Piloto piloto = criarPiloto(300, 1);
        piloto.setJogadorHumano(true);
        piloto.setModoPilotagem(Piloto.AGRESSIVO);
        piloto.setStress(95);
        // habilidade/potência padrão (0): testeHabilidadePilotoCarro() falha sempre, igual pra IA.

        controleEscapada.processaEscapadaDaPista(piloto);

        assertEquals(5, piloto.getTracado(),
                "modo automático segue a mesma regra do manual — não há diferença de tratamento pro jogador humano");
        assertEquals(Piloto.AGRESSIVO, piloto.getModoPilotagem());
    }

    // ---- Entrada já passada (tolerância de salto de ciclo) e zona alcançável ----

    @Test
    void carroDentroDaZonaMuitoAlemDaEntrada_naoForcaEscapada_soZonasAindaAlcancaveis() {
        registrarEscapada(criarEscapada(1, 200, 700));
        Piloto piloto = criarPiloto(550, 1);
        piloto.setModoPilotagem(Piloto.AGRESSIVO);
        piloto.setStress(95);

        controleEscapada.processaEscapadaDaPista(piloto);

        assertEquals(1, piloto.getTracado(), "350 índices além da entrada é longe demais (além da tolerância de 150) — essa zona já foi perdida");
    }

    @Test
    void carroComSaltoGrandeDeGanho_140IndicesAlemDaEntrada_aindaForcaEscapada() {
        registrarEscapada(criarEscapada(1, 200, 700));
        Piloto piloto = criarPiloto(340, 1);
        piloto.setModoPilotagem(Piloto.AGRESSIVO);
        piloto.setStress(95);

        controleEscapada.processaEscapadaDaPista(piloto);

        assertEquals(5, piloto.getTracado(),
                "140 índices além da entrada (dentro da tolerância de 150) ainda deveria forçar a escapada");
    }

    @Test
    void semEscapadaNoTracadoAtual_naoFazNada() {
        registrarEscapada(criarEscapada(2, 300, 360));
        Piloto piloto = criarPiloto(280, 1);
        piloto.setModoPilotagem(Piloto.AGRESSIVO);
        piloto.setStress(95);

        controleEscapada.processaEscapadaDaPista(piloto);

        assertEquals(1, piloto.getTracado(), "só há escapada no traçado 2; piloto no traçado 1 não deveria ser afetado");
    }

    // ---- Retorno da escapada (traçado de fuga 4/5 → origem), inalterado por esta mudança ----

    @Test
    void saidaDaEscapada_dentroDe100IndicesDoFim_comHabilidade_voltaParaOTracadoDeOrigem1() {
        registrarEscapada(criarEscapada(1, 200, 360));
        Piloto piloto = criarPiloto(280, 5);
        piloto.setHabilidade(1000);
        when(controleJogo.getRandom().nextDouble()).thenReturn(0.0);

        controleEscapada.processaEscapadaDaPista(piloto);

        assertEquals(1, piloto.getTracado(),
                "a 80 índices do fim (indiceSaida=360), com o teste de habilidade bem-sucedido, deveria voltar pro traçado de origem (1), nunca pro 2");
    }

    @Test
    void saidaDaEscapada_dentroDe100IndicesDoFim_comHabilidade_voltaParaOTracadoDeOrigem2() {
        registrarEscapada(criarEscapada(2, 200, 360));
        Piloto piloto = criarPiloto(280, 4);
        piloto.setHabilidade(1000);
        when(controleJogo.getRandom().nextDouble()).thenReturn(0.0);

        controleEscapada.processaEscapadaDaPista(piloto);

        assertEquals(2, piloto.getTracado(),
                "a 80 índices do fim (indiceSaida=360), com o teste de habilidade bem-sucedido, deveria voltar pro traçado de origem (2), nunca pro 1");
    }

    @Test
    void saidaDaEscapada_semSucessoNoTesteDeHabilidade_continuaNoTracadoDeFuga() {
        registrarEscapada(criarEscapada(1, 200, 360));
        Piloto piloto = criarPiloto(280, 5);

        controleEscapada.processaEscapadaDaPista(piloto);

        assertEquals(5, piloto.getTracado(), "sem sucesso no teste de habilidade, deveria continuar no traçado de fuga");
    }

    @Test
    void saidaDaEscapada_foraDaJanelaDe100IndicesDoFim_naoTentaVoltarMesmoComHabilidade() {
        registrarEscapada(criarEscapada(1, 200, 500));
        Piloto piloto = criarPiloto(280, 5);
        piloto.setHabilidade(1000);
        when(controleJogo.getRandom().nextDouble()).thenReturn(0.0);

        controleEscapada.processaEscapadaDaPista(piloto);

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

        controleEscapada.processaEscapadaDaPista(piloto);

        assertEquals(60, piloto.getGanho(), "ganho deveria ser reduzido a 0.6x durante a escapada");
        assertEquals(Carro.GIRO_MIN_VAL, piloto.getCarro().getGiro(), "giro deveria ser travado no mínimo durante a escapada");
        assertEquals(Piloto.LENTO, piloto.getModoPilotagem(), "modo deveria ser travado em LENTO durante a escapada");
    }

    @Test
    void aoVoltarDoTracadoDeFugaParaOTracadoDeOrigem_modoEGiroSaoRestauradosAoNormal() {
        Piloto piloto = criarPiloto(320, 5);
        piloto.setModoPilotagem(Piloto.AGRESSIVO);
        piloto.getCarro().setGiro(Carro.GIRO_MAX_VAL);

        controleEscapada.processaEscapadaDaPista(piloto);
        assertEquals(Piloto.LENTO, piloto.getModoPilotagem(), "deveria estar em LENTO enquanto no traçado de fuga");
        assertEquals(Carro.GIRO_MIN_VAL, piloto.getCarro().getGiro());

        // Simula o retorno já ter acontecido (processaSaidaDaEscapada mudou o traçado).
        piloto.setTracado(2);
        controleEscapada.processaEscapadaDaPista(piloto);

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

        controleEscapada.processaEscapadaDaPista(piloto);

        assertEquals(5, piloto.getTracado(),
                "não deveria haver diferença de regra entre a volta 1 e as demais — agressivo+estressado na entrada escapa igual em qualquer volta");
    }

    // ---- Piloto indo pro box é excluído da escapada desde a decisão ----

    @Test
    void pilotoDecidiuIrProBox_naoETestadoNemMarcadoPelaEscapada() {
        registrarEscapada(criarEscapada(1, 300, 360));
        Piloto piloto = criarPiloto(300, 1);
        piloto.setModoPilotagem(Piloto.AGRESSIVO);
        piloto.setStress(95);
        piloto.setBox(true); // decidiu ir pro box, mas ainda na pista principal (getPtosBox() == 0).

        controleEscapada.processaEscapadaDaPista(piloto);

        assertEquals(1, piloto.getTracado(), "piloto indo pro box não deveria ser marcado nem escapar, mesmo satisfazendo a pré-condição de risco");
        verify(controleJogo.getRandom(), never()).nextDouble();
    }

    @Test
    void pilotoJaMarcadoPelaEscapada_decideIrProBox_travaELiberada() {
        registrarEscapada(criarEscapada(1, 300, 400));
        Piloto piloto = criarPiloto(260, 1);
        piloto.setModoPilotagem(Piloto.AGRESSIVO);
        piloto.setStress(95);
        piloto.getCarro().setPotencia(0); // força falha no teste de habilidade, garantindo a marca.

        controleEscapada.processaEscapadaDaPista(piloto);
        assertEquals(1, piloto.getTracado(), "ainda longe da entrada, só marcado");

        piloto.setBox(true);
        controleEscapada.processaEscapadaDaPista(piloto);

        // Trava liberada: mudarTracado por qualquer outra via volta a funcionar normalmente.
        boolean mudou = piloto.mudarTracado(0);
        assertTrue(mudou, "trava de escapada deveria ter sido liberada ao decidir ir pro box, mesmo já marcado");
        assertEquals(0, piloto.getTracado());
    }
}
