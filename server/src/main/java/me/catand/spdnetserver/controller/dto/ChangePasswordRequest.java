package me.catand.spdnetserver.controller.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChangePasswordRequest {
    private String name;
    private String oldPassword;
    private String newPassword;
}
