package antonionorfo.norflyHorizonTours.controllers;

import antonionorfo.norflyHorizonTours.payloads.ReviewDTO;
import antonionorfo.norflyHorizonTours.services.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private static final Logger logger = LoggerFactory.getLogger(ReviewController.class);

    private final ReviewService reviewService;

    @PostMapping
    public ResponseEntity<ReviewDTO> addReview(@RequestBody @Valid ReviewDTO reviewDTO) {
        logger.info("Adding review for excursion: {}", reviewDTO.excursionId());
        ReviewDTO createdReview = reviewService.addReview(reviewDTO);
        return ResponseEntity.ok(createdReview);
    }

    @PutMapping("/{reviewId}")
    public ResponseEntity<ReviewDTO> updateReview(@PathVariable UUID reviewId, @RequestBody @Valid ReviewDTO reviewDTO) {
        logger.info("Updating review with ID: {}", reviewId);
        ReviewDTO updatedReview = reviewService.updateReview(reviewId, reviewDTO);
        return ResponseEntity.ok(updatedReview);
    }

    @DeleteMapping("/{reviewId}")
    public ResponseEntity<Void> deleteReview(@PathVariable UUID reviewId) {
        logger.info("Deleting review with ID: {}", reviewId);
        reviewService.deleteReview(reviewId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/excursion/{excursionId}")
    public ResponseEntity<List<ReviewDTO>> getReviewsForExcursion(@PathVariable UUID excursionId) {
        logger.info("Fetching reviews for excursion ID: {}", excursionId);
        List<ReviewDTO> reviews = reviewService.getReviewsForExcursion(excursionId);
        if (reviews.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(reviews);
    }
}
