/**
 * Controle de menus
 */

var idPilotoSelecionado;
var temporadaSelecionada;
var circuitoSelecionado;
var sessaoVisitante;

criarSessao();


$('#btnCriarJogo').bind("click", function() {
	criarJogo();
});

$('#temporadasDD').on('show.bs.dropdown', function() {
	listaTemporadas();
});
$('#circuitosDD').on('show.bs.dropdown', function() {
	listaCircuitos();
});

function mostrarEntrarJogo() {
	if (!temporadaSelecionada) {
		return;
	}
	if (!idPilotoSelecionado) {
		return;
	}
	if (!circuitoSelecionado) {
		return;
	}
	if (!sessaoVisitante || !sessaoVisitante.sessaoCliente
			|| !sessaoVisitante.sessaoCliente.token) {
		return;
	}
	$('#divEntrarNoJogo').removeClass('hidden');
}

function criarJogo() {
	var urlServico = "/f1mane/rest/letsRace/criarJogo/" + temporadaSelecionada
			+ "/" + idPilotoSelecionado + "/" + circuitoSelecionado;
	$.ajax({
		type : "GET",
		url : urlServico,
		headers : {
			'token' : sessaoVisitante.sessaoCliente.token
		},
		contentType : "application/json",
		dataType : "json",
		success : function(dadosJogo) {
			localStorage.setItem("nomeJogo", dadosJogo.nomeJogo);
			localStorage.setItem("token", sessaoVisitante.sessaoCliente.token);
			localStorage.setItem("idPilotoSelecionado", idPilotoSelecionado);
			window.location.href = "corrida.html";
		},
		error : function(xhRequest, ErrorText, thrownError) {
			console.log('criarJogo() ' + xhRequest.status + '  '
					+ xhRequest.responseText);
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
		success : function(response) {
			sessaoVisitante = response;
		},
		error : function(xhRequest, ErrorText, thrownError) {
			console.log('criarSessao() ' + xhRequest.status + '  '
					+ xhRequest.responseText);
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
		success : function(circuitosMap) {
			var circuitos = Object.keys(circuitosMap);
			if (circuitos.length == 0) {
				console.log('listaCircuitos() response.length==0');
				return;
			}
			$('#circuitosList').find('li').remove();
			$.each(circuitos, function(i, val) {
				var li = $('<li><a>' + circuitos[i] + '</a></li>');
				li.bind("click", function() {
					console.log('circuitosLabel click');
					$('#circuitosLabel').data('circuitos', circuitos[i]);
					$('#circuitosLabel').html(circuitos[i]);
					$('#imgCircuito').attr(
							'src',
							'/f1mane/rest/letsRace/circuitoMini/'
									+ circuitosMap[circuitos[i]]);
					circuitoSelecionado = circuitosMap[circuitos[i]];
					mostrarEntrarJogo();
				});
				$('#circuitosList').append(li);
			});
		},
		error : function(xhRequest, ErrorText, thrownError) {
			console.log('listaCircuitos() ' + xhRequest.status + '  '
					+ xhRequest.responseText);
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
			$('#trocaPneuCheckbox').prop('disabled', true);
			$('#trocaPneuCheckbox').prop('checked', response.trocaPneu);
			$('#reabastecimentoCheckbox').prop('disabled', true);
			$('#reabastecimentoCheckbox').prop('checked',
					response.reabastecimento);
			$('#ersCheckbox').prop('disabled', true);
			$('#ersCheckbox').prop('checked', response.ers);
			$('#drsCheckbox').prop('disabled', true);
			$('#drsCheckbox').prop('checked', response.drs);
			var pilotos = response.pilotos;
			$('#pilotos').find('tr').remove();
			$.each(pilotos, function(i, val) {
				var td1 = $('<td scope="row"/>');
				td1.append(pilotos[i].nome);
				var td2 = $('<td/>');
				td2.append(pilotos[i].nomeCarro);
				var tr = $('<tr style="cursor: pointer; cursor: hand" />');
				var capacete = $('<img class="img-responsive img-center"/>');
				capacete.attr('src', '/f1mane/rest/letsRace/capacete?id='
						+ pilotos[i].id + '&temporada=' + temporada);
				td1.append(capacete);
				tr.append(td1);
				var carroLado = $('<img class="img-responsive img-center"/>');
				carroLado.attr('src', '/f1mane/rest/letsRace/carroLado?id='
						+ pilotos[i].id + '&temporada=' + temporada);
				td2.append(carroLado);
				tr.append(td2);
				$('#pilotos').append(tr);
				tr.bind("click", function() {
					$('#pilotos').find('tr').removeClass('active');
					tr.addClass('active');
					idPilotoSelecionado = pilotos[i].id;
					mostrarEntrarJogo();
				});
			});
			$('#detalheTemporada').removeClass('hidden');
		},
		error : function(xhRequest, ErrorText, thrownError) {
			console.log('selecionaTemporada() ' + xhRequest.status + '  '
					+ xhRequest.responseText);
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
		success : function(temporadas) {
			if (temporadas.length == 0) {
				console.log('listaTemporadas() response.length==0');
				return;
			}
			$('#temporadasList').find('li').remove();
			$.each(temporadas, function(i, val) {
				var li = $('<li><a>' + temporadas[i] + '</a></li>');
				li.bind("click", function() {
					console.log('temporadasLabel click');
					$('#temporadasLabel').data('temporada', temporadas[i]);
					$('#temporadasLabel').html(temporadas[i]);
					selecionaTemporada(temporadas[i]);
					temporadaSelecionada = temporadas[i];
				});
				$('#temporadasList').append(li);
			});

		},
		error : function(xhRequest, ErrorText, thrownError) {
			console.log('listaTemporadas() ' + xhRequest.status + '  '
					+ xhRequest.responseText);
		}
	});
}