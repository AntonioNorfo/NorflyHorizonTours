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

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
        System.out.println("User found: " + user.getUserId());

        Excursion excursion = excursionRepository.findById(excursionId)
                .orElseThrow(() -> new ResourceNotFoundException("Excursion not found with ID: " + excursionId));
        System.out.println("Excursion found: " + excursion.getExcursionId());

        if (quantity <= 0) {
            throw new BadRequestException("Quantity must be greater than zero.");
        }

        AvailabilityDate availabilityDate = availabilityDateRepository.findById(availabilityDateId)
                .orElseThrow(() -> new ResourceNotFoundException("Availability date not found with ID: " + availabilityDateId));
        System.out.println("Availability date found: " + availabilityDate.getAvailabilityId());

        if (!availabilityDate.getExcursion().getExcursionId().equals(excursionId)) {
            throw new BadRequestException("The selected availability date does not belong to the specified excursion.");
        }

        if (availabilityDate.getRemainingSeats() < quantity) {
            throw new BadRequestException("Not enough available seats for the selected date.");
        }

        Cart cart = cartRepository.findByUser(user)
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setUser(user);
                    newCart.setDateAddedCart(LocalDateTime.now());
                    cartRepository.save(newCart);
                    System.out.println("New cart created for user: " + userId);
                    return newCart;
                });

        CartItem existingItem = cart.getItems().stream()
                .filter(item -> item.getExcursion().getExcursionId().equals(excursionId)
                        && item.getAvailabilityDate().getAvailabilityId().equals(availabilityDateId))
                .findFirst()
                .orElse(null);

        if (existingItem != null) {
            existingItem.setQuantity(existingItem.getQuantity() + quantity);
            existingItem.setPrice(existingItem.calculatePrice());
            cartItemRepository.save(existingItem);
            System.out.println("Updated item in cart: " + existingItem.getCartItemId());
        } else {
            CartItem newItem = new CartItem();
            newItem.setCart(cart);
            newItem.setExcursion(excursion);
            newItem.setAvailabilityDate(availabilityDate);
            newItem.setQuantity(quantity);
            newItem.setPrice(excursion.getPrice().multiply(BigDecimal.valueOf(quantity)));
            cart.getItems().add(newItem);
            cartItemRepository.save(newItem);
            System.out.println("Added new item to cart: " + newItem.getCartItemId());
        }

        availabilityDate.setRemainingSeats(availabilityDate.getRemainingSeats() - quantity);
        availabilityDateRepository.save(availabilityDate);
        System.out.println("Updated availability date remaining seats: " + availabilityDate.getRemainingSeats());

        return mapToCartDTO(cart);
    }


    @Transactional
    public CartDTO updateCartItemQuantity(UUID userId, UUID cartItemId, Integer newQuantity) {
        if (newQuantity <= 0) {
            throw new BadRequestException("Quantity must be greater than zero.");
        }

        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found with ID: " + cartItemId));

        Excursion excursion = cartItem.getExcursion();
        int availableSpots = excursion.getMaxParticipants() - cartItem.getQuantity();

        if (newQuantity > cartItem.getQuantity() + availableSpots) {
            throw new BadRequestException("Not enough available spots for this excursion.");
        }

        cartItem.setQuantity(newQuantity);
        cartItem.setPrice(cartItem.calculatePrice());
        cartItemRepository.save(cartItem);

        return mapToCartDTO(cartItem.getCart());
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
                cartItem.getPrice()
        );
    }

}
