/**
 * Controle video e desenho
 */
var mapaIdNos;
var cvRotate = document.createElement('canvas');
var ctxRotate = cvRotate.getContext('2d');
var maneCanvas = document.getElementById('maneCanvas')
var maneContext = maneCanvas.getContext('2d');
var imgBg = new Image();
var desenhaImagens = true;
var ptBg = {
	x : 0,
	y : 0
};
var posicaoCentraliza = 0;

function vdp_desenha() {
	vdp_centralizaPilotoSelecionado();
	vdp_desenhaBackGround();
	vdp_desenhaObjs();
	vdp_desenhaCarrosCima();
}

function vdp_centralizaPilotoSelecionado() {
	if (dadosParciais == null || dadosParciais.posisPack == null
			|| circuito == null) {
		return;
	}
	var piloto = dadosParciais.posisPack.posis[posicaoCentraliza];
	var ponto = vdp_obeterPonto(piloto);
	if (ponto == null) {
		return;
	}
	vdp_centralizaPonto(ponto);
}

function vdp_desenhaBackGround() {
	maneContext.clearRect(0, 0, maneCanvas.width, maneCanvas.height);
	maneCanvas.width = window.innerWidth;
	maneCanvas.height = window.innerHeight;
	var sW = maneCanvas.width;
	var sH = maneCanvas.height;
	if (imgBg.src != "" && desenhaImagens && imgBg.complete) {
		try {
			maneContext.drawImage(imgBg, ptBg.x, ptBg.y, sW, sH, 0, 0,
					maneCanvas.width, maneCanvas.height);
		} catch (e) {
			console.log('vdp_desenhaBackGround');
			console.log(e);
		}
	}
}

function vdp_carregaBackGround() {
	if (circuito == null) {
		return;
	}
	if (imgBg.src != "") {
		return;
	}
	imgBg.src = "../sowbreira/f1mane/recursos/" + circuito.backGround;
	// imgBg.src = "../sowbreira/f1mane/recursos/testeBG_mro.jpg";
}

function vdp_obeterPonto(piloto) {
	var no = mapaIdNos.get(piloto.idNo);
	var ponto;
	if (no.box) {
		var idNo = piloto.idNo - circuito.pistaFull.length;
		ponto = circuito.boxFull[idNo];
		if (piloto.tracado == 1) {
			ponto = circuito.box1Full[idNo];
		}
		if (piloto.tracado == 2) {
			ponto = circuito.box2Full[idNo];
		}
	} else {
		ponto = circuito.pistaFull[piloto.idNo];
		if (piloto.tracado == 1) {
			ponto = circuito.pista1Full[piloto.idNo];
		}
		if (piloto.tracado == 2) {
			ponto = circuito.pista2Full[piloto.idNo];
		}
		if (piloto.tracado == 4) {
			ponto = circuito.pista4Full[piloto.idNo];
		}
		if (piloto.tracado == 5) {
			ponto = circuito.pista5Full[piloto.idNo];
		}
	}
	return ponto;
}

function vdp_desenhaCarrosCima() {
	if (circuito == null || circuito.objetosNoTransparencia == null
			|| dadosParciais == null || dadosJogo == null
			|| carrosImgMap == null || imgBg.src == "") {
		return;
	}
	var posicaoPilotos = dadosParciais.posisPack;
	for (i = 0; i < posicaoPilotos.posis.length; i++) {
		maneContext.beginPath();
		var piloto = posicaoPilotos.posis[i];
		if (piloto.idPiloto == idPilotoSelecionado) {
			posicaoCentraliza = i;
		}
		var ponto = vdp_obeterPonto(piloto);
		if (ponto == null) {
			continue;
		}
		if (ponto != null && ponto.x != null && ponto.y != null) {
			var angulo = 0;
			if ((piloto.idNo - 5) > 0
					&& (piloto.idNo + 5) < circuito.pistaFull.length) {
				var frenteCar = circuito.pistaFull[piloto.idNo - 5];
				var trazCar = circuito.pistaFull[piloto.idNo + 5];
				angulo = gu_calculaAngulo(frenteCar, trazCar, 0);
			}

			if (carrosImgMap != null) {
				var imgCarro = carrosImgMap.get(piloto.idPiloto);
				var x = ponto.x - ptBg.x - (imgCarro.width / 2);
				var y = ponto.y - ptBg.y - (imgCarro.height / 2);

				if(idPilotoSelecionado == piloto.idPiloto){
					maneContext.strokeStyle = '#00FF00';
					maneContext.rect(x-5, y, 80, 20);
				}else if(piloto.humano){
					maneContext.strokeStyle = '#FFFF00';
					maneContext.rect(x-5, y, 80, 20);
				}	
				
				maneContext.fillStyle = corFundo
				maneContext.fillRect(x - 5, y, 80, 20);
				maneContext.font = '14px sans-serif';
				maneContext.fillStyle = "black"
				maneContext.fillText(pilotosMap.get(piloto.idPiloto).nome, x, y+15);

				if(desenhaImagens){
					maneContext.drawImage(vdp_rotacionarCarro(imgCarro, angulo), x,
							y);
				}
			}
		}
		maneContext.closePath();
		maneContext.stroke();
	}
}

function vdp_desenhaObjs() {
	if (circuito == null || circuito.objetosNoTransparencia == null
			|| dadosParciais == null) {
		return;
	}

	for (i = 0; i < circuito.objetosNoTransparencia.length; i++) {
		var pontosTp = circuito.objetosNoTransparencia[i];
		maneContext.beginPath();
		for (j = 0; j < pontosTp.pontos.length; j++) {
			var pt = pontosTp.pontos[j];
			if (j == 0) {
				maneContext.moveTo(pt.x - ptBg.x, pt.y - ptBg.y);
			} else {
				maneContext.lineTo(pt.x - ptBg.x, pt.y - ptBg.y);
			}

		}
		maneContext.closePath();
		maneContext.fill();
	}
}

function vdp_centralizaPonto(ponto) {
	var x = ponto.x;
	var y = ponto.y;

	x -= maneCanvas.width / 2;
	y -= maneCanvas.height / 2;

	if (imgBg != null) {
		if (x < 0) {
			x = 0;
		}
		if (y < 0) {
			y = 0;
		}
		var sW = maneCanvas.width;
		if ((x + sW) > imgBg.width) {
			x -= ((x + sW) - imgBg.width);
		}
		var sH = maneCanvas.height;
		if ((y + sH) > imgBg.height) {
			y -= ((y + sH) - imgBg.height);
		}
	}
	ptBg.x = x;
	ptBg.y = y;
}

function vdp_rotacionarCarro(imgCarro, angulo) {
	var maiorLado = 0;
	if (imgCarro.width > imgCarro.height) {
		maiorLado = imgCarro.width;
	} else {
		maiorLado = imgCarro.height;
	}
	cvRotate.width = maiorLado;
	cvRotate.height = maiorLado;
	ctxRotate.translate(maiorLado / 2, maiorLado / 2);
	ctxRotate.rotate(angulo);
	ctxRotate.drawImage(imgCarro, -maiorLado / 2, -maiorLado / 2);
	return cvRotate;
}