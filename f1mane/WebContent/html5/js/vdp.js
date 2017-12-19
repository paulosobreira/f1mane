/**
 * Controle video e desenho
 */
var mapaIdNos;
var mapaIdNosSuave;
var mapaTracadoSuave = new Map();
var mapaIndexTracadoSuave = new Map();
var mapaPontoSuave = new Map();
var cvRotate = document.createElement('canvas');
var ctxRotate = cvRotate.getContext('2d');
var cvBlend = document.createElement('canvas');
var ctxBlend = cvBlend.getContext('2d');
var cvFx = document.createElement('canvas');
var ctxFx = cvFx.getContext('2d');
var cvBg;
var ctxBg;
var maneCanvas = document.getElementById('maneCanvas')
var maneContext = maneCanvas.getContext('2d');
var pilotosTracadoForaMap = new Map();
var desenhaImagens = true;
var pitLane = false;
var desenhouMarcasLargadaGrid = false;
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
var pilotosEfeitosMap = new Map();
maneCanvas.width = 0;
maneCanvas.height = 0;

function vdp_desenha(fps) {
	if (imgBg && imgBg.complete) {
		cvBg = document.createElement('canvas');
		cvBg.width = imgBg.width;
		cvBg.height = imgBg.height;
		ctxBg = cvBg.getContext('2d');
		ctxBg.drawImage(imgBg, 0, 0);
		imgBg = null;
	}
	if (dadosJogo == null || circuito == null || !ativo || !cvBg
			|| contCarregouMidia != 0) {
		return;
	}
	vdp_atualizaSuave();
	vdp_centralizaPilotoSelecionado();
	vdp_desenhaBackGround();
	//vdp_desenhaMarcasLargadaGrid();
	vdp_desenhaCarrosCima();
	vdp_desenhaNomesCima();
	// vdp_desenhaObjs();
	vdp_desenhaClima();
	ctl_desenha();
	// if (fps != null) {
	// maneContext.fillStyle = 'black';
	// maneContext.fillText("FPS: " + fps.frameRate(), 4, 30);
	// }
}

function vdp_desenhaMarcasLargadaGrid() {
	if (desenhouMarcasLargadaGrid || circuito == null
			|| circuito.objetosNoTransparencia == null || dadosParciais == null
			|| dadosJogo == null || carrosImgMap == null || cvBg == null
			|| carrosImgMap == null) {
		return;
	}
	var posicaoPilotos = dadosParciais.posisPack;
	for (var i = 0; i < posicaoPilotos.posis.length; i++) {
		var piloto = posicaoPilotos.posis[i];
		var no = mapaIdNos.get(piloto.idNo);
		var frenteCar = safeArray(circuito.pistaFull, no.index + 15);
		var atrasCar = safeArray(circuito.pistaFull, no.index - 15);
		var angulo = gu_calculaAngulo(frenteCar, atrasCar, 180);
		var ponto = vdp_obterPonto(piloto);
		var x = ponto.x - ptBg.x - 60;
		var y = ponto.y - ptBg.y - 60;
		var rotacionar = vdp_rotacionar(girdLargadaMarca, angulo);
		ctxBg.drawImage(rotacionar, x + ptBg.x, y + ptBg.y);
	}
	desenhouMarcasLargadaGrid = true;
}

