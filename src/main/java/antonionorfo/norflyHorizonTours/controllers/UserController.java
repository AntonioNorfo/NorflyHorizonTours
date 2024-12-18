package antonionorfo.norflyHorizonTours.controllers;

import antonionorfo.norflyHorizonTours.entities.User;
import antonionorfo.norflyHorizonTours.payloads.UpdatePasswordDTO;
import antonionorfo.norflyHorizonTours.payloads.UpdateUserDTO;
import antonionorfo.norflyHorizonTours.payloads.UserDTO;
import antonionorfo.norflyHorizonTours.services.CloudinaryService;
import antonionorfo.norflyHorizonTours.services.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;
    private final CloudinaryService cloudinaryService;

    @GetMapping("/{userId}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable UUID userId) {
        logger.info("Fetching user details for ID: {}", userId);
        User user = userService.findById(userId);
        return ResponseEntity.ok(new UserDTO(
                user.getUserId(),
                user.getFirstName(),
                user.getLastName(),
                user.getUsername(),
                user.getEmail(),
                user.getProfilePhotoUrl(),
                user.getRole(),
                null
        ));
    }

    @GetMapping("/email")
    public ResponseEntity<UserDTO> findByEmail(@RequestParam String email) {
        logger.info("Fetching user by email: {}", email);
        Optional<User> user = userService.findByEmail(email);
        return user.map(u -> ResponseEntity.ok(new UserDTO(
                        u.getUserId(),
                        u.getFirstName(),
                        u.getLastName(),
                        null,
                        u.getEmail(),
                        u.getProfilePhotoUrl(),
                        u.getRole(),
                        null)))
                .orElse(ResponseEntity.notFound().build());
    }


    @PutMapping("/{userId}")
    public ResponseEntity<?> updateUser(
            @PathVariable UUID userId,
            @RequestBody @Valid UpdateUserDTO updateUserDTO) {

        logger.info("Richiesta di aggiornamento per user ID: {}", userId);
        logger.info("Dati ricevuti: firstName={}, lastName={}, password={}",
                updateUserDTO.firstName(), updateUserDTO.lastName(), updateUserDTO.password());

        try {
            userService.verifyPassword(userId, updateUserDTO.password());
            User updatedUser = userService.updateUserDetails(userId, updateUserDTO);

            return ResponseEntity.ok(new UserDTO(
                    updatedUser.getUserId(),
                    updatedUser.getFirstName(),
                    updatedUser.getLastName(),
                    updatedUser.getUsername(),
                    updatedUser.getEmail(),
                    updatedUser.getProfilePhotoUrl(),
                    updatedUser.getRole(),
                    null
            ));
        } catch (IllegalArgumentException e) {
            logger.error("Password errata per user ID: {}", userId);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid password.");
        }
    }


    @PutMapping("/{userId}/password")
    public ResponseEntity<String> updatePassword(
            @PathVariable UUID userId,
            @RequestBody @Valid UpdatePasswordDTO passwordPayload) {
        logger.info("Updating password for user ID: {}", userId);
        userService.updatePassword(userId, passwordPayload.currentPassword(), passwordPayload.newPassword());
        return ResponseEntity.ok("Password updated successfully");
    }

    @PutMapping("/{userId}/photo")
    public ResponseEntity<String> updateProfilePhoto(
            @PathVariable UUID userId,
            @RequestParam("file") MultipartFile file) {
        try {
            User updatedUser = userService.updateProfilePhoto(userId, file);
            return ResponseEntity.ok(updatedUser.getProfilePhotoUrl());
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to upload profile photo: " + e.getMessage());
        }
    }


    @DeleteMapping("/{userId}")
    public ResponseEntity<String> deleteUser(@PathVariable UUID userId) {
        logger.info("Deleting user with ID: {}", userId);
        userService.deleteUser(userId);
        return ResponseEntity.ok("User deleted successfully.");
    }

    @PostMapping("/password/reset")
    public ResponseEntity<String> resetPassword(@RequestParam String email) {
        logger.info("Initiating password reset for email: {}", email);
        if (!userService.emailExists(email)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Email not found!");
        }
        return ResponseEntity.ok("Password reset link sent to: " + email);
    }
}
