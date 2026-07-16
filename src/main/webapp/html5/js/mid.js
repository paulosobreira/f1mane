/**
 * Arquivo de midia
 */
var imgBg;
var imgFarois,imgFarois1,imgFarois2,imgFarois3,imgFarois4,imgFaroisApagados;
var imgPneuM, imgPneuD, imgPneuC;
var menosAsa, maisAsa, normalAsa;
var motor, capacete;
var safetycar;
var travadaRoda0, travadaRoda1, travadaRoda2;
var girdLargadaMarca;
var pow;
var bandeirada;
var carroCimaFreiosD1, carroCimaFreiosD2, carroCimaFreiosD3, carroCimaFreiosD4, carroCimaFreiosD5;
var carroCimaFreiosE1, carroCimaFreiosE2, carroCimaFreiosE3, carroCimaFreiosE4, carroCimaFreiosE5;
var carregouMidia = false;
var contCarregouMidia = 0;
var imgBgSolicitado = false;
var carrosLadoImgMap;
var carrosImgMap;
var carrosImgSemAereofolioMap;
var capaceteImgMap;
var jogadorImgMap;
var objImgPistaMap = new Map();
var mapaRotacionar = new Map();
var mapaRastroFaisca = new Map();
var mapaTravadaRodaFumaca = new Map();
var eixoCarro = 30;
var preCarrega = true;
var MID_MAX_TENTATIVAS_IMG = 5;
var MID_DELAY_RETRY_IMG_MS = 1000;
var MID_INTERVALO_FILA_CARROS_MS = 5000;
var mid_filaCarrosNaoCarregados = [];
var mid_filaCarrosSet = new Set();

/**
 * Carrega uma imagem via URL e, em caso de falha (rede instável, timeout,
 * erro transitório no servidor), tenta novamente algumas vezes com um
 * pequeno atraso antes de desistir. Sem isso, uma falha de rede pontual
 * deixava o carro (visão de cima) permanentemente quebrado na tela, já
 * que <img>/Image não tenta novamente sozinho.
 *
 * aoFinalizar(sucesso), se informado, é chamado uma única vez: com true
 * quando a imagem carrega (onload) e com false quando as tentativas se
 * esgotam sem sucesso. Usado por mid_carregaImagensCarro para saber quando
 * enfileirar o carro na fila de retry ou invalidar o cache de rotação.
 */
function mid_carregaImagemComRetry(img, url, tentativas, aoFinalizar) {
    tentativas = tentativas || 0;
    img.onload = function () {
        if (aoFinalizar) {
            aoFinalizar(true);
        }
    };
    img.onerror = function () {
        if (tentativas < MID_MAX_TENTATIVAS_IMG) {
            setTimeout(function () {
                mid_carregaImagemComRetry(img, url, tentativas + 1, aoFinalizar);
            }, MID_DELAY_RETRY_IMG_MS * (tentativas + 1));
        } else if (aoFinalizar) {
            aoFinalizar(false);
        }
    };
    img.src = url + (url.indexOf('?') >= 0 ? '&' : '?') + '_retry=' + tentativas;
}

/**
 * Remove do cache de sprites rotacionados (mapaRotacionar, chaveado por
 * "carro.id-anguloGraus") todas as entradas do carro informado. Necessário
 * porque vdp_rotacionar desenha o <img> num canvas na hora em que é chamado:
 * se a imagem ainda não tinha carregado nesse instante (ex.: o preCarrega de
 * mid_caregaMidia roda 5s após o início do carregamento), o canvas cacheado
 * fica quebrado para sempre, mesmo depois da imagem terminar de carregar.
 */
function mid_invalidaRotacaoCarro(carroId) {
    var prefixo = carroId + "-";
    mapaRotacionar.forEach(function (valor, chave) {
        if (chave.indexOf(prefixo) === 0) {
            mapaRotacionar.delete(chave);
        }
    });
}

function mid_enfileiraCarroNaoCarregado(pilotoId) {
    if (mid_filaCarrosSet.has(pilotoId)) {
        return;
    }
    mid_filaCarrosSet.add(pilotoId);
    mid_filaCarrosNaoCarregados.push(pilotoId);
}

