package br.f1mane.servidor.controles;

import br.f1mane.entidades.Piloto;
import br.f1mane.recursos.CarregadorRecursos;
import br.f1mane.recursos.idiomas.Lang;
import br.f1mane.servidor.entidades.TOs.*;
import br.f1mane.servidor.entidades.persistencia.CampeonatoSrv;
import br.f1mane.servidor.entidades.persistencia.CarreiraDadosSrv;
import br.f1mane.servidor.entidades.persistencia.CorridasDadosSrv;
import br.f1mane.servidor.entidades.persistencia.JogadorDadosSrv;
import br.nnpe.*;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;

import java.util.*;

/**
 * @author Paulo Sobreira Criado em 27/10/2007 as 18:50:08
 */
public class ControleClassificacao {
	private final ControlePersistencia controlePersistencia;
	private final ControleCampeonatoServidor controleCampeonatoServidor;
	private final CarregadorRecursos carregadorRecursos = CarregadorRecursos.getCarregadorRecursos(false);

	/**
	 * @param controlePersistencia
	 */
	public ControleClassificacao(ControlePersistencia controlePersistencia,
								 ControleCampeonatoServidor controleCampeonatoServidor) {
		super();
		this.controlePersistencia = controlePersistencia;
		this.controleCampeonatoServidor = controleCampeonatoServidor;
	}

	public List obterListaClassificacao(Integer ano) {
		Session session = controlePersistencia.getSession();
		try {
			List returnList = new ArrayList();
			Dia ini = new Dia(1, 1, ano.intValue());
			Dia fim = new Dia(1, 1, ano.intValue() + 1);
			Set jogadores = controlePersistencia.obterListaJogadoresCorridasPeriodo(session, ini, fim);
			for (Iterator iter = jogadores.iterator(); iter.hasNext();) {
				String key = (String) iter.next();
				JogadorDadosSrv jogadorDadosSrv = controlePersistencia.carregaDadosJogador(key, session);
				List corridas = session.createCriteria(CorridasDadosSrv.class)
						.add(Restrictions.eq("jogadorDadosSrv", jogadorDadosSrv))
						.add(Restrictions.ge("tempoFim", Long.valueOf(ini.toTimestamp().getTime())))
						.add(Restrictions.le("tempoFim", Long.valueOf(fim.toTimestamp().getTime()))).list();
				DadosJogador dadosJogador = new DadosJogador();
				dadosJogador.setNome(jogadorDadosSrv.getNome());
				dadosJogador.setUltimoAceso(jogadorDadosSrv.getUltimoLogon());
				dadosJogador.setPontos(somarPontos(jogadorDadosSrv, ano, corridas));
				dadosJogador.setCorridas(corridas.size());
				returnList.add(dadosJogador);
			}
			Collections.sort(returnList, new MyComparator());
			return returnList;
		} finally {
			if (session.isOpen()) {
				session.close();
			}
		}
	}

	private long somarPontos(JogadorDadosSrv jogadorDadosSrv, Integer ano, List corridas) {

		int pts = 0;
		for (Iterator iter = corridas.iterator(); iter.hasNext();) {
			CorridasDadosSrv corridasDadosSrv = (CorridasDadosSrv) iter.next();
			pts += corridasDadosSrv.getPontos();
		}
		return pts;
	}

