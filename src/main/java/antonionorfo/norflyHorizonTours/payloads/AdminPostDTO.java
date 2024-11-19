package antonionorfo.norflyHorizonTours.payloads;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.UUID;

public record AdminPostDTO(
        UUID adminPostId,

        @NotEmpty(message = "Title is required!")
        @Size(min = 2, max = 100, message = "Title must be between 2 and 100 characters!")
        String titlePost,

        @NotEmpty(message = "Content is required!")
        String contentPost,

        String photoPostAdmin,
        String videoPostAdmin,

        @NotNull(message = "Publication date is required!")
        LocalDate publicationDate
) {
}
