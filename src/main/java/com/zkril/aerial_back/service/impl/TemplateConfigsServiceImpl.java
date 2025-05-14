package com.zkril.aerial_back.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zkril.aerial_back.pojo.TemplateConfigs;
import com.zkril.aerial_back.service.TemplateConfigsService;
import com.zkril.aerial_back.mapper.TemplateConfigsMapper;
import org.springframework.stereotype.Service;

/**
* @author zkril
* @description 针对表【template_configs(模板配置表，对应每个项目的技术参数)】的数据库操作Service实现
* @createDate 2025-05-07 18:40:30
*/
@Service
public class TemplateConfigsServiceImpl extends ServiceImpl<TemplateConfigsMapper, TemplateConfigs>
    implements TemplateConfigsService{

}




