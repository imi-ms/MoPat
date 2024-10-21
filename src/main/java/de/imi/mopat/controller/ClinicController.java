package de.imi.mopat.controller;

import de.imi.mopat.dao.BundleDao;
import de.imi.mopat.dao.ClinicConfigurationDao;
import de.imi.mopat.dao.ClinicConfigurationMappingDao;
import de.imi.mopat.dao.ClinicDao;
import de.imi.mopat.dao.user.AclClassDao;
import de.imi.mopat.dao.user.AclEntryDao;
import de.imi.mopat.dao.user.AclObjectIdentityDao;
import de.imi.mopat.dao.user.UserDao;
import de.imi.mopat.helper.controller.BundleService;
import de.imi.mopat.helper.controller.ClinicConfigurationMappingService;
import de.imi.mopat.helper.controller.ClinicConfigurationService;
import de.imi.mopat.helper.controller.ClinicService;
import de.imi.mopat.model.*;
import de.imi.mopat.model.dto.*;
import de.imi.mopat.model.user.AclEntry;
import de.imi.mopat.model.user.User;
import de.imi.mopat.model.dto.BundleClinicDTO;
import de.imi.mopat.model.dto.BundleDTO;
import de.imi.mopat.model.dto.ClinicDTO;
import de.imi.mopat.model.dto.UserDTO;
import de.imi.mopat.model.enumeration.PermissionType;
import de.imi.mopat.model.user.AclObjectIdentity;
import de.imi.mopat.validator.BundleDTOValidator;
import de.imi.mopat.validator.ClinicDTOValidator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

/**
 *
 */
@Controller
public class ClinicController {

    @Autowired
    private AclEntryDao aclEntryDao;
    @Autowired
    private AclClassDao aclClassDao;
    @Autowired
    private AclObjectIdentityDao aclObjectIdentityDao;
    @Autowired
    private ClinicDao clinicDao;
    @Autowired
    private ClinicDTOValidator clinicDTOValidator;
    @Autowired
    private BundleDao bundleDao;
    @Autowired
    private BundleDao bundleClinicDao;
    @Autowired
    private BundleDTOValidator bundleValidator;
    @Autowired
    private UserDao userDao;
    @Autowired
    private MessageSource messageSource;
    @Autowired
    private BundleService bundleService;
    @Autowired
    private ClinicService clinicService;
    @Autowired
    private ClinicConfigurationDao clinicConfigurationDao;
    @Autowired
    private ClinicConfigurationMappingDao clinicConfigurationMappingDao;
    @Autowired
    private ClinicConfigurationMappingService clinicConfigurationMappingService;
    @Autowired
    private ClinicConfigurationService clinicConfigurationService;

    /**
     * @param id The Id of the {@link Clinic} object
     * @return Returns all {@link Bundle bundles} not assigned to the {@link Clinic} object.
     */
    private List<BundleDTO> getAvailableBundleDTOs(final Long id) {
        Clinic clinic;
        if (id == null || id <= 0) {
            clinic = new Clinic();
        } else {
            clinic = clinicDao.getElementById(id);
        }

        List<BundleDTO> bundleDTOs = new ArrayList<>();

        // Add bundles not already assigned to this clinic
        outerloop:
        for (Bundle bundle : bundleDao.getAllElements()) {
            for (BundleClinic bundleClinic : clinic.getBundleClinics()) {

                if (bundleClinic.getBundle().equals(bundle)) {
                    continue outerloop;
                }
            }
            // Add only bundles, which are published
            // and have at least one questionnaire attached
            if (bundle.getIsPublished() && !bundle.getBundleQuestionnaires()
                                                  .isEmpty()) {
                bundleDTOs.add(bundleService.toBundleDTO(true,bundle));
            }
        }

        // Sort by name
        Collections.sort(bundleDTOs, new Comparator<BundleDTO>() {
            @Override
            public int compare(final BundleDTO o1, final BundleDTO o2) {
                return o1.getName().compareToIgnoreCase(o2.getName());
            }
        });
        return bundleDTOs;
    }

