package com.chung.lifusic.account.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.HashMap;

@Slf4j
@Component
public class CustomSocketHandler extends TextWebSocketHandler {
    private HashMap<String, WebSocketSession> sessionMap = new HashMap<>();

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) {
        // 메시지 발송
        String msg = message.getPayload();
        log.info("handleTextMessage: {}", msg);
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        // 소켓 연결
        log.info("Socket connected: {}", session.getId());
        sessionMap.put(session.getId(), session);
        log.info("sessionMap: {}", sessionMap);
        super.afterConnectionEstablished(session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        // 소켓 연결 끊어짐
        log.info("Socket disconnected: {}", session.getId());
        sessionMap.remove(session.getId());
        super.afterConnectionClosed(session, status);
    }

    private void sendMessage(WebSocketSession session, String payload) {
        log.info("sendMessage: {}", payload);
        TextMessage message = new TextMessage(payload);
        try {
            session.sendMessage(message);
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    public void sendMessageToAll(String payload) {
        sessionMap.forEach((s, webSocketSession) -> this.sendMessage(webSocketSession, payload));
    }
}
