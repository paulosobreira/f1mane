package br.flmane.entidades;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import br.flmane.controles.ControleEscapada;
import br.flmane.controles.InterfaceJogo;

/**
 * Cobre a derrapagem (traçado 0 → 1/2): independente de {@code stress}/
 * {@code modoPilotagem} (ao contrário da escapada ancorada, ver
 * {@code PilotoEscapadaAncoradaTracadoTest}), dispara só por pneus abaixo de
 * 30% + falha no teste de habilidade de freios, em curva baixa ou alta, no
 * traçado 0.
 */
class PilotoDerrapagemTest {

    private InterfaceJogo controleJogo;
    private ControleEscapada controleEscapada;
    private List<No> pista;
    private List<Piloto> pilotos;
    private Circuito circuito;

    @BeforeEach
    void setUp() {
        pista = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            No no = new No();
            no.setIndex(i);
            no.setPoint(new Point(i, i));
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
        when(random.intervalo(1, 2)).thenReturn(1);

        circuito = mock(Circuito.class);
        when(circuito.getIndiceTracado()).thenReturn(24.0);
        when(circuito.getIndiceTracadoForaPista()).thenReturn(84.0);
        when(circuito.getMultiplicadorLarguraPista()).thenReturn(1.0);
        when(circuito.getObjetos()).thenReturn(new ArrayList<>());
        when(controleJogo.getCircuito()).thenReturn(circuito);

        controleEscapada = new ControleEscapada(controleJogo);
    }

    private Piloto criarPiloto(java.awt.Color tipoDoNo) {
        Piloto piloto = new Piloto();
        piloto.setNome("Piloto");
        Carro carro = new Carro();
        carro.setPiloto(piloto);
        // Pneus "novos" por padrão — testes que exercitam a regra de pneus<30% setam
        // explicitamente um valor baixo.
        carro.setPorcentagemDesgastePneus(100);
        // Freios altos por padrão — sem isso, testeHabilidadePilotoFreios() falha sempre
        // (Carro.testeFreios() fica em 0 por padrão), mascarando cenários de sucesso no teste de
        // habilidade. Testes que querem exercitar o caminho de falha zeram explicitamente.
        carro.setFreios(1000);
        piloto.setCarro(carro);
        piloto.setControleJogo(controleJogo);
        No no = pista.get(10);
        no.setTipo(tipoDoNo);
        piloto.setNoAtual(no);
        piloto.setTracado(0);
        pilotos.add(piloto);
        return piloto;
    }

    private void registrarEscapada(int tracadoOrigem, int indiceEntrada, int indiceSaida) {
        ObjetoEscapada escapada = new ObjetoEscapada();
        escapada.setTracadoOrigem(tracadoOrigem);
        escapada.setIndiceEntrada(indiceEntrada);
        escapada.setIndiceSaida(indiceSaida);
        List<ObjetoPista> objetos = new ArrayList<>();
        objetos.add(escapada);
        when(circuito.getObjetos()).thenReturn(objetos);
    }

    @Test
    void curvaBaixa_pneusBaixos_falhaNoTesteDeFreios_derrapaParaTracado1Ou2() {
        Piloto piloto = criarPiloto(No.CURVA_BAIXA);
        piloto.getCarro().setPorcentagemDesgastePneus(15);
        // freios/habilidade padrão (0): testeHabilidadePilotoFreios() falha sempre.

        controleEscapada.processaDerrapagem(piloto);

        assertEquals(1, piloto.getTracado(), "curva baixa + pneus baixos + falha no teste de freios deveria derrapar");
        verify(controleJogo, times(1)).travouRodas(piloto);
    }

    @Test
    void curvaAlta_pneusBaixos_falhaNoTesteDeFreios_tambemDerapa() {
        Piloto piloto = criarPiloto(No.CURVA_ALTA);
        piloto.getCarro().setPorcentagemDesgastePneus(15);

        controleEscapada.processaDerrapagem(piloto);

        assertEquals(1, piloto.getTracado(), "curva alta também deveria derrapar, do mesmo jeito que curva baixa");
    }

    @Test
    void pneusAcimaDe30PorCento_naoDerrapa_mesmoFalhandoNoTeste() {
        Piloto piloto = criarPiloto(No.CURVA_BAIXA);
        piloto.getCarro().setPorcentagemDesgastePneus(50);

        controleEscapada.processaDerrapagem(piloto);

        assertEquals(0, piloto.getTracado(), "pneus acima de 30% não deveriam derrapar, mesmo com falha no teste de freios");
    }

    @Test
    void pneusExatosEm30PorCento_naoDerrapa() {
        Piloto piloto = criarPiloto(No.CURVA_BAIXA);
        piloto.getCarro().setPorcentagemDesgastePneus(30);

        controleEscapada.processaDerrapagem(piloto);

        assertEquals(0, piloto.getTracado(), "pneus exatamente em 30% não deveriam satisfazer a pré-condição (exige < 30)");
    }

