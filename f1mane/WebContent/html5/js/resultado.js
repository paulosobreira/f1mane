var idPilotoSelecionado, temporadaSelecionada, circuitoSelecionado;
var token = localStorage.getItem("token");
var nomeJogo = localStorage.getItem("nomeJogo");

$('#trocaPneuCheck').append(lang_text('trocaPneus'));
$('#reabastecimentoCheck').append(lang_text('reabastecimento'));
$('#153').html(lang_text('153'));
$('#154').html(lang_text('154'));

if (token == null) {
	token = getParameter('token');
}
if (nomeJogo == null) {
	nomeJogo = getParameter('nomeJogo');
}

function resultado() {
	$.ajax({
		type : "GET",
		headers : {
			'token' : token
		},
		url : "/f1mane/rest/letsRace/dadosJogo?nomeJogo=" + nomeJogo,
		contentType : "application/json",
		dataType : "json",
		success : function(dadosJogo) {
			if ('24' != dadosJogo.estado) {
				window.location.href = "index.html";
				return;
			}
			$('#imgCircuito').attr('src', '/f1mane/rest/letsRace/circuitoMini/' + dadosJogo.arquivoCircuito);
			circuitoSelecionado = dadosJogo.arquivoCircuito;
			idPilotoSelecionado = dadosJogo.idPilotoSelecionado;
			temporadaSelecionada = dadosJogo.temporada;
			$('#circuitosLabel').html(dadosJogo.nomeCircuito);
			$('#temporadasLabel').html(dadosJogo.temporada);
			if(dadosJogo.trocaPneu){
				$('#trocaPneuCheck').removeClass('text-muted');
			}else{
				$('#trocaPneuCheck').addClass('text-muted');
			}
			if(dadosJogo.reabastecimento){
				$('#reabastecimentoCheck').removeClass('text-muted');
			}else{
				$('#reabastecimentoCheck').addClass('text-muted');
			}
			if(dadosJogo.ers){
				$('#ersCheck').removeClass('text-muted');
			}else{
				$('#ersCheck').addClass('text-muted');
			}
			if(dadosJogo.drs){
				$('#drsCheck').removeClass('text-muted');
			}else{
				$('#drsCheck').addClass('text-muted');
			}
			var pilotos = dadosJogo.pilotos;
			$('#pilotos').find('tr').remove();
			$.each(pilotos, function(i, val) {
				montaLinhaGridResultado(i, pilotos[i]);
			});
		},
		error : function(xhRequest, ErrorText, thrownError) {
			tratamentoErro(xhRequest);
			console.log(xhRequest.status + '  '
					+ xhRequest.responseText + ' ' + ErrorText);
		}
	});
}

function montaLinhaGridResultado(i, piloto) {
	var tr = $('<tr style="cursor: pointer; cursor: hand" />');
	var td1 = $('<td scope="row"/>');
	if(piloto.desqualificado){
		td1.append('DNF ');
	}else{
		td1.append('P'+(i+1)+' ');
	}
	td1.append(piloto.nome);
	td1.append('<br>');
	td1.append('Box : ');
	td1.append(piloto.qtdeParadasBox);
	td1.append('<br>');
	td1.append(lang_text('216')+' : ');
	td1.append(piloto.carro.porcentagemDesgastePneus + '%');
	td1.append('<br>');
	td1.append(lang_text('215')+' : ');
	td1.append(piloto.carro.porcentagemCombustivel + '%');
	td1.append('<br>');
	td1.append(lang_text('217')+' : ');
	td1.append(piloto.carro.porcentagemDesgasteMotor + '%');
	td1.append('<br>');
	var capacete = $('<img class="img-responsive img-responsive-line img-left"/>');
	capacete.attr('src', '/f1mane/rest/letsRace/capacete?id=' + piloto.id + '&temporada=' + temporadaSelecionada);
	var capacetes = $('<div style="display:  inline-flex;"  />');
	capacetes.append(capacete);
	if(piloto.imgJogador!=null){
		var imgJogador = $('<img class="img-responsive img-center userPic"/>');	
		imgJogador.attr('src', piloto.imgJogador);
		capacetes.append(imgJogador);
	}
	var pneu = $('<img class="img-responsive img-responsive-line img-right"/>');
	if (piloto.carro.tipoPneu == 'TIPO_PNEU_MOLE') {
		pneu.attr('src', 'img/pneuMole.png');
	} else if (piloto.carro.tipoPneu == 'TIPO_PNEU_DURO') {
		pneu.attr('src', 'img/pneuDuro.png');
	} else if (piloto.carro.tipoPneu == 'TIPO_PNEU_CHUVA') {
		pneu.attr('src', 'img/pneuChuva.png');
	}
	capacetes.append(pneu);
	td1.append(capacetes);
	tr.append(td1);

	var td2 = $('<td/>');
	td2.append(piloto.nomeCarro);
	td2.append('<br>');
	if(piloto.nomeJogador!=null){
		td2.append('<b>'+piloto.nomeJogador+'</b>');
		td2.append('<br>');
	}
	td2.append(lang_text('079'));
	if(piloto.melhorVolta){
		td2.append(piloto.melhorVolta.tempoVoltaFormatado);
	}else{
		td2.append('-');
	}
	td2.append('<br>');
	td2.append(lang_text('vantagem')+' : ');
	td2.append(piloto.vantagem);
	td2.append('<br>');
	td2.append(lang_text('ranking')+' : ');
	if(piloto.diferencaPosiscoesCorrida>0){
		td2.append('+');
	}
	td2.append(piloto.diferencaPosiscoesCorrida);
	td2.append('P');
	td2.append('<br>');
	td2.append(lang_text('161')+' : ');
	td2.append(piloto.pontosCorrida);
	if(piloto.porcentagemPontosCorrida!=null && piloto.porcentagemPontosCorrida>0){
		td2.append(' ('+piloto.porcentagemPontosCorrida+"%)");
	}
	td2.append('<br>');
	var carroLado = $('<img class="img-responsive img-center"/>');
	carroLado.attr('src', '/f1mane/rest/letsRace/carroLado?id=' + piloto.id + '&temporada=' + temporadaSelecionada);
	td2.append(carroLado);
	tr.append(td2);
	if (piloto.id == idPilotoSelecionado) {
		tr.addClass('success');
	} else if (piloto.nomeJogador) {
		tr.addClass('warning');
	}
	$('#pilotos').append(tr);
}

resultado();