function initLogin() {
    // Bind the init function on the pagechange event
    $(document).bind('pagebeforeshow', function () {
        setTimeout(function () {
            init();
        }, 250);
        setTimeout(function () {
            centerCopyrightText();
        }, 1500);
    });
    // Execute the init method initially
    init();
}

function init() {
    // Spring sometimes creates a second ui-page. Thats a problem, because the
    // following selectors select the dom-elements in the first ui-page, which 
    // is not shown. So the elements still remain in the showed ui-page.
    // If there are 2 ui-pages remove the first one, which is hidden anyway
    if ($('.ui-page').length === 2) {
        $('.ui-page:first').remove();
        // Make the ui-page's height to 100%
        $(".ui-page").height('100%');
    }
    // Remove the close button on the login page
    $("#closeDialogButton").remove();
    $("#closeDialogNavLink").remove(); 

    //Remove the help button on the login page
    $("#helpButton").remove();

    // Align the mopat logo to the center
    $('#mopatImage').parent().css('text-align', 'center');

    // Focus the username input field
    if (!Modernizr.touchevents) {
        $("#username").focus();
    }
    // custom placeholder for login fields if there is no native support
    if (!Modernizr.input.placeholder) {
        $('#username,#password').focus(function () {
            var input = $(this);
            if (input.val() === input.attr('placeholder')) {
                input.val('');
                input.removeClass('placeholder');
            }
        }).blur(function () {
            var input = $(this);
            if (input.val() === '' || input.val() === input.attr('placeholder')) {
                input.addClass('placeholder');
                input.val(input.attr('placeholder'));
            }
        }).blur().parents('form').submit(function () {
            $(this).find('#username,#password').each(function () {
                var input = $(this);
                if (input.val() === input.attr('placeholder')) {
                    input.val('');
                }
            });
        });
    }

    initHeader();

}

 /**
  * Checks the current screen size on size change (e.g. if tablet is rotated)
  * and shows an error message while hiding all content.
  * If screen size is bigger again content is then shown again
  
var contentIsVisible = true; 

$(window).on('resize', function() {

    if ($(window).width() < 960) {
        $('#content').hide();
        $('#footer').hide();
        $('#orientationErrorScreen').show();
         contentIsVisible = false;
    }
    else if ($(window).width() > 960 && contentIsVisible == false) {
        $('#content').show(); 
        $('#footer').show();
        $('#orientationErrorScreen').hide();
        contentIsVisible = true; 
    }
});

*/


function initHeader() {
    //Specify the languages here that should be removed from the front page
    let languagesToRemove = [
        "fr_FR", "pl_PL", "it_IT", "tr_TR", 
        "ar", "fa_IR", "dari", "ku", "hi_IN", 
        "nl_NL", "no_NO", "pt_PT", "ru_RU", 
        "sv_SE", "sq_AL"
    ]

    languagesToRemove.forEach(language => {
        // Remove unnecessary languages from the login page
        $("#localeChanger option[value='" + language + "']").remove();
        // Remove them from the mobile nav as well 
        $("#" + language).remove(); 
    });

    // Set the language dropdown to the current language (application default or current used language)
    // by getting the option which describes the language and set it to selected. Refresh the element after that.
    if (typeof currentLanguage !== 'undefined') {
        $("#localeChanger").val(currentLanguage);
    } else {
        $("#localeChanger").val("de_DE");
    }
}