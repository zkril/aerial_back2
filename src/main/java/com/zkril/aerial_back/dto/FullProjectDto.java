package com.zkril.aerial_back.dto;

import lombok.Data;

import java.util.List;

/**
 * 用于一次性接收完整项目配置、模板参数及 JSON 输出设置的 DTO。
 */
@Data
public class FullProjectDto {

    // ========== 1. 项目信息 ==========

    /** 当前创建该项目的用户ID（必须） */
    private Integer userId;

    /** 项目所在的文件夹ID（必须） */
    private Integer folderId;

    /** 项目名称 */
    private String itemName;

    /** 项目封面图 URL，可为空 */
    private String imageUrl;

    /** 模板类型，如 Ideal_Circular，用于后续匹配模板配置 */
    private String type;

    // ========== 2. TemplateConfigs 配置字段 ==========

    /** 模板配置名 */
    private String name;

    /** 频率起始值 */
    private Double frequencyStart;

    /** 频率终止值 */
    private Double frequencyEnd;

    /** 频率采样点数 */
    private Integer frequencyPoints;

    /** 远场 θ 起始角度（单位：度） */
    private Double thetaStart;

    /** 远场 θ 终止角度 */
    private Double thetaEnd;

    /** θ方向采样点数 */
    private Integer thetaPoints;

    /** φ 起始角度 */
    private Double phiStart;

    /** φ 终止角度 */
    private Double phiEnd;

    /** φ方向采样点数 */
    private Integer phiPoints;

    /**
     * 蒙特卡洛次数
     */
    private Integer monteCarloNum;

    /**
     * 全向(1)/定向天线(0)
     */
    private Integer omniDire;

    /**
     * 是否非均匀,是（1）
     */
    private Integer uniform;

    /**
     * 相位高斯噪声
     */
    private Integer gaussnoise;
    /**
     * 理想阵元数目
     */
    private Integer idealPointNum;

    private List<OptimizeVariableDTO> optimizeList;
    private List<ObjectiveDirectionTargetDto> targetList;
    private List<ConstraintDirectionDto> constraintList;
    /**
     * 是否添加鲁棒设计，是（1）
     */
    private Integer isRobust;
    // ========== 3. TemplateParameters 配置字段（global_conf） ==========

    /** 算法名，如 DCNSGA_II_DE_MO */
    private String algorithmName;

    /**是否从上一次中断继续（1：是，0：否）*/
    private Integer continueLastbreakpoint;

    /** 评估超时时间，单位毫秒（默认 1000） */
    private Integer evaluateTimeout;


    /** 并发线程数 */
    private Integer threadNum;

    private List<AlgorithmParamItemDto> algorithmParamList;
}
