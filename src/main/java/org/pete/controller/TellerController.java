package org.pete.controller;

import org.pete.model.request.UserLoginRequest;
import org.pete.model.result.UserLoginResult;
import org.pete.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/teller")
public class TellerController {

    private final UserService userService;

    public TellerController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping(path = "/login")
    public ResponseEntity<?> tellerLogin(@RequestBody UserLoginRequest userLoginRequest) {
        UserLoginResult result = userService.login(userLoginRequest);

        return switch (result) {
            case UserLoginResult.WrongPassword wrongPassword -> ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            case UserLoginResult.UserNotFound userNotFound -> ResponseEntity.notFound().build();
            default -> ResponseEntity.ok().build();
        };
    }
}
