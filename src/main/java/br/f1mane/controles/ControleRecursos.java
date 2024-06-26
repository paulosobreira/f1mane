package br.f1mane.controles;

import br.f1mane.entidades.*;
import br.f1mane.recursos.CarregadorRecursos;
import br.f1mane.servidor.JogoServidor;
import br.nnpe.Logger;
import br.nnpe.Util;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;
import java.util.*;

/**
 * @author Paulo Sobreira
 */
public abstract class ControleRecursos {
    protected Circuito circuito;
    protected List<Piloto> pilotos = new ArrayList<Piloto>();
    protected List<No> nosDaPista = new ArrayList<No>();
    protected List<No> nosDoBox = new ArrayList<No>();
    protected List<Carro> carros;
    protected CarregadorRecursos carregadorRecursos;
    protected static Map<String, String> circuitos;
    public static Map<String, Integer> circuitosCiclo;
    protected final Map<No, No> mapaNoProxCurva = new HashMap<No, No>();
    protected final Map<No, No> mapaNoCurvaAnterior = new HashMap<No, No>();
    protected Map<Integer, No> mapaIdsNos = new HashMap<Integer, No>();
    protected Map<No, Integer> mapaNosIds = new HashMap<No, Integer>();
    private String temporada;
    private final Set<Integer> idsNoPista = new HashSet<Integer>();
    private final Set<Integer> idsNoBox = new HashSet<Integer>();

    public BufferedImage obterCapacete(Piloto piloto) {
        return carregadorRecursos.obterCapacete(piloto, this.getTemporada());
    }

    public BufferedImage obterCarroLado(Piloto piloto) {
        return carregadorRecursos.obterCarroLado(piloto, temporada);
    }

    private BufferedImage obterCarroLadoSemAreofolio(Piloto piloto) {
        return carregadorRecursos.obterCarroLadoSemAreofolio(piloto, temporada);
    }

    public No obterNoPorId(int idNo) {
        return mapaIdsNos.get(Integer.valueOf(idNo));
    }

    public Integer obterIdPorNo(No no) {
        return mapaNosIds.get(no);
    }

    public BufferedImage obterCarroCimaSemAreofolio(Piloto piloto,
                                                    String modelo) {
        return carregadorRecursos.obterCarroCimaSemAreofolio(piloto, modelo);
    }

    public BufferedImage obterCarroCima(Piloto piloto) {
        return carregadorRecursos.obterCarroCima(piloto, temporada);
    }

    public void setTemporada(String seasson) {
        this.temporada = seasson;
    }

    public String getTemporada() {
        return temporada;
    }

    public Map<Integer, No> getMapaIdsNos() {
        return mapaIdsNos;
    }

    public void setMapaIdsNos(Map<Integer, No> mapaIdsNos) {
        this.mapaIdsNos = mapaIdsNos;
    }

    public Map<No, Integer> getMapaNosIds() {
        return mapaNosIds;
    }

    public void setMapaNosIds(Map<No, Integer> mapaNosIds) {
        this.mapaNosIds = mapaNosIds;
    }

    public ControleRecursos() throws Exception {
        carregadorRecursos = CarregadorRecursos.getCarregadorRecursos(false);
        circuitos = carregarCircuitos();
    }

    public ControleRecursos(String temporada) throws Exception {
        if (temporada != null) {
            this.temporada = temporada;
        }
        carregarPilotosCarros();
        circuitos = carregarCircuitos();
    }

    public void carregarPilotosCarros() throws IOException {
        if (this instanceof JogoServidor) {
            carregadorRecursos = CarregadorRecursos
                    .getCarregadorRecursos(false);
        } else {
            carregadorRecursos = CarregadorRecursos.getCarregadorRecursos(true);
        }
        carros = carregadorRecursos.carregarListaCarros(temporada);
        pilotos = carregadorRecursos.carregarListaPilotos(temporada);
        carregadorRecursos.ligarPilotosCarros(pilotos, carros);
    }

    public void carregaRecursos(String circuitoStr) throws Exception {
        carregaRecursos(circuitoStr, null, null);
    }

