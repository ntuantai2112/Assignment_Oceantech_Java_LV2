package com.octl2.api.dto.response;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@JsonPropertyOrder({"province", "districts", "district", "subDistricts", "subDistrict", "fulfilment", "lastmile", "warehouse"})
public class SubDistrictResponse extends DistrictResponse {
    private SubDistrictResponse subDistrict;
}
