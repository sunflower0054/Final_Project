package com.office.monitoring.member;

import com.office.monitoring.member.dto.MyInfoResponse;
import com.office.monitoring.member.dto.RegisterRequest;
import com.office.monitoring.member.dto.RegisterResponse;
import com.office.monitoring.member.dto.UpdateMyInfoRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    public boolean checkUsernameAvailable(String username) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("아이디는 필수입니다.");
        }

        String normalizedUsername = username.trim();
        return memberRepository.findByUsername(normalizedUsername).isEmpty();
    }

    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        String username = request.username().trim();

        if (memberRepository.findByUsername(username).isPresent()) {
            throw new IllegalArgumentException("이미 사용 중인 아이디입니다.");
        }

        Member member = Member.builder()
                .username(username)
                .password(passwordEncoder.encode(request.password().trim()))
                .name(request.name().trim())
                .phone(request.phone().trim())
                .purpose(request.purpose().trim())
                .role(Role.FAMILY)
                .build();

        memberRepository.save(member);

        return new RegisterResponse(true, "회원가입이 완료되었습니다.");
    }

    public MyInfoResponse getMyInfo() {
        return MyInfoResponse.from(getCurrentMember());
    }

    @Transactional
    public MyInfoResponse updateMyInfo(UpdateMyInfoRequest request) {
        Member member = getCurrentMember();
        member.updateMyInfo(
                request.name(),
                request.phone(),
                request.purpose()
        );
        return MyInfoResponse.from(member);
    }

    private Member getCurrentMember() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            throw new IllegalStateException("로그인한 사용자만 이용할 수 있습니다.");
        }

        String username = authentication.getName();

        return memberRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("현재 로그인한 사용자를 찾을 수 없습니다."));
    }
}
