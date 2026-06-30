package br.f1mane.servidor.controles;

import br.f1mane.entidades.Carro;
import br.f1mane.entidades.Piloto;
import br.f1mane.recursos.idiomas.Lang;
import br.f1mane.servidor.entidades.TOs.MsgSrv;
import br.f1mane.servidor.entidades.persistencia.*;
import br.f1mane.servidor.util.HibernateUtil;
import br.nnpe.Dia;
import br.nnpe.Logger;
import br.nnpe.Util;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.*;

/**
 * @author Paulo Sobreira Criada em 20/10/2007 as 14:19:54
 */
public class ControlePersistencia {

    private static PaddockDadosSrv paddockDadosSrv;

    private final static String lock = "lock";

    public Session getSession() {
        return HibernateUtil.getSession();
    }

    public ControlePersistencia(String webDir, String webInfDir) {
        super();
    }

    public ControlePersistencia() throws Exception {
        super();

        try {
            if (paddockDadosSrv == null) {
                Logger.logar("============ Antes ==========================");
                Logger.logar("heapSize " + Runtime.getRuntime().totalMemory() / 1024 + " kb");
                Logger.logar("heapMaxSize " + Runtime.getRuntime().maxMemory() / 1024 + " kb");
                Logger.logar("heapFreeSize " + Runtime.getRuntime().freeMemory() / 1024 + " kb");
                Logger.logar("============ Depois==========================");
                Logger.logar("heapSize " + Runtime.getRuntime().totalMemory() / 1024 + " kb");
                Logger.logar("heapMaxSize " + Runtime.getRuntime().maxMemory() / 1024 + " kb");
                Logger.logar("heapFreeSize " + Runtime.getRuntime().freeMemory() / 1024 + " kb");
            }

        } catch (Exception e) {
            Logger.logarExept(e);
            paddockDadosSrv = new PaddockDadosSrv();
        }

    }

