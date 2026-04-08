package com.office.monitoring.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/stats")
@RequiredArgsConstructor
/** 관리자 통계 API 요청을 처리하는 컨트롤러. */
public class AdminStatsController {

    private final AdminStatsService adminStatsService;

    @GetMapping("/users")
    public AdminStatsDTO.UserStatsResponse getUserStats() {
        return adminStatsService.getUserStats();
    }

    @GetMapping("/residents")
    public AdminStatsDTO.ResidentStatsResponse getResidentStats() {
        return adminStatsService.getResidentStats();
    }

    @GetMapping("/events")
    public AdminStatsDTO.EventStatsResponse getEventStats(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month) {
        return adminStatsService.getEventStats(year, month);
    }
}
