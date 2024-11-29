package antonionorfo.norflyHorizonTours.services;

import antonionorfo.norflyHorizonTours.entities.User;
import antonionorfo.norflyHorizonTours.exception.UnauthorizedException;
import antonionorfo.norflyHorizonTours.payloads.LoginRequestDTO;
import antonionorfo.norflyHorizonTours.repositories.UserRepository;
import antonionorfo.norflyHorizonTours.tools.JWT;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JWT jwt;

    public String checkCredentialsAndGenerateToken(LoginRequestDTO loginRequest) {
        User user = userRepository.findByUsername(loginRequest.email())
                .orElseThrow(() -> new UnauthorizedException("Credenziali errate!"));

        if (!passwordEncoder.matches(loginRequest.password(), user.getPassword())) {
            throw new UnauthorizedException("Credenziali errate!");
        }


        return jwt.createToken(user);
    }
}
