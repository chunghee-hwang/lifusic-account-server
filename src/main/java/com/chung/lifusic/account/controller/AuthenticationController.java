package com.chung.lifusic.account.controller;

import com.chung.lifusic.account.common.exception.CustomException;
import com.chung.lifusic.account.dto.*;
import com.chung.lifusic.account.service.AuthenticationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/account")
@RequiredArgsConstructor
public class AuthenticationController {
    private final AuthenticationService authenticationService;
    // 회원 가입
    @PostMapping("/user")
    public ResponseEntity<CommonResponse> register(
            @Valid @RequestBody RegisterRequest request
    ) throws Exception {
        return ResponseEntity.ok(authenticationService.register(request));
    }

    // 로그인
    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> authenticate(
            @Valid @RequestBody AuthenticationRequest request
    ) {
        return ResponseEntity.ok(authenticationService.authenticate(request));
    }

    // 로그아웃
    @PostMapping("/logout")
    public ResponseEntity<CommonResponse> logout() throws CustomException {
        return ResponseEntity.ok(authenticationService.logout());
    }

    // 자기 자신에 대한 정보 확인
    @GetMapping("/me")
    public ResponseEntity<GetUserResponse> getUser() {
        return ResponseEntity.ok(authenticationService.getUser());
    }
}
