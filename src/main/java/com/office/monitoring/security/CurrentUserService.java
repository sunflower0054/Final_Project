package com.office.monitoring.security;

import com.office.monitoring.member.Member;
import com.office.monitoring.member.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
/** 로그인 사용자 식별과 접근 제어 규칙을 구성하는 보안 구성 요소. */
public class CurrentUserService {

    private final MemberRepository memberRepository;

    /** 인증/인가 관련 데이터를 조회해 호출자에게 필요한 형태로 반환한다. */
    public String getUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("로그인한 사용자만 이용할 수 있습니다.");
        }

        if (authentication instanceof AnonymousAuthenticationToken) {
            throw new IllegalStateException("로그인한 사용자만 이용할 수 있습니다.");
        }

        String username = authentication.getName();

        if (username == null || username.isBlank()) {
            throw new IllegalStateException("로그인한 사용자만 이용할 수 있습니다.");
        }

        return username;
    }

    /** 인증/인가 관련 데이터를 조회해 호출자에게 필요한 형태로 반환한다. */
    public Member getCurrentMember() {
        String username = getUsername();
        return memberRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("현재 로그인한 사용자를 찾을 수 없습니다."));
    }

    /** 인증/인가 관련 데이터를 조회해 호출자에게 필요한 형태로 반환한다. */
    public Long getResidentId() {
        Member member = getCurrentMember();

        if (member.getResidentId() == null) {
            throw new IllegalStateException("먼저 거주자 정보를 등록해야 합니다.");
        }

        return member.getResidentId();
    }
}
