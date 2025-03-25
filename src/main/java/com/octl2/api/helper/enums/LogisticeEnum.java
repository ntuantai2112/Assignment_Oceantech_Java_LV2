package com.octl2.api.helper.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum LogisticeEnum {

    PROVINCE_NOT_FOUND("No logistic data found for this province"),
    PROVINCE_NAME_NOT_NULL("Province name is required"),
    LOGISTICS_NOT_FOUND("No logistic data found for this province"),
    WAREHOUSE_NOT_FOUND("No warehouse data available"),
    FULFILLMENT_NOT_FOUND("No fulfillment center found"),
    LASTMILE_NOT_FOUND("No last-mile delivery service available");


    private final String message;
}
