package com.zkril.aerial_back.dto;

import lombok.Data;

@Data
public class ObjectiveDirectionTargetDto {
    private String target;         // 如 '侧向误差'、'第二相关峰'
    private Double thetaStart;
    private Double thetaEnd;
    private Double phiStart;
    private Double phiEnd;
    private String optimizeType;   // 如 'min'
}
