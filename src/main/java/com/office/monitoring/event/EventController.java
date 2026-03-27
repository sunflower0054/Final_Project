package com.office.monitoring.event;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/event")
public class EventController {

    @GetMapping({"", "/events_timeline"})
    public String eventsTimeline() {
        return "event/events_timeline";
    }

    @GetMapping("/detail")
    public String eventDetail() {
        return "event/event_detail";
    }
}
