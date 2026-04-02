/*
import com.office.monitoring.event.EventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminStatsService {

    private final UserRepository userRepository;
    private final ResidentRepository residentRepository;
    private final EventRepository eventRepository;

    // [AD-01] 회원 통계
    public AdminStatsDTO.UserStatsResponse getUserStats() {
        AdminStatsDTO.UserStatsResponse response = new AdminStatsDTO.UserStatsResponse();

        response.setTotalUsers(userRepository.count());

        // 1. 연령대별 그룹핑 (구글 차트 형식)
        List<List<Object>> ageGroups = new ArrayList<>();
        ageGroups.add(Arrays.asList("연령대", "인원 수"));
        // DB에서 연령대별 count 집계 (예: "20대", 350)
        List<Object[]> ageStats = userRepository.countUsersByAgeGroup();
        for (Object[] stat : ageStats) {
            ageGroups.add(Arrays.asList(stat[0], stat[1]));
        }
        response.setAgeGroups(ageGroups);

        // 2. 이용목적별 그룹핑
        List<List<Object>> purposes = new ArrayList<>();
        purposes.add(Arrays.asList("목적", "비율"));
        List<Object[]> purposeStats = userRepository.countUsersByPurpose();
        for (Object[] stat : purposeStats) {
            purposes.add(Arrays.asList(stat[0] != null ? stat[0].toString() : "미지정", stat[1]));
        }
        response.setPurposes(purposes);

        return response;
    }

    // [AD-02] 독거노인 통계
    public AdminStatsDTO.ResidentStatsResponse getResidentStats() {
        AdminStatsDTO.ResidentStatsResponse response = new AdminStatsDTO.ResidentStatsResponse();

        response.setTotalResidents(residentRepository.count());

        // 평균 나이 계산 로직 (DB 함수 혹은 Java 연산)
        Double avgAge = residentRepository.calculateAverageAge();
        response.setAverageAge(avgAge != null ? Math.round(avgAge * 10) / 10.0 : 0.0);

        // 연령대(60대, 70대, 80대 이상) 그룹핑
        List<List<Object>> ageGroups = new ArrayList<>();
        ageGroups.add(Arrays.asList("연령대", "인원 수"));
        List<Object[]> residentAgeStats = residentRepository.countResidentsBySeniorAgeGroup();
        for (Object[] stat : residentAgeStats) {
            ageGroups.add(Arrays.asList(stat[0], stat[1]));
        }
        response.setAgeGroups(ageGroups);

        return response;
    }

    // [AD-03] 이벤트 통계
    public AdminStatsDTO.EventStatsResponse getEventStats(Integer year, Integer month) {
        AdminStatsDTO.EventStatsResponse response = new AdminStatsDTO.EventStatsResponse();

        response.setTotalEvents(eventRepository.countByYearAndMonth(year, month));

        // 처리 상태별 통계 (OPEN, RESOLVED, CLOSED) - CLOSED가 오탐지율 지표
        List<List<Object>> byStatus = new ArrayList<>();
        byStatus.add(Arrays.asList("상태", "건수"));
        List<Object[]> statusStats = eventRepository.countEventsByStatus(year, month);
        for (Object[] stat : statusStats) {
            byStatus.add(Arrays.asList(stat[0].toString(), stat[1]));
        }
        response.setByStatus(byStatus);

        // 월별/타입별 발생 추이 (Line Chart용 복합 데이터)
        // 형식: ['월', '낙상', '움직임 없음', '폭행']
        List<List<Object>> monthlyTrend = new ArrayList<>();
        monthlyTrend.add(Arrays.asList("월", "낙상", "움직임 없음", "폭행"));

        // DB에서 가져온 월별 집계 데이터를 차트 형식에 맞게 맵핑
        List<Object[]> trendData = eventRepository.getMonthlyEventTrend(year);
        for (Object[] row : trendData) {
            monthlyTrend.add(Arrays.asList(row[0] + "월", row[1], row[2], row[3]));
        }
        response.setMonthlyTrend(monthlyTrend);

        return response;
    }
}
*/
