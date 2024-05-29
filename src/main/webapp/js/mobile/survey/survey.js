var Questiontypes = {
    MULTIPLE_CHOICE: "MULTIPLE_CHOICE",
    SLIDER: "SLIDER",
    NUMBER_CHECKBOX: "NUMBER_CHECKBOX",
    NUMBER_CHECKBOX_TEXT: "NUMBER_CHECKBOX_TEXT",
    DROP_DOWN: "DROP_DOWN",
    FREE_TEXT: "FREE_TEXT",
    INFO_TEXT: "INFO_TEXT",
    NUMBER_INPUT: "NUMBER_INPUT",
    DATE: "DATE",
    IMAGE: "IMAGE",
    BARCODE: "BARCODE"
};

var States = {
    PREVIOUS_QUESTION: "PREVIOUS_QUESTION",
    NEXT_QUESTION: "NEXT_QUESTION",
    QUESTIONNAIRE_FINAL: "QUESTIONNAIRE_FINAL",
    BUNDLE_FINAL: "BUNDLE_FINAL",
    QUESTIONNAIRE_WELCOME: "QUESTIONNAIRE_WELCOME",
    COMPLETENESS_CHECK: "COMPLETENESS_CHECK",
    LOGOUT: "LOGOUT"
};
var orientationVertical = true;
var currentState;
var nextState;
var positionOfCurrentQuestion = 0;
var finishedQuestionnaireCount = 0;

var completionMode = false;

// Stores the current fontsize class for all elements
// follows structure: "resizable-0" - "resizable-5"
var fontSizeClass = 0; 
var fontSizeMaxClass = 5; 
var fontSizeMinClass = 0;

// The debounce time in ms till the next send request
var debounceTime = 5000;
var debounceTimeCheck = Date.now();

// Flag to keep track if user has clicked close title once for a question
var wasScrollTitleManuallyClosed = false;

//Save options globally to handle question switching
var questionOptions = {
    "nextState": null,
    "switchQuestion": false
}; 

function setSwitchQuestion(bool) {
    this.questionOptions.switchQuestion = bool; 
}

/**
 * Event Listener for scroll event which does the following:
 * - adds shadow to the navigation menu when scrolled down 
 * - If scrolled up again and the title is less than half in the viewport: Show scrolling title
 * - If scrolled down again: hide scrolling title
 * - Hides Shadows and scrolling title if completely at the top again 
 */
// Initial state
var scrollPos = 0;
var timeout = 0; 
$(window).scroll(function() {  
    clearTimeout(timeout);

    //Timeout to prevent flickering
    var scrollDirIsUp = scrollDirectionIsUp(scrollPos); 
    scrollPos = window.scrollY;

    // If view is scrolled down either a drop shadow has to be added or the scrolling question title should be shown
    if (scrollPos > 0) {
        //Show scrolling questionTitle div if scroll direction is currently up, user has not clicked the close button and the title is not overflowing
        if (scrollDirIsUp && !wasScrollTitleManuallyClosed && !isScrollTitleOverflow()) {
            //Set Timeout to only show title after a certain time
            timeout = setTimeout(function () {
                // Double check position to prevent title showing if scrolled to the top in the meantime
                if (scrollPos > 0 && !isTitleHalfVisible()) {
                    $(".stickyHeader").removeClass("active");
                    $("#scrollQuestionTitle").addClass("active");
                    $("#scrollQuestionTitle").removeClass("no-height");
                }
            }, 250);
        } else { //Show navigation drop shadow otherwise
            hideScrollTitle(true); 
            $(".stickyHeader").addClass("active");
        }
    } else { //Deactivate both if view is scrolled to the top
        $(".stickyHeader").removeClass("active"); 
        hideScrollTitle(false);
        wasScrollTitleManuallyClosed = false;
    }
});

/**
 * Checks if the view is scrolled up by comparing the current scroll distance with a previous one
 * @param {*} scrollPosBefore The scroll distance from a previous step 
 * @returns Boolean
 */
function scrollDirectionIsUp(scrollPosBefore) {
    if ($(window).scrollTop() < scrollPosBefore) {
		return true;
    } else {
		return false;
    }
}

/**
 * Checks if the #questionTitle div is currently at least half in the viewport
 * The function compensates for the header bar height
 * It returns true if the middle point of the #questionTitle is biggert than the header height 
 * @returns Boolean
 */
function isTitleHalfVisible() {
    var titleRect = document.querySelector("#questionTitle").getBoundingClientRect();
    var titleMiddle = titleRect.top + titleRect.height / 2;
    var headerHeight = (document.querySelector(".header").getBoundingClientRect().height);

    return titleMiddle >= headerHeight 

}

/**
 * Checks if the #ScrollQuestionTitle div is not overflowing the viewport
 * It returns true if the middle point of the #ScrollQuestionTitle is bigger than the screen height
 * @returns Boolean
 */
function isScrollTitleOverflow(){
    var titleRect = document.getElementById("scrollQuestionTitle");
    titleRect.classList.remove("no-height");
    titleRect.classList.add('auto-height');

    var titleHeight = titleRect.getBoundingClientRect().height;
    titleRect.classList.remove('auto-height');
    titleRect.classList.add("no-height");

    var headerHeight = (document.querySelector(".header").getBoundingClientRect().height);

    return Number(titleHeight + headerHeight) > window.innerHeight;
}

/**
 * Hides the scrollQuestionTitle div
 * Checks if sticky header should be active
 * @param(*) showScrollAnimation: boolean - Whether the div should scroll out or just fade (e.g. if viewport is at the top)
 */
