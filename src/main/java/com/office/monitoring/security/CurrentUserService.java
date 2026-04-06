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
public class CurrentUserService {

    private final MemberRepository memberRepository;

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

    public Member getCurrentMember() {
        String username = getUsername();
        return memberRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("현재 로그인한 사용자를 찾을 수 없습니다."));
    }

    public Long getResidentId() {
        Member member = getCurrentMember();

        if (member.getResidentId() == null) {
            throw new IllegalStateException("먼저 거주자 정보를 등록해야 합니다.");
        }

        return member.getResidentId();
    }
}
