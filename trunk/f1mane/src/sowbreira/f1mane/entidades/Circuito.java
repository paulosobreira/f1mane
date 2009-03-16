package sowbreira.f1mane.entidades;

import java.awt.Point;
import java.io.Serializable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import br.nnpe.GeoUtil;

public class Circuito implements Serializable {
	private static final long serialVersionUID = -1488529358105580761L;
	private String backGround;
	private List pista = new ArrayList();
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
}
