/**
 * Controle comandos no jogo
 */
var controles = [];
var largura, altura, centroX;
var cargaErs;
var telaCheia = false;
var corFundo = "rgba(255, 255, 255, 0.6)";
var corAmarelo = "rgba(255, 255, 0, 0.6)";
var corVermelho = "rgba(255, 0, 0, 0.6)";
var contCargaErs;
var confirmaSair = false;

function ctl_desenha() {
	largura = maneCanvas.width;
	altura = maneCanvas.height;
	centroX = largura / 2;
	centroY = altura / 2;
	ctl_desenhaInfoSegundosParaIniciar();
	ctl_desenhaQualificacao();
	ctl_desenhaInfoEsquerda();
	ctl_desenhaInfoDireita();
	ctl_desenhaInfoBaixo();
	ctl_desenhaInfoAsa();
	ctl_desenhaControles();
	ctl_desenhaFarois();
}

function ctl_desenhaFarois(){
	if (!dadosParciais) {
		return;
	}
	if (dadosParciais.estado != "13") {
		return;
	}
	maneContext.drawImage(imgFarois, centroX - (imgFarois.width/2) , 100);
}


function ctl_desenhaQualificacao(){
	if(!dadosJogo || dadosParciais){
		return;
	}
	if(dadosJogo.estado != "10"){
		return;
	}
	
	var x = centroX-140;
	var y = 20;
	
	maneContext.fillStyle = corFundo
	maneContext.fillRect(x-10, y, 170, 40);
	maneContext.font = '24px sans-serif';
	maneContext.fillStyle = "black"
	maneContext.fillText(lang_text('Classificação'), x + 5, y + 28);

	y += 60;
		
	for (var i = 0; i < dadosJogo.pilotos.length; i++) {
		if(i%2==0){
			x = centroX-140;
		}else{
			x = centroX+30;
		}
		var piloto = dadosJogo.pilotos[i];
		maneContext.beginPath();
		maneContext.font = '14px sans-serif';
		
		var nmPiloto = piloto.nome;
		nmPiloto = nmPiloto.split(".")[1];
		nmPiloto = nmPiloto.substr(0, 3);
		nmPiloto = (i + 1) + ' ' + nmPiloto;
		var tempo = piloto.tempoVoltaQualificacao;

		maneContext.fillStyle = piloto.carro.cor1Hex;
		maneContext.fillRect(x - 10, y, 5, 20);
//		maneContext.fillStyle = piloto.carro.cor2Hex;
//		maneContext.fillRect(x - 7, y, 3, 20);
		
		maneContext.fillStyle = corFundo
		maneContext.fillRect(x - 5, y, 50, 20);
		maneContext.fillStyle = "black"
		maneContext.fillText(nmPiloto, x, y + 15);
		
		if (idPilotoSelecionado == piloto.id) {
			maneContext.fillStyle = '#00FF00';
			maneContext.fillRect(x - 10, y+20, 130, 5);
		} else if (piloto.jogadorHumano) {
			maneContext.fillStyle = '#FFFF00';
			maneContext.fillRect(x - 10, y+20, 130, 5);
		}
		
		maneContext.fillStyle = corFundo
		maneContext.fillRect(x + 50, y, 70, 20);
		maneContext.fillStyle = "black"
		maneContext.fillText(tempo, x + 60, y + 15);
		
		maneContext.closePath();
		maneContext.stroke();
		
		y+=15;

	}
	
}

function ctl_desenhaInfoSegundosParaIniciar(){
	if(!dadosJogo || dadosParciais){
		return;
	}
	if(dadosJogo.estado != "07"){
		return;
	}
	var x = centroX - 70;
	var y = centroY - 50;
	maneContext.fillStyle = corFundo
	maneContext.fillRect(x-10, y, 170, 40);
	maneContext.font = '24px sans-serif';
	maneContext.fillStyle = "black"
	maneContext.fillText(lang_text('Inicia em :'), x + 5, y + 28);
	maneContext.fillText(dadosJogo.segundosParaIniciar, x + 120, y + 28);
	
}

