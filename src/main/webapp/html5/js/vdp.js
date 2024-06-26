/**
 * Controle video e desenho
 */
var mapaIdNos;
var mapaIdPilotosNosSuave;
var mapaTracadoSuave = new Map();
var mapaTracadoSuaveVaiPara = new Map();
var mapaIndexTracadoSuave = new Map();
var mapaGanhoSuave = new Map();
var mapaPontoSuave = new Map();
var mapaRastroChuva = new Map();
var pilotosEfeitosMap = new Map();
var measureText = new Map();
var rotateCache = true;
var cvBlend = document.createElement('canvas');
var ctxBlend = cvBlend.getContext('2d');
var fxArray = [];
var fxChuvaRetaArray = [];
var fxChuvaAltaArray = [];
var fxChuvaBaixaArray = [];
var borrachaNaPista = [];
var cvBg;
var ctxBg;
var maneCanvas = document.getElementById('maneCanvas')
var maneContext = maneCanvas.getContext('2d');
var desenhaImagens = true;
var pitLane = false;
var desenhouMarcasLargadaGrid = false;
var ptBg = {
    x: 0,
    y: 0
};
var largura = 0
var altura = 0;
var descontoCanvasY = 0;
var descontoCanvasX = 0;
var rectBg = {
    left: 0,
    top: 0,
    right: 0,
    bottom: 0
};
var rectObj = {
    left: 0,
    top: 0,
    right: 0,
    bottom: 0
};
var pontoColisaoArray = new Array();
var blendOp = 'destination-out'
var alphaCorCeu = 0.0;
var corCeu = "rgba(255, 255, 255, " + alphaCorCeu + ")";
var corChuva = "rgba(255, 255, 255,0.5)";
var corFaisca = "rgba(255, 255, 0 ,0.5)";
var vdp_amarelo = "rgba(255, 255, 0, 0.6)";
var vdp_vermelho = "rgba(255, 0, 0, 0.6)";
var vdp_verde = "rgba(0, 255, 0, 0.6)";
maneCanvas.width = 0;
maneCanvas.height = 0;
var alertaAerefolioPow = null;
var mapaPow = new Map();
var loopPilotos = false;
var ajsChuvaX = 54;
var ajsChuvaY = 54;
var ajsFxX = 40;
var ajsFxY = 40;

var zoom = 1;

var ctlCanvas = document.createElement('canvas');
var ctlContext = ctlCanvas.getContext('2d');

var imgFaroisAcesosCont = 0;
var imgFaroisApagadosCont = 0;

var funqueue = [];

var fps;

function vdp_desenha(fps_p) {
    fps = fps_p;
    vdp_processaAlturaLargura();
    vdp_processaBackGround();
    if (dadosJogo == null || circuito == null || !ativo || !cvBg ||
        contCarregouMidia != 0) {
        return;
    }
    vdp_atualizaSuave();
    vdp_centralizaPilotoSelecionado();
    vdp_desenhaBackGround();
    vdp_desenhaMarcasLargadaGrid();
    vdp_desenhaBorrachaNaPista();
    vdp_desenhaCarrosCima();
    vdp_desenhaPontosCarrosCima();
    vdp_desenhaNomesCima();
    vdp_desenhaClima();
    vdp_pow();
    vdp_desenhaFarois();
    vdp_ctl();
    vdp_debugRectBg();
}

function vdp_desenhaBorrachaNaPista() {
    if (zoom != 1) {
        return;
    }
    if (dadosParciais == null) {
        return;
    }
    if (dadosParciais.clima == "png/chuva.png") {
        borrachaNaPista = [];
        return;
    }
    for (var i = 0; i < borrachaNaPista.length; i++) {
        var borracha = borrachaNaPista[i];

        rectObj.left = borracha.x;
        rectObj.top = borracha.y;
        rectObj.right = borracha.x + borracha.img.width;
        rectObj.bottom = borracha.y + borracha.img.height;

        if (!vdp_intersectRect(rectBg, rectObj)) {
            continue;
        }
        var x = borracha.x - ptBg.x;
        var y = borracha.y - ptBg.y;
        desenha(maneContext, borracha.img, x, y);
    }
}

function vdp_processaAlturaLargura() {
    maneCanvas.width = window.innerWidth;
    maneCanvas.height = window.innerHeight;
    if (localStorage.getItem('tela') == 'menor') {
        if (maneCanvas.width > maneCanvas.height) {
            descontoCanvasX = ((maneCanvas.width * 20) / 100);
            descontoCanvasY = 0;
        } else {
            descontoCanvasY = ((maneCanvas.height * 20) / 100);
            descontoCanvasX = 0;
        }
    }
    altura = maneCanvas.height - (2 * descontoCanvasY);
    largura = maneCanvas.width - (2 * descontoCanvasX);
}

function vdp_processaBackGround() {
    if (imgBg && imgBg.complete) {
        if (imgBg.width == 0 || imgBg.height == 0) {
            imgBg.src = "/flmane/rest/letsRace/circuitoBg/" +
                circuito.backGround;
        }
    }
    if (imgBg && imgBg.complete) {
//        console.log('imgBg && imgBg.complete');
        cvBg = document.createElement('canvas');
        cvBg.width = imgBg.width;
        cvBg.height = imgBg.height;
        ctxBg = cvBg.getContext('2d');
        desenha(ctxBg, imgBg, 0, 0);
        imgBg = null;
    }
}

function vdp_ctl() {
    if (ctlCanvas.width > 0 && ctlCanvas.height > 0) {
        maneContext.drawImage(ctlCanvas, 0, 0);
    }
}

function vdp_debugRectBg() {
    if (desenhaImagens) {
        return;
    }
    maneContext.beginPath();
    maneContext.strokeStyle = "black"
    maneContext.rect(rectBg.left - ptBg.x, rectBg.top - ptBg.y,
        (rectBg.right - rectBg.left), (rectBg.bottom - rectBg.top));
    maneContext.closePath();
    maneContext.stroke();
}

