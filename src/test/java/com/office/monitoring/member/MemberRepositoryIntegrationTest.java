package com.office.monitoring.member;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/** 회원 기능의 조건별 응답과 저장 결과를 검증하는 테스트 클래스. */
class MemberRepositoryIntegrationTest extends MemberIntegrationTestSupport {

    @Test
    /** 주어진 요청 조건에서 기대한 상태 코드와 응답/데이터 결과가 유지되는지 검증한다. */
    void 회원조회는_username으로_동작한다() {
        Member found = memberRepository.findByUsername("user").orElse(null);

        assertThat(found).isNotNull();
        assertThat(found.getUsername()).isEqualTo("user");
        assertThat(found.getPhone()).isEqualTo("010-2222-2222");
    }
}
