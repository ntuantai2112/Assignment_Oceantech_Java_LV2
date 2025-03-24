package com.octl2.api.controller;

import com.octl2.api.commons.OctResponse;
import com.octl2.api.dto.response.LogisticResponse;
import com.octl2.api.service.LogisticService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
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

}
