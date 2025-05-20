package com.zkril.aerial_back.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zkril.aerial_back.pojo.ProductComments;
import com.zkril.aerial_back.service.ProductCommentsService;
import com.zkril.aerial_back.mapper.ProductCommentsMapper;
import org.springframework.stereotype.Service;

/**
* @author zkril
* @description 针对表【product_comments】的数据库操作Service实现
* @createDate 2025-05-16 17:58:08
*/
@Service
public class ProductCommentsServiceImpl extends ServiceImpl<ProductCommentsMapper, ProductComments>
    implements ProductCommentsService{

}




