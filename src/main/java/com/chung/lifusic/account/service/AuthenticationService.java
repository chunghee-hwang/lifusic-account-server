package com.chung.lifusic.account.service;

import com.chung.lifusic.account.common.Constants;
import com.chung.lifusic.account.common.exception.CustomException;
import com.chung.lifusic.account.dto.AuthenticationRequest;
import com.chung.lifusic.account.dto.AuthenticationResponse;
import com.chung.lifusic.account.dto.LogoutResponse;
import com.chung.lifusic.account.dto.RegisterRequest;
import com.chung.lifusic.account.entity.Role;
import com.chung.lifusic.account.entity.User;
import com.chung.lifusic.account.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationService{
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    public AuthenticationResponse register(RegisterRequest request) throws Exception {
        User prevUser = userRepository.findByEmail(request.getEmail()).orElseGet(() -> null);
        if (prevUser != null) {
            throw new CustomException(Constants.ExceptionType.AUTHENTICATION, HttpStatus.BAD_REQUEST, "User already Exists");
        }
        // 회원가입을 위해 유저를 db에 등록
        User user = User.builder()
                .firstname(request.getFirstname())
                .lastname(request.getLastname())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword())) // 비밀번호 인코딩
                .role(Role.USER)
                .build();
        userRepository.save(user);
        String jwtToken = jwtService.generateToken(user);
        return AuthenticationResponse.builder()
                .token(jwtToken)
                .build();
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        // 인증 시도. 인증에 실패하면 AuthenticationError 반환됨
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        // 인증 성공 시
        User user = userRepository.findByEmail(request.getEmail()).orElseThrow();
        String jwtToken = jwtService.generateToken(user);
        return AuthenticationResponse.builder()
                .token(jwtToken)
                .build();
    }

    // 로그아웃
    public LogoutResponse logout() throws CustomException {
        final CustomException exception = new CustomException(Constants.ExceptionType.AUTHENTICATION, HttpStatus.NOT_FOUND, "Error occurred");
        // Context에 저장되어있는 사용자 정보를 꺼낸다
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            throw exception;
        }
        Object principal = authentication.getPrincipal();
        // principal이 "anonymous"로 넘어올 때가 있어서 예외처리
        if (!(principal instanceof User user)) {
            throw exception;
        }
        jwtService.expireToken(user.getEmail());

        return LogoutResponse.builder()
                .success(true)
                .build();
    }


}
