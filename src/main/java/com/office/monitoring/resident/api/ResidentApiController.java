package com.office.monitoring.resident.api;

import com.office.monitoring.resident.ResidentService;
import com.office.monitoring.resident.dto.ResidentCreateRequest;
import com.office.monitoring.resident.dto.ResidentCreateResponse;
import com.office.monitoring.resident.dto.ResidentResponse;
import com.office.monitoring.resident.dto.ResidentUpdateRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/residents")
public class ResidentApiController {

    private final ResidentService residentService;

    @PostMapping
    public ResidentCreateResponse createResident(@Valid @RequestBody ResidentCreateRequest request) {
        Long residentId = residentService.createResident(request);
        return new ResidentCreateResponse(true, residentId, "거주자 정보가 등록되었습니다.");
    }

    @GetMapping
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

    @GetMapping("/{id}")
    public Map<String, Object> getResident(@PathVariable Long id) {
        ResidentResponse resident = residentService.getResident(id);
        return Map.of(
                "success", true,
                "resident", resident
        );
    }
}
