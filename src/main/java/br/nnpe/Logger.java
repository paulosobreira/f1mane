package br.nnpe;

import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class Logger {

    private static final org.slf4j.Logger LOG =
            LoggerFactory.getLogger("FLMANE");

    /**
     * Limite de assinaturas distintas de exceção retidas em memória. Ao
     * estourar, a entrada mais antiga (por ordem de primeira ocorrência) é
     * descartada — num container de uptime longo o mapa antes crescia até
     * este teto e, ao enchê-lo, parava de registrar qualquer exceção nova,
     * ficando preso ao diagnóstico do começo da vida do processo.
     */
    public static final int MAX_TOP_EXCEPTIONS = 10000;

    public static Map<String, Integer> topExceptions =
            new LinkedHashMap<String, Integer>() {
                @Override
                protected boolean removeEldestEntry(
                        Map.Entry<String, Integer> eldest) {
                    return size() > MAX_TOP_EXCEPTIONS;
                }
            };

    public static void logar(String val) {
        LOG.info(val);
    }

    public static void logar(int val) {
        LOG.info(String.valueOf(val));
    }

    public static void logar(double val) {
        LOG.info(String.valueOf(val));
    }

    public static Object logar(Object val) {
        LOG.info(String.valueOf(val));
        return val;
    }

    public static void logarExept(Throwable e) {
        LOG.error(e.toString(), e);
        topExecpts(e);
    }

    public static synchronized void topExecpts(Throwable e) {
        StackTraceElement[] trace = e.getStackTrace();
        StringBuilder retorno = new StringBuilder();
        int size = Math.min(trace.length, 15);
        retorno.append(e.getClass())
                .append(" - ")
                .append(e.getLocalizedMessage())
                .append("<br>");
        for (int i = 0; i < size; i++) {
            retorno.append(trace[i]).append("<br>");
        }
        String val = retorno.toString();
        Integer numExceps = topExceptions.get(val);
        if (numExceps == null) {
            topExceptions.put(val, 1);
        } else {
            topExceptions.put(val, numExceps + 1);
        }
    }
}