package me.catand.spdnetserver.controller.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChangeNameRequest {
    private String currentName;
    private String password;
    private String newName;
}