	public void processaCorrida(long tempoInicio, long tempoFim, Map mapVoltasJogadoresOnline, List pilotos,
								DadosCriarJogo dadosCriarJogo) {
		if (!Global.DATABASE) {
			return;
		}
		Session session = controlePersistencia.getSession();
		Transaction transaction = session.beginTransaction();
		try {
			for (Iterator iter = pilotos.iterator(); iter.hasNext();) {
				Piloto piloto = (Piloto) iter.next();
				if (piloto.isJogadorHumano() && !Util.isNullOrEmpty(piloto.getTokenJogador())) {
					JogadorDadosSrv jogadorDadosSrv = controlePersistencia.carregaDadosJogador(piloto.getTokenJogador(),
							session);
					if (jogadorDadosSrv == null) {
						continue;
					}
					CorridasDadosSrv corridasDadosSrv = new CorridasDadosSrv();
					corridasDadosSrv.setTemporada(dadosCriarJogo.getTemporada());
					corridasDadosSrv.setPiloto(piloto.getNome());
					corridasDadosSrv.setCarro(piloto.getNomeCarro());
					corridasDadosSrv.setTempoInicio(tempoInicio);
					corridasDadosSrv.setTempoFim(tempoFim);
					corridasDadosSrv.setCircuito(dadosCriarJogo.getCircuitoSelecionado());
					corridasDadosSrv.setNumVoltas(dadosCriarJogo.getQtdeVoltas().intValue());
					corridasDadosSrv.setAutomaticoManual(dadosCriarJogo.getAutomaticoManual());
					int pts = gerarPontos(piloto);
					CarreiraDadosSrv carreiraDadosSrv = controlePersistencia
							.carregaCarreiraJogador(piloto.getTokenJogador(), false, session);
					if (carreiraDadosSrv != null && carreiraDadosSrv.isModoCarreira()) {
						int ptsCarreira = pts;
						if (ptsCarreira == 0) {
							ptsCarreira = 1;
						}
						ptsCarreira += calculaBonusCarreira(carreiraDadosSrv);

						carreiraDadosSrv.setPtsConstrutores(carreiraDadosSrv.getPtsConstrutores() + ptsCarreira);
						carreiraDadosSrv.setPtsConstrutoresGanhos(carreiraDadosSrv.getPtsConstrutoresGanhos() +ptsCarreira);
					}
					corridasDadosSrv.setPontos(pts);
					corridasDadosSrv.setPosicao(piloto.getPosicao());
					processarPontos(mapVoltasJogadoresOnline, piloto, corridasDadosSrv);
					JogadorDadosSrv idJog = new JogadorDadosSrv();
					idJog.setId(jogadorDadosSrv.getId());
					corridasDadosSrv.setJogadorDadosSrv(idJog);
					jogadorDadosSrv.getCorridas().add(corridasDadosSrv);
					session.saveOrUpdate(corridasDadosSrv);
					session.saveOrUpdate(carreiraDadosSrv);
					session.saveOrUpdate(jogadorDadosSrv);
				}
			}
			transaction.commit();
		} catch (Exception e) {
			transaction.rollback();
			Logger.logarExept(e);
		} finally {
			if (session.isOpen()) {
				session.close();
			}
		}
	}

	private int calculaBonusCarreira(CarreiraDadosSrv carreiraDadosSrv) {
		int pts = 0;
		if (carreiraDadosSrv == null) {
			return 0;
		}
		if (carreiraDadosSrv.getPtsConstrutoresGanhos() < 9999) {
			pts += 5;
		}
		if (carreiraDadosSrv.getPtsConstrutoresGanhos() < 5000) {
			pts += 5;
		}
		if (carreiraDadosSrv.getPtsConstrutoresGanhos() < 1000) {
			pts += 10;
		}
		return pts;
	}

	public void processarPontos(Map mapVoltasJogadoresOnline, Piloto piloto, CorridasDadosSrv corridasDadosSrv) {
		double numVoltas = corridasDadosSrv.getNumVoltas();
		double voltasConcluidas = 0;
		for (Iterator iterMaster = mapVoltasJogadoresOnline.keySet().iterator(); iterMaster.hasNext();) {
			Integer lap = (Integer) iterMaster.next();
			List voltas = (List) mapVoltasJogadoresOnline.get(lap);
			for (Iterator iter = voltas.iterator(); iter.hasNext();) {
				VoltaJogadorOnline jogadorOnline = (VoltaJogadorOnline) iter.next();
				if (jogadorOnline.getJogador().equals(piloto.getTokenJogador())) {
					voltasConcluidas++;
					if (!jogadorOnline.getPiloto().equals(piloto.getNome())) {
						corridasDadosSrv.setMudouCarro(true);
					}
					break;
				}
			}
		}
		if (voltasConcluidas >= numVoltas) {
			voltasConcluidas = numVoltas;
		}

		if (corridasDadosSrv.isMudouCarro()) {
			corridasDadosSrv.setPontos(0);
			piloto.setPontosCorrida(0);
			piloto.setPorcentagemPontosCorrida(0);
		} else {
			double porcent = voltasConcluidas / numVoltas;
			corridasDadosSrv.setPorcentConcluida((int) (porcent * 100));
			double pontos = Math.ceil(porcent * corridasDadosSrv.getPontos());
			int pontosInt = (int) pontos;
			corridasDadosSrv.setPontos(pontosInt);
			piloto.setPontosCorrida(pontosInt);
			piloto.setPorcentagemPontosCorrida(Math.round(porcent * 100));

		}
	}

	public int gerarPontos(Piloto p) {
		if (p.getPosicao() == 1) {
			return 25;
		} else if (p.getPosicao() == 2) {
			return 18;
		} else if (p.getPosicao() == 3) {
			return 15;
		} else if (p.getPosicao() == 4) {
			return 12;
		} else if (p.getPosicao() == 5) {
			return 10;
		} else if (p.getPosicao() == 6) {
			return 8;
		} else if (p.getPosicao() == 7) {
			return 6;
		} else if (p.getPosicao() == 8) {
			return 4;
		} else if (p.getPosicao() == 9) {
			return 2;
		} else if (p.getPosicao() == 10) {
			return 1;
		} else {
			return 0;
		}
	}

