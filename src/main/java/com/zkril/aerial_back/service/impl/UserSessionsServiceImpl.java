package com.zkril.aerial_back.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zkril.aerial_back.pojo.UserSessions;
import com.zkril.aerial_back.service.UserSessionsService;
import com.zkril.aerial_back.mapper.UserSessionsMapper;
import org.springframework.stereotype.Service;

/**
* @author zkril
* @description 针对表【user_sessions】的数据库操作Service实现
* @createDate 2025-04-22 14:38:25
*/
@Service
public class UserSessionsServiceImpl extends ServiceImpl<UserSessionsMapper, UserSessions>
    implements UserSessionsService{

}




