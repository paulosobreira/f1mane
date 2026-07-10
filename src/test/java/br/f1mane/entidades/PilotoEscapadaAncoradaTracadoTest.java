package br.f1mane.entidades;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
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
 * Cobre a reconexão da corrida ao novo modelo de {@link ObjetoEscapada}
 * (mudança escapada-ia-corrida-box-uniforme): comprometimento por
 * agressividade+estresse dentro de 40 índices da entrada, tentativa de
 * desvio para o traçado 0 dentro de 100 índices para os demais pilotos, e a
 * execução forçada da escapada (traçado 4/5) ao alcançar a entrada.
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

    @Test
    void agressivoEEstressado_dentroDe40Indices_comprometeSemTentarDesviar() {
        registrarEscapada(criarEscapada(1, 300, 360));
        Piloto piloto = criarPiloto(260, 1);
        piloto.setModoPilotagem(Piloto.AGRESSIVO);
        piloto.setStress(95);

        piloto.processaEscapadaDaPista();

        assertEquals(1, piloto.getTracado(), "comprometido não deveria tentar mudar para o traçado 0 antes da entrada");
    }

    @Test
    void agressivoEEstressado_umIndiceAlemDaJanelaDe40_aindaTentaDesviar() {
        registrarEscapada(criarEscapada(1, 301, 400));
        Piloto piloto = criarPiloto(260, 1);
        piloto.setModoPilotagem(Piloto.AGRESSIVO);
        piloto.setStress(95);

        piloto.processaEscapadaDaPista();

        assertEquals(0, piloto.getTracado(), "a 41 índices (janela caiu de 50 pra 40) ainda não deveria se comprometer");
    }

    @Test
    void agressivoEEstressado_foraDaJanelaDe40_aindaTentaDesviar() {
        registrarEscapada(criarEscapada(1, 340, 400));
        Piloto piloto = criarPiloto(260, 1);
        piloto.setModoPilotagem(Piloto.AGRESSIVO);
        piloto.setStress(95);

        piloto.processaEscapadaDaPista();

        assertEquals(0, piloto.getTracado(), "a 80 índices (>40) ainda não deveria se comprometer, e a tentativa de desvio deveria ter sucesso (sem colisão/cooldown)");
    }

    @Test
    void carroDentroDaZonaMuitoAlemDaEntrada_naoForcaEscapada_soZonasAindaAlcancaveis() {
        // Regressão: carro chega no traçado 1 bem depois da entrada (200), por exemplo por
        // uma troca de traçado lateral no meio da zona, sem nenhuma relação com a entrada em
        // si — antes da correção, qualquer índice dentro de [entrada, saida] disparava a
        // escapada forçada, mesmo estando muito além da entrada. Isso inflava demais a taxa
        // de escapadas.
        registrarEscapada(criarEscapada(1, 200, 400));
        Piloto piloto = criarPiloto(350, 1);
        piloto.setModoPilotagem(Piloto.AGRESSIVO);
        piloto.setStress(95);

        piloto.processaEscapadaDaPista();

        assertEquals(1, piloto.getTracado(), "150 índices além da entrada é longe demais pra ainda contar — essa zona já foi perdida");
    }

    @Test
    void carroLigeiramenteAlemDaEntrada_dentroDaTolerancia_aindaForcaEscapada() {
        // O índice avança por avancoLimitado (pode ser >1 por ciclo), então um salto pode
        // pular exatamente por cima da entrada — uma pequena tolerância negativa continua
        // válida, só não pode ser o intervalo inteiro da zona.
        registrarEscapada(criarEscapada(1, 200, 400));
        Piloto piloto = criarPiloto(215, 1);
        piloto.setModoPilotagem(Piloto.AGRESSIVO);
        piloto.setStress(95);

        piloto.processaEscapadaDaPista();

        assertEquals(5, piloto.getTracado(), "15 índices além da entrada ainda está dentro da tolerância de salto de 1 ciclo");
    }

    @Test
    void pilotoNormal_dentroDe100Indices_tentaDesviarComSucesso() {
        registrarEscapada(criarEscapada(2, 340, 400));
        Piloto piloto = criarPiloto(260, 2);
        piloto.setModoPilotagem(Piloto.NORMAL);
        piloto.setStress(0);

        piloto.processaEscapadaDaPista();

        assertEquals(0, piloto.getTracado(), "piloto normal deveria conseguir desviar para o traçado 0 dentro da janela de 100 índices");
    }

    @Test
    void pilotoNormal_alcancaAEntradaAindaNoTracadoOrigem1_forcaEscapadaNoTracado5() {
        registrarEscapada(criarEscapada(1, 300, 360));
        Piloto piloto = criarPiloto(300, 1);
        piloto.setModoPilotagem(Piloto.NORMAL);
        piloto.setStress(0);

        piloto.processaEscapadaDaPista();

        assertEquals(5, piloto.getTracado(),
                "ao alcançar a entrada ainda no traçado 1, deveria forçar a escapada para o traçado 5 "
                        + "(mudarTracado só permite voltar de 5 pra 1, nunca de 4 pra 1 — por isso a fuga do traçado 1 tem que ser pelo 5)");
    }

    @Test
    void pilotoNormal_alcancaAEntradaAindaNoTracadoOrigem2_forcaEscapadaNoTracado4() {
        registrarEscapada(criarEscapada(2, 300, 360));
        Piloto piloto = criarPiloto(300, 2);
        piloto.setModoPilotagem(Piloto.NORMAL);
        piloto.setStress(0);

        piloto.processaEscapadaDaPista();

        assertEquals(4, piloto.getTracado(),
                "ao alcançar a entrada ainda no traçado 2, deveria forçar a escapada para o traçado 4 "
                        + "(mudarTracado só permite voltar de 4 pra 2, nunca de 5 pra 2 — por isso a fuga do traçado 2 tem que ser pelo 4)");
    }

    @Test
    void pilotoEmModoLento_naoEscapaNoGatilho_mesmoComprometido() {
        registrarEscapada(criarEscapada(1, 300, 360));
        Piloto piloto = criarPiloto(300, 1);
        piloto.setModoPilotagem(Piloto.LENTO);
        piloto.setStress(95);

        piloto.processaEscapadaDaPista();

        assertEquals(1, piloto.getTracado(), "piloto em modo LENTO nunca deveria escapar, mesmo alcançando a entrada com stress alto");
    }

    @Test
    void testeDeHabilidadeBemSucedidoNoGatilho_mudaParaLentoENaoEscapa() {
        registrarEscapada(criarEscapada(1, 300, 360));
        Piloto piloto = criarPiloto(300, 1);
        piloto.setModoPilotagem(Piloto.NORMAL);
        piloto.setStress(0);
        piloto.setHabilidade(1000);
        when(controleJogo.getRandom().nextDouble()).thenReturn(0.0);

        piloto.processaEscapadaDaPista();

        assertEquals(1, piloto.getTracado(), "teste de habilidade bem-sucedido no gatilho deveria evitar a escapada, mantendo o piloto no traçado 1");
        assertEquals(Piloto.LENTO, piloto.getModoPilotagem(), "piloto que se salvou no teste de habilidade deveria mudar para o modo LENTO");
    }

    @Test
    void testeDeHabilidadeFalhaNoGatilho_forcaEscapadaNormalmente() {
        registrarEscapada(criarEscapada(1, 300, 360));
        Piloto piloto = criarPiloto(300, 1);
        piloto.setModoPilotagem(Piloto.NORMAL);
        piloto.setStress(0);
        // habilidade padrão (0): teste de habilidade falha sempre.

        piloto.processaEscapadaDaPista();

        assertEquals(5, piloto.getTracado(), "teste de habilidade sem sucesso deveria forçar a escapada normalmente");
    }

    @Test
    void jogadorHumanoEmModoManual_naoRecebeTesteDeHabilidade_escapaNormalmente() {
        // Zona bem mais longa que a janela de retorno (100 índices) pra não confundir com o
        // teste de habilidade SEPARADO de Piloto.processaSaidaDaEscapada (retorno da escapada,
        // fora do escopo desta mudança) — com uma zona curta, o retorno dispararia no mesmo
        // ciclo da entrada (já dentro dos 100 índices do fim) e devolveria o piloto pro traçado
        // de origem antes da asserção, mascarando o comportamento sendo testado aqui.
        registrarEscapada(criarEscapada(1, 300, 500));
        when(controleJogo.getAutomaticoManual()).thenReturn(Global.CONTROLE_MANUAL);
        Piloto piloto = criarPiloto(300, 1);
        piloto.setJogadorHumano(true);
        piloto.setModoPilotagem(Piloto.NORMAL);
        piloto.setStress(0);
        // habilidade máxima + nextDouble()=0.0 passaria no teste se ele fosse aplicado.
        piloto.setHabilidade(1000);
        when(controleJogo.getRandom().nextDouble()).thenReturn(0.0);

        piloto.processaEscapadaDaPista();

        assertEquals(5, piloto.getTracado(),
                "jogador humano em modo manual não recebe o teste de habilidade automático — escapa normalmente, mesmo com habilidade máxima");
        assertEquals(Piloto.NORMAL, piloto.getModoPilotagem(), "não deveria ter sido mudado pra LENTO automaticamente");
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
        piloto.setModoPilotagem(Piloto.NORMAL);
        piloto.setStress(0);
        piloto.setHabilidade(1000);
        when(controleJogo.getRandom().nextDouble()).thenReturn(0.0);

        piloto.processaEscapadaDaPista();

        assertEquals(1, piloto.getTracado(),
                "jogador humano em modo AUTOMATICO (não manual) continua recebendo o teste de habilidade, como um piloto de IA");
        assertEquals(Piloto.LENTO, piloto.getModoPilotagem());
    }

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
        // habilidade padrão (0): testeHabilidadePiloto() falha sempre (nextDouble() >= 0.0 nunca é < 0.0).

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

    @Test
    void semEscapadaNoTracadoAtual_naoFazNada() {
        registrarEscapada(criarEscapada(2, 300, 360));
        Piloto piloto = criarPiloto(280, 1);
        piloto.setModoPilotagem(Piloto.AGRESSIVO);
        piloto.setStress(95);

        piloto.processaEscapadaDaPista();

        assertEquals(1, piloto.getTracado(), "só há escapada no traçado 2; piloto no traçado 1 não deveria ser afetado");
    }

    @Test
    void flagGlobalDeTeste_comprometePilotoNormalNaoEstressado() {
        Global.FORCAR_ESCAPADA_TESTE = true;
        registrarEscapada(criarEscapada(1, 300, 360));
        Piloto piloto = criarPiloto(260, 1);
        piloto.setModoPilotagem(Piloto.NORMAL);
        piloto.setStress(0);

        piloto.processaEscapadaDaPista();

        assertEquals(1, piloto.getTracado(), "com a flag ativa, mesmo um piloto normal/não estressado deveria se comprometer e não tentar desviar");
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
    void velocidadeEModoReduzidosDuranteATracado4() {
        Piloto piloto = criarPiloto(320, 4);
        piloto.setModoPilotagem(Piloto.AGRESSIVO);
        piloto.getCarro().setGiro(Carro.GIRO_MAX_VAL);
        piloto.setGanho(100);

        piloto.processaEscapadaDaPista();

        assertEquals(40, piloto.getGanho(), "ganho deveria ser reduzido a 0.4x durante a escapada");
        assertEquals(Carro.GIRO_MIN_VAL, piloto.getCarro().getGiro(), "giro deveria ser travado no mínimo durante a escapada");
        assertEquals(Piloto.LENTO, piloto.getModoPilotagem(), "modo deveria ser travado em LENTO durante a escapada");
    }

    @Test
    void velocidadeEModoNaoReduzidosDuranteAAnimacaoDeRetorno_soNoTracado4ou5Mesmo() {
        // A partir do feedback do usuário, a redução vale só enquanto o traçado é
        // literalmente 4/5 — não mais durante toda a janela de retorno (a animação de
        // troca de traçado é só o suavizado visual, comum a qualquer mudança de traçado).
        Piloto piloto = criarPiloto(320, 0);
        piloto.setTracadoAntigo(5);
        piloto.setIndiceTracado(10);
        piloto.setModoPilotagem(Piloto.AGRESSIVO);
        piloto.getCarro().setGiro(Carro.GIRO_MAX_VAL);
        piloto.setGanho(100);

        piloto.processaEscapadaDaPista();

        assertEquals(100, piloto.getGanho(), "ganho não deveria mais ser reduzido durante a animação de retorno, só no traçado 4/5 em si");
        assertEquals(Carro.GIRO_MAX_VAL, piloto.getCarro().getGiro(), "giro não deveria ser mexido durante a animação de retorno");
        assertEquals(Piloto.AGRESSIVO, piloto.getModoPilotagem(), "modo não deveria ser mexido durante a animação de retorno");
    }

    @Test
    void aoVoltarDoTracadoDeFugaParaOTracadoDeOrigem_modoEGiroSaoRestauradosAoNormal() {
        // Regressão: sem restaurar explicitamente, o piloto ficava travado em LENTO/giro
        // mínimo pra sempre depois de escapar, porque o reset automático de outro método
        // (processaIAnovoIndex) não roda sempre (ex.: com colisao != null).
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

    @Test
    void gatilhoCegoExistente_continuaFuncionandoSemAlteracao() {
        // Regressão: o gatilho cego (stress+agressivo+curva baixa no traçado 0, empurrando
        // pra 1/2 aleatoriamente) não deveria ter sido alterado por esta mudança — roda em
        // paralelo à nova lógica ancorada a ObjetoEscapada, sem relação com ela.
        when(controleJogo.getRandom().intervalo(1, 2)).thenReturn(1);
        Piloto piloto = criarPiloto(260, 0);
        piloto.setModoPilotagem(Piloto.AGRESSIVO);
        piloto.setStress(95);
        piloto.getCarro().setPorcentagemDesgastePneus(10);
        No noAtual = piloto.getNoAtual();
        noAtual.setTipo(No.CURVA_BAIXA);

        piloto.processaEscapadaDaPista();

        assertEquals(1, piloto.getTracado(), "gatilho cego deveria continuar empurrando pro traçado 1 ou 2 quando em curva baixa no traçado 0");
    }

}
