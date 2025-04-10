package com.octl2.api.service.impl;

import com.octl2.api.commons.exception.ErrorMessages;
import com.octl2.api.commons.exception.OctEntityNotFoundException;
import com.octl2.api.commons.exception.OctException;
import com.octl2.api.commons.suberror.ApiMessageError;
import com.octl2.api.config.MappingLevel;
import com.octl2.api.dto.LogisticDTO;
import com.octl2.api.dto.ProvinceDTO;
import com.octl2.api.dto.response.*;
import com.octl2.api.entity.District;
import com.octl2.api.entity.Partner;
import com.octl2.api.entity.Province;
import com.octl2.api.entity.SubDistrict;
import com.octl2.api.helper.enums.LogisticeEnum;
import com.octl2.api.helper.enums.PartnerType;
import com.octl2.api.repository.*;
import com.octl2.api.service.LogisticService;
import com.octl2.api.util.excel.BaseExport;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@Transactional
public class LogisticServiceImpl implements LogisticService {

    DefaultDeliveryRepository defaultDeliveryRepo;
    ProvinceRepository provinceRepo;
    DistrictRepository districtRepo;
    SubDistrictRepository subDistrictRepo;
    WarehouseRepository warehouseRepo;
    PartnerRepository partnerRepo;
    MappingLevel mappingLevel;


    // Hàm lấy ra danh sách các Province và Logistic có phân trang
    @Override
    public Page<LogisticResponse> findLogisticByProvinces(Pageable pageable) {
        int levelMapping = getLevelMapping();
        Page<LogisticDTO> results = defaultDeliveryRepo.findLogisticsByProvinces(levelMapping, pageable);
        validatePage(pageable, results);
        if (results.isEmpty()) {
            throw new OctEntityNotFoundException(
                    ErrorMessages.NOT_FOUND,
                    new ApiMessageError(LogisticeEnum.LOGISTICS_NOT_FOUND.getMessage())
            );
        }
        return results.map(this::convertToProvinceResponse);
    }


    // Lấy ra danh sách Districts và Logistic theo ProvinceId
    @Override
    public Page<LogisticResponse> getLogisticByDistricts(Integer provinceId, Pageable pageable) {
        int levelMapping = getLevelMapping();
        ProvinceResponse provinceResponse = getDataResponseById(provinceId, ProvinceResponse.class);
        Page<LogisticDTO> results = defaultDeliveryRepo.findLogisticsByDistricts(levelMapping, provinceId.longValue(), pageable);
        validatePage(pageable, results);
        if (results.isEmpty()) {
            throw new OctEntityNotFoundException(
                    ErrorMessages.NOT_FOUND,
                    new ApiMessageError(LogisticeEnum.LOGISTICS_NOT_FOUND.getMessage())
            );
        }
        //Nhóm thông tin
        Map<Long, List<LogisticDTO>> groupedByDistrict = results.stream()
                .collect(Collectors.groupingBy(LogisticDTO::getDistrictId));
        List<DistrictResponse> districtResponses = groupedByDistrict.entrySet().stream()
                .map(entry -> getLocationResponse(entry.getKey(), entry.getValue(), DistrictResponse.class))
                .collect(Collectors.toList());
        getLogisticResponsesWithProvince(results.getContent());
        provinceResponse.setDistricts(districtResponses);
        return results.map(dto -> LogisticResponse.builder()
                .province(provinceResponse)
                .build());
    }

    // Lấy ra danh sách SubDistricts và Logistic theo DistrictId
    @Override
    public Page<LogisticResponse> getLogisticBySubDistricts(Integer districtId, Pageable pageable) {
        int levelMapping = getLevelMapping();
        DistrictResponse districtResponse = getDataResponseById(districtId, DistrictResponse.class);
        ProvinceResponse provinceResponse = convertProvinceDTO(districtId);
        Page<LogisticDTO> results = defaultDeliveryRepo.getLogisticsBySubDistricts(levelMapping, districtId.longValue(), pageable);
        if (results.isEmpty()) {
            throw new OctEntityNotFoundException(
                    ErrorMessages.NOT_FOUND,
                    new ApiMessageError(LogisticeEnum.LOGISTICS_NOT_FOUND.getMessage())
            );
        }

        Map<Long, List<LogisticDTO>> groupBySubDistrictId = results.stream()
                .collect(Collectors.groupingBy(LogisticDTO::getSubDistrictId));

        List<SubDistrictResponse> subDistrictList = groupBySubDistrictId.entrySet().stream()
                .map(entry -> getLocationResponse(entry.getKey(), entry.getValue(), SubDistrictResponse.class))
                .collect(Collectors.toList());
        districtResponse.setSubDistricts(subDistrictList);

        return results.map(response -> LogisticResponse.builder()
                .province(provinceResponse)
                .district(districtResponse)
                .build());
    }

