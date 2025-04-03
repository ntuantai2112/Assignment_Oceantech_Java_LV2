package com.octl2.api.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.util.List;

@Setter
@Getter
@SuperBuilder
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"province", "fulfilment", "lastmile", "warehouse"})
public class LogisticResponse extends LogisticData implements Serializable {
    private LogisticData logistics;
    private ProvinceResponse province;
    private DistrictResponse district;
    private SubDistrictResponse subDistrict;
    private PartnerResponse fulfilment;
    private PartnerResponse lastmile;
    private WarehouseResponse warehouse;
}
