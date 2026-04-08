package com.office.monitoring.admin;

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

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
/** 관리자 통계 집계를 제공하는 서비스. */
public class AdminStatsService {

    private static final String UNKNOWN_AGE_GROUP = "미상";
    private static final String UNSPECIFIED_PURPOSE = "미지정";
    private static final String FALL_DETECTED = "FALL_DETECTED";
    private static final String NO_MOTION_DETECTED = "NO_MOTION_DETECTED";
    private static final String VIOLENT_MOTION_DETECTED = "VIOLENT_MOTION_DETECTED";
    private static final String CONFIRMED = "CONFIRMED";
    private static final String AUTO_REPORTED = "AUTO_REPORTED";
    private static final String CLOSED = "CLOSED";

    private final MemberRepository memberRepository;
    private final ResidentRepository residentRepository;
    private final EventRepository eventRepository;

    public AdminStatsDTO.UserStatsResponse getUserStats() {
        List<MemberRepository.AdminStatsView> members = memberRepository.findAllForAdminStats();
        return new AdminStatsDTO.UserStatsResponse(
                true,
                members.size(),
                buildUserAgeGroups(members),
                buildUserPurposes(members)
        );
    }

    public AdminStatsDTO.ResidentStatsResponse getResidentStats() {
        List<ResidentRepository.AdminStatsView> residents = residentRepository.findAllForAdminStats();
        return new AdminStatsDTO.ResidentStatsResponse(
                true,
                residents.size(),
                calculateAverageAge(residents),
                buildResidentAgeGroups(residents)
        );
    }

    public AdminStatsDTO.EventStatsResponse getEventStats(Integer year, Integer month) {
        List<EventRepository.AdminStatsView> events = getFilteredEvents(year, month);
        return new AdminStatsDTO.EventStatsResponse(
                true,
                events.size(),
                buildEventTypes(events),
                buildEventStatuses(events),
                buildMonthlyTrend(events, year, month)
        );
    }

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

    private String normalizePurpose(String purpose) {
        if (purpose == null) {
            return UNSPECIFIED_PURPOSE;
        }

        String trimmedPurpose = purpose.trim();
        return trimmedPurpose.isEmpty() ? UNSPECIFIED_PURPOSE : trimmedPurpose;
    }

    private int calculateResidentAge(LocalDate birthDate, LocalDate today) {
        if (birthDate == null || birthDate.isAfter(today)) {
            return -1;
        }

        return Period.between(birthDate, today).getYears();
    }

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

    private List<EventRepository.AdminStatsView> getFilteredEvents(Integer year, Integer month) {
        if (year == null) {
            return eventRepository.findAllForAdminStats();
        }

        boolean monthFilterEnabled = isValidMonth(month);
        LocalDateTime start = monthFilterEnabled
                ? LocalDateTime.of(year, month, 1, 0, 0)
                : LocalDateTime.of(year, 1, 1, 0, 0);
        LocalDateTime end = monthFilterEnabled
                ? start.plusMonths(1)
                : start.plusYears(1);

        return eventRepository.findAllForAdminStatsBetween(start, end);
    }

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

    private String toEventTypeLabel(String eventType) {
        return switch (eventType) {
            case FALL_DETECTED -> "낙상";
            case NO_MOTION_DETECTED -> "움직임 없음";
            case VIOLENT_MOTION_DETECTED -> "폭행";
            default -> null;
        };
    }

    private Integer toEventTypeIndex(String eventType) {
        return switch (eventType) {
            case FALL_DETECTED -> 0;
            case NO_MOTION_DETECTED -> 1;
            case VIOLENT_MOTION_DETECTED -> 2;
            default -> null;
        };
    }

    private String toEventStatusLabel(String status) {
        return switch (status) {
            case CONFIRMED -> "수동신고";
            case AUTO_REPORTED -> "자동신고";
            case CLOSED -> "오탐지";
            default -> null;
        };
    }

    private String formatMonthLabel(YearMonth yearMonth, Integer requestedYear) {
        if (requestedYear != null) {
            return yearMonth.getMonthValue() + "월";
        }
        return "%d-%02d".formatted(yearMonth.getYear(), yearMonth.getMonthValue());
    }

    private boolean isValidMonth(Integer month) {
        return month != null && month >= 1 && month <= 12;
    }

    private List<Object> row(Object... values) {
        return Arrays.asList(values);
    }
}
