package com.anteater.memberservice.auth.entity;

public enum SubscriptionStatus { // 열거형이라서 ActivationRespose toString에서 오류가 발생했었음, 자료형 잘 확인하기 서비스 코드 수정 필요
    UNACTIVATED,
    BASIC,
    PREMIUM
}