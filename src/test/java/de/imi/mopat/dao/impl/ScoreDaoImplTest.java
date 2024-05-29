package de.imi.mopat.dao.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import de.imi.mopat.config.AppConfig;
import de.imi.mopat.config.ApplicationSecurityConfig;
import de.imi.mopat.config.MvcWebApplicationInitializer;
import de.imi.mopat.config.PersistenceConfig;
import de.imi.mopat.dao.ConfigurationGroupDao;
import de.imi.mopat.dao.ExportTemplateDao;
import de.imi.mopat.dao.ExpressionDao;
import de.imi.mopat.dao.QuestionDao;
import de.imi.mopat.dao.QuestionnaireDao;
import de.imi.mopat.dao.ScoreDao;
import de.imi.mopat.model.ConfigurationGroup;
import de.imi.mopat.model.ConfigurationGroupTest;
import de.imi.mopat.model.ExportRuleScore;
import de.imi.mopat.model.ExportTemplate;
import de.imi.mopat.model.Question;
import de.imi.mopat.model.QuestionTest;
import de.imi.mopat.model.Questionnaire;
import de.imi.mopat.model.QuestionnaireTest;
import de.imi.mopat.model.enumeration.ExportScoreFieldType;
import de.imi.mopat.model.enumeration.ExportTemplateType;
import de.imi.mopat.model.score.BinaryExpression;
import de.imi.mopat.model.score.Expression;
import de.imi.mopat.model.score.Score;
import de.imi.mopat.model.score.UnaryExpression;
import de.imi.mopat.utils.Helper;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