function vdp_setup() {
    fxArray = [];
    fxChuvaRetaArray = [];
    fxChuvaAltaArray = [];
    fxChuvaBaixaArray = [];
    for (var i = 0; i < 3; i++) {
        vdp_gerarImgFaiscaFx();
        vdp_gerarRastroChuvaFx(120, 'R');
        vdp_gerarRastroChuvaFx(80, 'A');
        vdp_gerarRastroChuvaFx(60, 'B');
    }
}

function vdp_pow() {
    if (dadosParciais == null) {
        return;
    }
    var piloto = dadosParciais.posisPack.posis[posicaoCentraliza];

    if (alertaAerefolioPow == null && dadosParciais.alertaAerefolio) {
        alertaAerefolioPow = 20;
    }

    if (alertaAerefolioPow > 0 && !pitLane) {
        var ponto = vdp_obterPonto(piloto);
        if (ponto != null) {
            pontoColisaoArray.push(ponto);
        }
        alertaAerefolioPow--;
    }

    if (pitLane) {
        alertaAerefolioPow = null;
    }

    for (var i = 0; i < pontoColisaoArray.length; i++) {
        var ponto = pontoColisaoArray[i];
        var x = ponto.x - ptBg.x;
        var y = ponto.y - ptBg.y;

        rectObj.left = ponto.x;
        rectObj.top = ponto.y;
        rectObj.right = ponto.x + pow.width;
        rectObj.bottom = ponto.y + pow.height;

        if (!vdp_intersectRect(rectBg, rectObj)) {
            continue;
        }
        maneContext.beginPath();
        desenha(maneContext, pow, x - 20, y - 17);
        maneContext.closePath();
        maneContext.stroke();
    }
    pontoColisaoArray = [];
}

function vdp_desenhaMarcasLargadaGrid() {
    if (desenhouMarcasLargadaGrid || circuito == null ||
        circuito.objetosNoTransparencia == null || dadosParciais == null ||
        dadosJogo == null || carrosImgMap == null || cvBg == null ||
        carrosImgMap == null || dadosParciais.estado != '13') {
        return;
    }
    var posicaoPilotos = dadosParciais.posisPack;
    for (var i = 0; i < posicaoPilotos.posis.length; i++) {
        var piloto = posicaoPilotos.posis[i];
        var no = mapaIdNos.get(piloto.idNo);
        var frenteCar = safeArray(circuito.pistaFull, no.index + eixoCarro);
        var atrasCar = safeArray(circuito.pistaFull, no.index - eixoCarro);
        var angulo = gu_calculaAngulo(frenteCar, atrasCar, 180);
        var ponto = vdp_obterPonto(piloto);
        var x = ponto.x - ptBg.x - 60;
        var y = ponto.y - ptBg.y - 60;
        var rotacionar = vdp_rotacionar(girdLargadaMarca, angulo);
        desenha(ctxBg, rotacionar, x + ptBg.x, y + ptBg.y);
    }
    desenhouMarcasLargadaGrid = true;
}
//@Deprecated
function vdp_loopCalculaGanhoSuave(diff, piloto) {
    var ganho = 0;
    var maxLoop = 1000;
    var no = vdp_no_suave(piloto);
    if (no.tipoJson == 'A') {
        maxLoop = 500;
    }
    if (no.tipoJson == 'B') {
        maxLoop = 250;
    }
    var inc = 45;
    for (var i = 0; i < maxLoop; i += inc) {
        // if (piloto.idPiloto == idPilotoSelecionado) {
        // console.log(' vdp_loopCalculaGanhoSuave '+i);
        // }
        ganho += 1;
        if (diff >= i && diff < i + inc) {
            break;
        }
    }
    return ganho;
}

