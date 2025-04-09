package com.octl2.api.helper.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum LevelMapping {

    LEVEL_MAPPING_ONE(1),
    LEVEL_MAPPING_TWO(2),
    LEVEL_MAPPING_THREE(3);

    private final int levelMapping;
}
