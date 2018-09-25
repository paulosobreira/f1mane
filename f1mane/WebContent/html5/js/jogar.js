/**
 * Controle de criação de jogo
 */
esconderEntrarJogo();
var idPilotoSelecionado;
var temporadaSelecionada;
var circuitoSelecionado;
var token;
var circuitos, dadosJogo, drsTeporada;
var jogoPreparado = false;

$('#trocaPneuCheck').append(lang_text('trocaPneus'));
$('#reabastecimentoCheck').append(lang_text('reabastecimento'));
if("true" == localStorage.getItem("modoCarreira")){
	$('#selecionePiloto').append(lang_text('substituirPiloto'));
}else{
	$('#selecionePiloto').append(lang_text('selecionePiloto'));	
}

$('#153').html(lang_text('153'));
$('#154').html(lang_text('154'));

$('#TIPO_PNEU_MOLE').html(lang_text('TIPO_PNEU_MOLE'));
$('#TIPO_PNEU_DURO').html(lang_text('TIPO_PNEU_DURO'));
$('#MENOS_ASA').html(lang_text('menosAjuste'));
$('#MAIS_ASA').html(lang_text('maisAjuste'));
$('#ASA_NORMAL').html(lang_text('ajusteMediano'));

$('#tipoPnelLabel').html(lang_text('264') + ' : ');
$('#numVoltasLabel').html(lang_text('195'));
$('#procentCombustivelLabel').html(lang_text('083') + ' : ');
$('#tipoAsaLabel').html(lang_text('084') + ' : ');

if (localStorage.getItem("token")) {
	token = localStorage.getItem("token");
	idPilotoSelecionado = localStorage.getItem("idPilotoSelecionado");
	carregarDadosJogo();
} else {
	criarSessao();
}

$(document).on('click', '.number-spinner button', function() {
	var btn = $(this), oldValue = btn.closest('.number-spinner').find('input').val().trim(), newVal = 0;
	if (btn.attr('data-dir') == 'up') {
		newVal = parseInt(oldValue) + 10;
	} else {
		if (oldValue > 10) {
			newVal = parseInt(oldValue) - 10;
		} else {
			newVal = 10;
		}
	}
	btn.closest('.number-spinner').find('input').val(newVal);
});

$('#selecionaTpPneu').find('tr').bind("click", function() {
	$('#selecionaTpPneu').find('tr').removeClass('success');
	$(this).addClass('success');
});

$('#selecionaTpAsa').find('tr').bind("click", function() {
	$('#selecionaTpAsa').find('tr').removeClass('success');
	$(this).addClass('success');
});

$('#btnJogar').unbind().bind("click", function() {
	if("true" == localStorage.getItem("modoCampeonato")){
		jogarCampeonato();
	}else if ((dadosJogo.estado == 'NENHUM' || dadosJogo.estado == '07') && !jogoPreparado) {
		preparaJogo();
	} else {
		jogar();
	}
});

$('#temporadaCarousel').on('slide.bs.carousel', function(event) {
	selecionaTemporada($(event.relatedTarget).prop('temporada'));
});

$('#temporadaCarousel').carousel('pause');

$('#circuitoCarousel').on('slide.bs.carousel', function(event) {
	circuitoSelecionado = $(event.relatedTarget).prop('circuito');
});

$('#circuitoCarousel').carousel('pause');

$('.carousel').carousel({
	pause: true,
	interval: false
	});

function mostrarEntrarJogo() {
	if (temporadaSelecionada == null) {
		return;
	}
	if (idPilotoSelecionado == null) {
		return;
	}
	if (circuitoSelecionado == null) {
		return;
	}
	if (token == null) {
		return;
	}
	$('#btnJogar').show();
}

function esconderEntrarJogo() {
	$('#btnJogar').hide();
}

