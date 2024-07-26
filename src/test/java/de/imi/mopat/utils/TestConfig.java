package de.imi.mopat.utils;

import de.imi.mopat.helper.controller.QuestionnaireGroupService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TestConfig {

    @Bean
    public MockQuestionnaireGroupDao mockQuestionnaireGroupDao() {
        return new MockQuestionnaireGroupDao();
    }

    @Bean
    public QuestionnaireGroupService questionnaireGroupService(MockQuestionnaireGroupDao mockQuestionnaireGroupDao) {
        QuestionnaireGroupService service = new QuestionnaireGroupService();
        service.setQuestionnaireGroupDao(mockQuestionnaireGroupDao);
        return service;
    }
}

