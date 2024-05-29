/**
 * This ensures that the current dialog overlay the current site instead of hiding the current site
 */
$(function() {
    $('body').on('pagebeforeshow', 'div[data-role="dialog"]', function(e, ui) {
        ui.prevPage.addClass("ui-dialog-background");
    });
		
    $('body').on('pagehide', 'div[data-role="dialog"]', function(e, ui) {
        $(".ui-dialog-background ").removeClass("ui-dialog-background");
    });
});