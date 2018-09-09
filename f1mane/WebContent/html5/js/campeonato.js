/**
 * Controle de Campeonato
 */
if(localStorage.getItem("token") != null) {
	carregaCampeonato();
} else {
	toaster(lang_text('precisaEstaLogado'), 4000, 'alert alert-danger');
}

$('#153').html(lang_text('153'));
$('#154').html(lang_text('154'));
$('#selecionarPilotoTxt').html(lang_text('120'));
$('#trocaPneuCheck').append(lang_text('trocaPneus'));
$('#reabastecimentoCheck').append(lang_text('reabastecimento'));
$('#rodada').append(lang_text('rodada'));
$('#nomeCircuito').append(lang_text('nomeCircuito'));

$('#nomeCampeonato').html(lang_text('nomeCampeonato'));

var idPilotoSelecionado;
var temporadaSelecionada;
var pilotos;

$('#temporadaCarousel').on('slide.bs.carousel', function(event) {
	selecionaTemporada($(event.relatedTarget).prop('temporada'));
});
$('#temporadaCarousel').carousel('pause');

$('.carousel').carousel({
	pause : true,
	interval : false
});

$('#criarCampeonatoBtn').bind("click", function() {
	criarCampeonato();
});

$('#btnJogar').bind("click", function() {
	localStorage.setItem("modoCampeonato", true);
	window.location = "jogar.html";
});

var adicionarLiCircuito;
var circuitos;

function carregaCampeonato() {
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
				console.log('carregaCampeonato() null');
				listaCircuitos();
				var selecionar = function() {
					selecionaPilotosTemporada();
				};
				$('#pilotoSelecionado').unbind().bind("click", selecionar);
				$('#selecionarPilotoBtn').unbind().bind("click", selecionar);
				listaTemporadas();
			}else{
				$('#criarCampeonato').addClass('hide');
				$('#listarCampeonato').removeClass('hide');
				var campeonato = response;
				$('#temporadasLabelCarrgeda').html('');
				$('#temporadasLabelCarrgeda').append(campeonato.temporada);
				var detalheTemporada = $('#detalheTemporada').clone();
				if (campeonato.trocaPneu) {
					detalheTemporada.find('#trocaPneuCheck').removeClass('line-through');
				} else {
					detalheTemporada.find('#trocaPneuCheck').addClass('line-through');
				}
				if (campeonato.reabastecimento) {
					detalheTemporada.find('#reabastecimentoCheck').removeClass('line-through');
				} else {
					detalheTemporada.find('#reabastecimentoCheck').addClass('line-through');
				}
				if (campeonato.ers) {
					detalheTemporada.find('#ersCheck').removeClass('line-through');
				} else {
					detalheTemporada.find('#ersCheck').addClass('line-through');
				}
				if (campeonato.drs) {
					detalheTemporada.find('#ersCheck').removeClass('line-through');
				} else {
					detalheTemporada.find('#ersCheck').addClass('line-through');
				}
				
				var pilotoSelecionado = $('#pilotoSelecionado').clone();
				
				pilotoSelecionado.removeClass('hide');

				pilotoSelecionado.find('#imgCarroPilotoSelecionado').attr('src', '/f1mane/rest/letsRace/carroLado/' + campeonato.temporadaCarro + '/' + campeonato.idCarro);
				pilotoSelecionado.find('#imgCapacetePilotoSelecionado').attr('src', '/f1mane/rest/letsRace/capacete/' + campeonato.temporadaCapacete + '/' +campeonato.idPiloto);

				
				pilotoSelecionado.find('#nomePilotoSelecionado').html('');
				pilotoSelecionado.find('#nomePilotoSelecionado').append(campeonato.nomePiloto);
				pilotoSelecionado.find('#nomeCarroSelecionado').html('');
				pilotoSelecionado.find('#nomeCarroSelecionado').append(campeonato.carroPiloto);
				
				$('#temporadasLabelCarrgeda').after(detalheTemporada);
				
				detalheTemporada.after(pilotoSelecionado);
				
				var corridas = campeonato.corridas;
				
				$('#corridasTO').find('tr').remove();
				$.each(corridas, function(i, val) {
					var corrida = corridas[i];
					var tr = $('<tr style="cursor: pointer; cursor: hand" />');
					var td1 = $('<td style="width: 20%;" />');
					var td2 = $('<td/>');
					td1.append(corrida.rodada);
					td2.append(corrida.nomeCircuito);
					tr.append(td1);
					tr.append(td2);
					$('#corridasTO').append(tr);
					console.log('corrida.data '+corrida.data);
					if(corrida.data==null){
						tr.css("color","darkGray");
					}else{
						tr = $('<tr style="border-bottom-style: solid; border-bottom-color: darkgray;" />');
						td = $('<td/>');
						var div = $('<div style="border-left-color: '+corrida.corVencedor+';" class="divPiloto" />');
						div.append(corrida.vencedor);
						td.append(div);
						td.append(corrida.data);
						tr.append($('<td/>'));
						tr.append(td);
						$('#corridasTO').append(tr);
					}
				});
			}
		},
		error : function(xhRequest, ErrorText, thrownError) {
			if (xhRequest.status == 204) {
				toaster(lang_text('precisaEstaLogado'), 4000,
						'alert alert-danger');
			} else {
				tratamentoErro(xhRequest);
			}
			console.log('carregaCampeonato() ' + xhRequest.status + '  '
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
			for (var i = 0; i < temporadasRes.length-1; i++) {
				var temp = temporadasRes[i];
				var dv = $('<div class="item"></div>');
				var h1 = $('<h1 class="text-center"></h1>');
				dv.prop('temporada', temp);
				h1.append(temp);
				dv.append(h1);
				$('#temporadaCarousel-inner').append(dv);
			}
			$('#temporadaActive').prop('temporada', temporadasRes[temporadasRes.length-1]);
			selecionaTemporada(temporadasRes[temporadasRes.length-1]);
		},
		error : function(xhRequest, ErrorText, thrownError) {
			tratamentoErro(xhRequest);
			console.log('listaTemporadas() ' + xhRequest.status + '  '
					+ xhRequest.responseText);
		}
	});
}