    @Test
    void sucessoNoTesteDeFreios_evitaADerrapagem() {
        Piloto piloto = criarPiloto(No.CURVA_BAIXA);
        piloto.getCarro().setPorcentagemDesgastePneus(15);
        piloto.getCarro().setFreios(1000);
        piloto.setHabilidade(1000);
        when(controleJogo.getRandom().nextDouble()).thenReturn(0.0);

        controleEscapada.processaDerrapagem(piloto);

        assertEquals(0, piloto.getTracado(), "sucesso no teste de freios deveria evitar a derrapagem");
    }

    @Test
    void foraDeCurva_reta_naoDerrapa() {
        Piloto piloto = criarPiloto(No.RETA);
        piloto.getCarro().setPorcentagemDesgastePneus(15);

        controleEscapada.processaDerrapagem(piloto);

        assertEquals(0, piloto.getTracado(), "fora de curva (reta) não deveria derrapar, mesmo com pneus baixos e falha no teste");
    }

    @Test
    void independenciaDeStressEModoDePilotagem_derrapaEmNormalSemStress() {
        // Diferente da escapada ancorada, a derrapagem não depende de stress/modo.
        Piloto piloto = criarPiloto(No.CURVA_BAIXA);
        piloto.getCarro().setPorcentagemDesgastePneus(15);
        piloto.setModoPilotagem(Piloto.NORMAL);
        piloto.setStress(0);

        controleEscapada.processaDerrapagem(piloto);

        assertEquals(1, piloto.getTracado(), "derrapagem deveria disparar mesmo em modo NORMAL e stress zero");
    }

    @Test
    void escolhaDeLado_comEscapadaAFrenteNoTracado2_vaiParaTracado2() {
        Piloto piloto = criarPiloto(No.CURVA_BAIXA);
        piloto.getCarro().setPorcentagemDesgastePneus(15);
        registrarEscapada(2, 20, 40);

        controleEscapada.processaDerrapagem(piloto);

        assertEquals(2, piloto.getTracado(), "com escapada à frente ancorada no traçado 2, deveria derrapar pra esse lado");
    }

    @Test
    void escolhaDeLado_comEscapadaAFrenteNoTracado1_vaiParaTracado1() {
        Piloto piloto = criarPiloto(No.CURVA_BAIXA);
        piloto.getCarro().setPorcentagemDesgastePneus(15);
        registrarEscapada(1, 20, 40);

        controleEscapada.processaDerrapagem(piloto);

        assertEquals(1, piloto.getTracado(), "com escapada à frente ancorada no traçado 1, deveria derrapar pra esse lado");
    }

    @Test
    void escolhaDeLado_semEscapadaAFrenteNoCircuito_sorteia() {
        Piloto piloto = criarPiloto(No.CURVA_BAIXA);
        piloto.getCarro().setPorcentagemDesgastePneus(15);
        // circuito.getObjetos() vazio por padrão (setUp) — nenhuma escapada à frente.

        controleEscapada.processaDerrapagem(piloto);

        assertEquals(1, piloto.getTracado(), "sem escapada à frente no circuito, o lado deveria vir do sorteio (mockado pra 1)");
        verify(controleJogo.getRandom(), times(1)).intervalo(1, 2);
    }

    @Test
    void safetyCarNaPista_naoDerrapa() {
        when(controleJogo.isSafetyCarNaPista()).thenReturn(true);
        Piloto piloto = criarPiloto(No.CURVA_BAIXA);
        piloto.getCarro().setPorcentagemDesgastePneus(15);

        controleEscapada.processaDerrapagem(piloto);

        assertEquals(0, piloto.getTracado(), "com safety car na pista, a derrapagem não deveria disparar");
    }

    @Test
    void modoQualify_naoDerrapa() {
        when(controleJogo.isModoQualify()).thenReturn(true);
        Piloto piloto = criarPiloto(No.CURVA_BAIXA);
        piloto.getCarro().setPorcentagemDesgastePneus(15);

        controleEscapada.processaDerrapagem(piloto);

        assertEquals(0, piloto.getTracado(), "em modo qualify, a derrapagem não deveria disparar");
    }

    @Test
    void emRotaDeBox_naoDerrapa() {
        Piloto piloto = criarPiloto(No.CURVA_BAIXA);
        piloto.getCarro().setPorcentagemDesgastePneus(15);
        piloto.setPtosBox(5);

        controleEscapada.processaDerrapagem(piloto);

        assertEquals(0, piloto.getTracado(), "piloto em rota de box não deveria derrapar");
    }

    @Test
    void naoForcado_animacaoEmAndamento_naoDerrapaAindaNesseCiclo() {
        // mudarTracado NÃO forçado já adia sozinho enquanto uma troca anterior está em
        // andamento (indiceTracado != 0) — a derrapagem não precisa de checagem própria.
        Piloto piloto = criarPiloto(No.CURVA_BAIXA);
        piloto.getCarro().setPorcentagemDesgastePneus(15);
        piloto.setIndiceTracado(10);

        controleEscapada.processaDerrapagem(piloto);

        assertEquals(0, piloto.getTracado(), "com animação de troca em andamento, a derrapagem deveria ser adiada, não aplicada nesse ciclo");
    }
}
