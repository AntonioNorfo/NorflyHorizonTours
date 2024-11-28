package antonionorfo.norflyHorizonTours.controllers;

import antonionorfo.norflyHorizonTours.payloads.CommentDTO;
import antonionorfo.norflyHorizonTours.services.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/posts/{postId}/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @PostMapping
    public ResponseEntity<CommentDTO> addComment(
            @PathVariable UUID postId,
            @RequestBody @Valid CommentDTO commentDTO
    ) {
        return ResponseEntity.ok(commentService.addComment(postId, commentDTO));
    }

    @GetMapping
    public ResponseEntity<List<CommentDTO>> getComments(@PathVariable UUID postId) {
        return ResponseEntity.ok(commentService.getCommentsByPost(postId));
    }

    @PutMapping("/{commentId}")
    public ResponseEntity<CommentDTO> updateComment(
            @PathVariable UUID postId,
            @PathVariable UUID commentId,
            @RequestBody @Valid CommentDTO commentDTO,
            @RequestHeader("userId") UUID userId
    ) {
        return ResponseEntity.ok(commentService.updateComment(postId, commentId, commentDTO, userId));
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable UUID commentId,
            @RequestHeader("userId") UUID userId
    ) {
        commentService.deleteComment(commentId, userId);
        return ResponseEntity.noContent().build();
    }
}
