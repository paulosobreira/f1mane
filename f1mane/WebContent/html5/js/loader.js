var loader = $('<div class="loader"></div>');
$('body').prepend(loader);
var $loading = loader.hide();
$(document).ajaxStart(function() {
	mostraLoad();
}).ajaxStop(function() {
	escondeLoad();
});

function mostraLoad(){
	$("button").prop("disabled",true);
	$("a").prop("disabled",true);
	$('#f1body').hide();
	$loading.show();
}

function escondeLoad(){
	$("button").prop("disabled",false);
	$("a").prop("disabled",false);
	$loading.hide();
	$('#f1body').show();
}