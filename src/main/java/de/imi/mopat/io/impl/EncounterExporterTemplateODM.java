package de.imi.mopat.io.impl;

import ca.uhn.hl7v2.DefaultHapiContext;
import ca.uhn.hl7v2.HapiContext;
import ca.uhn.hl7v2.app.Connection;
import ca.uhn.hl7v2.app.Initiator;
import ca.uhn.hl7v2.llp.MinLowerLayerProtocol;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.model.v23.datatype.ST;
import ca.uhn.hl7v2.model.v23.group.ORU_R01_PATIENT;
import ca.uhn.hl7v2.model.v23.message.ORU_R01;
import ca.uhn.hl7v2.model.v23.segment.MSH;
import ca.uhn.hl7v2.model.v23.segment.OBR;
import ca.uhn.hl7v2.model.v23.segment.OBX;
import ca.uhn.hl7v2.parser.DefaultXMLParser;
import ca.uhn.hl7v2.parser.PipeParser;
import de.imi.mopat.dao.ConfigurationDao;
import de.imi.mopat.helper.controller.Constants;
import de.imi.mopat.helper.controller.ODMProcessingBean;
import de.imi.mopat.io.EncounterExporterTemplate;
import de.imi.mopat.model.BundleClinic;
import de.imi.mopat.model.Configuration;
import de.imi.mopat.model.Encounter;
import de.imi.mopat.model.ExportTemplate;
import de.imi.mopat.model.enumeration.ExportStatus;
import de.unimuenster.imi.org.cdisc.odm.v132.ODM;
import de.unimuenster.imi.org.cdisc.odm.v132.ODMcomplexTypeDefinitionClinicalData;
import de.unimuenster.imi.org.cdisc.odm.v132.ODMcomplexTypeDefinitionFormData;
import de.unimuenster.imi.org.cdisc.odm.v132.ODMcomplexTypeDefinitionFormDef;
import de.unimuenster.imi.org.cdisc.odm.v132.ODMcomplexTypeDefinitionItemData;
import de.unimuenster.imi.org.cdisc.odm.v132.ODMcomplexTypeDefinitionItemGroupData;
import de.unimuenster.imi.org.cdisc.odm.v132.ODMcomplexTypeDefinitionItemGroupDef;
import de.unimuenster.imi.org.cdisc.odm.v132.ODMcomplexTypeDefinitionItemGroupRef;
import de.unimuenster.imi.org.cdisc.odm.v132.ODMcomplexTypeDefinitionMetaDataVersion;
import de.unimuenster.imi.org.cdisc.odm.v132.ODMcomplexTypeDefinitionSiteRef;
import de.unimuenster.imi.org.cdisc.odm.v132.ODMcomplexTypeDefinitionStudy;
import de.unimuenster.imi.org.cdisc.odm.v132.ODMcomplexTypeDefinitionStudyEventData;
import de.unimuenster.imi.org.cdisc.odm.v132.ODMcomplexTypeDefinitionSubjectData;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.StringWriter;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import jakarta.xml.bind.JAXBContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;

/**
 * The class EncounterExporterTemplateODM generates an ODM file with clinical data from an Encounter
 * provided.
 */
public class EncounterExporterTemplateODM implements EncounterExporterTemplate {

    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(
        EncounterExporterTemplateODM.class);
    private static final String FILE_SUFFIX = "xml";
    private static final String DOT = ".";
    private static final String UNDERSCORE = "_";
    private static final SimpleDateFormat ODMFILENAMEDATEFORMAT = new SimpleDateFormat(
        "dd.MM.yyyy_HH.mm.ss");

    private Encounter encounter;
    private ExportTemplate exportTemplate;
    private ODM exportODM;
    private final ConfigurationDao configurationDao;

    private final ODMProcessingBean odmProcessor;

    // Map to store ItemGroupDefs for the clinical data section (solution for
    // several ItemGroups)
    private HashMap<String, ODMcomplexTypeDefinitionItemGroupData> odmClinicalDataGroupDefs;

