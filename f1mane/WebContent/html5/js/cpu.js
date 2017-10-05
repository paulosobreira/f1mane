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
var alternador = true;
var alternadorValor = 0;
var carrosImgMap;
var carrosLadoImgMap;


// update canvas with some information and animation
// var fps = new FpsCtrl(20, function(e) {
// vdp_desenha();
// })

// start the loop
// fps.start();

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
		for (i = 0; i < dadosJogo.pilotos.length; i++) {
			pilotosMap.set(dadosJogo.pilotos[i].id,
					dadosJogo.pilotos[i]);
		}
		carrosImgMap = new Map();
		carrosLadoImgMap = new Map();
		for (i = 0; i < dadosJogo.pilotos.length; i++) {
			var pilotos = dadosJogo.pilotos[i];
			var imgCarro = new Image();
			imgCarro.src = "/f1mane/rest/letsRace/carroCima?nomeJogo="
					+ dadosJogo.nomeJogo + "&idPiloto=" + pilotos.id;
			carrosImgMap.set(pilotos.id, imgCarro);
			var imgCarroLado = new Image();
			imgCarroLado.src = "/f1mane/rest/letsRace/carroLado?id="
					+ pilotos.id + "&temporada=" + dadosJogo.temporada;
			carrosLadoImgMap.set(pilotos.id, imgCarroLado);
		}
		console.log('cpu_main rest_ciruito()');
		rest_ciruito();
	}
	if (dadosJogo != null && circuito != null && imgBg.src == "") {
		console.log('cpu_main vdp_carregaBackGround()');
		vdp_carregaBackGround();
	}
	if (dadosJogo != null && circuito != null && ativo && imgBg.complete) {
		delay = 500;
		rest_dadosParciais();
		vdp_desenha();
		ctl_desenha();
		cpu_altenador();
	} else {
		console.log('cpu_main dadosJogo:' + dadosJogo + ' circuito:' + circuito
				+ ' ativo:' + ativo + ' imgBg.complete:' + imgBg.complete);
		delay = 100;
	}
	if(dadosJogo){
		console.log(dadosJogo.estado);
	}
}

function cpu_altenador(){
	if(alternador){
		alternadorValor++;
		if(alternadorValor>20){
			alternador=false;	
		}
	}else{
		alternadorValor--;
		if(alternadorValor<-20){
			alternador=true;	
		}
	}
} 

var main = setInterval(cpu_main, delay);