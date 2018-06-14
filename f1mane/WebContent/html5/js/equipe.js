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
//listaTemporadas();
carregaEquipe();
$('#temporadaCarousel').carousel('pause');

$('#btnSalvar').bind("click", function() {
	salvarEquipe();
});

$('#btnJogar').bind("click", function() {
	localStorage.setItem("modoCarreira", true);
	window.location = "menus.html";
});

$('#temporadaCarousel').on('slide.bs.carousel', function(event) {
	selecionaTemporada($(event.relatedTarget).prop('temporada'));
	$('#temporadaCarousel').carousel('pause');
});


$(document).on('click', '.number-spinner button', function() {
	var ptsConstrutores = parseInt($('#pontosConstrutoresValor').html());
	var btn = $(this), oldValue = btn.closest('.number-spinner').find('input').val().trim(), newVal = 0;
	oldValue =parseInt(oldValue);
	if (btn.attr('data-dir') == 'up') {
		if (oldValue < 600){
			ptsConstrutores-=1;
		}else if (oldValue >= 600 && oldValue < 700) {
			ptsConstrutores-=2;
		}else if (oldValue >= 700 && oldValue < 800) {
			ptsConstrutores-=10;
		}else if (oldValue >= 800 && oldValue < 900) {
			ptsConstrutores-=100;
		}else if (oldValue >= 800 && oldValue < 999) {
			ptsConstrutores-=200;
		}
		newVal = oldValue + 1;
	} else {
		if (oldValue < 600){
			ptsConstrutores+=1;
		}else if (oldValue >= 600 && oldValue < 700) {
			ptsConstrutores+=2;
		}else if (oldValue >= 700 && oldValue < 800) {
			ptsConstrutores+=10;
		}else if (oldValue >= 800 && oldValue < 900) {
			ptsConstrutores+=100;
		}else if (oldValue >= 800 && oldValue < 999) {
			ptsConstrutores+=200;
		}		
		if (oldValue > 1) {
			newVal = oldValue - 1;
		} else {
			newVal = 0;
		}
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
			$('#pontosConstrutoresValor').html(response.ptsConstrutores);
			$('#pontosGanhosValor').html(response.ptsConstrutoresGanhos);
			$('#habilidadePilotoValor').val(response.ptsPiloto);
			$('#potenciaCarroValor').val(response.ptsCarro);
			$('#aerodinamicaCarroValor').val(response.ptsAerodinamica);
			$('#freioCarroValor').val(response.ptsFreio);
			$('#corEquipeValue1').val(rgbToHex(response.c1R,response.c1G,response.c1B));
			$('#corEquipeValue2').val(rgbToHex(response.c2R,response.c2G,response.c2B));
		},
		error : function(xhRequest, ErrorText, thrownError) {
			tratamentoErro(xhRequest);
			console.log('carregaEquipe() ' + xhRequest.status + '  ' + xhRequest.responseText);
		}
	});
}	
	
function salvarEquipe() {
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
			c1R : c1.r,
			c1G : c1.g,
			c1B : c1.b,
			c2R : c2.r,
			c2G : c2.g,
			c2B : c2.b
		};
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
			$('#escolher').html('');
			var pilotos = response.pilotos;
			$.each(pilotos, function(i, val) {
				var piloto = pilotos[i];
				var div = $('<div class="row"/>');
				var capacete = $('<img class="img-responsive img-center"/>');
				capacete.attr('src', '/f1mane/rest/letsRace/capacete/' + temporadaSelecionada + '/' + piloto.id);
				div.append(capacete);
				div.append(piloto.nome);
				$('#escolher').append(div);
				
				div = $('<div class="row"/>');
				var carroCima = $('<img class="img-responsive img-center"/>');
				carroCima.attr('src', '/f1mane/rest/letsRace/carroCima/' + temporadaSelecionada + '/' + piloto.carro.id);
				div.append(carroCima);
				
				var carroLado = $('<img class="img-responsive img-center"/>');
				carroLado.attr('src', '/f1mane/rest/letsRace/carroLado/' + temporadaSelecionada + '/' + piloto.carro.id);
				div.append(carroLado);
				div.append($('<br>'));
				div.append(piloto.carro.nome);
				$('#escolher').append(div);
			});
			$('#temporadaCarousel').carousel('pause');
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
			$('#temporadaCarousel').carousel('pause');
		},
		error : function(xhRequest, ErrorText, thrownError) {
			tratamentoErro(xhRequest);
			console.log('listaTemporadas() ' + xhRequest.status + '  ' + xhRequest.responseText);
		}
	});
}


