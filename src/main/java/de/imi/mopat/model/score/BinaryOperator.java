package de.imi.mopat.model.score;

import jakarta.persistence.Entity;

/**
 * This operator requires two {@link Expression expressions} for calculation.
 */
@Entity
public abstract class BinaryOperator extends Operator {

}