function hideScrollTitle(showScrollAnimation) {
    scrollPos = window.scrollY; 
    if (showScrollAnimation) {
        $("#scrollQuestionTitle").removeClass("active");
    } else {
        $("#scrollQuestionTitle").addClass("transition"); 
        setTimeout(function() {
            $("#scrollQuestionTitle").removeClass("active");
            $("#scrollQuestionTitle").removeClass("transition");
        },1000)
    }

    if (scrollPos > 0) {
        $(".stickyHeader").addClass("active"); 
    }
}

/**
 * This method displays the welcome page of the bundle
 */
function showBundleWelcome() {
    // Create the question content
    var contentString = '';
    // does welcomeText exists and is not empty or filled with spaces
    if (encounter.bundle.localizedWelcomeText[encounter.bundleLanguage] && $.trim(encounter.bundle.localizedWelcomeText[encounter.bundleLanguage].replace(/&nbsp;/g, "").replace(/<br>/g, "").replace(/ /g, "").replace(/<p><\/p>/g, "") !== "")) {
        contentString += encounter.bundle.localizedWelcomeText[encounter.bundleLanguage];
    } else {
        contentString += strings['survey.bundle.questionnaires'] + ':<br>';
        // List all questionnaires of the bundle
        contentString += "<ul>";
        $.each(encounter.bundle.bundleQuestionnaires, function (index, value) {
            contentString += "<li>" + value.questionnaire.localizedDisplayName[encounter.bundleLanguage];
            // Show "optional" if the questionnaire is target of a condition or is initial deactivated
            if (value.questionnaire.hasConditionsAsTarget === true || value.isEnabled <= 0) {
                contentString += " (" + strings['survey.questionnaire.label.optional'] + ")";
            }
            contentString += "</li>";
        });
        contentString += "</ul>";
    }
    // Set the question content
    $("#questionContent").append($("<span/>", {"html": contentString, "class": "resizable"}));

    var buttonTextNext = strings['survey.questionnaire.button.startSurvey'];

    Navigation.updateNavigation(encounter.bundle.name, null, null, buttonTextNext, orientationVertical, false);

    // Set the font size
    setFontSize(fontSizeClass);
}

/**
 * This method displays the final page of the bundle
 */
