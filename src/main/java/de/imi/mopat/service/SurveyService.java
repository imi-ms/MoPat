package de.imi.mopat.service;

import de.imi.mopat.dao.BundleDao;
import de.imi.mopat.dao.EncounterDao;
import de.imi.mopat.helper.controller.LocaleHelper;
import de.imi.mopat.helper.model.BundleDTOMapper;
import de.imi.mopat.helper.model.EncounterDTOMapper;
import de.imi.mopat.model.Bundle;
import de.imi.mopat.model.Encounter;
import de.imi.mopat.model.dto.BundleDTO;
import de.imi.mopat.model.dto.EncounterDTO;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

@Service
public class SurveyService {

    @Autowired
    private BundleDao bundleDao;

    @Autowired
    private BundleDTOMapper bundleDTOMapper;

    @Autowired
    private EncounterDao encounterDao;

    @Autowired
    private EncounterDTOMapper encounterDTOMapper;

    /**
     * Builds a map of published, clinic-associated bundles to their incomplete encounters grouped
     * by language for the case referenced by the given encounter.
     *
     * <p>Only bundles that are published and assigned to at least one clinic are included.
     * Incomplete encounters are added only if their bundle is present in the resulting map.</p>
     *
     * @param encounterDTO encounter containing the case number used to load incomplete encounters
     * @return sorted map of bundles to language-specific lists of incomplete encounters
     */
    public SortedMap<BundleDTO, Map<String, List<EncounterDTO>>> getBundleLanguageEncounterMap(
        EncounterDTO encounterDTO
    ) {
        SortedMap<BundleDTO, Map<String, List<EncounterDTO>>> encountersByBundleAndLanguage = new TreeMap<>(
            (o1, o2) -> o1.getName().compareToIgnoreCase(o2.getName())
        );

        addPublishedAndAssignedBundlesToMap(encountersByBundleAndLanguage);
        addIncompleteEncountersForCaseToMap(encounterDTO.getCaseNumber(),
            encountersByBundleAndLanguage);

        return encountersByBundleAndLanguage;
    }

    /**
     * Returns whether the encounter identified by the given UUID is not available for further processing.
     *
     * <p>An encounter is treated as unavailable if the UUID is missing, no encounter exists for it,
     * the encounter is already completed, or no scheduled encounter is assigned.</p>
     *
     * @param uuid UUID of the encounter to validate
     * @return {@code true} if the encounter is missing, completed, or otherwise unavailable
     */
    public boolean isEncounterForUUIDCompletedOrUnavailable(String uuid) {
        if (uuid == null || uuid.isEmpty()) {
            return true;
        }

        Encounter encounter = encounterDao.getElementByUUID(uuid);
        return isCompletedOrUnavailable(encounter);
    }

    /**
     * Returns whether the given encounter is completed or cannot be used.
     *
     * @param encounter encounter to check
     * @return {@code true} if the encounter is {@code null}, already ended,
     * or has no scheduled encounter assigned
     */
    public boolean isCompletedOrUnavailable(Encounter encounter) {
        return encounter == null
            || encounter.getEndTime() != null
            || encounter.getEncounterScheduled() == null;
    }

    /**
     * Loads the encounter identified by the given UUID and maps it to an {@link EncounterDTO}.
     *
     * @param uuid UUID of the encounter to load
     * @return mapped encounter DTO
     */
    public EncounterDTO getEncounterDTOForUUID(String uuid) {
        return encounterDTOMapper.apply(true, encounterDao.getElementByUUID(uuid));
    }

    /**
     * Sets and persists the start time of the given encounter if it is accessed for the first time.
     *
     * <p>An encounter is considered to be accessed for the first time if it is not completed
     * and no question has been seen yet.</p>
     *
     * @param encounterDTO encounter to initialize
     */
    public void startEncounterIfFirstAccess(EncounterDTO encounterDTO) {
        if (!isFirstAccess(encounterDTO)) {
            return;
        }

        Timestamp startTime = new Timestamp(new Date().getTime());
        encounterDTO.setStartTime(startTime);

        Encounter encounter = encounterDao.getElementById(encounterDTO.getId());
        encounter.setStartTime(startTime);
        encounterDao.merge(encounter);
    }

    /**
     * Returns whether the given encounter is being accessed for the first time.
     *
     * @param encounterDTO encounter to check
     * @return {@code true} if the encounter is not completed and has no last seen question
     */
    private boolean isFirstAccess(EncounterDTO encounterDTO) {
        return encounterDTO.getEndTime() == null
            && encounterDTO.getLastSeenQuestionId() == null;
    }

    /**
     * Returns whether the language selection step can be skipped for the given encounter.
     *
     * <p>The selection is skipped if the encounter is resumed or if the bundle provides only one
     * available language.</p>
     *
     * @param encounterDTO encounter to evaluate
     * @return {@code true} if language selection is not needed
     */
    public boolean shouldSkipLanguageSelection(EncounterDTO encounterDTO) {
        return encounterDTO.getLastSeenQuestionId() != null
            || hasOnlyOneAvailableLanguage(encounterDTO);
    }

