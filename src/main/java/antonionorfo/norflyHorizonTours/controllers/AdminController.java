package antonionorfo.norflyHorizonTours.controllers;

import antonionorfo.norflyHorizonTours.payloads.AdminPostDTO;
import antonionorfo.norflyHorizonTours.payloads.ExcursionDTO;
import antonionorfo.norflyHorizonTours.services.AdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @PostMapping("/excursions")
    public ResponseEntity<ExcursionDTO> createExcursion(@RequestBody @Valid ExcursionDTO excursionDTO) {
        return ResponseEntity.ok(adminService.createExcursion(excursionDTO));
    }

    @PutMapping("/excursions/{excursionId}")
    public ResponseEntity<ExcursionDTO> updateExcursion(@PathVariable UUID excursionId, @RequestBody @Valid ExcursionDTO excursionDTO) {
        return ResponseEntity.ok(adminService.updateExcursion(excursionId, excursionDTO));
    }

    @DeleteMapping("/excursions/{excursionId}")
    public ResponseEntity<Void> deleteExcursion(@PathVariable UUID excursionId) {
        adminService.deleteExcursion(excursionId);
        return ResponseEntity.noContent().build();
    }


    @PutMapping("/users/{userId}/block")
    public ResponseEntity<Void> blockUser(@PathVariable UUID userId) {
        adminService.blockUser(userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/posts")
    public ResponseEntity<AdminPostDTO> createPost(@RequestBody @Valid AdminPostDTO adminPostDTO) {
        return ResponseEntity.ok(adminService.createPost(adminPostDTO));
    }

    @PutMapping("/posts/{postId}")
    public ResponseEntity<AdminPostDTO> updatePost(@PathVariable UUID postId, @RequestBody @Valid AdminPostDTO adminPostDTO) {
        return ResponseEntity.ok(adminService.updatePost(postId, adminPostDTO));
    }

    @DeleteMapping("/posts/{postId}")
    public ResponseEntity<Void> deletePost(@PathVariable UUID postId) {
        adminService.deletePost(postId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/reviews/{reviewId}")
    public ResponseEntity<Void> deleteReview(@PathVariable UUID reviewId) {
        adminService.deleteReview(reviewId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/users/{userId}/reviews/block")
    public ResponseEntity<Void> blockUserReviews(@PathVariable UUID userId) {
        adminService.blockUserReviews(userId);
        return ResponseEntity.noContent().build();
    }

}
