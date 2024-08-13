package com.anteater.memberservice.auth.dto.request;
import lombok.Value;

import java.time.LocalDate;

@Value
public class RegisterRequest {
    String username;
    String email;
    String password;
    String name;
    LocalDate birthdate;
}
