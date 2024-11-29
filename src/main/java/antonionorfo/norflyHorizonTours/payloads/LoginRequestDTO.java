package antonionorfo.norflyHorizonTours.payloads;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;

public record LoginRequestDTO(
        @NotEmpty(message = "Email is required!")
        @Email(message = "Invalid email format!")
        String email,

        @NotEmpty(message = "Password is required!")
        String password
) {
}