function ctl_desenhaControles() {
	if (!dadosParciais) {
		return;
	}
	controles.forEach(function(controle) {
				if (ctl_removeControle(controle)) {
					return;
				}
				maneContext.beginPath();
				maneContext.setLineDash([]);
				if (controle.evalY) {
					controle.y = eval(controle.evalY);
				}
				if (controle.evalX) {
					controle.x = eval(controle.evalX);
				}

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
						contCargaErs = 25;
					}
					if(contCargaErs>0){
						contCargaErs--;
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
				
				if (controle.tipo == 'Box' && dadosParciais.box) {
					maneContext.strokeStyle = '#FFFF00';
				}

				if (controle.tipo == 'CombustivelValor') {
					controle.valor = dadosParciais.combustBox;
					controle.exibir = dadosParciais.combustBox;
				}

				maneContext.font = '30px sans-serif';
				maneContext.fillStyle = "black"
			    var verImg = maneContext.strokeStyle != controle.cor || (controle.tipo != 'controlePiloto' && controle.tipo != 'controleMotor');				
				if (controle.img && verImg) {
					maneContext.rect(controle.x, controle.y,
							controle.width + 5, controle.height + 5);
					maneContext.drawImage(controle.img, controle.x, controle.y);
				} else {
					maneContext.rect(controle.x, controle.y, controle.width,
							controle.height);
					if (controle.centralizaTexto) {
						maneContext.fillText(controle.exibir, controle.x
								+ (controle.width / 2) - 10, controle.y
								+ (controle.height / 2) + 10);
					} else {
						maneContext.fillText(controle.exibir, controle.x + 5,
								controle.y + (controle.height / 2) + 10);

					}
				}
				maneContext.closePath();
				maneContext.stroke();
			});
}

