package antonionorfo.norflyHorizonTours.controllers;

import antonionorfo.norflyHorizonTours.payloads.NotificationDTO;
import antonionorfo.norflyHorizonTours.services.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping("/admin/notifications")
    public ResponseEntity<Void> sendNotification(@RequestBody @Valid NotificationDTO notificationDTO) {
        notificationService.sendNotification(notificationDTO);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/users/{userId}/notifications")
    public ResponseEntity<List<NotificationDTO>> getUserNotifications(@PathVariable UUID userId) {
        return ResponseEntity.ok(notificationService.getUserNotifications(userId));
    }

    @PutMapping("/users/{userId}/notifications/{notificationId}/read")
    public ResponseEntity<Void> markNotificationAsRead(
            @PathVariable UUID userId,
            @PathVariable UUID notificationId
    ) {
        notificationService.markAsRead(userId, notificationId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/users/{userId}/notifications/{notificationId}")
    public ResponseEntity<Void> deleteNotification(
            @PathVariable UUID userId,
            @PathVariable UUID notificationId
    ) {
        notificationService.deleteNotification(userId, notificationId);
        return ResponseEntity.noContent().build();
    }
}
