package de.imi.mopat.controller;

import com.mchange.v1.db.sql.UnsupportedTypeException;
import de.imi.mopat.dao.ConfigurationDao;
import de.imi.mopat.dao.ConfigurationGroupDao;
import de.imi.mopat.helper.controller.ConfigurationService;
import de.imi.mopat.helper.controller.MailSender;
import de.imi.mopat.helper.controller.MultiPartFileUploadBean;
import de.imi.mopat.helper.controller.StringUtilities;
import de.imi.mopat.model.Configuration;
import de.imi.mopat.model.ConfigurationGroup;
import de.imi.mopat.model.PatternConfiguration;
import de.imi.mopat.model.SelectConfiguration;
import de.imi.mopat.model.dto.ConfigurationComponentDTO;
import de.imi.mopat.model.dto.ConfigurationDTO;
import de.imi.mopat.model.dto.ConfigurationGroupDTO;
import de.imi.mopat.validator.ConfigurationComponentDTOValidator;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Objects;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 *
 */
@Controller
public class ConfigurationController {

    @Autowired
    private ConfigurationDao configurationDao;

    @Autowired
    private ConfigurationGroupDao configurationGroupDao;

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private ConfigurationComponentDTOValidator configurationComponentDTOValidator;

    @Autowired
    private ApplicationContext appContext;

    @Autowired
    private MailSender mailSender;

    @Autowired
    private ConfigurationService configurationService;

    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(
        ConfigurationController.class);

    /**
     * @return Returns a new {@link ConfigurationDTO} object which contains a map with configuration
     * id's as key and {@link Configuration} objects as value. Put this configuration into the model
     * with <i>configurationDTO</i> as key.
     */
    @ModelAttribute("configurationComponentDTO")
    public ConfigurationComponentDTO getConfigurationComponentDTO() {
        Map<String, List<ConfigurationGroupDTO>> configurationGroupDTOs = new LinkedHashMap<>();    //Use LinkedHashMap to guarantee
        // sorting the map by configurationgroup's position

        List<ConfigurationGroup> configurationGroups = configurationGroupDao.getAllElements();

        Collections.sort(configurationGroups, new Comparator<ConfigurationGroup>() {
            @Override
            public int compare(final ConfigurationGroup o1, final ConfigurationGroup o2) {
                return o1.getPosition().compareTo(o2.getPosition());
            }
        });

        for (ConfigurationGroup configurationGroup : configurationGroups) {
            //Convert to ConfigurationGroupDTO
            ConfigurationGroupDTO configurationGroupDTO = configurationGroup.toConfigurationGroupDTO();
            List<ConfigurationDTO> configurationDTOs = new ArrayList<>();

            //Go through all adherent configurations
            for (Configuration configuration : configurationGroup.getConfigurations()) {
                if (configuration.getParent() == null) {
                    ConfigurationDTO configurationDTO = configuration.toConfigurationDTO();

                    if (configuration.getChildren() != null && !configuration.getChildren()
                        .isEmpty()) {
                        configurationService.processChildrenElements(configuration, configurationDTO);
                    }
                    configurationDTOs.add(configurationDTO);
                }
            }
            configurationGroupDTO.setConfigurationDTOs(configurationDTOs);
            configurationGroupDTO.setDeletable(
                configurationGroupDao.isConfigurationGroupDeletable(configurationGroupDTO.getId()));

            String key = configurationGroupDTO.getLabelMessageCode()
                                              .substring(configurationGroupDTO.getLabelMessageCode()
                                                                                        .lastIndexOf('.') + 1);
            //Add configurationGroupDTO to given entry
            if (configurationGroupDTOs.containsKey(key)) {
                configurationGroupDTOs.get(key).add(configurationGroupDTO);
            } else {  //Create new entry if key doesn't exist
                List<ConfigurationGroupDTO> newEntry = new ArrayList<>();
                newEntry.add(configurationGroupDTO);
                configurationGroupDTOs.put(key, newEntry);
            }
        }

        for(ConfigurationDTO configurationDTO : configurationGroupDTOs.get("general").get(0).getConfigurationDTOs()){
            if(Objects.equals(configurationDTO.getAttribute(), "logo")){
                configurationDTO.setValue(processImages(configurationDTO));
            }
        }

        ConfigurationComponentDTO configurationComponentDTO =
            new ConfigurationComponentDTO(configurationGroupDTOs);
        restoreConfigurationGroupDTOReferences(configurationComponentDTO);
        configurationComponentDTO.setImageDeleteMap(new HashMap<>());
        return configurationComponentDTO;

    }

