package de.imi.mopat.model.score;

import java.util.Random;

/**
 *
 */
public class BinaryOperatorNumericTest {

    private static final Random random = new Random();

    /**
     * Returns a random valid new {@link BinaryOperatorNumeric}
     *
     * @return Returns a random valid new {@link BinaryOperatorNumeric}
     */
    public static BinaryOperatorNumeric getNewValidBinaryOperatorNumeric() {
        switch (random.nextInt(3)) {
            case 0:
                return DivideTest.getNewValidDivide();
            case 1:
                return MinusTest.getNewValidMinus();
            default:
                return PlusTest.getNewValidPlus();
        }
    }
}
