package com.octl2.api.service.impl;

import com.octl2.api.commons.exception.ErrorMessages;
import com.octl2.api.commons.exception.OctEntityNotFoundException;
import com.octl2.api.commons.suberror.ApiMessageError;
import com.octl2.api.dto.response.*;
import com.octl2.api.entity.Partner;
import com.octl2.api.entity.Warehouse;
import com.octl2.api.helper.enums.LogisticeEnum;
import com.octl2.api.helper.enums.PartnerType;
import com.octl2.api.repository.DefaultDeliveryRepository;
import com.octl2.api.repository.PartnerRepository;
import com.octl2.api.repository.ProvinceRepository;
import com.octl2.api.repository.WarehouseRepository;
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
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@Transactional
public class LogisticServiceImpl implements LogisticService {

    DefaultDeliveryRepository defaultDeliveryRepo;
    ProvinceRepository provinceRepo;
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
        return getLogisticResponses(results);
    }

    @Override
    public List<LogisticResponse> getLogisticByProvinceName(String provinceName) {
        if (provinceName.isEmpty() || provinceName == null) {
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
        return getLogisticResponses(results);
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
        return getLogisticResponses(results);
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

        return results.map(this::convertToLogisticResponse);
    }



    @Override
    public Page<LogisticResponse> getLogisticByDistricts(Pageable pageable) {
        return null;
    }


    // Hàm Convert giá trị từ LogisticDTO -> Response
    private LogisticResponse convertToLogisticResponse(LogisticDTO dto) {
        return getLogisticResponses(Collections.singletonList(dto)).get(0);
    }

    // Hàm trả về danh sách LogisticResponse
    private List<LogisticResponse> getLogisticResponses(List<LogisticDTO> results) {
        ProvinceResponse provinceResponse = getProvinceResponse(results);
        List<PartnerResponse> ffmList = extractPartners(results, PartnerType.FFM);
        List<PartnerResponse> lmList = extractPartners(results, PartnerType.LM);
        List<WarehouseResponse> warehouses = extractWarehouses(results);
        return Collections.singletonList(new LogisticResponse(provinceResponse, ffmList, lmList, warehouses));
    }

    // Hàm Lấy ra giá trị Province
    private ProvinceResponse getProvinceResponse(List<LogisticDTO> results) {
        ProvinceResponse provinceResponse = provinceRepo.findById(results.get(0).getProvinceId())
                .map(province -> new ProvinceResponse(province.getId(), province.getName(), province.getCode()))
                .orElseThrow(() -> new OctEntityNotFoundException(
                        ErrorMessages.NOT_FOUND,
                        new ApiMessageError(LogisticeEnum.LOGISTICS_NOT_FOUND.getMessage())
                ));
        return provinceResponse;
    }

    //Hàm lấy ra danh sách partner và Convert từ DTO -> Response
    private List<PartnerResponse> extractPartners(List<LogisticDTO> results, PartnerType partnerType) {
        return results.stream()
                .filter(row ->
                        (partnerType == PartnerType.FFM && row.getFfmId() != null) || (partnerType == PartnerType.LM && row.getLmId() != null))
                .map(row -> {
                    Long partnerId = (partnerType == PartnerType.FFM) ? row.getFfmId().longValue() : row.getLmId().longValue();
                    Partner ffm = partnerRepo.findById(partnerId)
                            .orElseThrow(() -> new OctEntityNotFoundException(
                                    ErrorMessages.NOT_FOUND,
                                    new ApiMessageError("Partner ID "
                                            + row.getWarehouseId() + " not found")
                            ));
                    return new PartnerResponse(ffm.getId().intValue(), ffm.getName(), ffm.getShortname());

                })
                .collect(Collectors.toList());

    }


    //Hàm lấy ra danh sách warehouse và Convert từ DTO -> Response
    private List<WarehouseResponse> extractWarehouses(List<LogisticDTO> results) {

        List<WarehouseResponse> responses = results.stream()
                .filter(row -> row.getWarehouseId() != null)
                .map(row -> {
                    Long warehouseId = row.getWarehouseId().longValue();
                    Warehouse warehouse = warehouseRepo.findById(warehouseId)
                            .orElseThrow(() -> new OctEntityNotFoundException(
                                    ErrorMessages.NOT_FOUND,
                                    new ApiMessageError("Warehouse ID " + row.getWarehouseId() + " not found")
                            ));

                    return new WarehouseResponse(warehouse.getId().intValue(), warehouse.getWarehouseName(), warehouse.getWarehouseShortname(), warehouse.getContactName(), warehouse.getContactPhone(), warehouse.getFullAddress());
                })
                .collect(Collectors.toList());

        return responses;
    }


}
