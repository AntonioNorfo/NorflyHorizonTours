package antonionorfo.norflyHorizonTours.controllers;

import antonionorfo.norflyHorizonTours.payloads.PaymentDTO;
import antonionorfo.norflyHorizonTours.payloads.PaymentRequestDTO;
import antonionorfo.norflyHorizonTours.services.PaymentService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/users/{userId}/payments")
@RequiredArgsConstructor
public class PaymentController {

    private static final Logger log = LoggerFactory.getLogger(PaymentController.class);
    private final PaymentService paymentService;

    @PostMapping
    public ResponseEntity<PaymentDTO> createPayment(
            @PathVariable UUID userId,
            @RequestBody PaymentRequestDTO paymentRequestDTO,
            @RequestParam List<UUID> selectedItemIds
    ) {
        log.info("Ricevuto userId: {}", userId);
        log.info("selectedItemIds ricevuti: {}", selectedItemIds);

        if (selectedItemIds == null || selectedItemIds.isEmpty()) {
            throw new IllegalArgumentException("selectedItemIds non possono essere null o vuoti.");
        }

        PaymentDTO createdPayment = paymentService.createPayment(userId, paymentRequestDTO, selectedItemIds);
        return ResponseEntity.ok(createdPayment);
    }


    @GetMapping
    public ResponseEntity<List<PaymentDTO>> getAllPaymentsForUser(@PathVariable UUID userId) {
        List<PaymentDTO> payments = paymentService.getPaymentsByUser(userId);
        return ResponseEntity.ok(payments);
    }

    @GetMapping("/{paymentId}")
    public ResponseEntity<PaymentDTO> getPaymentById(
            @PathVariable UUID userId,
            @PathVariable UUID paymentId
    ) {
        PaymentDTO payment = paymentService.getPaymentById(paymentId);
        return ResponseEntity.ok(payment);
    }

    @GetMapping("/booking/{bookingId}")
    public ResponseEntity<List<PaymentDTO>> getPaymentsByBooking(
            @PathVariable UUID userId,
            @PathVariable UUID bookingId
    ) {
        List<PaymentDTO> payments = paymentService.getPaymentsByBooking(bookingId);
        return ResponseEntity.ok(payments);
    }

    @PostMapping("/{paymentId}/refund")
    public ResponseEntity<PaymentDTO> refundPayment(
            @PathVariable UUID userId,
            @PathVariable UUID paymentId
    ) {
        PaymentDTO refundedPayment = paymentService.refundPayment(paymentId);
        return ResponseEntity.ok(refundedPayment);
    }
}
