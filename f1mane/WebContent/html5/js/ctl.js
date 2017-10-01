/**
 * Controle comandos no jogo
 */
var controles = [];
var largura, altura;
var cargaErs;

maneCanvas.addEventListener('click',
		function(event) {
			var x = event.pageX;
			var y = event.pageY;

			var clickControle = false;
			controles
					.forEach(function(controle) {
						if (y > controle.y && y < controle.y + controle.height
								&& x > controle.x
								&& x < controle.x + controle.width) {
							clickControle = true;
							if (controle.tipo == 'controleMotor') {
								rest_potenciaMotor(controle.valor);
							}
							if (controle.tipo == 'controlePiloto') {
								rest_agressividadePiloto(controle.valor);
							}
							if (controle.tipo == 'Ers') {
								rest_ers();
							}
							if (controle.tipo == 'Drs') {
								rest_drs();
							}
							if (controle.tipo == 'Box') {
								rest_boxPiloto(!dadosParciais.box,
										dadosParciais.tpPneusBox,
										dadosParciais.combustBox,
										dadosParciais.asaBox);
							}
							if (controle.tipo == 'Pneu') {
								rest_boxPiloto(true, controle.valor,
										dadosParciais.combustBox,
										dadosParciais.asaBox);
							}
							if (controle.tipo == 'Asa') {
								rest_boxPiloto(true, dadosParciais.tpPneusBox,
										dadosParciais.combustBox,
										controle.valor);
							}
							if (controle.tipo == 'Combustivel') {
								if (controle.valor == '+') {
									rest_boxPiloto(true,
											dadosParciais.tpPneusBox,
											dadosParciais.combustBox + 10,
											dadosParciais.asaBox);
								} else if (controle.valor == '-') {
									rest_boxPiloto(true,
											dadosParciais.tpPneusBox,
											dadosParciais.combustBox - 10,
											dadosParciais.asaBox);
								}

							}
						}
					});
			if (!clickControle) {
				ctl_mudaTracadoPiloto(event);
			}

		}, false);

function ctl_mudaTracadoPiloto(event) {
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
	if ((piloto.tracado == 1 && mudar == 1)
			|| (piloto.tracado == 2 && mudar == 2)) {
		menor = 100000;
	}

	console.log('rest_tracadoPiloto(mudar); ' + mudar)
	if (menor != 100000) {
		rest_tracadoPiloto(mudar);
	}
}

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

				if (controle.tipo == 'Ers' && dadosJogo.ers) {
					if (!cargaErs) {
						cargaErs = dadosParciais.cargaErs;
					}
					if (dadosParciais.cargaErs != cargaErs) {
						cargaErs = dadosParciais.cargaErs;
						maneContext.strokeStyle = '#00FF00';
					} else {
						maneContext.strokeStyle = controle.cor;
					}
				}

				if (controle.tipo == 'Drs' && dadosJogo.drs) {
					if (dadosParciais.asa == 'MENOS_ASA') {
						maneContext.strokeStyle = '#00FF00';
					} else {
						maneContext.strokeStyle = controle.cor;
					}
				}

				if (!dadosParciais.box
						&& (controle.tipo == 'Asa' || controle.tipo == 'Pneu'
								|| controle.tipo == 'CombustivelValor' || controle.tipo == 'Combustivel')) {
					return;
				}
				if (controle.tipo == 'CombustivelValor') {
					controle.valor = dadosParciais.combustBox;
				}

				maneContext.rect(controle.x, controle.y, controle.width,
						controle.height);
				maneContext.font = '30px sans-serif';
				if (controle.centralizaTexto) {
					maneContext.fillText(controle.exibir, controle.x
							+ (controle.width / 2) - 10, controle.y
							+ (controle.height / 2) + 10);
				} else {
					maneContext.fillText(controle.exibir, controle.x + 5,
							controle.y + (controle.height / 2) + 10);

				}
				maneContext.closePath();
				maneContext.stroke();
			});
}

controles.push({
	cor : '#BABACA',
	valor : 'GIRO_MIN',
	exibir : '1',
	centralizaTexto : true,
	tipo : 'controleMotor',
	width : 40,
	height : 40,
	evalY : 'window.innerHeight - 100;',
	y : window.innerHeight - 100,
	x : 10
});
controles.push({
	cor : '#BABACA',
	valor : 'GIRO_NOR',
	exibir : '2',
	tipo : 'controleMotor',
	centralizaTexto : true,
	width : 40,
	height : 40,
	evalY : 'window.innerHeight - 100;',
	y : window.innerHeight - 100,
	x : 60
});
controles.push({
	cor : '#BABACA',
	valor : 'GIRO_MAX',
	exibir : '3',
	tipo : 'controleMotor',
	centralizaTexto : true,
	width : 40,
	height : 40,
	evalY : 'window.innerHeight - 100;',
	y : window.innerHeight - 100,
	x : 110
});