    // Hàm lấy ra giá trị Level Mapping từ file Config
    private int getLevelMapping() {
        int levelMapping = mappingLevel.getLevelMapping();

        if (levelMapping < 1 || levelMapping > 3) {
            throw new OctEntityNotFoundException(
                    ErrorMessages.INVALID_VALUE,
                    new ApiMessageError(LogisticeEnum.LOGISTIC_LEVEL_MAPPING.getMessage())
            );
        }
        return levelMapping;
    }

    // Hàm Convert giá trị từ ProvinceDTO -> Response
    private LogisticResponse convertToProvinceResponse(LogisticDTO dto) {
        LogisticResponse response = getLogisticResponsesWithProvince(Collections.singletonList(dto)).get(0);
        return response;
    }


    // Hàm trả về danh sách LogisticResponse - với Province
    private List<LogisticResponse> getLogisticResponsesWithProvince(List<LogisticDTO> results) {
        ProvinceResponse provinceResponse = getLocationResponse(results, ProvinceResponse.class);
        List<PartnerResponse> ffmList = extractPartners(results, PartnerType.FFM);
        List<PartnerResponse> lmList = extractPartners(results, PartnerType.LM);
        List<WarehouseResponse> warehouses = extractWarehouses(results);
        LogisticData logisticData = getLogisticData(ffmList, lmList, warehouses);
        return Collections.singletonList(LogisticResponse.builder()
                .province(provinceResponse)
                .logistics(logisticData)
                .build());
    }

    // Chức năng lấy giá trị LogisticData
    private LogisticData getLogisticData(List<PartnerResponse> ffmList, List<PartnerResponse> lmList,
                                         List<WarehouseResponse> warehouses) {
        return LogisticData.builder()
                .fulfilments(ffmList)
                .lastmiles(lmList)
                .warehouses(warehouses)
                .build();
    }

    // Hàm Lấy ra giá trị ProvinceResponse từ danh sách LogisticDTO
    private <T> T getLocationResponse(List<LogisticDTO> results, Class<T> responseType) {

        if (responseType == ProvinceResponse.class) {
            ProvinceResponse provinceResponse;
            provinceResponse = provinceRepo.findById(results.get(0).getProvinceId())
                    .map(province -> ProvinceResponse.builder()
                            .id(province.getId())
                            .name(province.getName())
                            .code(province.getCode())
                            .build()
                    ).orElseThrow(() -> new OctEntityNotFoundException(
                            ErrorMessages.NOT_FOUND,
                            new ApiMessageError(LogisticeEnum.PROVINCE_NOT_FOUND.getMessage())
                    ));
            return responseType.cast(provinceResponse);
        } else if (responseType == DistrictResponse.class) {
            District district = districtRepo.findById(results.get(0).getDistrictId().longValue())
                    .orElseThrow(() -> new OctEntityNotFoundException(
                            ErrorMessages.NOT_FOUND,
                            new ApiMessageError(LogisticeEnum.DISTRICT_NOT_FOUND.getMessage())
                    ));

            return responseType.cast(DistrictResponse.builder()
                    .id(district.getId())
                    .name(district.getName())
                    .code(district.getCode())
                    .build());

        } else if (responseType == SubDistrictResponse.class) {
            SubDistrict subDistrict = subDistrictRepo.findById((results.get(0).getSubDistrictId().longValue()))
                    .orElseThrow(() -> new OctEntityNotFoundException(
                            ErrorMessages.NOT_FOUND,
                            new ApiMessageError(LogisticeEnum.SUB_DISTRICT_NOT_FOUND.getMessage())
                    ));

            return responseType.cast(SubDistrictResponse.builder()
                    .id(subDistrict.getId())
                    .name(subDistrict.getName())
                    .code(subDistrict.getCode())
                    .build());

        }
        log.error("Unsupported type: {}" + responseType.getSimpleName());
        throw new OctException(ErrorMessages.UNSUPPORTED_TYPE);

    }


