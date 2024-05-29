package de.imi.mopat.model.score;

import java.util.Random;

/**
 *
 */
public class UnaryOperatorTest {

    private static final Random random = new Random();

    /**
     * Return a new valid random instance of {@link UnaryOperator}
     *
     * @return New valid UnaryOperator instance.
     */
    public static UnaryOperator getNewValidUnaryOperator() {
        switch (random.nextInt(3)) {
            case 0:
                return ValueOperatorTest.getNewValidValueOperator();
            case 1:
                return ValueOfQuestionOperatorTest.getNewValidValueOfQuestionOperator();
            default:
                return ValueOfScoreOperatorTest.getNewValidValueOfScoreOperator();
        }
    }
}
