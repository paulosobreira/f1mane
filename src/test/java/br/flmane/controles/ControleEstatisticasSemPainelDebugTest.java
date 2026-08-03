package br.flmane.controles;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.lang.reflect.Field;

import org.junit.jupiter.api.Test;

/**
 * No servidor headless ninguém pede o painel de debug — a corrida é servida por
 * REST e desenhada no navegador. {@code atualizaInfoDebug()} não pode, nesse
 * caso, montar o texto a cada tick nem acordar a Event Dispatch Thread do Swing
 * (o que antes ainda estouraria em NPE no {@code JEditorPane} nulo).
 */
class ControleEstatisticasSemPainelDebugTest {

    @Test
    void semPainelSolicitado_naoMontaTextoNemTocaNoJogo() throws Exception {
        InterfaceJogo jogo = mock(InterfaceJogo.class);
        ControleEstatisticas controleEstatisticas = new ControleEstatisticas(jogo);

        assertDoesNotThrow(controleEstatisticas::atualizaInfoDebug);

        verify(jogo, never()).atualizaInfoDebug(org.mockito.ArgumentMatchers.any(StringBuilder.class));
        assertNull(campo(controleEstatisticas, "infoTextual"),
                "nenhum JEditorPane deve ser criado");
        assertNull(campo(controleEstatisticas, "painelDebug"),
                "nenhum JPanel deve ser criado");
    }

    private static Object campo(ControleEstatisticas alvo, String nome) throws Exception {
        Field campo = ControleEstatisticas.class.getDeclaredField(nome);
        campo.setAccessible(true);
        return campo.get(alvo);
    }
}