    // Chức năng Convert giá trị ProvinceDTO sang ProvinceResponse
    private ProvinceResponse convertProvinceDTO(Integer districtId) {
        ProvinceDTO provinceDTO = provinceRepo.findByDistrictId(districtId.longValue())
                .orElseThrow(() -> new OctEntityNotFoundException(
                        ErrorMessages.NOT_FOUND,
                        new ApiMessageError(LogisticeEnum.PROVINCE_NOT_FOUND.getMessage())
                ));
        return ProvinceResponse.builder()
                .id(provinceDTO.getId())
                .name(provinceDTO.getName())
                .code(provinceDTO.getCode())
                .build();
    }

    // Hàm lấy ra giá trị Response theo locationId truyền vào và trả về đối tượng tương ứng
    private <T> T getDataResponseById(Integer locationId, Class<T> responseType) {
        if ((responseType == ProvinceResponse.class)) {
            Province province = provinceRepo.findById(locationId.longValue())
                    .orElseThrow(() -> new OctEntityNotFoundException(
                            ErrorMessages.NOT_FOUND,
                            new ApiMessageError(LogisticeEnum.PROVINCE_NOT_FOUND.getMessage())
                    ));

            return responseType.cast(ProvinceResponse.builder()
                    .id(province.getId())
                    .name(province.getName())
                    .code(province.getCode())
                    .build());

        } else if (responseType == DistrictResponse.class) {
            District district = districtRepo.findById(locationId.longValue())
                    .orElseThrow(() -> new OctEntityNotFoundException(
                            ErrorMessages.NOT_FOUND,
                            new ApiMessageError(LogisticeEnum.DISTRICT_NOT_FOUND.getMessage())
                    ));

            return responseType.cast(DistrictResponse.builder()
                    .id(district.getId())
                    .name(district.getName())
                    .code(district.getCode())
                    .build());

        } else if (responseType == SubDistrictResponse.class) {
            SubDistrict subDistrict = subDistrictRepo.findById(locationId.longValue())
                    .orElseThrow(() -> new OctEntityNotFoundException(
                            ErrorMessages.NOT_FOUND,
                            new ApiMessageError(LogisticeEnum.SUB_DISTRICT_NOT_FOUND.getMessage())
                    ));

            return responseType.cast(SubDistrictResponse.builder()
                    .id(subDistrict.getId())
                    .name(subDistrict.getName())
                    .code(subDistrict.getCode())
                    .build());

        }
        log.error("Unsupported type: {}" + responseType.getSimpleName());
        throw new OctException(ErrorMessages.UNSUPPORTED_TYPE);

    }

    // Hàm Convert giá trị từ Entity -> Response theo entity truyền vào
    private <T> T convertLocationResponse(Object entity, Class<T> responseType) {
        Long entityId;
        if (entity instanceof District) {
            entityId = ((District) entity).getId();
            return responseType.cast(districtRepo.findById(entityId)
                    .map(district -> DistrictResponse.builder()
                            .id(district.getId())
                            .name(district.getName())
                            .code(district.getCode())
                            .build()
                    ).orElseThrow(() -> new OctEntityNotFoundException(
                            ErrorMessages.NOT_FOUND,
                            new ApiMessageError(LogisticeEnum.DISTRICT_NOT_FOUND.getMessage())
                    )));
        } else if (entity instanceof SubDistrict) {
            entityId = ((SubDistrict) entity).getId();
            return responseType.cast(subDistrictRepo.findById(entityId)
                    .map(subDistrict -> SubDistrictResponse.builder()
                            .id(subDistrict.getId())
                            .name(subDistrict.getName())
                            .code(subDistrict.getCode())
                            .build()
                    ).orElseThrow(() -> new OctEntityNotFoundException(
                            ErrorMessages.NOT_FOUND,
                            new ApiMessageError(LogisticeEnum.SUB_DISTRICT_NOT_FOUND.getMessage())
                    )));
        }
        throw new OctException(ErrorMessages.UNSUPPORTED_TYPE);

    }

