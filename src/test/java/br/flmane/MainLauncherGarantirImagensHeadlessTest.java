package br.flmane;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import br.flmane.controles.ControleRecursos;
import br.flmane.recursos.CarregadorRecursos;
import br.flmane.recursos.ImagensHeadlessDisco;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Vector;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockedStatic;

/**
 * {@code garantirImagensHeadless(String)} decide entre gerar as imagens
 * headless (e gravar o marcador de conclusão) ou pular a pré-geração
 * inteira quando o diretório informado já tem uma pré-geração anterior —
 * é o que permite um container reiniciado sobre a mesma imagem (com as
 * imagens já assadas no {@code docker build}) subir sem pagar o custo de
 * boot de novo. {@code ControleRecursos}/{@code CarregadorRecursos} são
 * mockados pra devolver coleções vazias, então a pré-geração real não
 * roda (evita minutos de geração de assets de verdade num teste unitário).
 */
class MainLauncherGarantirImagensHeadlessTest {

    @Test
    void semMarcadorPrevio_geraEGravaMarcador(@TempDir Path diretorioFixo) throws Exception {
        try (MockedStatic<ControleRecursos> controleRecursos = mockStatic(ControleRecursos.class);
             MockedStatic<CarregadorRecursos> carregadorRecursosEstatico =
                     mockStatic(CarregadorRecursos.class)) {
            controleRecursos.when(ControleRecursos::carregarCircuitos)
                    .thenReturn(Collections.emptyMap());
            CarregadorRecursos carregadorRecursosMock = mock(CarregadorRecursos.class);
            carregadorRecursosEstatico
                    .when(() -> CarregadorRecursos.getCarregadorRecursos(eq(false)))
                    .thenReturn(carregadorRecursosMock);
            when(carregadorRecursosMock.getVectorTemps()).thenReturn(new Vector<>());

            MainLauncher.garantirImagensHeadless(diretorioFixo.toString());
        }

        assertTrue(Files.exists(diretorioFixo.resolve(".pronto")));
    }

    @Test
    void comMarcadorPrevio_pulaAPreGeracao(@TempDir Path diretorioFixo) throws Exception {
        ImagensHeadlessDisco.iniciar(diretorioFixo.toString());
        ImagensHeadlessDisco.marcarPreGeracaoConcluida();

        try (MockedStatic<ControleRecursos> controleRecursos = mockStatic(ControleRecursos.class)) {
            MainLauncher.garantirImagensHeadless(diretorioFixo.toString());

            controleRecursos.verifyNoInteractions();
        }

        assertFalse(Files.exists(diretorioFixo.resolve("circuitos").resolve("qualquer.jpg")));
    }
}
