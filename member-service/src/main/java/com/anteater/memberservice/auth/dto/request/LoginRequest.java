package com.anteater.memberservice.auth.dto.request;

import lombok.Value;

@Value
public class LoginRequest {
    String username;
    String password;
}

//불변 객체로 만들어 로그인 정보의 안전성을 높이기


