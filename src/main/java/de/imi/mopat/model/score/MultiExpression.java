package de.imi.mopat.model.score;

import de.imi.mopat.model.Encounter;
import de.imi.mopat.model.dto.ExpressionDTO;

import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.CascadeType;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;

/**
 * This expression contains a {@link MultiOperator} and a list of child
 * {@link Expression expressions}.
 */
@Entity
@DiscriminatorValue("MULTI")
public class MultiExpression extends Expression {

    @JoinColumn(name = "operator", referencedColumnName = "id")
    @ManyToOne
    private MultiOperator multiOperator;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Expression> expressions = new ArrayList<>();

    /**
     * Get the list with the child {@link Expression expressions} of this expression object.
     *
     * @return The list with the child {@link Expression expressions} of this expression object.
     */
    public List<Expression> getExpressions() {
        return expressions;
    }

    /**
     * Set the child {@link Expression expressions} to a new list of
     * {@link Expression expressions}.
     *
     * @param expressions List with {@link Expression expressions}.
     */
    public void setExpressions(List<Expression> expressions) {
        this.expressions = expressions;
    }

    @Override
    public Operator getOperator() {
        return multiOperator;
    }

    /**
     * Set a {@link MultiOperator} to this expression.
     *
     * @param multiOperator The {@link MultiOperator} which should be set.
     */
    public void setOperator(final MultiOperator multiOperator) {
        this.multiOperator = multiOperator;
    }

    @Override
    public Object evaluate(final Encounter encounter) {
        return multiOperator.evaluate(this, encounter);
    }

    @Override
    public String getFormula(final Encounter encounter, final String defaultLanguage) {
        return multiOperator.getFormula(this, encounter, defaultLanguage);
    }

    @Override
    public ExpressionDTO toExpressionDTO() {
        ExpressionDTO expressionDTO = new ExpressionDTO();
        expressionDTO.setOperatorId(this.getOperator().getId());

        for (Expression childExpression : this.getExpressions()) {
            ExpressionDTO childExpressionDTO = childExpression.toExpressionDTO();
            expressionDTO.addExpressions(childExpressionDTO);
        }
        return expressionDTO;
    }
}
