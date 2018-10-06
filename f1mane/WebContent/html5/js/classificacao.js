/**
 * Controle de classificacao
 */
$('#classificacao_geral').html(lang_text('geral'));
$('#classificacao_circuito').html(lang_text('circuito'));
$('#classificacao_equipes').html(lang_text('277'));
$('#classificacao_temporada').html(lang_text('251'));
$('#classificacao_campeonato').html(lang_text('268'));
if(localStorage.getItem("nomeJogador")){
	$('#nomeJogador').append('<b>' + localStorage.getItem("nomeJogador") + '</b>');
}
if(localStorage.getItem("imagemJogador")){
	$('#imgJogador').attr('src', localStorage.getItem("imagemJogador"));
}