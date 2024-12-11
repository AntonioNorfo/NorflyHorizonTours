package antonionorfo.norflyHorizonTours.services;

import antonionorfo.norflyHorizonTours.entities.AvailabilityDate;
import antonionorfo.norflyHorizonTours.entities.Excursion;
import antonionorfo.norflyHorizonTours.exception.BadRequestException;
import antonionorfo.norflyHorizonTours.exception.ResourceNotFoundException;
import antonionorfo.norflyHorizonTours.repositories.AvailabilityDateRepository;
import antonionorfo.norflyHorizonTours.repositories.ExcursionRepository;
import com.github.javafaker.Faker;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static antonionorfo.norflyHorizonTours.tools.MailgunSender.logger;

@Service
@RequiredArgsConstructor
public class AvailabilityService {

    private final AvailabilityDateRepository availabilityDateRepository;
    private final ExcursionRepository excursionRepository;
    private final Faker faker = new Faker();

    public void generateDefaultAvailabilityForExcursion(Excursion excursion) {
        if (availabilityDateRepository.findByExcursion(excursion).isEmpty()) {
            LocalDateTime startRange = LocalDateTime.now();
            LocalDateTime endRange = startRange.plusYears(2);

            int occurrencesPerWeek = 3;
            int totalWeeks = (int) (endRange.toLocalDate().toEpochDay() - startRange.toLocalDate().toEpochDay()) / 7;
            int totalOccurrences = totalWeeks * occurrencesPerWeek;

            if ("1 day".equalsIgnoreCase(excursion.getDuration())) {
                generateDailyAvailability(excursion, startRange, endRange, totalOccurrences);
            } else {
                generateHourlyAvailability(excursion, startRange, endRange, totalOccurrences);
            }
        } else {
            logger.info("Disponibilità già presente per l'escursione con ID: {}", excursion.getExcursionId());
        }
    }


    private void generateDailyAvailability(Excursion excursion, LocalDateTime startRange, LocalDateTime endRange, int totalOccurrences) {
        validateDateRange(startRange, endRange);

        List<AvailabilityDate> availabilityDates = new ArrayList<>();
        LocalDate currentDate = startRange.toLocalDate();

        for (int i = 0; i < totalOccurrences; i++) {
            LocalDate randomDay = currentDate.plusDays(faker.number().numberBetween(0, 7));
            if (randomDay.isAfter(endRange.toLocalDate())) break;

            AvailabilityDate availabilityDate = new AvailabilityDate();
            availabilityDate.setExcursion(excursion);
            availabilityDate.setDateAvailable(randomDay.atStartOfDay());
            availabilityDate.setRemainingSeats(excursion.getMaxParticipants());
            availabilityDate.setIsBooked(false);

            availabilityDates.add(availabilityDate);

            if ((i + 1) % 3 == 0) {
                currentDate = currentDate.plusWeeks(1);
            }
        }

        availabilityDateRepository.saveAll(availabilityDates);
    }

    private void generateHourlyAvailability(Excursion excursion, LocalDateTime startRange, LocalDateTime endRange, int totalOccurrences) {
        validateDateRange(startRange, endRange);

        List<AvailabilityDate> availabilityDates = new ArrayList<>();
        LocalDate currentDate = startRange.toLocalDate();

        for (int i = 0; i < totalOccurrences; i++) {
            LocalDate randomDay = currentDate.plusDays(faker.number().numberBetween(0, 7));
            if (randomDay.isAfter(endRange.toLocalDate())) break;

            int slotsPerDay = faker.number().numberBetween(2, 4);
            for (int j = 0; j < slotsPerDay; j++) {
                LocalDateTime randomTime = randomDay.atTime(faker.number().numberBetween(8, 20), 0);

                AvailabilityDate availabilityDate = new AvailabilityDate();
                availabilityDate.setExcursion(excursion);
                availabilityDate.setDateAvailable(randomTime);
                availabilityDate.setRemainingSeats(excursion.getMaxParticipants());
                availabilityDate.setIsBooked(false);

                availabilityDates.add(availabilityDate);
            }

            if ((i + 1) % 3 == 0) {
                currentDate = currentDate.plusWeeks(1);
            }
        }

        availabilityDateRepository.saveAll(availabilityDates);
    }


    public void generateAvailabilityForExcursion(UUID excursionId) {
        Excursion excursion = excursionRepository.findById(excursionId)
                .orElseThrow(() -> new ResourceNotFoundException("Excursion not found with ID: " + excursionId));

        generateDefaultAvailabilityForExcursion(excursion);
    }


    public List<LocalDateTime> getAvailableDates(UUID excursionId) {
        Excursion excursion = excursionRepository.findById(excursionId)
                .orElseThrow(() -> new ResourceNotFoundException("Excursion not found"));

        List<AvailabilityDate> availabilityDates = availabilityDateRepository.findByExcursion(excursion);

        return availabilityDates.stream()
                .filter(availabilityDate -> !availabilityDate.getIsBooked() && availabilityDate.getRemainingSeats() > 0)
                .map(AvailabilityDate::getDateAvailable)
                .collect(Collectors.toList());
    }


    public int countAvailableSeats(UUID excursionId, LocalDateTime dateAvailable) {
        Excursion excursion = excursionRepository.findById(excursionId)
                .orElseThrow(() -> new ResourceNotFoundException("Excursion not found"));

        AvailabilityDate availabilityDate = availabilityDateRepository.findByExcursionAndDateAvailable(excursion, dateAvailable)
                .orElseThrow(() -> new ResourceNotFoundException("Availability date not found"));

        return availabilityDate.getRemainingSeats();
    }

    public void updateAvailability(UUID excursionId, LocalDateTime dateTime, int seatsBooked) {
        Excursion excursion = excursionRepository.findById(excursionId)
                .orElseThrow(() -> new ResourceNotFoundException("Excursion not found"));

        AvailabilityDate availabilityDate = availabilityDateRepository.findByExcursionAndDateAvailable(excursion, dateTime)
                .orElseThrow(() -> new ResourceNotFoundException("Availability date not found"));

        if (availabilityDate.getRemainingSeats() < seatsBooked) {
            throw new BadRequestException("Not enough seats available");
        }

        availabilityDate.setRemainingSeats(availabilityDate.getRemainingSeats() - seatsBooked);

        if (availabilityDate.getRemainingSeats() == 0) {
            availabilityDate.setIsBooked(true);
        }

        availabilityDateRepository.save(availabilityDate);
    }


    /**
     * Valida l'intervallo di date.
     */
    private void validateDateRange(LocalDateTime startRange, LocalDateTime endRange) {
        if (startRange.isAfter(endRange)) {
            throw new BadRequestException("Invalid date range.");
        }
    }
}
