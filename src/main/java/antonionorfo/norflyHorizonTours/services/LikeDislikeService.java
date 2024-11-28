package antonionorfo.norflyHorizonTours.services;

import antonionorfo.norflyHorizonTours.entities.LikeDislike;
import antonionorfo.norflyHorizonTours.entities.Review;
import antonionorfo.norflyHorizonTours.entities.User;
import antonionorfo.norflyHorizonTours.exception.ResourceNotFoundException;
import antonionorfo.norflyHorizonTours.payloads.LikeDislikeDTO;
import antonionorfo.norflyHorizonTours.repositories.LikeDislikeRepository;
import antonionorfo.norflyHorizonTours.repositories.ReviewRepository;
import antonionorfo.norflyHorizonTours.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LikeDislikeService {

    private final LikeDislikeRepository likeDislikeRepository;
    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;

    public LikeDislikeDTO addLikeDislike(LikeDislikeDTO likeDislikeDTO, UUID reviewId) {
        User user = userRepository.findById(likeDislikeDTO.userId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + likeDislikeDTO.userId()));

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found with ID: " + reviewId));

        LikeDislike existingLikeDislike = likeDislikeRepository.findByUserAndReview(user, review);
        if (existingLikeDislike != null) {
            existingLikeDislike.setReactionType(likeDislikeDTO.reactionType());
            likeDislikeRepository.save(existingLikeDislike);
            return mapToLikeDislikeDTO(existingLikeDislike);
        } else {
            LikeDislike likeDislike = new LikeDislike();
            likeDislike.setUser(user);
            likeDislike.setReview(review);
            likeDislike.setReactionType(likeDislikeDTO.reactionType());

            likeDislikeRepository.save(likeDislike);
            return mapToLikeDislikeDTO(likeDislike);
        }
    }

    public LikeDislikeDTO updateLikeDislike(LikeDislikeDTO likeDislikeDTO, UUID reviewId) {

        User user = userRepository.findById(likeDislikeDTO.userId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + likeDislikeDTO.userId()));

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found with ID: " + reviewId));

        LikeDislike likeDislike = likeDislikeRepository.findByUserAndReview(user, review);
        if (likeDislike == null) {
            throw new ResourceNotFoundException("No like/dislike found for this user and review.");
        }

        likeDislike.setReactionType(likeDislikeDTO.reactionType());
        likeDislikeRepository.save(likeDislike);

        return mapToLikeDislikeDTO(likeDislike);
    }

    public void removeLikeDislike(UUID userId, UUID reviewId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found with ID: " + reviewId));

        LikeDislike likeDislike = likeDislikeRepository.findByUserAndReview(user, review);
        if (likeDislike == null) {
            throw new ResourceNotFoundException("No like/dislike found for this user and review.");
        }

        likeDislikeRepository.delete(likeDislike);
    }

    public List<LikeDislikeDTO> getLikesForReview(UUID reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found with ID: " + reviewId));

        return likeDislikeRepository.findByReview(review).stream()
                .map(this::mapToLikeDislikeDTO)
                .collect(Collectors.toList());
    }

    private LikeDislikeDTO mapToLikeDislikeDTO(LikeDislike likeDislike) {
        return new LikeDislikeDTO(
                likeDislike.getLikeDislikeId(),
                likeDislike.getReactionType(),
                likeDislike.getUser().getUserId()
        );
    }
}

