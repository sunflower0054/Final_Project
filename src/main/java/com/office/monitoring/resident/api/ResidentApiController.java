package com.office.monitoring.resident.api;

import com.office.monitoring.resident.ResidentService;
import com.office.monitoring.resident.ResidentDeletionBlockedException;
import com.office.monitoring.resident.dto.ResidentCreateRequest;
import com.office.monitoring.resident.dto.ResidentCreateResponse;
import com.office.monitoring.resident.dto.ResidentResponse;
import com.office.monitoring.resident.dto.ResidentUpdateRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/residents")
/** ResidentApiController의 역할을 담당한다. */
public class ResidentApiController {

    private final ResidentService residentService;

    @PostMapping
    /** createResident 동작을 수행한다. */
    public ResidentCreateResponse createResident(@Valid @RequestBody ResidentCreateRequest request) {
        Long residentId = residentService.createResident(request);
        return new ResidentCreateResponse(true, residentId, "거주자 정보가 등록되었습니다.");
    }

    @GetMapping
    /** getResidents 동작을 수행한다. */
    public Map<String, Object> getResidents() {
        return Map.of(
                "success", true,
                "residents", residentService.getResidents()
        );
    }

    @PutMapping("/{id}")
    public Map<String, Object> updateResident(@PathVariable Long id,
                                              @Valid @RequestBody ResidentUpdateRequest request) {
        residentService.updateResident(id, request);
        return Map.of(
                "success", true,
                "message", "거주자 정보가 수정되었습니다."
        );
    }

    @DeleteMapping("/{id}")
    /** deleteResident 동작을 수행한다. */
    public Map<String, Object> deleteResident(@PathVariable Long id) {
        residentService.deleteResident(id);
        return Map.of(
                "success", true,
                "message", "거주자 정보가 삭제되었습니다."
        );
    }

    @GetMapping("/{id}")
    /** getResident 동작을 수행한다. */
    public Map<String, Object> getResident(@PathVariable Long id) {
        ResidentResponse resident = residentService.getResident(id);
        return Map.of(
                "success", true,
                "resident", resident
        );
    }

    @ExceptionHandler(ResidentDeletionBlockedException.class)
    /** handleResidentDeletionBlockedException 동작을 수행한다. */
    public ResponseEntity<Map<String, Object>> handleResidentDeletionBlockedException(ResidentDeletionBlockedException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                "success", false,
                "message", exception.getMessage()
        ));
    }
}
