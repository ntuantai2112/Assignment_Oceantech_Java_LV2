package com.octl2.api.helper.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum LogisticeEnum {

    LOGISTIC_PROVINCE_SUCCESS("Get provinces and logistics successfully!"),
    LOGISTIC_DISTRICT_SUCCESS("Get districts and logistics successfully!"),
    LOGISTIC_SUBDISTRICT_SUCCESS("Get subdistricts and logistics successfully!"),
    PROVINCE_NOT_FOUND("No logistic data found for this province"),
    PROVINCE_ID_INVALID("Invalid Province ID. Please enter a positive number."),
    PROVINCE_ID_NOT_NULL("Province ID cannot be null. Please enter a valid province ID."),
    DISTRICT_NOT_FOUND("No logistic data found for this district"),
    SUB_DISTRICT_NOT_FOUND("No logistic data found for this subdistrict"),
    PROVINCE_NAME_NOT_NULL("Province name is required"),
    LOGISTICS_NOT_FOUND("No logistic data found for this province"),
    LOGISTICS_ID_INVALID("ID cannot be null. Please enter a valid ID."),
    WAREHOUSE_NOT_FOUND("No warehouse data available"),
    FULFILLMENT_NOT_FOUND("No fulfillment center found"),
    LASTMILE_NOT_FOUND("No last-mile delivery service available");


    private final String message;
}
