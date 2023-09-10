package com.chung.lifusic.account.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GetUserResponse {
    private Long id;
    private String email;
    private String name;
    private String role;
}
