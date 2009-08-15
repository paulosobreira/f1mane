package sowbreira.f1mane.paddock.servlet;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import sowbreira.f1mane.paddock.entidades.persistencia.CorridasDadosSrv;
import sowbreira.f1mane.paddock.entidades.persistencia.JogadorDadosSrv;
import sowbreira.f1mane.paddock.entidades.persistencia.PaddockDadosSrv;
import br.nnpe.Dia;
import br.nnpe.Logger;

/**
 * @author Paulo Sobreira Criado em 20/10/2007 as 14:19:54
 */
public class ControlePersistencia {

	private String basePath;
	private String nomeArquivo = "paddockDadosSrv.zip";
	private static PaddockDadosSrv paddockDadosSrv;

	/**
	 * @param basePath
	 * @throws FileNotFoundException
	 */
	public ControlePersistencia(String basePath) throws Exception {
		super();
		this.basePath = basePath;

		try {
			if (paddockDadosSrv == null) {
				Logger.logar("============ Antes ==========================");
				Logger.logar("heapSize " + Runtime.getRuntime().totalMemory()
						/ 1024 + " kb");
				Logger.logar("heapMaxSize " + Runtime.getRuntime().maxMemory()
						/ 1024 + " kb");
				Logger.logar("heapFreeSize "
						+ Runtime.getRuntime().freeMemory() / 1024 + " kb");
				paddockDadosSrv = lerDados();
				Logger.logar("============ Depois==========================");
				Logger.logar("heapSize " + Runtime.getRuntime().totalMemory()
						/ 1024 + " kb");
				Logger.logar("heapMaxSize " + Runtime.getRuntime().maxMemory()
						/ 1024 + " kb");
				Logger.logar("heapFreeSize "
						+ Runtime.getRuntime().freeMemory() / 1024 + " kb");
			}

		} catch (Exception e) {
			Logger.logarExept(e);
			paddockDadosSrv = new PaddockDadosSrv();
		}

	}

	public void gravarDados() throws IOException {
		synchronized (paddockDadosSrv) {
			processarLimpesa(paddockDadosSrv);
			paddockDadosSrv.setLastSave(System.currentTimeMillis());
			ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
			XMLEncoder encoder = new XMLEncoder(byteArrayOutputStream);
			encoder.writeObject(paddockDadosSrv);
			encoder.flush();
			ZipOutputStream zipOutputStream = new ZipOutputStream(
					new FileOutputStream(basePath + nomeArquivo));
			ZipEntry entry = new ZipEntry("paddockDadosSrv.xml");
			zipOutputStream.putNextEntry(entry);
			zipOutputStream.write(byteArrayOutputStream.toByteArray());
			zipOutputStream.closeEntry();
			zipOutputStream.close();
			ZipOutputStream zipOutputStreamBak = new ZipOutputStream(
					new FileOutputStream(basePath + "BAK_" + nomeArquivo));
			ZipEntry entryBak = new ZipEntry("paddockDadosSrv.xml");
			zipOutputStreamBak.putNextEntry(entryBak);
			zipOutputStreamBak.write(byteArrayOutputStream.toByteArray());
			zipOutputStreamBak.closeEntry();
			zipOutputStreamBak.close();
		}
	}

	private void processarLimpesa(PaddockDadosSrv pds) {
		Map map = pds.getJogadoresMap();
		for (Iterator iter = map.keySet().iterator(); iter.hasNext();) {
			String key = (String) iter.next();
			JogadorDadosSrv jogadorDadosSrv = (JogadorDadosSrv) map.get(key);
			List corridas = jogadorDadosSrv.getCorridas();
			for (Iterator iterator = corridas.iterator(); iterator.hasNext();) {
				CorridasDadosSrv corridasDadosSrv = (CorridasDadosSrv) iterator
						.next();
				if (corridasDadosSrv.getPontos() == 0) {
					iterator.remove();
				}

			}
			Dia dia = new Dia(jogadorDadosSrv.getUltimoLogon());
			Dia hj = new Dia();
			if (hj.daysBetween(dia) > 60 && corridas.size() < 20) {
				iter.remove();
			}
		}

	}

	private PaddockDadosSrv lerDados() throws Exception {
		ZipInputStream zin = new ZipInputStream(new FileInputStream(basePath
				+ nomeArquivo));
		zin.getNextEntry();
		ByteArrayOutputStream arrayDinamico = new ByteArrayOutputStream();
		int byt = zin.read();

		while (-1 != byt) {
			arrayDinamico.write(byt);
			byt = zin.read();
		}

		arrayDinamico.flush();

		ByteArrayInputStream bin = new ByteArrayInputStream(arrayDinamico
				.toByteArray());
		XMLDecoder decoder = new XMLDecoder(bin);
		return (PaddockDadosSrv) decoder.readObject();
	}

	public static void main(String[] args) throws Exception {
		// ControlePersistencia controlePersistencia = new ControlePersistencia(
		// "d:" + File.separator);
		// controlePersistencia.paddockDadosSrv.getJogadoresMap().put("teste3",
		// "Paulo sobreira");
		// controlePersistencia.gravarDados();
		Dia dia = new Dia("01/06/2009");
		Dia hj = new Dia();
		Logger.logar(hj.daysBetween(dia));
	}

	public byte[] obterBytesBase(String tipo) {
		try {
			synchronized (paddockDadosSrv) {

				ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream();
				BufferedInputStream bufferedInputStream = new BufferedInputStream(
						new FileInputStream(basePath + tipo + nomeArquivo));
				int byt = bufferedInputStream.read();

				while (-1 != byt) {
					arrayOutputStream.write(byt);
					byt = bufferedInputStream.read();
				}
				return arrayOutputStream.toByteArray();
			}
		} catch (Exception e) {
		}
		return null;
	}

	public JogadorDadosSrv carregaDadosJogador(String nomeJogador) {
		JogadorDadosSrv jogadorDadosSrv = (JogadorDadosSrv) paddockDadosSrv
				.getJogadoresMap().get(nomeJogador);
		return jogadorDadosSrv;
	}

	public Object getPaddockDados() {
		return nomeArquivo;
	}

	public Set obterListaJogadores() {
		return paddockDadosSrv.getJogadoresMap().keySet();
	}

	public void adicionarJogador(String nome, JogadorDadosSrv jogadorDadosSrv) {
		paddockDadosSrv.getJogadoresMap().put(nome, jogadorDadosSrv);

	}

}