controles.push({
	cor : '#BABACA',
	valor : 'AGRESSIVO',
	exibir : '3',
	tipo : 'controlePiloto',
	centralizaTexto : true,
	width : 40,
	height : 40,
	evalY : 'window.innerHeight - 100;',
	y : window.innerHeight - 100,
	evalX : 'window.innerWidth - 150;',
	x : 0
});
controles.push({
	cor : '#BABACA',
	valor : 'NORMAL',
	exibir : '2',
	centralizaTexto : true,
	tipo : 'controlePiloto',
	width : 40,
	height : 40,
	evalY : 'window.innerHeight - 100;',
	y : window.innerHeight - 100,
	evalX : 'window.innerWidth - 100;',
	x : 0
});
controles.push({
	cor : '#BABACA',
	valor : 'LENTO',
	exibir : '1',
	centralizaTexto : true,
	tipo : 'controlePiloto',
	width : 40,
	height : 40,
	evalY : 'window.innerHeight - 100;',
	y : window.innerHeight - 100,
	evalX : 'window.innerWidth - 50;',
	x : 0
});
controles.push({
	cor : '#BABACA',
	valor : 'E',
	exibir : 'E',
	tipo : 'Ers',
	centralizaTexto : true,
	width : 40,
	height : 40,
	evalY : 'window.innerHeight - 150;',
	y : window.innerHeight - 150,
	x : 10
});
controles.push({
	cor : '#BABACA',
	valor : 'D',
	exibir : 'D',
	tipo : 'Drs',
	centralizaTexto : true,
	width : 40,
	height : 40,
	evalY : 'window.innerHeight - 150;',
	y : window.innerHeight - 150,
	evalX : 'window.innerWidth - 50;',
	x : 0
});

// Box
controles.push({
	cor : '#BABACA',
	valor : 'BOX',
	exibir : 'BOX',
	tipo : 'Box',
	centralizaTexto : false,
	width : 80,
	height : 40,
	y : 10,
	evalX : '(window.innerWidth/2 - 40);',
	x : (window.innerWidth / 2 - 40)
});

controles.push({
	cor : '#BABACA',
	valor : 'TIPO_PNEU_MOLE',
	exibir : 'M',
	tipo : 'Pneu',
	centralizaTexto : true,
	width : 40,
	height : 40,
	y : 60,
	evalX : '(window.innerWidth/2 - 80);',
	x : (window.innerWidth / 2 - 80)
});
controles.push({
	cor : '#BABACA',
	valor : 'TIPO_PNEU_DURO',
	exibir : 'D',
	tipo : 'Pneu',
	centralizaTexto : true,
	width : 40,
	height : 40,
	y : 60,
	evalX : '(window.innerWidth/2 - 20);',
	x : (window.innerWidth / 2 - 20)
});
controles.push({
	cor : '#BABACA',
	valor : 'TIPO_PNEU_CHUVA',
	exibir : 'C',
	tipo : 'Pneu',
	centralizaTexto : true,
	width : 40,
	height : 40,
	y : 60,
	evalX : '(window.innerWidth/2 + 40);',
	x : (window.innerWidth / 2 + 40)
});

controles.push({
	cor : '#BABACA',
	valor : 'MAIS_ASA',
	exibir : '1',
	tipo : 'Asa',
	centralizaTexto : true,
	width : 40,
	height : 40,
	y : 110,
	evalX : '(window.innerWidth/2 - 80);',
	x : (window.innerWidth / 2 - 80)
});
controles.push({
	cor : '#BABACA',
	valor : 'ASA_NORMAL',
	exibir : '2',
	tipo : 'Asa',
	centralizaTexto : true,
	width : 40,
	height : 40,
	y : 110,
	evalX : '(window.innerWidth/2 - 20);',
	x : (window.innerWidth / 2 - 20)
});
controles.push({
	cor : '#BABACA',
	valor : 'MAIS_ASA',
	exibir : '3',
	tipo : 'Asa',
	centralizaTexto : true,
	width : 40,
	height : 40,
	y : 110,
	evalX : '(window.innerWidth/2 + 40);',
	x : (window.innerWidth / 2 + 40)
});

controles.push({
	cor : '#BABACA',
	valor : '-',
	exibir : '-',
	tipo : 'Combustivel',
	centralizaTexto : true,
	width : 40,
	height : 40,
	y : 160,
	evalX : '(window.innerWidth/2 - 80);',
	x : (window.innerWidth / 2 - 80)
});
controles.push({
	cor : '#BABACA',
	valor : '',
	exibir : '100',
	tipo : 'CombustivelValor',
	centralizaTexto : false,
	width : 60,
	height : 40,
	y : 160,
	evalX : '(window.innerWidth/2 - 30);',
	x : (window.innerWidth / 2 - 30)
});
controles.push({
	cor : '#BABACA',
	valor : '+',
	exibir : '+',
	tipo : 'Combustivel',
	centralizaTexto : true,
	width : 40,
	height : 40,
	y : 160,
	evalX : '(window.innerWidth/2 + 40);',
	x : (window.innerWidth / 2 + 40)
});
