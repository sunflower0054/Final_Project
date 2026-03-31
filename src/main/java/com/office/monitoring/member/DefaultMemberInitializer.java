package com.office.monitoring.member;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@Profile({"local", "dev"})
@RequiredArgsConstructor
public class DefaultMemberInitializer implements CommandLineRunner {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        if (memberRepository.count() > 0) {
            return;
        }

        memberRepository.save(Member.builder()
            .loginId("admin")
            .password(passwordEncoder.encode("admin1234!"))
            .name("관리자")
            .email("admin@office.local")
            .role(Role.ADMIN)
            .enabled(true)
            .build());

        memberRepository.save(Member.builder()
            .loginId("user")
            .password(passwordEncoder.encode("user1234!"))
            .name("사용자")
            .email("user@office.local")
            .role(Role.FAMILY)
            .enabled(true)
            .build());

        log.info("기본 계정(admin/user)을 생성했습니다. (profile: local/dev)");
    }
}
