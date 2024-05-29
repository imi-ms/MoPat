package de.imi.mopat.model.score;

import java.util.Random;

/**
 *
 */
public class MultiOperatorTest {

    private static final Random random = new Random();

    /**
     * Returns a random valid new {@link MultiOperator}
     *
     * @return Returns a random valid new {@link MultiOperator}
     */
    public static MultiOperator getNewValidMultiOperator() {
        switch (random.nextInt(3)) {
            case 0:
                return AverageTest.getNewValidAverage();
            case 1:
                return CounterTest.getNewValidCounter();
            default:
                return SumTest.getNewValidSum();
        }
    }
}
