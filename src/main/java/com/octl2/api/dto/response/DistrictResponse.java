package com.octl2.api.dto.response;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Getter
@Setter
@SuperBuilder
@JsonPropertyOrder({"province", "districts", "district", "fulfilment", "lastmile", "warehouse"})
public class DistrictResponse extends ProvinceResponse {
    private List<SubDistrictResponse> subDistricts;
}
