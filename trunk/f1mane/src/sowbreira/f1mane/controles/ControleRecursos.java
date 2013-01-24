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
	protected CarregadorRecursos carregadorRecursos;
	protected Map circuitos = new HashMap();
	protected Map circuitosClima = new HashMap();
	protected Map temporadasTransp = new HashMap();
	protected Map<No, No> mapaNoProxCurva = new HashMap<No, No>();
	protected Map<Point, Integer> mapaLadosDerrapaCurva = new HashMap<Point, Integer>();
	protected Map<Integer, No> mapaIdsNos = new HashMap<Integer, No>();
	protected Map<No, Integer> mapaNosIds = new HashMap<No, Integer>();
	private final static int TRANPS = 250;
	private String seasson;
	private Set idsNoPista = new HashSet();
	private Set idsNoBox = new HashSet();
	private static Map bufferCarrosCima = new HashMap();
	private static Map bufferCarrosCimaSemAreofolio = new HashMap();
	private static Map bufferCarrosLado = new HashMap();
	private static Map bufferCarrosLadoSemAreofolio = new HashMap();

	public BufferedImage obterCarroLado(Piloto piloto) {
		Carro carro = piloto.getCarro();
		if (Carro.PERDEU_AEREOFOLIO.equals(piloto.getCarro().getDanificado())) {
			return obterCarroLadoSemAreofolio(piloto);

		}
		synchronized (bufferCarrosLado) {
			BufferedImage carroLado = (BufferedImage) bufferCarrosLado
					.get(carro.getNome());
			if (carroLado == null) {
				carroLado = ImageUtil.geraTransparencia(CarregadorRecursos
						.carregaImgSemCache("CarroLado.png"), 190);
				BufferedImage cor1 = CarregadorRecursos.gerarCoresCarros(carro
						.getCor1(), "CarroLadoC1.png");
				BufferedImage cor2 = CarregadorRecursos.gerarCoresCarros(carro
						.getCor2(), "CarroLadoC2.png");
				Graphics graphics = carroLado.getGraphics();
				graphics.drawImage(cor1, 0, 0, null);
				graphics.drawImage(cor2, 0, 0, null);
				graphics.dispose();
				if (carro.getImg() != null) {
					try {
						BufferedImage carroLadoPng = null;
						if (carro.getImg().endsWith(".png")) {
							carroLadoPng = CarregadorRecursos
									.carregaImagem(carro.getImg());
							bufferCarrosLado.put(carro.getNome(), carroLadoPng);
							carroLado = carroLadoPng;
						} else {
							carroLadoPng = CarregadorRecursos
									.carregaImgSemCache(carro.getImg());

							if (carroLadoPng != null) {
								carroLado = carroLadoPng;
								Integer transp = new Integer(
										(String) temporadasTransp
												.get(getTemporada()));
								bufferCarrosLado.put(carro.getNome(), ImageUtil
										.geraTransparencia(carroLado, transp));
							}
						}
					} catch (Exception e) {
						carro.setImg(null);
						bufferCarrosLado.put(carro.getNome(), ImageUtil
								.geraTransparencia(carroLado, Color.WHITE));
					}
				} else {
					bufferCarrosLado.put(carro.getNome(), ImageUtil
							.geraTransparencia(carroLado, Color.WHITE));
				}

			}
			return carroLado;
		}
	}

	private BufferedImage obterCarroLadoSemAreofolio(Piloto piloto) {
		Carro carro = piloto.getCarro();
		synchronized (bufferCarrosLadoSemAreofolio) {
			BufferedImage carroLado = (BufferedImage) bufferCarrosLadoSemAreofolio
					.get(carro.getNome());
			if (carroLado == null) {
				carroLado = CarregadorRecursos
						.carregaImgSemCache("CarroLado.png");
				BufferedImage cor1 = CarregadorRecursos.gerarCoresCarros(carro
						.getCor1(), "CarroLadoC1.png");
				BufferedImage cor2 = CarregadorRecursos.gerarCoresCarros(carro
						.getCor2(), "CarroLadoC3.png");
				Graphics graphics = carroLado.getGraphics();
				graphics.drawImage(cor1, 0, 0, null);
				graphics.drawImage(cor2, 0, 0, null);
				graphics.dispose();
				if (carro.getImg() != null) {
					try {
						BufferedImage carroLadoPng = null;
						if (carro.getImg().endsWith(".png")) {
							carroLadoPng = CarregadorRecursos
									.carregaImagem(carro.getImg());
							carroLado = carroLadoPng;
							bufferCarrosLadoSemAreofolio.put(carro.getNome(),
									carroLadoPng);
						} else {
							carroLadoPng = CarregadorRecursos
									.carregaImgSemCache(carro.getImg());
							if (carroLadoPng != null) {
								carroLado = carroLadoPng;
								Integer transp = new Integer(
										(String) temporadasTransp
												.get(getTemporada()));
								bufferCarrosLadoSemAreofolio.put(carro
										.getNome(), ImageUtil
										.geraTransparencia(carroLado, transp));
							}
						}
					} catch (Exception e) {
						carro.setImg(null);
						bufferCarrosLadoSemAreofolio.put(carro.getNome(),
								ImageUtil.geraTransparencia(carroLado,
										Color.WHITE));
					}
				} else {
					bufferCarrosLadoSemAreofolio.put(carro.getNome(), ImageUtil
							.geraTransparencia(carroLado, Color.WHITE));
				}
			}
			return carroLado;
		}
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
		synchronized (bufferCarrosCimaSemAreofolio) {
			BufferedImage carroCima = (BufferedImage) bufferCarrosCimaSemAreofolio
					.get(carro.getNome());
			if (carroCima == null) {
				carroCima = ImageUtil.geraTransparencia(CarregadorRecursos
						.carregaImgSemCache("CarroCima.png"), 190);
				BufferedImage cor1 = CarregadorRecursos.gerarCoresCarros(carro
						.getCor1(), "CarroCimaC1.png");
				BufferedImage cor2 = CarregadorRecursos.gerarCoresCarros(carro
						.getCor2(), "CarroCimaC3.png");
				Graphics graphics = carroCima.getGraphics();
				graphics.drawImage(cor2, 0, 0, null);
				graphics.drawImage(cor1, 0, 0, null);
				graphics.dispose();
				bufferCarrosCimaSemAreofolio.put(carro.getNome(), ImageUtil
						.geraTransparencia(carroCima, Color.WHITE));
			}
			return carroCima;
		}
	}

	public BufferedImage obterCarroCima(Piloto piloto) {
		Carro carro = piloto.getCarro();
		if (Carro.PERDEU_AEREOFOLIO.equals(piloto.getCarro().getDanificado())) {
			return obterCarroCimaSemAreofolio(piloto);

		}
		synchronized (bufferCarrosCima) {
			BufferedImage carroCima = (BufferedImage) bufferCarrosCima
					.get(carro.getNome());
			if (carroCima == null) {
				carroCima = ImageUtil.geraTransparencia(CarregadorRecursos
						.carregaImgSemCache("CarroCima.png"), 190);
				BufferedImage cor1 = CarregadorRecursos.gerarCoresCarros(carro
						.getCor1(), "CarroCimaC1.png");
				BufferedImage cor2 = CarregadorRecursos.gerarCoresCarros(carro
						.getCor2(), "CarroCimaC2.png");
				Graphics graphics = carroCima.getGraphics();
				graphics.drawImage(cor2, 0, 0, null);
				graphics.drawImage(cor1, 0, 0, null);
				graphics.dispose();
				bufferCarrosCima.put(carro.getNome(), ImageUtil
						.geraTransparencia(carroCima, Color.WHITE));
			}
			return carroCima;
		}
	}

	public void setTemporada(String seasson) {
		this.seasson = seasson;

	}

	public String getTemporada() {
		return seasson;
	}

	public Map getMapaIdsNos() {
		return mapaIdsNos;
	}

	public void setMapaIdsNos(Map mapaIdsNos) {
		this.mapaIdsNos = mapaIdsNos;
	}

	public Map getMapaNosIds() {
		return mapaNosIds;
	}

	public void setMapaNosIds(Map mapaNosIds) {
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
		} else {
			this.seasson = "t2009";
		}
		carregadorRecursos = new CarregadorRecursos(true);
		carros = carregadorRecursos.carregarListaCarros(seasson);
		pilotos = carregadorRecursos.carregarListaPilotos(seasson);
		carregadorRecursos.ligarPilotosCarros(pilotos, carros);
		carregarTemporadasTransp();
		carregarCircuitos();
	}

	public void carregaRecursos(String circuitoStr) throws Exception {
		carregaRecursos(circuitoStr, null, null);
	}

	public void carregaRecursos(String circuitoStr, List pilotos, List carros)
			throws Exception {
		if (pilotos != null) {
			this.pilotos = pilotos;
		}
		if (carros != null) {
			this.carros = carros;
		}
		ObjectInputStream ois = new ObjectInputStream(carregadorRecursos
				.getClass().getResourceAsStream(circuitoStr));

		circuito = (Circuito) ois.readObject();
		circuito.vetorizarPista();
		String nome = "";
		for (Iterator iterator = circuitos.keySet().iterator(); iterator
				.hasNext();) {
			String key = (String) iterator.next();
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
		for (Iterator iterator = mapaNoProxCurva.keySet().iterator(); iterator
				.hasNext();) {
			No no = (No) iterator.next();
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
		if (escapeList != null && !escapeList.isEmpty()) {
			for (Iterator iterator = escapeList.iterator(); iterator.hasNext();) {
				Point pointDerrapagem = (Point) iterator.next();
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
				Point p1 = GeoUtil.calculaPonto(calculaAngulo, Util
						.inte(Carro.ALTURA
								* circuito.getMultiplicadorLarguraPista()),
						new Point(Util.inte(rectangle.getCenterX()), Util
								.inte(rectangle.getCenterY())));
				Point p2 = GeoUtil.calculaPonto(calculaAngulo + 180, Util
						.inte(Carro.ALTURA
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
		String strVal = String.valueOf(Util.intervalo(50, 99))
				+ Util.intervalo(0, 9);
		System.out.println(strVal);
		// Logger.logar(((int) (0 + Math.random() * 9)));
	}

	protected void definirHabilidadePadraoPilotos(int value) {
		for (Iterator iter = pilotos.iterator(); iter.hasNext();) {
			Piloto piloto = (Piloto) iter.next();
			String strVal = String.valueOf(value) + Util.intervalo(0, 9);
			piloto.setHabilidade(Integer.parseInt(strVal));
		}
	}

	protected void definirPotenciaPadraoCarros(int value) {
		for (Iterator iter = pilotos.iterator(); iter.hasNext();) {
			Piloto piloto = (Piloto) iter.next();
			piloto.getCarro().setPotencia(value);
		}
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

			properties.load(CarregadorRecursos
					.recursoComoStream("properties/chuvaPistas.properties"));

			propName = properties.propertyNames();
			while (propName.hasMoreElements()) {
				final String name = (String) propName.nextElement();
				circuitosClima.put(name, properties.getProperty(name));
			}

		} catch (IOException e) {
			Logger.logarExept(e);
		}
	}

	public int porcentagemChuvaCircuito(String circuito) {
		if (circuitosClima == null) {
			return 0;
		}
		Object object = circuitos.get(circuito);
		if (object == null) {
			return 0;
		}
		String val = (String) circuitosClima.get(object);
		if (Util.isNullOrEmpty(val)) {
			return 0;
		}
		return Integer.valueOf(val);
	}

	public Map getCircuitosClima() {
		return circuitosClima;
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

}