	public List obterListaCorridas(String nomeJogador, Integer ano) {
		Session session = controlePersistencia.getSession();
		try {
			return controlePersistencia.obterListaCorridas(nomeJogador, session, ano);
		} finally {
			if (session.isOpen()) {
				session.close();
			}
		}

	}

	public void preencherListaContrutores(SrvPaddockPack srvPaddockPack, Integer ano) {
		Map mapaCarros = new HashMap();
		Map mapaPilotos = new HashMap();
		Session session = controlePersistencia.getSession();
		try {
			Set jogadores = controlePersistencia.obterListaJogadores(session);
			for (Iterator iter = jogadores.iterator(); iter.hasNext();) {
				String key = (String) iter.next();
				JogadorDadosSrv jogadorDadosSrv = controlePersistencia.carregaDadosJogador(key, session);
				List corridas = controlePersistencia.obterListaCorridas(jogadorDadosSrv.getNome(), session, ano);
				for (Iterator iterator = corridas.iterator(); iterator.hasNext();) {
					CorridasDadosSrv corridasDadosSrv = (CorridasDadosSrv) iterator.next();
					Integer ptsCarro = (Integer) mapaCarros.get(corridasDadosSrv.getCarro());
					if (ptsCarro == null) {
						mapaCarros.put(corridasDadosSrv.getCarro(), new Integer(corridasDadosSrv.getPontos()));
					} else {
						mapaCarros.put(corridasDadosSrv.getCarro(),
								new Integer(corridasDadosSrv.getPontos() + ptsCarro.intValue()));
					}
					Integer ptsPiloto = (Integer) mapaPilotos.get(corridasDadosSrv.getPiloto());
					if (ptsPiloto == null) {
						mapaPilotos.put(corridasDadosSrv.getPiloto(), new Integer(corridasDadosSrv.getPontos()));
					} else {
						mapaPilotos.put(corridasDadosSrv.getPiloto(),
								new Integer(corridasDadosSrv.getPontos() + ptsPiloto.intValue()));
					}

				}
			}

			List listaCarros = new LinkedList();
			List listaPilotos = new LinkedList();
			for (Iterator iterator = mapaCarros.keySet().iterator(); iterator.hasNext();) {
				String key = (String) iterator.next();
				DadosClassificacaoCarros dadosConstrutoresCarros = new DadosClassificacaoCarros();
				dadosConstrutoresCarros.setNome(key);
				dadosConstrutoresCarros.setPontos(((Integer) mapaCarros.get(key)).intValue());
				if (dadosConstrutoresCarros.getPontos() > 0)
					listaCarros.add(dadosConstrutoresCarros);

			}
			for (Iterator iterator = mapaPilotos.keySet().iterator(); iterator.hasNext();) {
				String key = (String) iterator.next();
				DadosClassificacaoPilotos dadosConstrutoresPilotos = new DadosClassificacaoPilotos();
				dadosConstrutoresPilotos.setNome(key);
				dadosConstrutoresPilotos.setPontos(((Integer) mapaPilotos.get(key)).intValue());
				if (dadosConstrutoresPilotos.getPontos() > 0)
					listaPilotos.add(dadosConstrutoresPilotos);
			}
			srvPaddockPack.setListaConstrutoresCarros(listaCarros);
			srvPaddockPack.setListaConstrutoresPilotos(listaPilotos);
		} finally {
			if (session.isOpen()) {
				session.close();
			}
		}
	}

	public Object verCarreira(ClientPaddockPack clientPaddockPack, Session session) {
		if (!Global.DATABASE) {
			return null;
		}
		return verCarreira(clientPaddockPack.getSessaoCliente().getToken(), session);
	}

	public Object verCarreira(String token, Session session) {
		CarreiraDadosSrv carreiraDadosSrv = controlePersistencia.carregaCarreiraJogador(token, true, session);
		if (carreiraDadosSrv != null) {
			carreiraDadosSrv.setBonus(Integer.valueOf(calculaBonusCarreira(carreiraDadosSrv)));
		}
		return carreiraDadosSrv;
	}

