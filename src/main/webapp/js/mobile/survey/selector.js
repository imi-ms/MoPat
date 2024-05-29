/**
 * This module is used to select elements which pose as answers, and store them in a response, which is then
 * put into the encounter object. Moreover, in case some elements have been selected previously, it makes sure that the
 * selected elements are also visible to the user as such.
 */
function Selector() {

}

/**
 * Selects the element with the corresponding answerId and updates
 * the corresponding response with the provided values.
 * 
 * @param questionId The id of the shown question
 * @param answerId The id of the answer of the element that has been or is to be selected
 * @param value The value of the element
 * @param customtext The customtext of the element
 * @param manual Indicates whether the selection was done manually by a user or by the system.
 */
Selector.selectElement = function (questionId, answerId, value, customtext, date, manual) {

    var question = encounter.getCurrentQuestionnaire().getQuestionById(questionId);
    var answer = question.getAnswerById(answerId);
    switch (question.questionType) {
        // MULTIPLE CHOICE
        case Questiontypes.MULTIPLE_CHOICE:
            if (answer.localizedLabel !== null) {
                Selector.selectAnswer(question, answer, manual);
            } else {
                Selector.setFreetext(answerId, customtext);
            }
            break;
            // NUMBER CHECKBOX TEXT
        case Questiontypes.NUMBER_CHECKBOX_TEXT:
            Selector.setNumberedCheckboxText(question, value, customtext);
            break;
            // NUMBER CHECKBOX
        case Questiontypes.NUMBER_CHECKBOX:
            Selector.selectNumberedCheckbox(question, answer, value, manual);
            break;
            // SLIDER
        case Questiontypes.SLIDER:
            Selector.selectSlider(question, answer, value, manual);
            break;
            // FREE TEXT 				
        case Questiontypes.FREE_TEXT:
        case Questiontypes.BARCODE:
            Selector.setFreetext(answerId, customtext);
            break;
            // DROP DOWN               
        case Questiontypes.DROP_DOWN:
            if (answer.localizedLabel !== null) {
                Selector.selectDropDownOption(question, answer);
            } else {
                Selector.setFreetext(answerId, customtext);
            }
            break;
            // DATE
        case Questiontypes.DATE:
            Selector.setDate(question, date, manual);
            break;
        case Questiontypes.NUMBER_INPUT:
            Selector.setInput(question, value, manual);
            break;
        case Questiontypes.IMAGE:
            Selector.setImage(question, value);
            break;
        case Questiontypes.BODY_PART:
            Selector.selectBodyPart(question, answer, manual);
            break;
    }

    if (question !== null) {
        var isResponseValid = question.isResponseValid(encounter.getResponsesForQuestion(question));
        // If a valid response is given, the next button flashes two times
        if (isResponseValid === true && manual === true) {
            $("#buttonNext").addClass("btn-attention");
            $("#buttonNext").addClass("btn-animate"); 
            setTimeout(function() {
                $("#buttonNext").removeClass("btn-animate"); 
              }, 4000);
        } else {
            $("#buttonNext").removeClass("btn-attention"); 
            $("#buttonNext").removeClass("btn-animate"); 
        }
    }
};

/**
 * Updates the input with the provided value and updates the ecounter.
 */
Selector.setInput = function (question, value, manual) {
    var answer = question.answers[0];
    var response = encounter.getResponse(answer.id);
    var isSelected = true;

    if (response === null) {
        response = new Response();
    }

    var input = $("#numberInput");
    var tooltip = $("#toolTipText");

    // If the given value is not an Integer empty the input field
    if (isNaN(value)) {
        value = "";
        isSelected = false;
    }
    // Handle the condition if the the answer was given manually
    if (manual === true) {
        this.handleConditions(question, answer, value, isSelected);
    }
    // If the given response is out of range move it into the range
    if (value === "") {
        encounter.removeResponse(response);
        input.val("");
        return;
    } else if (answer.minValue !== null && value < answer.minValue) {
        value = answer.minValue;
    } else if (answer.maxValue !== null && value > answer.maxValue) {
        value = answer.maxValue;
    } else if (answer.stepsize !== null) {
        if ( value != stepsizeRounding(answer.minValue, value, answer.stepsize) ) {
            value = stepsizeRounding(answer.minValue, value, answer.stepsize);
            tooltip.html("Zahl wurde gerundet auf "+ value);
            tooltip.css("opacity","100");
        }
    }

    response.answerId = answer.id;
    response.value = value;
    input.val(value);
    setTimeout(()=>{
     tooltip.css("opacity","0");
    },2000)
    encounter.mergeResponse(response);
};

