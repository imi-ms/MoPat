package de.imi.mopat.model.dto.export;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import de.imi.mopat.model.Questionnaire;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * This class represents the data transfer object of model {@link Questionnaire} including its
 * export templates and mappings to convert a model to json for import and export.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonTypeName("questionnaireComplete")
public class JsonCompleteQuestionnaireDTO extends JsonQuestionnaireDTO {

    private SortedMap<Long, JsonExportTemplateDTO> exportDTOs = new TreeMap<>();


    public SortedMap<Long, JsonExportTemplateDTO> getExportDTOs() {
        return exportDTOs;
    }
    public void setExportDTOs(SortedMap<Long, JsonExportTemplateDTO> exportDTOs) {
        this.exportDTOs = exportDTOs;
    }

    public void addExportDTOs(long id, JsonExportTemplateDTO jsonExportTemplateDTO){
        this.exportDTOs.put(
            id,
            jsonExportTemplateDTO);

    }
}
