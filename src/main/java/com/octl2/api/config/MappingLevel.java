package com.octl2.api.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "logistics")
@Getter
@Setter
public class MappingLevel {

    private int levelMapping;
}
