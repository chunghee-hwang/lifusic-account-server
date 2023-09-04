package com.chung.lifusic.account.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LogoutResponse {
    private boolean success;
}
