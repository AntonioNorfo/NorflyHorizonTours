package antonionorfo.norflyHorizonTours.services;

import antonionorfo.norflyHorizonTours.entities.User;
import antonionorfo.norflyHorizonTours.exception.BadRequestException;
import antonionorfo.norflyHorizonTours.exception.DuplicateResourceException;
import antonionorfo.norflyHorizonTours.exception.ResourceNotFoundException;
import antonionorfo.norflyHorizonTours.payloads.UpdateUserDTO;
import antonionorfo.norflyHorizonTours.payloads.UserDTO;
import antonionorfo.norflyHorizonTours.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CloudinaryService cloudinaryService;

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public User findById(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
    }

    public User updateUser(UUID userId, UserDTO updatedUser) {
        User user = findById(userId);

        if (updatedUser.firstName() != null) user.setFirstName(updatedUser.firstName());
        if (updatedUser.lastName() != null) user.setLastName(updatedUser.lastName());

        if (updatedUser.email() != null && !user.getEmail().equals(updatedUser.email())) {
            if (userRepository.existsByEmail(updatedUser.email())) {
                throw new DuplicateResourceException("Email already in use: " + updatedUser.email());
            }
            user.setEmail(updatedUser.email());
        }

        if (updatedUser.profilePhotoUrl() != null) user.setProfilePhotoUrl(updatedUser.profilePhotoUrl());

        return userRepository.save(user);
    }

    public void verifyPassword(UUID userId, String password) {
        User user = findById(userId);

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BadRequestException("Incorrect password");
        }
    }

    public User updateUserDetails(UUID userId, UpdateUserDTO updateUserDTO) {
        User user = findById(userId);

        if (updateUserDTO.email() != null && !updateUserDTO.email().equals(user.getEmail())) {
            if (userRepository.existsByEmail(updateUserDTO.email())) {
                throw new DuplicateResourceException("Email already in use");
            }
            user.setEmail(updateUserDTO.email());
        }

        user.setFirstName(updateUserDTO.firstName());
        user.setLastName(updateUserDTO.lastName());

        return userRepository.save(user);
    }

    public void updatePassword(UUID userId, String currentPassword, String newPassword) {
        User user = findById(userId);

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new BadRequestException("Current password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    public boolean emailExists(String email) {
        return userRepository.existsByEmail(email);
    }

    public void deleteUser(UUID userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with ID: " + userId);
        }
        userRepository.deleteById(userId);
    }

    public User updateProfilePhoto(UUID userId, MultipartFile file) throws IOException {
        User user = findById(userId);

        String photoUrl = cloudinaryService.uploadImage(file);

        user.setProfilePhotoUrl(photoUrl);
        return userRepository.save(user);
    }

    public String resetPassword(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        String resetToken = UUID.randomUUID().toString();

        return "Password reset token for " + email + ": " + resetToken;
    }
}
