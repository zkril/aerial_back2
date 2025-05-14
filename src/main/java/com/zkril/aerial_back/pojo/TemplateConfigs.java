package com.zkril.aerial_back.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 模板配置表，对应每个项目的技术参数
 * @TableName template_configs
 */
@TableName(value ="template_configs")
@Data
public class TemplateConfigs implements Serializable {
    /**
     * 模板配置主键 ID
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 关联的项目 ID（外键）
     */
    private Integer projectId;

    /**
     * 模板名称
     */
    private String name;

    /**
     * 模板类型（如 Ideal_Circular）
     */
    private String type;

    /**
     * 频率起始值
     */
    private Double frequencyStart;

    /**
     * 频率终止值
     */
    private Double frequencyEnd;

    /**
     * 频率采样点数量
     */
    private Integer frequencyPoints;

    /**
     * θ（Theta）起始角度
     */
    private Double thetaStart;

    /**
     * θ 终止角度
     */
    private Double thetaEnd;

    /**
     * θ 方向步长
     */
    private Integer thetaPoints;

    /**
     * φ（Phi）起始角度
     */
    private Double phiStart;

    /**
     * φ 终止角度
     */
    private Double phiEnd;

    /**
     * φ 方向步长
     */
    private Integer phiPoints;

    /**
     * 创建时间
     */
    private Date createdTime;

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

    /**
     * 是否添加鲁棒设计，是（1）
     */
    private Integer isRobust;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}