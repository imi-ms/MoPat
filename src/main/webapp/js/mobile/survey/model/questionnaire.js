function Questionnaire() {

    var questionIndex = -1;
    var currentProgress = 0;

    this.id;
    this.name;
    this.localizedWelcomeText;
    this.localizedFinalText;
    this.localizedDisplayName;
    this.logo;
    this.hasConditionsAsTarget;
    this.questions;

    //Transfer variable. Only use for transerfing the questionDTOs into the questions variable
    this.questionDTOs;

    this.init = function () {
        // If the logo is not null, create a base64 representation, thus it can be used on the client
        if (this.logo !== null) {
            MobileApplication.getBase64Image("questionnairelogo_" + this.id ,contextPath + "/images/questionnaire/" + this.id + "/" + this.logo);
        }

        var temp = new Array();
        $.each(this.questionDTOs, function (index, value) {
            var newQuestion = new Question();
            temp.push($.extend(newQuestion, value));
            newQuestion.init();
        });

        this.questions = temp;
    };

    /**
     * Returns the number of required questions in this questionnaire.
     * 
     * @returns The number of required questions in this questionnaire.
     */
    this.getRequiredQuestionsCount = function () {
        var requiredQuestionsCount = 0;
        for (var i = 0; i < this.questions.length; i++) {
            var question = this.questions[i];
            if (question.isRequired === true && question.isEnabled > 0) {
                requiredQuestionsCount++;
            }
        }
        return requiredQuestionsCount;
    };

    /**
     * Returns the number of answerable questions in this questionnaire. So all question of type INFO_TEXT will not be counted.
     * 
     * @returns The number of answerable questions in this questionnaire.
     */
    this.getAnswerableQuestionsCount = function () {
        var answerableQuestionsCount = 0;
        for (var i = 0; i < this.questions.length; i++) {
            var question = this.questions[i];
            if (question.questionType !== Questiontypes.INFO_TEXT && question.isEnabled > 0) {
                answerableQuestionsCount++;
            }
        }
        return answerableQuestionsCount;
    };
    
    /**
     * Set the question index
     * @param {type} newQuestionIndex The new question index
     */
    this.setQuestionIndex = function (newQuestionIndex) {
        questionIndex = newQuestionIndex;
    };

    /**
     * Get the question index
     * @returns {Number|questionIndex|i}
     */
    this.getQuestionIndex = function () {
        return questionIndex;
    };

    /**
     * If completionMode is false, this method returns the next consecutive question 
     * of the questionnaire. If there is no further question, <code>null</code> is returned.
     * Otherwise the next question with the value <code>true</code> for the isIncomplete
     * property returned.
     * 
     */
    this.getNextQuestion = function (completionMode) {
        if (questionIndex >= this.questions.length - 1) {
            return null;
        }

        // Loop through all following questions to search the next one which is visibe
        for (var i = questionIndex + 1; i < this.questions.length; i++) {
            var question = this.questions[i];
            var hasActiveAnswer = question.hasActiveAnswer();
            // If the completionMode is not active, just return the next visible question
            // otherwise return the visible question if it is incomplete
            if (question.isEnabled > 0 && (completionMode === false || (completionMode === true && question.isIncomplete === true)) && hasActiveAnswer === true) {
                questionIndex = i;
                return question;
            }
        }

        // return null by default
        return null;
    };

    /**
     * Returns the previous question
     */
    this.getPreviousQuestion = function (completionMode) {
        // If questionIndex is less than 0, return null
        if (questionIndex === -1) {
            return null;
        }
        
        // Loop through all previous questions to search the next one which is visibe
        for (var i = questionIndex - 1; i >= 0; i--) {
            var question = this.questions[i];
            var hasActiveAnswer = question.hasActiveAnswer();
            // If the completionMode is not active, just return the next visible question
            // otherwise return the visible question if it is incomplete
            if (question.isEnabled > 0 && (completionMode === false || (completionMode === true && question.isIncomplete === true)) && hasActiveAnswer === true) {
                questionIndex = i;
                return question;
            }
        }

        // return null by default
        return null;
    };

    /**
     * Returns the current question
     */
    this.getCurrentQuestion = function () {
        if (questionIndex >= 0) {
            return this.questions[questionIndex];
        } else {
            return null;
        }
    };

    /*
     * @returns The last question of this questionnaire
     */
    this.getLastQuestion = function (completionMode) {
        var question;
        questionIndex = -1;
        // Loop through all questions while there is a next one
        while (this.hasNextQuestion(completionMode)) {
            question = this.getNextQuestion(completionMode);
        }
        return question;
    };

    /**
     * Returns whether or not there is another question in this questionnaire
     */
    this.hasNextQuestion = function (completionMode) {
        // If the end of the questions is reached, return false no matter what 
        // the value of the completionMode is.
        if (questionIndex === this.questions.length - 1) {
            return false;
        }

        // Loop through all next questions to check if there is a visible one before
        for (var i = questionIndex + 1; i < this.questions.length; i++) {
            var question = this.questions[i];
            var hasActiveAnswer = question.hasActiveAnswer();
            // If the completionMode is not active, there is a next question
            // otherwise check if the next question is incomplete
            if (question.isEnabled > 0 && (completionMode === false || (completionMode === true && question.isIncomplete === true)) && hasActiveAnswer) {
                return true;
            }
        }

        //return false by default
        return false;
    };

    /**
     * Return true if the given question has an active previous question, false otherwise
     */
    this.hasQuestionActivePredecessor = function (question){
        for(var i = 0; i < question.position - 1; i++){
            if(this.questions[i].isEnabled > 0){
                return true;
            }
        }
        return false;
    }

    /**
     * Return true if the given question is the first question of the questionnaire
     */
    this.isFirstQuestion = function (question, completionMode) {
        if (completionMode === true) {
            return question.isFirstIncomplete === true;
        } else if (completionMode === false) {
            return !this.hasQuestionActivePredecessor(question);
        }
    };

    /**
     * Returns the question with the given Id
     */
    this.getQuestionById = function (questionId) {
        var question = null;
        $.each(this.questions, function (index, value) {
            if (value.id === questionId) {
                question = value;
            }
        });

        return question;
    };
    
    /**
     * Returns the questionnaire progress as an array: 
     * [0] = questionIndex, [1] = questionCount or null if the questionIndex is zero
     */
    this.getProgress = function () {
        var positionOfCurrentQuestion;
        var numberOfEnabledQuestions;
        if (encounter.bundle.showProgressPerBundle === true) {
            positionOfCurrentQuestion = encounter.getNumberOfEnabledQuestionsInAnsweredQuestionnaires() + encounter.getCurrentQuestionnaire().getPositionOfCurrentQuestion();
            numberOfEnabledQuestions = encounter.bundle.getNumberOfEnabledQuestions();
        } else {
            positionOfCurrentQuestion = this.getPositionOfCurrentQuestion();
            numberOfEnabledQuestions = this.getNumberOfEnabledQuestions();
        }
        // If the progress would be like 0/3 return null
        if (positionOfCurrentQuestion !== 0) {
            return Array(positionOfCurrentQuestion, numberOfEnabledQuestions);
        } else {
            return null;
        }
    };

    /**
     * Get the number of the enabled questions in this questionnaire
     */
    this.getNumberOfEnabledQuestions = function () {
        var numberOfEnabledQuestions = 0;
        for (var i = 0; i < this.questions.length; i++) {
            var hasActiveAnswer = this.questions[i].hasActiveAnswer();
            if (this.questions[i].isEnabled > 0 && hasActiveAnswer === true) {
                numberOfEnabledQuestions++;
            }
        }
        return numberOfEnabledQuestions;
    };

    /**
     * Get the position of the current question, not counting disabled questions
     */
    this.getPositionOfCurrentQuestion = function () {
        var positionOfCurrentQuestion = questionIndex + 1;
        for (var i = 0; i < questionIndex + 1; i++) {
            if (this.questions[i].isEnabled <= 0) {
                positionOfCurrentQuestion--;
            }
        }
        return positionOfCurrentQuestion;
    };

    /**
     * Figures out if this questionnaire has got at least one following questionnaire that is active
     */
    this.hasActiveFollower = function () {
        var bundleQuestionnairePosition = null;
        for (var i = 0; i < encounter.bundle.bundleQuestionnaires.length; i++) {
            var currentBundleQuestionnaire = encounter.bundle.bundleQuestionnaires[i];
            // Find the position of the current questionnaire
            if (currentBundleQuestionnaire.questionnaire.id === this.id) {
                bundleQuestionnairePosition = currentBundleQuestionnaire.position;
            }
            // Get the next active BundleQuestionnaire
            if (bundleQuestionnairePosition !== null && bundleQuestionnairePosition < currentBundleQuestionnaire.position && currentBundleQuestionnaire.isEnabled > 0) {
                // Jump into here only if the currentBundleQuestionnaire follows this questionnaire
                return true;
            }
        }
        return false;
    }
}