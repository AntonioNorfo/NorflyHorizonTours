package antonionorfo.norflyHorizonTours.payloads;

import antonionorfo.norflyHorizonTours.enums.ReactionType;
import jakarta.validation.constraints.*;

import java.util.UUID;

public record LikeDislikeDTO(
        UUID likeDislikeId,

        @NotNull(message = "Reaction type is required!")
        ReactionType reactionType,

        @NotNull(message = "User ID is required!")
        UUID userId,

        @NotNull(message = "Review ID is required!")
        UUID reviewId
) {
}
