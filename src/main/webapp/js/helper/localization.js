function Localization() {
}

Localization.initShowLocalizedPage = function (currentLanguage, checkAvalaibleLanguages) {
    /* If this item was not already persited make the current language to the first of this page */
    if (checkAvalaibleLanguages == false) {
        /* Add the current language to the addedLanguages div */
        addLanguageToAddedLanguages(currentLanguage);
        /* If the question is already persisted */
    } else {
        /* Use the localablePostElement for loop through all added languages. This must be done
         * only for the first required localized element! Use the firstRequiredLocalableElement class therefore */
        $('.firstRequiredLocalableElement .localablePostElement').each(function () {
            var name = $(this).attr('name');
            var language = name.substring(name.indexOf('[') + 1, name.indexOf(']'));
            addLanguageToAddedLanguages(language);
        });
    }

    /* Bind a onClick function to every collapse-link which toggle the bi */
    $("[data-bs-toggle=collapse]").click(function () {
        collapsableClick($(this));
    });

};

Localization.addNewLanguage = function (selectedLanguageElement) {
    /* Get the language text from the dropdown */
    var language = selectedLanguageElement.attr('id');
    /* Get the language label from the dropdown element */
    var languageLabel = selectedLanguageElement.text();
    /* Get the flag image from the dropdown */
    var img = $(selectedLanguageElement).children('img').clone();
    /* Remove this language from the selection and add it to added languages */
    addLanguageToAddedLanguages(language);

    /* Loop through each element with the localable class */
    $('.localableLast').each(function () {
        createNewLocalizedFormElement($(this), language, languageLabel, img);
    });
};

Localization.deleteLanguage = function (languageToDeleteElement) {
    /* Get the id from the language button. I.e. de_DE_Delete. */
    var localCode = $(languageToDeleteElement).attr('id');
    /* Remove the 'Delete' at the end of the id to get the locale code*/
    localCode = localCode.substring(0, localCode.lastIndexOf('_'));
    /* Find all elements with the class form-group-[language]*/
    $('.localable-form-group-' + localCode).each(function () {
        /* Get the formGroup element */
        var formGroupDiv = $(this);
        /* If this element has either the class localableLast,
         * make sure, that the previous element get this class, to indicate where
         * a new added language must be added */
        if (formGroupDiv.hasClass('localableLast')) {
            formGroupDiv.prev().addClass('localableLast');
        }

        /* Remove the element */
        $(this).remove();
    });
    /* Add this language to the language dropdown to make it selectable again */
    $('#addLanguageDropdown #' + localCode).parent().css('display', 'block');

    /* Delete the language delete link for this language */
    $(languageToDeleteElement).remove();
};

/* PRIVATE FUNCTIONS */

function addLanguageToAddedLanguages(language) {
    /* Add this language to the addedLanguages div */
    var deleteLanguageButton = $('#addLanguageDropdown #' + language).clone();
    /* Remove dropdown-item class from language */ 
    deleteLanguageButton.removeClass('dropdown-item');
    /* Create a unique id to make the new element selectable via #-selector */
    deleteLanguageButton.attr('id', language + '_Delete');
    /* Show the dialog and add the id of the newly created element to this modal. This id will be used to make sure, that the right 
     * language will be deleted after the user confirmed the deletion */
    deleteLanguageButton.attr('onclick', "deleteLanguageFromAddedLanguages(" + $(deleteLanguageButton).attr('id') + ")");
    /* Create a bootstrap button based on the link */
    deleteLanguageButton.addClass('btn btn btn-primary');
    /* Add a remove bi to the button */
    var icon = $('<i/>', {
        "class": "bi-trash-fill",
        "style": "padding-left: 8px; top: 2px;"
    });
    deleteLanguageButton.append(icon);

    /* Style the new created button */
    deleteLanguageButton.css('margin', '6px');
    deleteLanguageButton.css('padding', '6px');

    $('#addedLanguages').append(deleteLanguageButton);
    /* Remove this language from the dropdown to avoid double added languages */
    $('#addLanguageDropdown #' + language).parent().css('display', 'none');
}
;

