package br.flmane.servidor.netty;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Substitui o antigo {@code Response} do JAX-RS nos endpoints de
 * {@code LetsRace} — mesmo formato (status/entity/content-type/headers),
 * sem depender de JAX-RS.
 */
public final class RespostaHttp {

    private final int status;
    private final Object entity;
    private final String contentType;
    private final Map<String, String> headers;
    private final boolean gzip;

    private RespostaHttp(int status, Object entity, String contentType,
                          Map<String, String> headers, boolean gzip) {
        this.status = status;
        this.entity = entity;
        this.contentType = contentType;
        this.headers = headers;
        this.gzip = gzip;
    }

    public int getStatus() {
        return status;
    }

    public Object getEntity() {
        return entity;
    }

    public String getContentType() {
        return contentType;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public boolean isGzip() {
        return gzip;
    }

    /**
     * Usado só pelo {@link Roteador}, depois que a rota é casada, para
     * aplicar o content-type padrão da rota (equivalente a {@code @Produces})
     * quando o handler não definiu um explicitamente, e a compressão gzip
     * (equivalente ao {@code @Compress}/{@code GZIPWriterInterceptor} de hoje).
     */
    static RespostaHttp comMetadadosDaRota(RespostaHttp original,
                                            String contentTypePadrao,
                                            boolean gzip) {
        String contentType = original.contentType != null
                ? original.contentType : contentTypePadrao;
        return new RespostaHttp(original.status, original.entity, contentType,
                original.headers, gzip);
    }

    public static Builder status(int status) {
        return new Builder(status);
    }

    public static RespostaHttp ok(Object entity) {
        return status(200).entity(entity).build();
    }

    public static final class Builder {
        private final int status;
        private Object entity;
        private String contentType;
        private final Map<String, String> headers = new LinkedHashMap<>();

        private Builder(int status) {
            this.status = status;
        }

        public Builder entity(Object entity) {
            this.entity = entity;
            return this;
        }

        public Builder tipo(String contentType) {
            this.contentType = contentType;
            return this;
        }

        public Builder header(String nome, String valor) {
            headers.put(nome, valor);
            return this;
        }

        public RespostaHttp build() {
            return new RespostaHttp(status, entity, contentType, headers, false);
        }
    }
}
