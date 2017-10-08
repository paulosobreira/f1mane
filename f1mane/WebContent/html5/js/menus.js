/**
 * Controle de menus
 */

var idPilotoSelecionado;
var temporadaSelecionada;
var circuitoSelecionado;
var token;
var circuitos, temporadas;

if (localStorage.getItem("token")) {
	token = localStorage.getItem("token");
	dadosJogo();
} else {
	criarSessao();
}

$('#btnJogar').bind("click", function() {
	jogar();
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
	$('#btnJogar').removeClass('disabled');
	$('#btnJogar').html('Jogar');
}

function dadosJogo() {
	$
			.ajax({
				type : "GET",
				headers : {
					'token' : localStorage.getItem("token")
				},
				url : "/f1mane/rest/letsRace/dadosJogo",
				contentType : "application/json",
				dataType : "json",
				success : function(dadosJogo) {
					console.log(dadosJogo);
					if ('NENHUM' == dadosJogo.estado) {
						listaTemporadas();
						listaCircuitos();
						return;
					}
					$('#imgCircuito').attr('src','/f1mane/rest/letsRace/circuitoMini/'+ dadosJogo.arquivoCircuito);
					circuitoSelecionado = dadosJogo.arquivoCircuito;
					idPilotoSelecionado = dadosJogo.idPilotoSelecionado;
					temporadaSelecionada = dadosJogo.temporada;
					$('#circuitosLabel').html(dadosJogo.nomeCircuito);
					$('#temporadasLabel').html(dadosJogo.temporada);
					$('#trocaPneuCheckbox').prop('disabled', true);
					$('#trocaPneuCheckbox')
							.prop('checked', dadosJogo.trocaPneu);
					$('#reabastecimentoCheckbox').prop('disabled', true);
					$('#reabastecimentoCheckbox').prop('checked',
							dadosJogo.reabastecimento);
					$('#ersCheckbox').prop('disabled', true);
					$('#ersCheckbox').prop('checked', dadosJogo.ers);
					$('#drsCheckbox').prop('disabled', true);
					$('#drsCheckbox').prop('checked', dadosJogo.drs);
					var pilotos = dadosJogo.pilotos;
					$('#pilotos').find('tr').remove();
					$.each(pilotos,	function(i, val) {
						var td1 = $('<td scope="row"/>');
						td1.append(pilotos[i].nome);
						var td2 = $('<td/>');
						td2.append(pilotos[i].nomeCarro);
						var tr = $('<tr style="cursor: pointer; cursor: hand" />');
						var capacete = $('<img class="img-responsive img-center"/>');
						capacete.attr('src','/f1mane/rest/letsRace/capacete?id='
										+ pilotos[i].id
										+ '&temporada='
										+ temporadaSelecionada);
						td1.append(capacete);
						tr.append(td1);
						var carroLado = $('<img class="img-responsive img-center"/>');
						carroLado.attr('src','/f1mane/rest/letsRace/carroLado?id='
										+ pilotos[i].id
										+ '&temporada='
										+ temporadaSelecionada);
						td2.append(carroLado);
						if (pilotos[i].nomeJogador) {
							tr.addClass('warning');
						}
						tr.append(td2);
						$('#pilotos').append(tr);
						tr.unbind();
						tr.bind("click",function() {
							if(tr.hasClass('warning')){
								return;
							}
							$('#pilotos').find('tr').removeClass('active');
							tr.addClass('active');
							idPilotoSelecionado = pilotos[i].id;
							mostrarEntrarJogo();
						});
					});
					$('#detalheTemporada').removeClass('hidden');
					mostrarEntrarJogo();

				},
				error : function(xhRequest, ErrorText, thrownError) {
					console.log(xhRequest.status + '  '
							+ xhRequest.responseText + ' ' + ErrorText);
					dadosJogo();
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
			alert(xhRequest.responseJSON.messageString);
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
		success : function(circuitosRes) {
			if (circuitosRes.length == 0) {
				console.log('listaCircuitos() response.length==0');
				return;
			}
			circuitos = circuitosRes;
			var circuito = circuitosRes[0];
			$('#circuitosLabel').html(circuito.nome);
			$('#imgCircuito').attr('src',
					'/f1mane/rest/letsRace/circuitoMini/' + circuito.arquivo);
			circuitoSelecionado = circuito.arquivo;
			$('#circuitoActive').prop('circuito',circuito.arquivo);
			$.each(circuitosRes,	function(i, val) {
				if(i==0){
					return;
				}
				var dv = $('<div class="item"></div>');
				var img = $('<img class="img-responsive center-block"/>');
				img.attr('src',
						'/f1mane/rest/letsRace/circuitoMini/' + this.arquivo);
				var h3 = $('<h3 class="text-center"></h3>');
				dv.prop('circuito',this.arquivo);
				h3.append(this.nome);
				dv.append(h3);
				dv.append(img);
				$('#circuitoCarousel-inner').append(dv);
			});		
			$('#temporadaCarousel').carousel('pause');
			$('#circuitoCarousel').carousel('pause');
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
			$('#temporadasLabel').html(temporada);
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
		success : function(temporadasRes) {
			if (temporadasRes.length == 0) {
				console.log('listaTemporadas() response.length==0');
				return;
			}
			temporadas = temporadasRes;
			selecionaTemporada(temporadasRes[0]);
			temporadaSelecionada = temporadasRes[0];
			$('#temporadaActive').prop('temporada',temporadasRes[0]);
			$.each(temporadasRes,	function(i, val) {
				if(i==0){
					return;
				}
				var dv = $('<div class="item"></div>');
				var h1 = $('<h1 class="text-center"></h1>');
				dv.prop('temporada',this);
				h1.append(this);
				dv.append(h1);
				$('#temporadaCarousel-inner').append(dv);
			});
			$('#temporadaCarousel').carousel('pause');
			$('#circuitoCarousel').carousel('pause');
		},
		error : function(xhRequest, ErrorText, thrownError) {
			console.log('listaTemporadas() ' + xhRequest.status + '  '
					+ xhRequest.responseText);
		}
	});
}