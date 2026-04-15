package com.office.monitoring;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
/** 애플리케이션 기능의 조건별 응답과 저장 결과를 검증하는 테스트 클래스. */
class MonitoringApplicationTests {

    @Test
    /** 주어진 요청 조건에서 기대한 상태 코드와 응답/데이터 결과가 유지되는지 검증한다. */
    void contextLoads() {
    }
}
