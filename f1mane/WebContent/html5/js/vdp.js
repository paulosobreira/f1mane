/**
 * Controle video e desenho
 */
var mapaIdNos;
var cvRotate = document.createElement('canvas');
var ctxRotate = cvRotate.getContext('2d');
var cvBlend = document.createElement('canvas');
var ctxBlend = cvBlend.getContext('2d');
var maneCanvas = document.getElementById('maneCanvas')
var maneContext = maneCanvas.getContext('2d');
var imgBg = new Image();
var desenhaImagens = true;
var pitLane = false;
var ptBg = {
	x : 0,
	y : 0
};
var rectBg = {
	left : ptBg.x,
	top : ptBg.y,
	right : ptBg.x + maneCanvas.width,
	bottom : ptBg.y + maneCanvas.height
};
var blendOp = 'destination-out'
var corNublado = "rgba(255, 255, 255, 0.3)";
var corChuva = "rgba(255, 255, 255,0.5)";
var corFaisca = "rgba(255, 255, 0 ,0.5)";

function vdp_desenha() {
	if (dadosJogo == null || circuito == null || !ativo || !imgBg.complete) {
		return;
	}
	vdp_centralizaPilotoSelecionado();
	vdp_desenhaBackGround();
	// vdp_desenhaObjs();
	vdp_desenhaCarrosCima();
	vdp_desenhaClima();
	ctl_desenha();
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

function vdp_desenhaClima() {
	if (dadosParciais && dadosParciais.clima
			&& dadosParciais.clima == "nublado.png") {
		maneContext.fillStyle = corNublado;
		maneContext.fillRect(0, 0, maneCanvas.width, maneCanvas.height);
	}

	if (dadosParciais && dadosParciais.clima
			&& dadosParciais.clima == "chuva.png") {
		maneContext.fillStyle = corNublado;
		maneContext.fillRect(0, 0, maneCanvas.width, maneCanvas.height);
		var p1;
		var p2;
		for (var i = 0; i < maneCanvas.width; i += 20) {
			for (var j = 0; j < maneCanvas.height; j += 20) {
				if (Math.random() > .8) {
					p1 = {
						x : i + 10,
						y : j + 10
					};
					p2 = {
						x : i + 15,
						y : j + 20
					};
					maneContext.beginPath();
					maneContext.strokeStyle = corChuva;
					maneContext.moveTo(p1.x, p1.y);
					maneContext.lineTo(p2.x, p2.y);
					maneContext.stroke();
				}
			}
		}
	}
}

function vdp_desenhaRastroFaisca(frenteCar, trazCar) {
	if (dadosParciais.clima == "chuva.png") {
		return;
	}
	for (var i = 0; i < 15; i++) {
		var p1 = {
			x : intervalo((frenteCar.x - ptBg.x - intervalo(2.5, 10)), (frenteCar.x
					- ptBg.x + intervalo(2.5, 10))),
			y : intervalo((frenteCar.y - ptBg.y - intervalo(2.5, 10)), (frenteCar.y
					- ptBg.y + intervalo(2.5, 10)))
		};
		var p2 = {
			x : intervalo((trazCar.x - ptBg.x - intervalo(2.5, 15)), (trazCar.x
					- ptBg.x + intervalo(2.5, 15))),
			y : intervalo((trazCar.y - ptBg.y - intervalo(2.5, 15)), (trazCar.y
					- ptBg.y + intervalo(2.5, 15)))
		};
		var angulo = gu_calculaAngulo(p1, p2, 180);
		var ptDest = gu_calculaPonto(angulo, intervalo(43, 86), p1);
		maneContext.beginPath();
		maneContext.strokeStyle = corFaisca;
		maneContext.setLineDash([intervalo(5, 10), intervalo(10, 15)]);
		maneContext.moveTo(p1.x, p1.y);
		maneContext.lineTo(ptDest.x, ptDest.y);
		maneContext.closePath();
		maneContext.stroke();
		maneContext.restore();
	}
}

function vdp_desenhaRastroChuva(frenteCar, trazCar) {
	if (dadosParciais.clima != "chuva.png") {
		return;
	}
	for (var i = 0; i < 20; i++) {
		if (i % (Math.random() > 0.5 ? 3 : 2) == 0) {
			continue;
		}
		var p1 = {
			x : frenteCar.x - ptBg.x,
			y : frenteCar.y - ptBg.y
		};
		var p2 = {
			x : intervalo((trazCar.x - ptBg.x - intervalo(2.5, 6)), (trazCar.x
					- ptBg.x + intervalo(2.5, 6))),
			y : intervalo((trazCar.y - ptBg.y - intervalo(2.5, 6)), (trazCar.y
					- ptBg.y + intervalo(2.5, 6)))
		};
		var angulo = gu_calculaAngulo(p1, p2, 180);
		var ptDest = gu_calculaPonto(angulo, intervalo(20, 200), p1);
		maneContext.beginPath();
		maneContext.strokeStyle = corChuva;
		maneContext.moveTo(p1.x, p1.y);
		maneContext.lineTo(ptDest.x, ptDest.y);
		maneContext.closePath();
		maneContext.stroke();
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
	for (var i = 0; i < posicaoPilotos.posis.length; i++) {
		maneContext.beginPath();
		var piloto = posicaoPilotos.posis[i];
		if (pilotosDnfMap.get(piloto.idPiloto)) {
			continue;
		}
		;
		var ponto = vdp_obeterPonto(piloto);
		if (ponto == null) {
			continue;
		}
		if (ponto != null && ponto.x != null && ponto.y != null) {
			var angulo = 0;
			var frenteCar;
			var trazCar;
			var no = mapaIdNos.get(piloto.idNo);
			if (no.box) {
				var idNo = piloto.idNo - circuito.pistaFull.length;
				if ((idNo - 15) > 0 && (idNo + 15) < circuito.boxFull.length) {
					frenteCar = safeArray(circuito.boxFull, idNo + 15);
					trazCar = safeArray(circuito.boxFull, idNo - 15);
					if (piloto.tracado == 1) {
						frenteCar = safeArray(circuito.box1Full, idNo + 15);
						trazCar = safeArray(circuito.box1Full, idNo - 15);
					}
					if (piloto.tracado == 2) {
						frenteCar = safeArray(circuito.box2Full, idNo + 15);
						trazCar = safeArray(circuito.box2Full, idNo - 15);
					}
					if (piloto.idPiloto == idPilotoSelecionado) {
						pitLane = true;
					}
				}
			} else {
				if ((piloto.idNo - 15) > 0
						&& (piloto.idNo + 15) < circuito.pistaFull.length) {
					frenteCar = safeArray(circuito.pistaFull, piloto.idNo + 15);
					trazCar = safeArray(circuito.pistaFull, piloto.idNo - 15);

					if (piloto.tracado == 1) {
						frenteCar = safeArray(circuito.pista1Full,
								piloto.idNo + 15);
						trazCar = safeArray(circuito.pista1Full,
								piloto.idNo - 15);
					}
					if (piloto.tracado == 2) {
						frenteCar = safeArray(circuito.pista2Full,
								piloto.idNo + 15);
						trazCar = safeArray(circuito.pista2Full,
								piloto.idNo - 15);
					}
					if (piloto.idPiloto == idPilotoSelecionado) {
						pitLane = false;
					}
				}
			}
			angulo = gu_calculaAngulo(frenteCar, trazCar, 180);

			if (carrosImgMap != null) {
				var imgCarro = carrosImgMap.get(piloto.idPiloto);
				if (pilotosAereofolioMap.get(piloto.idPiloto)) {
					imgCarro = carrosImgSemAereofolioMap.get(piloto.idPiloto);
				}
				var rectObj = {
					left : ponto.x,
					top : ponto.y,
					right : ponto.x + imgCarro.width,
					bottom : ponto.y + imgCarro.height
				};
				if (!vdp_intersectRect(rectBg, rectObj)) {
					continue;
				}

				var x = ponto.x - ptBg.x - (imgCarro.width / 2);
				var y = ponto.y - ptBg.y - (imgCarro.height / 2);

				maneContext.font = '14px sans-serif';
				var nmPiloto = pilotosMap.get(piloto.idPiloto).nome;
				var laruraTxt = maneContext.measureText(nmPiloto).width + 10;
				if (idPilotoSelecionado == piloto.idPiloto) {
					maneContext.strokeStyle = '#00FF00';
					maneContext.rect(x - 5, y, laruraTxt, 20);
				} else if (piloto.humano) {
					maneContext.strokeStyle = '#FFFF00';
					maneContext.rect(x - 5, y, laruraTxt, 20);
				}
				maneContext.fillStyle = corFundo
				maneContext.fillRect(x - 5, y, laruraTxt, 20);
				maneContext.fillStyle = "black"
				maneContext.fillText(nmPiloto, x, y + 15);

				if (desenhaImagens) {
					var rotacionarCarro = vdp_rotacionarCarro(imgCarro, angulo);
					var blendCarro = vdp_blendCarro(rotacionarCarro, ponto, x,
							y, no);
					maneContext.drawImage(blendCarro, x, y);
				}
				if (vdp_emMovimento(piloto.idPiloto)) {
					vdp_desenhaRastroChuva(frenteCar, trazCar);
					if (pilotosFaiscaMap.get(piloto.idPiloto)>0) {
						pilotosFaiscaMap.set(piloto.idPiloto,pilotosFaiscaMap.get(piloto.idPiloto)-1);
						vdp_desenhaRastroFaisca(frenteCar, trazCar);
						
					}
				}
			}
		}
		maneContext.closePath();
		maneContext.stroke();
	}
}

function vdp_emMovimento(id) {
	if (!dadosParciais) {
		return false;
	}
	return ptsPistaMapAnterior.get(id) != ptsPistaMap.get(id)
}

function vdp_intersectRect(r1, r2) {
	return !(r2.left > r1.right || r2.right < r1.left || r2.top > r1.bottom || r2.bottom < r1.top);
}

function vdp_desenhaObjs() {
	if (circuito == null || circuito.objetosNoTransparencia == null
			|| dadosParciais == null) {
		return;
	}
	for (i = 0; i < circuito.objetosNoTransparencia.length; i++) {
		var pontosTp = circuito.objetosNoTransparencia[i];
		var img = objImgPistaMap.get(i);
		var rectObj = {
			left : pontosTp.x,
			top : pontosTp.y,
			right : pontosTp.x + img.width,
			bottom : pontosTp.y + img.height
		};
		if (img && img.width > 0 && img.height > 0
				&& vdp_intersectRect(rectBg, rectObj)) {
			maneContext
					.drawImage(img, pontosTp.x - ptBg.x, pontosTp.y - ptBg.y);
		}
	}
}

function vdp_centralizaPonto(ponto) {
	maneCanvas.width = window.innerWidth;
	maneCanvas.height = window.innerHeight;
	var x = ponto.x;
	var y = ponto.y;

	x -= (maneCanvas.width / 2);
	y -= (maneCanvas.height / 2);

	if (imgBg != null && imgBg.complete) {
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
	rectBg = {
		left : ptBg.x,
		top : ptBg.y,
		right : ptBg.x + maneCanvas.width,
		bottom : ptBg.y + maneCanvas.height
	};
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

function vdp_blendCarro(imgCarro, ptCarro, xCarro, yCarro, no) {
	var maiorLado = 0;
	if (imgCarro.width > imgCarro.height) {
		maiorLado = imgCarro.width;
	} else {
		maiorLado = imgCarro.height;
	}
	cvBlend.width = maiorLado;
	cvBlend.height = maiorLado;
	ctxBlend.drawImage(imgCarro, 0, 0);

	for (var j = 0; j < circuito.objetosNoTransparencia.length; j++) {
		var img = objImgPistaMap.get(j);
		var pontosTp = circuito.objetosNoTransparencia[j];
		if (pontosTp.transparenciaBox != no.box) {
			continue;
		}
		if (pontosTp.indexFim != 0
				&& pontosTp.indexInicio != 0
				&& (no.index < pontosTp.indexInicio || no.index > pontosTp.indexFim)) {
			continue;
		}
		var rectObj = {
			left : pontosTp.x,
			top : pontosTp.y,
			right : pontosTp.x + img.width,
			bottom : pontosTp.y + img.height
		};
		if (img && img.width > 0 && img.height > 0
				&& vdp_intersectRect(rectBg, rectObj)) {
			var x = pontosTp.x - ptBg.x - xCarro;
			var y = pontosTp.y - ptBg.y - yCarro;
			ctxBlend.globalCompositeOperation = blendOp;
			ctxBlend.drawImage(img, x, y);
		}
	}

	return cvBlend;
}