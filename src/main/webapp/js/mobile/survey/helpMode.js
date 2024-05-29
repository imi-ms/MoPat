var HELP_ICON_SETTINGS = {
	PREVIOUS_BUTTON: {
		target_id: "#buttonPrevious",
		help_icon_id: "helpModePrevious",
	},
	NEXT_BUTTON: {
		target_id: "#buttonNext",
		help_icon_id: "helpModeNext",
	},
	TITLE_AND_PROGRESS: {
		target_id: "#questionnaireTitle",
		help_icon_id: "helpModeTitle",
	},
	FONT_SIZE_BUTTON: {
		target_id: "#fontSizeButton",
		help_icon_id: "helpModeIncreaseDecrease",
	},
	WELCOME_LOGO: {
		target_id: "#questionContent",
		help_icon_id: "helpModeLogo",
	},
	WELCOME_TEXT: {
		target_id: "#content",
		help_icon_id: "helpModeText",
	},
	QUESTION_IMAGE_REDO: {
		target_id: "#toolbarDiv",
		help_icon_id: "helpModeToolbarContent",
	},
	QUESTION_IMAGE_CANVAS: {
		target_id: "#canvas",
		help_icon_id: "helpModeQuestionContent",
	},
	QUESTION_CONTENT: {
		target_id: "#questionContent",
		help_icon_id: "helpModeQuestionContent",
	},
	QUESTION_TITLE: {
		target_id: "#questionTitleText",
		help_icon_id: "helpModeQuestionTitle",
	},
	QUESTIONNAIRE_FINAL_CONTENT: {
		target_id: "#content",
		help_icon_id: "helpModeContent",
	},
	BUNDLE_FINAL: {
		target_id: "#content",
		help_icon_id: "helpModeContent",
	},
	BUNDLE_FINAL_OVERVIEW: {
		target_id: "#bundleFinalInfo",
		help_icon_id: "helpModeOverview",
	},
	BUNDLE_WELCOME: {
		target_id: "#content",
		help_icon_id: "helpModeContent",
	},
};

function helpMode() {}

/**
 * Function to be added to the resize event of the window
 * Processes every visible help icon and repositions it
 */
helpMode.repositionHelpIcons = function () {
	$(".helpIconLink").each(function () {
		var settings = findObjectByTargetId($(this).attr("data-destination-id"));
		repositionElement($(this), settings.target_id);
	});
};

/**
 * Helper method to find the help icon settings 
 * object for a given target id
 * @param {*} targetId the target id to find 
 * @returns 
 */
function findObjectByTargetId(targetId) {
	for (let key in HELP_ICON_SETTINGS) {
		if (HELP_ICON_SETTINGS[key].target_id === targetId) {
			return HELP_ICON_SETTINGS[key];
		}
	}
}

/**
 * Helper function to reposition the element based on a target
 * Will compute the center position of the target element 
 * and adjust the offset accordingly
 * @param {*} element 
 * @param {*} target 
 */
function repositionElement(element, target) {
	var centerStyle = getElementOverlayStyle($(target), element);

	$(element).css(centerStyle);
}

// Activate the help mode
helpMode.showHelp = function () {
	// Check which state is the current state and create the right info buttons
	switch (currentState) {
		case States.QUESTIONNAIRE_WELCOME:
			setWelcomeHelpButtons(encounter);
			break;

		case States.NEXT_QUESTION:
		case States.PREVIOUS_QUESTION:
			setQuestionHelpIcons();
			break;

		case States.COMPLETENESS_CHECK:
			setCompletenessHelpIcons();
			break;

		case States.QUESTIONNAIRE_FINAL:
			setQuestionnaireFinalHelpIcons();
			break;

		case States.BUNDLE_FINAL:
			setBundleFinalHelpIcons();
			break;

		case States.LOGOUT:
			break;

		// default is currently used for BUNDLE_WELCOME state
		default:
			setBundleWelcomeHelpIcons();
			break;
	}
	// Create Increase/Decrease info button, because it's on every page
	setFontSizeButtonHelpIcon();
};

// Deactivate the help mode and remove all objects with 'helpMode'-class
helpMode.hideHelp = function () {
	$(".helpMode").remove();
};