    public void carregaRecursos(String circuitoStr, List<Piloto> pilotos,
                                List<Carro> carros) throws Exception {
        if (pilotos != null) {
            this.pilotos = pilotos;
        }
        if (carros != null) {
            this.carros = carros;
        }
        mapaIdsNos.clear();
        mapaNosIds.clear();
        idsNoPista.clear();
        idsNoBox.clear();
        mapaNoProxCurva.clear();
        mapaNoCurvaAnterior.clear();
        circuito = CarregadorRecursos.carregarCircuito(circuitoStr);
        circuito.vetorizarPista();
        String nome = "";
        for (Iterator<String> iterator = circuitos.keySet().iterator(); iterator
                .hasNext(); ) {
            String key = iterator.next();
            if (circuitoStr.equals(circuitos.get(key))) {
                nome = key;
                break;
            }

        }
        circuito.setNome(nome);
        nosDaPista = circuito.getPistaFull();
        nosDoBox = circuito.getBoxFull();
        circuito.gerarObjetosNoTransparencia();
        int contId = 1;
        List<No> listaDeRetas = new ArrayList<No>();
        for (Iterator<No> iter = nosDaPista.iterator(); iter.hasNext(); ) {
            No noPsita = iter.next();
            Integer pistaId = new Integer(contId);
            contId++;
            mapaIdsNos.put(pistaId, noPsita);
            mapaNosIds.put(noPsita, pistaId);
            idsNoPista.add(pistaId);
            if (noPsita.verificaRetaOuLargada()) {
                listaDeRetas.add(noPsita);
            }
        }

        for (Iterator iter = nosDoBox.iterator(); iter.hasNext(); ) {
            No noDoBox = (No) iter.next();
            Integer boxId = new Integer(contId);
            contId++;
            mapaIdsNos.put(boxId, noDoBox);
            mapaNosIds.put(noDoBox, boxId);
            idsNoBox.add(boxId);
        }
        No primiraCurva = null;
        for (int iindex = 0; iindex < listaDeRetas.size(); iindex++) {
            No no = listaDeRetas.get(iindex);
            int index = no.getIndex();
            for (int i = index; i < nosDaPista.size(); i++) {
                No noCurva = (No) nosDaPista.get(i);
                if (noCurva.verificaCurvaBaixa()
                        || noCurva.verificaCurvaAlta()) {
                    mapaNoProxCurva.put(no, noCurva);
                    if (primiraCurva == null) {
                        primiraCurva = noCurva;
                    }
                    break;
                }
            }
        }
        No ultimaCurva = null;
        for (int iindex = listaDeRetas.size() - 1; iindex >= 0; iindex--) {
            No no = listaDeRetas.get(iindex);
            int index = no.getIndex();
            for (int i = index; i >= 0; i--) {
                No noCurva = (No) nosDaPista.get(i);
                if (noCurva.verificaCurvaBaixa()
                        || noCurva.verificaCurvaAlta()) {
                    mapaNoCurvaAnterior.put(no, noCurva);
                    if (ultimaCurva == null) {
                        ultimaCurva = noCurva;
                    }
                    break;
                }
            }
        }
        for (int i = 0; i < nosDaPista.size(); i++) {
            No no = (No) nosDaPista.get(i);
            if (no.verificaRetaOuLargada()
                    && mapaNoCurvaAnterior.get(no) == null) {
                mapaNoCurvaAnterior.put(no, ultimaCurva);
            }
        }

        for (int i = nosDaPista.size() - 1; i >= 0; i--) {
            No no = (No) nosDaPista.get(i);
            if (no.verificaRetaOuLargada() && mapaNoProxCurva.get(no) == null) {
                mapaNoProxCurva.put(no, primiraCurva);
            }
        }

    }

    public int obterLadoEscape(Point pontoDerrapada) {
        Set<PontoEscape> keySet = circuito.getEscapeMap().keySet();
        for (Iterator iterator = keySet.iterator(); iterator.hasNext(); ) {
            PontoEscape pontoEscapeKey = (PontoEscape) iterator.next();
            if (pontoEscapeKey.getPoint().equals(pontoDerrapada)) {
                return pontoEscapeKey.getPista();
            }
        }
        return 0;
    }

    public List obterPista(Piloto piloto) {
        No noPiloto = piloto.getNoAtual();
        return obterPista(noPiloto);
    }