    private String processImages(final ConfigurationDTO configurationDTO){
        if(configurationDTO.getValue()!=null){
            String realPath = configurationDao.getImageUploadPath() + configurationDTO.getValue();
            String fileName = realPath.substring(realPath.lastIndexOf("/"));
            try {
                return StringUtilities.convertImageToBase64String(realPath, fileName);
            } catch (IOException e) {
                return null;
            }
        }
        return null;
    }



    /**
     * Helper function to restore the references for every ConfigurationDTO in the
     * ConfigurationComponentDTO to its ConfigurationGroupDTO. The reference is lost upon
     * transforming the Configuration to a ConfigurationDTO as the Configuration only knows the
     * Reference for the ConfigurationGroup not the ConfigurationGroupDTO
     *
     * @param configurationComponentDTO : The ConfigurationComponentDTO to restore the references
     *                                  for
     */
    private void restoreConfigurationGroupDTOReferences(
        final ConfigurationComponentDTO configurationComponentDTO) {
        for (List<ConfigurationGroupDTO> configurationGroupDTOs : configurationComponentDTO.getConfigurationGroupDTOs()
            .values()) {
            for (ConfigurationGroupDTO currentConfigurationGroupDTO : configurationGroupDTOs) {
                for (ConfigurationDTO currentConfigurationDTO : currentConfigurationGroupDTO.getConfigurationDTOs()) {
                    recursivelyRestoreConfigurationGroupDTOReferences(currentConfigurationDTO,
                        currentConfigurationGroupDTO);
                }
            }
        }
    }

    /**
     * Helper function that recursively processes every ConfigurationDTO and its childs: If there is
     * no reference to a ConfigurationGroupDTO, it will set it to the current group that is
     * processed
     *
     * @param currentConfigurationDTO The currently processed ConfigurationDTO
     * @param referenceGroup          The reference ConfigurationGroupDTO that will be set
     */
    private void recursivelyRestoreConfigurationGroupDTOReferences(
        final ConfigurationDTO currentConfigurationDTO,
        final ConfigurationGroupDTO referenceGroup) {
        if (currentConfigurationDTO.getConfigurationGroupDTO() == null) {
            currentConfigurationDTO.setConfigurationGroupDTO(referenceGroup);
        }

        if (currentConfigurationDTO.getChildren() != null && !currentConfigurationDTO.getChildren()
            .isEmpty()) {
            for (ConfigurationDTO childDTO : currentConfigurationDTO.getChildren()) {
                recursivelyRestoreConfigurationGroupDTOReferences(childDTO, referenceGroup);
            }
        }
    }

    /**
     * Controls the HTTP GET requests for the URL <i>/configuration/edit</i>. Shows the page
     * containing the form fields for the {@link Configuration configuration} object.
     *
     * @param model The model, which holds the information for the view.
     * @return The <i>configuration/edit</i> website.
     */
    @RequestMapping(value = "/configuration/edit", method = RequestMethod.GET)
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public String showConfiguration(final Model model) {
        return "configuration/edit";
    }

