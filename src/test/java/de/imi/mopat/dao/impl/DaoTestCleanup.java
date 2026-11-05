package de.imi.mopat.dao.impl;

import de.imi.mopat.model.Response;
import de.imi.mopat.model.Review;
import jakarta.persistence.EntityManager;

final class DaoTestCleanup {

    private DaoTestCleanup() {
    }

    static void clearResponses(EntityManager entityManager) {
        for (Response response : entityManager.createQuery("SELECT r FROM Response r", Response.class)
            .getResultList()) {
            entityManager.remove(response);
        }
        entityManager.flush();
    }

    static void clearQuestionReferences(EntityManager entityManager) {
        clearResponses(entityManager);
        entityManager.createNativeQuery("UPDATE expression SET question = NULL WHERE question IS NOT NULL")
            .executeUpdate();
        entityManager.createNativeQuery(
            "UPDATE mopat_condition SET target_answer_question_id = NULL WHERE target_answer_question_id IS NOT NULL")
            .executeUpdate();
    }

    static void clearReviews(EntityManager entityManager) {
        for (Review review : entityManager.createQuery("SELECT r FROM Review r", Review.class)
            .getResultList()) {
            entityManager.remove(review);
        }
        entityManager.flush();
    }
}