function gerarTr1Pilotos(piloto){
	var temporadaCapacete = temporadaSelecionada;
	var temporadaCarro = temporadaSelecionada;
	var pilotoId = piloto.id;
	var carroId = piloto.carro.id;
	if(piloto.idCapaceteLivery!=null && piloto.temporadaCapaceteLivery!=null){
		temporadaCapacete = piloto.temporadaCapaceteLivery;
		pilotoId = piloto.idCapaceteLivery;
	}
	
	if(piloto.idCarroLivery!=null && piloto.temporadaCarroLivery!=null){
		temporadaCarro = piloto.temporadaCarroLivery;
		carroId = piloto.idCarroLivery;
	}
	
	
	var td1 = $('<td scope="row" style="display: grid;"/>');
	td1.append(piloto.nome);
	var td2 = $('<td/>');
	td2.append(piloto.nomeCarro);
	var tr = $('<tr style="cursor: pointer; cursor: hand" />');
	var capacetes = $('<div style="display:  inline-flex;"  />');
	td1.append(capacetes);
	var capacete = $('<img class="img-responsive img-center"/>');
	capacete.attr('src', '/f1mane/rest/letsRace/capacete/' + temporadaCapacete + '/' +pilotoId);
	capacetes.append($('<br>'));
	capacetes.append(capacete);
	if(piloto.imgJogador!=null){
		var imgJogador = $('<img class="img-responsive img-center userPic"/>');	
		imgJogador.attr('src', piloto.imgJogador);
		capacetes.append(imgJogador);
	}
	tr.append(td1);
	var carroLado = $('<img class="img-responsive img-center"/>');
	carroLado.attr('src', '/f1mane/rest/letsRace/carroLado/' + temporadaCarro + '/' + carroId);
	td2.append(carroLado);
	if (piloto.id == idPilotoSelecionado) {
		tr.addClass('success');
	} else if (piloto.jogadorHumano) {
		tr.addClass('warning');
	}
	if(piloto.nomeJogador!=null){
		td1.append('<br>');
		td2.append('<b>'+piloto.nomeJogador+'</b>');
	}
	tr.append(td2);
	return tr;
}

function gerarTr2Pilotos(piloto){
	var tr = $('<tr class="statusPilotoCarro hidden"/>');
	var td = $('<td colspan="2"/>');
	var div = $('<div/>');
	
	div.append(lang_text('255'));
	var habilidade = $('<div class="progress"/>').append($('<div style="width: '+(piloto.habilidade/10)+'%;" class="progress-bar fundoCinza" role="progressbar" aria-valuenow="'+piloto.habilidade+'" aria-valuemin="0" aria-valuemax="1000"/>'));
	div.append(habilidade);
	div.append(lang_text('256'));
	var potencia = $('<div class="progress"/>').append($('<div style="width: '+(piloto.carro.potencia/10)+'%;" class="progress-bar fundoCinza" role="progressbar" aria-valuenow="'+piloto.carro.potencia+'" aria-valuemin="0" aria-valuemax="1000"/>'));
	div.append(potencia);
	div.append(lang_text('aerodinamicaCarro'));
	var aerodinamica = $('<div class="progress"/>').append($('<div style="width: '+(piloto.carro.aerodinamica/10)+'%;" class="progress-bar fundoCinza" role="progressbar" aria-valuenow="'+piloto.carro.aerodinamica+'" aria-valuemin="0" aria-valuemax="1000"/>'));
	div.append(aerodinamica);
	div.append(lang_text('freioCarro'));
	var freios = $('<div class="progress"/>').append($('<div style="width: '+(piloto.carro.freios/10)+'%;" class="progress-bar fundoCinza" role="progressbar" aria-valuenow="'+piloto.carro.freios+'" aria-valuemin="0" aria-valuemax="1000"/>'));
	div.append(freios);
	td.append(div);
	tr.append(td);
	return tr;
}

function carregarDadosJogo() {
	if("true" == localStorage.getItem("modoCampeonato")){
		carregarDadosJogoCampeonato();
	}else{
		carregarDadosJogoPadrao();
	}
}

