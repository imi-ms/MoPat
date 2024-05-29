function Encounter() {

    var questionnaireIndex = -1;
    this.isCompleted;
    this.lastSeenQuestionId;
    this.id;
    this.uuid;
    this.bundle;
    this.isTest;
    this.responses;
    this.bundleLanguage;
    // Ids of activated questionnaires for export
    this.activeQuestionnaireIds;
    // Indicates whether an incomplete encounter was continued or not
    this.continuation = false;
    // Indicates whether an encounter is local or at home
    this.isAtHome;
    //Transfer variable. Only use for transfering the bundleDTO into the bundle variable
    this.bundleDTO;

    this.init = function () {

        // Create a array for the responses
        var tempResponses = new Array();

        // If there are given responses from an incomplete encounter: loop through
        for (var i = 0; i < this.responses.length; i++) {
            // Create a Response-Object from the plain js object
            var response = $.extend(new Response(), this.responses[i]);
            response.init();
            // Add the response to the array
            tempResponses.push(response);
        }
        // Overwrite the old array within the plain object with the new Response-Object array
        this.responses = tempResponses;

        // merge the plain bundle object in a Bundle()-Object
        this.bundle = $.extend(new Bundle(), this.bundleDTO);
        // Initialize the bundle
        this.bundle.init();

        // If the encounter is an incomplete one, get the last seen question, set the 
        // questionnaireIndex of the encounter and questionIndex of the choosen questionnaire
        // Create a flag to indicate if the question was found
        var questionFound = false;

        // Create an array to memorize the active questionnaire ids
        this.activeQuestionnaireIds = new Array();
        for (var i = 0; i < this.bundle.bundleQuestionnaires.length; i++) {
            if (this.bundle.bundleQuestionnaires[i].isEnabled > 0) {
                this.activeQuestionnaireIds.push(this.bundle.bundleQuestionnaires[i].questionnaire.id);
            }
        }
        
        // Name the loop to allow a break out
        findLastSeenQuestion:
        for (var i = 0; i < this.bundle.bundleQuestionnaires.length; i++) {
            var currentQuestionnaire = this.bundle.bundleQuestionnaires[i].questionnaire;
            for (var j = 0; j < currentQuestionnaire.questions.length; j++) {
                var currentQuestion = currentQuestionnaire.questions[j];
                // Check if the current question is the lastSeenQuestion
                if (this.lastSeenQuestionId === currentQuestion.id) {
                    // In this case set the questionnaire index
                    questionnaireIndex = i;
                    
                    if (j > 0) {
                        // Set the question index but dekrement j before, because
                        // the switchState-Methode gets next question. So this is the current one.
                        currentQuestionnaire.setQuestionIndex(--j);
                        // Trigger all conditions for questions before the current question including the current question
                        for (var k = 0; k <= currentQuestionnaire.getQuestionIndex() + 1; k++) {
                            currentQuestionnaire.questions[k].handleConditions();
                        }
                        nextState = States.NEXT_QUESTION;
                    } else {
                        // Handle conditions of the first question in this questionnaire
                        currentQuestionnaire.questions[0].handleConditions();
                        // Decrement the questionnaire index, because the
                        // switchState-Method gets next questionnaire. So this is the current one.
                        questionnaireIndex = i - 1;
                        // Indicate the encounter as an incomplete one
                        this.continuation = true;
                        nextState = States.QUESTIONNAIRE_WELCOME;
                    }

                    switchState(nextState);

                    questionFound = true;

                    // Break out of the named loop
                    break findLastSeenQuestion;
                }
            }
        }

        // If there was no last seen question, start from the beginning with
        // bundleWelcome and QUESTIONNAIRE_WELCOME as first nextState
        if (questionFound === false) {
            showBundleWelcome();
            nextState = States.QUESTIONNAIRE_WELCOME;
        }
        // disable reduce font size button because we already start with the minimum
        $("#reduceFontSizeButton").attr('disabled', true);

    };

    /**
     * Return the next consecutive questionnaire
     */
    this.getNextQuestionnaire = function () {
        return this.bundle.bundleQuestionnaires[++questionnaireIndex].questionnaire;
    };

    /**
     * Returns the currently active questionnaire
     */
    this.getCurrentQuestionnaire = function () {
        if (questionnaireIndex >= 0 && questionnaireIndex < this.bundle.bundleQuestionnaires.length) {
            return this.bundle.bundleQuestionnaires[questionnaireIndex].questionnaire;
        } else {
            return null;
        }
    };

    /**
     * Returns <code>true</code> if there is a following questionnaire. 
     */
    this.hasNextQuestionnaire = function () {
        return (this.bundle.bundleQuestionnaires.length - 1 != questionnaireIndex);
    };

    /**
     * Returns all responses created for the given question
     */
    this.getResponsesForQuestion = function (question) {
        var responses = new Array();

        $.each(this.responses, function (responseIndex, response) {
            $.each(question.answers, function (answerIndex, answer) {
                if (response.answerId == answer.id) {
                    responses.push(response);
                }
            });
        });

        return responses;
    };
    
    /**
     * Returns the count of select answer responses of the given question and
     * ignores the freetext answer reponse.
     */
    this.getCountOfSelectAnswerResponses = function (question) {
        var count = 0;
        
        $.each(this.responses, function (responseIndex, response) {
            $.each(question.answers, function (answerIndex, answer) {
                if (response.answerId === answer.id && response.customtext == null) {
                    count++;
                }
            });
        });
        
        return count;
    }

    /**
     * Returns the response created for the answer with the given answerId. If there exists 
     * no such response, a new one is created.
     */
    this.getResponse = function (answerId) {
        var response = null;
        // Only if there is an appropriate response, response will be set to something else than null
        $.each(this.responses, function (index, value) {
            if (value.answerId == answerId) {
                response = value;
                return;
            }
        });

        return response;
    };

    /**
     * Removes a given response from the encounter
     */
    this.removeResponse = function (response) {

        this.responses = $.grep(this.responses, function (answer) {
            return answer.answerId != response.answerId;
        });
    };

    /**
     * Adds the given response to the encounter, if it does not exist already. If it exists, the existing response is updated.
     */
    this.mergeResponse = function (response) {
        var isMerged = false;

        $.each(this.responses, function (index, value) {
            if (value.answerId == response.answerId) {
                this.value = response.value;
                this.customtext = response.customtext;
                isMerged = true;
            }
        });

        if (!isMerged) {
            this.responses.push(response);
        }
    };

    /**
     * Get the number of the enabled questions in the already answered questionnaires
     */
    this.getNumberOfEnabledQuestionsInAnsweredQuestionnaires = function () {
        var numberOfEnabledQuestions = 0;
        for (var i = 0; i < questionnaireIndex; i++) {
            numberOfEnabledQuestions = numberOfEnabledQuestions + this.bundle.bundleQuestionnaires[i].questionnaire.getNumberOfEnabledQuestions();
        }
        return numberOfEnabledQuestions;
    };

    /**
     * Activates or deactivates a questionnaire and add/removes the ids of active/inactive questionnaires
     * 
     * @param {type} bundleQuestionnaire The bundleQuestionnaire object to set enabled/disabled
     * @param Boolean active Value to enable or disable the bundleQuestionnaire
     */
    this.setQuestionnaireActivation = function (bundleQuestionnaire, active) {
        var questionnaireId = bundleQuestionnaire.questionnaire.id;

        if (active === true) {
            //Checks if activeQuestionnaireIds contains questionnaireId before
            if (jQuery.inArray(questionnaireId, this.activeQuestionnaireIds) === -1) {
                this.activeQuestionnaireIds.push(bundleQuestionnaire.questionnaire.id);
            }
        } else {
            //grep removes all elements from the activeQuestionnaireIds array that do not fit with the condition that is contained in the return argument
            this.activeQuestionnaireIds = jQuery.grep(this.activeQuestionnaireIds, function (id, index) {
                return (id !== questionnaireId);
            });
        }
    };

    /**
     * Figures out if questionnaire with given id has got at least one following active questionnaire
     * 
     * @param {type} questionnaireId Id of the questionnaire whose active follwers are searched
     * @returns {Boolean} True of there's an active follower, false if not
     */
    this.hasActiveFollower = function (questionnaireId) {
        var bundleQuestionnairePosition = null;
        for (var i = 0; i < this.bundle.bundleQuestionnaires.length; i++) {
            var currentBundleQuestionnaire = encounter.bundle.bundleQuestionnaires[i];
            // Find the position of the current questionnaire
            if (currentBundleQuestionnaire.questionnaire.id === questionnaireId) {
                bundleQuestionnairePosition = currentBundleQuestionnaire.position;
            }
            // Get the next active BundleQuestionnaire
            if (bundleQuestionnairePosition !== null && bundleQuestionnairePosition < currentBundleQuestionnaire.position && currentBundleQuestionnaire.isEnabled > 0) {
                // Jump into here only if the currentBundleQuestionnaire follows the bundleQuestionnaire of questionnaire with given id
                return true;
            }
        }
        return false;
    };

    /**
     * Returns the index of the current questionnaire
     * 
     * @returns 
     */
    this.getQuestionnaireIndex = function () {
        return questionnaireIndex;
    }
}