package com.octl2.api.service.impl;

import com.octl2.api.commons.exception.ErrorMessages;
import com.octl2.api.commons.exception.OctEntityNotFoundException;
import com.octl2.api.commons.exception.OctException;
import com.octl2.api.commons.suberror.ApiMessageError;
import com.octl2.api.config.MappingLevel;
import com.octl2.api.dto.LogisticDTO;
import com.octl2.api.dto.ProvinceDTO;
import com.octl2.api.dto.response.*;
import com.octl2.api.entity.*;
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
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;
import java.util.function.Function;
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

        LogisticPreloadData preloadData = preloadData(results.getContent(), levelMapping);
        List<LogisticResponse> logisticResponses = results.getContent()
                .stream().map(dto -> mapToLogisticResponse(dto, levelMapping, preloadData))
                .collect(Collectors.toList());
        return new PageImpl<>(logisticResponses, pageable, results.getTotalElements());
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

        Map<Long, List<LogisticDTO>> groupedByDistrict = results.stream()
                .collect(Collectors.groupingBy(LogisticDTO::getDistrictId));
        List<DistrictResponse> districtResponses = groupedByDistrict.entrySet().stream()
                .map(entry -> getLocationResponse(entry.getKey(), entry.getValue(), DistrictResponse.class))
                .collect(Collectors.toList());
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
            LogisticPreloadData preloadData = preloadData(logisticData, levelMapping);
            List<LogisticResponse> logisticsResponse = logisticData.stream()
                    .map(dto -> mapToLogisticResponse(dto, levelMapping, preloadData))
                    .collect(Collectors.toList());

            BaseExport<LogisticResponse> baseExport = new BaseExport<>(logisticsResponse);
            baseExport.writeHeaderLine(levelMapping);

            String[] fields = baseExport.buildExportFields(levelMapping);
            baseExport.writeDataLines(fields, LogisticResponse.class);
            baseExport.autoSizeAllColumns();
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

    // hàm Convert giá trị  từ đối tượng ListDTO sang đối tượng Logistic Response theo level Mapping
    private LogisticResponse mapToLogisticResponse(LogisticDTO dto, int levelMapping, LogisticPreloadData preloadData) {
        LogisticResponse logisticResponse = LogisticResponse.builder().build();
        Province province = preloadData.getProvinceMap().get(dto.getProvinceId());
        if (province == null) {
            throw new OctEntityNotFoundException(
                    ErrorMessages.NOT_FOUND,
                    new ApiMessageError(LogisticeEnum.PROVINCE_NOT_FOUND.getMessage()));
        }
        logisticResponse.setProvince(ProvinceResponse.builder()
                .id(province.getId())
                .name(province.getName())
                .code(province.getCode())
                .build()
        );

        if (levelMapping >= 2 && dto.getDistrictId() != null) {

            District district = preloadData.getDistrictMap().get(dto.getDistrictId());
            if (district == null) {
                throw new OctEntityNotFoundException(
                        ErrorMessages.NOT_FOUND,
                        new ApiMessageError(LogisticeEnum.DISTRICT_NOT_FOUND.getMessage()));
            }
            logisticResponse.setDistrict(DistrictResponse.builder()
                    .id(district.getId())
                    .name(district.getName())
                    .code(district.getCode())
                    .build());
        }

        if (levelMapping == 3 && dto.getSubDistrictId() != null) {
            SubDistrict subDistrict = preloadData.getSubDistrictMap().get(dto.getSubDistrictId());
            if (subDistrict == null) {
                throw new OctEntityNotFoundException(
                        ErrorMessages.NOT_FOUND,
                        new ApiMessageError(LogisticeEnum.SUB_DISTRICT_NOT_FOUND.getMessage()));
            }
            logisticResponse.setSubDistrict(SubDistrictResponse.builder()
                    .id(subDistrict.getId())
                    .name(subDistrict.getName())
                    .code(subDistrict.getCode())
                    .build());
        }

        Partner ffm = preloadData.getPartnerMap().get(dto.getFfmId());
        Partner lm = preloadData.getPartnerMap().get(dto.getLmId());
        if (ffm != null) {
            logisticResponse.setFulfilment(PartnerResponse.builder()
                    .id(ffm.getId().intValue())
                    .name(ffm.getName())
                    .sortName(ffm.getShortname())
                    .build());
        }

        if (lm != null) {
            logisticResponse.setLastmile(PartnerResponse.builder()
                    .id(lm.getId().intValue())
                    .name(lm.getName())
                    .sortName(lm.getShortname())
                    .build());
        }
        Warehouse wh = preloadData.getWarehouseMap().get(dto.getWarehouseId());
        preloadData.getWarehouseMap();
        if (wh != null) {
            logisticResponse.setWarehouse(WarehouseResponse.builder()
                    .id(wh.getId().intValue())
                    .name(wh.getWarehouseName())
                    .sortName(wh.getWarehouseShortname())
                    .contact(wh.getContactName())
                    .phone(wh.getContactPhone())
                    .address(wh.getFullAddress())
                    .build());
        }
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

    // Tạo hàm duyệt qua danh sách DTO theo level Mapping trả về LogisticPreloadData
    private LogisticPreloadData preloadData(List<LogisticDTO> dtoList, int levelMapping) {
        Set<Long> provinceIds = dtoList.stream()
                .map(LogisticDTO::getProvinceId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Set<Long> districtIds = new HashSet<>();
        Set<Long> subDistrictIds = new HashSet<>();
        if (levelMapping >= 2) {
            districtIds = dtoList.stream()
                    .map(LogisticDTO::getDistrictId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());

        }
        if (levelMapping == 3) {
            subDistrictIds = dtoList.stream()
                    .map(LogisticDTO::getSubDistrictId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
        }

        Set<Long> fulfilmentIds = dtoList.stream()
                .map(LogisticDTO::getFfmId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Set<Long> lastmileIds = dtoList.stream()
                .map(LogisticDTO::getLmId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Set<Long> wareHouseIds = dtoList.stream()
                .map(LogisticDTO::getWarehouseId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());


        Map<Long, Province> provinceMap = provinceRepo.findAllById(provinceIds)
                .stream()
                .collect(Collectors.toMap(Province::getId, Function.identity()));

        Map<Long, District> districtMap = districtRepo.findAllById(districtIds)
                .stream()
                .collect(Collectors.toMap(District::getId, Function.identity()));

        Map<Long, SubDistrict> subDistrictMap = subDistrictRepo.findAllById(subDistrictIds)
                .stream()
                .collect(Collectors.toMap(SubDistrict::getId, Function.identity()));


        Set<Long> allPartner = new HashSet<>(fulfilmentIds);
        allPartner.addAll(lastmileIds);

        Map<Long, Partner> partnerMap = partnerRepo.findAllById(allPartner)
                .stream()
                .collect(Collectors.toMap(Partner::getId, Function.identity()));

        Map<Long, Warehouse> warehouseMap = warehouseRepo.findAllById(wareHouseIds)
                .stream()
                .collect(Collectors.toMap(Warehouse::getId, Function.identity()));


        return LogisticPreloadData.builder()
                .provinceMap(provinceMap)
                .districtMap(districtMap)
                .subDistrictMap(subDistrictMap)
                .partnerMap(partnerMap)
                .warehouseMap(warehouseMap)
                .build();
    }

}
