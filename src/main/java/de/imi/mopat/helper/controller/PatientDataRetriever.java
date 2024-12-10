package de.imi.mopat.helper.controller;

import de.imi.mopat.model.Clinic;
import de.imi.mopat.model.dto.EncounterDTO;

/**
 * An interface to give the possibility to retrieve patient data, such as a patient's first name,
 * last name, patient ID and date of birth from the/a local HIS (component). Implementations of this
 * interface can vary between the type of protocol and system approached to retrieve these patient
 * data.
 * <p>
 * IMPORTANT: The annotation @Autowired is not allowed for PatientDataRetriever. Autowired fields
 * will be null after saving a new configuration in the administration gui.
 *
 * @version 1.0
 */
public abstract class PatientDataRetriever {

    /**
     * @param caseNumber the patient's case number to look up. Must not be
     *                   <code>null</code>. Will be trimmed
     * @return <code>null</code> if no patient data could be retrieved.
     * Otherwise: an {@link EncounterDTO} object filled with as many fields as possible. A calling
     * method should check each of the {@link EncounterDTO EncounterDTO's} field for
     * <code>null</code> values before using them.
     */
    public abstract EncounterDTO retrievePatientData(Clinic clinic, String caseNumber);
}