function vdp_atualizaSuave() {
	if (circuito == null || circuito.objetosNoTransparencia == null
			|| dadosParciais == null || dadosJogo == null
			|| carrosImgMap == null || cvBg == null) {
		return;
	}
	var posicaoPilotos = dadosParciais.posisPack;
	for (var i = 0; i < posicaoPilotos.posis.length; i++) {
		var piloto = posicaoPilotos.posis[i];
		var noSuave = mapaIdNosSuave.get(piloto.idPiloto);
		var noReal = mapaIdNos.get(piloto.idNo);
		if (noSuave == null) {
			// if (piloto.idPiloto == idPilotoSelecionado) {
			// console.log(piloto.idPiloto + ' noSuave == null ');
			// }
			mapaIdNosSuave.set(piloto.idPiloto, noReal);
			continue;
		}
		var indexReal = noReal.index;
		var indexSuave = noSuave.index;
		if (indexReal == indexSuave) {
			// if (piloto.idPiloto == idPilotoSelecionado) {
			// console.log(piloto.idPiloto + ' indexReal == indexSuave '
			// + indexReal + ' ' + indexSuave);
			// }
			continue;
		}
		if (!noReal.box && !noSuave.box && indexSuave > indexReal) {
			indexReal = indexReal + (circuito.pistaFull.length - 1);
		}
		if (noReal.box && !noSuave.box) {
			if (piloto.idPiloto == idPilotoSelecionado) {
				console.log(piloto.idPiloto + ' noReal.box && !noSuave.box ');
			}
			indexReal = indexReal + circuito.entradaBoxIndex;
		}
		if (!noReal.box && noSuave.box) {
			if (piloto.idPiloto == idPilotoSelecionado) {
				console.log(piloto.idPiloto + ' !noReal.box && noSuave.box ');
			}
			indexReal = (indexReal - circuito.saidaBoxIndex)
					+ (circuito.boxFull.length - 1);
		}
		var diff = (indexReal - indexSuave);
		var multi = diff / 100;
		arr = [ 3.5 * multi, 2.5 * multi, 2 * multi ];
		if (noSuave.box) {
			arr = [ 3 * multi, 2 * multi, 2 * multi ];
		}
		if (mapaTracadoSuave.get(piloto.idPiloto) == 4
				|| mapaTracadoSuave.get(piloto.idPiloto) == 5) {
			arr = [ 2 * multi, 2 * multi, 1 * multi ];
		}
		var novoIndex;
		if (noSuave.retaOuLargada) {
			novoIndex = noSuave.index + Math.round(arr[0]);
		} else if (noSuave.curvaAlta) {
			novoIndex = noSuave.index + Math.round(arr[1]);
		} else if (noSuave.curvaBaixa) {
			novoIndex = noSuave.index + Math.round(arr[2]);
		} else {
			novoIndex = noSuave.index + Math.round(arr[2]);
		}
		if (novoIndex > indexReal) {
			if (piloto.idPiloto == idPilotoSelecionado) {
				console.log(piloto.idPiloto + ' novoIndex > indexReal '
						+ novoIndex);
			}
			novoIndex = indexReal - 1;
		}
		if (noSuave.box) {
			if (novoIndex > (circuito.boxFull.length - 1)) {
				novoIndex = (circuito.boxFull.length - 1);
			}
		} else {
			if (novoIndex > (circuito.pistaFull.length - 1)) {
				novoIndex = novoIndex - circuito.pistaFull.length - 1;
			}
		}
		if (novoIndex < 0) {
			novoIndex = 0;
		}
		var noSuaveNovo = circuito.pistaFull[novoIndex];
		if (noReal.box && !noSuave.box && novoIndex >= circuito.entradaBoxIndex) {
			if (piloto.idPiloto == idPilotoSelecionado) {
				console
						.log(piloto.idPiloto
								+ ' noReal.box && !noSuave.box && novoIndex >= circuito.entradaBoxIndex '
								+ novoIndex);
			}
			noSuaveNovo = circuito.boxFull[novoIndex - circuito.entradaBoxIndex];
		}
		if (noSuave.box) {
			noSuaveNovo = circuito.boxFull[novoIndex];
		}
		if (!noReal.box && noSuave.box
				&& novoIndex >= (circuito.boxFull.length - 1)) {
			if (piloto.idPiloto == idPilotoSelecionado) {
				console
						.log(piloto.idPiloto
								+ ' !no.box && noSuave.box && novoIndex > (circuito.boxFull.length - 1 '
								+ novoIndex);
			}
			noSuaveNovo = circuito.pistaFull[novoIndex
					- (circuito.boxFull.length - 1) + circuito.saidaBoxIndex];
		}
		if (noSuaveNovo == null) {
			console.log(piloto.idPiloto + ' noSuaveNovo ' + noS);
		}
		mapaIdNosSuave.set(piloto.idPiloto, noSuaveNovo);
		if (diff >= 1000) {
			console.log(piloto.idPiloto + ' diff >= 1000 ' + novoIndex);
			mapaIdNosSuave.set(piloto.idPiloto, noReal);
		}
	}
	var posicaoPilotos = dadosParciais.posisPack;
	for (var i = 0; i < posicaoPilotos.posis.length; i++) {
		var piloto = posicaoPilotos.posis[i];
		var noSuave = mapaIdNosSuave.get(piloto.idPiloto);
		var noReal = mapaIdNos.get(piloto.idNo);
		mapaPontoSuave.set(piloto.idPiloto, vdp_pontoTracadoSuave(piloto,
				noSuave, noReal));
	}
}

