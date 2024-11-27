package antonionorfo.norflyHorizonTours.payloads;

import jakarta.validation.constraints.NotBlank;

public record UpdateUserDTO(
        @NotBlank(message = "First name is required") String firstName,
        @NotBlank(message = "Last name is required") String lastName,
        @NotBlank(message = "Username is required") String username,
        @NotBlank(message = "Email is required") String email,
        @NotBlank(message = "Password is required") String password
) {
}
