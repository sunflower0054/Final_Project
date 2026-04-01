package com.office.monitoring.member;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "withdrawn_users")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WithdrawnUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "original_user_id", nullable = false)
    private Long originalUserId;

    @Column(nullable = false, length = 50)
    private String username;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false, length = 20)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    @Column(name = "birth_year")
    private Integer birthYear;

    @Column(length = 255)
    private String purpose;

    @Column(name = "resident_id")
    private Long residentId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "withdrawn_at", nullable = false)
    private LocalDateTime withdrawnAt;

    @PrePersist
    protected void onCreate() {
        if (withdrawnAt == null) {
            withdrawnAt = LocalDateTime.now();
        }
    }

    public static WithdrawnUser from(Member member) {
        return WithdrawnUser.builder()
                .originalUserId(member.getId())
                .username(member.getUsername())
                .password(member.getPassword())
                .name(member.getName())
                .phone(member.getPhone())
                .role(member.getRole())
                .birthYear(member.getBirthYear())
                .purpose(member.getPurpose())
                .residentId(member.getResidentId())
                .createdAt(member.getCreatedAt())
                .build();
    }
}
