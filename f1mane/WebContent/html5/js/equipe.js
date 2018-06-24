/**
 * Controle de equipe
 */
$('#nomeEquipe').html(lang_text('254'));
$('#corEquipe1').html(lang_text('corEquipeCarro')+' 1 :');
$('#corEquipe2').html(lang_text('corEquipeCarro')+' 2 :');
$('#nomePiloto').html(lang_text('253'));
$('#nomePilotoAbreviado').html(lang_text('nomePilotoAbreviado'));
$('#pontosGanhos').html(lang_text('pontosGanhos'));
$('#pontosDisponiveis').html(lang_text('pontosDisponiveis'));
$('#habilidadePiloto').html(lang_text('255'));
$('#potenciaCarro').html(lang_text('256'));
$('#freioCarro').html(lang_text('freioCarro'));
$('#aerodinamicaCarro').html(lang_text('aerodinamicaCarro'));
var temporadaSelecionada;
listaTemporadas();
carregaEquipe();
$('.carousel').carousel({
	pause: true,
	interval: false
	});

$('#btnSalvar').bind("click", function() {
	salvarEquipe();
});

$('#btnJogar').bind("click", function() {
	localStorage.setItem("modoCarreira", true);
	window.location = "menus.html";
});

$('#temporadaCarousel').on('slide.bs.carousel', function(event) {
	selecionaTemporada($(event.relatedTarget).prop('temporada'));
	
});

$(document).on('click', '.number-spinner button', function() {
	var ptsConstrutores = parseInt($('#pontosConstrutoresValor').html());
	var btn = $(this), oldValue = btn.closest('.number-spinner').find('input').val().trim(), newVal = 0;
	oldValue =parseInt(oldValue);
	
	var tp = 0;

	if (btn.attr('data-dir') == 'up') {
		newVal = oldValue + 1;
		tp=1;
	} else {
		if (oldValue > 1) {
			newVal = oldValue - 1;
		} else {
			newVal = 0;
		}
		tp=-1;
	}

	var inc = 0;
	if (newVal >= 400 && newVal < 600) {
		inc = 1;
		if (oldValue == 600) {
			inc = 2;
		}
	} else if (newVal >= 600 && newVal < 700) {
		inc = 2;
		if (oldValue == 700) {
			inc = 10;
		}
	} else if (newVal >= 700 && newVal < 800) {
		inc = 10;
		if (oldValue == 800) {
			inc = 100;
		}
	} else if (newVal >= 800 && newVal < 900) {
		inc = 100;
		if (oldValue == 900) {
			inc = 200;
		}
	} else if (newVal >= 900 && newVal < 999) {
		inc = 200;
	}
	if(tp==1){
		ptsConstrutores-=inc;
	}
	if(tp==-1){
		ptsConstrutores+=inc;
	}
	
	$('#pontosConstrutoresValor').html(ptsConstrutores);
	btn.closest('.number-spinner').find('input').val(newVal);
});

