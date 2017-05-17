package sowbreira.f1mane.entidades;

import java.awt.Color;
import java.awt.Point;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import br.nnpe.GeoUtil;
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class Circuito implements Serializable {
	private static final long serialVersionUID = -1488529358105580761L;
	private String backGround;
	private List<No> pista = new ArrayList<No>();
	private transient List<No> pistaFull = new ArrayList<No>();
	private transient List<No> pistaKey = new ArrayList<No>();
	private List<No> box = new ArrayList<No>();
	private transient List<No> boxFull = new ArrayList<No>();
	private transient List<No> boxKey = new ArrayList<No>();
	private double multiplicadorPista;
	private double multiplicadorLarguraPista;
	private int ladoBox = 0;
	private int ladoBoxSaidaBox = 0;
	private int probalidadeChuva = 0;
	private int entradaBoxIndex;
	private int saidaBoxIndex;
	private int paradaBoxIndex;
	private String nome;
	private boolean noite;
	private boolean usaBkg;
	private transient List<ObjetoPistaJSon> objetosNoTransparencia;
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
				arrayList.addAll(converterPointNo(GeoUtil.drawBresenhamLine(
						noAnt.getPoint(), no.getPoint()), noAnt));
				noAnt = no;
			}
		}

		if (!pista.isEmpty()) {
			No no = (No) pista.get(0);
			arrayList.addAll(converterPointNo(
					GeoUtil.drawBresenhamLine(noAnt.getPoint(), no.getPoint()),
					noAnt));
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

	public void vetorizarPista(double multi, double larg) {
		multiplicadorLarguraPista = larg;
		multiplicadorPista = multi;
		if (usaBkg) {
			multi = 1;
		}
		No noAnt = null;
		if (pistaFull == null) {
			pistaFull = new ArrayList<No>();
		}
		pistaFull.clear();
		if (pistaKey == null) {
			pistaKey = new ArrayList<No>();
		}
		pistaKey.clear();
		List<No> pistaTemp = new ArrayList<No>();
		for (Iterator<No> iter = pista.iterator(); iter.hasNext();) {
			No no = iter.next();
			No newNo = new No();
			newNo.setPoint(new Point(no.getPoint().x, no.getPoint().y));
			newNo.setTipo(no.getTipo());
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
				pistaFull.addAll(converterPointNo(
						GeoUtil.drawBresenhamLine(p1, p2), noAnt));
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
			pistaFull.addAll(
					converterPointNo(GeoUtil.drawBresenhamLine(p1, p2), noAnt));

		}
		for (int i = 0; i < pistaFull.size(); i++) {
			No no = (No) pistaFull.get(i);
			no.setIndex(i);
		}

		if (boxFull == null) {
			boxFull = new ArrayList<No>();
		}
		boxFull.clear();
		if (boxKey == null) {
			boxKey = new ArrayList<No>();
		}
		boxKey.clear();
		List<No> boxTemp = new ArrayList<No>();
		for (Iterator<No> iter = box.iterator(); iter.hasNext();) {
			No no = iter.next();
			no.setBox(true);
			No newNo = new No();
			newNo.setPoint(new Point(no.getPoint().x, no.getPoint().y));
			newNo.setTipo(no.getTipo());
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
				boxFull.addAll(converterPointNo(
						GeoUtil.drawBresenhamLine(p1, p2), noAnt));
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
			boxFull.addAll(
					converterPointNo(GeoUtil.drawBresenhamLine(p1, p2), noAnt));

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
				List<Point> entrada = GeoUtil.drawBresenhamLine(
						boxEntrada.getPoint(), pistaNo.getPoint());
				if (entrada.size() < entradaBoxSize) {
					entradaBoxSize = entrada.size();
					entradaBoxIndex = i;
				}
				List<Point> saida = GeoUtil.drawBresenhamLine(
						boxSaida.getPoint(), pistaNo.getPoint());
				if (saida.size() < saidaBoxSize) {
					saidaBoxSize = saida.size();
					saidaBoxIndex = i;
				}
			}
			for (int i = 0; i < boxFull.size(); i++) {
				No boxNo = (No) boxFull.get(i);
				if (No.PARADA_BOX.equals(boxNo.getTipo())) {
					paradaBoxIndex = i;
					break;
				}
			}
		}
	}

	public int getParadaBoxIndex() {
		return paradaBoxIndex;
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
				arrayList.addAll(converterPointNo(GeoUtil.drawBresenhamLine(
						noAnt.getPoint(), no.getPoint()), noAnt));
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

	public void setEscapeList(List<Point> escapeList) {
		this.escapeList = escapeList;
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
		for (Iterator<ObjetoPista> iterator = objetospista.iterator(); iterator
				.hasNext();) {
			ObjetoPista objetoPista = iterator.next();
			if (!(objetoPista instanceof ObjetoTransparencia))
				continue;
			ObjetoTransparencia objetoTransparencia = (ObjetoTransparencia) objetoPista;
			List<Point> pontosPoint = objetoTransparencia.getPontos();
			ObjetoPistaJSon objetoPistaJSon = new ObjetoPistaJSon();
			List<Ponto> pontos = new ArrayList<Ponto>();
			objetoPistaJSon.setPontos(pontos);
			objetoPistaJSon.setIndexInicio(
					objetoTransparencia.getInicioTransparencia());
			objetoPistaJSon
					.setIndexFim(objetoTransparencia.getFimTransparencia());
			objetosNoTransparencia.add(objetoPistaJSon);
			for (Iterator<Point> iterator2 = pontosPoint.iterator(); iterator2
					.hasNext();) {
				Point point = iterator2.next();
				Ponto ponto = new Ponto();
				ponto.setPoint(point);
				pontos.add(ponto);
			}
		}

	}

}
