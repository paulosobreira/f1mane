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

    @Test
    void objetoLivre_estaRegistradoComoTipoCriavelNaoDeCenario() {
        boolean encontrado = false;
        for (br.f1mane.editor.TipoObjetoPista tipo : br.f1mane.editor.TipoObjetoPista.values()) {
            if (tipo.criar() instanceof ObjetoLivre) {
                encontrado = true;
                assertTrue(!tipo.isCenario(),
                        "ObjetoLivre deve ficar em circuito.objetos (como Escapada/Transparencia), não em objetosCenario");
            }
        }
        assertTrue(encontrado, "ObjetoLivre deveria estar registrado em TipoObjetoPista para ser criável no editor");
    }
}
