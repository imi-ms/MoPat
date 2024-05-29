package de.imi.mopat.io.impl;

import de.imi.mopat.helper.controller.ODMProcessingBean;
import de.imi.mopat.io.ExportTemplateImporter;
import de.unimuenster.imi.org.cdisc.odm.v132.DataType;
import de.unimuenster.imi.org.cdisc.odm.v132.ODM;
import de.unimuenster.imi.org.cdisc.odm.v132.ODMcomplexTypeDefinitionCodeList;
import de.unimuenster.imi.org.cdisc.odm.v132.ODMcomplexTypeDefinitionCodeListItem;
import de.unimuenster.imi.org.cdisc.odm.v132.ODMcomplexTypeDefinitionCodeListRef;
import de.unimuenster.imi.org.cdisc.odm.v132.ODMcomplexTypeDefinitionFormDef;
import de.unimuenster.imi.org.cdisc.odm.v132.ODMcomplexTypeDefinitionItemDef;
import de.unimuenster.imi.org.cdisc.odm.v132.ODMcomplexTypeDefinitionItemGroupDef;
import de.unimuenster.imi.org.cdisc.odm.v132.ODMcomplexTypeDefinitionItemGroupRef;
import de.unimuenster.imi.org.cdisc.odm.v132.ODMcomplexTypeDefinitionItemRef;
import de.unimuenster.imi.org.cdisc.odm.v132.ODMcomplexTypeDefinitionMetaDataVersion;
import de.unimuenster.imi.org.cdisc.odm.v132.ODMcomplexTypeDefinitionStudy;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.parsers.ParserConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

/**
 * An instance of the class {@link ExportTemplateImporterODM} shall be used to provide the structure
 * of ODM-files. It is designed to load an XML file according to the structure provided by the model
 * of the CDISC ODM-standard.
 */