function ctl_mudaTracadoPiloto(event) {
	if(!dadosParciais){
		return;
	}
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

function ctl_desenhaInfo() {
	$('#info').css('position', 'absolute');
	if(altura>480){
		$('#info').css('top', (altura-100)+'px');
	}else{
		$('#info').css('top', (altura-40)+'px');
	}
	$('#info').css('left', '10px');
	$('#info').css('margin-right', '10px');
	$('#info').css('font-family', 'sans-serif');
	if($('#info').html().indexOf('table')>0){
		$('#info').css('background-color', corFundo);
		$('#info').css('font-size', '11px');
		if(altura<480){
			$('#info').css('top', (altura-90)+'px');
			$('#info').css('left', centroX - ($('#info').width()/2) +'px');
		}
	}else{
		$('#info').css('background-color', corFundo);
		$('#info').css('font-size', '14px');
	}
}

function ctl_desenhaInfoBaixo() {
	if (dadosParciais == null) {
		return;
	}
	if(confirmaSair){
		$('#info').hide();
		return;
	}
	if(dadosParciais.posisPack.safetyNoId != 0 ||  dadosParciais.estado != '12' ||  dadosParciais.recebeuBanderada || (alternador && $('#info').html()!='')){
		ctl_desenhaInfo();
		$('#info').show();
	}else{
		$('#info').hide();
		ctl_desenhaInfoCarros();
	}
}

function ctl_desenhaInfoAsa() {
	if (!dadosParciais) {
		return;
	}
	if(confirmaSair){
		return;
	}
	if(dadosJogo.drs){
		return
	}
	
	var y =(altura > 480)?(maneCanvas.height - 200):(maneCanvas.height - 150);
	var x = maneCanvas.width - 70;
	var img;
	if(dadosParciais.asa == 'MAIS_ASA'){
		img = maisAsa;
	}else if(dadosParciais.asa == 'ASA_NORMAL'){
		img =  normalAsa;
	}else if(dadosParciais.asa == 'MENOS_ASA'){
		img = menosAsa;
	}
	maneContext.strokeStyle = '#babaca';
	maneContext.rect(x, y, img.width + 5, img.height + 5);
	maneContext.drawImage(img, x, y);
}


function ctl_desenhaInfoCarros() {
	var posicaoPilotos = dadosParciais.posisPack;
	var pilotoSelecionado = posicaoPilotos.posis[posicaoCentraliza];
	var img1, img2;
	var imgCap1, imgCap2;
	var imgPneu1, imgPneu2;
	var idPiloto1, idPiloto2;

	var diff;

	if (posicaoCentraliza == 0) {
		img1 = carrosLadoImgMap.get(posicaoPilotos.posis[0].idPiloto);
		img2 = carrosLadoImgMap.get(posicaoPilotos.posis[1].idPiloto);
		idPiloto1 = posicaoPilotos.posis[0].idPiloto;
		idPiloto2 = posicaoPilotos.posis[1].idPiloto;
		imgCap1 = capaceteImgMap.get(posicaoPilotos.posis[0].idPiloto);
		imgCap2 = capaceteImgMap.get(posicaoPilotos.posis[1].idPiloto);
		var ptsFrente = ptsPistaMap.get(posicaoPilotos.posis[0].idPiloto);
		var ptsAtras = ptsPistaMap.get(posicaoPilotos.posis[1].idPiloto);
		diff = formatarTempo(ptsFrente - ptsAtras);
		if (dadosParciais.tpPneus == "TIPO_PNEU_MOLE") {
			imgPneu1 = imgPneuM;
		} else if (dadosParciais.tpPneus == "TIPO_PNEU_DURO") {
			imgPneu1 = imgPneuD;
		} else if (dadosParciais.tpPneus == "TIPO_PNEU_CHUVA") {
			imgPneu1 = imgPneuC;
		}
		if (dadosParciais.tpPneusAtras == "TIPO_PNEU_MOLE") {
			imgPneu2 = imgPneuM;
		} else if (dadosParciais.tpPneusAtras == "TIPO_PNEU_DURO") {
			imgPneu2 = imgPneuD;
		} else if (dadosParciais.tpPneusAtras == "TIPO_PNEU_CHUVA") {
			imgPneu2 = imgPneuC;
		}
	} else if (posicaoCentraliza == posicaoPilotos.posis.length - 1) {
		img1 = carrosLadoImgMap
				.get(posicaoPilotos.posis[posicaoPilotos.posis.length - 2].idPiloto);
		img2 = carrosLadoImgMap
				.get(posicaoPilotos.posis[posicaoPilotos.posis.length - 1].idPiloto);
		idPiloto1 = posicaoPilotos.posis[posicaoPilotos.posis.length - 2].idPiloto;
		idPiloto2 = posicaoPilotos.posis[posicaoPilotos.posis.length - 1].idPiloto;

		imgCap1 = capaceteImgMap
				.get(posicaoPilotos.posis[posicaoPilotos.posis.length - 2].idPiloto);
		imgCap2 = capaceteImgMap
				.get(posicaoPilotos.posis[posicaoPilotos.posis.length - 1].idPiloto);
		var ptsFrente = ptsPistaMap
				.get(posicaoPilotos.posis[posicaoPilotos.posis.length - 2].idPiloto);
		var ptsAtras = ptsPistaMap
				.get(posicaoPilotos.posis[posicaoPilotos.posis.length - 1].idPiloto);
		diff = formatarTempo(ptsFrente - ptsAtras);
		if (dadosParciais.tpPneusFrente == "TIPO_PNEU_MOLE") {
			imgPneu1 = imgPneuM;
		} else if (dadosParciais.tpPneusFrente == "TIPO_PNEU_DURO") {
			imgPneu1 = imgPneuD;
		} else if (dadosParciais.tpPneusFrente == "TIPO_PNEU_CHUVA") {
			imgPneu1 = imgPneuC;
		}
		if (dadosParciais.tpPneus == "TIPO_PNEU_MOLE") {
			imgPneu2 = imgPneuM;
		} else if (dadosParciais.tpPneus == "TIPO_PNEU_DURO") {
			imgPneu2 = imgPneuD;
		} else if (dadosParciais.tpPneus == "TIPO_PNEU_CHUVA") {
			imgPneu2 = imgPneuC;
		}

	} else {
		var pilotoFrete = posicaoPilotos.posis[posicaoCentraliza - 1];
		var pilotoAtras = posicaoPilotos.posis[posicaoCentraliza + 1];
		var ptsFrente = ptsPistaMap.get(pilotoFrete.idPiloto);
		var ptsAtras = ptsPistaMap.get(pilotoAtras.idPiloto);
		var pSelPts = ptsPistaMap
				.get(posicaoPilotos.posis[posicaoCentraliza].idPiloto);
		var diffFrente = ptsFrente - pSelPts;
		var diffAtras = pSelPts - ptsAtras;
		if (diffFrente < diffAtras) {
			img1 = carrosLadoImgMap.get(pilotoFrete.idPiloto);
			img2 = carrosLadoImgMap
					.get(posicaoPilotos.posis[posicaoCentraliza].idPiloto);
			
			idPiloto1 = pilotoFrete.idPiloto;
			idPiloto2 = posicaoPilotos.posis[posicaoCentraliza].idPiloto;
			imgCap1 = capaceteImgMap.get(pilotoFrete.idPiloto);
			imgCap2 = capaceteImgMap
					.get(posicaoPilotos.posis[posicaoCentraliza].idPiloto);
			if (dadosParciais.tpPneus == "TIPO_PNEU_MOLE") {
				imgPneu2 = imgPneuM;
			} else if (dadosParciais.tpPneus == "TIPO_PNEU_DURO") {
				imgPneu2 = imgPneuD;
			} else if (dadosParciais.tpPneus == "TIPO_PNEU_CHUVA") {
				imgPneu2 = imgPneuC;
			}
			if (dadosParciais.tpPneusFrente == "TIPO_PNEU_MOLE") {
				imgPneu1 = imgPneuM;
			} else if (dadosParciais.tpPneusFrente == "TIPO_PNEU_DURO") {
				imgPneu1 = imgPneuD;
			} else if (dadosParciais.tpPneusFrente == "TIPO_PNEU_CHUVA") {
				imgPneu1 = imgPneuC;
			}
		} else {
			img1 = carrosLadoImgMap
					.get(posicaoPilotos.posis[posicaoCentraliza].idPiloto);
			img2 = carrosLadoImgMap.get(pilotoAtras.idPiloto);
			idPiloto1 = posicaoPilotos.posis[posicaoCentraliza].idPiloto;
			idPiloto2 = pilotoAtras.idPiloto;
			
			imgCap1 = capaceteImgMap
					.get(posicaoPilotos.posis[posicaoCentraliza].idPiloto);
			imgCap2 = capaceteImgMap.get(pilotoAtras.idPiloto);
			if (dadosParciais.tpPneus == "TIPO_PNEU_MOLE") {
				imgPneu1 = imgPneuM;
			} else if (dadosParciais.tpPneus == "TIPO_PNEU_DURO") {
				imgPneu1 = imgPneuD;
			} else if (dadosParciais.tpPneus == "TIPO_PNEU_CHUVA") {
				imgPneu1 = imgPneuC;
			}
			if (dadosParciais.tpPneusAtras == "TIPO_PNEU_MOLE") {
				imgPneu2 = imgPneuM;
			} else if (dadosParciais.tpPneusAtras == "TIPO_PNEU_DURO") {
				imgPneu2 = imgPneuD;
			} else if (dadosParciais.tpPneusAtras == "TIPO_PNEU_CHUVA") {
				imgPneu2 = imgPneuC;
			}
		}
		diff = formatarTempo(ptsFrente - ptsAtras);
	}
	if (diff) {
		maneContext.beginPath();
		maneContext.fillStyle = corFundo
		maneContext.font = '14px sans-serif';
		maneContext.fillRect(centroX - 25, altura - 40,  maneContext.measureText(diff).width+10, 20);
		maneContext.fillStyle = "black"
		maneContext.fillText(diff, centroX - 20, altura - 25);
		maneContext.closePath();
		maneContext.stroke();
	}
	if (img1) {
		maneContext.drawImage(img1, centroX - img1.width - 40, altura
				- img1.height - 10);
		ctl_problemasCarrro(img1 , centroX, idPiloto1 , 1);
		if (imgPneu1) {
			var x;
			var y;
			if(largura<450){
				x = centroX - imgPneu1.width - 40;
				y =  altura - (imgPneu1.height*2) - 10
			}else{
				x = centroX - imgPneu1.width - img1.width - 40;
				y =  altura - imgPneu1.height - 10;
			}
			maneContext.drawImage(imgPneu1,x, y);
			if (imgCap1) {
				if(largura<450){
					x = centroX - imgCap1.width	- 45 - (imgPneu1.width);
					y = altura - (imgCap1.height*2) - 10;
				}else{
					x = centroX - imgCap1.width	- img1.width - 45 - (imgPneu1.width / 2);
					y = altura - imgCap1.height - 10;
				}
				maneContext.drawImage(imgCap1, x, y);
			}
		}
	}
	if (img2) {
		maneContext.drawImage(img2, centroX + 40, altura - img2.height - 10);
		ctl_problemasCarrro(img2 , centroX, idPiloto2 , 2);
		if (imgPneu2) {
			var x;
			var y;
			if(largura<450){
				x = centroX + 40;
				y =  altura - (imgPneu2.height*2) - 10
			}else{
				x = centroX + 40 + img2.width;
				y =  altura - imgPneu2.height - 10;
			}
			maneContext.drawImage(imgPneu2, x , y);
			if (imgCap2) {
				if(largura<450){
					x = centroX + 45 + (imgPneu2.width);
					y = altura - (imgCap1.height*2) - 10;
				}else{
					x = centroX + 45 + img2.width + (imgPneu2.width / 2);
					y = altura - imgCap2.height - 10;
				}
				maneContext.drawImage(imgCap2, x , y);
			}
		}
	}
}

function ctl_problemasCarrro(img , x, idPiloto , posicao){
	var alertaMotor;
	
	var alertaAerefolio = dadosParciais.alertaAerefolio;
	
	if(idPilotoSelecionado!=idPiloto){
		alertaMotor = ("PANE_SECA" == dadosParciais.dano) || (dadosParciais.dano ==  "EXPLODIU_MOTOR");
	}
	var perdeuAerefolio = pilotosAereofolioMap.get(idPiloto);
	var dnf = pilotosDnfMap.get(idPiloto);
	
	if(posicao == 1){
		maneContext.beginPath();
		if(perdeuAerefolio){
			maneContext.fillStyle = corVermelho;
			maneContext.fillRect(x - img.width - 35, altura - img.height + 10 , 20, 20);
		}else if(alertaAerefolio){
			maneContext.fillStyle = corAmarelo;
			maneContext.fillRect(x - img.width - 35, altura - img.height + 10 , 20, 20);
		}
		if(alertaMotor){
			maneContext.fillStyle = corVermelho;
			maneContext.fillRect(x - (img.width/2) - 35, altura - img.height - 10 , 60, 30);
		}
		if(dnf){
			maneContext.fillStyle = corVermelho;
			maneContext.fillRect(x - img.width - 40, altura	- img.height - 10, img.width, img.height);
		}
		maneContext.closePath();
		maneContext.stroke();
	}else{
		maneContext.beginPath();
		if(perdeuAerefolio){
			maneContext.fillStyle = corVermelho;
			maneContext.fillRect(x + 45, altura - img.height + 10 , 20, 20);
		}
		
		if(alertaMotor){
			maneContext.fillStyle = corVermelho;
			maneContext.fillRect(x  + (img.width/2) + 45, altura - img.height - 10 , 60, 30);
		}
		
		if(dnf){
			maneContext.fillStyle = corVermelho;
			maneContext.fillRect(x + 40, altura - img.height - 10, img.width, img.height);
		}
		
		maneContext.closePath();
		maneContext.stroke();

	}
		
}

function ctl_desenhaInfoDireita() {
	if (!dadosParciais) {
		return;	}
	if(confirmaSair){
		return;
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
		maneContext.fillText(lang_text('Vol') + ' '
				+ (dadosParciais.voltaAtual - 1) + ' '
				+ formatarTempo(dadosParciais.ultima1), x + 5, y + 15);

		y += 30;

		if (dadosParciais.ultima2) {
			maneContext.fillStyle = corFundo
			maneContext.fillRect(x, y, 110, 20);
			maneContext.font = '14px sans-serif';
			maneContext.fillStyle = "black"
			maneContext.fillText(lang_text('Vol') + ' '
					+ (dadosParciais.voltaAtual - 2) + ' '
					+ formatarTempo(dadosParciais.ultima2), x + 5, y + 15);
			y += 30;
		}

		if (dadosParciais.ultima3) {
			maneContext.fillStyle = corFundo
			maneContext.fillRect(x, y, 110, 20);
			maneContext.font = '14px sans-serif';
			maneContext.fillStyle = "black"
			maneContext.fillText(lang_text('Vol') + ' '
					+ (dadosParciais.voltaAtual - 3) + ' '
					+ formatarTempo(dadosParciais.ultima3), x + 5, y + 15);
			y += 30;
		}

		if (dadosParciais.ultima4) {
			maneContext.fillStyle = corFundo
			maneContext.fillRect(x, y, 110, 20);
			maneContext.font = '14px sans-serif';
			maneContext.fillStyle = "black"
			maneContext.fillText(lang_text('Vol') + ' '
					+ (dadosParciais.voltaAtual - 4) + ' '
					+ formatarTempo(dadosParciais.ultima4), x + 5, y + 15);
			y += 30;
		}

	}

	var posicaoPilotos = dadosParciais.posisPack;
	x+=50;
	var larg = 55;
	if (posicaoPilotos
			&& (altura > 480 || (alternador || !dadosParciais.melhorVolta))) {
		var piloto = posicaoPilotos.posis[0];
		var nomePiloto = pilotosMap.get(piloto.idPiloto).nome;
		nomePiloto = nomePiloto.split(".")[1];
		nomePiloto = nomePiloto.substr(0, 3);
		maneContext.beginPath();
		maneContext.fillStyle = corFundo
		maneContext.fillRect(x, y, larg, 20);
		maneContext.font = '14px sans-serif';
		maneContext.fillStyle = "black"
		maneContext.fillText('1 ' + nomePiloto, x + 5, y + 15);
		if (idPilotoSelecionado == piloto.idPiloto) {
			maneContext.strokeStyle = '#00FF00';
			maneContext.rect(x, y, larg, 20);
		} else if (piloto.humano) {
			maneContext.strokeStyle = '#FFFF00';
			maneContext.rect(x, y, larg, 20);
		}
		maneContext.closePath();
		maneContext.stroke();

		y += 30;
		var min = posicaoCentraliza - 2;
		var max = posicaoCentraliza + 2;

		if (min < 1) {
			min = 1;
			max = 5;
		} else if (max > posicaoPilotos.posis.length) {
			var diff =  max - (posicaoPilotos.posis.length);
			min -= diff;
			max -= diff;
		}
		for (var i = min; i < max; i++) {
			maneContext.beginPath();
			var piloto = posicaoPilotos.posis[i];
			var nomePiloto = pilotosMap.get(piloto.idPiloto).nome;
			nomePiloto = nomePiloto.split(".")[1];
			nomePiloto = nomePiloto.substr(0, 3);
			maneContext.fillStyle = corFundo
			maneContext.fillRect(x, y, larg, 20);
			maneContext.font = '14px sans-serif';
			maneContext.fillStyle = "black"
			maneContext.fillText((i + 1) + ' ' + nomePiloto, x + 5, y + 15);
			if (idPilotoSelecionado == piloto.idPiloto) {
				maneContext.strokeStyle = '#00FF00';
				maneContext.rect(x, y, larg, 20);
			} else if (piloto.humano) {
				maneContext.strokeStyle = '#FFFF00';
				maneContext.rect(x, y, larg, 20);
			}
			y += 30;
			maneContext.closePath();
			maneContext.stroke();
		}
	}
	maneContext.closePath();
	maneContext.stroke();
}

function ctl_desenhaInfoEsquerda() {
	if (!dadosParciais) {
		return;	}
	if(confirmaSair){
		return;
	}
	maneContext.beginPath();

	var x = 10;
	var y = 10;

	if (altura > 480 || !alternador) {

		maneContext.fillStyle = corFundo
		maneContext.font = '14px sans-serif';
		maneContext.fillRect(x, y, maneContext.measureText(dadosJogo.nomeCircuito).width+10, 20);
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
		if (pitLane) {
			maneContext.fillText('PitLane', x + 5, y + 15);
		}else if(dadosParciais.posisPack.safetyNoId != 0){
			maneContext.fillText('SafetyCar', x + 5, y + 15);
		}	else {
			maneContext.fillText('~' + dadosParciais.velocidade + ' Km/h',
					x + 5, y + 15);
		}
		if (pitLane || dadosParciais.posisPack.safetyNoId != 0) {
			maneContext.beginPath();
			maneContext.strokeStyle = '#FFFF00';
			maneContext.rect(x, y, 80, 20);
			maneContext.closePath();
			maneContext.stroke();			
		}

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
			maneContext.beginPath();
			maneContext.strokeStyle = '#FF0000';
			maneContext.rect(x, y, 80, 20);
			maneContext.closePath();
			maneContext.stroke();			
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
			maneContext.beginPath();
			maneContext.strokeStyle = '#FF0000';
			maneContext.rect(x, y, 80, 20);
			maneContext.closePath();
			maneContext.stroke();			
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
			maneContext.beginPath();
			maneContext.strokeStyle = '#FF0000';
			maneContext.rect(x, y, 80, 20);
			maneContext.closePath();
			maneContext.stroke();
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
			maneContext.beginPath();
			maneContext.strokeStyle = '#FF0000';
			maneContext.rect(x, y, 80, 20);
			maneContext.closePath();
			maneContext.stroke();
		}else if (dadosParciais.stress > 70) {
			maneContext.beginPath();
			maneContext.strokeStyle = '#FFFF00';
			maneContext.rect(x, y, 80, 20);
			maneContext.closePath();
			maneContext.stroke();
		}

		y += 30;
		if(dadosJogo.ers){
			maneContext.fillStyle = corFundo
			maneContext.fillRect(x, y, 80, 20);
			maneContext.font = '14px sans-serif';
			maneContext.fillStyle = "black"
			maneContext.fillText('Ers ', x + 5, y + 15);
			maneContext.fillText(dadosParciais.cargaErs + '%', x
					+ (dadosParciais.cargaErs > 99 ? 45 : 50), y + 15);
		}
	}
	maneContext.closePath();
	maneContext.stroke();
}

