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
/** 회원 관련 HTTP 요청을 받아 JSON 응답으로 반환하는 API 컨트롤러. */
public class MyInfoApiController {

    private final MemberService memberService;

    @GetMapping
    /** 회원 관련 데이터를 조회해 호출자에게 필요한 형태로 반환한다. */
    public MyInfoResponse getMyInfo() {
        return memberService.getMyInfo();
    }

    @PutMapping
    /** 수정 요청값을 기존 회원 정보에 반영하고 최신 결과를 반환한다. */
    public MyInfoResponse updateMyInfo(@Valid @RequestBody UpdateMyInfoRequest request) {
        return memberService.updateMyInfo(request);
    }
}