    /**
     * @param id The Id of the {@link Clinic} object.
     * @return Returns all {@link User Users} that are available to be assigned to the
     * {@link Clinic} object.
     */
    private List<UserDTO> getAvailableUserDTOs(final Long id) {

        List<UserDTO> availableUserDTOs = new ArrayList<>();

        // Add all users that are available
        boolean assigned = false;
        for (User user : userDao.getAllElements()) {
            if (id != null) {
                for (AclEntry aclEntry : aclEntryDao.getAllElements()) {
                    if (aclEntry.getAclObjectIdentity().getObjectIdIdentity().equals(id)
                        && aclEntry.getUser().getId().equals(user.getId())) {
                        assigned = true;
                        break;
                    }
                }
                if (!assigned) {
                    availableUserDTOs.add(user.toUserDTO());
                }
                assigned = false;
            } else {
                availableUserDTOs.add(user.toUserDTO());
            }
        }

        return availableUserDTOs;
    }

    /**
     * Controls the HTTP GET requests for the URL <i>/clinic/list</i>. Shows the list of clinics.
     *
     * @param model The model, which holds information for the view.
     * @return The <i>clinic/clinic</i> website.
     */
    @RequestMapping(value = "/clinic/list", method = RequestMethod.GET)
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public String showClinics(final Model model) {
        model.addAttribute("allClinics", clinicDao.getAllElements());
        return "clinic/list";
    }

    /**
     * Controls the HTTP GET requests for the URL <i>/clinic/edit</i>. Shows the page containing the
     * form fields for editing a {@link Clinic} object.
     *
     * @param clinicId Id of the {@link Clinic} object.
     * @param model    The model, which holds information for the view.
     * @return The <i>clinic/edit</i> website.
     */
    @RequestMapping(value = "/clinic/edit", method = RequestMethod.GET)
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public String editClinic(@RequestParam(value = "id", required = false) final Long clinicId,
        final Model model) {

        ClinicDTO clinicDTO = new ClinicDTO();
        Clinic clinic = clinicDao.getElementById(clinicId);
        if (clinic != null) {
            clinicDTO = clinicService.toClinicDTO(clinic);
        }

        List<UserDTO> assignedUserDTOs = new ArrayList<>();
        List<UserDTO> availableUserDTOs = getAvailableUserDTOs(clinicId);

        for (User user : userDao.getAllElements()) {
            UserDTO assignedUserDTO = user.toUserDTO();
            assignedUserDTOs.add(assignedUserDTO);
            for (UserDTO userDTO : availableUserDTOs) {
                if (assignedUserDTO.getId().equals(userDTO.getId())) {
                    assignedUserDTOs.remove(assignedUserDTO);
                }
            }
        }

        List<ClinicConfigurationDTO> clinicConfigurationDTOS = new ArrayList<>();
        List<ClinicConfiguration> clinicConfigurations = clinicConfigurationDao.getAllElements();
        for (ClinicConfiguration configuration : clinicConfigurations) {
            if (configuration.getParent() == null) {
                ClinicConfigurationDTO configurationDTO = configuration.toClinicConfigurationDTO();

                if (configuration.getChildren() != null && !configuration.getChildren()
                        .isEmpty()) {
                    clinicConfigurationService.processChildrenElements(configuration, configurationDTO);
                }
                clinicConfigurationDTOS.add(configurationDTO);
            }
        }
        if(clinic == null){
            List<ClinicConfigurationMappingDTO> clinicConfigurationMappingDTOS;
            clinicConfigurationMappingDTOS = recursivelyInitializeClinicConfigurationMappingDTOS(clinicConfigurationDTOS);
            clinicDTO.setClinicConfigurationMappingDTOS(clinicConfigurationMappingDTOS);
        }

        clinicDTO.setAssignedUserDTOs(assignedUserDTOs);
        model.addAttribute("clinicDTO", clinicDTO);
        model.addAttribute("availableBundleDTOs", getAvailableBundleDTOs(clinicId));
        model.addAttribute("availableUserDTOs", availableUserDTOs);
        return "clinic/edit";
    }

