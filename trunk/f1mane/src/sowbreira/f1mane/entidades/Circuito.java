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
	private List pistaInflada = new ArrayList();
	private Map nosOut = new HashMap();
	private Map nosIn = new HashMap();
	private List box = new ArrayList();

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
		List arrayListM = new ArrayList();
		No noAnt = null;

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
			arrayListM.addAll(converterPointNo(GeoUtil
					.drawBresenhamLine(p1, p2), noAnt));

		}
		if (nosIn == null) {
			nosIn = new HashMap();
		}
		if (nosOut == null) {
			nosOut = new HashMap();
		}
		int menorX = Integer.MAX_VALUE;
		int menorY = Integer.MAX_VALUE;
		int maiorX = 0;
		int maiorY = 0;
		for (int i = 0; i < arrayListM.size(); i++) {
			No no = (No) arrayListM.get(i);
			no.setIndex(i);
			Point p = no.getPoint();
			if (p.x < menorX) {
				menorX = p.x;
			}
			if (p.y < menorY) {
				menorY = p.y;
			}
			if (p.x > maiorX) {
				maiorX = p.x;
			}
			if (p.y > maiorY) {
				maiorY = p.y;
			}

			// Point pOut = new Point(p.x, p.y);
			// pOut.x *= 1.5;
			// pOut.y *= 1.5;
			// nosIn.put(no, pOut);
		}
		List retaM = GeoUtil.drawBresenhamLine(new Point(menorX, menorY),
				new Point(maiorX, maiorY));
		Point midM = (Point) retaM.get((retaM.size() - 1) / 2);

		for (int i = 0; i < arrayListM.size(); i++) {
			No no = (No) arrayListM.get(i);
			Point p = no.getPoint();
			Point pIn = new Point(p.x, p.y);

			if (p.x < midM.x) {
				pIn.x += 15;
			}
			if (p.y < midM.y) {
				pIn.y += 15;
			}
			if (p.x > midM.x) {
				pIn.x -= 15;
			}
			if (p.y > midM.y) {
				pIn.y -= 15;
			}
			nosIn.put(no, pIn);
		}

		if (pistaInflada == null) {
			pistaInflada = new ArrayList();
		}
		pistaInflada.clear();
		pistaInflada.addAll(arrayListM);
	}

	public List getPistaInflada() {
		return pistaInflada;
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

	public Map getNosOut() {
		return nosOut;
	}

	public Map getNosIn() {
		return nosIn;
	}

}
