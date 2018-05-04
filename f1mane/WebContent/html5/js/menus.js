/**
 * Controle de menus
 */
esconderEntrarJogo();
var idPilotoSelecionado;
var temporadaSelecionada;
var circuitoSelecionado;
var token;
var circuitos, temporadas, dadosJogo, drsTeporada;
var jogoPreparado = false;

$('#trocaPneuCheck').append(lang_text('trocaPneus'));
$('#reabastecimentoCheck').append(lang_text('reabastecimento'));
$('#selecionePiloto').append(lang_text('selecionePiloto'));
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

$('#btnJogar').bind("click", function() {
	if ((dadosJogo.estado == 'NENHUM' || dadosJogo.estado == '07') && !jogoPreparado) {
		preparaJogo();
	} else {
		jogar();
	}
});

$('#temporadaCarousel').on('slide.bs.carousel', function(event) {
	selecionaTemporada($(event.relatedTarget).prop('temporada'));
	$('#temporadaCarousel').carousel('pause');
	$('#circuitoCarousel').carousel('pause');
});

$('#temporadaCarousel').carousel('pause');

$('#circuitoCarousel').on('slide.bs.carousel', function(event) {
	circuitoSelecionado = $(event.relatedTarget).prop('circuito');
	$('#temporadaCarousel').carousel('pause');
	$('#circuitoCarousel').carousel('pause');
});

$('#circuitoCarousel').carousel('pause');

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

function carregarDadosJogo() {
	$.ajax({
		type : "GET",
		headers : {
			'token' : localStorage.getItem("token")
		},
		url : "/f1mane/rest/letsRace/dadosJogo",
		contentType : "application/json",
		dataType : "json",
		success : function(dadosJogoParam) {
			dadosJogo = dadosJogoParam;
			console.log(dadosJogo);
			if ('NENHUM' == dadosJogo.estado) {
				listaTemporadas();
				listaCircuitos();
				return;
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
				$('#trocaPneuCheck').removeClass('text-muted');
			} else {
				$('#trocaPneuCheck').addClass('text-muted');
			}
			if (dadosJogo.reabastecimento) {
				$('#reabastecimentoCheck').removeClass('text-muted');
			} else {
				$('#reabastecimentoCheck').addClass('text-muted');
			}
			if (dadosJogo.ers) {
				$('#ersCheck').removeClass('text-muted');
			} else {
				$('#ersCheck').addClass('text-muted');
			}
			if (dadosJogo.drs) {
				drsTeporada = true;
				$('#drsCheck').removeClass('text-muted');
			} else {
				$('#drsCheck').addClass('text-muted');
			}
			var pilotos = dadosJogo.pilotos;
			$('#pilotos').find('tr').remove();
			$.each(pilotos, function(i, val) {
				var td1 = $('<td scope="row"/>');
				td1.append(pilotos[i].nome);
				var td2 = $('<td/>');
				td2.append(pilotos[i].nomeCarro);
				var tr = $('<tr style="cursor: pointer; cursor: hand" />');
				var capacete = $('<img class="img-responsive img-center"/>');
				capacete.attr('src', '/f1mane/rest/letsRace/capacete?id=' + pilotos[i].id + '&temporada=' + temporadaSelecionada);
				td1.append(capacete);
				if(pilotos[i].imgJogador!=null){
					var imgJogador = $('<img class="img-responsive img-center userPic"/>');	
					imgJogador.attr('src', pilotos[i].imgJogador);
					td1.append(imgJogador);
				}
				tr.append(td1);
				var carroLado = $('<img class="img-responsive img-center"/>');
				carroLado.attr('src', '/f1mane/rest/letsRace/carroLado?id=' + pilotos[i].id + '&temporada=' + temporadaSelecionada);
				td2.append(carroLado);
				if(pilotos[i].nomeJogador!=null && pilotos[i].imgJogador!=null){
					td2.append(pilotos[i].nomeJogador);
				}
				if (pilotos[i].id == idPilotoSelecionado) {
					tr.addClass('success');
				} else if (pilotos[i].jogadorHumano) {
					tr.addClass('warning');
				}
				tr.append(td2);
				$('#pilotos').append(tr);
				tr.unbind();
				tr.bind("click", function() {
					if (tr.hasClass('warning') || tr.hasClass('success')) {
						return;
					}
					$('#pilotos').find('tr').removeClass('success');
					tr.addClass('success');
					idPilotoSelecionado = pilotos[i].id;
					mostrarEntrarJogo();
				});
			});
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
	$('#voltar').attr('href', 'menus.html');
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
			+ "/" + tpPneu + "/" + combustivel + "/" + tpAsa;
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
					window.location.href = "menus.html";
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
				$('#trocaPneuCheck').removeClass('text-muted');
			} else {
				$('#trocaPneuCheck').addClass('text-muted');
			}
			if (response.reabastecimento) {
				$('#reabastecimentoCheck').removeClass('text-muted');
			} else {
				$('#reabastecimentoCheck').addClass('text-muted');
			}
			if (response.ers) {
				$('#ersCheck').removeClass('text-muted');
			} else {
				$('#ersCheck').addClass('text-muted');
			}
			drsTeporada = response.drs;
			if (response.drs) {
				$('#drsCheck').removeClass('text-muted');
			} else {
				$('#drsCheck').addClass('text-muted');
			}
			temporadaSelecionada = temporada;
			var pilotos = response.pilotos;
			$('#pilotos').find('tr').remove();
			$.each(pilotos, function(i, val) {
				var td1 = $('<td scope="row"/>');
				td1.append(pilotos[i].nome);
				var td2 = $('<td/>');
				td2.append(pilotos[i].nomeCarro);
				var tr = $('<tr style="cursor: pointer; cursor: hand" />');
				var capacete = $('<img class="img-responsive img-center"/>');
				capacete.attr('src', '/f1mane/rest/letsRace/capacete?id=' + pilotos[i].id + '&temporada=' + temporada);
				td1.append(capacete);
				tr.append(td1);
				var carroLado = $('<img class="img-responsive img-center"/>');
				carroLado.attr('src', '/f1mane/rest/letsRace/carroLado?id=' + pilotos[i].id + '&temporada=' + temporada);
				td2.append(carroLado);
				tr.append(td2);
				$('#pilotos').append(tr);
				tr.unbind();
				tr.bind("click", function() {
					if (tr.hasClass('warning') || tr.hasClass('success')) {
						return;
					}
					$('#pilotos').find('tr').removeClass('success');
					tr.addClass('success');
					idPilotoSelecionado = pilotos[i].id;
					mostrarEntrarJogo();
				});
			});
			$('#detalheTemporada').removeClass('hidden');
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
			temporadas = temporadasRes;
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
			$('#circuitoCarousel').carousel('pause');
		},
		error : function(xhRequest, ErrorText, thrownError) {
			tratamentoErro(xhRequest);
			console.log('listaTemporadas() ' + xhRequest.status + '  ' + xhRequest.responseText);
		}
	});
}