package br.flmane.recursos;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Cobre o diretório fixo (via override, já que a variável de ambiente real
 * não é mutável a partir do teste) e o marcador de pré-geração concluída,
 * usados pra reaproveitar imagens headless já assadas entre restarts do
 * mesmo container em vez de recriar um diretório temporário a cada boot.
 */
class ImagensHeadlessDiscoTest {

    @AfterEach
    void reseta() throws IOException {
        ImagensHeadlessDisco.iniciar((String) null);
    }

    @Test
    void diretorioFixoInformado_eUsadoDiretamente(@TempDir Path diretorioFixo) throws IOException {
        ImagensHeadlessDisco.iniciar(diretorioFixo.toString());

        assertEquals(diretorioFixo, ImagensHeadlessDisco.diretorioBase());
        assertTrue(Files.isDirectory(diretorioFixo.resolve("circuitos")));
        assertTrue(Files.isDirectory(diretorioFixo.resolve("carros")));
        assertTrue(Files.isDirectory(diretorioFixo.resolve("capacetes")));
    }

    @Test
    void diretorioFixoAusente_criaDiretorioTemporarioNovo() throws IOException {
        ImagensHeadlessDisco.iniciar((String) null);

        Path base = ImagensHeadlessDisco.diretorioBase();
        assertTrue(Files.isDirectory(base));
        assertTrue(base.getFileName().toString().startsWith("flmane-imagens-headless"));
    }

    @Test
    void semMarcador_preGeracaoConcluidaERetornaFalse(@TempDir Path diretorioFixo) throws IOException {
        ImagensHeadlessDisco.iniciar(diretorioFixo.toString());

        assertFalse(ImagensHeadlessDisco.preGeracaoConcluida());
    }

    @Test
    void marcarPreGeracaoConcluida_gravaMarcadorQuePersiste(@TempDir Path diretorioFixo) throws IOException {
        ImagensHeadlessDisco.iniciar(diretorioFixo.toString());

        ImagensHeadlessDisco.marcarPreGeracaoConcluida();

        assertTrue(ImagensHeadlessDisco.preGeracaoConcluida());
        assertTrue(Files.exists(diretorioFixo.resolve(".pronto")));
    }

    @Test
    void reiniciarSobreOMesmoDiretorioFixo_reaproveitaMarcadorExistente(
            @TempDir Path diretorioFixo) throws IOException {
        ImagensHeadlessDisco.iniciar(diretorioFixo.toString());
        ImagensHeadlessDisco.marcarPreGeracaoConcluida();

        ImagensHeadlessDisco.iniciar(diretorioFixo.toString());

        assertTrue(ImagensHeadlessDisco.preGeracaoConcluida(),
                "reiniciar sobre o mesmo diretorio fixo deve enxergar o marcador ja gravado");
    }
}