/**
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {AppConfig.class, ApplicationSecurityConfig.class,
    MvcWebApplicationInitializer.class, PersistenceConfig.class})
@TestPropertySource(locations = {"classpath:mopat-test.properties"})
@WebAppConfiguration
public class ScoreDaoImplTest {

    private static final Random random = new Random();
    @Autowired
    ScoreDao testScoreDao;
    @Autowired
    QuestionDao questionDao;
    @Autowired
    QuestionnaireDao questionnaireDao;
    @Autowired
    ExpressionDao expressionDao;
    @Autowired
    ExportTemplateDao exportTemplateDao;
    @Autowired
    ConfigurationGroupDao configurationGroupDao;

    /**
     * Test of {@link ScoreDaoImpl#hasScore}.<br> Valid input: existing {@link Question} with and
     * without a {@link Score}
     */
    @Test
    public void testHasScore() {
        Questionnaire testQuestionnaire = QuestionnaireTest.getNewValidQuestionnaire();
        questionnaireDao.merge(testQuestionnaire);
        Question testQuestion = QuestionTest.getNewValidQuestion(testQuestionnaire);
        questionDao.merge(testQuestion);
        assertFalse("The Question had Scores although it was new",
            testScoreDao.hasScore(testQuestion));
        Score testScore = new Score();
        testScore.setQuestionnaire(testQuestionnaire);
        UnaryExpression testExpression = new UnaryExpression();
        testExpression.setQuestion(testQuestion);
        questionDao.merge(testQuestion);
        testScore.setExpression(testExpression);
        testScoreDao.merge(testScore);
        assertTrue("The Question had no Score although the Score was set right before",
            testScoreDao.hasScore(testQuestion));
        testScoreDao.remove(testScore);
        questionDao.remove(testQuestion);
    }

    /**
     * Test of {@link ScoreDaoImpl#getScores}.<br> Valid input: existing {@link Question} with
     * random numer of {@link Score Scores}
     */
    @Test
    public void testGetScores() {
        Questionnaire testQuestionnaire = QuestionnaireTest.getNewValidQuestionnaire();
        questionnaireDao.merge(testQuestionnaire);
        Question testQuestion = QuestionTest.getNewValidQuestion(testQuestionnaire);
        questionDao.merge(testQuestion);
        assertTrue("The Question had Scores although it was new",
            testScoreDao.getScores(testQuestion).isEmpty());
        List<Score> testScores = new ArrayList<>();
        int countScores = random.nextInt(25) + 1;
        for (int i = 0; i < countScores; i++) {
            Score testScore = new Score();
            testScore.setQuestionnaire(testQuestionnaire);
            BinaryExpression testBinaryExpression = new BinaryExpression();
            UnaryExpression testUnaryExpressionQuestion = new UnaryExpression();
            testUnaryExpressionQuestion.setQuestion(testQuestion);
            UnaryExpression testUnaryExpressionValue = new UnaryExpression();
            testUnaryExpressionValue.setValue(random.nextDouble());
            List<Expression> unaryExpressions = new ArrayList<>();
            unaryExpressions.add(testUnaryExpressionQuestion);
            unaryExpressions.add(testUnaryExpressionValue);
            testBinaryExpression.setExpressions(unaryExpressions);
            testUnaryExpressionQuestion.setParent(testBinaryExpression);
            testUnaryExpressionValue.setParent(testBinaryExpression);
            questionDao.merge(testQuestion);
            testScore.setExpression(testBinaryExpression);
            testScoreDao.merge(testScore);
            testScores.add(testScore);
        }
        assertEquals("The getting list of Scores was not the expected one", testScores,
            testScoreDao.getScores(testQuestion));
        for (Score score : testScoreDao.getScores(testQuestion)) {
            testScoreDao.remove(score);
        }
        questionDao.remove(testQuestion);
    }

    /**
     * Test of {@link ScoreDaoImpl#remove}.<br> Valid input: existing {@link Score} with
     * {@link ExportRuleScore ExportRuleScores}
     */
    @Test
    @WithUserDetails(value = "admin", userDetailsServiceBeanName = "MoPatUserDetailsService")
    public void testRemove() {
        Questionnaire testQuestionnaire = QuestionnaireTest.getNewValidQuestionnaire();
        questionnaireDao.merge(testQuestionnaire);
        List<ExportTemplate> testExportTemplates = new ArrayList<>();
        Score testScore = new Score();
        int countExportRules = random.nextInt(25) + 1;
        for (int i = 0; i < countExportRules; i++) {
            String testName = Helper.getRandomAlphanumericString(random.nextInt(50) + 3);
            ExportTemplateType testExportTemplateType = Helper.getRandomEnum(
                ExportTemplateType.class);
            String testFilename = Helper.getRandomAlphanumericString(random.nextInt(50) + 3);
            ConfigurationGroup testConfigurationGroup = ConfigurationGroupTest.getNewValidConfigurationGroup();
            testConfigurationGroup.setLabelMessageCode(
                Helper.getRandomAlphanumericString(random.nextInt(50) + 3));
            testConfigurationGroup.setPosition(Math.abs(random.nextInt()) + 10000);
            configurationGroupDao.merge(testConfigurationGroup);
            ExportTemplate testExportTemplate = new ExportTemplate(testName, testExportTemplateType,
                testFilename, testConfigurationGroup, testQuestionnaire);
            exportTemplateDao.merge(testExportTemplate);
            testExportTemplates.add(testExportTemplate);

            String testExportField = Helper.getRandomString(random.nextInt(50) + 1);
            ExportScoreFieldType testScoreField = Helper.getRandomEnum(ExportScoreFieldType.class);
            ExportRuleScore testExportRuleScore = new ExportRuleScore(testExportTemplate,
                testExportField, testScore, testScoreField);

            testScore.addExportRule(testExportRuleScore);
        }
        testScore.setQuestionnaire(testQuestionnaire);
        testScoreDao.merge(testScore);
        assertEquals("The getting count of ExportRules was not the expected one", countExportRules,
            testScore.getExportRules().size());
        assertEquals("The Score was not added correctly to the Questionnaire", 1,
            testQuestionnaire.getScores().size());
        testScoreDao.remove(testScore);
        assertEquals("The Score was not removed correctly from the Questionnaire", 0,
            testQuestionnaire.getScores().size());

        for (ExportTemplate exportTemplate : testExportTemplates) {
            exportTemplateDao.remove(exportTemplate);
        }
        List<ConfigurationGroup> allConfigurationGroup = configurationGroupDao.getAllElements();
        for (ConfigurationGroup configurationGroup : allConfigurationGroup) {
            if (configurationGroup.getPosition() >= 10000) {
                configurationGroupDao.remove(configurationGroup);
            }
        }
        questionnaireDao.remove(testQuestionnaire);
    }
}
