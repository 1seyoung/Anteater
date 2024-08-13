package com.anteater.memberservice.auth.dto.request;

import lombok.Value;

@Value
public class LogoutRequest {
    boolean allDevices;
}
//단일 속성을 가진 불변 객체로 만들어 로그아웃 요청을 나타냄