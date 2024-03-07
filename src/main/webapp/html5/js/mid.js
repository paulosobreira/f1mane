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
		var imgCarro = new Image();
		var temporadaCarro = dadosJogo.temporada;
		var temporadaCapacete = dadosJogo.temporada;
		var carroId = piloto.carro.id;
		var pilotoId = piloto.id;

		if (piloto.idCapaceteLivery != null && piloto.temporadaCapaceteLivery != null) {
			temporadaCapacete = piloto.temporadaCapaceteLivery;
			pilotoId = piloto.idCapaceteLivery;
		}

		if (piloto.idCarroLivery != null && piloto.temporadaCarroLivery != null) {
			temporadaCarro = piloto.temporadaCarroLivery;
			carroId = piloto.idCarroLivery;
		}

		imgCarro.src = '/flmane/rest/letsRace/carroCima/' + temporadaCarro + '/' + carroId;
		carrosImgMap.set(piloto.id, imgCarro);

		var imgSemAereofolio = new Image();
		imgSemAereofolio.src = "/flmane/rest/letsRace/carroCimaSemAreofolio/" + temporadaCarro + "/" + carroId;
		carrosImgSemAereofolioMap.set(piloto.id, imgSemAereofolio);

		var imgCarroLado = new Image();
		imgCarroLado.src = "/flmane/rest/letsRace/carroLado/" + temporadaCarro + "/" + carroId;
		carrosLadoImgMap.set(piloto.id, imgCarroLado);

		var imgCapacete = new Image();
		imgCapacete.src = "/flmane/rest/letsRace/capacete/" + temporadaCapacete + "/" + pilotoId;
		capaceteImgMap.set(piloto.id, imgCapacete);

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
	
	
	imgBg = new Image();
	imgBg.src = "/flmane/rest/letsRace/circuitoJpg/" + circuito.backGround

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
	safetycar.src = "/flmane/rest/letsRace/png/sfcima"
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