function createNewLocalizedFormElement(elem, language, languageText, img) {
    /* Remove the localableLast class from the current localableLast element. */
    $(elem).removeClass('localableLast');

    var newFormGroup = $('<div/>', {
        "class": 'form-group localable-form-group-' + language + ' localableLast'
    });

    /* Copy special css styles i.e. display=none */
    newFormGroup.css('display', $(elem).css('display'));

    /* Create a new id from the original collapsable container */
    var newCollapseId = $(elem).find('.localablePostElement').parent().attr('id');
    /* Are characters after the last '_' numeric, it is a localized select answer
     * and the answer counter must be added after the new language. E.g. localizedLabelCollapsableText_de_0 */
    var numberOfSelectAnswer = '';
    var charactersAfterLastSeperator = newCollapseId.substring(newCollapseId.lastIndexOf('_') + 1);
    if ($.isNumeric(charactersAfterLastSeperator) === true) {
        /* Get the answer counter out of the id E.g. localizedLabelCollapsableText_de_0 --> _0 */
        numberOfSelectAnswer = newCollapseId.substr(newCollapseId.lastIndexOf('_'));
    }
    /* E.g. localizedLabelCollapsableText_de_0 --> localizedLabelCollapsableText_ */
    newCollapseId = newCollapseId.substr(0, newCollapseId.indexOf('_') + 1);
    /* If the element is not a select answer, the numberOfSelectAnswer contains an empty string */
    /* E.g. localizedLabelCollapsableText_ + ar + _0 */
    newCollapseId = newCollapseId + language + numberOfSelectAnswer;

    /* Create the collapse link */
    var collapseLink = $('<a/>', {
        href: '#' + newCollapseId,
        'data-bs-toggle': 'collapse',
        style: 'color: #000000; text-decoration: none;',
        ondragstart: 'return false;'
    });

    /* Bind a onClick function to the new link */
    collapseLink.click(function () {
        collapsableClick($(this));
    });
    /* Clone the language label and set text/img to the new added language. 
     * Use find() instead of children(), because the textarea is two levels below in the DOM. */
    var languageLabel = $(elem).find('.languageLabel').clone();
    languageLabel.text(languageText + ' ');

    /* languageLabel.append(img) does not work; use mixed pure js and jQuery instead */
    languageLabel.append(img[0].outerHTML);
    languageLabel.css('padding-right', '7px');

    /* Clone the description label, which is the next element after the language label. 
     * Use find() instead of children(), because the textarea is two levels below in the DOM. */
    var descriptionLabel = $(elem).find('.languageLabel').next().clone();
    /* Get the bootstrap icon element */
    var bootstrapSpan = $(elem).find('a .bi').clone();
    /* Add both labels and the bi element to the cloned collapse link */
    collapseLink.append(languageLabel).append(descriptionLabel).append(bootstrapSpan);

    /* Create the collapsable textarea container */
    var collapsableContainer = $('<div/>', {
        "class": 'collapse show',
        "id": newCollapseId
    });

    /* Add the link and the textarea container to the new div element */
    newFormGroup.append(collapseLink).append(collapsableContainer);

    /* Create a new post element */
    var newPostElement = $(elem).find('.localablePostElement').clone();
    /* Get the name from the element which will be sent to the server after submitting the form */
    var nameFromLocalablePostElement = $(elem).find('.localablePostElement').attr('name');
    /* Get the string from the beginning until the last '['. E.g. answers[0].localizedFreetextLabel[de] --> answers[0].localizedFreetextLabel */
    nameFromLocalablePostElement = nameFromLocalablePostElement.substr(0, nameFromLocalablePostElement.lastIndexOf('['));
    /* Add the language to the name of the new element to keep the languages apart on the server */
    newPostElement.attr('name', nameFromLocalablePostElement + '[' + language + ']');
    /* Add the textarea to the new textareaContainer */
    collapsableContainer.append(newPostElement);

    /* Add the cloned element */
    $(elem).after(newFormGroup);

    /* If the new created post element is a wysiwyg editor, initialize it with summernote */
    if (newPostElement.hasClass('wysiwyg')) {
        /* Let summernote create a wysiwyg editor from the newly added textarea */
        Application.createNewWysiwygEditor(newPostElement, language);
    }
}

function collapsableClick(elem) {
    var bootstrapIconSpan = elem.children('span');
    var formGroupContainer = elem.parent();

    if (bootstrapIconSpan.hasClass('bi-chevron-up')) {
        /* Change the icon from minus to plus*/
        bootstrapIconSpan.removeClass('bi-chevron-up');
        bootstrapIconSpan.addClass('bi-chevron-down');
        /* Check if the collapsed input/textarea has any content. 
         * If so, mark the collapsable link green. Mark it yellow otherwise.
         * For that the alert-classes will be used. */
        var collapsingTextarea = elem.next().children('textarea');
        var collapsingInput = elem.next().children('input');

        /* Check val() for inputs and code() for textareas */
        var hasContent = false;
        /* Check if the content is an empty string (like it is in Chrome and Firefox if there is no content) and for "<br>" (in IE if there is no content) */
        if ($(collapsingTextarea).hasClass("wysiwyg")) {
            if (collapsingTextarea.length > 0 && collapsingTextarea.summernote("code").replace(/&nbsp;/g, "").replace(/<br>/g, "").replace(/ /g, "").replace(/<p><\/p>/g, "") !== "") {
                hasContent = true;
            }
        } else {
            if (collapsingTextarea.length > 0 && collapsingTextarea.val() !== "") {
                hasContent = true;
            }
        }
        if (collapsingInput.length > 0 && collapsingInput.val() !== "" && collapsingInput.val().replace(/\s/g, "").length > 0) {
            hasContent = true;
        }

        /* If the textarea/input has content, mark it green */
        if (hasContent === true) {
            formGroupContainer.addClass('alert alert-success');
            /* Mark it yellow otherwise */
        } else {
            formGroupContainer.addClass('alert alert-warning');
        }

        /* Make the link floating over the whole page width, to make the colored
         * parent div looks like it is clickable */
        elem.css('float', 'right');
        elem.css('width', '100%');
        /* Remove the focus from this link: otherwise Firefox shows up a border after clicking */
        elem.blur();
        /* Center the link */
        formGroupContainer.css('padding', '10px');
        formGroupContainer.css('height', '40px');

    } else if (bootstrapIconSpan.hasClass('bi-chevron-down')) {
        /* Delete all made css, to reset the original design */
        formGroupContainer.css('padding', '');
        formGroupContainer.css('height', '');
        elem.css('float', '');
        elem.css('width', '');
        /* Remove the focus from this link: otherwise Firefox shows up a border after clicking */
        elem.blur();

        /* Change the icon from plus to minus */
        bootstrapIconSpan.removeClass('bi-chevron-down');
        bootstrapIconSpan.addClass('bi-chevron-up');
        /* Remove all alert classes from the parent div */
        formGroupContainer.removeClass('alert alert-warning alert-success');
    }
}

function deleteLanguageFromAddedLanguages(clickedLanguageId) {
    /* There must be at least one language in this question */
    if ($('#addedLanguages').children().length === 1) {
        $('#deleteLastLanguageModal').modal('show');
    } else {
        /* If there is more than one language in this question, show the dialog
         * and add the id from the language element which should be deleted */
        $('#deleteLanguageModal').data('id', clickedLanguageId).modal('show');
    }
}