package antonionorfo.norflyHorizonTours.payloads;

import jakarta.validation.constraints.NotEmpty;

public record UpdatePasswordDTO(
        @NotEmpty(message = "Current password is required!")
        String currentPassword,

        @NotEmpty(message = "New password is required!")
        String newPassword
) {
}
