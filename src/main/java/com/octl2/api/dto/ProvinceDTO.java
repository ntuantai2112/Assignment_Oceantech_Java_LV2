package com.octl2.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public interface ProvinceDTO {

    Long getId();

    String getName();

    String getShortname();

    String getCode();

    String getDescription();
}
