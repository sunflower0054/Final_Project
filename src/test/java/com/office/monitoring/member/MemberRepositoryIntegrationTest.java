package com.office.monitoring.member;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/** MemberRepositoryIntegrationTest 테스트를 정의한다. */
class MemberRepositoryIntegrationTest extends MemberIntegrationTestSupport {

/** 회원조회는_username으로_동작한다 시나리오를 검증한다. */
    @Test
    void 회원조회는_username으로_동작한다() {
        Member found = memberRepository.findByUsername("user").orElse(null);

        assertThat(found).isNotNull();
        assertThat(found.getUsername()).isEqualTo("user");
        assertThat(found.getPhone()).isEqualTo("010-2222-2222");
    }
}
