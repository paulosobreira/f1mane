package br.flmane.servidor.netty;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import br.nnpe.Logger;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;

/**
 * Router HTTP enxuto: casa {@code (HttpMethod, path)} contra os templates
 * registrados (segmentos estáticos + {@code {token}}), sem depender de
 * anotações JAX-RS. Cada rota carrega seu content-type padrão (equivalente
 * a {@code @Produces}) e sua flag de compressão (equivalente a
 * {@code @Compress}), aplicados à resposta do handler depois do despacho.
 */
public final class Roteador {

    @FunctionalInterface
    public interface RotaHandler {
        RespostaHttp lidar(ContextoRequisicao ctx) throws Exception;
    }

    private static final class Rota {
        final HttpMethod metodo;
        final String[] segmentos;
        final String contentTypePadrao;
        final boolean gzip;
        final RotaHandler handler;

        Rota(HttpMethod metodo, String template, String contentTypePadrao,
             boolean gzip, RotaHandler handler) {
            this.metodo = metodo;
            String semBarraInicial = template.startsWith("/")
                    ? template.substring(1) : template;
            this.segmentos = semBarraInicial.isEmpty()
                    ? new String[0] : semBarraInicial.split("/");
            this.contentTypePadrao = contentTypePadrao;
            this.gzip = gzip;
            this.handler = handler;
        }

        Map<String, String> casar(HttpMethod metodoReq, String[] segmentosPath) {
            if (!this.metodo.equals(metodoReq)
                    || this.segmentos.length != segmentosPath.length) {
                return null;
            }
            Map<String, String> parametros = new LinkedHashMap<>();
            for (int i = 0; i < segmentos.length; i++) {
                String segmentoRota = segmentos[i];
                if (segmentoRota.startsWith("{") && segmentoRota.endsWith("}")) {
                    parametros.put(
                            segmentoRota.substring(1, segmentoRota.length() - 1),
                            URLDecoder.decode(segmentosPath[i], StandardCharsets.UTF_8));
                } else if (!segmentoRota.equals(segmentosPath[i])) {
                    return null;
                }
            }
            return parametros;
        }
    }

    private final List<Rota> rotas = new ArrayList<>();

    public void registrar(HttpMethod metodo, String template,
                           String contentTypePadrao, boolean gzip,
                           RotaHandler handler) {
        rotas.add(new Rota(metodo, template, contentTypePadrao, gzip, handler));
    }

    /**
     * @param path já sem o prefixo do recurso (ex.: {@code /circuito}, sem
     *             {@code /flmane/rest/letsRace}) e sem query string
     * @return a resposta da rota casada (nunca null com content-type nulo se
     * a rota tiver um padrão), ou {@code null} se nenhuma rota casar
     */
    public RespostaHttp despachar(HttpMethod metodo, String path,
                                   FullHttpRequest requisicao) {
        String semBarraInicial = path.startsWith("/") ? path.substring(1) : path;
        String[] segmentosPath = semBarraInicial.isEmpty()
                ? new String[0] : semBarraInicial.split("/");
        for (Rota rota : rotas) {
            Map<String, String> parametros = rota.casar(metodo, segmentosPath);
            if (parametros == null) {
                continue;
            }
            try {
                RespostaHttp resposta = rota.handler.lidar(
                        new ContextoRequisicao(requisicao, parametros));
                return RespostaHttp.comMetadadosDaRota(
                        resposta, rota.contentTypePadrao, rota.gzip);
            } catch (Exception e) {
                Logger.logarExept(e);
                return RespostaHttp.status(500)
                        .entity("Server error.")
                        .tipo("text/plain").build();
            }
        }
        return null;
    }
}
