package br.f1mane.entidades;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.junit.jupiter.api.Test;

import br.f1mane.controles.InterfaceJogo;

/**
 * Validação estatística (Monte Carlo) de que, em curvas, "molhado%" = 1.0
 * produz ganho médio menor que "molhado%" = 0.0 — não uma garantia por tick
 * (os sorteios continuam probabilísticos, ver design.md D5 e o requirement
 * "Chuva não pode, em expectativa, produzir ganho maior que condição seca em
 * curvas"), mas uma tendência que se confirma sobre muitas amostras. Reta
 * fora de zona de frenagem e frenagem-antes-de-curva-alta continuam sem
 * qualquer diferença entre seco e chuva, como já é hoje (não é escopo desta
 * change fechar essas lacunas).
 *
 * Usa GameRandom real com seed fixa (não a seed=1 especial, que force
 * nextDouble()=0.5 sempre) para ter variabilidade genuína entre amostras,
 * mantendo o teste 100% reprodutível.
 */
class GanhoMolhadoEstatisticoTest {

    private static final int AMOSTRAS = 3000;
    private static final long SEED = 12345L;

    // --- Carro.calculaModificadorPneu ---

    private Carro criarCarroParaPneu(double molhado) {
        InterfaceJogo controleJogo = mock(InterfaceJogo.class);
        when(controleJogo.getRandom()).thenReturn(new GameRandom(SEED));
        when(controleJogo.getMolhado()).thenReturn(molhado);
        when(controleJogo.verificaPistaEmborrachada()).thenReturn(false);
        when(controleJogo.isChovendo()).thenReturn(false);

        Piloto piloto = new Piloto();
        piloto.setHabilidade(500); // 50% de chance nos testes de habilidade
        Carro carro = new Carro();
        carro.setPiloto(piloto);
        piloto.setCarro(carro);
        piloto.setControleJogo(controleJogo);
        carro.setTipoPneu(Carro.TIPO_PNEU_MOLE);
        carro.setPorcentagemDesgastePneus(50);
        carro.setFreios(500); // 50% de chance em testeFreios()
        return carro;
    }

    private double invocaCalculaModificadorPneu(Carro carro, No no) throws Exception {
        Method m = Carro.class.getDeclaredMethod("calculaModificadorPneu", double.class, No.class);
        m.setAccessible(true);
        return (double) m.invoke(carro, 100.0, no);
    }

    private double mediaCalculaModificadorPneu(double molhado, No no) throws Exception {
        Carro carro = criarCarroParaPneu(molhado);
        double soma = 0;
        for (int i = 0; i < AMOSTRAS; i++) {
            soma += invocaCalculaModificadorPneu(carro, no);
        }
        return soma / AMOSTRAS;
    }

    @Test
    void calculaModificadorPneu_curvaBaixa_mediaMolhadaMenorQueMediaSeca() throws Exception {
        No curvaBaixa = new No();
        curvaBaixa.setIndex(10);
        curvaBaixa.setTipo(No.CURVA_BAIXA);

        double mediaSeca = mediaCalculaModificadorPneu(0.0, curvaBaixa);
        double mediaMolhada = mediaCalculaModificadorPneu(1.0, curvaBaixa);

        assertTrue(mediaMolhada < mediaSeca,
                "media molhada (" + mediaMolhada + ") deveria ser menor que a media seca (" + mediaSeca + ")");
    }

    @Test
    void calculaModificadorPneu_reta_semDiferencaEntreSecoEMolhado() throws Exception {
        No reta = new No();
        reta.setIndex(10);
        reta.setTipo(No.RETA);

        double mediaSeca = mediaCalculaModificadorPneu(0.0, reta);
        double mediaMolhada = mediaCalculaModificadorPneu(1.0, reta);

        assertEquals(mediaSeca, mediaMolhada, 1e-9, "em reta, molhado% nao deveria fazer nenhuma diferenca");
    }

    // --- Piloto.calculaModificadorPrincipal ---

    private Piloto criarPilotoParaPrincipal(double molhado, No no) {
        InterfaceJogo controleJogo = mock(InterfaceJogo.class);
        when(controleJogo.getRandom()).thenReturn(new GameRandom(SEED));
        when(controleJogo.getMolhado()).thenReturn(molhado);

        Piloto piloto = new Piloto();
        piloto.setHabilidade(500);
        Carro carro = new Carro();
        carro.setPiloto(piloto);
        piloto.setCarro(carro);
        piloto.setControleJogo(controleJogo);
        piloto.setNoAtual(no);
        return piloto;
    }

    private int invocaCalculaModificadorPrincipal(Piloto piloto) throws Exception {
        Method m = Piloto.class.getDeclaredMethod("calculaModificadorPrincipal");
        m.setAccessible(true);
        return (int) m.invoke(piloto);
    }

