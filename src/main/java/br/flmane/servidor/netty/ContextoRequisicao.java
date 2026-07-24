package br.flmane.servidor.netty;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;

/**
 * Contexto de uma requisição já casada com uma rota: dá acesso aos
 * parâmetros de path, query e header, e ao corpo (desserializado via
 * Jackson), sem expor o tipo do Netty para os handlers de {@code LetsRace}.
 */
public final class ContextoRequisicao {

    static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final FullHttpRequest requisicao;
    private final Map<String, String> parametrosPath;
    private final QueryStringDecoder queryStringDecoder;

    ContextoRequisicao(FullHttpRequest requisicao,
                        Map<String, String> parametrosPath) {
        this.requisicao = requisicao;
        this.parametrosPath = parametrosPath;
        this.queryStringDecoder = new QueryStringDecoder(requisicao.uri());
    }

    public String path(String nome) {
        return parametrosPath.get(nome);
    }

    public String query(String nome) {
        List<String> valores = queryStringDecoder.parameters().get(nome);
        return (valores == null || valores.isEmpty()) ? null : valores.get(0);
    }

    public String header(String nome) {
        return requisicao.headers().get(nome);
    }

    public <T> T corpoComo(Class<T> tipo) throws IOException {
        String corpo = requisicao.content().toString(StandardCharsets.UTF_8);
        return OBJECT_MAPPER.readValue(corpo, tipo);
    }
}
