package sowbreira.f1mane.controles;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import sowbreira.f1mane.entidades.Carro;
import sowbreira.f1mane.entidades.Circuito;
import sowbreira.f1mane.entidades.No;
import sowbreira.f1mane.entidades.NoWrapper;
import sowbreira.f1mane.entidades.ObjetoEscapada;
import sowbreira.f1mane.entidades.ObjetoPista;
import sowbreira.f1mane.entidades.Piloto;
import sowbreira.f1mane.recursos.CarregadorRecursos;
import br.nnpe.GeoUtil;
import br.nnpe.ImageUtil;
import br.nnpe.Logger;
import br.nnpe.Util;

/**
 * @author Paulo Sobreira
 */
public abstract class ControleRecursos {
	protected Circuito circuito;
	protected List<Piloto> pilotos = new ArrayList<Piloto>();
	protected List nosDaPista = new ArrayList();
	protected List nosDoBox = new ArrayList();
	protected List carros;
	protected List<NoWrapper> pistaWrapperFull = new ArrayList<NoWrapper>();
	protected List<NoWrapper> boxWrapperFull = new ArrayList<NoWrapper>();
	protected CarregadorRecursos carregadorRecursos;
	protected Map<String, String> circuitos = new HashMap<String, String>();
	protected Map<String, String> temporadasTransp = new HashMap<String, String>();
	protected Map<No, No> mapaNoProxCurva = new HashMap<No, No>();
	protected Map<Point, Integer> mapaLadosDerrapaCurva = new HashMap<Point, Integer>();
	protected Map<Integer, No> mapaIdsNos = new HashMap<Integer, No>();
	protected Map<No, Integer> mapaNosIds = new HashMap<No, Integer>();
	private final static int TRANPS = 250;
	private String seasson;
	private Set<Integer> idsNoPista = new HashSet<Integer>();
	private Set<Integer> idsNoBox = new HashSet<Integer>();
	private static Map<String, BufferedImage> bufferCarrosCima = new HashMap<String, BufferedImage>();
	private static Map<String, BufferedImage> bufferCarrosCimaSemAreofolio = new HashMap<String, BufferedImage>();
	private static Map<String, BufferedImage> bufferCarrosLado = new HashMap<String, BufferedImage>();
	private static Map<String, BufferedImage> bufferCapacete = new HashMap<String, BufferedImage>();
	private static Map<String, BufferedImage> bufferCarrosLadoSemAreofolio = new HashMap<String, BufferedImage>();

