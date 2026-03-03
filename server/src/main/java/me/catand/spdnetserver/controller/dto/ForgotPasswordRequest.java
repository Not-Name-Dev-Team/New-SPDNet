package me.catand.spdnetserver.controller.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ForgotPasswordRequest {
    private String email;
    private String verificationCode;
    private String newPassword;
}
