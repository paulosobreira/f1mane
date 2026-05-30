/**
 * Controle do menu inicial
 */
$('#voltar').hide();
$('#sair').hide();
$('#entrar').show();
$('#painelLogado').hide();
if (localStorage.getItem("versao") != $("#versao").val()) {
	console.log('Limpando localStorage versao: ' + $("#versao").val());
	var token = localStorage.getItem("token");
	var plataforma = localStorage.getItem('plataforma');
	var tela = localStorage.getItem('tela');
	localStorage.clear();
	localStorage.setItem("versao", $("#versao").val());
	localStorage.setItem('plataforma', plataforma);
	localStorage.setItem('tela', tela);
	if (token != null) {
		localStorage.setItem("token", token);
	}
}

localStorage.removeItem("idPilotoSelecionado");
localStorage.removeItem("nomeJogo");
localStorage.removeItem("nomeJogador");
localStorage.removeItem("imagemJogador");
localStorage.setItem("modoCarreira", false);
localStorage.setItem("modoCampeonato", false);
$(document).ready(function () {
	var nome = localStorage.getItem('nomeJogadorSessao');
	if (nome) {
		$('#nomeJogadorSessao').val(nome);
	}
});

var token = getParameter('token');

var plataforma = getParameter('plataforma');

var limpar = getParameter('limpar');

if (limpar == 'S') {
	localStorage.removeItem("token");
}

if (token == null) {
	token = localStorage.getItem("token");
}

if (token != null) {
	dadosJogador();
    $('#buttonDiv').remove();
    $('#sair').show();
    $('#entrar').hide();
    $('#painelLogado').show();
}

if (plataforma != null) {
	localStorage.setItem("plataforma", plataforma);
}

plataforma = localStorage.getItem('plataforma');

if (plataforma == "android") {
	$('#voltar').show();
	$('#voltar').unbind().bind("click", function() {
		Android.exitApp();
	});
}

var userLang = navigator.language || navigator.userLanguage;
if (userLang != null && localStorage.getItem('idioma') == null) {
	if (userLang.split('-')[0] == 'pt') {
		lang_idioma('pt', true);
	} else if (userLang.split('-')[0] == 'it') {
		lang_idioma('it', true);
	} else {
		lang_idioma('en', true);
	}
} else {
	lang_idioma(localStorage.getItem('idioma'), true);
}

$('#btnJogar').html(lang_text('jogar'));
$('#btnSobre').html(lang_text('sobre'));
$('#btnControles').html(lang_text('verControles'));
$('#btnConfiguracao').html(lang_text('configuracao'));
$('#btnClassificacao').html(lang_text('ranking'));
$('#btnEquipe').html(lang_text('221'));
$('#btnCampeonato').html(lang_text('268'));
$('#btnCafe').html(lang_text('cafe'));
$("#nomeJogadorSessao")
        .attr("placeholder", lang_text('nomeJogadorSessao'));

$('#btnSobre').bind("click", function() {
	$('#botoes').hide();
	sobre();
	$('#voltar').show();
	$('#voltar').unbind().bind("click", function() {
		$('.creditos').remove();
		$('#botoes').show();
		if (plataforma == "android") {
			$('#voltar').unbind().bind("click", function() {
				Android.exitApp();
			});
		} else {
			$('#voltar').hide();
		}
	});
});

$('#sair').bind("click", function() {
    localStorage.removeItem("token");
    location.reload();
});

$('#entrar').bind("click", function() {
    criarSessao();
});

function criarSessao() {
    var nome = $('#nomeJogadorSessao').val();
	var urlServico = "/flmane/rest/letsRace/criarSessaoNome";
	var headers = {};
	if (nome && nome.trim() !== '') {
		headers.nome = nome.trim();
	}else{
	    return;
	}
	$.ajax({
		type : "GET",
		url : urlServico,
		headers : headers,
		contentType : "application/json",
		dataType : "json",
		success : function(sessaoVisitante) {
			token = sessaoVisitante.sessaoCliente.token;
			localStorage.setItem("token", token);
			$('#entrar').hide();
			$('#sair').show();
		},
		error : function(xhRequest, ErrorText, thrownError) {
			tratamentoErro(xhRequest);
			console.log('criarSessao() ' + xhRequest.status + '  ' + xhRequest.responseText);
		}
	});
}


