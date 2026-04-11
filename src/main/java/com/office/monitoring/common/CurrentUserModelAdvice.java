package com.office.monitoring.common;

import com.office.monitoring.member.Member;
import com.office.monitoring.member.Role;
import com.office.monitoring.security.CurrentUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.LinkedHashMap;
import java.util.Map;

@ControllerAdvice
@RequiredArgsConstructor
/** 여러 기능에서 공통으로 사용하는 모델 구성과 예외 응답을 제공하는 구성 요소. */
public class CurrentUserModelAdvice {

    private static final String NOT_REGISTERED = "미등록";

    private final CurrentUserService currentUserService;

    @ModelAttribute("currentUser")
    /** 요청된 공통 작업에 필요한 입력을 반영해 결과 값을 생성한다. */
    public Map<String, Object> currentUser() {
        System.out.println("=== currentUser() 실행됨 ===");
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        System.out.println("=== authentication: " + authentication);
        System.out.println("=== isAuthenticated: " + (authentication != null ? authentication.isAuthenticated() : "null"));
        System.out.println("=== isAnonymous: " + (authentication instanceof AnonymousAuthenticationToken));

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            System.out.println("=== 인증 실패 → false 반환");
            return createDefaultModel(false);
        }

        try {
            Member member = currentUserService.getCurrentMember();
            System.out.println("=== member 조회 성공: " + member);

            Map<String, Object> model = createDefaultModel(true);
            model.put("username", display(member.getUsername()));
            model.put("name", display(member.getName()));
            model.put("phone", display(member.getPhone()));
            model.put("purpose", display(member.getPurpose()));
            model.put("residentId", member.getResidentId());
            model.put("role", member.getRole() != null ? member.getRole().name() : NOT_REGISTERED);
            model.put("roleLabel", toRoleLabel(member.getRole()));

            System.out.println("=== 최종 model: " + model);
            return model;

        } catch (Exception e) {
            System.out.println("=== 예외 발생: " + e.getMessage());
            e.printStackTrace();
            return createDefaultModel(false);
        }
    }

    /** 요청 데이터를 공통 기준으로 저장하고 저장 결과를 반환한다. */
    private Map<String, Object> createDefaultModel(boolean authenticated) {
        Map<String, Object> model = new LinkedHashMap<>();
        model.put("authenticated", authenticated);

        model.put("username", NOT_REGISTERED);
        model.put("name", NOT_REGISTERED);
        model.put("phone", NOT_REGISTERED);
        model.put("purpose", NOT_REGISTERED);
        model.put("residentId", null);
        model.put("role", NOT_REGISTERED);
        model.put("roleLabel", NOT_REGISTERED);

        model.put("email", NOT_REGISTERED);
        model.put("gender", NOT_REGISTERED);
        model.put("birthDate", NOT_REGISTERED);
        model.put("managedDeviceCount", NOT_REGISTERED);
        model.put("monthlyAlertCount", NOT_REGISTERED);

        return model;
    }

    /** 요청된 공통 작업에 필요한 입력을 반영해 결과 값을 생성한다. */
    private String display(String value) {
        if (value == null || value.isBlank()) {
            return NOT_REGISTERED;
        }
        return value.trim();
    }

    /** 요청/엔티티 데이터를 다른 표현 객체로 변환해 반환한다. */
    private String toRoleLabel(Role role) {
        if (role == null) {
            return NOT_REGISTERED;
        }

        return switch (role) {
            case ADMIN -> "시스템 관리자";
            case FAMILY -> "보호자";
        };
    }
}
