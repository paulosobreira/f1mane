package br.flmane.recursos;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import br.flmane.entidades.Circuito;

/**
 * {@link MigracaoCicloCircuito} extrai o ciclo (segundo campo de uma linha
 * antiga de 3 campos) para o circuito correspondente, e reescreve a linha
 * só com nome de exibição + ativo.
 */
class MigracaoCicloCircuitoTest {

    @Test
    void migrarLinha_extraiCicloParaOCircuitoEReescreveALinhaComDoisCampos(@TempDir File diretorioCircuitos)
            throws Exception {
        File arquivoMeta = new File(diretorioCircuitos, "teste_mro_meta.xml");
        escrever(new Circuito(), arquivoMeta);

        String novaLinha = MigracaoCicloCircuito.migrarLinha("teste_mro.xml=Teste,233,true", diretorioCircuitos);

        assertEquals("teste_mro.xml=Teste,true", novaLinha);
        assertEquals(233, ler(arquivoMeta).getCiclo());
    }

    @Test
    void migrarLinha_semArquivoDeMetadados_aindaAssimReescreveALinha(@TempDir File diretorioCircuitos)
            throws Exception {
        String novaLinha = MigracaoCicloCircuito.migrarLinha("semmeta_mro.xml=Sem Meta,180,false", diretorioCircuitos);

        assertEquals("semmeta_mro.xml=Sem Meta,false", novaLinha);
    }

    @Test
    void migrarLinha_jaMigrada_naoAlteraNada(@TempDir File diretorioCircuitos) throws Exception {
        String novaLinha = MigracaoCicloCircuito.migrarLinha("jamigrado_mro.xml=Ja Migrado,true", diretorioCircuitos);

        assertEquals("jamigrado_mro.xml=Ja Migrado,true", novaLinha);
    }

    private void escrever(Circuito circuito, File arquivo) throws Exception {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        XMLEncoder encoder = new XMLEncoder(byteArrayOutputStream);
        encoder.writeObject(circuito);
        encoder.flush();
        String conteudo = new String(byteArrayOutputStream.toByteArray()) + "</java>";
        try (FileOutputStream fileOutputStream = new FileOutputStream(arquivo)) {
            fileOutputStream.write(conteudo.getBytes());
        }
    }

    private Circuito ler(File arquivo) throws Exception {
        try (FileInputStream stream = new FileInputStream(arquivo)) {
            XMLDecoder decoder = new XMLDecoder(stream);
            return (Circuito) decoder.readObject();
        }
    }
}