function ctl_removeControle(controle) {
	
	if (!dadosJogo.drs && controle.tipo == 'Drs') {
		return true;
	}
	
	if (!dadosJogo.ers && controle.tipo == 'Ers') {
		return true;
	}
	if (!dadosJogo.reabastacimento
			&& (controle.tipo == 'CombustivelValor' || controle.tipo == 'Combustivel')) {
		return true;
	}
	
	if (!dadosParciais.box
			&& (controle.tipo == 'Asa' || controle.tipo == 'Pneu'
					|| controle.tipo == 'CombustivelValor' || controle.tipo == 'Combustivel')) {
		return true;
	}
	if (dadosJogo.drs && controle.tipo == 'Asa'
			&& 'chuva.png' != dadosParciais.clima) {
		return true;
	}

	if (!dadosJogo.trocaPneu && controle.tipo == 'Pneu' && dadosJogo.pPneus > 0) {
		return true;
	}
	
	if(!dadosParciais.box &&(controle.tipo == 'confirmaSair' || controle.tipo == 'cancelaSair' || controle.tipo == 'perguntaSair' )){
		return true;
	}
	
	if(!confirmaSair && (controle.tipo == 'confirmaSair' || controle.tipo == 'cancelaSair' )){
		return true;
	}
	
	if(confirmaSair && controle.tipo != 'confirmaSair' && controle.tipo != 'cancelaSair'){
		return true;
	}

	return false;
}

