package com.octl2.api.service;

import com.octl2.api.dto.ProvinceDTO;

import javax.servlet.http.HttpServletResponse;

public interface ProvinceService {

    ProvinceDTO getBybId(long id);

    void ExportExcel(HttpServletResponse response);
}