/**
 * Creates a HTML link tag with the given ID, text and the info image
 * @param {String} id ID of the infolink tag
 * @param {String} text The text, which is shown in the Popup, when you click on this Tag
 * @returns {undefined} an info link tag with the given id and text
 */
function generateInfoLinkTagByID(id, text) {
	// Get the big info image as an image-tag
	var infoImageBig = $("<i />", {
		id: "infoImageBig",
		class: "bi bi-info-circle-fill bi-helpMode",
	});

	// Create an a-tag with an href on the popup
	var returnlink = $("<a>", {
		id: id,
		class: "helpMode helpIconLink",
		href: "#helpPopup",
		style: "z-index: 1002; position: absolute",
		"data-bs-toggle": "modal",
		"data-bs-target": "#helpPopup",
	});

	// Add an onclick function to the a-tag that changes the popup text and increases the popups size if required
	returnlink.on("click", function () {
		$("#helpPopupContent").empty();
		$("#helpPopupContent").append(text);
		infoImageBig.addClass("bi-active");
	});
	// Append the image to the a-tag
	infoImageBig.appendTo(returnlink);
	return returnlink;
}

/**
 * Function to add an info Icon for a specific target element
 * @param {*} destinationElement the target for which the info icon should be displayed
 * @param {*} helpIconId the id for the created icon
 * @param {*} helpDialogText the text that should be displayed when clicking the icon
 * @param {*} offsetTop the top offset from the target position
 * @param {*} offsetLeft the left offset from the target position
 */
function appendInfoIconToElement(
	destinationElement,
	helpIconId,
	helpDialogText
) {
	var infoNext = generateInfoLinkTagByID(helpIconId, helpDialogText);
	$("#infoIconOverlay").append(infoNext);

	$("#" + helpIconId)[0].setAttribute(
		"data-destination-id",
		"#" + destinationElement.attr("id")
	);

	var centerStyle = getElementOverlayStyle(
		destinationElement,
		$("#" + helpIconId)
	);
	$("#" + helpIconId).css(centerStyle);
}

/**
 * Function to compute the center position of a target Element
 * that can be applied to the icon element
 * @param {*} element the target element
 * @param {*} iconElement the icon element
 * @returns offset element
 */
function getElementOverlayStyle(element, iconElement) {
	var offset = element.offset();
	var rect = element[0].getBoundingClientRect();
	var iconRect = iconElement[0].getBoundingClientRect();

	offset.top = rect.top;
	offset.left = rect.left + (rect.width / 2 - iconRect.width / 2);

	return offset;
}

/**
 * Adds an icon to the previous button
 */
function setPreviousButtonHelpIcon() {
	if ($("#buttonPrevious").is(":visible")) {
		appendInfoIconToElement(
			$(HELP_ICON_SETTINGS.PREVIOUS_BUTTON.target_id),
			HELP_ICON_SETTINGS.PREVIOUS_BUTTON.help_icon_id,
			getInfoTextForPreviousButton()
		);
	}
}

/**
 * Returns the help text for the previous button depending on the current state of the survey
 * @returns help text string
 */
function getInfoTextForPreviousButton() {
	switch (currentState) {
		case States.COMPLETENESS_CHECK:
			return strings[
				"helpMode.label.questionnaire.previousQuestionButton"
			];
		case States.QUESTIONNAIRE_FINAL:
			return strings[
				"helpMode.label.questionnaireFinal.previousQuestionnaireButton"
			];
		default:
			return strings[
				"helpMode.label.questionnaire.previousQuestionButton"
			];
	}
}

/**
 * Adds an icon for the next button
 */
function setNextButtonHelpIcon() {
	appendInfoIconToElement(
		$(HELP_ICON_SETTINGS.NEXT_BUTTON.target_id),
		HELP_ICON_SETTINGS.NEXT_BUTTON.help_icon_id,
		getInfoTextForNextButton()
	);
}

/**
 * Returns the help text for the next button depending on the current state of the survey
 * @returns help text string
 */
