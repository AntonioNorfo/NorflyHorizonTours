package antonionorfo.norflyHorizonTours.security;

import antonionorfo.norflyHorizonTours.entities.User;
import antonionorfo.norflyHorizonTours.services.UserService;
import antonionorfo.norflyHorizonTours.tools.JWT;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
public class JWTCheckerFilter extends OncePerRequestFilter {

    private final JWT jwt;
    private final UserService userService;

    public JWTCheckerFilter(JWT jwt, @Lazy UserService userService) {
        this.jwt = jwt;
        this.userService = userService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Missing or malformed Authorization header");
            return;
        }

        String accessToken = authHeader.substring(7);
        try {
            jwt.verifyToken(accessToken);
            String userIdString = jwt.getUserIdFromToken(accessToken);
            UUID userId = UUID.fromString(userIdString);

            User currentUser = userService.findById(userId.toString());

            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    currentUser, null, currentUser.getAuthorities()
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);

        } catch (Exception ex) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Invalid or expired token");
            return;
        }

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        return new AntPathMatcher().match("/auth/**", request.getServletPath());
    }
}
