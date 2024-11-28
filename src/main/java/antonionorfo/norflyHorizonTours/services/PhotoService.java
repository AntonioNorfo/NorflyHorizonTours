package antonionorfo.norflyHorizonTours.services;

import antonionorfo.norflyHorizonTours.entities.Excursion;
import antonionorfo.norflyHorizonTours.entities.Photo;
import antonionorfo.norflyHorizonTours.exception.BadRequestException;
import antonionorfo.norflyHorizonTours.exception.ResourceNotFoundException;
import antonionorfo.norflyHorizonTours.payloads.PhotoDTO;
import antonionorfo.norflyHorizonTours.repositories.ExcursionRepository;
import antonionorfo.norflyHorizonTours.repositories.PhotoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PhotoService {

    private final PhotoRepository photoRepository;
    private final ExcursionRepository excursionRepository;
    private final CloudinaryService cloudinaryService;

    public PhotoDTO uploadPhoto(UUID excursionId, MultipartFile file, boolean isCoverPhoto) throws IOException {
        Excursion excursion = excursionRepository.findById(excursionId)
                .orElseThrow(() -> new ResourceNotFoundException("Excursion not found!"));

        String uploadedUrl = cloudinaryService.uploadImage(file);

        Photo photo = new Photo();
        photo.setPhotoOfExcursion(uploadedUrl);
        photo.setIsCoverPhoto(isCoverPhoto);
        photo.setExcursion(excursion);

        Photo savedPhoto = photoRepository.save(photo);
        return mapToDTO(savedPhoto);
    }

    public void deletePhoto(UUID photoId, UUID userId) {
        Photo photo = photoRepository.findById(photoId)
                .orElseThrow(() -> new ResourceNotFoundException("Photo not found!"));

        if (!isAdmin(userId) && !photo.getExcursion().getCity().getCountry().getRegion().equals(userId)) {
            throw new BadRequestException("You do not have permission to delete this photo.");
        }

        try {
            cloudinaryService.deleteImage(photo.getPhotoOfExcursion());
        } catch (IOException e) {
            throw new BadRequestException("Failed to delete the image on Cloudinary!");
        }

        photoRepository.delete(photo);
    }

    public PhotoDTO getPhotoDetails(UUID photoId) {
        Photo photo = photoRepository.findById(photoId)
                .orElseThrow(() -> new ResourceNotFoundException("Photo not found!"));
        return mapToDTO(photo);
    }

    private PhotoDTO mapToDTO(Photo photo) {
        return new PhotoDTO(
                photo.getPhotoId(),
                photo.getPhotoOfExcursion(),
                photo.getIsCoverPhoto(),
                photo.getExcursion().getExcursionId()
        );
    }

    private boolean isAdmin(UUID userId) {
        return false;
    }
}