function ctl_gerarControles() {
	controles.push({
		cor : '#babaca',
		valor : 'GIRO_MIN',
		exibir : '1',
		centralizaTexto : true,
		tipo : 'controleMotor',
		width : 40,
		height : 40,
		evalY : '(altura > 480)?(maneCanvas.height - 150):(maneCanvas.height - 100);',
		y : 0,
		x : 10,
		img : motor
	});
	controles.push({
		cor : '#babaca',
		valor : 'GIRO_NOR',
		exibir : '2',
		tipo : 'controleMotor',
		centralizaTexto : true,
		width : 40,
		height : 40,
		evalY : '(altura > 480)?(maneCanvas.height - 150):(maneCanvas.height - 100);',
		y : 0,
		x : 60,
		img : motor
	});
	controles.push({
		cor : '#babaca',
		valor : 'GIRO_MAX',
		exibir : '3',
		tipo : 'controleMotor',
		centralizaTexto : true,
		width : 40,
		height : 40,
		evalY : '(altura > 480)?(maneCanvas.height - 150):(maneCanvas.height - 100);',
		y : 0,
		x : 110,
		img : motor
	});

	controles.push({
		cor : '#babaca',
		valor : 'AGRESSIVO',
		exibir : '3',
		tipo : 'controlePiloto',
		centralizaTexto : true,
		width : 40,
		height : 40,
		evalY : '(altura > 480)?(maneCanvas.height - 150):(maneCanvas.height - 100);',
		y : 0,
		evalX : 'maneCanvas.width - 150;',
		x : 0,
		img : capacete
	});
	controles.push({
		cor : '#babaca',
		valor : 'NORMAL',
		exibir : '2',
		centralizaTexto : true,
		tipo : 'controlePiloto',
		width : 40,
		height : 40,
		evalY : '(altura > 480)?(maneCanvas.height - 150):(maneCanvas.height - 100);',
		y : 0,
		evalX : 'maneCanvas.width - 100;',
		x : 0,
		img : capacete
	});
	controles.push({
		cor : '#babaca',
		valor : 'LENTO',
		exibir : '1',
		centralizaTexto : true,
		tipo : 'controlePiloto',
		width : 40,
		height : 40,
		evalY : '(altura > 480)?(maneCanvas.height - 150):(maneCanvas.height - 100);',
		y : 0,
		evalX : 'maneCanvas.width - 50;',
		x : 0,
		img : capacete
	});
	controles.push({
		cor : '#babaca',
		valor : 'E',
		exibir : 'Ers',
		tipo : 'Ers',
		centralizaTexto : false,
		width : 60,
		height : 40,
		evalY : '(altura > 480)?(maneCanvas.height - 200):(maneCanvas.height - 150);',
		y : 0,
		x : 10
	});
	controles.push({
		cor : '#babaca',
		valor : 'D',
		exibir : 'Drs',
		tipo : 'Drs',
		centralizaTexto : false,
		width : 60,
		height : 40,
		evalY : '(altura > 480)?(maneCanvas.height - 200):(maneCanvas.height - 150);',
		y : 0,
		evalX : 'maneCanvas.width - 70;',
		x : 0
	});

	// Box
	controles.push({
		cor : '#babaca',
		valor : 'BOX',
		exibir : 'BOX',
		tipo : 'Box',
		centralizaTexto : false,
		width : 80,
		height : 40,
		y : 10,
		evalX : '(maneCanvas.width/2 - 40);',
		x : 0
	});

	controles.push({
		cor : '#babaca',
		valor : 'TIPO_PNEU_MOLE',
		exibir : 'M',
		tipo : 'Pneu',
		centralizaTexto : true,
		width : 40,
		height : 40,
		y : 60,
		evalX : '(maneCanvas.width/2 - 80);',
		x : 0,
		img : imgPneuM
	});
	controles.push({
		cor : '#babaca',
		valor : 'TIPO_PNEU_DURO',
		exibir : 'D',
		tipo : 'Pneu',
		centralizaTexto : true,
		width : 40,
		height : 40,
		y : 60,
		evalX : '(maneCanvas.width/2 - 20);',
		x : 0,
		img : imgPneuD
	});
	controles.push({
		cor : '#babaca',
		valor : 'TIPO_PNEU_CHUVA',
		exibir : 'C',
		tipo : 'Pneu',
		centralizaTexto : true,
		width : 40,
		height : 40,
		y : 60,
		evalX : '(maneCanvas.width/2 + 40);',
		x : 0,
		img : imgPneuC
	});

	controles.push({
		cor : '#babaca',
		valor : 'MENOS_ASA',
		exibir : '1',
		tipo : 'Asa',
		centralizaTexto : true,
		width : 40,
		height : 40,
		y : 110,
		evalX : '(maneCanvas.width/2 - 80);',
		x : 0,
		img : menosAsa
	});
	controles.push({
		cor : '#babaca',
		valor : 'ASA_NORMAL',
		exibir : '2',
		tipo : 'Asa',
		centralizaTexto : true,
		width : 40,
		height : 40,
		y : 110,
		evalX : '(maneCanvas.width/2 - 20);',
		x : 0,
		img : normalAsa
	});
	controles.push({
		cor : '#babaca',
		valor : 'MAIS_ASA',
		exibir : '3',
		tipo : 'Asa',
		centralizaTexto : true,
		width : 40,
		height : 40,
		y : 110,
		evalX : '(maneCanvas.width/2 + 40);',
		x : 0,
		img : maisAsa
	});

	controles.push({
		cor : '#babaca',
		valor : '-',
		exibir : '-',
		tipo : 'Combustivel',
		centralizaTexto : true,
		width : 40,
		height : 40,
		y : 160,
		evalX : '(maneCanvas.width/2 - 80);',
		x : 0
	});
	controles.push({
		cor : '#babaca',
		valor : '',
		exibir : '100',
		tipo : 'CombustivelValor',
		centralizaTexto : false,
		width : 60,
		height : 40,
		y : 160,
		evalX : '(maneCanvas.width/2 - 30);',
		x : 0
	});
	controles.push({
		cor : '#babaca',
		valor : '+',
		exibir : '+',
		tipo : 'Combustivel',
		centralizaTexto : true,
		width : 40,
		height : 40,
		y : 160,
		evalX : '(maneCanvas.width/2 + 40);',
		x : 0
	});
	controles.push({
		cor : '#babaca',
		valor : '',
		exibir : lang_text('Sair?'),
		tipo : 'perguntaSair',
		centralizaTexto : false,
		width : 80,
		height : 40,
		y : 210,
		evalX : '(maneCanvas.width/2 - 40);',
		x : 0
	});	
	controles.push({
		cor : '#babaca',
		valor : '',
		exibir : lang_text('Cancela'),
		tipo : 'cancelaSair',
		centralizaTexto : false,
		width : 120,
		height : 40,
		y : 210,
		evalX : '(maneCanvas.width/2 - 140);',
		x : 0
	});
	controles.push({
		cor : '#babaca',
		valor : '',
		exibir : lang_text('Confirma'),
		tipo : 'confirmaSair',
		centralizaTexto : false,
		width : 130,
		height : 40,
		y : 210,
		evalX : '(maneCanvas.width/2 + 20);',
		x : 0
	});	
}
maneCanvas.addEventListener('click',ctl_click, false);

