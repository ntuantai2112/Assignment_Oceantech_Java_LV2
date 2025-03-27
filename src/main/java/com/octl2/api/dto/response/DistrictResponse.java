package com.octl2.api.dto.response;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Getter
@SuperBuilder
@JsonPropertyOrder({"province", "districts","district" ,"id", "name", "code", "fulfilments", "lastmiles", "warehouses"})
public class DistrictResponse extends ProvinceResponse {

}
