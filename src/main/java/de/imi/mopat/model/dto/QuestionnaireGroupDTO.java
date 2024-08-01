package de.imi.mopat.model.dto;

import java.util.List;

public class QuestionnaireGroupDTO {
    private Long groupId;
    private String groupName;
    private List<QuestionnaireDTO> questionnaireDTOS;

    // Getter und Setter
    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    public String getGroupName() {
        return groupName;
    }
    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public List<QuestionnaireDTO> getQuestionnaireDTOS() {
        return questionnaireDTOS;
    }

    public void setQuestionnaireDTOS(List<QuestionnaireDTO> questionnaireDTOS) {
        this.questionnaireDTOS = questionnaireDTOS;
    }

}
