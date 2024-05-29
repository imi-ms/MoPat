package de.imi.mopat.model.score;

import jakarta.persistence.Entity;

/**
 * This operator requires only one {@link Expression} for calculation.
 */
@Entity
public abstract class UnaryOperator extends Operator {

}
