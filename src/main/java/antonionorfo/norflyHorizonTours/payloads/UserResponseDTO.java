package antonionorfo.norflyHorizonTours.payloads;

import java.util.UUID;

public record UserResponseDTO(
        UUID userId,
        String firstName,
        String lastName,
        String username,
        String email,
        String profilePhotoUrl
) {}
