package br.f1mane.servidor;

import java.io.File;

import br.f1mane.servidor.controles.ControlePaddockServidor;
import br.f1mane.servidor.controles.ControlePersistencia;
import br.nnpe.Logger;
import br.f1mane.recursos.idiomas.Lang;

public class PaddockServer {

	private static ControlePaddockServidor controlePaddock;
	private static ControlePersistencia controlePersistencia;
	private static MonitorAtividade monitorAtividade;
	private static Boolean iniciado = Boolean.FALSE;

	public static ControlePaddockServidor getControlePaddock() {
		init(null);
		return controlePaddock;
	}
	public static ControlePersistencia getControlePersistencia() {
		init(null);
		return controlePersistencia;
	}
	public static MonitorAtividade getMonitorAtividade() {
		init(null);
		return monitorAtividade;
	}

	public static synchronized void init(String realpath) {
		if (iniciado.booleanValue()) {
			return;
		}
		Lang.setSrvgame(true);
		try {
			controlePersistencia = new ControlePersistencia(
					realpath + File.separator + "WEB-INF" + File.separator);
		} catch (Exception e) {
			Logger.logarExept(e);
		}
		controlePaddock = new ControlePaddockServidor(controlePersistencia);
		monitorAtividade = new MonitorAtividade(controlePaddock);
		Thread monitor = new Thread(monitorAtividade);
		monitor.start();
		iniciado = Boolean.TRUE;
	}

}
