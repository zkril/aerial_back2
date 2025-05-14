package com.zkril.aerial_back.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.zkril.aerial_back.dto.*;
import com.zkril.aerial_back.mapper.*;
import com.zkril.aerial_back.pojo.*;
import com.zkril.aerial_back.util.Result;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

@RestController
@RequestMapping("/projects")
public class ProjectController {

    @Autowired
    private ProjectsMapper projectsMapper;
    @Autowired
    private TemplateConfigsMapper templateConfigsMapper;
    @Autowired
    private TemplateParametersMapper templateParametersMapper;
    @Autowired
    private TemplateOptimizeVariablesMapper templateOptimizeVariablesMapper;
    @Autowired
    private TemplateObjectiveDirectionTargetMapper templateObjectiveDirectionTargetMapper;
    @Autowired
    private TemplateConstraintDirectionMapper templateConstraintDirectionMapper;
    @Autowired
    private TemplateAlgorithmParamMapper templateAlgorithmParamMapper;
    @Autowired
    private AlgorithmParamInstanceMapper algorithmParamInstanceMapper;
    // 根据 folderId 获取项目列表
    @GetMapping("/list_by_folder")
    public Result getProjectsByFolder(@RequestParam Integer folderId) {
        List<Projects> projects = projectsMapper.selectList(
                new QueryWrapper<Projects>().eq("folder_id", folderId)
        );

        List<Map<String, Object>> result = new ArrayList<>();
        for (Projects project : projects) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", project.getId());
            map.put("name", project.getName());
            map.put("desc", project.getDescription());
            map.put("time", project.getCreateTime());  // 转成时间戳
            map.put("type", project.getPtype());
            map.put("photo", project.getImageUrl());
            result.add(map);
        }