function ctl_click(event) {
	if(!telaCheia){
		try {
			document.getElementById('body').webkitRequestFullScreen(); // Chrome
		} catch (e) {
			try {
				document.getElementById('body').mozRequestFullScreen(); // Firefox
			} catch (e) {
				try {
					document.getElementById('body').requestFullscreen();// Edge
				} catch (e) {
				}
			}
		}
		telaCheia = true;
	}
	var x = event.pageX;
	var y = event.pageY;

	var clickControle = false;
	for (var i = 0; i < controles.length; i++) {
		var controle = controles[i];
		var contains = (y > controle.y && y < controle.y + controle.height
		&& x > controle.x
		&& x < controle.x + controle.width);
		if (!contains){
			continue;
		}
		if (ctl_removeControle(controle)) {
			continue;
		}
		clickControle = true;
		if (controle.tipo == 'controleMotor') {
			rest_potenciaMotor(controle.valor);
			return;
		}
		if (controle.tipo == 'controlePiloto') {
			rest_agressividadePiloto(controle.valor);
			return;
		}
		if (controle.tipo == 'Ers') {
			rest_ers();
			return;
		}
		if (controle.tipo == 'Drs') {
			rest_drs();
			return;
		}
		if (controle.tipo == 'Box') {
			rest_boxPiloto(!dadosParciais.box,
					dadosParciais.tpPneusBox,
					dadosParciais.combustBox,
					dadosParciais.asaBox);
			return;
		}
		if (controle.tipo == 'Pneu') {
			rest_boxPiloto(true, controle.valor,
					dadosParciais.combustBox,
					dadosParciais.asaBox);
			return;
		}
		if (controle.tipo == 'Asa') {
			rest_boxPiloto(true, dadosParciais.tpPneusBox,
					dadosParciais.combustBox,
					controle.valor);
			return;
		}
		if (controle.tipo == 'perguntaSair') {
			confirmaSair = true;
			return;
		}
		if (controle.tipo == 'cancelaSair') {
			confirmaSair = false;
			return;
		}
		if (controle.tipo == 'confirmaSair') {
			cpu_sair();
			return;
		}
		if (controle.tipo == 'Combustivel') {
			if (controle.valor == '+') {
				rest_boxPiloto(true,
						dadosParciais.tpPneusBox,
						dadosParciais.combustBox + 10,
						dadosParciais.asaBox);
				return;
			} else if (controle.valor == '-') {
				rest_boxPiloto(true,
						dadosParciais.tpPneusBox,
						dadosParciais.combustBox - 10,
						dadosParciais.asaBox);
				return;
			}
		}
	}
	if (!clickControle) {
		ctl_mudaTracadoPiloto(event);
	}
}
