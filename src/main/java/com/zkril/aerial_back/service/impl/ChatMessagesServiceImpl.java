package com.zkril.aerial_back.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zkril.aerial_back.pojo.ChatMessages;
import com.zkril.aerial_back.service.ChatMessagesService;
import com.zkril.aerial_back.mapper.ChatMessagesMapper;
import org.springframework.stereotype.Service;

/**
* @author zkril
* @description 针对表【chat_messages】的数据库操作Service实现
* @createDate 2025-04-19 15:39:07
*/
@Service
public class ChatMessagesServiceImpl extends ServiceImpl<ChatMessagesMapper, ChatMessages>
    implements ChatMessagesService{

}




