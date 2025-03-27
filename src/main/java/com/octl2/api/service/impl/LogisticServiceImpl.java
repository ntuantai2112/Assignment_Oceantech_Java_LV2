package com.octl2.api.service.impl;

import com.octl2.api.commons.exception.ErrorMessages;
import com.octl2.api.commons.exception.OctEntityNotFoundException;
import com.octl2.api.commons.suberror.ApiMessageError;
import com.octl2.api.dto.LogisticDTO;
import com.octl2.api.dto.response.*;
import com.octl2.api.entity.District;
import com.octl2.api.entity.Partner;
import com.octl2.api.entity.Province;
import com.octl2.api.entity.Warehouse;
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
    WarehouseRepository warehouseRepo;
    PartnerRepository partnerRepo;

    @Override
    public List<LogisticResponse> getLogisticByProvince(Integer provinceId) {
        List<LogisticDTO> results = defaultDeliveryRepo.findLogisticsByProvince(provinceId);
        if (results.isEmpty()) {
            throw new OctEntityNotFoundException(
                    ErrorMessages.NOT_FOUND,
                    new ApiMessageError(LogisticeEnum.LOGISTICS_NOT_FOUND.getMessage())
            );
        }
        return getLogisticResponsesWithProvince(results);
    }

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

    @Override
    public Page<LogisticResponse> getLogisticByDistricts(Long provinceId, Pageable pageable) {

        Page<LogisticDTO> results = defaultDeliveryRepo.getLogisticsByDistricts(provinceId, pageable);

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
        return Collections.singletonList(LogisticResponse.builder()
                .province(provinceResponse)
                .fulfilments(ffmList)
                .lastmiles(lmList)
                .warehouses(warehouses)
                .build());
    }

    // Hàm Lấy ra giá trị Province từ danh sách LogisticDTO
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

    private ProvinceResponse getProvinceResponse(Long provinceId) {
        return provinceRepo.findById(provinceId)
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


    // Hàm Lấy ra giá trị DistrictRespose từ
    private DistrictResponse getDistrictResponse(List<LogisticDTO> results) {
        DistrictResponse districtResponse;
        districtResponse = districtRepo.findById(results.get(0).getDistrictId())
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


    // Chức năng Convert List LogisticDTO -> DistrictReponse
    private DistrictResponse getDistrictResponse(Long districtId, List<LogisticDTO> results) {
        District district = districtRepo.findById(districtId)
                .orElseThrow(() -> new OctEntityNotFoundException(
                        ErrorMessages.NOT_FOUND,
                        new ApiMessageError(LogisticeEnum.DISTRICT_NOT_FOUND.getMessage())
                ));
        return DistrictResponse.builder()
                .id(district.getId())
                .name(district.getName())
                .code(district.getCode())
                .fulfilments(extractPartners(results, PartnerType.FFM))
                .lastmiles(extractPartners(results, PartnerType.LM))
                .warehouses(extractWarehouses(results))
                .build();
    }

}
