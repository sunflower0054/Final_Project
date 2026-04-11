package com.office.monitoring.admin;

import com.office.monitoring.admin.dto.AdminCreatedAtStatsFilter;
import com.office.monitoring.admin.dto.AdminEventStatsFilter;
import com.office.monitoring.admin.dto.AdminStatsDTO;
import com.office.monitoring.event.EventRepository;
import com.office.monitoring.member.MemberRepository;
import com.office.monitoring.resident.ResidentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/** 관리자 통계 집계를 제공하는 서비스. */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminStatsService {

    private static final String UNKNOWN_AGE_GROUP = "미상";
    private static final String UNSPECIFIED_PURPOSE = "미지정";
    private static final String FALL_DETECTED = "FALL_DETECTED";
    private static final String NO_MOTION_DETECTED = "NO_MOTION_DETECTED";
    private static final String VIOLENT_MOTION_DETECTED = "VIOLENT_MOTION_DETECTED";
    private static final String CONFIRMED = "CONFIRMED";
    private static final String AUTO_REPORTED = "AUTO_REPORTED";
    private static final String CLOSED = "CLOSED";
    private static final String PENDING = "PENDING";
    private static final List<String> ALLOWED_EVENT_TYPES = List.of(
            FALL_DETECTED, NO_MOTION_DETECTED, VIOLENT_MOTION_DETECTED
    );
    private static final List<String> ALLOWED_STATUSES = List.of(
            PENDING, CONFIRMED, AUTO_REPORTED, CLOSED
    );

    private final MemberRepository memberRepository;
    private final ResidentRepository residentRepository;
    private final EventRepository eventRepository;

    /** 회원 수, 연령대, 가입 목적 통계를 조합한다. */
    public AdminStatsDTO.UserStatsResponse getUserStats() {
        return getUserStats(new AdminCreatedAtStatsFilter(null, null));
    }

    public AdminStatsDTO.UserStatsResponse getUserStats(AdminCreatedAtStatsFilter filter) {
        List<MemberRepository.AdminStatsView> members = getFilteredMembers(filter);
        return new AdminStatsDTO.UserStatsResponse(
                true,
                members.size(),
                buildUserAgeGroups(members),
                buildUserPurposes(members)
        );
    }

    /** 거주자 수, 평균 나이, 연령대 통계를 조합한다. */
    public AdminStatsDTO.ResidentStatsResponse getResidentStats() {
        return getResidentStats(new AdminCreatedAtStatsFilter(null, null));
    }

    public AdminStatsDTO.ResidentStatsResponse getResidentStats(AdminCreatedAtStatsFilter filter) {
        List<ResidentRepository.AdminStatsView> residents = getFilteredResidents(filter);
        return new AdminStatsDTO.ResidentStatsResponse(
                true,
                residents.size(),
                calculateAverageAge(residents),
                buildResidentAgeGroups(residents)
        );
    }

    /** 연도·월 필터를 반영해 이벤트 통계를 조합한다. */
    public AdminStatsDTO.EventStatsResponse getEventStats(AdminEventStatsFilter filter) {
        List<EventRepository.AdminStatsView> events = getFilteredEvents(filter);
        return new AdminStatsDTO.EventStatsResponse(
                true,
                events.size(),
                buildEventTypes(events),
                buildEventStatuses(events),
                buildMonthlyTrend(events, filter.year(), filter.month())
        );
    }

    /** 유효한 생년월일만 대상으로 평균 나이를 계산한다. */
    private double calculateAverageAge(List<ResidentRepository.AdminStatsView> residents) {
        LocalDate today = LocalDate.now();
        double averageAge = residents.stream()
                .map(view -> calculateResidentAge(view.getBirthDate(), today))
                .filter(age -> age >= 0)
                .mapToInt(Integer::intValue)
                .average()
                .orElse(0.0);

        return Math.round(averageAge * 10) / 10.0;
    }

    /** 회원 birthYear를 차트용 연령대 배열로 변환한다. */
    private List<List<Object>> buildUserAgeGroups(List<MemberRepository.AdminStatsView> members) {
        Map<String, Long> ageGroups = new LinkedHashMap<>();
        ageGroups.put("10대", 0L);
        ageGroups.put("20대", 0L);
        ageGroups.put("30대", 0L);
        ageGroups.put("40대", 0L);
        ageGroups.put("50대", 0L);
        ageGroups.put("60대 이상", 0L);
        ageGroups.put(UNKNOWN_AGE_GROUP, 0L);

        int currentYear = LocalDate.now().getYear();
        members.forEach(member -> ageGroups.compute(resolveUserAgeGroup(member.getBirthYear(), currentYear),
                (key, count) -> count == null ? 1L : count + 1));

        List<List<Object>> response = new ArrayList<>();
        response.add(row("연령대", "인원 수"));
        ageGroups.forEach((label, count) -> response.add(row(label, count)));
        return response;
    }

    /** 회원 purpose를 정규화해 목적별 집계 배열을 만든다. */
    private List<List<Object>> buildUserPurposes(List<MemberRepository.AdminStatsView> members) {
        Map<String, Long> purposeCounts = members.stream()
                .map(view -> normalizePurpose(view.getPurpose()))
                .collect(Collectors.groupingBy(
                        purpose -> purpose,
                        LinkedHashMap::new,
                        Collectors.counting()
                ));

        List<List<Object>> response = new ArrayList<>();
        response.add(row("이용 목적", "회원 수"));
        purposeCounts.entrySet().stream()
                .sorted(Comparator
                        .comparing((Map.Entry<String, Long> entry) -> UNSPECIFIED_PURPOSE.equals(entry.getKey()) ? 1 : 0)
                        .thenComparing(Map.Entry::getKey))
                .forEach(entry -> response.add(row(entry.getKey(), entry.getValue())));
        return response;
    }

    /** 거주자 birthDate를 기준으로 고령층 연령대 분포를 만든다. */
    private List<List<Object>> buildResidentAgeGroups(List<ResidentRepository.AdminStatsView> residents) {
        Map<String, Long> ageGroups = new LinkedHashMap<>();
        ageGroups.put("60대", 0L);
        ageGroups.put("70대", 0L);
        ageGroups.put("80대 이상", 0L);
        ageGroups.put("기타", 0L);

        LocalDate today = LocalDate.now();
        residents.forEach(resident -> ageGroups.compute(resolveResidentAgeGroup(resident.getBirthDate(), today),
                (key, count) -> count == null ? 1L : count + 1));

        List<List<Object>> response = new ArrayList<>();
        response.add(row("연령대", "인원 수"));
        ageGroups.forEach((label, count) -> response.add(row(label, count)));
        return response;
    }

    /** 회원 출생연도를 10년 단위 연령대로 매핑한다. */
    private String resolveUserAgeGroup(Integer birthYear, int currentYear) {
        if (birthYear == null || birthYear > currentYear) {
            return UNKNOWN_AGE_GROUP;
        }

        int age = currentYear - birthYear;
        if (age < 20) {
            return "10대";
        }
        if (age < 30) {
            return "20대";
        }
        if (age < 40) {
            return "30대";
        }
        if (age < 50) {
            return "40대";
        }
        if (age < 60) {
            return "50대";
        }
        return "60대 이상";
    }

    /** null·blank 목적값을 미지정으로 통일한다. */
    private String normalizePurpose(String purpose) {
        if (purpose == null) {
            return UNSPECIFIED_PURPOSE;
        }

        String trimmedPurpose = purpose.trim();
        return trimmedPurpose.isEmpty() ? UNSPECIFIED_PURPOSE : trimmedPurpose;
    }

    /** 미래 날짜나 null은 제외할 수 있게 -1로 처리한다. */
    private int calculateResidentAge(LocalDate birthDate, LocalDate today) {
        if (birthDate == null || birthDate.isAfter(today)) {
            return -1;
        }

        return Period.between(birthDate, today).getYears();
    }

    /** 거주자 나이를 60대, 70대, 80대 이상, 기타로 구분한다. */
    private String resolveResidentAgeGroup(LocalDate birthDate, LocalDate today) {
        int age = calculateResidentAge(birthDate, today);

        if (age >= 80) {
            return "80대 이상";
        }
        if (age >= 70) {
            return "70대";
        }
        if (age >= 60) {
            return "60대";
        }
        return "기타";
    }

    /** year/month 조합에 맞는 이벤트 조회 범위를 결정한다. */
    private List<MemberRepository.AdminStatsView> getFilteredMembers(AdminCreatedAtStatsFilter filter) {
        Integer year = filter.year();
        Integer month = filter.month();

        if (year == null) {
            return memberRepository.findAllForAdminStats();
        }

        boolean monthFilterEnabled = isValidMonth(month);
        LocalDateTime start = monthFilterEnabled
                ? LocalDateTime.of(year, month, 1, 0, 0)
                : LocalDateTime.of(year, 1, 1, 0, 0);
        LocalDateTime end = monthFilterEnabled
                ? start.plusMonths(1)
                : start.plusYears(1);

        return memberRepository.findAllForAdminStatsByCreatedAtBetween(start, end);
    }

    private List<ResidentRepository.AdminStatsView> getFilteredResidents(AdminCreatedAtStatsFilter filter) {
        Integer year = filter.year();
        Integer month = filter.month();

        if (year == null) {
            return residentRepository.findAllForAdminStats();
        }

        boolean monthFilterEnabled = isValidMonth(month);
        LocalDateTime start = monthFilterEnabled
                ? LocalDateTime.of(year, month, 1, 0, 0)
                : LocalDateTime.of(year, 1, 1, 0, 0);
        LocalDateTime end = monthFilterEnabled
                ? start.plusMonths(1)
                : start.plusYears(1);

        return residentRepository.findAllForAdminStatsByCreatedAtBetween(start, end);
    }

    private List<EventRepository.AdminStatsView> getFilteredEvents(AdminEventStatsFilter filter) {
        Integer year = filter.year();
        Integer month = filter.month();
        String eventType = normalizeEventType(filter.eventType());
        String status = normalizeStatus(filter.status());

        if (year == null) {
            return eventRepository.findAllForAdminStatsByFilter(eventType, status);
        }

        boolean monthFilterEnabled = isValidMonth(month);
        LocalDateTime start = monthFilterEnabled
                ? LocalDateTime.of(year, month, 1, 0, 0)
                : LocalDateTime.of(year, 1, 1, 0, 0);
        LocalDateTime end = monthFilterEnabled
                ? start.plusMonths(1)
                : start.plusYears(1);

        return eventRepository.findAllForAdminStatsBetweenByFilter(start, end, eventType, status);
    }

    /** 이벤트 타입을 고정 라벨 기준으로 집계한다. */
    private List<List<Object>> buildEventTypes(List<EventRepository.AdminStatsView> events) {
        Map<String, Long> typeCounts = new LinkedHashMap<>();
        typeCounts.put("낙상", 0L);
        typeCounts.put("움직임 없음", 0L);
        typeCounts.put("폭행", 0L);

        events.stream()
                .map(view -> toEventTypeLabel(view.getEventType()))
                .filter(label -> label != null)
                .forEach(label -> typeCounts.compute(label, (key, count) -> count == null ? 1L : count + 1));

        List<List<Object>> response = new ArrayList<>();
        response.add(row("유형", "건수"));
        typeCounts.forEach((label, count) -> response.add(row(label, count)));
        return response;
    }

    /** 운영 완료 상태만 상태 차트용으로 집계한다. */
    private List<List<Object>> buildEventStatuses(List<EventRepository.AdminStatsView> events) {
        Map<String, Long> statusCounts = new LinkedHashMap<>();
        statusCounts.put("수동신고", 0L);
        statusCounts.put("자동신고", 0L);
        statusCounts.put("오탐지", 0L);

        events.stream()
                .map(view -> toEventStatusLabel(view.getStatus()))
                .filter(label -> label != null)
                .forEach(label -> statusCounts.compute(label, (key, count) -> count == null ? 1L : count + 1));

        List<List<Object>> response = new ArrayList<>();
        response.add(row("상태", "건수"));
        statusCounts.forEach((label, count) -> response.add(row(label, count)));
        return response;
    }

    /** 필터 조건에 맞는 월별 이벤트 추이 배열을 만든다. */
    private List<List<Object>> buildMonthlyTrend(List<EventRepository.AdminStatsView> events, Integer year, Integer month) {
        List<List<Object>> response = new ArrayList<>();
        response.add(row("월", "낙상", "움직임 없음", "폭행"));

        Map<YearMonth, long[]> monthlyCounts = new LinkedHashMap<>();
        boolean monthFilterEnabled = year != null && isValidMonth(month);

        if (monthFilterEnabled) {
            monthlyCounts.put(YearMonth.of(year, month), new long[3]);
        } else if (year != null) {
            for (int currentMonth = 1; currentMonth <= 12; currentMonth++) {
                monthlyCounts.put(YearMonth.of(year, currentMonth), new long[3]);
            }
        } else {
            List<YearMonth> months = events.stream()
                    .map(EventRepository.AdminStatsView::getTimestamp)
                    .filter(timestamp -> timestamp != null)
                    .map(YearMonth::from)
                    .sorted()
                    .distinct()
                    .toList();
            months.forEach(value -> monthlyCounts.put(value, new long[3]));
        }

        events.forEach(event -> {
            if (event.getTimestamp() == null) {
                return;
            }

            Integer typeIndex = toEventTypeIndex(event.getEventType());
            if (typeIndex == null) {
                return;
            }

            YearMonth yearMonth = YearMonth.from(event.getTimestamp());
            long[] counts = monthlyCounts.get(yearMonth);
            if (counts != null) {
                counts[typeIndex]++;
            }
        });

        monthlyCounts.forEach((yearMonth, counts) -> response.add(row(
                formatMonthLabel(yearMonth, year),
                counts[0],
                counts[1],
                counts[2]
        )));

        return response;
    }

    /** 원본 이벤트 타입을 차트 라벨로 변환한다. */
    private String toEventTypeLabel(String eventType) {
        return switch (eventType) {
            case FALL_DETECTED -> "낙상";
            case NO_MOTION_DETECTED -> "움직임 없음";
            case VIOLENT_MOTION_DETECTED -> "폭행";
            default -> null;
        };
    }

    /** 월별 추이 배열에서 타입별 컬럼 위치를 반환한다. */
    private Integer toEventTypeIndex(String eventType) {
        return switch (eventType) {
            case FALL_DETECTED -> 0;
            case NO_MOTION_DETECTED -> 1;
            case VIOLENT_MOTION_DETECTED -> 2;
            default -> null;
        };
    }

    /** 원본 이벤트 상태를 상태 차트 라벨로 변환한다. */
    private String toEventStatusLabel(String status) {
        return switch (status) {
            case CONFIRMED -> "수동신고";
            case AUTO_REPORTED -> "자동신고";
            case CLOSED -> "오탐지";
            default -> null;
        };
    }

    /** 전체 기준과 연도 기준에 맞춰 월 라벨 형식을 맞춘다. */
    private String formatMonthLabel(YearMonth yearMonth, Integer requestedYear) {
        if (requestedYear != null) {
            return yearMonth.getMonthValue() + "월";
        }
        return "%d-%02d".formatted(yearMonth.getYear(), yearMonth.getMonthValue());
    }

    /** 문자열 값이 ALLOWED_EVENT_TYPES 안에 없으면 null 반환 */
    private String normalizeEventType(String eventType) {
        if (eventType == null || eventType.isBlank()) {
            return null;
        }
        String trimmed = eventType.trim();
        return ALLOWED_EVENT_TYPES.contains(trimmed) ? trimmed : null;
    }

    /** 문자열 값이 ALLOWED_STATUSES 안에 없으면 null 반환 */
    private String normalizeStatus(String status) {
        if (status == null || status.isBlank()) {
            return null;
        }
        String trimmed = status.trim();
        return ALLOWED_STATUSES.contains(trimmed) ? trimmed : null;
    }

    /** month 파라미터가 실제 월 범위인지 확인한다. */
    private boolean isValidMonth(Integer month) {
        return month != null && month >= 1 && month <= 12;
    }

    /** Google Charts용 2차원 배열의 한 행을 만든다. */
    private List<Object> row(Object... values) {
        return Arrays.asList(values);
    }
}