    /**
     * Controls the HTTP POST requests for the URL <i>/clinic/edit</i>. Provides the ability to
     * update a {@link Clinic} object.
     *
     * @param action    The name of the submit button which has been clicked.
     * @param clinicDTO The current {@link ClinicDTO}.
     * @param result    The result for validation of the {@link Clinic}.
     * @param model     The model, which holds the information for the view.
     * @return Redirect to the <i>clinic/list</i> website.
     */
    @RequestMapping(value = "/clinic/edit", method = RequestMethod.POST)
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Transactional("MoPat_User")
    public String editClinic(@RequestParam final String action,
        @ModelAttribute("clinicDTO") @Valid final ClinicDTO clinicDTO, final BindingResult result,
        final Model model) {

        if (action.equalsIgnoreCase("cancel")) {
            return "redirect:/clinic/list";
        }

        // Validate the clinic object
        clinicDTOValidator.validate(clinicDTO, result);
        if (result.hasErrors()) {
            // take care that the bundles assigned to/taken away from the clinic
            // disappear from/re-appear in the list of available bundles,
            // although an error is present.
            // Thus, the latest changes by the user stay in the UI
            List<BundleDTO> availableBundles = getAvailableBundleDTOs(null);

            if (clinicDTO.getBundleClinicDTOs() != null) {
                for (
                    Iterator<BundleClinicDTO> iterator = clinicDTO.getBundleClinicDTOs().iterator();
                    iterator.hasNext(); ) {
                    BundleDTO assignedBundleDTO = iterator.next().getBundleDTO();
                    if (assignedBundleDTO == null || assignedBundleDTO.getId() == null) {
                        iterator.remove();
                    } else {
                        assignedBundleDTO.setName(
                            bundleDao.getElementById(assignedBundleDTO.getId()).getName());
                    }
                }

                List<BundleDTO> bundleDTOsToDelete = new ArrayList<>();
                for (BundleClinicDTO bundleClinicDTO : clinicDTO.getBundleClinicDTOs()) {
                    for (BundleDTO bundleDTO : new ArrayList<>(availableBundles)) {
                        if (bundleClinicDTO.getBundleDTO().getId().equals(bundleDTO.getId())) {
                            bundleDTOsToDelete.add(bundleDTO);
                            break;
                        }
                    }
                }
                availableBundles.removeAll(bundleDTOsToDelete);
            }

            //If the result has an error,
            // his keeps the changes at assigned-/availableUsersTable
            List<UserDTO> availableUserDTOs = getAvailableUserDTOs(null);
            List<UserDTO> assignedUserDTOs = new ArrayList<>();
            if (clinicDTO.getAssignedUserDTOs() != null) {
                for (UserDTO userDTO : clinicDTO.getAssignedUserDTOs()) {
                    if (userDTO.getId() != null) {
                        //userDTO only contains id,
                        //so we need to get new userDTO from userDao that
                        // contains username etc.
                        assignedUserDTOs.add(userDao.getElementById(userDTO.getId()).toUserDTO());
                    }
                }
            }

            clinicDTO.setAssignedUserDTOs(assignedUserDTOs);

            List<UserDTO> userDTOsToDelete = new ArrayList<>();
            for (UserDTO userDTO : availableUserDTOs) {
                for (UserDTO assignedUserDTO : assignedUserDTOs) {
                    if (userDTO.getId().equals(assignedUserDTO.getId())) {
                        userDTOsToDelete.add(userDTO);
                    }
                }
            }

            List<ClinicConfigurationMappingDTO> clinicConfigurationMappingDTOS = new ArrayList<>();
            for(ClinicConfigurationMappingDTO clinicConfigurationMappingDTO: clinicDTO.getClinicConfigurationMappingDTOS()){
                clinicConfigurationMappingDTOS.add(clinicConfigurationMappingService.processClinicConfigurationMappingDTO(clinicConfigurationMappingDTO));
            }
            clinicDTO.setClinicConfigurationMappingDTOS(clinicConfigurationMappingDTOS);
            availableUserDTOs.removeAll(userDTOsToDelete);

            model.addAttribute("availableBundleDTOs", availableBundles);
            model.addAttribute("availableUserDTOs", availableUserDTOs);
            model.addAttribute("clinicDTO", clinicDTO);
            return "clinic/edit";
        }

        // Delete empty assignedUserDTOs from clinicDTO
        if (clinicDTO.getAssignedUserDTOs() != null && !clinicDTO.getAssignedUserDTOs().isEmpty()) {
            for (Iterator assignedUserDTOIterator = clinicDTO.getAssignedUserDTOs().iterator();
                assignedUserDTOIterator.hasNext(); ) {
                UserDTO userDTO = (UserDTO) assignedUserDTOIterator.next();
                if (userDTO == null || userDTO.getId() == null) {
                    assignedUserDTOIterator.remove();
                }
            }
        }

        // Delete empty bundleClinicDTOs from clinicDTO
        if (clinicDTO.getBundleClinicDTOs() != null && !clinicDTO.getBundleClinicDTOs().isEmpty()) {
            for (Iterator<BundleClinicDTO> bundleClinicDTOIterator = clinicDTO.getBundleClinicDTOs()
                .iterator(); bundleClinicDTOIterator.hasNext(); ) {
                BundleClinicDTO bundleClinicDTO = bundleClinicDTOIterator.next();
                if (bundleClinicDTO.getBundleDTO() == null
                    || bundleClinicDTO.getBundleDTO().getId() == null) {
                    bundleClinicDTOIterator.remove();
                }
            }
        }

        Clinic clinic = new Clinic();
        clinic.setDescription(clinicDTO.getDescription());
        clinic.setName(clinicDTO.getName());
        clinic.setEmail(clinicDTO.getEmail());
        List<Bundle> deletedBundles = new ArrayList<>();
        // If a existing clinic was edited
        if (clinicDTO.getId() != null) {
            List<BundleClinic> deletedBundleClinics = new ArrayList<>();
            List<BundleClinic> currentBundleClinics = new ArrayList<>();
            clinic = clinicDao.getElementById(clinicDTO.getId());
            clinic.setDescription(clinicDTO.getDescription());
            clinic.setName(clinicDTO.getName());
            clinic.setEmail(clinicDTO.getEmail());
            // Get bundleClinics from outdated clinic retrieved from
            // PersistenceContext
            for (BundleClinic bundleClinic : clinic.getBundleClinics()) {
                deletedBundleClinics.add(bundleClinic);
            }

            // Get current bundleClinics from given clinicDTO
            if (clinicDTO.getBundleClinicDTOs() != null && !clinicDTO.getBundleClinicDTOs()
                .isEmpty()) {
                // Define persistedBundleClinics as array list from the
                // clinic's bundleClinics
                // (bundleClinics is a tree set sorted by its positions) here
                // to take care that there won't be a bundleClinic deleted
                // from clinic's bundleClinics
                // because there are bundleClinics with the same position
                List<BundleClinic> persistedBundleClinics = new ArrayList<>(
                    clinic.getBundleClinics());
                outerloop:
                for (BundleClinicDTO bundleClinicDTO : clinicDTO.getBundleClinicDTOs()) {
                    for (BundleClinic persistedBundleClinic : persistedBundleClinics) {
                        // If the bundleClinicDTO is already persisted
                        if (persistedBundleClinic.getBundle().getId()
                            .equals(bundleClinicDTO.getBundleDTO().getId())) {
                            // Update the position of the already attached
                            // bundleClinic
                            persistedBundleClinic.setPosition(bundleClinicDTO.getPosition());
                            // Store it into current bundleClinics and go to
                            // the next bundleClinicDTO
                            currentBundleClinics.add(persistedBundleClinic);
                            continue outerloop;
                        }
                    }
                    // If the bundle is new to this clinic
                    Bundle bundle = bundleDao.getElementById(
                        bundleClinicDTO.getBundleDTO().getId());
                    // Create a new bundleClinic and assign it
                    BundleClinic bundleClinic = new BundleClinic(bundleClinicDTO.getPosition(),
                        clinic, bundle);
                    currentBundleClinics.add(bundleClinic);
                    clinic.addBundleClinic(bundleClinic);
                    bundle.addBundleClinic(bundleClinic);
                }
            }
            // Remove deleted bundleClinics from the clinic
            deletedBundleClinics.removeAll(currentBundleClinics);
            clinic.removeBundleClinics(deletedBundleClinics);
            // Get the bundles deleted from the clinic and remove the
            // corresponding bundleClinic
            for (BundleClinic bundleClinic : deletedBundleClinics) {
                deletedBundles.add(bundleClinic.getBundle());
                bundleClinic.getBundle().removeBundleClinic(bundleClinic);
            }
        } else {// Add bundleClinics to the newly created clinic
            if (clinicDTO.getBundleClinicDTOs() != null && !clinicDTO.getBundleClinicDTOs()
                .isEmpty()) {
                for (BundleClinicDTO bundleClinicDTO : clinicDTO.getBundleClinicDTOs()) {
                    // If the bundle is new to this clinic
                    Bundle bundle = bundleDao.getElementById(
                        bundleClinicDTO.getBundleDTO().getId());
                    // Create a new bundleClinic and assign it
                    BundleClinic bundleClinic = new BundleClinic(bundleClinicDTO.getPosition(),
                        clinic, bundle);
                    clinic.addBundleClinic(bundleClinic);
                    bundle.addBundleClinic(bundleClinic);
                }
            }
        }

        List<ClinicConfigurationMapping> clinicConfigurationMappingList;
        List<ClinicConfigurationMappingDTO> clinicConfigurationMappingDTOS = clinicDTO.getClinicConfigurationMappingDTOS();
        clinicConfigurationMappingList=processClinicConfigurationMappingDTOS(clinic, clinicConfigurationMappingDTOS);
        clinic.setClinicConfigurationMappings(clinicConfigurationMappingList);
        if (clinic.getId() != null) {
            clinicDao.merge(clinic);
        } else { // If the clinic is new, create a corresponding ACLObject
            clinicDao.merge(clinic);
            // Get the current user, which is the owner of the clinic
            User currentUser = (User) SecurityContextHolder.getContext().getAuthentication()
                .getPrincipal();
            // Create a new ACLObjectIdentity for the clinic and save it
            AclObjectIdentity clinicObjectIdentity = new AclObjectIdentity(clinic.getId(),
                Boolean.TRUE, aclClassDao.getElementByClass(Clinic.class.getName()), currentUser,
                null);
            aclObjectIdentityDao.persist(clinicObjectIdentity);
        }
        // Update the current bundles in the database
        for (BundleClinic bundleClinic : clinic.getBundleClinics()) {
            bundleDao.merge(bundleClinic.getBundle());
        }
        // Update the deleted bundles in the database
        for (Bundle deletedBundle : deletedBundles) {
            bundleDao.merge(deletedBundle);
        }
        clinicDao.updateUserRights(clinic, deletedBundles, clinicDTO.getAssignedUserDTOs());

        return "redirect:/clinic/list";
    }

