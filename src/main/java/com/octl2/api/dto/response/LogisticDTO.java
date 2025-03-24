package com.octl2.api.dto.response;


import lombok.AllArgsConstructor;
import lombok.Getter;

public interface LogisticDTO {
    Integer getProvinceId();

    String getProvinceName();

    Integer getFfmId();

    String getFfmName();

    String getFfmShortname();

    Integer getLmId();

    String getLmName();

    String getLmShortname();

    Long getWarehouseId();

    String getWarehouseName();

    String getWarehouseShortname();
}

