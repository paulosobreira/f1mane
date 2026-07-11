package br.f1mane.entidades;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.awt.Point;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;

import br.f1mane.controles.InterfaceJogo;

/**
 * Cobre o novo cálculo de velocidade real (km/h a partir do avanço de índice
 * relativo ao tamanho do circuito e à distância real em km), o fallback pra
 * circuitos sem distanciaKm informado, o teto com oscilação (370-375), e o
 * efeito artificial de reta sustentada — que sobe gradualmente até o teto
 * depois de 3s contínuos numa reta, mas só no valor exibido
 * (velocidadeExibir/calculaVelocidadeExibir), nunca na velocidade real
 * (velocidade/calculoVelocidade), usada por física, rede e efeitos visuais
 * (chuva).
 */
class PilotoCalculoVelocidadeTest {

    private InterfaceJogo controleJogo;

    private Piloto criarPiloto(int distanciaKm, int nosPorVolta, long cicloMs) {
        controleJogo = mock(InterfaceJogo.class);
        GameRandom random = new GameRandom(1);
        when(controleJogo.getRandom()).thenReturn(random);
        when(controleJogo.isChovendo()).thenReturn(false);
        when(controleJogo.isErs()).thenReturn(false);

        Circuito circuito = new Circuito();
        circuito.setDistanciaKm(distanciaKm);
        when(controleJogo.getCircuito()).thenReturn(circuito);

        List<No> nos = new ArrayList<>(Collections.nCopies(nosPorVolta, new No()));
        when(controleJogo.getNosDaPista()).thenReturn(nos);
        when(controleJogo.tempoCicloCircuito()).thenReturn(cicloMs);

        Piloto piloto = new Piloto();
        piloto.setControleJogo(controleJogo);
        Carro carro = new Carro();
        carro.setPiloto(piloto);
        carro.setPotencia(0);
        carro.setAerodinamica(0);
        carro.setFreios(0);
        carro.setPorcentagemCombustivel(50);
        piloto.setCarro(carro);
        piloto.setHabilidade(1000);

        No no = new No();
        no.setIndex(500);
        no.setPoint(new Point(500, 100));
        no.setTipo(No.RETA);
        piloto.setNoAtual(no);
        piloto.setAtivarDRS(false);
        piloto.setAtivarErs(false);
        piloto.setFreiandoReta(false);
        return piloto;
    }

    private void setGanhoMax(Piloto piloto, double ganhoMax) throws Exception {
        Field f = Piloto.class.getDeclaredField("ganhoMax");
        f.setAccessible(true);
        f.set(piloto, ganhoMax);
    }

    /**
     * Pula direto pro estado "reta sustentada ativa" (equivalente a já ter passado
     * dos 3s continuos em reta), sem precisar rodar os ciclos de aquecimento
     * (que disparariam a suavização normal e mudariam velocidadeExibir de forma
     * imprevisível antes do ponto que o teste quer observar).
     */
    private void ativarRetaSustentadaDireto(Piloto piloto) throws Exception {
        Field f = Piloto.class.getDeclaredField("tempoContinuoNaRetaMs");
        f.setAccessible(true);
        f.set(piloto, 3000L);
    }

    /** Espelha o que processaNovoIndex() faz de verdade: setVelocidade(calculoVelocidade(ganho)). */
    private int chamarCalculoVelocidade(Piloto piloto, double ganho) throws Exception {
        Method m = Piloto.class.getDeclaredMethod("calculoVelocidade", double.class);
        m.setAccessible(true);
        int velocidade = (int) m.invoke(piloto, ganho);
        piloto.setVelocidade(velocidade);
        return velocidade;
    }

    private int chamarCalculoVelocidadeReal(Piloto piloto, double ganho, int distanciaKm) throws Exception {
        Method m = Piloto.class.getDeclaredMethod("calculoVelocidadeReal", double.class, int.class);
        m.setAccessible(true);
        return (int) m.invoke(piloto, ganho, distanciaKm);
    }

