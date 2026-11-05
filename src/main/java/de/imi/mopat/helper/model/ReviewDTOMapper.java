package de.imi.mopat.helper.model;

import de.imi.mopat.model.Review;
import de.imi.mopat.model.dto.ReviewDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.function.Function;

@Component
public class ReviewDTOMapper implements Function<Review, ReviewDTO> {

    @Autowired
    QuestionnaireDTOMapper questionnaireDTOMapper;

    @Autowired
    ReviewMessageDTOMapper reviewMessageDTOMapper;

    private static final org.slf4j.Logger LOGGER =
            org.slf4j.LoggerFactory.getLogger(ReviewDTOMapper.class);

    @Override
    public ReviewDTO apply(Review review) {
        ReviewDTO reviewDTO = new ReviewDTO();
        reviewDTO.setId(review.getId());
        reviewDTO.setEditorId(review.getEditorId());
        reviewDTO.setStatus(review.getStatus());
        reviewDTO.setReviewerId(review.getReviewerId());
        reviewDTO.setUpdatedAt(review.getUpdatedAt());
        reviewDTO.setCreatedAt(review.getCreatedAt());
        reviewDTO.setQuestionnaire(questionnaireDTOMapper.apply(review.getQuestionnaire()));
        reviewDTO.setConversation(review.getConversation().stream()
                .map(reviewMessageDTOMapper)
                .toList());
        return reviewDTO;
    }
}