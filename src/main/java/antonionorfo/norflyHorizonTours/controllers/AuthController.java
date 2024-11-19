package antonionorfo.norflyHorizonTours.controllers;

import antonionorfo.norflyHorizonTours.entities.User;
import antonionorfo.norflyHorizonTours.exception.BadRequestException;
import antonionorfo.norflyHorizonTours.payloads.LoginRequestDTO;
import antonionorfo.norflyHorizonTours.payloads.LoginResponseDTO;
import antonionorfo.norflyHorizonTours.payloads.UserDTO;
import antonionorfo.norflyHorizonTours.payloads.UserResponseDTO;
import antonionorfo.norflyHorizonTours.services.AuthService;
import antonionorfo.norflyHorizonTours.services.UserService;
import antonionorfo.norflyHorizonTours.tools.MailgunSender;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.stream.Collectors;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserService userService;
    private final MailgunSender mailgunSender;

    @PostMapping("/login")
    public LoginResponseDTO login(@RequestBody LoginRequestDTO body) {
        return new LoginResponseDTO(authService.checkCredentialsAndGenerateToken(body));
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponseDTO register(@RequestBody @Validated UserDTO body, BindingResult validationResult) {
        if (validationResult.hasErrors()) {
            String message = validationResult.getAllErrors().stream()
                    .map(objectError -> objectError.getDefaultMessage())
                    .collect(Collectors.joining(". "));
            throw new BadRequestException("Errore nel payload: " + message);
        }

        User newUser = userService.registerUser(body);

        try {
            mailgunSender.sendRegistrationEmail(newUser);
        } catch (Exception e) {
            System.err.println("Errore nell'invio dell'email: " + e.getMessage());
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

}

