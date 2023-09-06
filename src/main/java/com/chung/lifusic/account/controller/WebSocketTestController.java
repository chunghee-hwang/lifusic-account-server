package com.chung.lifusic.account.controller;

import com.chung.lifusic.account.service.CustomSocketHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
@Slf4j
public class WebSocketTestController {
    private final CustomSocketHandler socketHandler;
    @GetMapping("/hello")
    public ResponseEntity<String> hello() {
        log.info("hello from web socket controller");
        socketHandler.sendMessageToAll("hello!!");
        return ResponseEntity.ok("OK!");
    }
}
