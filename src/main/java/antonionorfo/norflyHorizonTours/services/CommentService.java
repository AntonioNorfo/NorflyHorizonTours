package antonionorfo.norflyHorizonTours.services;

import antonionorfo.norflyHorizonTours.entities.AdminPost;
import antonionorfo.norflyHorizonTours.entities.Comment;
import antonionorfo.norflyHorizonTours.entities.User;
import antonionorfo.norflyHorizonTours.enums.Role;
import antonionorfo.norflyHorizonTours.exception.ResourceNotFoundException;
import antonionorfo.norflyHorizonTours.payloads.CommentDTO;
import antonionorfo.norflyHorizonTours.repositories.AdminPostRepository;
import antonionorfo.norflyHorizonTours.repositories.CommentRepository;
import antonionorfo.norflyHorizonTours.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final AdminPostRepository adminPostRepository;
    private final UserRepository userRepository;

    public CommentDTO addComment(UUID adminPostId, CommentDTO commentDTO) {
        User user = userRepository.findById(commentDTO.userId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + commentDTO.userId()));

        AdminPost adminPost = adminPostRepository.findById(adminPostId)
                .orElseThrow(() -> new ResourceNotFoundException("Admin post not found with ID: " + adminPostId));

        Comment comment = new Comment();
        comment.setTextComment(commentDTO.textComment());
        comment.setDateComment(commentDTO.dateComment() != null ? commentDTO.dateComment() : LocalDate.now());
        comment.setAdminResponse(commentDTO.adminResponse() != null ? commentDTO.adminResponse() : false);
        comment.setUser(user);
        comment.setAdminPost(adminPost);

        commentRepository.save(comment);

        return mapToCommentDTO(comment);
    }

    public List<CommentDTO> getCommentsByPost(UUID adminPostId) {
        AdminPost adminPost = adminPostRepository.findById(adminPostId)
                .orElseThrow(() -> new ResourceNotFoundException("Admin post not found with ID: " + adminPostId));

        List<Comment> comments = commentRepository.findByAdminPost(adminPost);

        return comments.stream().map(this::mapToCommentDTO).collect(Collectors.toList());
    }

    public CommentDTO updateComment(UUID adminPostId, UUID commentId, CommentDTO commentDTO, UUID userId) {
        authorizeCommentAction(userId, commentId);

        AdminPost adminPost = adminPostRepository.findById(adminPostId)
                .orElseThrow(() -> new ResourceNotFoundException("Admin post not found with ID: " + adminPostId));

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found with ID: " + commentId));

        if (commentDTO.textComment() != null) {
            comment.setTextComment(commentDTO.textComment());
        }

        if (commentDTO.adminResponse() != null) {
            comment.setAdminResponse(commentDTO.adminResponse());
        }

        comment.setDateComment(LocalDate.now());
        commentRepository.save(comment);

        return mapToCommentDTO(comment);
    }

    public void deleteComment(UUID commentId, UUID userId) {
        authorizeCommentAction(userId, commentId);
        commentRepository.deleteById(commentId);
    }

    private void authorizeCommentAction(UUID userId, UUID commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found with ID: " + commentId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        // Check if user is the author of the comment or an admin
        if (!comment.getUser().getUserId().equals(userId) && !user.getRole().equals(Role.ADMIN)) {
            throw new SecurityException("You are not authorized to perform this action.");
        }
    }

    private CommentDTO mapToCommentDTO(Comment comment) {
        return new CommentDTO(
                comment.getCommentId(),
                comment.getTextComment(),
                comment.getDateComment(),
                comment.getAdminResponse(),
                comment.getUser().getUserId()
        );
    }
}
