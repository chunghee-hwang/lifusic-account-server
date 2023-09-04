package com.chung.lifusic.account.controller;

import com.chung.lifusic.account.common.exception.CustomException;
import com.chung.lifusic.account.dto.AuthenticationRequest;
import com.chung.lifusic.account.dto.AuthenticationResponse;
import com.chung.lifusic.account.dto.LogoutResponse;
import com.chung.lifusic.account.dto.RegisterRequest;
import com.chung.lifusic.account.service.AuthenticationService;
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
    public ResponseEntity<AuthenticationResponse> register(
            @RequestBody RegisterRequest request
    ) throws Exception {
        return ResponseEntity.ok(authenticationService.register(request));
    }

    // 로그인
    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> authenticate(
            @RequestBody AuthenticationRequest request
    ) {
        return ResponseEntity.ok(authenticationService.authenticate(request));
    }

    @PostMapping("/logout")
    public ResponseEntity<LogoutResponse> logout() throws CustomException {
        return ResponseEntity.ok(authenticationService.logout());
    }
}