	public Object atualizaCarreira(ClientPaddockPack clientPaddockPack) {
		CarreiraDadosSrv carreiraDadosSrv = new CarreiraDadosSrv();
		carreiraDadosSrv.setNomePiloto(clientPaddockPack.getJogadorDadosSrv().getNomePiloto());
		carreiraDadosSrv.setNomeCarro(clientPaddockPack.getJogadorDadosSrv().getNomeCarro());
		carreiraDadosSrv.setPtsCarro(clientPaddockPack.getJogadorDadosSrv().getPtsCarro());
		carreiraDadosSrv.setPtsPiloto(clientPaddockPack.getJogadorDadosSrv().getPtsPiloto());
		carreiraDadosSrv.setPtsAerodinamica(clientPaddockPack.getJogadorDadosSrv().getPtsAerodinamica());
		carreiraDadosSrv.setPtsFreio(clientPaddockPack.getJogadorDadosSrv().getPtsFreio());
		carreiraDadosSrv.setPtsConstrutores(clientPaddockPack.getJogadorDadosSrv().getPtsConstrutores());
		carreiraDadosSrv.setModoCarreira(clientPaddockPack.getJogadorDadosSrv().isModoCarreira());
		carreiraDadosSrv.setC1R(clientPaddockPack.getJogadorDadosSrv().getC1R());
		carreiraDadosSrv.setC1G(clientPaddockPack.getJogadorDadosSrv().getC1G());
		carreiraDadosSrv.setC1B(clientPaddockPack.getJogadorDadosSrv().getC1B());
		carreiraDadosSrv.setC2R(clientPaddockPack.getJogadorDadosSrv().getC2R());
		carreiraDadosSrv.setC2G(clientPaddockPack.getJogadorDadosSrv().getC2G());
		carreiraDadosSrv.setC2B(clientPaddockPack.getJogadorDadosSrv().getC2B());
		return atualizaCarreira(clientPaddockPack.getSessaoCliente().getToken(), carreiraDadosSrv);
	}

