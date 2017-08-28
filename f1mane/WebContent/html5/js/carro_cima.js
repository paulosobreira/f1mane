var carrosImgMap;
var cvRotate = document.createElement('canvas');
var ctxRotate = cvRotate.getContext('2d');


function rotacionarCarro(imgCarro,angulo){
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