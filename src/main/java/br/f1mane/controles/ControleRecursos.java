package br.f1mane.controles;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import br.f1mane.entidades.*;
import br.nnpe.BeanUtil;
import br.nnpe.Logger;
import br.nnpe.Util;
import br.f1mane.servidor.JogoServidor;
import br.f1mane.recursos.CarregadorRecursos;

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
    protected Map<String, String> circuitos;
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
        return mapaIdsNos.get(idNo);
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
            Integer pistaId = new Integer(contId++);
            mapaIdsNos.put(pistaId, noPsita);
            mapaNosIds.put(noPsita, pistaId);
            idsNoPista.add(pistaId);
            if (noPsita.verificaRetaOuLargada()) {
                listaDeRetas.add(noPsita);
            }
        }

        for (Iterator iter = nosDoBox.iterator(); iter.hasNext(); ) {
            No noDoBox = (No) iter.next();
            Integer boxId = new Integer(contId++);
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
            ObjectInputStream ois = new ObjectInputStream(
                    CarregadorRecursos.recursoComoStream("circuitos_old/" + circuito));
            sowbreira.f1mane.entidades.Circuito circuitoObj = (sowbreira.f1mane.entidades.Circuito) ois.readObject();
            circuitoObj.vetorizarPista();
            System.out.println(nmCircuito + " " + circuitoObj);
            Circuito circuitoBr = new Circuito();
            BeanUtil.copiarVO(circuitoObj, circuitoBr);
            circuitoBr.setPista(new ArrayList<>());
            for (Iterator iter = circuitoObj.getPista().iterator(); iter.hasNext(); ) {
                Object element = (Object) iter.next();
                No no = new No();
                BeanUtil.copiarVO(element, no);
                circuitoBr.getPista().add(no);
            }
            circuitoBr.setBox(new ArrayList<>());
            for (Iterator iter = circuitoObj.getBox().iterator(); iter.hasNext(); ) {
                Object element = (Object) iter.next();
                No no = new No();
                BeanUtil.copiarVO(element, no);
                circuitoBr.getBox().add(no);
            }
            if (circuitoObj.getObjetos() != null) {
                circuitoBr.setObjetos(new ArrayList<>());
                for (Iterator iter = circuitoObj.getObjetos().iterator(); iter.hasNext(); ) {
                    Object element = (Object) iter.next();
                    ObjetoPista obj = null;
                    if (element instanceof sowbreira.f1mane.entidades.ObjetoEscapada) {
                        obj = new ObjetoEscapada();
                    } else if (element instanceof sowbreira.f1mane.entidades.ObjetoTransparencia) {
                        obj = new ObjetoTransparencia();
                    } else if (element instanceof sowbreira.f1mane.entidades.ObjetoArquibancada) {
                        obj = new ObjetoArquibancada();
                    } else if (element instanceof sowbreira.f1mane.entidades.ObjetoCirculo) {
                        obj = new ObjetoCirculo();
                    } else if (element instanceof sowbreira.f1mane.entidades.ObjetoConstrucao) {
                        obj = new ObjetoConstrucao();
                    } else if (element instanceof sowbreira.f1mane.entidades.ObjetoLivre) {
                        System.out.println("Objeto Livre");
                        obj = new ObjetoLivre();
                    } else if (element instanceof sowbreira.f1mane.entidades.ObjetoGuadRails) {
                        obj = new ObjetoGuadRails();
                    } else if (element instanceof sowbreira.f1mane.entidades.ObjetoPneus) {
                        obj = new ObjetoPneus();
                    }
                    BeanUtil.copiarVO(element, obj);
                    circuitoBr.getObjetos().add(obj);
                    if (element instanceof sowbreira.f1mane.entidades.ObjetoTransparencia) {
                        sowbreira.f1mane.entidades.ObjetoTransparencia objetoTransparenciaElement = (sowbreira.f1mane.entidades.ObjetoTransparencia) element;
                        ObjetoTransparencia objetoTransparencia = (ObjetoTransparencia) obj;
                        objetoTransparencia.setPontos(new ArrayList<>());
                        for (Iterator iter2 = objetoTransparenciaElement.getPontos().iterator(); iter2.hasNext(); ) {
                            Object element2 = (Object) iter2.next();
                            Point p = new Point();
                            BeanUtil.copiarVO(element2, p);
                            objetoTransparencia.getPontos().add(p);
                        }
                        objetoTransparencia.gerar();
                        System.out.println(objetoTransparencia.getPosicaoQuina());
                        System.out.println(objetoTransparencia.getPolygon().getBounds());
                    }
                }
            }
            circuitoBr.setEscapeList(new ArrayList<>());
            for (Iterator iter = circuitoObj.getEscapeList().iterator(); iter.hasNext(); ) {
                Object element = (Object) iter.next();
                Point p = new Point();
                BeanUtil.copiarVO(element, p);
                circuitoBr.getEscapeList().add(p);
            }
            circuitoBr.setMultiplicador(circuitoObj.getMultiplciador());
            circuitoBr.setMultiplicadorLarguraPista(circuitoObj.getMultiplicadorLarguraPista());

            circuitoBr.vetorizarPista();
            System.out.println(nmCircuito + " " + circuitoBr);
            System.out.println("---");
            File file = new File("/home/sobreira/git/f1mane/src/main/resources/circuitos/" + circuito);

            FileOutputStream fileOutputStream = new FileOutputStream(file);
            ObjectOutputStream oos = new ObjectOutputStream(fileOutputStream);
            oos.writeObject(circuitoBr);
            oos.flush();
            fileOutputStream.close();
        }

    }

    public static Map<String, String> carregarCircuitos() {

        Map<String, String> circuitos = new HashMap<String, String>();
        final Properties properties = new Properties();

        try {
            properties.load(CarregadorRecursos
                    .recursoComoStream("properties/pistas.properties"));
            Enumeration propName = properties.propertyNames();
            while (propName.hasMoreElements()) {
                final String name = (String) propName.nextElement();
                circuitos.put(properties.getProperty(name), name);
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
        int idInt = new Integer(id);

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
