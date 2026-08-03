package br.flmane;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;

/**
 * As flags de runtime do modo headless moram só no {@code ENTRYPOINT} do
 * {@code flmane.dockerfile} — não há script wrapper onde repeti-las. Uma
 * edição que as remova passaria despercebida até alguém medir a memória do
 * container de novo, então a guarda é aqui.
 */
class FlmaneDockerfileFlagsRuntimeTest {

    @Test
    void entrypointRodaComAwtHeadless() throws Exception {
        assertTrue(entrypoint().contains("-Djava.awt.headless=true"),
                "o ENTRYPOINT deve subir a JVM com -Djava.awt.headless=true; "
                        + "sem isso o servidor inicializa o toolkit gráfico nativo à toa");
    }

    @Test
    void entrypointDimensionaHeapPeloLimiteDoContainer() throws Exception {
        assertTrue(entrypoint().contains("-XX:MaxRAMPercentage"),
                "o ENTRYPOINT deve derivar o heap do limite de memória do container "
                        + "(-XX:MaxRAMPercentage), e não do default de 1/4 da RAM do host");
    }

    @Test
    void preGeracaoDeImagensTambemRodaHeadless() throws Exception {
        String linhaPreGeracao = linhaContendo("--pre-gerar-imagens");

        assertTrue(linhaPreGeracao.contains("-Djava.awt.headless=true"),
                "a etapa de pré-geração no docker build também deve rodar headless");
    }

    private static String entrypoint() throws Exception {
        return linhaContendo("ENTRYPOINT");
    }

    private static String linhaContendo(String trecho) throws Exception {
        Path dockerfile = Paths.get("flmane.dockerfile");
        for (String linha : Files.readAllLines(dockerfile, StandardCharsets.UTF_8)) {
            if (linha.contains(trecho) && !linha.trim().startsWith("#")) {
                return linha;
            }
        }
        throw new AssertionError(
                "nenhuma linha com '" + trecho + "' em " + dockerfile.toAbsolutePath());
    }
}
