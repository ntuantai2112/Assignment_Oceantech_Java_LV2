package com.octl2.api.service;

import com.octl2.api.dto.response.LogisticResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import javax.servlet.http.HttpServletResponse;


public interface LogisticService {

    // Lấy tất cả các Province và Logistic
    Page<LogisticResponse> findLogisticByProvinces(Pageable pageable);



    Page<LogisticResponse> getLogisticByDistricts(Integer provinceId, Pageable pageable);

    Page<LogisticResponse> getLogisticBySubDistricts(Integer districtId, Pageable pageable);

//     ByteArrayResource exportLogisticToExcel();

     void exportLogisticToExcel(HttpServletResponse response);
}