    /**
     * Controls the HTTP POST requests for the URL <i>/configuration/edit</i>. Provides the ability
     * to create a new {@link Configuration configuration} object.
     *
     * @param action                    The name of the submit button which has been clicked.
     * @param request                   The request, which was sent from the client's browser.
     * @param configurationComponentDTO The {@link ConfigurationComponentDTO} object from the view.
     * @param result                    The result for the validation of the {@link Configuration}
     *                                  object.
     * @param model                     The model, which holds the information for the view.
     * @param imageFiles                The image files sent for the configuration.
     * @param redirectAttributes        Stores the information for a redirect scenario.
     * @return The <i>configuration/edit</i> website.
     */
    @RequestMapping(value = "/configuration/edit", method = RequestMethod.POST)
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public String editConfiguration(@RequestParam final String action,
        final HttpServletRequest request,
        @ModelAttribute("configurationComponentDTO") final ConfigurationComponentDTO configurationComponentDTO,
        final BindingResult result, final Model model, final MultiPartFileUploadBean imageFiles,
        final RedirectAttributes redirectAttributes) {
        String serverStoragePath = configurationDao.getImageUploadPath();
        String recoveryStoragePath = configurationDao.getObjectStoragePath();

        //Fill the currently added configurationDTO's properties
        for (String key : configurationComponentDTO.getConfigurationGroupDTOs().keySet()) {
            for (ConfigurationGroupDTO configurationGroupDTO : configurationComponentDTO.getConfigurationGroupDTOs()
                .get(key)) {
                if (configurationGroupDTO.getReferringId() != null) {
                    //configurationGroup has been added currently
                    ConfigurationGroup referringConfigurationGroup = configurationGroupDao.getElementById(
                        configurationGroupDTO.getReferringId());

                    cloneConfigurationGroupDTOFromReferringConfigurationGroup(configurationGroupDTO,
                        referringConfigurationGroup);
                }
            }
        }

        //Validate configuration group DTOs
        configurationComponentDTOValidator.validate(configurationComponentDTO, result);
        if (result.hasErrors()) {
            for (String key : configurationComponentDTO.getConfigurationGroupDTOs().keySet()) {
                for (ConfigurationGroupDTO configurationGroupDTO : configurationComponentDTO.getConfigurationGroupDTOs()
                    .get(key)) {
                    if (configurationGroupDTO.getId() != null) {
                        configurationGroupDTO.setDeletable(
                            configurationGroupDao.isConfigurationGroupDeletable(
                                configurationGroupDTO.getId()));
                    } else {
                        configurationGroupDTO.setDeletable(true);
                    }
                }
            }
            return "configuration/edit";
        }

        for(ConfigurationDTO configurationDTO : configurationComponentDTO.getConfigurationGroupDTOs().get("general").get(0).getConfigurationDTOs()){
            String attribute=configurationDTO.getAttribute();
            if(Objects.equals(attribute, "logo")){
                String logo = configurationDTO.getValue();
                if(!Objects.equals(logo, null)){
                    configurationDTO.setValue(configurationDao.getLogoPath());
                }
            }
            if(Objects.equals(attribute,"imageUploadPath")){
                if(!Objects.equals(serverStoragePath, null)){
                    serverStoragePath=configurationDTO.getValue();
                }
            }
        }

        deleteFilesAndImages(
            configurationComponentDTO,
            recoveryStoragePath,
            serverStoragePath);

        try {
            saveImagesAndFilesInUploadDirectory(imageFiles.getFiles(), configurationComponentDTO,
                recoveryStoragePath, serverStoragePath,
                request.getSession().getServletContext().getContextPath());
        } catch (Exception e) {
            result.reject("file",
                messageSource.getMessage("bundle.error.wrongImageType", new Object[]{},
                    LocaleContextHolder.getLocale()));
        }

        for (String key : configurationComponentDTO.getConfigurationGroupDTOs().keySet()) {
            for (ConfigurationGroupDTO configurationGroupDTO : configurationComponentDTO.getConfigurationGroupDTOs()
                .get(key)) {
                //Either the configuration existed before or it has been
                // added currently
                if (configurationGroupDTO.getId() != null) {
                    boolean deleteConfigurationGroup = configurationComponentDTO.getConfigurationsToDelete()
                        .contains(configurationGroupDTO.getId());
                    updateConfigurationGroupForNewDTO(configurationGroupDTO,
                        deleteConfigurationGroup);
                } else if (configurationGroupDTO.getReferringId() != null) {
                    //configuration has been added currently
                    createNewConfigurationGroupForConfigurationGroupDTO(configurationGroupDTO);
                }
            }
        }

        // Check configuration entries for UPDATE-Methods and call them
        for (Configuration configuration : configurationDao.getAllElements()) {
            if (configuration.getUpdateMethod() != null && !configuration.getUpdateMethod()
                .isEmpty()) {
                // Invoke method via reflection
                Class<?> clazz;
                try {
                    clazz = Class.forName(configuration.getEntityClass());
                    Method method = clazz.getMethod(configuration.getUpdateMethod());
                    Boolean updateSucceeded = (Boolean) method.invoke(appContext.getBean(clazz));
                    if (updateSucceeded != null && !updateSucceeded) {
                        result.rejectValue(
                            "configurations[" + configuration.getId() + "]" + ".value",
                            "errormessage", "UPDATE METHOD FAIL");
                    }
                } catch (ClassNotFoundException | NoSuchMethodException | SecurityException |
                         BeansException | IllegalAccessException | IllegalArgumentException |
                         InvocationTargetException ex) {
                    LOGGER.error("Error during update of configuration, while"
                        + " using the JAVA Reflection API.");
                }
            }
        }
        mailSender.update();
        redirectAttributes.addFlashAttribute("success",
            messageSource.getMessage("configuration.success.changed", new Object[]{},
                LocaleContextHolder.getLocale()));
        return "redirect:/configuration/edit";
    }