    @Test
    void formulaReal_usaGanho_distanciaKm_nosPorVolta_e_tempoCiclo() throws Exception {
        // ganho=10, distanciaKm=1000m, nosPorVolta=100, ciclo=100ms
        // -> (10 * 1000 * 3600.0) / (100 * 100) = 3600
        Piloto piloto = criarPiloto(1000, 100, 100L);

        int velocidade = chamarCalculoVelocidadeReal(piloto, 10, 1000);

        assertEquals(3600, velocidade);
    }

    @Test
    void circuitosComDistanciasDiferentes_produzemVelocidadesProporcionais() throws Exception {
        Piloto piloto1km = criarPiloto(1000, 100, 100L);
        Piloto piloto2km = criarPiloto(2000, 100, 100L);

        int velocidade1 = chamarCalculoVelocidadeReal(piloto1km, 10, 1000);
        int velocidade2 = chamarCalculoVelocidadeReal(piloto2km, 10, 2000);

        assertEquals(velocidade1 * 2, velocidade2,
                "circuito com o dobro da distância deveria produzir o dobro da velocidade, pro mesmo ganho");
    }

    @Test
    void distanciaKmZero_mantemFormulaAntiga_semAtingirOTeto() throws Exception {
        Piloto piloto = criarPiloto(0, 100, 160L);
        setGanhoMax(piloto, 40.0);
        double ganho = 20.0;

        int val = 290;
        val += (21 - (50 / 100.0 / 5.0));
        double esperado = (val * ganho * 0.7 / 40.0) + ganho * 0.7;

        int velocidade = chamarCalculoVelocidade(piloto, ganho);

        assertEquals(Math.round(esperado), velocidade,
                "sem distanciaKm, o resultado deve bater com a fórmula antiga (fallback), abaixo do teto");
    }

    @Test
    void velocidadeCalculada_acimaDoTeto_nuncaUltrapassaTetoMaximo() throws Exception {
        // distanciaKm/nosPorVolta/ciclo escolhidos pra forçar um valor bem acima do teto
        Piloto piloto = criarPiloto(5000, 10, 100L);

        for (int i = 0; i < 20; i++) {
            int velocidade = chamarCalculoVelocidade(piloto, 30);
            assertTrue(velocidade <= Piloto.TETO_VELOCIDADE_MAX,
                    "velocidade nao deveria ultrapassar o teto maximo: " + velocidade);
            assertTrue(velocidade >= Piloto.TETO_VELOCIDADE_MIN,
                    "velocidade no teto nao deveria cair abaixo do piso da oscilacao: " + velocidade);
        }
    }

    @Test
    void velocidadeNoTeto_oscilaEntreMinEMax_aoLongoDeCiclosConsecutivos() throws Exception {
        Piloto piloto = criarPiloto(5000, 10, 100L);

        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;
        for (int i = 0; i < 20; i++) {
            int velocidade = chamarCalculoVelocidade(piloto, 30);
            min = Math.min(min, velocidade);
            max = Math.max(max, velocidade);
        }

        assertEquals(Piloto.TETO_VELOCIDADE_MIN, min);
        assertEquals(Piloto.TETO_VELOCIDADE_MAX, max);
    }

    @Test
    void doisPilotosNoTeto_oscilamDeFormaIndependente() throws Exception {
        Piloto piloto1 = criarPiloto(5000, 10, 100L);
        Piloto piloto2 = criarPiloto(5000, 10, 100L);

        // avanca piloto1 sozinho por alguns ciclos antes de comecar a avancar piloto2 junto,
        // pra garantir que as duas oscilacoes fiquem fora de fase uma da outra
        for (int i = 0; i < 3; i++) {
            chamarCalculoVelocidade(piloto1, 30);
        }

        boolean encontrouDiferenca = false;
        for (int i = 0; i < 20; i++) {
            int velocidade1 = chamarCalculoVelocidade(piloto1, 30);
            int velocidade2 = chamarCalculoVelocidade(piloto2, 30);
            if (velocidade1 != velocidade2) {
                encontrouDiferenca = true;
                break;
            }
        }

        assertTrue(encontrouDiferenca,
                "pilotos que atingem o teto em momentos diferentes deveriam oscilar fora de fase um do outro");
    }

