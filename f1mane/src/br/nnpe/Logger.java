package br.nnpe;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Paulo Sobreira Criado em 25/10/2009 as 17:27:25
 */
public class Logger {

	public static Map topExceptions = new HashMap();

	public static boolean ativo = false;

	public static boolean novaSession = false;

	public static void topExecpts(Exception e) {
		novaSession = true;
		if (ativo) {
			e.printStackTrace();
		}
		if (topExceptions == null) {
			topExceptions = new HashMap();
		}
		if (topExceptions.size() < 100) {
			StackTraceElement[] trace = e.getStackTrace();
			StringBuffer retorno = new StringBuffer();
			int size = ((trace.length > 15) ? 15 : trace.length);
			retorno.append(e.getClass() + " - " + e.getLocalizedMessage()
					+ "<br>");
			for (int i = 0; i < size; i++)
				retorno.append(trace[i] + "<br>");
			String val = retorno.toString();
			Integer numExceps = (Integer) topExceptions.get(val);
			if (numExceps == null) {
				topExceptions.put(val, new Integer(1));
			} else {
				topExceptions.put(val, new Integer(numExceps.intValue() + 1));
			}
		}

	}

	public static void logar(String val) {
		if (ativo) {
			System.out.println(val);
		}

	}

	public static void logar(int val) {
		if (ativo) {
			System.out.println(val);
		}

	}

	public static void logar(double val) {
		if (ativo) {
			System.out.println(val);
		}

	}

	public static void logar(Object val) {
		if (ativo) {
			System.out.println(val);
		}
	}

	public static void logarExept(Throwable e) {
		if (ativo) {
			e.printStackTrace();
			novaSession = true;
		} else if (e instanceof Exception) {
			topExecpts((Exception) e);
		}
	}
}