function vdp_centralizaPilotoSelecionado() {
	if (dadosParciais == null || dadosParciais.posisPack == null) {
		if (circuito != null) {
			vdp_centralizaPonto(circuito.creditosPonto);
		}
		return;
	}
	var piloto = dadosParciais.posisPack.posis[posicaoCentraliza];
	var ponto = vdp_obterPonto(piloto);
	if (ponto == null) {
		return;
	}
	vdp_centralizaPonto(ponto);
}

function vdp_obterPonto(piloto, real) {
	var no;
	if (real) {
		no = mapaIdNos.get(piloto.idNo);
	} else {
		no = mapaIdNosSuave.get(piloto.idPiloto);
		if (no == null) {
			no = mapaIdNos.get(piloto.idNo);
		}
		var ponto = mapaPontoSuave.get(piloto.idPiloto);
		if (ponto != null) {
			return ponto;
		}
	}
	if (no.box) {
		ponto = circuito.boxFull[no.index];
		if (piloto.tracado == 1) {
			ponto = circuito.box1Full[no.index];
		}
		if (piloto.tracado == 2) {
			ponto = circuito.box2Full[no.index];
		}
	} else {
		ponto = circuito.pistaFull[no.index];
		if (piloto.tracado == 1) {
			ponto = circuito.pista1Full[no.index];
		}
		if (piloto.tracado == 2) {
			ponto = circuito.pista2Full[no.index];
		}
		if (piloto.tracado == 4) {
			ponto = circuito.pista4Full[no.index];
			if (ponto == null) {
				ponto = circuito.pista2Full[no.index];
			}
		}
		if (piloto.tracado == 5) {
			ponto = circuito.pista5Full[no.index];
			if (ponto == null) {
				ponto = circuito.pista1Full[no.index];
			}
		}
	}
	return ponto;
}