function carregarDadosJogoCampeonato(){
	var urlServico = "/f1mane/rest/letsRace/campeonato";
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
			if (response == null) {
				window.location = "jogar.html";
			}else{
				var campeonato = response;
				$('#criaJogo').removeClass('hide');
				$('#selecionarPiloto').addClass('hide');
				$('#detalheTemporada').removeClass('hidden');
				$('#temporadaAnterior').remove();
				$('#temporadaProxima').remove();
				$('#circuitoAnterior').remove();
				$('#circuitoProximo').remove();				
				$('#imgCircuito').attr('src', '/f1mane/rest/letsRace/circuitoMini/' + campeonato.arquivoCircuitoAtual);
				circuitoSelecionado = campeonato.arquivoCircuitoAtual;
				temporadaSelecionada = campeonato.temporada;
				var circuitosLabel = campeonato.nomeCircuitoAtual;
				$('#circuitosLabel').html(circuitosLabel);
				$('#temporadasLabel').html(temporadaSelecionada);
				if (campeonato.trocaPneu) {
					$('#trocaPneuCheck').removeClass('line-through');
				} else {
					$('#trocaPneuCheck').addClass('line-through');
				}
				if (campeonato.reabastecimento) {
					$('#reabastecimentoCheck').removeClass('line-through');
				} else {
					$('#reabastecimentoCheck').addClass('line-through');
				}
				if (campeonato.ers) {
					$('#ersCheck').removeClass('line-through');
				} else {
					$('#ersCheck').addClass('line-through');
				}
				if (campeonato.drs) {
					drsTeporada = true;
					$('#drsCheck').removeClass('line-through');
				} else {
					$('#drsCheck').addClass('line-through');
				}
				if (drsTeporada) {
					$('#ajusteDeAsa').addClass('hide');
				}
				$('#btnJogar').css('z-index', '100000');
				$('#idNumeroVoltas').addClass('hide');
				$('#btnJogar').show();
			}
		},
		error : function(xhRequest, ErrorText, thrownError) {
			if (xhRequest.status == 204) {
				toaster(lang_text('precisaEstaLogado'), 4000,
						'alert alert-danger');
			} else {
				tratamentoErro(xhRequest);
			}
			console.log('carregarDadosJogoCampeonato() ' + xhRequest.status + '  '
					+ xhRequest.responseText);
		}
	});	
}

