var maneCanvas = document.getElementById('maneCanvas')
var maneContext = maneCanvas.getContext('2d');
var imgBg = new Image();
var ptBg = {
	x : 0,
	y : 0
};
var circuito;
var dadosJogo;
var posicaoPilotos;
var carrosImgMap;


function mapaDeCarros() {
	if(dadosJogo==null){
		return;
	}
	if(carrosImgMap!=null){
		return;
	}
	carrosImgMap = new Map();
	for (i = 0; i < dadosJogo.pilotosList.length; i++) {
		var pilotos = dadosJogo.pilotosList[i];
		var imgCarro =  new Image();
		imgCarro.src = "../rest/teste/carroCima";
		carrosImgMap.set(pilotos.id, imgCarro);
	}		
}



function render() {
	maneContext.clearRect(0, 0, maneCanvas.width, maneCanvas.height);
	maneCanvas.width = window.innerWidth;
	maneCanvas.height = window.innerHeight;
	var sW = maneCanvas.width;
	var sH = maneCanvas.height;
	if(imgBg.src!=""){
		try {
			maneContext.drawImage(imgBg, ptBg.x, ptBg.y, sW, sH, 0, 0,
					maneCanvas.width, maneCanvas.height);			
		} catch (e) {
			// TODO: handle exception
		}

	}else if(circuito !=null){
		imgBg.src = "../sowbreira/f1mane/recursos/"+circuito.backGround;
	//	imgBg.src = "../sowbreira/f1mane/recursos/testeBG_mro.jpg";
	}

	desenhaObjs();
	if (fps != null) {
		maneContext.fillText("FPS: " + fps.frameRate(), 4, 30);
	}
	if (circuito != null && circuito.backGround != null) {
		maneContext.fillText("Circuito: " + circuito.backGround, 4, 60);
	}
}

function desenhaObjs() {
	if (circuito == null || circuito.objetosNoTransparencia == null
			|| posicaoPilotos == null) {
		return;
	}

	maneContext.fillStyle = '#babaca';
	maneContext.beginPath();
	for (i = 0; i < circuito.pista.length; i++) {
		var pt = circuito.pista[i];
		if (i == 0) {
			maneContext.moveTo(pt.x - ptBg.x, pt.y - ptBg.y);
		} else {
			maneContext.lineTo(pt.x - ptBg.x, pt.y - ptBg.y);
		}
	}
	maneContext.stroke();
	maneContext.closePath();

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

	for (i = 0; i < posicaoPilotos.posis.length; i++) {
		var pos = posicaoPilotos.posis[i];
		var ponto = circuito.pistaFull[pos.idNo];
		if(ponto==null){
			continue;
		}
		if (i == 0) {
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
		if (ponto != null && ponto.x != null && ponto.y != null) {
			mapaDeCarros();
			maneContext.fillText("Piloto: " + pos.idPiloto, ponto.x - ptBg.x,
					ponto.y - ptBg.y);
            if(carrosImgMap!=null){
				var imgCarro = carrosImgMap.get(pos.idPiloto);
				maneContext.drawImage(imgCarro, ponto.x - ptBg.x, ponto.y - ptBg.y);					
			}					
		}
	}
}

// update canvas with some information and animation
var fps = new FpsCtrl(60, function(e) {
	render();
})

// start the loop
fps.start();

rest_criarJogo();