    /**
     * Controls the HTTP requests for the URL <i>clinic/remove</i>. Removes a {@link Clinic} object
     * by a given id and redirects to the list of clinics.
     *
     * @param id    Id of the {@link Clinic} object, which should be removed.
     * @param model The model, which holds the information for the view.
     * @return Redirect to the <i>clinic/clinic</i> website.
     */
    @RequestMapping(value = "/clinic/remove")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Transactional("MoPat_User")
    public String removeClinic(@RequestParam(value = "id", required = true) final Long id,
        final Model model) {
        Clinic clinic = clinicDao.getElementById(id);

        if (clinic != null) {
            // Revoke all permissions for all users to this clinic
            // and the inherited bundles, Key = User, Value = Permission
            for (Map.Entry<User, PermissionType> entry : aclEntryDao.getUserRightsByObject(clinic)
                .entrySet()) {
                clinicDao.revokeRight(clinic, entry.getKey(), entry.getValue(), Boolean.TRUE);
            }
            // Delete connection to the bundles
            for (BundleClinic bundleClinic : clinic.getBundleClinics()) {
                Bundle bundle = bundleClinic.getBundle();
                bundle.removeBundleClinic(bundleClinic);
                bundleDao.merge(bundle);
            }
            clinic.removeAllBundleClinics();
            // Delete the corresponding ACL object for the removed clinic
            aclObjectIdentityDao.remove(aclObjectIdentityDao.getElementByClassAndObjectId(
                aclClassDao.getElementByClass(Clinic.class.getName()), id));
            // Delete the clinic
            clinicDao.remove(clinic);
            model.addAttribute("messageSuccess",
                messageSource.getMessage("clinic.message.deleteSuccess",
                    new Object[]{clinic.getName()}, LocaleContextHolder.getLocale()));
        }
        return showClinics(model);
    }


