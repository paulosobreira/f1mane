package br.flmane.recursos.idiomas;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

import org.junit.jupiter.api.Test;

/**
 * O launcher passou a obter seus rótulos via {@link Lang#msg(String)}; esta
 * suíte garante que nenhum bundle empacotado fique sem as chaves novas (o que
 * faria o rótulo cair no fallback de chave crua do {@code Lang.msg}).
 */
class LangBundlesLauncherKeysTest {

    private static final String[] CHAVES_LAUNCHER = {
            "launcherCopiarLink",
            "launcherAbrirNavegador",
            "launcherJogoSolo",
            "launcherJogoMulti"
    };

    @Test
    void todosBundlesDeIdiomaTemAsChavesDoLauncher() throws Exception {
        File dirIdiomas = diretorioIdiomas();
        File[] bundles = dirIdiomas.listFiles((dir, name) ->
                name.startsWith("mensagens_") && name.endsWith(".properties"));

        assertTrue(bundles != null && bundles.length > 0,
                "nenhum bundle mensagens_XX.properties encontrado em "
                        + dirIdiomas);

        for (File bundleFile : bundles) {
            Properties props = new Properties();
            try (InputStream in = new FileInputStream(bundleFile)) {
                props.load(in);
            }
            for (String chave : CHAVES_LAUNCHER) {
                assertTrue(props.containsKey(chave),
                        chave + " ausente em " + bundleFile.getName());
            }
        }
    }

    private static File diretorioIdiomas() throws Exception {
        URL url = LangBundlesLauncherKeysTest.class.getClassLoader()
                .getResource("idiomas/mensagens_pt.properties");
        return new File(url.toURI()).getParentFile();
    }
}
