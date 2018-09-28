package sowbreira.f1mane.paddock.servlet;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.swing.JFileChooser;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import br.nnpe.Constantes;
import br.nnpe.Dia;
import br.nnpe.HibernateUtil;
import br.nnpe.Logger;
import br.nnpe.Util;
import sowbreira.f1mane.entidades.Carro;
import sowbreira.f1mane.entidades.Piloto;
import sowbreira.f1mane.paddock.PaddockConstants;
import sowbreira.f1mane.paddock.entidades.TOs.MsgSrv;
import sowbreira.f1mane.paddock.entidades.persistencia.CampeonatoSrv;
import sowbreira.f1mane.paddock.entidades.persistencia.CarreiraDadosSrv;
import sowbreira.f1mane.paddock.entidades.persistencia.CorridaCampeonatoSrv;
import sowbreira.f1mane.paddock.entidades.persistencia.CorridasDadosSrv;
import sowbreira.f1mane.paddock.entidades.persistencia.F1ManeDados;
import sowbreira.f1mane.paddock.entidades.persistencia.JogadorDadosSrv;
import sowbreira.f1mane.paddock.entidades.persistencia.PaddockDadosSrv;
import sowbreira.f1mane.recursos.CarregadorRecursos;
import sowbreira.f1mane.recursos.idiomas.Lang;

/**
 * @author Paulo Sobreira Criado em 20/10/2007 as 14:19:54
 */
public class ControlePersistencia {

	private String basePath;
	private String nomeArquivo = "paddockDadosSrv.zip";
	private static PaddockDadosSrv paddockDadosSrv;

	private String webInfDir;

	private String webDir;

	public Session getSession() {
		if (!Constantes.DATABASE) {
			return null;
		}
		return HibernateUtil.getSessionFactory().openSession();
	}

	public ControlePersistencia(String webDir, String webInfDir) {
		super();
		this.webInfDir = webInfDir;
		this.webDir = webDir;
		try {
			Properties properties = new Properties();
			properties.load(PaddockConstants.class
					.getResourceAsStream("server.properties"));
		} catch (Exception e) {
			Logger.logarExept(e);
		}
	}

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
				Logger.logar("heapSize "
						+ Runtime.getRuntime().totalMemory() / 1024 + " kb");
				Logger.logar("heapMaxSize "
						+ Runtime.getRuntime().maxMemory() / 1024 + " kb");
				Logger.logar("heapFreeSize "
						+ Runtime.getRuntime().freeMemory() / 1024 + " kb");
				// paddockDadosSrv = lerDados();
				Logger.logar("============ Depois==========================");
				Logger.logar("heapSize "
						+ Runtime.getRuntime().totalMemory() / 1024 + " kb");
				Logger.logar("heapMaxSize "
						+ Runtime.getRuntime().maxMemory() / 1024 + " kb");
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
			processarLimpeza(paddockDadosSrv);
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

	private void processarLimpeza(PaddockDadosSrv pds) {
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
		ZipInputStream zin = new ZipInputStream(
				new FileInputStream(basePath + nomeArquivo));
		zin.getNextEntry();
		ByteArrayOutputStream arrayDinamico = new ByteArrayOutputStream();
		int byt = zin.read();

		while (-1 != byt) {
			arrayDinamico.write(byt);
			byt = zin.read();
		}

		arrayDinamico.flush();

		ByteArrayInputStream bin = new ByteArrayInputStream(
				arrayDinamico.toByteArray());
		XMLDecoder decoder = new XMLDecoder(bin);
		return (PaddockDadosSrv) decoder.readObject();
	}

