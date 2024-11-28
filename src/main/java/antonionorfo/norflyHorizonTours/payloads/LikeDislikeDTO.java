package antonionorfo.norflyHorizonTours.payloads;

import antonionorfo.norflyHorizonTours.enums.ReactionType;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record LikeDislikeDTO(
        UUID likeDislikeId,

        @NotNull(message = "Reaction type is required!")
        ReactionType reactionType,

        UUID userId
) {
    public LikeDislikeDTO withUserId(UUID userId) {
        return new LikeDislikeDTO(this.likeDislikeId, this.reactionType, userId);
    }

    public LikeDislikeDTO withReviewId(UUID reviewId) {
        return new LikeDislikeDTO(this.likeDislikeId, this.reactionType, this.userId);
    }
}
