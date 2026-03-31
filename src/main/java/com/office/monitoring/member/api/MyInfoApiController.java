package com.office.monitoring.member.api;

import com.office.monitoring.member.MemberService;
import com.office.monitoring.member.dto.MyInfoResponse;
import com.office.monitoring.member.dto.UpdateMyInfoRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/my-info")
public class MyInfoApiController {

    private final MemberService memberService;

    @GetMapping
    public MyInfoResponse getMyInfo() {
        return memberService.getMyInfo();
    }

    @PutMapping
    public ResponseEntity<?> updateMyInfo(@Valid @RequestBody UpdateMyInfoRequest request) {
        memberService.updateMyInfo(request);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "내 정보가 수정되었습니다."
        ));
    }
}