    private ConfigurationDTO getConfigurationDTOForId(
        final Map<String, List<ConfigurationGroupDTO>> configurationGroupDTOs,
        final Long searchId) {
        //Process all configurationGroups
        for (List<ConfigurationGroupDTO> configurationGroupList : configurationGroupDTOs.values()) {
            // Process every named configuration group, if multiple
            // configurations for the same category exist
            for (ConfigurationGroupDTO currentConfigurationGroupDTO : configurationGroupList) {
                // Process every ConfigurationDTO for every group
                for (ConfigurationDTO currentConfigurationDTO : currentConfigurationGroupDTO.getConfigurationDTOs()) {
                    ConfigurationDTO foundDTO = recursivelyFindConfigurationDTOInGroupForId(
                        currentConfigurationDTO, searchId);
                    if (foundDTO != null) {
                        return foundDTO;
                    }
                }
            }
        }
        // No match found after processing all entries
        return null;
    }


    private ConfigurationDTO getConfigurationDTOForId(
        final ConfigurationGroupDTO configurationGroupDTO, final Long searchId) {
        for (ConfigurationDTO currentConfigurationDTO : configurationGroupDTO.getConfigurationDTOs()) {
            ConfigurationDTO foundDTO = recursivelyFindConfigurationDTOInGroupForId(
                currentConfigurationDTO, searchId);
            if (foundDTO != null) {
                return foundDTO;
            }
        }
        // No match found after processing all entries
        return null;
    }

    private ConfigurationDTO recursivelyFindConfigurationDTOInGroupForId(
        final ConfigurationDTO currentConfigurationDTO, final Long searchId) {
        // If this is the correct ID, return it
        if (currentConfigurationDTO.getId() != null && currentConfigurationDTO.getId()
            .equals(searchId)) {
            return currentConfigurationDTO;
            // Else search in child configurations
        } else {
            if (currentConfigurationDTO.getChildren() != null
                && !currentConfigurationDTO.getChildren().isEmpty()) {
                for (ConfigurationDTO childDTO : currentConfigurationDTO.getChildren()) {
                    ConfigurationDTO foundDTO = recursivelyFindConfigurationDTOInGroupForId(
                        childDTO, searchId);
                    if (foundDTO != null) {
                        return foundDTO;
                    }
                }
            }
        }
        // No match found after processing all entries
        return null;
    }

    private void updateConfigurationGroupForNewDTO(
        final ConfigurationGroupDTO newConfigurationGroupDTO,
        final boolean deleteConfigurationGroup) {
        //configuration existed before
        ConfigurationGroup configurationGroup = configurationGroupDao.getElementById(
            newConfigurationGroupDTO.getId());
        if (!deleteConfigurationGroup) {
            configurationGroup.setName(newConfigurationGroupDTO.getName());
            recursivelyUpdateConfigurationList(configurationGroup.getConfigurations(),
                newConfigurationGroupDTO);
            configurationGroupDao.merge(configurationGroup);
        } else if (configurationGroupDao.isConfigurationGroupDeletable(
            newConfigurationGroupDTO.getId())) {
            configurationGroupDao.remove(configurationGroup);
        }
    }

    private void recursivelyUpdateConfigurationList(final List<Configuration> configurationList,
        final ConfigurationGroupDTO newConfigurationGroupDTO) {
        for (Configuration configuration : configurationList) {
            ConfigurationDTO matchingConfigurationDTO = getConfigurationDTOForId(
                newConfigurationGroupDTO, configuration.getId());
            if (matchingConfigurationDTO != null) {
                configuration.setValue(matchingConfigurationDTO.getValue());
            }

            if (configuration.getChildren() != null && !configuration.getChildren().isEmpty()) {
                recursivelyUpdateConfigurationList(configuration.getChildren(),
                    newConfigurationGroupDTO);
            }
        }
    }

