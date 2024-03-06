package br.f1mane.servidor.controles;

import br.f1mane.entidades.Carro;
import br.f1mane.entidades.Piloto;
import br.f1mane.recursos.CarregadorRecursos;
import br.f1mane.recursos.idiomas.Lang;
import br.f1mane.servidor.entidades.TOs.MsgSrv;
import br.f1mane.servidor.entidades.persistencia.*;
import br.f1mane.servidor.util.HibernateUtil;
import br.nnpe.Constantes;
import br.nnpe.Dia;
import br.nnpe.Logger;
import br.nnpe.Util;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import javax.swing.*;
import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * @author Paulo Sobreira Criado em 20/10/2007 as 14:19:54
 */
public class ControlePersistencia {

    private static PaddockDadosSrv paddockDadosSrv;

    private final static String lock = "lock";

    public Session getSession() {
        if (!Constantes.DATABASE) {
            return null;
        }
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
                // paddockDadosSrv = lerDados();
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

    public JogadorDadosSrv carregaDadosJogador(String tokenJogador, Session session) {
        List jogador = session.createCriteria(JogadorDadosSrv.class).add(Restrictions.eq("token", tokenJogador)).list();
        JogadorDadosSrv jogadorDadosSrv = (JogadorDadosSrv) (jogador.isEmpty() ? null : jogador.get(0));
        return jogadorDadosSrv;
    }

    public JogadorDadosSrv carregaDadosJogadorNome(String nomeJogador, Session session) {
        List jogador = session.createCriteria(JogadorDadosSrv.class).add(Restrictions.eq("nome", nomeJogador)).list();
        JogadorDadosSrv jogadorDadosSrv = (JogadorDadosSrv) (jogador.isEmpty() ? null : jogador.get(0));
        return jogadorDadosSrv;
    }

    public JogadorDadosSrv carregaDadosJogadorId(Long id, Session session) {
        List jogador = session.createCriteria(JogadorDadosSrv.class).add(Restrictions.eq("id", id)).list();
        JogadorDadosSrv jogadorDadosSrv = (JogadorDadosSrv) (jogador.isEmpty() ? null : jogador.get(0));
        return jogadorDadosSrv;
    }

    public JogadorDadosSrv carregaDadosJogadorIdGoogle(String idGoogle, Session session) {
        List jogador = session.createCriteria(JogadorDadosSrv.class).add(Restrictions.eq("idGoogle", idGoogle)).list();
        JogadorDadosSrv jogadorDadosSrv = (JogadorDadosSrv) (jogador.isEmpty() ? null : jogador.get(0));
        return jogadorDadosSrv;
    }

    public Set obterListaJogadores(Session session) {
        Set nomes = new HashSet();
        List jogador = session.createCriteria(JogadorDadosSrv.class).list();
        for (Iterator iterator = jogador.iterator(); iterator.hasNext(); ) {
            JogadorDadosSrv jogadorDadosSrv = (JogadorDadosSrv) iterator.next();
            nomes.add(jogadorDadosSrv.getToken());
        }
        return nomes;
    }

    public Set obterListaJogadoresCorridasPeriodo(Session session, Dia ini, Dia fim) {
        Set nomes = new HashSet();
        List corridas = session.createCriteria(CorridasDadosSrv.class)
                .add(Restrictions.ge("tempoFim", Long.valueOf(ini.toTimestamp().getTime())))
                .add(Restrictions.le("tempoFim", Long.valueOf(fim.toTimestamp().getTime()))).list();
        for (Iterator iterator = corridas.iterator(); iterator.hasNext(); ) {
            CorridasDadosSrv corridasDadosSrv = (CorridasDadosSrv) iterator.next();
            nomes.add(corridasDadosSrv.getJogadorDadosSrv().getToken());
        }
        return nomes;
    }

    public void adicionarJogador(String nome, JogadorDadosSrv jogadorDadosSrv, Session session) throws Exception {
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

    public void gravarDados(Session session, F1ManeDados... f1ManeDados) throws Exception {
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

    public List obterListaCorridas(String nomeJogador, Session session, Integer ano) {
        Criteria criteria = session.createCriteria(CorridasDadosSrv.class).createAlias("jogadorDadosSrv", "j")
                .add(Restrictions.eq("j.nome", nomeJogador)).addOrder(Order.asc("tempoInicio"));
        if (ano != null) {
            Dia ini = new Dia(1, 1, ano.intValue());
            Dia fim = new Dia(1, 1, ano.intValue() + 1);
            criteria.add(Restrictions.ge("tempoFim", Long.valueOf(ini.toTimestamp().getTime())));
            criteria.add(Restrictions.le("tempoFim", Long.valueOf(fim.toTimestamp().getTime())));
        }

        List corridas = criteria.list();
        for (Iterator iterator = corridas.iterator(); iterator.hasNext(); ) {
            CorridasDadosSrv corridasDadosSrv = (CorridasDadosSrv) iterator.next();
            session.evict(corridasDadosSrv);
            corridasDadosSrv.setJogadorDadosSrv(null);
        }
        return corridas;
    }

    public List<CorridasDadosSrv> obterClassificacaoCircuito(String circuito, Session session) {
        if (!Constantes.DATABASE) {
            return null;
        }
        Criteria criteria = session.createCriteria(CorridasDadosSrv.class);
        criteria.add(Restrictions.eq("circuito", circuito));
        criteria.add(Restrictions.gt("pontos", Integer.valueOf(0)));
        List corridas = criteria.list();
        return corridas;
    }

    public List<CorridasDadosSrv> obterClassificacaoTemporada(String temporadaSelecionada, Session session) {
        if (!Constantes.DATABASE) {
            return null;
        }
        Criteria criteria = session.createCriteria(CorridasDadosSrv.class);
        criteria.add(Restrictions.eq("temporada", temporadaSelecionada));
        criteria.add(Restrictions.gt("pontos", Integer.valueOf(0)));
        List corridas = criteria.list();
        return corridas;
    }

    public CarreiraDadosSrv carregaCarreiraJogador(String token, boolean vaiCliente, Session session) {
        if (!Constantes.DATABASE) {
            return null;
        }
        Logger.logar("Buacar Carreira token " + token);
        List list = session.createCriteria(CarreiraDadosSrv.class).createAlias("jogadorDadosSrv", "j")
                .add(Restrictions.eq("j.token", token)).list();
        CarreiraDadosSrv carreiraDadosSrv = null;
        if (!list.isEmpty()) {
            carreiraDadosSrv = (CarreiraDadosSrv) list.get(0);
        }
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
        List jogador = session.createCriteria(JogadorDadosSrv.class).add(Restrictions.eq("email", emailJogador)).list();
        JogadorDadosSrv jogadorDadosSrv = (JogadorDadosSrv) (jogador.isEmpty() ? null : jogador.get(0));
        return jogadorDadosSrv;
    }

    public List<CampeonatoSrv> obterListaCampeonatos(Session session) {
        return session.createCriteria(CampeonatoSrv.class).addOrder(Order.desc("dataCriacao")).list();

    }

    public CampeonatoSrv pesquisaCampeonato(Session session, String id, boolean cliente) {
        List campeonatos = session.createCriteria(CampeonatoSrv.class).add(Restrictions.eq("id", Long.valueOf(Long.parseLong(id)))).list();
        CampeonatoSrv campeonato = (CampeonatoSrv) (campeonatos.isEmpty() ? null : campeonatos.get(0));
        if (campeonato == null) {
            return null;
        }
        if (cliente) {
            campeonatoCliente(session, campeonato);
        }
        return campeonato;

    }

    public List pesquisaCampeonatos(String token, Session session, boolean cliente) {
        List campeonatos = session.createCriteria(CampeonatoSrv.class).createAlias("jogadorDadosSrv", "j")
                .add(Restrictions.eq("j.token", token)).list();
        if (cliente) {
            for (Iterator iterator = campeonatos.iterator(); iterator.hasNext(); ) {
                CampeonatoSrv campeonato = (CampeonatoSrv) iterator.next();
                campeonatoCliente(session, campeonato);
            }
        }
        return campeonatos;
    }

    public List pesquisaCampeonatosEmAberto(String token, Session session, boolean cliente) {
        List campeonatos = session.createCriteria(CampeonatoSrv.class).createAlias("jogadorDadosSrv", "j")
                .add(Restrictions.eq("j.token", token)).add(Restrictions.eq("finalizado", Boolean.FALSE)).list();
        if (cliente) {
            for (Iterator iterator = campeonatos.iterator(); iterator.hasNext(); ) {
                CampeonatoSrv campeonato = (CampeonatoSrv) iterator.next();
                campeonatoCliente(session, campeonato);
            }
        }
        return campeonatos;
    }

    public CampeonatoSrv pesquisaCampeonatoId(String id, Session session) {
        CampeonatoSrv campeonatoSrv = (CampeonatoSrv) session.createCriteria(CampeonatoSrv.class)
                .add(Restrictions.eq("id", Long.valueOf(Long.parseLong(id)))).uniqueResult();
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
        List campeonatos = session.createCriteria(CampeonatoSrv.class)
                .add(Restrictions.eq("jogadorDadosSrv", jogadorDadosSrv)).list();
        return campeonatos;
    }

    public MsgSrv modoCarreira(String token, boolean modo) {
        if (!Constantes.DATABASE) {
            return null;
        }
        Session session = getSession();
        try {
            CarreiraDadosSrv carreiraDadosSrv = carregaCarreiraJogador(token, false, session);
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
        List list = session.createCriteria(CarreiraDadosSrv.class).createAlias("jogadorDadosSrv", "j")
                .add(Restrictions.ne("j.id", idJogador)).add(Restrictions.eq("nomeCarro", nomeCarro)).list();
        return !list.isEmpty();
    }

    public boolean existeNomePiloto(Session session, String nomePiloto, Long idJogador) {
        List list = session.createCriteria(CarreiraDadosSrv.class).createAlias("jogadorDadosSrv", "j")
                .add(Restrictions.ne("j.id", idJogador)).add(Restrictions.eq("nomePiloto", nomePiloto)).list();
        return !list.isEmpty();
    }

    public boolean existeNomeCampeonato(Session session, String nome) {
        List list = session.createCriteria(CampeonatoSrv.class).add(Restrictions.eq("nome", nome)).list();
        return !list.isEmpty();
    }

    public List<CorridasDadosSrv> obterClassificacaoGeral(Session session) {
        if (!Constantes.DATABASE) {
            return null;
        }
        Criteria criteria = session.createCriteria(CorridasDadosSrv.class);
        criteria.add(Restrictions.gt("pontos", Integer.valueOf(0)));
        List corridas = criteria.list();
        return corridas;
    }

    public List obterClassificacaoEquipes(Session session) {
        Criteria criteria = session.createCriteria(CarreiraDadosSrv.class);
        criteria.add(Restrictions.isNotNull("nomeCarro"));
        criteria.add(Restrictions.isNotNull("nomePiloto"));
        criteria.add(Restrictions.gt("ptsConstrutoresGanhos", Integer.valueOf(0)));
        criteria.addOrder(Order.desc("ptsConstrutoresGanhos"));
        List carreiras = criteria.list();
        return carreiras;
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
        String hql = "from CampeonatoSrv obj where obj.id in (select distinct obj2.id from  CampeonatoSrv obj2 inner join  obj2.corridaCampeonatos cc where cc.tempoFim >= :tempoFim)";
        Query qry = session.createQuery(hql);
        qry.setParameter("tempoFim", Long.valueOf(umMesAtras.toTimestamp().getTime()));
        List list = qry.list();
        return list;
    }

}