function carregaEquipe() {
	var urlServico = "/f1mane/rest/letsRace/equipe";
	$.ajax({
		type : "GET",
		url : urlServico,
		headers : {
			'token' : localStorage.getItem("token"),
			'idioma' : localStorage.getItem('idioma')
		},
		contentType : "application/json",
		dataType : "json",
		success : function(response) {
			if (!response) {
				console.log('carregaEquipe() null');
				return;
			}
			if(response.nomeCarro!=null){
				$('#nomeEquipeValor').val(response.nomeCarro);
			}
			if(response.nomePiloto!=null){
				$('#nomePilotoValor').val(response.nomePiloto);
			}
			if(response.nomePilotoAbreviado!=null){
				$('#nomePilotoAbreviadoValor').val(response.nomePilotoAbreviado);
			}
			$('#habilidadePilotoValor').val(response.ptsPiloto);
			$('#pontosConstrutoresValor').html(response.ptsConstrutores);
			$('#pontosGanhosValor').html(response.ptsConstrutoresGanhos);
			$('#potenciaCarroValor').val(response.ptsCarro);
			$('#aerodinamicaCarroValor').val(response.ptsAerodinamica);
			$('#freioCarroValor').val(response.ptsFreio);
			$('#corEquipeValue1').val(rgbToHex(response.c1R,response.c1G,response.c1B));
			$('#corEquipeValue2').val(rgbToHex(response.c2R,response.c2G,response.c2B));
			$('#temporadaCapaceteLivery').val(response.temporadaCapaceteLivery);
			$('#temporadaCarroLivery').val(response.temporadaCarroLivery);
			$('#idCapaceteLivery').val(response.idCapaceteLivery);
			$('#idCarroLivery').val(response.idCarroLivery);
			if(response.temporadaCapaceteLivery !=null && response.idCapaceteLivery != null && response.idCapaceteLivery != 0){
				$('#idImgCapacete').attr('src', '/f1mane/rest/letsRace/capacete/' + response.temporadaCapaceteLivery + '/' + response.idCapaceteLivery);
			}else{
				$('#idImgCapacete').attr('src', '/f1mane/rest/letsRace/capacete/' + rgbToHexUrlSafe(response.c1R,response.c1G,response.c1B) + '/' + rgbToHexUrlSafe(response.c2R,response.c2G,response.c2B));
			}

			if(response.temporadaCarroLivery !=null && response.idCarroLivery != null && response.idCarroLivery != 0){
				$('#idImgCarroLado').attr('src', '/f1mane/rest/letsRace/carroLado/' + response.temporadaCarroLivery + '/' + response.idCarroLivery);
			}else{
				$('#idImgCarroLado').attr('src', '/f1mane/rest/letsRace/carroLado/' + rgbToHexUrlSafe(response.c1R,response.c1G,response.c1B) + '/' + rgbToHexUrlSafe(response.c2R,response.c2G,response.c2B));
			}
			
			if(response.temporadaCarroLivery !=null && response.idCarroLivery != null && response.idCarroLivery != 0){
				$('#idImgCarroCima').attr('src', '/f1mane/rest/letsRace/carroCima/' + response.temporadaCarroLivery + '/' + response.idCarroLivery);
			}else{
				$('#idImgCarroCima').attr('src', '/f1mane/rest/letsRace/carroCima/' + rgbToHexUrlSafe(response.c1R,response.c1G,response.c1B) + '/' + rgbToHexUrlSafe(response.c2R,response.c2G,response.c2B));
			}

			
			$('#idImgCapacete').bind("click", function() {
				$('#equipe').addClass('hidden');
				$('#escolha').removeClass('hidden');
				$('#capacetes').removeClass('hidden');
				$('#voltar').attr('href', 'equipe.html');
			});
			
			var clickCarro = function() {
				$('#equipe').addClass('hidden');
				$('#escolha').removeClass('hidden');
				$('#carros').removeClass('hidden');
				$('#voltar').attr('href', 'equipe.html');
			};
			
			$('#idImgCarroCima').bind("click",clickCarro );
			
			$('#idImgCarroLado').bind("click",clickCarro);
		},
		error : function(xhRequest, ErrorText, thrownError) {
			tratamentoErro(xhRequest);
			console.log('carregaEquipe() ' + xhRequest.status + '  ' + xhRequest.responseText);
		}
	});
}	

