package com.office.monitoring.member.api;

import com.office.monitoring.member.MemberService;
import com.office.monitoring.member.dto.MyInfoResponse;
import com.office.monitoring.member.dto.UpdateMyInfoRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

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
    public MyInfoResponse updateMyInfo(@Valid @RequestBody UpdateMyInfoRequest request) {
        return memberService.updateMyInfo(request);
    }
}
