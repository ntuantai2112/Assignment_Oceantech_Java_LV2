package com.octl2.api.controller;

import com.octl2.api.commons.OctResponse;
import com.octl2.api.commons.exception.ErrorMessage;
import com.octl2.api.commons.exception.ErrorMessages;
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
        List<LogisticResponse> logisticResponses = logisticService.getLogisticByProvinceId(provinceId);
        log.info("Get province and logistics by provinceId successfully!");
        return OctResponse.build(logisticResponses);
    }

    @GetMapping("/province}")
    public OctResponse<List<LogisticResponse>> getLogisticProvinceById(@RequestParam("provinceId") @Min(1) Integer provinceId) {
        List<LogisticResponse> logisticResponses = logisticService.getLogisticByProvinceId(provinceId);
        log.info("Get province and logistics by provinceId successfully!");
        return OctResponse.build(logisticResponses);
    }

    @GetMapping("/province-by-name")
    public OctResponse<List<LogisticResponse>> getLogisticProvinceName(@RequestParam(name = "name", required = false) String provinceName) {
        List<LogisticResponse> logisticResponses = logisticService.getLogisticByProvinceName(provinceName);
        log.info("Get province and logistics by province name successfully!");
        return OctResponse.build(logisticResponses);
    }


    @GetMapping("/provinces")
    public OctResponse<Page<LogisticResponse>> getLogisticProvincesPaged(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<LogisticResponse> logisticResponses = logisticService.getLogisticByProvincesPage(pageable);
        log.info("Get provinces and logistics successfully!");
        return OctResponse.build(logisticResponses, LogisticeEnum.LOGISTIC_PROVINCE_SUCCESS.getMessage(), pageable.getPageSize());
    }


    @GetMapping("/provinces-list")
    public OctResponse<List<LogisticResponse>> getLogisticProvinces() {
        List<LogisticResponse> logisticResponses = logisticService.getLogisticByProvinces();
        return OctResponse.build(logisticResponses);
    }

    @GetMapping("/districts")
    public OctResponse<Page<LogisticResponse>> getLogisticDistricts(
            @RequestParam(name = "provinceId") Integer provinceId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<LogisticResponse> logisticResponses = logisticService.getLogisticByDistricts(provinceId, pageable);
        log.info("Get district and logistics successfully!");
        return OctResponse.build(logisticResponses, LogisticeEnum.LOGISTIC_DISTRICT_SUCCESS.getMessage(), pageable.getPageSize());
    }

    @GetMapping("/communes")
    public OctResponse<Page<LogisticResponse>> getLogisticSubDistricts(
            @RequestParam(name = "districtId") Integer districtId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<LogisticResponse> logisticResponses = logisticService.getLogisticBySubDistricts(districtId, pageable);
        log.info("Get Sub District and logistics successfully!");
        return OctResponse.build(logisticResponses, LogisticeEnum.LOGISTIC_SUBDISTRICT_SUCCESS.getMessage(), pageable.getPageSize());
    }

}
