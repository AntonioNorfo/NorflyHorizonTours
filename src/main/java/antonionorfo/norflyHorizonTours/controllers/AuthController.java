package antonionorfo.norflyHorizonTours.controllers;

import antonionorfo.norflyHorizonTours.entities.User;
import antonionorfo.norflyHorizonTours.enums.Role;
import antonionorfo.norflyHorizonTours.exception.BadRequestException;
import antonionorfo.norflyHorizonTours.exception.DuplicateResourceException;
import antonionorfo.norflyHorizonTours.payloads.*;
import antonionorfo.norflyHorizonTours.repositories.UserRepository;
import antonionorfo.norflyHorizonTours.services.AuthService;
import antonionorfo.norflyHorizonTours.tools.MailgunSender;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final MailgunSender mailgunSender;
    private final Cloudinary cloudinary;

    @PostMapping("/login")
    public LoginResponseDTO login(@RequestBody LoginRequestDTO body) {
        var token = authService.checkCredentialsAndGenerateToken(body);
        var user = userRepository.findByEmail(body.email())
                .orElseThrow(() -> new BadRequestException("User not found"));

        var response = new LoginResponseDTO(token, user.getUserId());
        System.out.println("Response: " + response);
        return response;
    }


    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponseDTO register(
            @RequestPart("user") String userJson,
            @RequestPart(value = "file", required = false) MultipartFile file,
            BindingResult validationResult) {

        ObjectMapper objectMapper = new ObjectMapper();
        UserDTO body;
        try {
            body = objectMapper.readValue(userJson, UserDTO.class);
        } catch (JsonProcessingException e) {
            throw new BadRequestException("Invalid JSON format for user: " + e.getMessage());
        }

        if (validationResult.hasErrors()) {
            String message = validationResult.getAllErrors().stream()
                    .map(objectError -> objectError.getDefaultMessage())
                    .collect(Collectors.joining(". "));
            throw new BadRequestException("Payload error: " + message);
        }

        if (userRepository.existsByEmail(body.email())) {
            throw new DuplicateResourceException("Email already in use: " + body.email());
        }
        if (userRepository.existsByUsername(body.username())) {
            throw new DuplicateResourceException("Username already in use: " + body.username());
        }

        User newUser = new User();
        newUser.setFirstName(body.firstName());
        newUser.setLastName(body.lastName());
        newUser.setUsername(body.username());
        newUser.setEmail(body.email());
        newUser.setPassword(passwordEncoder.encode(body.password()));
        newUser.setRole(Role.USER);

        if (file != null && !file.isEmpty()) {
            try {
                Map<String, Object> uploadResult = cloudinary.uploader().upload(
                        file.getBytes(),
                        ObjectUtils.emptyMap()
                );

                String photoUrl = (String) uploadResult.get("secure_url");
                newUser.setProfilePhotoUrl(photoUrl);
            } catch (IOException e) {
                throw new BadRequestException("Failed to upload profile photo: " + e.getMessage());
            }
        }

        userRepository.save(newUser);

        try {
            mailgunSender.sendRegistrationEmail(newUser);
        } catch (Exception e) {
            System.err.println("Error sending registration email: " + e.getMessage());
        }

        return new UserResponseDTO(
                newUser.getUserId(),
                newUser.getFirstName(),
                newUser.getLastName(),
                newUser.getUsername(),
                newUser.getEmail(),
                newUser.getProfilePhotoUrl()
        );
    }


    @PostMapping("/register/admin")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponseDTO registerAdmin(@RequestBody @Validated AdminDTO body, BindingResult validationResult) {
        if (validationResult.hasErrors()) {
            String message = validationResult.getAllErrors().stream()
                    .map(objectError -> objectError.getDefaultMessage())
                    .collect(Collectors.joining(". "));
            throw new BadRequestException("Payload error: " + message);
        }

        if (userRepository.existsByEmail(body.email())) {
            throw new DuplicateResourceException("Email already in use: " + body.email());
        }
        if (userRepository.existsByUsername(body.username())) {
            throw new DuplicateResourceException("Username already in use: " + body.username());
        }

        User newAdmin = new User();
        newAdmin.setFirstName(body.firstName());
        newAdmin.setLastName(body.lastName());
        newAdmin.setUsername(body.username());
        newAdmin.setEmail(body.email());
        newAdmin.setPassword(passwordEncoder.encode(body.password()));
        newAdmin.setRole(Role.ADMIN);

        userRepository.save(newAdmin);

        return new UserResponseDTO(
                newAdmin.getUserId(),
                newAdmin.getFirstName(),
                newAdmin.getLastName(),
                newAdmin.getUsername(),
                newAdmin.getEmail(),
                newAdmin.getProfilePhotoUrl()
        );
    }


}


