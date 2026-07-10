package br.f1mane.editor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.awt.Point;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import br.f1mane.entidades.Circuito;
import br.f1mane.entidades.No;

/**
 * Cobre a validação de proximidade usada pelo novo ObjetoEscapada
 * (noMaisProximoTracado1e2/noMaisProximoDoTracado em MainPanelEditor): um
 * clique perto de um nó do traçado 1 ou 2 (dentro de TOLERANCIA_ESCAPADA_PX)
 * é aceito; fora da tolerância, ou perto só do traçado errado, é rejeitado.
 */
class MainPanelEditorEscapadaValidacaoTest {

    private Circuito circuitoVetorizado() {
        Circuito circuito = new Circuito();
        List<No> pista = new ArrayList<>();
        pista.add(criarNo(1000, 1000));
        pista.add(criarNo(4000, 1000));
        pista.add(criarNo(4000, 4000));
        pista.add(criarNo(1000, 4000));
        circuito.setPista(pista);
        circuito.setBox(new ArrayList<>());
        circuito.setMultiplicadorLarguraPista(1.5);
        circuito.vetorizarPista();
        return circuito;
    }

    private No criarNo(int x, int y) {
        No no = new No();
        no.setPoint(new Point(x, y));
        no.setTipo(No.RETA);
        return no;
    }

    private static No noMaisProximoTracado1e2(MainPanelEditor editor, Point candidato) throws Exception {
        Method metodo = MainPanelEditor.class.getDeclaredMethod("noMaisProximoTracado1e2", Point.class);
        metodo.setAccessible(true);
        return (No) metodo.invoke(editor, candidato);
    }

    private static No noMaisProximoDoTracado(MainPanelEditor editor, Point candidato, int tracado) throws Exception {
        Method metodo = MainPanelEditor.class.getDeclaredMethod("noMaisProximoDoTracado", Point.class, int.class);
        metodo.setAccessible(true);
        return (No) metodo.invoke(editor, candidato, tracado);
    }

    @Test
    void cliqueExatoSobreNoDoTracado1_eAceito() throws Exception {
        Circuito circuito = circuitoVetorizado();
        MainPanelEditor editor = new MainPanelEditor();
        editor.setCircuito(circuito);

        No noAlvo = circuito.getPista1Full().get(300);
        No encontrado = noMaisProximoTracado1e2(editor, new Point(noAlvo.getPoint()));

        assertEquals(1, encontrado.getTracado(), "deveria ancorar num nó do traçado 1");
        assertEquals(noAlvo.getPoint(), encontrado.getPoint());
    }

    @Test
    void cliqueExatoSobreNoDoTracado2_eAceito() throws Exception {
        Circuito circuito = circuitoVetorizado();
        MainPanelEditor editor = new MainPanelEditor();
        editor.setCircuito(circuito);

        No noAlvo = circuito.getPista2Full().get(300);
        No encontrado = noMaisProximoTracado1e2(editor, new Point(noAlvo.getPoint()));

        assertEquals(2, encontrado.getTracado(), "deveria ancorar num nó do traçado 2");
        assertEquals(noAlvo.getPoint(), encontrado.getPoint());
    }

    @Test
    void cliqueLongeDeQualquerTracado_eRejeitado() throws Exception {
        Circuito circuito = circuitoVetorizado();
        MainPanelEditor editor = new MainPanelEditor();
        editor.setCircuito(circuito);

        // Bem longe de qualquer traçado 1/2 (centro do quadrado da pista).
        No encontrado = noMaisProximoTracado1e2(editor, new Point(2500, 2500));

        assertNull(encontrado, "clique longe de qualquer nó do traçado 1/2 não deveria ser aceito");
    }

    @Test
    void retornoNoTracadoErrado_eRejeitado() throws Exception {
        Circuito circuito = circuitoVetorizado();
        MainPanelEditor editor = new MainPanelEditor();
        editor.setCircuito(circuito);

        No noTracado1 = circuito.getPista1Full().get(300);

        // Ponto exatamente sobre um nó do traçado 1, validado contra o
        // traçado 2 (papel do retorno quando a saída foi no traçado 2): não
        // deveria encontrar nada, mesmo que o ponto esteja em cima de um nó
        // real (só que do traçado errado).
        No encontradoTracado2 = noMaisProximoDoTracado(editor, new Point(noTracado1.getPoint()), 2);
        No encontradoTracado1 = noMaisProximoDoTracado(editor, new Point(noTracado1.getPoint()), 1);

        assertNull(encontradoTracado2, "nó do traçado 1 não deveria validar como retorno do traçado 2");
        assertEquals(noTracado1.getPoint(), encontradoTracado1.getPoint(),
                "o mesmo ponto deveria validar normalmente contra o traçado correto (1)");
    }
}
