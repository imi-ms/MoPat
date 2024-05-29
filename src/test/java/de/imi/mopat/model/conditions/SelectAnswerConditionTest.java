/**
 *
 */
package de.imi.mopat.model.conditions;

import de.imi.mopat.model.Bundle;
import de.imi.mopat.model.BundleTest;
import de.imi.mopat.model.QuestionTest;
import de.imi.mopat.model.SelectAnswerTest;
import de.imi.mopat.utils.Helper;

/**
 * @uathor Tobias Hardt <tobiashardt@uni-muenster.de>
 * @since v1.2
 */
public class SelectAnswerConditionTest {

    /**
     * Returns a valid new SelectAnswerCondition
     *
     * @return Returns a valid new SelectAnswerCondition
     */
    public static SelectAnswerCondition getNewValidSelectAnswerCondition() {
        ConditionActionType testAction = Helper.getRandomEnum(ConditionActionType.class);
        ConditionTrigger testTrigger = SelectAnswerTest.getNewValidSelectAnswer();
        ConditionTarget testTarget = QuestionTest.getNewValidQuestion();
        Bundle testBundle = BundleTest.getNewValidBundle();
        return new SelectAnswerCondition(testTrigger, testTarget, testAction, testBundle);
    }
}
