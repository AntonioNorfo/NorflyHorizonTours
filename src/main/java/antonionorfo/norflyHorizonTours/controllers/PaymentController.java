package antonionorfo.norflyHorizonTours.controllers;

import antonionorfo.norflyHorizonTours.payloads.PaymentDTO;
import antonionorfo.norflyHorizonTours.services.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    public ResponseEntity<PaymentDTO> createPayment(@RequestBody PaymentDTO paymentDTO) {
        PaymentDTO createdPayment = paymentService.createPayment(paymentDTO);
        return ResponseEntity.ok(createdPayment);
    }

    @GetMapping("/{paymentId}")
    public ResponseEntity<PaymentDTO> getPaymentById(@PathVariable UUID paymentId) {
        PaymentDTO payment = paymentService.getPaymentById(paymentId);
        return ResponseEntity.ok(payment);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<PaymentDTO>> getPaymentsByUser(@PathVariable UUID userId) {
        List<PaymentDTO> payments = paymentService.getPaymentsByUser(userId);
        return ResponseEntity.ok(payments);
    }

    @GetMapping("/booking/{bookingId}")
    public ResponseEntity<List<PaymentDTO>> getPaymentsByBooking(@PathVariable UUID bookingId) {
        List<PaymentDTO> payments = paymentService.getPaymentsByBooking(bookingId);
        return ResponseEntity.ok(payments);
    }

    @PostMapping("/{paymentId}/refund")
    public ResponseEntity<PaymentDTO> refundPayment(@PathVariable UUID paymentId) {
        PaymentDTO refundedPayment = paymentService.refundPayment(paymentId);
        return ResponseEntity.ok(refundedPayment);
    }
}
