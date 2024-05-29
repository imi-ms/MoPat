package de.imi.mopat.dao;

import de.imi.mopat.model.score.Operator;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 *
 */
@Component
public interface OperatorDao extends MoPatDao<Operator> {

    List<Operator> getOperators();

    @Override
    Operator getElementById(Long id);

    Operator getOperatorByDisplaySign(String displaySign);
}