    // Chức năng Convert đối tượng từ DTO -> Response theo id và list
    private <T> T getLocationResponse(Long locationId, List<LogisticDTO> results, Class<T> responseType) {

        if (responseType == DistrictResponse.class) {
            District district = districtRepo.findById(locationId)
                    .orElseThrow(() -> new OctEntityNotFoundException(
                            ErrorMessages.NOT_FOUND,
                            new ApiMessageError(LogisticeEnum.DISTRICT_NOT_FOUND.getMessage())
                    ));


            return responseType.cast(DistrictResponse.builder()
                    .district(convertLocationResponse(district, DistrictResponse.class))
                    .fulfilments(removeDuplicatePartners(extractPartners(results, PartnerType.FFM)))
                    .lastmiles(removeDuplicatePartners(extractPartners(results, PartnerType.LM)))
                    .warehouses(removeDuplicateWarehouses(extractWarehouses(results)))
                    .build());

        } else if (responseType == SubDistrictResponse.class) {
            SubDistrict subDistrict = subDistrictRepo.findById(locationId)
                    .orElseThrow(() -> new OctEntityNotFoundException(
                            ErrorMessages.NOT_FOUND,
                            new ApiMessageError(LogisticeEnum.SUB_DISTRICT_NOT_FOUND.getMessage())
                    ));

            return responseType.cast(SubDistrictResponse.builder()
                    .subDistrict(convertLocationResponse(subDistrict, SubDistrictResponse.class))
                    .fulfilments(removeDuplicatePartners(extractPartners(results, PartnerType.FFM)))
                    .lastmiles(removeDuplicatePartners(extractPartners(results, PartnerType.LM)))
                    .warehouses(removeDuplicateWarehouses(extractWarehouses(results)))
                    .build());
        }
        throw new OctException(ErrorMessages.UNSUPPORTED_TYPE);
    }


    // Chức năng Validate PartnerType
    private boolean isValidPartnerType(LogisticDTO row, PartnerType partnerType) {
        return (partnerType == PartnerType.FFM && row.getFfmId() != null) ||
               (partnerType == PartnerType.LM && row.getLmId() != null);
    }

    // Chức năng lấy ra giá trị PartnerRepository từ LogistictDTO
    private PartnerResponse buildPartnerResponse(LogisticDTO row, PartnerType partnerType) {
        Long partnerId = (partnerType == PartnerType.FFM) ? row.getFfmId() : row.getLmId();

        if (partnerId == null) {
            return null;
        }

        Partner partner = partnerRepo.findById(partnerId)
                .orElseThrow(() -> new OctEntityNotFoundException(
                        ErrorMessages.NOT_FOUND,
                        new ApiMessageError("Partner ID " + partnerId + " not found")
                ));
        return PartnerResponse.builder()
                .id(partner.getId().intValue())
                .name(partner.getName())
                .sortName(partner.getShortname())
                .build();
    }

    //Hàm lấy ra danh sách partner và Convert từ DTO -> Response
    private List<PartnerResponse> extractPartners(List<LogisticDTO> results, PartnerType partnerType) {
        return results.stream()
                .filter(partner -> isValidPartnerType(partner, partnerType))
                .map(dto -> buildPartnerResponse(dto, partnerType))
                .collect(Collectors.toList());
    }


    // Chức năng lấy giá trị Warehouse từ List LogisticDTO
    private WarehouseResponse buildWarehouseResponse(LogisticDTO row) {

        if (row.getWarehouseId() == null) {
            return null;
        }

        return warehouseRepo.findById(row.getWarehouseId())
                .map(warehouse -> WarehouseResponse.builder()
                        .id(warehouse.getId().intValue())
                        .name(warehouse.getWarehouseName())
                        .sortName(warehouse.getWarehouseShortname())
                        .contact(warehouse.getContactName())
                        .phone(warehouse.getContactPhone())
                        .address(warehouse.getFullAddress())
                        .build())
                .orElseThrow(() -> new OctEntityNotFoundException(
                        ErrorMessages.NOT_FOUND,
                        new ApiMessageError("Warehouse ID " + row.getWarehouseId() + " not found")
                ));
    }

