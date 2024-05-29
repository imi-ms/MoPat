/*
 * This function select the widget and both labels around and center them vertical
 */
function centerQuestionContentVertical() {
    var widget = $('#widgetContainer');
    var labelLeft = $('#questionLabelLeft');
    var labelRight = $('#questionLabelRight');
    // Get height from labelLeft, labelRight and widget to center
    // this elements vertical,
    var labelLeftHeight = labelLeft.height();
    var labelRightHeight = labelRight.height();
    var widgetHeight = widget.height();

    // Get the max height
    if (widgetHeight < 88) {
        var labelMaxHeight = labelRightHeight;
        if (labelLeftHeight > labelRightHeight) {
            labelMaxHeight = labelLeftHeight;
            // Set margin-top for the smaler label
            labelRight.css('margin-top', labelMaxHeight / 2 - labelRightHeight / 2);
        } else {
            // Set margin-top for the smaler label
            labelLeft.css('margin-top', labelMaxHeight / 2 - labelLeftHeight / 2);
        }
        // Set margin-top for the slider
        var widgetMarginTop = labelMaxHeight / 2 - widgetHeight / 2;
        widget.css('margin-top', widgetMarginTop);
        // If the widget get negative margin top, give the surroundet question container
        // margin top = the same number of pixels in positiv
        // To prevent the checkboxes for floating under the header
        if (widgetMarginTop < 0) {
            $('#questionContainer').css('margin-top', widgetMarginTop * (-1));
        }
    } else {
        labelLeft.css('margin-top', "8px");
        labelRight.css('margin-top', widgetHeight - 86 + "px");
        
    }

}

/*
 * This function calculate the optimal width for the labels and the widget
 */
function calculateQuestionContentWidth() {
    var labelLeft = $('#questionLabelLeft');
    var labelRight = $('#questionLabelRight');
    var widgetContainer = $('#widgetContainer');

    // Set the width of the widget. Give the element 10px puffer width
    widgetContainer.width("70%");

    // Set the width of the labels
    labelLeft.width("14%");
    labelRight.width("14%");
}

/*
 * This function creates a text for the min/max choice of questions
 */
function getMinMaxAnswerText(minAnswerNumber, maxAnswerNumber, answers) {
    var minMaxAnswerText;
    if (maxAnswerNumber > 1) {
        if (minAnswerNumber > 1) {
            if (maxAnswerNumber === answers.length) {
                minMaxAnswerText = strings['survey.questionnaire.MinAnswer'];
            } else {
                if (minAnswerNumber === maxAnswerNumber) {
                    minMaxAnswerText = strings['survey.questionnaire.ExactAnswer'];
                } else {
                    minMaxAnswerText = strings['survey.questionnaire.MinMaxAnswer'];
                }
            }
        } else if (maxAnswerNumber !== answers.length) {
            minMaxAnswerText = strings['survey.questionnaire.MaxAnswer'];
        }
    }
    if (minMaxAnswerText != null) {
        minMaxAnswerText = minMaxAnswerText.replace('{max}', maxAnswerNumber);
        minMaxAnswerText = minMaxAnswerText.replace('{min}', minAnswerNumber);
    }

    return $("<span/>", {text: minMaxAnswerText, "class": "resizable"});
}

/*
 * This function rearranges the labels and checkboxes for the vertical number checkbox question type.
 */
function rearrangeVerticalNumberCheckboxes() {
    var checkboxLabelMax = $('#questionLabelTop');
    var checkboxLabelMin = $('#questionLabelBottom');
    var checkboxContainer = $('#widgetContainer');
    var labelContainer = $('#labelContainer');
    // Set the height of the label container to the height of the checkbox container
    labelContainer.height(checkboxContainer.height());

    var width = (labelContainer.width() * 0.04);
    checkboxLabelMax.css('padding-right', width);
    checkboxLabelMin.css('padding-right', width);

    checkboxLabelMax.css('margin-top', '-10px');
    checkboxLabelMin.css('margin-bottom', '8px');
}