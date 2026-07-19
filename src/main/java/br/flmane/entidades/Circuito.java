package br.flmane.entidades;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import br.nnpe.Global;
import br.nnpe.GeoUtil;
import br.nnpe.Util;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Circuito implements Serializable {
    private static final long serialVersionUID = -1488529358105580761L;
    private String backGround;
    @JsonIgnore
    private List<No> pista = new ArrayList<No>();
    private List<No> pistaFull = new ArrayList<No>();
    private List<No> pista1Full = new ArrayList<No>();
    private List<No> pista2Full = new ArrayList<No>();
    private List<No> pista5Full = new ArrayList<No>();
    private List<No> pista4Full = new ArrayList<No>();
    @JsonIgnore
    private List<No> box = new ArrayList<No>();
    private transient List<No> boxFull = new ArrayList<No>();
    private List<No> box1Full = new ArrayList<No>();
    private List<No> box2Full = new ArrayList<No>();
    private int ladoBox = 0;
    private int ladoBoxSaidaBox = 0;
    private int probalidadeChuva = 0;
    private double velocidadePista = 0;
    /** Milissegundos entre ticks de simulação (InterfaceJogo.tempoCicloCircuito()) — não é contagem de voltas. */
    private int ciclo = 160;
    /** Distância oficial do circuito em quilômetros, informada por quem edita o circuito — não calculada a partir do traçado. */
    private int distanciaKm = 0;
    private int entradaBoxIndex;
    private int saidaBoxIndex;
    private int paradaBoxIndex;
    private int fimParadaBoxIndex;
    private String nome;
    private boolean noite;
    private boolean usaBkg;
    private boolean ativo;
    private transient List<ObjetoPistaJSon> objetosNoTransparencia;

    @JsonIgnore
    private transient List<No> pistaKey = new ArrayList<No>();
    @JsonIgnore
    private transient List<No> boxKey = new ArrayList<No>();
    @JsonIgnore
    private double multiplicadorPista;
    @JsonIgnore
    private double multiplicadorLarguraPista;
    /**
     * Override do ângulo da linha de largada (graus, 0-360); null usa o
     * ângulo calculado a partir da direção local da pista no nó de largada
     * (ver DesenhoProceduralCircuito#calculaAnguloNaturalLargada). Editável
     * no editor de circuitos, ao lado do campo de largura da pista.
     */
    @JsonIgnore
    private Double anguloLargada;
    @JsonIgnore
    private List<ObjetoPista> objetos;
    @JsonIgnore
    private List<ObjetoPista> objetosCenario;
    @JsonIgnore
    private List<Point> escapeList = new ArrayList<Point>();
    @JsonIgnore
    private Color corFundo;
    @JsonIgnore
    private Color corAsfalto;
    @JsonIgnore
    private Color corBox1;
    @JsonIgnore
    private Color corBox2;
    @JsonIgnore
    private Color corZebra1;
    @JsonIgnore
    private Color corZebra2;

    public String getNome() {
        return nome;
    }

    public boolean isUsaBkg() {
        return usaBkg;
    }

    public void setUsaBkg(boolean usaBkg) {
        this.usaBkg = usaBkg;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public List<No> geraPontosPista() {
        List<No> arrayList = new ArrayList<No>();
        No noAnt = null;

        for (Iterator<No> iter = pista.iterator(); iter.hasNext(); ) {
            No no = iter.next();

            if (noAnt != null) {
                arrayList.addAll(converterPointNo(GeoUtil.drawBresenhamLine(noAnt.getPoint(), no.getPoint()), noAnt));
            }
            noAnt = no;
        }

        if (!pista.isEmpty() && noAnt != null) {
            No no = (No) pista.get(0);
            arrayList.addAll(converterPointNo(GeoUtil.drawBresenhamLine(noAnt.getPoint(), no.getPoint()), noAnt));
        }
        for (int i = 0; i < arrayList.size(); i++) {
            No no = (No) arrayList.get(i);
            no.setIndex(i);
        }
        return arrayList;
    }

    public int getLadoBox() {
        return ladoBox;
    }

    public void setLadoBox(int ladoBox) {
        this.ladoBox = ladoBox;
    }

    public List<No> getBoxFull() {
        return boxFull;
    }

    public List<No> getBoxKey() {
        return boxKey;
    }

    public void vetorizarPista() {
        vetorizarPista(multiplicadorPista, multiplicadorLarguraPista);
    }

    public synchronized void vetorizarPista(double multi, double larg) {
        multiplicadorLarguraPista = larg;
        paradaBoxIndex = 0;
        fimParadaBoxIndex = 0;
        No noAnt = null;
        pistaFull = new ArrayList<No>();
        pistaKey = new ArrayList<No>();
        List<No> pistaTemp = new ArrayList<No>();

        for (Iterator<No> iter = pista.iterator(); iter.hasNext(); ) {
            No no = iter.next();
            No newNo = new No();
            newNo.setPoint(new Point(no.getPoint().x, no.getPoint().y));
            newNo.setTipo(no.getTipo());
            newNo.setTracado(0);
            pistaTemp.add(newNo);
        }

        for (Iterator<No> iter = pistaTemp.iterator(); iter.hasNext(); ) {
            No no = iter.next();
            if (noAnt != null) {
                Point p1 = noAnt.getPoint();
                Point p2 = no.getPoint();
                Point p3 = new Point(p2.x, p2.y);
                pistaKey.add(noAnt);
                pistaFull.addAll(converterPointNo(GeoUtil.drawBresenhamLine(p1, p2), noAnt));
                no.setPoint(p3);
            }
            noAnt = no;
        }

        if (!pistaTemp.isEmpty() && noAnt != null) {
            No no = (No) pistaTemp.get(0);
            Point p1 = noAnt.getPoint();
            Point p2 = no.getPoint();
            pistaKey.add(noAnt);
            pistaFull.addAll(converterPointNo(GeoUtil.drawBresenhamLine(p1, p2), noAnt));

        }
        for (int i = 0; i < pistaFull.size(); i++) {
            No no = (No) pistaFull.get(i);
            no.setIndex(i);
        }

        boxFull = new ArrayList<No>();
        boxKey = new ArrayList<No>();
        List<No> boxTemp = new ArrayList<No>();
        for (Iterator<No> iter = box.iterator(); iter.hasNext(); ) {
            No no = iter.next();
            no.setBox(true);
            No newNo = new No();
            newNo.setPoint(new Point(no.getPoint().x, no.getPoint().y));
            newNo.setTipo(no.getTipo());
            newNo.setTracado(0);
            newNo.setBox(true);
            boxTemp.add(newNo);
        }

        noAnt = null;

        for (Iterator<No> iter = boxTemp.iterator(); iter.hasNext(); ) {
            No no = iter.next();
            if (noAnt != null) {
                Point p1 = noAnt.getPoint();
                Point p2 = no.getPoint();
                Point p3 = new Point(p2.x, p2.y);
                boxKey.add(noAnt);
                boxFull.addAll(converterPointNo(GeoUtil.drawBresenhamLine(p1, p2), noAnt));
                no.setPoint(p3);
            }
            noAnt = no;
        }
        if (!boxTemp.isEmpty() && noAnt != null) {
            No no = (No) boxTemp.get(boxTemp.size() - 1);
            Point p1 = noAnt.getPoint();
            Point p2 = no.getPoint();
            boxKey.add(noAnt);
            boxFull.addAll(converterPointNo(GeoUtil.drawBresenhamLine(p1, p2), noAnt));

        }
        for (int i = 0; i < boxFull.size(); i++) {
            No no = (No) boxFull.get(i);
            no.setIndex(i);
        }

        if (!boxKey.isEmpty()) {
            No boxEntrada = (No) boxKey.get(0);
            No boxSaida = (No) boxKey.get(boxKey.size() - 1);
            int entradaBoxSize = Integer.MAX_VALUE;
            int saidaBoxSize = Integer.MAX_VALUE;
            for (int i = 0; i < pistaFull.size(); i += 50) {
                No pistaNo = (No) pistaFull.get(i);
                List<Point> entrada = GeoUtil.drawBresenhamLine(boxEntrada.getPoint(), pistaNo.getPoint());
                if (entrada.size() < entradaBoxSize) {
                    entradaBoxSize = entrada.size();
                    entradaBoxIndex = i;
                }
                List<Point> saida = GeoUtil.drawBresenhamLine(boxSaida.getPoint(), pistaNo.getPoint());
                if (saida.size() < saidaBoxSize) {
                    saidaBoxSize = saida.size();
                    saidaBoxIndex = i;
                }
            }
            for (int i = 0; i < boxFull.size(); i++) {
                No boxNo = (No) boxFull.get(i);
                if (paradaBoxIndex == 0 && No.PARADA_BOX.equals(boxNo.getTipo())) {
                    paradaBoxIndex = i;
                }
                if (fimParadaBoxIndex == 0 && No.FIM_BOX.equals(boxNo.getTipo())) {
                    fimParadaBoxIndex = i;
                }

            }
        }
        gerarTracado1e2Box();
        gerarTracado1e2Pista();
        gerarEscapeList();
        gerarTracadosDeFuga();
    }

    /**
     * Reprocessa só o traçado de escapada (pista4Full/pista5Full), sem
     * revetorizar a pista inteira. Usado pelo editor para atualizar o
     * desenho dos nós de escapada em tempo real ao mover um objeto Escapada
     * ou ajustar sua largura/altura (comprimento/amplitude da onda), sem o
     * custo de {@link #vetorizarPista}. Requer que a pista já tenha sido
     * vetorizada ao menos uma vez (pistaFull/pista1Full/pista2Full
     * populados) — chamado antes disso é inofensivo, mas não gera zonas.
     */
    public synchronized void reprocessarEscapadas() {
        gerarEscapeList();
        gerarTracadosDeFuga();
    }

    private void gerarTracado1e2Pista() {
        pista1Full = new ArrayList<No>();
        pista2Full = new ArrayList<No>();
        double calculaAngulo;
        for (int i = 0; i < pistaFull.size(); i++) {
            No no = pistaFull.get(i);
            Point p = no.getPoint();
            Point frenteCar;
            Point trazCar;
            int traz = i - Piloto.METADE_CARRO;
            int frente = i + Piloto.METADE_CARRO;
            if (traz < 0) {
                traz = (pistaFull.size() - 1) + traz;
            }
            if (frente > (pistaFull.size() - 1)) {
                frente = (frente - (pistaFull.size() - 1)) - 1;
            }
            trazCar = pistaFull.get(traz).getPoint();
            frenteCar = pistaFull.get(frente).getPoint();
            calculaAngulo = GeoUtil.calculaAngulo(frenteCar, trazCar, 0);
            Rectangle2D rectangle = new Rectangle2D.Double((p.x - Carro.MEIA_LARGURA_CIMA),
                    (p.y - Carro.MEIA_ALTURA_CIMA), Carro.LARGURA_CIMA, Carro.ALTURA_CIMA);
            Point p1 = GeoUtil.calculaPonto(calculaAngulo, Util.inteiro(Carro.ALTURA * getMultiplicadorLarguraPista()),
                    new Point(Util.inteiro(rectangle.getCenterX()), Util.inteiro(rectangle.getCenterY())));
            Point p2 = GeoUtil.calculaPonto(calculaAngulo + 180,
                    Util.inteiro(Carro.ALTURA * getMultiplicadorLarguraPista()),
                    new Point(Util.inteiro(rectangle.getCenterX()), Util.inteiro(rectangle.getCenterY())));
            No newNo1 = new No();
            newNo1.setPoint(p1);
            newNo1.setTipo(no.getTipo());
            newNo1.setTracado(1);
            newNo1.setIndex(no.getIndex());
            pista1Full.add(newNo1);
            No newNo2 = new No();
            newNo2.setPoint(p2);
            newNo2.setTipo(no.getTipo());
            newNo2.setTracado(2);
            newNo2.setIndex(no.getIndex());
            pista2Full.add(newNo2);
        }
    }

    private void gerarTracado1e2Box() {
        box1Full = new ArrayList<No>();
        box2Full = new ArrayList<No>();
        double calculaAngulo;
        for (int i = 0; i < boxFull.size(); i++) {
            No no = boxFull.get(i);
            Point p = no.getPoint();
            Point frenteCar;
            Point trazCar;
            int traz = i - Piloto.METADE_CARRO;
            int frente = i + Piloto.METADE_CARRO;
            if (frente > (boxFull.size() - 1)) {
                frente = (boxFull.size() - 1);
            }
            if (traz < 0) {
                trazCar = pistaFull.get(entradaBoxIndex + traz).getPoint();
            } else {
                trazCar = boxFull.get(traz).getPoint();
            }
            frenteCar = boxFull.get(frente).getPoint();
            calculaAngulo = GeoUtil.calculaAngulo(frenteCar, trazCar, 0);
            Rectangle2D rectangle = new Rectangle2D.Double((p.x - Carro.MEIA_LARGURA_CIMA),
                    (p.y - Carro.MEIA_ALTURA_CIMA), Carro.LARGURA_CIMA, Carro.ALTURA_CIMA);
            Point p1 = GeoUtil.calculaPonto(calculaAngulo, Util.inteiro(Carro.ALTURA * getMultiplicadorLarguraPista()),
                    new Point(Util.inteiro(rectangle.getCenterX()), Util.inteiro(rectangle.getCenterY())));
            Point p2 = GeoUtil.calculaPonto(calculaAngulo + 180,
                    Util.inteiro(Carro.ALTURA * getMultiplicadorLarguraPista()),
                    new Point(Util.inteiro(rectangle.getCenterX()), Util.inteiro(rectangle.getCenterY())));
            No newNo1 = new No();
            newNo1.setPoint(p1);
            newNo1.setTipo(no.getTipo());
            newNo1.setIndex(no.getIndex());
            newNo1.setTracado(1);
            box1Full.add(newNo1);
            No newNo2 = new No();
            newNo2.setPoint(p2);
            newNo2.setTipo(no.getTipo());
            newNo2.setTracado(2);
            newNo2.setIndex(no.getIndex());
            box2Full.add(newNo2);
        }
    }

    /**
     * Popula {@link #pista4Full}/{@link #pista5Full} a partir dos
     * {@link ObjetoEscapada} do circuito: para cada um com
     * {@code indiceEntrada}/{@code indiceSaida} válidos, gera nós de
     * traçado 4 (quando {@code tracadoOrigem == 1}) ou 5 (quando
     * {@code tracadoOrigem == 2}) interpolados ao longo do trajeto de pontos,
     * nos índices {@code [indiceEntrada, indiceSaida]} — o resto de cada
     * lista permanece {@code null}. O mapeamento 1→4/2→5 (e não 1→5/2→4)
     * é exigido pelas regras já existentes de {@code Piloto.mudarTracado}:
     * um piloto só volta de 4 para {0,1} e de 5 para {0,2}.
     */
    private void gerarTracadosDeFuga() {
        int tamanho = pistaFull.size();
        pista4Full = new ArrayList<No>(Collections.<No>nCopies(tamanho, null));
        pista5Full = new ArrayList<No>(Collections.<No>nCopies(tamanho, null));
        List<ObjetoPista> objetosCircuito = getObjetos();
        if (objetosCircuito == null) {
            return;
        }
        for (ObjetoPista objetoPista : objetosCircuito) {
            if (!(objetoPista instanceof ObjetoEscapada)) {
                continue;
            }
            preencherTracadoEscapada((ObjetoEscapada) objetoPista, tamanho);
        }
    }

    /**
     * Preenche, em {@link #pista4Full} ou {@link #pista5Full} (conforme
     * {@code escapada.getTracadoOrigem()}), os índices
     * {@code [indiceEntrada, indiceSaida]} com nós interpolados por
     * comprimento de arco ao longo de {@code escapada.obterPontosAbsolutos()}
     * — mesmo algoritmo usado pelo editor em
     * {@code TestePista.construirTracadoEscapada}/{@link GeoUtil#pontoNoTrajeto}.
     * Sem efeito (não lança exceção) quando os índices não são válidos ou o
     * trajeto tem menos de 2 pontos.
     * <p>
     * Mapeamento tracadoOrigem→traçado de fuga é {@code 1→5}/{@code 2→4}
     * (não {@code 1→4}/{@code 2→5}) — confirmado por
     * {@code Piloto.mudarTracado} (bloqueia 4→{0,1} e 5→{0,2}, ou seja, só
     * permite RETORNAR de 4 para 2 e de 5 para 1). Trocado depois de um bug
     * relatado em produção onde carros saíam pelo traçado 1 e voltavam no
     * traçado 2.
     */
    private void preencherTracadoEscapada(ObjetoEscapada escapada, int tamanho) {
        int indiceEntrada = escapada.getIndiceEntrada();
        int indiceSaida = escapada.getIndiceSaida();
        if (indiceEntrada < 0 || indiceSaida <= indiceEntrada) {
            return;
        }
        List<Point> pontos = escapada.obterPontosAbsolutos();
        if (pontos == null || pontos.size() < 2) {
            return;
        }
        int tracado = escapada.getTracadoOrigem() == 1 ? 5 : 4;
        List<No> destino = tracado == 4 ? pista4Full : pista5Full;
        int comprimento = indiceSaida - indiceEntrada;
        for (int passo = 0; passo <= comprimento; passo++) {
            int index = indiceEntrada + passo;
            if (index < 0 || index >= tamanho) {
                continue;
            }
            double t = (double) passo / (double) comprimento;
            No no = new No();
            no.setPoint(GeoUtil.pontoNoTrajeto(pontos, t));
            no.setTipo(No.RETA);
            no.setIndex(index);
            no.setTracado(tracado);
            destino.set(index, no);
        }
    }

    public static void main(String[] args) {
    }

    /**
     * {@code ObjetoEscapada} não tem mais um único "centro" de acionamento
     * (ver javadoc de {@link #gerarTracadosDeFuga()}) — {@link #escapeList}
     * fica sempre vazia a partir desta mudança; nenhum código fora desta
     * classe a consome.
     */
    private void gerarEscapeList() {
        escapeList = new ArrayList<Point>();
    }

    public int getParadaBoxIndex() {
        return paradaBoxIndex;
    }

    public int getFimParadaBoxIndex() {
        return fimParadaBoxIndex;
    }

    public List<No> getPistaFull() {
        return pistaFull;
    }

    public List<No> geraPontosBox() {
        List<No> arrayList = new ArrayList<No>();
        No noAnt = null;

        for (Iterator<No> iter = box.iterator(); iter.hasNext(); ) {
            No no = iter.next();
            if (noAnt != null) {
                arrayList.addAll(converterPointNo(GeoUtil.drawBresenhamLine(noAnt.getPoint(), no.getPoint()), noAnt));
            }
            noAnt = no;
        }
        boolean paradaBox = false;
        for (int i = 0; i < arrayList.size(); i++) {
            No no = (No) arrayList.get(i);
            if (No.PARADA_BOX.equals(no.getTipo()) && !paradaBox) {
                paradaBox = true;
            }
            no.setIndex(i);
        }
        return arrayList;
    }

    public Collection<No> converterPointNo(List<Point> list, No no) {
        List<No> retorno = new ArrayList<No>();

        for (Iterator<Point> iter = list.iterator(); iter.hasNext(); ) {
            Point element = iter.next();
            No newNo = new No();
            newNo.setPoint(element);
            newNo.setTipo(no.getTipo());
            newNo.setBox(no.isBox());
            newNo.setTracado(no.getTracado());
            retorno.add(newNo);
        }

        return retorno;
    }

    public String getBackGround() {
        return backGround;
    }

    /**
     * Sem o prefixo "set" de propósito: XMLEncoder só persiste uma
     * propriedade se existir o par getter+setter no padrão JavaBeans, e o
     * nome do jpg de referência não é mais dado guardado no XML — é derivado
     * pelo mesmo nome-base do arquivo XML do circuito (ver
     * {@link br.flmane.recursos.CarregadorRecursos#carregarCircuito}).
     */
    public void definirBackGroundPorConvencao(String backGround) {
        this.backGround = backGround;
    }

    /**
     * Cópia para persistir em {@code <nome>_mro_meta.xml}: mantém metadados
     * leves e o traçado autorado (pista/box), suficiente para desenhar uma
     * miniatura. Não inclui {@code ativo} (passa a viver em
     * {@code circuitos.properties}, ver
     * {@link br.flmane.recursos.CarregadorRecursos#circuitoAtivo}), nem
     * objetos/objetosCenario, nem os campos derivados por
     * {@link #vetorizarPista()} — ficam no valor padrão de um
     * {@code Circuito} novo e por isso o {@code XMLEncoder} não os grava.
     */
    public Circuito copiaParaArquivoMetadados() {
        Circuito copia = new Circuito();
        copia.setNome(nome);
        copia.setNoite(noite);
        copia.setUsaBkg(usaBkg);
        copia.setProbalidadeChuva(probalidadeChuva);
        copia.setVelocidadePista(velocidadePista);
        copia.setLadoBox(ladoBox);
        copia.setLadoBoxSaidaBox(ladoBoxSaidaBox);
        copia.setCorFundo(corFundo);
        copia.setCorAsfalto(corAsfalto);
        copia.setCorBox1(corBox1);
        copia.setCorBox2(corBox2);
        copia.setCorZebra1(corZebra1);
        copia.setCorZebra2(corZebra2);
        copia.setMultiplicadorLarguraPista(multiplicadorLarguraPista);
        copia.setAnguloLargada(anguloLargada);
        copia.setCiclo(ciclo);
        copia.setDistanciaKm(distanciaKm);
        copia.setPista(new ArrayList<No>(pista));
        copia.setBox(new ArrayList<No>(box));
        return copia;
    }

    /**
     * Cópia para persistir em {@code <nome>_mro.xml}: mantém só objetos de
     * cenário/função (dados não deriváveis). Metadados, traçado autorado e
     * campos derivados ficam no valor padrão de um {@code Circuito} novo.
     */
    public Circuito copiaParaArquivoObjetos() {
        Circuito copia = new Circuito();
        copia.setObjetos(objetos != null ? new ArrayList<ObjetoPista>(objetos) : new ArrayList<ObjetoPista>());
        copia.setObjetosCenario(
                objetosCenario != null ? new ArrayList<ObjetoPista>(objetosCenario) : new ArrayList<ObjetoPista>());
        return copia;
    }

    public List<No> getBox() {
        return box;
    }

    public void setBox(List<No> box) {
        this.box = box;
    }

    public List<No> getPista() {
        return pista;
    }

    public void setPista(List<No> pista) {
        this.pista = pista;
    }

    public List<No> getPistaKey() {
        return pistaKey;
    }

    public double getMultiplciador() {
        return 9;
    }

    public void setMultiplicador(double multiplicadorPista) {
        this.multiplicadorPista = multiplicadorPista;
    }

    public int getEntradaBoxIndex() {
        return entradaBoxIndex;
    }

    public int getSaidaBoxIndex() {
        return saidaBoxIndex;
    }

    public double getMultiplicadorLarguraPista() {
        return multiplicadorLarguraPista;
    }

    public Double getAnguloLargada() {
        return anguloLargada;
    }

    public void setAnguloLargada(Double anguloLargada) {
        this.anguloLargada = anguloLargada;
    }

    public List<ObjetoPista> getObjetos() {
        return objetos;
    }

    public void setObjetos(List<ObjetoPista> objetos) {
        this.objetos = objetos;
    }

    public List<ObjetoPista> getObjetosCenario() {
        return objetosCenario;
    }

    public void setObjetosCenario(List<ObjetoPista> objetosCenario) {
        this.objetosCenario = objetosCenario;
    }

    public Color getCorFundo() {
        return corFundo;
    }

    public void setCorFundo(Color corFundo) {
        this.corFundo = corFundo;
    }

    public Color getCorAsfalto() {
        return corAsfalto;
    }

    public void setCorAsfalto(Color corAsfalto) {
        this.corAsfalto = corAsfalto;
    }

    public Color getCorBox1() {
        return corBox1;
    }

    public void setCorBox1(Color corBox1) {
        this.corBox1 = corBox1;
    }

    public Color getCorBox2() {
        return corBox2;
    }

    public void setCorBox2(Color corBox2) {
        this.corBox2 = corBox2;
    }

    public Color getCorZebra1() {
        return corZebra1;
    }

    public void setCorZebra1(Color corZebra1) {
        this.corZebra1 = corZebra1;
    }

    public Color getCorZebra2() {
        return corZebra2;
    }

    public void setCorZebra2(Color corZebra2) {
        this.corZebra2 = corZebra2;
    }

    public boolean isNoite() {
        return noite;
    }

    public void setNoite(boolean noite) {
        this.noite = noite;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }

    public List<Point> getEscapeList() {
        return escapeList;
    }

    public int getLadoBoxSaidaBox() {
        return ladoBoxSaidaBox;
    }

    public void setLadoBoxSaidaBox(int ladoBoxSaidaBox) {
        this.ladoBoxSaidaBox = ladoBoxSaidaBox;
    }

    public int getProbalidadeChuva() {
        return probalidadeChuva;
    }

    public void setProbalidadeChuva(int probalidadeChuva) {
        this.probalidadeChuva = probalidadeChuva;
    }

    public int getCiclo() {
        return ciclo;
    }

    public void setCiclo(int ciclo) {
        this.ciclo = ciclo;
    }

    public int getDistanciaKm() {
        return distanciaKm;
    }

    public void setDistanciaKm(int distanciaKm) {
        this.distanciaKm = distanciaKm;
    }

    /**
     * Ganhos médios (nós de pistaFull avançados por tick) usados só para
     * estimar o tempo de volta no editor — médias aproximadas das faixas de
     * {@link Piloto#calculaModificadorPrincipal()} antes de qualquer
     * modificador de carro/piloto/clima/dano: reta/largada 30-50 (média 40),
     * curva alta 20-30 (média 25), curva baixa 10-20 (média 15). Não é uma
     * refatoração da fórmula de corrida, só uma ordem de grandeza pro editor.
     */
    private static final int GANHO_MEDIO_RETA = 40;
    private static final int GANHO_MEDIO_CURVA_ALTA = 25;
    private static final int GANHO_MEDIO_CURVA_BAIXA = 15;

    /**
     * Estimativa de tempo de volta, em milissegundos: soma, para cada nó de
     * {@link #pistaFull}, {@code 1 / ganhoMedio(tipoDoNo)} ticks, multiplicada
     * por {@link #ciclo} (ms/tick). Não precisa ser exata — é uma ferramenta
     * de apoio pra calibrar {@code ciclo} no editor, não um substituto da
     * simulação real (ver {@link #GANHO_MEDIO_RETA} e afins).
     */
    public long estimarTempoVoltaMs() {
        double ticks = 0;
        for (No no : pistaFull) {
            if (no.verificaRetaOuLargada()) {
                ticks += 1.0 / GANHO_MEDIO_RETA;
            } else if (no.verificaCurvaAlta()) {
                ticks += 1.0 / GANHO_MEDIO_CURVA_ALTA;
            } else if (no.verificaCurvaBaixa()) {
                ticks += 1.0 / GANHO_MEDIO_CURVA_BAIXA;
            }
        }
        return Math.round(ticks * ciclo);
    }

    public List<ObjetoPistaJSon> getObjetosNoTransparencia() {
        return objetosNoTransparencia;
    }

    public void gerarObjetosNoTransparencia() {
        objetosNoTransparencia = new ArrayList<ObjetoPistaJSon>();
        List<ObjetoPista> objetospista = getObjetos();
        if (objetospista == null) {
            return;
        }
        for (Iterator<ObjetoPista> iterator = objetospista.iterator(); iterator.hasNext(); ) {
            ObjetoPista objetoPista = iterator.next();
            if (!(objetoPista instanceof ObjetoTransparencia))
                continue;

            ObjetoTransparencia objetoTransparencia = (ObjetoTransparencia) objetoPista;
            Rectangle area = objetoPista.obterArea();
            if (area == null || area.width <= 0 || area.height <= 0 || objetoTransparencia.getPosicaoQuina() == null) {
                continue;
            }
            ObjetoPistaJSon objetoPistaJSon = new ObjetoPistaJSon();
            objetoPistaJSon.setX(Integer.valueOf(objetoTransparencia.getPosicaoQuina().x));
            objetoPistaJSon.setY(Integer.valueOf(objetoTransparencia.getPosicaoQuina().y));
            objetoPistaJSon.setIndexInicio(Integer.valueOf(objetoTransparencia.getInicioTransparencia()));
            objetoPistaJSon.setIndexFim(Integer.valueOf(objetoTransparencia.getFimTransparencia()));
            objetoPistaJSon.setTransparenciaBox(objetoTransparencia.isTransparenciaBox());
            objetosNoTransparencia.add(objetoPistaJSon);
        }

    }

    public List<No> getPista1Full() {
        return pista1Full;
    }

    public List<No> getPista2Full() {
        return pista2Full;
    }

    public void setPistaKey(List<No> pistaKey) {
        this.pistaKey = pistaKey;
    }

    public void setBoxKey(List<No> boxKey) {
        this.boxKey = boxKey;
    }

    public void setEscapeList(List<Point> escapeList) {
        this.escapeList = escapeList;
    }

    public List<No> getPista5Full() {
        return pista5Full;
    }

    public List<No> getPista4Full() {
        return pista4Full;
    }

    public List<No> getBox1Full() {
        return box1Full;
    }

    public List<No> getBox2Full() {
        return box2Full;
    }

    public double getIndiceTracado() {
        return Carro.ALTURA * getMultiplicadorLarguraPista();
    }

    public double getIndiceTracadoForaPista() {
        return Carro.ALTURA * 3.5 * getMultiplicadorLarguraPista();
    }

    public BufferedImage desenhaMiniCircuito() {
        int maxX = 0;
        int maxY = 0;
        double doubleMulti = 25;
        for (Iterator iterator = pista.iterator(); iterator.hasNext(); ) {
            No no = (No) iterator.next();
            Point p = new Point(no.getX(), no.getY());
            p.x /= doubleMulti;
            p.y /= doubleMulti;
            if (p.x > maxX) {
                maxX = p.x;
            }
            if (p.y > maxY) {
                maxY = p.y;
            }
        }
        BufferedImage image = new BufferedImage(maxX + (int) (maxX * 0.1), maxY + (int) (maxY * 0.1),
                BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = (Graphics2D) image.getGraphics();
        desenhaMiniCircuito(g2d, 0, 0);
        return image;
    }

    public void desenhaMiniCircuito(Graphics2D g2d, int x, int y) {
        int maxLagura = 0;
        g2d.setStroke(new BasicStroke(3.0f));
        g2d.setColor(Color.BLACK);
        List pista = getPista();
        ArrayList pistaMinimizada = new ArrayList();
        double doubleMulti = 25;
        Map map = new HashMap();
        for (Iterator iterator = pista.iterator(); iterator.hasNext(); ) {
            No no = (No) iterator.next();
            Point p = new Point(no.getX(), no.getY());
            p.x /= doubleMulti;
            p.y /= doubleMulti;
            if (p.x > maxLagura) {
                maxLagura = p.x;
            }
            if (!pistaMinimizada.contains(p)) {
                map.put(p, no);
                pistaMinimizada.add(p);
            }
        }
        ArrayList boxMinimizado = new ArrayList();
        List box = getBox();
        for (Iterator iterator = box.iterator(); iterator.hasNext(); ) {
            No no = (No) iterator.next();
            Point p = new Point(no.getX(), no.getY());
            p.x /= doubleMulti;
            p.y /= doubleMulti;
            if (p.x > maxLagura) {
                maxLagura = p.x;
            }
            if (!boxMinimizado.contains(p))
                boxMinimizado.add(p);
        }

        int incX = 0;// (320 - maxLagura) / 2;

        Point o = new Point(0, 0);
        Point oldP = null;
        No ultNo = null;
        for (Iterator iterator = pistaMinimizada.iterator(); iterator.hasNext(); ) {
            Point p = (Point) iterator.next();
            if (oldP != null) {
                No no = (No) map.get(oldP);
                if (no.verificaCurvaBaixa()) {
                    g2d.setColor(Color.red);
                } else if (no.verificaCurvaAlta()) {
                    g2d.setColor(Color.orange);
                } else if (no.verificaRetaOuLargada()) {
                    g2d.setColor(new Color(0, 200, 0));
                }
                g2d.drawLine(o.x + oldP.x + incX + x, o.y + oldP.y + y, o.x + p.x + incX + x, o.y + p.y + y);
            }
            oldP = p;
            ultNo = (No) map.get(oldP);
        }
        Point p0 = (Point) pistaMinimizada.get(0);
        if (ultNo != null && ultNo.verificaCurvaBaixa()) {
            g2d.setColor(Color.red);
        } else if (ultNo != null && ultNo.verificaCurvaAlta()) {
            g2d.setColor(Color.orange);
        } else if (ultNo != null && ultNo.verificaRetaOuLargada()) {
            g2d.setColor(new Color(0, 200, 0));
        }
        if (oldP != null) {
            g2d.drawLine(o.x + oldP.x + incX + x, o.y + oldP.y + y, o.x + p0.x + incX + x, o.y + p0.y + y);
        }
        g2d.setStroke(new BasicStroke(2.0f));
        oldP = null;
        g2d.setColor(Color.lightGray);
        for (Iterator iterator = boxMinimizado.iterator(); iterator.hasNext(); ) {
            Point p = (Point) iterator.next();
            if (oldP != null) {
                g2d.drawLine(o.x + oldP.x + incX + x, o.y + oldP.y + y, o.x + p.x + incX + x, o.y + p.y + y);
            }
            oldP = p;
        }
    }

    public BufferedImage desenhaObjetoPista(String indice) {
        Integer i = null;
        try {
            i = Integer.parseInt(indice);
        } catch (Exception e) {
        }
        if (i == null) {
            return null;
        }
        List<ObjetoTransparencia> objsTransp = new ArrayList<ObjetoTransparencia>();
        for (Iterator iterator = objetos.iterator(); iterator.hasNext(); ) {
            ObjetoPista objetoPista = (ObjetoPista) iterator.next();
            if (objetoPista instanceof ObjetoTransparencia) {
                objsTransp.add((ObjetoTransparencia) objetoPista);
            }
        }

        ObjetoTransparencia transparencia = objsTransp.get(i.intValue());
        List<Point> pontos = transparencia.getPontos();
        Polygon polygon = new Polygon();
        Point posicaoQuina = transparencia.getPosicaoQuina();
        for (Point ponto : pontos) {
            if (posicaoQuina != null) {
                polygon.addPoint(Util.inteiro(ponto.x - posicaoQuina.x), Util.inteiro(ponto.y - posicaoQuina.y));
            } else {
                polygon.addPoint((int) (ponto.x), (int) (ponto.y));
            }
        }
        Rectangle area = transparencia.obterArea();
        BufferedImage image = new BufferedImage(area.width, area.height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = (Graphics2D) image.getGraphics();
        g2d.setColor(Color.BLACK);
        AffineTransform affineTransform = AffineTransform.getScaleInstance(1, 1);
        double rad = Math.toRadians((double) transparencia.getAngulo());
        affineTransform.setToRotation(rad, polygon.getBounds().getCenterX(), polygon.getBounds().getCenterY());
        GeneralPath generalPath = new GeneralPath(polygon);
        generalPath.transform(affineTransform);
        g2d.fill(generalPath.createTransformedShape(affineTransform));
        return image;
    }

    public double getVelocidadePista() {
        return Global.VELOCIDADE_PISTA;
    }

    public void setVelocidadePista(double velocidadePista) {
        this.velocidadePista = velocidadePista;
    }

    public void setMultiplicadorLarguraPista(double multiplicadorLarguraPista) {
        this.multiplicadorLarguraPista = multiplicadorLarguraPista;
    }

    @Override
    public String toString() {
        return "Circuito{" +
                "backGround='" + backGround + '\'' +
                ", nome='" + nome + '\'' +
                ", velocidadePista=" + velocidadePista +
                ", noite=" + noite +
                ", multiplicadorPista=" + multiplicadorPista +
                ", multiplicadorLarguraPista=" + multiplicadorLarguraPista +
                ", pista=" + pista.size() +
                ", pistaFull=" + pistaFull.size() +
                ", pista1Full=" + pista1Full.size() +
                ", pista2Full=" + pista2Full.size() +
                ", pista5Full=" + pista5Full.size() +
                ", pista4Full=" + pista4Full.size() +
                ", box=" + box.size() +
                ", boxFull=" + (boxFull != null ? String.valueOf(boxFull.size()) : "null") +
                ", box1Full=" + box1Full.size() +
                ", box2Full=" + box2Full.size() +
                ", ladoBox=" + ladoBox +
                ", ladoBoxSaidaBox=" + ladoBoxSaidaBox +
                ", probalidadeChuva=" + probalidadeChuva +
                ", entradaBoxIndex=" + entradaBoxIndex +
                ", saidaBoxIndex=" + saidaBoxIndex +
                ", paradaBoxIndex=" + paradaBoxIndex +
                ", fimParadaBoxIndex=" + fimParadaBoxIndex +
                ", usaBkg=" + usaBkg +
                ", objetosNoTransparencia=" + objetosNoTransparencia +
                ", pistaKey=" + (pistaKey != null ? String.valueOf(pistaKey.size()) : "null") +
                ", boxKey=" + (boxKey != null ? String.valueOf(boxKey.size()) : "null") +
                ", objetos=" + (objetos != null ? String.valueOf(objetos.size()) : "null") +
                ", escapeList=" + escapeList.size() +
                ", corFundo=" + corFundo +
                '}';
    }

}
