package com.zkril.aerial_back.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zkril.aerial_back.pojo.Products;
import com.zkril.aerial_back.service.ProductsService;
import com.zkril.aerial_back.mapper.ProductsMapper;
import org.springframework.stereotype.Service;

/**
* @author zkril
* @description 针对表【products】的数据库操作Service实现
* @createDate 2025-04-16 20:13:16
*/
@Service
public class ProductsServiceImpl extends ServiceImpl<ProductsMapper, Products>
    implements ProductsService{

}




