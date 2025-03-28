package com.octl2.api.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WarehouseResponse {

    private Integer id;
    private String name;
    private String sortName;
    private String contact;
    private String phone;
    private String address;


}