    /**
     * Returns whether the bundle of the given encounter provides exactly one available language.
     *
     * @param encounterDTO encounter whose bundle is checked
     * @return {@code true} if only one language is available
     */
    private boolean hasOnlyOneAvailableLanguage(EncounterDTO encounterDTO) {
        return encounterDTO.getBundleDTO().getAvailableLanguages().size() == 1;
    }

    /**
     * Returns the bundle language of the given encounter, initializing it if necessary.
     *
     * <p>If no bundle language is set yet, a matching survey locale is determined and assigned
     * to the encounter DTO.</p>
     *
     * @param encounterDTO encounter whose bundle language is resolved
     * @return resolved bundle language
     */
    public String resolveBundleLanguage(EncounterDTO encounterDTO) {
        if (encounterDTO.getBundleLanguage() != null) {
            return encounterDTO.getBundleLanguage();
        }

        String resolvedLanguage = determineBundleLanguage(encounterDTO);
        encounterDTO.setBundleLanguage(resolvedLanguage);
        return resolvedLanguage;
    }

    /**
     * Determines the most suitable bundle language for the given encounter.
     *
     * <p>The first available bundle language is matched against the survey locales.
     * If no match is found, the current request locale is used as fallback.</p>
     *
     * @param encounterDTO encounter whose bundle language should be determined
     * @return resolved language code
     */
    private String determineBundleLanguage(EncounterDTO encounterDTO) {
        String configuredLanguage = encounterDTO.getBundleDTO().getAvailableLanguages().get(0);

        for (String locale : LocaleHelper.getLocalesUsedInSurvey()) {
            if (matchesLanguage(locale, configuredLanguage)) {
                return locale;
            }
        }

        return LocaleContextHolder.getLocale().toString();
    }

    /**
     * Returns whether the given locale matches the specified language.
     *
     * <p>A match is assumed if the locale string contains the full language code
     * or its two-character prefix.</p>
     *
     * @param locale locale string to check
     * @param language language code to match against
     * @return {@code true} if the locale matches the language
     */
    private boolean matchesLanguage(String locale, String language) {
        return locale.contains(language)
            || locale.contains(language.substring(0, 2));
    }

    /**
     * Adds all published bundles with at least one assigned clinic to the given map.
     *
     * <p>Each matching bundle is inserted with an empty language-to-encounters map as its
     * value.</p>
     *
     * @param encountersByBundleAndLanguage target map to populate with eligible bundles
     */
    private void addPublishedAndAssignedBundlesToMap(
        SortedMap<BundleDTO, Map<String, List<EncounterDTO>>> encountersByBundleAndLanguage
    ) {
        for (Bundle bundle : bundleDao.getAllElements()) {
            if (bundle.getIsPublished() && !bundle.getBundleClinics().isEmpty()) {
                encountersByBundleAndLanguage.put(
                    bundleDTOMapper.apply(false, bundle),
                    new HashMap<>());
            }
        }
    }

    /**
     * Loads all incomplete encounters for the case referenced case id and adds them to the provided
     * bundle/language map.
     *
     * <p>Each incomplete encounter is inserted only if its bundle is already present
     * in the target map.</p>
     *
     * @param caseNumber                    caseNumber to fetch encounters for
     * @param encountersByBundleAndLanguage target map to enrich with incomplete encounters
     */
    private void addIncompleteEncountersForCaseToMap(
        String caseNumber,
        SortedMap<BundleDTO, Map<String, List<EncounterDTO>>> encountersByBundleAndLanguage
    ) {
        List<Encounter> incompleteEncounters =
            encounterDao.getIncompleteEncounters(caseNumber);

        for (Encounter incompleteEncounter : incompleteEncounters) {
            addIncompleteEncounterToMap(encountersByBundleAndLanguage, incompleteEncounter);
        }
    }

    /**
     * Adds an incomplete encounter to the existing bundle/language map when the related bundle is
     * already present in the map.
     *
     * <p>If the bundle is missing, the encounter is ignored. This typically means the bundle
     * is not visible in the current context.</p>
     *
     * @param encountersByBundleAndLanguage map of bundles to their language-specific encounters
     * @param incompleteEncounter           the incomplete encounter to insert
     */
    private void addIncompleteEncounterToMap(
        SortedMap<BundleDTO, Map<String, List<EncounterDTO>>> encountersByBundleAndLanguage,
        Encounter incompleteEncounter
    ) {
        BundleDTO bundle = bundleDTOMapper.apply(false, incompleteEncounter.getBundle());
        Map<String, List<EncounterDTO>> encountersByLanguage = encountersByBundleAndLanguage.get(
            bundle);

        if (encountersByLanguage == null) {
            return;
        }

        String language = incompleteEncounter.getBundleLanguage();
        EncounterDTO encounter = encounterDTOMapper.apply(true, incompleteEncounter);

        encountersByLanguage
            .computeIfAbsent(language, key -> new ArrayList<>())
            .add(encounter);
    }
}
