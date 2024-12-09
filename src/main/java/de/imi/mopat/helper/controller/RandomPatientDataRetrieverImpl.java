package de.imi.mopat.helper.controller;

import de.imi.mopat.model.Clinic;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.slf4j.Logger;
import de.imi.mopat.model.dto.EncounterDTO;
import de.imi.mopat.model.enumeration.Gender;

/**
 * Retrieves dummy patient data configured in the mopat configuration. If a property's value is
 * 'null', a <code>null</code> value will be used. If properties are missing, it retrieves fixed
 * dummy data.
 *
 * @version 1.0
 */
public class RandomPatientDataRetrieverImpl extends PatientDataRetriever {

    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(
        RandomPatientDataRetrieverImpl.class);

    private final List<EncounterDTO> encounterDTOs = new ArrayList<>();
    private final Random random = new Random();
    private static final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy");

    private static final String[] FIRSTNAMES = {"Selina", "Tony", "Bruce", "Bugs", "Clark",
        "Wonder", "Indiana", "James T.", "Alan", "Edsger" + " W.", "Marie", "Roger", "Serena",
        "Timo", "Peter", "Micky", "Chuck", "Rob", "Mary"};
    private static final String[] LASTNAMES = {"Kyle", "Stark", "Wayne", "Bunny", "Kent", "Woman",
        "Jones", "Kirk", "Turing", "Dijkstra", "Curie", "Federer", "Williams", "Boll", "Pan",
        "Maus", "Norris", "Cole", "Poppins"};
    private static final String[] BIRTHDATES = {"14-03-1976", "29-05-1970", "19-02-1939",
        "27-07-1940", "19-02-1938", "22-03-1941", "01-07" + "-1899", "22-03-2233", "23-06-1912",
        "11-05-1930", "07-11-1867", "08-08-1981", "26-09-1981", "08-03-1981", "10-01-1902",
        "18-11" + "-1928", "10-03-1940", "30-01-1019", "15-06-1934"};
    private static final Gender[] GENDERS = {Gender.FEMALE, Gender.MALE, Gender.MALE, Gender.MALE,
        Gender.MALE, Gender.FEMALE, Gender.MALE, Gender.MALE, Gender.MALE, Gender.MALE,
        Gender.FEMALE, Gender.MALE, Gender.FEMALE, Gender.MALE, Gender.MALE, Gender.MALE,
        Gender.MALE, Gender.MALE, Gender.FEMALE};
    private static final Long[] PATIENT_IDS = {1L, 1010101L, 42L, 3118181520L, 19211651813114L,
        2342L, 4711L, 1701L, 51497131L, 1960L, 92L, 17L, 5734L, 435843L, 1902L, 1423L, 4312L,
        5887634L, 4466886644L};

    public RandomPatientDataRetrieverImpl() {

        for (int i = 0; i < FIRSTNAMES.length; i++) {
            EncounterDTO encounterDTO = new EncounterDTO();
            encounterDTO.setFirstname(FIRSTNAMES[i]);
            encounterDTO.setLastname(LASTNAMES[i]);
            try {
                encounterDTO.setBirthdate(
                    new java.sql.Date(simpleDateFormat.parse(BIRTHDATES[i]).getTime()));
            } catch (ParseException e) {
                encounterDTO.setBirthdate(new java.sql.Date(System.currentTimeMillis()));
            }
            encounterDTO.setGender(GENDERS[i]);
            encounterDTO.setPatientID(PATIENT_IDS[i]);
            encounterDTOs.add(encounterDTO);
        }
    }

    @Override
    public EncounterDTO retrievePatientData(final Clinic clinic,final String caseNumber) {
        LOGGER.debug("Case number is: {}", caseNumber);
        assert caseNumber != null : "The given caseNumber was null";

        EncounterDTO result = new EncounterDTO();
        result.setCaseNumber(caseNumber);

        EncounterDTO encounterDTO = encounterDTOs.get(random.nextInt(encounterDTOs.size()));

        result.setFirstname(encounterDTO.getFirstname());
        result.setLastname(encounterDTO.getLastname());
        result.setBirthdate(encounterDTO.getBirthdate());
        result.setGender(encounterDTO.getGender());
        result.setPatientID(encounterDTO.getPatientID());

        return result;
    }
}
