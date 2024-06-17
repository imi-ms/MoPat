package de.imi.mopat.dao;

import de.imi.mopat.model.Review;
import de.imi.mopat.model.Bundle;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public interface ReviewDao extends MoPatDao<Review> {

    List<Review> findByUserId(Long userId);

    List<Review> findByUserIdAndIsReviewedFalse(Long userId);

    List<Review> findByBundle(Bundle bundle);
}