function dadosJogador() {
	var urlServico = "/flmane/rest/letsRace/dadosToken/";
	$.ajax({
		type : "GET",
		url : urlServico,
		headers : {
			'token' : localStorage.getItem("token")
		},
		success : function(srvPaddockPack) {
			if (srvPaddockPack) {
				localStorage.setItem("nomeJogador", srvPaddockPack.sessaoCliente.nomeJogador);
				$('#nomeJogador').empty();
                $('#nomeJogador').append('<b>' + localStorage.getItem("nomeJogador") + '</b>');
				if (!srvPaddockPack.sessaoCliente.guest) {
					localStorage.setItem("imagemJogador", srvPaddockPack.sessaoCliente.imagemJogador);
					$('#imgJogador').attr('src', localStorage.getItem("imagemJogador"));
				}
				if (srvPaddockPack.sessaoCliente.jogoAtual) {
					localStorage.setItem("nomeJogo", srvPaddockPack.sessaoCliente.jogoAtual);
				}
			}
		},
		error : function(xhRequest, ErrorText, thrownError) {
			$('#botoes').show();
			if (xhRequest.status == 404) {
				toaster(lang_text('210'), 4000, 'alert alert-danger');
				localStorage.removeItem("token");
				return;
			}
			tratamentoErro(xhRequest);
			console.log('sobre() ' + xhRequest.status + '  ' + xhRequest.responseText);
		}
	});
}

function sobre() {
	var urlServico = "/flmane/rest/letsRace/sobre/";
	$.ajax({
		type : "GET",
		url : urlServico,
		success : function(sobreRes) {
			var dv = $('<div class="creditos"></div>');
			dv.append(sobreRes);
			$('#mainContainer').append(dv);
			dv.bind("click", function() {
				dv.remove();
				$('#botoes').show();
				$('#voltar').hide();
			});
		},
		error : function(xhRequest, ErrorText, thrownError) {
			$('#botoes').show();
			tratamentoErro(xhRequest);
			console.log('sobre() ' + xhRequest.status + '  ' + xhRequest.responseText);
		}
	});
}
	//Ripple-effect animation
    (function($) {
        $(".ripple-effect").click(function(e){
            var rippler = $(this);

            // create .ink element if it doesn't exist
            if(rippler.find(".ink").length == 0) {
                rippler.append("<span class='ink'></span>");
            }

            var ink = rippler.find(".ink");

            // prevent quick double clicks
            ink.removeClass("animate");

            // set .ink diametr
            if(!ink.height() && !ink.width())
            {
                var d = Math.max(rippler.outerWidth(), rippler.outerHeight());
                ink.css({height: d, width: d});
            }

            // get click coordinates
            var x = e.pageX - rippler.offset().left - ink.width()/2;
            var y = e.pageY - rippler.offset().top - ink.height()/2;

            // set .ink position and add class .animate
            ink.css({
              top: y+'px',
              left:x+'px'
            }).addClass("animate");
        })
    })(jQuery);

    /**
    precisa dominio valido para login com gooogle
    function handleCredentialResponse(response) {
        const data = jwt_decode(response.credential)
        var urlServico = "/flmane/rest/letsRace/criarSessaoGoogle";
        $.ajax({
            type : "GET",
            url : urlServico,
            headers : {
                'idGoogle' : data.sub,
                'nome' : data.name,
                'urlFoto' : data.picture,
                'email' : data.email
            },
            success : function(srvPaddockPack) {
                if (srvPaddockPack) {
                    localStorage.setItem("nomeJogador", srvPaddockPack.sessaoCliente.nomeJogador);
                    localStorage.setItem("token", srvPaddockPack.sessaoCliente.token);
                    $('#nomeJogador').empty();
                    $('#nomeJogador').append('<b>' + localStorage.getItem("nomeJogador") + '</b>');
                    if (!srvPaddockPack.sessaoCliente.guest) {
                        localStorage.setItem("imagemJogador", srvPaddockPack.sessaoCliente.imagemJogador);
                        $('#imgJogador').attr('src', localStorage.getItem("imagemJogador"));
                    }
                    if (srvPaddockPack.sessaoCliente.jogoAtual) {
                        localStorage.setItem("nomeJogo", srvPaddockPack.sessaoCliente.jogoAtual);
                    }
                    $('#buttonDiv').remove();
                    $('#sair').show();
                    $('#entrar').hide();
                    $('#painelLogado').show();
                }
            },
            error : function(xhRequest, ErrorText, thrownError) {
                $('#botoes').show();
                if (xhRequest.status == 404) {
                    toaster(lang_text('210'), 4000, 'alert alert-danger');
                    localStorage.removeItem("token");
                    token = null;
                }
                tratamentoErro(xhRequest);
                console.log('sobre() ' + xhRequest.status + '  ' + xhRequest.responseText);
            }
        });
        console.log("JWT ID token: " + data);
    }
    if (localStorage.getItem("token") == null){
        window.onload = function () {
          google.accounts.id.initialize({
            client_id: $("#idGoogle").val(),
            callback: handleCredentialResponse
          });
          google.accounts.id.renderButton(
            document.getElementById("buttonDiv"),
            { type:"standard",
              theme: "outline",
              size: "large" ,
              logo_alignment:"center" }  // customization attributes
          );
          google.accounts.id.prompt(); // also display the One Tap dialog
        }
    }
    **/
