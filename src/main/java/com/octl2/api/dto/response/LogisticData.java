package com.octl2.api.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

@Getter
@SuperBuilder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LogisticData implements Serializable {
    private List<PartnerResponse> fulfilments;
    private List<PartnerResponse> lastmiles;
    private List<WarehouseResponse> warehouses;
    private Set<PartnerResponse> fulfilmentsSet;
    private Set<PartnerResponse> lastmilesSet;
    private Set<WarehouseResponse> warehousesSet;

}
