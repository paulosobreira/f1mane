package br.flmane;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

/**
 * criarDiretorioTemporarioSeguro() substitui o Files.createTempDirectory()
 * puro em extrairWebapp(): em Linux/macOS, java.io.tmpdir é compartilhado
 * por todos os usuários da máquina, e o diretório criado ali (que recebe o
 * webapp extraído do jar, incluindo web.xml) não pode ficar acessível a
 * outros usuários locais. Por isso o diretório temporário passou a ser
 * criado dentro do home do usuário (~/.flmane/tmp), nunca em
 * java.io.tmpdir, além de restringir permissões quando o filesystem
 * suporta POSIX.
 */
class MainLauncherCriarDiretorioTemporarioSeguroTest {

    private Path diretorioCriado;

    @AfterEach
    void limpa() throws IOException {
        if (diretorioCriado != null && Files.exists(diretorioCriado)) {
            Files.delete(diretorioCriado);
        }
    }

    private Path invocaCriarDiretorioTemporarioSeguro(String prefixo)
            throws Exception {
        Method m = MainLauncher.class.getDeclaredMethod(
                "criarDiretorioTemporarioSeguro", String.class);
        m.setAccessible(true);
        return (Path) m.invoke(null, prefixo);
    }

    @Test
    void diretorioCriado_existeEUsaOPrefixoInformado() throws Exception {
        diretorioCriado = invocaCriarDiretorioTemporarioSeguro("flmane-webapp");

        assertTrue(Files.isDirectory(diretorioCriado));
        assertTrue(diretorioCriado.getFileName().toString()
                .startsWith("flmane-webapp"));
    }

    @Test
    void emSistemaPosix_permissoesRestringemAcessoAoDono() throws Exception {
        assumeTrue(FileSystems.getDefault()
                .supportedFileAttributeViews()
                .contains("posix"));

        diretorioCriado = invocaCriarDiretorioTemporarioSeguro("flmane-webapp");

        PosixFileAttributeView view = Files.getFileAttributeView(
                diretorioCriado, PosixFileAttributeView.class);
        Set<PosixFilePermission> permissoes = view.readAttributes().permissions();

        assertEquals(PosixFilePermissions.fromString("rwx------"), permissoes,
                "diretorio nao deve ser legivel/gravavel por outros usuarios");
    }

    @Test
    void diretorioFicaDentroDoHomeDoUsuario_naoNoTmpCompartilhado() throws Exception {
        diretorioCriado = invocaCriarDiretorioTemporarioSeguro("flmane-webapp");

        Path homeEsperado = Path.of(System.getProperty("user.home"), ".flmane", "tmp");
        assertTrue(diretorioCriado.startsWith(homeEsperado),
                "diretorio temporario deveria ficar sob ~/.flmane/tmp, e nao em java.io.tmpdir");
    }

    @Test
    void emSistemaPosix_diretorioBaseTambemFicaRestritoAoDono() throws Exception {
        assumeTrue(FileSystems.getDefault()
                .supportedFileAttributeViews()
                .contains("posix"));

        diretorioCriado = invocaCriarDiretorioTemporarioSeguro("flmane-webapp");
        Path base = diretorioCriado.getParent();

        PosixFileAttributeView view = Files.getFileAttributeView(
                base, PosixFileAttributeView.class);
        Set<PosixFilePermission> permissoes = view.readAttributes().permissions();

        assertEquals(PosixFilePermissions.fromString("rwx------"), permissoes,
                "diretorio base (~/.flmane/tmp) nao deve ser legivel/gravavel por outros usuarios");
    }
}
