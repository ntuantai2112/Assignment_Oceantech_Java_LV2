package com.octl2.api.service;

import com.octl2.api.dto.response.LogisticResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


public interface LogisticService {

    List<LogisticResponse> getLogisticByProvinceId(Integer provinceId);

    List<LogisticResponse> getLogisticByProvinceName(String provinceName);

    Page<LogisticResponse> getLogisticByProvincesPage(Pageable pageable);

    Page<LogisticResponse> findLogisticByProvince(int levelMapping,Long provinceId,Pageable pageable);
    Page<LogisticResponse> findLogisticByProvince(int levelMapping,Pageable pageable);

    List<LogisticResponse> getLogisticByProvinces();

    Page<LogisticResponse> getLogisticByDistricts(Integer provinceId, Pageable pageable);

    Page<LogisticResponse> getLogisticBySubDistricts(Integer districtId, Pageable pageable);

     ByteArrayResource exportLogisticToExcel(int levelMapping);
}
