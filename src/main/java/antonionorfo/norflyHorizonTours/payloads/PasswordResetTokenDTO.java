package antonionorfo.norflyHorizonTours.payloads;

import jakarta.validation.constraints.*;

import java.time.LocalDateTime;
import java.util.UUID;

public record PasswordResetTokenDTO(
        UUID resetTokenId,

        @NotEmpty(message = "Token is required!")
        String token,

        @NotNull(message = "Expiration date is required!")
        LocalDateTime expirationDate,

        @NotNull(message = "Token usage status is required!")
        Boolean isUsed,

        @NotNull(message = "User ID is required!")
        UUID userId
) {
}