	public BufferedImage obterCapacete(Piloto piloto) {
		try {
			String chave = piloto.getNomeOriginal()
					+ piloto.getCarro().getNome();
			BufferedImage ret = bufferCapacete.get(chave);
			if (ret == null) {
				ret = CarregadorRecursos.carregaImagem("capacetes/" + seasson
						+ "/" + piloto.getNomeOriginal().replaceAll("\\.", "")
						+ ".png");
				if (ret == null) {
					ret = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);
				}
				bufferCapacete.put(chave, ret);
			}
			return ret;
		} catch (Exception e) {
			return null;
		}
	}

	public BufferedImage obterCarroLado(Piloto piloto) {
		Carro carro = piloto.getCarro();
		if (Carro.PERDEU_AEREOFOLIO.equals(piloto.getCarro().getDanificado())) {
			return obterCarroLadoSemAreofolio(piloto);

		}
		BufferedImage carroLado = bufferCarrosLado.get(carro.getNome());
		if (carroLado == null) {
			carroLado = CarregadorRecursos.carregaImagem("CarroLado.png");
			BufferedImage cor1 = CarregadorRecursos.gerarCoresCarros(
					carro.getCor1(), "CarroLadoC1.png");
			BufferedImage cor2 = CarregadorRecursos.gerarCoresCarros(
					carro.getCor2(), "CarroLadoC2.png");
			Graphics graphics = carroLado.getGraphics();
			graphics.drawImage(cor1, 0, 0, null);
			graphics.drawImage(cor2, 0, 0, null);
			graphics.dispose();
			if (carro.getImg() != null) {
				try {
					BufferedImage carroLadoPng = null;
					if (carro.getImg().endsWith(".png")) {
						carroLadoPng = CarregadorRecursos.carregaImagem(carro
								.getImg());
						bufferCarrosLado.put(carro.getNome(), carroLadoPng);
						carroLado = carroLadoPng;
					} else {
						carroLadoPng = CarregadorRecursos
								.carregaImgSemCache(carro.getImg());

						if (carroLadoPng != null) {
							carroLado = carroLadoPng;
							Integer transp = new Integer(
									temporadasTransp.get(getTemporada()));
							bufferCarrosLado.put(carro.getNome(), carroLado);
						}
					}
				} catch (Exception e) {
					carro.setImg(null);
					bufferCarrosLado
							.put(carro.getNome(), ImageUtil.geraTransparencia(
									carroLado, Color.WHITE));
				}
			} else {
				bufferCarrosLado.put(carro.getNome(),
						ImageUtil.geraTransparencia(carroLado, Color.WHITE));
			}

		}
		return carroLado;
	}

	private BufferedImage obterCarroLadoSemAreofolio(Piloto piloto) {
		Carro carro = piloto.getCarro();
		BufferedImage carroLado = bufferCarrosLadoSemAreofolio.get(carro
				.getNome());
		if (carroLado == null) {
			carroLado = CarregadorRecursos.carregaImagem("CarroLado.png");
			BufferedImage cor1 = CarregadorRecursos.gerarCoresCarros(
					carro.getCor1(), "CarroLadoC1.png");
			BufferedImage cor2 = CarregadorRecursos.gerarCoresCarros(
					carro.getCor2(), "CarroLadoC3.png");
			Graphics graphics = carroLado.getGraphics();
			graphics.drawImage(cor1, 0, 0, null);
			graphics.drawImage(cor2, 0, 0, null);
			graphics.dispose();
			if (carro.getImg() != null) {
				try {
					BufferedImage carroLadoPng = null;
					if (carro.getImg().endsWith(".png")) {
						carroLadoPng = CarregadorRecursos.carregaImagem(carro
								.getImg());
						carroLado = carroLadoPng;
						bufferCarrosLadoSemAreofolio.put(carro.getNome(),
								carroLadoPng);
					} else {
						carroLadoPng = CarregadorRecursos
								.carregaImgSemCache(carro.getImg());
						if (carroLadoPng != null) {
							carroLado = carroLadoPng;
							Integer transp = new Integer(
									temporadasTransp.get(getTemporada()));
							bufferCarrosLadoSemAreofolio.put(carro.getNome(),
									ImageUtil.geraTransparencia(carroLado,
											transp));
						}
					}
				} catch (Exception e) {
					carro.setImg(null);
					bufferCarrosLadoSemAreofolio
							.put(carro.getNome(), carroLado);
				}
			} else {
				bufferCarrosLadoSemAreofolio.put(carro.getNome(), carroLado);
			}
		}
		return carroLado;
	}

	public No obterNoPorId(int idNo) {
		return mapaIdsNos.get(idNo);
	}

	public Integer obterIdPorNo(No no) {
		return mapaNosIds.get(no);
	}

	protected void limpaBuffers() {
		if (bufferCarrosCima != null)
			bufferCarrosCima.clear();
		if (bufferCarrosCimaSemAreofolio != null)
			bufferCarrosCimaSemAreofolio.clear();
		if (bufferCarrosLado != null)
			bufferCarrosLado.clear();
		if (bufferCarrosLadoSemAreofolio != null)
			bufferCarrosLadoSemAreofolio.clear();
	}

	private BufferedImage obterCarroCimaSemAreofolio(Piloto piloto) {
		Carro carro = piloto.getCarro();
		BufferedImage carroCima = bufferCarrosCimaSemAreofolio.get(carro
				.getNome());
		if (carroCima == null) {
			carroCima = CarregadorRecursos.carregaImagem("CarroCima.png");
			BufferedImage cor1 = CarregadorRecursos.gerarCoresCarros(
					carro.getCor1(), "CarroCimaC1.png");
			BufferedImage cor2 = CarregadorRecursos.gerarCoresCarros(
					carro.getCor2(), "CarroCimaC3.png");
			Graphics graphics = carroCima.getGraphics();
			graphics.drawImage(cor2, 0, 0, null);
			graphics.drawImage(cor1, 0, 0, null);
			graphics.dispose();
			bufferCarrosCimaSemAreofolio.put(carro.getNome(),
					ImageUtil.geraTransparencia(carroCima, Color.WHITE));
		}
		return carroCima;
	}

	public BufferedImage obterCarroCima(Piloto piloto) {
		Carro carro = piloto.getCarro();
		if (Carro.PERDEU_AEREOFOLIO.equals(piloto.getCarro().getDanificado())) {
			return obterCarroCimaSemAreofolio(piloto);

		}
		BufferedImage carroCima = bufferCarrosCima.get(carro.getNome());
		if (carroCima == null) {
			carroCima = CarregadorRecursos.carregaImagem("CarroCima.png");
			BufferedImage cor1 = CarregadorRecursos.gerarCoresCarros(
					carro.getCor1(), "CarroCimaC1.png");
			BufferedImage cor2 = CarregadorRecursos.gerarCoresCarros(
					carro.getCor2(), "CarroCimaC2.png");
			Graphics graphics = carroCima.getGraphics();
			graphics.drawImage(cor2, 0, 0, null);
			graphics.drawImage(cor1, 0, 0, null);
			graphics.dispose();
			bufferCarrosCima.put(carro.getNome(),
					ImageUtil.geraTransparencia(carroCima, Color.WHITE));
		}
		return carroCima;
	}

	public void setTemporada(String seasson) {
		this.seasson = seasson;
	}

	public String getTemporada() {
		return seasson;
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
		carregadorRecursos = new CarregadorRecursos(true);
		carregarTemporadasTransp();
		carregarCircuitos();
	}

	public ControleRecursos(String temporada) throws Exception {
		if (temporada != null) {
			this.seasson = temporada;
		}
		carregarPilotosCarros();
		carregarTemporadasTransp();
		carregarCircuitos();
	}

	public void carregarPilotosCarros() throws IOException {
		carregadorRecursos = new CarregadorRecursos(true);
		carros = carregadorRecursos.carregarListaCarros(seasson);
		pilotos = carregadorRecursos.carregarListaPilotos(seasson);
		carregadorRecursos.ligarPilotosCarros(pilotos, carros);
	}

	public void carregaRecursos(String circuitoStr) throws Exception {
		carregaRecursos(circuitoStr, null, null);
	}

	public void carregaRecursos(String circuitoStr, List<Piloto> pilotos,
			List carros) throws Exception {
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
		pistaWrapperFull.clear();
		boxWrapperFull.clear();
		mapaNoProxCurva.clear();
		ObjectInputStream ois = new ObjectInputStream(carregadorRecursos
				.getClass().getResourceAsStream(circuitoStr));

		circuito = (Circuito) ois.readObject();
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
		int contId = 1;
		for (Iterator iter = nosDaPista.iterator(); iter.hasNext();) {
			No noPsita = (No) iter.next();
			Integer pistaId = new Integer(contId++);
			mapaIdsNos.put(pistaId, noPsita);
			mapaNosIds.put(noPsita, pistaId);
			idsNoPista.add(pistaId);
			if (noPsita.verificaRetaOuLargada())
				mapaNoProxCurva.put(noPsita, null);
		}
		for (Iterator iter = nosDoBox.iterator(); iter.hasNext();) {
			No noDoBox = (No) iter.next();
			Integer boxId = new Integer(contId++);
			mapaIdsNos.put(boxId, noDoBox);
			mapaNosIds.put(noDoBox, boxId);
			idsNoBox.add(boxId);
		}
		for (Iterator<No> iterator = mapaNoProxCurva.keySet().iterator(); iterator
				.hasNext();) {
			No no = iterator.next();
			int index = no.getIndex();
			for (int i = index; i < nosDaPista.size(); i++) {
				No noCurva = (No) nosDaPista.get(i);
				if (noCurva.verificaCruvaBaixa()) {
					mapaNoProxCurva.put(no, noCurva);
					break;
				}
			}
		}
		List<Point> escapeList = circuito.getEscapeList();
		if (escapeList == null || escapeList.isEmpty()) {
			escapeList = new ArrayList<Point>();
			List<ObjetoPista> objetos = circuito.getObjetos();
			if (objetos != null) {
				for (ObjetoPista objetoPista : objetos) {
					if (objetoPista instanceof ObjetoEscapada) {
						ObjetoEscapada objetoEscapada = (ObjetoEscapada) objetoPista;
						escapeList.add(objetoEscapada.centro());
					}
				}
			}
		}
		circuito.setEscapeList(escapeList);
		if (escapeList != null && !escapeList.isEmpty()) {
			for (Iterator<Point> iterator = escapeList.iterator(); iterator
					.hasNext();) {
				Point pointDerrapagem = iterator.next();
				No noPerto = null;
				double menorDistancia = Double.MAX_VALUE;
				for (Iterator iterator2 = nosDaPista.iterator(); iterator2
						.hasNext();) {
					No no = (No) iterator2.next();
					Point pointPista = (Point) no.getPoint();
					double distaciaEntrePontos = GeoUtil.distaciaEntrePontos(
							pointPista, pointDerrapagem);
					if (distaciaEntrePontos < menorDistancia) {
						menorDistancia = distaciaEntrePontos;
						noPerto = no;
					}
				}
				if (noPerto == null) {
					continue;
				}
				Point p = noPerto.getPoint();
				Rectangle2D rectangle = new Rectangle2D.Double(
						(p.x - Carro.MEIA_ALTURA * Carro.FATOR_AREA_CARRO),
						(p.y - Carro.MEIA_ALTURA * Carro.FATOR_AREA_CARRO),
						Carro.ALTURA * Carro.FATOR_AREA_CARRO, Carro.ALTURA
								* Carro.FATOR_AREA_CARRO);
				int cont = noPerto.getIndex();
				int traz = cont - 44;
				int frente = cont + 44;
				Point trazCar = ((No) nosDaPista.get(traz)).getPoint();
				Point frenteCar = ((No) nosDaPista.get(frente)).getPoint();
				double calculaAngulo = GeoUtil.calculaAngulo(frenteCar,
						trazCar, 0);
				Point p1 = GeoUtil.calculaPonto(
						calculaAngulo,
						Util.inte(Carro.ALTURA
								* circuito.getMultiplicadorLarguraPista()),
						new Point(Util.inte(rectangle.getCenterX()), Util
								.inte(rectangle.getCenterY())));
				Point p2 = GeoUtil.calculaPonto(
						calculaAngulo + 180,
						Util.inte(Carro.ALTURA
								* circuito.getMultiplicadorLarguraPista()),
						new Point(Util.inte(rectangle.getCenterX()), Util
								.inte(rectangle.getCenterY())));
				double distaciaEntrePontos1 = GeoUtil.distaciaEntrePontos(p1,
						pointDerrapagem);
				double distaciaEntrePontos2 = GeoUtil.distaciaEntrePontos(p2,
						pointDerrapagem);
				if (distaciaEntrePontos1 < distaciaEntrePontos2) {
					mapaLadosDerrapaCurva.put(pointDerrapagem, 5);
				}
				if (distaciaEntrePontos2 < distaciaEntrePontos1) {
					mapaLadosDerrapaCurva.put(pointDerrapagem, 4);
				}
			}
		}
	}

	public int obterLadoDerrapa(Point pontoDerrapada) {
		return mapaLadosDerrapaCurva.get(pontoDerrapada);
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
		System.out.println("F.Alonso".replaceAll("\\.", ""));
		// Logger.logar(((int) (0 + Math.random() * 9)));

	}


	protected void carregarCircuitos() {
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
	}

	public int porcentagemChuvaCircuito(String circuito) {
		ObjectInputStream ois;
		try {
			ois = new ObjectInputStream(carregadorRecursos.getClass()
					.getResourceAsStream(circuitos.get(circuito)));
			Circuito circuitoObj = (Circuito) ois.readObject();
			return circuitoObj.getProbalidadeChuva();
		} catch (Exception e) {
			Logger.logarExept(e);
		}
		return 0;
	}

	protected void carregarTemporadasTransp() {
		final Properties properties = new Properties();

		try {
			properties
					.load(CarregadorRecursos
							.recursoComoStream("properties/temporadasTransp.properties"));

			Enumeration propName = properties.propertyNames();
			while (propName.hasMoreElements()) {
				final String name = (String) propName.nextElement();
				temporadasTransp.put(name, properties.getProperty(name));

			}
		} catch (IOException e) {
			Logger.logarExept(e);
		}
	}

	public List<NoWrapper> getPistaWrapperFull() {
		if (pistaWrapperFull.isEmpty() && circuito != null) {
			List pistaFull = circuito.getPistaFull();
			for (Iterator iterator = pistaFull.iterator(); iterator.hasNext();) {
				No no = (No) iterator.next();
				pistaWrapperFull.add(new NoWrapper(no));
			}
		}
		return pistaWrapperFull;
	}

	public List<NoWrapper> getBoxWrapperFull() {
		if (boxWrapperFull.isEmpty() && circuito != null) {
			List boxFull = circuito.getBoxFull();
			for (Iterator iterator = boxFull.iterator(); iterator.hasNext();) {
				No no = (No) iterator.next();
				boxWrapperFull.add(new NoWrapper(no));
			}
		}
		return boxWrapperFull;
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

}
