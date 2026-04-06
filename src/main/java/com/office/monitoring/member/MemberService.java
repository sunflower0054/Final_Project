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
/** 회원 정보 조회, 가입, 수정, 탈퇴와 관련된 도메인 동작을 담당하는 구성 요소. */
public class MemberService {

    private final MemberRepository memberRepository;
    private final WithdrawnUserRepository withdrawnUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final CurrentUserService currentUserService;

    /** 입력된 값이 회원 규칙을 만족하는지 판별해 사용 가능 여부를 반환한다. */
    public boolean checkUsernameAvailable(String username) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("아이디는 필수입니다.");
        }

        String normalizedUsername = username.trim();
        return memberRepository.findByUsername(normalizedUsername).isEmpty();
    }

    @Transactional
    /** 요청 데이터를 회원 기준으로 저장하고 저장 결과를 반환한다. */
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

    /** 회원 관련 데이터를 조회해 호출자에게 필요한 형태로 반환한다. */
    public MyInfoResponse getMyInfo() {
        return MyInfoResponse.from(currentUserService.getCurrentMember());
    }

    @Transactional
    /** 수정 요청값을 기존 회원 정보에 반영하고 최신 결과를 반환한다. */
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
    /** 대상 회원 정보를 삭제 또는 탈퇴 처리하고 완료 결과를 반환한다. */
    public WithdrawResponse withdraw() {
        Member member = currentUserService.getCurrentMember();

        WithdrawnUser withdrawnUser = WithdrawnUser.from(member);
        withdrawnUserRepository.save(withdrawnUser);

        memberRepository.delete(member);

        return new WithdrawResponse(true, "회원탈퇴가 완료되었습니다.");
    }
}
