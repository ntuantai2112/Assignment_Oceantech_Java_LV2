package com.octl2.api.controller;

import com.octl2.api.commons.OctResponse;
import com.octl2.api.commons.exception.ErrorMessage;
import com.octl2.api.commons.exception.ErrorMessages;
import com.octl2.api.dto.response.LogisticResponse;
import com.octl2.api.service.LogisticService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.Min;
import java.util.List;

@RestController
@RequestMapping("/api/v1/logistics")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Validated
@Slf4j
public class LogisticController {

    LogisticService logisticService;

    @GetMapping("/province/{provinceId}")
    public OctResponse<List<LogisticResponse>> getLogisticProvince(@PathVariable("provinceId") Integer provinceId) {
        List<LogisticResponse> logisticResponses = logisticService.getLogisticByProvince(provinceId);
        return OctResponse.build(logisticResponses);
    }

    @GetMapping("/province")
    public OctResponse<List<LogisticResponse>> getLogisticProvinceName(@RequestParam(name = "name", required = false) String provinceName) {
        List<LogisticResponse> logisticResponses = logisticService.getLogisticByProvinceName(provinceName);
        return OctResponse.build(logisticResponses);
    }

    @GetMapping("/provinces/paged")
    public OctResponse<Page<LogisticResponse>> getLogisticProvincesPaged(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<LogisticResponse> logisticResponses = logisticService.getLogisticByProvincesPage(pageable);
        return OctResponse.build(logisticResponses, pageable.getPageSize());
    }

    @GetMapping("/provinces")
    public OctResponse<List<LogisticResponse>> getLogisticProvinces() {
        List<LogisticResponse> logisticResponses = logisticService.getLogisticByProvinces();
        return OctResponse.build(logisticResponses);
    }

}
