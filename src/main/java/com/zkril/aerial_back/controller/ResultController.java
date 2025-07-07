package com.zkril.aerial_back.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.zkril.aerial_back.mapper.ProjectResultMapper;
import com.zkril.aerial_back.pojo.ProjectResult;
import com.zkril.aerial_back.service.ProjectResultService;
import com.zkril.aerial_back.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

@RestController
@RequestMapping("/result")
public class ResultController {

    @Autowired
    private ProjectResultMapper projectResultMapper;

    /**
     * 接收 JSON 文件上传并保存解析结果
     * @param file 前端上传的JSON文件 (application/json)
     * @param projectId URL中的项目ID参数
     * @return 操作结果的响应实体
     */
    @PostMapping("/upload")
    @Transactional
    public Result uploadResultFile(@RequestParam("file") MultipartFile file,
                                   @RequestParam("projectId") Integer projectId) {
        LambdaQueryWrapper<ProjectResult> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ProjectResult::getProjectId, projectId);
        List<ProjectResult> oldresultList = projectResultMapper.selectList(queryWrapper);
        if (!oldresultList.isEmpty()) {
            for (ProjectResult projectResult : oldresultList) {
                projectResultMapper.deleteById(projectResult.getId());
            }
        }

        try {
            if (file.isEmpty()) {
                return Result.fail("上传失败：文件为空");
            }

            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootArray = mapper.readTree(file.getInputStream());

            if (!rootArray.isArray() || rootArray.size() == 0) {
                return Result.fail("上传失败：JSON 格式应为数组包裹对象");
            }

            JsonNode rootNode = rootArray.get(0);
            Iterator<String> fieldNames = rootNode.fieldNames();
            List<ProjectResult> resultList = new ArrayList<>();

            while (fieldNames.hasNext()) {
                String key = fieldNames.next();
                JsonNode itemNode = rootNode.get(key);

                ProjectResult result = new ProjectResult();
                result.setProjectId(projectId);
                result.setConstraintsXy(itemNode.get("constraints") != null ? itemNode.get("constraints").toString() : null);
                result.setEfeasible(itemNode.get("efeasible") != null ? itemNode.get("efeasible").asInt() : null);
                result.setExtraObjectives(itemNode.get("extra_objectives") != null ? itemNode.get("extra_objectives").toString() : null);
                result.setExtrainfo(itemNode.get("extrainfo") != null ? itemNode.get("extrainfo").toString() : null);
                result.setGenes(itemNode.get("genes") != null ? itemNode.get("genes").toString() : null);
                result.setModifypheno(itemNode.get("modifypheno") != null ? itemNode.get("modifypheno").asInt() : null);
                result.setNondomlayer(itemNode.get("nondomLayer") != null ? itemNode.get("nondomLayer").toString() : null);
                result.setObjectives(itemNode.get("objectives") != null ? itemNode.get("objectives").toString() : null);
                result.setValid(itemNode.get("valid") != null ? itemNode.get("valid").asInt() : 0);
                result.setDetail(itemNode.get("data").toString()); // 注意：此处仍然是 JSON 节点，建议转成字符串再存
                System.out.println(itemNode.get("pheno"));
                result.setPheno(itemNode.get("pheno") != null ? itemNode.get("pheno").toString() : null);
                result.setFreq(itemNode.get("Freq") != null ? itemNode.get("Freq").toString() : null);
                result.setPhi(itemNode.get("Phi") != null ? itemNode.get("Phi").toString() : null);
                result.setTheta(itemNode.get("Theta") != null ? itemNode.get("Theta").toString() : null);
                resultList.add(result);
            }

            for (ProjectResult r : resultList) {
                System.out.println(r);
                projectResultMapper.insert(r); // 确保你这个 insert 返回值正常
            }

            return Result.ok("文件上传解析成功，共插入：" + resultList.size() + " 条数据");

        } catch (JsonProcessingException e) {
            return Result.fail("上传失败：JSON 文件格式错误");
        } catch (Exception e) {
            e.printStackTrace(); // 调试用
            return Result.fail("上传失败：服务器内部错误");
        }
    }

    @GetMapping("/list/{projectId}")
    public Result getListAsJson(@PathVariable Integer projectId) {
        LambdaQueryWrapper<ProjectResult> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ProjectResult::getProjectId, projectId);
        List<ProjectResult> resultList = projectResultMapper.selectList(queryWrapper);

        if (resultList.isEmpty()) {
            return Result.fail("未找到记录");
        }

        ObjectMapper mapper = new ObjectMapper();
        List<Map<String, Object>> responseList = new ArrayList<>();

        for (ProjectResult result : resultList) {
            try {
                if (result.getObjectives() != null) {
                    JsonNode jsonNode = mapper.readTree(result.getObjectives().toString());
                    Map<String, Object> map = new java.util.HashMap<>();
                    map.put("id", result.getId());
                    map.put("objectives", jsonNode);
                    responseList.add(map);
                }
            } catch (JsonProcessingException e) {
                e.printStackTrace();
                return Result.fail("解析 objectives 字段失败");
            }
        }

        return Result.ok(responseList);
    }

    @GetMapping("/pheno/{resultId}")
    public Result getPheno(@PathVariable Integer resultId) {
        ProjectResult result=projectResultMapper.selectById(resultId);

        if (result==null) {
            return Result.fail("未找到记录");
        }

        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> response = new HashMap<>();

        try {
            if (result.getPheno() != null) {
                JsonNode jsonNode = mapper.readTree(result.getPheno().toString());
                response.put("phone", jsonNode);
            }
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return Result.fail("解析 pheno 字段失败");
        }

        return Result.ok(response);
    }

    @GetMapping("/2dr/{resultId}")
    public Result get2DCurve(@PathVariable Integer resultId,
                             @RequestParam String primarytype,
                             @RequestParam String category,
                             @RequestParam(defaultValue = "deg") String unit,
                             @RequestParam(defaultValue = "0") Integer phaseIndex) throws JsonProcessingException {

        ProjectResult result = projectResultMapper.selectById(resultId);
        if (result == null) return Result.fail("未找到记录");

        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(result.getDetail().toString());

        JsonNode freqList  = mapper.readTree(result.getFreq().toString());
        JsonNode phiList   = mapper.readTree(result.getPhi().toString());
        JsonNode thetaList = mapper.readTree(result.getTheta().toString());

        List<String> xAxis = new ArrayList<>();
        List<Map<String, Object>> series = new ArrayList<>();

        // 获取目标数据源
        JsonNode dataNode;
        if ("相位".equals(category)) {
            dataNode = root.get("phase_diff").get("realData").get(phaseIndex);  // freq -> theta -> phi
        } else {
            int idx = switch (category) {
                case "方位面测向误差" -> 0;
                case "俯仰面测向误差" -> 1;
                case "第二相关峰" -> 2;
                default -> -1;
            };
            if (idx == -1) return Result.fail("不支持的 category");
            dataNode = root.get("objectives").get("realData").get(idx);         // freq -> theta -> phi
        }

        // 设置 xAxis 维度
        switch (primarytype) {
            case "Phi" -> phiList.forEach(p -> xAxis.add(p.asText()));
            case "Theta" -> thetaList.forEach(t -> xAxis.add(t.asText()));
            case "Freq" -> freqList.forEach(f -> xAxis.add(f.asText()));
            default -> { return Result.fail("primarytype 仅支持 Phi / Theta / Freq"); }
        }

        // 设置单位后缀
        String unitLabel = "rad".equalsIgnoreCase(unit) ? "rad" : "deg";

        // 构造曲线（组合另外两维）
        for (int f = 0; f < freqList.size(); f++) {
            for (int t = 0; t < thetaList.size(); t++) {
                for (int p = 0; p < phiList.size(); p++) {

                    List<Double> yList = new ArrayList<>();
                    switch (primarytype) {
                        case "Phi" -> {
                            for (int i = 0; i < phiList.size(); i++) {
                                double v = dataNode.get(f).get(t).get(i).asDouble();
                                yList.add(convert(v, unit));
                            }
                        }
                        case "Theta" -> {
                            for (int i = 0; i < thetaList.size(); i++) {
                                double v = dataNode.get(f).get(i).get(p).asDouble();
                                yList.add(convert(v, unit));
                            }
                        }
                        case "Freq" -> {
                            for (int i = 0; i < freqList.size(); i++) {
                                double v = dataNode.get(i).get(t).get(p).asDouble();
                                yList.add(convert(v, unit));
                            }
                        }
                    }

                    // 曲线命名（仅标出横轴之外的两个维度）
                    String name = switch (primarytype) {
                        case "Phi" -> String.format("%s\nFreq=%sMHz,Theta=%s%s",
                                category,
                                freqList.get(f).asText(),
                                thetaList.get(t).asText(), unitLabel);
                        case "Theta" -> String.format("%s\nFreq=%sMHz,Phi=%s%s",
                                category,
                                freqList.get(f).asText(),
                                phiList.get(p).asText(), unitLabel);
                        case "Freq" -> String.format("%s\nTheta=%s%s,Phi=%s%s",
                                category,
                                thetaList.get(t).asText(), unitLabel,
                                phiList.get(p).asText(), unitLabel);
                        default -> "未知类型";
                    };

                    // 校验该曲线是否与 xAxis 尺寸一致
                    boolean valid =
                            (primarytype.equals("Phi") && yList.size() == phiList.size()) ||
                                    (primarytype.equals("Theta") && yList.size() == thetaList.size()) ||
                                    (primarytype.equals("Freq") && yList.size() == freqList.size());

                    if (valid) {
                        series.add(Map.of("name", name, "data", yList));
                    }
                }
            }
        }


        return Result.ok(Map.of("xAxis", xAxis, "series", series));
    }

    private double convert(double v, String unit) {
        return "rad".equalsIgnoreCase(unit) ? Math.toRadians(v) : v;
    }

    @GetMapping("/3dr/{resultId}")
    public Result get3DR(@PathVariable Integer resultId,
                         @RequestParam String xAxis,
                         @RequestParam String yAxis,
                         @RequestParam String zType,
                         @RequestParam int zIndex,
                         @RequestParam double filterValue,
                         @RequestParam(defaultValue = "deg") String unit) throws JsonProcessingException {

        if (xAxis.equals(yAxis)) return Result.fail("xAxis 和 yAxis 不能相同");

        ProjectResult result = projectResultMapper.selectById(resultId);
        if (result == null) return Result.fail("未找到记录");

        ObjectMapper mapper = new ObjectMapper();
        JsonNode detail = mapper.readTree(result.getDetail().toString());

        JsonNode freqList  = mapper.readTree(result.getFreq().toString());
        JsonNode phiList   = mapper.readTree(result.getPhi().toString());
        JsonNode thetaList = mapper.readTree(result.getTheta().toString());

        // 轴数据
        Map<String, JsonNode> axisMap = Map.of(
                "Freq", freqList,
                "Phi", phiList,
                "Theta", thetaList
        );

        List<Double> xList = new ArrayList<>();
        axisMap.get(xAxis).forEach(v -> xList.add(v.asDouble()));

        List<Double> yList = new ArrayList<>();
        axisMap.get(yAxis).forEach(v -> yList.add(v.asDouble()));

        // 固定轴
        String fixedAxis = List.of("Freq", "Phi", "Theta").stream()
                .filter(ax -> !ax.equals(xAxis) && !ax.equals(yAxis))
                .findFirst()
                .orElseThrow();

        int fixedIndex = findNearestIndex(axisMap.get(fixedAxis), filterValue);
        if (fixedIndex == -1) return Result.fail("固定维度值未找到");

        // 获取数据源
        JsonNode dataNode;
        if ("phase_diff".equals(zType)) {
            dataNode = detail.get("6_2").get("phase_diff").get("realData").get(zIndex);
        } else if ("objectives".equals(zType)) {
            dataNode = detail.get("6_2").get("objectives").get("realData").get(zIndex);
        } else {
            return Result.fail("zType 参数无效，应为 phase_diff 或 objectives");
        }

        // 构造三维点
        List<List<Object>> points = new ArrayList<>();

        for (int xi = 0; xi < xList.size(); xi++) {
            for (int yi = 0; yi < yList.size(); yi++) {
                int freqIdx  = xAxis.equals("Freq")  ? xi : yAxis.equals("Freq")  ? yi : fixedIndex;
                int thetaIdx = xAxis.equals("Theta") ? xi : yAxis.equals("Theta") ? yi : fixedIndex;
                int phiIdx   = xAxis.equals("Phi")   ? xi : yAxis.equals("Phi")   ? yi : fixedIndex;

                double raw = dataNode.get(freqIdx).get(thetaIdx).get(phiIdx).asDouble();
                double zVal = convert(raw, unit);

                points.add(List.of(
                        xList.get(xi),
                        yList.get(yi),
                        zVal
                ));
            }
        }

        Map<String, Object> resp = new HashMap<>();
        resp.put("xLabel", xAxis);
        resp.put("yLabel", yAxis);
        resp.put("zLabel", zType + "[" + zIndex + "]" + (unit.equals("rad") ? "(rad)" : "(deg)"));
        resp.put("data", points);

        return Result.ok(resp);
    }


    // 工具：匹配 filterValue 最近值对应的下标
    private int findNearestIndex(JsonNode list, double target) {
        double minDelta = Double.MAX_VALUE;
        int index = -1;
        for (int i = 0; i < list.size(); i++) {
            double val = list.get(i).asDouble();
            double delta = Math.abs(val - target);
            if (delta < minDelta) {
                minDelta = delta;
                index = i;
            }
        }
        return index;
    }


