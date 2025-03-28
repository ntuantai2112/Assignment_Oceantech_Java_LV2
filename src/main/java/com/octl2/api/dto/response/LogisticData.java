package com.octl2.api.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Getter
@SuperBuilder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LogisticData {
    private List<PartnerResponse> fulfilments;
    private List<PartnerResponse> lastmiles;
    private List<WarehouseResponse> warehouses;

}