function vdp_pontoTracadoSuave(piloto, noSuave, noReal) {
	var no = noSuave;
	if (no == null) {
		no = noReal;
	}
	if (no == null) {
		return;
	}
	var tracadoSuave = mapaTracadoSuave.get(piloto.idPiloto);
	if (tracadoSuave == null) {
		tracadoSuave = piloto.tracado;
		mapaTracadoSuave.set(piloto.idPiloto, tracadoSuave);
	}
	if (tracadoSuave == piloto.tracado) {
		mapaIndexTracadoSuave.set(piloto.idPiloto, 0);
		return;
	}
	var indexTracadoSuave = mapaIndexTracadoSuave.get(piloto.idPiloto);
	if (indexTracadoSuave == null || indexTracadoSuave == 0) {
		indexTracadoSuave = 1000;
		mapaIndexTracadoSuave.set(piloto.idPiloto, indexTracadoSuave);
	}
	if (indexTracadoSuave == 0) {
		return;
	}
	var pontoSuave;
	var ponto;
	if (tracadoSuave == 0) {
		pontoSuave = circuito.pistaFull[no.index];
		if (no.box) {
			pontoSuave = circuito.boxFull[no.index]
		}
	}
	if (tracadoSuave == 1) {
		pontoSuave = circuito.pista1Full[no.index];
		if (no.box) {
			pontoSuave = circuito.box1Full[no.index]
		}
	}
	if (tracadoSuave == 2) {
		pontoSuave = circuito.pista2Full[no.index];
		if (no.box) {
			pontoSuave = circuito.box2Full[no.index]
		}
	}
	if (tracadoSuave == 4) {
		pontoSuave = circuito.pista4Full[no.index];
		if (pontoSuave == null) {
			pontoSuave = circuito.pista2Full[no.index];
		}
		if (no.box) {
			pontoSuave = circuito.box2Full[no.index]
		}
	}
	if (tracadoSuave == 5) {
		pontoSuave = circuito.pista5Full[no.index];
		if (pontoSuave == null) {
			pontoSuave = circuito.pista1Full[no.index];
		}
		if (no.box) {
			pontoSuave = circuito.box1Full[no.index]
		}
	}

	if (piloto.tracado == 0) {
		ponto = circuito.pistaFull[no.index];
		if (no.box) {
			ponto = circuito.boxFull[no.index]
		}
	}
	if (piloto.tracado == 1) {
		ponto = circuito.pista1Full[no.index];
		if (no.box) {
			ponto = circuito.box1Full[no.index]
		}
	}
	if (piloto.tracado == 2) {
		ponto = circuito.pista2Full[no.index];
		if (no.box) {
			ponto = circuito.box2Full[no.index]
		}
	}
	if (piloto.tracado == 4) {
		ponto = circuito.pista4Full[no.index];
		if (ponto == null) {
			ponto = circuito.pista2Full[no.index];
		}
		if (no.box) {
			ponto = circuito.box2Full[no.index]
		}
	}
	if (piloto.tracado == 5) {
		ponto = circuito.pista5Full[no.index];
		if (ponto == null) {
			ponto = circuito.pista1Full[no.index];
		}
		if (no.box) {
			ponto = circuito.box1Full[no.index]
		}
	}

	var tracadoFora = pilotosTracadoForaMap.get(piloto.idPiloto);
	if (tracadoFora == null && (piloto.tracado == 4 || piloto.tracado == 5)) {
		tracadoFora = piloto.tracado;
		console.log(piloto.idPiloto + ' Escapou Pista ' + tracadoFora);
		pilotosTracadoForaMap.set(piloto.idPiloto, tracadoFora);
	}
	if (tracadoFora == 4) {
		ponto = circuito.pista4Full[no.index];
		if (ponto == null) {
			ponto = circuito.pista2Full[no.index];
			pilotosTracadoForaMap.set(piloto.idPiloto, null);
			console.log(piloto.idPiloto + ' Voltou Pista ' + tracadoFora);
		}
	}
	if (tracadoFora == 5) {
		ponto = circuito.pista5Full[no.index];
		if (ponto == null) {
			ponto = circuito.pista1Full[no.index];
			pilotosTracadoForaMap.set(piloto.idPiloto, null);
			console.log(piloto.idPiloto + ' Voltou Pista ' + tracadoFora);
		}
	}
	var linha = gu_bline(ponto, pontoSuave);
	if (indexTracadoSuave > linha.length - 1) {
		indexTracadoSuave = linha.length - 1;
	}
	if (indexTracadoSuave < 0) {
		indexTracadoSuave = 0;
	}
	var pontoTracadoSuave = linha[indexTracadoSuave];
	indexTracadoSuave--;
	if (indexTracadoSuave <= 0) {
		indexTracadoSuave = 0;
		mapaTracadoSuave.set(piloto.idPiloto, piloto.tracado);
	}
	mapaIndexTracadoSuave.set(piloto.idPiloto, Math.round(indexTracadoSuave));
	return pontoTracadoSuave;
}

