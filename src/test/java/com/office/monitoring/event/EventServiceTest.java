package com.office.monitoring.event;

import com.office.monitoring.eventImage.EventImage;
import com.office.monitoring.eventImage.EventImageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;


@ExtendWith(MockitoExtension.class)
class EventServiceTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private EventImageRepository eventImageRepository;

    @InjectMocks
    private EventService eventService;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(eventService, "uploadDir", tempDir.toString());
        ReflectionTestUtils.setField(eventService, "sep", "/");

        AtomicLong seq = new AtomicLong(100L);
        lenient().when(eventRepository.save(any(Event.class))).thenAnswer(invocation -> {
            Event event = invocation.getArgument(0);
            event.setId(seq.getAndIncrement());
            return event;
        });
    }

    @Test
    void 정상요청과_이미지첨부시_이벤트와_이미지가_저장되고_파일이_생성된다() throws IOException {
        EventReceiveRequest request = 요청("1", "VIOLENT_MOTION_DETECTED", "2026-04-06T10:11:12", "0.85",
                "{'person_count': 2, 'max_velocity': 0.05, 'last_motion_timestamp': '2026-04-06T10:00:00'}");
        MockMultipartFile image = new MockMultipartFile("frameImage", "frame.jpg", "image/jpeg", "image-bytes".getBytes());

        Event result = eventService.receiveEvent(request, image);

        ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);
        verify(eventRepository).save(eventCaptor.capture());

        Event savedEvent = eventCaptor.getValue();
        assertThat(result.getId()).isEqualTo(savedEvent.getId());
        assertThat(savedEvent.getStatus()).isEqualTo("PENDING");
        assertThat(savedEvent.getResidentId()).isEqualTo(1L);
        assertThat(savedEvent.getEventType()).isEqualTo("VIOLENT_MOTION_DETECTED");
        assertThat(savedEvent.getConfidence()).isEqualTo(0.85D);
        assertThat(savedEvent.getTimestamp()).isEqualTo(LocalDateTime.of(2026, 4, 6, 10, 11, 12));
        assertThat(savedEvent.getPersonCount()).isEqualTo(2);
        assertThat(savedEvent.getMaxVelocity()).isEqualTo(0.05D);
        assertThat(savedEvent.getLastMotionTimestamp()).isEqualTo(LocalDateTime.of(2026, 4, 6, 10, 0, 0));

        ArgumentCaptor<EventImage> imageCaptor = ArgumentCaptor.forClass(EventImage.class);
        verify(eventImageRepository).save(imageCaptor.capture());

        EventImage savedImage = imageCaptor.getValue();
        assertThat(savedImage.getEvent()).isSameAs(savedEvent);
        assertThat(savedImage.getImagePath()).contains("/events/1/2026-04-06/");
        assertThat(savedImage.getImagePath()).contains(savedEvent.getId() + "_");

        assertThat(Files.exists(Path.of(savedImage.getImagePath()))).isTrue();
    }

    @Test
    void 이미지가_null이면_이벤트만_저장되고_이미지는_저장되지_않는다() throws IOException {
        EventReceiveRequest request = 요청("2", "FALL_DETECTED", "2026-04-06T12:00:00", "0.9", null);

        eventService.receiveEvent(request, null);

        verify(eventRepository).save(any(Event.class));
        verify(eventImageRepository, never()).save(any(EventImage.class));
    }

    @Test
    void 이미지가_empty면_이벤트만_저장되고_이미지는_저장되지_않는다() throws IOException {
        EventReceiveRequest request = 요청("3", "FALL_DETECTED", "2026-04-06T12:00:00", "0.9", null);
        MultipartFile emptyImage = new MockMultipartFile("frameImage", "", "image/jpeg", new byte[0]);

        eventService.receiveEvent(request, emptyImage);

        verify(eventRepository).save(any(Event.class));
        verify(eventImageRepository, never()).save(any(EventImage.class));
    }

    @Test
    void metadata가_없거나_키가_누락되어도_이벤트는_저장되고_파생필드는_null이다() throws IOException {
        EventReceiveRequest request = 요청("4", "NO_MOTION_DETECTED", "2026-04-06T13:00:00", "0.7", null);

        eventService.receiveEvent(request, null);

        ArgumentCaptor<Event> captor = ArgumentCaptor.forClass(Event.class);
        verify(eventRepository).save(captor.capture());

        Event savedEvent = captor.getValue();
        assertThat(savedEvent.getPersonCount()).isNull();
        assertThat(savedEvent.getMaxVelocity()).isNull();
        assertThat(savedEvent.getLastMotionTimestamp()).isNull();
    }

    @Test
    void metadata형식이_이상하면_파싱실패필드는_null로_처리되고_이벤트저장은_계속된다() throws IOException {
        EventReceiveRequest request = 요청("5", "VIOLENT_MOTION_DETECTED", "2026-04-06T14:00:00", "0.6",
                "{'person_count': two, 'max_velocity': fast, 'last_motion_timestamp': 'broken'}");

        eventService.receiveEvent(request, null);

        ArgumentCaptor<Event> captor = ArgumentCaptor.forClass(Event.class);
        verify(eventRepository).save(captor.capture());

        Event savedEvent = captor.getValue();
        assertThat(savedEvent.getPersonCount()).isNull();
        assertThat(savedEvent.getMaxVelocity()).isNull();
        assertThat(savedEvent.getLastMotionTimestamp()).isNull();
    }

    @Test
    void residentId_confidence_timestamp가_잘못된_문자열이면_예외가_발생하고_저장하지_않는다() {
        EventReceiveRequest badResidentId = 요청("not-number", "FALL_DETECTED", "2026-04-06T15:00:00", "0.5", null);
        EventReceiveRequest badConfidence = 요청("1", "FALL_DETECTED", "2026-04-06T15:00:00", "not-double", null);
        EventReceiveRequest badTimestamp = 요청("1", "FALL_DETECTED", "invalid-time", "0.5", null);

        assertThatThrownBy(() -> eventService.receiveEvent(badResidentId, null)).isInstanceOf(NumberFormatException.class);
        assertThatThrownBy(() -> eventService.receiveEvent(badConfidence, null)).isInstanceOf(NumberFormatException.class);
        assertThatThrownBy(() -> eventService.receiveEvent(badTimestamp, null)).isInstanceOf(DateTimeParseException.class);

        verify(eventRepository, never()).save(any(Event.class));
        verify(eventImageRepository, never()).save(any(EventImage.class));
    }

    private EventReceiveRequest 요청(String residentId, String eventType, String timestamp, String confidence, String metadata) {
        EventReceiveRequest request = new EventReceiveRequest();
        request.setResidentId(residentId);
        request.setEventType(eventType);
        request.setTimestamp(timestamp);
        request.setConfidence(confidence);
        request.setMetadata(metadata);
        return request;
    }
}
