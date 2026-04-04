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
public class CurrentUserModelAdvice {

    private static final String NOT_REGISTERED = "미등록";

    private final CurrentUserService currentUserService;

    @ModelAttribute("currentUser")
    public Map<String, Object> currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            return createDefaultModel(false);
        }

        Member member = currentUserService.getCurrentMember();

        Map<String, Object> model = createDefaultModel(true);
        model.put("username", display(member.getUsername()));
        model.put("name", display(member.getName()));
        model.put("phone", display(member.getPhone()));
        model.put("purpose", display(member.getPurpose()));
        model.put("residentId", member.getResidentId());
        model.put("role", member.getRole() != null ? member.getRole().name() : NOT_REGISTERED);
        model.put("roleLabel", toRoleLabel(member.getRole()));

        return model;
    }

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

    private String display(String value) {
        if (value == null || value.isBlank()) {
            return NOT_REGISTERED;
        }
        return value.trim();
    }

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