function objetoEquipe(){
	var c1 = hexToRgb($('#corEquipeValue1').val())
	var c2 = hexToRgb($('#corEquipeValue2').val())
	var dataObj = {
		nomeCarro : $('#nomeEquipeValor').val(),
		nomePiloto : $('#nomePilotoValor').val(),
		nomePilotoAbreviado : $('#nomePilotoAbreviadoValor').val(),
		ptsPiloto : $('#habilidadePilotoValor').val(),
		ptsCarro : $('#potenciaCarroValor').val(),
		ptsAerodinamica : $('#aerodinamicaCarroValor').val(),
		ptsFreio : $('#freioCarroValor').val(),
		ptsConstrutores: $('#pontosConstrutoresValor').html(),
		temporadaCapaceteLivery : $('#temporadaCapaceteLivery').val(),
		temporadaCarroLivery : $('#temporadaCarroLivery').val(),
		idCapaceteLivery : $('#idCapaceteLivery').val(),
		idCarroLivery : $('#idCarroLivery').val(),
		c1R : c1.r,
		c1G : c1.g,
		c1B : c1.b,
		c2R : c2.r,
		c2G : c2.g,
		c2B : c2.b
	};
	return dataObj;
}

function salvarEquipe() {
	var dataObj = objetoEquipe();
	var urlServico = "/f1mane/rest/letsRace/equipe";
	$.ajax({
		type : "POST",
		url : urlServico,
		headers : {
			'token' : localStorage.getItem("token"),
			'idioma' : localStorage.getItem('idioma')
		},
		contentType : "application/json",
		dataType : "json",
		data : JSON.stringify(dataObj),
		success : function(response) {
			toaster(lang_text('250'), 3000, 'alert alert-success');
			carregaEquipe();
		},
		error : function(xhRequest, ErrorText, thrownError) {
			tratamentoErro(xhRequest);
			console.log('salvarEquipe() ' + xhRequest.status + '  ' + xhRequest.responseText);
		}
	});
}

function gerarTrCapaceteCores(){
	var response = objetoEquipe();
	var trCapacete = $('<tr/>');
	var tdCapacete = $('<td/>');
	$('#tableCapacetes').append(trCapacete);
	trCapacete.append(tdCapacete);
	var div = $('<div class="row"/>');
	var capacete = $('<img class="img-responsive img-center"/>');
	capacete.attr('src', '/f1mane/rest/letsRace/capacete/' + rgbToHexUrlSafe(response.c1R,response.c1G,response.c1B) + '/' + rgbToHexUrlSafe(response.c2R,response.c2G,response.c2B));
	div.append(capacete);
	var divNome = $('<div class="row transbox textCenter"/>');
	divNome.append(response.nomePiloto+'<br>');
	divNome.append(lang_text('255')+' '+response.ptsPiloto+'<br>');
	div.append(divNome);
	div.bind("click", function() {
		$('#temporadaCapaceteLivery').val(null);
		$('#idCapaceteLivery').val(null);
		$('#idImgCapacete').attr('src', '/f1mane/rest/letsRace/capacete/' + rgbToHexUrlSafe(response.c1R,response.c1G,response.c1B) + '/' + rgbToHexUrlSafe(response.c2R,response.c2G,response.c2B));
		$('#escolha').addClass('hidden');
		$('#capacetes').addClass('hidden');
		$('#equipe').removeClass('hidden');
	});
	tdCapacete.append(div);
}
function gerarTrCarro(){
	var response = objetoEquipe();
	var trCarro1 = $('<tr/>');
	var trCarro2 = $('<tr/>');
	$('#tableCarro').append(trCarro1);
	$('#tableCarro').append(trCarro2);
	var tdCarro1 = $('<td/>');
	var tdCarro2 = $('<td/>');
	var tdCarro3 = $('<td/>');
	var tdCarro4 = $('<td/>');
	trCarro1.append(tdCarro1);
	trCarro1.append(tdCarro2);
	trCarro2.append(tdCarro3);
	trCarro2.append(tdCarro4);
	var div1 = $('<div/>');
	var carroCima = $('<img class="img-responsive img-center" />');
	carroCima.attr('src', '/f1mane/rest/letsRace/carroCima/' + rgbToHexUrlSafe(response.c1R,response.c1G,response.c1B) + '/' + rgbToHexUrlSafe(response.c2R,response.c2G,response.c2B));
	div1.append(carroCima);
	var div3 = $('<div class="row transbox textCenter" />');
	div3.append(response.nomeCarro+'<br>');
	div3.append(lang_text('256')+' '+response.ptsCarro);
	tdCarro1.append(div1);
	tdCarro3.append(div3);
	
	var div2 = $('<div/>');
	var carroLado = $('<img class="img-responsive img-center" style="margin-top: 20px;" />');
	carroLado.attr('src', '/f1mane/rest/letsRace/carroLado/' + rgbToHexUrlSafe(response.c1R,response.c1G,response.c1B) + '/' + rgbToHexUrlSafe(response.c2R,response.c2G,response.c2B));
	div2.append(carroLado);
	var div4 = $('<div class="row transbox textCenter" />');
	div4.append(lang_text('freioCarro')+' '+response.ptsFreio+'<br>');
	div4.append(lang_text('aerodinamicaCarro')+' '+response.ptsAerodinamica+'<br>');
	tdCarro2.append(div2);
	tdCarro4.append(div4);
	
	var click = function() {
		$('#temporadaCarroLivery').val(null);
		$('#idCarroLivery').val(null);
		$('#idImgCarroLado').attr('src', '/f1mane/rest/letsRace/carroLado/' + rgbToHexUrlSafe(response.c1R,response.c1G,response.c1B) + '/' + rgbToHexUrlSafe(response.c2R,response.c2G,response.c2B));
		$('#idImgCarroCima').attr('src', '/f1mane/rest/letsRace/carroCima/' + rgbToHexUrlSafe(response.c1R,response.c1G,response.c1B) + '/' + rgbToHexUrlSafe(response.c2R,response.c2G,response.c2B));
		$('#escolha').addClass('hidden');
		$('#carros').addClass('hidden');
		$('#equipe').removeClass('hidden');
	};
	div1.bind("click", click);
	div2.bind("click", click);
	div3.bind("click", click);
	div4.bind("click", click);
	
}