/**
 * Carrega (ou recarrega) as 4 imagens de um carro/piloto como uma unidade só.
 * Se qualquer uma falhar após esgotar as tentativas de
 * mid_carregaImagemComRetry, o piloto inteiro volta para o fim da fila de
 * carros não carregados, processada em mid_processaFilaCarrosNaoCarregados.
 */
function mid_carregaImagensCarro(piloto) {
    var temporadaCarro = dadosJogo.temporada;
    var temporadaCapacete = dadosJogo.temporada;
    var carroIdBase = piloto.carro.id;
    var carroId = carroIdBase;
    var pilotoIdCapacete = piloto.id;

    if (piloto.idCapaceteLivery != null && piloto.temporadaCapaceteLivery != null) {
        temporadaCapacete = piloto.temporadaCapaceteLivery;
        pilotoIdCapacete = piloto.idCapaceteLivery;
    }

    if (piloto.idCarroLivery != null && piloto.temporadaCarroLivery != null) {
        temporadaCarro = piloto.temporadaCarroLivery;
        carroId = piloto.idCarroLivery;
    }

    var pendentes = 4;
    var algumaFalhou = false;

    function aoFinalizarImagem(sucesso) {
        pendentes--;
        if (!sucesso) {
            algumaFalhou = true;
        }
        if (pendentes == 0) {
            if (algumaFalhou) {
                mid_enfileiraCarroNaoCarregado(piloto.id);
            } else {
                mid_invalidaRotacaoCarro(carroIdBase);
            }
        }
    }

    var imgCarro = carrosImgMap.get(piloto.id) || new Image();
    mid_carregaImagemComRetry(imgCarro,
        '/flmane/rest/letsRace/carroCima/' + temporadaCarro + '/' + carroId,
        0, aoFinalizarImagem);
    carrosImgMap.set(piloto.id, imgCarro);

    var imgSemAereofolio = carrosImgSemAereofolioMap.get(piloto.id) || new Image();
    mid_carregaImagemComRetry(imgSemAereofolio,
        "/flmane/rest/letsRace/carroCimaSemAreofolio/" + temporadaCarro + "/" + carroId,
        0, aoFinalizarImagem);
    carrosImgSemAereofolioMap.set(piloto.id, imgSemAereofolio);

    var imgCarroLado = carrosLadoImgMap.get(piloto.id) || new Image();
    mid_carregaImagemComRetry(imgCarroLado,
        "/flmane/rest/letsRace/carroLado/" + temporadaCarro + "/" + carroId,
        0, aoFinalizarImagem);
    carrosLadoImgMap.set(piloto.id, imgCarroLado);

    var imgCapacete = capaceteImgMap.get(piloto.id) || new Image();
    mid_carregaImagemComRetry(imgCapacete,
        "/flmane/rest/letsRace/capacete/" + temporadaCapacete + "/" + pilotoIdCapacete,
        0, aoFinalizarImagem);
    capaceteImgMap.set(piloto.id, imgCapacete);
}

/**
 * Consumidor da fila: a cada MID_INTERVALO_FILA_CARROS_MS, tira o primeiro
 * carro da fila e tenta recarregar. Sucesso: fica fora da fila. Falha:
 * mid_carregaImagensCarro já o recoloca no fim, via aoFinalizarImagem.
 */
function mid_processaFilaCarrosNaoCarregados() {
    if (mid_filaCarrosNaoCarregados.length == 0 || pilotosMap == null) {
        return;
    }
    var pilotoId = mid_filaCarrosNaoCarregados.shift();
    mid_filaCarrosSet.delete(pilotoId);
    var piloto = pilotosMap.get(pilotoId);
    if (piloto == null) {
        return;
    }
    mid_carregaImagensCarro(piloto);
}

/**
 * Dispara o carregamento do jpg do circuito (gerado sob demanda no
 * servidor). Chamada em todos os estados a partir de cpu_main, inclusive a
 * sala de espera (07) — a tela de espera (pista + contador "Inicia em" +
 * msgCarregando) depende do cvBg existir pra ser desenhada. Normalmente é
 * um cache hit: o preload real acontece no clique de "Jogar"
 * (jogar_preCarregaBackGround) e o endpoint circuitoJpg manda Cache-Control.
 */