    private double mediaCalculaModificadorPrincipal(double molhado, No no) throws Exception {
        Piloto piloto = criarPilotoParaPrincipal(molhado, no);
        long soma = 0;
        for (int i = 0; i < AMOSTRAS; i++) {
            soma += invocaCalculaModificadorPrincipal(piloto);
        }
        return ((double) soma) / AMOSTRAS;
    }

    @Test
    void calculaModificadorPrincipal_curvaBaixa_mediaMolhadaMenorQueMediaSeca() throws Exception {
        No curvaBaixa = new No();
        curvaBaixa.setIndex(10);
        curvaBaixa.setTipo(No.CURVA_BAIXA);

        double mediaSeca = mediaCalculaModificadorPrincipal(0.0, curvaBaixa);
        double mediaMolhada = mediaCalculaModificadorPrincipal(1.0, curvaBaixa);

        assertTrue(mediaMolhada < mediaSeca,
                "media molhada (" + mediaMolhada + ") deveria ser menor que a media seca (" + mediaSeca + ")");
    }

    @Test
    void calculaModificadorPrincipal_reta_semDiferencaEntreSecoEMolhado() throws Exception {
        No reta = new No();
        reta.setIndex(10);
        reta.setTipo(No.RETA);

        double mediaSeca = mediaCalculaModificadorPrincipal(0.0, reta);
        double mediaMolhada = mediaCalculaModificadorPrincipal(1.0, reta);

        assertEquals(mediaSeca, mediaMolhada, 1e-9, "em reta, molhado% nao deveria fazer nenhuma diferenca");
    }

    // --- Piloto.processaFreioNaReta ---

    private Piloto criarPilotoParaFreioNaReta(double molhado, No proximaCurva) throws Exception {
        InterfaceJogo controleJogo = mock(InterfaceJogo.class);
        when(controleJogo.getRandom()).thenReturn(new GameRandom(SEED));
        when(controleJogo.getMolhado()).thenReturn(molhado);

        No noReta = new No();
        noReta.setIndex(100);
        noReta.setTipo(No.RETA);

        when(controleJogo.obterProxCurva(noReta)).thenReturn(proximaCurva);
        when(controleJogo.isNoZonaFrenagem(noReta)).thenReturn(true);

        Piloto piloto = new Piloto();
        piloto.setHabilidade(500);
        Carro carro = new Carro();
        carro.setPiloto(piloto);
        piloto.setCarro(carro);
        carro.setFreios(500);
        piloto.setControleJogo(controleJogo);
        piloto.setNoAtual(noReta);

        for (String nomeCampo : new String[] { "calculaDiffParaProximoRetardatario",
                "calculaDiffParaProximoRetardatarioMesmoTracado" }) {
            Field campo = Piloto.class.getDeclaredField(nomeCampo);
            campo.setAccessible(true);
            campo.setInt(piloto, 200);
        }
        return piloto;
    }

    private double mediaProcessaFreioNaReta(double molhado, No proximaCurva) throws Exception {
        double soma = 0;
        for (int i = 0; i < AMOSTRAS; i++) {
            Piloto piloto = criarPilotoParaFreioNaReta(molhado, proximaCurva);
            piloto.setGanho(100);
            piloto.processaFreioNaReta();
            soma += piloto.getGanho();
        }
        return soma / AMOSTRAS;
    }

    @Test
    void processaFreioNaReta_proximaCurvaBaixa_mediaMolhadaMenorQueMediaSeca() throws Exception {
        No curvaBaixa = new No();
        curvaBaixa.setIndex(150);
        curvaBaixa.setTipo(No.CURVA_BAIXA);

        double mediaSeca = mediaProcessaFreioNaReta(0.0, curvaBaixa);
        double mediaMolhada = mediaProcessaFreioNaReta(1.0, curvaBaixa);

        assertTrue(mediaMolhada < mediaSeca,
                "media molhada (" + mediaMolhada + ") deveria ser menor que a media seca (" + mediaSeca + ")");
    }

    @Test
    void processaFreioNaReta_proximaCurvaAlta_semDiferencaEntreSecoEMolhado() throws Exception {
        No curvaAlta = new No();
        curvaAlta.setIndex(150);
        curvaAlta.setTipo(No.CURVA_ALTA);

        double mediaSeca = mediaProcessaFreioNaReta(0.0, curvaAlta);
        double mediaMolhada = mediaProcessaFreioNaReta(1.0, curvaAlta);

        assertEquals(mediaSeca, mediaMolhada, 1e-9,
                "frenagem antes de curva alta nao entra no bloco de reducao de ganho, molhado% nao deveria fazer diferenca");
    }
}