function selecionaTemporada(temporada) {
	var urlServico = "/f1mane/rest/letsRace/temporadas/" + temporada;
	$.ajax({
		type : "GET",
		url : urlServico,
		contentType : "application/json",
		dataType : "json",
		success : function(response) {
			if (!response) {
				console.log('selecionaTemporada() null');
				return;
			}
			temporadaSelecionada = temporada;
			$('#temporadasLabel').html(temporada);
			var pilotos = response.pilotos;
			var mapCarros = new Map();
			$('#tableCapacetes').find('tr').remove();
			$('#tableCarro').find('tr').remove();
			gerarTrCapaceteCores();
			gerarTrCarro();
			$.each(pilotos, function(i, val) {
				var piloto = pilotos[i];
				var trCapacete = $('<tr/>');
				var tdCapacete = $('<td/>');
				$('#tableCapacetes').append(trCapacete);
				trCapacete.append(tdCapacete);
				var div = $('<div class="row"/>');
				var capacete = $('<img class="img-responsive img-center"/>');
				capacete.attr('src', '/f1mane/rest/letsRace/capacete/' + temporadaSelecionada + '/' + piloto.id);
				div.append(capacete);
				var divNome = $('<div class="row transbox textCenter"/>');
				divNome.append(piloto.nome+'<br>');
				divNome.append(lang_text('255')+' '+piloto.habilidade+'<br>');
				div.append(divNome);
				div.bind("click", function() {
					$('#temporadaCapaceteLivery').val(temporadaSelecionada);
					$('#idCapaceteLivery').val(piloto.id);
					$('#idImgCapacete').attr('src', '/f1mane/rest/letsRace/capacete/' + temporadaSelecionada + '/' + piloto.id);
					$('#escolha').addClass('hidden');
					$('#capacetes').addClass('hidden');
					$('#equipe').removeClass('hidden');
				});
				tdCapacete.append(div);
				if(mapCarros.get(piloto.carro.id)==null){
					mapCarros.set(piloto.carro.id,piloto.carro.id);
					var trCarro1 = $('<tr/>');
					var trCarro2 = $('<tr/>');
					$('#tableCarro').append(trCarro1);
					$('#tableCarro').append(trCarro2);
					var tdCarro1 = $('<td/>');
					var tdCarro2 = $('<td/>');
					var tdCarro3 = $('<td/>');
					var tdCarro4 = $('<td/>');
					trCarro1.append(tdCarro1);
					trCarro1.append(tdCarro2);
					trCarro2.append(tdCarro3);
					trCarro2.append(tdCarro4);
					
					var div1 = $('<div/>');
					var carroCima = $('<img class="img-responsive img-center" />');
					carroCima.attr('src', '/f1mane/rest/letsRace/carroCima/' + temporadaSelecionada + '/' + piloto.carro.id);
					div1.append(carroCima);
					var div3 = $('<div class="row transbox textCenter" />');
					div3.append(piloto.carro.nome+'<br>');
					div3.append(lang_text('256')+' '+piloto.carro.potencia);
					tdCarro1.append(div1);
					tdCarro3.append(div3);
					
					var div2 = $('<div/>');
					var carroLado = $('<img class="img-responsive img-center" style="margin-top: 20px;" />');
					carroLado.attr('src', '/f1mane/rest/letsRace/carroLado/' + temporadaSelecionada + '/' + piloto.carro.id);
					div2.append(carroLado);
					var div4 = $('<div class="row transbox textCenter" />');
					div4.append(lang_text('freioCarro')+' '+piloto.carro.freios+'<br>');
					div4.append(lang_text('aerodinamicaCarro')+' '+piloto.carro.aerodinamica+'<br>');
					tdCarro2.append(div2);
					tdCarro4.append(div4);
					
					var click = function() {
						$('#temporadaCarroLivery').val(temporadaSelecionada);
						$('#idCarroLivery').val(piloto.carro.id);
						$('#idImgCarroLado').attr('src', '/f1mane/rest/letsRace/carroLado/' + temporadaSelecionada + '/' + piloto.carro.id);
						$('#idImgCarroCima').attr('src', '/f1mane/rest/letsRace/carroCima/' + temporadaSelecionada + '/' + piloto.carro.id);
						$('#escolha').addClass('hidden');
						$('#carros').addClass('hidden');
						$('#equipe').removeClass('hidden');
					};
					div1.bind("click", click);
					div2.bind("click", click);
					div3.bind("click", click);
					div4.bind("click", click);
				}
			});
		},
		error : function(xhRequest, ErrorText, thrownError) {
			tratamentoErro(xhRequest);
			console.log('selecionaTemporada() ' + xhRequest.status + '  ' + xhRequest.responseText);
		}
	});
}

function listaTemporadas() {
	var urlServico = "/f1mane/rest/letsRace/temporadas";
	$.ajax({
		type : "GET",
		url : urlServico,
		contentType : "application/json",
		dataType : "json",
		success : function(temporadasRes) {
			if (temporadasRes.length == 0) {
				console.log('listaTemporadas() response.length==0');
				return;
			}
			selecionaTemporada(temporadasRes[0]);
			temporadaSelecionada = temporadasRes[0];
			$('#temporadaActive').prop('temporada', temporadasRes[0]);
			$.each(temporadasRes, function(i, val) {
				if (i == 0) {
					return;
				}
				var dv = $('<div class="item"></div>');
				var h1 = $('<h1 class="text-center"></h1>');
				dv.prop('temporada', this);
				h1.append(this);
				dv.append(h1);
				$('#temporadaCarousel-inner').append(dv);
			});
			
		},
		error : function(xhRequest, ErrorText, thrownError) {
			tratamentoErro(xhRequest);
			console.log('listaTemporadas() ' + xhRequest.status + '  ' + xhRequest.responseText);
		}
	});
}


