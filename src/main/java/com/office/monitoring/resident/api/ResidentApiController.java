package com.office.monitoring.resident.api;

import com.office.monitoring.resident.ResidentService;
import com.office.monitoring.resident.dto.ResidentCreateRequest;
import com.office.monitoring.resident.dto.ResidentCreateResponse;
import com.office.monitoring.resident.dto.ResidentResponse;
import com.office.monitoring.resident.dto.ResidentUpdateRequest;
import com.office.monitoring.security.CurrentUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/residents")
public class ResidentApiController {

    private final ResidentService residentService;
    private final CurrentUserService currentUserService;

    @PostMapping
    public ResidentCreateResponse createResident(@Valid @RequestBody ResidentCreateRequest request) {
        Long residentId = residentService.createResident(currentUserService.getUsername(), request);
        return new ResidentCreateResponse(true, residentId, "거주자 정보가 등록되었습니다.");
    }

    @PutMapping("/{id}")
    public Map<String, Object> updateResident(@PathVariable Long id,
                                              @Valid @RequestBody ResidentUpdateRequest request) {
        residentService.updateResident(currentUserService.getUsername(), id, request);
        return Map.of(
                "success", true,
                "message", "거주자 정보가 수정되었습니다."
        );
    }

    @GetMapping("/{id}")
    public Map<String, Object> getResident(@PathVariable Long id) {
        ResidentResponse resident = residentService.getResident(currentUserService.getUsername(), id);
        return Map.of(
                "success", true,
                "resident", resident
        );
    }
}
