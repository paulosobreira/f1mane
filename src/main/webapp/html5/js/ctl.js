/**

 * Controle comandos no jogo
 */
var controles = [];
var centroX;
var cargaErs;
var telaCheia = false;
var corFundo = "rgba(255, 255, 255, 0.6)";
var corAmarelo = "rgba(255, 255, 0, 0.6)";
var corBabaca = "rgba(186, 186, 202, 0.6)";
var corVermelho = "rgba(255, 0, 0, 0.6)";
var confirmaSair = false;
var dirZoom = '-';
var ctl_showFps = false;


function ctl_desenha() {
	ctlCanvas.width = window.innerWidth;
	ctlCanvas.height = window.innerHeight;
	ctlContext.clearRect(0, 0, ctlCanvas.width, ctlCanvas.height);
	centroX = window.innerWidth / 2;
	centroY = window.innerHeight / 2;
	ctl_desenhaInfoSegundosParaIniciar();
	ctl_desenhaQualificacao();
	ctl_desenhaInfoEsquerda();
	ctl_desenhaInfoDireita();
	ctl_desenhaInfoBaixo();
	ctl_desenhaInfoAsa();
	ctl_desenhaControles();
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
	
	ctlContext.fillStyle = corFundo
	ctlContext.font = '24px sans-serif';
	var ranking = lang_text('ranking');
	var laruraTxt = ctlContext.measureText(ranking).width + 30;
	ctlContext.fillRect(centroX-(laruraTxt/2), y, laruraTxt, 40);
	ctlContext.fillStyle = "black"
	ctlContext.fillText(ranking, centroX-(laruraTxt/2)+15, y + 28);

	y += 60;
		
	for (var i = 0; i < dadosJogo.pilotos.length; i++) {
		if(i%2==0){
			x = centroX-140;
		}else{
			x = centroX+30;
		}
		var piloto = dadosJogo.pilotos[i];
		ctlContext.beginPath();
		ctlContext.font = '14px sans-serif';
		
		var nmPiloto = piloto.nomeAbreviado;
		nmPiloto = (i + 1) + ' ' + nmPiloto;
		var tempo = piloto.tempoVoltaQualificacao;

		ctlContext.fillStyle = piloto.carro.cor1Hex;
		ctlContext.fillRect(x - 10, y, 5, 20);
// ctlContext.fillStyle = piloto.carro.cor2Hex;
// ctlContext.fillRect(x - 7, y, 3, 20);
		
		ctlContext.fillStyle = corFundo
		ctlContext.fillRect(x - 5, y, 50, 20);
		ctlContext.fillStyle = "black"
		ctlContext.fillText(nmPiloto, x, y + 15);
		
		if (idPilotoSelecionado == piloto.id) {
			ctlContext.fillStyle = '#00ff00';
			ctlContext.fillRect(x - 10, y+20, 130, 5);
		} else if (piloto.jogadorHumano) {
			ctlContext.fillStyle = '#ffff00';
			ctlContext.fillRect(x - 10, y+20, 130, 5);
		}
		
		ctlContext.fillStyle = corFundo
		ctlContext.fillRect(x + 50, y, 70, 20);
		ctlContext.fillStyle = "black"
		ctlContext.fillText(tempo, x + 60, y + 15);
		
		ctlContext.closePath();
		ctlContext.stroke();
		
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
	ctlContext.fillStyle = corFundo
	var iniciaEm = lang_text('iniciaEm')+' : '+dadosJogo.segundosParaIniciar;
	ctlContext.font = '24px sans-serif';
	var laruraTxt = ctlContext.measureText(iniciaEm).width+30;
	ctlContext.fillRect(centroX-(laruraTxt/2), y, laruraTxt, 40);
	ctlContext.fillStyle = "black"
	ctlContext.fillText(iniciaEm, centroX-(laruraTxt/2)+15, y + 28);

	
	y+=70;
	ctlContext.fillStyle = corFundo
	var msgCarregando = lang_text('msgCarregando');
	ctlContext.font = '14px sans-serif';
	laruraTxt = ctlContext.measureText(msgCarregando).width+30;
	ctlContext.fillRect(centroX-(laruraTxt/2), y, laruraTxt, 20);
	ctlContext.fillStyle = "black"
	ctlContext.fillText(msgCarregando, centroX-(laruraTxt/2)+15, y + 14);
	
}

function ctl_desenhaControles() {
	if (!dadosParciais) {
		return;
	}
	for (var i = 0; i < controles.length; i++) {
		var controle = controles[i];
		if (ctl_removeControle(controle)) {
			continue;
		}
		ctlContext.beginPath();
		if (controle.evalY) {
			controle.y = eval(controle.evalY);
		}
		if (controle.evalX) {
			controle.x = eval(controle.evalX);
		}

		var cor = controle.cor;

		if (controle.tipo == 'controleMotor') {
			if (dadosParciais.giro == 1 && controle.valor == 'GIRO_MIN') {
				cor = '#00ff00';
			} else if (dadosParciais.giro == 5
					&& controle.valor == 'GIRO_NOR') {
				cor = '#ffff00';
			} else if (dadosParciais.giro == 9
					&& controle.valor == 'GIRO_MAX') {
				cor = '#ff0000';
			}
		}
		if (controle.tipo == 'controlePiloto') {
			if (dadosParciais.modoPilotar == 'LENTO'
					&& controle.valor == 'LENTO') {
				cor = '#00ff00';
			} else if (dadosParciais.modoPilotar == 'NORMAL'
					&& controle.valor == 'NORMAL') {
				cor = '#ffff00';
			} else if (dadosParciais.modoPilotar == 'AGRESSIVO'
					&& controle.valor == 'AGRESSIVO') {
				cor = '#ff0000';
			}
		}

		if (controle.tipo == 'Ers' && dadosJogo.ers) {
			if (!cargaErs) {
				cargaErs = dadosParciais.cargaErs;
			}
			if (dadosParciais.cargaErs != cargaErs) {
				cargaErs = dadosParciais.cargaErs;
				cor = '#00ff00';
			}
		}
		
		if (controle.tipo == 'fps') {
			controle.exibir = fps.frameRate() +' FPS';
		}
		

		if (controle.tipo == 'Drs' && dadosJogo.drs
				&& dadosParciais.asa == 'MENOS_ASA') {
			cor = '#00ff00';
		}else if (controle.tipo == 'Drs' && dadosParciais.podeUsarDRS) {
		    cor = '#ffff00';
		}

		if (controle.tipo == 'Pneu'
				&& dadosParciais.tpPneusBox == controle.valor) {
			cor = '#ffff00';
		}

		if (controle.tipo == 'Asa'
				&& dadosParciais.asaBox == controle.valor) {
			cor = '#ffff00';
		}
		
		if (controle.tipo == 'Box' && dadosParciais.box) {
			cor = '#ffff00';
		}

		if (controle.tipo == 'CombustivelValor') {
			controle.valor = dadosParciais.combustBox;
			controle.exibir = dadosParciais.combustBox;
		}

		ctlContext.font = '30px sans-serif';
		var ajusteAluraText = 10;
		if (controle.tipo == 'perguntaSair') {
			ctlContext.font = '14px sans-serif';
			cor = '#ff0000';
			ajusteAluraText = 5;
		}
		ctlContext.fillStyle = "black"
	    var verImg = cor != controle.cor || (controle.tipo != 'controlePiloto' && controle.tipo != 'controleMotor');				
		if (controle.img && verImg) {
			ctlContext.beginPath();
			ctlContext.strokeStyle = cor;
			ctlContext.rect(controle.x, controle.y,
					controle.width + 5, controle.height + 5);
			ctlContext.closePath();
			ctlContext.stroke();
			desenha(ctlContext,controle.img, controle.x, controle.y);
		} else {
			ctlContext.fillStyle = controle.cor;
			if(controle.larguraTexto){
				var laruraTxt = ctlContext.measureText(controle.exibir).width + 10;
				ctlContext.strokeStyle =  controle.cor;
				ctlContext.rect(controle.x, controle.y, laruraTxt,	controle.height);
				if(cor!=controle.cor){
					ctlContext.beginPath();
					ctlContext.strokeStyle = cor;
					ctlContext.rect(controle.x, controle.y, laruraTxt,	controle.height);
					ctlContext.closePath();
					ctlContext.stroke();
				}
			}else{
				ctlContext.strokeStyle =  controle.cor;
				ctlContext.rect(controle.x, controle.y, controle.width, controle.height);
				if(cor!=controle.cor){
					ctlContext.beginPath();
					ctlContext.strokeStyle = cor;
					ctlContext.rect(controle.x, controle.y, controle.width,	controle.height);	
					ctlContext.closePath();
					ctlContext.stroke();
				}
			}

			if (controle.centralizaTexto) {
			    var x = controle.x + (controle.width / 2) - 10;
			    var y = controle.y + (controle.height / 2) + ajusteAluraText;
			    ctlContext.fillStyle = corBabaca;
				ctlContext.fillText(controle.exibir, x+1, y+1);
			    ctlContext.fillStyle = "black";
				ctlContext.fillText(controle.exibir, x, y);
			} else {
			    var x = controle.x + 5;
			    var y = controle.y + (controle.height / 2) + ajusteAluraText;
			    ctlContext.fillStyle = corBabaca;
                ctlContext.fillText(controle.exibir,x+1 ,y+1);
			    ctlContext.fillStyle = "black";
				ctlContext.fillText(controle.exibir,x ,y);
			}
		}
		ctlContext.closePath();
		ctlContext.stroke();
	}
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

//	console.log('rest_tracadoPiloto(mudar); ' + mudar)
	if (menor != 100000) {
		rest_tracadoPiloto(mudar);
	}
}

function ctl_desenhaInfo() {
	$('#info').css('position', 'absolute');
	if(window.innerHeight>window.innerWidth){
		$('#info').css('top', (window.innerHeight-100)+'px');
	}else{
		$('#info').css('top', (window.innerHeight-40)+'px');
	}
	if(window.innerHeight<window.innerWidth){
		$('#info').css('left',  centroX - ($('#info').width()/2)+'px');
	}else{
		$('#info').css('left', '10px');
		$('#info').css('margin-right', '10px');
	}
	$('#info').css('font-family', 'sans-serif');
	if($('#info').html().indexOf('table')>0){
		$('#info').css('background-color', corFundo);
		$('#info').css('font-size', '11px');
		if(window.innerHeight<window.innerWidth){
			$('#info').css('top', (window.innerHeight-90)+'px');
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
	$('#imgJog1').hide();
	$('#imgJog2').hide();
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
	if(dadosJogo.drs && dadosParciais.clima != "png/chuva.png"){
		return
	}
	
	var y =(window.innerHeight > window.innerWidth)?(window.innerHeight - 200):(window.innerHeight - 150);
	var x = window.innerWidth - 70;
	var img;
	if(dadosParciais.asa == 'MAIS_ASA'){
		img = maisAsa;
	}else if(dadosParciais.asa == 'ASA_NORMAL'){
		img =  normalAsa;
	}else if(dadosParciais.asa == 'MENOS_ASA'){
		img = menosAsa;
	}
	ctlContext.strokeStyle = corBabaca;
	ctlContext.rect(x, y, img.width + 5, img.height + 5);
	desenha(ctlContext,img, x, y);
}


function ctl_desenhaInfoCarros() {
	var posicaoPilotos = dadosParciais.posisPack;
	var img1, img2;
	var imgCap1, imgCap2;
	var imgPneu1, imgPneu2;
	var idPiloto1, idPiloto2;
	var noFrente , noAtras; 
	var imgJog1 , imgJog2
	
	var diff;
	
	var ultimo = posicaoPilotos.posis.length - 1 - pilotosDnfMap.size;

	if (posicaoCentraliza == 0) {
		img1 = carrosLadoImgMap.get(posicaoPilotos.posis[0].idPiloto);
		img2 = carrosLadoImgMap.get(posicaoPilotos.posis[1].idPiloto);
		idPiloto1 = posicaoPilotos.posis[0].idPiloto;
		idPiloto2 = posicaoPilotos.posis[1].idPiloto;
		imgCap1 = capaceteImgMap.get(posicaoPilotos.posis[0].idPiloto);
		imgCap2 = capaceteImgMap.get(posicaoPilotos.posis[1].idPiloto);
		imgJog1 = jogadorImgMap.get(posicaoPilotos.posis[0].idPiloto);
		imgJog2 = jogadorImgMap.get(posicaoPilotos.posis[1].idPiloto);
		noFrente = mapaIdNos.get(posicaoPilotos.posis[0].idNo);
		noAtras = mapaIdNos.get(posicaoPilotos.posis[1].idNo);
		var ptsFrente = noFrente.index;  
		var ptsAtras = noAtras.index;
		if(ptsFrente<ptsAtras){
			ptsFrente += circuito.pistaFull.length;
		}
		diff = formatarDiferenca(ptsFrente - ptsAtras);
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
	} else if (posicaoCentraliza == ultimo) {
		img1 = carrosLadoImgMap
				.get(posicaoPilotos.posis[ultimo - 1].idPiloto);
		img2 = carrosLadoImgMap
				.get(posicaoPilotos.posis[ultimo].idPiloto);
		idPiloto1 = posicaoPilotos.posis[ultimo - 1].idPiloto;
		idPiloto2 = posicaoPilotos.posis[ultimo].idPiloto;

		imgJog1 = jogadorImgMap
				.get(posicaoPilotos.posis[ultimo - 1].idPiloto);
		imgJog2 = jogadorImgMap
				.get(posicaoPilotos.posis[ultimo].idPiloto);
		
		imgCap1 = capaceteImgMap
				.get(posicaoPilotos.posis[ultimo - 1].idPiloto);
		imgCap2 = capaceteImgMap
				.get(posicaoPilotos.posis[ultimo].idPiloto);		
		
		noFrente = mapaIdNos.get(posicaoPilotos.posis[ultimo - 1].idNo);
		noAtras = mapaIdNos.get(posicaoPilotos.posis[ultimo].idNo);
		var ptsFrente = noFrente.index;  
		var ptsAtras = noAtras.index;
		if(ptsFrente<ptsAtras){
			ptsFrente += circuito.pistaFull.length;
		}
		diff = formatarDiferenca(ptsFrente - ptsAtras);
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
		if(pilotoAtras==null){
			return;
		}
		noFrente = mapaIdNos.get(pilotoFrete.idNo);
		noAtras = mapaIdNos.get(pilotoAtras.idNo);
		var noPsel = mapaIdNos.get(posicaoPilotos.posis[posicaoCentraliza].idNo);
		var ptsFrente = noFrente.index;  
		var ptsAtras = noAtras.index;
		var pSelPts = noPsel.index;
		if(pSelPts<ptsAtras){
			pSelPts += circuito.pistaFull.length;
		}
		if(ptsFrente<ptsAtras || ptsFrente<pSelPts){
			ptsFrente += circuito.pistaFull.length;
		}
		var diffFrente = ptsFrente - pSelPts;
		var diffAtras = pSelPts - ptsAtras;
		if (diffFrente < diffAtras) {
			diff = formatarDiferenca(diffFrente);
			img1 = carrosLadoImgMap.get(pilotoFrete.idPiloto);
			img2 = carrosLadoImgMap
					.get(posicaoPilotos.posis[posicaoCentraliza].idPiloto);
			
			idPiloto1 = pilotoFrete.idPiloto;
			idPiloto2 = posicaoPilotos.posis[posicaoCentraliza].idPiloto;
			imgCap1 = capaceteImgMap.get(pilotoFrete.idPiloto);
			imgCap2 = capaceteImgMap
					.get(posicaoPilotos.posis[posicaoCentraliza].idPiloto);
			
			imgJog1 = jogadorImgMap.get(pilotoFrete.idPiloto);
			imgJog2 = jogadorImgMap
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
			diff = formatarDiferenca(diffAtras);
			img1 = carrosLadoImgMap
					.get(posicaoPilotos.posis[posicaoCentraliza].idPiloto);
			img2 = carrosLadoImgMap.get(pilotoAtras.idPiloto);
			idPiloto1 = posicaoPilotos.posis[posicaoCentraliza].idPiloto;
			idPiloto2 = pilotoAtras.idPiloto;
			
			imgCap1 = capaceteImgMap
					.get(posicaoPilotos.posis[posicaoCentraliza].idPiloto);
			imgCap2 = capaceteImgMap.get(pilotoAtras.idPiloto);
			
			imgJog1 = jogadorImgMap
					.get(posicaoPilotos.posis[posicaoCentraliza].idPiloto);
			imgJog2 = jogadorImgMap.get(pilotoAtras.idPiloto);			
			
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
		
	}
	if (diff) {
        if(noFrente!=null && noFrente.box){
            diff = 'BOX';
        }
        if(noAtras!=null && noAtras.box){
            diff = 'BOX';
        }
		ctlContext.beginPath();
		ctlContext.fillStyle = corFundo
		ctlContext.font = '14px sans-serif';
		ctlContext.fillRect(centroX - 25, window.innerHeight - 40,  ctlContext.measureText(diff).width+10, 20);
		ctlContext.fillStyle = "black";
		ctlContext.fillText(diff, centroX - 20, window.innerHeight - 25);
		ctlContext.closePath();
		ctlContext.stroke();
	}
	if (img1 && desenhaImagens) {
		desenha(ctlContext,img1, centroX - img1.width - 40, window.innerHeight
				- img1.height - 10);
		if(idPiloto1==idPilotoSelecionado){
			ctl_problemasCarrro(img1 , centroX, idPiloto1 , 1);
		}
		if (imgPneu1) {
			var xCarro,yCarro,xCapacete,yCapacete;

			if(window.innerWidth<450){
				xCarro = centroX - imgPneu1.width - 40;
				yCarro =  window.innerHeight - (imgPneu1.height*2) - 10
			}else{
				xCarro = centroX - imgPneu1.width - img1.width - 40;
				yCarro =  window.innerHeight - imgPneu1.height - 10;
			}
			desenha(ctlContext,imgPneu1,xCarro, yCarro);
			if (imgCap1) {
				if(window.innerWidth<450){
					xCapacete = centroX - imgCap1.width	- 30 - (imgPneu1.width);
					yCapacete = yCarro;
				}else{
					xCapacete = centroX - imgCap1.width	- img1.width - 45 - (imgPneu1.width / 2);
					yCapacete = yCarro;
				}
				desenha(ctlContext,imgCap1, xCapacete, yCapacete);
			}
			if (imgJog1!=null) {
				$('#imgJog1').show();
				$('#imgJog1').css('position', 'absolute');
				$('#imgJog1').attr('src',imgJog1);
				$('#imgJog1').css('top',   (yCarro)+'px');
			    $('#imgJog1').css('left',  (xCapacete-30)+'px');
			}
		}
	}
	if (img2 && desenhaImagens) {
		desenha(ctlContext,img2, centroX + 40, window.innerHeight - img2.height - 10);
		if(idPiloto2==idPilotoSelecionado){
			ctl_problemasCarrro(img2 , centroX, idPiloto2 , 2);
		}
		if (imgPneu2) {
			var xCarro,yCarro,xCapacete,yCapacete;
			if(window.innerWidth<450){
				xCarro = centroX + 40;
				yCarro =  window.innerHeight - (imgPneu2.height*2) - 10
			}else{
				xCarro = centroX + 40 + img2.width;
				yCarro =  window.innerHeight - imgPneu2.height - 10;
			}
			desenha(ctlContext,imgPneu2, xCarro , yCarro);
			if (imgCap2) {
				if(window.innerWidth<450){
					xCapacete = centroX + 30 + (imgPneu2.width);
					yCapacete = yCarro;
				}else{
					xCapacete = centroX + 45 + img2.width + (imgPneu2.width / 2);
					yCapacete = yCarro;
				}
				desenha(ctlContext,imgCap2, xCapacete , yCapacete);
			}
			
			if (imgJog2!=null) {
				$('#imgJog2').show();
				$('#imgJog2').css('position', 'absolute');
				$('#imgJog2').attr('src',imgJog2);
				$('#imgJog2').css('top',   (yCarro)+'px');
			    $('#imgJog2').css('left',  (xCapacete+30)+'px');
			}
		}
	}
}

function ctl_problemasCarrro(img , x, idPiloto , posicao){
	var alertaMotorDnf;
	
	var alertaAerefolio = dadosParciais.alertaAerefolio;
	
	if(idPilotoSelecionado!=idPiloto){
		alertaMotorDnf = ("PANE_SECA" == dadosParciais.dano) || (dadosParciais.dano ==  "EXPLODIU_MOTOR");
	}
	var perdeuAerefolio = pilotosAereofolioMap.get(idPiloto);
	var dnf = pilotosDnfMap.get(idPiloto);
	
	if(posicao == 1){
		ctlContext.beginPath();
		if(perdeuAerefolio){
			ctlContext.fillStyle = corVermelho;
			ctlContext.fillRect(x - img.width - 35, window.innerHeight - img.height + 10 , 20, 20);
		}else if(alertaAerefolio){
			ctlContext.fillStyle = corAmarelo;
			ctlContext.fillRect(x - img.width - 35, window.innerHeight - img.height + 10 , 20, 20);
		}
		
		if(alertaMotorDnf){
			ctlContext.fillStyle = corVermelho;
			ctlContext.fillRect(x - (img.width/2) - 35, window.innerHeight - img.height - 10 , 50, 30);
		}else if(dadosParciais.alertaMotor){
			ctlContext.fillStyle = corAmarelo;
			ctlContext.fillRect(x - (img.width/2) - 35, window.innerHeight - img.height - 10 , 50, 30);
		}
		if(dnf){
			ctlContext.fillStyle = corVermelho;
			ctlContext.fillRect(x - img.width - 40, window.innerHeight	- img.height - 10, img.width, img.height);
		}
		ctlContext.closePath();
		ctlContext.stroke();
	}else{
		ctlContext.beginPath();
		if(perdeuAerefolio){
			ctlContext.fillStyle = corVermelho;
			ctlContext.fillRect(x + 45, window.innerHeight - img.height + 10 , 20, 20);
		}else if(alertaAerefolio){
			ctlContext.fillStyle = corAmarelo;
			ctlContext.fillRect(x + 45, window.innerHeight - img.height + 10 , 20, 20);
		}
		
		if(alertaMotorDnf){
			ctlContext.fillStyle = corVermelho;
			ctlContext.fillRect(x  + (img.width/2) + 45, window.innerHeight - img.height - 10 , 50, 30);
		}else if(dadosParciais.alertaMotor){
			ctlContext.fillStyle = corAmarelo;
			ctlContext.fillRect(x  + (img.width/2) + 45, window.innerHeight - img.height - 10 , 50, 30);
		}
		
		if(dnf){
			ctlContext.fillStyle = corVermelho;
			ctlContext.fillRect(x + 40, window.innerHeight - img.height - 10, img.width, img.height);
		}
		ctlContext.closePath();
		ctlContext.stroke();
	}
		
}

function ctl_desenhaInfoDireita() {
	if (!dadosParciais) {
		return;
	}
	if(confirmaSair){
		return;
	}
	ctlContext.beginPath();

	var x = window.innerWidth - 120;
	var y = 10;
	
	if(dadosParciais.box){
		y = 30;
	}

	if (dadosParciais.melhorVolta && (window.innerHeight > window.innerWidth || !alternador)) {
		ctlContext.fillStyle = corFundo
		ctlContext.fillRect(x, y, 110, 20);
		ctlContext.font = '14px sans-serif';
		ctlContext.fillStyle = "black"
		ctlContext.fillText(lang_text('278'), x + 5, y + 15);
		ctlContext.fillText(formatarTempo(dadosParciais.melhorVolta), x + 53,
				y + 15);
		y += 30;
	}

	if (dadosParciais.ultima1 && dadosParciais.pVolta>0 && (window.innerHeight > window.innerWidth || !alternador)) {
		ctlContext.fillStyle = corFundo
		ctlContext.fillRect(x, y, 110, 20);
		ctlContext.font = '14px sans-serif';
		ctlContext.fillStyle = "black"
		ctlContext.fillText(lang_text('082').substring(0,3) + ' '
				+ (dadosParciais.pVolta) + ' '
				+ formatarTempo(dadosParciais.ultima1), x + 5, y + 15);

		y += 30;

		if (dadosParciais.ultima2 && dadosParciais.pVolta>1) {
			ctlContext.fillStyle = corFundo
			ctlContext.fillRect(x, y, 110, 20);
			ctlContext.font = '14px sans-serif';
			ctlContext.fillStyle = "black"
			ctlContext.fillText(lang_text('082').substring(0,3) + ' '
					+ (dadosParciais.pVolta - 1) + ' '
					+ formatarTempo(dadosParciais.ultima2), x + 5, y + 15);
			y += 30;
		}

		if (dadosParciais.ultima3 && dadosParciais.pVolta>2) {
			ctlContext.fillStyle = corFundo
			ctlContext.fillRect(x, y, 110, 20);
			ctlContext.font = '14px sans-serif';
			ctlContext.fillStyle = "black"
			ctlContext.fillText(lang_text('082').substring(0,3) + ' '
					+ (dadosParciais.pVolta - 2) + ' '
					+ formatarTempo(dadosParciais.ultima3), x + 5, y + 15);
			y += 30;
		}

		if (dadosParciais.ultima4 && dadosParciais.pVolta>3) {
			ctlContext.fillStyle = corFundo
			ctlContext.fillRect(x, y, 110, 20);
			ctlContext.font = '14px sans-serif';
			ctlContext.fillStyle = "black"
			ctlContext.fillText(lang_text('082').substring(0,3) + ' '
					+ (dadosParciais.pVolta - 3) + ' '
					+ formatarTempo(dadosParciais.ultima4), x + 5, y + 15);
			y += 30;
		}

	}

	var posicaoPilotos = dadosParciais.posisPack;
	x+=50;
	var larg = 55;
	var posicaoDesenhada = new Map();
	if (posicaoPilotos
			&& (window.innerHeight > window.innerWidth || (alternador || !dadosParciais.melhorVolta))) {
		var piloto = posicaoPilotos.posis[0];
		var nomePiloto = pilotosMap.get(piloto.idPiloto).nomeAbreviado;
		ctlContext.beginPath();
		
		ctlContext.fillStyle = pilotosMap.get(piloto.idPiloto).carro.cor1Hex;
		ctlContext.fillRect(x, y, 5, 20);
		
		ctlContext.fillStyle = corFundo
		ctlContext.fillRect(x + 5, y, larg, 20);
		ctlContext.font = '14px sans-serif';
		ctlContext.fillStyle = "black"
		var posicao = pilotosBandeirada.get(piloto.idPiloto);
		if(posicao==null){
			posicao = '1';
		}
		posicaoDesenhada.set(posicao,posicao);
		ctlContext.fillText(posicao+' ' + nomePiloto, x + 10, y + 15);
		if (idPilotoSelecionado == piloto.idPiloto) {
			ctlContext.strokeStyle = '#00ff00';
			ctlContext.rect(x + 5, y, larg, 20);
		} else if (piloto.humano) {
			ctlContext.strokeStyle = '#ffff00';
			ctlContext.rect(x + 5, y, larg, 20);
		}
		ctlContext.closePath();
		ctlContext.stroke();

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
			ctlContext.beginPath();
			var piloto = posicaoPilotos.posis[i];
			var nomePiloto = pilotosMap.get(piloto.idPiloto).nomeAbreviado;
		
			ctlContext.fillStyle = pilotosMap.get(piloto.idPiloto).carro.cor1Hex;
			ctlContext.fillRect(x, y, 5, 20);
			
			ctlContext.fillStyle = corFundo
			ctlContext.fillRect(x + 5, y, larg, 20);
			ctlContext.font = '14px sans-serif';
			ctlContext.fillStyle = "black"
			var posicao = pilotosBandeirada.get(piloto.idPiloto);
			if(posicao==null){
				posicao = (i + 1);
			}
			if(posicaoDesenhada.get(posicao)==null){
				posicaoDesenhada.set(posicao,posicao);
			}else{
				posicao=null;
			}
			if(posicao==null){
				posicao = '?';
			}
			ctlContext.fillText(posicao + ' ' + nomePiloto, x + 10, y + 15);
			if (idPilotoSelecionado == piloto.idPiloto) {
				ctlContext.strokeStyle = '#00ff00';
				ctlContext.rect(x + 5, y, larg, 20);
			} else if (piloto.humano) {
				ctlContext.strokeStyle = '#ffff00';
				ctlContext.rect(x + 5, y, larg, 20);
			}
			y += 30;
			ctlContext.closePath();
			ctlContext.stroke();
		}
	}
	ctlContext.closePath();
	ctlContext.stroke();
}

function ctl_desenhaInfoEsquerda() {
	if (!dadosParciais) {
		return;
	}
	if(confirmaSair){
		return;
	}
	ctlContext.beginPath();

	var x = 10;
	var y = 10;

	if (window.innerHeight > window.innerWidth || !alternador) {

		ctlContext.fillStyle = corFundo
		ctlContext.font = '14px sans-serif';
		var circText = dadosJogo.nomeCircuito;
		ctlContext.fillRect(x, y, ctlContext.measureText(circText).width+10, 20);
		ctlContext.fillStyle = "black"
		ctlContext.fillText(circText, x + 5, y + 15);

		
		y += 30;

		if (dadosParciais.melhorVoltaCorrida) {
			ctlContext.fillStyle = corFundo
			ctlContext.fillRect(x, y, 120, 20);
			ctlContext.font = '14px sans-serif';
			ctlContext.fillStyle = "black"
			ctlContext.fillText(lang_text('corrida'), x + 5, y + 15);
			ctlContext.fillText(
					formatarTempo(dadosParciais.melhorVoltaCorrida), x + 60,
					y + 15);
			y += 30;
		}

		ctlContext.fillStyle = corFundo
		ctlContext.fillRect(x, y, 94, 20);
		ctlContext.font = '14px sans-serif';
	    ctlContext.fillStyle = "black"
    	ctlContext.fillText(lang_text('voltas') , x + 5, y + 15);
		var tVoltas = ctlContext.measureText(' '+dadosParciais.voltaAtual + '/' + dadosJogo.numeroVotas).width;
		ctlContext.fillText(' '+dadosParciais.voltaAtual + '/' + dadosJogo.numeroVotas, x + (90-tVoltas), y + 15);   	

        y += 30;
        
		ctlContext.fillStyle = corFundo
		ctlContext.fillRect(x, y, 94, 20);
		ctlContext.font = '14px sans-serif';
		ctlContext.fillStyle = "black"
		var climaDesc = '';
		if (dadosParciais.clima == "png/sol.png") {
			climaDesc = "Ensolarado"
		}
		if (dadosParciais.clima == "png/nublado.png") {
			climaDesc = "Nublado"
		}
		if (dadosParciais.clima == "png/chuva.png") {
			climaDesc = "Chovendo"
		}
		ctlContext.fillText(lang_text(climaDesc), x + 5, y + 15);

		y += 30;

		ctlContext.fillStyle = corFundo
		ctlContext.fillRect(x, y, 94, 20);
		ctlContext.font = '14px sans-serif';
		ctlContext.fillStyle = "black"
		if (pitLane) {
			ctlContext.fillText('PitLane', x + 5, y + 15);
		}else if(dadosParciais.posisPack.safetyNoId != 0){
			ctlContext.fillText('SafetyCar', x + 5, y + 15);
		}	else {
			ctlContext.fillText('~' + dadosParciais.velocidade + ' Km/h',
					x + 5, y + 15);
		}
		if (pitLane || dadosParciais.posisPack.safetyNoId != 0) {
			ctlContext.beginPath();
			ctlContext.strokeStyle = '#ffff00';
			ctlContext.rect(x, y, 94, 20);
			ctlContext.closePath();
			ctlContext.stroke();			
		}

		y += 30;

	}

	if (window.innerHeight > window.innerWidth || alternador) {

		ctlContext.fillStyle = corFundo
		ctlContext.fillRect(x, y, 94, 20);
		ctlContext.font = '14px sans-serif';
		ctlContext.fillStyle = "black"
		ctlContext.fillText(lang_text('215'), x + 5, y + 15);
		var tCombust = ctlContext.measureText(' '+dadosParciais.pCombust + '%').width;
		ctlContext.fillText(' '+dadosParciais.pCombust + '%', x + (90-tCombust), y + 15);

		if (dadosParciais.pCombust > 10 && dadosParciais.pCombust < 20)  {
			ctlContext.beginPath();
			ctlContext.strokeStyle = '#ffff00';
			ctlContext.rect(x, y, 94, 20);
			ctlContext.closePath();
			ctlContext.stroke();			
		}
		
		if (dadosParciais.pCombust <= 10) {
			ctlContext.beginPath();
			ctlContext.strokeStyle = '#ff0000';
			ctlContext.rect(x, y, 94, 20);
			ctlContext.closePath();
			ctlContext.stroke();			
		}
		
		

		y += 30;

		ctlContext.fillStyle = corFundo
		ctlContext.fillRect(x, y, 94, 20);
		ctlContext.font = '14px sans-serif';
		ctlContext.fillStyle = "black"
		ctlContext.fillText(lang_text('216'), x + 5, y + 15);
		var tPneus = ctlContext.measureText(' '+dadosParciais.pPneus + '%').width;
		ctlContext.fillText(' '+dadosParciais.pPneus + '%', x + (90-tPneus), y + 15);
		
		if (dadosParciais.pPneus > 10 && dadosParciais.pPneus < 20 ) {
			ctlContext.beginPath();
			ctlContext.strokeStyle = '#ffff00';
			ctlContext.rect(x, y, 94, 20);
			ctlContext.closePath();
			ctlContext.stroke();			
		}

		if (dadosParciais.pPneus <= 10) {
			ctlContext.beginPath();
			ctlContext.strokeStyle = '#ff0000';
			ctlContext.rect(x, y, 94, 20);
			ctlContext.closePath();
			ctlContext.stroke();			
		}

		y += 30;

		ctlContext.fillStyle = corFundo
		ctlContext.fillRect(x, y, 94, 20);
		ctlContext.font = '14px sans-serif';
		ctlContext.fillStyle = "black"
		ctlContext.fillText(lang_text('217'), x + 5, y + 15);
		var tMotor = ctlContext.measureText(' '+dadosParciais.pMotor + '%').width;
		ctlContext.fillText(' '+dadosParciais.pMotor + '%', x + (90-tMotor), y + 15);

		if (dadosParciais.pMotor > 10 && dadosParciais.pMotor <20) {
			ctlContext.beginPath();
			ctlContext.strokeStyle = '#ffff00';
			ctlContext.rect(x, y, 94, 20);
			ctlContext.closePath();
			ctlContext.stroke();
		}		
		
		if (dadosParciais.pMotor <= 10) {
			ctlContext.beginPath();
			ctlContext.strokeStyle = '#ff0000';
			ctlContext.rect(x, y, 94, 20);
			ctlContext.closePath();
			ctlContext.stroke();
		}
		
		if (dadosParciais.alertaMotor) {
			ctlContext.beginPath();
			ctlContext.strokeStyle = '#ffff00';
			ctlContext.rect(x+2, y+2, 90,16);
			ctlContext.closePath();
			ctlContext.stroke();
		}

		y += 30;

		ctlContext.fillStyle = corFundo
		ctlContext.fillRect(x, y, 94, 20);
		ctlContext.font = '14px sans-serif';
		ctlContext.fillStyle = "black"
		ctlContext.fillText(lang_text('153') , x + 5, y + 15);
		var tStress = ctlContext.measureText(' '+dadosParciais.stress + '%').width;
		ctlContext.fillText(' '+dadosParciais.stress + '%', x + (90-tStress), y + 15);

		
		if (dadosParciais.stress > 90) {
			ctlContext.beginPath();
			ctlContext.strokeStyle = '#ff0000';
			ctlContext.rect(x, y, 94, 20);
			ctlContext.closePath();
			ctlContext.stroke();
		}else if (dadosParciais.stress > 70) {
			ctlContext.beginPath();
			ctlContext.strokeStyle = '#ffff00';
			ctlContext.rect(x, y, 94, 20);
			ctlContext.closePath();
			ctlContext.stroke();
		}

		y += 30;
		if(dadosJogo.ers){
			ctlContext.fillStyle = corFundo
			ctlContext.fillRect(x, y, 94, 20);
			ctlContext.font = '14px sans-serif';
			ctlContext.fillStyle = "black"
			ctlContext.fillText('Ers ', x + 5, y + 15);
			var tErs = ctlContext.measureText(dadosParciais.cargaErs + '%').width;
			ctlContext.fillText(dadosParciais.cargaErs + '%', x + (90-tErs), y + 15);
		}
	}
	
	ctlContext.closePath();
	ctlContext.stroke();
}

function ctl_removeControle(controle) {
	
	if (!dadosJogo.drs && controle.tipo == 'Drs') {
		return true;
	}
	
	if (!ctl_showFps && controle.tipo == 'fps') {
		return true;
	}
	
	if (dadosParciais!=null && 'png/chuva.png' == dadosParciais.clima && controle.tipo == 'Drs'){
		return true;
	}
	
	if (!dadosJogo.ers && controle.tipo == 'Ers') {
		return true;
	}
	if (!dadosJogo.reabastecimento
			&& (controle.tipo == 'CombustivelValor' || controle.tipo == 'Combustivel')) {
		return true;
	}
	
	if (dadosParciais!=null && !dadosParciais.box
			&& (controle.tipo == 'Asa' || controle.tipo == 'Pneu'
					|| controle.tipo == 'CombustivelValor' || controle.tipo == 'Combustivel')) {
		return true;
	}
	if (dadosJogo.drs && controle.tipo == 'Asa'
			&& 'png/chuva.png' != dadosParciais.clima) {
		return true;
	}

	if (!dadosJogo.trocaPneu && controle.tipo == 'Pneu' && dadosJogo.pPneus > 0) {
		return true;
	}
	
	if(dadosParciais!=null && !dadosParciais.box &&(controle.tipo == 'confirmaSair' || controle.tipo == 'cancelaSair' || controle.tipo == 'perguntaSair' )){
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
		cor : corBabaca,
		valor : 'GIRO_MIN',
		exibir : '1',
		centralizaTexto : true,
		larguraTexto : false,
		tipo : 'controleMotor',
		width : 40,
		height : 40,
		evalY : '(window.innerHeight > window.innerWidth)?(window.innerHeight - 150):(window.innerHeight - 100);',
		y : 0,
		x : 10,
		img : motor
	});
	controles.push({
		cor : corBabaca,
		valor : 'GIRO_NOR',
		exibir : '2',
		tipo : 'controleMotor',
		centralizaTexto : true,
		larguraTexto : false,
		width : 40,
		height : 40,
		evalY : '(window.innerHeight > window.innerWidth)?(window.innerHeight - 150):(window.innerHeight - 100);',
		y : 0,
		x : 60,
		img : motor
	});
	controles.push({
		cor : corBabaca,
		valor : 'GIRO_MAX',
		exibir : '3',
		tipo : 'controleMotor',
		centralizaTexto : true,
		larguraTexto : false,
		width : 40,
		height : 40,
		evalY : '(window.innerHeight > window.innerWidth)?(window.innerHeight - 150):(window.innerHeight - 100);',
		y : 0,
		x : 110,
		img : motor
	});

	controles.push({
		cor : corBabaca,
		valor : 'AGRESSIVO',
		exibir : '3',
		tipo : 'controlePiloto',
		centralizaTexto : true,
		larguraTexto : false,
		width : 40,
		height : 40,
		evalY : '(window.innerHeight > window.innerWidth)?(window.innerHeight - 150):(window.innerHeight - 100);',
		y : 0,
		evalX : 'window.innerWidth - 150;',
		x : 0,
		img : capacete
	});
	controles.push({
		cor : corBabaca,
		valor : 'NORMAL',
		exibir : '2',
		centralizaTexto : true,
		larguraTexto : false,
		tipo : 'controlePiloto',
		width : 40,
		height : 40,
		evalY : '(window.innerHeight > window.innerWidth)?(window.innerHeight - 150):(window.innerHeight - 100);',
		y : 0,
		evalX : 'window.innerWidth - 100;',
		x : 0,
		img : capacete
	});
	controles.push({
		cor : corBabaca,
		valor : 'LENTO',
		exibir : '1',
		centralizaTexto : true,
		larguraTexto : false,
		tipo : 'controlePiloto',
		width : 40,
		height : 40,
		evalY : '(window.innerHeight > window.innerWidth)?(window.innerHeight - 150):(window.innerHeight - 100);',
		y : 0,
		evalX : 'window.innerWidth - 50;',
		x : 0,
		img : capacete
	});
	controles.push({
		cor : corBabaca,
		valor : '',
		exibir : '',
		tipo : 'fps',
		centralizaTexto : false,
		larguraTexto : false,
		width : 110,
		height : 40,
		evalY : '(window.innerHeight > window.innerWidth)?(window.innerHeight - 300):(window.innerHeight - 200);',
		y : 0,
		x : 10
	});
	
	controles.push({
		cor : corBabaca,
		valor : 'E',
		exibir : 'Ers',
		tipo : 'Ers',
		centralizaTexto : false,
		larguraTexto : false,
		width : 60,
		height : 40,
		evalY : '(window.innerHeight > window.innerWidth)?(window.innerHeight - 200):(window.innerHeight - 150);',
		y : 0,
		x : 10
	});
	controles.push({
		cor : corBabaca,
		valor : 'D',
		exibir : 'Drs',
		tipo : 'Drs',
		centralizaTexto : false,
		larguraTexto : false,
		width : 60,
		height : 40,
		evalY : '(window.innerHeight > window.innerWidth)?(window.innerHeight - 200):(window.innerHeight - 150);',
		y : 0,
		evalX : 'window.innerWidth - 70;',
		x : 0
	});

	controles.push({
		cor : corBabaca,
		valor : 'BOX',
		exibir : 'BOX',
		tipo : 'Box',
		centralizaTexto : false,
		larguraTexto : false,
		width : 75,
		height : 40,
		y : 10,
		evalX : '(window.innerWidth/2 - 40);',
		x : 0
	});
	
	controles.push({
		cor : corBabaca,
		valor : 'ZOOM',
		exibir : 'ZOOM',
		tipo : 'Zoom',
		centralizaTexto : false,
		larguraTexto : false,
		width : 100,
		height : 40,
		y : 10,
		evalX : '(window.innerWidth/2 - 50);',
		evalY : '(window.innerHeight > window.innerWidth)?(window.innerHeight - 200):(window.innerHeight - 100);',
		x : 0
	});

	controles.push({
		cor : corBabaca,
		valor : 'TIPO_PNEU_MOLE',
		exibir : 'M',
		tipo : 'Pneu',
		centralizaTexto : true,
		larguraTexto : false,
		width : 40,
		height : 40,
		y : 60,
		evalX : '(window.innerWidth/2 - 80);',
		x : 0,
		img : imgPneuM
	});
	controles.push({
		cor : corBabaca,
		valor : 'TIPO_PNEU_DURO',
		exibir : 'D',
		tipo : 'Pneu',
		centralizaTexto : true,
		larguraTexto : false,
		width : 40,
		height : 40,
		y : 60,
		evalX : '(window.innerWidth/2 - 20);',
		x : 0,
		img : imgPneuD
	});
	controles.push({
		cor : corBabaca,
		valor : 'TIPO_PNEU_CHUVA',
		exibir : 'C',
		tipo : 'Pneu',
		centralizaTexto : true,
		larguraTexto : false,
		width : 40,
		height : 40,
		y : 60,
		evalX : '(window.innerWidth/2 + 40);',
		x : 0,
		img : imgPneuC
	});

	controles.push({
		cor : corBabaca,
		valor : 'MENOS_ASA',
		exibir : '1',
		tipo : 'Asa',
		centralizaTexto : true,
		larguraTexto : false,
		width : 40,
		height : 40,
		y : 110,
		evalX : '(window.innerWidth/2 - 80);',
		x : 0,
		img : menosAsa
	});
	controles.push({
		cor : corBabaca,
		valor : 'ASA_NORMAL',
		exibir : '2',
		tipo : 'Asa',
		centralizaTexto : true,
		larguraTexto : false,
		width : 40,
		height : 40,
		y : 110,
		evalX : '(window.innerWidth/2 - 20);',
		x : 0,
		img : normalAsa
	});
	controles.push({
		cor : corBabaca,
		valor : 'MAIS_ASA',
		exibir : '3',
		tipo : 'Asa',
		centralizaTexto : true,
		larguraTexto : false,
		width : 40,
		height : 40,
		y : 110,
		evalX : '(window.innerWidth/2 + 40);',
		x : 0,
		img : maisAsa
	});

	controles.push({
		cor : corBabaca,
		valor : '-',
		exibir : '-',
		tipo : 'Combustivel',
		centralizaTexto : true,
		larguraTexto : false,
		width : 40,
		height : 40,
		y : 160,
		evalX : '(window.innerWidth/2 - 80);',
		x : 0
	});
	controles.push({
		cor : corBabaca,
		valor : '',
		exibir : '100',
		tipo : 'CombustivelValor',
		centralizaTexto : false,
		larguraTexto : false,
		width : 65,
		height : 40,
		y : 160,
		evalX : '(window.innerWidth/2 - 30);',
		x : 0
	});
	controles.push({
		cor : corBabaca,
		valor : '+',
		exibir : '+',
		tipo : 'Combustivel',
		centralizaTexto : true,
		larguraTexto : false,
		width : 40,
		height : 40,
		y : 160,
		evalX : '(window.innerWidth/2 + 45);',
		x : 0
	});
	controles.push({
		cor : corBabaca,
		valor : '',
		exibir : lang_text('sairJogo'),
		tipo : 'perguntaSair',
		centralizaTexto : false,
		larguraTexto : false,
		width : 100,
		height : 20,
		y : 5,
		evalX : '(window.innerWidth - 110);',
		x : 0		
	});	
	controles.push({
		cor : corBabaca,
		valor : '',
		exibir : lang_text('cancela'),
		tipo : 'cancelaSair',
		centralizaTexto : false,
		larguraTexto : true,
		width : 120,
		height : 40,
		y : 210,
		evalX : '(centroX-((ctlContext.measureText(controle.exibir).width + 80)/2))',
		x : 0
	});
	controles.push({
		cor : corBabaca,
		valor : '',
		exibir : lang_text('095'),
		tipo : 'confirmaSair',
		centralizaTexto : false,
		larguraTexto : true,
		width : 190,
		height : 40,
		evalY : '(window.innerHeight-50)',
		evalX : '(centroX-((ctlContext.measureText(controle.exibir).width + 30)/2))',
		x : 0
	});	
}
maneCanvas.addEventListener('click',ctl_click, false);

function ctl_click(event) {
	var x = event.pageX;
	var y = event.pageY;


	var clickControle = false;
	for (var i = 0; i < controles.length; i++) {
		var controle = controles[i];
		if (controle.evalY) {
			controle.y = eval(controle.evalY);
		}
		if (controle.evalX) {
			controle.x = eval(controle.evalX);
		}
		
		var contains = (y > controle.y && y < controle.y + controle.height
		&& x > controle.x
		&& x < controle.x + controle.width);
		if (!contains){
			continue;
		}
		if (controle.tipo == 'fps') {
			if(ctl_showFps){
				ctl_showFps = false;	
			}else{
				ctl_showFps = true;
			}
			clickControle = true;	
			return;
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
		if (controle.tipo == 'Zoom') {
			if(dirZoom == '-'){
				if(zoom == 4){
					dirZoom = '+';
					zoom = 2;
					return;
				}else if(zoom == 2){
					zoom = 4;
					return;
				}else if(zoom == 1){
					zoom = 2;
					return;
				}	
			}
			if(dirZoom == '+'){
				if(zoom == 4){
					zoom = 2;
					return;
				}else if(zoom == 2){
					zoom = 1;
					return;
				}else if(zoom == 1){
					dirZoom = '-';
					zoom = 2;
					return;
				}	
			}
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
