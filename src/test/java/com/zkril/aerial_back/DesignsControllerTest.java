package com.zkril.aerial_back;

import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.zkril.aerial_back.mapper.DesignsMapper;
import com.zkril.aerial_back.mapper.UserFavoriteMapper;
import com.zkril.aerial_back.pojo.Designs;
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
import static org.mockito.ArgumentMatchers.any;
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
class DesignsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DesignsMapper designsMapper;
    @MockBean
    private UserFavoriteMapper userFavoriteMapper;

    @Test
    void getAllDesigns_returnsList_success() throws Exception {
        // 准备设计数据列表
        Designs design1 = new Designs();
        design1.setDesignId(1);
        design1.setName("Design1");
        design1.setShape("Shape1");
        design1.setPhoto("img1.png,img2.png");
        design1.setIntro("Intro1");
        design1.setDescp("Desc1");
        design1.setHomePhoto("home1.png");
        design1.setData1Name("ParamA,ParamB");
        design1.setData1Min("0,1");
        design1.setData1Normal("5,6");
        design1.setData1Max("10,12");
        design1.setData1Unit("kg,cm");
        design1.setData2Name("FieldX,FieldY");
        design1.setData2Text("ValueX,ValueY");
        design1.setLink("http://example.com/design1");
        design1.setModel3d("model1.obj");
        design1.setEnhance3d("enhance1.obj");

        Designs design2 = new Designs();
        design2.setDesignId(2);
        design2.setName("Design2");
        design2.setShape("Shape2");
        design2.setPhoto("picA.png");
        design2.setIntro("Intro2");
        design2.setDescp("Desc2");
        design2.setHomePhoto("home2.png");
        design2.setData1Name("X");
        design2.setData1Min("1");
        design2.setData1Normal("2");
        design2.setData1Max("3");
        design2.setData1Unit("unit");
        design2.setData2Name("Y");
        design2.setData2Text("Z");
        design2.setLink("http://example.com/design2");
        design2.setModel3d("model2.obj");
        design2.setEnhance3d("enhance2.obj");

        List<Designs> designsList = Arrays.asList(design1, design2);
        when(designsMapper.selectList(null)).thenReturn(designsList);

        mockMvc.perform(get("/get_all_designs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2))
                // 校验第一个设计数据映射
                .andExpect(jsonPath("$.data[0].id").value(1))
                .andExpect(jsonPath("$.data[0].name").value("Design1"))
                .andExpect(jsonPath("$.data[0].type").value("design"))
                .andExpect(jsonPath("$.data[0].shape").value("Shape1"))
                .andExpect(jsonPath("$.data[0].photo[0]").value("http://localhost/img1.png"))
                .andExpect(jsonPath("$.data[0].photo[1]").value("http://localhost/img2.png"))
                .andExpect(jsonPath("$.data[0].intro").value("Intro1"))
                .andExpect(jsonPath("$.data[0].desc").value("Desc1"))
                .andExpect(jsonPath("$.data[0].homePhoto").value("http://localhost/" + design1.getHomePhoto()))
                .andExpect(jsonPath("$.data[0].data1.name[0]").value("ParamA"))
                .andExpect(jsonPath("$.data[0].data1.min[1]").value("1"))
                .andExpect(jsonPath("$.data[0].data1.unit[0]").value("kg"))
                .andExpect(jsonPath("$.data[0].data2.name[1]").value("FieldY"))
                .andExpect(jsonPath("$.data[0].data2.text[0]").value("ValueX"))
                .andExpect(jsonPath("$.data[0].link").value(design1.getLink()))
                .andExpect(jsonPath("$.data[0].['3DM']").value("http://localhost/" + design1.getModel3d()))
                .andExpect(jsonPath("$.data[0].['3DE']").value("http://localhost/" + design1.getEnhance3d()))
                // 校验第二个设计数据映射
                .andExpect(jsonPath("$.data[1].id").value(2))
                .andExpect(jsonPath("$.data[1].name").value("Design2"))
                .andExpect(jsonPath("$.data[1].type").value("design"))
                .andExpect(jsonPath("$.data[1].shape").value("Shape2"))
                .andExpect(jsonPath("$.data[1].photo[0]").value("http://localhost/picA.png"))
                .andExpect(jsonPath("$.data[1].intro").value("Intro2"))
                .andExpect(jsonPath("$.data[1].desc").value("Desc2"))
                .andExpect(jsonPath("$.data[1].homePhoto").value("http://localhost/" + design2.getHomePhoto()))
                .andExpect(jsonPath("$.data[1].data1.name[0]").value("X"))
                .andExpect(jsonPath("$.data[1].data2.text[0]").value("Z"))
                .andExpect(jsonPath("$.data[1].link").value(design2.getLink()))
                .andExpect(jsonPath("$.data[1].['3DM']").value("http://localhost/" + design2.getModel3d()))
                .andExpect(jsonPath("$.data[1].['3DE']").value("http://localhost/" + design2.getEnhance3d()));

        verify(designsMapper).selectList(null);
    }

    @Test
    void getProductById_tokenInvalid_fail() throws Exception {
        String token = "invalidToken";
        // 模拟 JWTUtils.verify 返回 null（无效 token）
        try (MockedStatic<com.zkril.aerial_back.util.JWTUtils> jwtMock = mockStatic(com.zkril.aerial_back.util.JWTUtils.class)) {
            jwtMock.when(() -> com.zkril.aerial_back.util.JWTUtils.verify(anyString())).thenReturn(null);
            mockMvc.perform(get("/get_designs").param("id", "1").header("token", token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(201))
                    .andExpect(jsonPath("$.message").exists())
                    .andExpect(jsonPath("$.data").value(nullValue()));
        }
        // 确认在 token 无效时未调用数据库查询
        verifyNoInteractions(designsMapper);
        verifyNoInteractions(userFavoriteMapper);
    }

    @Test
    void getProductById_designNotFound_fail() throws Exception {
        String token = "validToken";
        // 模拟 JWT 验证通过但设计不存在
        try (MockedStatic<com.zkril.aerial_back.util.JWTUtils> jwtMock = mockStatic(com.zkril.aerial_back.util.JWTUtils.class)) {
            DecodedJWT decoded = mock(DecodedJWT.class);
            Claim userIdClaim = mock(Claim.class);
            when(decoded.getClaim("userId")).thenReturn(userIdClaim);
            when(userIdClaim.asString()).thenReturn("5");
            jwtMock.when(() -> com.zkril.aerial_back.util.JWTUtils.verify(token)).thenReturn(decoded);
            when(designsMapper.selectById(99)).thenReturn(null);

            mockMvc.perform(get("/get_designs").param("id", "99").header("token", token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(201))
                    .andExpect(jsonPath("$.message").value("失败"))
                    .andExpect(jsonPath("$.data").value("未找到设计"));

            verify(designsMapper).selectById(99);
            verify(userFavoriteMapper, never()).selectOne(any());
        }
    }

    @Test
    void getProductById_found_noFavorite_success() throws Exception {
        String token = "validToken";
        // 模拟找到设计且未被收藏
        Designs design = new Designs();
        design.setDesignId(10);
        design.setName("DesignX");
        design.setShape("ShapeX");
        design.setPhoto("p1.jpg,p2.jpg");
        design.setIntro("IntroX");
        design.setDescp("DescX");
        design.setHomePhoto("homeX.png");
        design.setData1Name("D1");
        design.setData1Min("1");
        design.setData1Normal("2");
        design.setData1Max("3");
        design.setData1Unit("u");
        design.setData2Name("N1");
        design.setData2Text("T1");
        design.setLink("http://link");
        design.setModel3d("m3d.obj");
        design.setEnhance3d("e3d.obj");
        when(designsMapper.selectById(10)).thenReturn(design);
        when(userFavoriteMapper.selectOne(any())).thenReturn(null);

        try (MockedStatic<com.zkril.aerial_back.util.JWTUtils> jwtMock = mockStatic(com.zkril.aerial_back.util.JWTUtils.class)) {
            DecodedJWT decoded = mock(DecodedJWT.class);
            Claim userIdClaim = mock(Claim.class);
            when(decoded.getClaim("userId")).thenReturn(userIdClaim);
            when(userIdClaim.asString()).thenReturn("5");
            jwtMock.when(() -> com.zkril.aerial_back.util.JWTUtils.verify(token)).thenReturn(decoded);

            mockMvc.perform(get("/get_designs").param("id", "10").header("token", token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.id").value(10))
                    .andExpect(jsonPath("$.data.name").value("DesignX"))
                    .andExpect(jsonPath("$.data.type").value("design"))
                    .andExpect(jsonPath("$.data.shape").value("ShapeX"))
                    .andExpect(jsonPath("$.data.photo[0]").value("http://localhost/p1.jpg"))
                    .andExpect(jsonPath("$.data.intro").value("IntroX"))
                    .andExpect(jsonPath("$.data.desc").value("DescX"))
                    .andExpect(jsonPath("$.data.homePhoto").value("http://localhost/" + design.getHomePhoto()))
                    .andExpect(jsonPath("$.data.data1.name[0]").value("D1"))
                    .andExpect(jsonPath("$.data.data2.text[0]").value("T1"))
                    .andExpect(jsonPath("$.data.isStar").value(false))
                    .andExpect(jsonPath("$.data.link").value(design.getLink()))
                    .andExpect(jsonPath("$.data.['3DM']").value("http://localhost/" + design.getModel3d()))
                    .andExpect(jsonPath("$.data.['3DE']").value("http://localhost/" + design.getEnhance3d()));
        }

        verify(designsMapper).selectById(10);
        verify(userFavoriteMapper).selectOne(any());
    }

    @Test
    void getProductById_found_withFavorite_success() throws Exception {
        String token = "validToken";
        // 模拟找到设计且已被用户收藏
        Designs design = new Designs();
        design.setDesignId(11);
        design.setName("DesignY");
        design.setPhoto("");
        design.setIntro("IntroY");
        design.setDescp("DescY");
        design.setHomePhoto("homeY.png");
        design.setData1Name("");
        design.setData1Min("");
        design.setData1Normal("");
        design.setData1Max("");
        design.setData1Unit("");
        design.setData2Name("");
        design.setData2Text("");
        design.setShape("ShapeY");
        design.setLink("");
        design.setModel3d("modelY.obj");
        design.setEnhance3d("enhanceY.obj");
        when(designsMapper.selectById(11)).thenReturn(design);
        when(userFavoriteMapper.selectOne(any())).thenReturn(new UserFavorite());

        try (MockedStatic<com.zkril.aerial_back.util.JWTUtils> jwtMock = mockStatic(com.zkril.aerial_back.util.JWTUtils.class)) {
            DecodedJWT decoded = mock(DecodedJWT.class);
            Claim claim = mock(Claim.class);
            when(decoded.getClaim("userId")).thenReturn(claim);
            when(claim.asString()).thenReturn("5");
            jwtMock.when(() -> com.zkril.aerial_back.util.JWTUtils.verify(token)).thenReturn(decoded);

            mockMvc.perform(get("/get_designs").param("id", "11").header("token", token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.id").value(11))
                    .andExpect(jsonPath("$.data.isStar").value(true));
        }

        verify(designsMapper).selectById(11);
        verify(userFavoriteMapper).selectOne(any());
    }
}