public class ExportTemplateImporterODM implements ExportTemplateImporter {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExportTemplateImporterODM.class);

    private final ODMProcessingBean odmReader = new ODMProcessingBean();

    /**
     * importFile loads an XML-File and goes done a tree hierarchy in the ODM-File to construct and
     * load tags. All needed fields will be added do {@code List<String>} oIds for @return. File
     * will be rejected, if no Study elements, MetaDataVersion elements or FormDef elements can be
     * found.
     *
     * @param inputStream {@link InputStream} containing the uploaded file from
     *                    {@link
     *                    de.imi.mopat.controller.ExportMappingController#handleUpload(java.lang.Long,
     *                    java.lang.String, org.springframework.web.multipart.MultipartFile,
     *                    java.lang.String, de.imi.mopat.model.ExportTemplate,
     *                    org.springframework.validation.BindingResult,
     *                    jakarta.servlet.http.HttpServletRequest, org.springframework.ui.Model) }.
     *                    Must not be <code>null</code>.
     * @return {@code List<String>} aggregatedExportFields. Aggregated strings to specify the export
     * fields. The structure of these Strings is as follows ItemGroupOID_ItemOID(_[CodedValue | true
     * | false])
     * @throws IOException                  If an I/O error occurs
     * @throws SAXException                 Error parsing the XML stream.
     * @throws ParserConfigurationException if there is an error in the config
     */
    @Override
    public List<String> importFile(InputStream inputStream)
        throws IOException, SAXException, ParserConfigurationException {
        List<String> aggregatedExportFields = new ArrayList<>();
        try {
            ODM importedODM = odmReader.unmarshal(inputStream);

            List<ODMcomplexTypeDefinitionStudy> studyList = importedODM.getStudy();
            if (studyList == null || studyList.isEmpty()) {
                LOGGER.debug(
                    "The imported ODM did not contain any Study " + "elements. Will reject it.");
                // TODO add error to show to the user
            } else {
                LOGGER.debug("At least one Study element in the imported ODM."
                    + " Will take the first one (1. " + "implementation version).");
                ODMcomplexTypeDefinitionStudy study = studyList.get(0);
                List<ODMcomplexTypeDefinitionMetaDataVersion> metaDataVersionList = study.getMetaDataVersion();
                if (metaDataVersionList == null || metaDataVersionList.isEmpty()) {
                    LOGGER.debug("The imported ODM, first Study element, did "
                        + "not contain any MetaDataVersion " + "elements. Will reject it.");
                    // TODO add error to show to the user
                } else {
                    LOGGER.debug("At least one MetaDataVersion element in the"
                        + " imported ODM, first Study " + "element. Will take the first one "
                        + "(1. implementation version).");
                    ODMcomplexTypeDefinitionMetaDataVersion metaDataVersion = metaDataVersionList.get(
                        0);
                    List<ODMcomplexTypeDefinitionFormDef> formDefList = metaDataVersion.getFormDef();
                    if (formDefList == null || formDefList.isEmpty()) {
                        LOGGER.debug("The imported ODM, first Study element, "
                            + "first MetaDataVersion, did " + "not contain any FormDef "
                            + "elements. Will reject it.");
                        // TODO add error to show to the user
                    } else {
                        LOGGER.debug(
                            "At least one FormDef element in the " + "imported ODM, first Study "
                                + "element, first " + "MetaDataVersion. Will take "
                                + "the first one (1. " + "implementation version).");
                        ODMcomplexTypeDefinitionFormDef formDef = formDefList.get(0);

                        List<ODMcomplexTypeDefinitionItemGroupRef> itemGroupRefList = formDef.getItemGroupRef();
                        if (itemGroupRefList == null) {
                            LOGGER.debug("The FormDef of OID {} did not contain "
                                    + "any ItemGroupRefs. Won't " + "create any question(group)s",
                                formDef.getOID());
                        } else {
                            LOGGER.debug("The FormDef of OID {} contains at least "
                                    + "one ItemGroupRef. Checking for" + " ItemGroupDefs in "
                                    + "MetaDataVersion of OID {} now.", formDef.getOID(),
                                metaDataVersion.getOID());
                            List<ODMcomplexTypeDefinitionItemGroupDef> itemGroupDefList = metaDataVersion.getItemGroupDef();
                            if (itemGroupDefList == null || itemGroupDefList.isEmpty()) {
                                LOGGER.debug(
                                    "The MetaDataVersion of OID {} did " + "not contain any "
                                        + "ItemGroupDefs. Cannot " + "follow the referenced "
                                        + "ItemGroupRefs. Will create" + " an error message and "
                                        + "finish.", metaDataVersion.getOID());
                            } else {
                                LOGGER.debug("MetaDataVersion of OID {} " + "contains at least one"
                                        + " ItemGroupDef. Will " + "iterate over them and"
                                        + " the ItemGroupRefs in" + " FormDef of OID {} to"
                                        + " get referenced " + "question(group)s.",
                                    metaDataVersion.getOID(), formDef.getOID());
                                Map<ODMcomplexTypeDefinitionItemGroupRef, ODMcomplexTypeDefinitionItemGroupDef> matchingItemGroupRefDefs = new HashMap<>();
                                List<ODMcomplexTypeDefinitionItemGroupRef> matchedItemGroupRefs = new ArrayList<>();
                                List<ODMcomplexTypeDefinitionItemGroupDef> matchedItemGroupDefs = new ArrayList<>();
                                for (ODMcomplexTypeDefinitionItemGroupRef itemGroupRef : itemGroupRefList) {
                                    String refItemGroupOID = itemGroupRef.getItemGroupOID();
                                    LOGGER.debug("Now checking the ItemGroupRef of" + " OID {}.",
                                        refItemGroupOID);
                                    boolean itemGroupRefFoundInItemGroupDef = false;
                                    for (ODMcomplexTypeDefinitionItemGroupDef itemGroupDef : itemGroupDefList) {
                                        LOGGER.debug("Now checking the " + "ItemGroupDef with "
                                                + "OID {} for " + "equality with " + "ItemGroupRef of "
                                                + "ItemOID {}", itemGroupDef.getOID(),
                                            itemGroupRef.getItemGroupOID());
                                        if (refItemGroupOID.equalsIgnoreCase(
                                            itemGroupDef.getOID())) {
                                            LOGGER.debug(
                                                "ItemGroupDef with OID {}" + " matched the "
                                                    + "reffered OID. " + "Will put it "
                                                    + "into the list " + "of " + "ItemGroupDefs "
                                                    + "to make a " + "QuestionGroup " + "out of",
                                                itemGroupDef.getOID());
                                            itemGroupRefFoundInItemGroupDef = true;
                                            matchingItemGroupRefDefs.put(itemGroupRef,
                                                itemGroupDef);
                                            matchedItemGroupRefs.add(itemGroupRef);
                                            matchedItemGroupDefs.add(itemGroupDef);
                                            break;
                                        }
                                    }
                                    if (!itemGroupRefFoundInItemGroupDef) {
                                        LOGGER.debug("Iteration over " + "ItemGroupDefs" + " in "
                                                + "MetaDataVersion" + " of OID {} " + "done. No "
                                                + "ItemGroupDef " + "for the " + "ItemGroupRefOID"
                                                + " {} could be " + "found. The " + "ItemGroupRef"
                                                + "/-Def will " + "not be " + "converted"
                                                + "/considered.", metaDataVersion.getOID(),
                                            itemGroupRef.getItemGroupOID());
                                    }
                                }
                                LOGGER.debug(
                                    "Iteration over ItemGroupRefs in" + " FormDef of OID {} "
                                        + "and ItemGroupDefs in " + "MetaDataVersion of "
                                        + "OID {} done. Size of " + "matched "
                                        + "ItemGroupDefs: {}. " + "Will now check for "
                                        + "OrderNumbers", formDef.getOID(),
                                    metaDataVersion.getOID(), matchingItemGroupRefDefs.size());
                                boolean orderNumberForAllItemGroupRefsPresent = true;
                                Map<Integer, ODMcomplexTypeDefinitionItemGroupDef> orderedItemGroupDefs = new HashMap<>();
                                for (ODMcomplexTypeDefinitionItemGroupRef itemGroupRef : matchedItemGroupRefs) {
                                    LOGGER.debug(
                                        "Now checking for an OrderNumber " + "for ItemGroupRef of "
                                            + "OID {}", itemGroupRef.getItemGroupOID());
                                    BigInteger orderNumber = itemGroupRef.getOrderNumber();
                                    if (orderNumber == null) {
                                        LOGGER.debug(
                                            "The ItemGroupRef with OID {}" + " does not have an "
                                                + "OrderNumber, so " + "I'll write an "
                                                + "error message, but" + " continue in the "
                                                + "order as given in " + "the odm file",
                                            itemGroupRef.getItemGroupOID());
                                        orderNumberForAllItemGroupRefsPresent = false;
                                    } else {
                                        LOGGER.debug("The ItemGroupRef with " + "OID {} has an"
                                                + " OrderNumber," + " so I'll try " + "to put the "
                                                + "referenced " + "ItemGroupDef " + "onto its "
                                                + "position ({})" + " in the List",
                                            itemGroupRef.getItemGroupOID(), orderNumber.intValue());
                                        if (orderedItemGroupDefs.get(orderNumber.intValue())
                                            == null) {
                                            LOGGER.debug(
                                                "The List of ItemGroupDefs has a free spot at position/OrderNumber {}, so I'll just put the ItemGroupDef of OID {} there.",
                                                orderNumber.intValue(),
                                                itemGroupRef.getItemGroupOID());
                                            orderedItemGroupDefs.put(orderNumber.intValue(),
                                                matchingItemGroupRefDefs.get(itemGroupRef));
                                        } else {
                                            LOGGER.debug(
                                                "The List of ItemGroupDefs already has an ItemGroupDef at position/OrderNumber {}, which does not comply to the ODM standard. Will not consider orderNumbers in the end.",
                                                orderNumber.intValue());
                                            orderNumberForAllItemGroupRefsPresent = false;
                                        }
                                    }
                                }
                                List<ODMcomplexTypeDefinitionItemGroupDef> itemGroupDefListToIterateOver;
                                if (orderNumberForAllItemGroupRefsPresent) {
                                    LOGGER.debug("All ItemGroupDefs of " + "MetaDataVersion "
                                            + "of OID {} that " + "were referenced "
                                            + "in FormDef of OID" + " {} have a unique"
                                            + " OrderNumber in " + "their " + "ItemGroupRef. "
                                            + "Thus, I will " + "consider the " + "OrderNumbers when"
                                            + " converting to " + "Question(group)s",
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
                                    LOGGER.debug("Not all ItemGroupDefs of " + "MetaDataVersion "
                                            + "of OID {} that " + "were referenced "
                                            + "in FormDef of OID" + " {} have a unique"
                                            + " OrderNumber in " + "their " + "ItemGroupRef. "
                                            + "Thus, I will not " + "consider the "
                                            + "OrderNumbers when" + " converting to "
                                            + "Question(group)s", metaDataVersion.getOID(),
                                        formDef.getOID());
                                    itemGroupDefListToIterateOver = matchedItemGroupDefs;
                                }

                                LOGGER.debug(
                                    "Collection of all necessary info for" + " converting refered "
                                        + "ItemGroupDefs of FormDef " + "of OID {} done. Will now "
                                        + "iterate over them and do " + "the conversion.",
                                    formDef.getOID());
                                for (ODMcomplexTypeDefinitionItemGroupDef itemGroupDef : itemGroupDefListToIterateOver) {
                                    LOGGER.debug(
                                        "Checking the list of ItemRefs " + "for ItemGroupDef of "
                                            + "OID {}", itemGroupDef.getOID());
                                    List<ODMcomplexTypeDefinitionItemRef> itemRefList = itemGroupDef.getItemRef();
                                    if (itemRefList == null || itemRefList.isEmpty()) {
                                        LOGGER.debug(
                                            "The ItemGroupDef of OID {} " + "did not provide a "
                                                + "List of ItemRefs. " + "Since MoPat needs"
                                                + " them (they are " + "the questions), "
                                                + "this ItemGroupDef " + "will not be "
                                                + "converted. " + "Creating an error "
                                                + "message and " + "leaving.",
                                            itemGroupDef.getOID());
                                    } else {
                                        LOGGER.debug(
                                            "The ItemGroupDef of OID {} " + "does provide a "
                                                + "List of ItemRefs. " + "Will now check for"
                                                + " ItemDefs in " + "MetaDataVersion of"
                                                + " OID {}", itemGroupDef.getOID(),
                                            metaDataVersion.getOID());
                                        List<ODMcomplexTypeDefinitionItemDef> itemDefList = metaDataVersion.getItemDef();
                                        if (itemDefList == null || itemDefList.isEmpty()) {
                                            LOGGER.debug(
                                                "The MetaDataVersion" + " of OID " + "{} did "
                                                    + "not " + "contain " + "ItemDefs "
                                                    + "(getItemDef" + "() == " + "null || "
                                                    + "getItemDef" + "().istEmpty())"
                                                    + ". Won't be " + "able to create"
                                                    + " Questions out" + " of the " + "referred "
                                                    + "ItemDefs (in " + "ItemGroupDef "
                                                    + "with OID {}). " + "Will write an "
                                                    + "error message " + "and finish.",
                                                metaDataVersion.getOID(), itemGroupDef.getOID());
                                        } else {
                                            LOGGER.debug("The MetaDataVersion" + " of OID " + "{} "
                                                    + "contains " + "at least " + "one " + "ItemDef. "
                                                    + "Will now " + "iterate " + "over them"
                                                    + " and the " + "ItemRefs " + "in "
                                                    + "ItemGroupDef " + "of OID {} to " + "collect the "
                                                    + "ItemDefs.", metaDataVersion.getOID(),
                                                itemGroupDef.getOID());
                                            Map<ODMcomplexTypeDefinitionItemRef, ODMcomplexTypeDefinitionItemDef> matchingItemRefDefs = new HashMap<>();
                                            List<ODMcomplexTypeDefinitionItemRef> matchedItemRefs = new ArrayList<>();
                                            List<ODMcomplexTypeDefinitionItemDef> matchedItemDefs = new ArrayList<>();
                                            for (ODMcomplexTypeDefinitionItemRef itemRef : itemRefList) {
                                                LOGGER.debug("Now checking for an " + "ItemDef "
                                                        + "with OID " + "{} " + "(because " + "it is "
                                                        + "reffered " + "this way " + "in the "
                                                        + "ItemGroupDef" + " of OID " + "{}).",
                                                    itemRef.getItemOID(), itemGroupDef.getOID());
                                                String refItemOID = itemRef.getItemOID();
                                                boolean itemRefFoundInItemDef = false;
                                                for (ODMcomplexTypeDefinitionItemDef itemDef : itemDefList) {
                                                    LOGGER.debug("Now checking the"
                                                            + " ItemDef with OID {} for equality with ItemRef of ItemOID {}",
                                                        itemDef.getOID(), itemRef.getItemOID());
                                                    if (refItemOID.equalsIgnoreCase(
                                                        itemDef.getOID())) {
                                                        LOGGER.debug(
                                                            "ItemDef with OID {} matched the reffered OID. Will put it into the list of ItemDefs to make Question out of",
                                                            itemDef.getOID());
                                                        itemRefFoundInItemDef = true;
                                                        matchingItemRefDefs.put(itemRef, itemDef);
                                                        matchedItemRefs.add(itemRef);
                                                        matchedItemDefs.add(itemDef);
                                                        break;
                                                    }
                                                }
                                                if (!itemRefFoundInItemDef) {
                                                    LOGGER.debug(
                                                        "Iteration over " + "ItemDefs" + " in "
                                                            + "MetaDataVersion of OID {} done. No ItemDef for the ItemRefOID {} could be found. The ItemRef/-Def will not be converted/considered.",
                                                        metaDataVersion.getOID(),
                                                        itemRef.getItemOID());
                                                }
                                            }
                                            LOGGER.debug("Iteration over " + "ItemRefs " + "in "
                                                    + "ItemGroupDef " + "of OID {} and "
                                                    + "ItemDefs in " + "MetaDataVersion "
                                                    + "of OID {} done" + ". Size of " + "matched "
                                                    + "ItemDefs: {}. " + "Will now check" + " for "
                                                    + "OrderNumbers", itemGroupDef.getOID(),
                                                metaDataVersion.getOID(),
                                                matchingItemRefDefs.size());
                                            boolean orderNumberForAllItemRefsPresent = true;
                                            Map<Integer, ODMcomplexTypeDefinitionItemDef> orderedItemDefs = new HashMap<>();
                                            for (ODMcomplexTypeDefinitionItemRef itemRef : matchedItemRefs) {
                                                LOGGER.debug(
                                                    "Now checking for an " + "OrderNumber " + "for "
                                                        + "ItemRef of" + " OID {}",
                                                    itemRef.getItemOID());
                                                BigInteger orderNumber = itemRef.getOrderNumber();
                                                if (orderNumber == null) {
                                                    LOGGER.debug(
                                                        "The ItemRef with" + " OID " + "{} "
                                                            + "does " + "not " + "have " + "an "
                                                            + "OrderNumber, so I'll write an error message, but continue in the order as given in the odm file",
                                                        itemRef.getItemOID());
                                                    orderNumberForAllItemRefsPresent = false;
                                                } else {
                                                    LOGGER.debug(
                                                        "The ItemRef with" + " OID " + "{} has"
                                                            + " an "
                                                            + "OrderNumber, so I'll try to put the referenced ItemDef onto its position ({}) in the List",
                                                        itemRef.getItemOID(),
                                                        orderNumber.intValue());
                                                    if (orderedItemDefs.get(orderNumber.intValue())
                                                        == null) {
                                                        LOGGER.debug(
                                                            "The List of ItemDefs has a free spot at position/OrderNumber {}, so I'll just put the ItemDef of OID {} there.",
                                                            orderNumber.intValue(),
                                                            itemRef.getItemOID());
                                                        orderedItemDefs.put(orderNumber.intValue(),
                                                            matchingItemRefDefs.get(itemRef));
                                                    } else {
                                                        LOGGER.debug(
                                                            "The List of ItemDefs already has an ItemDef at position/OrderNumber {}, which does not comply to the ODM standard. Will not consider orderNumbers in the end.",
                                                            orderNumber.intValue());
                                                        orderNumberForAllItemRefsPresent = false;
                                                    }
                                                }
                                            }
                                            List<ODMcomplexTypeDefinitionItemDef> itemDefListToIterateOver;
                                            if (orderNumberForAllItemRefsPresent) {
                                                LOGGER.debug("All ItemDefs of"
                                                        + " MetaDataVersion of OID {} that were referenced in ItemGroupDef of OID {} have a unique OrderNumber in their ItemRef. Thus, I will consider the OrderNumbers when converting to Questions",
                                                    metaDataVersion.getOID(),
                                                    itemGroupDef.getOID());
                                                ArrayList<Integer> orderNumberList = new ArrayList<>(
                                                    orderedItemDefs.keySet());
                                                Collections.sort(orderNumberList);
                                                itemDefListToIterateOver = new ArrayList<>();
                                                for (Integer orderNumber : orderNumberList) {
                                                    itemDefListToIterateOver.add(
                                                        orderedItemDefs.get(orderNumber));
                                                }
                                            } else {
                                                LOGGER.debug("Not all ItemDefs of "
                                                        + "MetaDataVersion of OID {} that were referenced in ItemGroupDef of OID {} have a unique OrderNumber in their ItemRef. Thus, I will not consider the OrderNumbers when converting to Questions",
                                                    metaDataVersion.getOID(),
                                                    itemGroupDef.getOID());
                                                itemDefListToIterateOver = matchedItemDefs;
                                            }

                                            LOGGER.debug(
                                                "Collection of all " + "necessary info" + " for "
                                                    + "converting " + "refered " + "ItemDefs of "
                                                    + "ItemGroupDef " + "of OID {} done"
                                                    + ". Will now " + "iterate over "
                                                    + "them and do " + "the conversion.",
                                                itemGroupDef.getOID());
                                            for (ODMcomplexTypeDefinitionItemDef itemDef : itemDefListToIterateOver) {
                                                LOGGER.debug("Calling conversion " + "method for"
                                                    + " ItemDef " + "of OID {}.", itemDef.getOID());
                                                ODMcomplexTypeDefinitionCodeListRef codeListRef = itemDef.getCodeListRef();
                                                if (codeListRef == null) {
                                                    LOGGER.debug(
                                                        "The ItemDef with" + " OID " + "{} did"
                                                            + " not " + "refer " + "to a "
                                                            + "CodeListRef (getCodeListRef() == null), so I'll check for the DataType and create proper questions",
                                                        itemDef.getOID());
                                                    DataType dataType = itemDef.getDataType();
                                                    switch (dataType) {
                                                        case BOOLEAN: {
                                                            LOGGER.debug(
                                                                "The DataType of ItemDef with OID {} was {}. Will convert it into a simple multiple choice question with the options 'yes' and 'no'.",
                                                                itemDef.getOID(), dataType);
                                                            // Replace "." to
                                                            // avoid
                                                            // Javascript
                                                            // errors and
                                                            // replace "_"
                                                            // because it is
                                                            // the internal
                                                            // separator
                                                            aggregatedExportFields.add(
                                                                itemGroupDef.getOID()
                                                                    .replace(".", "u002E")
                                                                    .replace("_", "u005F") + "_"
                                                                    + itemDef.getOID()
                                                                    .replace(".", "u002E")
                                                                    .replace("_", "u005F"));
                                                            aggregatedExportFields.add(
                                                                itemGroupDef.getOID()
                                                                    .replace(".", "u002E")
                                                                    .replace("_", "u005F") + "_"
                                                                    + itemDef.getOID()
                                                                    .replace(".", "u002E")
                                                                    .replace("_", "u005F")
                                                                    + "_true");
                                                            aggregatedExportFields.add(
                                                                itemGroupDef.getOID()
                                                                    .replace(".", "u002E")
                                                                    .replace("_", "u005F") + "_"
                                                                    + itemDef.getOID()
                                                                    .replace(".", "u002E")
                                                                    .replace("_", "u005F")
                                                                    + "_false");
                                                            break;
                                                        }
                                                        default: {
                                                            // Replace "." to
                                                            // avoid
                                                            // Javascript
                                                            // errors and
                                                            // replace "_"
                                                            // because it is
                                                            // the internal
                                                            // separator
                                                            aggregatedExportFields.add(
                                                                itemGroupDef.getOID()
                                                                    .replace(".", "u002E")
                                                                    .replace("_", "u005F") + "_"
                                                                    + itemDef.getOID()
                                                                    .replace(".", "u002E")
                                                                    .replace("_", "u005F"));
                                                            break;
                                                        }
                                                    }
                                                } // [bt] end of
                                                // DataType-driven handling
                                                // of question type
                                                else {// [bt] beginning of
                                                    // Multiple Choice
                                                    // Question/CodeList stuff
                                                    String codeListOID = codeListRef.getCodeListOID();
                                                    List<ODMcomplexTypeDefinitionCodeList> codeListList = metaDataVersion.getCodeList();
                                                    if (codeListList == null) {
                                                        LOGGER.debug(
                                                            "The ItemDef with OID {} refers to a CodeList (getCodeListRef() != null) with OID {}, but the given MetaDataVersion with OID {} does not contain any CodeList (getCodeList() == null). Will add an error message and finish",
                                                            itemDef.getOID(), codeListOID,
                                                            metaDataVersion.getOID());
                                                    } else {
                                                        LOGGER.debug(
                                                            "The ItemDef with OID {} refers to a CodeList (getCodeListRef() != null) with OID {} and the MetaDataVersion with OID {} contains a list of CodeLists. Will now iterate over them to find the referred one.",
                                                            itemDef.getOID(), codeListOID,
                                                            metaDataVersion.getOID());
                                                        ODMcomplexTypeDefinitionCodeList odmAnswerList = null;
                                                        for (ODMcomplexTypeDefinitionCodeList codeList : codeListList) {
                                                            LOGGER.debug(
                                                                "checking CodeList with OID {}",
                                                                codeList.getOID());
                                                            if (codeList.getOID()
                                                                .equalsIgnoreCase(codeListOID)) {
                                                                LOGGER.debug(
                                                                    "CodeList with OID {} matched (ignoring cases) the referenced CodeList ({}). Will take this one for the answers of the multiple choice question");
                                                                odmAnswerList = codeList;
                                                                break;
                                                            }
                                                        }

                                                        if (odmAnswerList == null) {
                                                            LOGGER.debug(
                                                                "No matching CodeList could be found for the referenced CodeList with OID {} in MetaDataVersion with OID {} to create a multiple choice question for ItemDef with OID {}. Will create an error message and finish",
                                                                codeListOID,
                                                                metaDataVersion.getOID(),
                                                                itemDef.getOID());
                                                        } else {
                                                            for (ODMcomplexTypeDefinitionCodeListItem codeListItem : odmAnswerList.getCodeListItem()) {
                                                                // Replace "
                                                                // ." to
                                                                // avoid
                                                                // Javascript
                                                                // errors and
                                                                // replace
                                                                // "_"
                                                                // because it
                                                                // is the
                                                                // internal
                                                                // separator
                                                                aggregatedExportFields.add(
                                                                    itemGroupDef.getOID()
                                                                        .replace(".", "u002E")
                                                                        .replace("_", "u005F") + "_"
                                                                        + itemDef.getOID()
                                                                        .replace(".", "u002E")
                                                                        .replace("_", "u005F") + "_"
                                                                        + codeListItem.getCodedValue()
                                                                        .replace(".", "u002E")
                                                                        .replace("_", "u005F"));
                                                            }
                                                        }

                                                    }
                                                } // [bt] end of Multiple
                                                // Choice/CodeList stuff
                                            }
                                        }
                                    }

                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.debug("An error occured during reading of ODM: {}", e);
        }
        return aggregatedExportFields;
    }
}
