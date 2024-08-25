package com.anteater.apigateway.jwt;

class TokenResponse {
    private String accessToken;

    // constructor, getter and setter
    public TokenResponse() {}

    public TokenResponse(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }
}