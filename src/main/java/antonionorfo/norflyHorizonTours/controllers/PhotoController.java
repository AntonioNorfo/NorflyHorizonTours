package antonionorfo.norflyHorizonTours.controllers;

import antonionorfo.norflyHorizonTours.payloads.PhotoDTO;
import antonionorfo.norflyHorizonTours.services.PhotoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/photos")
@RequiredArgsConstructor
public class PhotoController {

    private final PhotoService photoService;

    @PostMapping("/upload")
    public ResponseEntity<PhotoDTO> uploadPhoto(
            @RequestParam UUID excursionId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "isCoverPhoto", defaultValue = "false") boolean isCoverPhoto
    ) throws IOException {
        PhotoDTO photo = photoService.uploadPhoto(excursionId, file, isCoverPhoto);
        return ResponseEntity.ok(photo);
    }

    @DeleteMapping("/{photoId}")
    public ResponseEntity<Void> deletePhoto(
            @PathVariable UUID photoId,
            @RequestHeader("User-ID") UUID userId
    ) {
        photoService.deletePhoto(photoId, userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{photoId}")
    public ResponseEntity<PhotoDTO> getPhotoDetails(@PathVariable UUID photoId) {
        return ResponseEntity.ok(photoService.getPhotoDetails(photoId));
    }
}
