package org.example.expert.domain.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.expert.config.AuthUserArgumentResolver;
import org.example.expert.config.WebConfig;
import org.example.expert.domain.user.dto.request.UserChangePasswordRequest;
import org.example.expert.domain.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@Import({WebConfig.class, AuthUserArgumentResolver.class})
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    private static final String URL = "/users";

    private static byte[] body(ObjectMapper om, String oldPw, String newPw) throws Exception {
        return om.writeValueAsBytes(new UserChangePasswordRequest(oldPw, newPw));
    }

    @Test
    void 새_비밀번호가_8자_미만이면_검증_단계에서_400을_반환한다() throws Exception {
        mockMvc.perform(put(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .requestAttr("userId", 1L)
                        .requestAttr("email", "user@example.com")
                        .requestAttr("userRole", "USER")
                        .content(body(objectMapper, "OldPass1", "Ab1xxx")))
                .andExpect(status().isBadRequest());

        verify(userService, never()).changePassword(org.mockito.ArgumentMatchers.anyLong(), org.mockito.ArgumentMatchers.any());
    }

    @Test
    void 새_비밀번호에_숫자가_없으면_검증_단계에서_400을_반환한다() throws Exception {
        mockMvc.perform(put(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .requestAttr("userId", 1L)
                        .requestAttr("email", "user@example.com")
                        .requestAttr("userRole", "USER")
                        .content(body(objectMapper, "OldPass1", "Abcdefghi")))
                .andExpect(status().isBadRequest());

        verify(userService, never()).changePassword(org.mockito.ArgumentMatchers.anyLong(), org.mockito.ArgumentMatchers.any());
    }

    @Test
    void 새_비밀번호에_대문자가_없으면_검증_단계에서_400을_반환한다() throws Exception {
        mockMvc.perform(put(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .requestAttr("userId", 1L)
                        .requestAttr("email", "user@example.com")
                        .requestAttr("userRole", "USER")
                        .content(body(objectMapper, "OldPass1", "abcdefg1")))
                .andExpect(status().isBadRequest());

        verify(userService, never()).changePassword(org.mockito.ArgumentMatchers.anyLong(), org.mockito.ArgumentMatchers.any());
    }

    @Test
    void 모든_조건을_만족하면_서비스_changePassword가_호출된다() throws Exception {
        mockMvc.perform(put(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .requestAttr("userId", 1L)
                        .requestAttr("email", "user@example.com")
                        .requestAttr("userRole", "USER")
                        .content(body(objectMapper, "OldPass1", "NewPass123")))
                .andExpect(status().isOk());

        verify(userService).changePassword(org.mockito.ArgumentMatchers.eq(1L), org.mockito.ArgumentMatchers.any());
    }
}
