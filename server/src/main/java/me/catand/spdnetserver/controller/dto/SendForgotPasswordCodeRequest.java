package me.catand.spdnetserver.controller.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SendForgotPasswordCodeRequest {
    private String email;
}
