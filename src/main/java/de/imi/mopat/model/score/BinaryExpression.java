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
 *
 */
@Entity
@DiscriminatorValue("BINARY")
public class BinaryExpression extends Expression {

    @JoinColumn(name = "operator", referencedColumnName = "id")
    @ManyToOne
    private BinaryOperator binaryOperator;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Expression> expressions = new ArrayList<>();

    /**
     * Get the list with the child {@link Expression expressions} of this {@link Expression}
     * object.
     *
     * @return The list with the child {@link Expression expressions} of this {@link Expression}
     * object.
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
    public void setExpressions(final List<Expression> expressions) {
        assert expressions != null : "The given set was null";
        assert expressions.size() == 2 : "The given list's size was unequal 2";
        this.expressions = expressions;
    }

    @Override
    public Operator getOperator() {
        return binaryOperator;
    }

    /**
     * Set a {@link BinaryOperator} to this {@link Expression}.
     *
     * @param binaryOperator The {@link BinaryOperator} which should be set.
     */
    public void setOperator(final BinaryOperator binaryOperator) {
        this.binaryOperator = binaryOperator;
    }

    @Override
    public Object evaluate(final Encounter encounter) {
        return binaryOperator.evaluate(this, encounter);
    }

    @Override
    public String getFormula(final Encounter encounter, final String defaultLanguage) {
        return binaryOperator.getFormula(this, encounter, defaultLanguage);
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