	public Object atualizaCarreira(String token, CarreiraDadosSrv carreiraDados) {
		Session session = controlePersistencia.getSession();
		try {
			if (Util.isNullOrEmpty(carreiraDados.getNomeCarro()) || Util.isNullOrEmpty(carreiraDados.getNomePiloto())
					|| Util.isNullOrEmpty(carreiraDados.getNomePilotoAbreviado())) {
				return new MsgSrv(Lang.msg("128"));
			}

			if (carreiraDados.getNomeCarro().length() > 20 || carreiraDados.getNomePiloto().length() > 20) {
				return new MsgSrv(Lang.msg("249"));
			}
			StringBuilder nmAbrv = new StringBuilder();
			for (int i = 0; i < carreiraDados.getNomePilotoAbreviado().length(); i++) {
				if (i > 2) {
					break;
				}
				Character character = new Character(carreiraDados.getNomePilotoAbreviado().charAt(i));
				if (i == 0) {
					nmAbrv.append(character.toString().toUpperCase());
				} else {
					nmAbrv.append(character.toString().toLowerCase());
				}

			}
			carreiraDados.setNomePilotoAbreviado(nmAbrv.toString());

			CarreiraDadosSrv carreiraDadosSrv = controlePersistencia.carregaCarreiraJogador(token, false, session);

			if (controlePersistencia.existeNomeCarro(session, carreiraDados.getNomeCarro(),
					carreiraDadosSrv.getJogadorDadosSrv().getId())) {
				return new MsgSrv(Lang.msg("existeNomeCarro"));
			}
			if (controlePersistencia.existeNomePiloto(session, carreiraDados.getNomePiloto(),
					carreiraDadosSrv.getJogadorDadosSrv().getId())) {
				return new MsgSrv(Lang.msg("existeNomePiloto"));
			}

			carreiraDadosSrv.setNomePiloto(carreiraDados.getNomePiloto());
			carreiraDadosSrv.setNomeCarro(carreiraDados.getNomeCarro());
			carreiraDadosSrv.setNomePilotoAbreviado(carreiraDados.getNomePilotoAbreviado());
			int ptsAerodinamica = carreiraDados.getPtsAerodinamica();
			int ptsCarro = carreiraDados.getPtsCarro();
			int ptsFreio = carreiraDados.getPtsFreio();
			int ptsPiloto = carreiraDados.getPtsPiloto();

			int validadeDistribucaoPontos = validadeDistribuicaoPontos(carreiraDadosSrv, ptsAerodinamica, ptsCarro,
					ptsFreio, ptsPiloto);
			if (validadeDistribucaoPontos < 0) {
				return new MsgSrv(Lang.msg("erroAtualizarCarreira"));
			}

			if (carreiraDados.getTemporadaCapaceteLivery() != null && carreiraDados.getIdCapaceteLivery() != null) {
				List<Piloto> list = carregadorRecursos.carregarTemporadasPilotos()
						.get("t" + carreiraDados.getTemporadaCapaceteLivery());
				for (Iterator iterator = list.iterator(); iterator.hasNext();) {
					Piloto piloto = (Piloto) iterator.next();
					Logger.logar(
							piloto.getNome() + " " + piloto.getHabilidadeReal() + " " + carreiraDados.getPtsPiloto());
					if (piloto.getId() == carreiraDados.getIdCapaceteLivery().intValue()
							&& piloto.getHabilidadeReal() > carreiraDados.getPtsPiloto()) {
						return new MsgSrv(Lang.msg("pinturaCapacete",
								new String[] { String.valueOf(piloto.getHabilidadeReal()) }));
					}
				}
			}

			if (carreiraDados.getTemporadaCarroLivery() != null && carreiraDados.getIdCarroLivery() != null) {
				List<Piloto> list = carregadorRecursos.carregarTemporadasPilotos()
						.get("t" + carreiraDados.getTemporadaCarroLivery());
				for (Iterator iterator = list.iterator(); iterator.hasNext();) {
					Piloto piloto = (Piloto) iterator.next();
					if (piloto.getCarro().getId() == carreiraDados.getIdCarroLivery().intValue()
							&& (piloto.getCarro().getPotenciaReal() > carreiraDados.getPtsCarro()
							|| piloto.getCarro().getAerodinamica() > carreiraDados.getPtsAerodinamica()
							|| piloto.getCarro().getFreios() > carreiraDados.getPtsFreio())) {
						return new MsgSrv(Lang.msg("pinturaCarro",
								new String[] { String.valueOf(piloto.getCarro().getPotenciaReal()),
										String.valueOf(piloto.getCarro().getAerodinamica()),
										String.valueOf(piloto.getCarro().getFreios()) }));
					}
				}
			}

			carreiraDadosSrv.setPtsConstrutores(validadeDistribucaoPontos);
			carreiraDadosSrv.setPtsCarro(carreiraDados.getPtsCarro());
			carreiraDadosSrv.setPtsPiloto(carreiraDados.getPtsPiloto());
			carreiraDadosSrv.setPtsAerodinamica(carreiraDados.getPtsAerodinamica());
			carreiraDadosSrv.setPtsFreio(carreiraDados.getPtsFreio());
			carreiraDadosSrv.setModoCarreira(carreiraDados.isModoCarreira());
			carreiraDadosSrv.setC1R(carreiraDados.getC1R());
			carreiraDadosSrv.setC1G(carreiraDados.getC1G());
			carreiraDadosSrv.setC1B(carreiraDados.getC1B());
			carreiraDadosSrv.setC2R(carreiraDados.getC2R());
			carreiraDadosSrv.setC2G(carreiraDados.getC2G());
			carreiraDadosSrv.setC2B(carreiraDados.getC2B());
			carreiraDadosSrv.setIdCarroLivery(carreiraDados.getIdCarroLivery());
			carreiraDadosSrv.setIdCapaceteLivery(carreiraDados.getIdCapaceteLivery());
			carreiraDadosSrv.setIdCarroLivery(carreiraDados.getIdCarroLivery());
			carreiraDadosSrv.setTemporadaCapaceteLivery(carreiraDados.getTemporadaCapaceteLivery());
			carreiraDadosSrv.setTemporadaCarroLivery(carreiraDados.getTemporadaCarroLivery());

			controlePersistencia.gravarDados(session, carreiraDadosSrv);
		} catch (Exception e) {
			Logger.logarExept(e);
			return new ErroServ(e);
		} finally {
			if (session.isOpen()) {
				session.close();
			}
		}
		return new MsgSrv(Lang.msg("250"));
	}

	public static int validadeDistribuicaoPontos(CarreiraDadosSrv carreiraDadosSrv, int ptsAerodinamica, int ptsCarro,
												 int ptsFreio, int ptsPiloto) {
		int ptsConstrutoresBase = carreiraDadosSrv.getPtsConstrutores();
		int ptsAerodinamicaBase = carreiraDadosSrv.getPtsAerodinamica();
		int ptsCarroBase = carreiraDadosSrv.getPtsCarro();
		int ptsFreioBase = carreiraDadosSrv.getPtsFreio();
		int ptsPilotoBase = carreiraDadosSrv.getPtsPiloto();

		Numero numero = new Numero(Integer.valueOf(ptsConstrutoresBase));
		Logger.logar("ptsConstrutoresBase antes " + numero.getNumero().intValue());

		if (ptsAerodinamica < ptsAerodinamicaBase) {
			redistribuiPontos(ptsAerodinamicaBase, ptsAerodinamica, numero);
		}
		if (ptsCarro < ptsCarroBase) {
			redistribuiPontos(ptsCarroBase, ptsCarro, numero);
		}
		if (ptsFreio < ptsFreioBase) {
			redistribuiPontos(ptsFreioBase, ptsFreio, numero);
		}
		if (ptsPiloto < ptsPilotoBase) {
			redistribuiPontos(ptsPilotoBase, ptsPiloto, numero);
		}

		if (ptsAerodinamica > ptsAerodinamicaBase) {
			redistribuiPontos(ptsAerodinamicaBase, ptsAerodinamica, numero);
		}
		if (ptsCarro > ptsCarroBase) {
			redistribuiPontos(ptsCarroBase, ptsCarro, numero);
		}
		if (ptsFreio > ptsFreioBase) {
			redistribuiPontos(ptsFreioBase, ptsFreio, numero);
		}
		if (ptsPiloto > ptsPilotoBase) {
			redistribuiPontos(ptsPilotoBase, ptsPiloto, numero);
		}

		Logger.logar("ptsConstrutoresBase depois " + numero.getNumero().intValue());

		return numero.getNumero().intValue();

	}

