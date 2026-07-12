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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import br.f1mane.controles.InterfaceJogo;
import br.nnpe.Global;

/**
 * Cobre a entrada no box em três camadas de intensidade crescente (ver spec
 * {@code tracado-safe-lane-change}): camada 1 (aproximação não forçada, já
 * existente), camada 2 (aproximação forçada dentro de
 * {@code JANELA_ENTRADA_BOX_FORCADA} índices da entrada, nova), e camada 3
 * (posicionamento garantido no ponto de parada via {@code Piloto.posicionarNoBox},
 * nova — só age se as duas primeiras não resolveram a tempo).
 */
class PilotoEntradaBoxCamadasTest {

    private static final int TAMANHO_PISTA = 2000;
    private static final int ENTRADA_BOX_INDEX = 1000;

    private InterfaceJogo controleJogo;
    private List<No> pista;
    private List<Piloto> pilotos;
    private Circuito circuito;

    @BeforeEach
    void setUp() {
        pista = new ArrayList<>();
        for (int i = 0; i < TAMANHO_PISTA; i++) {
            No no = new No();
            no.setIndex(i);
            no.setPoint(new Point(i, 100));
            pista.add(no);
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
        when(circuito.getEntradaBoxIndex()).thenReturn(ENTRADA_BOX_INDEX);
        when(circuito.getLadoBoxSaidaBox()).thenReturn(2);
        when(circuito.getLadoBox()).thenReturn(2);
        when(controleJogo.getCircuito()).thenReturn(circuito);
    }

    private Piloto criarPiloto(int index, int tracado) {
        Piloto piloto = new Piloto();
        piloto.setNome("Piloto");
        Carro carro = new Carro();
        carro.setPiloto(piloto);
        carro.setPorcentagemDesgastePneus(100);
        carro.setPotencia(1000);
        carro.setFreios(1000);
        piloto.setCarro(carro);
        piloto.setControleJogo(controleJogo);
        piloto.setNoAtual(pista.get(index));
        piloto.setTracado(tracado);
        pilotos.add(piloto);
        return piloto;
    }

    // ---- Camadas 1/2: aproximação em processaMudarTracado() ----

    @Test
    void camada1_forA_daJanelaForcada_naoForcaAMudanca() {
        // 500 índices da entrada: dentro da janela de detecção (1000), fora da forçada (100).
        Piloto piloto = criarPiloto(ENTRADA_BOX_INDEX - 500, 1);
        piloto.setBox(true);
        piloto.setIndiceTracado(5); // animação em andamento — só guarda não-forçada bloqueia.
        when(controleJogo.verificaEntradaBox(piloto)).thenReturn(true);

        piloto.processaMudarTracado();

        assertEquals(1, piloto.getTracado(),
                "fora da janela forçada, animação em andamento deveria bloquear a mudança não forçada");
    }

    @Test
    void camada2_dentroDaJanelaForcada_forcaAMudancaIgnorandoAnimacaoEmAndamento() {
        Piloto piloto = criarPiloto(ENTRADA_BOX_INDEX - 50, 1);
        piloto.setBox(true);
        piloto.setIndiceTracado(5); // animação em andamento — guarda não-forçada bloquearia.
        when(controleJogo.verificaEntradaBox(piloto)).thenReturn(true);

        piloto.processaMudarTracado();

        assertEquals(0, piloto.getTracado(),
                "dentro da janela forçada, a mudança pro traçado 0 (intermediário) ignora animação em andamento");
    }

    @Test
    void camada2_ageTambemProJogadorHumanoEmModoManual() {
        when(controleJogo.getAutomaticoManual()).thenReturn(Global.CONTROLE_MANUAL);
        Piloto piloto = criarPiloto(ENTRADA_BOX_INDEX - 50, 1);
        piloto.setJogadorHumano(true);
        piloto.setBox(true);
        piloto.setIndiceTracado(5);
        when(controleJogo.verificaEntradaBox(piloto)).thenReturn(true);

        piloto.processaMudarTracado();

        assertEquals(0, piloto.getTracado(),
                "camada 2 não depende da chave de piloto automático/manual, igual à camada 1");
    }

    // ---- Camada 3: posicionamento garantido (Piloto.posicionarNoBox) ----

    @Test
    void camada3_posicionaNoLadoDoBoxQuandoAindaNaoEstaLa() {
        Piloto piloto = criarPiloto(500, 1);

        boolean mudou = piloto.posicionarNoBox(2);

        assertTrue(mudou);
        assertEquals(2, piloto.getTracado(), "deveria ser posicionado no traçado do box, mesmo vindo do traçado oposto (1)");
    }

    @Test
    void camada3_ladoOpostoTambemFunciona() {
        Piloto piloto = criarPiloto(500, 2);

        boolean mudou = piloto.posicionarNoBox(1);

        assertTrue(mudou);
        assertEquals(1, piloto.getTracado());
    }

    @Test
    void camada3_eNoOpQuandoJaEstaNoTracadoCerto() {
        Piloto piloto = criarPiloto(500, 2);
        piloto.setIndiceTracado(7);
        piloto.setTracadoAntigo(1);

        boolean mudou = piloto.posicionarNoBox(2);

        assertFalse(mudou);
        assertEquals(2, piloto.getTracado());
        assertEquals(7, piloto.getIndiceTracado(), "não deveria resetar indiceTracado quando já está no traçado certo");
        assertEquals(1, piloto.getTracadoAntigo(), "não deveria resetar tracadoAntigo quando já está no traçado certo");
    }

    @Test
    void camada3_naoReposicionaPilotoMarcadoPelaEscapada() {
        ObjetoEscapada escapada = new ObjetoEscapada();
        escapada.setTracadoOrigem(1);
        escapada.setIndiceEntrada(300);
        escapada.setIndiceSaida(360);
        List<ObjetoPista> objetos = new ArrayList<>();
        objetos.add(escapada);
        when(circuito.getObjetos()).thenReturn(objetos);

        Piloto piloto = criarPiloto(260, 1);
        piloto.setModoPilotagem(Piloto.AGRESSIVO);
        piloto.setStress(95);
        piloto.getCarro().setPotencia(0); // força falha no teste de habilidade, garantindo a marca.
        piloto.processaEscapadaDaPista();
        assertEquals(1, piloto.getTracado(), "ainda longe da entrada, só marcado");

        boolean mudou = piloto.posicionarNoBox(2);

        assertFalse(mudou, "trava de escapada deveria impedir o posicionamento no box");
        assertEquals(1, piloto.getTracado());
    }

    // ---- Prioridade entre causas de processaMudarTracado() ----

    @Test
    void duasCausasSatisfeitas_soAPrioridadeMaisAltaAge() {
        // Entrada no box (prioridade 1 da lista) e recentralização sem tráfego (prioridade 6,
        // última) satisfeitas ao mesmo tempo — alvos diferentes (2 vs 0) tornam o resultado
        // observável: só a de maior prioridade deveria agir.
        Piloto piloto = criarPiloto(ENTRADA_BOX_INDEX - 500, 0);
        piloto.setBox(true);
        when(controleJogo.verificaEntradaBox(piloto)).thenReturn(true);

        // Satisfaz recentralizaSemTrafego() também (!isJogadorHumano() && diff > 250 em ambos os lados).
        when(controleJogo.obterCarroNaFrente(piloto)).thenReturn(null);
        when(controleJogo.obterCarroAtras(piloto)).thenReturn(null);
        when(controleJogo.obterCarroNaFrenteRetardatario(piloto, false)).thenReturn(null);
        when(controleJogo.calculaDiffParaProximoRetardatario(piloto, false)).thenReturn(300);
        when(controleJogo.calculaDiffParaProximoRetardatario(piloto, true)).thenReturn(300);
        when(controleJogo.calculaDiferencaParaAnterior(piloto)).thenReturn(300);
        when(controleJogo.calculaDiferencaParaProximo(piloto)).thenReturn(300);
        piloto.calculaCarrosAdjacentes();

        piloto.processaMudarTracado();

        assertEquals(2, piloto.getTracado(),
                "entrada no box tem prioridade mais alta — deveria mudar pro traçado do box (2), não recentralizar pro 0");
    }
}
