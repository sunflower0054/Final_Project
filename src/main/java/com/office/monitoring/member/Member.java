package com.office.monitoring.member;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "users", uniqueConstraints = {
        @UniqueConstraint(name = "uk_users_username", columnNames = "username")
})
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
/** 회원 정보 조회, 가입, 수정, 탈퇴와 관련된 도메인 동작을 담당하는 구성 요소. */
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "username", nullable = false, length = 50)
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

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    /** 요청된 회원 작업에 필요한 입력을 반영해 결과 값을 생성한다. */
    protected void onCreate() {
        if (role == null) {
            role = Role.FAMILY;
        }
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    /** 수정 요청값을 기존 회원 정보에 반영하고 최신 결과를 반환한다. */
    public void updateMyInfo(String name, String phone, String purpose) {
        this.name = name.trim();
        this.phone = phone.trim();
        this.purpose = purpose.trim();
    }

    /** 요청된 회원 작업에 필요한 입력을 반영해 결과 값을 생성한다. */
    public void assignResident(Long residentId) {
        this.residentId = residentId;
    }
}