	private static void redistribuiPontos(int ptsBase, int pts, Numero numero) {
		if (pts < ptsBase) {
			for (int i = ptsBase; i > pts; i--) {
				Util.processaValorPontosCarreira(i, i - 1, numero);
			}
		}
		if (pts > ptsBase) {
			for (int i = ptsBase; i < pts; i++) {
				Util.processaValorPontosCarreira(i, i + 1, numero);
			}
		}
	}

	public CarreiraDadosSrv obterCarreiraSrv(String token) {
		if (!Global.DATABASE) {
			return null;
		}
		Session session = controlePersistencia.getSession();
		try {
			CarreiraDadosSrv carreiraDadosSrv = controlePersistencia.carregaCarreiraJogador(token, false, session);
			return carreiraDadosSrv;
		} finally {
			session.close();
		}
	}

	public List obterClassificacaoCircuito(String nomeCircuito) {
		Session session = controlePersistencia.getSession();
		try {
			Map<Long, DadosClassificacaoJogador> mapa = new HashMap<>();
			List<CorridasDadosSrv> corridas = controlePersistencia.obterClassificacaoCircuito(nomeCircuito, session);
			for (Iterator iterator = corridas.iterator(); iterator.hasNext();) {
				CorridasDadosSrv corridasDadosSrv = (CorridasDadosSrv) iterator.next();
				DadosClassificacaoJogador dadosClassificacaoCircuito = mapa
						.get(corridasDadosSrv.getJogadorDadosSrv().getId());
				if (dadosClassificacaoCircuito == null) {
					dadosClassificacaoCircuito = new DadosClassificacaoJogador();
					dadosClassificacaoCircuito.setNome(corridasDadosSrv.getJogadorDadosSrv().getNome());
					dadosClassificacaoCircuito
							.setImagemJogador(corridasDadosSrv.getJogadorDadosSrv().getImagemJogador());
					mapa.put(corridasDadosSrv.getJogadorDadosSrv().getId(), dadosClassificacaoCircuito);
				}
				dadosClassificacaoCircuito.setCorridas(Integer.valueOf(dadosClassificacaoCircuito.getCorridas().intValue() + 1));
				dadosClassificacaoCircuito
						.setPontos(Integer.valueOf(dadosClassificacaoCircuito.getPontos().intValue() + corridasDadosSrv.getPontos()));
			}
			List<DadosClassificacaoJogador> classificacao = new ArrayList<DadosClassificacaoJogador>(mapa.values());
			Collections.sort(classificacao, new DadosClassificacaoJogadorComparator());
			return classificacao;
		} catch (Exception e) {
			Logger.logarExept(e);
		} finally {
			session.close();
		}
		return null;
	}

	public Object obterClassificacaoTemporada(String temporadaSelecionada) {
		Session session = controlePersistencia.getSession();
		try {
			Map<Long, DadosClassificacaoJogador> mapa = new HashMap<>();
			List<CorridasDadosSrv> corridas = controlePersistencia
					.obterClassificacaoTemporada("t" + temporadaSelecionada, session);
			for (Iterator iterator = corridas.iterator(); iterator.hasNext();) {
				CorridasDadosSrv corridasDadosSrv = (CorridasDadosSrv) iterator.next();
				DadosClassificacaoJogador dadosClassificacaoCircuito = mapa
						.get(corridasDadosSrv.getJogadorDadosSrv().getId());
				if (dadosClassificacaoCircuito == null) {
					dadosClassificacaoCircuito = new DadosClassificacaoJogador();
					dadosClassificacaoCircuito.setNome(corridasDadosSrv.getJogadorDadosSrv().getNome());
					dadosClassificacaoCircuito
							.setImagemJogador(corridasDadosSrv.getJogadorDadosSrv().getImagemJogador());
					mapa.put(corridasDadosSrv.getJogadorDadosSrv().getId(), dadosClassificacaoCircuito);
				}
				dadosClassificacaoCircuito.setCorridas(Integer.valueOf(dadosClassificacaoCircuito.getCorridas().intValue() + 1));
				dadosClassificacaoCircuito
						.setPontos(Integer.valueOf(dadosClassificacaoCircuito.getPontos().intValue() + corridasDadosSrv.getPontos()));
			}
			List<DadosClassificacaoJogador> classificacao = new ArrayList<DadosClassificacaoJogador>(mapa.values());
			Collections.sort(classificacao, new Comparator<DadosClassificacaoJogador>() {
				@Override
				public int compare(DadosClassificacaoJogador o1, DadosClassificacaoJogador o2) {
					int compareTo = o2.getPontos().compareTo(o1.getPontos());
					if (compareTo == 0) {
						return o2.getCorridas().compareTo(o1.getCorridas());
					} else {
						return compareTo;
					}
				}
			});
			return classificacao;
		} catch (Exception e) {
			Logger.logarExept(e);
		} finally {
			session.close();
		}
		return null;
	}

