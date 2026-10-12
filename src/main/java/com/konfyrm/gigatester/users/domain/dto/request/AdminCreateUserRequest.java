package com.konfyrm.gigatester.users.domain.dto.request;

import lombok.Data;

/** An admin creating an account by hand: they choose both the username and the password. */
@Data
public class AdminCreateUserRequest {
    private String username;
    private String password;
}