    private void recursivelyFindChildConfigurations(Clinic clinic, ClinicConfigurationDTO clinicConfigurationDTO, List<ClinicConfigurationMapping> clinicConfigurationMappingList){
            ClinicConfiguration clinicConfiguration = new ClinicConfiguration(
                    clinicConfigurationDTO.getEntityClass(), clinicConfigurationDTO.getAttribute(), clinicConfigurationDTO.getConfigurationType(),
                    clinicConfigurationDTO.getLabelMessageCode(),clinicConfigurationDTO.getDescriptionMessageCode(),clinicConfigurationDTO.getTestMethod(),clinicConfigurationDTO.getUpdateMethod(),
                    clinicConfigurationDTO.getPosition());
            for (ClinicConfigurationDTO child : clinicConfigurationDTO.getChildren()) {
                if (child.getChildren() != null && !child.getChildren().isEmpty()) {
                    recursivelyFindChildConfigurations(clinic, child, clinicConfigurationMappingList);
                }
            }
            clinicConfigurationMappingList.add(new ClinicConfigurationMapping(clinic, clinicConfiguration, clinicConfiguration.getValue()));

    }

    private List<ClinicConfigurationMapping> processClinicConfigurationMappingDTOS(Clinic clinic, final List<ClinicConfigurationMappingDTO> clinicConfigurationMappingDTOS){
        List<ClinicConfigurationMapping> clinicConfigurationMappingList = new ArrayList<>();
        for(ClinicConfigurationMappingDTO clinicConfigurationMappingDTO : clinicConfigurationMappingDTOS){
            ClinicConfiguration clinicConfiguration = clinicConfigurationDao.getElementById(clinicConfigurationMappingDTO.getClinicConfigurationId());
            if(clinic.getId()==null){
                ClinicConfigurationMapping clinicConfigurationMapping = new ClinicConfigurationMapping(clinic, clinicConfiguration, clinicConfigurationMappingDTO.getValue());
                clinicConfigurationMappingList.add(clinicConfigurationMapping);
                if(clinicConfigurationMappingDTO.getChildren() != null){
                    List<ClinicConfigurationMapping> children = processClinicConfigurationMappingDTOS(clinic, clinicConfigurationMappingDTO.getChildren());
                    clinicConfigurationMappingList.addAll(children);
                }
            } else {
                ClinicConfigurationMapping clinicConfigurationMapping = clinicConfigurationMappingDao.getElementById(clinicConfigurationMappingDTO.getId());
                clinicConfigurationMapping.setValue(clinicConfigurationMappingDTO.getValue());
                clinicConfigurationMappingList.add(clinicConfigurationMapping);
                if(clinicConfigurationMappingDTO.getChildren() != null){
                    List<ClinicConfigurationMapping> children = processClinicConfigurationMappingDTOS(clinic, clinicConfigurationMappingDTO.getChildren());
                    clinicConfigurationMappingList.addAll(children);
                }
            }
        }

        return clinicConfigurationMappingList;
    }

    private List<ClinicConfigurationMappingDTO> recursivelyInitializeClinicConfigurationMappingDTOS(List<ClinicConfigurationDTO> clinicConfigurationDTOS){
        List<ClinicConfigurationMappingDTO> clinicConfigurationMappingDTOS = new ArrayList<>();
        for(ClinicConfigurationDTO clinicConfigurationDTO: clinicConfigurationDTOS){
            ClinicConfigurationMappingDTO clinicConfigurationMappingDTO = clinicConfigurationMappingService.toClinicConfigurationMappingDTO(clinicConfigurationDTO);
            if(clinicConfigurationDTO.getChildren() != null){
                List<ClinicConfigurationMappingDTO> children = recursivelyInitializeClinicConfigurationMappingDTOS(clinicConfigurationDTO.getChildren());
                clinicConfigurationMappingDTO.setChildren(children);
            }
            clinicConfigurationMappingDTOS.add(clinicConfigurationMappingDTO);
        }

        return clinicConfigurationMappingDTOS;
    }
}
