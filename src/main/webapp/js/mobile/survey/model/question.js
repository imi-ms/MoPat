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
    BODY_PART: "BODY_PART",
    BARCODE: "BARCODE"
};

var ImagePath = {
    FRONT: "images/body_front.svg",
    BACK: "images/body_back.svg"
};

var ImageType = {
    FRONT: "FRONT",
    BACK: "BACK",
    FRONT_BACK: "FRONT_BACK"
};

function Question() {

    this.id;
    this.isRequired;
    this.isEnabled;
    this.isIncomplete;
    this.isFirstIncomplete;
    this.maxNumberAnswers;
    this.minNumberAnswers;
    this.position;
    this.localizedQuestionText;
    this.questionType;
    this.answers;
    this.bodyPartImages;

    var response;
    var image;

    /**
     * This method needs to be called in order for the objects associated with
     * this object to be created
     */
    this.init = function () {
        var temp = new Array();
        $.each(this.answers, function (index, value) {
            var newAnswer = new Answer();
            temp.push($.extend(newAnswer, value));
            newAnswer.init();
        });
        this.answers = temp;

        if (this.isEnabled === true) {
            this.isEnabled = 1;
        } else {
            this.isEnabled = 0;
        }
    };

    /**
     * Returns the answer identified by the given answer Id.
     *
     * @param {Long} answerId The Id of the searched answer.
     * @returns {Answer} the answer identified by the given answer Id.
     */
    this.getAnswerById = function (answerId) {
        for (var i = 0; i < this.answers.length; i++) {
            if (this.answers[i].id === parseInt(answerId)) {
                return this.answers[i];
            }
        }
        return null;
    };

    this.calculatePositionBasedOnIndex = function(index, min, max) {
        // This needs to be adjusted depending on how you want the icons to scale with the slider values
        return min + (max - min) * index / 100;
    };

    /**
     * Appends the HTML representation of the current question given its
     * questiontype to the questioncontent object.
     * 
     * @param questioncontent
     *            The element into which the content is to be injected
     *            (ATTENTION: PASS BY REFERENCE)
     */
    this.getHTML = function (questioncontent) {
        // This is necessary so that the current object can be accessed from
        // within the jQuery functions

        // TODO Not really necessary
        var question = this;

        switch (this.questionType) {

            // MULTIPLE CHOICE
            case Questiontypes.MULTIPLE_CHOICE:
                questioncontent.append(getMinMaxAnswerText(this.minNumberAnswers, this.maxNumberAnswers, this.answers));
                var inputType = 'checkbox';
                // If only one response is allowed, create radio buttons instead of checkboxes
                if (this.maxNumberAnswers === 1) {
                    inputType = 'radio';
                }

                // Store the isOther and freetext answers
                isOtherAnswer = null;
                freetextAnswer = null;

                var multipleChoiceContainer = $("<div/>", {
                    id: "multiple-choice"
                });

                // Create the answer buttons for all possible answers
                $.each(this.answers, function (index, answer) {
                    if (answer.isEnabled > 0) {
                        if (answer.isOther === false && answer.localizedLabel !== null) {

                            var formCheck = $("<div/>", {
                                "class": "form-check"
                            });

                            var answerLabel = $("<label/>", {
                                "for": "input" + answer.id, 
                                "class": "btn btn-outline-primary center resizable", 
                                "id": answer.id
                            }); 

                            var leftDiv = $("<div/>", {
                                "class": "left"
                            });

                            var answerInput = $("<input/>", {
                                type: inputType, 
                                id: "input" + answer.id, 
                                name: "check",
                                "onclick": "Selector.selectElement(" + question.id + ", " + answer.id + ",'','','',true);",
                            }); 

                            var rightDiv =$("<div/>", {
                                "class": "right"
                            }); 

                            rightDiv.html(answer.localizedLabel[encounter.bundleLanguage]);
                            leftDiv.html(answerInput);
                            answerLabel.append(leftDiv);
                            answerLabel.append(rightDiv); 
                            formCheck.append(answerLabel); 
                            multipleChoiceContainer.append(formCheck)
                        } else if (answer.isOther === true) {
                            isOtherAnswer = answer;
                        } else {
                            freetextAnswer = answer;
                        }
                    }
                });


                // Create the isOther answer as last and append the textarea if necessary
                if (isOtherAnswer !== null && freetextAnswer !== null) {

                    var formCheck = $("<div/>", {
                        "class": "form-check"
                    });

                    var answerLabel = $("<label/>", {
                        "for": "input" + isOtherAnswer.id, 
                        "class": "btn btn-outline-primary center resizable", 
                        "id": isOtherAnswer.id
                    }); 

                    var leftDiv = $("<div/>", {
                        "class": "left"
                    });

                    var answerInput = $("<input/>", {
                        type: inputType, 
                        id: "input" + isOtherAnswer.id, 
                        name: "check",
                        "onclick": "Selector.selectElement(" + question.id + ", " + isOtherAnswer.id + ",'','','',true);",
                    }); 

                    var rightDiv =$("<div/>", {
                        "class": "right"
                    }); 

                    rightDiv.html(isOtherAnswer.localizedLabel[encounter.bundleLanguage]);
                    leftDiv.html(answerInput);
                    answerLabel.append(leftDiv);
                    answerLabel.append(rightDiv); 
                    formCheck.append(answerLabel); 
                
                    multipleChoiceContainer.append(formCheck);

                    var textareaWrapper = $("<div/>", {
                        "class": "textareaWrapper"
                    }); 

                    var textarea = $("<textarea/>", {
                        "id": "textarea",
                        "class": "resizable",
                        "name": "textarea",
                        "onChange": "Selector.selectElement(" + question.id + ", " + freetextAnswer.id + ", '', $('textarea').val(), '', true)",
                        "cols": "40",
                        "rows": "8",
                        "style": "height: auto;",
                        "disabled": "disabled"
                    });
                    textareaWrapper.append(textarea); 

                    multipleChoiceContainer.append(textareaWrapper);
                }

                questioncontent.append(multipleChoiceContainer); 


                break;
                // DROP DOWN
            case Questiontypes.DROP_DOWN :
                var dropDownWrapper = $("<div/>", {
                    "class": "select-wrapper w-100"
                })

                var dropDown = $("<select/>", {
                    "id": 'dropDown', 
                    "class": "btn resizable resizanle-0 select-mobile w-100"
                });

                dropDown.change(function () {
                    var selectedOption = $(this).find(":selected");
                    Selector.selectElement(question.id, selectedOption.attr('id'), selectedOption.html(), '', '', true);
                });
                // Create first option for no answer
                $("<option/>", {
                    "value": null,
                    "text": strings['survey.questionnaire.dropDownNoSelect']
                }).appendTo(dropDown);

                // Store the isOther and freetext answers
                isOtherAnswer = null;
                freetextAnswer = null;

                // Create one option for each possible answers
                $.each(this.answers, function (index, answer) {
                    var enable = "display: block;";
                    if (answer.isEnabled <= 0) {
                        enable = "display: none;"
                    }
                    if (answer.isOther === false && answer.localizedLabel !== null) {
                        $("<option/>", {
                            "id": answer.id,
                            "value": answer.id,
                            "text": answer.localizedLabel[encounter.bundleLanguage],
                            "style": enable
                        }).appendTo(dropDown);
                    } else if (answer.isOther === true) {
                        isOtherAnswer = answer;
                    } else {
                        freetextAnswer = answer;
                    }
                });

                dropDownWrapper.append(dropDown); 

                // Create the isOther answer as last and append the textarea if necessary
                if (isOtherAnswer !== null && freetextAnswer !== null) {
                    var enable = "display: block;";
                    if (isOtherAnswer.isEnabled <= 0) {
                        enable = "display: none;"
                    }
                    $("<option/>", {
                        "id": isOtherAnswer.id,
                        "value": isOtherAnswer.id,
                        "text": isOtherAnswer.localizedLabel[encounter.bundleLanguage],
                        "style": enable
                    }).appendTo(dropDown);

                    // Append drop down to the question content
                    questioncontent.append(dropDownWrapper);

                    var textarea = $("<textarea/>", {
                        "id": "textarea",
                        "class": "resizable mt-5",
                        "name": "textarea",
                        "onChange": "Selector.selectElement(" + question.id + ", " + freetextAnswer.id + ", '', $('textarea').val(), '', true)",
                        "cols": "40",
                        "rows": "8",
                        "style": "height: auto;" + enable,
                        "disabled": "disabled"
                    });

                    questioncontent.append(textarea); 
                } else {
                    // Append drop down to the question content
                    questioncontent.append(dropDownWrapper);
                }
                break;
                //SLIDER
            case Questiontypes.SLIDER:

                var showValue = this.answers[0].showValueOnButton; 

                //Parent div. Id is important to apply all styling to child divs
                var sliderDiv = $("<div/>", {
                    "id": "slider"
                }); 

                // Makes content flex
                var contentDiv = $("<div/>", {
                    "class": "d-flex justify-content-between"
                });

                // Div for the main input elements as well as the icons
                var inputDiv = $("<div/>", {
                    "class": "w-100 justify-content-center m-auto"
                });

                //Icon div that spreads all icons evenly
                var iconDiv = $("<div/>", {
                    "class": "d-flex justify-content-between align-items-center"
                });

                /**
                 * Creates 101 div elements with the same size as the slider thumb
                 * As all divs are inside a flexbox the ones without content will shrink in size
                 */
                if(this.answers[0].sliderIconConfigDTO != null){
                    for (let i = 0; i < 101; i++) {
                        let iconContainer = $("<div/>", {
                            "class": "d-flex justify-content-center align-items-center iconContainer"
                        }); 
                        let calculatedSliderPosition = this.calculatePositionBasedOnIndex(i, this.answers[0].minValue, this.answers[0].maxValue);
    
                        // Checks all icon elements and if the position matches the index of the current div appends the icon
                        this.answers[0].sliderIconConfigDTO.sliderIconDetailDTOS.forEach((icon) => {
                            if(icon.predefinedSliderIcon != null){
                                if (icon.iconPosition === i) {
                                    let iconContent = $("<i/>", {
                                        "class": "bi"
                                    })
                                    iconContent.addClass(icon.predefinedSliderIcon); 
                                    iconContent.on('click', () => {
                                        $("#range").val(calculatedSliderPosition).trigger('change');
                                    });
                                    iconContainer.append(iconContent); 
                                }
                            } else {
                                if (icon.iconPosition === i) {
                                    let iconContent = $("<img/>", {
                                        "src": icon.userIconBase64
                                    })
                                    iconContent.addClass(icon.predefinedSliderIcon); 
                                    iconContent.on('click', () => {
                                        $("#range").val(calculatedSliderPosition).trigger('change');
                                    });
                                    iconContainer.append(iconContent); 
                                }
                            }
                        }); 
    
                        iconDiv.append(iconContainer); 
                    }
                }
                else{
                    for (let i = 0; i < 101; i++) {
                        let iconContainer = $("<div/>", {
                            "class": "d-flex justify-content-center align-items-center iconContainer"
                        }); 
    
                        // Checks all icon elements and if the position matches the index of the current div appends the icon
                        this.answers[0].icons.forEach((icon) => {
                            if (icon.iconPosition === i) {
                                let iconContent = $("<i/>", {
                                    "class": "bi"
                                })
                                iconContent.addClass(icon.predefinedSliderIcon); 
                                iconContent.on('click', () => {
                                    $("#range").val(calculatedSliderPosition).trigger('change');
                                });
                                iconContainer.append(iconContent); 
                            }
                        }); 
    
                        iconDiv.append(iconContainer); 
                    }
                }
                
                

                //Spacer between the icon row and the input slider; To have space for the value div
                var spacerDiv = $("<div/>", {
                    "class": "spaceBuffer"
                }); 

                //Wrapper for the range elements to align slider with value div
                var rangeWrap = $("<div/>", {
                    "class": "rangeWrap"
                }); 

                //The input element that creates a range slider
                var inputElement = $("<input />", {
                    "id": "range",
                    "type": "range", 
                    "min": this.answers[0].minValue, 
                    "max": this.answers[0].maxValue, 
                    "step": this.answers[0].stepsize,
                    "class": "form-range hiddenThumb"
                })

                //Value div that gets placed / shown / filled by setSliderValue function
                var rangeValue =$("<div/>", {
                    "class": "rangeValue hideValue",
                    "id": "rangeV"
                }); 

                //Only show value div, if activated for question
                if (showValue !== true) {
                    rangeValue.addClass("d-none"); 
                }

                // Event for pressing the slider: Shows the value div
                var checkMouseDown = ()=>{
                    if ($("#rangeV").hasClass("hideValue")) {
                        changed = true; 
                        $("#sliderInput").val($("#range").val());
                        question.setSliderValue();
                        question.toggleSliderValueOntop(true);
                        question.toggleSliderMargin(showValue, true);
                    }
                };

                /**
                 * Event for releasing the slider:
                 * If the value was not changed the value div will be hidden and the value for data binding is emptied
                 */
                var _object = this;
                // The jQuery selector must be used instead of the slider
                // variable, because otherwise the events are not attached
                
                var changed = false;
                var checkMouseUp = ()=>{
                    if (changed === false) {
                        $("#sliderInput").val("");
                        question.toggleSliderValueOntop(false);
                        question.toggleSliderMargin(showValue, false);
                    } else {
                        changed = false; 
                    }
                    Selector.selectElement(question.id, _object.answers[0].id, $("#sliderInput").val(), '', '', true);
                };

                //Simple function to set changed to true
                var setChanged = ()=>{
                    changed = true; 
                };


                //Add event listeners to input div
                inputElement.on("input", question.setSliderValue);
                inputElement.on("input", setChanged);
                inputElement.on("change", checkMouseDown);

                inputElement.on("mousedown", checkMouseDown);
                inputElement.on("touchstart", checkMouseDown);
                inputElement.on("mouseup", checkMouseUp);
                inputElement.on("touchend", checkMouseUp);

                //Add resize element so slider value div is always positioned correctly
                $(window).on("resize", function() {
                    question.setSliderValue(); 
                });

                // The hidden input that is used for data binding
                var bindingInput = $("<input/>", {
                    "type": "hidden", 
                    "id": "sliderInput"
                }); 


                //Order the elements to create slider 
                rangeWrap.append(inputElement); 

                rangeWrap.append(rangeValue); 
                rangeWrap.append(bindingInput); 

                inputDiv.append(iconDiv); 
                inputDiv.append(spacerDiv); 
                inputDiv.append(rangeWrap); 

                contentDiv.append(inputDiv); 
                
                sliderDiv.append(contentDiv); 

                //Fetch localized texts from answerDTO
                var localizedMinText = this.answers[0].localizedMinimumText[encounter.bundleLanguage];
                var localizedMaxText = this.answers[0].localizedMaximumText[encounter.bundleLanguage];

                // Create text divs that are below the range slider, on each end respectively 
                var textDiv = $("<div/>", {
                    "class": "d-flex justify-content-between"
                }); 

                var leftText = $("<div/>", {
                    "class": "textLeft align-self-start me-auto text-start mb-4 hyphenate resizable",
                    "html": localizedMinText === "" || typeof localizedMinText === 'undefined' ? "&nbsp" : localizedMinText, 
                }); 

                var rightText = $("<div/>", {
                    "class": "textRight align-self-start ms-auto text-end mb-4 hyphenate resizable",
                    "html": localizedMaxText === "" || typeof localizedMaxText === 'undefined' ? "&nbsp" : localizedMaxText, 
                });

                textDiv.append(leftText); 
                textDiv.append(rightText); 

                sliderDiv.append(textDiv); 

                var sliderWidthElement = $("<div/>", {
                    "class": "d-none sliderWidth"
                }); 

                sliderDiv.append(sliderWidthElement); 

                questioncontent.append(sliderDiv); 
                break; 

                
                // INFO TEXT 
            case Questiontypes.INFO_TEXT:
                questioncontent.append($("<span/>", {"html": question.localizedQuestionText[encounter.bundleLanguage], "class": "resizable"}));
                $("#questionTitle").empty();
                $("#questionTitle").append($("<span />", {"text": strings['survey.question.infotext.hint']}));
                break;
                // FREE TEXT 
            case Questiontypes.FREE_TEXT:
                var textarea = $("<textarea/>", {
                    "id": "textarea",
                    "class": "resizable",
                    "name": "textarea",
                    "onChange": "Selector.selectElement(" + question.id + ", " + this.answers[0].id + ", '', $('textarea').val(), '', true)",
                    "cols": "40",
                    "rows": "8",
                    "style": "height: auto;"
                });
                questioncontent.append(textarea);
                break;
            case Questiontypes.BARCODE:

                // Add textarea to see the scan
                var textarea = $("<textarea/>", {
                    "id": "textarea",
                    "class": "resizable",
                    "name": "textarea",
                    "onChange": "Selector.selectElement(" + question.id + ", " + question.answers[0].id + ", '', $('textarea').val(), '', true)",
                    "cols": "40",
                    "rows": "8",
                    "style": "height: auto;",
                    "disabled": "disabled"
                });
                questioncontent.append(textarea);

                // Check if the device supports WebRTC
                if (Modernizr.getusermedia === true) {
                    var barcodeReader = new ZXing.BrowserBarcodeReader();
                    var qrcodeReader = new ZXing.BrowserQRCodeReader();
                    var datamatrixReader = new ZXing.BrowserDatamatrixCodeReader();
                    var videoDeviceIndex = "";
                    var videoDeviceIndex = 0;

                    var switchCamera = function(event) {
                        /*[- Reset to clear all existing instances -]*/
                        barcodeReader.reset();
                        qrcodeReader.reset(); 
                        datamatrixReader.reset(); 
                        barcodeReader.getVideoInputDevices().then(function(inputDevices) {
                            if (inputDevices.length > 0) {
                                if (inputDevices.length -1 > videoDeviceIndex) {
                                    videoDeviceIndex++;
                                } else {
                                    videoDeviceIndex = 0; 
                                }
                                videoDeviceId = inputDevices[videoDeviceIndex].deviceId; 
                                setTimeout(loadVideoDiv(), 1);
                            }
                        });
                    };

                    var loadVideoDiv = function() {
                        // Check if the scan was successful and fill in the result
                        barcodeReader.decodeFromInputVideoDevice(videoDeviceId, 'video').then(function (result) {
                            document.getElementById('textarea').value = result.text;
                            $(".btn-close").click(); 
                            barcodeReader.reset();
                            qrcodeReader.reset();
                            datamatrixReader.reset();
                            $('#clearButton').removeAttr('disabled');
                            Selector.selectElement(question.id, question.answers[0].id, '', $('#textarea').val(), '', true);
                        })
                        .catch(function (err) {});
                        qrcodeReader.decodeFromInputVideoDevice(videoDeviceId, 'video').then(function (result) {
                            document.getElementById('textarea').value = result.text;
                            $(".btn-close").click(); 
                            barcodeReader.reset();
                            qrcodeReader.reset();
                            datamatrixReader.reset();
                            $('#clearButton').removeAttr('disabled');
                            Selector.selectElement(question.id, question.answers[0].id, '', $('#textarea').val(), '', true);
                        })
                        .catch(function (err) {});
                        datamatrixReader.decodeFromInputVideoDevice(videoDeviceId, 'video').then(function (result) {
                            document.getElementById('textarea').value = result.text;
                            $(".btn-close").click(); 
                            barcodeReader.reset();
                            qrcodeReader.reset();
                            datamatrixReader.reset();
                            $('#clearButton').removeAttr('disabled');
                            Selector.selectElement(question.id, question.answers[0].id, '', $('#textarea').val(), '', true);
                        })
                        .catch(function (err) {});
                    };

                    var closeModal = function() {
                        barcodeReader.reset();
                        qrcodeReader.reset();
                        datamatrixReader.reset();
                        $('#clearButton').removeAttr('disabled');
                    }

                    var openModal = function() {
                        barcodeReader.reset();
                        qrcodeReader.reset();
                        datamatrixReader.reset();
                        // Disable the clear button while scanning
                        $('#clearButton').attr('disabled', 'disabled');
                        // Timeout is needed for authorization on an safari device
                        barcodeReader.getVideoInputDevices().then(function() {
                            setTimeout(loadVideoDiv(), 1);
                        });
                    }

                    barcodeReader.getVideoInputDevices().then(function (videoInputDevices) {
                        // Only attach the functionality if there is at least one camera device
                        if (videoInputDevices.length > 0) {
                            videoDeviceId = videoInputDevices[0].deviceId;
                            // Add a button to begin scanning
                            var scanButton = $("<button/>", {
                                "id": "scanButton",
                                "class": "btn btn-mobile btn-question me-1 mb-2",
                                "title": strings['survey.question.barcode.scanButton'],
                                "data-bs-toggle": "modal",
                                "data-bs-target": "#videoModal"
                            });

                            var scanLabel = $("<span/>", {
                                "class": "resizable",
                                "text": strings['survey.question.barcode.scanButton']
                            });

                            scanButton.append(scanLabel);

                            var clearButton = $("<button/>", {
                                "id": "clearButton",
                                "class": "btn btn-mobile btn-question ms-1 mb-2",
                                "title": strings['survey.question.barcode.clearButton'],
                            });

                            var clearLabel = $("<span/>", {
                                "class": "resizable",
                                "text": strings['survey.question.barcode.clearButton']
                            });

                            clearButton.append(clearLabel);

                            var buttonDiv = $("<div/>", {
                                "id": "buttonDiv",
                                "class": "d-flex" 
                            });

                            buttonDiv.append(scanButton);
                            buttonDiv.append(clearButton);

                            var videoModal = $("<div/>", {
                                "class": "modal modal-xl fade",
                                "id": "videoModal",
                                "data-backdrop": "static", 
                                "data-keyboard": "false",
                                "tabindex": "-1",
                                "aria-labeledby": "staticBackdrop",
                                "aria-hidden": "true"
                            }); 

                            var modalDialog = $("<div/>", {
                                "class": "modal-dialog modal-dialog-centered"
                            }); 

                            var modalContent = $("<div/>", {
                                "class": "modal-content"
                            }); 

                            var modalHeader = $("<div/>", {
                                "class": "modal-header"
                            }); 
                            modalHeader.append($("<button/>", {
                                "type": "button",
                                "class": "btn-close",
                                "data-bs-dismiss": "modal",
                                "aria-label": "Close"
                            })); 

                            var modalBody = $("<div/>", {
                                "class": "modal-body"
                            });

                            // Add a div for the video input
                            var videoDiv = $("<div/>", {
                                "id": "videoDiv",
                                "class": "d-flex flex-column justify-content-center align-items-center",
                                "style": "display: none;"
                            });

                            var barcodeInfoText = $("<div/>", {
                                "id": "barcodeInfoText",
                                "style": "width: 100%;"
                            });

                            var titleSpan = document.createElement('span');
                            titleSpan.innerHTML = '<b>' + strings['survey.check.barcodereader.header'] + '</b><br>';
                            $(titleSpan).addClass('resizable');

                            var infoDiv = $("<div/>", {
                                "class": "d-flex justify-content-between align-items-center mb-5"
                            });

                            var infoSpan = document.createElement('span');
                            infoSpan.innerHTML = strings['survey.check.barcodereader.content'];
                            $(infoSpan).addClass('resizable');

                            var switchCameraButton = $("<button/>", {
                                "type": "button",
                                "class": "btn-mobile",
                            }); 

                            switchCameraButton.append($("<span/>", {
                                "class": "me-2",
                                "text": strings["survey.check.barcodereader.switchCamera"],
                            }));

                            switchCameraButton.append($("<i/>", {
                                "class": "bi bi-camera-fill"
                            })); 

                            switchCameraButton.append($("<i/>", {
                                "class": "bi bi-arrow-clockwise"
                            })); 

                            switchCameraButton.on("click", switchCamera); 

                            infoDiv.append(infoSpan); 
                            infoDiv.append(switchCameraButton); 

                            barcodeInfoText.append(titleSpan);
                            barcodeInfoText.append(infoDiv);
                            videoDiv.append(barcodeInfoText);

                            var video = document.createElement('video');
                            video.id = 'video';
                            video.playsinline = true;
                            video.autoplay = true;
                            video.muted = true;
                            video.autofocus = true;
                            video.style = 'max-width: 600px; max-height: 600px;'
                            video.classList.add("col-12");

                            videoDiv.append(video);

                            modalBody.append(videoDiv); 

                            modalContent.append(modalHeader);
                            modalContent.append(modalBody); 

                            modalDialog.append(modalContent); 

                            videoModal.append(modalDialog); 


                            questioncontent.prepend(videoModal); 
                            questioncontent.prepend(buttonDiv);
                            
                            setFontSize(fontSizeClass);

                            $("#videoModal").on("hidden.bs.modal", closeModal);

                            $("#videoModal").on("shown.bs.modal", openModal);
                            // Add the EventListener for the click event to the scan button
                            document.getElementById('clearButton').addEventListener('click', function () {
                                $('#textarea').val('');
                                Selector.selectElement(question.id, question.answers[0].id, '', $('#textarea').val(), '', true);
                            });
                        } else {
                            $('#textarea').removeAttr('disabled');
                        }
                    });
                } else {
                    $('#textarea').removeAttr('disabled');
                }

                break;
                // NUMBER INPUT
            case Questiontypes.NUMBER_INPUT:
                // Get the appropriate message for the number input. Could be either a decimal or a integer.
                if (this.answers[0].stepsize != null) {
                    var topicLabelHtml = this.answers[0].stepsize % 1 != 0 ? 
                    strings['survey.questionnaire.label.numberInput.wrongInputDecimal'] : 
                    strings['survey.questionnaire.label.numberInput.wrongInputInteger'];
                } else {
                    //If no stepsize is given, any input is allowed 
                    var topicLabelHtml = strings['survey.questionnaire.label.numberInput.wrongInputDecimal'];
                }
                var roundNoteHtml = strings['survey.questionnaire.label.numberInput.roundingNote'];
                // If the number input has a min and a max value, only a min value or only a max get the additional message.
                if (this.answers[0].minValue !== null && this.answers[0].maxValue !== null) {
                    topicLabelHtml += " " + strings['survey.questionnaire.label.numberInput.minMax'];
                } else if (this.answers[0].maxValue !== null) {
                    topicLabelHtml += " " + strings['survey.questionnaire.label.numberInput.max'];
                } else if (this.answers[0].minValue !== null) {
                    topicLabelHtml += " " + strings['survey.questionnaire.label.numberInput.min'];
                } 
                //Always append stepsize message if a value was given
                if (this.answers[0].stepsize !== null) {
                    topicLabelHtml += " " + strings['survey.questionnaire.label.numberInput.stepSize'];
                }
                // Replace the placeholders.
                topicLabelHtml = topicLabelHtml.replace('{min}', this.answers[0].minValue);
                topicLabelHtml = topicLabelHtml.replace('{max}', this.answers[0].maxValue);
                
                if (this.answers[0].stepsize != null) {
                    var numStr = this.answers[0].stepsize;
                    if (this.answers[0].stepsize.includes('.') && this.answers[0].stepsize.endsWith('.0')) {
                        // Remove the decimal point and trailing zero
                        numStr = this.answers[0].stepsize.slice(0, -2);
                    }
                    topicLabelHtml = topicLabelHtml.replace("{stepSize}",numStr);
                }

                var topicLabel = $("<label/>", {
                    "html": topicLabelHtml,
                    "class": "resizable"
                });

                questioncontent.append(topicLabel);

                if (this.answers[0].stepsize != null) {
                    var roundingNoteLabel = $("<p/>", {
                        "html": roundNoteHtml,
                        "class": "mb-5"
                    });
                    questioncontent.append(roundingNoteLabel);
                }

                var row = $("<div/>", {
                    "class": "row mt-5 d-flex align-items-center"
                }); 

                var inputLabel = $("<div/>", {
                    "class": "col-12 col-bp-4 d-flex align-items-center"
                }); 

                inputLabel.append($("<span/>", {
                    "id": "freetextLabel",
                    "class": "resizable",
                    "html": this.answers[0].stepsize % 1 != 0 ? strings['survey.questionnaire.label.numberInput.decimal'] + ":" : strings['survey.questionnaire.label.numberInput.integer'] + ":"
                })); 

                var inputDiv = $("<div/>", {
                    "class": "tooltipC col-12 col-bp-8"
                }); 

                inputDiv.append($("<input/>", {
                    "type": "number",
                    "class": "resizable form-control gray",
                    "min": this.answers[0].minValue,
                    "max": this.answers[0].maxValue,
                    "step": this.answers[0].stepsize,
                    "name": "numberInput",
                    "id": "numberInput",
                    "onChange": "Selector.selectElement(" + question.id + ", " + this.answers[0].id + ", this.value, '', '', true)"
                }));

                inputDiv.append($("<span/>", {
                    "id": "toolTipText"
                }));

                row.append(inputLabel); 
                row.append(inputDiv); 

                questioncontent.append(row); 
                break;
                // NUMBER CHECKBOX TEXT
            case Questiontypes.NUMBER_CHECKBOX_TEXT:
                createNumberCheckboxes(this, questioncontent);

                var label = $("<div/>", {
                    "for": "textarea",
                    "class": "resizable",
                    "html": this.answers[0].localizedFreetextLabel[encounter.bundleLanguage]
                });
                

                var textareaWrapper = $("<div/>", {
                    "class": "textareaWrapper"
                }); 

                var textarea = $("<textarea/>", {
                    "id": "textarea",
                    "class": "resizable",
                    "name": "textarea",
                    "onChange": "Selector.selectElement(" + question.id + ", " + this.answers[0].id + ", parseInt($('input[name*=numberedCheckbox_]:checked').val()), $('textarea').val(), '', true)",
                    "cols": "40",
                    "rows": "8",
                    "style": "height: auto;"
                });
                textareaWrapper.append(label).append(textarea); 
                questioncontent.append(textareaWrapper);
                break;
                // NUMBER CHECKBOX
            case Questiontypes.NUMBER_CHECKBOX :
                createNumberCheckboxes(this, questioncontent);
                break;
            case Questiontypes.DATE:

                var topicLabelHtml = strings['survey.questionnaire.label.dateDescription'];
                // If the date input has a start and a end date, only a start date value or only a end date get the additional message.
                if (this.answers[0].startDate !== null && this.answers[0].endDate !== null) {
                    topicLabelHtml += " " + strings['survey.questionnaire.label.date.startEndDate'];
                } else if (this.answers[0].endDate !== null) {
                    topicLabelHtml += " " + strings['survey.questionnaire.label.date.endDate'];
                } else if (this.answers[0].startDate !== null) {
                    topicLabelHtml += " " + strings['survey.questionnaire.label.date.startDate'];
                }
                // Replace the placeholders.
                var dateOptions = {year: 'numeric', month: '2-digit', day: '2-digit'};
                topicLabelHtml = topicLabelHtml.replace('{startDate}', new Date(this.answers[0].startDate).toLocaleDateString(encounter.bundleLanguage.replace("_", "-"), dateOptions));
                topicLabelHtml = topicLabelHtml.replace('{endDate}', new Date(this.answers[0].endDate).toLocaleDateString(encounter.bundleLanguage.replace("_", "-"), dateOptions));

                var topicLabel = $("<label/>", {
                    "html": topicLabelHtml,
                    "class": "resizable"
                });

                questioncontent.append(topicLabel);

                var row = $("<div/>", {
                    "class": "row mt-5"
                }); 

                var inputLabel = $("<div/>", {
                    "class": "col-12 col-bp-3 d-flex align-items-center"
                }); 

                inputLabel.append($("<span/>", {
                    "class": "resizable",
                    "html": strings['survey.questionnaire.label.date'] + ':'
                }));

                var inputDiv = $("<div/>", {
                    "class": "col-12 col-bp-9"
                }); 

                var startDate = this.answers[0].startDate ? new Date(this.answers[0].startDate) : null;
                var startDateString = "";
                if (startDate !== null) {
                    startDateString = startDate.toISOString().slice(0, 10);
                }

                var endDate = this.answers[0].endDate ? new Date(this.answers[0].endDate) : null;
                var endDateString = "";
                if (endDate !== null) {
                    endDateString = endDate.toISOString().slice(0, 10);
                }

                inputDiv.append($("<input/>", {
                    "id": 'dateInput',
                    "class": "resizable form-control gray",
                    "type": 'date',
                    "min": startDateString,
                    "max": endDateString,
                    "onFocus": "this.showPicker()",
                    "onBlur": "Selector.selectElement(" + question.id + ", " + this.answers[0].id + ", null, null, $(this).val(), true)"
                }));

                row.append(inputLabel); 
                row.append(inputDiv); 

                questioncontent.append(row); 
                break;
                // IMAGE
            case Questiontypes.IMAGE:
                // Create a div which centers the canvas
                var canvasDiv = $("<div/>", {
                    "id": "canvasDiv",
                    "style": "text-align:center; width:100%;"
                });

                // Create a div which cointains the undo/redo buttons and the color picker
                var toolbarDiv = $("<div/>", {
                    "id": "toolbarDiv",
                    "class": "d-flex justify-content-center mb-3"
                });

                var buttonDiv = $("<div/>", {
                    "class": "d-flex"
                }); 

                var undoButton = $("<button/>", {
                    "id": "undoButton",
                    "class": "btn btn-mobile me-1",
                    "title": strings['survey.question.image.button.undo'],
                    "disabled": "true"
                });

                undoButton.append($("<span/>", {
                    "class": "bi bi-large bi-reply-fill"
                }));

                var redoButton = $("<button/>", {
                    "id": "redoButton",
                    "class": "btn btn-mobile ms-1",
                    "title": strings['survey.question.image.button.redo'],
                    "disabled": "true"
                });

                redoButton.append($("<span/>", {
                    "class": "bi bi-large bi-mirror-horizontal bi-reply-fill"
                }));

                var colorButtonGroup = $("<div/>", {
                    "class": "btn-group align-items-center ms-1 me-1 h-100"
                }); 

                colorButtonGroup.append($("<input/>", {
                    "name": "colorCheckbox",
                    "id": "checkboxBlack", 
                    "class": "btn-check",
                    "value": "#000000", 
                    "type": "radio",
                    "checked": "checked"
                })); 

                colorButtonGroup.append($("<label/>", {
                    "for": "checkboxBlack", 
                    "class": "btn btn-outline-primary h-100 d-flex align-items-center z-0",
                    "html": strings['survey.question.image.flipswitch.black']
                })); 

                colorButtonGroup.append($("<input/>", {
                    "name": "colorCheckbox",
                    "id": "checkboxWhite", 
                    "class": "btn-check",
                    "value": "#ffffff", 
                    "type": "radio",
                })); 

                colorButtonGroup.append($("<label/>", {
                    "for": "checkboxWhite", 
                    "class": "btn btn-outline-primary h-100 d-flex align-items-center z-0",
                    "html": strings['survey.question.image.flipswitch.white']
                })); 

                buttonDiv.append(undoButton); 
                buttonDiv.append(colorButtonGroup);
                buttonDiv.append(redoButton); 

                // Append all tools to the toolbar div
                toolbarDiv.append(buttonDiv); 
                // Create an empty canvas element and set its context to 2d
                var canvas = document.createElement("canvas");
                canvas.setAttribute("id", "canvas");

                this.rearrange();

                var rearrangeEvent = ()=> {
                    this.rearrange(); 
                }

                $(window).on("resize", rearrangeEvent); 

                // Enable the undo button if there are already points on th image
                if (encounter.getResponse(question.answers[0].id) !== null && encounter.getResponse(question.answers[0].id).pointsOnImage.length > 0) {
                    undoButton.removeAttr("disabled");
                }
                // Append the divs to the question content
                canvasDiv.append(canvas);
                questioncontent.append(toolbarDiv);
                questioncontent.append(canvasDiv);

                // Bind mouse actions on canvas and buttons
                canvas.addEventListener('click', function (e) {
                    var mouseClickPosition = [e.pageX - this.offsetLeft, e.pageY - this.offsetTop];
                    Selector.selectElement(question.id, question.answers[0].id, mouseClickPosition, '', '', true);
                    question.redraw(image);
                });

                document.getElementById("undoButton").onclick = function () {
                    Selector.selectElement(question.id, question.answers[0].id, 'undo', null, null, true);
                    question.redraw(image);
                };

                document.getElementById("redoButton").onclick = function () {
                    Selector.selectElement(question.id, question.answers[0].id, 'redo', null, null, true);
                    question.redraw(image);
                };
                break;
            case Questiontypes.BODY_PART:
                questioncontent.append($("<span />", {"text": strings['survey.question.bodyPart.hint'], "class": "resizable"}));
                questioncontent.append(getMinMaxAnswerText(this.minNumberAnswers, this.maxNumberAnswers, this.answers));
                //Get the imageType of this question by walking through attached images
                var imageType;
                for (var i = 0; i < this.bodyPartImages.length; i++) {
                    if (this.bodyPartImages[i] == ImagePath.FRONT) {
                        if (imageType != ImageType.BACK) {
                            imageType = ImageType.FRONT;
                        } else {
                            imageType = ImageType.FRONT_BACK;
                        }
                    }

                    if (this.bodyPartImages[i] == ImagePath.BACK) {
                        if (imageType != ImageType.FRONT) {
                            imageType = ImageType.BACK;
                        } else {
                            imageType = ImageType.FRONT_BACK;
                        }
                    }
                }

                //Create the div that contains the image element
                var imageContent;

                switch (imageType) {
                    case ImageType.FRONT:
                        imageContent = $('<div/>', {
                            "class": 'row',
                            "id": 'imageContent',
                        });
                        questioncontent.append(imageContent);

                        var frontImageContent = $('<div/>', {
                            "class": 'col-12',
                        });
                        imageContent.append(frontImageContent);

                        var frontDiv = $('<div/>', {
                            "id": 'svgDiv-' + ImageType.FRONT,
                            "class": "bodyCenter",
                        });
                        frontImageContent.append(frontDiv);

                        this.createAndScaleImageAndSvg(ImageType.FRONT, ImagePath.FRONT, images["frontImage"], frontDiv);

                        var rescaleImage = () => {
                            question.createAndScaleImageAndSvg(ImageType.FRONT, ImagePath.FRONT, images["frontImage"], frontDiv);
                        }
                        
                        //Add timeout before adding resize event because otherwise it will trigger before the image is created
                        setTimeout(function() {
                            $(window).on("resize", rescaleImage); 
                        },100)

                        break;
                    case ImageType.BACK:
                        imageContent = $('<div/>', {
                            "class": 'row',
                            "id": 'imageContent',
                        });
                        questioncontent.append(imageContent);

                        var backImageContent = $('<div/>', {
                            "class": 'col-12',
                        });
                        imageContent.append(backImageContent);

                        var backDiv = $('<div/>', {
                            "id": 'svgDiv-' + ImageType.BACK,
                            "class": "bodyCenter", 
                        });
                        backImageContent.append(backDiv);

                        this.createAndScaleImageAndSvg(ImageType.BACK, ImagePath.BACK, images["backImage"], backDiv);

                        var rescaleImage = () => {
                            question.createAndScaleImageAndSvg(ImageType.BACK, ImagePath.BACK, images["backImage"], backDiv);
                        }

                        //Add timeout before adding resize event because otherwise it will trigger before the image is created
                        setTimeout(function() {
                            $(window).on("resize", rescaleImage); 
                        },100)

                        break;
                    case ImageType.FRONT_BACK:
                        //reset image content for this image type
                        imageContent = $('<div/>', {
                            "class": 'row',
                            "id": 'imageContent',
                        });
                        questioncontent.append(imageContent);

                        var frontImageContent = $('<div/>', {
                            "class": 'col-12 col-bp-6',
                        });
                        imageContent.append(frontImageContent);

                        var backImageContent = $('<div/>', {
                            "class": 'col-12 col-bp-6',
                        });
                        imageContent.append(backImageContent);

                        var frontDiv = $('<div/>', {
                            "id": 'svgDiv-' + ImageType.FRONT,
                            "class": "bodyFront",
                        });
                        frontImageContent.append(frontDiv);

                        var backDiv = $('<div/>', {
                            "id": 'svgDiv-' + ImageType.BACK,
                            "class": "bodyBack", 
                        });
                        backImageContent.append(backDiv);

                        this.createAndScaleImageAndSvg(ImageType.FRONT, ImagePath.FRONT, images["frontImage"], frontDiv);
                        this.createAndScaleImageAndSvg(ImageType.BACK, ImagePath.BACK, images["backImage"], backDiv);

                        var rescaleImage = () => {
                            question.createAndScaleImageAndSvg(ImageType.FRONT, ImagePath.FRONT, images["frontImage"], frontDiv);
                            question.createAndScaleImageAndSvg(ImageType.BACK, ImagePath.BACK, images["backImage"], backDiv);
                        }

                        //Add timeout before adding resize event because otherwise it will trigger before the image is created
                        setTimeout(function() {
                            $(window).on("resize", rescaleImage); 
                        },100)
                        break;
                    default:
                        //place warning here that question type is not found
                        break;
                }
                imageContent.css('text-align', 'center');
                imageContent.css('padding-top', '10px');
                break;
        }

        // Set the font size
        setFontSize(fontSizeClass);

        // Div to check if the question is overflowing
        var overflowDiv = $("<div/>", {
            "id": "overflowDiv",
            "style": "float:left; height:1px; width:100%;"
        });
        questioncontent.append(overflowDiv);
    };

    /**
     * Creates the svg and the image html elements, as well as the
     * path elements for each bodyPartAnswer that belong to the question.
     * 
     * @param {type} imageType Specifies the current image.
     * @param {type} imagePath Path to the current image.
     * @param {type} imageSource The image represented as base64 string.
     * @param {type} div containing the svg whose size has to be specified.
     */
    this.createAndScaleImageAndSvg = function (imageType, imagePath, imageSource, div) {
        //create image to get height and width to size svg correctly
        try {
            var id = $("#svgDiv-" + imageType).find("svg").attr("id")
            var svg = SVG.get(id);
        } catch(e) {
            console.error(e.text); 
        }

        if (svg === undefined || svg === null) {
            var svg = SVG('svgDiv-' + imageType);
        }
        //get the question and its answers here because it's used in the image's load function
        var answers = this.answers;
        var question = this;

        //define what's to do when loading the image:
        //calculate size of svg and image element and set the path elements

        var scaleImage = (image, createNew)=> {
            var imageWidth = (imageType === "FRONT" ? 164 : 165); 
            var imageHeight = (imageType === "FRONT" ? 398 : 423); 

            var width = $("#imageContent").width() * 0.30;

            if (width < 180) {
                width = 180; 
            } else if (width > 330) {
                width = 330; 
            }

            var height; 
            var proportion; 

            proportion = imageHeight / imageWidth;
            height = width * proportion;

            div.height(height);
            div.width(width);

            //set the image size to the new values

            image.size(width, height);
            //create the group and path elements and set attributes related to each answer
            var group; 

            if (createNew === true) {
                group = svg.group();

                for (var i = 0; i < answers.length; i++) {
                    if (answers[i].bodyPartImage === imagePath) {
                        //create jQuery object
                        var answerPath = $(answers[i].bodyPartPath);
                        //create the path element with svg.js and currently created jQuery object
                        var groupPath = group.path(answerPath.attr('d'));
                        //transfer the attributes from jQuery object to svg.js element
                        groupPath.attr('id', 'answer' + answers[i].id);
                        groupPath.attr('class', answerPath.attr('class'));
                        groupPath.attr('style', answerPath.attr('style'));
                        groupPath.attr('onclick', 'Selector.selectElement(' + question.id + ',' + answers[i].id + ', null, null, null, true);');
                    }
                }
            } else {
                var id = $("#svgDiv-" + imageType).find("g").attr("id")
                group = SVG.get(id);
            }

            //scale values to adjust the path elements to the new images size
            var scaleX;
            var scaleY;
            var translateX = 0;
            if (imageWidth === 0 || navigator.userAgent.toLowerCase().match(/(ipad|iphone)/)) {
                scaleX = width / (imageType === "FRONT" ? 164 : 165);
                scaleY = height / (imageType === "FRONT" ? 398 : 423);
                if (!navigator.userAgent.toLowerCase().match(/(ipad|iphone)/)) {
                    image.scale(scaleX, scaleY);
                    image.translate(0, 0);
                } else if (navigator.userAgent.toLowerCase().match(/(ipad|iphone)/) && imageType === "BACK") {
                    scaleX = scaleY
                    translateX = 10;
                }
            } else {
                scaleX = width / imageWidth;
                scaleY = height / imageHeight;
            }
            //scale all elements the group contains so that they fit into the image
            group.scale(scaleX, scaleY);
            //this is necessary because of some reason svg library translates 
            //the path elements to some other values than (0,0) if this
            //function is never called
            group.translate(translateX, 0);

            //Select answers that has been already selected again because this load method is called asynchronously to the survey.showQuestion function
            //so if you try to do this there no path objects will be found because they won't be available then
            if (createNew === true) {
                var responses = encounter.getResponsesForQuestion(question);
                if (responses.length > 0) {
                    $.each(responses, function () {
                        var answer = question.getAnswerById(this.answerId);
                        if (answer.bodyPartImage === ImagePath.FRONT && imageType === ImageType.FRONT || answer.bodyPartImage === ImagePath.BACK && imageType === ImageType.BACK) {
                            Selector.selectElement(question.id, this.answerId, null, null, null, false);
                        }
                    });
                }
            }
        }


        try {
            var id = $("#svgDiv-" + imageType).find("image").attr("id")
            var image = SVG.get(id);
        } catch(e) {
            console.error(e.text); 
        }

        //Check if the image already exists, otherwise create it 
        if (image === null || image === undefined) {
            svg.image(imageSource).loaded(function (loader) {
                scaleImage(this, true); 
            });
        } else {
            scaleImage(image, false); 
        }
        

        return svg;
    };

    /**
     * Checks whether the given responses for this questions are valid or not.
     * 
     * @param {Response} responses The responses given for this question.
     * @returns {Boolean} True if the given responses are valid, otherwise false.
     */
    this.isResponseValid = function (responses) {
        if (typeof responses[0] === 'undefined') {
            return false;
        }

        switch (this.questionType) {
            case Questiontypes.BODY_PART:
            case Questiontypes.MULTIPLE_CHOICE:
                var minimumResponses = this.minNumberAnswers;
                var maximumResponses = this.maxNumberAnswers;
                
                isOtherAnswer = 0;
                // Check whether the question has an isOther option
                $.each(this.answers, function (index, answer) {
                    if (answer.isOther === true){
                        isOtherAnswer = 1;
                        return false;
                    }
                })
                // Check if the freetext answer is given or not
                if (isOtherAnswer === 1){
                    $.each(this.answers, function (index, answer) {
                        if (answer.localizedLabel === null && encounter.getResponse(answer.id) === null ){
                            isOtherAnswer = 0;
                            return false;
                        }
                    })
                }
                if (typeof responses[0].length === 'undefined') {
                    // Minimum and maximum number of responses should never be null
                    // Check just to be safe
                    if (minimumResponses === null && maximumResponses === null && responses.length > 0) {
                        return true;
                    }
                    // If responses array matches the criterias for the minimum and 
                    // maximum number of responses, the question is complete
                    // The freetext answer has to be substracted from the amount of given responses
                    if (minimumResponses !== null && maximumResponses !== null && responses.length - isOtherAnswer >= minimumResponses && responses.length - isOtherAnswer <= maximumResponses) {
                        return true;
                    }
                    return false;
                }
                break;
            case Questiontypes.SLIDER:
            case Questiontypes.NUMBER_CHECKBOX:
            case Questiontypes.NUMBER_CHECKBOX_TEXT:

                if (responses[0].value >= this.answers[0].minValue && responses[0].value <= this.answers[0].maxValue) {
                    return true;
                } else {
                    return false;
                }
                break;
            case Questiontypes.NUMBER_INPUT:
                var minimumCheck = true;
                var maximumCheck = true;
                if (responses[0].value !== null && responses[0].value !== "") {
                    if (this.answers[0].minValue !== 'undefined' && this.answers[0].minValue !== null) {
                        minimumCheck = (responses[0].value >= this.answers[0].minValue);
                    }
                    if (this.answers[0].maxValue !== 'undefined' && this.answers[0].maxValue !== null) {
                        maximumCheck = (responses[0].value <= this.answers[0].maxValue);
                    }
                    if (minimumCheck === true && maximumCheck === true) {
                        return true;
                    } else {
                        return false;
                    }
                } else {
                    return false;
                }
            case Questiontypes.DATE:
                if (responses[0].date === "") {
                    return false;
                }
                var responseParts = responses[0].date.split('-');
                // new Date(year, month [, day [, hours[, minutes[, seconds[, ms]]]]])
                // months are zero-based
                var responseDate = new Date(responseParts[0], responseParts[1] - 1, responseParts[2], 0, 0, 0, 0);
                var minimumCheck = true;
                var maximumCheck = true;
                if (this.answers[0].startDate !== 'undefined' && this.answers[0].startDate !== null) {
                    var startDateParts = this.answers[0].startDate.split('-');
                    var startDate = new Date(startDateParts[0], startDateParts[1] - 1, startDateParts[2], 0, 0, 0, 0);
                    minimumCheck = (responseDate.getTime() >= startDate.getTime());
                }
                if (this.answers[0].endDate !== 'undefined' && this.answers[0].endDate !== null) {
                    var endDateParts = this.answers[0].endDate.split('-');
                    var endDate = new Date(endDateParts[0], endDateParts[1] - 1, endDateParts[2], 0, 0, 0, 0);
                    maximumCheck = (responseDate.getTime() <= endDate.getTime());
                }
                if (minimumCheck === true && maximumCheck === true) {
                    return true;
                } else {
                    return false;
                }
                break;
            case Questiontypes.FREE_TEXT:
                if (responses[0].customtext !== null && responses[0].customtext !== "") {
                    return true;
                } else {
                    return false;
                }
                break;
            case Questiontypes.IMAGE:
                if (responses[0].currentPointsPosition !== -1) {
                    return true;
                } else {
                    return false;
                }
                break;
            default:
                return true;
                break;
        }
    };

    /**
     * Function that adds numbered checkboxes to the questioncontent
     * for a given question
     * @param {*} question for which the checkboxes should be created
     * @param {*} questioncontent to which the checkboxes should be added 
     */
    function createNumberCheckboxes(question, questioncontent) {
        //Handle vertical and horizontal checkboxes completely separately
        if (question.answers[0].vertical === true) {
            questioncontent.append(handleVerticalCheckboxes(question)); 
        } else {
            questioncontent.append(handleHorizontalCheckboxes(question)); 
        }
    }

    /**
     * Creates a new checkbox div with a vertical button group 
     * Handles the min and max text, if the question has these values
     * places the text above and below the button group  
     * @param {*} question element 
     * @returns numberedCheckboxes div
     */
    function handleVerticalCheckboxes(question) {
        // Cretae the minimum und maximum text
        var localizedMinText = question.answers[0].localizedMinimumText[encounter.bundleLanguage];
        var localizedMaxText = question.answers[0].localizedMaximumText[encounter.bundleLanguage];
        var hasMinMaxText = typeof localizedMinText !== 'undefined' && localizedMinText !== "" || typeof localizedMaxText !== 'undefined' && localizedMaxText !== "";

        var checkboxDiv = $("<div/>", {
            "id": "numberedCheckboxes"
        }); 

        //Add text above checkboxes if available 
        if (hasMinMaxText) {
            var textTop = $("<div/>", {
                "id": "textTop",
                "class": "d-flex align-content-center align-self-center justify-content-center mb-2", 
            }); 

            textTop.append($("<span/>", {
                "class": "resizable resizable-0"
            }).html(localizedMinText)); 
            
            checkboxDiv.append(textTop); 
        }

        // Flex div so that elements are centered
        var flexDiv = $("<div/>", {
            "class": "d-flex justify-content-center align-items-center"
        });

        //item div for the button group
        var itemDiv = $("<div/>", {
            "id": "items",
            "class": "d-flex align-content-center justify-content-center"
        }); 

        //vertical button group element
        var buttonGroup = $("<div/>", {
            "class": "btn-group-vertical flex-wrap w-100"
        });
        
        //Add all input elements
        for (
            let i = Number(question.answers[0].minValue); 
            i <= question.answers[0].maxValue; 
            i += Number(question.answers[0].stepsize)
        ) {
            var inputElement = $("<input/>", {
                "name": "numberedCheckboxes", 
                "id": "numberedCheckbox_" + i, 
                "class": "btn-check",
                "value": i, 
                "type": "radio",
            }); 
    
            //Event triggers checkbox toggle and saves encounter response
            inputElement[0].addEventListener("click", function() {
                Selector.selectElement(question.id ,question.answers[0].id, i, $('textarea').val(), '', true);
            });
    
            buttonGroup.append(inputElement); 
    
            buttonGroup.append($("<label/>", {
                "for": "numberedCheckbox_" + i,
                "class": "col-auto btn-outline-primary btn numberedCheckbox", 
            }).html(i))
        }

        itemDiv.append(buttonGroup); 
        flexDiv.append(itemDiv); 
        checkboxDiv.append(flexDiv); 

        //Add text below checkboxes if available 
        if (hasMinMaxText) {
            var textBottom = $("<div/>", {
                "id": "textBottom",
                "class": "d-flex align-content-center align-self-center justify-content-center mt-2", 
            }); 

            textBottom.append($("<span/>", {
                "class": "resizable resizable-0"
            }).html(localizedMaxText));
            
            checkboxDiv.append(textBottom);
        }
        return checkboxDiv; 
    }

    /**
     * Creates a new checkbox div with a horizontal button group 
     * Handles the min and max text, if question has values
     * Adds text above and below button group for mobile view and
     * left and right for normal view
     * @param {*} question element
     * @returns numberedCheckboxes div
     */
    function handleHorizontalCheckboxes(question) {
        // Cretae the minimum und maximum text
        var localizedMinText = question.answers[0].localizedMinimumText[encounter.bundleLanguage];
        var localizedMaxText = question.answers[0].localizedMaximumText[encounter.bundleLanguage];
        var hasMinMaxText = typeof localizedMinText !== 'undefined' && localizedMinText !== "" || typeof localizedMaxText !== 'undefined' && localizedMaxText !== "";

        var checkboxDiv = $("<div/>", {
            "id": "numberedCheckboxes"
        });

        // Append text above button group for mobile view
        if (hasMinMaxText) {
            var mobileTextTop = $("<div/>", {
                "class": "d-flex d-bp-none align-content-center align-self-start justify-content-start mb-2"
            });

            mobileTextTop.append($("<span/>", {
                "class": "resizable resizable-0",
            }).html(localizedMinText)); 

            checkboxDiv.append(mobileTextTop); 
        }

        // Flexbox to justify element between 
        var flexDiv = $("<div/>", {
            "class": "d-flex justify-content-between align-items-center"
        }); 

        //Add text left, if available
        if (hasMinMaxText) {
            //Append text left of the button group for normal view
            var leftText = $("<div/>", {
                "class": "d-none d-bp-flex align-content-center align-self-start justify-content-start me-2",
                "id": "textLeft"
            }); 

            leftText.append($("<span/>", {
                "class": "resizable resizable-0"
            }).html(localizedMinText));

            flexDiv.append(leftText); 
        } else {
            //Append empty div to center button group if no text is present
            flexDiv.append($("<div/>", {
                "class": "d-none d-bp-flex"
            }));
        }

        //Append empty div for mobile view to center button group 
        flexDiv.append($("<div/>", {
            "class": "d-flex d-bp-none"
        }))


        var itemsDiv = $("<div/>", {
            "class": "d-flex align-content-center justify-content-center", 
        }); 

        //Horizontal button group 
        var buttonGroup = $("<div/>", {
            "class": "btn-group flex-wrap w-100"
        })

        //Add all checkboxes
        for (
            let i = Number(question.answers[0].minValue); 
            i <= question.answers[0].maxValue; 
            i += Number(question.answers[0].stepsize)
        ) {
            var inputElement = $("<input/>", {
                "name": "numberedCheckboxes", 
                "id": "numberedCheckbox_" + i, 
                "class": "btn-check",
                "value": i, 
                "type": "radio",
            }); 
    
            //Event triggers checkbox toggle and saves encounter response
            inputElement[0].addEventListener("click", function() {
                Selector.selectElement(question.id ,question.answers[0].id, i, $('textarea').val(), '', true);
            });
    
            buttonGroup.append(inputElement); 
    
            var labelElement = $("<label/>", {
                "for": "numberedCheckbox_" + i,
                "class": "col-auto btn-outline-primary btn numberedCheckbox my-2", 
            }).html(i)

            setMaxWidthForNumberedCheckboxLabel(question.answers[0].maxValue, labelElement); 

            buttonGroup.append(labelElement)
        }

        itemsDiv.append(buttonGroup); 
        flexDiv.append(itemsDiv); 

        //Add text right, if available
        if (hasMinMaxText) {
            //Append text right of the button group for normal view
            var textRight = $("<div/>", {
                "id": "textRight",
                "class": "d-none d-bp-flex align-content-center align-self-end justify-content-end ms-2"
            }); 

            textRight.append($("<span/>", {
                "class": "resizable resizable-0"
            }).html(localizedMaxText)); 

            flexDiv.append(textRight); 
        } else {
            //Append empty div to center button group if no text is present
            flexDiv.append($("<div/>", {
                "class": "d-none d-bp-flex"
            })); 
        }

        //Append empty div for mobile view to center button group 
        flexDiv.append($("<div/>", {
            "class": "d-flex d-bp-none"
        }))

        checkboxDiv.append(flexDiv); 

        //Append text below button group for mobile view
        if (hasMinMaxText) {
            var mobileTextBottom = $("<div/>", {
                "class": "d-flex d-bp-none align-content-center align-self-end justify-content-end mt-2"
            }); 

            mobileTextBottom.append($("<span/>", {
                "class": "resizable resizable-0"
            }).html(localizedMaxText));

            checkboxDiv.append(mobileTextBottom); 
        }

        return checkboxDiv; 
    }

    /**
     * Computes the width for the numbered checkboxes by
     * using the maxValue and sets the style attribute of
     * the label element accordingly
     * @param {*} maxValue of the question
     * @param {*} labelElement to apply the max width to
     */
    function setMaxWidthForNumberedCheckboxLabel(maxValue, labelElement) {
        var width = String(maxValue).length * 15; //15px per digit 
        $(labelElement).css("max-width", width + "px"); 
    };

    /** 
     * Trigger all conditions of responses given for this question
     */
    this.handleConditions = function () {
        var responses = encounter.getResponsesForQuestion(this);
        for (var i = 0; i < responses.length; i++) {
            switch (this.questionType) {
                case "MULTIPLE_CHOICE":
                case "SLIDER":
                case "NUMBER_CHECKBOX":
                case "NUMBER_CHECKBOX_TEXT":
                    Selector.handleConditions(this, this.getAnswerById(responses[i].answerId), responses[i].value, true);
                    break;
            }
        }
    };

    /**
     * Checks if the question contains an active answer.
     * 
     * @returns {Boolean} True, if there's at least one active answer, otherwise returns false.
     */
    this.hasActiveAnswer = function () {
        if (this.questionType === "INFO_TEXT") {
            return true;
        }
        for (var i = 0; i < this.answers.length; i++) {
            if (this.answers[i].isEnabled > 0) {
                return true;
            }
        }
        return false;
    };


    this.rearrange = function () {
        var question = this;
        switch (question.questionType) {
            case Questiontypes.IMAGE:
                // Create an image
                image = new Image();

                // Bind the onload function, which resizes the canvas, to the image
                image.onload = function () {
                    var optimalWidth = image.naturalWidth;
                    var optimalHeight = image.naturalHeight;

                    // Set the maximum canvas size
                    var availableWidth = $('#canvasDiv').width();
                    var availableHeight = window.innerHeight - $('.header').height() - $('.footer').height() - $('#toolbarDiv').height() - 50;

                    // If the available size is smaller than the optimal size recalculate the optimal size depending on the images' proportion
                    if (availableWidth < optimalWidth) {
                        var proportion = optimalHeight / optimalWidth;
                        optimalWidth = availableWidth;
                        optimalHeight = optimalWidth * proportion;
                    }
                    // Only recalculate the optimal height, when it's not used on mobile devices
                    if (availableHeight < optimalHeight && isMobile.matches === false) {
                        var proportion = optimalWidth / optimalHeight;
                        optimalHeight = availableHeight;
                        optimalWidth = optimalHeight * proportion;
                    }

                    $('#canvas')[0].width = optimalWidth;
                    $('#canvas')[0].height = optimalHeight;

                    // Call the redraw function to draw the image and if applicable its points
                    question.redraw(this);
                };

                // Set the image source after the onload function to get sure that it's fired
                image.src = this.answers[0].imageBase64;

                break;
        }
    };

    this.redraw = function (image) {
        // Get the context from the existing canvas
        var context = $('#canvas')[0].getContext("2d");
        // Clear the canvas
        context.clearRect(0, 0, context.canvas.width, context.canvas.height);
        // Draw the image
        context.drawImage(image, 0, 0, image.width, image.height, 0, 0, $('#canvas')[0].width, $('#canvas')[0].height);
        // Set the linetype and linewidth
        context.lineJoin = "round";
        context.lineWidth = 2;

        // If the encounter has already a response
        if (encounter.getResponse(this.answers[0].id) !== null) {
            // Draw every point that must be shown
            for (var i = 0; i <= encounter.getResponse(this.answers[0].id).currentPointsPosition; i++) {
                // Get the current point and calculate its x and y coordinates depending on the canvas size
                var currentPoint = encounter.getResponse(this.answers[0].id).pointsOnImage[i];
                var xCoordinate = currentPoint.xCoordinate * $('#canvas')[0].width;
                var yCoordinate = currentPoint.yCoordinate * $('#canvas')[0].height;

                // Set the right color of the current point
                context.strokeStyle = currentPoint.color;

                // Draw an X on the calculated point
                context.beginPath();
                context.moveTo(xCoordinate - 5, yCoordinate - 5);
                context.lineTo(xCoordinate + 5, yCoordinate + 5);
                context.moveTo(xCoordinate + 5, yCoordinate - 5);
                context.lineTo(xCoordinate - 5, yCoordinate + 5);
                context.closePath();
                context.stroke();
            }
        }
    };

    this.removeInvalidAnswers = function () {
        if (this.questionType === Questiontypes.IMAGE) {
            if (encounter.getResponse(this.answers[0].id) !== null) {
                while (encounter.getResponse(this.answers[0].id).currentPointsPosition < encounter.getResponse(this.answers[0].id).pointsOnImage.length - 1) {
                    encounter.getResponse(this.answers[0].id).pointsOnImage.pop();
                }
            }
        }
    };

    /**
     * Function that gets triggered on any "input" of the range slider
     * Queries the currend width of the slider thumb and computes the correct position for 
     * the value div with the magic equation 
     * Sets the value for the value div as well as to the hidden data binding input
     */
    this.setSliderValue = function() {
        let range = document.getElementById('range');
        if (range !== null && range !== undefined) {
            let rangeV = document.getElementById('rangeV');
            let bindedRange = document.getElementById('sliderInput');
            let totalInputWidth = range.offsetWidth;
            let sliderWidth = document.querySelector(".sliderWidth");
            let thumbHalfWidth = Number(getComputedStyle(sliderWidth).width.replace("px", "")) / 2;

            let leftValue = (((range.valueAsNumber - range.min) / (range.max - range.min)) * ((totalInputWidth - thumbHalfWidth) - thumbHalfWidth)) + thumbHalfWidth;

            rangeV.innerHTML = `<span>${range.value}</span>`;
            bindedRange.value = range.value;
            $(rangeV).css("left", leftValue);
        }
    };

    // Toggles the visibility of the value div
    this.toggleSliderValueOntop = function(showElement) {
        if (showElement === true) {
            $("#rangeV").removeClass("hideValue");
            $("#range").removeClass("hiddenThumb");
            $("#range").removeClass("unselected");
        } else {
            $("#rangeV").addClass("hideValue"); 
            $("#range").addClass("unselected");
        }
    }
    this.toggleSliderMargin = function(showTooltip, showElement) {
            if (showTooltip === true) {
                if (showElement === true) {
                    $(".rangeWrap").addClass("tooltipMargin");
                } else {
                    $(".rangeWrap").removeClass("tooltipMargin");
                }
            }
        }
}

Question.replaceDotsForValidHtmlId = function (i) {
    var idPostfix = i.toString();
    // If the value is decimal, replace the dot, because id's must not contains dots
    if (i.toString().indexOf(".") >= 0) {
        idPostfix = i.toString().replace(".", "_");
    }
    return idPostfix;
};

/* Get the enable status of the current question, check if there are some available answers or not */
Question.getIsEnabled = function () {
    enableQuestion = false;
    for (var i = 0; i < this.answers.length; i++) {
        var answer = this.answers[i];
        if (answer.isEnabled > 0) {
            enableQuestion = true;
            break;
        }
    }
    if (enableQuestion === true && this.isEnabled > 0) {
        return true;
    } else {
        return false;
    }
};