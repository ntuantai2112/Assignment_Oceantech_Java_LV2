package com.octl2.api.helper.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum LogisticeEnum {

    LOGISTIC_PROVINCE_SUCCESS("Get provinces and logistics successfully!"),
    LOGISTIC_DISTRICT_SUCCESS("Get districts and logistics successfully!"),
    LOGISTIC_SUBDISTRICT_SUCCESS("Get subDistricts and logistics successfully!"),
    LOGISTIC_LEVEL_MAPPING("Invalid Level Mapping,Level mapping must be between 1 and 3!"),
    PROVINCE_NOT_FOUND("No logistic data found for this province"),
    PROVINCE_ID_INVALID("Invalid Province ID. Please enter a positive number."),
    PROVINCE_ID_NOT_NULL("Province ID cannot be null. Please enter a valid province ID."),
    DISTRICT_ID_NOT_NULL("District ID cannot be null. Please enter a valid district ID."),
    SUB_DISTRICT_ID_NOT_NULL("SubDistrict ID cannot be null. Please enter a valid SubDistrict ID."),
    DISTRICT_NOT_FOUND("No logistic data found for this district"),
    SUB_DISTRICT_NOT_FOUND("No logistic data found for this subDistrict"),
    PROVINCE_NAME_NOT_NULL("Province name is required"),
    LOGISTICS_NOT_FOUND("No logistics data found for this area"),
    LEVEL_MAPPING_NOT_NULL("No logistic data level mapping not null"),
    LOGISTICS_ID_INVALID("ID cannot be null. Please enter a valid ID."),
    WAREHOUSE_NOT_FOUND("No warehouse data available"),
    FULFILLMENT_NOT_FOUND("No fulfillment center found"),
    LASTMILE_NOT_FOUND("No last-mile delivery service available");


    private final String message;
}
