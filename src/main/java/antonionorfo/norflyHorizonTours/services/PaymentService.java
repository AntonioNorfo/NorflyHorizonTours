package antonionorfo.norflyHorizonTours.services;

import antonionorfo.norflyHorizonTours.entities.*;
import antonionorfo.norflyHorizonTours.enums.PaymentStatus;
import antonionorfo.norflyHorizonTours.exception.BadRequestException;
import antonionorfo.norflyHorizonTours.exception.ResourceNotFoundException;
import antonionorfo.norflyHorizonTours.payloads.PaymentDTO;
import antonionorfo.norflyHorizonTours.payloads.PaymentRequestDTO;
import antonionorfo.norflyHorizonTours.repositories.BookingRepository;
import antonionorfo.norflyHorizonTours.repositories.CartRepository;
import antonionorfo.norflyHorizonTours.repositories.PaymentRepository;
import antonionorfo.norflyHorizonTours.repositories.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);
    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;
    private final CartRepository cartRepository;
    private final UserRepository userRepository;

    @Transactional
    public PaymentDTO createPayment(UUID userId, PaymentRequestDTO paymentRequest, List<UUID> selectedItemIds) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Cart cart = cartRepository.findById(paymentRequest.cartId())
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));

        if (!cart.getUser().getUserId().equals(userId)) {
            throw new BadRequestException("The cart does not belong to the user.");
        }

        List<CartItem> selectedItems = cart.getItems().stream()
                .filter(item -> selectedItemIds.contains(item.getCartItemId()))
                .collect(Collectors.toList());

        if (selectedItems.isEmpty()) {
            throw new BadRequestException("No items selected for payment.");
        }

        BigDecimal totalAmount = selectedItems.stream()
                .map(CartItem::calculatePrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);

        Payment payment = new Payment();
        payment.setAmountPayment(totalAmount);
        payment.setPaymentDate(LocalDateTime.now());
        payment.setMethodPayment(paymentRequest.methodPayment());
        payment.setStatusPayment(PaymentStatus.SUCCESS);
        payment.setTransactionReference(paymentRequest.transactionReference());
        payment.setCart(cart);
        payment.setUser(user);

        paymentRepository.save(payment);

        for (CartItem item : selectedItems) {
            Booking booking = new Booking();
            booking.setBookingDate(LocalDateTime.now());
            booking.setQuantity(item.getQuantity());
            booking.setTotalPrice(item.calculatePrice());
            booking.setUser(user);
            booking.setExcursion(item.getExcursion());
            booking.setAvailabilityDate(item.getAvailabilityDate());
            bookingRepository.save(booking);
        }

        cart.getItems().removeAll(selectedItems);
        cartRepository.save(cart);

        return mapToDTO(payment);
    }


    public PaymentDTO getPaymentById(UUID paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));
        return mapToDTO(payment);
    }

    public List<PaymentDTO> getPaymentsByUser(UUID userId) {
        return paymentRepository.findAll().stream()
                .filter(payment -> payment.getUser().getUserId().equals(userId))
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<PaymentDTO> getPaymentsByBooking(UUID bookingId) {
        return paymentRepository.findAll().stream()
                .filter(payment -> payment.getCart() != null && payment.getCart().getItems().stream()
                        .anyMatch(item -> item.getExcursion().getExcursionId().equals(bookingId)))
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public PaymentDTO refundPayment(UUID paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));

        if (payment.getStatusPayment() != PaymentStatus.SUCCESS) {
            throw new BadRequestException("Only successful payments can be refunded");
        }

        payment.setStatusPayment(PaymentStatus.FAILED);
        Payment refundedPayment = paymentRepository.save(payment);

        return mapToDTO(refundedPayment);
    }

    private PaymentDTO mapToDTO(Payment payment) {
        UUID cartId = (payment.getCart() != null) ? payment.getCart().getCartId() : null;

        return new PaymentDTO(
                payment.getPaymentId(),
                payment.getAmountPayment(),
                payment.getPaymentDate(),
                payment.getMethodPayment(),
                payment.getStatusPayment(),
                payment.getTransactionReference(),
                cartId,
                payment.getUser().getUserId()
        );
    }

}
