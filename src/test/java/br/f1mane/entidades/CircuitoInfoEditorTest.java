package br.f1mane.entidades;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * Cobre {@code ciclo} (milissegundos por tick, movido de circuitos.properties
 * para o próprio Circuito), {@code distanciaKm} (informada pelo usuário, não
 * calculada) e a estimativa de tempo de volta a partir de ciclo + contagem de
 * nós de pistaFull por tipo.
 */
class CircuitoInfoEditorTest {

    @Test
    void ciclo_padraoEhCentoESessenta() {
        Circuito circuito = new Circuito();

        assertEquals(160, circuito.getCiclo());
    }

    @Test
    void setCiclo_gravaValorInformado() {
        Circuito circuito = new Circuito();

        circuito.setCiclo(200);

        assertEquals(200, circuito.getCiclo());
    }

    @Test
    void distanciaKm_padraoEhZero() {
        Circuito circuito = new Circuito();

        assertEquals(0, circuito.getDistanciaKm());
    }

    @Test
    void setDistanciaKm_gravaValorInformado_semSerAlteradaPorVetorizarPista() {
        Circuito circuito = circuitoDeTeste();

        circuito.setDistanciaKm(5);
        circuito.vetorizarPista();

        assertEquals(5, circuito.getDistanciaKm(),
                "distanciaKm é informada, não deveria mudar ao revetorizar o traçado");
    }

    @Test
    void estimarTempoVoltaMs_dobrarOCicloDobraOTempoEstimado() {
        Circuito circuito = circuitoDeTeste();
        circuito.setCiclo(100);

        long estimativaCicloCem = circuito.estimarTempoVoltaMs();
        assertTrue(estimativaCicloCem > 0, "circuito com traçado real deveria ter uma estimativa positiva");

        circuito.setCiclo(200);
        long estimativaCicloDuzentos = circuito.estimarTempoVoltaMs();

        // Math.round por chamada pode introduzir ±1ms de arredondamento —
        // não precisa ser exato, só refletir a mesma ordem de grandeza.
        assertTrue(Math.abs(estimativaCicloCem * 2 - estimativaCicloDuzentos) <= 1,
                "dobrar ciclo deveria dobrar (± arredondamento de 1ms) o tempo de volta estimado, pra mesma contagem de ticks");
    }

    @Test
    void estimarTempoVoltaMs_maisNosDeCurvaBaixa_aumentaOTempoEstimado() {
        // Curva baixa tem o menor ganho médio (mais ticks por nó) — um
        // traçado com mais nós de curva baixa deveria estimar tempo maior
        // que um com a mesma quantidade total de nós, mas todos de reta.
        Circuito circuitoComCurvaBaixa = circuitoDeTeste();
        circuitoComCurvaBaixa.setCiclo(150);
        long estimativaComCurva = circuitoComCurvaBaixa.estimarTempoVoltaMs();

        Circuito circuitoSoReta = new Circuito();
        List<No> pista = new ArrayList<>();
        No n1 = new No();
        n1.setPoint(new Point(0, 0));
        n1.setTipo(No.LARGADA);
        pista.add(n1);
        No n2 = new No();
        n2.setPoint(new Point(1000, 0));
        n2.setTipo(No.RETA);
        pista.add(n2);
        No n3 = new No();
        n3.setPoint(new Point(1000, 1000));
        n3.setTipo(No.RETA);
        pista.add(n3);
        No n4 = new No();
        n4.setPoint(new Point(0, 1000));
        n4.setTipo(No.RETA);
        pista.add(n4);
        circuitoSoReta.setPista(pista);
        circuitoSoReta.setBox(boxDeTeste());
        circuitoSoReta.vetorizarPista();
        circuitoSoReta.setCiclo(150);
        long estimativaSoReta = circuitoSoReta.estimarTempoVoltaMs();

        assertTrue(estimativaComCurva > estimativaSoReta,
                "traçado com curva alta/baixa deveria estimar tempo de volta maior que um traçado só de reta, mesmo ciclo");
    }

    /**
     * Loop grande o suficiente (e box deslocado para o meio de um dos lados,
     * não perto do nó inicial) para vetorizarPista() não estourar índice
     * negativo perto das bordas do traçado interpolado.
     */
    private Circuito circuitoDeTeste() {
        Circuito circuito = new Circuito();
        List<No> pista = new ArrayList<>();
        No n1 = new No();
        n1.setPoint(new Point(0, 0));
        n1.setTipo(No.LARGADA);
        pista.add(n1);
        No n2 = new No();
        n2.setPoint(new Point(1000, 0));
        n2.setTipo(No.RETA);
        pista.add(n2);
        No n3 = new No();
        n3.setPoint(new Point(1000, 1000));
        n3.setTipo(No.CURVA_ALTA);
        pista.add(n3);
        No n4 = new No();
        n4.setPoint(new Point(0, 1000));
        n4.setTipo(No.CURVA_BAIXA);
        pista.add(n4);
        circuito.setPista(pista);
        circuito.setBox(boxDeTeste());
        circuito.vetorizarPista();
        return circuito;
    }

    private List<No> boxDeTeste() {
        List<No> box = new ArrayList<>();
        No b1 = new No();
        b1.setPoint(new Point(1000, 400));
        b1.setTipo(No.BOX);
        box.add(b1);
        No b2 = new No();
        b2.setPoint(new Point(1000, 500));
        b2.setTipo(No.BOX);
        box.add(b2);
        No b3 = new No();
        b3.setPoint(new Point(1000, 600));
        b3.setTipo(No.BOX);
        box.add(b3);
        return box;
    }
}
