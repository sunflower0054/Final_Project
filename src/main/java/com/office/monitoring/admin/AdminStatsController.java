/*
//import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/admin/stats")
@RequiredArgsConstructor
// ADMIN 권한 검증 (Security 설정에 따라 조정)
//@PreAuthorize("hasRole('ADMIN')")
public class AdminStatsController {

    private final AdminStatsService adminStatsService;

    // [AD-01] 회원 통계 조회
    @GetMapping("/users")
    public AdminStatsDTO.UserStatsResponse getUserStats() {
        return adminStatsService.getUserStats();
    }

    // [AD-02] 독거노인 통계 조회
    @GetMapping("/residents")
    public AdminStatsDTO.ResidentStatsResponse getResidentStats() {
        return adminStatsService.getResidentStats();
    }

    // [AD-03] 이벤트 통계 조회
    @GetMapping("/events")
    public AdminStatsDTO.EventStatsResponse getEventStats(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month) {
        return adminStatsService.getEventStats(year, month);
    }
}*/
