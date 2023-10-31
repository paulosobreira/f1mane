/**
 * Controle do menu inicial
 */
$('#voltar').hide();
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
	localStorage.setItem("token", token);
	dadosJogador();
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

function dadosJogador() {
	var urlServico = "/f1mane/rest/letsRace/dadosToken/";
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
	var urlServico = "/f1mane/rest/letsRace/sobre/";
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

    function handleCredentialResponse(response) {
        const data = jwt_decode(response.credential)
        var urlServico = "/f1mane/rest/letsRace/criarSessaoGoogle";
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
            client_id: "143142801251-n0a6bc0rs03h41bt7ganklmlrokqn6te.apps.googleusercontent.com",
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
