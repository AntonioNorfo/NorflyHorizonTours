package antonionorfo.norflyHorizonTours.services;

import antonionorfo.norflyHorizonTours.entities.Excursion;
import antonionorfo.norflyHorizonTours.entities.Review;
import antonionorfo.norflyHorizonTours.entities.User;
import antonionorfo.norflyHorizonTours.exception.ResourceNotFoundException;
import antonionorfo.norflyHorizonTours.payloads.ReviewDTO;
import antonionorfo.norflyHorizonTours.repositories.ExcursionRepository;
import antonionorfo.norflyHorizonTours.repositories.ReviewRepository;
import antonionorfo.norflyHorizonTours.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final ExcursionRepository excursionRepository;

    public ReviewDTO addReview(ReviewDTO reviewDTO) {
        // Verifica che l'utente esista
        User user = userRepository.findById(reviewDTO.userId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found!"));

        Excursion excursion = excursionRepository.findById(reviewDTO.excursionId())
                .orElseThrow(() -> new ResourceNotFoundException("Excursion not found!"));

        Review review = new Review();
        review.setUser(user);
        review.setExcursion(excursion);
        review.setComment(reviewDTO.comment());
        review.setRating(reviewDTO.rating());
        review.setReviewDate(LocalDate.now());

        return mapToDTO(reviewRepository.save(review));
    }

    public ReviewDTO updateReview(UUID reviewId, ReviewDTO reviewDTO, UUID authenticatedUserId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found!"));

        if (!review.getUser().getUserId().equals(authenticatedUserId) && !isAdmin(authenticatedUserId)) {
            throw new ResourceNotFoundException("You do not have permission to update this review.");
        }

        review.setComment(reviewDTO.comment());
        review.setRating(reviewDTO.rating());

        return mapToDTO(reviewRepository.save(review));
    }

    public void deleteReview(UUID reviewId, UUID authenticatedUserId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found!"));

        if (!review.getUser().getUserId().equals(authenticatedUserId) && !isAdmin(authenticatedUserId)) {
            throw new ResourceNotFoundException("You do not have permission to delete this review.");
        }

        reviewRepository.delete(review);
    }

    public List<ReviewDTO> getReviewsForExcursion(UUID excursionId) {
        Excursion excursion = excursionRepository.findById(excursionId)
                .orElseThrow(() -> new ResourceNotFoundException("Excursion not found!"));

        return reviewRepository.findByExcursion(excursion).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    private ReviewDTO mapToDTO(Review review) {
        return new ReviewDTO(
                review.getReviewId(),
                review.getUser().getUserId(),
                review.getExcursion().getExcursionId(),
                review.getComment(),
                review.getRating(),
                review.getReviewDate()
        );
    }

    private boolean isAdmin(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found!"));

        return "ADMIN".equals(user.getRole());
    }
}