function carregarDadosJogoPadrao() {
	$.ajax({
		type : "GET",
		headers : {
			'token' : localStorage.getItem("token")
		},
		url : "/f1mane/rest/letsRace/dadosJogo?modoCarreira="+localStorage.getItem("modoCarreira"),
		contentType : "application/json",
		dataType : "json",
		success : function(dadosJogoParam) {
			dadosJogo = dadosJogoParam;
			if ('NENHUM' == dadosJogo.estado) {
				listaTemporadas();
				listaCircuitos();
				return;
			}
			//ESPERANDO_JOGO_COMECAR 07
			if (localStorage.getItem("modoCarreira")=="true" && '07' != dadosJogo.estado) {
				toaster(lang_text('247'), 3000, 'alert alert-danger');
			}
			$('#imgCircuito').attr('src', '/f1mane/rest/letsRace/circuitoMini/' + dadosJogo.arquivoCircuito);
			circuitoSelecionado = dadosJogo.arquivoCircuito;
			idPilotoSelecionado = dadosJogo.idPilotoSelecionado;
			temporadaSelecionada = dadosJogo.temporada;
			var circuitosLabel = dadosJogo.nomeCircuito;
			if (dadosJogo.voltaAtual != null && dadosJogo.numeroVotas != null) {
				circuitosLabel += ' ' + dadosJogo.voltaAtual + '/' + dadosJogo.numeroVotas;
			}
			$('#circuitosLabel').html(circuitosLabel);
			$('#temporadasLabel').html(dadosJogo.temporada);
			if (dadosJogo.trocaPneu) {
				$('#trocaPneuCheck').removeClass('line-through');
			} else {
				$('#trocaPneuCheck').addClass('line-through');
			}
			if (dadosJogo.reabastecimento) {
				$('#reabastecimentoCheck').removeClass('line-through');
			} else {
				$('#reabastecimentoCheck').addClass('line-through');
			}
			if (dadosJogo.ers) {
				$('#ersCheck').removeClass('line-through');
			} else {
				$('#ersCheck').addClass('line-through');
			}
			if (dadosJogo.drs) {
				drsTeporada = true;
				$('#drsCheck').removeClass('line-through');
			} else {
				$('#drsCheck').addClass('line-through');
			}
			var pilotos = dadosJogo.pilotos;
			$('#pilotos').find('tr').remove();
			$.each(pilotos, function(i, val) {
				var tr = gerarTr1Pilotos(pilotos[i]);
				$('#pilotos').append(tr);
				var statusPilotoCarro = gerarTr2Pilotos(pilotos[i]);
				$('#pilotos').append(statusPilotoCarro);
				tr.unbind();
				tr.bind("click", function() {
					if (tr.hasClass('warning') || tr.hasClass('success')) {
						return;
					}
					$('#pilotos').find('tr.statusPilotoCarro').removeClass('hidden').addClass('hidden');
					statusPilotoCarro.removeClass('hidden');
					$('#pilotos').find('tr').removeClass('success');
					tr.addClass('success');
					idPilotoSelecionado = pilotos[i].id;
					mostrarEntrarJogo();
				});
			});
			if(dadosJogo.campeonato!=null){
				$('#divCampeonato').removeClass('hide');
				$('#nomeCampeonatoRodada').append(dadosJogo.campeonato);
				$('#rodadaCampeonato').append(lang_text('rodada')+' '+dadosJogo.rodadaCampeonato);
			}

			
			$('#detalheTemporada').removeClass('hidden');
			$('#temporadaAnterior').remove();
			$('#temporadaProxima').remove();
			$('#circuitoAnterior').remove();
			$('#circuitoProximo').remove();
			if(localStorage.getItem("nomeJogo")){
				localStorage.setItem("idPilotoSelecionado", idPilotoSelecionado);
				window.location.href = "corrida.html";
			}
		},
		error : function(xhRequest, ErrorText, thrownError) {
			tratamentoErro(xhRequest);
			console.log(xhRequest.status + '  ' + xhRequest.responseText + ' ' + ErrorText);
			carregarDadosJogo();
		}
	});
}

function preparaJogo() {
	$('#criaJogo').removeClass('hide');
	$('#selecionarPiloto').addClass('hide');
	$('#temporadaAnterior').remove();
	$('#temporadaProxima').remove();
	$('#circuitoAnterior').remove();
	$('#circuitoProximo').remove();
	$('#voltar').attr('href', 'jogar.html');
	if (drsTeporada) {
		$('#ajusteDeAsa').addClass('hide');
	}
	$('#btnJogar').css('z-index', '100000');
	$('#idNumeroVoltas').addClass('hide');
	if (dadosJogo.estado == '07') {
		$('#idNumeroVoltas').addClass('hide');
	}
	jogoPreparado = true;
}

function jogarCampeonato() {
	var tpPneu = $('#selecionaTpPneu').find('tr.success').find('div.transbox').attr('id');
	var combustivel = $('#procentCombustivel').val();
	var tpAsa = $('#selecionaTpAsa').find('tr.success').find('div.transbox').attr('id');
	if (combustivel > 100) {
		combustivel = 100;
	}
	if (combustivel < 0 ) {
		combustivel = 0;
	}
	var urlServico = "/f1mane/rest/letsRace/jogarCampeonato/" + tpPneu + "/" + combustivel + "/" + tpAsa;
	$.ajax({
		type : "GET",
		url : urlServico,
		headers : {
			'token' : token,
			'idioma' : localStorage.getItem('idioma')
		},
		contentType : "application/json",
		dataType : "json",
		success : function(dadosJogo) {
			localStorage.setItem("nomeJogo", dadosJogo.nomeJogo);
			localStorage.setItem("token", token);
			localStorage.setItem("idPilotoSelecionado", dadosJogo.idPilotoSelecionado);
			window.location.href = "corrida.html";
		},
		error : function(xhRequest, ErrorText, thrownError) {
			tratamentoErro(xhRequest);
			if (xhRequest.status != 401) {
				setTimeout(function() {
					window.location.href = "jogar.html";
				}, 3000);

			}
			console.log('jogar() ' + xhRequest.status + '  ' + xhRequest.responseText);
		}
	});
}



