package br.flmane.servidor.netty;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;

/**
 * Cobertura do casamento de rotas do {@link Roteador} — a peça nova que
 * substitui a resolução de {@code @Path}/{@code @GET}/{@code @POST} do
 * JAX-RS, apontada em design.md como o maior risco da migração (parâmetro
 * mal casado gerando 500 silencioso ou parse errado num dos endpoints de
 * LetsRace).
 */
class RoteadorTest {

    private static FullHttpRequest requisicao(HttpMethod metodo, String uri) {
        return new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, metodo, uri);
    }

    @Test
    void casaSegmentosEstaticos() {
        Roteador roteador = new Roteador();
        roteador.registrar(HttpMethod.GET, "/circuitos", "application/json", false,
                ctx -> RespostaHttp.ok("lista"));

        RespostaHttp resposta = roteador.despachar(HttpMethod.GET, "/circuitos",
                requisicao(HttpMethod.GET, "/circuitos"));

        assertEquals(200, resposta.getStatus());
        assertEquals("lista", resposta.getEntity());
    }

    @Test
    void capturaParametroDinamico() {
        Roteador roteador = new Roteador();
        roteador.registrar(HttpMethod.GET, "/renovarSessaoVisitante/{token}", "application/json", false,
                ctx -> RespostaHttp.ok(ctx.path("token")));

        RespostaHttp resposta = roteador.despachar(HttpMethod.GET,
                "/renovarSessaoVisitante/abc-123",
                requisicao(HttpMethod.GET, "/renovarSessaoVisitante/abc-123"));

        assertEquals("abc-123", resposta.getEntity());
    }

    @Test
    void naoCasaQuandoNumeroDeSegmentosDifere() {
        Roteador roteador = new Roteador();
        roteador.registrar(HttpMethod.GET, "/temporadas/{temporada}", "application/json", false,
                ctx -> RespostaHttp.ok(ctx.path("temporada")));

        RespostaHttp resposta = roteador.despachar(HttpMethod.GET, "/temporadas",
                requisicao(HttpMethod.GET, "/temporadas"));

        assertNull(resposta);
    }

    @Test
    void getEPostNoMesmoPathSaoRotasDistintas() {
        Roteador roteador = new Roteador();
        roteador.registrar(HttpMethod.GET, "/equipe", "application/json", false,
                ctx -> RespostaHttp.ok("leitura"));
        roteador.registrar(HttpMethod.POST, "/equipe", "application/json", false,
                ctx -> RespostaHttp.ok("gravacao"));

        RespostaHttp get = roteador.despachar(HttpMethod.GET, "/equipe",
                requisicao(HttpMethod.GET, "/equipe"));
        RespostaHttp post = roteador.despachar(HttpMethod.POST, "/equipe",
                requisicao(HttpMethod.POST, "/equipe"));

        assertEquals("leitura", get.getEntity());
        assertEquals("gravacao", post.getEntity());
    }

    @Test
    void aplicaContentTypePadraoQuandoHandlerNaoDefine() {
        Roteador roteador = new Roteador();
        roteador.registrar(HttpMethod.GET, "/carroLado/{temporada}/{carro}", "image/png", false,
                ctx -> RespostaHttp.status(200).entity(new byte[]{1, 2, 3}).build());

        RespostaHttp resposta = roteador.despachar(HttpMethod.GET, "/carroLado/2024/1",
                requisicao(HttpMethod.GET, "/carroLado/2024/1"));

        assertEquals("image/png", resposta.getContentType());
    }

    @Test
    void handlerPodeSobrescreverContentTypePadrao() {
        Roteador roteador = new Roteador();
        roteador.registrar(HttpMethod.GET, "/circuito", "image/png", false,
                ctx -> RespostaHttp.status(500).entity("erro").tipo("application/json").build());

        RespostaHttp resposta = roteador.despachar(HttpMethod.GET, "/circuito",
                requisicao(HttpMethod.GET, "/circuito"));

        assertEquals("application/json", resposta.getContentType());
    }

    @Test
    void propagaFlagDeCompressaoDaRota() {
        Roteador roteador = new Roteador();
        roteador.registrar(HttpMethod.GET, "/classificacaoGeral", "application/json", true,
                ctx -> RespostaHttp.ok("dados"));
        roteador.registrar(HttpMethod.GET, "/verificaServico", "application/json", false,
                ctx -> RespostaHttp.ok("ok"));

        RespostaHttp comCompressao = roteador.despachar(HttpMethod.GET, "/classificacaoGeral",
                requisicao(HttpMethod.GET, "/classificacaoGeral"));
        RespostaHttp semCompressao = roteador.despachar(HttpMethod.GET, "/verificaServico",
                requisicao(HttpMethod.GET, "/verificaServico"));

        assertTrue(comCompressao.isGzip());
        assertTrue(!semCompressao.isGzip());
    }

    @Test
    void extraiParametroDeQueryEDeHeader() {
        Roteador roteador = new Roteador();
        roteador.registrar(HttpMethod.GET, "/circuito", "application/json", false,
                ctx -> RespostaHttp.ok(ctx.query("nomeCircuito") + "|" + ctx.header("token")));

        FullHttpRequest req = requisicao(HttpMethod.GET, "/circuito?nomeCircuito=interlagos");
        req.headers().set("token", "tok-1");

        RespostaHttp resposta = roteador.despachar(HttpMethod.GET, "/circuito", req);

        assertEquals("interlagos|tok-1", resposta.getEntity());
    }

    @Test
    void excecaoNoHandlerViraQuinhentos() {
        Roteador roteador = new Roteador();
        roteador.registrar(HttpMethod.GET, "/circuito", "application/json", false,
                ctx -> {
                    throw new RuntimeException("boom");
                });

        RespostaHttp resposta = roteador.despachar(HttpMethod.GET, "/circuito",
                requisicao(HttpMethod.GET, "/circuito"));

        assertEquals(500, resposta.getStatus());
    }

    @Test
    void retornaNullQuandoNenhumaRotaCasa() {
        Roteador roteador = new Roteador();
        roteador.registrar(HttpMethod.GET, "/circuito", "application/json", false,
                ctx -> RespostaHttp.ok("x"));

        RespostaHttp resposta = roteador.despachar(HttpMethod.GET, "/inexistente",
                requisicao(HttpMethod.GET, "/inexistente"));

        assertNull(resposta);
    }
}
