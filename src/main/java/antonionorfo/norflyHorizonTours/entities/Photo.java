package antonionorfo.norflyHorizonTours.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Photo {
    @Id
    @GeneratedValue
    private UUID photoId;

    private String photoOfExcursion;
    private Boolean isCoverPhoto = false;

    @ManyToOne
    @JoinColumn(name = "excursion_id")
    private Excursion excursion;
}
