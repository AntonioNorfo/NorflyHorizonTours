package antonionorfo.norflyHorizonTours.security;

import antonionorfo.norflyHorizonTours.entities.User;
import antonionorfo.norflyHorizonTours.services.UserService;
import antonionorfo.norflyHorizonTours.tools.JWT;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger logger = LoggerFactory.getLogger(JWTCheckerFilter.class);

    private final JWT jwt;
    private final UserService userService;

    public JWTCheckerFilter(JWT jwt, @Lazy UserService userService) {
        this.jwt = jwt;
        this.userService = userService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String servletPath = request.getServletPath();
        logger.info("=== Incoming Request ===");
        logger.info("Request Path: {}", servletPath);

        if (shouldNotFilter(request)) {
            logger.info("Skipping filter for path: {}", servletPath);
            filterChain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            logger.warn("Missing or malformed Authorization header for path: {}", servletPath);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"error\":\"Missing or malformed Authorization header\"}");
            return;
        }


        String accessToken = authHeader.substring(7);
        try {

            logger.debug("Verifying Token: {}", accessToken);
            jwt.verifyToken(accessToken);

            // Extract user ID and fetch user
            String userIdString = jwt.getUserIdFromToken(accessToken);
            UUID userId = UUID.fromString(userIdString);
            logger.debug("Extracted User ID: {}", userId);

            User currentUser = userService.findById(userId);
            if (currentUser == null) {
                logger.error("User not found for ID: {}", userId);
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Invalid or expired token");
                return;
            }

            logger.info("Authenticated User: {}", currentUser.getUsername());

            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    currentUser, null, currentUser.getAuthorities()
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);

        } catch (Exception ex) {
            logger.error("Error verifying token for path {}: {}", servletPath, ex.getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Invalid or expired token");
            return;
        }

        logger.info("Proceeding with the filter chain for path: {}", servletPath);
        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String servletPath = request.getServletPath();
        boolean shouldSkip = new AntPathMatcher().match("/auth/**", servletPath)
                || new AntPathMatcher().match("/countries/**", servletPath);
        logger.debug("Should not filter? {} for path: {}", shouldSkip, servletPath);
        return shouldSkip;
    }

}