/**
 * Updates the textarea with the provided customtext and updates the ecounter.
 */
Selector.setFreetext = function (answerId, customtext) {
    var response = encounter.getResponse(answerId);
    if (response === null) {
        response = new Response();
    }

    if (customtext === "") {
        encounter.removeResponse(response);
        return;
    } else {
        response.answerId = answerId;
        response.customtext = removeInvalidChars(customtext);

        var textarea = $("#textarea");
        // Set value of the textarea
        textarea.val(customtext);
        // Get the line count of the customtext and set the rows of the textarea
        if (customtext) {
            var lineCount = Math.ceil(parseInt(textarea[0].scrollHeight / parseInt($('textarea').css('lineHeight'))));
            textarea.attr('rows', lineCount);
        }

        encounter.mergeResponse(response);
    }
};


/**
 * Selects the slider and moves it to the appropriate position with regards to the value
 * and updates the ecounter.
 */
Selector.selectSlider = function (question, answer, value, manual) {
    var response = encounter.getResponse(question.answers[0].id);
    var oldResponseValue;
    if (response === null) {
        response = new Response();
        // A slider always has one answer
        response.answerId = question.answers[0].id;
    } else {
        // Set the old response value for condition handling
        oldResponseValue = response.value;
    }

    if (value === null || value === "" || value === undefined) {
        encounter.removeResponse(response);
        // Handle the condition if the the answer was given manually
        if (manual === true) {
            this.handleConditions(question, answer, oldResponseValue, false);
        }
    } else {
        response.value = value;
        // Handle the condition if the the answer was given manually
        if (manual === true) {
            this.handleConditions(question, answer, value, true);
        } else {
            //manual = false means it is triggered by showQuestion method
            //If there is a value 
            //Show all slider elements and update the values accordingly to previous answers
            var questionObject = new Question(); 

             $("#sliderInput").val(value); 
             $("#range").val(value); 
             questionObject.setSliderValue(); 
             questionObject.toggleSliderValueOntop(true); 
        }
        encounter.mergeResponse(response);
    }
};

/**
 * Selects the numbered checkbox with the passed value and updates the encounter.
 */
Selector.selectNumberedCheckbox = function (question, answer, value, manual) {
    var response = encounter.getResponse(question.answers[0].id);
    var oldResponseValue;
    if (response === null) {
        response = new Response();
    } else {
        // Set the old response value for condition handling
        oldResponseValue = response.value;
    }

    var idPostfix = Question.replaceDotsForValidHtmlId(value);

    let checked = $("#numberedCheckbox_" + idPostfix).attr("checked");
    if (typeof checked !== 'undefined' && checked !== false) {
        //If already checked only deselect all checkboxes
        deselectAllCheckboxes(); 
        // Handle the condition if the the answer was given manually
        if (manual === true) {
            this.handleConditions(question, answer, oldResponseValue, false);
        }
        encounter.removeResponse(response);

        // if it is an unchecked element, deselect all, but mark the current one as checked
    } else {
        deselectAllCheckboxes();
        addChecked($("#numberedCheckbox_" + idPostfix));
        // there is only one answer for a checkbox question
        response.answerId = question.answers[0].id;
        response.value = value;
        // Handle the condition if the the answer was given manually
        if (manual === true) {
            this.handleConditions(question, answer, value, true);
        }
        encounter.mergeResponse(response);
    }
};

/**
 * Updates the textarea with the provided customtext, selected the appropriate checkbox,
 * and updates the ecounter.
 */
Selector.setNumberedCheckboxText = function (question, value, customtext) {
    var response = encounter.getResponse(question.answers[0].id);
    // If there is no response already, create a new one.
    if (response === null) {
        response = new Response();
    }

    // If either the value nor the customtext is set, delete the response
    if (customtext === "" && value === "") {
        encounter.removeResponse(response);
        return;
    }

    // Check if the customtext has changed
    var customtextHasChanged = false;
    if (customtext !== response.customtext) {
        customtextHasChanged = true;
    }

    // Set the current customtext to the response
    response.customtext = removeInvalidChars(customtext);
    // Get the textarea
    var textarea = $("#textarea");
    // Insert the customtext in the textarea and set it to the response
    textarea.val(response.customtext);
    // Get the line count of the customtext and set the rows of the textarea
    if (response.customtext) {
        textarea.css("min-height", textarea[0].scrollHeight + "px");
    }

    var idPostfix = Question.replaceDotsForValidHtmlId(value);
    let checked = $("#numberedCheckbox_" + idPostfix).attr("checked");

    // if there is a value, set the checkboxes and the value of the response
    if (value !== "") {
        // If the customtext has not changed but the current checkbox is still selected De-select it
        if (typeof checked !== 'undefined' && checked !== false && !customtextHasChanged) {
            //If already checked only deselect all checkboxes
            deselectAllCheckboxes(); 
            // Remove the value from the response
            response.value = '';
            // Otherwise check the clicked checkbox and deselect all other
        } else {
            // if it is an unchecked element, deselect all, but mark the current one as checked
            deselectAllCheckboxes();
            addChecked($("#numberedCheckbox_" + idPostfix));
            // Set the value of the response
            response.value = value;
        }
    }

    // there is only one answer for a checkbox question
    response.answerId = question.answers[0].id;

    encounter.mergeResponse(response);
};

