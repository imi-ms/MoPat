package de.imi.mopat.model.score;

import java.util.Random;

/**
 *
 */
public class BinaryOperatorTest {

    private static final Random random = new Random();

    /**
     * Returns a random valid new {@link BinaryOperator}
     *
     * @return Returns a random valid new {@link BinaryOperator}
     */
    public static BinaryOperator getNewValidBinaryOperator() {
        if (random.nextBoolean()) {
            return BinaryOperatorBooleanTest.getNewValidBinaryOperatorBoolean();
        } else {
            return BinaryOperatorNumericTest.getNewValidBinaryOperatorNumeric();
        }
    }
}
