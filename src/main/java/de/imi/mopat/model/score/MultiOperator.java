package de.imi.mopat.model.score;

import jakarta.persistence.Entity;

/**
 * This operator can have several {@link Expression Expressions}. At least it contains one
 * {@link Expression}.
 */
@Entity
public abstract class MultiOperator extends Operator {

}
