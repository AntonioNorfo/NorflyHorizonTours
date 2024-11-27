package antonionorfo.norflyHorizonTours.controllers;

import antonionorfo.norflyHorizonTours.payloads.FavoriteDTO;
import antonionorfo.norflyHorizonTours.services.FavoriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteService favoriteService;

    @PostMapping("/{userId}/favorites/{excursionId}")
    public ResponseEntity<FavoriteDTO> addFavorite(@PathVariable UUID userId, @PathVariable UUID excursionId) {
        return ResponseEntity.ok(favoriteService.addFavorite(userId, excursionId));
    }

    @DeleteMapping("/{userId}/favorites/{excursionId}")
    public ResponseEntity<Void> removeFavorite(@PathVariable UUID userId, @PathVariable UUID excursionId) {
        favoriteService.removeFavorite(userId, excursionId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{userId}/favorites")
    public ResponseEntity<List<FavoriteDTO>> getUserFavorites(@PathVariable UUID userId) {
        return ResponseEntity.ok(favoriteService.getUserFavorites(userId));
    }
}
