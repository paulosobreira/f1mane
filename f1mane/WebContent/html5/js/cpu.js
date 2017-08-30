var criarJogo;
var circuito;
var dadosJogo;
var dadosParciais;
var ativo = true;
var jogoInciado = false;
var delay = 1000;
var pilotosMap = new Map();

// update canvas with some information and animation
// var fps = new FpsCtrl(20, function(e) {
// vdp_desenha();
// })

// start the loop
// fps.start();

maneCanvas.addEventListener('click', function(event) {
	ativo = !ativo;
});

function cpu_main(){
	if (criarJogo == null) {
		console.log('cpu_main rest_criarJogo()');
		rest_criarJogo();
	}
	if (criarJogo != null && dadosJogo == null) {
		console.log('cpu_main rest_dadosJogo()');
		rest_dadosJogo();
	}
	if (criarJogo != null && dadosJogo != null && circuito == null) {
		for (i = 0; i < dadosJogo.pilotosList.length; i++) {
			pilotosMap.set(dadosJogo.pilotosList[i].id, dadosJogo.pilotosList[i]);
		}
		console.log('cpu_main rest_ciruito()');
		rest_ciruito();
	}
	if (criarJogo != null && dadosJogo != null && circuito != null && !jogoInciado) {
		console.log('cpu_main rest_iniciarJogo()');
		jogoInciado = rest_iniciarJogo();
	}
	if(criarJogo != null && circuito != null && imgBg.src == ""){
		console.log('cpu_main vdp_carregaBackGround()');
		vdp_carregaBackGround();
	}
	if(criarJogo != null && circuito != null && dadosJogo != null && jogoInciado && ativo){
		delay = 100;
		rest_dadosParciais();
		vdp_desenha();
	}else{
		console.log('cpu_main inativo');
		delay = 1000;
	}
}

var main = setInterval(cpu_main, delay);