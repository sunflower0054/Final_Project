/*
package com.office.monitoring.withdrawnUser;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class WithdrawController {

    private final WithdrawService withdrawService;

    @DeleteMapping("/withdraw")
    public ResponseEntity<WithdrawDTO.Response> withdraw(
            @RequestBody WithdrawDTO.Request request,
            @AuthenticationPrincipal CustomUserDetails userDetails // 세션 또는 시큐리티 컨텍스트 사용자 식별
    ) {
        // 1. 세션에서 현재 사용자 식별 (userDetails.getId())
        withdrawService.softDeleteUser(userDetails.getId(), request);

        // 명세서에 정의된 응답 포맷
        return ResponseEntity.ok(new WithdrawDTO.Response(true, "회원 탈퇴 처리가 완료되었습니다. (서비스 이용 제한)"));
    }
}
*/
