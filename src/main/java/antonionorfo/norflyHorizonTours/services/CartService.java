package antonionorfo.norflyHorizonTours.services;

import antonionorfo.norflyHorizonTours.entities.Cart;
import antonionorfo.norflyHorizonTours.entities.Excursion;
import antonionorfo.norflyHorizonTours.entities.User;
import antonionorfo.norflyHorizonTours.exception.BadRequestException;
import antonionorfo.norflyHorizonTours.exception.ResourceNotFoundException;
import antonionorfo.norflyHorizonTours.payloads.CartDTO;
import antonionorfo.norflyHorizonTours.repositories.CartRepository;
import antonionorfo.norflyHorizonTours.repositories.ExcursionRepository;
import antonionorfo.norflyHorizonTours.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final UserRepository userRepository;
    private final ExcursionRepository excursionRepository;

    public List<CartDTO> getUserCart(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        List<Cart> cartItems = cartRepository.findByUser(user);
        return cartItems.stream().map(this::mapToCartDTO).collect(Collectors.toList());
    }

    public CartDTO addToCart(UUID userId, UUID excursionId, Integer quantity) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        Excursion excursion = excursionRepository.findById(excursionId)
                .orElseThrow(() -> new ResourceNotFoundException("Excursion not found with ID: " + excursionId));

        if (excursion.getMaxParticipants() < quantity) {
            throw new BadRequestException("Not enough available spots for this excursion.");
        }

        List<Cart> existingCartItems = cartRepository.findByUser(user);

        for (Cart item : existingCartItems) {
            if (hasConflict(item.getExcursion(), excursion)) {
                throw new BadRequestException("The excursion conflicts with another in your cart.");
            }
        }

        excursion.setMaxParticipants(excursion.getMaxParticipants() - quantity);
        excursionRepository.save(excursion);

        Cart cart = new Cart();
        cart.setUser(user);
        cart.setExcursion(excursion);
        cart.setQuantity(quantity != null ? quantity : 1);
        cart.setDateAddedCart(LocalDateTime.now());

        cartRepository.save(cart);

        return mapToCartDTO(cart);
    }


    public void removeFromCart(UUID userId, UUID excursionId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        Cart cart = cartRepository.findByUserAndExcursion_ExcursionId(user, excursionId);
        if (cart == null) {
            throw new ResourceNotFoundException("Cart item not found for user ID: " + userId + " and excursion ID: " + excursionId);
        }

        cartRepository.delete(cart);
    }


    public void checkoutCart(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        List<Cart> cartItems = cartRepository.findByUser(user);
        if (cartItems.isEmpty()) {
            throw new ResourceNotFoundException("Cart is empty!");
        }

        for (Cart item : cartItems) {
            Excursion excursion = item.getExcursion();

            if (excursion.getMaxParticipants() < item.getQuantity()) {
                throw new BadRequestException("Not enough spots available for " + excursion.getTitle());
            }

        }

        cartRepository.deleteAll(cartItems);
    }

    private CartDTO mapToCartDTO(Cart cart) {
        return new CartDTO(
                cart.getCartId(),
                cart.getUser().getUserId(),
                cart.getExcursion().getExcursionId(),
                cart.getDateAddedCart(),
                cart.getQuantity()
        );
    }

    private boolean hasConflict(Excursion existing, Excursion newExcursion) {
        return !(newExcursion.getEndDate().isBefore(existing.getStartDate()) ||
                newExcursion.getStartDate().isAfter(existing.getEndDate()));
    }
}