function deselectAllCheckboxes() {
    $("#checkboxValue").removeAttr("value"); 

    $("input[checked=checked]").each(function(_, element) {
        deselectCheckbox(element); 
    });
}

function deselectCheckbox(element) {
    $(element).get(0).removeAttribute("checked"); 
    $(element).prop("checked",false).parent().removeClass("active");
}

function addChecked(inputElement) {
    $(inputElement).attr("checked", "checked");  
}

Selector.selectDropDownOption = function (question, answer) {
    // Delete the last given answer
    for (var i = 0; i < question.answers.length; i++) {
        if (encounter.getResponse(question.answers[i].id) !== null) {
            this.handleConditions(question, question.answers[i], null, false);
            encounter.removeResponse(encounter.getResponse(question.answers[i].id));
            break;
        }
    }
    // If no answer is given select the item on index 0
    if (answer === null) {
        // Set the value of the dropDown
        $('#dropDown').select(0);
        //disable the checkbox
        $('#textarea').attr('disabled', 'disabled');
        //remove response 
        encounter.removeResponse(response);
    } else {
        // Get the answer id
        var answerId = answer.id;
        // Create a new response
        var response = new Response();
        // Search for the selected answer
        for (var i = 0; i < question.answers.length; i++) {
            if (question.answers[i].id === answerId) {
                // Set the answerId of the selected answer to the response
                response.answerId = question.answers[i].id;
                break;
            }
        }
        // Set the value of the dropDown
        $('#dropDown').val(answerId.toString());

        // If the answer is marked as other enable the textarea and the response
        if (answer.isOther === true) {
            $('#textarea').removeAttr('disabled');

            for (answerIndex in question.answers) {
                if (question.answers[answerIndex].localizedLabel === null) {
                    freetextResponse = encounter.getResponse(question.answers[answerIndex].id);
                    if (freetextResponse !== null) {
                        freetextResponse.enabled = true;
                    }
                    break;
                }
            }
        } else {
            // If the unchecked answer is marked as other disable the textarea and 
            // the response of the freetext answer
            $('#textarea').attr('disabled', 'disabled');

            for (answerIndex in question.answers) {
                if (question.answers[answerIndex].localizedLabel === null) {
                    freetextResponse = encounter.getResponse(question.answers[answerIndex].id);
                    if (freetextResponse !== null) {
                        freetextResponse.enabled = false;
                    }
                    break;
                }
            }
        }
        this.handleConditions(question, answer, null, true);
        // Merge the responses
        encounter.mergeResponse(response);
    }
};

/**
 * Selects the answer with the given answerId and updates the encounter.
 */
