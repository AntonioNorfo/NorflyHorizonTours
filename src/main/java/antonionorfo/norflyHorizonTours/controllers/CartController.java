package antonionorfo.norflyHorizonTours.controllers;

import antonionorfo.norflyHorizonTours.payloads.CartDTO;
import antonionorfo.norflyHorizonTours.services.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/users/{userId}/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping
    public ResponseEntity<List<CartDTO>> getCart(@PathVariable UUID userId) {
        return ResponseEntity.ok(cartService.getUserCart(userId));
    }

    @PostMapping
    public ResponseEntity<CartDTO> addToCart(
            @PathVariable UUID userId,
            @RequestBody @Valid CartDTO cartDTO
    ) {
        return ResponseEntity.ok(cartService.addToCart(userId, cartDTO.excursionId(), cartDTO.quantity()));
    }


    @DeleteMapping("/{excursionId}")
    public ResponseEntity<Void> removeFromCart(
            @PathVariable UUID userId,
            @PathVariable UUID excursionId
    ) {
        cartService.removeFromCart(userId, excursionId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/checkout")
    public ResponseEntity<Void> checkoutCart(@PathVariable UUID userId) {
        cartService.checkoutCart(userId);
        return ResponseEntity.noContent().build();
    }
}

