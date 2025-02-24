package de.imi.mopat.helper.controller;

import de.imi.mopat.dao.ReviewDao;
import de.imi.mopat.helper.model.ReviewDTOMapper;
import de.imi.mopat.model.dto.ReviewDTO;
import de.imi.mopat.model.dto.UserDTO;
import de.imi.mopat.model.enumeration.ReviewStatus;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

@Service
public class ReviewService {

    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(ReviewService.class);

    @Autowired
    private ReviewDao reviewDao;

    @Autowired
    private UserService userService;

    @Autowired
    private AuthService authService;

    @Autowired
    private ReviewDTOMapper reviewDTOMapper;

    private static final Comparator<ReviewDTO> REVIEW_COMPARATOR = Comparator
            .comparing((ReviewDTO review) -> review.getStatus() == ReviewStatus.APPROVED ? 1 : 0)
            .thenComparing(ReviewDTO::getUpdatedAt, Comparator.nullsLast(Comparator.reverseOrder()));


    public List<ReviewDTO> getAllReviews() {
        return reviewDao.getAllElements().stream()
                .map(reviewDTOMapper)
                .sorted(REVIEW_COMPARATOR)
                .peek(this::addUserDetailsToReviewDTO)
                .toList();
    }

    public List<ReviewDTO> getAssignedReviews() {
        Long userId = authService.getAuthenticatedUserId();

        Predicate<ReviewDTO> isAssignedToUser = reviewDTO ->
                Objects.equals(reviewDTO.getEditorId(), userId) ||
                        Objects.equals(reviewDTO.getReviewerId(), userId);

        return getAllReviews().stream()
                .filter(isAssignedToUser)
                .sorted(REVIEW_COMPARATOR)
                .toList();
    }

    private void addUserDetailsToReviewDTO(ReviewDTO reviewDTO) {
        UserDTO editor = userService.getUserDTOById(reviewDTO.getEditorId());
        UserDTO reviewer = userService.getUserDTOById(reviewDTO.getReviewerId());

        if (editor != null) {
            reviewDTO.setEditorName(editor.getFirstname() + " " + editor.getLastname());
            reviewDTO.setEditorInitials(getInitials(editor.getFirstname(), editor.getLastname()));
        }

        if (reviewer != null) {
            reviewDTO.setReviewerName(reviewer.getFirstname() + " " + reviewer.getLastname());
            reviewDTO.setReviewerInitials(getInitials(reviewer.getFirstname(), reviewer.getLastname()));
        }
    }

    private String getInitials(String firstname, String lastname) {
        StringBuilder initials = new StringBuilder();

        if (firstname != null && !firstname.isEmpty()) {
            initials.append(firstname.charAt(0));
        }
        if (lastname != null && !lastname.isEmpty()) {
            initials.append(lastname.charAt(0));
        }

        return initials.toString().toUpperCase();
    }
}