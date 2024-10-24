package de.imi.mopat.dao;

import de.imi.mopat.model.Question;
import de.imi.mopat.model.Questionnaire;
import de.imi.mopat.model.score.Score;
import java.util.List;
import java.util.Set;
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
     * Returns a Set of unique questionnaire ids that is evaluated by parsing the
     * questionnaireIds to check for and returning only those that have a score.
     * Query is used for optimized processing times
     * @param questionnaireIds to check if a score exists
     * @return Set of ids from questionnaireIds that have a score
     */
    public Set<Long> findQuestionnairesWithScores(List<Long> questionnaireIds);

    /**
     * Returns the list of associated {@link Score scores} to the given {@link Question}.
     *
     * @param question The {@link Question} for which the {@link Score scores} should be returned.
     * @return The list of associated {@link Score scores} to the given {@link Question}.
     */
    List<Score> getScores(Question question);
}
