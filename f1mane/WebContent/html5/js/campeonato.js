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

$('#nomeCampeonato').html(lang_text('nomeCampeonato'));
$('#criarCampeonatoBtn').html(lang_text('criarCampeonato'));

var idPilotoSelecionado;
var temporadaSelecionada;
listaTemporadas();

$('#temporadaCarousel').on('slide.bs.carousel', function(event) {
	selecionaTemporada($(event.relatedTarget).prop('temporada'));
});

$('.carousel').carousel({
	pause : true,
	interval : false
});

$('#criarCampeonatoBtn').bind("click", function() {
	criarCampeonato();
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
				return;
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
			selecionaTemporada(temporadasRes[0]);
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
	$('#criarCampeonato').find('#temporadaCarousel').after(divSemCircuitosSelecinados);
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
	$('#temporadasLabel').html(temporada);
}

function criarCampeonato() {
	var dataObj = objetoCampeonato();
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
			toaster(lang_text('250'), 3000, 'alert alert-success');
			//carregaCampeonato();
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
		lst.push(lisSel[j].circuito.arquivo);
	}
	var dataObj = {
		nome : $('#nomeEquipeValor').val(),
		temporada : $('#nomePilotoValor').val(),
		corridaCampeonatos : lst
		};
	return dataObj;
}


function selecionaPilotosTemporada(temporada) {
	var urlServico = "/f1mane/rest/letsRace/temporadas/" + temporada;
	$.ajax({
		type : "GET",
		url : urlServico,
		contentType : "application/json",
		dataType : "json",
		success : function(response) {
			if (!response) {
				console.log('selecionaPilotosTemporada() null');
				return;
			}
			$('#criarCampeonato').addClass('hide');
			$('#selecionarPiloto').removeClass('hide');
			var pilotos = response.pilotos;
			var mapCarros = new Map();
			$('#pilotos').find('tr').remove();
			$.each(pilotos, function(i, val) {
				var tr = gerarTr1Pilotos(pilotos[i]);
				$('#pilotos').append(tr);
				var statusPilotoCarro = gerarTr2Pilotos(pilotos[i]);
				$('#pilotos').append(statusPilotoCarro);
				tr.unbind();
				tr.bind("click", function() {
					console.log(pilotos[i].nome + ' Selecionado');
				});
			});
			pilotoCarreiraTemporada();
		},
		error : function(xhRequest, ErrorText, thrownError) {
			tratamentoErro(xhRequest);
			console.log('selecionaPilotosTemporada() ' + xhRequest.status + '  ' + xhRequest.responseText);
		}
	});
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
				console.log(piloto.nome + ' Selecionado');
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


