package sowbreira.f1mane.entidades;

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
	private List pistaInfladaFull = new ArrayList();
	private List pistaInfladaKey = new ArrayList();
	private Map nosOutKeys = new HashMap();
	private Map nosInKeys = new HashMap();
	private List box = new ArrayList();
	private double multiInfla;

	public List geraPontosPista() {
		List arrayList = new ArrayList();
		No noAnt = null;

		for (Iterator iter = pista.iterator(); iter.hasNext();) {
			No no = (No) iter.next();

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
			arrayList.addAll(converterPointNo(GeoUtil.drawBresenhamLine(noAnt
					.getPoint(), no.getPoint()), noAnt));
		}
		for (int i = 0; i < arrayList.size(); i++) {
			No no = (No) arrayList.get(i);
			no.setIndex(i);
		}
		return arrayList;
	}

	public void geraPontosPistaInflada(double multi) {
		multiInfla = multi;
		List arrayListM = new ArrayList();
		No noAnt = null;
		if (pistaInfladaFull == null) {
			pistaInfladaFull = new ArrayList();
		}
		if (pistaInfladaKey == null) {
			pistaInfladaKey = new ArrayList();
		}

		for (Iterator iter = pista.iterator(); iter.hasNext();) {
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
				pistaInfladaKey.add(noAnt);
				arrayListM.addAll(converterPointNo(GeoUtil.drawBresenhamLine(
						p1, p2), noAnt));
				no.setPoint(p3);
				noAnt = no;
			}
		}

		if (!pista.isEmpty()) {
			No no = (No) pista.get(0);
			Point p1 = noAnt.getPoint();
			Point p2 = no.getPoint();
			p1.x *= multi;
			p1.y *= multi;
			pistaInfladaKey.add(noAnt);
			arrayListM.addAll(converterPointNo(GeoUtil
					.drawBresenhamLine(p1, p2), noAnt));

		}
		for (int i = 0; i < arrayListM.size(); i++) {
			No no = (No) arrayListM.get(i);
			no.setIndex(i);
		}

		pistaInfladaFull.clear();
		pistaInfladaFull.addAll(arrayListM);
		gerarPistasAlternativas();
	}

	private void gerarPistasAlternativas() {
		No noAnt = null;
		if (nosInKeys == null) {
			nosInKeys = new HashMap();
		}
		if (nosOutKeys == null) {
			nosOutKeys = new HashMap();
		}
		int trk = (int) (multiInfla * 2);
		for (int i = 0; i < pistaInfladaKey.size(); i++) {
			No no = (No) pistaInfladaKey.get(i);
			Point p = no.getPoint();
			Point in = new Point(p.x - trk, p.y + trk);
			Point out = new Point(p.x + trk, p.y - trk);
			nosInKeys.put(no, in);
			nosOutKeys.put(no, out);
			noAnt = no;
		}
	}

	public List getPistaInflada() {
		return pistaInfladaFull;
	}

	public List geraPontosBox() {
		List arrayList = new ArrayList();
		No noAnt = null;

		for (Iterator iter = box.iterator(); iter.hasNext();) {
			No no = (No) iter.next();

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

	public Map getNosOutKeys() {
		return nosOutKeys;
	}

	public Map getNosInKeys() {
		return nosInKeys;
	}

	public List getPistaInfladaKey() {
		return pistaInfladaKey;
	}

	public double getMultiInfla() {
		return multiInfla;
	}

}
