package sowbreira.f1mane.entidades;

import java.awt.Color;
import java.awt.Point;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import br.nnpe.GeoUtil;

public class Circuito implements Serializable {
	private static final long serialVersionUID = -1488529358105580761L;
	private String backGround;
	private List pista = new ArrayList();
	private transient List pistaFull = new ArrayList();
	private transient List pistaKey = new ArrayList();
	private List box = new ArrayList();
	private transient List boxFull = new ArrayList();
	private transient List boxKey = new ArrayList();
	private double multiplicadorPista;
	private double multiplicadorLarguraPista;
	private int ladoBox = 0;
	private int ladoBoxSaidaBox = 0;
	private int probalidadeChuva = 0;
	private int entradaBoxIndex;
	private int saidaBoxIndex;
	private int paradaBoxIndex;
	private Color corFundo;
	private List<ObjetoPista> objetos;
	private String nome;
	private boolean noite;
	private boolean usaBkg;
	private Point creditos;
	private List<Point> escapeList = new ArrayList<Point>();

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

	public List geraPontosPista() {
		List arrayList = new ArrayList();
		No noAnt = null;

		for (Iterator iter = pista.iterator(); iter.hasNext();) {
			No no = (No) iter.next();

			if (noAnt == null) {
				noAnt = no;
			} else {
				arrayList.addAll(converterPointNo(
						GeoUtil.drawBresenhamLine(noAnt.getPoint(),
								no.getPoint()), noAnt));
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

	public List getBoxFull() {
		return boxFull;
	}

	public List getBoxKey() {
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
			pistaFull = new ArrayList();
		}
		pistaFull.clear();
		if (pistaKey == null) {
			pistaKey = new ArrayList();
		}
		pistaKey.clear();
		List pistaTemp = new ArrayList();
		for (Iterator iter = pista.iterator(); iter.hasNext();) {
			No no = (No) iter.next();
			No newNo = new No();
			newNo.setPoint(new Point(no.getPoint().x, no.getPoint().y));
			newNo.setTipo(no.getTipo());
			pistaTemp.add(newNo);
		}

		for (Iterator iter = pistaTemp.iterator(); iter.hasNext();) {
			No no = (No) iter.next();
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
			pistaFull.addAll(converterPointNo(
					GeoUtil.drawBresenhamLine(p1, p2), noAnt));

		}
		for (int i = 0; i < pistaFull.size(); i++) {
			No no = (No) pistaFull.get(i);
			no.setIndex(i);
		}

		if (boxFull == null) {
			boxFull = new ArrayList();
		}
		boxFull.clear();
		if (boxKey == null) {
			boxKey = new ArrayList();
		}
		boxKey.clear();
		List boxTemp = new ArrayList();
		for (Iterator iter = box.iterator(); iter.hasNext();) {
			No no = (No) iter.next();
			No newNo = new No();
			newNo.setPoint(new Point(no.getPoint().x, no.getPoint().y));
			newNo.setTipo(no.getTipo());
			boxTemp.add(newNo);
		}

		noAnt = null;

		for (Iterator iter = boxTemp.iterator(); iter.hasNext();) {
			No no = (No) iter.next();
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
			boxFull.addAll(converterPointNo(GeoUtil.drawBresenhamLine(p1, p2),
					noAnt));

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
				List entrada = GeoUtil.drawBresenhamLine(boxEntrada.getPoint(),
						pistaNo.getPoint());
				if (entrada.size() < entradaBoxSize) {
					entradaBoxSize = entrada.size();
					entradaBoxIndex = i;
				}
				List saida = GeoUtil.drawBresenhamLine(boxSaida.getPoint(),
						pistaNo.getPoint());
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

	public List getPistaFull() {
		return pistaFull;
	}

	public List geraPontosBox() {
		List arrayList = new ArrayList();
		No noAnt = null;

		for (Iterator iter = box.iterator(); iter.hasNext();) {
			No no = (No) iter.next();

			if (noAnt == null) {
				noAnt = no;
			} else {
				arrayList.addAll(converterPointNo(
						GeoUtil.drawBresenhamLine(noAnt.getPoint(),
								no.getPoint()), noAnt));
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

	public Collection converterPointNo(List list, No no) {
		List retorno = new ArrayList();

		for (Iterator iter = list.iterator(); iter.hasNext();) {
			Point element = (Point) iter.next();
			No newNo = new No();
			newNo.setPoint(element);
			newNo.setTipo(no.getTipo());
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

	public List getBox() {
		return box;
	}

	public void setBox(List box) {
		this.box = box;
	}

	public List getPista() {
		return pista;
	}

	public void setPista(List pista) {
		this.pista = pista;
	}

	public List getPistaKey() {
		return pistaKey;
	}

	public double getMultiplciador() {
		//return multiplicadorPista;
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

}
