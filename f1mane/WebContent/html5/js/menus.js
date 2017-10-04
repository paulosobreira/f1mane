/**
 * Controle de menus
 */

var idPilotoSelecionado;
var temporadaSelecionada;
var circuitoSelecionado;
var token;

if (localStorage.getItem("token")) {
	token = localStorage.getItem("token");
	dadosJogo();
} else {
	criarSessao();
}

$('#btnJogar').bind("click", function() {
	jogar();
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
	if (!token) {
		return;
	}
	$('#divEntrarNoJogo').removeClass('hidden');
}

function dadosJogo() {
	$.ajax({
		type : "GET",
		headers : {
			'token' : localStorage.getItem("token")
		},
		url : "/f1mane/rest/letsRace/dadosJogo",
		contentType : "application/json",
		dataType : "json",
		success : function(dadosJogo) {
			console.log(dadosJogo);
			if('NENHUM'==dadosJogo.estado){
				$('#temporadasDD').on('show.bs.dropdown', function() {
					listaTemporadas();
				});
				$('#circuitosDD').on('show.bs.dropdown', function() {
					listaCircuitos();
				});
				return;
			}
			$('#imgCircuito').attr('src',
					'/f1mane/rest/letsRace/circuitoMini/' + dadosJogo.arquivoCircuito);
			circuitoSelecionado = dadosJogo.arquivoCircuito;
			idPilotoSelecionado = dadosJogo.idPilotoSelecionado;
			temporadaSelecionada = dadosJogo.temporada;
			$('#circuitosLabel').html(dadosJogo.nomeCircuito);
			$('#temporadasLabel').html(dadosJogo.temporada);
			$('#trocaPneuCheckbox').prop('disabled', true);
			$('#trocaPneuCheckbox').prop('checked', dadosJogo.trocaPneu);
			$('#reabastecimentoCheckbox').prop('disabled', true);
			$('#reabastecimentoCheckbox').prop('checked',
					dadosJogo.reabastecimento);
			$('#ersCheckbox').prop('disabled', true);
			$('#ersCheckbox').prop('checked', dadosJogo.ers);
			$('#drsCheckbox').prop('disabled', true);
			$('#drsCheckbox').prop('checked', dadosJogo.drs);
			var pilotos = dadosJogo.pilotos;
			$('#pilotos').find('tr').remove();
			$.each(pilotos, function(i, val) {
				var td1 = $('<td scope="row"/>');
				td1.append(pilotos[i].nome);
				var td2 = $('<td/>');
				td2.append(pilotos[i].nomeCarro);
				var tr = $('<tr style="cursor: pointer; cursor: hand" />');
				var capacete = $('<img class="img-responsive img-center"/>');
//				capacete.attr('src', '/f1mane/rest/letsRace/capacete?id='
//						+ pilotos[i].id + '&temporada=' + temporadaSelecionada);
				td1.append(capacete);
				tr.append(td1);
				var carroLado = $('<img class="img-responsive img-center"/>');
//				carroLado.attr('src', '/f1mane/rest/letsRace/carroLado?id='
//						+ pilotos[i].id + '&temporada=' + temporadaSelecionada);
				td2.append(carroLado);
				if(pilotos[i].nomeJogador){
					tr.addClass('info');
				}
				tr.append(td2);
				$('#pilotos').append(tr);
				tr.addClass('active');
				tr.unbind();
				tr.bind("click", function() {
					$('#pilotos').find('tr').removeClass('info');
					$('#pilotos').find('tr').removeClass('active');
					tr.addClass('active');
					idPilotoSelecionado = pilotos[i].id;
				});
			});
			$('#detalheTemporada').removeClass('hidden');
			mostrarEntrarJogo();

		},
		error : function(xhRequest, ErrorText, thrownError) {
			console.log(xhRequest.status + '  ' + xhRequest.responseText);
		}
	});
}

function jogar() {
	var urlServico = "/f1mane/rest/letsRace/jogar/" + temporadaSelecionada
			+ "/" + idPilotoSelecionado + "/" + circuitoSelecionado;
	$.ajax({
		type : "GET",
		url : urlServico,
		headers : {
			'token' : token
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
			console.log('jogar() ' + xhRequest.status + '  '
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
		success : function(sessaoVisitante) {
			token = sessaoVisitante.sessaoCliente.token;
			localStorage.setItem("token", token);
			dadosJogo();
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
		success : function(circuitos) {
			if (circuitos.length == 0) {
				console.log('listaCircuitos() response.length==0');
				return;
			}
			$('#circuitosList').find('li').remove();
			$.each(circuitos, function(i, circuito) {
				var li = $('<li><a>' + circuito.nome + '</a></li>');
				li.bind("click", function() {
					console.log('circuitosLabel click');
					$('#circuitosLabel').data('circuitos', circuito.nome);
					$('#circuitosLabel').html(circuito.nome);
					$('#imgCircuito').attr(
							'src',
							'/f1mane/rest/letsRace/circuitoMini/'
									+ circuito.arquivo);
					circuitoSelecionado = circuito.arquivo;
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
//				capacete.attr('src', '/f1mane/rest/letsRace/capacete?id='
//						+ pilotos[i].id + '&temporada=' + temporada);
				td1.append(capacete);
				tr.append(td1);
				var carroLado = $('<img class="img-responsive img-center"/>');
//				carroLado.attr('src', '/f1mane/rest/letsRace/carroLado?id='
//						+ pilotos[i].id + '&temporada=' + temporada);
				td2.append(carroLado);
				tr.append(td2);
				$('#pilotos').append(tr);
				tr.unbind();
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