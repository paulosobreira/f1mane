package sowbreira.f1mane.entidades;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import br.nnpe.Constantes;
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
	private int entradaBoxIndex;
	private int saidaBoxIndex;
	private int paradaBoxIndex;
	private int fimParadaBoxIndex;
	private String nome;
	private boolean noite;
	private boolean usaBkg;
	private transient List<ObjetoPistaJSon> objetosNoTransparencia;

	@JsonIgnore
	private transient List<No> pistaKey = new ArrayList<No>();
	@JsonIgnore
	private transient List<No> boxKey = new ArrayList<No>();
	@JsonIgnore
	private Map<PontoEscape, List<No>> escapeMap = new HashMap<PontoEscape, List<No>>();
	@JsonIgnore
	private double multiplicadorPista;
	@JsonIgnore
	private double multiplicadorLarguraPista;
	@JsonIgnore
	private List<ObjetoPista> objetos;
	@JsonIgnore
	private Point creditos;
	@JsonIgnore
	private List<Point> escapeList = new ArrayList<Point>();
	@JsonIgnore
	private Color corFundo;

	public Point getCreditos() {
		return creditos;
	}

	public Ponto getCreditosPonto() {
		return new Ponto(creditos);
	}

	public void setCreditos(Point creditos) {
		this.creditos = creditos;
	}

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

		for (Iterator<No> iter = pista.iterator(); iter.hasNext();) {
			No no = iter.next();

			if (noAnt == null) {
				noAnt = no;
			} else {
				arrayList.addAll(converterPointNo(GeoUtil.drawBresenhamLine(noAnt.getPoint(), no.getPoint()), noAnt));
				noAnt = no;
			}
		}

		if (!pista.isEmpty()) {
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
		multiplicadorPista = multi;
		paradaBoxIndex = 0;
		fimParadaBoxIndex = 0;
		if (usaBkg) {
			multi = 1;
		}
		No noAnt = null;
		pistaFull = new ArrayList<No>();
		pistaKey = new ArrayList<No>();
		List<No> pistaTemp = new ArrayList<No>();

		for (Iterator<No> iter = pista.iterator(); iter.hasNext();) {
			No no = iter.next();
			No newNo = new No();
			newNo.setPoint(new Point(no.getPoint().x, no.getPoint().y));
			newNo.setTipo(no.getTipo());
			newNo.setTracado(0);
			pistaTemp.add(newNo);
		}

		for (Iterator<No> iter = pistaTemp.iterator(); iter.hasNext();) {
			No no = iter.next();
			if (noAnt == null) {
				noAnt = no;
			} else {
				Point p1 = noAnt.getPoint();
				Point p2 = no.getPoint();
				Point p3 = new Point(p2.x, p2.y);
				p1.x *= multi;
				p1.y *= multi;
				p2.x *= multi;
				p2.y *= multi;
				pistaKey.add(noAnt);
				pistaFull.addAll(converterPointNo(GeoUtil.drawBresenhamLine(p1, p2), noAnt));
				no.setPoint(p3);
				noAnt = no;
			}
		}

		if (!pistaTemp.isEmpty()) {
			No no = (No) pistaTemp.get(0);
			Point p1 = noAnt.getPoint();
			Point p2 = no.getPoint();
			p1.x *= multi;
			p1.y *= multi;
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
		for (Iterator<No> iter = box.iterator(); iter.hasNext();) {
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

		for (Iterator<No> iter = boxTemp.iterator(); iter.hasNext();) {
			No no = iter.next();
			if (noAnt == null) {
				noAnt = no;
			} else {
				Point p1 = noAnt.getPoint();
				Point p2 = no.getPoint();
				Point p3 = new Point(p2.x, p2.y);
				p1.x *= multi;
				p1.y *= multi;
				p2.x *= multi;
				p2.y *= multi;
				boxKey.add(noAnt);
				boxFull.addAll(converterPointNo(GeoUtil.drawBresenhamLine(p1, p2), noAnt));
				no.setPoint(p3);
				noAnt = no;
			}
		}
		if (!boxTemp.isEmpty()) {
			No no = (No) boxTemp.get(boxTemp.size() - 1);
			Point p1 = noAnt.getPoint();
			Point p2 = no.getPoint();
			p1.x *= multi;
			p1.y *= multi;
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
		gerarEscapeMap();
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

	private void gerarEscapeMap() {
		escapeMap = new HashMap<PontoEscape, List<No>>();
		pista4Full = new ArrayList<No>();
		pista4Full.addAll(pista2Full);
		pista5Full = new ArrayList<No>();
		pista5Full.addAll(pista1Full);
		List<No> nosDaPista = getPistaFull();
		for (Iterator<Point> iterator = escapeList.iterator(); iterator.hasNext();) {
			Point pointDerrapagem = iterator.next();
			No noPerto = null;
			double menorDistancia = Double.MAX_VALUE;
			for (Iterator iterator2 = nosDaPista.iterator(); iterator2.hasNext();) {
				No no = (No) iterator2.next();
				Point pointPista = (Point) no.getPoint();
				double distaciaEntrePontos = GeoUtil.distaciaEntrePontos(pointPista, pointDerrapagem);
				if (distaciaEntrePontos < menorDistancia) {
					menorDistancia = distaciaEntrePontos;
					noPerto = no;
				}
			}
			if (noPerto == null) {
				continue;
			}
			Point p = noPerto.getPoint();
			Rectangle2D rectangle = new Rectangle2D.Double((p.x - Carro.MEIA_ALTURA * Carro.FATOR_AREA_CARRO),
					(p.y - Carro.MEIA_ALTURA * Carro.FATOR_AREA_CARRO), Carro.ALTURA * Carro.FATOR_AREA_CARRO,
					Carro.ALTURA * Carro.FATOR_AREA_CARRO);
			int cont = noPerto.getIndex();
			int traz = cont - 44;
			int frente = cont + 44;
			Point trazCar = ((No) nosDaPista.get(traz)).getPoint();
			Point frenteCar = ((No) nosDaPista.get(frente)).getPoint();
			double calculaAngulo = GeoUtil.calculaAngulo(frenteCar, trazCar, 0);
			Point p1 = GeoUtil.calculaPonto(calculaAngulo, Util.inteiro(Carro.ALTURA * getMultiplicadorLarguraPista()),
					new Point(Util.inteiro(rectangle.getCenterX()), Util.inteiro(rectangle.getCenterY())));
			Point p2 = GeoUtil.calculaPonto(calculaAngulo + 180,
					Util.inteiro(Carro.ALTURA * getMultiplicadorLarguraPista()),
					new Point(Util.inteiro(rectangle.getCenterX()), Util.inteiro(rectangle.getCenterY())));
			double distaciaEntrePontos1 = GeoUtil.distaciaEntrePontos(p1, pointDerrapagem);
			double distaciaEntrePontos2 = GeoUtil.distaciaEntrePontos(p2, pointDerrapagem);
			int contSaida = 0;
			int max = Util.inteiro(Carro.ALTURA * 3.5 * getMultiplicadorLarguraPista());
			int contVolta = max;
			int index = noPerto.getIndex();
			int contMax = (int) (index + (max * 15));
			int contPonto = 0;
			if (distaciaEntrePontos1 < distaciaEntrePontos2) {
				PontoEscape ponto = new PontoEscape();
				ponto.setPoint(pointDerrapagem);
				ponto.setPista(5);
				pista5Full.set(index, pista1Full.get(index));
				Point p5 = null;
				for (int i = index; i < contMax; i++) {
					No noIndex = pista1Full.get(i);
					if (i % 2 == 0) {
						contSaida++;
					}
					if (contSaida < max) {
						contPonto = contSaida;
					} else if (contVolta > 0) {
						boolean sair = false;
						for (int j = (index + 10); j < pista1Full.size(); j++) {
							if (GeoUtil.distaciaEntrePontos(pista1Full.get(j).getPoint(), p5) < 1) {
								sair = true;
							}
						}
						if (sair) {
							break;
						}
						if (i % 3 == 0) {
							contVolta--;
						}
						contPonto = contVolta;
					}
					p5 = GeoUtil.calculaPonto(calculaAngulo, Util.inteiro(contPonto), noIndex.getPoint());
					No newNo = new No();
					newNo.setPoint(p5);
					newNo.setTracado(5);
					newNo.setTipo(noIndex.getTipo());
					pista5Full.set(i, newNo);
				}
				escapeMap.put(ponto, pista5Full);
			}
			if (distaciaEntrePontos2 < distaciaEntrePontos1) {
				PontoEscape ponto = new PontoEscape();
				ponto.setPoint(pointDerrapagem);
				ponto.setPista(4);
				pista4Full.set(index, pista2Full.get(index));
				Point p4 = null;
				for (int i = index; i < contMax; i++) {
					No noIndex = pista2Full.get(i);
					if (i % 2 == 0) {
						contSaida++;
					}
					if (contSaida < max) {
						contPonto = contSaida;
					} else if (contVolta > 0) {
						boolean sair = false;
						for (int j = (index + 10); j < pista2Full.size(); j++) {
							if (GeoUtil.distaciaEntrePontos(pista2Full.get(j).getPoint(), p4) < 1) {
								sair = true;
							}
						}
						if (sair) {
							break;
						}
                        if (i % 3 == 0) {
							contVolta--;
						}
						contPonto = contVolta;
					}
					p4 = GeoUtil.calculaPonto(calculaAngulo + 180, Util.inteiro(contPonto), noIndex.getPoint());
					No newNo = new No();
					newNo.setPoint(p4);
					newNo.setTipo(noIndex.getTipo());
					newNo.setTracado(4);
					pista4Full.set(i, newNo);
				}
				escapeMap.put(ponto, pista4Full);
			}
		}
		for (int i = 0; i < pista4Full.size(); i++) {
			No no = pista4Full.get(i);
			if (no.getTracado() != 4) {
				pista4Full.set(i, null);
			}
		}
		for (int i = 0; i < pista5Full.size(); i++) {
			No no = pista5Full.get(i);
			if (no.getTracado() != 5) {
				pista5Full.set(i, null);
			}
		}
	}

	public static void main(String[] args) {
		List<String> teste = new ArrayList<String>();
		teste.add("ASD");
		teste.add(null);
		teste.add("BLA");
		for (int i = 0; i < teste.size(); i++) {
			System.out.println(teste.get(i));
		}
	}

	private void gerarEscapeList() {
		escapeList = new ArrayList<Point>();
		List<ObjetoPista> objetos = getObjetos();
		if (objetos != null) {
			for (ObjetoPista objetoPista : objetos) {
				if (objetoPista instanceof ObjetoEscapada) {
					ObjetoEscapada objetoEscapada = (ObjetoEscapada) objetoPista;
					escapeList.add(objetoEscapada.centro());
				}
			}
		}
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

		for (Iterator<No> iter = box.iterator(); iter.hasNext();) {
			No no = iter.next();
			if (noAnt == null) {
				noAnt = no;
			} else {
				arrayList.addAll(converterPointNo(GeoUtil.drawBresenhamLine(noAnt.getPoint(), no.getPoint()), noAnt));
				noAnt = no;
			}
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

		for (Iterator<Point> iter = list.iterator(); iter.hasNext();) {
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

	public void setBackGround(String backGround) {
		this.backGround = backGround;
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

	public List<ObjetoPista> getObjetos() {
		return objetos;
	}

	public void setObjetos(List<ObjetoPista> objetos) {
		this.objetos = objetos;
	}

	public Color getCorFundo() {
		return corFundo;
	}

	public void setCorFundo(Color corFundo) {
		this.corFundo = corFundo;
	}

	public boolean isNoite() {
		return noite;
	}

	public void setNoite(boolean noite) {
		this.noite = noite;
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

	public List<ObjetoPistaJSon> getObjetosNoTransparencia() {
		return objetosNoTransparencia;
	}

	public void gerarObjetosNoTransparencia() {
		objetosNoTransparencia = new ArrayList<ObjetoPistaJSon>();
		List<ObjetoPista> objetospista = getObjetos();
		if (objetospista == null) {
			return;
		}
		for (Iterator<ObjetoPista> iterator = objetospista.iterator(); iterator.hasNext();) {
			ObjetoPista objetoPista = iterator.next();
			if (!(objetoPista instanceof ObjetoTransparencia))
				continue;

			ObjetoTransparencia objetoTransparencia = (ObjetoTransparencia) objetoPista;
			Rectangle area = objetoPista.obterArea();
			if (area == null || area.width <= 0 || area.height <= 0) {
				continue;
			}
			ObjetoPistaJSon objetoPistaJSon = new ObjetoPistaJSon();
			objetoPistaJSon.setX(objetoTransparencia.getPosicaoQuina().x);
			objetoPistaJSon.setY(objetoTransparencia.getPosicaoQuina().y);
			objetoPistaJSon.setIndexInicio(objetoTransparencia.getInicioTransparencia());
			objetoPistaJSon.setIndexFim(objetoTransparencia.getFimTransparencia());
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

	public Map<PontoEscape, List<No>> getEscapeMap() {
		return escapeMap;
	}

	public void setEscapeMap(Map<PontoEscape, List<No>> escapeMap) {
		this.escapeMap = escapeMap;
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
		for (Iterator iterator = pista.iterator(); iterator.hasNext();) {
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
		setarHints(g2d);
		desenhaMiniCircuito(g2d, 0, 0);
		return image;
	}

	private void setarHints(Graphics2D g2d) {
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
		g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g2d.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
		g2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
		g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

	}

	public void desenhaMiniCircuito(Graphics2D g2d, int x, int y) {
		int maxLagura = 0;
		g2d.setStroke(new BasicStroke(3.0f));
		g2d.setColor(Color.BLACK);
		List pista = getPista();
		ArrayList pistaMinimizada = new ArrayList();
		double doubleMulti = 25;
		Map map = new HashMap();
		for (Iterator iterator = pista.iterator(); iterator.hasNext();) {
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
		for (Iterator iterator = box.iterator(); iterator.hasNext();) {
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
		for (Iterator iterator = pistaMinimizada.iterator(); iterator.hasNext();) {
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
		if (ultNo.verificaCurvaBaixa()) {
			g2d.setColor(Color.red);
		} else if (ultNo.verificaCurvaAlta()) {
			g2d.setColor(Color.orange);
		} else if (ultNo.verificaRetaOuLargada()) {
			g2d.setColor(new Color(0, 200, 0));
		}
		g2d.drawLine(o.x + oldP.x + incX + x, o.y + oldP.y + y, o.x + p0.x + incX + x, o.y + p0.y + y);

		g2d.setStroke(new BasicStroke(2.0f));
		oldP = null;
		g2d.setColor(Color.lightGray);
		for (Iterator iterator = boxMinimizado.iterator(); iterator.hasNext();) {
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
			i = new Integer(indice);
		} catch (Exception e) {
		}
		if (i == null) {
			return null;
		}
		List<ObjetoTransparencia> objsTransp = new ArrayList<ObjetoTransparencia>();
		for (Iterator iterator = objetos.iterator(); iterator.hasNext();) {
			ObjetoPista objetoPista = (ObjetoPista) iterator.next();
			if (objetoPista instanceof ObjetoTransparencia) {
				objsTransp.add((ObjetoTransparencia) objetoPista);
			}
		}

		ObjetoTransparencia transparencia = objsTransp.get(i);
		;
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
		if (velocidadePista == 0) {
			return Constantes.VELOCIDADE_PISTA;
		}
		return velocidadePista;
	}

	public void setVelocidadePista(double velocidadePista) {
		this.velocidadePista = velocidadePista;
	}

}
