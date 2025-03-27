package com.octl2.api.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;


public interface LogisticDTO {
    Long getProvinceId();

    Long getDistrictId();

    Long getFfmId();

    Long getLmId();

    Long getWarehouseId();
}

