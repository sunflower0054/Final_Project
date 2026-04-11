package com.office.monitoring.resident;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/resident")
/** 거주자 데이터 조회·등록·수정·삭제 흐름을 담당하는 구성 요소. */
public class ResidentController {

    @GetMapping("/detail")
    /** 요청된 거주자 작업에 필요한 입력을 반영해 결과 값을 생성한다. */
    public String residentDetail() {
        return "resident/resident_detail";
    }

    @GetMapping("/edit")
    /** 요청된 거주자 작업에 필요한 입력을 반영해 결과 값을 생성한다. */
    public String residentEdit() {
        return "resident/resident_edit";
    }
}