function vdp_atualizaSuave() {
    if (circuito == null || circuito.objetosNoTransparencia == null ||
        dadosParciais == null || dadosJogo == null ||
        carrosImgMap == null || cvBg == null) {
        return;
    }
    var posicaoPilotos = dadosParciais.posisPack;
    for (var i = 0; i < posicaoPilotos.posis.length; i++) {
        var piloto = posicaoPilotos.posis[i];
        // if (piloto.idPiloto == idPilotoSelecionado) {
        // console.log(piloto.idPiloto);
        // }
        if (pilotosDnfMap.get(piloto.idPiloto)) {
            continue;
        }
        var noSuave = mapaIdPilotosNosSuave.get(piloto.idPiloto);
        var noReal = mapaIdNos.get(piloto.idNo);
        if (noSuave == null) {
            // if (piloto.idPiloto == idPilotoSelecionado) {
            // console.log(piloto.idPiloto + ' noSuave == null ');
            // }
            mapaIdPilotosNosSuave.set(piloto.idPiloto, noReal);
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
            // if (piloto.idPiloto == idPilotoSelecionado) {
            // console.log(piloto.idPiloto + ' noReal.box && !noSuave.box ');
            // }
            indexReal = indexReal + circuito.entradaBoxIndex;
        }
        if (!noReal.box && noSuave.box) {
            indexReal = (indexReal - circuito.saidaBoxIndex) +
                (circuito.boxFull.length - 1);
        }
        var diff = (indexReal - indexSuave);
        // var ganhoSuave = vdp_loopCalculaGanhoSuave(diff, piloto);
        var multi = diff / 100;

        var ganhoSuave = 2.5 * multi;

        if (noSuave.tipoJson == 'A') {
            ganhoSuave = 2.5 * multi;
        }
        if (noSuave.tipoJson == 'B') {
            ganhoSuave = 2 * multi
        }

        var ganhoSuaveAnt = mapaGanhoSuave.get(piloto.idPiloto)
        if (ganhoSuaveAnt == null) {
            ganhoSuaveAnt = 0;
        }
        var incGanhoSuaveAnt = 1;

        if (ganhoSuave > ganhoSuaveAnt) {
            ganhoSuave = ganhoSuaveAnt + incGanhoSuaveAnt;
        }
        if (ganhoSuave <= ganhoSuaveAnt) {
            if (noSuave.tipoJson == 'R') {
                incGanhoSuaveAnt = 1 / (ganhoSuave < 2 ? ganhoSuave :
                    (ganhoSuave / 2));
            }
            ganhoSuave = ganhoSuaveAnt - incGanhoSuaveAnt;
        }
        if (noSuave.tipoJson == 'R' && noReal.tipoJson == 'R' &&
            sleepDefault == sleepMain && ganhoSuaveAnt > ganhoSuave &&
            diff > intervaloInt(200, 300)) {
            ganhoSuave = ganhoSuaveAnt;
        }
        if (diff == 0) {
            ganhoSuave = 0;
        }
        if (ganhoSuave <= 0) {
            ganhoSuave = 0;
        }

        // if (piloto.idPiloto == idPilotoSelecionado) {
        // console.log('ganhoSuave ' + ganhoSuave + ' noSuave.tipoJson ' +
        // noSuave.tipoJson);
        // }

        mapaGanhoSuave.set(piloto.idPiloto, ganhoSuave);
        var novoIndex = noSuave.index + Math.round(ganhoSuave);
        if (novoIndex > indexReal) {
            // if (piloto.idPiloto == idPilotoSelecionado) {
            // console.log(piloto.idPiloto + ' novoIndex > indexReal ' +
            // novoIndex);
            // }
            novoIndex = indexReal;
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
            // if (piloto.idPiloto == idPilotoSelecionado) {
            // console.log(piloto.idPiloto + ' noReal.box && !noSuave.box &&
            // novoIndex >= circuito.entradaBoxIndex ' + novoIndex);
            // }
            noSuaveNovo = circuito.boxFull[novoIndex - circuito.entradaBoxIndex];
        }
        if (noSuave.box) {
            noSuaveNovo = circuito.boxFull[novoIndex];
        }
        if (!noReal.box && noSuave.box &&
            novoIndex >= (circuito.boxFull.length - 1)) {
            // if (piloto.idPiloto == idPilotoSelecionado) {
            // console.log(piloto.idPiloto + ' !no.box && noSuave.box &&
            // novoIndex > (circuito.boxFull.length - 1 ' + novoIndex);
            // }
            noSuaveNovo = circuito.pistaFull[novoIndex -
                (circuito.boxFull.length - 1) + circuito.saidaBoxIndex];
        }
        // if (noSuaveNovo == null) {
        // console.log(piloto.idPiloto + ' noSuaveNovo ' + noS);
        // }
        mapaIdPilotosNosSuave.set(piloto.idPiloto, noSuaveNovo);
        if (diff >= 1200) {
            // console.log(piloto.idPiloto + ' diff >= 1000 ' + novoIndex);
            // mapaIdPilotosNosSuave.set(piloto.idPiloto, noReal);
            mapaIdPilotosNosSuave.clear();
        }
        if (loopPilotos) {
            var piloto = posicaoPilotos.posis[i];
            var noSuave = mapaIdPilotosNosSuave.get(piloto.idPiloto);
            var noReal = mapaIdNos.get(piloto.idNo);
            var tracadoSuave = mapaTracadoSuave.get(piloto.idPiloto);
            if (tracadoSuave == null) {
                tracadoSuave = piloto.tracado;
            }
            mapaPontoSuave.set(piloto.idPiloto, vdp_pontoTracadoSuave(piloto,
                noSuave, noReal));
        }

    }
    if (!loopPilotos) {
        var posicaoPilotos = dadosParciais.posisPack;
        for (var i = 0; i < posicaoPilotos.posis.length; i++) {
            var piloto = posicaoPilotos.posis[i];
            var noSuave = mapaIdPilotosNosSuave.get(piloto.idPiloto);
            var noReal = mapaIdNos.get(piloto.idNo);
            var tracadoSuave = mapaTracadoSuave.get(piloto.idPiloto);
            if (tracadoSuave == null) {
                tracadoSuave = piloto.tracado;
            }
            mapaPontoSuave.set(piloto.idPiloto, vdp_pontoTracadoSuave(piloto,
                noSuave, noReal));
        }
        loopPilotos = true;
    }
}

function vdp_colisaoTracadoSuave(pilotoParam) {
    var tracadoSuaveVaiPara = mapaTracadoSuaveVaiPara.get(pilotoParam.idPiloto);
    if (tracadoSuaveVaiPara == null) {
        return false;
    }
    var noSuaveParam = vdp_no_suave(pilotoParam);
    if (noSuaveParam.box) {
        return false;
    }
    if (tracadoSuaveVaiPara == 4) {
        tracadoSuaveVaiPara = 2;
    }
    if (tracadoSuaveVaiPara == 5) {
        tracadoSuaveVaiPara = 1;
    }
    var posicaoPilotos = dadosParciais.posisPack;
    for (var i = 0; i < posicaoPilotos.posis.length; i++) {
        var piloto = posicaoPilotos.posis[i];
        if (pilotoParam.idPiloto == piloto.idPiloto) {
            continue;
        }
        if (pilotosDnfMap.get(piloto.idPiloto)) {
            continue;
        }
        var tracadoSuave = mapaTracadoSuave.get(piloto.idPiloto);
        if (tracadoSuave == null) {
            continue;
        }
        if (tracadoSuave == 4) {
            tracadoSuave = 2;
        }
        if (tracadoSuave == 5) {
            tracadoSuave = 1;
        }
        var condicaoColisao = tracadoSuave == tracadoSuaveVaiPara;

        if (!condicaoColisao) {
            continue;
        }
        var noSuave = vdp_no_suave(piloto);
        if (noSuave.box) {
            continue;
        }
        var nEixo = (eixoCarro);
        var indexAtras = (noSuave.index - nEixo) > 0 ? (noSuave.index - nEixo) :
            0;
        var indexFrente = (noSuave.index + nEixo) < circuito.pistaFull.length ? (noSuave.index + nEixo) :
            circuito.pistaFull.length - 1;
        if (noSuaveParam.index > indexAtras && noSuaveParam.index < indexFrente) {
            return true;
        }
    }
    return false;
}

