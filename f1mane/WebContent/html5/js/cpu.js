/**
 * Controle central de jogo
 */
var circuito;
var dadosJogo;
var dadosParciais;
var ativo = true;
var delay = 100;
var pilotosMap = new Map();
var token;
var idPilotoSelecionado;
var nomeJogo;

// update canvas with some information and animation
// var fps = new FpsCtrl(20, function(e) {
// vdp_desenha();
// })

// start the loop
// fps.start();

maneCanvas.addEventListener('click', function(event) {
	// ativo = !ativo;
});

function cpu_main() {
	if (nomeJogo == null) {
		nomeJogo = localStorage.getItem("nomeJogo");
		idPilotoSelecionado = localStorage.getItem("idPilotoSelecionado");
		token = localStorage.getItem("token");
		if (nomeJogo == null) {
			console.log('nomeJogo==null');
			return;
		}
	}
	if (dadosJogo == null) {
		rest_dadosJogo(nomeJogo);
	}
	if (dadosJogo != null && circuito == null) {
		for (i = 0; i < dadosJogo.pilotosList.length; i++) {
			pilotosMap.set(dadosJogo.pilotosList[i].id,
					dadosJogo.pilotosList[i]);
		}
		carrosImgMap = new Map();
		for (i = 0; i < dadosJogo.pilotosList.length; i++) {
			var pilotos = dadosJogo.pilotosList[i];
			var imgCarro = new Image();
			imgCarro.src = "/f1mane/rest/letsRace/carroCima?nomeJogo="
					+ dadosJogo.nomeJogo + "&idPiloto=" + pilotos.id;
			carrosImgMap.set(pilotos.id, imgCarro);
		}
		console.log('cpu_main rest_ciruito()');
		rest_ciruito();
	}
	if (dadosJogo != null && circuito != null && imgBg.src == "") {
		console.log('cpu_main vdp_carregaBackGround()');
		vdp_carregaBackGround();
	}
	if (dadosJogo != null && circuito != null && ativo) {
		delay = 100;
		rest_dadosParciais();
		vdp_desenha();
	} else {
		console.log('cpu_main inativo');
		delay = 100;
	}
}

var main = setInterval(cpu_main, delay);