package com.office.monitoring.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 관리자 통계 API 요청을 받는 컨트롤러. */
@RestController
@RequestMapping("/api/v1/admin/stats")
@RequiredArgsConstructor
public class AdminStatsController {

    private final AdminStatsService adminStatsService;

    /** 회원 통계 집계 결과를 반환한다. */
    @GetMapping("/users")
    public AdminStatsDTO.UserStatsResponse getUserStats() {
        return adminStatsService.getUserStats();
    }

    /** 거주자 통계 집계 결과를 반환한다. */
    @GetMapping("/residents")
    public AdminStatsDTO.ResidentStatsResponse getResidentStats() {
        return adminStatsService.getResidentStats();
    }

    /** 연도·월 조건에 맞는 이벤트 통계를 반환한다. */
    @GetMapping("/events")
    public AdminStatsDTO.EventStatsResponse getEventStats(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month) {
        return adminStatsService.getEventStats(year, month);
    }
}
