package br.f1mane.entidades;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.awt.Point;
import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * Cobre, de ponta a ponta e sem depender do editor Swing (mesma limitação de
 * ambiente já documentada em CircuitoObjetosCenarioIntegrationTest — não há
 * display interativo disponível), o fluxo completo de um ObjetoLivre curvo
 * com tipo definido: criação, adição ao circuito, persistência via
 * XMLEncoder/XMLDecoder (o mesmo mecanismo usado para salvar/reabrir um
 * circuito no editor) e sobrevivência da curva e do tipo após recarregar.
 *
 * Validação manual ainda pendente (fora do alcance deste teste): criar um
 * ObjetoLivre de cada tipo pela UI do editor, arrastar uma haste, salvar o
 * circuito pelo editor e reabri-lo, observando visualmente a curva e o
 * padrão de preenchimento.
 */
class ObjetoLivreCircuitoIntegrationTest {

    @Test
    void objetoLivreCurvoComTipo_sobrevivePersistenciaCompletaDoCircuito() throws Exception {
        Circuito circuito = new Circuito();
        circuito.setPista(new ArrayList<No>());
        circuito.setBox(new ArrayList<No>());

        ObjetoLivre objetoLivre = new ObjetoLivre();
        objetoLivre.setNome("Lago Teste");
        objetoLivre.setTipo(TipoObjetoLivre.AGUA);
        objetoLivre.setPosicaoQuina(new Point(500, 500));

        List<PontoCurva> vertices = new ArrayList<>();
        PontoCurva v0 = new PontoCurva(new Point(0, 0));
        v0.setHasteFim(new Point(30, -40));
        vertices.add(v0);
        vertices.add(new PontoCurva(new Point(100, 0)));
        vertices.add(new PontoCurva(new Point(50, 100)));
        objetoLivre.setVertices(vertices);
        objetoLivre.gerar();

        List<ObjetoPista> objetos = new ArrayList<>();
        objetos.add(objetoLivre);
        circuito.setObjetos(objetos);

        // Persistência via XMLEncoder/XMLDecoder: o mesmo mecanismo usado por
        // "Salvar Pista Atual" e pela navegação Anterior/Próximo no editor.
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (XMLEncoder encoder = new XMLEncoder(baos)) {
            encoder.writeObject(circuito);
        }
        Circuito recarregado;
        try (XMLDecoder decoder = new XMLDecoder(new ByteArrayInputStream(baos.toByteArray()))) {
            recarregado = (Circuito) decoder.readObject();
        }

        assertNotNull(recarregado.getObjetos());
        ObjetoLivre objetoRecarregado = (ObjetoLivre) recarregado.getObjetos().stream()
                .filter(o -> o instanceof ObjetoLivre)
                .findFirst()
                .orElseGet(() -> fail("ObjetoLivre não sobreviveu ao round-trip XMLEncoder/XMLDecoder"));

        assertEquals("Lago Teste", objetoRecarregado.getNome());
        assertEquals(TipoObjetoLivre.AGUA, objetoRecarregado.getTipo());
        assertEquals(3, objetoRecarregado.getVertices().size());
        assertEquals(new Point(0, 0), objetoRecarregado.getVertices().get(0).getPosicao());
        assertEquals(new Point(30, -40), objetoRecarregado.getVertices().get(0).getHasteFim());

        // A curva sobrevive: mesma prova matemática usada em ObjetoLivreTest
        // (o mínimo da cúbica nesse segmento é negativo, o polígono reto nunca seria).
        objetoRecarregado.gerar();
        assertTrue(objetoRecarregado.getForma().getBounds().y < 0,
                "a curvatura da haste deveria ter sobrevivido ao round-trip");
    }

    /**
     * Regressão: a forma (generalPath) não é bean property e não sobrevive ao
     * XMLDecoder; nada no fluxo de carga chama gerar(), então os objetos
     * apareciam na lista do editor mas não eram renderizados. desenha() deve
     * regenerar o path sozinho na primeira renderização após recarregar.
     */
    @Test
    void objetoLivreRecarregado_desenhaSemChamarGerarManualmente() throws Exception {
        Circuito circuito = new Circuito();
        circuito.setPista(new ArrayList<No>());
        circuito.setBox(new ArrayList<No>());

        ObjetoLivre objetoLivre = new ObjetoLivre();
        objetoLivre.setCorPimaria(new java.awt.Color(255, 0, 0));
        List<Point> pontos = new ArrayList<>();
        pontos.add(new Point(10, 10));
        pontos.add(new Point(110, 10));
        pontos.add(new Point(60, 90));
        objetoLivre.setPontos(pontos);
        objetoLivre.gerar();
        objetoLivre.setPosicaoQuina(objetoLivre.obterArea().getLocation());

        List<ObjetoPista> objetos = new ArrayList<>();
        objetos.add(objetoLivre);
        circuito.setObjetos(objetos);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (XMLEncoder encoder = new XMLEncoder(baos)) {
            encoder.writeObject(circuito);
        }
        Circuito recarregado;
        try (XMLDecoder decoder = new XMLDecoder(new ByteArrayInputStream(baos.toByteArray()))) {
            recarregado = (Circuito) decoder.readObject();
        }
        ObjetoLivre objetoRecarregado = (ObjetoLivre) recarregado.getObjetos().get(0);

        // Sem gerar() manual: desenha() direto, como fazem o editor e o jogo.
        java.awt.image.BufferedImage img =
                new java.awt.image.BufferedImage(200, 150, java.awt.image.BufferedImage.TYPE_INT_RGB);
        java.awt.Graphics2D g2d = img.createGraphics();
        objetoRecarregado.desenha(g2d, 1.0);
        g2d.dispose();

        boolean pintou = false;
        int vermelho = new java.awt.Color(255, 0, 0).getRGB();
        for (int x = 0; x < img.getWidth() && !pintou; x++) {
            for (int y = 0; y < img.getHeight() && !pintou; y++) {
                pintou = img.getRGB(x, y) == vermelho;
            }
        }
        assertTrue(pintou, "o objeto recarregado deveria ser renderizado sem precisar de gerar() manual");
    }

    /**
     * ObjetoLivre é um objeto de desenho (mesma família de
     * Arquibancada/Construcao/GuardRails/Pneus), não um objeto de função
     * como Escapada/Transparencia — por isso fica em circuito.objetosCenario,
     * de onde DesenhoProceduralCircuito realmente o desenha em corrida.
     */
    @Test
    void objetoLivre_estaRegistradoComoTipoCriavelDeCenario() {
        boolean encontrado = false;
        for (br.f1mane.editor.TipoObjetoPista tipo : br.f1mane.editor.TipoObjetoPista.values()) {
            if (tipo.criar() instanceof ObjetoLivre) {
                encontrado = true;
                assertTrue(tipo.isCenario(),
                        "ObjetoLivre deve ficar em circuito.objetosCenario (como Arquibancada/Construcao/GuardRails/Pneus)");
            }
        }
        assertTrue(encontrado, "ObjetoLivre deveria estar registrado em TipoObjetoPista para ser criável no editor");
    }
}
