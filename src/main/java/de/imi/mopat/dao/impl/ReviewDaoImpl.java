package de.imi.mopat.dao.impl;

import de.imi.mopat.dao.ReviewDao;
import de.imi.mopat.model.Bundle;
import de.imi.mopat.model.Review;
import de.imi.mopat.model.enumeration.PermissionType;
import de.imi.mopat.model.user.User;
import jakarta.persistence.TypedQuery;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public class ReviewDaoImpl extends MoPatDaoImpl<Review> implements ReviewDao {

    @Override
    public List<Review> findByUserId(Long userId) {
        TypedQuery<Review> query = moPatEntityManager.createQuery("SELECT r FROM Review r WHERE r.userId = :userId", Review.class);
        query.setParameter("userId", userId);
        return query.getResultList();
    }

    @Override
    public List<Review> findByUserIdAndIsReviewedFalse(Long userId) {
        TypedQuery<Review> query = moPatEntityManager.createQuery("SELECT r FROM Review r WHERE r.userId = :userId AND r.isReviewed = false", Review.class);
        query.setParameter("userId", userId);
        return query.getResultList();
    }

    @Override
    public List<Review> findByBundle(Bundle bundle) {
        TypedQuery<Review> query = moPatEntityManager.createQuery("SELECT r FROM Review r WHERE r.bundle = :bundle", Review.class);
        query.setParameter("bundle", bundle);
        return query.getResultList();
    }

    @Override
    public Class<Review> getEntityClass() {
        return Review.class;
    }
}