function getInfoTextForNextButton() {
	switch (currentState) {
		case States.QUESTIONNAIRE_WELCOME:
			return strings["helpMode.label.questionnaireWelcome.nextButton"];
		case States.NEXT_QUESTION:
		case States.PREVIOUS_QUESTION:
			if (nextState === States.NEXT_QUESTION) {
				return strings[
					"helpMode.label.questionnaire.nextQuestionButton"
				];
			} else {
				return strings[
					"helpMode.label.questionnaire.finishQuestionnaireButton"
				];
			}
		case States.COMPLETENESS_CHECK:
			if (encounter.hasNextQuestionnaire()) {
				return strings[
					"helpMode.label.completenessCheck.nextQuestionnaireButton"
				];
			} else {
				return strings[
					"helpMode.label.completenessCheck.finishQuestionnaireButton"
				];
			}
		case States.QUESTIONNAIRE_FINAL:
			if (nextState === States.QUESTIONNAIRE_WELCOME) {
				return strings[
					"helpMode.label.questionnaireFinal.nextQuestionnaireButton"
				];
			} else {
				return strings[
					"helpMode.label.questionnaireFinal.endSurveyButton"
				];
			}
		case States.BUNDLE_FINAL:
			return strings["helpMode.label.bundleFinal.closeButton"];
		default:
			//As there is currently no state for Bundle Welcome this is handled here
			return strings["helpMode.label.bundleWelcome.nextButton"];
	}
}

/**
 * Adds an icon for the title in the survey header
 */
function setTitleAndProgressHelpIcon() {
	appendInfoIconToElement(
		$(HELP_ICON_SETTINGS.TITLE_AND_PROGRESS.target_id),
		HELP_ICON_SETTINGS.TITLE_AND_PROGRESS.help_icon_id,
		getInfoTextForTitleAndProgress()
	);
}

/**
 * Returns the help text for the header title depending on
 * the current state of the survey and certain settings
 * @returns
 */
function getInfoTextForTitleAndProgress() {
	switch (currentState) {
		case States.QUESTIONNAIRE_WELCOME:
			return strings["helpMode.label.questionnaireWelcome.title"];
		case States.QUESTIONNAIRE_FINAL:
			return strings["helpMode.label.questionnaireFinal.title"];
		case States.NEXT_QUESTION:
		case States.PREVIOUS_QUESTION:
			if (encounter.bundle.showProgressPerBundle === true) {
				return strings[
					"helpMode.label.questionnaire.titleAndProgressPerBundle"
				];
			} else {
				return strings["helpMode.label.questionnaire.titleAndProgress"];
			}
		default:
			return strings["helpMode.label.bundleWelcome.title"];
	}
}

/**
 * Adds a help icon for the font size button
 */
function setFontSizeButtonHelpIcon() {
	appendInfoIconToElement(
		$(HELP_ICON_SETTINGS.FONT_SIZE_BUTTON.target_id),
		HELP_ICON_SETTINGS.FONT_SIZE_BUTTON.help_icon_id,
		strings["helpMode.label.increaseDecrease"]
	);
}

/**
 * Adds all icons for the Questionnaire welcome view
 */
function setWelcomeHelpButtons() {
	var currentQuestionnaire = encounter.getCurrentQuestionnaire();

	setNextButtonHelpIcon();

	if (currentQuestionnaire.logo !== null) {
		setWelcomeLogoHelpIcon(currentQuestionnaire);
	}

	if (
		currentQuestionnaire.localizedWelcomeText[encounter.bundleLanguage] !==
			undefined ||
		currentQuestionnaire.localizedWelcomeText[encounter.bundleLanguage] !==
			""
	) {
		setWelcomeTextHelpIcon();
	}

	setTitleAndProgressHelpIcon();
}

/**
 * Adds an icon to the questionnaire welcome logo
 */
function setWelcomeLogoHelpIcon() {
	appendInfoIconToElement(
		$(HELP_ICON_SETTINGS.WELCOME_LOGO.target_id).children()[0],
		HELP_ICON_SETTINGS.WELCOME_LOGO.help_icon_id,
		strings["helpMode.label.questionnaireWelcome.logo"]
	);
}

/**
 * Adds an icon to the questionnaire welcome text
 */
