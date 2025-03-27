package com.octl2.api.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WarehouseResponse {

    private Integer id;
    private String name;
    private String sortName;
    private String contact;
    private String phone;
    private String address;


    public WarehouseResponse(Integer id, String name) {
        this.id = id;
        this.name = name;
    }
}
