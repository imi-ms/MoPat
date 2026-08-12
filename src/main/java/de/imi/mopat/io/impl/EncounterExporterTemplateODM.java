package de.imi.mopat.io.impl;

import de.imi.mopat.dao.ConfigurationDao;
import de.imi.mopat.helper.controller.Constants;
import de.imi.mopat.helper.controller.DocumentParser;
import de.imi.mopat.io.EncounterExporterTemplate;
import de.imi.mopat.io.importer.odm.ODMProcessingBean;
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
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.StringWriter;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.parsers.ParserConfigurationException;
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
    private final ConfigurationDao configurationDao;
    private final ODMProcessingBean odmProcessor;
    private Encounter encounter;
    private ExportTemplate exportTemplate;
    private ODM exportODM;
    // Map to store ItemGroupDefs for the clinical data section (solution for
    // several ItemGroups)
    private HashMap<String, ODMcomplexTypeDefinitionItemGroupData> odmClinicalDataGroupDefs;
    private final DocumentParser documentParser = new DocumentParser();

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
        String sendingFacility = null;
        String receivingApplication = null;
        String receivingFacility = null;
        String obrFillerOrderNumber = null;

        // Get export configurations
        for (Configuration configuration : exportTemplate.getConfigurationGroup()
            .getConfigurations()) {
            switch (configuration.getAttribute()) {
                case "exportInDirectory":
                    exportInDirectory = Boolean.parseBoolean(configuration.getValue());
                    break;
                case "exportPath":
                    exportPath = configuration.getValue();
                    break;
                case "exportViaRest":
                    exportViaRest = Boolean.parseBoolean(configuration.getValue());
                    break;
                case "exportUrl":
                    exportUrl = configuration.getValue();
                    break;
                case "exportODMviaHL7":
                    exportViaHL7 = Boolean.parseBoolean(configuration.getValue());
                    break;
                case "ODMviaHL7Hostname":
                    hl7Hostname = configuration.getValue();
                    break;
                case "ODMviaHL7Port":
                    try {
                        hl7Port = Integer.parseInt(configuration.getValue());
                    } catch (NumberFormatException numberFormatException) {
                        hl7Port = null;
                    }
                    break;
                case "ODMviaHL7SendingFacility":
                    sendingFacility = configuration.getValue();
                    break;
                case "ODMviaHL7ReceivingApplication":
                    receivingApplication = configuration.getValue();
                    break;
                case "ODMviaHL7ReceivingFacility":
                    receivingFacility = configuration.getValue();
                    break;
                case "ODMviaHL7OBRFillerOrderNumber":
                    obrFillerOrderNumber = configuration.getValue();
                    break;
                default:
                    break;
            }
        }

        if (exportInDirectory != null && exportInDirectory) {
            exportToDirectory(exportPath);
        }

        ExportStatus exportStatus = ExportStatus.SUCCESS;
        if (exportViaRest) {
            exportStatus = exportToHTTP(exportUrl);
        }
        if (exportViaHL7 != null && exportViaHL7 && hl7Hostname != null && !hl7Hostname.isEmpty()
            && hl7Port != null && sendingFacility != null && receivingApplication != null
            && receivingFacility != null && obrFillerOrderNumber != null) {
            HL7MessageHelper hl7MessageHelper = new HL7MessageHelper();
            exportStatus = hl7MessageHelper.createAndSendMessageWithBlob(hl7Hostname, hl7Port, exportTemplate,
                encounter, sendingFacility, receivingFacility, receivingFacility,
                obrFillerOrderNumber, generateODMBlob());
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

    private String generateODMBlob() throws ParserConfigurationException, JAXBException {
        // This code is adapted from http://www.mkyong.com/java/how-to-read-xml-file-in-java-dom-parser/
        // Load inputStream into w3c Document object..
        Document document = documentParser.newDocument();
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
            return output.replaceAll("u005F", " ");
        } catch (TransformerException exception) {
            LOGGER.error(exception.toString());
            return null;
        }
    }

}
