package com.zkril.aerial_back;


import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.zkril.aerial_back.mapper.ProductsMapper;
import com.zkril.aerial_back.mapper.UserFavoriteMapper;
import com.zkril.aerial_back.pojo.Products;
import com.zkril.aerial_back.pojo.UserFavorite;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import java.util.Arrays;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
        "app.image-base-url=http://localhost/"
})
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "app.image-base-url=http://localhost/"
})
class ProductsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductsMapper productsMapper;
    @MockBean
    private UserFavoriteMapper userFavoriteMapper;

    @Test
    void getAllProducts_returnsList_success() throws Exception {
        // 准备产品列表数据
        Products product1 = new Products();
        product1.setProductId(1);
        product1.setName("Product1");
        product1.setPhoto("p1.png,p2.png");
        product1.setIntro("Intro1");
        product1.setDescp("Desc1");
        product1.setHomePhoto("home1.png");
        product1.setData1Name("A,B");
        product1.setData1Min("1,2");
        product1.setData1Normal("3,4");
        product1.setData1Max("5,6");
        product1.setData1Unit("U1,U2");
        product1.setData2Name("X,Y");
        product1.setData2Text("Xval,Yval");
        product1.setBuylink("http://buy1");

        Products product2 = new Products();
        product2.setProductId(2);
        product2.setName("Product2");
        product2.setPhoto("single.png");
        product2.setIntro("Intro2");
        product2.setDescp("Desc2");
        product2.setHomePhoto("home2.png");
        product2.setData1Name("");
        product2.setData1Min("");
        product2.setData1Normal("");
        product2.setData1Max("");
        product2.setData1Unit("");
        product2.setData2Name("");
        product2.setData2Text("");
        product2.setBuylink("");

        List<Products> productList = Arrays.asList(product1, product2);
        when(productsMapper.selectList(null)).thenReturn(productList);

        mockMvc.perform(get("/get_all_products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2))
                // 校验第一个产品数据映射
                .andExpect(jsonPath("$.data[0].id").value(1))
                .andExpect(jsonPath("$.data[0].name").value("Product1"))
                .andExpect(jsonPath("$.data[0].type").value("product"))
                .andExpect(jsonPath("$.data[0].photo[0]").value("http://localhost/p1.png"))
                .andExpect(jsonPath("$.data[0].photo[1]").value("http://localhost/p2.png"))
                .andExpect(jsonPath("$.data[0].intro").value("Intro1"))
                .andExpect(jsonPath("$.data[0].desc").value("Desc1"))
                .andExpect(jsonPath("$.data[0].homePhoto").value("http://localhost/" + product1.getHomePhoto()))
                .andExpect(jsonPath("$.data[0].data1.name[0]").value("A"))
                .andExpect(jsonPath("$.data[0].data2.text[1]").value("Yval"))
                .andExpect(jsonPath("$.data[0].buylink").value(product1.getBuylink()))
                // 校验第二个产品数据映射
                .andExpect(jsonPath("$.data[1].id").value(2))
                .andExpect(jsonPath("$.data[1].name").value("Product2"))
                .andExpect(jsonPath("$.data[1].type").value("product"))
                .andExpect(jsonPath("$.data[1].photo[0]").value("http://localhost/single.png"))
                .andExpect(jsonPath("$.data[1].intro").value("Intro2"))
                .andExpect(jsonPath("$.data[1].desc").value("Desc2"))
                .andExpect(jsonPath("$.data[1].homePhoto").value("http://localhost/" + product2.getHomePhoto()))
                .andExpect(jsonPath("$.data[1].buylink").value(product2.getBuylink()));

        verify(productsMapper).selectList(null);
    }

    @Test
    void getProductById_tokenInvalid_fail() throws Exception {
        String token = "invalidToken";
        // 模拟无效 token
        try (MockedStatic<com.zkril.aerial_back.util.JWTUtils> jwtMock = mockStatic(com.zkril.aerial_back.util.JWTUtils.class)) {
            jwtMock.when(() -> com.zkril.aerial_back.util.JWTUtils.verify(anyString())).thenReturn(null);
            mockMvc.perform(get("/get_product").param("id", "1").header("token", token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(201))
                    .andExpect(jsonPath("$.message").exists())
                    .andExpect(jsonPath("$.data").value(nullValue()));
        }
        verifyNoInteractions(productsMapper);
        verifyNoInteractions(userFavoriteMapper);
    }

    @Test
    void getProductById_productNotFound_fail() throws Exception {
        String token = "validToken";
        // 模拟 token 验证通过但产品不存在
        try (MockedStatic<com.zkril.aerial_back.util.JWTUtils> jwtMock = mockStatic(com.zkril.aerial_back.util.JWTUtils.class)) {
            DecodedJWT decoded = mock(DecodedJWT.class);
            Claim claim = mock(Claim.class);
            when(decoded.getClaim("userId")).thenReturn(claim);
            when(claim.asString()).thenReturn("10");
            jwtMock.when(() -> com.zkril.aerial_back.util.JWTUtils.verify(token)).thenReturn(decoded);
            when(productsMapper.selectById(99)).thenReturn(null);

            mockMvc.perform(get("/get_product").param("id", "99").header("token", token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(201))
                    .andExpect(jsonPath("$.message").value("失败"))
                    .andExpect(jsonPath("$.data").value("未找到产品"));

            verify(productsMapper).selectById(99);
            verify(userFavoriteMapper, never()).selectOne(any());
        }
    }

    @Test
    void getProductById_found_noFavorite_success() throws Exception {
        String token = "validToken";
        // 模拟产品存在且未收藏
        Products product = new Products();
        product.setProductId(5);
        product.setName("ProdX");
        product.setPhoto("a.jpg,b.jpg");
        product.setIntro("IntroX");
        product.setDescp("DescX");
        product.setHomePhoto("homeX.png");
        product.setData1Name("N1");
        product.setData1Min("1");
        product.setData1Normal("2");
        product.setData1Max("3");
        product.setData1Unit("U");
        product.setData2Name("DN");
        product.setData2Text("DT");
        product.setBuylink("linkX");
        when(productsMapper.selectById(5)).thenReturn(product);
        when(userFavoriteMapper.selectOne(any())).thenReturn(null);

        try (MockedStatic<com.zkril.aerial_back.util.JWTUtils> jwtMock = mockStatic(com.zkril.aerial_back.util.JWTUtils.class)) {
            DecodedJWT decoded = mock(DecodedJWT.class);
            Claim claim = mock(Claim.class);
            when(decoded.getClaim("userId")).thenReturn(claim);
            when(claim.asString()).thenReturn("8");
            jwtMock.when(() -> com.zkril.aerial_back.util.JWTUtils.verify(token)).thenReturn(decoded);

            mockMvc.perform(get("/get_product").param("id", "5").header("token", token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.id").value(5))
                    .andExpect(jsonPath("$.data.name").value("ProdX"))
                    .andExpect(jsonPath("$.data.type").value("product"))
                    .andExpect(jsonPath("$.data.photo[1]").value("http://localhost/b.jpg"))
                    .andExpect(jsonPath("$.data.intro").value("IntroX"))
                    .andExpect(jsonPath("$.data.desc").value("DescX"))
                    .andExpect(jsonPath("$.data.homePhoto").value("http://localhost/" + product.getHomePhoto()))
                    .andExpect(jsonPath("$.data.data1.name[0]").value("N1"))
                    .andExpect(jsonPath("$.data.data2.text[0]").value("DT"))
                    .andExpect(jsonPath("$.data.isStar").value(false))
                    .andExpect(jsonPath("$.data.buylink").value(product.getBuylink()));
        }

        verify(productsMapper).selectById(5);
        verify(userFavoriteMapper).selectOne(any());
    }

    @Test
    void getProductById_found_withFavorite_success() throws Exception {
        String token = "validToken";
        // 模拟产品存在且已收藏
        Products product = new Products();
        product.setProductId(6);
        product.setName("ProdY");
        product.setPhoto("");
        product.setIntro("");
        product.setDescp("");
        product.setHomePhoto("homeY.png");
        product.setData1Name("");
        product.setData1Min("");
        product.setData1Normal("");
        product.setData1Max("");
        product.setData1Unit("");
        product.setData2Name("");
        product.setData2Text("");
        product.setBuylink("");
        when(productsMapper.selectById(6)).thenReturn(product);
        when(userFavoriteMapper.selectOne(any())).thenReturn(new UserFavorite());

        try (MockedStatic<com.zkril.aerial_back.util.JWTUtils> jwtMock = mockStatic(com.zkril.aerial_back.util.JWTUtils.class)) {
            DecodedJWT decoded = mock(DecodedJWT.class);
            Claim claim = mock(Claim.class);
            when(decoded.getClaim("userId")).thenReturn(claim);
            when(claim.asString()).thenReturn("12");
            jwtMock.when(() -> com.zkril.aerial_back.util.JWTUtils.verify(token)).thenReturn(decoded);

            mockMvc.perform(get("/get_product").param("id", "6").header("token", token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.id").value(6))
                    .andExpect(jsonPath("$.data.isStar").value(true));
        }

        verify(productsMapper).selectById(6);
        verify(userFavoriteMapper).selectOne(any());
    }
}

