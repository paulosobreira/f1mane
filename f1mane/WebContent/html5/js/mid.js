/**
 * Arquivo de midia
 */
var imgBg;
var imgFarois, imgFaroisApagados;
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

function mid_caregaMidia() {

	carrosImgMap = new Map();
	carrosImgSemAereofolioMap = new Map();
	carrosLadoImgMap = new Map();
	jogadorImgMap = new Map();
	capaceteImgMap = new Map();
	for (var i = 0; i < dadosJogo.pilotos.length; i++) {
		var piloto = dadosJogo.pilotos[i];
		var imgCarro = new Image();
		imgCarro.src = "/f1mane/rest/letsRace/carroCima?nomeJogo=" + dadosJogo.nomeJogo + "&idPiloto=" + piloto.id;
		carrosImgMap.set(piloto.id, imgCarro);

		var imgSemAereofolio = new Image();
		imgSemAereofolio.src = "/f1mane/rest/letsRace/carroCimaSemAreofolio?nomeJogo=" + dadosJogo.nomeJogo + "&idPiloto=" + piloto.id;
		carrosImgSemAereofolioMap.set(piloto.id, imgSemAereofolio);

		var imgCarroLado = new Image();
		imgCarroLado.src = "/f1mane/rest/letsRace/carroLado?id=" + piloto.id + "&temporada=" + dadosJogo.temporada;
		carrosLadoImgMap.set(piloto.id, imgCarroLado);

		var imgCapacete = new Image();
		imgCapacete.src = "/f1mane/rest/letsRace/capacete?id=" + piloto.id + "&temporada=" + dadosJogo.temporada
		capaceteImgMap.set(piloto.id, imgCapacete);
		
		if(piloto.imgJogador!=null){
			var jogadorImg = new Image();
			jogadorImg.src = piloto.imgJogador;
			jogadorImg.setAttribute('height','44px');
			jogadorImg.setAttribute('border-radius','50%');
			jogadorImgMap.set(piloto.id, jogadorImg);			
		}
		
	}

	imgBg = new Image();
	// imgBg.src = {urlBg} + circuito.backGround;
	imgBg.src = "http://sowbreira-26fe1.firebaseapp.com/f1mane/sowbreira/f1mane/recursos/" + circuito.backGround;
	// imgBg.src =
	// "http://games-sobreira.193b.starter-ca-central-1.openshiftapps.com/f1manepistas/pistas/"
	// + circuito.backGround;

	pow = new Image();
	pow.src = "img/pow.png"
	imgFarois = new Image();
	imgFarois.src = "img/farois.png"
	imgFaroisApagados = new Image();
	imgFaroisApagados.src = "img/farois-apagados.png"
	imgPneuM = new Image();
	imgPneuM.src = "img/pneuMole.png"
	imgPneuD = new Image();
	imgPneuD.src = "img/pneuDuro.png"
	imgPneuC = new Image();
	imgPneuC.src = "img/pneuChuva.png"
	girdLargadaMarca = new Image();
	girdLargadaMarca.src = "/f1mane/rest/letsRace/png/GridCarro/180";
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
		
	bandeirada= new Image();
	bandeirada.src = "/f1mane/rest/letsRace/png/flags"
	
	for (var i = 0; i < circuito.objetosNoTransparencia.length; i++) {
		var img = new Image();
		img.src = "/f1mane/rest/letsRace/objetoPista/" + dadosJogo.arquivoCircuito + "/" + i;
		objImgPistaMap.set(i, img);
	}

}
