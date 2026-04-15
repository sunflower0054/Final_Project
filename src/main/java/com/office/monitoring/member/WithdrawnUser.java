package com.office.monitoring.member;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "withdrawn_users")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
/** 회원 정보 조회, 가입, 수정, 탈퇴와 관련된 도메인 동작을 담당하는 구성 요소. */
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
    /** 요청된 회원 작업에 필요한 입력을 반영해 결과 값을 생성한다. */
    protected void onCreate() {
        if (withdrawnAt == null) {
            withdrawnAt = LocalDateTime.now();
        }
    }

    /** 요청/엔티티 데이터를 다른 표현 객체로 변환해 반환한다. */
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
