package org.pete.controller;

import org.pete.model.request.UserLoginRequest;
import org.pete.model.request.RegisterCustomerRequest;
import org.pete.model.response.RegisterCustomerResponse;
import org.pete.model.result.UserLoginResult;
import org.pete.model.result.RegisterCustomerResult;
import org.pete.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/customer")
public class CustomerController {

    private final UserService userService;

    public CustomerController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping(path = "/register")
    public ResponseEntity<RegisterCustomerResponse> registerCustomer(@RequestBody RegisterCustomerRequest registerCustomerRequest) {
        RegisterCustomerResult result = userService.registerCustomer(registerCustomerRequest);
        return switch (result) {
            case RegisterCustomerResult.ValidationFails vf ->
                    ResponseEntity
                            .badRequest().
                            body(
                                    new RegisterCustomerResponse(false, vf.getMessage())
                            );
            case RegisterCustomerResult.CustAlreadyExists ce->
                    ResponseEntity
                            .ok(
                                    new RegisterCustomerResponse(false, null)
                            );
            default -> ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(
                            new RegisterCustomerResponse(true, null)
                    );
        };
    }

    @PostMapping(path = "/login")
    public ResponseEntity<?> customerLogin(@RequestBody UserLoginRequest userLoginRequest) {
        UserLoginResult result = userService.login(userLoginRequest);

        return switch (result) {
            case UserLoginResult.WrongPassword wrongPassword -> ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            case UserLoginResult.UserNotFound userNotFound -> ResponseEntity.notFound().build();
            default -> ResponseEntity.ok().build();
        };
    }
}
