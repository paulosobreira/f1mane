package br.nnpe;

import sowbreira.f1mane.paddock.servlet.ServletBaseDados;
/**
 * @author Paulo Sobreira
 * Criado em 25/10/2009 as 17:27:25
 */
public class Logger {

	public static boolean ativo = false;

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
		} else if (e instanceof Exception) {
			ServletBaseDados.topExecpts((Exception) e);
		}
	}
}