	public void migrar() throws Exception {
		JFileChooser fileChooser = new JFileChooser(CarregadorRecursos.class
				.getResource("CarregadorRecursos.class").getFile());
		fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

		int result = fileChooser.showOpenDialog(null);

		if (result == JFileChooser.CANCEL_OPTION) {
			return;
		}

		ZipInputStream zin = new ZipInputStream(
				new FileInputStream(fileChooser.getSelectedFile()));
		zin.getNextEntry();
		ByteArrayOutputStream arrayDinamico = new ByteArrayOutputStream();
		int byt = zin.read();

		while (-1 != byt) {
			arrayDinamico.write(byt);
			byt = zin.read();
		}

		arrayDinamico.flush();

		ByteArrayInputStream bin = new ByteArrayInputStream(
				arrayDinamico.toByteArray());
		XMLDecoder decoder = new XMLDecoder(bin);
		PaddockDadosSrv paddockDadosSrv = (PaddockDadosSrv) decoder
				.readObject();
		// PaddockDadosSrv paddockDadosSrv = lerDados();
		Session session = HibernateUtil.getSessionFactory().openSession();
		try {

			Transaction beginTransaction = session.beginTransaction();
			Map jogadores = paddockDadosSrv.getJogadoresMap();
			Set emails = new HashSet();
			for (Iterator iterator = jogadores.keySet().iterator(); iterator
					.hasNext();) {
				String nome = (String) iterator.next();
				JogadorDadosSrv jogadorDadosSrv = (JogadorDadosSrv) jogadores
						.get(nome);
				CarreiraDadosSrv carreiraDadosSrv = null;
				List corridas = jogadorDadosSrv.getCorridas();
				for (Iterator iterator2 = corridas.iterator(); iterator2
						.hasNext();) {
					CorridasDadosSrv corridasDadosSrv = (CorridasDadosSrv) iterator2
							.next();
					corridasDadosSrv.setJogadorDadosSrv(jogadorDadosSrv);
				}
				if (emails.contains(jogadorDadosSrv.getEmail()))
					continue;
				emails.add(jogadorDadosSrv.getEmail());

				try {
					session.saveOrUpdate(jogadorDadosSrv);
					if (carreiraDadosSrv == null) {
						carreiraDadosSrv = new CarreiraDadosSrv();
					}
					carreiraDadosSrv.setJogadorDadosSrv(jogadorDadosSrv);
					session.saveOrUpdate(carreiraDadosSrv);
					session.saveOrUpdate(jogadorDadosSrv);
				} catch (Exception e) {
					Logger.logarExept(e);
				}

			}
			beginTransaction.commit();
		} finally {
			session.close();
		}
	}

