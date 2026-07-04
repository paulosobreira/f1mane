package br.f1mane.recursos;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import br.f1mane.entidades.Circuito;
import br.f1mane.entidades.ObjetoLivre;

/**
 * ObjetoLivre passou a ser objeto de cenário (ver TipoObjetoPista.LIVRE),
 * mas circuitos XML salvos antes dessa mudança ainda têm instâncias de
 * ObjetoLivre gravadas em circuito.objetos (classificação legada). Sem uma
 * migração no carregamento, esses objetos ficariam presos lá para sempre —
 * nunca desenhados de fato em corrida (DesenhoProceduralCircuito só desenha
 * objetosCenario) e mostrados na lista errada no editor.
 * "albert_park_mro.xml" é um circuito real com ObjetoLivre legado salvo em
 * objetos, útil para provar a migração de ponta a ponta.
 */
class ObjetoLivreMigracaoCenarioTest {

    @Test
    void carregarCircuitoComObjetoLivreLegado_migraParaObjetosCenario() throws Exception {
        Circuito circuito = CarregadorRecursos.carregarCircuito("albert_park_mro.xml");

        boolean temLivreEmObjetos = circuito.getObjetos() != null
                && circuito.getObjetos().stream().anyMatch(o -> o instanceof ObjetoLivre);
        assertFalse(temLivreEmObjetos, "ObjetoLivre não deveria mais ficar em circuito.objetos após a migração");

        boolean temLivreEmObjetosCenario = circuito.getObjetosCenario() != null
                && circuito.getObjetosCenario().stream().anyMatch(o -> o instanceof ObjetoLivre);
        assertTrue(temLivreEmObjetosCenario,
                "esperava encontrar os ObjetoLivre legados (Objeto 6/7/8 etc.) migrados para objetosCenario");
    }
}
