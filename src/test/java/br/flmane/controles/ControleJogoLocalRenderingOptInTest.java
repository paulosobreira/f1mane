package br.flmane.controles;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import br.flmane.recursos.CarregadorRecursos;

import java.lang.reflect.Field;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

/**
 * Uma corrida servida por REST é desenhada no navegador do cliente: criar o
 * {@code GerenciadorVisual} nela só carregaria as classes de
 * {@code br.flmane.visao} num processo que nunca desenha.
 * <p>
 * A decisão era um {@code instanceof JogoServidor} escondido no construtor da
 * superclasse — qualquer subclasse de servidor nova voltaria a criar o
 * gerenciador em silêncio. Agora é um parâmetro explícito, e é isso que este
 * teste fixa.
 */
class ControleJogoLocalRenderingOptInTest {

    /**
     * Construir um {@code ControleJogoLocal} liga o cache estático de recursos
     * (flag global e sticky de {@code CarregadorRecursos}) e deixa circuitos
     * desserializados nos mapas — outros testes da suíte receberiam então um
     * circuito já mutado por este. Limpar aqui mantém a suíte independente de
     * ordem.
     */
    @AfterEach
    void limparCachesEstaticos() {
        CarregadorRecursos.liberarCachesPreGeracao();
    }

    @Test
    void semRendering_naoCriaGerenciadorVisual() throws Exception {
        ControleJogoLocal semRendering = new ControleJogoLocal("t2024", false) {
        };

        assertNull(gerenciadorVisual(semRendering),
                "modo sem rendering não deve instanciar GerenciadorVisual");
    }

    @Test
    void comRendering_criaGerenciadorVisual() throws Exception {
        ControleJogoLocal comRendering = new ControleJogoLocal("t2024", true) {
        };

        assertNotNull(gerenciadorVisual(comRendering),
                "modo gráfico deve continuar instanciando GerenciadorVisual");
    }

    private static Object gerenciadorVisual(ControleJogoLocal controle) throws Exception {
        Field campo = ControleJogoLocal.class.getDeclaredField("gerenciadorVisual");
        campo.setAccessible(true);
        return campo.get(controle);
    }
}
