package com.octl2.api.service.impl;

import com.octl2.api.commons.exception.ErrorMessages;
import com.octl2.api.commons.exception.OctEntityNotFoundException;
import com.octl2.api.commons.suberror.ApiMessageError;
import com.octl2.api.dto.LogisticDTO;
import com.octl2.api.dto.ProvinceDTO;
import com.octl2.api.dto.response.*;
import com.octl2.api.entity.*;
import com.octl2.api.helper.enums.LogisticeEnum;
import com.octl2.api.helper.enums.PartnerType;
import com.octl2.api.repository.*;
import com.octl2.api.service.LogisticService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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

    // Chức năn lấy thông tin Province và Logistice theo ProvinceId
    @Override
    public List<LogisticResponse> getLogisticByProvinceId(Integer provinceId) {
        validateData(provinceId);
        List<LogisticDTO> results = defaultDeliveryRepo.findLogisticsByProvince(provinceId);
        if (results.isEmpty()) {
            throw new OctEntityNotFoundException(
                    ErrorMessages.NOT_FOUND,
                    new ApiMessageError(LogisticeEnum.LOGISTICS_NOT_FOUND.getMessage())
            );
        }
        return getLogisticResponsesWithProvince(results);
    }

    // Chức năn lấy thông tin Province và Logistice theo Province Name
    @Override
    public List<LogisticResponse> getLogisticByProvinceName(String provinceName) {
        if (provinceName.isEmpty()) {
            throw new OctEntityNotFoundException(
                    ErrorMessages.BAD_REQUEST,
                    new ApiMessageError(LogisticeEnum.PROVINCE_NAME_NOT_NULL.getMessage())
            );
        }

        List<LogisticDTO> results = defaultDeliveryRepo.findLogisticsByProvinceName(provinceName);
        if (results.isEmpty()) {
            throw new OctEntityNotFoundException(
                    ErrorMessages.NOT_FOUND,
                    new ApiMessageError(LogisticeEnum.LOGISTICS_NOT_FOUND.getMessage())
            );
        }
        return getLogisticResponsesWithProvince(results);
    }

    // Chức năn lấy thông tin Province và Logistice
    @Override
    public List<LogisticResponse> getLogisticByProvinces() {
        List<LogisticDTO> results = defaultDeliveryRepo.findLogisticsByProvinces();
        if (results.isEmpty()) {
            throw new OctEntityNotFoundException(
                    ErrorMessages.NOT_FOUND,
                    new ApiMessageError(LogisticeEnum.LOGISTICS_NOT_FOUND.getMessage())
            );
        }
        return getLogisticResponsesWithProvince(results);
    }


    // Hàm lấy ra danh sách các Province và Logistic có phân trang
    @Override
    public Page<LogisticResponse> getLogisticByProvincesPage(Pageable pageable) {
        Page<LogisticDTO> results = defaultDeliveryRepo.getLogisticsByProvinces(pageable);
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
        validateData(provinceId);
        Page<LogisticDTO> results = defaultDeliveryRepo.getLogisticsByDistricts(provinceId.longValue(), pageable);

        if (results.isEmpty()) {
            throw new OctEntityNotFoundException(
                    ErrorMessages.NOT_FOUND,
                    new ApiMessageError(LogisticeEnum.LOGISTICS_NOT_FOUND.getMessage())
            );
        }
        ProvinceResponse provinceResponse = getProvinceResponse(provinceId);
        //Nhóm thông tin
        Map<Long, List<LogisticDTO>> groupedByDistrict = results.stream()
                .collect(Collectors.groupingBy(LogisticDTO::getDistrictId));

        List<DistrictResponse> districtResponses = groupedByDistrict.entrySet().stream()
                .map(entry -> getDistrictResponse(entry.getKey(), entry.getValue())
                )
                .collect(Collectors.toList());
        provinceResponse.setDistricts(districtResponses);
        return results.map(dto -> LogisticResponse.builder()
                .province(provinceResponse)
                .build());
    }

    // Lấy ra danh sách SubDistricts và Logistic theo DistrictId
    @Override
    public Page<LogisticResponse> getLogisticBySubDistricts(Integer districtId, Pageable pageable) {

        validateData(districtId);
        Page<LogisticDTO> results = defaultDeliveryRepo.getLogisticsBySubDistricts(districtId.longValue(), pageable);
        if (results.isEmpty()) {
            throw new OctEntityNotFoundException(
                    ErrorMessages.NOT_FOUND,
                    new ApiMessageError(LogisticeEnum.LOGISTICS_NOT_FOUND.getMessage())
            );
        }
        ProvinceResponse provinceResponse = convertProvinceDTO(districtId);
        DistrictResponse districtResponse = getDistrictResponse(districtId);
        Map<Long, List<LogisticDTO>> groupBySubdistrictId = results.stream()
                .collect(Collectors.groupingBy(LogisticDTO::getSubDistrictId));

        List<SubDistrictResponse> subDistrictList = groupBySubdistrictId.entrySet().stream()
                .map(entry -> getSubDistrictResponse(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
        districtResponse.setSubDistricts(subDistrictList);

        return results.map(response -> LogisticResponse.builder()
                .province(provinceResponse)
                .district(districtResponse)
                .build());
    }

    // Hàm Validate provinceId
    private void validateData(Integer input) {
        if (input == null || Objects.isNull(input)) {
            throw new OctEntityNotFoundException(
                    ErrorMessages.MISSING_REQUIRED_FIELD,
                    new ApiMessageError(LogisticeEnum.PROVINCE_ID_NOT_NULL.getMessage())
            );
        }

        if (input <= 0) {
            throw new OctEntityNotFoundException(
                    ErrorMessages.INVALID_VALUE,
                    new ApiMessageError(LogisticeEnum.LOGISTICS_ID_INVALID.getMessage())
            );
        }

    }

    // Hàm Convert giá trị từ ProvinceDTO -> Response
    private LogisticResponse convertToProvinceResponse(LogisticDTO dto) {
        return getLogisticResponsesWithProvince(Collections.singletonList(dto)).get(0);
    }


    // Hàm trả về danh sách LogisticResponse
    private List<LogisticResponse> getLogisticResponsesWithProvince(List<LogisticDTO> results) {
        ProvinceResponse provinceResponse = getProvinceResponse(results);
        List<PartnerResponse> ffmList = extractPartners(results, PartnerType.FFM);
        List<PartnerResponse> lmList = extractPartners(results, PartnerType.LM);
        List<WarehouseResponse> warehouses = extractWarehouses(results);
        LogisticData logisticData = getLogisticData(ffmList, lmList, warehouses);
        return Collections.singletonList(LogisticResponse.builder()
                .province(provinceResponse)
                .logistics(logisticData)
                .build());
    }

    private LogisticData getLogisticData(List<PartnerResponse> ffmList, List<PartnerResponse> lmList,
                                         List<WarehouseResponse> warehouses) {
        return LogisticData.builder()
                .fulfilments(ffmList)
                .lastmiles(lmList)
                .warehouses(warehouses)
                .build();
    }

    // Hàm Lấy ra giá trị ProvinceResponse từ danh sách LogisticDTO
    private ProvinceResponse getProvinceResponse(List<LogisticDTO> results) {
        ProvinceResponse provinceResponse;
        provinceResponse = provinceRepo.findById(results.get(0).getProvinceId())
                .map(province -> ProvinceResponse.builder()
                        .id(province.getId())
                        .name(province.getName())
                        .code(province.getCode())
                        .build()
                ).orElseThrow(() -> new OctEntityNotFoundException(
                        ErrorMessages.NOT_FOUND,
                        new ApiMessageError(LogisticeEnum.LOGISTICS_NOT_FOUND.getMessage())
                ));
        return provinceResponse;
    }

    private ProvinceResponse getProvinceResponse(Integer provinceId) {
        return provinceRepo.findById(provinceId.longValue())
                .map(province -> ProvinceResponse.builder()
                        .id(province.getId())
                        .name(province.getName())
                        .code(province.getCode())
                        .build())
                .orElseThrow(() -> new OctEntityNotFoundException(
                        ErrorMessages.NOT_FOUND,
                        new ApiMessageError(LogisticeEnum.LOGISTICS_NOT_FOUND.getMessage())
                ));
    }

    private DistrictResponse getDistrictResponse(Integer districtId) {
        return districtRepo.findById(districtId.longValue())
                .map(district -> DistrictResponse.builder()
                        .id(district.getId())
                        .name(district.getName())
                        .code(district.getCode())
                        .build())
                .orElseThrow(() -> new OctEntityNotFoundException(
                        ErrorMessages.NOT_FOUND,
                        new ApiMessageError(LogisticeEnum.LOGISTICS_NOT_FOUND.getMessage())
                ));
    }


    //Hàm lấy ra danh sách partner và Convert từ DTO -> Response
    private List<PartnerResponse> extractPartners(List<LogisticDTO> results, PartnerType partnerType) {
        return results.stream()
                .filter(row -> isValidPartnerType(row, partnerType))
                .map(row -> buildPartnerResponse(row, partnerType))
                .collect(Collectors.toList());
    }

    // Chức năng Validate PartnerType
    private boolean isValidPartnerType(LogisticDTO row, PartnerType partnerType) {
        return (partnerType == PartnerType.FFM && row.getFfmId() != null) ||
               (partnerType == PartnerType.LM && row.getLmId() != null);
    }

    // Chức năng lấy ra giá trị PartnerRepository từ LogistictDTO
    private PartnerResponse buildPartnerResponse(LogisticDTO row, PartnerType partnerType) {
        Long partnerId = (partnerType == PartnerType.FFM) ? row.getFfmId() : row.getLmId();
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


    //Hàm lấy ra danh sách warehouse và Convert từ DTO -> Response
    private List<WarehouseResponse> extractWarehouses(List<LogisticDTO> results) {
        return results.stream()
                .filter(row -> row.getWarehouseId() != null)
                .map(row -> buildWarehouseResponse(row))
                .collect(Collectors.toList());
    }

    // Chức năng lấy giá trị Warehouse từ List LogisticDTO
    private WarehouseResponse buildWarehouseResponse(LogisticDTO row) {
        return warehouseRepo.findById(row.getWarehouseId())
                .map(warehouse -> WarehouseResponse.builder()
                        .id(warehouse.getId().intValue())
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

    // Hàm Lấy ra giá trị DistrictResponse từ DistrictEntity
    private DistrictResponse convertDistrictResponse(District districtEntity) {
        DistrictResponse districtResponse;
        districtResponse = districtRepo.findById(districtEntity.getId())
                .map(district -> DistrictResponse.builder()
                        .id(district.getId())
                        .name(district.getName())
                        .code(district.getCode())
                        .build()
                ).orElseThrow(() -> new OctEntityNotFoundException(
                        ErrorMessages.NOT_FOUND,
                        new ApiMessageError(LogisticeEnum.LOGISTICS_NOT_FOUND.getMessage())
                ));
        return districtResponse;
    }

    // Chức năng Convert List LogisticDTO -> DistrictResponse
    private DistrictResponse getDistrictResponse(Long districtId, List<LogisticDTO> results) {
        District district = districtRepo.findById(districtId)
                .orElseThrow(() -> new OctEntityNotFoundException(
                        ErrorMessages.NOT_FOUND,
                        new ApiMessageError(LogisticeEnum.DISTRICT_NOT_FOUND.getMessage())
                ));

//        LogisticData logisticData = getLogisticData(extractPartners(results, PartnerType.FFM), extractPartners(results, PartnerType.LM), extractWarehouses(results));

        return DistrictResponse.builder()
                .district(convertDistrictResponse(district))
                .fulfilments(extractPartners(results, PartnerType.FFM))
                .lastmiles(extractPartners(results, PartnerType.LM))
                .warehouses(extractWarehouses(results))
                .build();
    }


    // Hàm Lấy ra giá trị DistrictResponse từ DistrictEntity
    private SubDistrictResponse convertSubDistrict(SubDistrict subDistrictEntity) {
        SubDistrictResponse subDistrictResponse;
        subDistrictResponse = subDistrictRepo.findById(subDistrictEntity.getId())
                .map(subDistrict -> SubDistrictResponse.builder()
                        .id(subDistrict.getId())
                        .name(subDistrict.getName())
                        .code(subDistrict.getCode())
                        .build()
                ).orElseThrow(() -> new OctEntityNotFoundException(
                        ErrorMessages.NOT_FOUND,
                        new ApiMessageError(LogisticeEnum.LOGISTICS_NOT_FOUND.getMessage())
                ));
        return subDistrictResponse;
    }

    // Chức năng Convert List LogiticDTO -> SubDistrictResponse
    private SubDistrictResponse getSubDistrictResponse(Long subDistrictId, List<LogisticDTO> results) {
        SubDistrict subDistrict = subDistrictRepo.findById(subDistrictId)
                .orElseThrow(() -> new OctEntityNotFoundException(
                        ErrorMessages.NOT_FOUND,
                        new ApiMessageError(LogisticeEnum.DISTRICT_NOT_FOUND.getMessage())
                ));
        return SubDistrictResponse.builder()
                .subDistrict(convertSubDistrict(subDistrict))
                .fulfilments(extractPartners(results, PartnerType.FFM))
                .lastmiles(extractPartners(results, PartnerType.LM))
                .warehouses(extractWarehouses(results))
                .build();
    }


    // Chức năng Convert giá trị ProvinceDTO sang ProvinceResponse;
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
}