	public static void main(String[] args) throws Exception {
		Dia dia = new Dia();
		System.out.println(dia.getMonth());
		System.out.println(new Date(dia.toTimestamp().getTime()));
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

	public JogadorDadosSrv carregaDadosJogador(String tokenJogador,
			Session session) {
		List jogador = session.createCriteria(JogadorDadosSrv.class)
				.add(Restrictions.eq("token", tokenJogador)).list();
		JogadorDadosSrv jogadorDadosSrv = (JogadorDadosSrv) (jogador.isEmpty()
				? null
				: jogador.get(0));
		return jogadorDadosSrv;
	}

	public JogadorDadosSrv carregaDadosJogadorId(Long id, Session session) {
		List jogador = session.createCriteria(JogadorDadosSrv.class)
				.add(Restrictions.eq("id", id)).list();
		JogadorDadosSrv jogadorDadosSrv = (JogadorDadosSrv) (jogador.isEmpty()
				? null
				: jogador.get(0));
		return jogadorDadosSrv;
	}

	public JogadorDadosSrv carregaDadosJogadorIdGoogle(String idGoogle,
			Session session) {
		List jogador = session.createCriteria(JogadorDadosSrv.class)
				.add(Restrictions.eq("idGoogle", idGoogle)).list();
		JogadorDadosSrv jogadorDadosSrv = (JogadorDadosSrv) (jogador.isEmpty()
				? null
				: jogador.get(0));
		return jogadorDadosSrv;
	}

	public Set obterListaJogadores(Session session) {
		Set nomes = new HashSet();
		List jogador = session.createCriteria(JogadorDadosSrv.class).list();
		for (Iterator iterator = jogador.iterator(); iterator.hasNext();) {
			JogadorDadosSrv jogadorDadosSrv = (JogadorDadosSrv) iterator.next();
			nomes.add(jogadorDadosSrv.getNome());
		}
		return nomes;
	}

	public Set obterListaJogadoresCorridasPeriodo(Session session, Dia ini,
			Dia fim) {
		Set nomes = new HashSet();
		List corridas = session.createCriteria(CorridasDadosSrv.class)
				.add(Restrictions.ge("tempoFim", ini.toTimestamp().getTime()))
				.add(Restrictions.le("tempoFim", fim.toTimestamp().getTime()))
				.list();
		for (Iterator iterator = corridas.iterator(); iterator.hasNext();) {
			CorridasDadosSrv corridasDadosSrv = (CorridasDadosSrv) iterator
					.next();
			nomes.add(corridasDadosSrv.getJogadorDadosSrv().getNome());
		}
		return nomes;
	}

	public void adicionarJogador(String nome, JogadorDadosSrv jogadorDadosSrv,
			Session session) throws Exception {
		Transaction transaction = session.beginTransaction();
		try {
			jogadorDadosSrv.setLoginCriador(jogadorDadosSrv.getNome());
			session.saveOrUpdate(jogadorDadosSrv);
			CarreiraDadosSrv carreiraDadosSrv = new CarreiraDadosSrv();
			carreiraDadosSrv.setPtsAerodinamica(600);
			carreiraDadosSrv.setPtsCarro(600);
			carreiraDadosSrv.setPtsFreio(600);
			carreiraDadosSrv.setPtsPiloto(600);
			carreiraDadosSrv.setJogadorDadosSrv(jogadorDadosSrv);
			session.saveOrUpdate(carreiraDadosSrv);
			transaction.commit();
		} catch (Exception e) {
			transaction.rollback();
			throw e;
		}
	}

	public byte[] obterBytesBase() {
		try {
			File file = new File(webInfDir + "hipersonic.tar.gz");
			if (file != null) {
				file.delete();
			}
			Connection connection = getSession().connection();
			String sql = "BACKUP DATABASE TO '" + webInfDir
					+ "hipersonic.tar.gz' BLOCKING";

			connection.createStatement().executeUpdate(sql);
			ZipOutputStream zipOutputStream = new ZipOutputStream(
					new FileOutputStream(webInfDir + "algolbkp.zip"));

			ByteArrayOutputStream hsByteArrayOutputStream = new ByteArrayOutputStream();
			FileInputStream fileInputStream = new FileInputStream(
					webInfDir + "hipersonic.tar.gz");
			int byt = fileInputStream.read();

			while (-1 != byt) {
				hsByteArrayOutputStream.write(byt);
				byt = fileInputStream.read();
			}

			ZipEntry entry = new ZipEntry("hipersonic.tar.gz");
			zipOutputStream.putNextEntry(entry);
			zipOutputStream.write(hsByteArrayOutputStream.toByteArray());

			zipDir(webDir + "midia", zipOutputStream);

			zipOutputStream.close();

			ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream();
			BufferedInputStream bufferedInputStream = new BufferedInputStream(
					new FileInputStream(webInfDir + "f1mane.zip"));
			byt = bufferedInputStream.read();

			while (-1 != byt) {
				arrayOutputStream.write(byt);
				byt = bufferedInputStream.read();
			}

			arrayOutputStream.flush();

			return arrayOutputStream.toByteArray();
		} catch (Exception e) {
			Logger.logarExept(e);
		}
		return null;
	}

	public void zipDir(String dir2zip, ZipOutputStream zos) {
		try {
			// create a new File object based on the directory we
			// have to zip File
			File zipDir = new File(dir2zip);
			// get a listing of the directory content
			String[] dirList = zipDir.list();
			byte[] readBuffer = new byte[2156];
			int bytesIn = 0;
			// loop through dirList, and zip the files
			for (int i = 0; i < dirList.length; i++) {
				File f = new File(zipDir, dirList[i]);
				if (f.isDirectory()) {
					// if the File object is a directory, call this
					// function again to add its content recursively
					String filePath = f.getPath();
					zipDir(filePath, zos);
					// loop again
					continue;
				}
				// if we reached here, the File object f was not
				// a directory
				// create a FileInputStream on top of f
				FileInputStream fis = new FileInputStream(f);
				// create a new zip entry
				ZipEntry anEntry = new ZipEntry(f.getAbsolutePath()
						.split("f1mane" + File.separator + File.separator)[1]);
				// place the zip entry in the ZipOutputStream object
				zos.putNextEntry(anEntry);
				// now write the content of the file to the ZipOutputStream
				while ((bytesIn = fis.read(readBuffer)) != -1) {
					zos.write(readBuffer, 0, bytesIn);
				}
				// close the Stream
				fis.close();
			}
		} catch (Exception e) {
			Logger.logarExept(e);
		}
	}

	public void gravarDados(Session session, F1ManeDados... f1ManeDados)
			throws Exception {
		Transaction transaction = session.beginTransaction();
		try {
			for (int i = 0; i < f1ManeDados.length; i++) {
				session.saveOrUpdate(f1ManeDados[i]);
			}
			transaction.commit();
		} catch (Exception e) {
			transaction.rollback();
			throw e;
		} finally {
			if (session.isOpen()) {
				session.close();
			}
		}

	}

	public List obterListaCorridas(String nomeJogador, Session session) {
		return obterListaCorridas(nomeJogador, session, null);
	}

	public List obterListaCorridas(String nomeJogador, Session session,
			Integer ano) {
		Criteria criteria = session.createCriteria(CorridasDadosSrv.class)
				.createAlias("jogadorDadosSrv", "j")
				.add(Restrictions.eq("j.nome", nomeJogador))
				.addOrder(Order.asc("tempoInicio"));
		if (ano != null) {
			Dia ini = new Dia(1, 1, ano);
			Dia fim = new Dia(1, 1, ano + 1);
			criteria.add(
					Restrictions.ge("tempoFim", ini.toTimestamp().getTime()));
			criteria.add(
					Restrictions.le("tempoFim", fim.toTimestamp().getTime()));
		}

		List corridas = criteria.list();
		for (Iterator iterator = corridas.iterator(); iterator.hasNext();) {
			CorridasDadosSrv corridasDadosSrv = (CorridasDadosSrv) iterator
					.next();
			session.evict(corridasDadosSrv);
			corridasDadosSrv.setJogadorDadosSrv(null);
		}
		return corridas;
	}

	public List<CorridasDadosSrv> obterClassificacaoCircuito(String circuito,
			Session session) {
		if (!Constantes.DATABASE) {
			return null;
		}
		Criteria criteria = session.createCriteria(CorridasDadosSrv.class);
		criteria.add(Restrictions.eq("circuito", circuito));
		criteria.add(Restrictions.gt("pontos", 0));
		List corridas = criteria.list();
		return corridas;
	}

	public CarreiraDadosSrv carregaCarreiraJogador(String token,
			boolean vaiCliente, Session session) {
		if (!Constantes.DATABASE) {
			return null;
		}
		Logger.logar("Buacar Carreira token " + token);
		List list = session.createCriteria(CarreiraDadosSrv.class)
				.createAlias("jogadorDadosSrv", "j")
				.add(Restrictions.eq("j.token", token)).list();
		CarreiraDadosSrv carreiraDadosSrv = null;
		if (!list.isEmpty()) {
			carreiraDadosSrv = (CarreiraDadosSrv) list.get(0);
		}
		if (vaiCliente && carreiraDadosSrv != null) {
			session.flush();
			session.evict(carreiraDadosSrv);
			carreiraDadosSrv.setJogadorDadosSrv(null);
		}
		if (carreiraDadosSrv != null && carreiraDadosSrv.getId() == null) {
			return null;
		}
		return carreiraDadosSrv;

	}

	public JogadorDadosSrv carregaDadosJogadorEmail(String emailJogador,
			Session session) {
		List jogador = session.createCriteria(JogadorDadosSrv.class)
				.add(Restrictions.eq("email", emailJogador)).list();
		JogadorDadosSrv jogadorDadosSrv = (JogadorDadosSrv) (jogador.isEmpty()
				? null
				: jogador.get(0));
		return jogadorDadosSrv;
	}

	public List<CampeonatoSrv> obterListaCampeonatos(Session session) {
		return session.createCriteria(CampeonatoSrv.class)
				.addOrder(Order.desc("dataCriacao")).list();

	}

	public CampeonatoSrv pesquisaCampeonato(Session session, String id,
			boolean cliente) {
		List campeonatos = session.createCriteria(CampeonatoSrv.class)
				.add(Restrictions.eq("id", new Long(id))).list();
		CampeonatoSrv campeonato = (CampeonatoSrv) (campeonatos.isEmpty()
				? null
				: campeonatos.get(0));
		if (campeonato == null) {
			return null;
		}
		if (cliente) {
			campeonatoCliente(session, campeonato);
		}
		return campeonato;

	}

	public List pesquisaCampeonatos(String token, Session session,
			boolean cliente) {
		List campeonatos = session.createCriteria(CampeonatoSrv.class)
				.createAlias("jogadorDadosSrv", "j")
				.add(Restrictions.eq("j.token", token)).list();
		if (cliente) {
			for (Iterator iterator = campeonatos.iterator(); iterator
					.hasNext();) {
				CampeonatoSrv campeonato = (CampeonatoSrv) iterator.next();
				campeonatoCliente(session, campeonato);
			}
		}
		return campeonatos;
	}

	public List pesquisaCampeonatosEmAberto(String token, Session session,
			boolean cliente) {
		List campeonatos = session.createCriteria(CampeonatoSrv.class)
				.createAlias("jogadorDadosSrv", "j")
				.add(Restrictions.eq("j.token", token))
				.add(Restrictions.eq("finalizado", false)).list();
		if (cliente) {
			for (Iterator iterator = campeonatos.iterator(); iterator
					.hasNext();) {
				CampeonatoSrv campeonato = (CampeonatoSrv) iterator.next();
				campeonatoCliente(session, campeonato);
			}
		}
		return campeonatos;
	}

	public void campeonatoCliente(Session session, CampeonatoSrv campeonato) {
		for (CorridaCampeonatoSrv corridaCampeonato : campeonato
				.getCorridaCampeonatos()) {
			corridaCampeonato.setDadosCorridaCampeonatos(Util.removePersistBag(
					corridaCampeonato.getDadosCorridaCampeonatos(), session));
		}
		campeonato.setCorridaCampeonatos(Util
				.removePersistBag(campeonato.getCorridaCampeonatos(), session));
		campeonato.getJogadorDadosSrv().setCorridas(Util.removePersistBag(
				campeonato.getJogadorDadosSrv().getCorridas(), session));
		session.evict(campeonato);
	}

	public List pesquisaCampeonatos(JogadorDadosSrv jogadorDadosSrv,
			Session session) {
		List campeonatos = session.createCriteria(CampeonatoSrv.class)
				.add(Restrictions.eq("jogadorDadosSrv", jogadorDadosSrv))
				.list();
		return campeonatos;
	}

	public MsgSrv modoCarreira(String token, boolean modo) {
		Session session = getSession();
		try {
			CarreiraDadosSrv carreiraDadosSrv = carregaCarreiraJogador(token,
					false, session);
			if (carreiraDadosSrv != null) {
				if (modo && (Util.isNullOrEmpty(carreiraDadosSrv.getNomeCarro())
						|| Util.isNullOrEmpty(carreiraDadosSrv.getNomePiloto())
						|| Util.isNullOrEmpty(
								carreiraDadosSrv.getNomePilotoAbreviado()))) {
					return new MsgSrv(Lang.msg("128"));
				}
				carreiraDadosSrv.setModoCarreira(modo);
				gravarDados(session, carreiraDadosSrv);
			}
		} catch (Exception e) {
			Logger.logarExept(e);
		} finally {
			if (session.isOpen()) {
				session.close();
			}
		}
		return null;
	}

	public boolean existeNomeCarro(Session session, String nomeCarro,
			Long idJogador) {
		List list = session.createCriteria(CarreiraDadosSrv.class)
				.createAlias("jogadorDadosSrv", "j")
				.add(Restrictions.ne("j.id", idJogador))
				.add(Restrictions.eq("nomeCarro", nomeCarro)).list();
		return !list.isEmpty();
	}

	public boolean existeNomePiloto(Session session, String nomePiloto,
			Long idJogador) {
		List list = session.createCriteria(CarreiraDadosSrv.class)
				.createAlias("jogadorDadosSrv", "j")
				.add(Restrictions.ne("j.id", idJogador))
				.add(Restrictions.eq("nomePiloto", nomePiloto)).list();
		return !list.isEmpty();
	}

	public boolean existeNomeCampeonato(Session session, String nome) {
		List list = session.createCriteria(CampeonatoSrv.class)
				.add(Restrictions.eq("nome", nome)).list();
		return !list.isEmpty();
	}

	public List<CorridasDadosSrv> obterClassificacaoGeral(Session session) {
		if (!Constantes.DATABASE) {
			return null;
		}
		Criteria criteria = session.createCriteria(CorridasDadosSrv.class);
		criteria.add(Restrictions.gt("pontos", 0));
		List corridas = criteria.list();
		return corridas;
	}

	public List obterClassificacaoEquipes(Session session) {
		Criteria criteria = session.createCriteria(CarreiraDadosSrv.class);
		criteria.add(Restrictions.isNotNull("nomeCarro"));
		criteria.add(Restrictions.isNotNull("nomePiloto"));
		criteria.add(Restrictions.gt("ptsConstrutoresGanhos", 0));
		criteria.addOrder(Order.desc("ptsConstrutoresGanhos"));
		List carreiras = criteria.list();
		return carreiras;
	}

	public void carreiraDadosParaPiloto(CarreiraDadosSrv carreiraDadosSrv,
			Piloto piloto) {
		piloto.setNome(carreiraDadosSrv.getNomePiloto());

		if (carreiraDadosSrv.getTemporadaCapaceteLivery() != null) {
			piloto.setTemporadaCapaceteLivery(
					carreiraDadosSrv.getTemporadaCapaceteLivery().toString());
		} else {
			piloto.setTemporadaCapaceteLivery(
					Util.rgb2hex(carreiraDadosSrv.geraCor1()));
		}

		if (carreiraDadosSrv.getTemporadaCarroLivery() != null) {
			piloto.setTemporadaCarroLivery(
					carreiraDadosSrv.getTemporadaCarroLivery().toString());
		} else {
			piloto.setTemporadaCarroLivery(
					Util.rgb2hex(carreiraDadosSrv.geraCor1()));
		}

		if (carreiraDadosSrv.getTemporadaCapaceteLivery() != null
				&& carreiraDadosSrv.getIdCapaceteLivery() != null
				&& carreiraDadosSrv.getIdCapaceteLivery() != 0) {
			piloto.setIdCapaceteLivery(
					carreiraDadosSrv.getIdCapaceteLivery().toString());
		} else {
			piloto.setIdCapaceteLivery(
					Util.rgb2hex(carreiraDadosSrv.geraCor2()));

		}
		if (carreiraDadosSrv.getTemporadaCarroLivery() != null
				&& carreiraDadosSrv.getIdCarroLivery() != null
				&& carreiraDadosSrv.getIdCarroLivery() != 0) {
			piloto.setIdCarroLivery(
					carreiraDadosSrv.getIdCarroLivery().toString());
		} else {
			piloto.setIdCarroLivery(Util.rgb2hex(carreiraDadosSrv.geraCor2()));

		}
		piloto.setNomeCarro(carreiraDadosSrv.getNomeCarro());
		piloto.setHabilidade(carreiraDadosSrv.getPtsPiloto());
		Carro carro = new Carro();
		piloto.setCarro(carro);
		carro.setAerodinamica(carreiraDadosSrv.getPtsAerodinamica());
		carro.setPotencia(carreiraDadosSrv.getPtsCarro());
		carro.setFreios(carreiraDadosSrv.getPtsFreio());
		carro.setCor1(carreiraDadosSrv.geraCor1());
		carro.setCor2(carreiraDadosSrv.geraCor2());
		piloto.setPontosCorrida(carreiraDadosSrv.getPtsConstrutoresGanhos());
	}

	public List<CampeonatoSrv> obterClassificacaoCampeonato(Session session) {
		Dia umMesAtras = new Dia();
		umMesAtras.backMonth();
		List<CampeonatoSrv> campeonatos = session
				.createCriteria(CampeonatoSrv.class)
				.createAlias("corridaCampeonatos", "cc").add(Restrictions
						.ge("cc.tempoFim", umMesAtras.toTimestamp().getTime()))
				.list();
		return campeonatos;
	}

}
