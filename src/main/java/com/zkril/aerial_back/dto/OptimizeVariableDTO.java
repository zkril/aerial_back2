package com.zkril.aerial_back.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class OptimizeVariableDTO {
    private String name;
    private String min;
    private String max;
    @JsonProperty("default")
    private String defaultValue;
    private Boolean optimize_1;
}
