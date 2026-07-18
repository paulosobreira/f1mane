package br.flmane.entidades;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Point;
import java.awt.Rectangle;
import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * Cobre o modelo vetorial de ObjetoLivre: hastes neutras produzem o mesmo
 * resultado do polígono reto legado; uma haste ajustada produz uma curva
 * visivelmente diferente; e a compatibilidade com circuitos legados que só
 * têm `pontos` (sem `vertices`).
 */
class ObjetoLivreTest {

    private static final List<Point> TRIANGULO = Arrays.asList(
            new Point(0, 0), new Point(100, 0), new Point(50, 100));

    @Test
    void gerar_semHastes_produzTrianguloRetoEquivalenteAoLegado() {
        ObjetoLivre objetoLivre = new ObjetoLivre();
        objetoLivre.setPontos(new ArrayList<>(TRIANGULO));

        objetoLivre.gerar();

        assertEquals(new Rectangle(0, 0, 100, 100), objetoLivre.getForma().getBounds());
        assertTrue(objetoLivre.getForma().contains(50, 50), "ponto interno ao triângulo deveria estar dentro da forma");
        assertFalse(objetoLivre.getForma().contains(5, 90), "ponto fora do triângulo não deveria estar dentro da forma");
    }

    @Test
    void gerar_naoMigraPontosParaVerticesSozinho() {
        ObjetoLivre objetoLivre = new ObjetoLivre();
        objetoLivre.setPontos(new ArrayList<>(TRIANGULO));

        objetoLivre.gerar();

        assertTrue(objetoLivre.getVertices().isEmpty(),
                "desenhar um objeto legado não deveria por si só migrar pontos para vertices");
    }

    @Test
    void gerar_comHasteAjustada_curvaSaiDoRetanguloDoPoligonoReto() {
        ObjetoLivre reto = new ObjetoLivre();
        reto.setPontos(new ArrayList<>(TRIANGULO));
        reto.gerar();

        ObjetoLivre curvo = new ObjetoLivre();
        List<PontoCurva> vertices = new ArrayList<>();
        PontoCurva v0 = new PontoCurva(new Point(0, 0));
        v0.setHasteFim(new Point(30, -40));
        vertices.add(v0);
        vertices.add(new PontoCurva(new Point(100, 0)));
        vertices.add(new PontoCurva(new Point(50, 100)));
        curvo.setVertices(vertices);
        curvo.gerar();

        // o triângulo reto nunca sai de y=0..100 (bounds.y == 0)
        assertTrue(reto.getForma().getBounds().y >= 0);
        // a curva de (0,0) até (100,0) com controle em (30,-40) dobra para y
        // negativo (minimo matemático da cúbica em y ~= -17.8) — sai do
        // retângulo do polígono reto original.
        assertTrue(curvo.getForma().getBounds().y < 0,
                "esperava que a haste ajustada fizesse a curva sair do retângulo do polígono reto");
    }

    @Test
    void inicializarVerticesSeNecessario_copiaPontosLegadosParaVertices() {
        ObjetoLivre objetoLivre = new ObjetoLivre();
        objetoLivre.setPontos(new ArrayList<>(TRIANGULO));

        objetoLivre.inicializarVerticesSeNecessario();

        assertEquals(3, objetoLivre.getVertices().size());
        assertEquals(new Point(0, 0), objetoLivre.getVertices().get(0).getPosicao());
        assertEquals(new Point(100, 0), objetoLivre.getVertices().get(1).getPosicao());
        assertEquals(new Point(50, 100), objetoLivre.getVertices().get(2).getPosicao());

        // hastes recém-migradas continuam neutras: o desenho não muda
        objetoLivre.gerar();
        assertEquals(new Rectangle(0, 0, 100, 100), objetoLivre.getForma().getBounds());
    }

    @Test
    void inicializarVerticesSeNecessario_naoSobrescreveVerticesJaExistentes() {
        ObjetoLivre objetoLivre = new ObjetoLivre();
        objetoLivre.setPontos(new ArrayList<>(TRIANGULO));
        List<PontoCurva> verticesExistentes = new ArrayList<>();
        verticesExistentes.add(new PontoCurva(new Point(5, 5)));
        objetoLivre.setVertices(verticesExistentes);

        objetoLivre.inicializarVerticesSeNecessario();

        assertEquals(1, objetoLivre.getVertices().size());
        assertEquals(new Point(5, 5), objetoLivre.getVertices().get(0).getPosicao());
    }

    @Test
    void inicializarVerticesSeNecessario_semPontosLegados_naoFazNada() {
        ObjetoLivre objetoLivre = new ObjetoLivre();

        objetoLivre.inicializarVerticesSeNecessario();

        assertTrue(objetoLivre.getVertices().isEmpty());
    }

    @Test
    void objetoLivreLegado_sobrevivePersistenciaReflexivaEDesenhaIgual() throws Exception {
        ObjetoLivre original = new ObjetoLivre();
        original.setPontos(new ArrayList<>(TRIANGULO));
        // Simula um circuito XML de antes desta mudança: apenas "pontos",
        // "vertices" nunca existiu nesse arquivo.

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (XMLEncoder encoder = new XMLEncoder(baos)) {
            encoder.writeObject(original);
        }

        ObjetoLivre recarregado;
        try (XMLDecoder decoder = new XMLDecoder(new ByteArrayInputStream(baos.toByteArray()))) {
            recarregado = (ObjetoLivre) decoder.readObject();
        }

        assertTrue(recarregado.getVertices().isEmpty());
        recarregado.gerar();
        assertEquals(new Rectangle(0, 0, 100, 100), recarregado.getForma().getBounds());
        assertTrue(recarregado.getForma().contains(50, 50));
    }
}
