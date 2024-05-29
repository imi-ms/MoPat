function centerCopyrightText() {
    // run after page is ready (fully loaded)
    // Make a autofocus if the browser does not support touch
    // Calculate the width of the copyright text and center it
    var blockAWidth = $(".footer .ui-block-a").width();
    var blockBTextWidth = $(".footer .ui-block-b span").width();
    var blockCWidth = $(".footer .ui-block-c").width();
    $(".footer .ui-block-b span").css("margin-left", ($(window).width() - blockAWidth - blockCWidth - blockBTextWidth) / 2);
}