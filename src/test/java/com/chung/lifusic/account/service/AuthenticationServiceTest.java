package com.chung.lifusic.account.service;

import com.chung.lifusic.account.common.Role;
import com.chung.lifusic.account.common.exception.CustomException;
import com.chung.lifusic.account.dto.*;
import com.chung.lifusic.account.entity.User;
import com.chung.lifusic.account.repository.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mockStatic;

@ExtendWith(MockitoExtension.class)
public class AuthenticationServiceTest {
    @InjectMocks
    private AuthenticationService authenticationService;

    @Mock
    private JwtService jwtService;

    @Mock
    private UserRepository repository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @DisplayName("중복 이메일로 회원 가입 시 실패")
    @Test
    public void registerWithAlreadyExists() {
        final String email = "test@email.com";
        // given
        RegisterRequest request = getRegisterRequest(email, "admin");

        User user = getUser(email, Role.ADMIN);
        // mocking
        given(repository.findByEmail(any()))
                .willReturn(Optional.ofNullable(user));
        // then
        Assertions.assertThrows(CustomException.class, () -> {
            // when
            authenticationService.register(request);
        });
    }

    @DisplayName("새 이메일로 회원가입 시 성공")
    @Test
    public void register() {
        final String email = "test@email.com";
        // given
        RegisterRequest request = getRegisterRequest(email, "admin");
        User user = getUser(email, Role.ADMIN);

        // mocking
        given(repository.findByEmail(any()))
                .willReturn(Optional.empty());
        given(repository.save(any())).willReturn(user);
        given(passwordEncoder.encode(any())).willReturn("abcd");

        // when
        CommonResponse result = null;
        try {
            result = authenticationService.register(request);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        // then
        Assertions.assertNotNull(result);
        Assertions.assertTrue(result.isSuccess());
    }

    @DisplayName("유효하지 않은 이메일 또는 비밀번호 입력 시 로그인 실패")
    @Test
    public void invalidAuthenticate() {
        // given
        final String email = "test@email.com";
        AuthenticationRequest request = getAuthenticationRequest(email);
        // mocking
        given(authenticationManager.authenticate(any())).willThrow(BadCredentialsException.class);

        // then
        Assertions.assertThrows(AuthenticationException.class, () -> {
            // when
            authenticationService.authenticate(request);
        });
    }

    @DisplayName("유효한 이메일, 비밀번호 입력 시 로그인 성공")
    @Test
    public void validAuthenticate() {
        // given
        final String email = "test@email.com";
        AuthenticationRequest request = getAuthenticationRequest(email);
        User user = getUser(email, Role.ADMIN);
        final String token = "generatedToken";

        // mocking
        given(authenticationManager.authenticate(any())).willReturn(new UsernamePasswordAuthenticationToken(user, request.getPassword()));
        given(repository.findByEmail(any())).willReturn(Optional.ofNullable(user));
        given(jwtService.generateToken(any())).willReturn(token);

        // when
        AuthenticationResponse response = authenticationService.authenticate(request);

        // then
        Assertions.assertEquals(token, response.getToken());
    }

    @DisplayName("컨텍스트에 인증 정보가 없으면 로그아웃 실패")
    @Test
    public void logoutFailWhenAuthNotInContext() {
        // mocking
        MockedStatic<SecurityContextHolder> holderMockedStatic = mockStatic(SecurityContextHolder.class);
        given(SecurityContextHolder.getContext()).willReturn(new SecurityContextImpl());

        // then
        Assertions.assertThrows(CustomException.class, () -> {
            // when
            authenticationService.logout();
        });

        holderMockedStatic.close();
    }

    @DisplayName("그 외 조건 시 로그아웃 성공")
    @Test
    public void logoutSuccess() {
        // given
        User user = getUser("test@email.com", Role.ADMIN);
        Authentication authentication = new UsernamePasswordAuthenticationToken(user, user.getPassword());

        // mocking
        MockedStatic<SecurityContextHolder> holderMockedStatic = mockStatic(SecurityContextHolder.class);
        given(SecurityContextHolder.getContext()).willReturn(new SecurityContextImpl(authentication));


        // when
        CommonResponse response = null;
        try {
            response = authenticationService.logout();
        } catch (Exception exception) {
            exception.printStackTrace();
        }

        // then
        Assertions.assertNotNull(response);
        Assertions.assertTrue(response.isSuccess());
        holderMockedStatic.close();
    }

    @DisplayName("유저 정보 가져오기 - 컨텍스트에 인증 정보가 없으면 null 반환")
    @Test
    public void getEmptyUserIfNotAuthenticated() {
        // given

        // mocking
        MockedStatic<SecurityContextHolder> holderMockedStatic = mockStatic(SecurityContextHolder.class);
        given(SecurityContextHolder.getContext()).willReturn(new SecurityContextImpl());

        // when
        GetUserResponse response = authenticationService.getUser();

        // then
        Assertions.assertNull(response);
        holderMockedStatic.close();
    }

    @DisplayName("유저 정보 가져오기 - 컨텍스트에 유저 principal이 없으면 null 반환")
    @Test
    public void getEmptyUserIfPrincipleNotExist() {
        // given
        Authentication authentication = new UsernamePasswordAuthenticationToken(null, "1234");

        // mocking
        MockedStatic<SecurityContextHolder> holderMockedStatic = mockStatic(SecurityContextHolder.class);
        given(SecurityContextHolder.getContext()).willReturn(new SecurityContextImpl(authentication));

        // when
        GetUserResponse response = authenticationService.getUser();

        // then
        Assertions.assertNull(response);
        holderMockedStatic.close();
    }

    @DisplayName("유저 정보 가져오기 - 그 이외 조건에서 성공")
    @Test
    public void getUserSuccess() {
        // given
        User principal = getUser("test@email.com", Role.ADMIN);
        Authentication authentication = new UsernamePasswordAuthenticationToken(principal, principal.getPassword());

        // mocking
        MockedStatic<SecurityContextHolder> holderMockedStatic = mockStatic(SecurityContextHolder.class);
        given(SecurityContextHolder.getContext()).willReturn(new SecurityContextImpl(authentication));

        // when
        GetUserResponse response = authenticationService.getUser();

        // then
        Assertions.assertNotNull(response);
        Assertions.assertNotNull(response.getId());
        Assertions.assertNotNull(response.getEmail());
        Assertions.assertNotNull(response.getName());
        Assertions.assertNotNull(response.getRole());

        holderMockedStatic.close();
    }


    private User getUser(String email, Role role) {
        return User.builder().id(1L).email(email)
                .name("test")
                .role(role)
                .password("1234")
                .build();
    }

    private RegisterRequest getRegisterRequest(String email, String role) {
        RegisterRequest request = new RegisterRequest();
        request.setEmail(email);
        request.setName("test");
        request.setPassword("1234");
        request.setRole(role);
        return request;
    }

    private AuthenticationRequest getAuthenticationRequest(String email) {
        AuthenticationRequest authenticationRequest = new AuthenticationRequest();
        authenticationRequest.setEmail(email);
        authenticationRequest.setPassword("1234");
        return authenticationRequest;
    }
}




