function jogar() {
	if (temporadaSelecionada == null || idPilotoSelecionado == null || circuitoSelecionado == null) {
		return;
	}
	var tpPneu = $('#selecionaTpPneu').find('tr.success').find('div.transbox').attr('id');
	var voltas = $('#numVoltas').val();
	var combustivel = $('#procentCombustivel').val();
	var tpAsa = $('#selecionaTpAsa').find('tr.success').find('div.transbox').attr('id');
	if (voltas > 100) {
		voltas = 100;
	}
	if (voltas < 0) {
		voltas = 0;
	}
	if (combustivel > 100) {
		combustivel = 100;
	}
	if (combustivel < 0 ) {
		combustivel = 0;
	}
	var urlServico = "/f1mane/rest/letsRace/jogar/" + temporadaSelecionada + "/" + idPilotoSelecionado + "/" + circuitoSelecionado + "/" + voltas
			+ "/" + tpPneu + "/" + combustivel + "/" + tpAsa+"/"+localStorage.getItem("modoCarreira");
	$.ajax({
		type : "GET",
		url : urlServico,
		headers : {
			'token' : token,
			'idioma' : localStorage.getItem('idioma')
		},
		contentType : "application/json",
		dataType : "json",
		success : function(dadosJogo) {
			localStorage.setItem("nomeJogo", dadosJogo.nomeJogo);
			localStorage.setItem("token", token);
			localStorage.setItem("idPilotoSelecionado", idPilotoSelecionado);
			window.location.href = "corrida.html";
		},
		error : function(xhRequest, ErrorText, thrownError) {
			tratamentoErro(xhRequest);
			if (xhRequest.status != 401) {
				setTimeout(function() {
					window.location.href = "jogar.html";
				}, 3000);

			}
			console.log('jogar() ' + xhRequest.status + '  ' + xhRequest.responseText);
		}
	});
}

function criarSessao() {
	var urlServico = "/f1mane/rest/letsRace/criarSessaoVisitante";
	$.ajax({
		type : "GET",
		url : urlServico,
		contentType : "application/json",
		dataType : "json",
		success : function(sessaoVisitante) {
			token = sessaoVisitante.sessaoCliente.token;
			localStorage.setItem("token", token);
			carregarDadosJogo();
		},
		error : function(xhRequest, ErrorText, thrownError) {
			tratamentoErro(xhRequest);
			console.log('criarSessao() ' + xhRequest.status + '  ' + xhRequest.responseText);
		}
	});
}

