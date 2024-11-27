package antonionorfo.norflyHorizonTours.services;

import antonionorfo.norflyHorizonTours.entities.Booking;
import antonionorfo.norflyHorizonTours.entities.Payment;
import antonionorfo.norflyHorizonTours.enums.PaymentStatus;
import antonionorfo.norflyHorizonTours.exception.ResourceNotFoundException;
import antonionorfo.norflyHorizonTours.payloads.PaymentDTO;
import antonionorfo.norflyHorizonTours.repositories.BookingRepository;
import antonionorfo.norflyHorizonTours.repositories.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;

    public PaymentDTO createPayment(PaymentDTO paymentDTO) {
        Booking booking = bookingRepository.findById(paymentDTO.bookingId())
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        Payment payment = new Payment();
        payment.setAmountPayment(paymentDTO.amountPayment());
        payment.setPaymentDate(LocalDateTime.now());
        payment.setMethodPayment(paymentDTO.methodPayment());
        payment.setStatusPayment(PaymentStatus.PENDING); // Default status
        payment.setTransactionReference(paymentDTO.transactionReference());
        payment.setBooking(booking);

        Payment savedPayment = paymentRepository.save(payment);

        return mapToDTO(savedPayment);
    }

    public PaymentDTO getPaymentById(UUID paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));

        return mapToDTO(payment);
    }

    public List<PaymentDTO> getPaymentsByUser(UUID userId) {
        List<Payment> payments = paymentRepository.findAll().stream()
                .filter(payment -> payment.getBooking().getUser().getUserId().equals(userId))
                .collect(Collectors.toList());

        return payments.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    public List<PaymentDTO> getPaymentsByBooking(UUID bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        List<Payment> payments = paymentRepository.findAll().stream()
                .filter(payment -> payment.getBooking().equals(booking))
                .collect(Collectors.toList());

        return payments.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    public PaymentDTO refundPayment(UUID paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));

        if (payment.getStatusPayment() != PaymentStatus.SUCCESS) {
            throw new IllegalArgumentException("Only successful payments can be refunded");
        }

        payment.setStatusPayment(PaymentStatus.FAILED); // Simulate refund (could be a separate status)
        Payment updatedPayment = paymentRepository.save(payment);

        return mapToDTO(updatedPayment);
    }

    private PaymentDTO mapToDTO(Payment payment) {
        return new PaymentDTO(
                payment.getPaymentId(),
                payment.getAmountPayment(),
                payment.getPaymentDate(),
                payment.getMethodPayment(),
                payment.getStatusPayment(),
                payment.getTransactionReference(),
                payment.getBooking().getBookingId()
        );
    }
}
