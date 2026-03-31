package com.office.monitoring.member.api;

import com.office.monitoring.member.MemberService;
import com.office.monitoring.member.dto.MyInfoResponse;
import com.office.monitoring.member.dto.UpdateMyInfoRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/my-info")
public class MyInfoApiController {

    private final MemberService memberService;

    @GetMapping
    public MyInfoResponse getMyInfo(@AuthenticationPrincipal UserDetails principal) {
        return memberService.getMyInfo(extractUsername(principal));
    }

    @PutMapping
    public MyInfoResponse updateMyInfo(@AuthenticationPrincipal UserDetails principal,
                                       @Valid @RequestBody UpdateMyInfoRequest request) {
        return memberService.updateMyInfo(extractUsername(principal), request);
    }

    private String extractUsername(UserDetails principal) {
        if (principal == null || principal.getUsername() == null || principal.getUsername().isBlank()) {
            throw new IllegalStateException("로그인한 사용자만 이용할 수 있습니다.");
        }
        return principal.getUsername();
    }
}
