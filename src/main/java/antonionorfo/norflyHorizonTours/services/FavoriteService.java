package antonionorfo.norflyHorizonTours.services;

import antonionorfo.norflyHorizonTours.entities.Excursion;
import antonionorfo.norflyHorizonTours.entities.Favorite;
import antonionorfo.norflyHorizonTours.entities.User;
import antonionorfo.norflyHorizonTours.exception.ResourceNotFoundException;
import antonionorfo.norflyHorizonTours.payloads.FavoriteDTO;
import antonionorfo.norflyHorizonTours.repositories.ExcursionRepository;
import antonionorfo.norflyHorizonTours.repositories.FavoriteRepository;
import antonionorfo.norflyHorizonTours.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final UserRepository userRepository;
    private final ExcursionRepository excursionRepository;

    public FavoriteDTO addFavorite(UUID userId, UUID excursionId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Excursion excursion = excursionRepository.findById(excursionId)
                .orElseThrow(() -> new ResourceNotFoundException("Excursion not found"));

        if (favoriteRepository.findByUser_UserIdAndExcursion_ExcursionId(userId, excursionId).isPresent()) {
            throw new IllegalStateException("Excursion is already in favorites");
        }

        Favorite favorite = new Favorite();
        favorite.setUser(user);
        favorite.setExcursion(excursion);
        favorite.setAddedFavoriteDate(LocalDate.now());

        Favorite savedFavorite = favoriteRepository.save(favorite);

        return mapToDTO(savedFavorite);
    }

    public void removeFavorite(UUID userId, UUID excursionId) {
        Favorite favorite = favoriteRepository.findByUser_UserIdAndExcursion_ExcursionId(userId, excursionId)
                .orElseThrow(() -> new ResourceNotFoundException("Favorite not found"));
        favoriteRepository.delete(favorite);
    }

    public List<FavoriteDTO> getUserFavorites(UUID userId) {
        return favoriteRepository.findByUser_UserId(userId)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    private FavoriteDTO mapToDTO(Favorite favorite) {
        return new FavoriteDTO(
                favorite.getFavoriteId(),
                favorite.getUser().getUserId(),
                favorite.getCity() != null ? favorite.getCity().getId() : null,
                favorite.getExcursion().getExcursionId(),
                favorite.getAddedFavoriteDate(),
                favorite.getExcursion().getTitle(),
                favorite.getExcursion().getDescriptionExcursion()
        );
    }
}
