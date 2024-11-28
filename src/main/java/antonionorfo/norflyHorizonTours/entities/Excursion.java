package antonionorfo.norflyHorizonTours.entities;

import antonionorfo.norflyHorizonTours.enums.DifficultyLevel;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
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

    @ManyToOne
    @JoinColumn(name = "country_id")
    private Country country;

    private LocalDateTime startDate;

    private LocalDateTime endDate;

    @ElementCollection
    @CollectionTable(name = "excursion_markers", joinColumns = @JoinColumn(name = "excursion_id"))
    @Column(name = "marker")
    private List<String> markers;

    @PrePersist
    @PreUpdate
    private void ensureDatesAreNotNull() {
        if (startDate == null) {
            startDate = LocalDateTime.now();
        }
        if (endDate == null) {
            endDate = startDate.plusDays(1);
        }
    }

    public String getDescriptionExcursion() {
        return descriptionExcursion;
    }

    public void setDescriptionExcursion(String descriptionExcursion) {
        this.descriptionExcursion = descriptionExcursion;
    }
}