function setWelcomeTextHelpIcon() {
	appendInfoIconToElement(
		$(HELP_ICON_SETTINGS.WELCOME_TEXT.target_id),
		HELP_ICON_SETTINGS.WELCOME_TEXT.help_icon_id,
		strings["helpMode.label.questionnaireWelcome.text"]
	);
}

/**
 * Adds all icons for the question view
 */
function setQuestionHelpIcons() {
	// Previous-Button
	setPreviousButtonHelpIcon();

	// Next-Button
	setNextButtonHelpIcon();

	// Title and progressbar
	setTitleAndProgressHelpIcon();

	var currentQuestionType = encounter
		.getCurrentQuestionnaire()
		.getCurrentQuestion().questionType;

	//Info Text has no content or question title
	if (currentQuestionType !== Questiontypes.INFO_TEXT) {
		setQuestionContentHelpIcon(currentQuestionType);

		setQuestionTitleHelpIcon();
	}
}

/**
 * Handles the icon for the question content depending on the question type
 * @param {*} currentQuestionType
 */
function setQuestionContentHelpIcon(currentQuestionType) {
	//Create the corresponding help icons
	switch (currentQuestionType) {
		//For image questions, two help icons will be added
		case Questiontypes.IMAGE:
			appendInfoIconToElement(
				$(HELP_ICON_SETTINGS.QUESTION_IMAGE_REDO.target_id),
				HELP_ICON_SETTINGS.QUESTION_IMAGE_REDO.help_icon_id,
				strings["helpMode.label.question.toolbar"]
			);

			appendInfoIconToElement(
				$(HELP_ICON_SETTINGS.QUESTION_IMAGE_CANVAS.target_id),
				HELP_ICON_SETTINGS.QUESTION_IMAGE_CANVAS.help_icon_id,
				strings["helpMode.label.question.image"]
			);

			break;

		//For every other question type the info icon will be added to the content directly
		case Questiontypes.MULTIPLE_CHOICE:
		case Questiontypes.SLIDER:
		case Questiontypes.FREE_TEXT:
		case Questiontypes.NUMBER_INPUT:
		case Questiontypes.NUMBER_CHECKBOX_TEXT:
		case Questiontypes.NUMBER_CHECKBOX:
		case Questiontypes.DROP_DOWN:
		case Questiontypes.DATE:
		case Questiontypes.BODY_PART:
			//First compute the offset left depending on question type
			/*if (
				currentQuestionType === Questiontypes.SLIDER ||
				currentQuestionType === Questiontypes.NUMBER_CHECKBOX ||
				currentQuestionType === Questiontypes.NUMBER_CHECKBOX_TEXT ||
				currentQuestionType === Questiontypes.DROP_DOWN
			) {
				var offsetLeft = HELP_ICON_SETTINGS.QUESTION_CONTENT.specialLeft;
			} else {
				var offsetLeft = HELP_ICON_SETTINGS.QUESTION_CONTENT.left;
			}*/

			//Then add the icon
			appendInfoIconToElement(
				$(HELP_ICON_SETTINGS.QUESTION_CONTENT.target_id),
				HELP_ICON_SETTINGS.QUESTION_CONTENT.help_icon_id,
				getInfoTextForQuestionType(currentQuestionType)
			);
			break;
	}
}

/**
 * Returns the help text for the question depending on the question type
 * @param {*} questionType the question type
 * @returns the help text string
 */
function getInfoTextForQuestionType(questionType) {
	switch (questionType) {
		case Questiontypes.MULTIPLE_CHOICE:
			return strings["helpMode.label.question.multipleChoice"];
		case Questiontypes.SLIDER:
			return strings["helpMode.label.question.slider"];

		case Questiontypes.FREE_TEXT:
			return strings["helpMode.label.question.freeText"];

		case Questiontypes.NUMBER_INPUT:
			return strings["helpMode.label.question.numberInput"];

		case Questiontypes.NUMBER_CHECKBOX_TEXT:
			return strings["helpMode.label.question.numberCheckboxText"];

		case Questiontypes.NUMBER_CHECKBOX:
			return strings["helpMode.label.question.numberCheckbox"];

		case Questiontypes.DROP_DOWN:
			return strings["helpMode.label.question.dropDown"];

		case Questiontypes.DATE:
			return strings["helpMode.label.question.date"];
		case Questiontypes.BODY_PART:
			return strings["helpMode.label.question.bodyPart"];

		default:
			return null;
	}
}

