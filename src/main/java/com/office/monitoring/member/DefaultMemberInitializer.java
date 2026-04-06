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
/** DefaultMemberInitializer의 역할을 담당한다. */
public class DefaultMemberInitializer implements CommandLineRunner {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    /** run 동작을 수행한다. */
    public void run(String... args) {
        if (memberRepository.count() > 0) {
            return;
        }

        memberRepository.save(Member.builder()
            .username("admin")
            .password(passwordEncoder.encode("admin1234!"))
            .name("관리자")
            .phone("010-0000-0000")
            .role(Role.ADMIN)
            .build());

        memberRepository.save(Member.builder()
            .username("user")
            .password(passwordEncoder.encode("user1234!"))
            .name("사용자")
            .phone("010-1111-1111")
            .role(Role.FAMILY)
            .build());

        log.info("기본 계정(admin/user)을 생성했습니다. (profile: local/dev)");
    }
}