function vdp_desenhaNomesCima() {
	if (circuito == null || circuito.objetosNoTransparencia == null
			|| dadosParciais == null || dadosJogo == null
			|| carrosImgMap == null || cvBg == null || carrosImgMap == null) {
		return;
	}
	var posicaoPilotos = dadosParciais.posisPack;
	for (var i = 0; i < posicaoPilotos.posis.length; i++) {
		var piloto = posicaoPilotos.posis[i];
		if (pilotosDnfMap.get(piloto.idPiloto)) {
			continue;
		}
		var ponto = vdp_obterPonto(piloto);
		if (ponto == null || ponto.x == null || ponto.y == null) {
			continue;
		}
		var no = mapaIdNosSuave.get(piloto.idPiloto);
		if (!no) {
			no = mapaIdNos.get(piloto.idNo);
		}
		var imgCarro = carrosImgMap.get(piloto.idPiloto);
		if (piloto.idPiloto == 'SC') {
			imgCarro = safetycar;
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

		var x = ponto.x - ptBg.x - (imgCarro.width / 3);
		var y = ponto.y - ptBg.y - (imgCarro.height / 2);
		maneContext.beginPath();
		maneContext.font = '14px sans-serif';
		var nmPiloto;
		if (piloto.idPiloto == 'SC') {
			nmPiloto = 'Safety Car';
		} else {
			nmPiloto = pilotosMap.get(piloto.idPiloto).nome;
			nmPiloto = nmPiloto.split(".")[1];
			nmPiloto = nmPiloto.substr(0, 3);
			nmPiloto = (i + 1) + ' ' + nmPiloto;
		}
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
		maneContext.closePath();
		maneContext.stroke();
	}
}

function vdp_desenhaCarrosCima() {
	if (circuito == null || circuito.objetosNoTransparencia == null
			|| dadosParciais == null || dadosJogo == null
			|| carrosImgMap == null || cvBg == null || carrosImgMap == null) {
		return;
	}
	var posicaoPilotos = dadosParciais.posisPack;
	for (var i = 0; i < posicaoPilotos.posis.length; i++) {
		var piloto = posicaoPilotos.posis[i];
		if (pilotosDnfMap.get(piloto.idPiloto)) {
			continue;
		}
		var ponto = vdp_obterPonto(piloto);
		if (ponto == null || ponto.x == null || ponto.y == null) {
			continue;
		}
		var imgCarro = carrosImgMap.get(piloto.idPiloto);
		if (pilotosAereofolioMap.get(piloto.idPiloto)) {
			imgCarro = carrosImgSemAereofolioMap.get(piloto.idPiloto);
		}
		if ('SC' == piloto.idPiloto) {
			imgCarro = safetycar;
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
		var angulo = 0;
		var frenteCar;
		var atrasCar;
		var no = mapaIdNosSuave.get(piloto.idPiloto);
		if (!no) {
			no = mapaIdNos.get(piloto.idNo);
		}
		if (no.box) {
			var indexAtras = (no.index - 15) > 0 ? (no.index - 15) : 0;
			var indexFrente = (no.index + 15) < circuito.boxFull.length ? (no.index + 15)
					: circuito.boxFull.length - 1;

			frenteCar = safeArray(circuito.boxFull, indexFrente);
			atrasCar = safeArray(circuito.boxFull, indexAtras);
			if (piloto.tracado == 1) {
				frenteCar = safeArray(circuito.box1Full, indexFrente);
				atrasCar = safeArray(circuito.box1Full, indexAtras);
			}
			if (piloto.tracado == 2) {
				frenteCar = safeArray(circuito.box2Full, indexFrente);
				atrasCar = safeArray(circuito.box2Full, indexAtras);
			}
			angulo = gu_calculaAngulo(frenteCar, atrasCar, 180);
			if (piloto.idPiloto == idPilotoSelecionado) {
				pitLane = true;
			}
		} else {
			var indexAtras = (no.index - 15) > 0 ? (no.index - 15) : 0;
			var indexFrente = (no.index + 15) < circuito.pistaFull.length ? (no.index + 15)
					: circuito.pistaFull.length - 1;
			frenteCar = safeArray(circuito.pistaFull, no.index + 15);
			atrasCar = safeArray(circuito.pistaFull, no.index - 15);
			angulo = gu_calculaAngulo(frenteCar, atrasCar, 180);
			if (piloto.tracado == 2) {
				frenteCar = safeArray(circuito.pista2Full, indexFrente);
				atrasCar = safeArray(circuito.pista2Full, indexAtras);
			}
			if (piloto.tracado == 3) {
				frenteCar = safeArray(circuito.pista3Full, indexFrente);
				atrasCar = safeArray(circuito.pista3Full, indexAtras);
			}

			if (piloto.tracado == 4) {
				frenteCar = circuito.pista4Full[indexFrente];
				if (frenteCar == null) {
					frenteCar = safeArray(circuito.pista2Full, indexFrente);
				}
				atrasCar = circuito.pista4Full[indexAtras];
				if (atrasCar == null) {
					atrasCar = safeArray(circuito.pista2Full, indexAtras);
				}
				angulo = gu_calculaAngulo(frenteCar, atrasCar, 180);
			}
			if (piloto.tracado == 5) {
				frenteCar = circuito.pista5Full[indexFrente];
				if (frenteCar == null) {
					frenteCar = safeArray(circuito.pista1Full, indexFrente);
				}
				atrasCar = circuito.pista5Full[indexAtras];
				if (atrasCar == null) {
					atrasCar = safeArray(circuito.pista1Full, indexAtras);
				}
				angulo = gu_calculaAngulo(frenteCar, atrasCar, 180);
			}
			if (piloto.idPiloto == idPilotoSelecionado) {
				pitLane = false;
			}
		}

		var x = ponto.x - ptBg.x - (imgCarro.width / 2);
		var y = ponto.y - ptBg.y - (imgCarro.height / 2);
		pilotosEfeitosMap.set(piloto.idPiloto, true);
		var emMovimento = vdp_emMovimento(piloto.idPiloto);
		if (emMovimento && pilotosEfeitosMap.get(piloto.idPiloto)) {
			vdp_desenhaRastroFaiscaFx(piloto, x, y, angulo);
		}
		if (desenhaImagens) {
			var rotacionarCarro = vdp_rotacionar(imgCarro, angulo);
			var blendCarro = vdp_blendCarro(rotacionarCarro, ponto, x, y, no,
					piloto.idPiloto);
			maneContext.drawImage(blendCarro, x, y);
		}
		if (emMovimento && pilotosEfeitosMap.get(piloto.idPiloto)) {
			vdp_desenhaTravadaRoda(piloto, x, y, angulo);
			vdp_desenhaTravadaRodaFumaca(piloto, x, y, angulo, no);
			vdp_desenhaRastroChuvaFx(piloto, x, y, angulo, no);
		}
		if (piloto.idPiloto == idPilotoSelecionado) {
			ponto = vdp_obterPonto(piloto, true);
			if (ponto == null || ponto.x == null || ponto.y == null) {
				continue;
			}
			x = ponto.x - ptBg.x;
			y = ponto.y - ptBg.y;
			maneContext.beginPath();
			maneContext.fillStyle = 'pink';
			maneContext.fillRect(x - 5, y - 5, 10, 10);
			maneContext.closePath();
			maneContext.stroke();

		}

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

	if (cvBg != null) {
		if (x < 0) {
			x = 0;
		}
		if (y < 0) {
			y = 0;
		}

		var sW = maneCanvas.width;
		if ((x + sW) > cvBg.width) {
			x -= ((x + sW) - cvBg.width);
		}
		var sH = maneCanvas.height;
		if ((y + sH) > cvBg.height) {
			y -= ((y + sH) - cvBg.height);
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

function vdp_rotacionar(imgCarro, angulo) {
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

function vdp_blendCarro(imgCarro, ptCarro, xCarro, yCarro, no, idPiloto) {
	var maiorLado = 0;
	if (imgCarro.width > imgCarro.height) {
		maiorLado = imgCarro.width;
	} else {
		maiorLado = imgCarro.height;
	}
	cvBlend.width = maiorLado;
	cvBlend.height = maiorLado;
	ctxBlend.drawImage(imgCarro, 0, 0);
	var rectCarro = {
		left : ptCarro.x - 10,
		top : ptCarro.y - 10,
		right : ptCarro.x + 10,
		bottom : ptCarro.y + 10
	};
	for (var j = 0; j < circuito.objetosNoTransparencia.length; j++) {
		var img = objImgPistaMap.get(j);
		var pontosTp = circuito.objetosNoTransparencia[j];
		if (pontosTp.transparenciaBox != no.box) {
			continue;
		}
		if (pontosTp.indexFim
				&& pontosTp.indexFim != 0
				&& pontosTp.indexInicio
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
				&& vdp_intersectRect(rectCarro, rectObj)) {
			var x = pontosTp.x - ptBg.x - xCarro;
			var y = pontosTp.y - ptBg.y - yCarro;
			ctxBlend.globalCompositeOperation = blendOp;
			ctxBlend.drawImage(img, x, y);
			pilotosEfeitosMap.set(idPiloto, false);
		}
	}

	return cvBlend;
}

function vdp_desenhaBackGround() {
	maneContext.clearRect(0, 0, maneCanvas.width, maneCanvas.height);
	var sW = maneCanvas.width;
	var sH = maneCanvas.height;
	if (desenhaImagens) {
		try {
			maneContext.drawImage(cvBg, ptBg.x, ptBg.y, sW, sH, 0, 0,
					maneCanvas.width, maneCanvas.height);
		} catch (e) {
			console.log('vdp_desenhaBackGround');
			console.log(e);
		}
	}
}

function vdp_desenhaRastroFaiscaFx(piloto, x, y, angulo) {
	if (!pilotosFaiscaMap.get(piloto.idPiloto)
			|| pilotosFaiscaMap.get(piloto.idPiloto) <= 0) {
		return;
	}
	if (dadosParciais.clima == "chuva.png") {
		return;
	}
	var frenteCar, atrasCar;
	cvFx.width = 172;
	cvFx.height = 172;
	frenteCar = {
		x : Math.round(cvFx.width * 0.4),
		y : Math.round(cvFx.height * 0.5)
	};
	atrasCar = {
		x : Math.round(cvFx.width * 0.7),
		y : Math.round(cvFx.height * 0.5)
	};

	for (var i = 0; i < 10; i++) {
		var p1 = {
			x : intervalo((frenteCar.x - intervalo(2.5, 6)),
					(frenteCar.x + intervalo(2.5, 6))),
			y : intervalo((frenteCar.y - intervalo(2.5, 6)),
					(frenteCar.y + intervalo(2.5, 6)))
		};
		var p2 = {
			x : intervalo((atrasCar.x - intervalo(2.5, 15)),
					(atrasCar.x + intervalo(2.5, 15))),
			y : intervalo((atrasCar.y - intervalo(2.5, 15)),
					(atrasCar.y + intervalo(2.5, 15)))
		};
		var anguloFaisca = gu_calculaAngulo(p1, p2, 180);
		var ptDest = gu_calculaPonto(anguloFaisca, intervalo(43, 86), p1);
		ctxFx.beginPath();
		ctxFx.strokeStyle = corFaisca;
		ctxFx.setLineDash([ intervalo(5, 10), intervalo(10, 15) ]);
		ctxFx.moveTo(p1.x, p1.y);
		ctxFx.lineTo(ptDest.x, ptDest.y);
		ctxFx.closePath();
		ctxFx.stroke();
	}
	var rotacionar = vdp_rotacionar(cvFx, angulo);
	maneContext.drawImage(rotacionar, x - 40, y - 40);
	pilotosFaiscaMap.set(piloto.idPiloto,
			pilotosFaiscaMap.get(piloto.idPiloto) - 1);
}

function vdp_desenhaRastroChuvaFx(piloto, x, y, angulo, no) {
	if (dadosParciais.clima != "chuva.png") {
		return;
	}
	var frenteCar, atrasCar;
	cvFx.width = 344;
	cvFx.height = 344;
	frenteCar = {
		x : Math.round(cvFx.width * 0.45),
		y : Math.round(cvFx.height * 0.5)
	};
	atrasCar = {
		x : Math.round(cvFx.width * 0.95),
		y : Math.round(cvFx.height * 0.5)
	};

	for (var i = 0; i < 15; i++) {
		var p1 = {
			x : intervalo((frenteCar.x - intervalo(1, 10)),
					(frenteCar.x + intervalo(1, 10))),
			y : intervalo((frenteCar.y - intervalo(1, 10)),
					(frenteCar.y + intervalo(1, 10)))
		};

		var p2 = {
			x : intervalo((atrasCar.x - intervalo(10, 60)),
					(atrasCar.x + intervalo(10, 60))),
			y : intervalo((atrasCar.y - intervalo(10, 60)),
					(atrasCar.y + intervalo(10, 60)))
		};
		var anguloFx = gu_calculaAngulo(p1, p2, 180);
		var tam;
		if (no.retaOuLargada) {
			tam = 180;
		} else if (no.curvaAlta) {
			tam = 150;
		} else if (no.curvaBaixa) {
			tam = 100;
		}

		var ptDest = gu_calculaPonto(anguloFx, intervalo(20, tam), p1);
		ctxFx.beginPath();
		ctxFx.strokeStyle = corChuva;
		ctxFx.moveTo(p1.x, p1.y);
		ctxFx.lineTo(ptDest.x, ptDest.y);
		ctxFx.closePath();
		ctxFx.stroke();
	}
	var rotacionar = vdp_rotacionar(cvFx, angulo);
	maneContext.drawImage(rotacionar, x - 126, y - 126);
}

function vdp_desenhaTravadaRodaFumaca(piloto, x, y, angulo, no) {
	if (!pilotosTravadaFumacaMap.get(piloto.idPiloto)
			|| pilotosTravadaFumacaMap.get(piloto.idPiloto) <= 0) {
		return;
	}
	if (dadosParciais.clima == "chuva.png") {
		return;
	}
	var rotacionar;
	var sw = Math.round(intervalo(1, 5));
	var lado = (Math.random() > 0.5 ? 'D' : 'E');
	var noReal = mapaIdNos.get(piloto.idNo);
	if ((circuito.pista4Full[no.index] != null)
			|| (circuito.pista4Full[noReal.index] != null)) {
		lado = 'E'
	} else if ((circuito.pista5Full[no.index] != null)
			|| (circuito.pista5Full[noReal.index] != null)) {
		lado = 'D'
	}

	eval('rotacionar = vdp_rotacionar(carroCimaFreios' + lado + sw
			+ ', angulo)');
	maneContext.drawImage(rotacionar, x, y);
	pilotosTravadaFumacaMap.set(piloto.idPiloto, pilotosTravadaFumacaMap
			.get(piloto.idPiloto) - 1);
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

function vdp_desenhaTravadaRoda(piloto, x, y, angulo) {
	if (!pilotosTravadaMap.get(piloto.idPiloto)) {
		return;
	}
	if (dadosParciais.clima == "chuva.png") {
		return;
	}
	var rotacionar;
	var sw = Math.round(intervalo(0, 2));
	if (sw == 0) {
		rotacionar = vdp_rotacionar(travadaRoda0, angulo);
	} else if (sw == 1) {
		rotacionar = vdp_rotacionar(travadaRoda1, angulo);
	} else if (sw == 2) {
		rotacionar = vdp_rotacionar(travadaRoda2, angulo);
	}
	ctxBg.drawImage(rotacionar, x + ptBg.x, y + ptBg.y);
	pilotosTravadaMap.set(piloto.idPiloto, false);
}