Selector.selectAnswer = function (question, answer, manual) {
    var answerId = answer.id;
    var answerValue = answer.value;
    var isSelected = true;

    var response = encounter.getResponse(answerId);
    if (response === null) {
        response = new Response();
    }

    var checkedClass = 'checkbox-on';
    var uncheckedClass = 'checkbox-off';
    if (question.maxNumberAnswers === 1) {
        checkedClass = 'radio-on';
        uncheckedClass = 'radio-off';
    }

    // If the clicked button is already selected, remove this response, 
    // deselect the button, handle the conditions and return
    if ($("#" + answerId).hasClass(checkedClass) === true) {
        $("#" + answerId).removeClass(checkedClass);
        $("#" + answerId).addClass(uncheckedClass);
        // Checkboxes must be checked seperat
        $("#input" + answerId + ":radio").prop('checked', false);

        isSelected = false;
        encounter.removeResponse(response);

        // If the unchecked answer is marked as other disable the textarea and 
        // the response of the freetext answer
        if (answer.isOther === true) {
            $('#textarea').attr('disabled', 'disabled');
            $('#textarea').addClass('state-disabled');

            for (answerIndex in question.answers) {
                if (question.answers[answerIndex].localizedLabel === null) {
                    freetextResponse = encounter.getResponse(question.answers[answerIndex].id);
                    if (freetextResponse !== null) {
                        freetextResponse.enabled = false;
                    }
                    break;
                }
            }
        }

        // Handle the condition if the the answer was given manually
        if (manual === true) {
            this.handleConditions(question, answer, null, isSelected);
        }

        return;
    }

    // If the clicked button is not selected yet:
    // RADIO BUTTONS
    // If only one answer can be selected at a time
    if (question.maxNumberAnswers === 1) {
        // De-select the previously clicked answer
        if (encounter.getCountOfSelectAnswerResponses(question) >= 1) {
            // Get the selected radio button via jQuery
            var previousClickedRadio = $('.radio-on');
            // Deselect it
            previousClickedRadio.removeClass(checkedClass);
            previousClickedRadio.addClass(uncheckedClass);

            $("#input" + answerId).prop("checked", true);
            // Check which was the old response and not a freetext answer
            var oldResponse;
            var allResponses = encounter.getResponsesForQuestion(question);
            for (responseIndex in allResponses) {
                if (allResponses[responseIndex].customtext === undefined) {
                    oldResponse = allResponses[responseIndex];
                }
            }
            var oldAnswer = question.getAnswerById(oldResponse.answerId);

            // Remove the response
            encounter.removeResponse(oldResponse);

            // If the old answer is marked as other disable the textarea and 
            // the response of the freetext answer
            if (oldAnswer.isOther === true) {
                $('#textarea').attr('disabled', 'disabled');
                $('#textarea').addClass('state-disabled');

                for (answerIndex in question.answers) {
                    if (question.answers[answerIndex].localizedLabel === null) {
                        freetextResponse = encounter.getResponse(question.answers[answerIndex].id);
                        if (freetextResponse !== null) {
                            freetextResponse.enabled = false;
                        }
                        break;
                    }
                }
            }

            // Handle the conditions for the old answer. The selected flag is false in this case
            if (manual === true) {
                this.handleConditions(question, oldAnswer, null, false);
            }
        }

        // If the answer is marked as other enable the textarea and the response
        if (answer.isOther === true) {
            $('#textarea').removeAttr('disabled');
            $('#textarea').removeClass('state-disabled');

            for (answerIndex in question.answers) {
                if (question.answers[answerIndex].localizedLabel === null) {
                    freetextResponse = encounter.getResponse(question.answers[answerIndex].id);
                    if (freetextResponse !== null) {
                        freetextResponse.enabled = true;
                    }
                    break;
                }
            }
        }

        // Highlight the newly selected answer
        $("#" + answerId).removeClass(uncheckedClass);
        $("#" + answerId).addClass(checkedClass);

        response.answerId = answerId;
        response.value = answerValue;
        encounter.mergeResponse(response);
        // CHECKBOXES
    } else {
        // If the maximum number of answers is not reached, or if this answer has already been selected before
        if (encounter.getCountOfSelectAnswerResponses(question) < question.maxNumberAnswers || response.answerId != null) {
            $("#" + answerId).addClass(checkedClass);
            $("#" + answerId).removeClass(uncheckedClass);
            // Checkboxes must be checked seperat
            $("#input" + answerId).prop('checked', true);

            // If the answer is marked as other enable the textarea and the 
            // response
            if (answer.isOther === true) {
                $('#textarea').removeAttr('disabled');
                $('#textarea').removeClass('state-disabled');

                for (answerIndex in question.answers) {
                    if (question.answers[answerIndex].localizedLabel === null) {
                        freetextResponse = encounter.getResponse(question.answers[answerIndex].id);
                        if (freetextResponse !== null) {
                            freetextResponse.enabled = true;
                        }
                        break;
                    }
                }
            }

            response.answerId = answerId;
            response.value = answerValue;
            encounter.mergeResponse(response);
        } else {
            // Checkboxes must be checked seperately
            $("#input" + answerId).prop('checked', false);
            isSelected = false;
        }
    }
    // Handle the condition if the the answer was given manually
    if (manual === true) {
        this.handleConditions(question, answer, null, isSelected);
    }
};

/**
 * Updates the date with the provided date and updates the encounter.
 */