	public boolean atualizarJogadoresOnlineCarreira(Piloto piloto, String token) {
		return atualizarJogadoresOnlineCarreira(piloto, token, true);
	}

	public boolean atualizarJogadoresOnlineCarreira(Piloto piloto, String token, boolean verificaModoCarrira) {
		if (token == null) {
			return false;
		}
		CarreiraDadosSrv carreiraDadosSrv = obterCarreiraSrv(token);
		if (carreiraDadosSrv == null) {
			return false;
		}
		if (verificaModoCarrira && !carreiraDadosSrv.isModoCarreira()) {
			return false;
		}
		piloto.setNome(carreiraDadosSrv.getNomePiloto());
		piloto.setNomeAbreviado(carreiraDadosSrv.getNomePilotoAbreviado());
		piloto.setHabilidade((int) (carreiraDadosSrv.getPtsPiloto()));
		piloto.getCarro().setNome(carreiraDadosSrv.getNomeCarro());
		piloto.getCarro().setId(carreiraDadosSrv.getId().intValue() + 100);
		piloto.setNomeCarro(carreiraDadosSrv.getNomeCarro());
		piloto.getCarro().setPotencia(carreiraDadosSrv.getPtsCarro());
		piloto.getCarro().setCor1(carreiraDadosSrv.geraCor1());
		piloto.getCarro().setCor2(carreiraDadosSrv.geraCor2());
		if (carreiraDadosSrv.getTemporadaCapaceteLivery() != null) {
			piloto.setTemporadaCapaceteLivery(carreiraDadosSrv.getTemporadaCapaceteLivery().toString());
		} else {
			piloto.setTemporadaCapaceteLivery(Util.rgb2hex(carreiraDadosSrv.geraCor1()));

		}
		if (carreiraDadosSrv.getTemporadaCarroLivery() != null) {
			piloto.setTemporadaCarroLivery(carreiraDadosSrv.getTemporadaCarroLivery().toString());
		} else {
			piloto.setTemporadaCarroLivery(Util.rgb2hex(carreiraDadosSrv.geraCor1()));
		}
		if (carreiraDadosSrv.getIdCarroLivery() != null) {
			piloto.setIdCarroLivery(carreiraDadosSrv.getIdCarroLivery().toString());
		} else {
			piloto.setIdCarroLivery(Util.rgb2hex(carreiraDadosSrv.geraCor2()));
		}
		if (carreiraDadosSrv.getIdCapaceteLivery() != null) {
			piloto.setIdCapaceteLivery(carreiraDadosSrv.getIdCapaceteLivery().toString());
		} else {
			piloto.setIdCapaceteLivery(Util.rgb2hex(carreiraDadosSrv.geraCor2()));
		}
		return true;
	}

