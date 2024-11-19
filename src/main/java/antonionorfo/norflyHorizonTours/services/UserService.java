package antonionorfo.norflyHorizonTours.services;

import antonionorfo.norflyHorizonTours.entities.User;
import antonionorfo.norflyHorizonTours.enums.Role;
import antonionorfo.norflyHorizonTours.exception.DuplicateResourceException;
import antonionorfo.norflyHorizonTours.exception.ResourceNotFoundException;
import antonionorfo.norflyHorizonTours.payloads.UserDTO;
import antonionorfo.norflyHorizonTours.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public User registerUser(UserDTO userDTO) {
        // Check for duplicate email and username
        if (userRepository.existsByEmail(userDTO.email())) {
            throw new DuplicateResourceException("Email already in use: " + userDTO.email());
        }
        if (userRepository.existsByUsername(userDTO.username())) {
            throw new DuplicateResourceException("Username already in use: " + userDTO.username());
        }

        User newUser = new User();
        newUser.setFirstName(userDTO.firstName());
        newUser.setLastName(userDTO.lastName());
        newUser.setUsername(userDTO.username());
        newUser.setEmail(userDTO.email());
        newUser.setPassword(passwordEncoder.encode(userDTO.password()));
        newUser.setRole(Role.USER);

        return userRepository.save(newUser);
    }

    public User findById(String userId) {
        return userRepository.findById(UUID.fromString(userId))
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
    }
}