function showBundleFinal() {
    // Show the bundle final text only if it exists and is not empty
    if (encounter.bundle.localizedFinalText[encounter.bundleLanguage] && encounter.bundle.localizedFinalText[encounter.bundleLanguage].replace(/&nbsp;/g, "").replace(/<br>/g, "").replace(/ /g, "").replace(/<p><\/p>/g, "") !== "") {
        $("#questionContent").append($("<span/>", {"html": encounter.bundle.localizedFinalText[encounter.bundleLanguage] + "<br><br>", "class": "resizable"}));
    }
    // First line prompts to return the device and the user 
    if (encounter.isAtHome === false) {
        $("#questionContent").append($("<span/>", {"html": "<b>" + strings['survey.questionnaire.label.returnDevice'] + "</b>" + "<br><br>", "class": "resizable"}));
    } else {
        $("#questionContent").append($("<span/>", {"html": "<b>" + strings['survey.questionnaire.label.endHomeDevice'] + "</b>" + "<br><br>", "class": "resizable"}));
    }
    $("#questionContent").append($("<span/>", {"html": strings['survey.questionnaire.label.answeredQuestionsDescription'] + "<br><br>", "id": "bundleFinalInfo", "class": "resizable"}));

    // Loop through all questionnaires
    for (var i = 0; i < encounter.bundle.bundleQuestionnaires.length; i++) {
        // Get current questionnaire
        var currentQuestionnaire = encounter.bundle.bundleQuestionnaires[i].questionnaire;

        // Compose string to display the amount of given responses
        if (encounter.bundle.bundleQuestionnaires[i].isEnabled > 0) {

            // And calculate needed information to display the amount of given responses
            var requiredQuestions = currentQuestionnaire.getRequiredQuestionsCount();
            var requiredQuestionsComplete = requiredQuestions - getIncompleteQuestionsCountFromQuestionnaire(currentQuestionnaire);
            var notRequiredQuestions = currentQuestionnaire.getAnswerableQuestionsCount() - requiredQuestions;
            var notRequiredQuestionsComplete = notRequiredQuestions - getIncompleteNotRequiredQuestionsCountFromQuestionnaire(currentQuestionnaire);

            var labelAnsweredQuestions = strings['survey.questionnaire.label.answeredQuestions'];

            labelAnsweredQuestions = labelAnsweredQuestions.replace('{nameQuestionnaire}', currentQuestionnaire.localizedDisplayName[encounter.bundleLanguage]);
            labelAnsweredQuestions = labelAnsweredQuestions.replace('{requiredQuestionsComplete}', requiredQuestionsComplete);
            labelAnsweredQuestions = labelAnsweredQuestions.replace('{requiredQuestions}', requiredQuestions);
            labelAnsweredQuestions = labelAnsweredQuestions.replace('{notRequiredQuestionsComplete}', notRequiredQuestionsComplete);
            labelAnsweredQuestions = labelAnsweredQuestions.replace('{notRequiredQuestions}', notRequiredQuestions);

            $("#questionContent").append($("<span/>", {"html": labelAnsweredQuestions + "<br><br>", "class": "resizable"}));
        } else {
            var labelQuestionnaireNotActiveDuringSurvey = strings['survey.questionnaire.label.questionnaireNotActiveDuringSurvey'];
            labelQuestionnaireNotActiveDuringSurvey = labelQuestionnaireNotActiveDuringSurvey.replace('{nameQuestionnaire}', currentQuestionnaire.localizedDisplayName[encounter.bundleLanguage]);

            $("#questionContent").append($("<span/>", {"html": labelQuestionnaireNotActiveDuringSurvey + "<br><br>", "class": "resizable"}));
        }
    }

    //Get questionnaire progress
    var progress = encounter.getCurrentQuestionnaire().getProgress();

    // Show activated scores
    if (encounter.isTest === false && encounter.bundle.hasActiveScores() === true) {
        // Post the encounter to be sure that the given responses are up to date
        postEncounterForScore(encounter);
        // Get the evaluated scores
        var activatedScores;
        $.ajax({
            url: "scores",
            type: "POST",
            async: false,
            data: "encounterUuid=" + encounter.uuid,
            success: function (data) {
                // Check if entries function is not supported
                if (!Object.entries) {
                    activatedScores = new Map();
                    var ownProps = Object.keys(data);
                    for (var i = 0; i < ownProps.length; i++) {
                        activatedScores.set(ownProps[i], data[ownProps[i]]);
                    }
                } else {
                    activatedScores = new Map(Object.entries(data));
                }
            }
        });

        jQuery('<div/>', {
            "id": 'scoreText',
            "style": 'float: left;'
        }).appendTo('#questionContent');

        jQuery('<div/>', {
            "id": 'scoreDatamatrix',
            "style": 'float: left; margin-left: 6em; margin-top: 2em;'
        }).appendTo('#questionContent');

        $('#scoreText').append($("<span/>", {"html": "<b>Scores:</b><br>", "class": "resizable"}));

        // Iterate over all given scores and print them
        var activatedScoresAsString = "";
        activatedScores.forEach(function (value, key) {
            if (value === null) {
                $("#scoreText").append($("<span/>", {"html": key + ": " + "<br>", "class": "resizable"}));
                activatedScoresAsString += key + ":\n";
            } else {
                $("#scoreText").append($("<span/>", {"html": key + ": " + value + "<br>", "class": "resizable", "style": "padding-left:2em;"}));
                activatedScoresAsString += key + ": " + value + "\n";
            }
        });

        var barcodeSettings = {
            barWidth: 1,
            barHeight: 50,
            moduleSize: 5,
            showHRI: false,
            addQuietZone: true,
            marginHRI: 0,
            bgColor: "#FFFFFF",
            color: "#000000",
            fontSize: 10,
            output: "bmp",
            posX: 0,
            posY: 0
        };

        // Show the printed scores as QR-code
        $("#scoreDatamatrix").barcode(activatedScoresAsString, "datamatrix", barcodeSettings);
    }

    var title = strings['survey.questionnaire.label.finishedSurvey'];
    var buttonTextNext = strings['survey.questionnaire.button.closeApplication'];
    $('#buttonNext').addClass('ui-icon-check');

    if(progress){
        Navigation.updateNavigation(title, [progress[1],progress[1]], null, buttonTextNext, orientationVertical, false);
    } else {
        Navigation.updateNavigation(title, [], null, buttonTextNext, orientationVertical, false);
    }
    // Set the font size
    setFontSize(fontSizeClass);
}

/**
 * This method displays the welcome page of the given questionnaire
 */
function showQuestionnaireWelcome(questionnaire) {

    if (questionnaire.logo !== null) {
        var logo = $("<img />", {
            "src": questionnaire.logoBase64,
            "style": "width:250px; float:right; margin-left: 5px; margin-bottom: 5px;"
        });

        if (isMobile.matches === true) {
            logo.css("width", "100px");
        }

        $("#questionContent").append(logo);
    }

    var styleWelcomeText = $("<p />", {
        "style": "text-align: justify;",
        "class": "resizable",
        "html": questionnaire.localizedWelcomeText[encounter.bundleLanguage]
    });
    $("#questionContent").append(styleWelcomeText);

    var title = questionnaire.localizedDisplayName[encounter.bundleLanguage];

    var buttonTextNext = strings['survey.questionnaire.button.startQuestionnaire'];

    Navigation.updateNavigation(title, null, null, buttonTextNext, orientationVertical, false);

    // Set the font size
    setFontSize(fontSizeClass);
}

/**
 * This method displays the final page of the given questionnaire
 */
function showQuestionnaireFinal(questionnaire, hasActiveFollower) {
    $("#questionContent").append($("<span/>", {"html": questionnaire.localizedFinalText[encounter.bundleLanguage], "class": "resizable"}));

    var title = questionnaire.localizedDisplayName[encounter.bundleLanguage];
    var buttonTextPrevious = ""
    var buttonTextNext = "";

    if (encounter.hasNextQuestionnaire() && hasActiveFollower === true) {
        //TODO: After questionnaire is completed it is not possible to go back at the moment. 
        // This has to be reimplemented before showing the button again 
        //buttonTextPrevious = strings['survey.questionnaire.button.returnToQuestionnaire'];  
        buttonTextNext = strings['survey.questionnaire.button.nextQuestionnaire'];
    } else {
        buttonTextNext = strings['survey.questionnaire.button.finishSurvey'];
    }

    Navigation.updateNavigation(title, null, buttonTextPrevious, buttonTextNext, orientationVertical, false);

    // Set the font size
    setFontSize(fontSizeClass);
}

