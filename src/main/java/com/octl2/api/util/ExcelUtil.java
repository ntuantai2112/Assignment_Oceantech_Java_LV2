package com.octl2.api.util;

import com.octl2.api.commons.exception.ErrorMessages;
import com.octl2.api.commons.exception.OctEntityNotFoundException;
import com.octl2.api.commons.suberror.ApiMessageError;
import com.octl2.api.dto.response.LogisticResponse;
import com.octl2.api.entity.District;
import com.octl2.api.entity.Province;
import com.octl2.api.helper.enums.LogisticeEnum;
import com.octl2.api.repository.*;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ExcelUtil {

    DefaultDeliveryRepository defaultDeliveryRepo;
    ProvinceRepository provinceRepo;
    DistrictRepository districtRepo;
    SubDistrictRepository subDistrictRepo;
    WarehouseRepository warehouseRepo;
    PartnerRepository partnerRepo;

    public byte[] exportToExcel(List<LogisticResponse> logisticsData, int levelMapping) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Logistics");

        // Định dạng tiêu đề
        CellStyle headerStyle = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        headerStyle.setFont(font);

        // Tạo tiêu đề
        Row headerRow = sheet.createRow(0);
        List<String> header = getHeadersByLevel(levelMapping);
        for (int i = 0; i < header.size(); i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(header.get(i));
            cell.setCellStyle(headerStyle);
        }

        // Ghi dữ liệu
        int rowIndex = 1;
        for (LogisticResponse data : logisticsData) {
            Row row = sheet.createRow(rowIndex++);
            int colIdx = 0;

            Province province = provinceRepo.findById(data.getProvince().getId())
                    .orElseThrow(() -> new OctEntityNotFoundException(
                            ErrorMessages.NOT_FOUND,
                            new ApiMessageError(LogisticeEnum.PROVINCE_NOT_FOUND.getMessage())
                    ));

            row.createCell(colIdx++).setCellValue(province.getId());
            row.createCell(colIdx++).setCellValue(province.getName());
            row.createCell(colIdx++).setCellValue(province.getCode());
            if (levelMapping > 1) {
                District district = districtRepo.findById(data.getDistrict().getId())
                        .orElseThrow(() -> new OctEntityNotFoundException(
                                ErrorMessages.NOT_FOUND,
                                new ApiMessageError(LogisticeEnum.DISTRICT_NOT_FOUND.getMessage())
                        ));
                row.createCell(colIdx++).setCellValue(district.getId());
                row.createCell(colIdx++).setCellValue(district.getName());
                row.createCell(colIdx++).setCellValue(district.getCode());
            }
//            if (levelMapping == 3){
//                SubDistrict subDistrict = subDistrictRepo.findById(data.getDistrict().get)
//                        .orElseThrow(() -> new OctEntityNotFoundException(
//                                ErrorMessages.NOT_FOUND,
//                                new ApiMessageError(LogisticeEnum.DISTRICT_NOT_FOUND.getMessage())
//                        ));
//                row.createCell(colIdx++).setCellValue(district.getId());
//                row.createCell(colIdx++).setCellValue(district.getName());
//                row.createCell(colIdx++).setCellValue(district.getCode());
//                row.createCell(colIdx++).setCellValue(data.getSubdistrictName());
//            }
//
//            row.createCell(colIdx++).setCellValue(data.getFfmName());
//            row.createCell(colIdx++).setCellValue(data.getLmName());
//            row.createCell(colIdx++).setCellValue(data.getWhName());
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        return outputStream.toByteArray();



    }


    private List<String> getHeadersByLevel(Integer levelMapping) {
        List<String> headers = new ArrayList<>();
        headers.add("Province Id");
        headers.add("Province Name");
        headers.add("Province Code");

        if (levelMapping > 1) {
            headers.add("District Id");
            headers.add("District Name");
            headers.add("District Code");
        }

        if (levelMapping == 3) {
            headers.add("SubDistrict Id");
            headers.add("SubDistrict Name");
            headers.add("SubDistrict Code");
        }

        headers.add("Fulfilment Name");
        headers.add("Lastmile  Name");
        headers.add("Warehouse  Name");

        return headers;
    }


}