    //Hàm lấy ra danh sách warehouse và Convert từ DTO -> Response
    private List<WarehouseResponse> extractWarehouses(List<LogisticDTO> results) {
        return results.stream()
                .filter(warehouse -> warehouse.getWarehouseId() != null)
                .map(this::buildWarehouseResponse)
                .collect(Collectors.toList());
    }


    // Chức năng Export Excel
    @Override
    public void exportLogisticToExcel(HttpServletResponse response) {
        try {
            int levelMapping = getLevelMapping();
            List<LogisticDTO> logisticData = getLogisticsByLevel(levelMapping);
            List<LogisticResponse> logisticsResponse = logisticData.stream()
                    .map(dto -> mapToLogisticResponse(dto, levelMapping))
                    .collect(Collectors.toList());

            BaseExport<LogisticResponse> baseExport = new BaseExport<>(logisticsResponse);
            baseExport.writeHeaderLine(levelMapping);

            String[] fields = baseExport.buildExportFields(levelMapping);
            baseExport.writeDataLines(fields, LogisticResponse.class);
            response.setContentType("application/octet-stream");
            response.setHeader("Content-Disposition", "attachment; filename=logistics.xlsx");
            baseExport.export(response);

        } catch (IOException e) {
            log.error("Error Export Excel:{} {}", e.getMessage(), e.getCause());
            throw new OctException(ErrorMessages.BAD_REQUEST);
        }
    }

    // Lấy ra danh sách LogisticDTO theo level Mapping
    public List<LogisticDTO> getLogisticsByLevel(int levelMapping) {
        switch (levelMapping) {
            case 1:
                return defaultDeliveryRepo.findLogisticsByProvince();
            case 2:
                return defaultDeliveryRepo.findLogisticsByDistrict();
            case 3:
                return defaultDeliveryRepo.findLogisticsBySubDistrict();
            default:
                log.error("Invalid levelMapping. Allowed values: 1, 2, 3");
                throw new OctException(ErrorMessages.INVALID_VALUE_LEVEL_MAPPING);
        }
    }

    // Hàm lấy giá trị Response theo Id
    private <T> List<T> getDataResponseByIds(List<Integer> locationIds, Class<T> responseType) {
        List<T> responseList = new ArrayList<>();

        if (responseType == ProvinceResponse.class) {
            List<Province> provinces = provinceRepo.findAllById(locationIds.stream()
                    .map(Integer::longValue)  // Chuyển đổi Integer thành Long
                    .collect(Collectors.toList()));

            provinces.forEach(province -> responseList.add(responseType.cast(ProvinceResponse.builder()
                    .id(province.getId())
                    .name(province.getName())
                    .code(province.getCode())
                    .build())));

        } else if (responseType == DistrictResponse.class) {
            List<District> districts = districtRepo.findAllById(locationIds.stream()
                    .map(Integer::longValue)
                    .collect(Collectors.toList()));

            districts.forEach(district -> {
                responseList.add(responseType.cast(DistrictResponse.builder()
                        .id(district.getId())
                        .name(district.getName())
                        .code(district.getCode())
                        .build()));
            });

        } else if (responseType == SubDistrictResponse.class) {
            List<SubDistrict> subDistricts = subDistrictRepo.findAllById(locationIds.stream()
                    .map(Integer::longValue)
                    .collect(Collectors.toList()));

            subDistricts.forEach(subDistrict -> {
                responseList.add(responseType.cast(SubDistrictResponse.builder()
                        .id(subDistrict.getId())
                        .name(subDistrict.getName())
                        .code(subDistrict.getCode())
                        .build()));
            });
        } else {
            log.error("Unsupported type: {}", responseType.getSimpleName());
            throw new OctException(ErrorMessages.UNSUPPORTED_TYPE);
        }

        return responseList;
    }

