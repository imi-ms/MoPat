function BundleQuestionnaire() {

    this.position;
    this.questionnaire;
    this.isEnabled;
    this.showScores;

    //Transfer variable. Only use for transerfing the questionnaireDTO into the questionnaire variable
    this.questionnaireDTO;

    this.init = function () {
        this.questionnaire = $.extend(new Questionnaire(), this.questionnaireDTO);
        this.questionnaire.init();
        // Enable the questionnaire, if it is in the list of active questionnaires
        if (this.isEnabled === true) {
            this.isEnabled = 1;
        } else {
            this.isEnabled = 0;
        }
    };
}