package com.zkril.aerial_back.dto;

import lombok.Data;

@Data
public class ConstraintDirectionDto {
    private String expression;   // 中文名，如 "侧向误差"
    private Double thetaStart;
    private Double thetaEnd;
    private Double phiStart;
    private Double phiEnd;
    private String status;       // 如 "<="
    private String limit;        // 约束值，对应 Directionobj
    private String error;        // 容差值，对应 delta
    private String weight;       // 权值
}
