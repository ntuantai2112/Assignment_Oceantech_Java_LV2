package com.octl2.api.dto.response;


import lombok.AllArgsConstructor;
import lombok.Getter;

public interface LogisticDTO {
    Long getDistrictId();
    Long getProvinceId();
    Long getFfmId();
    Long getLmId();
    Long getWarehouseId();
}