    private void cloneConfigurationGroupDTOFromReferringConfigurationGroup(
        final ConfigurationGroupDTO configurationGroupDTO,
        final ConfigurationGroup referringConfigurationGroup) {
        //set configurationGroupDTO's properties necessary for clean validation
        configurationGroupDTO.setPosition(referringConfigurationGroup.getPosition());
        configurationGroupDTO.setLabelMessageCode(
            referringConfigurationGroup.getLabelMessageCode());
        configurationGroupDTO.setRepeating(referringConfigurationGroup.isRepeating());

        recursivelyCloneConfigurationDTOsFromReferringConfiguration(0, 0,
            referringConfigurationGroup.getConfigurations(), configurationGroupDTO, null,
            configurationGroupDTO.getConfigurationDTOs());

    }

    private int recursivelyCloneConfigurationDTOsFromReferringConfiguration(
        final int currentConfigurationCounter, final int currentProcessedCounter,
        final List<Configuration> configurations, final ConfigurationGroupDTO configurationGroupDTO,
        final ConfigurationDTO parent, final List<ConfigurationDTO> currentConfigurations) {

        int runningCounter = currentConfigurationCounter;
        int processedElementsOnLevel = currentProcessedCounter;

        for (ConfigurationDTO currentConfigurationDTO : currentConfigurations) {
            try {
                Configuration referringConfiguration = configurations.get(runningCounter);

                cloneConfigurationDTOFromReferringConfiguration(referringConfiguration,
                    currentConfigurationDTO, parent, configurationGroupDTO);

                processedElementsOnLevel++;
                runningCounter++;

                if (currentConfigurationDTO.getChildren() != null
                    && !currentConfigurationDTO.getChildren().isEmpty()) {
                    int processedChildElements = recursivelyCloneConfigurationDTOsFromReferringConfiguration(
                        runningCounter, 0, configurations, configurationGroupDTO,
                        currentConfigurationDTO, currentConfigurationDTO.getChildren());

                    runningCounter += processedChildElements;
                    processedElementsOnLevel += processedChildElements;

                }
            } catch (Exception e) {
                LOGGER.error("Exception occured when processing the cloned " + "configuration", e);
            }
        }
        return processedElementsOnLevel;
    }

    private void cloneConfigurationDTOFromReferringConfiguration(
        final Configuration referringConfiguration, final ConfigurationDTO configurationDTO,
        final ConfigurationDTO parentDTO, final ConfigurationGroupDTO configurationGroupDTO) {
        configurationDTO.setAttribute(referringConfiguration.getAttribute());
        configurationDTO.setConfigurationGroupDTO(configurationGroupDTO);
        configurationDTO.setConfigurationType(referringConfiguration.getConfigurationType());
        configurationDTO.setDescriptionMessageCode(
            referringConfiguration.getDescriptionMessageCode());
        configurationDTO.setEntityClass(referringConfiguration.getEntityClass());
        configurationDTO.setLabelMessageCode(referringConfiguration.getLabelMessageCode());
        configurationDTO.setPosition(referringConfiguration.getPosition());
        configurationDTO.setParent(parentDTO);
        configurationDTO.setTestMethod(referringConfiguration.getTestMethod());
        configurationDTO.setUpdateMethod(referringConfiguration.getUpdateMethod());
        if(referringConfiguration.getClass() == SelectConfiguration.class){
            configurationDTO.setOptions(((SelectConfiguration) referringConfiguration).getOptions());
        }
    }

    private void createNewConfigurationGroupForConfigurationGroupDTO(
        final ConfigurationGroupDTO configurationGroupDTO) {
        ConfigurationGroup newConfigurationGroup = new ConfigurationGroup();
        newConfigurationGroup.setPosition(configurationGroupDTO.getPosition());
        newConfigurationGroup.setName(configurationGroupDTO.getName());
        newConfigurationGroup.setLabelMessageCode(configurationGroupDTO.getLabelMessageCode());
        newConfigurationGroup.setRepeating(configurationGroupDTO.isRepeating());
        List<Configuration> configurations = flattenNestedConfigurationList(
            createConfigurationListForConfigurationDTOs(
                configurationGroupDTO.getConfigurationDTOs(), newConfigurationGroup, null));

        newConfigurationGroup.setConfigurations(configurations);
        configurationGroupDao.merge(newConfigurationGroup);
    }