function showCompletenessCheck(incompletedQuestions) {
    var questionString;
    var buttonTextPrevious;
    if (incompletedQuestions === 1) {
        questionString = strings['survey.questionnaire.button.completenessCheckTitlePartSingle'];
        buttonTextPrevious = strings['survey.questionnaire.button.answerQuestionsSingle'];
    } else {
        questionString = strings['survey.questionnaire.button.completenessCheckTitlePartMultiple'];
        questionString = questionString.replace('{numberQuestions}', incompletedQuestions);
        buttonTextPrevious = strings['survey.questionnaire.button.answerQuestionsMultiple'];
    }

    $("#questionContent").append($("<span/>", {"html": questionString, "class": "resizable"}));

    var title = strings['survey.questionnaire.button.completenessCheck'];
    var buttonTextNext = "";
    if (encounter.hasNextQuestionnaire()) {
        buttonTextNext = strings['survey.questionnaire.button.nextQuestionnaire'];
    } else {
        buttonTextNext = strings['survey.questionnaire.button.finishQuestionnaire'];
    }

    Navigation.updateNavigation(title, null, buttonTextPrevious, buttonTextNext, orientationVertical, false);

    // Set the font size
    setFontSize(fontSizeClass);
}

/**
 * This method displays the given question
 */
function showQuestion(question) {
    updateQuestionTitles(question); 

    question.getHTML($("#questionContent"));

    // Select previously selected elements, if there were any
    if (question.questionType !== Questiontypes.IMAGE && question.questionType !== Questiontypes.BODY_PART && encounter.getResponsesForQuestion(question).length > 0) {
        var responses = encounter.getResponsesForQuestion(question);
        $.each(responses, function () {
            if (this.enabled === true) {
                Selector.selectElement(question.id, this.answerId, this.value, this.customtext, this.date, false);
            }
        });
    }
    updateButtonsAfterShowQuestion(question);
    updateScrollTitleVisibility();
}


/**
 * This function checks if the floating question overflows the screen when hidden and add/removes zero height
 */
 function updateScrollTitleVisibility(){
    if (isScrollTitleOverflow()) {
        $("#scrollQuestionTitle").addClass("no-height");
        $("#scrollQuestionTitle").removeClass("active");
    } else {
        $("#scrollQuestionTitle").removeClass("no-height");
    }
 }


/**
 * This function updates both question title divs with the same content
 * @param {*} question  
 */
function updateQuestionTitles(question) {
    //Add title to the question title div
    $("#questionTitle").append($("<span/>", {
        "id": "questionTitleText",
        "html": question.localizedQuestionText[encounter.bundleLanguage],
    }));

    //Add padding only to the question content for the scrolling question title
    var scrollQuestionTitlePaddingDiv = $("<div/>", {
        "class": "px-5",
    });

    //Add the actual question title to the padded div
    scrollQuestionTitlePaddingDiv.append($("<span/>", {
        "html": question.localizedQuestionText[encounter.bundleLanguage],
    }));

    //Merge everything to the scrolling question title div
    $("#scrollQuestionTitle").append(scrollQuestionTitlePaddingDiv); 
    
    //Add a collapse button to the scrolling question title
    var hideMenuDiv = $("<div/>", {
        "class": "w-100 border",
    });

    var hideMenuButton = $("<button/>", {
        "class": "btn-lowEmphasis w-100",
        "onclick": "manuallyCloseScrollTitle()"
    });

    var hideMenuBi = $("<i/>", {
        "class": "bi bi-chevron-compact-up"
    });

    hideMenuButton.append(hideMenuBi); 
    hideMenuDiv.append(hideMenuButton); 
    $("#scrollQuestionTitle").append(hideMenuDiv); 
}

function manuallyCloseScrollTitle(){
    hideScrollTitle(true);
    wasScrollTitleManuallyClosed = true;
}

function updateButtonsAfterShowQuestion(question) {
    // Update the navigation
    var title = encounter.getCurrentQuestionnaire().localizedDisplayName[encounter.bundleLanguage];
    var progress = encounter.getCurrentQuestionnaire().getProgress();

    var buttonTextNext = strings['survey.questionnaire.button.nextQuestion'];

    var buttonTextPrevious = strings['survey.questionnaire.button.previousQuestion'];

    // Don't show the back button on the first question
    if (encounter.getCurrentQuestionnaire().isFirstQuestion(question, completionMode)) {
        buttonTextPrevious = "";
    }

    // If it is the last question of the questionnaire, rename the Button
    if (!encounter.getCurrentQuestionnaire().hasNextQuestion(completionMode)) {
        buttonTextNext = strings['survey.questionnaire.button.finishQuestionnaire'];
    }

    Navigation.updateNavigation(title, progress, buttonTextPrevious, buttonTextNext, orientationVertical, encounter.bundle.deactivateProgressAndNameDuringSurvey);

    // Set the font size
    setFontSize(fontSizeClass);

}

/**
 * This method clears the question content, i.e. all answers and the question text. Moreover,
 * other parameters only relevant for the previous question are reseted.
 */
function clear() {
    $("#questionContent").empty();
    $("#questionTitle").empty();
    $("#scrollQuestionTitle").empty();
}

/**
 * Clears all timeouts that are currently registered 
 */
function clearTimeouts() {
    var id = window.setTimeout(function() {}, 0);

    while (id--) {
        window.clearTimeout(id); // will do nothing if no timeout with id is present
    }
}

/**
 * This method is executed when the previous button is clicked
 */
