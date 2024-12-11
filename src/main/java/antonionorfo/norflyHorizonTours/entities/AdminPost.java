package antonionorfo.norflyHorizonTours.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AdminPost {
    @Id
    @GeneratedValue

    private UUID adminPostId;

    private String titlePost;

    @Column(columnDefinition = "TEXT")
    private String contentPost;

    private String photoPostAdmin;
    private String videoPostAdmin;

    private LocalDate publicationDate;
}
