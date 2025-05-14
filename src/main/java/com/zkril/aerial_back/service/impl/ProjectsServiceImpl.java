package com.zkril.aerial_back.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zkril.aerial_back.pojo.Projects;
import com.zkril.aerial_back.service.ProjectsService;
import com.zkril.aerial_back.mapper.ProjectsMapper;
import org.springframework.stereotype.Service;

/**
* @author zkril
* @description 针对表【projects(用户项目表)】的数据库操作Service实现
* @createDate 2025-05-05 14:40:30
*/
@Service
public class ProjectsServiceImpl extends ServiceImpl<ProjectsMapper, Projects>
    implements ProjectsService{

}




