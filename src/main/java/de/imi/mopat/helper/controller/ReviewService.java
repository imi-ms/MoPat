package de.imi.mopat.helper.controller;

import de.imi.mopat.dao.ReviewDao;
import de.imi.mopat.model.Bundle;
import de.imi.mopat.model.Review;
import de.imi.mopat.model.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ReviewService {

    @Autowired
    private ReviewDao reviewDao;

    public List<Review> getPendingReviewsForCurrentUser(User currentUser) {
        return reviewDao.findByUserIdAndIsReviewedFalse(currentUser.getId());
    }

    public void createReviewsForBundle(Bundle bundle, List<Long> userIds) {
        for (Long userId : userIds) {
            Review review = new Review();
            review.setBundle(bundle);
            review.setUserId(userId);
            review.setReviewed(false);
            reviewDao.merge(review);
        }
    }

    public void markReviewAsReviewed(Long reviewId) {
        Review review = reviewDao.getElementById(reviewId);
        if (review != null) {
            review.setReviewed(true);
            review.setReviewedAt(LocalDateTime.now());
            reviewDao.merge(review);
        }
    }
}
