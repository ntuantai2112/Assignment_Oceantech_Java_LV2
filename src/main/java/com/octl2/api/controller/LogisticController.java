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
import org.apache.logging.log4j.message.Message;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.io.IOException;
import java.util.List;

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
    public ResponseEntity<ByteArrayResource> exportLogistic(@RequestParam(value = "levelMapping", defaultValue = "1") @Min(1) @Max(3) int LevelMapping) throws IOException {
        ByteArrayResource file = logisticService.exportLogisticToExcel(LevelMapping);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=logistics.xlsx")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(file);


    }

}