function previous() {
    //Clear timeouts to prevent them from registering "silently"
    clearTimeouts(); 
    //Clear all resizing events
    $(window).off("resize")
    addStaticResizingEvents();
    var currentQuestion = encounter.getCurrentQuestionnaire().getCurrentQuestion();
    // If completion mode is active, active predecessor question must not be checked since the completion mode checks it anyway
    if (completionMode === true || (currentQuestion !== null && encounter.getCurrentQuestionnaire().hasQuestionActivePredecessor(currentQuestion))) {
        // If the current question is an image question remove all points on image that are greater than the position
        if (currentQuestion !== null && currentQuestion !== undefined) {
            currentQuestion.removeInvalidAnswers();
        }
        clear();
        // If the questionnaire is in the completion mode on the check site, the previous button
        // switch the state to get to the incomplete questions
        if (this.nextState == States.QUESTIONNAIRE_FINAL && completionMode == true) {
            // Set the next state to NEXT_QUESTION
            this.nextState = States.NEXT_QUESTION;
            switchState(this.nextState);
        } else {
            // The previous view will always be a question
            // IMPORTANT: Do not set the this.nextState variable!
            switchState(States.PREVIOUS_QUESTION);
        }
    }
}

/**
 * This method is executed when the next button is clicked
 */
function next() {
    //Clear timeouts to prevent them from registering "silently"
    clearTimeouts();
    //Clear all resizing events
    $(window).off("resize")
    addStaticResizingEvents();
    //reset title click state to false for next question
    wasScrollTitleManuallyClosed = false;
    var switchQuestion = false; 

    // If the current page shows a question
    if (encounter.getCurrentQuestionnaire() !== null && encounter.getCurrentQuestionnaire().getCurrentQuestion() !== null) {

        var currentQuestion = encounter.getCurrentQuestionnaire().getCurrentQuestion();
        // If the current question is required
        if (currentQuestion !== null && currentQuestion.isRequired === true) {

            // If the response is not completed
            var isResponseValid = currentQuestion.isResponseValid(encounter.getResponsesForQuestion(currentQuestion));
            // Display a confirm dialog
            if (isResponseValid === false) {
                var questionConfirmElement = document.getElementById('questionConfirmDialog');
                var questionConfirmModal = bootstrap.Modal.getOrCreateInstance(questionConfirmElement);

                questionOptions.nextState = this.nextState;  
                //$("#questionConfirmDialog").popup("option", "nextState", this.nextState);

                questionConfirmElement.addEventListener("hidden.bs.modal", function() {
                    var switchQuestion = questionOptions.switchQuestion; 
                    var nextState = questionOptions.nextState; 
                    if (switchQuestion === true) {
                        clear();
                        questionOptions.switchQuestion = false; 
                        switchState(nextState);
                    }
                    return false;
                }); 

                questionConfirmModal.show();
            } else {
                switchQuestion = true;
            }
        } else {
            switchQuestion = true;
        }
    } else {
        switchQuestion = true;
    }
    // If the current question is an image question remove all points on image that are greater than the position
    if (currentQuestion !== null && currentQuestion !== undefined) {
        currentQuestion.removeInvalidAnswers();
    }
    // If the current state is not showing a question, which is required and not completed
    if (switchQuestion === true) {
        // Clear the website if we are not in logout state
        if (this.nextState !== States.LOGOUT) {
            clear();
        }
        // Go on to the next state
        switchState(this.nextState);
    }
}

/**
 * This method changes the view presented to the user according to a state
 * @param state Available states are <ul>
 * <li>BUNDLE_FINAL <i>Display the final page of the bundle</i></li>
 * <li>QUESTIONNAIRE_WELCOME <i>Display the welcome page of the following questionnaire. If it has no welcome text, go to the next question</i></li>
 * <li>QUESTIONNAIRE_FINAL <i>Display the final page of the current questionnaire. If it has no final text, 
 * go to the next questionnaire or the bundle final page, if there is no further questionnaire</i></li>
 * <li>NEXT_QUESTION <i>Display the next consecutive question. If this is the last question of the questionnaire, the next state will be QUESTIONNAIRE_FINAL</i></li>
 * <li>PREVIOUS_QUESTION <i>Display the previous question</i></li>
 * <li>LOGOUT <i>Client-side redirect to the login page</i></li>
 * </ul>
 */
