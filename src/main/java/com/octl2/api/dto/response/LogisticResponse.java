package com.octl2.api.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.octl2.api.entity.Partner;
import com.octl2.api.entity.Province;
import com.octl2.api.entity.Warehouse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Getter
@SuperBuilder
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"province", "fulfilments", "lastmiles", "warehouses"})
public class LogisticResponse extends LogisticData {
    private ProvinceResponse province;
    private DistrictResponse district;
}
