package antonionorfo.norflyHorizonTours.services;

import antonionorfo.norflyHorizonTours.entities.*;
import antonionorfo.norflyHorizonTours.exception.BadRequestException;
import antonionorfo.norflyHorizonTours.exception.ResourceNotFoundException;
import antonionorfo.norflyHorizonTours.payloads.CartDTO;
import antonionorfo.norflyHorizonTours.payloads.CartItemDTO;
import antonionorfo.norflyHorizonTours.repositories.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.stream.Collectors;

import static antonionorfo.norflyHorizonTours.tools.MailgunSender.logger;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;
    private final ExcursionRepository excursionRepository;
    private final AvailabilityDateRepository availabilityDateRepository;

    public CartDTO getUserCart(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        Cart cart = cartRepository.findByUser(user)
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setUser(user);
                    newCart.setDateAddedCart(LocalDateTime.now());
                    return cartRepository.save(newCart);
                });

        return mapToCartDTO(cart);
    }

    @Transactional
    public CartDTO addToCart(UUID userId, UUID excursionId, UUID availabilityDateId, Integer quantity) {

        logger.info("Attempting to add item to cart. User ID: {}, Excursion ID: {}, AvailabilityDate ID: {}, Quantity: {}",
                userId, excursionId, availabilityDateId, quantity);

        if (quantity == null || quantity <= 0) {
            logger.error("Invalid quantity: {}. Quantity must be greater than zero.", quantity);
            throw new BadRequestException("Quantity must be greater than zero.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    logger.error("User not found with ID: {}", userId);
                    return new ResourceNotFoundException("User not found with ID: " + userId);
                });
        logger.debug("User retrieved: {}", user.getUserId());

        Excursion excursion = excursionRepository.findById(excursionId)
                .orElseThrow(() -> {
                    logger.error("Excursion not found with ID: {}", excursionId);
                    return new ResourceNotFoundException("Excursion not found with ID: " + excursionId);
                });
        logger.debug("Excursion retrieved: {}", excursion.getExcursionId());

        AvailabilityDate availabilityDate = availabilityDateRepository.findById(availabilityDateId)
                .orElseThrow(() -> {
                    logger.error("Availability date not found with ID: {}", availabilityDateId);
                    return new ResourceNotFoundException("Availability date not found with ID: " + availabilityDateId);
                });
        logger.debug("AvailabilityDate retrieved: {}", availabilityDate.getAvailabilityId());

        if (!availabilityDate.getExcursion().getExcursionId().equals(excursionId)) {
            logger.error("Mismatch: AvailabilityDate {} does not belong to Excursion {}",
                    availabilityDateId, excursionId);
            throw new BadRequestException("The selected availability date does not belong to the specified excursion.");
        }

        if (availabilityDate.getRemainingSeats() < quantity) {
            logger.error("Not enough available seats. Requested: {}, Remaining: {}",
                    quantity, availabilityDate.getRemainingSeats());
            throw new BadRequestException("Not enough available seats for the selected date.");
        }

        Cart cart = cartRepository.findByUser(user).orElseGet(() -> {
            Cart newCart = new Cart();
            newCart.setUser(user);
            newCart.setDateAddedCart(LocalDateTime.now());
            cartRepository.save(newCart);
            logger.info("New cart created for user: {}", userId);
            return newCart;
        });

        CartItem existingItem = cart.getItems().stream()
                .filter(item -> item.getExcursion().getExcursionId().equals(excursionId)
                        && item.getAvailabilityDate().getAvailabilityId().equals(availabilityDateId))
                .findFirst()
                .orElse(null);

        if (existingItem != null) {
            int newQuantity = existingItem.getQuantity() + quantity;
            existingItem.setQuantity(newQuantity);
            existingItem.setPrice(existingItem.getExcursion().getPrice().multiply(BigDecimal.valueOf(newQuantity)));
            cartItemRepository.save(existingItem);
            logger.info("Updated existing cart item. CartItem ID: {}, New Quantity: {}",
                    existingItem.getCartItemId(), newQuantity);
        } else {
            CartItem newItem = new CartItem();
            newItem.setCart(cart);
            newItem.setExcursion(excursion);
            newItem.setAvailabilityDate(availabilityDate);
            newItem.setQuantity(quantity);
            newItem.setPrice(excursion.getPrice().multiply(BigDecimal.valueOf(quantity)));
            cart.getItems().add(newItem);
            cartItemRepository.save(newItem);
            logger.info("Added new item to cart. CartItem ID: {}, Quantity: {}",
                    newItem.getCartItemId(), quantity);
        }

        availabilityDate.setRemainingSeats(availabilityDate.getRemainingSeats() - quantity);
        availabilityDateRepository.save(availabilityDate);
        logger.info("Updated remaining seats for AvailabilityDate {}. Remaining Seats: {}",
                availabilityDateId, availabilityDate.getRemainingSeats());

        CartDTO cartDTO = mapToCartDTO(cart);
        logger.debug("Cart updated successfully. Cart ID: {}, Total Items: {}",
                cart.getCartId(), cart.getItems().size());

        return cartDTO;
    }

    public UUID getCartIdByUserId(UUID userId) {
        logger.info("Fetching cartId for userId: {}", userId);

        // Trova l'utente dal repository
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        // Trova il carrello dell'utente o lancia un'eccezione se non esiste
        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found for user with ID: " + userId));

        logger.info("CartId found for userId: {}, CartId: {}", userId, cart.getCartId());
        return cart.getCartId();
    }


    @Transactional
    public CartDTO updateCartItemQuantityAndRecalculateTotal(UUID userId, UUID cartItemId, Integer newQuantity) {
        logger.info("Updating cart item quantity. User ID: {}, CartItem ID: {}, New Quantity: {}", userId, cartItemId, newQuantity);

        if (newQuantity <= 0) {
            throw new BadRequestException("Quantity must be greater than zero.");
        }

        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found with ID: " + cartItemId));

        if (!cartItem.getCart().getUser().getUserId().equals(userId)) {
            throw new BadRequestException("Unauthorized action: Cart item does not belong to the user.");
        }

        AvailabilityDate availabilityDate = cartItem.getAvailabilityDate();
        int availableSeats = availabilityDate.getRemainingSeats() + cartItem.getQuantity();
        if (newQuantity > availableSeats) {
            throw new BadRequestException("Not enough available seats for this excursion.");
        }

        cartItem.setQuantity(newQuantity);
        cartItem.setPrice(cartItem.getExcursion().getPrice().multiply(BigDecimal.valueOf(newQuantity)));
        cartItemRepository.save(cartItem);
        availabilityDate.setRemainingSeats(availableSeats - newQuantity);
        availabilityDateRepository.save(availabilityDate);

        Cart cart = cartItem.getCart();
        BigDecimal newTotal = cart.getItems().stream()
                .map(CartItem::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        cart.setTotalAmount(newTotal);
        cartRepository.save(cart);

        logger.info("Cart updated successfully. Cart ID: {}, New Total: {}", cart.getCartId(), newTotal);

        // Ritorna il carrello aggiornato come DTO
        return mapToCartDTO(cart);
    }


    @Transactional
    public void removeFromCart(UUID userId, UUID cartItemId) {
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found with ID: " + cartItemId));

        cartItem.getCart().getItems().remove(cartItem);
        cartItemRepository.delete(cartItem);
    }

    private CartDTO mapToCartDTO(Cart cart) {
        BigDecimal totalAmount = cart.getItems().stream()
                .map(CartItem::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new CartDTO(
                cart.getCartId(),
                cart.getUser().getUserId(),
                cart.getDateAddedCart(),
                cart.getItems().stream()
                        .map(this::mapToCartItemDTO)
                        .collect(Collectors.toList()),
                totalAmount
        );
    }


    private CartItemDTO mapToCartItemDTO(CartItem cartItem) {
        return new CartItemDTO(
                cartItem.getCartItemId(),
                cartItem.getExcursion().getExcursionId(),
                cartItem.getAvailabilityDate().getAvailabilityId(),
                cartItem.getQuantity(),
                cartItem.getPrice(),
                cartItem.getExcursion().getTitle(),
                cartItem.getExcursion().getDescriptionExcursion(),
                cartItem.getExcursion().getDuration(),
                cartItem.getExcursion().getDifficultyLevel()
        );
    }

}
