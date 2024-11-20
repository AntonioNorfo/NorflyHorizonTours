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
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        System.out.println("=== Incoming Request ===");
        System.out.println("Request Path: " + request.getServletPath());
        System.out.println("Request Headers:");
        request.getHeaderNames().asIterator().forEachRemaining(header ->
                System.out.println(header + ": " + request.getHeader(header))
        );

        if (shouldNotFilter(request)) {
            System.out.println("Skipping filter for path: " + request.getServletPath());
            filterChain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader("Authorization");
        System.out.println("Authorization Header: " + authHeader);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            System.out.println("Missing or malformed Authorization header");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Missing or malformed Authorization header");
            return;
        }

        String accessToken = authHeader.substring(7);
        try {
            System.out.println("Verifying Token: " + accessToken);
            jwt.verifyToken(accessToken);

            String userIdString = jwt.getUserIdFromToken(accessToken);
            System.out.println("Extracted User ID: " + userIdString);
            UUID userId = UUID.fromString(userIdString);

            User currentUser = userService.findById(userId.toString());
            System.out.println("Authenticated User: " + currentUser.getUsername());

            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    currentUser, null, currentUser.getAuthorities()
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);

        } catch (Exception ex) {
            System.out.println("Error verifying token: " + ex.getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Invalid or expired token");
            return;
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            System.out.println("Authentication failed, rejecting request");
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().write("Forbidden");
            return;
        }

        System.out.println("Proceeding with the filter chain...");
        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String servletPath = request.getServletPath();
        boolean shouldSkip = new AntPathMatcher().match("/auth/**", servletPath)
                || new AntPathMatcher().match("/countries/**", servletPath);
        System.out.println("Should not filter? " + shouldSkip + " for path: " + servletPath);
        return shouldSkip;
    }
}
