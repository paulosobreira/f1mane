package br.f1mane.recursos;

import br.f1mane.entidades.Circuito;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Utilitário de migração, executado uma única vez: para cada circuito ainda
 * no formato antigo (um único {@code *_mro.xml} com tudo, incluindo
 * {@code ativo}), gera {@code _mro_meta.xml} (metadados leves + pista/box) e
 * substitui {@code _mro.xml} pela versão só com objetos/objetosCenario, e
 * migra o valor de {@code ativo} para o terceiro campo da linha
 * correspondente em {@code circuitos.properties}.
 * <p>
 * Idempotente: pula qualquer circuito cujo {@code _mro_meta.xml} já exista
 * (evita decodificar um {@code _mro.xml} já migrado, que perderia
 * pista/box/ativo numa segunda passada). Não é chamado por nenhum fluxo do
 * jogo/editor — rode manualmente uma única vez ao adotar esta mudança.
 */
public class MigracaoCircuitoMetadados {

    public static void main(String[] args) throws Exception {
        File diretorio = new File("src/main/resources/circuitos");
        File[] arquivos = diretorio.listFiles((dir, name) -> name.endsWith("_mro.xml"));
        if (arquivos == null) {
            System.out.println("Diretório de circuitos não encontrado: " + diretorio.getAbsolutePath());
            return;
        }
        for (File arquivo : arquivos) {
            migrar(arquivo);
        }
    }

    private static void migrar(File arquivo) throws Exception {
        File arquivoMeta = new File(arquivo.getParentFile(),
                CarregadorRecursos.nomeArquivoMetadados(arquivo.getName()));
        if (arquivoMeta.exists()) {
            System.out.println("Já migrado, pulando: " + arquivo.getName());
            return;
        }

        Circuito circuitoOriginal;
        try (FileInputStream stream = new FileInputStream(arquivo)) {
            XMLDecoder decoder = new XMLDecoder(stream);
            circuitoOriginal = (Circuito) decoder.readObject();
        }
        boolean ativoOriginal = circuitoOriginal.isAtivo();

        escrever(circuitoOriginal.copiaParaArquivoMetadados(), arquivoMeta);
        escrever(circuitoOriginal.copiaParaArquivoObjetos(), arquivo);
        CarregadorRecursos.atualizarAtivoEmCircuitosProperties(arquivo.getName(), ativoOriginal);

        System.out.println("Migrado: " + arquivo.getName() + " (ativo=" + ativoOriginal + ")");
    }

    private static void escrever(Circuito circuito, File arquivo) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        XMLEncoder encoder = new XMLEncoder(byteArrayOutputStream);
        encoder.writeObject(circuito);
        encoder.flush();
        String conteudo = new String(byteArrayOutputStream.toByteArray()) + "</java>";
        try (FileOutputStream fileOutputStream = new FileOutputStream(arquivo)) {
            fileOutputStream.write(conteudo.getBytes());
        }
    }
}