Selector.setDate = function (question, newDate, manual) {
    var response = encounter.getResponse(question.answers[0].id);
    var minDate = question.answers[0].startDate;
    var maxDate = question.answers[0].endDate;
    if (response === null) {
        response = new Response();
    }

    response.answerId = question.answers[0].id;
    var displayDate = "";
    var responseDate = "";

    if (newDate === "") {
        encounter.removeResponse(response);
        return;
    } else {
        // If the browser has a native datepicker
        if (Modernizr.inputtypes.date === true) {
            // Convert incoming date to a unique date format
            var date = new Date(newDate);
            displayDate = date.getFullYear() + '-' + ('0' + (date.getMonth() + 1)).slice(-2) + '-' + ('0' + date.getDate()).slice(-2);
            // If the date was manually set
        } else if (manual === true) {
            // If the language is german
            if (encounter.bundleLanguage === "de_DE") {
                // Convert from german date standard
                var date = $.datepicker.parseDate("dd.mm.yy", newDate);
                displayDate = ('0' + date.getDate()).slice(-2) + '.' + ('0' + (date.getMonth() + 1)).slice(-2) + '.' + date.getFullYear();
            } else {
                // Convert from ISO date standard
                var date = $.datepicker.parseDate("yy-mm-dd", newDate);
                displayDate = date.getFullYear() + '-' + ('0' + (date.getMonth() + 1)).slice(-2) + '-' + ('0' + date.getDate()).slice(-2);
            }
            // Take the timezone offset into account and set the right date
            var offset = date.getTimezoneOffset() * 60000;
            date = new Date(date.getTime() - offset);
            // If the date was not set manually
        } else {
            var date = $.datepicker.parseDate("yy-mm-dd", newDate);
            if (encounter.bundleLanguage === "de_DE") {
                displayDate = ('0' + date.getDate()).slice(-2) + '.' + ('0' + (date.getMonth() + 1)).slice(-2) + '.' + date.getFullYear();
            } else {
                displayDate = date.getFullYear() + '-' + ('0' + (date.getMonth() + 1)).slice(-2) + '-' + ('0' + date.getDate()).slice(-2);
            }
            // Take the timezone offset into account and set the right date
            var offset = date.getTimezoneOffset() * 60000;
            date = new Date(date.getTime() - offset);
        }
        responseDate = date.getFullYear() + '-' + ('0' + (date.getMonth() + 1)).slice(-2) + '-' + ('0' + date.getDate()).slice(-2);
    }

    if (minDate !== null && responseDate < minDate) {
        responseDate = "";
        displayDate = "";
    } else if (maxDate !== null && responseDate > maxDate) {
        responseDate = "";
        displayDate = "";
    }

    response.date = responseDate;
    encounter.mergeResponse(response);
    // set the newDate in the dateInput
    $("#dateInput").val(displayDate);
};

Selector.setImage = function (question, value) {
    // Get the response of the current encounter
    var response = encounter.getResponse(question.answers[0].id);
    // If the response is null, create a new one and initialize the pointsOnImage Array
    if (response === null) {
        response = new Response();
        response.pointsOnImage = new Array();
    }

    // Check if a button was pressed or the image was clicked and activate/deactivate the buttons
    if (value === "undo") {
        response.currentPointsPosition--;
        $("#redoButton").removeAttr("disabled");
        if (response.currentPointsPosition === -1) {
            $("#undoButton").attr("disabled", "true");
        }
    } else if (value === "redo") {
        response.currentPointsPosition++;
        $("#undoButton").removeAttr("disabled");
        if (response.currentPointsPosition === response.pointsOnImage.length - 1) {
            $("#redoButton").attr("disabled", "true");
        }
    } else {
        // If the image was clicked remove all points that are not shown
        while (response.currentPointsPosition < response.pointsOnImage.length - 1) {
            response.pointsOnImage.pop();
        }
        // Create a new PointOnImage and set its values
        var pointOnImage = new PointOnImage();
        pointOnImage.position = response.pointsOnImage.length;
        pointOnImage.xCoordinate = value[0] / $('#canvas').width();
        pointOnImage.yCoordinate = value[1] / $('#canvas').height();
        // Check, which color was chosen
        pointOnImage.color = $("input[type='radio']:checked").val();

        // Add the new point to the array
        response.answerId = question.answers[0].id;
        response.pointsOnImage.push(pointOnImage);
        response.currentPointsPosition++;
        $("#redoButton").attr("disabled", "true");
        $("#undoButton").removeAttr("disabled");
    }
    encounter.mergeResponse(response);
};

Selector.selectBodyPart = function (question, answer, manual) {
    var response = encounter.getResponse(answer.id);

    if (response === null) {
        response = new Response();
    }

    var selectedPath = document.getElementById('answer' + answer.id);

    //deselect path if it was selected before
    if (selectedPath.getAttribute("class") === "shape-selected") {
        selectedPath.setAttribute("class", "shape");
        encounter.removeResponse(response);
        return;
    }

    //select path if it was deselected before
    if (selectedPath.getAttribute('class') === "shape") {
        //Deselect current selected answer if max number of selectable answers is 1
        if (question.maxNumberAnswers === 1) {
            $(".shape-selected").each(function () {
                $(this).click();
            });
        }

        if ($(".shape-selected").length < question.maxNumberAnswers) {
            selectedPath.setAttribute("class", "shape-selected");
            //add answer to response and merge it to encounter
            response.answerId = answer.id;
            encounter.mergeResponse(response);
        } else {
            //do something else? like showing an alert to advice that maximum number of answer is reachead?
        }
    }
};