        return Result.ok(result);
    }


    @PostMapping("/create")
    @Transactional(
            // 默认 Propagation.REQUIRED 即可，保证和外层事务共用或新建
            propagation = Propagation.REQUIRED,
            // 指定哪些异常触发回滚（RuntimeException + Error 已经默认回滚）
            rollbackFor = Exception.class
    )
    public Result createProjectAndConfig(@RequestBody FullProjectDto dto) {
        System.out.println(dto);
        // 1. 创建项目
        Projects project = new Projects();
        project.setUserId(dto.getUserId());
        project.setFolderId(dto.getFolderId());
        project.setName(dto.getItemName());
        project.setImageUrl(dto.getImageUrl());
        project.setPtype("public");
        project.setTemplateType(dto.getType());
        Date now = Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant());
        project.setCreateTime(now);
        project.setUpdateTime(now);

        projectsMapper.insert(project);

        // 2. 保存配置（TemplateConfigs）
        TemplateConfigs config = new TemplateConfigs();
        config.setProjectId(project.getId());
        config.setName(dto.getName());
        config.setType(dto.getType());
        config.setFrequencyStart(dto.getFrequencyStart());
        config.setFrequencyEnd(dto.getFrequencyEnd());
        config.setFrequencyPoints(dto.getFrequencyPoints());
        config.setThetaStart(dto.getThetaStart());
        config.setThetaEnd(dto.getThetaEnd());
        config.setThetaPoints(dto.getThetaPoints());
        config.setPhiStart(dto.getPhiStart());
        config.setPhiEnd(dto.getPhiEnd());
        config.setPhiPoints(dto.getPhiPoints());
        config.setCreatedTime(now);
        config.setMonteCarloNum(dto.getMonteCarloNum());
        config.setOmniDire(dto.getOmniDire());
        config.setUniform(dto.getUniform());
        config.setGaussnoise(dto.getGaussnoise());
        config.setIdealPointNum(dto.getIdealPointNum());
        config.setIsRobust(dto.getIsRobust());
        templateConfigsMapper.insert(config);

        int templateConfigId = config.getId();
        for (OptimizeVariableDTO optDto : dto.getOptimizeList()) {
            TemplateOptimizeVariables varEntity = new TemplateOptimizeVariables();
            varEntity.setTemplateConfigId((long) templateConfigId);
            varEntity.setName(optDto.getName());
            varEntity.setMinValue(optDto.getMin());
            varEntity.setMaxValue(optDto.getMax());
            varEntity.setDefaultValue(optDto.getDefaultValue());
            varEntity.setOptimize1(optDto.getOptimize_1()?1:0);
            // 调用 Mapper 插入记录
            templateOptimizeVariablesMapper.insert(varEntity);
        }
        if (dto.getTargetList() != null) {
            for (ObjectiveDirectionTargetDto dtoItem : dto.getTargetList()) {
                TemplateObjectiveDirectionTarget entity = new TemplateObjectiveDirectionTarget();
                entity.setTemplateConfigId((long) templateConfigId);
                entity.setTarget(dtoItem.getTarget());
                entity.setThetaStart(dtoItem.getThetaStart());
                entity.setThetaEnd(dtoItem.getThetaEnd());
                entity.setPhiStart(dtoItem.getPhiStart());
                entity.setPhiEnd(dtoItem.getPhiEnd());
                entity.setOptimizeType(dtoItem.getOptimizeType());
                templateObjectiveDirectionTargetMapper.insert(entity);
            }
        }
        if (dto.getConstraintList() != null) {
            for (ConstraintDirectionDto item : dto.getConstraintList()) {
                TemplateConstraintDirection entity = new TemplateConstraintDirection();
                entity.setTemplateConfigId((long) templateConfigId);
                entity.setExpression(item.getExpression());
                entity.setThetaStart(item.getThetaStart());
                entity.setThetaEnd(item.getThetaEnd());
                entity.setPhiStart(item.getPhiStart());
                entity.setPhiEnd(item.getPhiEnd());
                entity.setStatus(item.getStatus());
                entity.setLimitValue(Double.parseDouble(item.getLimit()));
                entity.setErrorValue(Double.parseDouble(item.getError()));
                entity.setWeightValue(Double.parseDouble(item.getWeight()));
                templateConstraintDirectionMapper.insert(entity);
            }
        }


        // 3. 保存参数配置（TemplateParameters）
        TemplateParameters param = new TemplateParameters();
        param.setProjectId(project.getId());
        param.setAlgorithmName(dto.getAlgorithmName());
        param.setEvaluateTimeout(dto.getEvaluateTimeout());
        param.setProblemName(dto.getType()); // e.g., Ideal_circular
        param.setThreadnum(dto.getThreadNum());
        param.setContinuelastbreakpoint(dto.getContinueLastbreakpoint());
        templateParametersMapper.insert(param);

        if (dto.getConstraintList() != null){
            for (AlgorithmParamItemDto item : dto.getAlgorithmParamList()) {
                AlgorithmParamInstance record = new AlgorithmParamInstance();
                record.setAlgorithm(dto.getAlgorithmName());
                record.setParamKey(item.getKey());
                record.setValue(item.getValue());
                record.setNote(item.getLabel());
                record.setProjectId(Long.valueOf(project.getId())); // 可选
                algorithmParamInstanceMapper.insert(record); // 自定义 upsert
            }
        }


        project.setTemplateConfigId(config.getId());
        project.setTemplateParametersId(param.getId());

        projectsMapper.updateById(project);

        return Result.ok("项目及模板创建成功");
    }

    @GetMapping("/export/ideal_conf")
    public Result exportIdealConf(@RequestParam Integer projectId, HttpServletResponse response) throws Exception {
        // 1. 查询项目对应的模板配置
        TemplateConfigs config = templateConfigsMapper.selectOne(
                new QueryWrapper<TemplateConfigs>().eq("project_id", projectId));
        if (config == null) {
            return Result.fail("项目配置不存在");
        }
        TemplateParameters param = templateParametersMapper.selectOne(
                new QueryWrapper<TemplateParameters>().eq("project_id", projectId)
        );

        if (param == null) {
            return Result.fail("未找到全局参数配置");
        }
        List<AlgorithmParamInstance> algorithm = algorithmParamInstanceMapper.selectList(
                new QueryWrapper<AlgorithmParamInstance>().eq("project_id", projectId)
        );

        // 2. 初始化 Jackson ObjectMapper 和根节点
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode globalConf = mapper.createObjectNode();
        globalConf.put("ALGORITHM_NAME", param.getAlgorithmName());
        globalConf.put("CONTINUE_LASTBREAKPOINT", param.getContinuelastbreakpoint().toString());
        globalConf.put("EVALUATE_TIMEOUT", String.valueOf(param.getEvaluateTimeout()));
        globalConf.put("NodeAndThread", "[]");
        globalConf.put("OutputFreq","1");
        globalConf.put("PROBLEM_NAME", param.getProblemName());
        globalConf.put("SAVE_ANALYSISRESULT", "1");
        globalConf.put("ThreadNum", String.valueOf(param.getThreadnum()));
        globalConf.put("outfilepath", "D:/Documents/outfilepath");
        globalConf.put("outhfsspath", "D:/Documents/outhfsspath");


        ObjectNode algorithmConf = mapper.createObjectNode();
        for (AlgorithmParamInstance item : algorithm) {
            ObjectNode paramNode = mapper.createObjectNode();
            paramNode.put(item.getParamKey(), item.getValue());
            paramNode.put("note", item.getNote());
            algorithmConf.set(item.getParamKey(), paramNode);
        }


        // 3. 构建各部分 JSON 节点
        ObjectNode root = mapper.createObjectNode();
        // 3.1 各种 Constraint*Setting 部分，直接使用模板默认值
        ObjectNode constraintAngle = mapper.createObjectNode();
        constraintAngle.put("Phi_Lower_ang", "[[]]");
        constraintAngle.put("Phi_Upper_ang", "[[]]");
        constraintAngle.put("Theta_Lower_ang", "[[]]");
        constraintAngle.put("Theta_Upper_ang", "[[]]");
        constraintAngle.put("angleobj", "[[]]");
        constraintAngle.put("delta_ang", "[[]]");
        constraintAngle.put("functiontype_ang", "[[]]");
        constraintAngle.put("optimaltype_ang", "[[]]");
        constraintAngle.put("weight_ang", "[[]]");
        root.set("ConstraintAngleSetting", constraintAngle);

        ObjectNode constraintAxial = mapper.createObjectNode();
        constraintAxial.put("Phi_Lower_axial", "[[]]");
        constraintAxial.put("Phi_Upper_axial", "[[]]");
        constraintAxial.put("Theta_Lower_axial", "[[]]");
        constraintAxial.put("Theta_Upper_axial", "[[]]");
        constraintAxial.put("axialobj", "[[]]");
        constraintAxial.put("delta_axial", "[[]]");
        constraintAxial.put("functiontype_axial", "[[]]");
        constraintAxial.put("optimaltype_axial", "[[]]");
        constraintAxial.put("weight_axial", "[[]]");
        root.set("ConstraintAxialratioSetting", constraintAxial);

        ObjectNode constraintDirection = mapper.createObjectNode();
        List<TemplateConstraintDirection> constraints = templateConstraintDirectionMapper.selectList(
                new QueryWrapper<TemplateConstraintDirection>().eq("template_config_id", config.getId())
        );
        List<String> directionTypes_0 = new ArrayList<>();
        List<Double> thetaLower_0 = new ArrayList<>();
        List<Double> thetaUpper_0 = new ArrayList<>();
        List<Double> phiLower_0 = new ArrayList<>();
        List<Double> phiUpper_0 = new ArrayList<>();
        List<Double> directionObjs_0 = new ArrayList<>();
        List<Double> deltas = new ArrayList<>();
        List<String> statuses = new ArrayList<>();
        List<Double> weights = new ArrayList<>();
        for (TemplateConstraintDirection item : constraints) {
            directionTypes_0.add(item.getExpression());
            thetaLower_0.add(item.getThetaStart());
            thetaUpper_0.add(item.getThetaEnd());
            phiLower_0.add(item.getPhiStart());
            phiUpper_0.add(item.getPhiEnd());
            directionObjs_0.add(item.getLimitValue());
            deltas.add(item.getErrorValue());
            statuses.add(item.getStatus());
            weights.add(item.getWeightValue());
        }
        constraintDirection.put("DirectionType", toSingleQuotedNestedArrayString(directionTypes_0));
        constraintDirection.put("Theta_Lower_Direction", mapper.writeValueAsString(Collections.singletonList(thetaLower_0)));
        constraintDirection.put("Theta_Upper_Direction", mapper.writeValueAsString(Collections.singletonList(thetaUpper_0)));
        constraintDirection.put("Phi_Lower_Direction", mapper.writeValueAsString(Collections.singletonList(phiLower_0)));
        constraintDirection.put("Phi_Upper_Direction", mapper.writeValueAsString(Collections.singletonList(phiUpper_0)));
        constraintDirection.put("Directionobj", mapper.writeValueAsString(Collections.singletonList(directionObjs_0)));
        constraintDirection.put("delta_Direction", mapper.writeValueAsString(Collections.singletonList(deltas)));
        constraintDirection.put("optimaltype_Direction", toSingleQuotedNestedArrayString(statuses));
        constraintDirection.put("weight_Direction", mapper.writeValueAsString(Collections.singletonList(weights)));
        root.set("ConstraintDirectionSetting", constraintDirection);

        ObjectNode constraintGain = mapper.createObjectNode();
        constraintGain.put("Phi_Lower_gain", "[[]]");
        constraintGain.put("Phi_Upper_gain", "[[]]");
        constraintGain.put("Theta_Lower_gain", "[[]]");
        constraintGain.put("Theta_Upper_gain", "[[]]");
        constraintGain.put("delta_gain", "[[]]");
        constraintGain.put("functiontype_gain", "[[]]");
        constraintGain.put("gainobj", "[[]]");
        constraintGain.put("optimaltype_gain", "[[]]");
        constraintGain.put("weight_gain", "[[]]");
        root.set("ConstraintGainSetting", constraintGain);

        ObjectNode constraintSpara = mapper.createObjectNode();
        constraintSpara.put("Phi_Lower_S", "[[]]");
        constraintSpara.put("Phi_Upper_S", "[[]]");
        constraintSpara.put("R0_imag", "[[]]");
        constraintSpara.put("R0_real", "[[]]");
        constraintSpara.put("Theta_Lower_S", "[[]]");
        constraintSpara.put("Theta_Upper_S", "[[]]");
        constraintSpara.put("conValue_S", "[[]]");
        constraintSpara.put("delta_S", "[[]]");
        constraintSpara.put("optimaltype_S", "[[]]");
        constraintSpara.put("weight_S", "[[]]");
        root.set("ConstraintSparaSetting", constraintSpara);

        ObjectNode constraintStructure = mapper.createObjectNode();
        constraintStructure.put("constraint_value", "[[]]");
        constraintStructure.put("delta_Structure", "[[]]");
        constraintStructure.put("expression", "[[]]");
        constraintStructure.put("optimaltype", "[[]]");
        constraintStructure.put("weight_expression", "[[]]");
        root.set("ConstraintStructure", constraintStructure);

        ObjectNode constraintVSWR = mapper.createObjectNode();
        // VSWR约束部分有5组值，用字符串形式的数组表示
        constraintVSWR.put("R0_imag", "[[0.0],[0.0],[0.0],[0.0],[0.0]]");
        constraintVSWR.put("R0_real", "[[50.0],[50.0],[50.0],[50.0],[50.0]]");
        constraintVSWR.put("R1_imag", "[[None],[None],[None],[None],[None]]");
        constraintVSWR.put("R1_real", "[[None],[None],[None],[None],[None]]");
        constraintVSWR.put("ReturnLossType", "[[0],[0],[0],[0],[0]]");
        constraintVSWR.put("S11", "[[None],[None],[None],[None],[None]]");
        constraintVSWR.put("delta_imag", "[[None],[None],[None],[None],[None]]");
        constraintVSWR.put("delta_real", "[[None],[None],[None],[None],[None]]");
        constraintVSWR.put("functiontype_vswr", "[['mean'],['mean'],['mean'],['mean'],['mean']]");
        constraintVSWR.put("functiontype_vswr_all", "[['mean']]");
        constraintVSWR.put("optimaltype_vswr", "[['<='],['<='],['<='],['<='],['<=']]");
        constraintVSWR.put("vswrobj", "[[3.0],[3.0],[3.0],[3.0],[3.0]]");
        constraintVSWR.put("weight_vswr", "[[1.0],[1.0],[1.0],[1.0],[1.0]]");
        root.set("ConstraintVSWRSetting", constraintVSWR);

        // 3.2 频率设置 FreSetting，用项目配置的频率参数替换
        ObjectNode freSetting = mapper.createObjectNode();
        // 获取频率相关字段，若为空则用默认值0或模板值
        double fStart = config.getFrequencyStart() != null ? config.getFrequencyStart() : 0.0;
        double fEnd = config.getFrequencyEnd() != null ? config.getFrequencyEnd() : 0.0;
        int fPoints = config.getFrequencyPoints() != null ? config.getFrequencyPoints() : 1;
        // 将数值转换为无小数点（如200.0转成200）字符串，放入方括号
        String freStartStr = "[" + (fStart % 1.0 == 0 ? (int) fStart : fStart) + "]";
        String freEndStr = "[" + (fEnd % 1.0 == 0 ? (int) fEnd : fEnd) + "]";
        String freNumStr = "[" + fPoints + "]";
        freSetting.put("FreStart", freStartStr);
        freSetting.put("FreEnd", freEndStr);
        freSetting.put("FreNumber", freNumStr);
        // PM 和 SweepType 使用模板默认0
        freSetting.put("PM", "[0]");
        freSetting.put("SweepType", "[0]");
        root.set("FreSetting", freSetting);

        // 3.3 目标设置 (Objective* 部分) 保持模板默认值（大多为空或默认类型），这些与 TemplateConfigs 无直接关系
        ObjectNode objAngle = mapper.createObjectNode();
        objAngle.put("Phi_Lower_ang", "[[]]");
        objAngle.put("Phi_Upper_ang", "[[]]");
        objAngle.put("Theta_Lower_ang", "[[]]");
        objAngle.put("Theta_Upper_ang", "[[]]");
        objAngle.put("functiontype_ang", "[[]]");
        objAngle.put("optimaltype_ang", "[[]]");
        root.set("ObjectiveAngleSetting", objAngle);

        ObjectNode objAxial = mapper.createObjectNode();
        objAxial.put("Phi_Lower_axial", "[[]]");
        objAxial.put("Phi_Upper_axial", "[[]]");
        objAxial.put("Theta_Lower_axial", "[[]]");
        objAxial.put("Theta_Upper_axial", "[[]]");
        objAxial.put("functiontype_axial", "[[]]");
        objAxial.put("optimaltype_axial", "[[]]");
        root.set("ObjectiveAxialratioSetting", objAxial);

        List<TemplateObjectiveDirectionTarget> targets = templateObjectiveDirectionTargetMapper.selectList(
                new QueryWrapper<TemplateObjectiveDirectionTarget>().eq("template_config_id", config.getId())
        );
        ObjectNode objDirection = mapper.createObjectNode();
        List<String> directionTypes = new ArrayList<>();
        List<Double> thetaLower_1 = new ArrayList<>();
        List<Double> thetaUpper_1 = new ArrayList<>();
        List<Double> phiLower_1 = new ArrayList<>();
        List<Double> phiUpper_1 = new ArrayList<>();
        List<String> optimizeTypes = new ArrayList<>();

        for (TemplateObjectiveDirectionTarget target : targets) {
            directionTypes.add(target.getTarget());  // 如 '侧向误差'
            thetaLower_1.add(target.getThetaStart());
            thetaUpper_1.add(target.getThetaEnd());
            phiLower_1.add(target.getPhiStart());
            phiUpper_1.add(target.getPhiEnd());
            optimizeTypes.add(target.getOptimizeType());
        }

        // 设置 JSON 字段（使用字符串包裹以保持格式）
        objDirection.put("DirectionType", toSingleQuotedNestedArrayString(directionTypes));
        objDirection.put("Theta_Lower_Direction", mapper.writeValueAsString(Collections.singletonList(thetaLower_1)));
        objDirection.put("Theta_Upper_Direction", mapper.writeValueAsString(Collections.singletonList(thetaUpper_1)));
        objDirection.put("Phi_Lower_Direction", mapper.writeValueAsString(Collections.singletonList(phiLower_1)));
        objDirection.put("Phi_Upper_Direction", mapper.writeValueAsString(Collections.singletonList(phiUpper_1)));
        objDirection.put("optimaltype_Direction", toSingleQuotedNestedArrayString(optimizeTypes));
        root.set("ObjectiveDirectionSetting", objDirection);

        ObjectNode objGain = mapper.createObjectNode();
        objGain.put("Phi_Lower_gain", "[[]]");
        objGain.put("Phi_Upper_gain", "[[]]");
        objGain.put("Theta_Lower_gain", "[[]]");
        objGain.put("Theta_Upper_gain", "[[]]");
        objGain.put("functiontype_gain", "[[]]");
        objGain.put("optimaltype_gain", "[[]]");
        root.set("ObjectiveGainSetting", objGain);

        ObjectNode objRobust = mapper.createObjectNode();
        objRobust.put("Phi_Lower_robust", "[[0]]");
        objRobust.put("Phi_Upper_robust", "[[0]]");
        objRobust.put("Theta_Lower_robust", "[[-60]]");
        objRobust.put("Theta_Upper_robust", "[[60]]");
        objRobust.put("is_selected", "["+config.getIsRobust()+"]");
        root.set("ObjectiveRobust", objRobust);

        ObjectNode objSpara = mapper.createObjectNode();
        objSpara.put("Phi_Lower_S", "[[]]");
        objSpara.put("Phi_Upper_S", "[[]]");
        objSpara.put("R0_imag", "[[]]");
        objSpara.put("R0_real", "[[]]");
        objSpara.put("Theta_Lower_S", "[[]]");
        objSpara.put("Theta_Upper_S", "[[]]");
        objSpara.put("optimaltype_S", "[[]]");
        root.set("ObjectiveSparaSetting", objSpara);

        ObjectNode objStructure = mapper.createObjectNode();
        objStructure.put("expression", "[[]]");
        objStructure.put("expression_key", "[[]]");
        objStructure.put("optimaltype", "[[]]");
        root.set("ObjectiveStructure", objStructure);

        ObjectNode objVSWR = mapper.createObjectNode();
        objVSWR.put("R0_imag", "[[0.0],[0.0],[0.0],[0.0],[0.0]]");
        objVSWR.put("R0_real", "[[50.0],[50.0],[50.0],[50.0],[50.0]]");
        objVSWR.put("ReturnLossType", "[[0],[0],[0],[0],[0]]");
        objVSWR.put("functiontype_vswr", "[['mean'],['mean'],['mean'],['mean'],['mean']]");
        objVSWR.put("functiontype_vswr_all", "[['mean']]");
        objVSWR.put("optimaltype_vswr", "[['min'],['min'],['min'],['min'],['min']]");
        root.set("ObjectiveVSWRSetting", objVSWR);

        // 3.4 端口号设置 (PortNum) - 模板预设四个端口，这里直接使用模板值
        ObjectNode portNum = mapper.createObjectNode();
        int n0 = config.getIdealPointNum();
        for (int i = 1; i <= n0; i++) {
            portNum.put("PortNum" + i,String.valueOf(i));
        }
        root.set("PortNum", portNum);

        // 3.5 ThetaPhiStep 设置，用 TemplateConfigs 角度范围和点数
        ObjectNode thetaPhi = mapper.createObjectNode();
        // φ 和 θ 的上下限值（默认0，如提供则用提供值）
        double phiLower = config.getPhiStart() != null ? config.getPhiStart() : 0.0;
        double phiUpper = config.getPhiEnd() != null ? config.getPhiEnd() : 0.0;
        double thetaLower = config.getThetaStart() != null ? config.getThetaStart() : 0.0;
        double thetaUpper = config.getThetaEnd() != null ? config.getThetaEnd() : 0.0;
        // 步长，如果点数>1
        double phiStep = config.getPhiPoints() ;
        double thetaStep = config.getThetaPoints();
        // 转成所需格式字符串（取整如有必要）
        String phiLowerStr = "[" + (phiLower % 1.0 == 0 ? (int) phiLower : phiLower) + "]";
        String phiUpperStr = "[" + (phiUpper % 1.0 == 0 ? (int) phiUpper : phiUpper) + "]";
        String thetaLowerStr = "[" + (thetaLower % 1.0 == 0 ? (int) thetaLower : thetaLower) + "]";
        String thetaUpperStr = "[" + (thetaUpper % 1.0 == 0 ? (int) thetaUpper : thetaUpper) + "]";
        String phiStepStr = "[" + (phiStep % 1.0 == 0 ? (int) phiStep : phiStep) + "]";
        String thetaStepStr = "[" + (thetaStep % 1.0 == 0 ? (int) thetaStep : thetaStep) + "]";
        thetaPhi.put("PhiLower", phiLowerStr);
        thetaPhi.put("PhiUpper", phiUpperStr);
        thetaPhi.put("PhiStep", phiStepStr);
        thetaPhi.put("ThetaLower", thetaLowerStr);
        thetaPhi.put("ThetaUpper", thetaUpperStr);
        thetaPhi.put("ThetaStep", thetaStepStr);
        root.set("ThetaPhiStep", thetaPhi);

        // 3.6 附加参数 aExtraParameters，使用模板默认值，idealPointNum 根据需要替换
        ObjectNode extraParams = mapper.createObjectNode();
        ObjectNode monteCarlo = mapper.createObjectNode();
        monteCarlo.put("MonteCarloNum", config.getMonteCarloNum().toString());
        monteCarlo.put("note", "蒙特卡洛次数");
        extraParams.set("MonteCarloNum", monteCarlo);

        ObjectNode omniDire = mapper.createObjectNode();
        omniDire.put("OmniDire", config.getOmniDire().toString());
        omniDire.put("note", "全向/定向天线");
        extraParams.set("OmniDire", omniDire);

        ObjectNode hfss = mapper.createObjectNode();
        hfss.put("associatedHFSS", "0");
        hfss.put("note", "关联电磁仿真");
        extraParams.set("associatedHFSS", hfss);

        ObjectNode noise = mapper.createObjectNode();
        noise.put("gaussNoise", config.getGaussnoise().toString());
        noise.put("note", "相位高斯噪声(°)");
        extraParams.set("gaussNoise", noise);

        ObjectNode idealNum = mapper.createObjectNode();
        // 如果需要可根据shape决定理想阵元数目
        idealNum.put("idealPointNum", config.getIdealPointNum().toString());
        idealNum.put("note", "理想阵元数目");
        extraParams.set("idealPointNum", idealNum);

        ObjectNode uniform = mapper.createObjectNode();
        uniform.put("uniform", config.getUniform().toString());
        uniform.put("note", "是否非均匀");
        extraParams.set("uniform", uniform);

        root.set("aExtraParameters", extraParams);

        // 3.7 目标键 objectiveKey，模板固定的性能指标和变量键列表
        ObjectNode objectiveKey = mapper.createObjectNode();
        // 性能指标列表
        objectiveKey.putPOJO("performanceKey",
                Arrays.asList("gain", "axial", "vswr", "s11"));
        List<String> varsKey = new ArrayList<>();
        varsKey.add("r1"); // 添加固定的半径变量

        int n = config.getIdealPointNum();
        for (int i = 1; i <= n; i++) {
            varsKey.add("point_phe" + i);
        }
        objectiveKey.putPOJO("varsKey", varsKey);
        root.set("objectiveKey", objectiveKey);

        // 查询当前 config 对应的优化变量列表
        List<TemplateOptimizeVariables> optimizeVariables = templateOptimizeVariablesMapper.selectList(
                new QueryWrapper<TemplateOptimizeVariables>()
                        .eq("template_config_id", config.getId())
        );

        // 3.8 变量定义 variables 列表
        List<ObjectNode> variablesList = new ArrayList<>();
        for (TemplateOptimizeVariables var : optimizeVariables) {
            ObjectNode varNode = mapper.createObjectNode();
            // 设置 note 字段：展示变量名
            varNode.put("note", var.getName());

            // 推断变量键名，如 r1(mm) 提取为 r1，point_phe1(°) 提取为 point_phe1
            String key = extractVariableKey(var.getName());  // 你可以实现一个辅助方法来处理

            // 设置键值范围，例如 "r1": [0, 2500]
            List<Integer> range = Arrays.asList(Integer.parseInt(var.getMinValue()),
                    Integer.parseInt(var.getMaxValue()));
            varNode.putPOJO(key, range);
            variablesList.add(varNode);
        }
        // 将 variables 列表设置到 root
        root.putPOJO("variables", variablesList);

        // 3.9 变量类型 variablesType
        ObjectNode varsType = mapper.createObjectNode();
        // variableAngle 列表包含所有 point_phe 变量名
        List<String> angleVars = new ArrayList<>();
        for (int i = 1; i <= config.getIdealPointNum(); i++) {
            angleVars.add("point_phe" + i);
        }
        varsType.putPOJO("variableAngle", angleVars);
        // variableR 列表包含 r1 或矩形的长度宽度变量，这里圆形只有 r1
        varsType.putPOJO("variableR", Collections.singletonList("r1"));
        // variableStructure 列表为空（圆形情况下无额外结构变量）
        varsType.putPOJO("variableStructure", new ArrayList<>());
        root.set("variablesType", varsType);

        // 3.10 变量默认值 varsValue 列表，顺序与 variables 对应
        List<ObjectNode> varsValueList = new ArrayList<>();
        for (TemplateOptimizeVariables var : optimizeVariables) {
            ObjectNode varNode = mapper.createObjectNode();
            // 设置 note 字段：展示变量名
            varNode.put("default_value", Double.valueOf(var.getDefaultValue()));
            System.out.println(var);
            System.out.println(var.getOptimize1());
            varNode.put("is_selected", var.getOptimize1());
            varsValueList.add(varNode);
        }
        root.putPOJO("varsValue", varsValueList);

        ObjectNode combined = mapper.createObjectNode();
        combined.set("ideal_conf", root);
        combined.set("global_conf", globalConf);
        combined.set("algorithm_conf", algorithmConf);
        // 4. 使用 ObjectMapper 将组装的 JSON 对象转换为字符串
        String finalJson = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(combined);

        try {
            // 1. 生成最终 JSON 字符串
            //String singleQuoteJson = finalJson.replace("\"", "'");
            // 2. 设置响应头
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            // 下载文件名可自定义
            response.setHeader("Content-Disposition", "attachment; filename=combined_conf.json");

            // 3. 输出文件内容
            ServletOutputStream out = response.getOutputStream();
            out.write(finalJson.getBytes(StandardCharsets.UTF_8));
            out.flush();
            out.close();
        } catch (Exception e) {
            throw new RuntimeException("导出配置失败: " + e.getMessage(), e);
        }
        return Result.ok(projectId);
    }
    private String extractVariableKey(String name) {
        // 从 "point_phe1(°)" 提取 "point_phe1"，去除括号部分
        int idx = name.indexOf('(');
        return idx > 0 ? name.substring(0, idx).trim() : name.trim();
    }
    public static String toSingleQuotedNestedArrayString(List<String> list) {
        StringBuilder sb = new StringBuilder();
        sb.append("[[");
        for (int i = 0; i < list.size(); i++) {
            sb.append("'").append(list.get(i)).append("'");
            if (i != list.size() - 1) {
                sb.append(",");
            }
        }
        sb.append("]]");
        return sb.toString();
    }

    @GetMapping("/algorithm/param-template")
    public Result getAlgorithmParamTemplates() {
        QueryWrapper<TemplateAlgorithmParam> templateAlgorithmParamQueryWrapper = new QueryWrapper<>();
        List<TemplateAlgorithmParam> list = templateAlgorithmParamMapper.selectList(templateAlgorithmParamQueryWrapper);

        Map<String, List<Map<String, Object>>> result = new LinkedHashMap<>();

        for (TemplateAlgorithmParam item : list) {
            List<Map<String, Object>> paramList = result.get(item.getAlgorithm());
            if (paramList == null) {
                paramList = new ArrayList<>();
                result.put(item.getAlgorithm(), paramList);
            }

            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("label", item.getLabel());
            paramMap.put("key", item.getParamKey());
            paramMap.put("value", parseValue(item.getDefaultValue(), item.getValueType()));
            paramList.add(paramMap);
        }

        return Result.ok(result);
    }

    // 工具方法：兼容 Java 8 的类型转换
    private Object parseValue(String value, String type) {
        if (value == null || type == null) return value;
        try {
            switch (type.toLowerCase()) {
                case "int":
                    return Integer.parseInt(value);
                case "float":
                    return Float.parseFloat(value);
                case "double":
                    return Double.parseDouble(value);
                case "boolean":
                    return Boolean.parseBoolean(value);
                default:
                    return value;
            }
        } catch (Exception e) {
            return value;
        }
    }



}
