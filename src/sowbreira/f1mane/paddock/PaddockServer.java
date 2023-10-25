package sowbreira.f1mane.paddock;

import java.io.File;

import br.nnpe.Logger;
import sowbreira.f1mane.paddock.servlet.ControlePaddockServidor;
import sowbreira.f1mane.paddock.servlet.ControlePersistencia;
import sowbreira.f1mane.paddock.servlet.MonitorAtividade;
import sowbreira.f1mane.recursos.idiomas.Lang;

public class PaddockServer {

	private static ControlePaddockServidor controlePaddock;
	private static ControlePersistencia controlePersistencia;
	private static MonitorAtividade monitorAtividade;
	private static Boolean iniciado = false;

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
		if (iniciado) {
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
		iniciado = true;
	}

}