function switchState(state) {
    $(window).off('resizeEnd.styleQuestion');
    $(window).off('resize.initWidgetContainer');
    currentState = state;
    switch (state) {
        // BUNDLE_FINAL
        case States.BUNDLE_FINAL:
            // Export the last questionnaire, if it's enabled, because the bundle is finished
            if (encounter.bundle.bundleQuestionnaires[encounter.bundle.bundleQuestionnaires.length - 1].isEnabled > 0) {
                exportEncounter(encounter, encounter.bundle.bundleQuestionnaires[encounter.bundle.bundleQuestionnaires.length - 1].questionnaire.id);
            }
            showBundleFinal();
            // The bundle final page is only shown if everything else is finished, thus it only remains to log out
            this.nextState = States.LOGOUT;
            break;
            // QUESTIONNAIRE_WELCOME
        case States.QUESTIONNAIRE_WELCOME:
            // Switch to the new questionnaire
            var currentQuestionnaire = encounter.getNextQuestionnaire();
            var currentBundleQuestionnaire;
            $.each(encounter.bundle.bundleQuestionnaires, function (index, bundleQuestionnaire) {
                // Find the current questionnaire in the list of the bundle's 
                // questionnaires if it is not the first one
                if (bundleQuestionnaire.questionnaire.id === currentQuestionnaire.id) {
                    currentBundleQuestionnaire = bundleQuestionnaire;
                    if (bundleQuestionnaire.position !== 1)
                    {
                        // Set the lastSeenQuestionId to the first question of the currentQuestionnaire
                        // to prevent double exporting after resuming the encounter
                        encounter.lastSeenQuestionId = currentQuestionnaire.questions[0].id;
                        if (encounter.continuation === false && encounter.bundle.bundleQuestionnaires[bundleQuestionnaire.position - 2].isEnabled > 0) {
                            // Export the questionnaire finished before the new current questionnaire 
                            exportEncounter(encounter, encounter.bundle.bundleQuestionnaires[bundleQuestionnaire.position - 2].questionnaire.id);
                        }
                    }
                }
            });
            // After continuing an incomplete encounter the continuation flag has to be reset
            encounter.continuation = false;

            if (currentBundleQuestionnaire.isEnabled > 0) {
                // A questionnaire welcome is always followed by a question
                this.nextState = States.NEXT_QUESTION;
                // Deactivate completion mode for every new questionnaire
                completionMode = false;

                // If the current questionnaire has no welcomeText and no logo, skip it
                if ((currentQuestionnaire.localizedWelcomeText[encounter.bundleLanguage] === undefined ||
                        $.trim(currentQuestionnaire.localizedWelcomeText[encounter.bundleLanguage].replace(/&nbsp;/g, "").replace(/<br>/g, "").replace(/ /g, "").replace(/<p><\/p>/g, "")) === "")
                        && currentQuestionnaire.logo === null) {
                    next();
                } else {
                    showQuestionnaireWelcome(currentQuestionnaire);
                }
            } else {
                this.nextState = States.QUESTIONNAIRE_WELCOME;
                next();
            }
            break;
            // COMPLETENESS_CHECK
        case States.COMPLETENESS_CHECK:
            // If there are incompleted questions in the current questionnaire,
            // switch into the completionMode and reset the questionIndex of this questionnaire
            var incompletedQuestions = getIncompleteQuestionsCountFromQuestionnaire(encounter.getCurrentQuestionnaire());
            if (incompletedQuestions > 0) {
                //if completion mode is already true and still have required unanswered questions, then show the next question
                if(completionMode == true){
                    switchState(States.NEXT_QUESTION);
                } else {
                    completionMode = true;
                    // reset the question index
                    encounter.getCurrentQuestionnaire().setQuestionIndex(-1);
                    // Don't show the questionnaire final page. Show the completeness check site
                    showCompletenessCheck(incompletedQuestions);
                    // Set the next state to questionnaire final to indicate, that the current state is
                    // COMPLETENESS CHECK
                    this.nextState = States.QUESTIONNAIRE_FINAL;
                }
                break;
            }
            
            // Otherwise switch to the questionnaire final state
            switchState(States.QUESTIONNAIRE_FINAL);
            break;
            // QUESTIONNAIRE_FINAL
        case States.QUESTIONNAIRE_FINAL:
            // If the state is QUESTIONNAIRE_FINAL, leave the completion mode
            completionMode = false;

            var currentQuestionnaire = encounter.getCurrentQuestionnaire();
            var hasActiveFollower = encounter.hasActiveFollower(currentQuestionnaire.id);

            // If there is another questionnaire, go to that one
            if (encounter.hasNextQuestionnaire() && hasActiveFollower === true) {
                this.nextState = States.QUESTIONNAIRE_WELCOME;
                // Otherwise the survey is finished
            } else {
                this.nextState = States.BUNDLE_FINAL;
            }

            if (currentQuestionnaire.localizedFinalText[encounter.bundleLanguage] === undefined
                    || $.trim(currentQuestionnaire.localizedFinalText[encounter.bundleLanguage].replace(/&nbsp;/g, "").replace(/<br>/g, "").replace(/ /g, "").replace(/<p><\/p>/g, "")) === "") {
                next();
            } else {
                showQuestionnaireFinal(encounter.getCurrentQuestionnaire(), hasActiveFollower);
            }
            // Export the encounter on the next click of the next-Button
            break;
            // NEXT_QUESTION
        case States.NEXT_QUESTION:
            // Get the next question
            var nextQuestion = encounter.getCurrentQuestionnaire().getNextQuestion(completionMode);

            // Submit the encounter if the debounce time is over but do not care for its success
            if (nextQuestion.position > 1) {
                var timeNow = Date.now();
                if ((timeNow - debounceTimeCheck) > debounceTime) {
                    postEncounter(encounter, false);
                    debounceTimeCheck = timeNow;
                }
            }
            // Set the next question to the last seen one of the encounter
            encounter.lastSeenQuestionId = nextQuestion.id;

            // Show the next question
            showQuestion(nextQuestion);

            // if this was the last question of the current questionnaire, otherwise the 
            // next state value remains NEXT_QUESTION
            if (!encounter.getCurrentQuestionnaire().hasNextQuestion(completionMode)) {
                this.nextState = States.COMPLETENESS_CHECK;
            }
            break;
            // PREVIOUS_QUESTION
        case States.PREVIOUS_QUESTION:
            // If the next state is a question or the questionnaire final page, the current element is a question
            if (this.nextState === States.NEXT_QUESTION || this.nextState === States.QUESTIONNAIRE_FINAL || this.nextState === States.COMPLETENESS_CHECK) {
                // Submit the encounter if the debounce time is over but do not care for its success
                var timeNow = Date.now();
                if ((timeNow - debounceTimeCheck) > debounceTime) {
                    postEncounter(encounter, false);
                    debounceTimeCheck = timeNow;
                }
                // If it is the questionnaire final page, and you go one back from the current question,
                // the next question (seen from the previous one) will be the current one
                if (this.nextState === States.QUESTIONNAIRE_FINAL || this.nextState === States.COMPLETENESS_CHECK) {
                    this.nextState = States.NEXT_QUESTION;
                }
                // Get the previous question
                var previousQuestion = encounter.getCurrentQuestionnaire().getPreviousQuestion(completionMode);
                // Set the previous question to the last seen one of the encounter
                encounter.lastSeenQuestionId = previousQuestion.id;

                // Show the previous questions
                showQuestion(previousQuestion);
                // If it is any other state, the previous question must be the last question of the current questionnaire
            } else {
                showQuestion(encounter.getCurrentQuestionnaire().getLastQuestion(completionMode));
                this.nextState = States.QUESTIONNAIRE_FINAL;
            }
            break;
            // LOGOUT
        case States.LOGOUT:
            if (MobileApplication.isInternetAvailable() === true) {
                // For indicate that the last site is shown, set the this.nextState to null 
                this.nextState = null;
                // The user wants to end the application, thus open the data submit popup
                var submitDialog = document.getElementById("submitDialog"); 
                bootstrap.Modal.getOrCreateInstance(submitDialog).show();

                // Set the isCompleted Flag for the encounter
                encounter.isCompleted = true;

                // This is the final submit. Do it here already, because the setInterval method
                // will execute its content the first time AFTER it has waited the desired time
                postEncounter(encounter, true);
                var intervalID = setInterval(function () {
                    postEncounter(encounter, true);
                    // Stop interval
                    clearInterval(intervalID);
                }, waitBeforeResubmit);
            } else {
                var noInternetConnectionDialog = document.getElementById("noInternetConnectionDialog"); 
                bootstrap.Modal.getOrCreateInstance(noInternetConnectionDialog).show();
            }
            break;
    }

    $(document).scrollTop(0);
}

