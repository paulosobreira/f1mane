package br.flmane.servidor.netty;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import br.nnpe.Logger;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;

/**
 * Serve os arquivos estáticos extraídos de {@code webapp/} (equivalente ao
 * que o Tomcat servia hoje sob o contexto {@code /flmane}), com
 * {@code index.html} como documento padrão em diretórios.
 */
public final class ArquivoEstaticoHandler {

    private static final Map<String, String> CONTENT_TYPES = Map.ofEntries(
            Map.entry("html", "text/html;charset=UTF-8"),
            Map.entry("htm", "text/html;charset=UTF-8"),
            Map.entry("css", "text/css"),
            Map.entry("js", "application/javascript"),
            Map.entry("json", "application/json"),
            Map.entry("jpg", "image/jpeg"),
            Map.entry("jpeg", "image/jpeg"),
            Map.entry("png", "image/png"),
            Map.entry("gif", "image/gif"),
            Map.entry("ico", "image/x-icon"),
            Map.entry("svg", "image/svg+xml"),
            Map.entry("woff", "font/woff"),
            Map.entry("woff2", "font/woff2"),
            Map.entry("ttf", "font/ttf"),
            Map.entry("eot", "application/vnd.ms-fontobject"),
            Map.entry("txt", "text/plain;charset=UTF-8"));

    private final Path raiz;

    public ArquivoEstaticoHandler(Path raiz) {
        this.raiz = raiz.normalize();
    }

    public FullHttpResponse servir(String path) {
        String semBarraInicial = path.startsWith("/") ? path.substring(1) : path;
        Path candidato = raiz.resolve(semBarraInicial).normalize();
        if (!candidato.startsWith(raiz)) {
            return resposta(HttpResponseStatus.FORBIDDEN, new byte[0], "text/plain");
        }
        if (Files.isDirectory(candidato)) {
            candidato = candidato.resolve("index.html");
        }
        if (!Files.isRegularFile(candidato)) {
            return resposta(HttpResponseStatus.NOT_FOUND, new byte[0], "text/plain");
        }
        try {
            byte[] conteudo = Files.readAllBytes(candidato);
            return resposta(HttpResponseStatus.OK, conteudo, contentTypeDe(candidato));
        } catch (IOException e) {
            Logger.logarExept(e);
            return resposta(HttpResponseStatus.INTERNAL_SERVER_ERROR, new byte[0], "text/plain");
        }
    }

    private String contentTypeDe(Path arquivo) {
        String nome = arquivo.getFileName().toString();
        int indicePonto = nome.lastIndexOf('.');
        if (indicePonto < 0) {
            return "application/octet-stream";
        }
        String extensao = nome.substring(indicePonto + 1).toLowerCase();
        return CONTENT_TYPES.getOrDefault(extensao, "application/octet-stream");
    }

    private FullHttpResponse resposta(HttpResponseStatus status, byte[] corpo, String contentType) {
        FullHttpResponse httpResponse = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1, status, Unpooled.wrappedBuffer(corpo));
        httpResponse.headers().set(HttpHeaderNames.CONTENT_TYPE, contentType);
        httpResponse.headers().set(HttpHeaderNames.CONTENT_LENGTH, corpo.length);
        return httpResponse;
    }
}