    private List<Configuration> createConfigurationListForConfigurationDTOs(
        final List<ConfigurationDTO> configurationDTOs,
        final ConfigurationGroup referenceConfigurationGroup, final Configuration parent) {
        List<Configuration> resultList = new ArrayList<>();

        for (ConfigurationDTO configurationDTO : configurationDTOs) {
            Configuration newConfiguration;
            //Handle Select-, Pattern- and usual Configuration cases
            if (configurationDTO.getOptions() != null) {
                newConfiguration= new SelectConfiguration(configurationDTO.getOptions(),null,configurationDTO.getEntityClass(),
                    configurationDTO.getAttribute(),configurationDTO.getValue(), configurationDTO.getConfigurationType(),
                    configurationDTO.getLabelMessageCode(),
                    configurationDTO.getDescriptionMessageCode(), configurationDTO.getTestMethod(),
                    configurationDTO.getUpdateMethod(),configurationDTO.getPosition(),
                    referenceConfigurationGroup);
                resultList.add(newConfiguration);
            } else if (configurationDTO.getPattern() != null && !configurationDTO.getPattern()
                .equalsIgnoreCase("")) {

                newConfiguration= new PatternConfiguration(configurationDTO.getPattern(),null,configurationDTO.getEntityClass(),
                    configurationDTO.getAttribute(),configurationDTO.getValue(), configurationDTO.getConfigurationType(),
                    configurationDTO.getLabelMessageCode(),
                    configurationDTO.getDescriptionMessageCode(), configurationDTO.getTestMethod(),
                    configurationDTO.getUpdateMethod(),configurationDTO.getPosition(),
                    referenceConfigurationGroup);
                resultList.add(newConfiguration);

                resultList.add(newConfiguration);
            } else {
                newConfiguration = new Configuration(configurationDTO.getEntityClass(),
                    configurationDTO.getAttribute(), configurationDTO.getConfigurationType(),
                    configurationDTO.getLabelMessageCode(),
                    configurationDTO.getDescriptionMessageCode(), configurationDTO.getTestMethod(),
                    configurationDTO.getUpdateMethod(), configurationDTO.getPosition(),
                    referenceConfigurationGroup);
                newConfiguration.setValue(configurationDTO.getValue());
                resultList.add(newConfiguration);
            }

            if (parent != null) {
                newConfiguration.setParent(parent);
            }

            if (configurationDTO.getChildren() != null && !configurationDTO.getChildren()
                .isEmpty()) {
                List<Configuration> children = createConfigurationListForConfigurationDTOs(
                    configurationDTO.getChildren(), referenceConfigurationGroup, newConfiguration);
                newConfiguration.setChildren(children);
            }
        }

        return resultList;
    }

    private List<Configuration> flattenNestedConfigurationList(
        final List<Configuration> nestedConfigurations) {
        List<Configuration> resultList = new ArrayList<>();

        for (Configuration configuration : nestedConfigurations) {
            resultList.add(configuration);

            if (configuration.getChildren() != null && !configuration.getChildren().isEmpty()) {
                resultList.addAll(flattenNestedConfigurationList(configuration.getChildren()));
            }
        }

        return resultList;
    }