function mid_carregaBackGroundCorrida() {
	if (imgBgSolicitado || imgBg == null || circuito == null) {
		return;
	}
	imgBgSolicitado = true;
	imgBg.src = "/flmane/rest/letsRace/circuitoJpg/" + circuito.backGround;
}

function mid_atualizaJogadores() {
	for (var i = 0; i < dadosJogo.pilotos.length; i++) {
		var piloto = dadosJogo.pilotos[i];
		jogadorImgMap.set(piloto.id, piloto.imgJogador);
	}
}

function mid_caregaMidia() {

	carrosImgMap = new Map();
	carrosImgSemAereofolioMap = new Map();
	carrosLadoImgMap = new Map();
	jogadorImgMap = new Map();
	capaceteImgMap = new Map();
	for (var i = 0; i < dadosJogo.pilotos.length; i++) {
		var piloto = dadosJogo.pilotos[i];
		mid_carregaImagensCarro(piloto);
		jogadorImgMap.set(piloto.id, piloto.imgJogador);
	}

	if(preCarrega){
		setTimeout(function fnRotacionarCarro() {
			for (var ip = 0; ip < dadosJogo.pilotos.length; ip++) {
				var piloto = dadosJogo.pilotos[ip];
				for (var i = 0; i < circuito.pistaFull.length; i++) {
					var frenteCar = safeArray(circuito.pistaFull, i + eixoCarro);
					var atrasCar = safeArray(circuito.pistaFull, i - eixoCarro);
					var angulo = gu_calculaAngulo(frenteCar, atrasCar, 180);
					var anguloGraus = Math.round(Math.degrees(angulo / 6));
					var chave = piloto.carro.id + "-" + anguloGraus;
					var rotacionarCarro = mapaRotacionar.get(chave);
					if (rotacionarCarro == null) {
						var imgCarro = carrosImgMap.get(piloto.id);
						rotacionarCarro = vdp_rotacionar(imgCarro, angulo);
						mapaRotacionar.set(chave, rotacionarCarro);
					}
					var intervaloVar = intervaloInt(0, fxArray.length - 1);
					chave = intervaloVar + "-" + anguloGraus;
					var faisca = mapaRastroFaisca.get(chave);
					if(faisca==null){
						var fx = fxArray[intervaloVar];
						faisca = vdp_rotacionar(fx, angulo);
						mapaRastroFaisca.set(chave, faisca);
					}
					
					var sw = Math.round(intervalo(1, 5));
					var lado = 'D';
					chave = lado + "-" + sw + "-" + anguloGraus;
					var	fumaca = mapaTravadaRodaFumaca.get(chave);
					if(fumaca==null){
						var fx = eval('carroCimaFreios' + lado + sw);
						var fumaca = vdp_rotacionar(fx, angulo);
						mapaTravadaRodaFumaca.set(chave, fumaca);
					}
					
					lado = 'E';
					chave = lado + "-" + sw + "-" + anguloGraus;
					fumaca = mapaTravadaRodaFumaca.get(chave);
					if(fumaca==null){
						var fx = eval('carroCimaFreios' + lado + sw);
						var fumaca = vdp_rotacionar(fx, angulo);
						mapaTravadaRodaFumaca.set(chave, fumaca);
					}
				}
			}
		}, 5000);
	}
	
	
	// imgBg fica sem src aqui de propósito: o jpg do circuito só é pedido
	// (e gerado no servidor) a partir do qualify/corrida, não ao entrar na
	// sala de espera (estado 07) — ver mid_carregaBackGroundCorrida(),
	// chamada pelo cpu_main quando o estado é qualify (10) ou corrida.
	imgBg = new Image();

	pow = new Image();
	pow.src = "/flmane/rest/letsRace/png/pow"
	imgFarois = new Image();
	imgFarois.src = "/flmane/rest/letsRace/png/farois"
	imgFarois1 = new Image();
	imgFarois1.src = "/flmane/rest/letsRace/png/farois1"
	imgFarois2 = new Image();
	imgFarois2.src = "/flmane/rest/letsRace/png/farois2"
	imgFarois3 = new Image();
	imgFarois3.src = "/flmane/rest/letsRace/png/farois3"
	imgFarois4 = new Image();
	imgFarois4.src = "/flmane/rest/letsRace/png/farois4"
	imgFaroisApagados = new Image();
	imgFaroisApagados.src = "/flmane/rest/letsRace/png/farois-apagados"
	imgPneuM = new Image();
	imgPneuM.src = "/flmane/rest/letsRace/png/pneuMoleMenor"
	imgPneuD = new Image();
	imgPneuD.src = "/flmane/rest/letsRace/png/pneuDuroMenor"
	imgPneuC = new Image();
	imgPneuC.src = "/flmane/rest/letsRace/png/pneuChuvaMenor"
	girdLargadaMarca = new Image();
	girdLargadaMarca.src = "/flmane/rest/letsRace/png/GridCarro/180"
	motor = new Image();
	motor.src = "/flmane/rest/letsRace/png/motor"
	capacete = new Image();
	capacete.src = "/flmane/rest/letsRace/png/capaceteMonster"
	menosAsa = new Image();
	menosAsa.src = "/flmane/rest/letsRace/png/menosAsa"
	maisAsa = new Image();
	maisAsa.src = "/flmane/rest/letsRace/png/maisAsa"
	normalAsa = new Image();
	normalAsa.src = "/flmane/rest/letsRace/png/normalAsa"
	safetycar = new Image();
	mid_carregaImagemComRetry(safetycar, "/flmane/rest/letsRace/png/sfcima");
	travadaRoda0 = new Image();
	travadaRoda0.src = "/flmane/rest/letsRace/png/travadaRoda0/50"
	travadaRoda1 = new Image();
	travadaRoda1.src = "/flmane/rest/letsRace/png/travadaRoda1/50"
	travadaRoda2 = new Image();
	travadaRoda2.src = "/flmane/rest/letsRace/png/travadaRoda2/50"

	carroCimaFreiosD1 = new Image();
	carroCimaFreiosD1.src = "/flmane/rest/letsRace/png/CarroCimaFreiosD1"
	carroCimaFreiosD2 = new Image();
	carroCimaFreiosD2.src = "/flmane/rest/letsRace/png/CarroCimaFreiosD2"
	carroCimaFreiosD3 = new Image();
	carroCimaFreiosD3.src = "/flmane/rest/letsRace/png/CarroCimaFreiosD3"
	carroCimaFreiosD4 = new Image();
	carroCimaFreiosD4.src = "/flmane/rest/letsRace/png/CarroCimaFreiosD4"
	carroCimaFreiosD5 = new Image();
	carroCimaFreiosD5.src = "/flmane/rest/letsRace/png/CarroCimaFreiosD5"

	carroCimaFreiosE1 = new Image();
	carroCimaFreiosE1.src = "/flmane/rest/letsRace/png/CarroCimaFreiosE1"
	carroCimaFreiosE2 = new Image();
	carroCimaFreiosE2.src = "/flmane/rest/letsRace/png/CarroCimaFreiosE2"
	carroCimaFreiosE3 = new Image();
	carroCimaFreiosE3.src = "/flmane/rest/letsRace/png/CarroCimaFreiosE3"
	carroCimaFreiosE4 = new Image();
	carroCimaFreiosE4.src = "/flmane/rest/letsRace/png/CarroCimaFreiosE4"
	carroCimaFreiosE5 = new Image();
	carroCimaFreiosE5.src = "/flmane/rest/letsRace/png/CarroCimaFreiosE5"

	bandeirada = new Image();
	bandeirada.src = "/flmane/rest/letsRace/png/flags"

	for (var i = 0; i < circuito.objetosNoTransparencia.length; i++) {
		var img = new Image();
		img.src = "/flmane/rest/letsRace/objetoPista/" + dadosJogo.arquivoCircuito + "/" + i;
		objImgPistaMap.set(i, img);
	}

}