/**
 * Returns the number of unanswered not required questions in this questionnaire.
 * 
 * @param questionnaire The questionnaire object which should be checked
 * @return The number of unanswered not required questions in this questionnaire.
 */
function getIncompleteNotRequiredQuestionsCountFromQuestionnaire(questionnaire) {
    var incompleteQuestions = 0;
    for (var questionIndex in questionnaire.questions) {
        var question = questionnaire.questions[questionIndex];
        var responses = encounter.getResponsesForQuestion(question);
        var min = question.minNumberAnswers;
        var max = question.maxNumberAnswers;
        if (question.questionType !== Questiontypes.INFO_TEXT && question.isEnabled > 0 && question.isRequired === false) {

            // If there is no min/max value and at least one response, the question is complete
            if (responses.length > 0) {
                if ((min === null || responses.length >= min) && (max === null || responses.length <= max)) {
                    continue;
                }
            }
            // If there is a min/max value and the number of the responses array matches
            // the criterias, the question is complete
            if (min !== null && max !== null && responses.length >= min && responses.length <= max) {
                continue;
            }
            incompleteQuestions++;
        }
    }
    return incompleteQuestions;
}
/**
 * Returns the number of unanswered required questions and marks the appropriate questions as incomplete.
 * 
 * @param questionnaire The questionnaire object which should be checked.
 * @return The number of unanswered required questions.
 */
function getIncompleteQuestionsCountFromQuestionnaire(questionnaire) {
    var incompleteQuestionsCount = 0;
    for (var questionIndex in questionnaire.questions) {
        var question = questionnaire.questions[questionIndex];
        // For each completeness check, all questions have to be marked as complete,
        // so that no questions remain, that were marked as incomplete in the last check
        question.isIncomplete = false;
        question.isFirstIncomplete = false;
        var responses = encounter.getResponsesForQuestion(question);
        var min = question.minNumberAnswers;
        var max = question.maxNumberAnswers;
        if (question.isRequired === true) {
            switch (question.questionType) {
                case Questiontypes.DROP_DOWN:
                case Questiontypes.MULTIPLE_CHOICE:
                    if (question.isResponseValid(responses) === true) {
                        continue;
                    }
                    break;
                case Questiontypes.IMAGE:
                    if (responses.length > 0 && responses[0].pointsOnImage.length > 0) {
                        continue;
                    }
                    break;
                default:
                    if (responses.length > 0) {
                        continue;
                    }
                    break;
            }
            // Otherwise the question is incomplete
            question.isIncomplete = true;
            // Mark the question as first incomplete one, if there are no incomplete questions yet
            if (incompleteQuestionsCount === 0) {
                question.isFirstIncomplete = true;
            }
            if(question.isEnabled == 1){
                incompleteQuestionsCount++;
            }
        }
    }
    return incompleteQuestionsCount;
}

/**
 * This methods posts the encounter object as a JSON representation to the server
 * 
 * @param encounter The encounter object with the user's responses
 * @param finalSubmit If it is the <code>true</code>, the success will cause the application
 * to redirect to the start page. If it is <code>false</code>, nothing will happen.
 */
