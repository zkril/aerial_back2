package com.zkril.aerial_back.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import lombok.Data;

/**
 * 
 * @TableName algorithm_param_instance
 */
@TableName(value ="algorithm_param_instance")
@Data
public class AlgorithmParamInstance implements Serializable {
    /**
     * 主键，自增 ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 算法名称，如 DCNSGA_II_DE_MO、ForwardAnalysis 等
     */
    private String algorithm;

    /**
     * 参数键，用于程序识别，如 maxGenerations、CoupleForward
     */
    private String paramKey;

    /**
     * 参数值，保存前端传入的配置值（统一存为字符串）
     */
    private String value;

    /**
     * 参数中文描述，用于界面显示，如 最大代数、有互耦正向分析
     */
    private String note;

    /**
     * 所属项目 ID
     */
    private Long projectId;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}