function prencherLiCircuitos(circuito) {
	var dv = $('<div class="item"></div>');
	var img = $('<img class="img-responsive center-block"/>');
	img.attr('src', '/f1mane/rest/letsRace/circuitoMini/' + circuito.arquivo);
	var h3 = $('<h3 class="text-center transbox"></h3>');
	dv.prop('circuito', circuito.arquivo);
	h3.append(circuito.nome);
	var remover = $('<i class="fa fa-plus floatBtnContent glyphicon glyphicon-trash"/>');
	var removerDv = $('<div class="relativeBtn remover"></div>');
	var adicionar = $('<i class="fa fa-plus floatBtnContent glyphicon glyphicon-plus"/>');
	var adicionarDv = $('<div class="relativeBtn adicionar"></div>');
	removerDv.append(remover);
	adicionarDv.append(adicionar);
	var dvBtns = $('<div class="relativeContainerBtn"></div>');
	dvBtns.append(adicionarDv);
	dvBtns.append(removerDv);
	dv.append(dvBtns);
	dv.append(h3);
	dv.append(img);
	var li = $('<li/>');
	li.append(dv);
	li.prop("circuito", circuito)
	return li;
}

function prencherListaCircuitosSelecionados() {
	$('#listaCircuitosSelecionados').find('li').remove();
	$.each(circuitos, function(i, val) {
		var li = prencherLiCircuitos(this);
		var nomeCircuito = this.nome;
		var clickAdd = function() {
			$('#criarCampeonato').addClass('hide');
			$('#circuitos').removeClass('hide');
			adicionarLiCircuito = nomeCircuito;
			prencherListaCircuitos();
		};
		var cickRem = function() {
			$('#listaCircuitosSelecionados').find(li).remove();
			var divSemCircuitos = $('#circuitos').find('#divSemCircuitos');
			if(divSemCircuitos){
				divSemCircuitos.remove();
			}
			adicionaNenhumCircutoSelecionado();
		};
		li.find('.adicionar').bind("click", clickAdd);
		li.find('.remover').bind("click", cickRem);
		$('#listaCircuitosSelecionados').append(li);
	});
}

function adicionaNenhumCircutoSelecionado() {
	if($('#listaCircuitosSelecionados').find('li')==null 
			|| $('#listaCircuitosSelecionados').find('li').length != 0 ){
		return;
	}
	var divSemCircuitosSelecinados = $('<div id="divSemCircuitosSelecinados" class="form-group"></div>');
	var adicionar = $('<i class="fa fa-plus floatBtnContent glyphicon glyphicon-plus" />');
	var adicionarDv = $('<div class="relativeBtn adicionar"></div>');
	adicionarDv.append(adicionar);
	var h3 = $('<h3 class="text-center transbox"></h3>');
	h3.append(lang_text('296'));
	divSemCircuitosSelecinados.append(h3);
	var clickAddNovo = function() {
		$('#criarCampeonato').addClass('hide');
		$('#circuitos').removeClass('hide');
		adicionarLiCircuito = "";
		prencherListaCircuitos();
	};
	adicionarDv.bind("click", clickAddNovo);
	divSemCircuitosSelecinados.append(adicionarDv);
	$('#criarCampeonato').find('#criarCampeonatoBtn').after(divSemCircuitosSelecinados);
}