    @Test
    void retaSustentada_apos3Segundos_ignoraVelocidadeRealESobeGradualmenteSoNoValorExibido() throws Exception {
        Piloto piloto = criarPiloto(0, 100, 1000L); // ciclo de 1000ms: cada chamada = 1 segundo em reta
        piloto.setVelocidade(0); // velocidade real fica parada em 0 o tempo todo (nunca mais setada neste teste)
        piloto.setVelocidadeExibir(300);

        piloto.calculaVelocidadeExibir(); // 1s continuo em reta
        int v1 = piloto.getVelocidadeExibir();
        piloto.calculaVelocidadeExibir(); // 2s continuo em reta
        int v2 = piloto.getVelocidadeExibir();
        piloto.calculaVelocidadeExibir(); // 3s continuo -> reta sustentada ativa
        int v3 = piloto.getVelocidadeExibir();
        piloto.calculaVelocidadeExibir(); // 4s continuo -> continua subindo
        int v4 = piloto.getVelocidadeExibir();

        assertTrue(v1 < 300 && v2 < v1,
                "abaixo dos 3s, velocidadeExibir deveria seguir a suavizacao normal em direcao a velocidade real (0), ou seja, caindo");
        assertEquals(v2 + 2, v3,
                "ao cruzar 3s continuos em reta, velocidadeExibir deveria subir +2 ignorando a velocidade real (0)");
        assertEquals(v3 + 2, v4, "uma vez em reta sustentada, cada ciclo soma +2 no valor exibido");
        assertEquals(0, piloto.getVelocidade(),
                "a velocidade real (usada por fisica/rede/efeitos) nunca deveria ser tocada pelo efeito de reta sustentada");
    }

    @Test
    void saindoDaReta_voltaImediatamenteARegraNormalNoValorExibido() throws Exception {
        Piloto piloto = criarPiloto(0, 100, 1000L);
        piloto.setVelocidade(0);
        piloto.setVelocidadeExibir(300);

        piloto.calculaVelocidadeExibir(); // 1s
        piloto.calculaVelocidadeExibir(); // 2s
        piloto.calculaVelocidadeExibir(); // 3s -> reta sustentada ativa
        int vEmRampa = piloto.getVelocidadeExibir();

        No curva = new No();
        curva.setIndex(600);
        curva.setPoint(new Point(600, 100));
        curva.setTipo(No.CURVA_ALTA);
        piloto.setNoAtual(curva);

        piloto.calculaVelocidadeExibir();
        int vAposCurva = piloto.getVelocidadeExibir();

        assertTrue(vAposCurva < vEmRampa,
                "ao entrar numa curva, velocidadeExibir deveria voltar a cair em direcao a velocidade real (0), nao continuar subindo +2");
    }

    @Test
    void tracadoDeFuga_nuncaAtivaRetaSustentada_mesmoComNoMarcadoComoReta() throws Exception {
        Piloto piloto = criarPiloto(0, 100, 1000L);
        piloto.setTracado(4); // traçado de fuga (escapada)
        piloto.setVelocidade(0);
        piloto.setVelocidadeExibir(300);

        piloto.calculaVelocidadeExibir();
        int v1 = piloto.getVelocidadeExibir();
        piloto.calculaVelocidadeExibir();
        piloto.calculaVelocidadeExibir();
        piloto.calculaVelocidadeExibir(); // 4o ciclo "no tipo reta", mas sempre na escapada
        int v4 = piloto.getVelocidadeExibir();

        assertEquals(v1 - 9, v4,
                "no traçado de fuga (4/5), a reta sustentada nunca deveria ativar: velocidadeExibir so cai (-3/ciclo) em direcao a velocidade real, nunca sobe +2");
    }