    public List obterPista(No noPiloto) {
        if (idsNoPista.contains(mapaNosIds.get(noPiloto))) {
            return nosDaPista;
        } else if (idsNoBox.contains(mapaNosIds.get(noPiloto))) {
            return nosDoBox;
        }
        return null;
    }

    public List obterNosPista() {
        return nosDaPista;
    }

    public static void main(String[] args) throws Exception {

        Map<String, String> carregarCircuitos = carregarCircuitos();
        for (Iterator iterator = carregarCircuitos.keySet().iterator(); iterator
                .hasNext(); ) {
            String nmCircuito = (String) iterator.next();
            String circuito = carregarCircuitos.get(nmCircuito);
            //circuito = "indianapoles_mro.xml";
            Circuito circuitoObj = CarregadorRecursos.carregarCircuito(circuito);
            circuitoObj.vetorizarPista();
            System.out.println(circuitoObj);
        }
    }

    public static Map<String, String> carregarCircuitos() {

        if(circuitos!=null){
            return circuitos;

        }

        circuitos = new HashMap<String, String>();
        circuitosCiclo= new HashMap<String, Integer>();
        final Properties properties = new Properties();

        try {
            properties.load(CarregadorRecursos
                    .recursoComoStream("properties/circuitos.properties"));
            Enumeration propName = properties.propertyNames();
            while (propName.hasMoreElements()) {
                final String name = (String) propName.nextElement();
                String names[] = properties.getProperty(name).split(",");
                circuitos.put(names[0], name);
                circuitosCiclo.put(names[0],Integer.valueOf(names[1]));
            }

        } catch (IOException e) {
            Logger.logarExept(e);
        }

        return circuitos;
    }

    public int porcentagemChuvaCircuito(String circuito) {
        try {
            if (circuitos == null || circuitos.isEmpty()) {
                return 0;
            }
            String string = circuitos.get(circuito);
            if (string == null) {
                return 0;
            }
            Circuito circuitoObj = CarregadorRecursos.carregarCircuito(string);
            return circuitoObj.getProbalidadeChuva();
        } catch (Exception e) {
            Logger.logarExept(e);
        }
        return 0;
    }

    public List<Piloto> getPilotosCopia() {
        List<Piloto> pilotosCopy = new ArrayList<Piloto>();
        while (pilotosCopy.isEmpty()) {
            try {
                if (pilotos == null || pilotos.isEmpty()) {
                    return pilotosCopy;
                }
                pilotosCopy.addAll(pilotos);
            } catch (Exception e) {
                pilotosCopy.clear();
                Logger.logarExept(e);
            }
        }
        return pilotosCopy;
    }

    public Piloto obterPilotoPorId(String id) {
        if (id == null) {
            return null;
        }
        int idInt = new Integer(id).intValue();

        for (Iterator iterator = pilotos.iterator(); iterator.hasNext(); ) {
            Piloto piloto = (Piloto) iterator.next();
            if (idInt == piloto.getId()) {
                return piloto;
            }
        }
        return null;
    }

    public static String nomeArquivoCircuitoParaPista(String arquivoCircuito) {
        Map<String, String> carregarCircuitos = ControleRecursos
                .carregarCircuitos();
        for (Iterator iterator = carregarCircuitos.keySet().iterator(); iterator
                .hasNext(); ) {
            String nmCircuito = (String) iterator.next();
            if (carregarCircuitos.get(nmCircuito).equals(arquivoCircuito)) {
                return nmCircuito;
            }
        }
        return null;
    }

    public static String nomeCircuitoParaArquivoCircuito(String nomeCircuito,
                                                         boolean substVogais) {
        Map<String, String> carregarCircuitos = ControleRecursos
                .carregarCircuitos();
        for (Iterator iterator = carregarCircuitos.keySet().iterator(); iterator
                .hasNext(); ) {
            String nmCircuito = (String) iterator.next();
            String compare = nmCircuito;
            if (substVogais) {
                compare = Util.substVogais(nmCircuito);
            }
            if (compare.equals(nomeCircuito)) {
                return carregarCircuitos.get(nmCircuito);
            }
        }
        return null;
    }

}
