package com.office.monitoring.member;

import com.office.monitoring.member.dto.MyInfoResponse;
import com.office.monitoring.member.dto.RegisterRequest;
import com.office.monitoring.member.dto.RegisterResponse;
import com.office.monitoring.member.dto.UpdateMyInfoRequest;
import com.office.monitoring.member.dto.WithdrawResponse;
import com.office.monitoring.security.CurrentUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
/** MemberService의 역할을 담당한다. */
public class MemberService {

    private final MemberRepository memberRepository;
    private final WithdrawnUserRepository withdrawnUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final CurrentUserService currentUserService;

    /** checkUsernameAvailable 동작을 수행한다. */
    public boolean checkUsernameAvailable(String username) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("아이디는 필수입니다.");
        }

        String normalizedUsername = username.trim();
        return memberRepository.findByUsername(normalizedUsername).isEmpty();
    }

    @Transactional
    /** register 동작을 수행한다. */
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
                .birthYear(request.birthYear())
                .purpose(request.purpose().trim())
                .role(Role.FAMILY)
                .build();

        memberRepository.save(member);

        return new RegisterResponse(true, "회원가입이 완료되었습니다.");
    }

    /** getMyInfo 동작을 수행한다. */
    public MyInfoResponse getMyInfo() {
        return MyInfoResponse.from(currentUserService.getCurrentMember());
    }

    @Transactional
    /** updateMyInfo 동작을 수행한다. */
    public MyInfoResponse updateMyInfo(UpdateMyInfoRequest request) {
        Member member = currentUserService.getCurrentMember();
        member.updateMyInfo(
                request.name(),
                request.phone(),
                request.purpose()
        );
        return MyInfoResponse.from(member);
    }

    @Transactional
    /** withdraw 동작을 수행한다. */
    public WithdrawResponse withdraw() {
        Member member = currentUserService.getCurrentMember();

        WithdrawnUser withdrawnUser = WithdrawnUser.from(member);
        withdrawnUserRepository.save(withdrawnUser);

        memberRepository.delete(member);

        return new WithdrawResponse(true, "회원탈퇴가 완료되었습니다.");
    }
}
