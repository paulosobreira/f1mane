package br.f1mane.recursos;

import br.f1mane.entidades.Circuito;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Utilitário de migração, executado uma única vez: extrai o campo `ciclo`
 * (segundo valor, hoje em {@code <NomeExibicao>,<ciclo>,<ativo>`) de cada
 * linha de {@code circuitos.properties} para o campo {@code ciclo} do
 * circuito correspondente, e reescreve a linha só com
 * {@code <NomeExibicao>,<ativo>}. Idempotente: linhas que já têm só dois
 * campos são deixadas como estão.
 */
public class MigracaoCicloCircuito {

    public static void main(String[] args) throws Exception {
        File propriedades = new File("src/main/resources/properties/circuitos.properties");
        File diretorioCircuitos = new File("src/main/resources/circuitos");
        List<String> linhas = new ArrayList<String>();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(propriedades), StandardCharsets.UTF_8))) {
            String linha;
            while ((linha = reader.readLine()) != null) {
                linhas.add(linha);
            }
        }

        List<String> novasLinhas = new ArrayList<String>();
        for (String linha : linhas) {
            novasLinhas.add(migrarLinha(linha, diretorioCircuitos));
        }

        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(propriedades), StandardCharsets.UTF_8))) {
            for (String linha : novasLinhas) {
                writer.write(linha);
                writer.newLine();
            }
        }
    }

    /** Pacote-privado (em vez de private) pra permitir teste direto com um diretório de circuitos temporário. */
    static String migrarLinha(String linha, File diretorioCircuitos) throws Exception {
        int idxIgual = linha.indexOf('=');
        if (idxIgual <= 0) {
            return linha;
        }
        String arquivo = linha.substring(0, idxIgual);
        String[] campos = linha.substring(idxIgual + 1).split(",", -1);
        if (campos.length < 3) {
            System.out.println("Já migrado ou formato inesperado, mantendo como está: " + arquivo);
            return linha;
        }
        String nomeExibicao = campos[0];
        String cicloStr = campos[1];
        String ativo = campos[2];

        int ciclo;
        try {
            ciclo = Integer.parseInt(cicloStr.trim());
        } catch (NumberFormatException e) {
            System.out.println("Ciclo inválido para " + arquivo + " (\"" + cicloStr + "\"); mantendo default no circuito");
            return arquivo + "=" + nomeExibicao + "," + ativo;
        }

        File arquivoMeta = new File(diretorioCircuitos, CarregadorRecursos.nomeArquivoMetadados(arquivo));
        if (!arquivoMeta.exists()) {
            System.out.println("Arquivo de metadados não encontrado para " + arquivo + "; ciclo=" + ciclo
                    + " não migrado para o circuito (só removido de circuitos.properties)");
            return arquivo + "=" + nomeExibicao + "," + ativo;
        }

        Circuito circuitoMeta;
        try (FileInputStream stream = new FileInputStream(arquivoMeta)) {
            XMLDecoder decoder = new XMLDecoder(stream);
            circuitoMeta = (Circuito) decoder.readObject();
        }
        circuitoMeta.setCiclo(ciclo);
        escrever(circuitoMeta, arquivoMeta);
        System.out.println("Migrado: " + arquivo + " (ciclo=" + ciclo + ")");
        return arquivo + "=" + nomeExibicao + "," + ativo;
    }

    private static void escrever(Circuito circuito, File arquivo) throws Exception {
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
