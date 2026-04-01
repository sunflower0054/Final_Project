package com.office.monitoring.dailyActivity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "daily_activity",
        uniqueConstraints = @UniqueConstraint(columnNames = {"resident_id", "date"}))
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DailyActivity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "resident_id", nullable = false)
    private Long residentId;

    @Column(name = "date", nullable = false)
    private LocalDate date;

    @Column(name = "motion_score", nullable = false)
    private Integer motionScore;
}