function listaCircuitos() {
	var urlServico = "/f1mane/rest/letsRace/circuitos";
	$.ajax({
		type : "GET",
		url : urlServico,
		contentType : "application/json",
		dataType : "json",
		success : function(circuitosRes) {
			if (circuitosRes.length == 0) {
				console.log('listaCircuitos() response.length==0');
				return;
			}
			circuitos = circuitosRes;
			var circuito = circuitosRes[0];
			$('#circuitosLabel').html(circuito.nome);
			$('#imgCircuito').attr('src', '/f1mane/rest/letsRace/circuitoMini/' + circuito.arquivo);
			circuitoSelecionado = circuito.arquivo;
			$('#circuitoActive').prop('circuito', circuito.arquivo);
			var dvChuva = $('<div class="well"></div>');
			dvChuva.append(lang_text('probChuva') + ' : ' + circuito.probalidadeChuva + '%');
			$('#circuitoActive').append(dvChuva);
			$.each(circuitosRes, function(i, val) {
				if (i == 0) {
					return;
				}
				var dv = $('<div class="item"></div>');
				var img = $('<img class="img-responsive center-block"/>');
				img.attr('src', '/f1mane/rest/letsRace/circuitoMini/' + this.arquivo);
				var h3 = $('<h3 class="text-center"></h3>');
				dv.prop('circuito', this.arquivo);
				h3.append(this.nome);
				dv.append(h3);
				dv.append(img);
				var dvChuva = $('<div class="well"></div>');
				dvChuva.append(lang_text('probChuva') + ' : ' + this.probalidadeChuva + '%');
				dv.append(dvChuva);
				$('#circuitoCarousel-inner').append(dv);
			});
			$('#temporadaCarousel').carousel('pause');
			$('#circuitoCarousel').carousel('pause');
		},
		error : function(xhRequest, ErrorText, thrownError) {
			tratamentoErro(xhRequest);
			console.log('listaCircuitos() ' + xhRequest.status + '  ' + xhRequest.responseText);
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
			if (idPilotoSelecionado != null) {
				console.log('selecionaTemporada ' + temporada + ' idPilotoSelecionado ' + idPilotoSelecionado);
				idPilotoSelecionado = null;
				esconderEntrarJogo();
			}
			$('#temporadasLabel').html(temporada);
			if (response.trocaPneu) {
				$('#trocaPneuCheck').removeClass('line-through');
			} else {
				$('#trocaPneuCheck').addClass('line-through');
			}
			if (response.reabastecimento) {
				$('#reabastecimentoCheck').removeClass('line-through');
			} else {
				$('#reabastecimentoCheck').addClass('line-through');
			}
			if (response.ers) {
				$('#ersCheck').removeClass('line-through');
			} else {
				$('#ersCheck').addClass('line-through');
			}
			drsTeporada = response.drs;
			if (response.drs) {
				$('#drsCheck').removeClass('line-through');
			} else {
				$('#drsCheck').addClass('line-through');
			}
			temporadaSelecionada = temporada;
			var pilotos = response.pilotos;
			$('#pilotos').find('tr').remove();
			$.each(pilotos, function(i, val) {
				var tr = gerarTr1Pilotos(pilotos[i]);
				$('#pilotos').append(tr);
				var statusPilotoCarro = gerarTr2Pilotos(pilotos[i]);
				$('#pilotos').append(statusPilotoCarro);
				tr.unbind();
				tr.bind("click", function() {
					if (tr.hasClass('warning') || tr.hasClass('success')) {
						return;
					}
					$('#pilotos').find('tr.statusPilotoCarro').removeClass('hidden').addClass('hidden');
					statusPilotoCarro.removeClass('hidden');
					$('#pilotos').find('tr').removeClass('success');
					tr.addClass('success');
					idPilotoSelecionado = pilotos[i].id;
					mostrarEntrarJogo();
				});
			});
			$('#detalheTemporada').removeClass('hidden');
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
			for (var i = 0; i < temporadasRes.length-1; i++) {
				var temp = temporadasRes[i];
				var dv = $('<div class="item"></div>');
				var h1 = $('<h1 class="text-center"></h1>');
				dv.prop('temporada', temp);
				h1.append(temp);
				dv.append(h1);
				$('#temporadaCarousel-inner').append(dv);
			}
			selecionaTemporada(temporadasRes[temporadasRes.length-1]);
			temporadaSelecionada = temporadasRes[temporadasRes.length-1];
			$('#temporadaActive').prop('temporada', temporadaSelecionada);

			
			$('#temporadaCarousel').carousel('pause');
			$('#circuitoCarousel').carousel('pause');
		},
		error : function(xhRequest, ErrorText, thrownError) {
			tratamentoErro(xhRequest);
			console.log('listaTemporadas() ' + xhRequest.status + '  ' + xhRequest.responseText);
		}
	});
}