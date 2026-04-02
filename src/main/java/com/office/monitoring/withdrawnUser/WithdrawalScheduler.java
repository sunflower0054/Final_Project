/*
package com.office.monitoring.withdrawnUser;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class WithdrawalScheduler {

    private final UserRepository userRepository;
    private final WithdrawnUserRepository withdrawnUserRepository;

    // 3. 매일 새벽 3시 실행: withdrawn_users INSERT -> users DELETE
    @Scheduled(cron = "0 0 3 * * *", zone = "Asia/Seoul")
    @Transactional
    public void processWithdrawals() {
        log.info("새벽 3시 회원 탈퇴 배치 작업을 시작합니다.");

        // 상태가 WITHDRAWN인 유저 목록 조회
        List<User> withdrawnUsers = userRepository.findByStatus(UserStatus.WITHDRAWN);

        if (withdrawnUsers.isEmpty()) {
            log.info("처리할 탈퇴 대기 회원이 없습니다.");
            return;
        }

        for (User user : withdrawnUsers) {
            // 이력 보존용 엔티티 생성 (데이터 마이그레이션)
            WithdrawnUser backupUser = WithdrawnUser.builder()
                    .originalUserId(user.getId())
                    .username(user.getUsername())
                    .password(user.getPassword())
                    .name(user.getName())
                    .phone(user.getPhone())
                    .role(user.getRole())
                    .birthYear(user.getBirthYear())
                    .purpose(user.getWithdrawPurpose()) // 소프트 딜리트 시 저장해둔 사유
                    .residentId(user.getResidentId())
                    .createdAt(user.getCreatedAt())
                    .build();

            // 백업 테이블에 INSERT
            withdrawnUserRepository.save(backupUser);
        }

        // 원본 테이블에서 하드 딜리트 (DELETE)
        userRepository.deleteAll(withdrawnUsers);

        log.info("총 {}명의 회원 탈퇴 데이터 이관 및 삭제 처리가 완료되었습니다.", withdrawnUsers.size());
    }
}*/
