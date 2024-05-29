
/**
 * This module is used to update the elements of the navigation bar during the survey.
 */
function Navigation() {
};

/**
 * This method updates the visibility of the navigation buttons as well as their labels.
 * If the text of a button is empty, or the value is null, the button is hidden
 * 
 * @param title The new title for the navigation header.
 * @param buttonTextPrevious The new text for the "previous question" button.
 * @param buttonTextNext The new text for the "next question" button.
 */
Navigation.updateNavigation = function (title, progress, buttonTextPrevious, buttonTextNext, orientationVertical, deactivateProgressAndName) {

    if (buttonTextPrevious === null || buttonTextPrevious === "") {
        Navigation.hideButtonPrevious();
    } else {
        Navigation.updateButtonTextPrevious(buttonTextPrevious);
        Navigation.showButtonPrevious();
    }

    if (buttonTextNext === null || buttonTextNext === "") {
        Navigation.hideButtonNext();
    } else {
        Navigation.updateButtonTextNext(buttonTextNext);
        Navigation.showButtonNext();
    }

    Navigation.updateTitle(progress, title, orientationVertical, deactivateProgressAndName);
};

/**
 * Updates the title of the navigation header
 * 
 * @param title The new title for the navigation header.
 */
Navigation.updateTitle = function (progress, title, orientationVertical, deactivateProgressAndName) {
    if (deactivateProgressAndName === false) {
        // Change the title visualization
        $("#questionnaireTitle").empty();
        $("#questionnaireTitle").append(title);

        // If progress is activated, add info to dom
        if (progress !== null) {

            //activate text indicator in Nav bar
            $("#progressText").removeClass("d-none"); 
            var maxProgress = progress[1]; 
            var currentProgress = progress[0]; 

            //Set new values to text indicator
            $("#questionsAnswered").html(currentProgress); 
            $("#questionsTotal").html(maxProgress);

            $("#progressContainer").empty(); 

            //Fill progress bar with the right number of divs
            for (let i = 0; i < maxProgress; i++) {
                var newProgressElement = $("<div/>", {
                    "class": "progressElement"
                });

                //If the question was or is currently answered the elements are marked differently
                //CAVE currentProgress = 1 .. max, i = 0 .. max
                if (i < currentProgress) {
                    newProgressElement.addClass("active"); 
                    
                    // If element is currently shown question, add pulsating effect
                    if (i === currentProgress - 1) {
                        newProgressElement.addClass("current"); 
                    }
                } 

                // If maxProgress is smaller than a certain number, the progress should be shown as fragmented bars; Otherwise its shown as one bar
                //This is to prevent the different shrinkage for these elements for small screen sizes 
                if (maxProgress <= 40) {
                    newProgressElement.addClass("fragmented"); 
                }

                $("#progressContainer").append(newProgressElement); 
            }
        } 
        $("#questionnaireTitle").show();
    } else {
        $("#questionnaireTitle").hide();
    }
}

/**
 * Updates the text of the "previous question" button.
 * 
 * @param buttonTextPrevious The new text for the "previous question" button.
 */
Navigation.updateButtonTextPrevious = function (buttonTextPrevious) {
    $("#buttonPreviousText").text(buttonTextPrevious);
};

/**
 * Updates the text of the "next question" button.
 * 
 * @param buttonTextNext The new text for the "next question" button.
 */
Navigation.updateButtonTextNext = function (buttonTextNext) {
    $("#buttonNextText").text(buttonTextNext);
    // If the button text was updated, the stat changed, so remove the attention class
    $("#buttonNext").removeClass("btn-attention"); 
    $("#buttonNext").removeClass("btn-animate"); 
};

/**
 * Makes the "previous question" button visible
 */
Navigation.showButtonPrevious = function () {
    $("#buttonPrevious").removeClass("d-none");
    $("#buttonPrevious").addClass("d-flex");
};

/**
 * Makes the "previous question" button invisible
 */
Navigation.hideButtonPrevious = function () {
    $("#buttonPrevious").addClass("d-none");
    $("#buttonPrevious").removeClass("d-flex");
};

/**
 * Makes the "next question" button visible
 */
Navigation.showButtonNext = function () {
    $("#buttonNext").removeClass("d-none");
    $("#buttonNext").addClass("d-flex");
};

/**
 * Makes the "next question" button invisible
 */
Navigation.hideButtonNext = function () {
    $("#buttonNext").addClass("d-none");
    $("#buttonNext").removeClass("d-flex");
};