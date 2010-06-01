package sowbreira.f1mane.controles;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import sowbreira.f1mane.entidades.Carro;
import sowbreira.f1mane.entidades.Circuito;
import sowbreira.f1mane.entidades.No;
import sowbreira.f1mane.entidades.Piloto;
import sowbreira.f1mane.recursos.CarregadorRecursos;
import br.nnpe.ImageUtil;
import br.nnpe.Logger;

/**
 * @author Paulo Sobreira
 */
public abstract class ControleRecursos {
	protected Circuito circuito;
	protected List pilotos = new ArrayList();
	protected List nosDaPista = new ArrayList();
	protected List nosDoBox = new ArrayList();
	protected List carros;
	protected CarregadorRecursos carregadorRecursos;
	protected Map circuitos = new HashMap();
	protected Map mapaIdsNos = new HashMap();
	protected Map mapaNosIds = new HashMap();
	private String seasson;
	private static Map bufferCarrosCima = new HashMap();
	private static Map bufferCarrosCimaSemAreofolio = new HashMap();

	public BufferedImage obterCarroCimaSemAreofolio(Piloto piloto) {
		Carro carro = piloto.getCarro();
		BufferedImage carroCima = (BufferedImage) bufferCarrosCimaSemAreofolio
				.get(carro.getNome());
		if (carroCima == null) {
			carroCima = CarregadorRecursos.carregaImgSemCache("CarroCima.png");
			BufferedImage cor1 = CarregadorRecursos.gerarCoresCarros(carro
					.getCor1(), 1);
			BufferedImage cor2 = CarregadorRecursos.gerarCoresCarros(carro
					.getCor2(), 3);
			Graphics graphics = carroCima.getGraphics();
			graphics.drawImage(cor2, 0, 0, null);
			graphics.drawImage(cor1, 0, 0, null);
			graphics.dispose();
			bufferCarrosCimaSemAreofolio.put(carro.getNome(), ImageUtil
					.geraTransparencia(carroCima, Color.WHITE));
		}
		return carroCima;
	}

	public BufferedImage obterCarroCima(Piloto piloto) {
		Carro carro = piloto.getCarro();
		BufferedImage carroCima = (BufferedImage) bufferCarrosCima.get(carro
				.getNome());
		if (carroCima == null) {
			carroCima = CarregadorRecursos.carregaImgSemCache("CarroCima.png");
			BufferedImage cor1 = CarregadorRecursos.gerarCoresCarros(carro
					.getCor1(), 1);
			BufferedImage cor2 = CarregadorRecursos.gerarCoresCarros(carro
					.getCor2(), 2);
			Graphics graphics = carroCima.getGraphics();
			graphics.drawImage(cor2, 0, 0, null);
			graphics.drawImage(cor1, 0, 0, null);
			graphics.dispose();
			bufferCarrosCima.put(carro.getNome(), ImageUtil.geraTransparencia(
					carroCima, Color.WHITE));
		}
		return carroCima;
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
		}
		for (Iterator iter = nosDoBox.iterator(); iter.hasNext();) {
			No noDoBox = (No) iter.next();
			Integer boxId = new Integer(contId++);
			mapaIdsNos.put(boxId, noDoBox);
			mapaNosIds.put(noDoBox, boxId);
		}
	}

	public static void main(String[] args) {
		Logger.logar(((int) (0 + Math.random() * 9)));
	}

	protected void definirHabilidadePadraoPilotos(int value) {
		for (Iterator iter = pilotos.iterator(); iter.hasNext();) {
			Piloto piloto = (Piloto) iter.next();
			String strVal = String.valueOf(value)
					+ (int) (0 + Math.random() * 9);
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
				circuitos.put(name, properties.getProperty(name));

			}
		} catch (IOException e) {
			Logger.logarExept(e);
		}
	}
}
