package sowbreira.f1mane.controles;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import br.nnpe.Logger;
import sowbreira.f1mane.entidades.Carro;
import sowbreira.f1mane.entidades.Circuito;
import sowbreira.f1mane.entidades.No;
import sowbreira.f1mane.entidades.Piloto;
import sowbreira.f1mane.entidades.PontoEscape;
import sowbreira.f1mane.recursos.CarregadorRecursos;

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
	protected Map<String, String> circuitos = new HashMap<String, String>();
	protected Map<No, No> mapaNoProxCurva = new HashMap<No, No>();
	protected Map<No, No> mapaNoCurvaAnterior = new HashMap<No, No>();
	protected Map<Integer, No> mapaIdsNos = new HashMap<Integer, No>();
	protected Map<No, Integer> mapaNosIds = new HashMap<No, Integer>();
	private String temporada;
	private Set<Integer> idsNoPista = new HashSet<Integer>();
	private Set<Integer> idsNoBox = new HashSet<Integer>();

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

	private BufferedImage obterCarroCimaSemAreofolio(Piloto piloto,
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
		carregadorRecursos = CarregadorRecursos.getCarregadorRecursos();
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
		carregadorRecursos = CarregadorRecursos.getCarregadorRecursos();
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
				.hasNext();) {
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
		for (Iterator<No> iter = nosDaPista.iterator(); iter.hasNext();) {
			No noPsita = iter.next();
			Integer pistaId = new Integer(contId++);
			mapaIdsNos.put(pistaId, noPsita);
			mapaNosIds.put(noPsita, pistaId);
			idsNoPista.add(pistaId);
			if (noPsita.verificaRetaOuLargada()) {
				listaDeRetas.add(noPsita);
			}
		}

		for (Iterator iter = nosDoBox.iterator(); iter.hasNext();) {
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
		for (Iterator iterator = keySet.iterator(); iterator.hasNext();) {
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

	public static void main(String[] args) {
		// String strVal = String.valueOf(Util.intervalo(50, 99))
		// + Util.intervalo(0, 9);
		// Logger.logar(((int) (0 + Math.random() * 9)));

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
			Circuito circuitoObj =  CarregadorRecursos.carregarCircuito(string); 
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

		for (Iterator iterator = pilotos.iterator(); iterator.hasNext();) {
			Piloto piloto = (Piloto) iterator.next();
			if (idInt == piloto.getId()) {
				return piloto;
			}
		}
		return null;
	}

}
