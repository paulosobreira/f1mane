package br.flmane.servidor.netty;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import br.flmane.servidor.rest.LetsRace;
import br.flmane.servidor.rest.RotasLetsRace;
import br.flmane.servidor.servlet.PaginaConf;
import br.flmane.servidor.servlet.ServletPaddock;
import br.nnpe.Logger;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.QueryStringDecoder;

/**
 * Dispatcher HTTP único do modo servidor: substitui o Tomcat + web.xml,
 * roteando por prefixo de path para o protocolo de {@link ServletPaddock},
 * o router de {@code LetsRace}, {@code conf.jsp} ou os arquivos estáticos de
 * {@code webapp/} — todos sob o contexto {@code /flmane}, exatamente como
 * antes. Uma única instância é compartilhada entre conexões (sem estado por
 * requisição), por isso é {@code @Sharable}.
 */
@ChannelHandler.Sharable
public final class FlmaneHttpDispatcher extends SimpleChannelInboundHandler<FullHttpRequest> {

    private static final String CONTEXTO = "/flmane";
    private static final String PREFIXO_LETSRACE = "/flmane/rest/letsRace";
    private static final String PATH_SERVLET_PADDOCK = "/flmane/ServletPaddock";
    private static final String PATH_CONF = "/flmane/conf.jsp";

    private final ServletPaddock servletPaddock;
    private final Roteador letsRaceRouter;
    private final ArquivoEstaticoHandler arquivoEstaticoHandler;

    public FlmaneHttpDispatcher(Path raizWebapp) {
        this.servletPaddock = new ServletPaddock();
        this.letsRaceRouter = RotasLetsRace.criar(new LetsRace());
        this.arquivoEstaticoHandler = new ArquivoEstaticoHandler(raizWebapp);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest requisicao) {
        FullHttpResponse resposta = despachar(requisicao);
        boolean manterConexao = HttpUtil.isKeepAlive(requisicao);
        if (manterConexao) {
            resposta.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
        }
        ChannelFuture future = ctx.writeAndFlush(resposta);
        if (!manterConexao) {
            future.addListener(ChannelFutureListener.CLOSE);
        }
    }

    private FullHttpResponse despachar(FullHttpRequest requisicao) {
        try {
            String path = new QueryStringDecoder(requisicao.uri()).path();
            if (PATH_SERVLET_PADDOCK.equals(path)) {
                return servletPaddock.manipular(requisicao);
            }
            if (PATH_CONF.equals(path)) {
                return respostaHtml(PaginaConf.gerar(hostName(requisicao)));
            }
            if (path.startsWith(PREFIXO_LETSRACE)) {
                String remanescente = path.substring(PREFIXO_LETSRACE.length());
                RespostaHttp resposta = letsRaceRouter.despachar(
                        requisicao.method(),
                        remanescente.isEmpty() ? "/" : remanescente,
                        requisicao);
                return resposta == null
                        ? respostaVazia(HttpResponseStatus.NOT_FOUND)
                        : EscritorResposta.escrever(resposta);
            }
            if (path.equals(CONTEXTO) || path.startsWith(CONTEXTO + "/")) {
                return arquivoEstaticoHandler.servir(path.substring(CONTEXTO.length()));
            }
            return respostaVazia(HttpResponseStatus.NOT_FOUND);
        } catch (Exception e) {
            Logger.logarExept(e);
            return respostaVazia(HttpResponseStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private String hostName(FullHttpRequest requisicao) {
        String host = requisicao.headers().get(HttpHeaderNames.HOST);
        if (host == null) {
            return "localhost";
        }
        int indiceDaPorta = host.indexOf(':');
        return indiceDaPorta < 0 ? host : host.substring(0, indiceDaPorta);
    }

    private FullHttpResponse respostaHtml(String html) {
        byte[] corpo = html.getBytes(StandardCharsets.UTF_8);
        FullHttpResponse resposta = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1, HttpResponseStatus.OK, Unpooled.wrappedBuffer(corpo));
        resposta.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/html;charset=UTF-8");
        resposta.headers().set(HttpHeaderNames.CONTENT_LENGTH, corpo.length);
        return resposta;
    }

    private FullHttpResponse respostaVazia(HttpResponseStatus status) {
        FullHttpResponse resposta = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1, status, Unpooled.EMPTY_BUFFER);
        resposta.headers().set(HttpHeaderNames.CONTENT_LENGTH, 0);
        return resposta;
    }
}