    // hàm Convert giá trị từ ListDTO sang Level Response
    private List<LogisticResponse> convertValue(List<LogisticDTO> dtoList, int levelMapping) {
        return dtoList.stream().map(dto ->
        {
            Province province = provinceRepo.findById(dto.getProvinceId()).get();
            ProvinceResponse provinceResponse = ProvinceResponse.builder()
                    .id(province.getId())
                    .name(province.getName())
                    .build();
            DistrictResponse districtResponse = null;
            SubDistrictResponse subDistrictResponse = null;

            if (levelMapping > 1) {
                District district = districtRepo.findById(dto.getDistrictId()).get();
                districtResponse = DistrictResponse.builder()
                        .id(district.getId())
                        .name(district.getName())
                        .build();


            } else if (levelMapping == 3) {
                SubDistrict subDistrict = subDistrictRepo.findById(dto.getSubDistrictId()).get();
                subDistrictResponse = SubDistrictResponse.builder()
                        .id(subDistrict.getId())
                        .name(subDistrict.getName())
                        .build();
            }
            PartnerResponse ffm = buildPartnerResponse(dto, PartnerType.FFM);
            PartnerResponse lm = buildPartnerResponse(dto, PartnerType.LM);
            WarehouseResponse wh = buildWarehouseResponse(dto);

            return LogisticResponse.builder()
                    .province(provinceResponse)
                    .district(districtResponse)
                    .subDistrict(subDistrictResponse)
                    .fulfilment(ffm)
                    .lastmile(lm)
                    .warehouse(wh)
                    .build();

        }).collect(Collectors.toList());

    }

    // hàm Convert giá trị  từ đối tượng ListDTO sang đối tượng Logistic Response theo level Mapping
    private LogisticResponse mapToLogisticResponse(LogisticDTO dto, int levelMapping) {

        LogisticResponse logisticResponse = LogisticResponse.builder().build();
        ProvinceResponse province = getDataResponseById(dto.getProvinceId().intValue(), ProvinceResponse.class);
        logisticResponse.setProvince(province);

        if (levelMapping == 2 && dto.getDistrictId() != null) {
            DistrictResponse district = getDataResponseById(dto.getDistrictId().intValue(), DistrictResponse.class);
            logisticResponse.setDistrict(district);
        }

        if (levelMapping == 3 && dto.getSubDistrictId() != null) {
            SubDistrictResponse subDistrict = getDataResponseById(dto.getSubDistrictId().intValue(), SubDistrictResponse.class);
            logisticResponse.setSubDistrict(subDistrict);
        }

        PartnerResponse ffm = buildPartnerResponse(dto, PartnerType.FFM);
        PartnerResponse lm = buildPartnerResponse(dto, PartnerType.LM);
        WarehouseResponse wh = buildWarehouseResponse(dto);

        logisticResponse.setFulfilment(ffm);
        logisticResponse.setLastmile(lm);
        logisticResponse.setWarehouse(wh);

        return logisticResponse;


    }


    // Hàm trả về danh sách PartnerResponse loại bỏ các phần tử trùng lặp
    private List<PartnerResponse> removeDuplicatePartners(List<PartnerResponse> partners) {
        // Dùng Map để giữ lại duy nhất mỗi Partner theo ID
        Map<Long, PartnerResponse> partnerMap = new LinkedHashMap<>();
        for (PartnerResponse partner : partners) {
            partnerMap.putIfAbsent(partner.getId().longValue(), partner); // Nếu ID chưa có thì mới thêm
        }
        return new ArrayList<>(partnerMap.values());
    }

    // Hàm trả về danh sách WarehouseResponse loại bỏ các phần tử trùng lặp
    private List<WarehouseResponse> removeDuplicateWarehouses(List<WarehouseResponse> warehouses) {
        Map<Long, WarehouseResponse> warehouseMap = new LinkedHashMap<>();
        for (WarehouseResponse wh : warehouses) {
            warehouseMap.putIfAbsent(wh.getId().longValue(), wh); // Giữ bản ghi đầu tiên theo ID
        }
        return new ArrayList<>(warehouseMap.values());
    }

    // Chức năng Validate page
    private void validatePage(Pageable pageable, Page<LogisticDTO> results) {
        // Kiểm tra page nhập vào có vượt quá tổng số page không
        if (pageable.getPageNumber() > results.getTotalPages() - 1 && results.getTotalPages() > 0) {
            throw new OctException(
                    ErrorMessages.INVALID_PAGE_NUMBER);
        }
    }


}