    /**
     * Constructor with given {@link ConfigurationDao} to get configuration informations within this
     * instance.
     *
     * @param configurationDao The {@link ConfigurationDao} from the context.
     */
    public EncounterExporterTemplateODM(final ConfigurationDao configurationDao) {
        this.configurationDao = configurationDao;
        this.odmProcessor = new ODMProcessingBean();
    }

    @Override
    public void load(final Encounter encounter, final ExportTemplate exportTemplate)
        throws Exception {
        assert encounter != null : "The Encounter was null";
        assert exportTemplate != null : "The ExportTemplate was null";
        this.encounter = encounter;
        this.exportTemplate = exportTemplate;

        String objectStoragePath = configurationDao.getObjectStoragePath();
        if (objectStoragePath == null) {
            LOGGER.error("[SETUP] No object storage path found. Please provide a "
                    + "value for {} in the {} file", Constants.OBJECT_STORAGE_PATH_PROPERTY,
                Constants.CONFIGURATION);
        } else {
            LOGGER.info("[SETUP] Object storage path configuration found.");
        }
        LOGGER.info("[SETUP] Accessing properties file to look up the export " + "path"
            + " in  {}...[DONE]", Constants.CONFIGURATION);

        String templatePath = objectStoragePath + Constants.EXPORT_TEMPLATE_SUB_DIRECTORY;
        String filename = exportTemplate.getFilename();
        File file = new File(templatePath, filename);

        ODM importedODM = odmProcessor.unmarshal(new FileInputStream(file));

        if (importedODM == null) {
            LOGGER.error("[SETUP] Could not convert template file to ODM " + "object");
        } else {
            LOGGER.info("[SETUP] Successfully converted template file to ODM " + "object.");
        }
        //prepares the ODM export file
        exportODM = new ODM();

        GregorianCalendar gregorianCalendar = new GregorianCalendar();
        DatatypeFactory datatypeFactory = DatatypeFactory.newInstance();
        XMLGregorianCalendar now = datatypeFactory.newXMLGregorianCalendar(gregorianCalendar);

        exportODM.setCreationDateTime(now);
        exportODM.setFileType(importedODM.getFileType());
        exportODM.setODMVersion(importedODM.getODMVersion());
        LOGGER.info("ODM attribute prepared for export.");

        exportODM.setFileOID(encounter.getId() + "_" + exportTemplate.getId());
        exportODM.setDescription(importedODM.getDescription());

        List<ODMcomplexTypeDefinitionStudy> studyList = importedODM.getStudy();
        if (studyList == null || studyList.isEmpty()) {
            LOGGER.debug(
                "The imported ODM did not contain any Study " + "elements" + ". Will reject it.");
            // TODO add error to show to the user
        } else {
            LOGGER.debug("At least one Study element in the imported ODM. "
                + "Will take the first one (1. implementation " + "version).");
            ODMcomplexTypeDefinitionStudy study = studyList.get(0);
            List<ODMcomplexTypeDefinitionMetaDataVersion> metaDataVersionList = study.getMetaDataVersion();
            if (metaDataVersionList == null || metaDataVersionList.isEmpty()) {
                LOGGER.debug("The imported ODM, first Study element, did not "
                    + "contain any MetaDataVersion elements" + "." + " " + "Will reject it.");
                // TODO add error to show to the user
            } else {
                LOGGER.debug("At least one MetaDataVersion element in the "
                    + "imported ODM, first Study element. Will"
                    + " take the first one (1. implementation " + "version).");
                ODMcomplexTypeDefinitionMetaDataVersion metaDataVersion = metaDataVersionList.get(
                    0);
                List<ODMcomplexTypeDefinitionFormDef> formDefList = metaDataVersion.getFormDef();
                if (formDefList == null || formDefList.isEmpty()) {
                    LOGGER.debug("The imported ODM, first Study element, "
                        + "first MetaDataVersion, did not " + "contain any FormDef elements. Will "
                        + "reject it.");
                    // TODO add error to show to the user
                } else {
                    LOGGER.debug("At least one FormDef element in the "
                        + "imported ODM, first Study element, "
                        + "first MetaDataVersion. Will take " + "the first one (1. implementation "
                        + "version).");
                    ODMcomplexTypeDefinitionFormDef formDef = formDefList.get(0);

                    ODMcomplexTypeDefinitionClinicalData clinicalData = new ODMcomplexTypeDefinitionClinicalData();
                    clinicalData.setStudyOID(study.getOID());
                    clinicalData.setMetaDataVersionOID(metaDataVersion.getOID());
                    exportODM.getClinicalData().add(clinicalData);

                    ODMcomplexTypeDefinitionSubjectData subjectData = new ODMcomplexTypeDefinitionSubjectData();
                    subjectData.setSubjectKey(encounter.getCaseNumber());

                    Set<BundleClinic> bundleClinics = encounter.getBundle().getBundleClinics();
                    if (bundleClinics.size() == 1) {
                        ODMcomplexTypeDefinitionSiteRef siteRef = new ODMcomplexTypeDefinitionSiteRef();
                        siteRef.setLocationOID(
                            bundleClinics.iterator().next().getClinic().getName());
                        subjectData.setSiteRef(siteRef);
                    }

                    exportODM.getClinicalData().get(0).getSubjectData().add(subjectData);

                    ODMcomplexTypeDefinitionStudyEventData studyEventData = new ODMcomplexTypeDefinitionStudyEventData();
                    studyEventData.setStudyEventOID(
                        metaDataVersion.getStudyEventDef().get(0).getOID());
                    exportODM.getClinicalData().get(0).getSubjectData().get(0).getStudyEventData()
                        .add(studyEventData);

                    ODMcomplexTypeDefinitionFormData formData = new ODMcomplexTypeDefinitionFormData();
                    formData.setFormOID(formDef.getOID());
                    exportODM.getClinicalData().get(0).getSubjectData().get(0).getStudyEventData()
                        .get(0).getFormData().add(formData);

                    List<ODMcomplexTypeDefinitionItemGroupRef> itemGroupRefList = formDef.getItemGroupRef();
                    if (itemGroupRefList == null) {
                        LOGGER.debug("The FormDef of OID {} did not contain any "
                                + "ItemGroupRefs. Won't create any " + "question(group)s",
                            formDef.getOID());
                    } else {
                        LOGGER.debug("The FormDef of OID {} contains at least one "
                            + "ItemGroupRef. Checking for " + "ItemGroupDefs in MetaDataVersion of "
                            + "OID {} now.", formDef.getOID(), metaDataVersion.getOID());
                        List<ODMcomplexTypeDefinitionItemGroupDef> itemGroupDefList = metaDataVersion.getItemGroupDef();
                        if (itemGroupDefList == null || itemGroupDefList.isEmpty()) {
                            LOGGER.debug("The MetaDataVersion of OID {} did not "
                                    + "contain any ItemGroupDefs. " + "Cannot follow the referenced "
                                    + "ItemGroupRefs. Will create an " + "error message and finish.",
                                metaDataVersion.getOID());
                        } else {
                            LOGGER.debug("MetaDataVersion of OID {} contains "
                                    + "at least one ItemGroupDef. " + "Will iterate over them and "
                                    + "the ItemGroupRefs in " + "FormDef of OID {} to get "
                                    + "referenced question(group)s.", metaDataVersion.getOID(),
                                formDef.getOID());
                            Map<ODMcomplexTypeDefinitionItemGroupRef, ODMcomplexTypeDefinitionItemGroupDef> matchingItemGroupRefDefs = new HashMap<>();
                            List<ODMcomplexTypeDefinitionItemGroupRef> matchedItemGroupRefs = new ArrayList<>();
                            List<ODMcomplexTypeDefinitionItemGroupDef> matchedItemGroupDefs = new ArrayList<>();
                            for (ODMcomplexTypeDefinitionItemGroupRef itemGroupRef : itemGroupRefList) {
                                String refItemGroupOID = itemGroupRef.getItemGroupOID();
                                LOGGER.debug("Now checking the ItemGroupRef of " + "OID" + " {}.",
                                    refItemGroupOID);
                                boolean itemGroupRefFoundInItemGroupDef = false;
                                for (ODMcomplexTypeDefinitionItemGroupDef itemGroupDef : itemGroupDefList) {
                                    LOGGER.debug("Now checking the ItemGroupDef "
                                            + "with OID {} for equality" + " with ItemGroupRef of "
                                            + "ItemOID {}", itemGroupDef.getOID(),
                                        itemGroupRef.getItemGroupOID());
                                    if (refItemGroupOID.equalsIgnoreCase(itemGroupDef.getOID())) {
                                        LOGGER.debug(
                                            "ItemGroupDef with OID {} " + "matched the reffered"
                                                + " OID. Will put it " + "into the list of "
                                                + "ItemGroupDefs to " + "make a QuestionGroup"
                                                + " out of", itemGroupDef.getOID());
                                        itemGroupRefFoundInItemGroupDef = true;
                                        matchingItemGroupRefDefs.put(itemGroupRef, itemGroupDef);
                                        matchedItemGroupRefs.add(itemGroupRef);
                                        matchedItemGroupDefs.add(itemGroupDef);
                                        break;
                                    }
                                }
                                if (!itemGroupRefFoundInItemGroupDef) {
                                    LOGGER.debug("Iteration over " + "ItemGroupDefs in "
                                            + "MetaDataVersion of " + "OID {} done. No "
                                            + "ItemGroupDef for " + "the ItemGroupRefOID"
                                            + " {} could be found." + " The ItemGroupRef"
                                            + "/-Def will not be " + "converted" + "/considered.",
                                        metaDataVersion.getOID(), itemGroupRef.getItemGroupOID());
                                }
                            }
                            LOGGER.debug(
                                "Iteration over ItemGroupRefs in " + "FormDef of OID {} and "
                                    + "ItemGroupDefs in " + "MetaDataVersion of OID {} "
                                    + "done. Size of matched " + "ItemGroupDefs: {}. Will now"
                                    + " check for OrderNumbers", formDef.getOID(),
                                metaDataVersion.getOID(), matchingItemGroupRefDefs.size());
                            boolean orderNumberForAllItemGroupRefsPresent = true;
                            Map<Integer, ODMcomplexTypeDefinitionItemGroupDef> orderedItemGroupDefs = new HashMap<>();
                            for (ODMcomplexTypeDefinitionItemGroupRef itemGroupRef : matchedItemGroupRefs) {
                                LOGGER.debug("Now checking for an OrderNumber for "
                                    + "ItemGroupRef of OID {}", itemGroupRef.getItemGroupOID());
                                BigInteger orderNumber = itemGroupRef.getOrderNumber();
                                if (orderNumber == null) {
                                    LOGGER.debug(
                                        "The ItemGroupRef with OID {} " + "does not have an "
                                            + "OrderNumber, so I'll " + "write an error message, "
                                            + "but continue in the " + "order as given in the "
                                            + "odm file", itemGroupRef.getItemGroupOID());
                                    orderNumberForAllItemGroupRefsPresent = false;
                                } else {
                                    LOGGER.debug("The ItemGroupRef with OID " + "{} has an "
                                            + "OrderNumber, so " + "I" + "'ll try to put the" + " "
                                            + "referenced " + "ItemGroupDef onto "
                                            + "its position ({}) " + "in the List",
                                        itemGroupRef.getItemGroupOID(), orderNumber.intValue());
                                    if (orderedItemGroupDefs.get(orderNumber.intValue()) == null) {
                                        LOGGER.debug(
                                            "The List of " + "ItemGroupDefs " + "has a free spot"
                                                + " at position" + "/OrderNumber " + "{}, so I'll "
                                                + "just put the " + "ItemGroupDef of"
                                                + " OID {} there.", orderNumber.intValue(),
                                            itemGroupRef.getItemGroupOID());
                                        orderedItemGroupDefs.put(orderNumber.intValue(),
                                            matchingItemGroupRefDefs.get(itemGroupRef));
                                    } else {
                                        LOGGER.debug(
                                            "The List of ItemGroupDefs " + "already has an "
                                                + "ItemGroupDef at " + "position/OrderNumber"
                                                + " {}, which does not " + "comply to the ODM "
                                                + "standard. Will not " + "consider "
                                                + "orderNumbers in the " + "end.",
                                            orderNumber.intValue());
                                        orderNumberForAllItemGroupRefsPresent = false;
                                    }
                                }
                            }
                            List<ODMcomplexTypeDefinitionItemGroupDef> itemGroupDefListToIterateOver;
                            if (orderNumberForAllItemGroupRefsPresent) {
                                LOGGER.debug("All ItemGroupDefs of " + "MetaDataVersion of OID "
                                        + "{} that were referenced" + " in FormDef of OID {} "
                                        + "have a unique " + "OrderNumber in their "
                                        + "ItemGroupRef. Thus, I " + "will consider the "
                                        + "OrderNumbers when " + "converting to Question" + "(group)s",
                                    metaDataVersion.getOID(), formDef.getOID());
                                List<Integer> orderNumbers = new ArrayList<>(
                                    orderedItemGroupDefs.keySet());
                                Collections.sort(orderNumbers);
                                itemGroupDefListToIterateOver = new ArrayList<>();
                                for (Integer orderNumber : orderNumbers) {
                                    itemGroupDefListToIterateOver.add(
                                        orderedItemGroupDefs.get(orderNumber));
                                }
                            } else {
                                LOGGER.debug("Not all ItemGroupDefs of " + "MetaDataVersion of OID "
                                        + "{} that were referenced" + " in FormDef of OID {} "
                                        + "have a unique " + "OrderNumber in their "
                                        + "ItemGroupRef. Thus, I " + "will not consider the "
                                        + "OrderNumbers when " + "converting to Question" + "(group)s",
                                    metaDataVersion.getOID(), formDef.getOID());
                                itemGroupDefListToIterateOver = matchedItemGroupDefs;
                            }

                            odmClinicalDataGroupDefs = new HashMap<>();

                            LOGGER.debug("Collection of all necessary info for "
                                    + "creating the item data groups. " + "Will now iterate over the "
                                    + "itemGroupDefList and create the " + "ItemGroupDatas.",
                                formDef.getOID());
                            for (ODMcomplexTypeDefinitionItemGroupDef itemGroupDef : itemGroupDefListToIterateOver) {
                                ODMcomplexTypeDefinitionItemGroupData itemGroupData = new ODMcomplexTypeDefinitionItemGroupData();
                                itemGroupData.setItemGroupOID(itemGroupDef.getOID());
                                exportODM.getClinicalData().get(0).getSubjectData().get(0)
                                    .getStudyEventData().get(0).getFormData().get(0)
                                    .getItemGroupData().add(itemGroupData);

                            }
                            for (ODMcomplexTypeDefinitionItemGroupData item : exportODM.getClinicalData()
                                .get(0).getSubjectData().get(0).getStudyEventData().get(0)
                                .getFormData().get(0).getItemGroupData()) {
                                odmClinicalDataGroupDefs.put(item.getItemGroupOID(), item);
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public void write(final String exportField, final String value) throws Exception {
        String exportClean = exportField.replace("u002E", ".");
        // Split the exportField into splitExportField[0] (ItemGroupOID),
        // splitExportField[1] (ItemOID) and maybe splitExportField[2]
        // (CodedValue or Boolean)
        String[] splitExportField = exportClean.split("_");
        for (int i = 0; i < splitExportField.length; i++) {
            splitExportField[i] = splitExportField[i].replace("u005F", "_");
        }

        // Create a new item data object with the given ItemOID
        ODMcomplexTypeDefinitionItemData newItemData = new ODMcomplexTypeDefinitionItemData();
        newItemData.setItemOID(splitExportField[1]);

        // If the export field is a string, float, date or text value
        if (splitExportField.length < 3) {
            // Write the answer to the ItemGroup with the given ItemGroupOID
            newItemData.setValue(value);
            odmClinicalDataGroupDefs.get(splitExportField[0]).getItemDataGroup().add(newItemData);
        } // If the export field is a multiple choice or boolean value
        else if (splitExportField.length == 3) {
            // And if the answer was checked
            if (value.equals("TRUE")) {
                // And if the export field is a boolean value, write the
                // boolean answer to the item data value
                if (splitExportField[2].equals("TRUE")) {
                    newItemData.setValue("true");
                } else if (splitExportField[2].equals("FALSE")) {
                    newItemData.setValue("false");
                } // If the export field is a multiple choice value, write
                // the CodedValue to the item data value
                else {
                    newItemData.setValue(splitExportField[2]);
                }
                // Write the answer to the ItemGroup with the given ItemGroupOID
                odmClinicalDataGroupDefs.get(splitExportField[0]).getItemDataGroup()
                    .add(newItemData);
            }
            //otherwise the answer was not checked and it must not be stored
        } else {
            LOGGER.info("An Error occurred: The string has more than 3 " + "sections");
        }
    }

    @Override
    public ExportStatus flush() throws Exception {
        Boolean exportInDirectory = null;
        String exportPath = null;
        Boolean exportViaRest = null;
        String exportUrl = null;
        Boolean exportViaHL7 = null;
        String hl7Hostname = null;
        Integer hl7Port = null;

        // Get export configurations
        for (Configuration configuration : exportTemplate.getConfigurationGroup()
            .getConfigurations()) {
            if (configuration.getAttribute().equals("exportInDirectory")) {
                exportInDirectory = Boolean.parseBoolean(configuration.getValue());
            }
            if (configuration.getAttribute().equals("exportPath")) {
                exportPath = configuration.getValue();
            }
            if (configuration.getAttribute().equals("exportViaRest")) {
                exportViaRest = Boolean.parseBoolean(configuration.getValue());
            }
            if (configuration.getAttribute().equals("exportUrl")) {
                exportUrl = configuration.getValue();
            }
            if (configuration.getAttribute().equals("exportODMviaHL7")) {
                exportViaHL7 = Boolean.parseBoolean(configuration.getValue());
            }
            if (configuration.getAttribute().equals("ODMviaHL7Hostname")) {
                hl7Hostname = configuration.getValue();
            }
            if (configuration.getAttribute().equals("ODMviaHL7Port")) {
                try {
                    hl7Port = Integer.parseInt(configuration.getValue());
                } catch (NumberFormatException numberFormatException) {
                    hl7Port = null;
                }
            }
        }

        if (exportInDirectory) {
            exportToDirectory(exportPath);
        }

        ExportStatus exportStatus = ExportStatus.SUCCESS;
        if (exportViaRest) {
            exportStatus = exportToHTTP(exportUrl);
        }
        if (exportViaHL7 && hl7Hostname != null && !hl7Hostname.isEmpty() && hl7Port != null) {
            exportStatus = exportViaHL7(hl7Hostname, hl7Port);
        }
        return exportStatus;
    }

    /**
     * Exports the resultant ODM to a given path.
     *
     * @param exportPath Export path for the ODM file
     * @throws java.lang.Exception if a problem occurs
     */
    public void exportToDirectory(final String exportPath) throws Exception {
        // Make sure the path exists
        File path = new File(exportPath);
        if (!path.isDirectory()) {
            path.mkdirs();
        }

        //Create a sub-directory for the exported files
        String filepath = exportPath + File.separator + exportTemplate.getQuestionnaire().getName()
            .replaceAll(":", "_") + "/" + exportTemplate.getName().replaceAll(":", "_") + "/";
        File subDirectory = new File(filepath);
        if (!subDirectory.isDirectory()) {
            subDirectory.mkdirs();
        }

        // Write to disk
        File exportFile = new File(subDirectory, this.createODMFileName());
        odmProcessor.marshal(exportODM, exportFile);
    }

    /**
     * Exports the resultant ODM to a URL via REST-Interface.
     *
     * @param stringURL Export URL for the REST export
     * @return ExportStatus can be SUCCESSFUL, CONFLICT or FAILURE
     * @throws java.lang.Exception if a problem occurs
     */
    public ExportStatus exportToHTTP(final String stringURL) throws Exception {
        // 1. Set the destination URL from the string
        URL url = new URL(stringURL);

        // 2. Open connection and set connection timeout to 30 seconds
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setConnectTimeout(30000);

        // 3. Specify POST method
        con.setRequestMethod("POST");

        // 4. Set the Content-Type
        con.setRequestProperty("Content-Type", "text/xml");

        con.setDoOutput(true);

        // 5. Add XML data into POST request body and send the message
        // 5.1 Get connection output stream
        DataOutputStream httpMessage = new DataOutputStream(con.getOutputStream());

        // 5.2 Write the message into the output stream
        odmProcessor.marshal(exportODM, httpMessage);

        // 5.3 Send the request
        httpMessage.flush();

        // 5.4 close
        httpMessage.close();
        // Log the URL for the REST-Interface without parameters
        LOGGER.info("[FLUSH] Sending message to REST-Interface to URL {}",
            url.toString().substring(0, url.toString().indexOf("?")));
        LOGGER.info("[FLUSH] Got response with response code {} ({}) ", con.getResponseCode(),
            con.getResponseMessage());

        switch (con.getResponseCode()) {
            case 200:
                LOGGER.info("Successfully exported ODM via REST");
                return ExportStatus.SUCCESS;
            case 409:
                LOGGER.error("Could not export ODM via REST: " + con.getResponseCode()
                    + con.getResponseMessage());
                return ExportStatus.CONFLICT;
            default:
                LOGGER.error("Could not export ODM via REST: " + con.getResponseCode()
                    + con.getResponseMessage());
                return ExportStatus.FAILURE;
        }
    }

    /**
     * Injects the ODM file in a HL7 message and sends it to a communication server, which is
     * specified by the hostname and the port.
     *
     * @param hostname Hostname of the HL7 communication server.
     * @param port     Port of the HL7 communication server.
     * @return Status of the communication.
     * @throws Exception
     */
    private ExportStatus exportViaHL7(final String hostname, final Integer port) throws Exception {
        ORU_R01 hl7Message = new ORU_R01();

        LOGGER.info("[ODM via HL7] Creating a HL7 message to send patient " + "data...");
        // Message initialization
        hl7Message.initQuickstart("ORU", "R01", "P");

        // Include export template path
        String objectStoragePath = configurationDao.getObjectStoragePath();
        if (objectStoragePath == null) {
            LOGGER.error("[SETUP] No object storage path found. Please provide a "
                    + "value for {} in the {} file", Constants.OBJECT_STORAGE_PATH_PROPERTY,
                Constants.CONFIGURATION);
        } else {
            LOGGER.info("[SETUP] Object storage path configuration found.");
        }
        LOGGER.info("[SETUP] Accessing properties file to look up the export " + "path"
            + " in  {}...[DONE]", Constants.CONFIGURATION);

        // Set HL7 message header
        MSH msh = hl7Message.getMSH();

        for (Configuration configuration : exportTemplate.getConfigurationGroup()
            .getConfigurations()) {
            if (configuration.getAttribute().equals("ODMviaHL7SendingFacility")) {
                msh.getMsh4_SendingFacility().getHd1_NamespaceID()
                    .setValue(configuration.getValue());
            }
            if (configuration.getAttribute().equals("ODMviaHL7ReceivingApplication")) {
                msh.getMsh5_ReceivingApplication().getHd1_NamespaceID()
                    .setValue(configuration.getValue());
            }
            if (configuration.getAttribute().equals("ODMviaHL7ReveivingFacility")) {
                msh.getMsh6_ReceivingFacility().getHd1_NamespaceID()
                    .setValue(configuration.getValue());
            }
        }

        msh.getMsh3_SendingApplication().getHd1_NamespaceID()
            .setValue(exportTemplate.getQuestionnaire().getName());
        msh.getMessageControlID().setValue(encounter.getId() + "_" + exportTemplate.getId());
        msh.getMsh14_ContinuationPointer().setValue("L");

        LOGGER.info("Creating Patient data");
        ORU_R01_PATIENT patientData = hl7Message.getRESPONSE().getPATIENT();
        if (encounter.getPatientID() != null) {
            patientData.getPID().getPatientIDInternalID(0).getCx1_ID()
                .setValue(Long.toString(encounter.getPatientID()));
        } else {
            LOGGER.error("The patient does not have an ID");
        }
        if (encounter.getCaseNumber() != null) {
            patientData.getVISIT().getPV1().getPv119_VisitNumber().getCx1_ID()
                .setValue(encounter.getCaseNumber());
        } else {
            LOGGER.error("The patient does not have a case number");
        }

        LOGGER.info("Creating Observation data");

        OBR obr = hl7Message.getRESPONSE().getORDER_OBSERVATION().getOBR();
        for (Configuration configuration : exportTemplate.getConfigurationGroup()
            .getConfigurations()) {
            if (configuration.getAttribute().equals("ODMviaHL7OBRFillerOrderNumber")) {
                obr.getObr3_FillerOrderNumber().getEi1_EntityIdentifier()
                    .setValue(configuration.getValue());
            }
        }

        DateFormat dateFormatter = new SimpleDateFormat("yyyyMMddhhmm");
        obr.getObr7_ObservationDateTime().getTimeOfAnEvent()
            .setValue(dateFormatter.format(encounter.getStartTime()));

        OBX obx = hl7Message.getRESPONSE().getORDER_OBSERVATION().getOBSERVATION(0).getOBX();
        obx.getObx1_SetIDOBX().setValue("1");
        obx.getObx2_ValueType().setValue("ST");
        ST clinicalDataString = new ST(hl7Message);
        obx.getObservationValue(0).setData(clinicalDataString);
        obx.getObx10_NatureOfAbnormalTest().setValue("F");

        // This code is adapted from http://www.mkyong.com/java/how-to-read-xml-file-in-java-dom-parser/
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder;
        dBuilder = dbFactory.newDocumentBuilder();
        // Load inputStream into w3c Document object..
        Document document = dBuilder.newDocument();
        document.setXmlStandalone(true);

        JAXBContext ctx = JAXBContext.newInstance(exportODM.getClass());
        ctx.createMarshaller().marshal(exportODM, document);

        // Transform doc to string
        // Text copied from http://www.journaldev.com/1237/java-convert-string-to-xml-document-and-xml-document-to-string
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer;
        try {
            transformer = transformerFactory.newTransformer();
            // Remove XML declaration
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource(document), new StreamResult(writer));
            String output = writer.getBuffer().toString();
            // Replace spaces and new lines
            output = output.replaceAll(" ", "u005F").replaceAll("\\s", "");
            // Restore replaced spaces and set value
            clinicalDataString.setValue(output.replaceAll("u005F", " "));
        } catch (TransformerException exception) {
            LOGGER.error(exception.toString());
            return ExportStatus.FAILURE;
        }
        sendMessageViaComServer(hostname, port, hl7Message);

        return ExportStatus.SUCCESS;
    }

    /**
     * Creates a unique ODM XML Filename.
     *
     * @return The newly created unique ODM XML Filename.
     */
    private String createODMFileName() {
        String result =
            encounter.getCaseNumber() + UNDERSCORE + exportTemplate.getOriginalFilename()
                + UNDERSCORE + ODMFILENAMEDATEFORMAT.format(new Date()) + DOT + FILE_SUFFIX;
        return result;
    }

    /**
     * Exports the HL7 message via a communication server specified by host and port.
     *
     * @param hostname Host name of the communication server.
     * @param port     Port of the communication server
     * @throws java.lang.Exception
     */
    private void sendMessageViaComServer(final String hostname, final Integer port,
        final ORU_R01 hl7Message) throws Exception {
        // Set up a context: factory for connections and parsers and so on
        HapiContext context = new DefaultHapiContext();
        MinLowerLayerProtocol mllp = new MinLowerLayerProtocol();
        mllp.setCharset("ISO-8859-1");
        context.setLowerLayerProtocol(mllp);
        // Let the default Pipe parser parse our message
        PipeParser parser = context.getPipeParser();
        LOGGER.debug("[ODM via HL7] HL7 message created: {}", parser.encode(hl7Message));
        LOGGER.debug("Opening a Connection for HL7 messaging...");
        // Open a new connection with the given hostname, port, and
        // don't use TLS
        Connection connection = context.newClient(hostname, port, false);
        Initiator initiator = connection.getInitiator();
        LOGGER.debug("Opening a Connection for HL7 messaging...[DONE]");
        LOGGER.debug("Sending HL7 message...");
        initiator.setTimeout(30, TimeUnit.SECONDS);
        Message response = initiator.sendAndReceive(hl7Message);
        // Log the ACK which is an empty message (OPTIONAL)
        LOGGER.info((new DefaultXMLParser()).encode(response));
        LOGGER.debug("Sending HL7 message...[DONE]");
        connection.close();
    }
}
