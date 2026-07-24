package br.flmane.servidor.servlet;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.Timestamp;
import java.util.List;

import br.flmane.servidor.MonitorAtividade;
import br.flmane.servidor.PaddockConstants;
import br.flmane.servidor.PaddockServer;
import br.flmane.servidor.controles.ControlePaddockServidor;
import br.flmane.servidor.entidades.TOs.SessaoCliente;
import br.flmane.servidor.util.ZipUtil;
import br.nnpe.FormatDate;
import br.nnpe.Logger;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.QueryStringDecoder;

/**
 * Handler Netty do protocolo de serialização Java em {@code /ServletPaddock}
 * (usado pelo cliente multiplayer {@code AppletPaddock}) e das páginas HTML
 * de admin ({@code ?tipo=X} exceptions, {@code ?tipo=S} sessões) — mesmo
 * comportamento de quando esta classe era um {@code HttpServlet}, incluindo
 * o corte precoce da página quando não há parâmetro {@code tipo}.
 *
 * @author paulo.sobreira
 */
public class ServletPaddock {

    private static final String LOCK = "lock";

    private final ControlePaddockServidor controlePaddock;
    private final MonitorAtividade monitorAtividade;

    public ServletPaddock() {
        Logger.logar("Init");
        PaddockServer.init(null);
        this.controlePaddock = PaddockServer.getControlePaddock();
        this.monitorAtividade = PaddockServer.getMonitorAtividade();
    }

    public void destruir() {
        monitorAtividade.setAlive(false);
    }

    public FullHttpResponse manipular(FullHttpRequest requisicao) {
        try {
            Object objetoLido = lerObjeto(requisicao);
            if (objetoLido != null) {
                return respostaBinaria(processarObjeto(objetoLido));
            }
            return respostaHtml(paginaHtml(requisicao));
        } catch (Exception e) {
            Logger.topExecpts(e);
            return respostaBinaria(new byte[0]);
        }
    }

    private Object lerObjeto(FullHttpRequest requisicao) {
        byte[] corpo = ByteBufUtil.getBytes(requisicao.content());
        if (corpo.length == 0) {
            return null;
        }
        try (ObjectInputStream inputStream =
                     new ObjectInputStream(new ByteArrayInputStream(corpo))) {
            return inputStream.readObject();
        } catch (Exception e) {
            Logger.logar("inputStream null - > doGetHtml");
            return null;
        }
    }

    private byte[] processarObjeto(Object objetoLido) throws Exception {
        Object escrever = controlePaddock.processarObjetoRecebido(objetoLido);
        if (PaddockConstants.modoZip) {
            ByteArrayOutputStream zipOut = new ByteArrayOutputStream();
            ZipUtil.compactarObjeto(false, escrever, zipOut);
            return zipOut.toByteArray();
        }
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(escrever);
        oos.flush();
        return bos.toByteArray();
    }

    private String paginaHtml(FullHttpRequest requisicao) {
        StringBuilder html = new StringBuilder();
        try {
            abrirHtml5(html);
            html.append("<body>");
            String tipo = parametroTipo(requisicao);
            if (tipo == null) {
                return html.toString();
            } else if ("X".equals(tipo)) {
                topExceptions(html);
            } else if ("S".equals(tipo)) {
                sessoesAtivas(html);
            }
            html.append("<br/> ");
        } catch (Exception e) {
            html.append(e.getMessage());
        }
        html.append("<br/><a href='conf.jsp'>back</a>");
        html.append("</body></html>");
        return html.toString();
    }

    private String parametroTipo(FullHttpRequest requisicao) {
        List<String> valores = new QueryStringDecoder(requisicao.uri())
                .parameters().get("tipo");
        return (valores == null || valores.isEmpty()) ? null : valores.get(0);
    }

    private void topExceptions(StringBuilder html) {
        abrirHtml5(html);
        html.append("<body>");
        html.append("<h2>Fl-Mane Exceptions</h2><br><hr>");
        synchronized (LOCK) {
            for (String excecao : Logger.topExceptions.keySet()) {
                html.append("Quantidade : ").append(Logger.topExceptions.get(excecao));
                html.append("<br>");
                html.append(excecao);
                html.append("<br><hr>");
            }
        }
    }

    private void sessoesAtivas(StringBuilder html) {
        abrirHtml5(html);
        html.append("<body>");
        html.append("<h2>Fl-Mane Sess&otilde;es</h2><br>");
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        html.append("Hora Servidor : ").append(FormatDate.format(timestamp));
        html.append("<br><hr>");
        List<SessaoCliente> clientes = controlePaddock.getDadosPaddock().getClientes();
        int cont = 0;
        for (SessaoCliente sessaoCliente : clientes) {
            html.append("<br>");
            html.append("Jogador : ").append(sessaoCliente.getNomeJogador());
            html.append("<br>");
            timestamp = new Timestamp(sessaoCliente.getUlimaAtividade());
            html.append("&Uacute;ltima Atividade : ").append(FormatDate.format(timestamp));
            html.append("<br>");
            html.append("Jogo Atual : ").append(sessaoCliente.getJogoAtual());
            html.append("<hr>");
            cont++;
        }
        html.append("<br>");
        html.append("Total : ").append(cont);
        html.append("<br>");
    }

    private void abrirHtml5(StringBuilder html) {
        html.append("<!doctype html>");
        html.append("<html><head>");
        html.append("<meta http-equiv='Content-Type' content='text/html; charset=utf-8'>");
        html.append("<meta name='viewport' content='width=device-width, initial-scale=1, maximum-scale=1, user-scalable=no'>");
        html.append("</head>");
    }

    private FullHttpResponse respostaBinaria(byte[] corpo) {
        FullHttpResponse resposta = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1, HttpResponseStatus.OK, Unpooled.wrappedBuffer(corpo));
        resposta.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/octet-stream");
        resposta.headers().set(HttpHeaderNames.CONTENT_LENGTH, corpo.length);
        return resposta;
    }

    private FullHttpResponse respostaHtml(String html) {
        byte[] corpo = html.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        FullHttpResponse resposta = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1, HttpResponseStatus.OK, Unpooled.wrappedBuffer(corpo));
        resposta.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/html;charset=UTF-8");
        resposta.headers().set(HttpHeaderNames.CONTENT_LENGTH, corpo.length);
        return resposta;
    }
}
