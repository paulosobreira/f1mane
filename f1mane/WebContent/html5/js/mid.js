/**
 * Arquivo de midia
 */
var imgBg;
var imgPneuM, imgPneuD, imgPneuC;
var menosAsa, maisAsa, normalAsa;
var motor, capacete;
var safetycar;
var travadaRoda0,travadaRoda1,travadaRoda2;
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
	imgPneuM = new Image();
	imgPneuM.src = "img/pneuMole.png"
	imgPneuD = new Image();
	imgPneuD.src = "img/pneuDuro.png"
	imgPneuC = new Image();
	imgPneuC.src = "img/pneuChuva.png"
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
	travadaRoda0.src = "/f1mane/rest/letsRace/png/travadaRoda0/50"
	travadaRoda1 = new Image();
	travadaRoda1.src = "/f1mane/rest/letsRace/png/travadaRoda1/50"
	travadaRoda2 = new Image();
	travadaRoda2.src = "/f1mane/rest/letsRace/png/travadaRoda2/50"

	for (var i = 0; i < circuito.objetosNoTransparencia.length; i++) {
		var img = new Image();
		img.src = "/f1mane/rest/letsRace/objetoPista/" + dadosJogo.arquivoCircuito + "/" + i;
		objImgPistaMap.set(i, img);
	}

}
