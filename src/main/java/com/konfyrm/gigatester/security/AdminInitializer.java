package com.konfyrm.gigatester.security;

import com.konfyrm.gigatester.users.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AdminInitializer implements ApplicationRunner {

    private final GigaTesterProperties properties;
    private final UserService userService;

    @Override
    public void run(ApplicationArguments args) {
        userService.ensureAdminExists(
                properties.getAdminUsername(),
                properties.getAdminPassword()
        );
    }
}
