package com.octl2.api.dto.response;

import com.octl2.api.entity.*;
import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Getter
@Builder
public class LogisticPreloadData {

    Map<Long, Province> provinceMap;
    Map<Long, District> districtMap;
    Map<Long, SubDistrict> subDistrictMap;
    Map<Long, Partner> partnerMap;
    Map<Long, Warehouse> warehouseMap;
}