    @Test
    void zonaDeFrenagem_desativaRetaSustentada() throws Exception {
        Piloto piloto = criarPiloto(0, 100, 1000L);
        when(controleJogo.isNoZonaFrenagem(any())).thenReturn(true);
        piloto.setVelocidade(0);
        piloto.setVelocidadeExibir(300);

        piloto.calculaVelocidadeExibir();
        int v1 = piloto.getVelocidadeExibir();
        piloto.calculaVelocidadeExibir();
        piloto.calculaVelocidadeExibir();
        piloto.calculaVelocidadeExibir(); // 4s continuos em reta, mas sempre na zona de frenagem
        int v4 = piloto.getVelocidadeExibir();

        assertEquals(v1 - 9, v4,
                "na zona de frenagem, a reta sustentada nunca deveria ativar, mesmo com 4s continuos em reta: velocidadeExibir so cai (-3/ciclo)");
    }

    @Test
    void saindoDaZonaDeFrenagem_aRetaSustentadaAtivaImediatamenteSeOContadorJaPassouDoLimiar() throws Exception {
        Piloto piloto = criarPiloto(0, 100, 1000L);
        when(controleJogo.isNoZonaFrenagem(any())).thenReturn(true, true, true, false);
        piloto.setVelocidade(0);
        piloto.setVelocidadeExibir(300);

        piloto.calculaVelocidadeExibir(); // 1s, na zona de frenagem
        piloto.calculaVelocidadeExibir(); // 2s, na zona de frenagem
        piloto.calculaVelocidadeExibir(); // 3s, ainda na zona de frenagem -> nao ativa
        int v3 = piloto.getVelocidadeExibir();
        piloto.calculaVelocidadeExibir(); // 4s, fora da zona de frenagem -> ativa, contador ja acima do limiar
        int v4 = piloto.getVelocidadeExibir();

        assertEquals(v3 + 2, v4,
                "ao sair da zona de frenagem com o contador de reta continua ja acima do limiar, a rampa deveria ativar no mesmo ciclo");
    }

    @Test
    void incrementoContinuaCheioAbaixoDe300() throws Exception {
        Piloto piloto = criarPiloto(0, 100, 1000L);
        ativarRetaSustentadaDireto(piloto);
        piloto.setVelocidade(0);
        piloto.setVelocidadeExibir(250);

        piloto.calculaVelocidadeExibir();

        assertEquals(252, piloto.getVelocidadeExibir(),
                "abaixo de 300, o incremento da reta sustentada continua cheio (2 por ciclo)");
    }

    @Test
    void incrementoCaiParaUmEntre300e340() throws Exception {
        Piloto piloto = criarPiloto(0, 100, 1000L);
        ativarRetaSustentadaDireto(piloto);
        piloto.setVelocidade(0);
        piloto.setVelocidadeExibir(320);

        piloto.calculaVelocidadeExibir();

        assertEquals(321, piloto.getVelocidadeExibir(),
                "entre 300 e 340, o incremento da reta sustentada cai pra 1 por ciclo (metade do normal)");
    }

    @Test
    void incrementoFicaMuitoTenueAcimaDe340_soUmACadaTresCiclos() throws Exception {
        Piloto piloto = criarPiloto(0, 100, 1000L);
        ativarRetaSustentadaDireto(piloto);
        piloto.setVelocidade(0);
        piloto.setVelocidadeExibir(345);

        piloto.calculaVelocidadeExibir();
        int v1 = piloto.getVelocidadeExibir();
        piloto.calculaVelocidadeExibir();
        int v2 = piloto.getVelocidadeExibir();
        piloto.calculaVelocidadeExibir();
        int v3 = piloto.getVelocidadeExibir();

        assertEquals(345, v1, "acima de 340, a maioria dos ciclos nao incrementa velocidadeExibir");
        assertEquals(345, v2, "acima de 340, a maioria dos ciclos nao incrementa velocidadeExibir");
        assertEquals(346, v3, "a cada CICLOS_POR_INCREMENTO_MUITO_TENUE (3) ciclos, velocidadeExibir sobe 1 — o teto so e alcancado em retas bem longas");
    }
}
