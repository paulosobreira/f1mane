package br.f1mane.controles;

import br.f1mane.entidades.*;
import br.f1mane.recursos.CarregadorRecursos;
import br.f1mane.servidor.JogoServidor;
import br.nnpe.Global;
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
    /**
     * Nós (reta de frenagem + cluster de curva baixa) que pertencem a uma
     * zona de frenagem detectada, mapeados pra sua posição relativa dentro
     * dela (0.0 = início da zona, nó mais distante da curva; 1.0 = final da
     * zona, último nó do cluster de curva baixa). Ver {@link #calculaZonaFrenagem()}.
     */
    protected final Map<No, Double> zonaFrenagemPosicoes = new HashMap<No, Double>();
    protected Map<Integer, No> mapaIdsNos = new HashMap<Integer, No>();
    protected Map<No, Integer> mapaNosIds = new HashMap<No, Integer>();
    private String temporada;
    private final Set<Integer> idsNoPista = new HashSet<Integer>();
    private final Set<Integer> idsNoBox = new HashSet<Integer>();
    private GameRandom random;

    public GameRandom getRandom() {
        return random;
    }

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

    public Map<No, Integer> getMapaNosIds() {
        return mapaNosIds;
    }


    public ControleRecursos(long seed) throws Exception {
        carregadorRecursos = CarregadorRecursos.getCarregadorRecursos(false);
        circuitos = carregarCircuitos();
        this.random = new GameRandom(seed);
    }

    public ControleRecursos(String temporada,long seed) throws Exception {
        if (temporada != null) {
            this.temporada = temporada;
        }
        this.random = new GameRandom(seed);
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
        zonaFrenagemPosicoes.clear();
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
            Integer pistaId = Integer.valueOf(contId);
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
            Integer boxId = Integer.valueOf(contId);
            contId++;
            mapaIdsNos.put(boxId, noDoBox);
            mapaNosIds.put(noDoBox, boxId);
            idsNoBox.add(boxId);
        }
        No primiraCurva = null;
        for (int indexLista = 0; indexLista < listaDeRetas.size(); indexLista++) {
            No no = listaDeRetas.get(indexLista);
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
        for (int indexLista = listaDeRetas.size() - 1; indexLista >= 0; indexLista--) {
            No no = listaDeRetas.get(indexLista);
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

        calculaZonaFrenagem();
    }

    /** Recalcula {@link #zonaFrenagemPosicoes} a partir de {@link #nosDaPista}; ver {@link #calculaZonaFrenagem(List)}. */
    void calculaZonaFrenagem() {
        zonaFrenagemPosicoes.clear();
        zonaFrenagemPosicoes.putAll(calculaZonaFrenagem(nosDaPista));
    }

    /**
     * Detecta trechos de "zona de frenagem": um cluster contíguo de nós
     * {@code CURVA_BAIXA} com pelo menos {@link Global#MIN_NOS_CURVA_BAIXA_ZONA_FRENAGEM}
     * nós (curva fechada de verdade, não um kink isolado), precedido por uma
     * reta (ou largada, que conta como reta pra esse efeito) com no máximo
     * {@link Global#MAX_CLUSTERS_CURVA_ALTA_ZONA_FRENAGEM} <em>trechos</em>
     * (não nós individuais — uma única curva alta longa e sinuosa pode ter
     * centenas de nós e ainda ser só UM trecho) de curva alta no meio (mais
     * que isso é mais provável uma sequência de esses/chicane que uma
     * entrada única de curva alta seguida de freada forte pra uma curva
     * fechada). Quando o cluster qualifica, marca como zona de frenagem os
     * nós de reta/largada imediatamente anteriores (até
     * {@link Global#TAMANHO_ZONA_FRENAGEM}) e os próprios nós do cluster,
     * cada um com sua posição relativa dentro da zona (0.0 no nó mais
     * distante da curva — início da zona — até 1.0 no último nó do cluster
     * — final da zona), na ordem em que o piloto realmente percorre (reta
     * do início da zona até o fim do cluster). Varredura circular (a pista
     * é uma volta fechada — a reta antes de uma curva logo após a largada
     * normalmente cruza o índice 0), método estático e sem estado pra poder
     * ser reaproveitado pelo editor de circuitos (que não passa por
     * {@link #carregaRecursos}), não só pelo motor de jogo.
     */
    public static Map<No, Double> calculaZonaFrenagem(List<No> nosDaPista) {
        Map<No, Double> zonaFrenagem = new HashMap<No, Double>();
        int n = nosDaPista.size();
        if (n == 0) {
            return zonaFrenagem;
        }
        int i = 0;
        while (i < n) {
            No no = nosDaPista.get(i);
            if (!no.verificaCurvaBaixa()) {
                i++;
                continue;
            }
            int inicioCluster = i;
            int fimCluster = i;
            while (fimCluster + 1 < n && nosDaPista.get(fimCluster + 1).verificaCurvaBaixa()) {
                fimCluster++;
            }
            if (fimCluster - inicioCluster + 1 >= Global.MIN_NOS_CURVA_BAIXA_ZONA_FRENAGEM) {
                List<No> retaAcumulada = new ArrayList<No>();
                int clustersDeCurvaAlta = 0;
                boolean dentroDeClusterAlta = false;
                boolean valido = true;
                int j = inicioCluster - 1;
                int passos = 0;
                while (passos < n && retaAcumulada.size() < Global.TAMANHO_ZONA_FRENAGEM) {
                    int idx = ((j % n) + n) % n;
                    No anterior = nosDaPista.get(idx);
                    if (anterior.verificaCurvaBaixa()) {
                        break;
                    }
                    if (anterior.verificaCurvaAlta()) {
                        if (!dentroDeClusterAlta) {
                            dentroDeClusterAlta = true;
                            clustersDeCurvaAlta++;
                            if (clustersDeCurvaAlta > Global.MAX_CLUSTERS_CURVA_ALTA_ZONA_FRENAGEM) {
                                valido = false;
                                break;
                            }
                        }
                    } else {
                        dentroDeClusterAlta = false;
                        if (anterior.verificaRetaOuLargada()) {
                            retaAcumulada.add(anterior);
                        }
                    }
                    j--;
                    passos++;
                }
                if (valido && !retaAcumulada.isEmpty()) {
                    // retaAcumulada foi preenchida de trás pra frente (do nó mais perto do
                    // cluster pro mais distante); em ordem de percurso ela vem invertida.
                    List<No> emOrdemDePercurso = new ArrayList<No>();
                    for (int k = retaAcumulada.size() - 1; k >= 0; k--) {
                        emOrdemDePercurso.add(retaAcumulada.get(k));
                    }
                    for (int k = inicioCluster; k <= fimCluster; k++) {
                        emOrdemDePercurso.add(nosDaPista.get(k));
                    }
                    int total = emOrdemDePercurso.size();
                    for (int idx = 0; idx < total; idx++) {
                        double posicao = total > 1 ? (double) idx / (total - 1) : 0.0;
                        zonaFrenagem.put(emOrdemDePercurso.get(idx), posicao);
                    }
                }
            }
            i = fimCluster + 1;
        }
        return zonaFrenagem;
    }

    public boolean isNoZonaFrenagem(No no) {
        return zonaFrenagemPosicoes.containsKey(no);
    }

    /**
     * Posição relativa de {@code no} dentro da zona de frenagem detectada
     * (0.0 = início da zona, 1.0 = final, dentro do cluster de curva
     * baixa), ou {@code null} se o nó não pertence a nenhuma zona de
     * frenagem.
     */
    public Double obterPosicaoNaZonaFrenagem(No no) {
        return zonaFrenagemPosicoes.get(no);
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
                // O terceiro campo (ativo) já está no Properties carregado
                // acima, sem precisar desserializar nenhum circuito: carregar
                // os XMLs inteiros nesta listagem (que roda na abertura do
                // menu) deixava TODOS os circuitos presos em memória desde o
                // boot — o circuito completo só é carregado quando a corrida
                // (ou o preview do menu) realmente o usa.
                boolean ativo = names.length > 2 && Boolean.parseBoolean(names[2].trim());
                if (!ativo) {
                    continue;
                }
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
        int idInt = Integer.parseInt(id);

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
