function Condition () {
    this.id;
    this.targetId;
    this.action;
    this.targetClass;
    this.thresholdType;
    this.thresholdValue;
    this.bundleId;
    this.targetAnswerQuestionId;
    
    var wasTriggered;
    
    this.init = function() {
        switch(this.thresholdType){
            case "SMALLER_THAN":
                this.thresholdType = "<";
                break;
            case "SMALLER_THAN_EQUALS":
                this.thresholdType = "<=";
                break;
            case "EQUALS":
                this.thresholdType = "=";
                break;
            case "BIGGER_THAN":
                this.thresholdType = ">";
                break;
            case "BIGGER_THAN_EQUALS":
                this.thresholdType = ">=";
                break;
            case "NOT_EQUALS":
                this.thresholdType = "!=";
                break;
            default:
                break;
        }
        
        this.wasTriggered = false;
    };
}