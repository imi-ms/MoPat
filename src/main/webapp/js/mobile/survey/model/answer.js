function Answer() {

    this.id;
    this.startDate;
    this.endDate;
    this.minValue;
    this.maxValue;
    this.stepsize;
    this.label;
    this.value;
    this.showValueOnButton;
    this.localizedLabel;
    this.localizedMaximumText;
    this.localizedMinimumText;
    this.localizedFreetextLabel;
    this.hasResponse;
    this.hasExportRule;
    this.hasCondition;
    this.isEnabled;
    this.bodyPartMessageCode;
    this.bodyPartPath;
    this.bodyPartImage;
    this.isOther;

    this.isDecimal;
    this.date;
    this.conditions;

    /**
     * This method needs to be called in order for the objects associated with
     * this object to be created
     */
    this.init = function() {
        var temp = new Array();
        if (this.conditions !== null) {
            $.each(this.conditions, function(index, value) {
                var condition = new Condition();
                temp.push($.extend(condition, value));
                condition.init();
            });
        }
        this.conditions = temp;
        if (this.isEnabled === true) {
            this.isEnabled = 1;
        } else {
            this.isEnabled = 0;
        }
    };
}