function postEncounter(encounter, finalSubmit) {
    if (encounter.isTest === true) {
        if (finalSubmit) {
            window.location.replace(contextPath + '/mobile/user/login?lang=' + defaultLanguage);
        }
        return true;
    } else {
        var data = ["bundle"];
        if (finalSubmit === true) {
            //do not exclude bundleDTO if it's finalSubmit
            data.push("bundleDTO");
        }

        $.ajax({
            url: "encounter",
            type: "POST",
            contentType: "application/json; charset=utf-8",
            async: true,
            timeout: waitBeforeResubmit,
            // Exclude the bundle object
            data: JSON.stringify(excludeFromJSON(encounter, data)),
            success: function () {
                if (finalSubmit) {
                    // Open the success popup, which will overlay the previous one $("#submitDialog").popup("close");
                    setInterval(function() {
                        var submitDialog = document.getElementById("submitDialog");
                        bootstrap.Modal.getOrCreateInstance(submitDialog).hide();
                    }, 100)

                    setTimeout(function () {
                        setInterval(function () {
                            var successDialog = document.getElementById("successDialog"); 
                            bootstrap.Modal.getOrCreateInstance(successDialog).show();
                        }, 250);
                    }, 500);


                    setTimeout(function () {
                        window.location.replace(contextPath + '/mobile/user/login?lang=' + defaultLanguage);
                    }, waitBeforeRedirect);
                }
            },
            error: function () {
                if (finalSubmit) {
                    postEncounter(encounter, true);
                }
            }
        });
    }
}

/**
 * This methods posts the encounter object as a JSON representation to the server
 * 
 * @param encounter The encounter object with the user's responses
 */
function postEncounterForScore(encounter) {
    var data = ["bundle"];
    data.push("bundleDTO");

    $.ajax({
        url: "encounter",
        type: "POST",
        contentType: "application/json; charset=utf-8",
        async: false,
        // Exclude the bundle object
        data: JSON.stringify(excludeFromJSON(encounter, data)),
        success: function () {

        },
        error: function () {
            postEncounterForScore(encounter);
        }
    });
}

/**
 * This methods exports one questionnaire.
 * 
 * @param encounter The encounter object with the user's responses
 * @param questionnaireId The id of the questionnaire
 */
function exportEncounter(encounter, questionnaireId) {
    $.ajax({
        url: "finishQuestionnaire?questionnaireId=" + questionnaireId,
        type: "POST",
        contentType: "application/json; charset=utf-8",
        // Exclude the bundle object
        data: JSON.stringify(excludeFromJSON(encounter, ["bundle", "bundleDTO"]))
    });
}

/**
 * Function to notify the user that he will leave this site. Loadable on
 * every page to prevent the unintentional use of the browsers back button
 * 
 */
function notifyBack() {
    window.onbeforeunload = function () {
        // Not prompt a message if the nextState is null
        if (this.nextState === null) {
            return null;
        }
        return "";
    };
}

/**
 * DEPRECATED: Was used by old font size buttons
 * Reduce the font size of the question and answer text
 */
function reduceFontSize() {
    if (fontSizeClass > fontSizeMinClass) {
        fontSizeClass -= 1; 
    }
    setFontSize(fontSizeClass);

    $("#increaseFontSizeButton").attr('disabled', false);
    if (fontSizeClass === fontSizeMinClass) {
        $("#reduceFontSizeButton").attr('disabled', true);
    } else {
        $("#reduceFontSizeButton").attr('disabled', false);
    }

    if (encounter.getCurrentQuestionnaire() !== null) {
        encounter.getCurrentQuestionnaire().getCurrentQuestion().rearrange();
    }
}

/**
 * DEPRECATED: Was used by old font size buttons
 * Increase the font size of the question and answer text
 */
function increaseFontSize() {
    if (fontSizeClass < fontSizeMaxClass) {
        fontSizeClass += 1;
    }
    setFontSize(fontSizeClass);

    $("#reduceFontSizeButton").attr('disabled', false);
    if (fontSizeClass === fontSizeMaxClass) {
        // Enable the button
        $("#increaseFontSizeButton").attr('disabled', true);
    } else {
        $("#increaseFontSizeButton").attr('disabled', false);
    }

    if (encounter.getCurrentQuestionnaire() !== null) {
        encounter.getCurrentQuestionnaire().getCurrentQuestion().rearrange();
    }
}

/*  * Set the font size for every resizable dom element in the body
 */
function setFontSize(size) {
    var newSize = Number(size); 
    if (fontSizeClass !== newSize) {
        fontSizeClass = newSize; 
    }
    // Resize every p and div tag in the answer area
    $('.resizable').each(function () {
        replaceFontSize($(this), newSize);  
    });
    // Resize the question title
    replaceFontSize($("#questionTitle"), newSize + 1);
    replaceFontSize($("#scrollQuestionTitle"), newSize + 1); 

    toggleScrollIndicators(); 
}

function replaceFontSize(elem, size) {
    $(elem).removeClass("resizable-0 resizable-1 resizable-2 resizable-3 resizable-4 resizable-5 resizable-6");
    $(elem).addClass("resizable-" + size); 
}

/**
 * This method add static resizing events post clearance of all resizing evets
 */
function addStaticResizingEvents(){
    $(window).on("resize",function () {
        updateScrollTitleVisibility();
    });
}

/**
 * This method excludes the fields from the given object. For example, passing an encounter object
 * and the string ["bundle"] for the field will return an encounter object without associated bundles.
 * 
 * @param obj The object from which fields are to be removed
 * @param fields The fields to be removed from the object
 * 
 * @return The object without the fields
 */
function excludeFromJSON(obj, fields) {
    var object = {};
    for (field in obj) {
        if (fields.indexOf(field) === -1) {
            object[field] = obj[field];
        }
    }
    return object;
}