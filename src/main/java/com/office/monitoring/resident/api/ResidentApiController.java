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
/** 거주자 관련 HTTP 요청을 받아 JSON 응답으로 반환하는 API 컨트롤러. */
public class ResidentApiController {

    private final ResidentService residentService;

    @PostMapping
    /** 요청 데이터를 거주자 기준으로 저장하고 저장 결과를 반환한다. */
    public ResidentCreateResponse createResident(@Valid @RequestBody ResidentCreateRequest request) {
        Long residentId = residentService.createResident(request);
        return new ResidentCreateResponse(true, residentId, "거주자 정보가 등록되었습니다.");
    }

    @GetMapping
    /** 거주자 관련 데이터를 조회해 호출자에게 필요한 형태로 반환한다. */
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
    /** 대상 거주자 정보를 삭제 또는 탈퇴 처리하고 완료 결과를 반환한다. */
    public Map<String, Object> deleteResident(@PathVariable Long id) {
        residentService.deleteResident(id);
        return Map.of(
                "success", true,
                "message", "거주자 정보가 삭제되었습니다."
        );
    }

    @GetMapping("/{id}")
    /** 거주자 관련 데이터를 조회해 호출자에게 필요한 형태로 반환한다. */
    public Map<String, Object> getResident(@PathVariable Long id) {
        ResidentResponse resident = residentService.getResident(id);
        return Map.of(
                "success", true,
                "resident", resident
        );
    }

    @ExceptionHandler(ResidentDeletionBlockedException.class)
    /** 요청된 거주자 작업에 필요한 입력을 반영해 결과 값을 생성한다. */
    public ResponseEntity<Map<String, Object>> handleResidentDeletionBlockedException(ResidentDeletionBlockedException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                "success", false,
                "message", exception.getMessage()
        ));
    }
}
