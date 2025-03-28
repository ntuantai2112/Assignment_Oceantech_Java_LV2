package com.octl2.api.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"province", "fulfilment", "lastmile", "warehouse"})
public class LogisticResponse extends LogisticData {
    private LogisticData logistics;
    private ProvinceResponse province;
    private DistrictResponse district;
}
