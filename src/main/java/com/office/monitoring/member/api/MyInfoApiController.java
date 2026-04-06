package com.office.monitoring.member.api;

import com.office.monitoring.member.MemberService;
import com.office.monitoring.member.dto.MyInfoResponse;
import com.office.monitoring.member.dto.UpdateMyInfoRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/my-info")
/** MyInfoApiController의 역할을 담당한다. */
public class MyInfoApiController {

    private final MemberService memberService;

    @GetMapping
    /** getMyInfo 동작을 수행한다. */
    public MyInfoResponse getMyInfo() {
        return memberService.getMyInfo();
    }

    @PutMapping
    /** updateMyInfo 동작을 수행한다. */
    public MyInfoResponse updateMyInfo(@Valid @RequestBody UpdateMyInfoRequest request) {
        return memberService.updateMyInfo(request);
    }
}