function prencherListaCircuitos() {
	$('#listaCircuitos').find('li').remove();
	$.each(circuitos, function(i, val) {
		var li = prencherLiCircuitos(this);
		var lisSel = $('#listaCircuitosSelecionados').find('li');
		var selecionado = false;

		for (var j = 0; j < lisSel.length; j++) {
			var liS = lisSel[j];
			if (liS.circuito.nome == li.prop("circuito").nome) {
				selecionado = true;
				break;
			}
		}
		if (!selecionado) {
			var clickAddCirc = function() {
				$('#criarCampeonato').removeClass('hide');
				var divSemCircuitosSelecinados = $('#criarCampeonato').find('#divSemCircuitosSelecinados');
				if(divSemCircuitosSelecinados){
					divSemCircuitosSelecinados.remove();
				}
				$('#circuitos').addClass('hide');
				var liClone = li.clone();
				liClone.prop("circuito", li.prop("circuito"));
				liClone.find('.remover').removeClass('hide');
				var clickAdd = function() {
					$('#criarCampeonato').addClass('hide');
					$('#circuitos').removeClass('hide');
					adicionarLiCircuito = li.prop("circuito").nome;
					prencherListaCircuitos();
				};
				var cickRem = function() {
					$('#listaCircuitosSelecionados').find(liClone).remove();
					adicionaNenhumCircutoSelecionado();
				};
				liClone.find('.adicionar').bind("click", clickAdd);
				liClone.find('.remover').bind("click", cickRem);
				if(adicionarLiCircuito==""){
					$('#listaCircuitosSelecionados').append($(liClone));
				}else{
					for (var j = 0; j < lisSel.length; j++) {
						var liS = lisSel[j];
						if (liS.circuito.nome == adicionarLiCircuito) {
							$(liS).after($(liClone));
							break;
						}
					}	
				}
			};
			li.find('.adicionar').bind("click", clickAddCirc);
			li.find('.remover').addClass('hide');
			$('#listaCircuitos').append(li);
		}
	});
	if($('#listaCircuitos').find('li') != null 
			&& $('#listaCircuitos').find('li').length == 0 ){
		var divSemCircuitos = $('<div id="divSemCircuitos" ></div>');
		var txt = $('<h3 class="text-center transbox"></h3>');
		txt.append(lang_text('todosCircuitosSelecionados'));
		divSemCircuitos.append(txt);
		$('#circuitos').append(divSemCircuitos);
	}
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
			prencherListaCircuitosSelecionados();

		},
		error : function(xhRequest, ErrorText, thrownError) {
			tratamentoErro(xhRequest);
			console.log('listaCircuitos() ' + xhRequest.status + '  '
					+ xhRequest.responseText);
		}
	});
}

function selecionaTemporada(temporada) {
	temporadaSelecionada = temporada;
	carregaTemporada();
	$('#temporadasLabel').html(temporada);
	
	$('#nomePilotoSelecionado').html('');
	$('#nomeCarroSelecionado').html('');
	$('#imgCarroPilotoSelecionado').attr('src', '');
	$('#imgCapacetePilotoSelecionado').attr('src', '');
	$('#idPilotoSelecionado').val('');
	
	$('#pilotoSelecionado').addClass('hide');
	$('#divPilotoSelecionado').removeClass('hide');
	$('#temporadaCarousel').carousel('pause');
	
}

function carregaTemporada() {
	var urlServico = "/f1mane/rest/letsRace/temporadas/" + temporadaSelecionada;
	$.ajax({
		type : "GET",
		url : urlServico,
		contentType : "application/json",
		dataType : "json",
		success : function(response) {
			if (!response) {
				console.log('carregaTemporada() null');
				return;
			}
			pilotos = response.pilotos;
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
			if (response.drs) {
				$('#drsCheck').removeClass('line-through');
			} else {
				$('#drsCheck').addClass('line-through');
			}
			$('#temporadaCarousel').carousel('pause');
		},
		error : function(xhRequest, ErrorText, thrownError) {
			tratamentoErro(xhRequest);
			console.log('carregaTemporada() ' + xhRequest.status + '  ' + xhRequest.responseText);
		}
	});
}



