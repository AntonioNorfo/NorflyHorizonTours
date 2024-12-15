package antonionorfo.norflyHorizonTours.controllers;

import antonionorfo.norflyHorizonTours.exception.BadRequestException;
import antonionorfo.norflyHorizonTours.exception.ResourceNotFoundException;
import antonionorfo.norflyHorizonTours.payloads.CartDTO;
import antonionorfo.norflyHorizonTours.payloads.ErrorDTO;
import antonionorfo.norflyHorizonTours.services.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

import static antonionorfo.norflyHorizonTours.tools.MailgunSender.logger;

@RestController
@RequestMapping("/users/{userId}/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping
    public ResponseEntity<CartDTO> getUserCart(@PathVariable UUID userId) {
        logger.info("Fetching cart for userId: {}", userId);

        try {
            CartDTO cart = cartService.getUserCart(userId);
            logger.info("Cart fetched successfully for userId: {}", userId);
            return ResponseEntity.ok(cart);
        } catch (Exception e) {
            logger.error("Error while fetching cart for userId {}: {}", userId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/cartId")
    public ResponseEntity<UUID> getCartId(@PathVariable UUID userId) {
        logger.info("Fetching cartId for userId: {}", userId);

        try {
            UUID cartId = cartService.getCartIdByUserId(userId);
            logger.info("CartId fetched successfully for userId: {}", userId);
            return ResponseEntity.ok(cartId);
        } catch (ResourceNotFoundException e) {
            logger.error("Cart or User not found for userId {}: {}", userId, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            logger.error("Error while fetching cartId for userId {}: {}", userId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }


    @PostMapping("/items")
    public ResponseEntity<?> addItemToCart(
            @PathVariable UUID userId,
            @RequestParam(required = false) UUID excursionId,
            @RequestParam(required = false) UUID availabilityDateId,
            @RequestParam(required = false) Integer quantity
    ) {
        logger.info("Attempting to add item to cart for userId: {}, ExcursionId: {}, AvailabilityDateId: {}, Quantity: {}",
                userId, excursionId, availabilityDateId, quantity);
        logger.info("Raw request parameters - userId: {}, excursionId: {}, availabilityDateId: {}, quantity: {}",
                userId, excursionId, availabilityDateId, quantity);

        if (excursionId == null || availabilityDateId == null) {
            logger.error("Missing required parameters: excursionId or availabilityDateId for userId: {}", userId);
            throw new BadRequestException("Missing required parameters: excursionId or availabilityDateId.");
        }

        try {
            logger.info("Received parameters: userId={}, excursionId={}, availabilityDateId={}, quantity={}",
                    userId, excursionId, availabilityDateId, quantity);

            CartDTO updatedCart = cartService.addToCart(userId, excursionId, availabilityDateId, quantity);
            logger.info("Item added to cart successfully for userId: {}, Cart ID: {}", userId, updatedCart.cartId());
            return ResponseEntity.ok(updatedCart);
        } catch (BadRequestException e) {
            logger.error("Bad request while adding item to cart for userId {}: {}", userId, e.getMessage());
            return ResponseEntity.badRequest().body(new ErrorDTO("Bad request: " + e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error while adding item to cart for userId {}: {}", userId, e.getMessage(), e);
            return ResponseEntity.internalServerError().body(new ErrorDTO("Internal server error: " + e.getMessage()));
        }
    }

    @PatchMapping("/items/{cartItemId}")
    public ResponseEntity<CartDTO> updateItemQuantity(
            @PathVariable UUID userId,
            @PathVariable UUID cartItemId,
            @RequestParam Integer newQuantity
    ) {
        logger.info("Attempting to update cart item quantity for userId: {}, CartItemId: {}, New Quantity: {}",
                userId, cartItemId, newQuantity);

        if (newQuantity == null || newQuantity <= 0) {
            logger.error("Invalid newQuantity parameter for userId: {}, CartItemId: {}", userId, cartItemId);
            throw new BadRequestException("New quantity must be greater than zero.");
        }

        try {
            CartDTO updatedCart = cartService.updateCartItemQuantityAndRecalculateTotal(userId, cartItemId, newQuantity);

            logger.info("Cart item updated successfully for userId: {}, CartItemId: {}, Updated Total: {}",
                    userId, cartItemId, updatedCart.totalAmount());

            return ResponseEntity.ok(updatedCart);

        } catch (ResourceNotFoundException e) {
            logger.error("Resource not found while updating cart item for userId: {}, CartItemId: {}", userId, cartItemId, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (Exception e) {
            logger.error("Error while updating cart item for userId {}: {}", userId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }


    @DeleteMapping("/items/{cartItemId}")
    public ResponseEntity<Void> removeItemFromCart(
            @PathVariable UUID userId,
            @PathVariable UUID cartItemId
    ) {
        logger.info("Attempting to remove item from cart for userId: {}, CartItemId: {}", userId, cartItemId);

        try {
            cartService.removeFromCart(userId, cartItemId);
            logger.info("Cart item removed successfully for userId: {}, CartItemId: {}", userId, cartItemId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            logger.error("Error while removing cart item for userId {}: {}", userId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

}
