package antonionorfo.norflyHorizonTours.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
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
public class City {
    @Id
    @GeneratedValue
    private UUID cityId;

    private String name;
    private String country;

    @Column(columnDefinition = "TEXT")
    private String descriptionCity;

    private String coordinates;
}