    private void processarLimpeza(PaddockDadosSrv pds) {
        Map map = pds.getJogadoresMap();
        for (Iterator iter = map.keySet().iterator(); iter.hasNext(); ) {
            String key = (String) iter.next();
            JogadorDadosSrv jogadorDadosSrv = (JogadorDadosSrv) map.get(key);
            List corridas = jogadorDadosSrv.getCorridas();
            for (Iterator iterator = corridas.iterator(); iterator.hasNext(); ) {
                CorridasDadosSrv corridasDadosSrv = (CorridasDadosSrv) iterator.next();
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

    public JogadorDadosSrv carregaDadosJogador(String idUsuario, Session session) {
        List<JogadorDadosSrv> jogador = session.createQuery(
                "from JogadorDadosSrv where idUsuario = :idUsuario", JogadorDadosSrv.class)
                .setParameter("idUsuario", idUsuario).list();
        return jogador.isEmpty() ? null : jogador.get(0);
    }

    public JogadorDadosSrv carregaDadosJogadorNome(String nomeJogador, Session session) {
        List<JogadorDadosSrv> jogador = session.createQuery(
                "from JogadorDadosSrv where nome = :nome", JogadorDadosSrv.class)
                .setParameter("nome", nomeJogador).list();
        return jogador.isEmpty() ? null : jogador.get(0);
    }

    public JogadorDadosSrv carregaDadosJogadorIdUsuario(String idUsuario, Session session) {
        List<JogadorDadosSrv> jogador = session.createQuery(
                "from JogadorDadosSrv where idUsuario = :idUsuario", JogadorDadosSrv.class)
                .setParameter("idUsuario", idUsuario).list();
        return jogador.isEmpty() ? null : jogador.get(0);
    }

    public JogadorDadosSrv carregaDadosJogadorId(Long id, Session session) {
        List<JogadorDadosSrv> jogador = session.createQuery(
                "from JogadorDadosSrv where id = :id", JogadorDadosSrv.class)
                .setParameter("id", id).list();
        return jogador.isEmpty() ? null : jogador.get(0);
    }

    public Set obterListaJogadores(Session session) {
        Set nomes = new HashSet();
        List<JogadorDadosSrv> jogadores = session.createQuery(
                "from JogadorDadosSrv", JogadorDadosSrv.class).list();
        for (JogadorDadosSrv j : jogadores) {
            nomes.add(j.getIdUsuario());
        }
        return nomes;
    }

    public Set obterListaJogadoresCorridasPeriodo(Session session, Dia ini, Dia fim) {
        Set nomes = new HashSet();
        List<CorridasDadosSrv> corridas = session.createQuery(
                "from CorridasDadosSrv where tempoFim >= :ini and tempoFim <= :fim",
                CorridasDadosSrv.class)
                .setParameter("ini", ini.toTimestamp().getTime())
                .setParameter("fim", fim.toTimestamp().getTime()).list();
        for (CorridasDadosSrv c : corridas) {
            nomes.add(c.getJogadorDadosSrv().getIdUsuario());
        }
        return nomes;
    }

    public void adicionarJogador(String nome, JogadorDadosSrv jogadorDadosSrv, Session session) throws Exception {
        Transaction transaction = session.beginTransaction();
        try {
            jogadorDadosSrv.setLoginCriador(jogadorDadosSrv.getNome());
            JogadorDadosSrv managedJogador = session.merge(jogadorDadosSrv);
            CarreiraDadosSrv carreiraDadosSrv = new CarreiraDadosSrv();
            carreiraDadosSrv.setPtsAerodinamica(600);
            carreiraDadosSrv.setPtsCarro(600);
            carreiraDadosSrv.setPtsFreio(600);
            carreiraDadosSrv.setPtsPiloto(600);
            carreiraDadosSrv.setJogadorDadosSrv(managedJogador);
            session.merge(carreiraDadosSrv);
            transaction.commit();
        } catch (Exception e) {
            transaction.rollback();
            throw e;
        }
    }

    public void gravarDados(Session session, F1ManeDados... f1ManeDados) throws Exception {
        Transaction transaction = session.beginTransaction();
        try {
            for (F1ManeDados dado : f1ManeDados) {
                session.merge(dado);
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

    public List obterListaCorridas(String nomeJogador, Session session, Integer ano) {
        String hql = "from CorridasDadosSrv c join fetch c.jogadorDadosSrv j " +
                "where j.nome = :nome";
        if (ano != null) {
            Dia ini = new Dia(1, 1, ano.intValue());
            Dia fim = new Dia(1, 1, ano.intValue() + 1);
            hql += " and c.tempoFim >= :ini and c.tempoFim <= :fim";
            hql += " order by c.tempoInicio asc";
            List<CorridasDadosSrv> corridas = session.createQuery(hql, CorridasDadosSrv.class)
                    .setParameter("nome", nomeJogador)
                    .setParameter("ini", ini.toTimestamp().getTime())
                    .setParameter("fim", fim.toTimestamp().getTime()).list();
            for (CorridasDadosSrv c : corridas) {
                session.evict(c);
                c.setJogadorDadosSrv(null);
            }
            return corridas;
        }
        hql += " order by c.tempoInicio asc";
        List<CorridasDadosSrv> corridas = session.createQuery(hql, CorridasDadosSrv.class)
                .setParameter("nome", nomeJogador).list();
        for (CorridasDadosSrv c : corridas) {
            session.evict(c);
            c.setJogadorDadosSrv(null);
        }
        return corridas;
    }

    public List<CorridasDadosSrv> obterClassificacaoCircuito(String circuito, Session session) {
        return session.createQuery(
                "from CorridasDadosSrv where circuito = :circuito and pontos > 0",
                CorridasDadosSrv.class)
                .setParameter("circuito", circuito).list();
    }

    public List<CorridasDadosSrv> obterClassificacaoTemporada(String temporadaSelecionada, Session session) {
        return session.createQuery(
                "from CorridasDadosSrv where temporada = :temporada and pontos > 0",
                CorridasDadosSrv.class)
                .setParameter("temporada", temporadaSelecionada).list();
    }

    public CarreiraDadosSrv carregaCarreiraJogador(String idUsuario, boolean vaiCliente, Session session) {
        Logger.logar("Buscar Carreira idUsuario " + idUsuario);
        List<CarreiraDadosSrv> list = session.createQuery(
                "from CarreiraDadosSrv c join fetch c.jogadorDadosSrv j where j.idUsuario = :idUsuario",
                CarreiraDadosSrv.class)
                .setParameter("idUsuario", idUsuario).list();
        CarreiraDadosSrv carreiraDadosSrv = list.isEmpty() ? null : list.get(0);
        if (vaiCliente && carreiraDadosSrv != null) {
            session.evict(carreiraDadosSrv);
            carreiraDadosSrv.setJogadorDadosSrv(null);
        }
        if (carreiraDadosSrv != null && carreiraDadosSrv.getId() == null) {
            return null;
        }
        return carreiraDadosSrv;
    }

    public JogadorDadosSrv carregaDadosJogadorEmail(String emailJogador, Session session) {
        List<JogadorDadosSrv> jogador = session.createQuery(
                "from JogadorDadosSrv where email = :email", JogadorDadosSrv.class)
                .setParameter("email", emailJogador).list();
        return jogador.isEmpty() ? null : jogador.get(0);
    }

    public List<CampeonatoSrv> obterListaCampeonatos(Session session) {
        return session.createQuery(
                "from CampeonatoSrv order by dataCriacao desc", CampeonatoSrv.class).list();
    }

    public CampeonatoSrv pesquisaCampeonato(Session session, String id, boolean cliente) {
        List<CampeonatoSrv> campeonatos = session.createQuery(
                "from CampeonatoSrv where id = :id", CampeonatoSrv.class)
                .setParameter("id", Long.parseLong(id)).list();
        CampeonatoSrv campeonato = campeonatos.isEmpty() ? null : campeonatos.get(0);
        if (campeonato == null) {
            return null;
        }
        if (cliente) {
            campeonatoCliente(session, campeonato);
        }
        return campeonato;
    }

    public List pesquisaCampeonatos(String idUsuario, Session session, boolean cliente) {
        List<CampeonatoSrv> campeonatos = session.createQuery(
                "from CampeonatoSrv c join fetch c.jogadorDadosSrv j where j.idUsuario = :idUsuario",
                CampeonatoSrv.class)
                .setParameter("idUsuario", idUsuario).list();
        if (cliente) {
            for (CampeonatoSrv campeonato : campeonatos) {
                campeonatoCliente(session, campeonato);
            }
        }
        return campeonatos;
    }

    public List pesquisaCampeonatosEmAberto(String idUsuario, Session session, boolean cliente) {
        List<CampeonatoSrv> campeonatos = session.createQuery(
                "from CampeonatoSrv c join fetch c.jogadorDadosSrv j " +
                "where j.idUsuario = :idUsuario and c.finalizado = false",
                CampeonatoSrv.class)
                .setParameter("idUsuario", idUsuario).list();
        if (cliente) {
            for (CampeonatoSrv campeonato : campeonatos) {
                campeonatoCliente(session, campeonato);
            }
        }
        return campeonatos;
    }

    public CampeonatoSrv pesquisaCampeonatoId(String id, Session session) {
        CampeonatoSrv campeonatoSrv = session.createQuery(
                "from CampeonatoSrv where id = :id", CampeonatoSrv.class)
                .setParameter("id", Long.parseLong(id))
                .uniqueResultOptional().orElse(null);
        campeonatoCliente(session, campeonatoSrv);
        return campeonatoSrv;
    }

    public void campeonatoCliente(Session session, CampeonatoSrv campeonato) {
        for (CorridaCampeonatoSrv corridaCampeonato : campeonato.getCorridaCampeonatos()) {
            corridaCampeonato.setDadosCorridaCampeonatos(
                    Util.removePersistBag(corridaCampeonato.getDadosCorridaCampeonatos(), session));
            for (DadosCorridaCampeonatoSrv dadosCorridaCampeonatoSrv : corridaCampeonato.getDadosCorridaCampeonatos()) {
                if (dadosCorridaCampeonatoSrv.getJogador() == null) {
                    continue;
                }
                dadosCorridaCampeonatoSrv.setNomeJogador(
                        carregaDadosJogadorId(dadosCorridaCampeonatoSrv.getJogador(), session).getNome());
            }
        }
        campeonato.setCorridaCampeonatos(Util.removePersistBag(campeonato.getCorridaCampeonatos(), session));
        campeonato.getJogadorDadosSrv()
                .setCorridas(Util.removePersistBag(campeonato.getJogadorDadosSrv().getCorridas(), session));
        session.evict(campeonato);
    }

    public List pesquisaCampeonatos(JogadorDadosSrv jogadorDadosSrv, Session session) {
        return session.createQuery(
                "from CampeonatoSrv where jogadorDadosSrv = :jogador", CampeonatoSrv.class)
                .setParameter("jogador", jogadorDadosSrv).list();
    }

    public MsgSrv modoCarreira(String idUsuario, boolean modo) {
        Session session = getSession();
        try {
            CarreiraDadosSrv carreiraDadosSrv = carregaCarreiraJogador(idUsuario, false, session);
            if (carreiraDadosSrv != null) {
                if (modo && (Util.isNullOrEmpty(carreiraDadosSrv.getNomeCarro())
                        || Util.isNullOrEmpty(carreiraDadosSrv.getNomePiloto())
                        || Util.isNullOrEmpty(carreiraDadosSrv.getNomePilotoAbreviado()))) {
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

    public boolean existeNomeCarro(Session session, String nomeCarro, Long idJogador) {
        List<CarreiraDadosSrv> list = session.createQuery(
                "from CarreiraDadosSrv c join fetch c.jogadorDadosSrv j " +
                "where j.id <> :idJogador and c.nomeCarro = :nomeCarro",
                CarreiraDadosSrv.class)
                .setParameter("idJogador", idJogador)
                .setParameter("nomeCarro", nomeCarro).list();
        return !list.isEmpty();
    }

    public boolean existeNomePiloto(Session session, String nomePiloto, Long idJogador) {
        List<CarreiraDadosSrv> list = session.createQuery(
                "from CarreiraDadosSrv c join fetch c.jogadorDadosSrv j " +
                "where j.id <> :idJogador and c.nomePiloto = :nomePiloto",
                CarreiraDadosSrv.class)
                .setParameter("idJogador", idJogador)
                .setParameter("nomePiloto", nomePiloto).list();
        return !list.isEmpty();
    }

    public boolean existeNomeCampeonato(Session session, String nome) {
        List<CampeonatoSrv> list = session.createQuery(
                "from CampeonatoSrv where nome = :nome", CampeonatoSrv.class)
                .setParameter("nome", nome).list();
        return !list.isEmpty();
    }

    public List<CorridasDadosSrv> obterClassificacaoGeral(Session session) {
        return session.createQuery(
                "from CorridasDadosSrv where pontos > 0", CorridasDadosSrv.class).list();
    }

    public List obterClassificacaoEquipes(Session session) {
        return session.createQuery(
                "from CarreiraDadosSrv where nomeCarro is not null and nomePiloto is not null " +
                "and ptsConstrutoresGanhos > 0 order by ptsConstrutoresGanhos desc",
                CarreiraDadosSrv.class).list();
    }

    public void carreiraDadosParaPiloto(CarreiraDadosSrv carreiraDadosSrv, Piloto piloto) {
        piloto.setNome(carreiraDadosSrv.getNomePiloto());

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

        if (carreiraDadosSrv.getTemporadaCapaceteLivery() != null && carreiraDadosSrv.getIdCapaceteLivery() != null
                && carreiraDadosSrv.getIdCapaceteLivery().intValue() != 0) {
            piloto.setIdCapaceteLivery(carreiraDadosSrv.getIdCapaceteLivery().toString());
        } else {
            piloto.setIdCapaceteLivery(Util.rgb2hex(carreiraDadosSrv.geraCor2()));
        }
        if (carreiraDadosSrv.getTemporadaCarroLivery() != null && carreiraDadosSrv.getIdCarroLivery() != null
                && carreiraDadosSrv.getIdCarroLivery().intValue() != 0) {
            piloto.setIdCarroLivery(carreiraDadosSrv.getIdCarroLivery().toString());
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

    public List obterClassificacaoCampeonato(Session session) {
        Dia umMesAtras = new Dia();
        umMesAtras.advance(-15);
        String hql = "from CampeonatoSrv obj where obj.id in " +
                "(select distinct obj2.id from CampeonatoSrv obj2 inner join obj2.corridaCampeonatos cc " +
                "where cc.tempoFim >= :tempoFim)";
        return session.createQuery(hql, CampeonatoSrv.class)
                .setParameter("tempoFim", umMesAtras.toTimestamp().getTime())
                .list();
    }

}
