package com.octl2.api.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Getter
@Setter
@SuperBuilder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProvinceResponse extends LogisticData {
    private Long id;
    private String name;
    private String code;
    private List<DistrictResponse> districts;
    private DistrictResponse district;


}
