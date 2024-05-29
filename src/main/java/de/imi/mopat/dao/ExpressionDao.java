package de.imi.mopat.dao;

import de.imi.mopat.model.score.Expression;
import org.springframework.stereotype.Component;

/**
 * Interface for the data access for objects of type {@link Expression}.
 * <p>
 * Provides specific methods for the objects of type {@link Expression}.
 */
@Component
public interface ExpressionDao extends MoPatDao<Expression> {

}