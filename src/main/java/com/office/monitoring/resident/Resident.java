package com.office.monitoring.resident;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "residents")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
/** 거주자 데이터 조회·등록·수정·삭제 흐름을 담당하는 구성 요소. */
public class Resident {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @Column(name = "birth_date", nullable = false)
    private LocalDate birthDate;

    @Column(name = "address", nullable = false, length = 255)
    private String address;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "disease", length = 255)
    private String disease;

    @Column(name = "latitude", nullable = false)
    private Double latitude;

    @Column(name = "longitude", nullable = false)
    private Double longitude;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    /** 요청된 거주자 작업에 필요한 입력을 반영해 결과 값을 생성한다. */
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    public void update(String name,
                       LocalDate birthDate,
                       String address,
                       String phone,
                       String disease,
                       Double latitude,
                       Double longitude) {
        this.name = name.trim();
        this.birthDate = birthDate;
        this.address = address.trim();
        this.phone = trimToNull(phone);
        this.disease = trimToNull(disease);
        this.latitude = latitude;
        this.longitude = longitude;
    }

    /** 요청된 거주자 작업에 필요한 입력을 반영해 결과 값을 생성한다. */
    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