function selecionaPilotosTemporada() {
	$('#criarCampeonato').addClass('hide');
	$('#selecionarPiloto').removeClass('hide');
	var mapCarros = new Map();
	$('#pilotos').find('tr').remove();
	$.each(pilotos, function(i, val) {
		var tr = gerarTr1Pilotos(pilotos[i]);
		$('#pilotos').append(tr);
		var statusPilotoCarro = gerarTr2Pilotos(pilotos[i]);
		$('#pilotos').append(statusPilotoCarro);
		tr.unbind();
		tr.bind("click", function() {
			selecionarPilotoTemporada(pilotos[i]);
			$('#idPilotoSelecionado').val(pilotos[i].id);
		});
	});
	pilotoCarreiraTemporada();
}

function selecionarPilotoTemporada(piloto){
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
	$('#nomePilotoSelecionado').html(piloto.nome);
	$('#nomeCarroSelecionado').html(piloto.nomeCarro);
	$('#imgCarroPilotoSelecionado').attr('src', '/f1mane/rest/letsRace/carroLado/' + temporadaCarro + '/' + carroId);
	$('#imgCapacetePilotoSelecionado').attr('src', '/f1mane/rest/letsRace/capacete/' + temporadaCapacete + '/' +pilotoId);
	$('#criarCampeonato').removeClass('hide');
	$('#pilotoSelecionado').removeClass('hide');
	$('#selecionarPiloto').addClass('hide');
	$('#divPilotoSelecionado').addClass('hide');
}

function pilotoCarreiraTemporada(){
	var urlServico = "/f1mane/rest/letsRace/equipePilotoCarro";
	$.ajax({
		type : "GET",
		url : urlServico,
		contentType : "application/json",
		headers : {
			'token' : localStorage.getItem("token"),
			'idioma' : localStorage.getItem('idioma')
		},
		dataType : "json",
		success : function(response) {
			if (!response) {
				console.log('pilotoCarreiraTemporada() null');
				return;
			}
			var piloto = response;
			var tr = gerarTr1Pilotos(piloto);
			$('#pilotos').append(tr);
			var statusPilotoCarro = gerarTr2Pilotos(piloto);
			$('#pilotos').append(statusPilotoCarro);
			tr.unbind();
			tr.bind("click", function() {
				selecionarPilotoTemporada(piloto);
				$('#idPilotoSelecionado').val(0);
			});
		},
		error : function(xhRequest, ErrorText, thrownError) {
			tratamentoErro(xhRequest);
			console.log('pilotoCarreiraTemporada() ' + xhRequest.status + '  ' + xhRequest.responseText);
		}
	});
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


function criarCampeonato() {
	var dataObj = objetoCampeonato();
	if(dataObj.idPiloto == ''){
		toaster(lang_text('selecionePiloto'), 4000, 'alert alert-danger');
		return;
	}
	if(dataObj.nome == ''){
		toaster(lang_text('nomeCampeonatoObrigatorio'), 4000, 'alert alert-danger');
		return;
	}
	if(dataObj.corridaCampeonatos.length < 5){
		toaster(lang_text('min5CorridasCampeonato'), 4000, 'alert alert-danger');
		return;
	}	
	var urlServico = "/f1mane/rest/letsRace/campeonato";
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
			toaster(lang_text('campeonatoCriado'), 3000, 'alert alert-success');
			carregaCampeonato();
		},
		error : function(xhRequest, ErrorText, thrownError) {
			tratamentoErro(xhRequest);
			console.log('criarCampeonato() ' + xhRequest.status + '  ' + xhRequest.responseText);
		}
	});
}

function objetoCampeonato(){
	var lisSel = $('#listaCircuitosSelecionados').find('li');
	var lst = new Array();
	for (var j = 0; j < lisSel.length; j++) {
		var  corridaCampeonato = {
				nomeCircuito : lisSel[j].circuito.nome 
			};
		lst.push(corridaCampeonato);
	}
	var dataObj = {
		nome : $('#nomeCampeonatoValor').val(),
		temporada : temporadaSelecionada,
		idPiloto : $('#idPilotoSelecionado').val(),
		corridaCampeonatos : lst
		};
	return dataObj;
}