    private void saveImagesAndFilesInUploadDirectory(final List<MultipartFile> files,
        final ConfigurationComponentDTO configurationComponentDTO, final String recoveryStoragePath,
        final String serverStoragePath, final String contextPath) throws UnsupportedTypeException {
        for (MultipartFile file : files) {
            if (file != null && file.getSize() != 0) {
                Long configurationId = Long.valueOf(file.getName()
                    .substring(file.getName().indexOf('[') + 1, file.getName().indexOf(']')));
                ConfigurationDTO configurationToDelete = new ConfigurationDTO();

                String attribute = "";
                String groupName = "";

                ConfigurationDTO foundDTO = getConfigurationDTOForId(
                    configurationComponentDTO.getConfigurationGroupDTOs(), configurationId);
                if (foundDTO != null) {
                    attribute = foundDTO.getAttribute();
                    groupName = foundDTO.getConfigurationGroupDTO().getName();
                    configurationToDelete = foundDTO;
                }

                String pathPrefix = "configuration/" + attribute;
                String fileExtension = FilenameUtils.getExtension(file.getOriginalFilename());

                // First save all key files
                if (fileExtension.equalsIgnoreCase("pem") || fileExtension.equalsIgnoreCase(
                    "p12")) {
                    // Check if the recoveryStorage dir exists. If not,
                    // create it
                    File uploadDir = new File(recoveryStoragePath + "/keys");
                    if (!uploadDir.isDirectory()) {
                        uploadDir.mkdirs();
                    }
                    // Save the file under the recovery path
                    File keyFile = new File(
                        uploadDir + "/" + groupName + "_" + attribute + "." + fileExtension);
                    try {
                        FileUtils.writeByteArrayToFile(keyFile, file.getBytes());
                        configurationToDelete.setValue(keyFile.getAbsolutePath());
                    } catch (IOException ex) {
                        LOGGER.error("Error writing the keys on the hard drive. Make "
                            + "sure that the path {} exist and is writeable.", keyFile.getPath());
                    }
                } else if (fileExtension.equalsIgnoreCase("png") || fileExtension.equalsIgnoreCase(
                    "jpg") || fileExtension.equalsIgnoreCase("jpeg")
                    || fileExtension.equalsIgnoreCase("svg") || fileExtension.equalsIgnoreCase(
                    "xsd")) {
                    //Then save all images
                    // Check if the recoveryStorage dir exists. If not,
                    // create it
                    File uploadDir = new File(recoveryStoragePath + "/configuration");
                    if (!uploadDir.isDirectory()) {
                        uploadDir.mkdirs();
                    }
                    // Save the file for recovery
                    File recoveryFile =
                        new File(recoveryStoragePath + "/" + pathPrefix + "." + fileExtension);
                    // Check if the serverStorage dir exists. If not, create it
                    uploadDir = new File(serverStoragePath + "/configuration");
                    if (!uploadDir.isDirectory()) {
                        uploadDir.mkdirs();
                    }
                    // Save the file where it is available for the server
                    File configurationFile =
                        new File(serverStoragePath + "/" + pathPrefix + "." + fileExtension);
                    try {
                        FileUtils.writeByteArrayToFile(recoveryFile, file.getBytes());
                        FileUtils.writeByteArrayToFile(configurationFile, file.getBytes());
                        configurationToDelete.setValue(
                            contextPath + "/" + pathPrefix + "." + fileExtension);
                    } catch (IOException ex) {
                        LOGGER.error("Error writing the logo on the hard drive. Make "
                                + "sure that the paths {} and {} exist and are " + "writeable.",
                            recoveryFile.getPath(), configurationFile.getPath());
                    }
                } else {
                    throw new UnsupportedTypeException("File type is not a " + "supported file.");
                }
            }
        }
    }

    private void deleteFilesAndImages(final ConfigurationComponentDTO configurationComponentDTO,
        final String recoveryStoragePath, final String serverStoragePath) {
        ConfigurationDTO configurationToDelete = new ConfigurationDTO();
        for (Long id : configurationComponentDTO.getImageDeleteMap().keySet()) {
            if (configurationComponentDTO.getImageDeleteMap().get(id) != null) {
                if (configurationComponentDTO.getImageDeleteMap().getOrDefault(id, false)
                    && configurationGroupDao.isConfigurationGroupDeletable(id)) {
                    String attribute = "";
                    //Get the attribute for pathPrefix
                    ConfigurationDTO foundDTO = getConfigurationDTOForId(
                        configurationComponentDTO.getConfigurationGroupDTOs(), id);

                    if (foundDTO != null) {
                        attribute = foundDTO.getAttribute();
                        configurationToDelete = foundDTO;
                    }

                    String pathPrefix = "configuration/" + attribute;
                    // Get the recovery file
                    File recoveryFile = new File(recoveryStoragePath + pathPrefix);
                    recoveryFile.delete();
                    // Get the server file
                    File configurationFile =
                        new File(serverStoragePath + "/" + pathPrefix);
                    configurationFile.delete();
                    configurationToDelete.setValue(null);
                }
            }
        }
    }

}