/**
 * Adds an icon for the question title
 */
function setQuestionTitleHelpIcon() {
	appendInfoIconToElement(
		$(HELP_ICON_SETTINGS.QUESTION_TITLE.target_id),
		HELP_ICON_SETTINGS.QUESTION_TITLE.help_icon_id,
		strings["helpMode.label.question.title"]
	);
}

/**
 * Adds all icons fo the completeness check view
 */
function setCompletenessHelpIcons() {
	// Previous-Button
	setPreviousButtonHelpIcon();
	// Next-Button
	setNextButtonHelpIcon();
}

/**
 * Adds all icons for the final questionnaire view
 */
function setQuestionnaireFinalHelpIcons() {
	// Previous-Button
	setPreviousButtonHelpIcon();

	// Next-Button
	setNextButtonHelpIcon();

	// Title
	setTitleAndProgressHelpIcon();

	// Content
	setQuestionContentHelpIcon();
}

/**
 * Adds an icon to the final questionnaire content
 */
function setQuestionnaireFinalContentHelpIcon() {
	appendInfoIconToElement(
		$(HELP_ICON_SETTINGS.QUESTIONNAIRE_FINAL_CONTENT.target_id),
		HELP_ICON_SETTINGS.QUESTIONNAIRE_FINAL_CONTENT.help_icon_id,
		strings["helpMode.label.questionnaireFinal.text"]
	);
}

/**
 * Adds all icons for the final bundle view
 */
function setBundleFinalHelpIcons() {
	// Next-Button
	setNextButtonHelpIcon();
	// Content
	setBundleFinalContentHelpIcon();

	//Overview
	setBundleFinalOverviewHelpIcon();
}

/**
 * Adds an icon to the final bundle content
 */
function setBundleFinalContentHelpIcon() {
	if (
		encounter.bundle.localizedFinalText[encounter.bundleLanguage] &&
		$.trim(
			encounter.bundle.localizedFinalText[
				encounter.bundleLanguage
			].replace(/&nbsp;/g, "")
		) !== ""
	) {
		appendInfoIconToElement(
			$(HELP_ICON_SETTINGS.BUNDLE_FINAL.target_id),
			HELP_ICON_SETTINGS.BUNDLE_FINAL.help_icon_id,
			strings["helpMode.label.bundleFinal.text"]
		);
	}
}

/**
 * Adds an icon to the final bundle overview text
 */
function setBundleFinalOverviewHelpIcon() {
	appendInfoIconToElement(
		$(HELP_ICON_SETTINGS.BUNDLE_FINAL_OVERVIEW.target_id),
		HELP_ICON_SETTINGS.BUNDLE_FINAL_OVERVIEW.help_icon_id,
		strings["helpMode.label.bundleFinal.overview"]
	);
}

/**
 * Adds all icons to the bundle welcome view
 */
function setBundleWelcomeHelpIcons() {
	// Next-Button
	setNextButtonHelpIcon();
	// Title
	setTitleAndProgressHelpIcon();
	// Welcome text or list
	setBundleWelcomeTextHelpIcon();
}

/**
 * Adds an icon to the bundle welcome text
 */
function setBundleWelcomeTextHelpIcon() {
	if (
		encounter.bundle.localizedWelcomeText[encounter.bundleLanguage] &&
		$.trim(
			encounter.bundle.localizedWelcomeText[
				encounter.bundleLanguage
			].replace(/&nbsp;/g, "")
		) !== ""
	) {
		var infoText = strings["helpMode.label.bundleWelcome.welcomeText"];
	} else {
		var infoText = strings["helpMode.label.bundleWelcome.listText"];
	}

	appendInfoIconToElement(
		$(HELP_ICON_SETTINGS.BUNDLE_WELCOME.target_id),
		HELP_ICON_SETTINGS.BUNDLE_WELCOME.help_icon_id,
		infoText
	);
}