	public Object obterClassificacaoGeral() {
		Session session = controlePersistencia.getSession();
		try {
			Map<Long, DadosClassificacaoJogador> mapa = new HashMap<>();
			List<CorridasDadosSrv> corridas = controlePersistencia.obterClassificacaoGeral(session);
			for (Iterator iterator = corridas.iterator(); iterator.hasNext();) {
				CorridasDadosSrv corridasDadosSrv = (CorridasDadosSrv) iterator.next();
				DadosClassificacaoJogador dadosClassificacaoCircuito = mapa
						.get(corridasDadosSrv.getJogadorDadosSrv().getId());
				if (dadosClassificacaoCircuito == null) {
					dadosClassificacaoCircuito = new DadosClassificacaoJogador();
					dadosClassificacaoCircuito.setNome(corridasDadosSrv.getJogadorDadosSrv().getNome());
					dadosClassificacaoCircuito
							.setImagemJogador(corridasDadosSrv.getJogadorDadosSrv().getImagemJogador());
					mapa.put(corridasDadosSrv.getJogadorDadosSrv().getId(), dadosClassificacaoCircuito);
				}
				dadosClassificacaoCircuito.setCorridas(Integer.valueOf(dadosClassificacaoCircuito.getCorridas().intValue() + 1));
				dadosClassificacaoCircuito
						.setPontos(Integer.valueOf(dadosClassificacaoCircuito.getPontos().intValue() + corridasDadosSrv.getPontos()));
			}
			List<DadosClassificacaoJogador> classificacao = new ArrayList<DadosClassificacaoJogador>(mapa.values());

			List<DadosClassificacaoJogador> classificacaoremover = new ArrayList<DadosClassificacaoJogador>();
			for (Iterator iterator = classificacao.iterator(); iterator.hasNext();) {
				DadosClassificacaoJogador dadosClassificacaoJogador = (DadosClassificacaoJogador) iterator.next();
				if (dadosClassificacaoJogador.getPontos().intValue() < 50) {
					classificacaoremover.add(dadosClassificacaoJogador);
				}
			}

			classificacao.removeAll(classificacaoremover);
			Collections.sort(classificacao, new Comparator<DadosClassificacaoJogador>() {
				@Override
				public int compare(DadosClassificacaoJogador o1, DadosClassificacaoJogador o2) {
					int compareTo = o2.getPontos().compareTo(o1.getPontos());
					if (compareTo == 0) {
						return o2.getCorridas().compareTo(o1.getCorridas());
					} else {
						return compareTo;
					}
				}
			});
			return classificacao;
		} catch (Exception e) {
			Logger.logarExept(e);
		} finally {
			session.close();
		}
		return null;
	}

	public List<Piloto> obterClassificacaoEquipes() {
		Session session = controlePersistencia.getSession();
		try {
			List obterClassificacaoEquipes = controlePersistencia.obterClassificacaoEquipes(session);
			List<Piloto> ret = new ArrayList<Piloto>();
			for (Iterator iterator = obterClassificacaoEquipes.iterator(); iterator.hasNext();) {
				CarreiraDadosSrv carreiraDadosSrv = (CarreiraDadosSrv) iterator.next();
				Piloto piloto = new Piloto();
				controlePersistencia.carreiraDadosParaPiloto(carreiraDadosSrv, piloto);
				if(piloto.getPontosCorrida()>50) {
					ret.add(piloto);
				}
			}
			return ret;
		} catch (Exception e) {
			Logger.logarExept(e);
		} finally {
			session.close();
		}
		return null;
	}

	public Object obterClassificacaoCampeonato() {
		Session session = controlePersistencia.getSession();
		try {

			List pesquisaCampeonatosEmAberto = controlePersistencia.obterClassificacaoCampeonato(session);
			List<CampeonatoTO> ret = new ArrayList<CampeonatoTO>();
			for (Iterator iterator = pesquisaCampeonatosEmAberto.iterator(); iterator.hasNext();) {
				CampeonatoSrv campeonatoSrv = (CampeonatoSrv) iterator.next();
				CampeonatoTO campeonatoTO = new CampeonatoTO();
				controleCampeonatoServidor.processsaCorridaCampeonatoTO(campeonatoSrv, campeonatoTO);
				campeonatoTO.limpaListas();
				ret.add(campeonatoTO);
			}
			Collections.sort(ret, new CampeonatoTOComparator());

			return ret;
		} catch (Exception e) {
			Logger.logarExept(e);
		} finally {
			session.close();
		}
		return null;
	}

	private static class MyComparator implements Comparator {
		public int compare(Object arg0, Object arg1) {
			DadosJogador d0 = (DadosJogador) arg0;
			DadosJogador d1 = (DadosJogador) arg1;
			return new Long(d1.getPontos() * d1.getCorridas())
					.compareTo(new Long(d0.getPontos() * d0.getCorridas()));
		}
	}

	private static class DadosClassificacaoJogadorComparator implements Comparator<DadosClassificacaoJogador> {
		@Override
		public int compare(DadosClassificacaoJogador o1, DadosClassificacaoJogador o2) {
			int compareTo = o2.getPontos().compareTo(o1.getPontos());
			if (compareTo == 0) {
				return o2.getCorridas().compareTo(o1.getCorridas());
			} else {
				return compareTo;
			}
		}
	}

	private static class CampeonatoTOComparator implements Comparator<CampeonatoTO> {

		@Override
		public int compare(CampeonatoTO o1, CampeonatoTO o2) {
			return o2.getUltimaCorrida().compareTo(o1.getUltimaCorrida());
		}
	}
}
