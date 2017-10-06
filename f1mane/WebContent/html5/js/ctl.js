/**
 * Controle comandos no jogo
 */
var controles = [];
var largura, altura, centroX;
var cargaErs;
var corFundo = "rgba(255, 255, 255, 0.6)";

function ctl_desenha() {
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
	centroX = largura / 2;
	ctl_desenhaInfoEsquerda();
	ctl_desenhaInfoDireita();
	ctl_desenhaInfoBaixo();
	ctl_desenhaControles(evalX, evalY);
}

function ctl_desenhaControles(evalX, evalY) {
	controles
			.forEach(function(controle) {
				var validaControle = ctl_validaControle(controle);
				if (validaControle) {
					return;
				}

				if (evalY && controle.evalY) {
					controle.y = eval(controle.evalY);
				}
				if (evalX && controle.evalX) {
					controle.x = eval(controle.evalX);
				}
				maneContext.beginPath();

				maneContext.fillStyle = corFundo
				maneContext.fillRect(controle.x, controle.y, controle.width,
						controle.height);
				maneContext.strokeStyle = controle.cor;

				if (controle.tipo == 'controleMotor') {
					if (dadosParciais.giro == 1 && controle.valor == 'GIRO_MIN') {
						maneContext.strokeStyle = '#00FF00';
					} else if (dadosParciais.giro == 5
							&& controle.valor == 'GIRO_NOR') {
						maneContext.strokeStyle = '#FFFF00';
					} else if (dadosParciais.giro == 9
							&& controle.valor == 'GIRO_MAX') {
						maneContext.strokeStyle = '#FF0000';
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
					}
				}

				if (controle.tipo == 'Ers' && dadosJogo.ers) {
					if (!cargaErs) {
						cargaErs = dadosParciais.cargaErs;
					}
					if (dadosParciais.cargaErs != cargaErs) {
						cargaErs = dadosParciais.cargaErs;
						maneContext.strokeStyle = '#00FF00';
					}
				}

				if (controle.tipo == 'Drs' && dadosJogo.drs
						&& dadosParciais.asa == 'MENOS_ASA') {
					maneContext.strokeStyle = '#00FF00';
				}

				if (controle.tipo == 'Pneu'
						&& dadosParciais.tpPneusBox == controle.valor) {
					maneContext.strokeStyle = '#FFFF00';
				}

				if (controle.tipo == 'Asa'
						&& dadosParciais.asaBox == controle.valor) {
					maneContext.strokeStyle = '#FFFF00';
				}

				if (controle.tipo == 'CombustivelValor') {
					controle.valor = dadosParciais.combustBox;
				}

				maneContext.rect(controle.x, controle.y, controle.width,
						controle.height);
				maneContext.font = '30px sans-serif';
				maneContext.fillStyle = "black"
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

function ctl_desenhaInfoBaixo() {
	var posicaoPilotos = dadosParciais.posisPack;
	var pilotoSelecionado = posicaoPilotos.posis[posicaoCentraliza];

	var img1, img2;
	var imgCap1, imgCap2;
	var imgPneu1, imgPneu2;

	var diff;
	

	
	if (posicaoCentraliza == 0) {
		img1 = carrosLadoImgMap.get(posicaoPilotos.posis[0].idPiloto);
		img2 = carrosLadoImgMap.get(posicaoPilotos.posis[1].idPiloto);
		imgCap1 = capaceteImgMap.get(posicaoPilotos.posis[0].idPiloto);
		imgCap2 =	capaceteImgMap.get(posicaoPilotos.posis[1].idPiloto);
		var ptsFrente = ptsPistaMap.get(posicaoPilotos.posis[0].idPiloto);
		var ptsAtras = ptsPistaMap.get(posicaoPilotos.posis[1].idPiloto);
		diff = formatarTempo(ptsFrente-ptsAtras);
		if(dadosParciais.tpPneus == "TIPO_PNEU_MOLE"){
			imgPneu1 = imgPneuM;
		}else if(dadosParciais.tpPneus == "TIPO_PNEU_DURO"){
			imgPneu1 = imgPneuD;
		}else if(dadosParciais.tpPneus == "TIPO_PNEU_CHUVA"){
			imgPneu1 = imgPneuC;
		}
		if(dadosParciais.tpPneusAtras == "TIPO_PNEU_MOLE"){
			imgPneu2 = imgPneuM;
		}else if(dadosParciais.tpPneusAtras == "TIPO_PNEU_DURO"){
			imgPneu2 = imgPneuD;
		}else if(dadosParciais.tpPneusAtras == "TIPO_PNEU_CHUVA"){
			imgPneu2 = imgPneuC;
		}
	}else if (posicaoCentraliza == posicaoPilotos.posis.length-1) {
		img1 = carrosLadoImgMap.get(posicaoPilotos.posis[posicaoPilotos.posis.length-2].idPiloto);
		img2 = carrosLadoImgMap.get(posicaoPilotos.posis[posicaoPilotos.posis.length-1].idPiloto);
		imgCap1 = capaceteImgMap.get(posicaoPilotos.posis[posicaoPilotos.posis.length-2].idPiloto);
		imgCap2 =	capaceteImgMap.get(posicaoPilotos.posis[posicaoPilotos.posis.length-1].idPiloto);
		var ptsFrente = ptsPistaMap.get(posicaoPilotos.posis[posicaoPilotos.posis.length-2].idPiloto);
		var ptsAtras = ptsPistaMap.get(posicaoPilotos.posis[posicaoPilotos.posis.length-1].idPiloto);
		diff = formatarTempo(ptsFrente-ptsAtras);
		if(dadosParciais.tpPneusFrente == "TIPO_PNEU_MOLE"){
			imgPneu1 = imgPneuM;
		}else if(dadosParciais.tpPneusFrente == "TIPO_PNEU_DURO"){
			imgPneu1 = imgPneuD;
		}else if(dadosParciais.tpPneusFrente == "TIPO_PNEU_CHUVA"){
			imgPneu1 = imgPneuC;
		}
		if(dadosParciais.tpPneus == "TIPO_PNEU_MOLE"){
			imgPneu2 = imgPneuM;
		}else if(dadosParciais.tpPneus == "TIPO_PNEU_DURO"){
			imgPneu2 = imgPneuD;
		}else if(dadosParciais.tpPneus == "TIPO_PNEU_CHUVA"){
			imgPneu2 = imgPneuC;
		}

	}else{
		var pilotoFrete = posicaoPilotos.posis[posicaoCentraliza-1];
		var pilotoAtras = posicaoPilotos.posis[posicaoCentraliza+1];
		var ptsFrente = ptsPistaMap.get(pilotoFrete.idPiloto);
		var ptsAtras = ptsPistaMap.get(pilotoAtras.idPiloto);
		var pSelPts = ptsPistaMap.get(posicaoPilotos.posis[posicaoCentraliza].idPiloto);
		var diffFrente = ptsFrente - pSelPts;
		var diffAtras = pSelPts - ptsAtras;
		if(diffFrente<diffAtras){
			img1 = carrosLadoImgMap.get(pilotoFrete.idPiloto);
			img2 = carrosLadoImgMap.get(posicaoPilotos.posis[posicaoCentraliza].idPiloto);
			imgCap1 = capaceteImgMap.get(pilotoFrete.idPiloto);
			imgCap2 = capaceteImgMap.get(posicaoPilotos.posis[posicaoCentraliza].idPiloto);
			if(dadosParciais.tpPneus == "TIPO_PNEU_MOLE"){
				imgPneu2 = imgPneuM;
			}else if(dadosParciais.tpPneus == "TIPO_PNEU_DURO"){
				imgPneu2 = imgPneuD;
			}else if(dadosParciais.tpPneus == "TIPO_PNEU_CHUVA"){
				imgPneu2 = imgPneuC;
			}
			if(dadosParciais.tpPneusFrente == "TIPO_PNEU_MOLE"){
				imgPneu1 = imgPneuM;
			}else if(dadosParciais.tpPneusFrente == "TIPO_PNEU_DURO"){
				imgPneu1 = imgPneuD;
			}else if(dadosParciais.tpPneusFrente == "TIPO_PNEU_CHUVA"){
				imgPneu1 = imgPneuC;
			}
		}else{
			img1 = carrosLadoImgMap.get(posicaoPilotos.posis[posicaoCentraliza].idPiloto);
			img2 = carrosLadoImgMap.get(pilotoAtras.idPiloto);
			imgCap1 = capaceteImgMap.get(posicaoPilotos.posis[posicaoCentraliza].idPiloto);
			imgCap2 = capaceteImgMap.get(pilotoAtras.idPiloto);
			if(dadosParciais.tpPneus == "TIPO_PNEU_MOLE"){
				imgPneu1 = imgPneuM;
			}else if(dadosParciais.tpPneus == "TIPO_PNEU_DURO"){
				imgPneu1 = imgPneuD;
			}else if(dadosParciais.tpPneus == "TIPO_PNEU_CHUVA"){
				imgPneu1 = imgPneuC;
			}
			if(dadosParciais.tpPneusAtras == "TIPO_PNEU_MOLE"){
				imgPneu2 = imgPneuM;
			}else if(dadosParciais.tpPneusAtras == "TIPO_PNEU_DURO"){
				imgPneu2 = imgPneuD;
			}else if(dadosParciais.tpPneusAtras == "TIPO_PNEU_CHUVA"){
				imgPneu2 = imgPneuC;
			}
		}
		diff = formatarTempo(ptsFrente-ptsAtras);
	}
	if(diff){
		maneContext.fillStyle = corFundo
		maneContext.fillRect(centroX-25, altura-40, 60, 20);
		maneContext.font = '14px sans-serif';
		maneContext.fillStyle = "black"
		maneContext.fillText(diff, centroX-20,altura-25);
	}
	if (img1) {
		maneContext.drawImage(img1, centroX - img1.width - 40, altura
				- img1.height - 10);
		maneContext.drawImage(imgPneu1, centroX - imgPneu1.width - img1.width - 45, altura
				- imgPneu1.height - 10);
		if (imgCap1) {
			maneContext.drawImage(imgCap1, centroX - imgCap1.width - img1.width - 55, altura
					- imgCap1.height - 10);
		}
	}
	if (img2) {
		maneContext.drawImage(img2, centroX + 40, altura - img2.height - 10);
		maneContext.drawImage(imgPneu2, centroX + 45 + img2.width, altura - imgPneu2.height - 10);
		if (imgCap2) {
			maneContext.drawImage(imgCap2, centroX + 55 + img2.width, altura - imgCap2.height - 10);
		}
	}
}

function ctl_desenhaInfoDireita() {
	if (!dadosParciais) {
		return
		

	}

	maneContext.beginPath();

	var x = maneCanvas.width - 120;
	var y = 10;

	if (dadosParciais.melhorVolta && (altura > 480 || !alternador)) {
		maneContext.fillStyle = corFundo
		maneContext.fillRect(x, y, 110, 20);
		maneContext.font = '14px sans-serif';
		maneContext.fillStyle = "black"
		maneContext.fillText(lang_text('Melhor'), x + 5, y + 15);
		maneContext.fillText(formatarTempo(dadosParciais.melhorVolta), x + 53,
				y + 15);
		y += 30;
	}

	if (dadosParciais.ultima1 && (altura > 480 || !alternador)) {
		maneContext.fillStyle = corFundo
		maneContext.fillRect(x, y, 110, 20);
		maneContext.font = '14px sans-serif';
		maneContext.fillStyle = "black"
		maneContext.fillText(lang_text('Volta') + ' '
				+ (dadosParciais.voltaAtual - 1) + ' '
				+ formatarTempo(dadosParciais.ultima1), x + 5, y + 15);

		y += 30;

		if (dadosParciais.ultima2) {
			maneContext.fillStyle = corFundo
			maneContext.fillRect(x, y, 110, 20);
			maneContext.font = '14px sans-serif';
			maneContext.fillStyle = "black"
			maneContext.fillText(lang_text('Volta') + ' '
					+ (dadosParciais.voltaAtual - 2) + ' '
					+ formatarTempo(dadosParciais.ultima2), x + 5, y + 15);
			y += 30;
		}

		if (dadosParciais.ultima3) {
			maneContext.fillStyle = corFundo
			maneContext.fillRect(x, y, 110, 20);
			maneContext.font = '14px sans-serif';
			maneContext.fillStyle = "black"
			maneContext.fillText(lang_text('Volta') + ' '
					+ (dadosParciais.voltaAtual - 3) + ' '
					+ formatarTempo(dadosParciais.ultima3), x + 5, y + 15);
			y += 30;
		}

		if (dadosParciais.ultima4) {
			maneContext.fillStyle = corFundo
			maneContext.fillRect(x, y, 110, 20);
			maneContext.font = '14px sans-serif';
			maneContext.fillStyle = "black"
			maneContext.fillText(lang_text('Volta') + ' '
					+ (dadosParciais.voltaAtual - 4) + ' '
					+ formatarTempo(dadosParciais.ultima4), x + 5, y + 15);
			y += 30;
		}

	}

	var posicaoPilotos = dadosParciais.posisPack;
	if (posicaoPilotos
			&& (altura > 480 || (alternador || !dadosParciais.melhorVoltaCorrida))) {
		var piloto = posicaoPilotos.posis[0];
		var nomePiloto = pilotosMap.get(piloto.idPiloto).nome;
		maneContext.fillStyle = corFundo
		maneContext.fillRect(x, y, 110, 20);
		maneContext.font = '14px sans-serif';
		maneContext.fillStyle = "black"
		maneContext.fillText('1 ' + nomePiloto, x + 5, y + 15);
		if (idPilotoSelecionado == piloto.idPiloto) {
			maneContext.strokeStyle = '#00FF00';
			maneContext.rect(x, y, 110, 20);
		} else if (piloto.humano) {
			maneContext.strokeStyle = '#FFFF00';
			maneContext.rect(x, y, 110, 20);
		}

		y += 30;
		var min = posicaoCentraliza - 2;
		var max = posicaoCentraliza + 2;

		if (min < 1) {
			min = 1;
			max = 5;
		} else if (max > posicaoPilotos.posis.length) {
			var diff = posicaoPilotos.posis.length - max;
			min -= diff;
			max -= diff;
		}
		for (i = min; i < max; i++) {
			var piloto = posicaoPilotos.posis[i];
			var nomePiloto = pilotosMap.get(piloto.idPiloto).nome;
			maneContext.fillStyle = corFundo
			maneContext.fillRect(x, y, 110, 20);
			maneContext.font = '14px sans-serif';
			maneContext.fillStyle = "black"
			maneContext.fillText((i + 1) + ' ' + nomePiloto, x + 5, y + 15);
			if (idPilotoSelecionado == piloto.idPiloto) {
				maneContext.strokeStyle = '#00FF00';
				maneContext.rect(x, y, 110, 20);
			} else if (piloto.humano) {
				maneContext.strokeStyle = '#FFFF00';
				maneContext.rect(x, y, 110, 20);
			}
			y += 30;
		}
	}
	maneContext.closePath();
	maneContext.stroke();
}

function ctl_desenhaInfoEsquerda() {
	if (!dadosParciais) {
		return
		

	}

	maneContext.beginPath();

	var x = 10;
	var y = 10;

	if (altura > 480 || !alternador) {

		maneContext.fillStyle = corFundo
		maneContext.fillRect(x, y, 120, 20);
		maneContext.font = '14px sans-serif';
		maneContext.fillStyle = "black"
		maneContext.fillText(dadosJogo.nomeCircuito, x + 5, y + 15);

		y += 30;

		maneContext.fillStyle = corFundo
		maneContext.fillRect(x, y, 80, 20);
		maneContext.font = '14px sans-serif';
		maneContext.fillStyle = "black"
		maneContext.fillText(lang_text('Volta'), x + 5, y + 15);
		maneContext.fillText(dadosParciais.voltaAtual + '/'
				+ dadosJogo.numeroVotas, x + 45, y + 15);

		y += 30;

		if (dadosParciais.melhorVoltaCorrida) {
			maneContext.fillStyle = corFundo
			maneContext.fillRect(x, y, 110, 20);
			maneContext.font = '14px sans-serif';
			maneContext.fillStyle = "black"
			maneContext.fillText(lang_text('Corrida'), x + 5, y + 15);
			maneContext.fillText(
					formatarTempo(dadosParciais.melhorVoltaCorrida), x + 53,
					y + 15);
			y += 30;
		}

		maneContext.fillStyle = corFundo
		maneContext.fillRect(x, y, 80, 20);
		maneContext.font = '14px sans-serif';
		maneContext.fillStyle = "black"
		var climaDesc = '';
		if (dadosParciais.clima == "sol.png") {
			climaDesc = "Ensolarado"
		}
		if (dadosParciais.clima == "nublado.png") {
			climaDesc = "Nublado"
		}
		if (dadosParciais.clima == "chuva.png") {
			climaDesc = "Chovendo"
		}
		maneContext.fillText(lang_text(climaDesc), x + 5, y + 15);

		y += 30;

		maneContext.fillStyle = corFundo
		maneContext.fillRect(x, y, 80, 20);
		maneContext.font = '14px sans-serif';
		maneContext.fillStyle = "black"
		maneContext.fillText('~' + dadosParciais.velocidade + ' Km/h', x + 5,
				y + 15);

		y += 30;

	}

	if (altura > 480 || alternador) {

		maneContext.fillStyle = corFundo
		maneContext.fillRect(x, y, 80, 20);
		maneContext.font = '14px sans-serif';
		maneContext.fillStyle = "black"
		maneContext.fillText(lang_text('Comb.'), x + 5, y + 15);
		maneContext.fillText(dadosParciais.pCombust + '%', x
				+ (dadosParciais.pCombust > 99 ? 45 : 50), y + 15);
		if (dadosParciais.pCombust < 15) {
			maneContext.strokeStyle = '#FF0000';
			maneContext.rect(x, y, 80, 20);
		}

		y += 30;

		maneContext.fillStyle = corFundo
		maneContext.fillRect(x, y, 80, 20);
		maneContext.font = '14px sans-serif';
		maneContext.fillStyle = "black"
		maneContext.fillText(lang_text('Pneus'), x + 5, y + 15);
		maneContext.fillText(dadosParciais.pPneus + '%', x
				+ (dadosParciais.pPneus > 99 ? 45 : 50), y + 15);
		if (dadosParciais.pPneus < 15) {
			maneContext.strokeStyle = '#FF0000';
			maneContext.rect(x, y, 80, 20);
		}

		y += 30;

		maneContext.fillStyle = corFundo
		maneContext.fillRect(x, y, 80, 20);
		maneContext.font = '14px sans-serif';
		maneContext.fillStyle = "black"
		maneContext.fillText(lang_text('Motor'), x + 5, y + 15);
		maneContext.fillText(dadosParciais.pMotor + '%', x
				+ (dadosParciais.pMotor > 99 ? 45 : 50), y + 15);

		if (dadosParciais.pMotor < 15) {
			maneContext.strokeStyle = '#FF0000';
			maneContext.rect(x, y, 80, 20);
		}

		y += 30;

		maneContext.fillStyle = corFundo
		maneContext.fillRect(x, y, 80, 20);
		maneContext.font = '14px sans-serif';
		maneContext.fillStyle = "black"
		maneContext.fillText(lang_text('Piloto'), x + 5, y + 15);
		maneContext.fillText(dadosParciais.stress + '%', x
				+ (dadosParciais.stress > 99 ? 45 : 50), y + 15);

		if (dadosParciais.stress > 90) {
			maneContext.strokeStyle = '#FF0000';
			maneContext.rect(x, y, 80, 20);
		}

		y += 30;

		maneContext.fillStyle = corFundo
		maneContext.fillRect(x, y, 80, 20);
		maneContext.font = '14px sans-serif';
		maneContext.fillStyle = "black"
		maneContext.fillText('Ers ', x + 5, y + 15);
		maneContext.fillText(dadosParciais.cargaErs + '%', x
				+ (dadosParciais.cargaErs > 99 ? 45 : 50), y + 15);
	}
	maneContext.closePath();
	maneContext.stroke();
}

function ctl_validaControle(controle) {
	if (!dadosParciais.box
			&& (controle.tipo == 'Asa' || controle.tipo == 'Pneu'
					|| controle.tipo == 'CombustivelValor' || controle.tipo == 'Combustivel')) {
		return true;
	}
	if (dadosJogo.drs && controle.tipo == 'Asa'
			&& 'chuva.png' != dadosParciais.clima) {
		return true;
	}
	if (!dadosJogo.reabastacimento
			&& (controle.tipo == 'CombustivelValor' || controle.tipo == 'Combustivel')) {
		return true;
	}

	if (!dadosJogo.trocaPneu && controle.tipo == 'Pneu' && dadosJogo.pPneus > 0) {
		return true;
	}

	return false;
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
							var validaControle = ctl_validaControle(controle);
							if (validaControle) {
								return;
							}

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