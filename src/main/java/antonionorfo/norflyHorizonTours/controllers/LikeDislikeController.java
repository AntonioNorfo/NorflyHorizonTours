package antonionorfo.norflyHorizonTours.controllers;

import antonionorfo.norflyHorizonTours.payloads.LikeDislikeDTO;
import antonionorfo.norflyHorizonTours.services.LikeDislikeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/reviews/{reviewId}/likes")
@RequiredArgsConstructor
public class LikeDislikeController {

    private final LikeDislikeService likeDislikeService;

    @PostMapping
    public ResponseEntity<LikeDislikeDTO> addLikeDislike(
            @PathVariable UUID reviewId,
            @RequestBody @Valid LikeDislikeDTO likeDislikeDTO,
            @RequestHeader(value = "User-ID") UUID authenticatedUserId
    ) {
        likeDislikeDTO = likeDislikeDTO.withUserId(authenticatedUserId);
        LikeDislikeDTO response = likeDislikeService.addLikeDislike(likeDislikeDTO, reviewId);
        return ResponseEntity.ok(response);
    }

    @PutMapping
    public ResponseEntity<LikeDislikeDTO> updateLikeDislike(
            @PathVariable UUID reviewId,
            @RequestBody @Valid LikeDislikeDTO likeDislikeDTO,
            @RequestHeader(value = "User-ID") UUID authenticatedUserId
    ) {
        likeDislikeDTO = likeDislikeDTO.withUserId(authenticatedUserId);
        LikeDislikeDTO response = likeDislikeService.updateLikeDislike(likeDislikeDTO, reviewId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping
    public ResponseEntity<Void> removeLikeDislike(
            @PathVariable UUID reviewId,
            @RequestHeader(value = "User-ID") UUID authenticatedUserId
    ) {
        likeDislikeService.removeLikeDislike(authenticatedUserId, reviewId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<LikeDislikeDTO>> getLikesForReview(@PathVariable UUID reviewId) {
        return ResponseEntity.ok(likeDislikeService.getLikesForReview(reviewId));
    }
}
