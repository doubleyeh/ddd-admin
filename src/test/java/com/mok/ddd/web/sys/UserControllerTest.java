package com.mok.ddd.web.sys;

import com.mok.ddd.application.sys.dto.user.UserDTO;
import com.mok.ddd.application.sys.dto.user.UserPasswordDTO;
import com.mok.ddd.application.sys.dto.user.UserPostDTO;
import com.mok.ddd.application.sys.dto.user.UserPutDTO;
import com.mok.ddd.application.sys.service.UserService;
import com.mok.ddd.infrastructure.security.JwtAuthenticationFilter;
import com.mok.ddd.infrastructure.security.JwtTokenProvider;
import com.mok.ddd.infrastructure.sys.security.CustomUserDetailsService;
import com.mok.ddd.web.common.GlobalExceptionHandler;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.Order;
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
@Import(value = {UserController.class, GlobalExceptionHandler.class})
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

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
    private UserService userService;

    @MockitoBean
    private AuthenticationManager authenticationManager;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    @Order(1)
    @WithMockUser(username = "root", password = "root", authorities = "user:list")
    void getById_ReturnUserDTO_WhenFound() throws Exception {
        UserDTO user = new UserDTO();
        user.setId(1L);
        user.setUsername("root");
        given(userService.getById(1L)).willReturn(user);

        mockMvc.perform(get("/api/users/1")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(org.springframework.test.web.servlet.result.MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1L))
                .andExpect(jsonPath("$.data.username").value("root"));
    }

    @Test
    @Order(2)
    @WithMockUser(username = "root", password = "root", authorities = "user:list")
    void getById_ReturnUserDTO_WhenNotFound() throws Exception {
        UserDTO user = new UserDTO();
        user.setId(1L);
        user.setUsername("root");
        given(userService.getById(1L)).willReturn(user);

        mockMvc.perform(get("/api/users/2")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("404"));
    }

    @Test
    @Order(3)
    @WithMockUser(username = "root", password = "root", authorities = "user:list")
    void findPage_ReturnUserDTOPage() throws Exception {
        UserDTO user = new UserDTO();
        user.setId(1L);
        user.setUsername("testuser");
        Page<UserDTO> userPage = new PageImpl<>(Collections.singletonList(user));

        given(userService.findPage(any(), any(Pageable.class))).willReturn(userPage);

        mockMvc.perform(get("/api/users")
                        .param("username", "test")
                        .param("page", "0")
                        .param("size", "10")
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].id").value(1L))
                .andExpect(jsonPath("$.data.content[0].username").value("testuser"))
                .andExpect(jsonPath("$.data.totalElements").value(1));
    }

    @Test
    @Order(4)
    @WithMockUser(username = "root", password = "root", authorities = "user:create")
    void save_ReturnSavedUserDTO_WhenSuccessful() throws Exception {
        UserPostDTO postDTO = new UserPostDTO();
        postDTO.setUsername("newUser");
        postDTO.setPassword("password123");
        postDTO.setState(1);
        postDTO.setNickname("New User");

        UserDTO savedUser = new UserDTO();
        savedUser.setId(3L);
        savedUser.setUsername("newUser");

        given(userService.create(any(UserPostDTO.class))).willReturn(savedUser);

        mockMvc.perform(post("/api/users")
                        .content(jsonMapper.writeValueAsString(postDTO))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(3L))
                .andExpect(jsonPath("$.data.username").value("newUser"));
    }

    @Test
    @Order(5)
    @WithMockUser(username = "root", password = "root", authorities = "user:update")
    void update_ReturnUpdatedUserDTO_WhenSuccessful() throws Exception {
        Long userId = 4L;
        UserPutDTO putDTO = new UserPutDTO();
        putDTO.setUsername("updatedUser");
        putDTO.setNickname("Updated User");
        putDTO.setState(1);

        UserDTO updatedUser = new UserDTO();
        updatedUser.setId(userId);
        updatedUser.setUsername("updatedUser");

        given(userService.updateUser(any(UserPutDTO.class))).willReturn(updatedUser);

        mockMvc.perform(put("/api/users/{id}", userId)
                        .content(jsonMapper.writeValueAsString(putDTO))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(userId))
                .andExpect(jsonPath("$.data.username").value("updatedUser"));

        verify(userService).updateUser(any(UserPutDTO.class));
    }

    @Test
    @Order(6)
    @WithMockUser(username = "root", password = "root", authorities = "user:update")
    void resetPassword_ReturnNewPassword_WhenSuccessful() throws Exception {
        Long userId = 5L;

        mockMvc.perform(put("/api/users/{id}/password", userId)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.data").exists());

        verify(userService).updatePassword(any(UserPasswordDTO.class));
    }

    @Test
    @Order(7)
    @WithMockUser(username = "root", password = "root", authorities = "user:delete")
    void deleteById_ReturnSuccess_WhenSuccessful() throws Exception {
        Long userId = 6L;

        mockMvc.perform(delete("/api/users/{id}", userId)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.data").doesNotExist());

        verify(userService).deleteById(eq(userId));
    }

    @Test
    @Order(8)
    @WithMockUser(username = "root", password = "root", authorities = "user:create")
    void save_ReturnBadRequest_WhenUsernameIsTooShort() throws Exception {
        UserPostDTO postDTO = new UserPostDTO();
        postDTO.setUsername("abc");
        postDTO.setPassword("password123");
        postDTO.setState(1);

        mockMvc.perform(post("/api/users")
                        .content(jsonMapper.writeValueAsString(postDTO))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("400"))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @Order(9)
    @WithMockUser(username = "root", password = "root", authorities = "user:create")
    void save_ReturnBadRequest_WhenStateIsNull() throws Exception {
        UserPostDTO postDTO = new UserPostDTO();
        postDTO.setUsername("validuser");
        postDTO.setPassword("password123");
        postDTO.setState(null);

        mockMvc.perform(post("/api/users")
                        .content(jsonMapper.writeValueAsString(postDTO))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("400"))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.message").value("用户状态不能为空"));
    }

    @Test
    @Order(10)
    @WithMockUser(username = "root", password = "root", authorities = "user:update")
    void update_ReturnBadRequest_WhenUsernameIsTooLong() throws Exception {
        Long userId = 4L;
        UserPutDTO putDTO = new UserPutDTO();
        putDTO.setUsername("a".repeat(51));
        putDTO.setState(1);

        mockMvc.perform(put("/api/users/{id}", userId)
                        .content(jsonMapper.writeValueAsString(putDTO))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("400"))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @Order(11)
    @WithMockUser(username = "root", password = "root", authorities = "user:update")
    void update_ReturnBadRequest_WhenStateIsNull() throws Exception {
        Long userId = 4L;
        UserPutDTO putDTO = new UserPutDTO();
        putDTO.setUsername("validname");
        putDTO.setState(null);

        mockMvc.perform(put("/api/users/{id}", userId)
                        .content(jsonMapper.writeValueAsString(putDTO))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("400"))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @Order(12)
    @WithMockUser(username = "root", password = "root", authorities = "user:create")
    void save_ReturnBadRequest_WhenPasswordIsTooShort() throws Exception {
        UserPostDTO postDTO = new UserPostDTO();
        postDTO.setUsername("validUser");
        postDTO.setPassword("12345");
        postDTO.setState(1);

        mockMvc.perform(post("/api/users")
                        .content(jsonMapper.writeValueAsString(postDTO))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("400"))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @Order(13)
    @WithMockUser(username = "root", password = "root", authorities = "user:list")
    void findPage_ReturnValidPage_WhenPassingEmptyUsernameQuery() throws Exception {
        UserDTO user = new UserDTO();
        user.setId(1L);
        user.setUsername("testuser");
        Page<UserDTO> userPage = new PageImpl<>(List.of(user));

        given(userService.findPage(any(), any(Pageable.class))).willReturn(userPage);

        mockMvc.perform(get("/api/users")
                        .param("username", "")
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content.length()").value(1));
    }

    @Test
    @Order(14)
    @WithMockUser(username = "root", password = "root", authorities = "user:list")
    void findPage_ReturnValidPage_WhenPassingEmptyNicknameQuery() throws Exception {
        UserDTO user = new UserDTO();
        user.setId(1L);
        user.setUsername("testuser");
        Page<UserDTO> userPage = new PageImpl<>(List.of(user));

        given(userService.findPage(any(), any(Pageable.class))).willReturn(userPage);

        mockMvc.perform(get("/api/users")
                        .param("nickname", "")
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content.length()").value(1));
    }
}