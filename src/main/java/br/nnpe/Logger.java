package br.nnpe;

import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class Logger {

    private static final org.slf4j.Logger LOG =
            LoggerFactory.getLogger("FLMANE");

    public static Map<String, Integer> topExceptions =
            new LinkedHashMap<>();

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

    public static void topExecpts(Throwable e) {
        if (topExceptions.size() > 10000) {
            return;
        }
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