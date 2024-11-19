package antonionorfo.norflyHorizonTours.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PasswordResetToken {
    @Id
    @GeneratedValue
    private UUID resetTokenId;

    private String token;
    private LocalDateTime expirationDate;
    private Boolean isUsed;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
}
