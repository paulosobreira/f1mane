package br.f1mane.editor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;

import java.awt.Color;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import br.f1mane.entidades.ObjetoGuardRails;
import br.f1mane.entidades.ObjetoLivre;
import br.f1mane.entidades.ObjetoPista;
import br.f1mane.entidades.PontoCurva;
import br.f1mane.entidades.TipoObjetoLivre;

/**
 * Regressão do "bug do padrão": copiar um ObjetoLivre com padrão (brita,
 * vegetação, água) gerava uma cópia sem o tipo — a cópia nascia flat
 * (POLIGONO_SIMPLES) exatamente em cima do original, e era salva assim no
 * XML do circuito (ex.: "Objeto 8" do albert_park_mro.xml).
 */
class MainPanelEditorCopiarObjetoTest {

    @Test
    void clonarObjetoLivre_preservaOTipoDePadrao() throws Exception {
        ObjetoLivre original = new ObjetoLivre();
        original.setTipo(TipoObjetoLivre.BRITA);
        original.setCorPimaria(new Color(120, 120, 120));
        original.setCorSecundaria(new Color(60, 60, 60));
        List<Point> pontos = new ArrayList<>();
        pontos.add(new Point(0, 0));
        pontos.add(new Point(100, 0));
        pontos.add(new Point(50, 80));
        original.setPontos(pontos);
        original.gerar();
        original.setPosicaoQuina(original.obterArea().getLocation());

        ObjetoLivre copia = (ObjetoLivre) MainPanelEditor.clonarObjetoPista(original);

        assertEquals(TipoObjetoLivre.BRITA, copia.getTipo(),
                "a cópia deveria manter o padrão do original, não voltar a POLIGONO_SIMPLES");
        assertEquals(original.getCorPimaria(), copia.getCorPimaria());
        assertEquals(original.getCorSecundaria(), copia.getCorSecundaria());
    }

    @Test
    void clonarObjetoLivre_duplicaPontosEVertices_semCompartilharInstancias() throws Exception {
        ObjetoLivre original = new ObjetoLivre();
        List<Point> pontos = new ArrayList<>();
        pontos.add(new Point(10, 20));
        original.setPontos(pontos);
        List<PontoCurva> vertices = new ArrayList<>();
        PontoCurva vertice = new PontoCurva(new Point(10, 20));
        vertice.setHasteFim(new Point(30, 40));
        vertices.add(vertice);
        original.setVertices(vertices);
        original.gerar();
        original.setPosicaoQuina(new Point(10, 20));

        ObjetoLivre copia = (ObjetoLivre) MainPanelEditor.clonarObjetoPista(original);

        assertEquals(original.getPontos(), copia.getPontos());
        assertNotSame(original.getPontos().get(0), copia.getPontos().get(0));
        assertEquals(new Point(10, 20), copia.getVertices().get(0).getPosicao());
        assertEquals(new Point(30, 40), copia.getVertices().get(0).getHasteFim());
        assertNotSame(original.getVertices().get(0), copia.getVertices().get(0));
    }

    /**
     * GuardRails também é desenhado ponto a ponto (ver ObjetoGuardRails):
     * sem copiar {@code pontos}, a cópia nasceria sem encadeamento nenhum
     * (invisível), como acontecia com ObjetoLivre antes da correção acima.
     */
    @Test
    void clonarObjetoGuardRails_duplicaPontos_semCompartilharInstancias() throws Exception {
        ObjetoGuardRails original = new ObjetoGuardRails();
        List<Point> pontos = new ArrayList<>();
        pontos.add(new Point(10, 20));
        pontos.add(new Point(10, 120));
        original.setPontos(pontos);
        original.gerar();
        original.setPosicaoQuina(original.obterArea().getLocation());

        ObjetoGuardRails copia = (ObjetoGuardRails) MainPanelEditor.clonarObjetoPista(original);

        assertEquals(original.getPontos(), copia.getPontos());
        assertNotSame(original.getPontos().get(0), copia.getPontos().get(0));
    }

    @Test
    void clonarObjetoPista_copiaPropriedadesBasicas() throws Exception {
        ObjetoLivre original = new ObjetoLivre();
        original.setAltura(15);
        original.setLargura(25);
        original.setAngulo(9.0);
        original.setPintaEmcima(true);
        original.setNivelDesenho(-1);
        original.setTransparencia(200);
        original.setPosicaoQuina(new Point(5, 7));

        ObjetoPista copia = MainPanelEditor.clonarObjetoPista(original);

        assertEquals(15, copia.getAltura());
        assertEquals(25, copia.getLargura());
        assertEquals(9.0, copia.getAngulo());
        assertEquals(-1, copia.getNivelDesenho());
        assertEquals(false, copia.isPintaEmcima());
        assertEquals(200, copia.getTransparencia());
        assertEquals(new Point(5, 7), copia.getPosicaoQuina());
        assertNotSame(original.getPosicaoQuina(), copia.getPosicaoQuina());
    }
}
