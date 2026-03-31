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

        return memberRepository.findByUsername(username).isEmpty();
    }

    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        validateRegisterRequest(request);

        if (memberRepository.findByUsername(request.username()).isPresent()) {
            throw new IllegalArgumentException("이미 사용 중인 아이디입니다.");
        }

        Member member = Member.builder()
                .username(request.username())
                .password(passwordEncoder.encode(request.password()))
                .name(request.name())
                .phone(request.phone())
                .purpose(request.purpose())
                .role(Role.FAMILY)
                .build();

        memberRepository.save(member);

        return new RegisterResponse(true, "회원가입이 완료되었습니다.");
    }

    public MyInfoResponse getMyInfo() {
        Member member = getCurrentMember();
        return MyInfoResponse.from(member);
    }

    @Transactional
    public void updateMyInfo(UpdateMyInfoRequest request) {
        validateUpdateMyInfoRequest(request);

        Member member = getCurrentMember();
        member.updateMyInfo(
                request.name(),
                request.phone(),
                request.purpose()
        );
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

    private void validateRegisterRequest(RegisterRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("요청 본문이 비어 있습니다.");
        }
        if (request.username() == null || request.username().isBlank()) {
            throw new IllegalArgumentException("아이디는 필수입니다.");
        }
        if (request.password() == null || request.password().isBlank()) {
            throw new IllegalArgumentException("비밀번호는 필수입니다.");
        }
        if (request.name() == null || request.name().isBlank()) {
            throw new IllegalArgumentException("이름은 필수입니다.");
        }
        if (request.phone() == null || request.phone().isBlank()) {
            throw new IllegalArgumentException("전화번호는 필수입니다.");
        }
        if (request.purpose() == null || request.purpose().isBlank()) {
            throw new IllegalArgumentException("사용 목적은 필수입니다.");
        }
    }

    private void validateUpdateMyInfoRequest(UpdateMyInfoRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("요청 본문이 비어 있습니다.");
        }
        if (request.name() == null || request.name().isBlank()) {
            throw new IllegalArgumentException("이름은 필수입니다.");
        }
        if (request.phone() == null || request.phone().isBlank()) {
            throw new IllegalArgumentException("전화번호는 필수입니다.");
        }
        if (request.purpose() == null || request.purpose().isBlank()) {
            throw new IllegalArgumentException("사용 목적은 필수입니다.");
        }
    }
}
