package antonionorfo.norflyHorizonTours.services;

import antonionorfo.norflyHorizonTours.entities.AvailabilityDate;
import antonionorfo.norflyHorizonTours.entities.Excursion;
import antonionorfo.norflyHorizonTours.exception.BadRequestException;
import antonionorfo.norflyHorizonTours.exception.ResourceNotFoundException;
import antonionorfo.norflyHorizonTours.repositories.AvailabilityDateRepository;
import antonionorfo.norflyHorizonTours.repositories.ExcursionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AvailabilityService {

    @Autowired
    private AvailabilityDateRepository availabilityDateRepository;

    @Autowired
    private ExcursionRepository excursionRepository;

    public void generateDefaultAvailability(Excursion excursion) {
        LocalDate today = LocalDate.now();
        LocalDate sixMonthsFromNow = today.plusMonths(6);

        List<AvailabilityDate> availabilityDates = today.datesUntil(sixMonthsFromNow)
                .map(date -> {
                    AvailabilityDate availabilityDate = new AvailabilityDate();
                    availabilityDate.setDateAvailable(date);
                    availabilityDate.setRemainingSeats(excursion.getMaxParticipants());
                    availabilityDate.setIsBooked(false);
                    availabilityDate.setExcursion(excursion);
                    return availabilityDate;
                })
                .collect(Collectors.toList());

        availabilityDateRepository.saveAll(availabilityDates);
    }

    public int getExcursionAvailability(UUID excursionId, LocalDate date) {
        Excursion excursion = excursionRepository.findById(excursionId)
                .orElseThrow(() -> new ResourceNotFoundException("Excursion not found"));

        return availabilityDateRepository.findByExcursionAndDateAvailable(excursion, date)
                .map(AvailabilityDate::getRemainingSeats)
                .orElse(0);
    }

    public List<LocalDate> getAvailableDatesForExcursion(UUID excursionId) {
        Excursion excursion = excursionRepository.findById(excursionId)
                .orElseThrow(() -> new ResourceNotFoundException("Excursion not found"));

        return availabilityDateRepository.findByExcursionAndIsBookedFalse(excursion).stream()
                .map(AvailabilityDate::getDateAvailable)
                .collect(Collectors.toList());
    }

    public void bookSeats(UUID excursionId, LocalDate date, int numSeats) {
        Excursion excursion = excursionRepository.findById(excursionId)
                .orElseThrow(() -> new ResourceNotFoundException("Excursion not found"));

        AvailabilityDate availabilityDate = availabilityDateRepository.findByExcursionAndDateAvailable(excursion, date)
                .orElseThrow(() -> new ResourceNotFoundException("Availability date not found"));

        int remainingSeats = availabilityDate.getRemainingSeats();

        if (remainingSeats >= numSeats) {

            availabilityDate.setRemainingSeats(remainingSeats - numSeats);

            if (availabilityDate.getRemainingSeats() == 0) {
                availabilityDate.setIsBooked(true);
            }

            availabilityDateRepository.save(availabilityDate);
        } else {
            throw new BadRequestException("Not enough seats available.");
        }
    }

    public void cancelBooking(UUID excursionId, LocalDate date, int numSeats) {
        Excursion excursion = excursionRepository.findById(excursionId)
                .orElseThrow(() -> new ResourceNotFoundException("Excursion not found"));

        AvailabilityDate availabilityDate = availabilityDateRepository.findByExcursionAndDateAvailable(excursion, date)
                .orElseThrow(() -> new ResourceNotFoundException("Availability date not found"));

        int remainingSeats = availabilityDate.getRemainingSeats();

        availabilityDate.setRemainingSeats(remainingSeats + numSeats);

        if (availabilityDate.getRemainingSeats() > 0) {
            availabilityDate.setIsBooked(false);
        }

        availabilityDateRepository.save(availabilityDate);
    }
}