Selector.handleConditions = function (question, answer, value, isSelected) {
    // Loop through all conditions of this answer
    for (var i = 0; i < answer.conditions.length; i++) {
        // Get the current condition
        var condition = answer.conditions[i];
        // Switch over the targetClass
        switch (condition.targetClass) {
            case "de.imi.mopat.model.Question":
                // Get the real question object, not the target object from the condition
                var targetQuestion = encounter.getCurrentQuestionnaire().getQuestionById(condition.targetId);

                // Switch over the possible actions of this condition
                if (condition.thresholdType !== null && condition.thresholdType !== "") {
                    var enableThreshold = this.enableThresholdCondition(condition, isSelected, value);
                    if (enableThreshold === true) {
                        targetQuestion.isEnabled++;
                    } else if (enableThreshold === false) {
                        targetQuestion.isEnabled--;
                    }
                } else {
                    switch (condition.action) {
                        case "ENABLE":
                            // If the answer is selected, increase isEnabled of the target question
                            if (isSelected === true) {
                                targetQuestion.isEnabled++;
                            } else if (isSelected === false) {
                                // If the answer is deselected, decrease isEnabled of the target question
                                targetQuestion.isEnabled--;
                            }
                            break;
                        case "DISABLE":
                            // If the answer is selected, decrease isEnabled of the target question
                            if (isSelected === true) {
                                targetQuestion.isEnabled--;
                            } else if (isSelected === false) {
                                // If the answer is deselected, increase isEnabled of the target question
                                targetQuestion.isEnabled++;
                            }
                            break;
                    }
                }
                // Check if the targetQuestion is enabled
                if (targetQuestion.isEnabled > 0) {
                    // Handle all responses of the target question
                    this.setResponseActivationStateQuestion(targetQuestion, true);
                } else {
                    // Handle all responses of the target question
                    this.setResponseActivationStateQuestion(targetQuestion, false);
                }
                break;
            case "de.imi.mopat.model.Questionnaire":
                // Check if current bundle is equal to the bundle the condition belongs to
                var targetBundleQuestionnaire;
                if (condition.bundleId === encounter.bundle.id) {
                    // Search for the target questionnaire to do the action
                    for (var j = 0; j < encounter.bundle.bundleQuestionnaires.length; j++) {
                        if (condition.targetId === encounter.bundle.bundleQuestionnaires[j].questionnaire.id) {
                            targetBundleQuestionnaire = encounter.bundle.bundleQuestionnaires[j];
                        }
                    }

                    if (condition.thresholdType !== null && condition.thresholdType !== "") {
                        var enableThreshold = this.enableThresholdCondition(condition, isSelected, value);
                        if (enableThreshold === true) {
                            targetBundleQuestionnaire.isEnabled++;
                        } else if (enableThreshold === false) {
                            targetBundleQuestionnaire.isEnabled--;
                        }
                    } else {
                        switch (condition.action) {
                            case "ENABLE":
                                // If the answer is selected, increase isEnabled of the target question
                                if (isSelected === true) {
                                    targetBundleQuestionnaire.isEnabled++;
                                } else if (isSelected === false) {
                                    // If the answer is deselected, decrease isEnabled of the target question
                                    targetBundleQuestionnaire.isEnabled--;
                                }
                                break;
                            case "DISABLE":
                                // If the answer is selected, decrease isEnabled of the target question
                                if (isSelected === true) {
                                    targetBundleQuestionnaire.isEnabled--;
                                    // If the answer is deselected, increase isEnabled of the target question
                                } else if (isSelected === false) {
                                    targetBundleQuestionnaire.isEnabled++;
                                }
                                break;
                        }
                    }
                    encounter.setQuestionnaireActivation(targetBundleQuestionnaire, (targetBundleQuestionnaire.isEnabled > 0));
                }
                break;
            case "de.imi.mopat.model.SelectAnswer":
                // Get the question the targetAnswer belongs to and get the targetAnswer itself
                var targetAnswerQuestion = encounter.getCurrentQuestionnaire().getQuestionById(condition.targetAnswerQuestionId);
                var targetAnswer = targetAnswerQuestion.getAnswerById(condition.targetId);
                if (condition.thresholdType !== null && condition.thresholdType !== "") {
                    var enableThreshold = this.enableThresholdCondition(condition, isSelected, value);
                    if (enableThreshold === true) {
                        targetAnswer.isEnabled++;
                    } else if (enableThreshold === false) {
                        targetAnswer.isEnabled--;
                    }
                } else {
                    switch (condition.action) {
                        case "ENABLE":
                            // If the answer is selected, increase isEnabled of the target question
                            if (isSelected === true) {
                                targetAnswer.isEnabled++;
                            } else if (isSelected === false) {
                                // If the answer is deselected, decrease isEnabled of the target question
                                targetAnswer.isEnabled--;
                            }
                            break;
                        case "DISABLE":
                            // If the answer is selected, decrease isEnabled of the target question
                            if (isSelected === true) {
                                targetAnswer.isEnabled--;
                            } else if (isSelected === false) {
                                // If the answer is deselected, increase isEnabled of the target question
                                targetAnswer.isEnabled++;
                            }
                            break;
                    }
                }

                // Check if the target answer was marked as other and set the
                //  same isEnabled to the corresponding freetext answer
                if (targetAnswer.isOther === true) {
                    for (answerIndex in targetAnswerQuestion.answers) {
                        if (targetAnswerQuestion.answers[answerIndex].localizedLabel === null) {
                            targetAnswerQuestion.answers[answerIndex].isEnabled = targetAnswer.isEnabled;
                            // Check if the answer is enabled
                            if (targetAnswerQuestion.answers[answerIndex].isEnabled > 0) {
                                // Handle all responses of the target answer
                                this.setResponseActivationStateAnswer(targetAnswerQuestion.answers[answerIndex], true);
                            } else {
                                // Handle all responses of the target answer
                                this.setResponseActivationStateAnswer(targetAnswerQuestion.answers[answerIndex], false);
                            }
                            break;
                        }
                    }
                }

                // Check if the targetAnswer is enabled
                if (targetAnswer.isEnabled > 0) {
                    // Handle all responses of the target answer
                    this.setResponseActivationStateAnswer(targetAnswer, true);
                } else {
                    // Handle all responses of the target answer
                    this.setResponseActivationStateAnswer(targetAnswer, false);
                }

                break;
            default:
        }
    }
    // if this was the last question of the current questionnaire, otherwise the 
    // next state value remains NEXT_QUESTION
    if (!encounter.getCurrentQuestionnaire().hasNextQuestion(completionMode)) {
        nextState = States.COMPLETENESS_CHECK;
    } else {
        nextState = States.NEXT_QUESTION;
    }
    // Execute the showQuestion method from survey.js to update the navigation 
    updateButtonsAfterShowQuestion(question);
};

