package com.office.monitoring.event;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter @Setter
public class EventReceiveDto {

    private String resident_id;
    private String event_type;
    private String timestamp;
    private String confidence;
    private String metadata;
    private MultipartFile frame_image;
}