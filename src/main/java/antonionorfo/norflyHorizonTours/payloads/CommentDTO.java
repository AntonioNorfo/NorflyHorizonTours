package antonionorfo.norflyHorizonTours.payloads;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;

public record CommentDTO(
        UUID commentId,

        @NotEmpty(message = "Comment text is required!")
        String textComment,

        LocalDate dateComment,

        Boolean adminResponse,

        @NotNull(message = "User ID is required!")
        UUID userId
) {
}

