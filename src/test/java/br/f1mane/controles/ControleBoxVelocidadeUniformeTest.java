package br.f1mane.controles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.awt.Point;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import br.f1mane.entidades.Carro;
import br.f1mane.entidades.Circuito;
import br.f1mane.entidades.GameRandom;
import br.f1mane.entidades.No;
import br.f1mane.entidades.Piloto;

/**
 * Cobre a remoção do sorteio boxRapido/boxLento: o box sempre incrementa o
 * progresso/velocidade pelo mesmo valor (média dos antigos rápido/lento),
 * independentemente de rodar a "corrida" (o `ControleBox`) mais de uma vez.
 */
class ControleBoxVelocidadeUniformeTest {

    private void setField(Object alvo, String nome, Object valor) throws Exception {
        Field campo = alvo.getClass().getDeclaredField(nome);
        campo.setAccessible(true);
        campo.set(alvo, valor);
    }

    private No criarNo(int index, boolean box, boolean reta) {
        No no = new No();
        no.setIndex(index);
        no.setPoint(new Point(index, 0));
        no.setBox(box);
        no.setTipo(reta ? No.RETA : No.CURVA_BAIXA);
        return no;
    }

    /** Roda um ciclo de `processarPilotoBox` (nó de reta, fora da janela de entrada) e retorna o incremento de ptosBox observado. */
    private int rodarUmCicloEObterIncremento() throws Exception {
        InterfaceJogo controleJogo = mock(InterfaceJogo.class);
        Circuito circuito = mock(Circuito.class);
        when(circuito.getEntradaBoxIndex()).thenReturn(5000);
        when(circuito.getLadoBox()).thenReturn(1);
        when(controleJogo.getCircuito()).thenReturn(circuito);

        List<No> pistaGenerica = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            pistaGenerica.add(criarNo(i, false, true));
        }
        when(controleJogo.obterPista(any())).thenReturn(pistaGenerica);

        List<No> nosDoBox = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            nosDoBox.add(criarNo(i, true, true));
        }
        when(controleJogo.getNosDoBox()).thenReturn(nosDoBox);

        GameRandom random = mock(GameRandom.class);
        when(random.intervalo(50, 60)).thenReturn(0);
        when(controleJogo.getRandom()).thenReturn(random);

        Piloto piloto = new Piloto();
        piloto.setNome("Piloto");
        Carro carro = new Carro();
        carro.setNome("Carro");
        carro.setPiloto(piloto);
        piloto.setCarro(carro);
        piloto.setControleJogo(controleJogo);
        piloto.setTracado(0);
        piloto.setPtosBox(50);
        // Nó atual de reta, fora de qualquer box — cai no ramo "verificaRetaOuLargada()".
        piloto.setNoAtual(criarNo(2000, false, true));

        Map<Carro, No> boxEquipes = new HashMap<>();
        boxEquipes.put(carro, criarNo(1, true, true));

        ControleBox controleBox = new ControleBox();
        setField(controleBox, "controleJogo", controleJogo);
        setField(controleBox, "circuito", circuito);
        setField(controleBox, "boxEquipes", boxEquipes);
        setField(controleBox, "paradaBox", criarNo(9000, true, true));

        int ptosBoxAntes = piloto.getPtosBox();
        controleBox.processarPilotoBox(piloto);
        return piloto.getPtosBox() - ptosBoxAntes;
    }

    @Test
    void incrementoDeBoxEmRetaEhSempreOMesmo_naoDependeDeSorteio() throws Exception {
        int incremento1 = rodarUmCicloEObterIncremento();
        int incremento2 = rodarUmCicloEObterIncremento();
        int incremento3 = rodarUmCicloEObterIncremento();

        assertEquals(incremento1, incremento2, "o incremento não deveria variar entre execuções (sem sorteio)");
        assertEquals(incremento1, incremento3, "o incremento não deveria variar entre execuções (sem sorteio)");
        assertEquals(23, incremento1, "valor médio entre o antigo boxRapido (25) e boxLento (20) para reta/largada");
    }

    private boolean rodarProcessaIaIrBoxComCorridaPorcentagem(int corridaPorcentagem) throws Exception {
        ControleJogoLocal controleJogo = mock(ControleJogoLocal.class);
        when(controleJogo.isModoQualify()).thenReturn(false);
        when(controleJogo.isSafetyCarNaPista()).thenReturn(true);
        when(controleJogo.porcentagemCorridaConcluida()).thenReturn(corridaPorcentagem);

        Piloto piloto = new Piloto();
        piloto.setControleJogo(controleJogo);
        Carro carro = new Carro();
        carro.setPiloto(piloto);
        piloto.setCarro(carro);
        // Pneu abaixo de 50%: com safety car na pista, isso sozinho já marca box=true.
        carro.setPorcentagemDesgastePneus(30);
        piloto.setQtdeParadasBox(1);

        ControleAutomacao controleAutomacao = new ControleAutomacao(controleJogo, null);
        java.lang.reflect.Method metodo = ControleAutomacao.class.getDeclaredMethod("processaIaIrBox", Piloto.class);
        metodo.setAccessible(true);
        metodo.invoke(controleAutomacao, piloto);
        return piloto.isBox();
    }

    @Test
    void limiteUltimasVoltasParaDecidirParada_ehUnico_83() throws Exception {
        // Único valor (média entre os antigos 80/85) — abaixo dele, decisão de parada tardia não é revertida.
        assertEquals(true, rodarProcessaIaIrBoxComCorridaPorcentagem(82),
                "com a corrida em 82% (abaixo do limite de 83), box não deveria ser revertido");
        assertEquals(false, rodarProcessaIaIrBoxComCorridaPorcentagem(84),
                "com a corrida em 84% (acima do limite de 83), box deveria ser revertido para false");
    }
}
