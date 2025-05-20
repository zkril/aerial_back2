package com.zkril.aerial_back.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zkril.aerial_back.pojo.Users;
import com.zkril.aerial_back.service.UsersService;
import com.zkril.aerial_back.mapper.UsersMapper;
import org.springframework.stereotype.Service;

/**
* @author zkril
* @description 针对表【users】的数据库操作Service实现
* @createDate 2025-05-14 16:47:17
*/
@Service
public class UsersServiceImpl extends ServiceImpl<UsersMapper, Users>
    implements UsersService{

}




