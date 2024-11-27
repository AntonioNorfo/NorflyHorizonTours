package antonionorfo.norflyHorizonTours.payloads;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

public record AdminDTO(
        @NotEmpty(message = "First name is required!")
        @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters!")
        String firstName,

        @NotEmpty(message = "Last name is required!")
        @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters!")
        String lastName,

        @NotEmpty(message = "Username is required!")
        @Size(min = 4, max = 50, message = "Username must be between 4 and 50 characters!")
        String username,

        @NotEmpty(message = "Email is required!")
        @Email(message = "Invalid email format!")
        String email,

        @NotEmpty(message = "Password is required!")
        @Size(min = 8, max = 50, message = "Password must be between 8 and 50 characters!")
        String password
) {}
