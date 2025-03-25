package com.octl2.api.helper.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PartnerType {
    FFM(122),
    LM(121);

    private final int code;
}
