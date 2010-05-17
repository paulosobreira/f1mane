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
	private List pistaFull = new ArrayList();
	private List pistaKey = new ArrayList();
	private List box = new ArrayList();
	private List boxFull = new ArrayList();
	private List boxKey = new ArrayList();

	private double multiplicadorPista;
	private int trk = 220;
	private ArrayList ptsCurvaBaixa;

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

	public List getBoxFull() {
		return boxFull;
	}

	public List getBoxKey() {
		return boxKey;
	}

	public void geraPontosPistaInflada(double multi) {
		multiplicadorPista = multi;
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
				pistaFull.addAll(converterPointNo(GeoUtil.drawBresenhamLine(p1,
						p2), noAnt));
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
		ptsCurvaBaixa = new ArrayList();
		for (int i = 0; i < pistaFull.size(); i++) {
			No no = (No) pistaFull.get(i);
			no.setIndex(i);
			if (No.CURVA_BAIXA.equals(no.getTipo())
					|| No.CURVA_ALTA.equals(no.getTipo())) {
				ptsCurvaBaixa.add(no);
			}
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
				boxFull.addAll(converterPointNo(GeoUtil.drawBresenhamLine(p1,
						p2), noAnt));
				no.setPoint(p3);
				noAnt = no;
			}
		}

		if (!boxTemp.isEmpty()) {
			No no = (No) boxTemp.get(0);
			Point p1 = noAnt.getPoint();
			Point p2 = no.getPoint();
			p1.x *= multi;
			p1.y *= multi;
			boxKey.add(noAnt);
			boxFull.addAll(converterPointNo(GeoUtil.drawBresenhamLine(p1, p2),
					noAnt));

		}

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

	public List getPistaKey() {
		return pistaKey;
	}

	public double getMultiplciador() {
		return multiplicadorPista;
	}

	public ArrayList getPtsCurvaBaixa() {
		return ptsCurvaBaixa;
	}

}