//    @GetMapping("/2dr/{resultId}")
//    public Result get2DR(@PathVariable Integer resultId,@RequestParam String primarytype,@RequestParam String category,
//                         @RequestParam(defaultValue = "deg") String unit) {
//        ProjectResult result=projectResultMapper.selectById(resultId);
//        if (result==null) {
//            return Result.fail("未找到记录");
//        }
//        if(primarytype.equals("Phi")){
//            ObjectMapper mapper = new ObjectMapper();
//            Map<String, Object> response = new HashMap<>();
//
//            try {
//                if (result.getPheno() != null) {
//                    JsonNode jsonNode = mapper.readTree(result.getDetail().toString());
//                    JsonNode nodes = jsonNode.get("objectives").get("realData");
//                    JsonNode phiList = mapper.readTree(result.getPhi().toString());
//                    JsonNode freqList = mapper.readTree(result.getFreq().toString());
//                    JsonNode thetaList = mapper.readTree(result.getTheta().toString());
//                    List<String> xAxis = new ArrayList<>();
//                    phiList.forEach(p -> xAxis.add(p.asText()));
//
//                    List<Map<String, Object>> series = new ArrayList<>();
//
//                    if (category.equals("测向误差")) {
//                        nodes= nodes.get(0);
//                        for (int i = 0; i < nodes.size(); i++) {
//                            List<Double> yValues = new ArrayList<>();
//                            JsonNode freqData = nodes.get(i);
//
//                            for (JsonNode phiVal : freqData.get(0)) {
//                                double value = phiVal.asDouble();
//                                // 根据单位选择：转换角度为弧度
//                                if ("rad".equalsIgnoreCase(unit)) {
//                                    value = Math.toRadians(value);
//                                }
//                                yValues.add(value);
//                            }
//
//                            String freqLabel = freqList.get(i).asText();
//                            String thetaLabel = thetaList.get(0).asText(); // 固定 Theta=90deg
//                            String seriesName = "测向误差\nFreq=" + freqLabel + "MHz,Theta=" + thetaLabel + "deg";
//
//                            Map<String, Object> seriesItem = new HashMap<>();
//                            seriesItem.put("name", seriesName);
//                            seriesItem.put("data", yValues);
//                            series.add(seriesItem);
//                        }
//                    }else if(category.equals("第二相关峰")){
//                        nodes= nodes.get(1);
//                        for (int i = 0; i < nodes.size(); i++) {
//                            List<Double> yValues = new ArrayList<>();
//                            JsonNode freqData = nodes.get(i);
//
//                            for (JsonNode phiVal : freqData.get(0)) {
//                                double value = phiVal.asDouble();
//                                // 根据单位选择：转换角度为弧度
//                                if ("rad".equalsIgnoreCase(unit)) {
//                                    value = Math.toRadians(value);
//                                }
//                                yValues.add(value);
//                            }
//
//                            String freqLabel = freqList.get(i).asText();
//                            String thetaLabel = thetaList.get(0).asText(); // 固定 Theta=90deg
//                            String seriesName = "第二相关峰\nFreq=" + freqLabel + "MHz,Theta=" + thetaLabel + "deg";
//
//                            Map<String, Object> seriesItem = new HashMap<>();
//                            seriesItem.put("name", seriesName);
//                            seriesItem.put("data", yValues);
//                            series.add(seriesItem);
//                        }
//
//
//                    }
//
//                    response.put("xAxis", xAxis);       // 对应 Phi 值
//                    response.put("series", series);     // 曲线数据集合
//                    return Result.ok(response);
//
//
//                }
//            } catch (JsonProcessingException e) {
//                e.printStackTrace();
//                return Result.fail("解析 pheno 字段失败");
//            }
//            return Result.ok(response);
//        }else{
//            return Result.fail("功能尚未包含");
//        }
//
//    }

}

