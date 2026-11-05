package de.imi.mopat.helper.model;

import de.imi.mopat.model.ReviewMessage;
import de.imi.mopat.model.dto.ReviewMessageDTO;
import org.springframework.stereotype.Component;

import java.util.function.Function;

@Component
public class ReviewMessageDTOMapper implements Function<ReviewMessage, ReviewMessageDTO> {

    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(ReviewMessageDTOMapper.class);

    @Override
    public ReviewMessageDTO apply(ReviewMessage reviewMessage) {
        ReviewMessageDTO reviewMessageDTO = new ReviewMessageDTO();
        reviewMessageDTO.setId(reviewMessage.getId());
        reviewMessageDTO.setSenderId(reviewMessage.getSenderId());
        reviewMessageDTO.setReceiverId(reviewMessage.getReceiverId());
        reviewMessageDTO.setMessage(reviewMessage.getMessage());
        reviewMessageDTO.setSentAt(reviewMessage.getSentAt());
        return reviewMessageDTO;
    }
}