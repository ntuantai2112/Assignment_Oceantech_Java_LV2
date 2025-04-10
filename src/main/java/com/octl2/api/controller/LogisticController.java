package com.octl2.api.controller;

import com.octl2.api.commons.OctResponse;
import com.octl2.api.dto.response.LogisticResponse;
import com.octl2.api.helper.enums.LogisticeEnum;
import com.octl2.api.service.LogisticService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

@RestController
@RequestMapping("/api/v1/logistics")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Validated
@Slf4j
public class LogisticController {

    LogisticService logisticService;

    @GetMapping("/provinces")
    public OctResponse<Page<LogisticResponse>> getLogisticProvincesPaged(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Max(30) int size) {


        Pageable pageable = PageRequest.of(page, size);
        Page<LogisticResponse> logisticResponses = logisticService.findLogisticByProvinces(pageable);
        Long totalElement = logisticResponses.getTotalElements();
        log.info("Get provinces and logistics successfully!");
        return OctResponse.build(logisticResponses, LogisticeEnum.LOGISTIC_PROVINCE_SUCCESS.getMessage(), totalElement.intValue());

    }


    @GetMapping("/districts")
    public OctResponse<Page<LogisticResponse>> getLogisticDistricts(
            @RequestParam(name = "provinceId") Integer provinceId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<LogisticResponse> logisticResponses = logisticService.getLogisticByDistricts(provinceId, pageable);
        Long totalElement = logisticResponses.getTotalElements();
        log.info("Get district and logistics successfully!");
        return OctResponse.build(logisticResponses, LogisticeEnum.LOGISTIC_DISTRICT_SUCCESS.getMessage(), totalElement.intValue());
    }

    @GetMapping("/communes")
    public OctResponse<Page<LogisticResponse>> getLogisticSubDistricts(
            @RequestParam(name = "districtId") Integer districtId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<LogisticResponse> logisticResponses = logisticService.getLogisticBySubDistricts(districtId, pageable);
        Long totalElement = logisticResponses.getTotalElements();
        log.info("Get Sub District and logistics successfully!");
        return OctResponse.build(logisticResponses, LogisticeEnum.LOGISTIC_SUBDISTRICT_SUCCESS.getMessage(), totalElement.intValue());
    }

    @GetMapping("/export")
    public void exportLogistic(HttpServletResponse response) {
        logisticService.exportLogisticToExcel(response);
    }

}
