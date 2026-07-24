package br.flmane.servidor.netty;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.zip.GZIPOutputStream;

import br.nnpe.Logger;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;

/**
 * Converte uma {@link RespostaHttp} numa {@link FullHttpResponse} Netty:
 * serializa a entity (JSON via Jackson quando não é {@code byte[]}/
 * {@code String}), aplica o content-type resolvido, comprime com gzip
 * quando a rota pedir (equivalente ao {@code GZIPWriterInterceptor} de
 * hoje) e escreve os headers extras da resposta.
 */
public final class EscritorResposta {

    private EscritorResposta() {
    }

    public static FullHttpResponse escrever(RespostaHttp resposta) {
        byte[] corpo = serializar(resposta.getEntity());
        String contentType = resposta.getContentType() != null
                ? resposta.getContentType() : "application/octet-stream";
        boolean gzip = resposta.isGzip() && corpo.length > 0;
        if (gzip) {
            corpo = comprimir(corpo);
        }

        ByteBuf conteudo = Unpooled.wrappedBuffer(corpo);
        FullHttpResponse httpResponse = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1,
                HttpResponseStatus.valueOf(resposta.getStatus()),
                conteudo);
        httpResponse.headers().set(HttpHeaderNames.CONTENT_TYPE, contentType);
        httpResponse.headers().set(HttpHeaderNames.CONTENT_LENGTH, conteudo.readableBytes());
        if (gzip) {
            httpResponse.headers().set(HttpHeaderNames.CONTENT_ENCODING, "gzip");
        }
        for (Map.Entry<String, String> header : resposta.getHeaders().entrySet()) {
            httpResponse.headers().set(header.getKey(), header.getValue());
        }
        return httpResponse;
    }

    private static byte[] serializar(Object entity) {
        if (entity == null) {
            return new byte[0];
        }
        if (entity instanceof byte[]) {
            return (byte[]) entity;
        }
        if (entity instanceof String) {
            return ((String) entity).getBytes(StandardCharsets.UTF_8);
        }
        try {
            return ContextoRequisicao.OBJECT_MAPPER.writeValueAsBytes(entity);
        } catch (Exception e) {
            Logger.logarExept(e);
            return new byte[0];
        }
    }

    private static byte[] comprimir(byte[] dados) {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            try (GZIPOutputStream gzipOutputStream = new GZIPOutputStream(bos)) {
                gzipOutputStream.write(dados);
            }
            return bos.toByteArray();
        } catch (Exception e) {
            Logger.logarExept(e);
            return dados;
        }
    }
}
