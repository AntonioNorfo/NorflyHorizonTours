package antonionorfo.norflyHorizonTours.entities;

import antonionorfo.norflyHorizonTours.enums.DifficultyLevel;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Setter
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

    @ManyToOne
    @JoinColumn(name = "country_id")
    private Country country;

    @ElementCollection
    @CollectionTable(name = "excursion_markers", joinColumns = @JoinColumn(name = "excursion_id"))
    @Column(name = "marker")
    private List<String> markers;

    public int getDurationInHours() {
        if (duration == null || duration.isEmpty()) {
            throw new IllegalArgumentException("Duration is not set or invalid.");
        }

        if (duration.contains("hour")) {
            return Integer.parseInt(duration.split(" ")[0]);
        } else if (duration.contains("day")) {
            return Integer.parseInt(duration.split(" ")[0]) * 24;
        }

        throw new IllegalArgumentException("Unsupported duration format: " + duration);
    }

}

