package de.imi.mopat.model.score;

import java.util.Random;

/**
 *
 */
public class BinaryOperatorBooleanTest {

    private static final Random random = new Random();

    /**
     * Returns a random valid new {@link BinaryOperatorBoolean}
     *
     * @return Returns a random valid new {@link BinaryOperatorBoolean}
     */
    public static BinaryOperatorBoolean getNewValidBinaryOperatorBoolean() {
        switch (random.nextInt(6)) {
            case 0:
                return DifferentTest.getNewValidDifferent();
            case 1:
                return EqualsTest.getNewValidEquals();
            case 2:
                return GreaterEqualsTest.getNewValidGreaterEquals();
            case 3:
                return GreaterTest.getNewValidGreater();
            case 4:
                return LessEqualsTest.getNewValidLessEquals();
            default:
                return LessTest.getNewValidLess();
        }
    }
}
