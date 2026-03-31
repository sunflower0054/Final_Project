package com.office.monitoring.member.api;

import com.office.monitoring.member.MemberService;
import com.office.monitoring.member.dto.CheckUsernameResponse;
import com.office.monitoring.member.dto.RegisterRequest;
import com.office.monitoring.member.dto.RegisterResponse;
import com.office.monitoring.member.dto.WithdrawResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthApiController {

    private final MemberService memberService;

    @GetMapping("/check-username")
    public CheckUsernameResponse checkUsername(@RequestParam String username) {
        boolean available = memberService.checkUsernameAvailable(username);

        if (available) {
            return new CheckUsernameResponse(true, true, "사용 가능한 아이디입니다.");
        }

        return new CheckUsernameResponse(true, false, "이미 사용 중인 아이디입니다.");
    }

    @PostMapping("/register")
    public RegisterResponse register(@Valid @RequestBody RegisterRequest request) {
        return memberService.register(request);
    }

    @DeleteMapping("/withdraw")
    public WithdrawResponse withdraw(@AuthenticationPrincipal UserDetails principal,
                                     HttpServletRequest request,
                                     HttpServletResponse response) {
        String username = extractUsername(principal);

        WithdrawResponse result = memberService.withdraw(username);

        new SecurityContextLogoutHandler().logout(request, response, null);

        return result;
    }

    private String extractUsername(UserDetails principal) {
        if (principal == null || principal.getUsername().isBlank()) {
            throw new IllegalStateException("현재 로그인한 사용자를 확인할 수 없습니다.");
        }
        return principal.getUsername();
    }
}
