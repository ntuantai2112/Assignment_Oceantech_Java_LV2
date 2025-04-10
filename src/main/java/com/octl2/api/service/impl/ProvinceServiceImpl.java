package com.octl2.api.service.impl;

import com.octl2.api.commons.exception.ErrorMessages;
import com.octl2.api.commons.exception.OctException;
import com.octl2.api.config.MappingLevel;
import com.octl2.api.dto.ProvinceDTO;
import com.octl2.api.entity.Province;
import com.octl2.api.repository.ProvinceRepository;
import com.octl2.api.service.ProvinceService;
import com.octl2.api.util.excel.BaseExport;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@Service
@Transactional
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Slf4j
public class ProvinceServiceImpl implements ProvinceService {

    ProvinceRepository provinceRepo;

    MappingLevel mappingLevel;


    @Override
    public ProvinceDTO getBybId(long id) {
        return provinceRepo.getDtoById(id);
    }

    @Override
    public void ExportExcel(HttpServletResponse response) {
        int levelMapping = mappingLevel.getLevelMapping();
        List<Province> provinces = provinceRepo.findAll();
        BaseExport<Province> baseExport = new BaseExport<Province>(provinces);
        baseExport.writeHeaderLine(levelMapping);
        baseExport.writeDataLines(new String[]{"id", "name", "code",
        }, Province.class);

        try {
            response.setContentType("application/octet-stream");
            response.setHeader("Content-Disposition", "attachment; filename=provinces.xlsx");
            baseExport.export(response);
        } catch (IOException e) {
            log.error("Error Export Excel Province:{} {}", e.getMessage(), e.getCause());
            throw new OctException(ErrorMessages.BAD_REQUEST);
        }

    }
}
