package com.octl2.api.dto.response;

import com.octl2.api.entity.Partner;
import com.octl2.api.entity.Province;
import com.octl2.api.entity.Warehouse;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class LogisticResponse {

    private ProvinceResponse province;
    private List<PartnerResponse> fulfilments;
    private List<PartnerResponse> lastmiles;
    private List<WarehouseResponse> warehouses;
}
