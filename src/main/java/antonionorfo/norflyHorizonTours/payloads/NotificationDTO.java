package antonionorfo.norflyHorizonTours.payloads;

import jakarta.validation.constraints.*;

import java.time.LocalDateTime;
import java.util.UUID;

public record NotificationDTO(
        UUID notificationId,

        @NotEmpty(message = "Message is required!")
        String message,

        @NotNull(message = "Creation date is required!")
        LocalDateTime dateCreated,

        @NotNull(message = "Read status is required!")
        Boolean isRead,

        @NotNull(message = "User ID is required!")
        UUID userId
) {
}
