package com.mok.ddd.web.sys;

import com.mok.ddd.application.sys.dto.dict.*;
import com.mok.ddd.application.sys.service.DictService;
import com.mok.ddd.infrastructure.security.JwtAuthenticationFilter;
import com.mok.ddd.infrastructure.security.JwtTokenProvider;
import com.mok.ddd.infrastructure.sys.security.CustomUserDetailsService;
import com.mok.ddd.web.common.GlobalExceptionHandler;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import tools.jackson.databind.json.JsonMapper;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
@Import(value = {DictController.class, GlobalExceptionHandler.class})
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("字典管理控制器测试")
class DictControllerTest {

    @Configuration
    static class TestConfig {
        @Bean
        EntityManagerFactory entityManagerFactory() {
            return mock(EntityManagerFactory.class);
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JsonMapper jsonMapper;

    @MockitoBean
    private DictService dictService;

    @MockitoBean
    private AuthenticationManager authenticationManager;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    @WithMockUser(username = "root", authorities = "dict:list")
    @DisplayName("分页查询字典类型")
    void findTypePage_ShouldReturnPage_WhenAuthorized() throws Exception {
        DictTypeDTO dto = new DictTypeDTO();
        dto.setId(1L);
        dto.setName("Test Dict");
        dto.setCode("test_dict");
        Page<DictTypeDTO> page = new PageImpl<>(Collections.singletonList(dto));

        given(dictService.findPage(any(), any(Pageable.class))).willReturn(page);

        mockMvc.perform(get("/api/dict/type")
                        .param("name", "Test")
                        .param("page", "0")
                        .param("size", "10")
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].id").value(1L))
                .andExpect(jsonPath("$.data.content[0].code").value("test_dict"));
    }

    @Test
    @WithMockUser(username = "root", authorities = "dict:create")
    @DisplayName("创建字典类型")
    void createType_ShouldReturnDto_WhenAuthorized() throws Exception {
        DictTypeSaveDTO saveDTO = new DictTypeSaveDTO();
        saveDTO.setName("New Dict");
        saveDTO.setCode("new_dict");

        DictTypeDTO resultDTO = new DictTypeDTO();
        resultDTO.setId(2L);
        resultDTO.setName("New Dict");
        resultDTO.setCode("new_dict");

        given(dictService.createType(any(DictTypeSaveDTO.class))).willReturn(resultDTO);

        mockMvc.perform(post("/api/dict/type")
                        .content(jsonMapper.writeValueAsString(saveDTO))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(2L))
                .andExpect(jsonPath("$.data.code").value("new_dict"));
    }

    @Test
    @WithMockUser(username = "root", authorities = "dict:update")
    @DisplayName("更新字典类型")
    void updateType_ShouldReturnDto_WhenAuthorized() throws Exception {
        DictTypeSaveDTO saveDTO = new DictTypeSaveDTO();
        saveDTO.setId(1L);
        saveDTO.setName("Updated Dict");
        saveDTO.setCode("updated_dict");

        DictTypeDTO resultDTO = new DictTypeDTO();
        resultDTO.setId(1L);
        resultDTO.setName("Updated Dict");

        given(dictService.updateType(any(DictTypeSaveDTO.class))).willReturn(resultDTO);

        mockMvc.perform(put("/api/dict/type")
                        .content(jsonMapper.writeValueAsString(saveDTO))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1L))
                .andExpect(jsonPath("$.data.name").value("Updated Dict"));
    }

    @Test
    @WithMockUser(username = "root", authorities = "dict:delete")
    @DisplayName("删除字典类型")
    void deleteType_ShouldReturnSuccess_WhenAuthorized() throws Exception {
        Long id = 1L;

        mockMvc.perform(delete("/api/dict/type/{id}", id)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"));

        verify(dictService).deleteType(eq(id));
    }

    @Test
    @WithMockUser(username = "user")
    @DisplayName("根据类型编码获取字典数据(公共接口)")
    void getDataByType_ShouldReturnList_WhenAuthenticated() throws Exception {
        DictDataDTO dto = new DictDataDTO();
        dto.setLabel("Option 1");
        dto.setValue("1");
        List<DictDataDTO> list = Collections.singletonList(dto);

        given(dictService.getDataByType("test_type")).willReturn(list);

        mockMvc.perform(get("/api/dict/data/test_type")
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].label").value("Option 1"))
                .andExpect(jsonPath("$.data[0].value").value("1"));
    }

    @Test
    @WithMockUser(username = "root", authorities = "dict:create")
    @DisplayName("创建字典数据")
    void createData_ShouldReturnDto_WhenAuthorized() throws Exception {
        DictDataSaveDTO saveDTO = new DictDataSaveDTO();
        saveDTO.setTypeCode("test_type");
        saveDTO.setLabel("New Option");
        saveDTO.setValue("new_val");

        DictDataDTO resultDTO = new DictDataDTO();
        resultDTO.setId(10L);
        resultDTO.setLabel("New Option");

        given(dictService.createData(any(DictDataSaveDTO.class))).willReturn(resultDTO);

        mockMvc.perform(post("/api/dict/data")
                        .content(jsonMapper.writeValueAsString(saveDTO))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(10L))
                .andExpect(jsonPath("$.data.label").value("New Option"));
    }

    @Test
    @WithMockUser(username = "root", authorities = "dict:update")
    @DisplayName("更新字典数据")
    void updateData_ShouldReturnDto_WhenAuthorized() throws Exception {
        DictDataSaveDTO saveDTO = new DictDataSaveDTO();
        saveDTO.setId(10L);
        saveDTO.setTypeCode("test_type");
        saveDTO.setLabel("Updated Option");
        saveDTO.setValue("updated_val");

        DictDataDTO resultDTO = new DictDataDTO();
        resultDTO.setId(10L);
        resultDTO.setLabel("Updated Option");

        given(dictService.updateData(any(DictDataSaveDTO.class))).willReturn(resultDTO);

        mockMvc.perform(put("/api/dict/data")
                        .content(jsonMapper.writeValueAsString(saveDTO))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(10L))
                .andExpect(jsonPath("$.data.label").value("Updated Option"));
    }

    @Test
    @WithMockUser(username = "root", authorities = "dict:delete")
    @DisplayName("删除字典数据")
    void deleteData_ShouldReturnSuccess_WhenAuthorized() throws Exception {
        Long id = 10L;

        mockMvc.perform(delete("/api/dict/data/{id}", id)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"));

        verify(dictService).deleteData(eq(id));
    }
}
