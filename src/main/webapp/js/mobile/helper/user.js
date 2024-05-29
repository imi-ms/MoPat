function initUser() {
    // Bind the init function on the pagechange event
    $(document).bind('pagechange', function() {
        initUserInterface();
    });
    // Execute the init method initially
    initUserInterface();
    
    function initUserInterface() {
        // Spring sometimes creates a second ui-page. Thats a problem, because the
        // following selectors select the dom-elements in the first ui-page, which 
        // is not shown. So the elements still remain in the showed ui-page.
        // If there are 2 ui-pages remove the first one, which is hidden anyway
        if ($('.ui-page').length === 2) {
            $('.ui-page')[0].remove();
        }

        // Show the header on the forgot password page
        $(".header").show();
        
        centerCopyrightText();
    
        // Set the language dropdown to the current language (application default or current used language)
        // by getting the option which describes the language and set it to selected. Refresh the element after that.
        $("#localeChanger").val(currentLanguage);
    }
}
