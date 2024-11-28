package antonionorfo.norflyHorizonTours.services;

import antonionorfo.norflyHorizonTours.entities.Notification;
import antonionorfo.norflyHorizonTours.entities.User;
import antonionorfo.norflyHorizonTours.exception.ResourceNotFoundException;
import antonionorfo.norflyHorizonTours.payloads.NotificationDTO;
import antonionorfo.norflyHorizonTours.repositories.NotificationRepository;
import antonionorfo.norflyHorizonTours.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public void sendNotification(NotificationDTO notificationDTO) {
        User user = userRepository.findById(notificationDTO.userId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + notificationDTO.userId()));

        Notification notification = new Notification();
        notification.setMessage(notificationDTO.message());
        notification.setDateCreated(LocalDateTime.now());
        notification.setIsRead(false);
        notification.setUser(user);

        notificationRepository.save(notification);
    }

    public List<NotificationDTO> getUserNotifications(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        return notificationRepository.findByUser(user)
                .stream()
                .map(this::mapToNotificationDTO)
                .collect(Collectors.toList());
    }

    public void markAsRead(UUID userId, UUID notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with ID: " + notificationId));

        if (!notification.getUser().getUserId().equals(userId)) {
            throw new ResourceNotFoundException("Notification does not belong to the user with ID: " + userId);
        }

        notification.setIsRead(true);
        notificationRepository.save(notification);
    }

    public void deleteNotification(UUID userId, UUID notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with ID: " + notificationId));

        if (!notification.getUser().getUserId().equals(userId)) {
            throw new ResourceNotFoundException("Notification does not belong to the user with ID: " + userId);
        }

        notificationRepository.delete(notification);
    }

    private NotificationDTO mapToNotificationDTO(Notification notification) {
        return new NotificationDTO(
                notification.getNotificationId(),
                notification.getMessage(),
                notification.getDateCreated(),
                notification.getIsRead(),
                notification.getUser().getUserId()
        );
    }
}
