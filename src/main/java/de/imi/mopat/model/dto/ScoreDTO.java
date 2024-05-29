package de.imi.mopat.model.dto;

import java.util.List;

/**
 *
 */
public class ScoreDTO {

    private Long id = null;

    private String name = null;

    private Long questionnaireId;

    private ExpressionDTO expression;

    private List<String> dependingScoreNames;

    private boolean hasExportRules;

    public ScoreDTO() {
    }

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public Long getQuestionnaireId() {
        return questionnaireId;
    }

    public void setQuestionnaireId(final Long questionnaireId) {
        this.questionnaireId = questionnaireId;
    }

    public ExpressionDTO getExpression() {
        return expression;
    }

    public void setExpression(final ExpressionDTO expression) {
        this.expression = expression;
    }

    public List<String> getDependingScoreNames() {
        return dependingScoreNames;
    }

    public void setDependingScoreNames(final List<String> dependingScoreNames) {
        this.dependingScoreNames = dependingScoreNames;
    }

    public boolean hasExportRules() {
        return hasExportRules;
    }

    public void setHasExportRules(final boolean hasExportRules) {
        this.hasExportRules = hasExportRules;
    }
}
