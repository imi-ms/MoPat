package de.imi.mopat.dao;

import de.imi.mopat.model.Question;
import de.imi.mopat.model.Questionnaire;
import de.imi.mopat.model.score.Score;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * Interface for the data access for objects of type {@link Score}.
 * <p>
 * Provides specific methods for the objects of type {@link Score}.
 */
@Component
public interface ScoreDao extends MoPatDao<Score> {

    /**
     * Returns true if the {@link Question} has {@link Score scores}, false otherwise.
     *
     * @param question The {@link Question}, which should be checked for {@link Score scores}.
     * @return Returns true if the {@link Question} has {@link Score scores}, false otherwise.
     */
    boolean hasScore(Question question);

    /**
     * Returns true if the {@link Questionnaire} has {@link Score scores}, false otherwise.
     *
     * @param questionnaire The {@link Questionnaire}, which should be checked for
     *                      {@link Score scores}.
     * @return Returns true if the {@link Questionnaire} has {@link Score scores}, false otherwise.
     */
    boolean hasScore(Questionnaire questionnaire);

    /**
     * Returns the list of associated {@link Score scores} to the given {@link Question}.
     *
     * @param question The {@link Question} for which the {@link Score scores} should be returned.
     * @return The list of associated {@link Score scores} to the given {@link Question}.
     */
    List<Score> getScores(Question question);
}
