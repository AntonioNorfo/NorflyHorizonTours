package antonionorfo.norflyHorizonTours.services;

import antonionorfo.norflyHorizonTours.entities.*;
import antonionorfo.norflyHorizonTours.exception.ResourceNotFoundException;
import antonionorfo.norflyHorizonTours.payloads.AdminPostDTO;
import antonionorfo.norflyHorizonTours.payloads.BookingDTO;
import antonionorfo.norflyHorizonTours.payloads.ExcursionDTO;
import antonionorfo.norflyHorizonTours.repositories.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final ExcursionRepository excursionRepository;
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final AdminPostRepository adminPostRepository;
    private final ReviewRepository reviewRepository;
    private final CityRepository cityRepository;

    public ExcursionDTO createExcursion(ExcursionDTO excursionDTO) {
        City city = cityRepository.findById(excursionDTO.cityId())
                .orElseThrow(() -> new ResourceNotFoundException("Città non trovata con ID: " + excursionDTO.cityId()));

        Excursion excursion = new Excursion();
        excursion.setTitle(excursionDTO.title());
        excursion.setDescriptionExcursion(excursionDTO.descriptionExcursion());
        excursion.setPrice(excursionDTO.price());
        excursion.setDuration(excursionDTO.duration());
        excursion.setDifficultyLevel(excursionDTO.difficultyLevel());
        excursion.setInclusions(excursionDTO.inclusions());
        excursion.setRules(excursionDTO.rules());
        excursion.setNotRecommended(excursionDTO.notRecommended());
        excursion.setMaxParticipants(excursionDTO.maxParticipants());
        excursion.setCity(city);

        excursionRepository.save(excursion);

        return mapToExcursionDTO(excursion);
    }

    public ExcursionDTO updateExcursion(UUID excursionId, ExcursionDTO excursionDTO) {
        Excursion excursion = excursionRepository.findById(excursionId)
                .orElseThrow(() -> new ResourceNotFoundException("Escursione non trovata!"));
        excursion.setTitle(excursionDTO.title());
        excursion.setDescriptionExcursion(excursionDTO.descriptionExcursion());
        excursion.setPrice(excursionDTO.price());
        excursionRepository.save(excursion);
        return mapToExcursionDTO(excursion);
    }

    public void deleteExcursion(UUID excursionId) {
        if (!excursionRepository.existsById(excursionId)) {
            throw new ResourceNotFoundException("Escursione non trovata!");
        }
        excursionRepository.deleteById(excursionId);
    }

    public List<BookingDTO> getAllBookings() {
        return bookingRepository.findAll().stream().map(this::mapToBookingDTO).toList();
    }

    public void blockUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Utente non trovato!"));
        user.setBlocked(true);
        userRepository.save(user);
    }

    public AdminPostDTO createPost(AdminPostDTO adminPostDTO) {
        AdminPost post = new AdminPost();
        post.setTitlePost(adminPostDTO.titlePost());
        post.setContentPost(adminPostDTO.contentPost());
        adminPostRepository.save(post);
        return mapToAdminPostDTO(post);
    }

    public AdminPostDTO updatePost(UUID postId, AdminPostDTO adminPostDTO) {
        AdminPost post = adminPostRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post non trovato!"));
        post.setTitlePost(adminPostDTO.titlePost());
        post.setContentPost(adminPostDTO.contentPost());
        adminPostRepository.save(post);
        return mapToAdminPostDTO(post);
    }

    public void deletePost(UUID postId) {
        if (!adminPostRepository.existsById(postId)) {
            throw new ResourceNotFoundException("Post non trovato!");
        }
        adminPostRepository.deleteById(postId);
    }

    public void deleteReview(UUID reviewId) {
        if (!reviewRepository.existsById(reviewId)) {
            throw new ResourceNotFoundException("Recensione non trovata!");
        }
        reviewRepository.deleteById(reviewId);
    }

    public void blockUserReviews(UUID userId) {
        List<Review> reviews = reviewRepository.findByUser_UserId(userId);
        reviews.forEach(review -> review.setBlocked(true));
        reviewRepository.saveAll(reviews);
    }

    private ExcursionDTO mapToExcursionDTO(Excursion excursion) {
        return new ExcursionDTO(
                excursion.getExcursionId(),
                excursion.getTitle(),
                excursion.getDescriptionExcursion(),
                excursion.getPrice(),
                excursion.getDuration(),
                excursion.getDifficultyLevel(),
                excursion.getInclusions(),
                excursion.getRules(),
                excursion.getNotRecommended() != null ? excursion.getNotRecommended() : "",
                excursion.getMaxParticipants(),
                excursion.getCity() != null ? excursion.getCity().getId() : null,
                excursion.getCountry() != null ? excursion.getCountry().getId() : null,
                excursion.getMarkers()
        );
    }

    private BookingDTO mapToBookingDTO(Booking booking) {
        return new BookingDTO(
                booking.getBookingId(),
                booking.getUser().getUserId(),
                booking.getExcursion().getExcursionId(),
                booking.getBookingDate(),
                booking.getStartDate(),
                booking.getEndDate(),
                booking.getNumSeats(),
                booking.getStatusOfBooking()
        );
    }

    private AdminPostDTO mapToAdminPostDTO(AdminPost post) {
        return new AdminPostDTO(
                post.getAdminPostId(),
                post.getTitlePost(),
                post.getContentPost(),
                post.getPhotoPostAdmin(),
                post.getVideoPostAdmin(),
                post.getPublicationDate()
        );
    }
}
