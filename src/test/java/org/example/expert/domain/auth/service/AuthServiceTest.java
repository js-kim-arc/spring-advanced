package org.example.expert.domain.auth.service;

import org.example.expert.config.JwtUtil;
import org.example.expert.config.PasswordEncoder;
import org.example.expert.domain.auth.dto.request.SignupRequest;
import org.example.expert.domain.auth.dto.response.SignupResponse;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtUtil jwtUtil;
    @InjectMocks
    private AuthService authService;

    @Test
    void signup_이메일이_이미_존재하면_passwordEncoder가_호출되지_않고_예외가_발생한다() {
        // given
        SignupRequest request = new SignupRequest("dup@example.com", "password", "USER");
        given(userRepository.existsByEmail("dup@example.com")).willReturn(true);

        // when & then
        InvalidRequestException exception = assertThrows(
                InvalidRequestException.class,
                () -> authService.signup(request)
        );
        assertEquals("이미 존재하는 이메일입니다.", exception.getMessage());
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void signup_신규_이메일이면_인코딩_후_사용자가_저장되고_토큰이_반환된다() {
        // given
        SignupRequest request = new SignupRequest("new@example.com", "password", "USER");
        given(userRepository.existsByEmail("new@example.com")).willReturn(false);
        given(passwordEncoder.encode("password")).willReturn("encoded");
        given(userRepository.save(any(User.class))).willAnswer(invocation -> invocation.getArgument(0));
        given(jwtUtil.createToken(any(), anyString(), any())).willReturn("Bearer token");

        // when
        SignupResponse response = authService.signup(request);

        // then
        assertNotNull(response);
        assertEquals("Bearer token", response.getBearerToken());
        verify(passwordEncoder).encode("password");
        verify(userRepository).save(any(User.class));
    }
}