Selector.setResponseActivationStateQuestion = function (question, isEnabled) {
    for (var i = 0; i < question.answers.length; i++) {
        // Search for a existing response with the given answerId
        var response = encounter.getResponse(question.answers[i].id);
        if (response !== null) {
            // If isEnabled is false disable the response of the answer
            if (isEnabled === false) {
                response.enabled = false;
            } else {
                // If isEnabled is true, check if the isEnabled value of the 
                // answer is also true
                if (question.answers[i].isEnabled > 0) {
                    response.enabled = true;
                }
            }

        }
    }
};

Selector.setResponseActivationStateAnswer = function (answer, isEnabled) {
    // Search for a existing response with the given answerId
    var response = encounter.getResponse(answer.id);
    if (response !== null) {
        response.enabled = isEnabled;

    }
};

Selector.enableThresholdCondition = function (condition, isSelected, value) {
    var enable;
    switch (condition.action) {
        case "ENABLE":
            enable = false;
            switch (condition.thresholdType) {
                case "<":
                    if (parseFloat(value) < parseFloat(condition.thresholdValue)) {
                        // if the answer is selected, set the target question to enable = true
                        if (isSelected === true) {
                            // Set the enabled flag of the target question
                            enable = true;
                        } else if (isSelected === false) { // If it was deselected
                            // Set the enabled flag of the target question
                            enable = false;
                        }
                    }
                    break;
                case "<=":
                    if (parseFloat(value) <= parseFloat(condition.thresholdValue)) {
                        // if the answer is selected, set the target question to enable = true
                        if (isSelected === true) {
                            // Set the enabled flag of the target question
                            enable = true;
                        } else if (isSelected === false) { // If it was deselected
                            // Set the enabled flag of the target question
                            enable = false;
                        }
                    }
                    break;
                case "=":
                    if (parseFloat(value) === parseFloat(condition.thresholdValue)) {
                        // if the answer is selected, set the target question to enable = true
                        if (isSelected === true) {
                            // Set the enabled flag of the target question
                            enable = true;
                        } else if (isSelected === false) { // If it was deselected
                            // Set the enabled flag of the target question
                            enable = false;
                        }
                    }
                    break;
                case ">":
                    if (parseFloat(value) > parseFloat(condition.thresholdValue)) {
                        // if the answer is selected, set the target question to enable = true
                        if (isSelected === true) {
                            // Set the enabled flag of the target question
                            enable = true;
                        } else if (isSelected === false) { // If it was deselected
                            // Set the enabled flag of the target question
                            enable = false;
                        }
                    }
                    break;
                case ">=":
                    if (parseFloat(value) >= parseFloat(condition.thresholdValue)) {
                        // if the answer is selected, set the target question to enable = true
                        if (isSelected === true) {
                            // Set the enabled flag of the target question
                            enable = true;
                        } else if (isSelected === false) { // If it was deselected
                            // Set the enabled flag of the target question
                            enable = false;
                        }
                    }
                    break;
                case "!=":
                    if (parseFloat(value) !== parseFloat(condition.thresholdValue)) {
                        // if the answer is selected, set the target question to enable = true
                        if (isSelected === true) {
                            // Set the enabled flag of the target question
                            enable = true;
                        } else if (isSelected === false) { // If it was deselected
                            // Set the enabled flag of the target question
                            enable = false;
                        }
                    }
                    break;
            }
            break;
        case "DISABLE":
            enable = true;
            switch (condition.thresholdType) {
                case "<":
                    if (parseFloat(value) < parseFloat(condition.thresholdValue)) {
                        // if the answer is selected, set the target question to enable = true
                        if (isSelected === true) {
                            // Set the enabled flag of the target question
                            enable = false;
                        } else if (isSelected === false) { // If it was deselected
                            // Set the enabled flag of the target question
                            enable = true;
                        }
                    }
                    break;
                case "<=":
                    if (parseFloat(value) <= parseFloat(condition.thresholdValue)) {
                        // if the answer is selected, set the target question to enable = true
                        if (isSelected === true) {
                            // Set the enabled flag of the target question
                            enable = false;
                        } else if (isSelected === false) { // If it was deselected
                            // Set the enabled flag of the target question
                            enable = true;
                        }
                    }
                    break;
                case "=":
                    if (parseFloat(value) === parseFloat(condition.thresholdValue)) {
                        // if the answer is selected, set the target question to enable = true
                        if (isSelected === true) {
                            // Set the enabled flag of the target question
                            enable = false;
                        } else if (isSelected === false) { // If it was deselected
                            // Set the enabled flag of the target question
                            enable = true;
                        }
                    }
                    break;
                case ">":
                    if (parseFloat(value) > parseFloat(condition.thresholdValue)) {
                        // if the answer is selected, set the target question to enable = true
                        if (isSelected === true) {
                            // Set the enabled flag of the target question
                            enable = false;
                        } else if (isSelected === false) { // If it was deselected
                            // Set the enabled flag of the target question
                            enable = true;
                        }
                    }
                    break;
                case ">=":
                    if (parseFloat(value) >= parseFloat(condition.thresholdValue)) {
                        // if the answer is selected, set the target question to enable = true
                        if (isSelected === true) {
                            // Set the enabled flag of the target question
                            enable = false;
                        } else if (isSelected === false) { // If it was deselected
                            // Set the enabled flag of the target question
                            enable = true;
                        }
                    }
                    break;
                case "!=":
                    if (parseFloat(value) !== parseFloat(condition.thresholdValue)) {
                        // if the answer is selected, set the target question to enable = true
                        if (isSelected === true) {
                            // Set the enabled flag of the target question
                            enable = false;
                        } else if (isSelected === false) { // If it was deselected
                            // Set the enabled flag of the target question
                            enable = true;
                        }
                    }
                    break;
            }
    }
    if (condition.action === "ENABLE") {
        if (condition.wasTriggered === true) {
            if (enable === true) {
                enable = "already triggered";
            } else {
                condition.wasTriggered = false;
            }
        } else {
            if (enable === true) {
                condition.wasTriggered = true;
            } else {
                enable = "already triggered";
            }
        }
    } else {
        if (condition.wasTriggered === true) {
            if (enable === true) {
                condition.wasTriggered = false;
            } else {
                enable = "already triggered";
            }
        } else {
            if (enable === true) {
                enable = "already triggered";
            } else {
                condition.wasTriggered = true;
            }
        }
    }
    return enable;
};