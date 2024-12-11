package antonionorfo.norflyHorizonTours.payloads;

import java.util.UUID;

public record LoginResponseDTO(
        String accessToken,
        UUID userId
) {
}
