function Bundle() {

    this.id;
    this.name;
    this.localizedWelcomeText;
    this.localizedFinalText;
    this.bundleQuestionnaires;
    this.showProgressPerBundle;
    this.deactivateProgressAndNameDuringSurvey;
    //Transfer variable. Only use for transerfing the bundleQuestionnairesDTO into the bundleQuestionnaires variable
    this.bundleQuestionnaireDTOs;
    this.init = function () {

        var temp = new Array();

        $.each(this.bundleQuestionnaireDTOs, function (index, value) {
            var newBundleQuestionnaire = new BundleQuestionnaire();
            temp.push($.extend(newBundleQuestionnaire, value));
            newBundleQuestionnaire.init();
        });

        this.bundleQuestionnaires = temp;

    };

    /**
     * Returns all questionnaires associated with this bundle
     */
    this.getQuestionnaires = function () {
        var questionnaires = new Array();
        $.each(this.bundleQuestionnaires, function (index, value) {
            questionnaires.push(value.questionnaire);
        });
        return questionnaires;
    };

    /**
     * Returns a questionnaire with the given id.
     */
    this.getQuestionnaireById = function (id) {
        $.each(this.bundleQuestionnaires, function (index, value) {
            if (value.questionnaire.id === id) {
                return value.questionnaire;
            }
        });

        return null;
    };

    /**
     * Get the number of the enabled questions in this bundle
     */
    this.getNumberOfEnabledQuestions = function () {
        var numberOfEnabledQuestions = 0;
        for (var i = 0; i < this.bundleQuestionnaires.length; i++) {
            var currentQuestionnaire = this.bundleQuestionnaires[i].questionnaire;
            // And calculate needed information to display the amount of enabled questions
            numberOfEnabledQuestions = numberOfEnabledQuestions + currentQuestionnaire.getNumberOfEnabledQuestions();
        }
        return numberOfEnabledQuestions;
    };

    /**
     * Returns true if the showScores attribute of one BundleQeustionnaire is active, false otherwise.
     * 
     * @returns {Boolean} true if the showScores attribute of one BundleQeustionnaire is active, false otherwise.
     */
    this.hasActiveScores = function () {
        for (var i = 0; i < this.bundleQuestionnaires.length; i++) {
            if (this.bundleQuestionnaires[i].showScores > 0) {
                return true;
            }
        }
        return false;
    };
}