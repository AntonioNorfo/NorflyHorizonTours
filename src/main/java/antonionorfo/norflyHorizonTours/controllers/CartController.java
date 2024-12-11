package antonionorfo.norflyHorizonTours.controllers;

import antonionorfo.norflyHorizonTours.payloads.CartDTO;
import antonionorfo.norflyHorizonTours.services.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/users/{userId}/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping
    public ResponseEntity<CartDTO> getUserCart(@PathVariable UUID userId) {
        CartDTO cart = cartService.getUserCart(userId);
        return ResponseEntity.ok(cart);
    }

    @PostMapping("/items")
    public ResponseEntity<CartDTO> addItemToCart(
            @PathVariable UUID userId,
            @RequestParam UUID excursionId,
            @RequestParam UUID availabilityDateId,
            @RequestParam Integer quantity
    ) {
        CartDTO updatedCart = cartService.addToCart(userId, excursionId, availabilityDateId, quantity);
        System.out.println("Cart updated: " + updatedCart);
        return ResponseEntity.ok(updatedCart);
    }


    @PatchMapping("/items/{cartItemId}")
    public ResponseEntity<CartDTO> updateItemQuantity(
            @PathVariable UUID userId,
            @PathVariable UUID cartItemId,
            @RequestParam Integer newQuantity
    ) {
        CartDTO updatedCart = cartService.updateCartItemQuantity(userId, cartItemId, newQuantity);
        return ResponseEntity.ok(updatedCart);
    }

    @DeleteMapping("/items/{cartItemId}")
    public ResponseEntity<Void> removeItemFromCart(
            @PathVariable UUID userId,
            @PathVariable UUID cartItemId
    ) {
        cartService.removeFromCart(userId, cartItemId);
        return ResponseEntity.noContent().build();
    }
}
