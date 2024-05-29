/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.imi.mopat.model.enumeration;

import java.util.HashMap;
import java.util.Map;

/**
 * Definition of different FHIR extension usable in MoPat. These extensions define constraints and
 * special properties for questionnaires, questions and answers that don't cover the usual cases of
 * FHIR resources.
 */
public enum FHIRExtensionType {
    MIN_VALUE(
        "http://hl7.org/fhir/StructureDefinition/minValue"), //max value a numeric question accepts
    MAX_VALUE(
        "http://hl7.org/fhir/StructureDefinition/maxValue"), //min value a numeric question accepts
    MIN_NUMBER_ANSWER("http://hl7.org/fhir/StructureDefinition/questionnaire"
        + "-minOccurs"),      //min number of answers
    // that have to be chosen
    MAX_NUMBER_ANSWER("http://hl7.org/fhir/StructureDefinition/questionnaire"
        + "-maxOccurs"),      //max number of answers
    // that have to be chosen
    SCORE(
        "http://hl7.org/fhir/StructureDefinition/questionnaire-ordinalValue"),            //score for a single option
    MIN_MAX_TEXT("http://hl7.org/fhir/StructureDefinition/questionnaire"
        + "-optionPrefix"),     //text that is shown at
    // min/max of number checkbox or slide answer
    MAX_DECIMAL_PLACES(
        "http://hl7.org/fhir/StructureDefinition" + "/maxDecimalPlaces"),         //maximum
    // number of decimal places a number input answer accepts
    TRANSLATION(
        "http://hl7.org/fhir/StructureDefinition/translation");                     //Locale for language code
    private final String textValue;
    private static final Map<String, FHIRExtensionType> stringToEnum = new HashMap<>();

    static // Initialize map from constant name to enum constant
    {
        for (FHIRExtensionType cValue : values()) {
            stringToEnum.put(cValue.toString(), cValue);
        }
    }

    FHIRExtensionType(final String textValue) {
        this.textValue = textValue;
    }

    @Override
    public String toString() {
        return textValue;
    }

    public String getTextValue() {
        return textValue;
    }

    public static FHIRExtensionType fromString(final String textValue) {
        return stringToEnum.get(textValue);
    }
}
