package com.office.monitoring.event;


import com.office.monitoring.event.EventReceiveRequest;
import com.office.monitoring.event.EventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Slf4j
@RestController
@RequestMapping("/api/v1/events")
@RequiredArgsConstructor
public class EventApiController {

    private final EventService eventService;

    // 파이썬 → 스프링: multipart/form-data (payload + 이미지)
    @PostMapping("/receive")
    public ResponseEntity<String> receiveEvent(
            @RequestParam("resident_id")  String residentId,
            @RequestParam("event_type")   String eventType,
            @RequestParam("timestamp")    String timestamp,
            @RequestParam("confidence")   String confidence,
            @RequestParam(value = "metadata", required = false) String metadata,
            @RequestPart(value = "frame_image", required = false) MultipartFile frameImage
    ) {
        EventReceiveRequest req = new EventReceiveRequest();
        req.setResidentId(residentId);
        req.setEventType(eventType);
        req.setTimestamp(timestamp);
        req.setConfidence(confidence);
        req.setMetadata(metadata);

        try {
            eventService.receiveEvent(req, frameImage);
            return ResponseEntity.ok("success");
        } catch (IOException e) {
            log.error("[EVENT 수신 오류]", e);
            return ResponseEntity.internalServerError().body("이미지 저장 실패");
        } catch (Exception e) {
            log.error("[EVENT 처리 오류]", e);
            return ResponseEntity.internalServerError().body("이벤트 처리 실패");
        }
    }
}
