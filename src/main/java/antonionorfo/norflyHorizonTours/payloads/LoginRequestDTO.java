package antonionorfo.norflyHorizonTours.payloads;

import jakarta.validation.constraints.NotEmpty;

public record LoginRequestDTO(
        @NotEmpty(message = "Username is required!")
        String username,

        @NotEmpty(message = "Password is required!")
        String password
) {}
