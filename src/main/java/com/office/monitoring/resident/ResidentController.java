package com.office.monitoring.resident;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/resident")
/** ResidentController의 역할을 담당한다. */
public class ResidentController {

    @GetMapping("/detail")
    /** residentDetail 동작을 수행한다. */
    public String residentDetail() {
        return "resident/resident_detail";
    }

    @GetMapping("/edit")
    /** residentEdit 동작을 수행한다. */
    public String residentEdit() {
        return "resident/resident_edit";
    }
}
