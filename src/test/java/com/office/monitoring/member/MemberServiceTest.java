package com.office.monitoring.member;

import com.office.monitoring.member.dto.MyInfoResponse;
import com.office.monitoring.member.dto.WithdrawRequest;
import com.office.monitoring.member.dto.WithdrawResponse;
import com.office.monitoring.security.CurrentUserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
/** 회원 기능의 조건별 응답과 저장 결과를 검증하는 테스트 클래스. */
class MemberServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private WithdrawnUserRepository withdrawnUserRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private CurrentUserService currentUserService;

    @InjectMocks
    private MemberService memberService;

    @Test
    /** 주어진 요청 조건에서 기대한 상태 코드와 응답/데이터 결과가 유지되는지 검증한다. */
    void checkUsernameAvailable_앞뒤공백을_trim한_뒤_중복검사한다() {
        when(memberRepository.findByUsername("user")).thenReturn(Optional.of(Member.builder()
                .username("user")
                .build()));

        boolean available = memberService.checkUsernameAvailable("  user  ");

        assertThat(available).isFalse();
        verify(memberRepository).findByUsername("user");
    }

    @Test
    /** 주어진 요청 조건에서 기대한 상태 코드와 응답/데이터 결과가 유지되는지 검증한다. */
    void updateMyInfo_성공시_trim된_정보로_응답한다() {
        Member member = Member.builder()
                .username("user")
                .password("encoded")
                .name("기존 이름")
                .phone("010-1111-1111")
                .purpose("기존 목적")
                .role(Role.FAMILY)
                .build();

        when(currentUserService.getCurrentMember()).thenReturn(member);

        MyInfoResponse response = memberService.updateMyInfo(
                new com.office.monitoring.member.dto.UpdateMyInfoRequest(
                        "  수정 이름  ",
                        "  010-7777-8888  ",
                        "  가족 모니터링  "
                )
        );

        assertThat(member.getName()).isEqualTo("수정 이름");
        assertThat(member.getPhone()).isEqualTo("010-7777-8888");
        assertThat(member.getPurpose()).isEqualTo("가족 모니터링");
        assertThat(response.name()).isEqualTo("수정 이름");
        assertThat(response.phone()).isEqualTo("010-7777-8888");
        assertThat(response.purpose()).isEqualTo("가족 모니터링");
        assertThat(response.role()).isEqualTo("FAMILY");
    }

    @Test
    /** 주어진 요청 조건에서 기대한 상태 코드와 응답/데이터 결과가 유지되는지 검증한다. */
    void withdraw_탈퇴회원백업후_원본회원은_삭제한다() {
        Member member = Member.builder()
                .id(1L)
                .username("user")
                .password("encoded-password")
                .name("테스트 사용자")
                .phone("010-2222-2222")
                .birthYear(1990)
                .purpose("초기 목적")
                .residentId(101L)
                .role(Role.FAMILY)
                .createdAt(LocalDateTime.of(2026, 4, 1, 9, 30))
                .build();

        when(currentUserService.getCurrentMember()).thenReturn(member);
        when(passwordEncoder.matches("user1234!", "encoded-password")).thenReturn(true);

        WithdrawResponse response = memberService.withdraw(new WithdrawRequest("user1234!", "  기능 부족  "));

        ArgumentCaptor<WithdrawnUser> captor = ArgumentCaptor.forClass(WithdrawnUser.class);
        verify(withdrawnUserRepository).save(captor.capture());
        verify(memberRepository).delete(member);

        WithdrawnUser saved = captor.getValue();
        assertThat(saved.getOriginalUserId()).isEqualTo(1L);
        assertThat(saved.getUsername()).isEqualTo("user");
        assertThat(saved.getPassword()).isEqualTo("encoded-password");
        assertThat(saved.getName()).isEqualTo("테스트 사용자");
        assertThat(saved.getPhone()).isEqualTo("010-2222-2222");
        assertThat(saved.getBirthYear()).isEqualTo(1990);
        assertThat(saved.getPurpose()).isEqualTo("기능 부족");
        assertThat(saved.getResidentId()).isEqualTo(101L);
        assertThat(saved.getRole()).isEqualTo(Role.FAMILY);
        assertThat(saved.getCreatedAt()).isEqualTo(LocalDateTime.of(2026, 4, 1, 9, 30));
        assertThat(response.success()).isTrue();
        assertThat(response.message()).isEqualTo("회원탈퇴가 완료되었습니다.");
    }

    @Test
    void withdraw_비밀번호가_다르면_예외가_발생한다() {
        Member member = Member.builder()
                .username("user")
                .password("encoded-password")
                .build();

        when(currentUserService.getCurrentMember()).thenReturn(member);
        when(passwordEncoder.matches("wrong", "encoded-password")).thenReturn(false);

        assertThatThrownBy(() -> memberService.withdraw(new WithdrawRequest("wrong", "사유")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("비밀번호가 일치하지 않습니다.");
    }
}
