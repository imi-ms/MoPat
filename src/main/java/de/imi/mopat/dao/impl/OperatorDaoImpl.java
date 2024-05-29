package de.imi.mopat.dao.impl;

import de.imi.mopat.dao.OperatorDao;
import de.imi.mopat.model.score.Operator;

import java.util.ArrayList;
import java.util.List;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

/**
 *
 */
@Component(value = "OperatorDao")
public class OperatorDaoImpl extends MoPatDaoImpl<Operator> implements OperatorDao {

    private List<Operator> operators = new ArrayList<>();

    /**
     * Initializes the OperatorDao after construction by Spring and gets all
     * {@link Operator Operators} from the database.
     */
    @PostConstruct
    private void initialize() {
        operators = getAllElements();
    }

    @Override
    public List<Operator> getOperators() {
        return operators;
    }

    @Override
    public Operator getOperatorByDisplaySign(final String displaySign) {
        for (Operator operator : operators) {
            if (operator.getDisplaySign().equals(displaySign)) {
                return operator;
            }
        }

        return null;
    }

    @Override
    public Operator getElementById(final Long id) {
        for (Operator operator : operators) {
            if (operator.getId() == id) {
                return operator;
            }
        }
        return null;
    }
}
