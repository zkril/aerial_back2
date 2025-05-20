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
    public Result get2DR(@PathVariable Integer resultId,@RequestParam String primarytype,@RequestParam String category,
                         @RequestParam(defaultValue = "deg") String unit) {
        ProjectResult result=projectResultMapper.selectById(resultId);

        if (result==null) {
            return Result.fail("未找到记录");
        }
        if(primarytype.equals("Phi")){
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> response = new HashMap<>();

            try {
                if (result.getPheno() != null) {
                    JsonNode jsonNode = mapper.readTree(result.getDetail().toString());
                    JsonNode nodes = jsonNode.get("objectives").get("realData");
                    JsonNode phiList = mapper.readTree(result.getPhi().toString());
                    JsonNode freqList = mapper.readTree(result.getFreq().toString());
                    JsonNode thetaList = mapper.readTree(result.getTheta().toString());
                    List<String> xAxis = new ArrayList<>();
                    phiList.forEach(p -> xAxis.add(p.asText()));

                    List<Map<String, Object>> series = new ArrayList<>();

                    if (category.equals("测向误差")) {
                        nodes= nodes.get(0);
                        for (int i = 0; i < nodes.size(); i++) {
                            List<Double> yValues = new ArrayList<>();
                            JsonNode freqData = nodes.get(i);

                            for (JsonNode phiVal : freqData.get(0)) {
                                double value = phiVal.asDouble();
                                // 根据单位选择：转换角度为弧度
                                if ("rad".equalsIgnoreCase(unit)) {
                                    value = Math.toRadians(value);
                                }
                                yValues.add(value);
                            }

                            String freqLabel = freqList.get(i).asText();
                            String thetaLabel = thetaList.get(0).asText(); // 固定 Theta=90deg
                            String seriesName = "测向误差\nFreq=" + freqLabel + "MHz,Theta=" + thetaLabel + "deg";

                            Map<String, Object> seriesItem = new HashMap<>();
                            seriesItem.put("name", seriesName);
                            seriesItem.put("data", yValues);
                            series.add(seriesItem);
                        }
                    }else if(category.equals("第二相关峰")){
                        nodes= nodes.get(1);
                        for (int i = 0; i < nodes.size(); i++) {
                            List<Double> yValues = new ArrayList<>();
                            JsonNode freqData = nodes.get(i);

                            for (JsonNode phiVal : freqData.get(0)) {
                                double value = phiVal.asDouble();
                                // 根据单位选择：转换角度为弧度
                                if ("rad".equalsIgnoreCase(unit)) {
                                    value = Math.toRadians(value);
                                }
                                yValues.add(value);
                            }

                            String freqLabel = freqList.get(i).asText();
                            String thetaLabel = thetaList.get(0).asText(); // 固定 Theta=90deg
                            String seriesName = "第二相关峰\nFreq=" + freqLabel + "MHz,Theta=" + thetaLabel + "deg";

                            Map<String, Object> seriesItem = new HashMap<>();
                            seriesItem.put("name", seriesName);
                            seriesItem.put("data", yValues);
                            series.add(seriesItem);
                        }


                    }

                    response.put("xAxis", xAxis);       // 对应 Phi 值
                    response.put("series", series);     // 曲线数据集合
                    return Result.ok(response);


                }
            } catch (JsonProcessingException e) {
                e.printStackTrace();
                return Result.fail("解析 pheno 字段失败");
            }
            return Result.ok(response);
        }else{
            return Result.fail("功能尚未包含");
        }

    }

}

