package br.f1mane.entidades;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import br.f1mane.controles.InterfaceJogo;

/**
 * Cobre um bug relatado pelo usuário: suspeita de que um piloto à FRENTE
 * pudesse ser levado a entrar no traçado de fuga (4/5) só porque o piloto
 * ATRÁS dele estava, coincidentemente, escapando (tracado 4/5) naquele
 * momento — via a lógica defensiva de "espelhar o traçado do carro de trás"
 * em {@code Piloto.processaMudarTracado()} (ramo `mudouTracadoReta`, que
 * chama {@code mudarTracado(carroPilotoAtras.getPiloto().getTracado())} sem
 * nenhuma restrição a 0/1/2). Causa raiz confirmada: {@code mudarTracado()}
 * não tinha nenhum guard impedindo uma mudança NÃO forçada para o traçado 4
 * ou 5 partindo do traçado 1 ou 2 (só a origem 0 era bloqueada) — qualquer
 * lógica que copiasse o `getTracado()` de outro carro sem validar o valor
 * (aqui, ou também via a API do jogador em {@code ControleJogosServer})
 * podia colocar um piloto comum na geometria da escapada, sem nunca ter
 * passado pela mecânica oficial. Corrigido com um guard geral em
 * {@code mudarTracado()}: entrar em 4/5 exige {@code forcaMudar == true}.
 */
class PilotoMudarTracadoNaoEntraEmFugaTest {

    private static final int TAMANHO_PISTA = 1000;

    private InterfaceJogo controleJogo;
    private List<No> pista;
    private List<Piloto> pilotos;

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

        Circuito circuito = mock(Circuito.class);
        when(circuito.getIndiceTracado()).thenReturn(24.0);
        when(controleJogo.getCircuito()).thenReturn(circuito);
    }

    private Piloto criarPiloto(String nome, int index, int tracado) {
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

    // ---- guard direto em mudarTracado(): 4/5 exige forcaMudar ----

    @Test
    void mudarTracadoNaoForcado_deTracado1Para4_eBloqueado() {
        Piloto piloto = criarPiloto("Piloto", 100, 1);

        boolean mudou = piloto.mudarTracado(4);

        assertEquals(1, piloto.getTracado(), "sem forcaMudar, não deveria conseguir entrar no traçado de fuga 4 vindo do 1");
        assertEquals(false, mudou);
    }

    @Test
    void mudarTracadoNaoForcado_deTracado2Para5_eBloqueado() {
        Piloto piloto = criarPiloto("Piloto", 100, 2);

        boolean mudou = piloto.mudarTracado(5);

        assertEquals(2, piloto.getTracado(), "sem forcaMudar, não deveria conseguir entrar no traçado de fuga 5 vindo do 2");
        assertEquals(false, mudou);
    }

    @Test
    void mudarTracadoForcado_deTracado1Para5_continuaFuncionando() {
        // A mecânica oficial de escapada sempre chama com forcaMudar=true — o guard não pode
        // quebrar esse caminho legítimo.
        Piloto piloto = criarPiloto("Piloto", 100, 1);

        boolean mudou = piloto.mudarTracado(5, true);

        assertEquals(5, piloto.getTracado(), "com forcaMudar, a entrada no traçado de fuga continua permitida");
        assertEquals(true, mudou);
    }

    // ---- cenário relatado: piloto atrás escapando não arrasta o piloto da frente pro traçado de fuga ----

    private void configurarSemRetardatarioProximo(Piloto piloto) {
        // Valores bem acima de qualquer limiar usado em processaMudarTracado(), pra nenhum
        // outro ramo (retardatário à frente, defesa de posição) disparar antes do ramo
        // "mudouTracadoReta" sendo exercitado neste teste.
        when(controleJogo.obterCarroNaFrenteRetardatario(piloto, false)).thenReturn(null);
        when(controleJogo.calculaDiffParaProximoRetardatario(piloto, false)).thenReturn(300);
        when(controleJogo.calculaDiffParaProximoRetardatario(piloto, true)).thenReturn(300);
    }

    @Test
    void pilotoAtrasEscapando_naoArrastaPilotoDaFrenteParaOTracadoDeFuga() {
        Piloto frente = criarPiloto("Frente", 400, 1);
        Piloto atras = criarPiloto("Atras", 300, 5);
        frente.setHabilidade(1000);
        when(controleJogo.getRandom().nextDouble()).thenReturn(0.0);
        when(controleJogo.obterCarroAtras(frente)).thenReturn(atras.getCarro());
        when(controleJogo.calculaDiferencaParaAnterior(frente)).thenReturn(120);
        configurarSemRetardatarioProximo(frente);

        frente.calculaCarrosAdjacentes();
        frente.processaMudarTracado();

        assertEquals(1, frente.getTracado(),
                "o piloto da frente não deveria ser levado ao traçado de fuga só porque o piloto atrás estava escapando (tracado 5) nesse momento");
    }

    @Test
    void pilotoAtrasEmTracadoNormal_aindaConsegueAtrairPilotoDaFrenteParaOMesmoLado() {
        // Controle: confirma que o guard novo não quebrou o comportamento legítimo do ramo
        // "mudouTracadoReta" pra traçados normais (0/1/2) — só bloqueia 4/5. Frente parte do
        // traçado 0 (não do 1) porque mudarTracado já bloqueia a troca lateral direta 1<->2,
        // independente deste guard novo.
        Piloto frente = criarPiloto("Frente", 400, 0);
        Piloto atras = criarPiloto("Atras", 300, 1);
        frente.setHabilidade(1000);
        when(controleJogo.getRandom().nextDouble()).thenReturn(0.0);
        when(controleJogo.obterCarroAtras(frente)).thenReturn(atras.getCarro());
        when(controleJogo.calculaDiferencaParaAnterior(frente)).thenReturn(120);
        configurarSemRetardatarioProximo(frente);

        frente.calculaCarrosAdjacentes();
        frente.processaMudarTracado();

        assertEquals(1, frente.getTracado(), "pra traçados normais, o piloto da frente ainda deveria espelhar o traçado do piloto atrás");
    }
}
