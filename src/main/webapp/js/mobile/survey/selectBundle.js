$(function () {
    var currentBundleId;
    var availableGuiLanguages = [];

    // Get the available gui languages from the local changer in the header
    $('#localeChanger').children().each(function () {
        var guiLanguage = $(this).val();
        availableGuiLanguages.push(guiLanguage);
    });

    // If one of the available bundles was selected
    function handleBundleSelection(element) {
        currentBundleId = $(element).prop('value');

        // Enable the button if any language is available
        if ($('#bundleLanguageList_' + currentBundleId).find('option[id*=noLanguage]').length === 1) {
            $("#startSurveyButton").attr('disabled', true);
        } else {
            $("#startSurveyButton").attr('disabled', false);
        }

        // If the current clicked bundle was not selected before do some things:
        var currentOption = $(element).find("option[value*='" + currentBundleId + "']")
        if (!$(currentOption).hasClass("bundleSelected")) {

            // Disable the all hidden fields from the bundles
            $(".bundleEntry input[type='hidden']").prop("disabled", "disabled");
            // Remove the class which marks the bundle as selected
            $(".bundleSelected").removeClass("bundleSelected");

            // Hide the list of available incomplete encounters of all bundles
            $(".encounterList").hide();
            $(".bundleLanguagesList").hide();

            // Enable the newly selected bundle
            $(currentOption).addClass("bundleSelected");
            $(element).val(currentBundleId);

            // Show the list of available languages of the new bundle
            $('#bundleLanguageList_' + currentBundleId).show();

            // If another bundle was selected, any incomplete encounter selected in a modal dialog is to be de-selected
            deselectCurrentIncompleteEncounter();
            deselectCurrentLanguage();

            if ($('#bundleLanguageList_' + currentBundleId).find('option[id*=noLanguage]').length === 1) {
                //Disable language selection menu
                $('#bundleLanguageList_' + currentBundleId).find('select').attr('disabled', true); 
            } else {
                toggleLanguageSelects(currentBundleId);
                // Select the first option by default
                var elementToSelect = $('#bundleLanguageList_' + currentBundleId + ' option:first');
                $('#bundleLanguageList_' + currentBundleId).value = elementToSelect
                // Handle the language selection
                onLanguageSelect(elementToSelect);
            }

        }
        //Check if scroll Arrow needs to be shown
        toggleScrollIndicators();
    }


    // If one of the available bundles was selected
    //$("input[name='bundleRadio']").click(function () {
        //Handle bundle selection
    //    handleInputBundleSelection($(this)); 
    //});

    $("select[name=bundleId]").change(function () {
        handleBundleSelection($(this));
    })

    // If any language was selected
    $('.bundleLanguagesList select[name=bundleLanguage]').change(function () {
        // Handle the language selection
        onLanguageSelect($(this));
    });


    // If any incomplete encounter was selected
    $('.encounterList input[type=radio]').click(function () {
        onIncompleteEncounterClick($(this))
    });

    function onLanguageSelect(elem) {
        // Hide warning
        $('#guiLanguageWarning').hide();

        // Do not deselect an already selected language
        if (!$(elem).hasClass('languageSelected')) {
            deselectCurrentLanguage();
        }

        $(elem).addClass("languageSelected");

        var currentLocaleCode = $(elem).val();

        // The selected bundle language is the gui language by default
        $('#guiLanguage').val(currentLocaleCode);

        // If the current selected language is not in the list of available gui languages
        if ($.inArray(currentLocaleCode, availableGuiLanguages) === -1) {
            // But the corresponding 5 character locale code is in the list of available gui languages
            if ($.inArray(currentLocaleCode + "_" + currentLocaleCode.toUpperCase(), availableGuiLanguages) !== -1) {
                // Store the 5 character locale code for the GUI language
                $('#guiLanguage').val(currentLocaleCode + "_" + currentLocaleCode.toUpperCase());
            } // If the selected language is english, extend the language to english in Great Britain
            else if (currentLocaleCode === "en" && $.inArray(currentLocaleCode + "_GB", availableGuiLanguages) !== -1){
                $('#guiLanguage').val("en_GB");
            } // If the selected language is farsi, extend the language to farsi in Iran
            else if (currentLocaleCode === "fa" && $.inArray(currentLocaleCode + "_IR", availableGuiLanguages) !== -1){
                $('#guiLanguage').val("fa_IR");
            } // Otherwise show the language warning and set nothing for the GUI language
            else {
                $('#guiLanguageWarning').show();
                $('#guiLanguage').val('');
            }
        }

        // Hide the current shown incomplete encounter list
        $('.encounterList').hide();
        // Deselect the current selected incomplete encounter
        deselectCurrentIncompleteEncounter();

        // Show the encounter list for the selected bundle with the selected localeCode
        $('#encounterList_' + currentBundleId + '_' + currentLocaleCode).show();
        toggleEncounterSelects(currentBundleId, currentLocaleCode); 

        // Select the first option of the encounter list
        var newEncounterToSelect = $('#newEncounter_' + currentBundleId + '_' + currentLocaleCode);
        newEncounterToSelect.prop("checked", true); 
        newEncounterToSelect.addClass("encounterSelected");
    }

    function onIncompleteEncounterClick(elem) {
        // Do not deselect an already selected incomplete encounter
        if (!$(elem).hasClass('encounterSelected')) {
            deselectCurrentIncompleteEncounter();
        }

        $(elem).addClass("encounterSelected");
    }

    /**
     * Disables all language select menus in the form except for the current selected on
     * @param {*} id of the current selected languageSelect
     */
    function toggleLanguageSelects(id) {
        $(".bundleLanguagesList select[name=bundleLanguage]").attr("disabled", true); 
        $('#bundleLanguageList_' + id).find('select').attr('disabled', false); 
    }

    function toggleEncounterSelects(id, language) {
        $(".encounterList select[name=incompleteEncounterId]").attr("disabled", true); 
        $("div[id=encounterList_" + id + "_" + language + "]").find("select").attr("disabled", false)
    }

    function deselectCurrentIncompleteEncounter() {
        $(".encounterSelected").checked = false;
        $(".encounterSelected").removeClass("encounterSelected");
    }

    function deselectCurrentLanguage() {
        $(".languageSelected").checked=false; 
        $(".languageSelected").removeClass("languageSelected");
    }
        
    //to check if bundle has any language or not (only for test bundle page where bundle id is fixed and not selectable)
    if (window.location.href.indexOf("test") > -1) {
        if ($('#bundleLanguageList_' + $("select[name=bundleId]").prop('value')).find('option[id*=noLanguage]').length === 1) {
            $("#startSurveyButton").attr('disabled', true);
        } else {
            $("#startSurveyButton").attr('disabled', false);
        }
    }
});

