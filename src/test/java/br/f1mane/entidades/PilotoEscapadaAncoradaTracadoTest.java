package br.f1mane.entidades;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
        // Pneus "novos" por padrão (Carro.porcentagemDesgastePneus é 0 se nunca setado, o que
        // pareceria pneu criticamente gasto pra regra de pneus<20% — testes que querem exercitar
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
    void agressivoEEstressado_umIndiceAlemDaJanelaDe40_naoMudaDeTracadoAindaAntesDaEntrada() {
        // A lógica de escapada não dirige mais o carro pra longe da zona (ver D15 do design.md) —
        // ela só decide comprometimento/escapada a partir de onde o piloto já está. A 41 índices
        // (ainda longe da entrada), nada deveria mudar o traçado nesse ciclo.
        registrarEscapada(criarEscapada(1, 301, 400));
        Piloto piloto = criarPiloto(260, 1);
        piloto.setModoPilotagem(Piloto.AGRESSIVO);
        piloto.setStress(95);

        piloto.processaEscapadaDaPista();

        assertEquals(1, piloto.getTracado(), "longe da entrada, nada deveria mudar o traçado ainda — a lógica de escapada não desvia pro traçado 0 por conta própria");
    }

    @Test
    void agressivoEEstressado_foraDaJanelaDe40_naoMudaDeTracadoAindaAntesDaEntrada() {
        registrarEscapada(criarEscapada(1, 340, 400));
        Piloto piloto = criarPiloto(260, 1);
        piloto.setModoPilotagem(Piloto.AGRESSIVO);
        piloto.setStress(95);

        piloto.processaEscapadaDaPista();

        assertEquals(1, piloto.getTracado(), "a 80 índices, ainda longe da entrada, nada deveria mudar o traçado nesse ciclo");
    }

    @Test
    void carroDentroDaZonaMuitoAlemDaEntrada_naoForcaEscapada_soZonasAindaAlcancaveis() {
        // Regressão: carro chega no traçado 1 bem depois da entrada (200), por exemplo por
        // uma troca de traçado lateral no meio da zona, sem nenhuma relação com a entrada em
        // si — antes da correção original (D9), qualquer índice dentro de [entrada, saida]
        // disparava a escapada forçada, mesmo estando muito além da entrada. Isso inflava demais
        // a taxa de escapadas. Zona larga (500 índices, perto da mais estreita em Interlagos, 451)
        // e distância bem maior que a tolerância atual (150), pra continuar válido depois do
        // aumento de 20 pra 150 (bug de escapadas "aleatoriamente" puladas — ver javadoc da
        // constante).
        registrarEscapada(criarEscapada(1, 200, 700));
        Piloto piloto = criarPiloto(550, 1);
        piloto.setModoPilotagem(Piloto.AGRESSIVO);
        piloto.setStress(95);

        piloto.processaEscapadaDaPista();

        assertEquals(1, piloto.getTracado(), "350 índices além da entrada é longe demais pra ainda contar (bem além da tolerância de 150) — essa zona já foi perdida");
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
    void carroComSaltoGrandeDeGanho_140IndicesAlemDaEntrada_aindaForcaEscapada() {
        // Regressão do bug relatado em Interlagos: com a tolerância antiga (20), um salto de
        // ganho real (~50-55 num único ciclo, ver javadoc da constante) podia levar o índice de
        // "ainda não chegou" direto pra além da tolerância, sem nunca passar por um ciclo em que a
        // escapada disparasse — parecia aleatório entre voltas. 140 índices além da entrada
        // simula esse salto grande; com a tolerância aumentada pra 150, ainda deveria forçar a
        // escapada normalmente.
        registrarEscapada(criarEscapada(1, 200, 700));
        Piloto piloto = criarPiloto(340, 1);
        piloto.setModoPilotagem(Piloto.AGRESSIVO);
        piloto.setStress(95);

        piloto.processaEscapadaDaPista();

        assertEquals(5, piloto.getTracado(),
                "140 índices além da entrada (dentro da nova tolerância de 150) ainda deveria forçar a escapada, mesmo sendo um salto bem maior que o antigo limite de 20");
    }

    @Test
    void pilotoNormal_dentroDe100Indices_naoMudaDeTracadoAindaAntesDaEntrada() {
        // Piloto normal (não "em risco") longe da entrada: a lógica de escapada não faz nada
        // além de monitorar — não desvia o carro por conta própria (ver D15 do design.md).
        registrarEscapada(criarEscapada(2, 340, 400));
        Piloto piloto = criarPiloto(260, 2);
        piloto.setModoPilotagem(Piloto.NORMAL);
        piloto.setStress(0);

        piloto.processaEscapadaDaPista();

        assertEquals(2, piloto.getTracado(), "longe da entrada, nada deveria mudar o traçado do piloto normal nesse ciclo");
    }

    @Test
    void pilotoNormal_naoEmRisco_alcancaAEntradaNoTracadoOrigem1_naoEscapa() {
        // Correção de bug relatado: piloto NORMAL, sem stress e com pneus ok não está "em risco"
        // (ver emRiscoDeEscapada()) — não deveria ser testado nem forçado a escapar só por
        // alcançar a entrada ainda no traçado 1/2. Antes desta correção, qualquer piloto que
        // chegasse na entrada recebia uma "última chance" que quase sempre falhava (habilidade
        // baixa/1000), fazendo praticamente todo mundo escapar, independente das regras.
        registrarEscapada(criarEscapada(1, 300, 360));
        Piloto piloto = criarPiloto(300, 1);
        piloto.setModoPilotagem(Piloto.NORMAL);
        piloto.setStress(0);

        piloto.processaEscapadaDaPista();

        assertEquals(1, piloto.getTracado(),
                "piloto não em risco (normal, sem stress, pneus ok) não deveria escapar só por alcançar a entrada");
    }

    @Test
    void pilotoNormal_naoEmRisco_alcancaAEntradaNoTracadoOrigem2_naoEscapa() {
        registrarEscapada(criarEscapada(2, 300, 360));
        Piloto piloto = criarPiloto(300, 2);
        piloto.setModoPilotagem(Piloto.NORMAL);
        piloto.setStress(0);

        piloto.processaEscapadaDaPista();

        assertEquals(2, piloto.getTracado(),
                "piloto não em risco (normal, sem stress, pneus ok) não deveria escapar só por alcançar a entrada");
    }

    @Test
    void pneusBaixos_alcancaAEntradaAindaNoTracadoOrigem1_forcaEscapadaNoTracado5() {
        registrarEscapada(criarEscapada(1, 300, 360));
        Piloto piloto = criarPiloto(300, 1);
        piloto.setModoPilotagem(Piloto.NORMAL);
        piloto.setStress(0);
        piloto.getCarro().setPorcentagemDesgastePneus(15);
        // habilidade padrão (0): teste de habilidade falha sempre.

        piloto.processaEscapadaDaPista();

        assertEquals(5, piloto.getTracado(),
                "piloto em risco (pneus <20%) que falha no teste, ao alcançar a entrada ainda no traçado 1, deveria "
                        + "forçar a escapada para o traçado 5 (mudarTracado só permite voltar de 5 pra 1, nunca de 4 "
                        + "pra 1 — por isso a fuga do traçado 1 tem que ser pelo 5)");
    }

    @Test
    void pneusBaixos_alcancaAEntradaAindaNoTracadoOrigem2_forcaEscapadaNoTracado4() {
        registrarEscapada(criarEscapada(2, 300, 360));
        Piloto piloto = criarPiloto(300, 2);
        piloto.setModoPilotagem(Piloto.NORMAL);
        piloto.setStress(0);
        piloto.getCarro().setPorcentagemDesgastePneus(15);
        // habilidade padrão (0): teste de habilidade falha sempre.

        piloto.processaEscapadaDaPista();

        assertEquals(4, piloto.getTracado(),
                "piloto em risco (pneus <20%) que falha no teste, ao alcançar a entrada ainda no traçado 2, deveria "
                        + "forçar a escapada para o traçado 4 (mudarTracado só permite voltar de 4 pra 2, nunca de 5 "
                        + "pra 2 — por isso a fuga do traçado 2 tem que ser pelo 4)");
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
        // pneus baixos colocam o piloto em risco (só piloto em risco é testado/pode escapar).
        piloto.getCarro().setPorcentagemDesgastePneus(15);
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
        // pneus baixos colocam o piloto em risco (só piloto em risco é testado/pode escapar).
        piloto.getCarro().setPorcentagemDesgastePneus(15);
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
        // pneus baixos colocam o piloto em risco (só piloto em risco é testado/pode escapar).
        piloto.getCarro().setPorcentagemDesgastePneus(15);
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
        // pneus baixos colocam o piloto em risco (só piloto em risco é testado/pode escapar).
        piloto.getCarro().setPorcentagemDesgastePneus(15);
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
    void saidaDaEscapada_saltoGrandeAlemDoFim_dentroDaTolerancia_aindaTentaVoltar() {
        // Regressão: bug análogo ao da entrada (índice avança por avancoLimitado, que pode ser
        // >1 por ciclo — aqui reduzido pelo multiplicador de ganho de 0.8 do traçado de fuga, mas
        // ainda relevante). Sem tolerância nenhuma no lado da saída, um piloto que ultrapassasse
        // indiceSaida antes de um teste de habilidade bem-sucedido ficava preso no traçado de fuga
        // pra sempre — escapadaAtivaNoTracadoDeFuga() parava de encontrar a zona.
        registrarEscapada(criarEscapada(1, 200, 360));
        Piloto piloto = criarPiloto(420, 5); // 60 índices além de indiceSaida (360), dentro da nova tolerância (100)
        piloto.setHabilidade(1000);
        when(controleJogo.getRandom().nextDouble()).thenReturn(0.0);

        piloto.processaEscapadaDaPista();

        assertEquals(1, piloto.getTracado(),
                "60 índices além do fim (dentro da tolerância de 100) ainda deveria testar e voltar pro traçado de origem, mesmo já tendo ultrapassado indiceSaida");
    }

    @Test
    void saidaDaEscapada_muitoAlemDoFim_foraDaTolerancia_naoTentaVoltar() {
        registrarEscapada(criarEscapada(1, 200, 360));
        Piloto piloto = criarPiloto(510, 5); // 150 índices além de indiceSaida (360), além da tolerância (100)
        piloto.setHabilidade(1000);
        when(controleJogo.getRandom().nextDouble()).thenReturn(0.0);

        piloto.processaEscapadaDaPista();

        assertEquals(5, piloto.getTracado(),
                "150 índices além do fim é longe demais pra ainda contar (além da tolerância de 100) — não deveria tentar voltar, mesmo com habilidade máxima");
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
    void flagGlobalDeTeste_forcaEscapadaMesmoComPilotoEmLento() {
        // Pedido do usuário: com a flag de validação ativa, nem a exceção de LENTO deveria
        // impedir a escapada — a flag é justamente pra garantir que a mecânica dispare sem
        // exceção nenhuma num cenário controlado.
        Global.FORCAR_ESCAPADA_TESTE = true;
        registrarEscapada(criarEscapada(1, 300, 360));
        Piloto piloto = criarPiloto(300, 1);
        piloto.setModoPilotagem(Piloto.LENTO);
        piloto.setStress(0);

        piloto.processaEscapadaDaPista();

        assertEquals(5, piloto.getTracado(),
                "com a flag ativa, mesmo um piloto em modo LENTO deveria escapar ao alcançar a entrada");
    }

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
    void flagGlobalDeTeste_alemDe40Indices_naoConsegueMaisDesviar() {
        // Regressão do bug relatado: "FORCAR_ESCAPADA_TESTE não está funcionando". Antes da
        // correção, a flag só suprimia a checagem de agressivo+estresse DENTRO da janela de 40
        // índices; entre 41 e 100 o piloto continuava tentando (e, sem colisão, conseguindo)
        // desviar pro traçado 0 normalmente, escapando da zona antes de nunca ficar
        // "comprometido" — na prática a flag nunca surtia efeito numa corrida de validação.
        Global.FORCAR_ESCAPADA_TESTE = true;
        registrarEscapada(criarEscapada(1, 300, 400));
        Piloto piloto = criarPiloto(250, 1);
        piloto.setModoPilotagem(Piloto.NORMAL);
        piloto.setStress(0);

        piloto.processaEscapadaDaPista();

        assertEquals(1, piloto.getTracado(),
                "com a flag ativa, o piloto não deveria conseguir desviar pro traçado 0 mesmo a 50 índices (>40) da entrada");
    }

    @Test
    void pneusAbaixoDe20PorCento_dentroDe40Indices_comprometeSemTentarDesviar() {
        registrarEscapada(criarEscapada(1, 300, 360));
        Piloto piloto = criarPiloto(260, 1);
        piloto.setModoPilotagem(Piloto.NORMAL);
        piloto.setStress(0);
        piloto.getCarro().setPorcentagemDesgastePneus(15);

        piloto.processaEscapadaDaPista();

        assertEquals(1, piloto.getTracado(),
                "pneus abaixo de 20% deveriam comprometer o piloto (sem tentar desviar) a 40 índices da entrada, mesmo sem stress/agressividade");
    }

    @Test
    void pneusAbaixoDe20PorCento_alemDe40Indices_recebeTestePreventivoMasNaoMudaDeTracadoAinda() {
        // Longe da entrada, a lógica de escapada não desvia o carro (D15) — mas o piloto "em
        // risco" (pneus baixos) já ganha o teste preventivo (habilidade padrão 0: falha sempre).
        registrarEscapada(criarEscapada(1, 301, 400));
        Piloto piloto = criarPiloto(260, 1);
        piloto.setModoPilotagem(Piloto.NORMAL);
        piloto.setStress(0);
        piloto.getCarro().setPorcentagemDesgastePneus(15);

        piloto.processaEscapadaDaPista();

        assertEquals(1, piloto.getTracado(), "nada deveria mudar o traçado antes da entrada, mesmo em risco");
        assertEquals(Piloto.NORMAL, piloto.getModoPilotagem(), "teste preventivo sem sucesso (habilidade padrão) não deveria mudar o modo");
    }

    @Test
    void pneusAbaixoDe20PorCentoEEmLento_naoContaComoEmRisco() {
        // "o piloto não tiver lento no traçado com escapada vai escapar" — se já está em LENTO,
        // pneus baixos não deveriam colocá-lo em risco (a tarefa de ficar lento já foi cumprida).
        // Como a lógica de escapada não desvia mais o carro (D15), a única forma observável de
        // "não está em risco" é o teste de habilidade preventivo nunca ser chamado.
        registrarEscapada(criarEscapada(1, 301, 400));
        Piloto piloto = criarPiloto(260, 1);
        piloto.setModoPilotagem(Piloto.LENTO);
        piloto.setStress(0);
        piloto.getCarro().setPorcentagemDesgastePneus(15);

        piloto.processaEscapadaDaPista();

        assertEquals(1, piloto.getTracado(), "nada deveria mudar o traçado antes da entrada");
        assertEquals(Piloto.LENTO, piloto.getModoPilotagem(), "modo não deveria ser alterado");
        verify(controleJogo.getRandom(), never()).nextDouble();
    }

    @Test
    void testePreventivoA100Indices_bemSucedido_viraLentoAntesDaJanelaDeComprometimento() {
        registrarEscapada(criarEscapada(1, 300, 400));
        Piloto piloto = criarPiloto(200, 1);
        piloto.setModoPilotagem(Piloto.NORMAL);
        piloto.setStress(0);
        piloto.getCarro().setPorcentagemDesgastePneus(10);
        piloto.setHabilidade(1000);
        when(controleJogo.getRandom().nextDouble()).thenReturn(0.0);

        piloto.processaEscapadaDaPista();

        assertEquals(1, piloto.getTracado(), "teste preventivo bem-sucedido não deveria mudar o traçado, só o modo");
        assertEquals(Piloto.LENTO, piloto.getModoPilotagem(),
                "a 100 índices da entrada, com pneus baixos e teste de habilidade bem-sucedido, deveria virar LENTO preventivamente");
    }

    @Test
    void testePreventivoBemSucedido_ficaLivreDaZonaMesmoQueModoSejaResetadoDepois() {
        // A fonte de verdade de "já ficou livre" é o cache por zona/volta, não modoPilotagem —
        // porque outra lógica de IA (processaIAnovoIndex) pode resetar o modo pra NORMAL antes do
        // piloto alcançar a entrada, e mesmo assim ele não deveria escapar mais por essa zona.
        registrarEscapada(criarEscapada(1, 300, 400));
        Piloto piloto = criarPiloto(200, 1);
        piloto.setModoPilotagem(Piloto.NORMAL);
        piloto.setStress(0);
        piloto.getCarro().setPorcentagemDesgastePneus(10);
        piloto.setHabilidade(1000);
        when(controleJogo.getRandom().nextDouble()).thenReturn(0.0);
        piloto.processaEscapadaDaPista();
        assertEquals(Piloto.LENTO, piloto.getModoPilotagem());

        // Simula outra lógica de IA resetando o modo, e o piloto avançando até a entrada.
        piloto.setModoPilotagem(Piloto.NORMAL);
        piloto.setNoAtual(pista.get(300));

        piloto.processaEscapadaDaPista();

        assertEquals(1, piloto.getTracado(),
                "mesmo com o modo resetado pra NORMAL por fora, o piloto não deveria escapar — já tinha ficado livre dessa zona nesta volta");
    }

    @Test
    void testePreventivoFalha_naoTestaDeNovoNoGatilho_forcaEscapadaDireto() {
        // Zona bem mais longa que a janela de retorno (100 índices) pra não confundir com o
        // teste de habilidade SEPARADO de processaSaidaDaEscapada (retorno da escapada, fora do
        // escopo desta mudança) — com uma zona curta, o retorno dispararia no mesmo ciclo da
        // entrada e chamaria testeHabilidadePiloto() de novo, inflando a contagem verificada abaixo.
        registrarEscapada(criarEscapada(1, 300, 500));
        Piloto piloto = criarPiloto(260, 1);
        piloto.setModoPilotagem(Piloto.NORMAL);
        piloto.setStress(0);
        piloto.getCarro().setPorcentagemDesgastePneus(10);
        // habilidade padrão (0): teste de habilidade falha sempre.

        piloto.processaEscapadaDaPista();
        assertEquals(1, piloto.getTracado(), "aos 40 índices, comprometido, ainda não deveria ter alcançado a entrada");

        // Se o gatilho testasse de novo aqui, habilidade máxima + nextDouble()=0.0 passaria
        // sempre — isolando exatamente o comportamento sendo verificado (ausência de reteste).
        piloto.setHabilidade(1000);
        when(controleJogo.getRandom().nextDouble()).thenReturn(0.0);
        piloto.setNoAtual(pista.get(300));
        piloto.processaEscapadaDaPista();

        assertEquals(5, piloto.getTracado(),
                "sem sucesso no teste preventivo (a 40 índices), deveria forçar a escapada ao alcançar a entrada sem testar de novo, "
                        + "mesmo com habilidade máxima agora e um teste hipotético que sempre passaria");
    }

    @Test
    void testePreventivo_resetaAoMudarDeVolta_permitindoNovoTeste() {
        registrarEscapada(criarEscapada(1, 300, 360));
        Piloto piloto = criarPiloto(260, 1);
        piloto.setModoPilotagem(Piloto.NORMAL);
        piloto.setStress(0);
        piloto.getCarro().setPorcentagemDesgastePneus(10);
        // habilidade padrão (0): primeiro teste (na volta 0) falha sempre.

        piloto.processaEscapadaDaPista();
        assertEquals(Piloto.NORMAL, piloto.getModoPilotagem(), "primeiro teste (volta 0) deveria ter falhado, sem virar LENTO");

        piloto.setNumeroVolta(1);
        piloto.setHabilidade(1000);
        when(controleJogo.getRandom().nextDouble()).thenReturn(0.0);

        piloto.processaEscapadaDaPista();

        assertEquals(Piloto.LENTO, piloto.getModoPilotagem(),
                "na volta seguinte, o cache deveria ter sido limpo, permitindo um novo teste preventivo (que agora passa)");
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

    @Test
    void aposDerrapagemDoGatilhoCego_naoVoltaAutomaticamenteProTracadoZero() {
        // Regressão do bug relatado pelo usuário: o gatilho cego manda o piloto de 0 pra 1/2
        // (derrapagem por perda de controle, ver teste acima). No ciclo seguinte, se houvesse uma
        // escapada à frente nesse traçado a mais de 40 índices (fora da antiga janela de
        // comprometimento), a lógica de escapada tentava mudarTracado(0) pra evitar a zona —
        // fazendo o piloto "se recompor" e voltar pro traçado seguro logo depois de ter perdido o
        // controle, o que não faz sentido (ver D15 do design.md: quem decide voltar ao traçado 0
        // é a lógica geral de condução/o jogador, nunca a lógica de escapada).
        when(controleJogo.getRandom().intervalo(1, 2)).thenReturn(1);
        registrarEscapada(criarEscapada(1, 340, 400));
        Piloto piloto = criarPiloto(260, 0);
        piloto.setModoPilotagem(Piloto.AGRESSIVO);
        piloto.setStress(95);
        piloto.getCarro().setPorcentagemDesgastePneus(10);
        No noAtual = piloto.getNoAtual();
        noAtual.setTipo(No.CURVA_BAIXA);

        piloto.processaEscapadaDaPista();
        assertEquals(1, piloto.getTracado(), "gatilho cego deveria ter mandado o piloto pro traçado 1 (derrapagem)");

        // Ciclo seguinte: já não está mais em curva baixa (saiu da curva que causou a derrapagem),
        // continua agressivo+estressado — a zona de escapada está a 80 índices (>40) à frente.
        noAtual.setTipo(No.RETA);
        piloto.processaEscapadaDaPista();

        assertEquals(1, piloto.getTracado(),
                "não deveria voltar automaticamente pro traçado 0 só porque há uma escapada à frente");
    }

    // ---- volta 1 usa exatamente a mesma regra das demais voltas (decisão explícita do usuário:
    // reverte a tentativa de desligar a mecânica na volta 1 — ver tasks.md item 8a) ----

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

    @Test
    void volta1_naoMudaDeTracadoAindaAntesDaEntrada_comoQualquerOutraVolta() {
        when(controleJogo.getNumVoltaAtual()).thenReturn(1);
        registrarEscapada(criarEscapada(1, 340, 400));
        Piloto piloto = criarPiloto(260, 1);
        piloto.setModoPilotagem(Piloto.NORMAL);
        piloto.setStress(0);

        piloto.processaEscapadaDaPista();

        assertEquals(1, piloto.getTracado(), "longe da entrada, nada deveria mudar o traçado na volta 1, exatamente como em qualquer outra volta");
    }

    @Test
    void volta1_retornoDoTracadoDeFugaFuncionaComoQualquerOutraVolta() {
        when(controleJogo.getNumVoltaAtual()).thenReturn(1);
        registrarEscapada(criarEscapada(1, 200, 360));
        Piloto piloto = criarPiloto(280, 5);
        piloto.setHabilidade(1000);
        when(controleJogo.getRandom().nextDouble()).thenReturn(0.0);

        piloto.processaEscapadaDaPista();

        assertEquals(1, piloto.getTracado(),
                "o retorno do traçado de fuga (processaSaidaDaEscapada) também não deveria ter regra diferente na volta 1");
    }

}