function vdp_centralizaPilotoSelecionado() {
    if (dadosParciais == null || dadosParciais.posisPack == null) {
        if (circuito != null) {
            var pontoZoom = {
                x: circuito.creditosPonto.x / zoom,
                y: circuito.creditosPonto.y / zoom
            };
            vdp_centralizaPonto(pontoZoom);
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

function vdp_no_suave(piloto) {
    var no = mapaIdPilotosNosSuave.get(piloto.idPiloto);
    if (no == null) {
        no = mapaIdNos.get(piloto.idNo);
    }
    return no;
}

function vdp_obterPonto(piloto, real) {
    var no;
    var ponto;
    if (real) {
        no = mapaIdNos.get(piloto.idNo);
    } else {
        no = vdp_no_suave(piloto);
        ponto = mapaPontoSuave.get(piloto.idPiloto);
        if (ponto != null) {
            var pontoZoom = {
                x: ponto.x / zoom,
                y: ponto.y / zoom
            };
            return pontoZoom;
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
    var pontoZoom = {
        x: ponto.x / zoom,
        y: ponto.y / zoom
    };
    return pontoZoom;
}

function vdp_pontoTracado(tracado, no) {
    var ponto;
    if (tracado == 0) {
        ponto = circuito.pistaFull[no.index];
        if (no.box) {
            ponto = circuito.boxFull[no.index]
        }
    }
    if (tracado == 1) {
        ponto = circuito.pista1Full[no.index];
        if (no.box) {
            ponto = circuito.box1Full[no.index]
        }
    }
    if (tracado == 2) {
        ponto = circuito.pista2Full[no.index];
        if (no.box) {
            ponto = circuito.box2Full[no.index]
        }
    }
    if (tracado == 4) {
        ponto = circuito.pista4Full[no.index];
        if (ponto == null) {
            ponto = circuito.pista2Full[no.index];
        }
        if (no.box) {
            ponto = circuito.box2Full[no.index]
        }
    }
    if (tracado == 5) {
        ponto = circuito.pista5Full[no.index];
        if (ponto == null) {
            ponto = circuito.pista1Full[no.index];
        }
        if (no.box) {
            ponto = circuito.box1Full[no.index]
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

    var pontoSuave = vdp_pontoTracado(tracadoSuave, no);

    var colisao = false;
    if (zoom == 1 && vdp_containsRect(rectBg, pontoSuave) &&
        vdp_colisaoTracadoSuave(piloto)) {
        var ponto = vdp_obterPonto(piloto, false);
        if (ponto != null && ponto.x != null && ponto.y != null) {
            // pontoColisaoArray.push(ponto);
            colisao = true;
        }
    }

    var tracadoSuaveVaiPara = mapaTracadoSuaveVaiPara.get(piloto.idPiloto);
    if (tracadoSuaveVaiPara == null ||
        (tracadoSuaveVaiPara != piloto.tracado && !colisao && indexTracadoSuave == 0)) {
        tracadoSuaveVaiPara = piloto.tracado;
        mapaTracadoSuaveVaiPara.set(piloto.idPiloto, tracadoSuaveVaiPara);
        if (tracadoSuave == 4 || tracadoSuave == 5) {
            indexTracadoSuave = circuito.indiceTracadoForaPista * 2;
        } else {
            indexTracadoSuave = circuito.indiceTracado * 2;
        }
        mapaIndexTracadoSuave.set(piloto.idPiloto, indexTracadoSuave);
    }
    var ponto = vdp_pontoTracado(tracadoSuaveVaiPara, no);
    var linha = gu_bline(ponto, pontoSuave);
    if (indexTracadoSuave > linha.length - 1) {
        indexTracadoSuave = linha.length - 1;
    }
    if (indexTracadoSuave < 0) {
        indexTracadoSuave = 0;
    }
    var pontoTracadoSuave = linha[indexTracadoSuave];
    if (!colisao) {
        indexTracadoSuave--;
    }
    if (indexTracadoSuave <= 0) {
        indexTracadoSuave = 0;
        mapaTracadoSuave.set(piloto.idPiloto, tracadoSuaveVaiPara);
    }
    mapaIndexTracadoSuave.set(piloto.idPiloto, Math.round(indexTracadoSuave));
    return pontoTracadoSuave;
}

function vdp_desenhaNomesCima() {
    if (circuito == null || circuito.objetosNoTransparencia == null ||
        dadosParciais == null || dadosJogo == null ||
        carrosImgMap == null || cvBg == null || carrosImgMap == null) {
        return;
    }
    var posicaoPilotos = dadosParciais.posisPack;
    for (var i = 0; i < posicaoPilotos.posis.length; i++) {
        var piloto = posicaoPilotos.posis[i];
        var ponto = vdp_obterPonto(piloto);
        if (ponto == null || ponto.x == null || ponto.y == null) {
            continue;
        }
        var no = vdp_no_suave(piloto);

        if (!vdp_containsRect(rectBg, ponto)) {
            continue;
        }

        var x = ponto.x - ptBg.x - 30;
        var y = ponto.y - ptBg.y - 45;
        maneContext.beginPath();
        maneContext.font = '14px sans-serif';
        var nmPiloto;
        if (piloto.idPiloto == 'SC') {
            nmPiloto = 'Safety Car';
        } else {
            nmPiloto = pilotosMap.get(piloto.idPiloto).nomeAbreviado;
            var posicao = pilotosBandeirada.get(piloto.idPiloto);
            if (posicao == null) {
                posicao = (i + 1);
            }
            nmPiloto = posicao + ' ' + nmPiloto;
        }
        var laruraTxt = measureText.get(nmPiloto);
        if (laruraTxt == null) {
            laruraTxt = maneContext.measureText(nmPiloto).width + 10;
            measureText.set(nmPiloto, laruraTxt);
        }
        if (idPilotoSelecionado == piloto.idPiloto) {
            maneContext.strokeStyle = '#00ff00';
            maneContext.rect(x - 5, y, laruraTxt, 20);
        } else if (piloto.humano) {
            maneContext.strokeStyle = '#ffff00';
            maneContext.rect(x - 5, y, laruraTxt, 20);
        }
        maneContext.fillStyle = corFundo
        maneContext.fillRect(x - 5, y, laruraTxt, 20);
        maneContext.fillStyle = "black"
        maneContext.fillText(nmPiloto, x, y + 15);
        if (pilotosBandeirada.get(piloto.idPiloto) != null) {
            desenha(maneContext, bandeirada, x + 40, y - 5);
        }
        maneContext.closePath();
        maneContext.stroke();
    }
}

function vdp_desenhaPontosCarrosCima() {
    if (zoom == 1 || circuito == null ||
        circuito.objetosNoTransparencia == null || dadosParciais == null ||
        dadosJogo == null || carrosImgMap == null || cvBg == null ||
        carrosImgMap == null) {
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
        if (!vdp_containsRect(rectBg, ponto)) {
            continue;
        }
        var cor1 = '#a9a9a9';
        var cor2 = '#7F7F7F';
        var pl = pilotosMap.get(piloto.idPiloto);
        if (pl != null && pl.carro != null) {
            var cor1 = pl.carro.cor1Hex;
            var cor2 = pl.carro.cor2Hex;
        }

        maneContext.beginPath();
        maneContext.arc(ponto.x - ptBg.x, ponto.y - ptBg.y, 5, 0, 2 * Math.PI,
            false);
        maneContext.fillStyle = cor1;
        maneContext.fill();
        maneContext.lineWidth = 1;
        maneContext.strokeStyle = cor2;
        maneContext.stroke();
        maneContext.closePath();
    }
}

function vdp_desenhaCarrosCima() {
    if (zoom != 1 || circuito == null ||
        circuito.objetosNoTransparencia == null || dadosParciais == null ||
        dadosJogo == null || carrosImgMap == null || cvBg == null ||
        carrosImgMap == null) {
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
        if (!vdp_containsRect(rectBg, ponto)) {
            continue;
        }
        var angulo = 0;
        var frenteCar;
        var atrasCar;
        var no = vdp_no_suave(piloto);

        if (no.box) {
            var indexAtras = (no.index - eixoCarro) > 0 ? (no.index - eixoCarro) :
                0;
            var indexFrente = (no.index + eixoCarro) < circuito.boxFull.length ? (no.index + eixoCarro) :
                circuito.boxFull.length - 1;

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
            var indexAtras = (no.index - eixoCarro) > 0 ? (no.index - eixoCarro) :
                0;
            var indexFrente = (no.index + eixoCarro) < circuito.pistaFull.length ? (no.index + eixoCarro) :
                circuito.pistaFull.length - 1;
            frenteCar = safeArray(circuito.pistaFull, no.index + eixoCarro);
            atrasCar = safeArray(circuito.pistaFull, no.index - eixoCarro);
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

        var x = ponto.x - ptBg.x - 45;
        var y = ponto.y - ptBg.y - 45;
        var anguloGraus = Math.round(Math.degrees(angulo / 6));
        pilotosEfeitosMap.set(piloto.idPiloto, true);
        var emMovimento = vdp_emMovimento(piloto.idPiloto, piloto.idNo);
        var desenhaRastroFaiscaFx = null;
        var desenhaRastroChuvaFx = null;
        var desenhaTravadaRodaFumaca = null;
        if (desenhaImagens) {
            if (emMovimento && !no.box && localStorage.getItem('tela') != 'menor') {
                desenhaRastroFaiscaFx = vdp_desenhaRastroFaiscaFx(piloto,
                    angulo, anguloGraus);
                if (desenhaRastroFaiscaFx != null) {
                    var xj = x - ajsFxX;
                    var yj = y - ajsFxY;
                    var blendFaisca = vdp_blend(desenhaRastroFaiscaFx, ponto,
                        xj, yj, no, piloto.idPiloto, true);
                    if (blendFaisca) {
                        desenha(maneContext, blendFaisca, xj, yj);
                    }
                }
            }

            var rotacionarCarro = null;
            if (rotateCache) {
                if (pilotosAereofolioMap.get(piloto.idPiloto) ||
                    'SC' == piloto.idPiloto) {
                    rotacionarCarro = vdp_rotacionar(imgCarro, angulo);
                } else {
                    var pl = pilotosMap.get(piloto.idPiloto);
                    if (pl != null && pl.carro.id != null) {
                        var chave = pl.carro.id + "-" + anguloGraus;
                        rotacionarCarro = mapaRotacionar.get(chave);
                        if (rotacionarCarro == null) {
                            rotacionarCarro = vdp_rotacionar(imgCarro, angulo);
                            mapaRotacionar.set(chave, rotacionarCarro);
                        }
                    }
                }
            } else {
                rotacionarCarro = vdp_rotacionar(imgCarro, angulo);
            }

            var blendCarro = vdp_blend(rotacionarCarro, ponto, x, y, no,
                piloto.idPiloto);
            desenha(maneContext, blendCarro, x, y);
            if (emMovimento) {
                desenhaTravadaRodaFumaca = vdp_desenhaTravadaRodaFumaca(piloto,
                    no, angulo, anguloGraus);
                if (desenhaTravadaRodaFumaca != null) {
                    var blendFumaca = vdp_blend(desenhaTravadaRodaFumaca,
                        ponto, x, y, no, piloto.idPiloto, true);
                    if (blendFumaca) {
                        desenha(maneContext, blendFumaca, x, y);
                    }
                }
                desenhaRastroChuvaFx = vdp_desenhaRastroChuvaFx(piloto, no,
                    angulo, anguloGraus);
                if (desenhaRastroChuvaFx != null) {
                    var xj = x - ajsChuvaX;
                    var yj = y - ajsChuvaY;
                    var blendChuva = vdp_blend(desenhaRastroChuvaFx, ponto, xj,
                        yj, no, piloto.idPiloto);
                    desenha(maneContext, blendChuva, xj, yj);
                }
            }
        } else {
            maneContext.beginPath();

            maneContext.rect(ponto.x - ptBg.x - (imgCarro.width / 2), ponto.y -
                ptBg.y - (imgCarro.height / 2), imgCarro.width,
                imgCarro.height);
            maneContext.closePath();
            maneContext.stroke();
            maneContext.beginPath();
            maneContext.arc(ponto.x - ptBg.x, ponto.y - ptBg.y, 5, 0,
                2 * Math.PI, false);
            maneContext.strokeStyle = "black"
            maneContext.fill();
            maneContext.stroke();
            maneContext.closePath();
        }
        if (emMovimento && pilotosEfeitosMap.get(piloto.idPiloto)) {
            vdp_desenhaTravadaRoda(piloto, x, y, angulo, no);
        }

        if (ctl_showFps && piloto.idPiloto == idPilotoSelecionado) {
            ponto = vdp_obterPonto(piloto, true);
            if (ponto == null || ponto.x == null || ponto.y == null) {
                continue;
            }
            x = ponto.x - ptBg.x;
            y = ponto.y - ptBg.y;
            maneContext.beginPath();
            if (mapaIdNos.get(piloto.idNo).tipoJson == 'A') {
                maneContext.fillStyle = vdp_amarelo;
            } else if (mapaIdNos.get(piloto.idNo).tipoJson == 'B') {
                maneContext.fillStyle = vdp_vermelho;
            } else {
                maneContext.fillStyle = vdp_verde;
            }
            maneContext.fillRect(x - 5, y - 5, 10, 10);
            maneContext.closePath();
            maneContext.stroke();
        }

    }
}

function vdp_emMovimento(idPiloto, idNo) {
    if (!dadosParciais) {
        return false;
    }
    return idNoAnterior.get(idPiloto) != idNo;
}

function vdp_intersectRect(r1, r2) {
    return !(r2.left > r1.right || r2.right < r1.left || r2.top > r1.bottom || r2.bottom < r1.top);
}

function vdp_containsRect(r1, r2) {
    return r2.x > r1.left && r2.x < r1.right && r2.y > r1.top &&
        r2.y < r1.bottom;
}

function vdp_desenhaObjs() {
    if (circuito == null || circuito.objetosNoTransparencia == null ||
        dadosParciais == null) {
        return;
    }
    for (i = 0; i < circuito.objetosNoTransparencia.length; i++) {
        var pontosTp = circuito.objetosNoTransparencia[i];
        var img = objImgPistaMap.get(i);

        rectObj.left = pontosTp.x;
        rectObj.top = pontosTp.y;
        rectObj.right = pontosTp.x + img.width;
        rectObj.bottom = pontosTp.y + img.height;

        if (vdp_intersectRect(rectBg, rectObj)) {
            desenha(maneContext, img, pontosTp.x - ptBg.x, pontosTp.y - ptBg.y);
        }
    }
}

function vdp_centralizaPonto(ponto) {
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
        if ((x + sW) > (cvBg.width / zoom)) {
            x -= ((x + sW) - (cvBg.width / zoom));
        }
        var sH = maneCanvas.height;
        if ((y + sH) > (cvBg.height / zoom)) {
            y -= ((y + sH) - (cvBg.height / zoom));
        }
    }
    ptBg.x = x;
    ptBg.y = y;

    rectBg = {
        left: ptBg.x + descontoCanvasX,
        top: ptBg.y + descontoCanvasY,
        right: ptBg.x + largura + descontoCanvasX,
        bottom: ptBg.y + altura + descontoCanvasY
    };
}

function vdp_rotacionar(img, angulo) {
    var cvRotate = document.createElement('canvas');
    var ctxRotate = cvRotate.getContext('2d');
    var maiorLado = 0;
    if (img.width > img.height) {
        maiorLado = img.width;
    } else {
        maiorLado = img.height;
    }
    cvRotate.width = maiorLado;
    cvRotate.height = maiorLado;
    ctxRotate.translate(maiorLado / 2, maiorLado / 2);
    ctxRotate.rotate(angulo);
    desenha(ctxRotate, img, -maiorLado / 2, -maiorLado / 2);
    return cvRotate;
}

function vdp_blend(imgSrc, ptCarro, xCarro, yCarro, no, idPiloto, naoDesenha) {
    var maiorLado = 0;
    if (imgSrc.width > imgSrc.height) {
        maiorLado = imgSrc.width;
    } else {
        maiorLado = imgSrc.height;
    }
    cvBlend.width = maiorLado;
    cvBlend.height = maiorLado;
    var desenhouBlend = false;
    var rectCarro = {
        left: ptCarro.x - 10,
        top: ptCarro.y - 10,
        right: ptCarro.x + 10,
        bottom: ptCarro.y + 10
    };
    for (var j = 0; j < circuito.objetosNoTransparencia.length; j++) {
        var imgObj = objImgPistaMap.get(j);
        var pontosTp = circuito.objetosNoTransparencia[j];
        if (pontosTp.transparenciaBox != no.box) {
            continue;
        }
        if (pontosTp.indexFim &&
            pontosTp.indexFim != 0 &&
            pontosTp.indexInicio &&
            pontosTp.indexInicio != 0 &&
            (no.index < pontosTp.indexInicio || no.index > pontosTp.indexFim)) {
            continue;
        }

        rectObj.left = pontosTp.x;
        rectObj.top = pontosTp.y;
        rectObj.right = pontosTp.x + imgObj.width;
        rectObj.bottom = pontosTp.y + imgObj.height;

        if (imgObj && imgObj.width > 0 && imgObj.height > 0 &&
            vdp_intersectRect(rectCarro, rectObj)) {
            if (naoDesenha) {
                return null;
            }
            if (!desenhouBlend) {
                ctxBlend.clearRect(0, 0, cvBlend.width, cvBlend.height);
                desenha(ctxBlend, imgSrc, 0, 0);
                desenhouBlend = true;
            }
            var x = pontosTp.x - ptBg.x - xCarro;
            var y = pontosTp.y - ptBg.y - yCarro;
            ctxBlend.globalCompositeOperation = blendOp;
            desenha(ctxBlend, imgObj, x, y);
            pilotosEfeitosMap.set(idPiloto, false);
        }
    }
    if (!desenhouBlend) {
        return imgSrc;
    }

    return cvBlend;
}

function vdp_desenhaBackGround() {
    maneContext.clearRect(0, 0, window.innerWidth, window.innerHeight);
    var sW = zoom * (largura);
    var sH = zoom * (altura);
    if (desenhaImagens) {
        try {
            if (localStorage.getItem('tela') == 'menor') {
                maneContext.imageSmoothingEnabled = false;
            }
            maneContext.drawImage(cvBg, (ptBg.x + descontoCanvasX) * zoom,
                (ptBg.y + descontoCanvasY) * zoom, sW, sH, descontoCanvasX,
                descontoCanvasY, largura, altura);
        } catch (e) {
            console.log('vdp_desenhaBackGround');
            console.log(e);
        }
    }
}

function vdp_desenhaRastroFaiscaFx(piloto, angulo, anguloGraus) {
    if (!pilotosFaiscaMap.get(piloto.idPiloto) ||
        pilotosFaiscaMap.get(piloto.idPiloto) <= 0) {
        return null;
    }
    if (dadosParciais.clima == "png/chuva.png") {
        return null;
    }
    pilotosFaiscaMap.set(piloto.idPiloto,
        pilotosFaiscaMap.get(piloto.idPiloto) - 1);
    var intervalo = intervaloInt(0, fxArray.length - 1);
    var chave = intervalo + "-" + anguloGraus;
    var faisca = null;
    if (rotateCache) {
        faisca = mapaRastroFaisca.get(chave);
    }
    if (faisca != null) {
        return faisca;
    }
    var fn = function () {
        if (mapaRastroFaisca.get(chave) != null) {
            return;
        }
        var fx = fxArray[intervalo];
        faisca = vdp_rotacionar(fx, angulo);
        if (rotateCache) {
            mapaRastroFaisca.set(chave, faisca);
        }
    };
    funqueue.push(fn);
    return faisca;
}

function vdp_gerarImgFaiscaFx() {
    var cvFx = document.createElement('canvas');
    var ctxFx = cvFx.getContext('2d');
    var frenteCar, atrasCar;
    cvFx.width = 172;
    cvFx.height = 172;
    frenteCar = {
        x: Math.round(cvFx.width * 0.4),
        y: Math.round(cvFx.height * 0.5)
    };
    atrasCar = {
        x: Math.round(cvFx.width * 0.7),
        y: Math.round(cvFx.height * 0.5)
    };

    for (var i = 0; i < 10; i++) {
        var p1 = {
            x: intervaloInt((frenteCar.x - intervalo(2.5, 6)),
                (frenteCar.x + intervalo(2.5, 6))),
            y: intervaloInt((frenteCar.y - intervalo(2.5, 6)),
                (frenteCar.y + intervalo(2.5, 6)))
        };
        var p2 = {
            x: intervaloInt((atrasCar.x - intervalo(5, 20)),
                (atrasCar.x + intervalo(5, 20))),
            y: intervaloInt((atrasCar.y - intervalo(5, 20)),
                (atrasCar.y + intervalo(5, 20)))
        };
        var anguloFaisca = gu_calculaAngulo(p1, p2, 180);
        var ptDest = gu_calculaPonto(anguloFaisca, intervaloInt(43, 86), p1);
        ctxFx.beginPath();
        ctxFx.strokeStyle = corFaisca;
        ctxFx.setLineDash([intervaloInt(5, 10), intervaloInt(10, 15)]);
        ctxFx.moveTo(p1.x, p1.y);
        ctxFx.lineTo(ptDest.x, ptDest.y);
        ctxFx.closePath();
        ctxFx.stroke();
    }
    fxArray.push(cvFx);
}

function vdp_desenhaRastroChuvaFx(piloto, no, angulo, anguloGraus) {
    if (dadosParciais.clima != "png/chuva.png") {
        return null;
    }
    var lista;
    if (no.tipoJson == 'R') {
        lista = fxChuvaRetaArray;
    } else if (no.tipoJson == 'A') {
        lista = fxChuvaAltaArray;
    } else if (no.tipoJson == 'B') {
        lista = fxChuvaBaixaArray;
    } else {
        lista = fxChuvaBaixaArray;
    }
    var intervalo = intervaloInt(0, lista.length - 1);
    var chave = no.tipoJson + "-" + intervalo + "-" + anguloGraus;
    var chuva = null;
    if (rotateCache) {
        chuva = mapaRastroChuva.get(chave);
    }
    if (chuva != null) {
        return chuva;
    }
    var fx = lista[intervalo];
    var chuva = vdp_rotacionar(fx, angulo);
    if (rotateCache) {
        mapaRastroChuva.set(chave, chuva);
    }
    return chuva;
}

function vdp_gerarRastroChuvaFx(tam, lista) {
    var cvFx = document.createElement('canvas');
    var ctxFx = cvFx.getContext('2d');
    var frenteCar, atrasCar;
    cvFx.width = 200;
    cvFx.height = 200;
    frenteCar = {
        x: Math.round(cvFx.width * 0.44),
        y: Math.round(cvFx.height * 0.50)
    };
    atrasCar = {
        x: Math.round(cvFx.width * 0.7),
        y: Math.round(cvFx.height * 0.5)
    };

    for (var i = 0; i < 20; i++) {
        var p1 = {
            x: intervaloInt((frenteCar.x - intervalo(3, 9)),
                (frenteCar.x + intervalo(3, 9))),
            y: intervaloInt((frenteCar.y - intervalo(3, 9)),
                (frenteCar.y + intervalo(3, 9)))
        };
        var p2 = {
            x: intervaloInt((atrasCar.x - intervalo(3, 15)),
                (atrasCar.x + intervalo(3, 15))),
            y: intervaloInt((atrasCar.y - intervalo(3, 15)),
                (atrasCar.y + intervalo(3, 15)))
        };
        var anguloChuva = gu_calculaAngulo(p1, p2, 180);
        var ptDest = gu_calculaPonto(anguloChuva, intervaloInt(15, tam), p1);
        ctxFx.beginPath();
        ctxFx.strokeStyle = corChuva;
        ctxFx.setLineDash([intervaloInt(5, 15), intervaloInt(10, 15)]);
        ctxFx.moveTo(p1.x, p1.y);
        ctxFx.lineTo(ptDest.x, ptDest.y);
        ctxFx.closePath();
        ctxFx.stroke();
    }
    if (lista == 'R') {
        fxChuvaRetaArray.push(cvFx);
    }
    if (lista == 'A') {
        fxChuvaAltaArray.push(cvFx);
    }
    if (lista == 'B') {
        fxChuvaBaixaArray.push(cvFx);
    }
}

function vdp_desenhaTravadaRodaFumaca(piloto, no, angulo, anguloGraus) {
    if (!pilotosTravadaFumacaMap.get(piloto.idPiloto) ||
        pilotosTravadaFumacaMap.get(piloto.idPiloto) <= 0) {
        return null;
    }
    if (dadosParciais.clima == "png/chuva.png") {
        return null;
    }
    if (no.box) {
        return null;
    }
    var sw = Math.round(intervalo(1, 5));
    var lado = (Math.random() > 0.5 ? 'D' : 'E');
    var noReal = mapaIdNos.get(piloto.idNo);
    if ((circuito.pista4Full[no.index] != null) ||
        (circuito.pista4Full[noReal.index] != null)) {
        lado = 'E'
    } else if ((circuito.pista5Full[no.index] != null) ||
        (circuito.pista5Full[noReal.index] != null)) {
        lado = 'D'
    }
    pilotosTravadaFumacaMap.set(piloto.idPiloto, pilotosTravadaFumacaMap
        .get(piloto.idPiloto) - 1);

    var chave = lado + "-" + sw + "-" + anguloGraus;
    var fumaca = null
    if (rotateCache) {
        fumaca = mapaTravadaRodaFumaca.get(chave);
    }
    if (fumaca != null) {
        return fumaca;
    }
    var fn = function () {
        if (mapaTravadaRodaFumaca.get(chave) != null) {
            return;
        }
        var fx = eval('carroCimaFreios' + lado + sw);
        var fumaca = vdp_rotacionar(fx, angulo);
        if (rotateCache) {
            mapaTravadaRodaFumaca.set(chave, fumaca);
        }
    };
    funqueue.push(fn);
    return fumaca;
}

function vdp_precessaCorCeu() {
    if (!dadosParciais) {
        return;
    }
    if (dadosParciais.clima == "png/sol.png" && alphaCorCeu > 0) {
        alphaCorCeu -= 0.01;
        corCeu = "rgba(255, 255, 255, " + alphaCorCeu + ")";
    }
    if (dadosParciais.clima != "png/sol.png" && alphaCorCeu < 0.4) {
        alphaCorCeu += 0.01;
        corCeu = "rgba(255, 255, 255, " + alphaCorCeu + ")";
    }
}

function vdp_desenhaClima() {
    if (!dadosParciais) {
        return;
    }
    maneContext.fillStyle = corCeu;
    var maxX = largura + descontoCanvasX;
    var maxY = altura + descontoCanvasY;
    maneContext.fillRect(descontoCanvasX, descontoCanvasY, maxX, maxY);

    if (dadosParciais && dadosParciais.clima &&
        dadosParciais.clima == "png/chuva.png") {
        var p1;
        var p2;
        for (var i = descontoCanvasX; i < maxX; i += 20) {
            for (var j = descontoCanvasY; j < maxY; j += 20) {
                if (Math.random() > .8) {
                    p1 = {
                        x: i + 10,
                        y: j + 10
                    };
                    p2 = {
                        x: i + 15,
                        y: j + 20
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

function vdp_desenhaFarois() {
    if (!dadosParciais) {
        return;
    }
    if (dadosParciais.estado != "13") {
        if (imgFaroisApagadosCont < 30) {
            desenha(maneContext, imgFaroisApagados, centroX -
                (imgFarois.width / 2), 100);
            imgFaroisApagadosCont++;
        }
        return;
    }
    imgFaroisAcesosCont++;
    if (imgFaroisAcesosCont >= 160) {
        desenha(maneContext, imgFarois, centroX - (imgFarois.width / 2), 100);
    }
    if (imgFaroisAcesosCont < 160) {
        desenha(maneContext, imgFarois4, centroX - (imgFarois.width / 2), 100);
    }
    if (imgFaroisAcesosCont < 140) {
        desenha(maneContext, imgFarois3, centroX - (imgFarois.width / 2), 100);
    }
    if (imgFaroisAcesosCont < 120) {
        desenha(maneContext, imgFarois2, centroX - (imgFarois.width / 2), 100);
    }
    if (imgFaroisAcesosCont < 80) {
        desenha(maneContext, imgFarois1, centroX - (imgFarois.width / 2), 100);
    }
    if (imgFaroisAcesosCont < 40) {
        desenha(maneContext, imgFaroisApagados,
            centroX - (imgFarois.width / 2), 100);
    }
}

function vdp_desenhaTravadaRoda(piloto, x, y, angulo, no) {
    if (!pilotosTravadaMap.get(piloto.idPiloto)) {
        return;
    }
    if (dadosParciais.clima == "png/chuva.png") {
        return;
    }
    if (no.box) {
        return;
    }
    var fn = function () {
        var rotacionar;
        var sw = Math.round(intervalo(0, 2));
        if (sw == 0) {
            rotacionar = vdp_rotacionar(travadaRoda0, angulo);
        } else if (sw == 1) {
            rotacionar = vdp_rotacionar(travadaRoda1, angulo);
        } else if (sw == 2) {
            rotacionar = vdp_rotacionar(travadaRoda2, angulo);
        }
        //desenha(ctxBg, rotacionar, x + ptBg.x, y + ptBg.y);
        var borracha = {
            x: x + ptBg.x,
            y: y + ptBg.y,
            img: rotacionar
        };
        borrachaNaPista.push(borracha);
    };
    funqueue.push(fn);
    pilotosTravadaMap.set(piloto.idPiloto, false);
}