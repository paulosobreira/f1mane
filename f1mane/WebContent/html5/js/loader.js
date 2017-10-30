var loader = $('<div class="loader"></div>');
$('body').prepend(loader);
var $loading = loader.hide();
$(document).ajaxStart(function() {
	$("button").prop("disabled",true);
	$("a").prop("disabled",true);
	$('body').prepend($('<div class="capa"></div>'));
	$loading.show();
}).ajaxStop(function() {
	$("button").prop("disabled",false);
	$("a").prop("disabled",false);
	$loading.hide();
	$('body').find('.capa').remove();
});