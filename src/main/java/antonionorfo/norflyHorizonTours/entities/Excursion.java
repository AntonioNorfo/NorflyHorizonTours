package antonionorfo.norflyHorizonTours.entities;

import antonionorfo.norflyHorizonTours.enums.DifficultyLevel;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Excursion {
    @Id
    @GeneratedValue
    private UUID excursionId;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String descriptionExcursion;

    private BigDecimal price;
    private String duration;

    @Enumerated(EnumType.STRING)
    private DifficultyLevel difficultyLevel;

    @Column(columnDefinition = "TEXT")
    private String inclusions;

    @Column(columnDefinition = "TEXT")
    private String rules;

    @Column(columnDefinition = "TEXT")
    private String notRecommended;

    private Integer maxParticipants;

    @ManyToOne
    @JoinColumn(name = "city_id")
    private City city;


    public void setDescription(String description) {
        this.descriptionExcursion = description;
    }
}
