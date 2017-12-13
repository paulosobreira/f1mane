/**
 * Arquivo de midia
 */
var imgBg;
var imgFarois;
var imgPneuM, imgPneuD, imgPneuC;
var menosAsa, maisAsa, normalAsa;
var motor, capacete;
var safetycar;
var travadaRoda0, travadaRoda1, travadaRoda2;
var girdLargadaMarca;
var carroCimaFreiosD1, carroCimaFreiosD2, carroCimaFreiosD3, carroCimaFreiosD4, carroCimaFreiosD5;
var carroCimaFreiosE1, carroCimaFreiosE2, carroCimaFreiosE3, carroCimaFreiosE4, carroCimaFreiosE5;
var carregouMidia = false;
var contCarregouMidia = 0;
var carrosLadoImgMap;
var carrosImgMap;
var carrosImgSemAereofolioMap;
var capaceteImgMap;
var objImgPistaMap = new Map();

function mid_caregaMidia() {

	carrosImgMap = new Map();
	carrosImgSemAereofolioMap = new Map();
	carrosLadoImgMap = new Map();
	capaceteImgMap = new Map();
	for (var i = 0; i < dadosJogo.pilotos.length; i++) {
		var pilotos = dadosJogo.pilotos[i];
		var imgCarro = new Image();
		imgCarro.src = "/f1mane/rest/letsRace/carroCima?nomeJogo=" + dadosJogo.nomeJogo + "&idPiloto=" + pilotos.id;
		carrosImgMap.set(pilotos.id, imgCarro);

		var imgSemAereofolio = new Image();
		imgSemAereofolio.src = "/f1mane/rest/letsRace/carroCimaSemAreofolio?nomeJogo=" + dadosJogo.nomeJogo + "&idPiloto=" + pilotos.id;
		carrosImgSemAereofolioMap.set(pilotos.id, imgSemAereofolio);

		var imgCarroLado = new Image();
		imgCarroLado.src = "/f1mane/rest/letsRace/carroLado?id=" + pilotos.id + "&temporada=" + dadosJogo.temporada;
		carrosLadoImgMap.set(pilotos.id, imgCarroLado);

		var imgCapacete = new Image();
		imgCapacete.src = "/f1mane/rest/letsRace/capacete?id=" + pilotos.id + "&temporada=" + dadosJogo.temporada
		capaceteImgMap.set(pilotos.id, imgCapacete);
	}

	imgBg = new Image();
	imgBg.src = "../sowbreira/f1mane/recursos/" + circuito.backGround;
	imgFarois = new Image();
	imgFarois.src = "img/farois.png"
	imgPneuM = new Image();
	imgPneuM.src = "img/pneuMole.png"
	imgPneuD = new Image();
	imgPneuD.src = "img/pneuDuro.png"
	imgPneuC = new Image();
	imgPneuC.src = "img/pneuChuva.png"
    girdLargadaMarca = new Image();		
	girdLargadaMarca.src = "/f1mane/rest/letsRace/png/GridCarro";
	motor = new Image();
	motor.src = "img/motor.png"
	capacete = new Image();
	capacete.src = "img/capacete.png"
	menosAsa = new Image();
	menosAsa.src = "/f1mane/rest/letsRace/png/menosAsa";
	maisAsa = new Image();
	maisAsa.src = "/f1mane/rest/letsRace/png/maisAsa";
	normalAsa = new Image();
	normalAsa.src = "/f1mane/rest/letsRace/png/normalAsa"
	safetycar = new Image();
	safetycar.src = "/f1mane/rest/letsRace/png/sfcima"
	travadaRoda0 = new Image();
	travadaRoda0.src = "/f1mane/rest/letsRace/png/travadaRoda0/40"
	travadaRoda1 = new Image();
	travadaRoda1.src = "/f1mane/rest/letsRace/png/travadaRoda1/40"
	travadaRoda2 = new Image();
	travadaRoda2.src = "/f1mane/rest/letsRace/png/travadaRoda2/40"

	carroCimaFreiosD1 = new Image();
	carroCimaFreiosD1.src = "/f1mane/rest/letsRace/png/CarroCimaFreiosD1"
	carroCimaFreiosD2 = new Image();
	carroCimaFreiosD2.src = "/f1mane/rest/letsRace/png/CarroCimaFreiosD2"
	carroCimaFreiosD3 = new Image();
	carroCimaFreiosD3.src = "/f1mane/rest/letsRace/png/CarroCimaFreiosD3"
	carroCimaFreiosD4 = new Image();
	carroCimaFreiosD4.src = "/f1mane/rest/letsRace/png/CarroCimaFreiosD4"
	carroCimaFreiosD5 = new Image();
	carroCimaFreiosD5.src = "/f1mane/rest/letsRace/png/CarroCimaFreiosD5"

	carroCimaFreiosE1 = new Image();
	carroCimaFreiosE1.src = "/f1mane/rest/letsRace/png/CarroCimaFreiosE1"
	carroCimaFreiosE2 = new Image();
	carroCimaFreiosE2.src = "/f1mane/rest/letsRace/png/CarroCimaFreiosE2"
	carroCimaFreiosE3 = new Image();
	carroCimaFreiosE3.src = "/f1mane/rest/letsRace/png/CarroCimaFreiosE3"
	carroCimaFreiosE4 = new Image();
	carroCimaFreiosE4.src = "/f1mane/rest/letsRace/png/CarroCimaFreiosE4"
	carroCimaFreiosE5 = new Image();
	carroCimaFreiosE5.src = "/f1mane/rest/letsRace/png/CarroCimaFreiosE5"

	for (var i = 0; i < circuito.objetosNoTransparencia.length; i++) {
		var img = new Image();
		img.src = "/f1mane/rest/letsRace/objetoPista/" + dadosJogo.arquivoCircuito + "/" + i;
		objImgPistaMap.set(i, img);
	}

}
