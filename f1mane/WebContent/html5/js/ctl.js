/**
 * Controle comandos no jogo
 */
var controles = [];
var largura, altura;
maneCanvas.addEventListener('click', function(event) {
	var x = event.pageX;
	var y = event.pageY;

	// Collision detection between clicked offset and element.
	controles.forEach(function(controle) {
		if (y > controle.y && y < controle.y + controle.height
				&& x > controle.x && x < controle.x + controle.width) {
			if (controle.tipo == 'controleMotor') {
				rest_potenciaMotor(controle.valor);
			}
			if (controle.tipo == 'controlePiloto') {
				rest_agressividadePiloto(controle.valor);
			}
		}
	});

	var piloto = dadosParciais.posisPack.posis[posicaoCentraliza];

	var no = mapaIdNos.get(piloto.idNo);
	var ponto0;
	var ponto1;
	var ponto2;
	if (no.box) {
		return;
	}
	ponto0 = circuito.pistaFull[piloto.idNo];
	ponto1 = circuito.pista1Full[piloto.idNo];
	ponto2 = circuito.pista2Full[piloto.idNo];
	var pontoCarro0 = {
		x : ponto0.x - ptBg.x,
		y : ponto0.y - ptBg.y
	};
	var pontoCarro1 = {
		x : ponto1.x - ptBg.x,
		y : ponto1.y - ptBg.y
	};

	var pontoCarro2 = {
		x : ponto2.x - ptBg.x,
		y : ponto2.y - ptBg.y
	};
	var pontoClick = {
		x : event.pageX,
		y : event.pageY
	};
	var menor = 100000;
	var mudar = 0;
	var dp0 = gu_distancia(pontoCarro0, pontoClick);
	var dp1 = gu_distancia(pontoCarro1, pontoClick);
	var dp2 = gu_distancia(pontoCarro2, pontoClick);
	if (dp0 < menor && piloto.tracado != 0) {
		menor = dp0;
	}
	if (dp1 < menor && piloto.tracado == 0) {
		menor = dp1;
		mudar = 1;
	}
	if (dp2 < menor && piloto.tracado == 0) {
		menor = dp2;
		mudar = 2;
	}
	if ((mudar == 2 && piloto.tracado == 1)
			|| (mudar == 1 && piloto.tracado == 2)) {
		mudar = 0;
	}
	console.log('rest_tracadoPiloto(mudar); ' + mudar)
	if (menor < 100000) {
		rest_tracadoPiloto(mudar);
	}

}, false);

function ctl_desenhaControles() {
	if (dadosParciais == null) {
		return;
	}
	var evalX = false;
	var evalY = false;
	if (largura != maneCanvas.width) {
		evalX = true;
	}
	if (altura != maneCanvas.height) {
		evalY = true;
	}
	largura = maneCanvas.width;
	altura = maneCanvas.height;
	// Render elements.
	controles
			.forEach(function(controle) {
				if (evalY && controle.evalY) {
					controle.y = eval(controle.evalY);
				}
				if (evalX && controle.evalX) {
					controle.x = eval(controle.evalX);
				}
				maneContext.beginPath();

				if (controle.tipo == 'controleMotor') {
					if (dadosParciais.giro == 1 && controle.valor == 'GIRO_MIN') {
						maneContext.strokeStyle = '#00FF00';
					} else if (dadosParciais.giro == 5
							&& controle.valor == 'GIRO_NOR') {
						maneContext.strokeStyle = '#FFFF00';
					} else if (dadosParciais.giro == 9
							&& controle.valor == 'GIRO_MAX') {
						maneContext.strokeStyle = '#FF0000';
					} else {
						maneContext.strokeStyle = controle.cor;
					}
				}
				if (controle.tipo == 'controlePiloto') {
					if (dadosParciais.modoPilotar == 'LENTO'
							&& controle.valor == 'LENTO') {
						maneContext.strokeStyle = '#00FF00';
					} else if (dadosParciais.modoPilotar == 'NORMAL'
							&& controle.valor == 'NORMAL') {
						maneContext.strokeStyle = '#FFFF00';
					} else if (dadosParciais.modoPilotar == 'AGRESSIVO'
							&& controle.valor == 'AGRESSIVO') {
						maneContext.strokeStyle = '#FF0000';
					} else {
						maneContext.strokeStyle = controle.cor;
					}
				}

				maneContext.rect(controle.x, controle.y, controle.width,
						controle.height);
				maneContext.font = '30px sans-serif';
				maneContext.fillText(controle.exibir, controle.x
						+ (controle.width / 2) - 10, controle.y
						+ (controle.height / 2) + 10);
				maneContext.closePath();
				maneContext.stroke();
			});
}

// Add element.
controles.push({
	cor : '#BABACA',
	valor : 'GIRO_MIN',
	exibir : '1',
	tipo : 'controleMotor',
	width : 40,
	height : 40,
	evalY : 'window.innerHeight - (window.innerHeight/2-20);',
	y : window.innerHeight - (window.innerHeight / 2 - 20),
	x : 10
});
controles.push({
	cor : '#BABACA',
	valor : 'GIRO_NOR',
	exibir : '2',
	tipo : 'controleMotor',
	width : 40,
	height : 40,
	evalY : 'window.innerHeight - (window.innerHeight/2-20);',
	y : window.innerHeight - (window.innerHeight / 2 - 20),
	x : 60
});
controles.push({
	cor : '#BABACA',
	valor : 'GIRO_MAX',
	exibir : '3',
	tipo : 'controleMotor',
	width : 40,
	height : 40,
	evalY : 'window.innerHeight - (window.innerHeight/2-20);',
	y : window.innerHeight - (window.innerHeight / 2 - 20),
	x : 110
});

controles.push({
	cor : '#BABACA',
	valor : 'AGRESSIVO',
	exibir : '3',
	tipo : 'controlePiloto',
	width : 40,
	height : 40,
	evalY : 'window.innerHeight - (window.innerHeight/2-20);',
	y : window.innerHeight - (window.innerHeight / 2 - 20),
	evalX : 'window.innerWidth - 150;',
	x : 0
});
controles.push({
	cor : '#BABACA',
	valor : 'NORMAL',
	exibir : '2',
	tipo : 'controlePiloto',
	width : 40,
	height : 40,
	evalY : 'window.innerHeight - (window.innerHeight/2-20);',
	y : window.innerHeight - (window.innerHeight / 2 - 20),
	evalX : 'window.innerWidth - 100;',
	x : 0
});
controles.push({
	cor : '#BABACA',
	valor : 'LENTO',
	exibir : '1',
	tipo : 'controlePiloto',
	width : 40,
	height : 40,
	evalY : 'window.innerHeight - (window.innerHeight/2-20);',
	y : window.innerHeight - (window.innerHeight / 2 - 20),
	evalX : 'window.innerWidth - 50;',
	x : 0
});