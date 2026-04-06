package com.office.monitoring.member;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MemberRepositoryIntegrationTest extends MemberIntegrationTestSupport {

    @Test
    void 회원조회는_username으로_동작한다() {
        Member found = memberRepository.findByUsername("user").orElse(null);

        assertThat(found).isNotNull();
        assertThat(found.getUsername()).isEqualTo("user");
        assertThat(found.getPhone()).isEqualTo("010-2222-2222");
    }
}
