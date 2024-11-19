package antonionorfo.norflyHorizonTours.payloads;

import antonionorfo.norflyHorizonTours.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record UserDTO(
        UUID userId,

        @NotEmpty(message = "First name is required!")
        @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters!")
        String firstName,

        @NotEmpty(message = "Last name is required!")
        @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters!")
        String lastName,

        @NotEmpty(message = "Username is required!")
        @Size(min = 2, max = 50, message = "Username must be between 2 and 50 characters!")
        String username,

        @NotEmpty(message = "Email is required!")
        @Email(message = "Please provide a valid email!")
        String email,

        String profilePhotoUrl,

        @NotNull(message = "Role is required!")
        Role role,

        @NotEmpty(message = "Password is required!")
        @Size(min = 8, message = "Password must be at least 8 characters long!")
        String password
) {
}
