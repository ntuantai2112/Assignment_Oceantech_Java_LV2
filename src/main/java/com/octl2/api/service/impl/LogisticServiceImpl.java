package com.octl2.api.service.impl;


import com.octl2.api.commons.exception.ErrorMessages;
import com.octl2.api.commons.exception.OctEntityNotFoundException;
import com.octl2.api.commons.exception.OctException;
import com.octl2.api.commons.suberror.ApiMessageError;
import com.octl2.api.dto.response.*;
import com.octl2.api.entity.Province;
import com.octl2.api.entity.Warehouse;
import com.octl2.api.repository.DefaultDeliveryRepository;
import com.octl2.api.repository.PartnerRepository;
import com.octl2.api.repository.ProvinceRepository;
import com.octl2.api.repository.WarehouseRepository;
import com.octl2.api.service.LogisticService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.Tuple;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
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
                    new ApiMessageError("No logistic data found for this province")
            );
        }
        ProvinceResponse provinceResponse = getProvinceResponse(results);

        List<PartnerResponse> ffmList = results.stream()
                .filter(row -> row.getFfmId() != null)
                .map(row -> new PartnerResponse(row.getFfmId(), row.getFfmName()))
                .collect(Collectors.toList());

        List<PartnerResponse> lmList = results.stream()
                .filter(row -> row.getLmId() != null)
                .map(row -> new PartnerResponse(row.getLmId(), row.getLmName()))
                .collect(Collectors.toList());

//        List<WarehouseResponse> warehouses = results.stream()
//                .filter(row -> row.getWarehouseId() != null)
//                .map(row -> new WarehouseResponse(row.getWarehouseId().intValue(), row.getWarehouseName()))
//                .collect(Collectors.toList());


        List<WarehouseResponse> warehouses = extractWarehouses(results);

        return Collections.singletonList(new LogisticResponse(provinceResponse, ffmList, lmList, warehouses));
    }


    private ProvinceResponse getProvinceResponse(List<LogisticDTO> results) {
        ProvinceResponse provinceResponse = provinceRepo.findById(Long.valueOf(results.get(0).getProvinceId()))
                .map(province -> new ProvinceResponse(province.getId(), province.getName(), province.getCode()))
                .orElseThrow(() -> new OctEntityNotFoundException(
                        ErrorMessages.NOT_FOUND,
                        new ApiMessageError("No logistic data found for this province")
                ));

        return provinceResponse;
    }


    private List<WarehouseResponse> extractWarehouses(List<LogisticDTO> results) {
        return results.stream()
                .filter(row -> row.getWarehouseId() != null)
                .map(row -> {
//                    Long warehouseId = row.getWarehouseId().longValue();

                    Long warehouseId = 84347L;
                    System.out.println(warehouseId);
                    Warehouse warehouse = warehouseRepo.findById(warehouseId)
                            .orElseThrow(() -> new OctEntityNotFoundException(
                                    ErrorMessages.NOT_FOUND,
                                    new ApiMessageError("Warehouse ID " + row.getWarehouseId() + " not found")
                            ));

                    // Gán giá trị từ entity sang response
                    return new WarehouseResponse(warehouse.getId().intValue(), warehouse.getWarehouseName(), warehouse.getWarehouseShortname(), warehouse.getContactName(), warehouse.getContactPhone(), warehouse.getFullAddress());
                })
                .collect(Collectors.toList());
    }


}
