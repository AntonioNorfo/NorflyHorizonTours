package antonionorfo.norflyHorizonTours.controllers;

import antonionorfo.norflyHorizonTours.payloads.PaymentDTO;
import antonionorfo.norflyHorizonTours.payloads.PaymentRequestDTO;
import antonionorfo.norflyHorizonTours.services.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/users/{userId}/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    public ResponseEntity<PaymentDTO> createPayment(
            @PathVariable UUID userId,
            @RequestBody PaymentRequestDTO paymentRequestDTO
    ) {
        PaymentDTO createdPayment = paymentService.createPayment(userId, paymentRequestDTO);
        return ResponseEntity.ok(createdPayment);
    }

    @PostMapping("/{paymentId}/finalize")
    public ResponseEntity<Void> finalizePayment(
            @PathVariable UUID userId,
            @PathVariable UUID paymentId
    ) {
        paymentService.finalizePayment(paymentId);
        return ResponseEntity.ok().build();
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
