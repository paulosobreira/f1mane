package br.flmane.recursos;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Diretório temporário de imagens (fundo/mini de circuito, carro-lado,
 * carro-cima com/sem aerofólio, capacete) pré-geradas no boot do servidor
 * headless — a fonte de verdade servida por {@code LetsRace} deixa de ser um
 * {@code BufferedImage} em cache estático e passa a ser o arquivo em disco
 * aqui gerenciado. Recriado do zero a cada boot, no mesmo padrão seguro
 * (perms POSIX restritas ao dono, fora do java.io.tmpdir compartilhado) já
 * usado por {@code MainLauncher.extrairWebapp()}.
 */
public final class ImagensHeadlessDisco {

    private static volatile Path base;

    /**
     * Locks por caminho de arquivo (não por bytes de imagem) — usados só
     * para colapsar requisições concorrentes ao mesmo asset ainda não
     * pré-gerado (fallback preguiçoso) numa única geração.
     */
    private static final ConcurrentHashMap<String, Object> locks = new ConcurrentHashMap<>();

    private ImagensHeadlessDisco() {
    }

    public static synchronized void iniciar() throws IOException {
        Path novaBase = criarDiretorioTemporarioSeguro("flmane-imagens-headless");
        Files.createDirectories(novaBase.resolve("circuitos"));
        Files.createDirectories(novaBase.resolve("carros"));
        Files.createDirectories(novaBase.resolve("capacetes"));
        base = novaBase;
        locks.clear();
    }

    public static boolean iniciado() {
        return base != null;
    }

    public static Path diretorioBase() {
        return base;
    }

    public static File arquivoCircuitoFundo(String nomeJpg) {
        return base.resolve("circuitos").resolve(nomeJpg).toFile();
    }

    public static File arquivoCircuitoMini(String nomeXml) {
        String nomeArquivo = nomeXml.replaceFirst("\\.xml$", "_mini.png");
        return base.resolve("circuitos").resolve(nomeArquivo).toFile();
    }

    public static File arquivoCarroLado(String temporadaBare, int idCarro) {
        return base.resolve("carros")
                .resolve("t" + temporadaBare + "_" + idCarro + "_lado.png").toFile();
    }

    public static File arquivoCarroCima(String temporadaBare, int idCarro, boolean semAsa) {
        String sufixo = semAsa ? "_cima_sem_asa.png" : "_cima.png";
        return base.resolve("carros")
                .resolve("t" + temporadaBare + "_" + idCarro + sufixo).toFile();
    }

    public static File arquivoCapacete(String temporadaBare, int idPiloto) {
        return base.resolve("capacetes")
                .resolve("t" + temporadaBare + "_" + idPiloto + ".png").toFile();
    }

    public interface GeradorImagem {
        BufferedImage gerar() throws Exception;
    }

    /**
     * Lê os bytes de {@code arquivo} se já existir; caso contrário, gera a
     * imagem via {@code gerador}, grava em disco e devolve os bytes —
     * requisições concorrentes para o mesmo {@code arquivo} colapsam num
     * único lock, sem nunca reter o {@code BufferedImage} resultante em
     * memória além do escopo desta chamada.
     */
    public static byte[] obterOuGerarBytes(File arquivo, String formatoImageIO,
                                            GeradorImagem gerador) throws Exception {
        byte[] existente = lerSeExistir(arquivo);
        if (existente != null) {
            return existente;
        }
        Object lock = locks.computeIfAbsent(arquivo.getAbsolutePath(), k -> new Object());
        synchronized (lock) {
            existente = lerSeExistir(arquivo);
            if (existente != null) {
                return existente;
            }
            BufferedImage imagem = gerador.gerar();
            if (imagem == null) {
                return null;
            }
            byte[] bytes = codificar(imagem, formatoImageIO);
            Files.write(arquivo.toPath(), bytes);
            return bytes;
        }
    }

    private static byte[] lerSeExistir(File arquivo) throws IOException {
        if (!arquivo.exists()) {
            return null;
        }
        return Files.readAllBytes(arquivo.toPath());
    }

    private static byte[] codificar(BufferedImage imagem, String formato) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(imagem, formato, baos);
        return baos.toByteArray();
    }

    /**
     * Grava {@code imagem} em {@code arquivo} no formato indicado; não faz
     * nada se {@code imagem} for nula (falha de geração já logada pelo
     * chamador).
     */
    public static void gravar(File arquivo, BufferedImage imagem, String formato) throws IOException {
        if (imagem == null) {
            return;
        }
        Files.write(arquivo.toPath(), codificar(imagem, formato));
    }

    /**
     * Mesmo padrão de segurança de
     * {@code MainLauncher.criarDiretorioTemporarioSeguro}: diretório
     * temporário dentro do home do usuário (nunca no java.io.tmpdir
     * compartilhado), com permissões POSIX restritas ao dono quando
     * suportado pelo filesystem.
     */
    private static Path criarDiretorioTemporarioSeguro(String prefixo) throws IOException {
        Path base = diretorioBaseTemporario();
        if (FileSystems.getDefault().supportedFileAttributeViews().contains("posix")) {
            FileAttribute<Set<PosixFilePermission>> apenasDono =
                    PosixFilePermissions.asFileAttribute(
                            PosixFilePermissions.fromString("rwx------"));
            return Files.createTempDirectory(base, prefixo, apenasDono);
        }
        return Files.createTempDirectory(base, prefixo);
    }

    private static Path diretorioBaseTemporario() throws IOException {
        Path base = Paths.get(System.getProperty("user.home"), ".flmane", "tmp");
        if (FileSystems.getDefault().supportedFileAttributeViews().contains("posix")) {
            FileAttribute<Set<PosixFilePermission>> apenasDono =
                    PosixFilePermissions.asFileAttribute(
                            PosixFilePermissions.fromString("rwx------"));
            Files.createDirectories(base, apenasDono);
            Files.setPosixFilePermissions(base, PosixFilePermissions.fromString("rwx------"));
        } else {
            Files.createDirectories(base);
        }
        return base;
    }
}
